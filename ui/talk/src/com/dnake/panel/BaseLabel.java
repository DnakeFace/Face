package com.dnake.panel;

import com.dnake.misc.Sound;
import com.dnake.misc.SysTalk;
import com.dnake.v700.sys;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

@SuppressLint("HandlerLeak")
public class BaseLabel extends Activity {
	public static long mKeyTs = System.currentTimeMillis();
	public static long mTimerWDT = 0;

	private Handler e_timer = null;
	protected Boolean bFinish = true;

	private Thread bThread = null;
	protected Boolean bRun = true;

	public int tPeriod = 100;

	public class ProcessThread implements Runnable {
		@Override
		public void run() {
			while(bRun) {
				try {
					Thread.sleep(tPeriod);
				} catch (InterruptedException e) {
				}
				if (WakeTask.isScreenOn()) {
					if (e_timer != null)
						e_timer.sendMessage(e_timer.obtainMessage());
				}
			}
		}
	}

	public void onTimer() {
	}

	public void onKey(String key) {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Resources r = this.getResources();
		sys.scaled = r.getDisplayMetrics().density;

		Sound.load();

		//页面亮度设置成最亮
		Window w = this.getWindow();
		WindowManager.LayoutParams lp = w.getAttributes();
		lp.screenBrightness = 1;
		w.setAttributes(lp);
	}

	@Override
	public void onStart() {
		super.onStart();
		SysTalk.Keys.clear();

		this.setup();
	}

	private void setup() {
		WakeTask.acquire();

		if (e_timer == null) {
			e_timer = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);

					if (isFinishing())
						return;

					mTimerWDT = System.currentTimeMillis();
					onTimer();

					if (SysTalk.Keys.size() > 0) {
						mKeyTs = System.currentTimeMillis();
						String s = SysTalk.Keys.poll();
						if (s != null)
							onKey(s);
					}

					if (bFinish && WakeTask.timeout()) {
						tStop();
						if (!isFinishing())
							finish();
					}
				}
			};
		}
		this.tStart();
	}

	@Override
    public void onStop() {
		super.onStop();

		e_timer = null;
		this.tStop();
	}

	@Override
	public void onRestart() {
		super.onRestart();

		if (bFinish && WakeTask.timeout()) {
			this.tStop();
			finish();
		} else
			this.tStart();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (bFinish && WakeTask.timeout()) {
			this.tStop();
			finish();
		}else
			this.tStart();
	}

	private void tStart() {
		bRun = true;
		if (bThread == null) {
			ProcessThread pt = new ProcessThread();
			bThread = new Thread(pt);
			bThread.start();
		}
	}

	protected void tStop() {
		bRun = false;
		if (bThread != null) {
			bThread.interrupt();
			bThread = null;
		}
		mTimerWDT = 0;
	}
}
