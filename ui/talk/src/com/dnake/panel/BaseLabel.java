package com.dnake.panel;

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

	private BaseLabel mBaseCtx = this;

	private Handler mTimer = null;
	protected Boolean mAutoFinish = true;

	private Thread mThread = null;
	protected Boolean mRunning = true;

	public int mPeriod = 100;

	public class ProcessThread implements Runnable {
		@Override
		public void run() {
			while(mRunning) {
				try {
					Thread.sleep(mPeriod);
				} catch (InterruptedException e) {
				}
				if (WakeTask.isScreenOn()) {
					synchronized(mBaseCtx) {
						if (mTimer != null) {
							mTimer.sendMessage(mTimer.obtainMessage());
						}
					}
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
	}

	@Override
	public void onStop() {
		super.onStop();

		synchronized(mBaseCtx) {
			mTimer = null;
			this.tStop();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		synchronized(mBaseCtx) {
			if (mAutoFinish && WakeTask.timeout()) {
				this.tStop();
				finish();
			} else {
				this.setup();
			}
		}
	}

	private void tStart() {
		mRunning = true;
		if (mThread == null) {
			ProcessThread pt = new ProcessThread();
			mThread = new Thread(pt);
			mThread.start();
		}
	}

	protected void tStop() {
		mRunning = false;
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
		mTimerWDT = 0;
	}

	private void setup() {
		WakeTask.acquire();

		if (mTimer == null) {
			mTimer = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);

					if (isFinishing())
						return;

					mTimerWDT = System.currentTimeMillis();

					synchronized(mBaseCtx) {
						onTimer();
					}

					if (SysTalk.Keys.size() > 0) {
						mKeyTs = System.currentTimeMillis();
						String key = SysTalk.Keys.poll();
						if (key != null) {
							synchronized(mBaseCtx) {
								onKey(key);
							}
						}
					}

					if (mAutoFinish && WakeTask.timeout()) {
						tStop();
						if (!isFinishing())
							finish();
					}
				}
			};
		}
		this.tStart();
	}
}
