package com.dnake.panel;

import com.dnake.misc.SysTalk;
import com.dnake.misc.sCaller;
import com.dnake.misc.Sound;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

@SuppressLint({ "DefaultLocale", "SetJavaScriptEnabled", "NewApi" })
public class TalkLabel extends BaseLabel {
	public static Intent mIntent = null;
	public static TalkLabel mContext = null;

	public static int build = 0;
	public static int unit = 0;
	public static int id;

	public static int OUT = 0;
	public static int IN = 1;
	public static int mMode = 0; // 0:呼出, 1:呼入

	private MediaPlayer mPlayer = null;

	private TextView mText;
	private long mAutoTs = 0;
	private long mEndTs = 0;
	private String mPstnDtmf = null; // 语音网关拨号DTMF

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.talk);

		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mContext = this;
		mPeriod = 300;

		mText = (TextView) this.findViewById(R.id.prompt_text);

		if (mMode == OUT) {
			mText.setText(R.string.sCaller_call);
		} else if (mMode == IN) {
			mText.setText(R.string.sCaller_monitor);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		mContext = this;
		this.onStart2();
	}

	public void onStart2() {
		sCaller.refresh();

		if (mMode == OUT) {
			String s;
			if (id == 0) { // 呼叫管理机，使用固定ID
				id = 10001;
				s = "10001";
			} else {
				s = String.format("%d%02d%04d", build, unit, id);
			}

			sCaller.query(s);

			OnCompletionListener listener = new OnCompletionListener() {
				public void onCompletion(MediaPlayer p) {
					p.stop();
					p.reset();
					p.release();
					mPlayer = null;
				}
			};
			mPlayer = Sound.play(Sound.ringback, true, listener);
		} else {
			mAutoTs = System.currentTimeMillis();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (sCaller.running != sCaller.NONE)
			this.stop(true);
		mIntent = null;
		mAutoTs = 0;
		mContext = null;
	}

	@Override
	public void onTimer() {
		super.onTimer();

		if (this.isFinishing())
			return;

		WakeTask.acquire();

		if (this.getWindow().getDecorView().getSystemUiVisibility() != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
			this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}

		if (mMode == OUT) {// 呼叫
			if (sCaller.running == sCaller.QUERY) {
				if (sys.qResult.sip.url != null) {
					sCaller.start(sys.qResult.sip.url);
					sCaller.logger(sCaller.logger.CALL);
					sys.qResult.sip.url = null;
				} else if (sys.qResult.d600.ip != null) {
					System.out.println("ip: " + sys.qResult.d600.ip);
					sCaller.s600(sys.qResult.d600.host, sys.qResult.d600.ip);
				} else if (sCaller.timeout() >= sCaller.Time.QUERY)
					this.forward();
			} else if (sCaller.running == sCaller.CALL) {
				if (sCaller.timeout() >= sCaller.Time.CALL || sys.qResult.result >= 400)
					this.forward();
			} else if (sCaller.running == sCaller.RINGING) {
				// 有转呼25秒超时，其他按系统设置
				int timeout = sys.panel.ringing * 1000;
				if (sCaller.timeout() >= timeout) { // 振铃超时
					if (!this.forward()) { // 转呼失败
						mText.setText(R.string.sCaller_no_answer);
					}
				}
			} else if (sCaller.running == sCaller.TALK) {
				if (sCaller.timeout() >= sys.talk.timeout * 1000) {// 对讲结束
					mText.setText(R.string.sCaller_finish);
					this.stop(true);
				}

				if (mPstnDtmf != null && sCaller.timeout() > 1000) {
					char d = mPstnDtmf.charAt(0);
					mPstnDtmf = mPstnDtmf.substring(1);
					if (mPstnDtmf.length() <= 0)
						mPstnDtmf = null;
					dxml p = new dxml();
					dmsg req = new dmsg();
					p.setText("/params/dtmf", String.valueOf(d));
					req.to("/talk/send_dtmf", p.toString());
				}
			}
		} else if (mMode == IN) {// 监视
			if (mAutoTs != 0 && Math.abs(System.currentTimeMillis() - mAutoTs) > 500) {
				mAutoTs = 0;
				sCaller.running = sCaller.TALK;
				dmsg req = new dmsg();
				req.to("/talk/start", null);
			}
			if (sCaller.timeout() >= sCaller.Time.MONITOR) {// 监视超时
				this.stop(true);
				if (!this.isFinishing())
					this.finish();
			}
		}

		if (mEndTs != 0) {
			if (Math.abs(System.currentTimeMillis() - mEndTs) >= 1000) {
				if (!this.isFinishing())
					this.finish();
				mEndTs = 0;
			}
		} else {
			if (sCaller.bStop) { // 挂断
				this.stop(true);
				mText.setText(R.string.sCaller_finish);
			}
		}
	}

	@Override
	public void onKey(String key) {
		super.onKey(key);
		if (key.charAt(0) == '*' || key.charAt(0) == 'X') {
			this.stop(true);
			if (!this.isFinishing())
				this.finish();
		}
	}

	private Boolean forward() {
		mText.setText(R.string.sCaller_call_err);
		Sound.play(Sound.call_err, false);
		this.stop(true);
		return false;
	}

	public void play() {
		if (mPlayer != null) {
			Sound.stop(mPlayer);
			mPlayer = null;
		}
		mText.setText(R.string.sCaller_talk);
		sCaller.logger(sCaller.logger.ANSWER);
	}

	public void stop(Boolean m) {
		if (mMode == OUT) {
			if (sCaller.running == sCaller.RINGING)
				sCaller.logger(sCaller.logger.FAILED);
			else if (sCaller.running == sCaller.TALK)
				sCaller.logger(sCaller.logger.END);
		}

		dmsg req = new dmsg();
		req.to("/talk/stop", null);
		sCaller.reset();

		if (m) {
			if (mPlayer != null) {
				Sound.stop(mPlayer);
				mPlayer = null;
			}
			mEndTs = System.currentTimeMillis();
		} else
			mEndTs = 0;
	}

	public static void start(int b, int u, int r) {
		build = b;
		unit = u;
		id = r;
		mMode = OUT;
		SysTalk.start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true;
		return super.onKeyDown(keyCode, event);
	}
}
