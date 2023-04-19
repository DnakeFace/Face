package com.dnake.desktop;

import com.dnake.misc.WakeTask;
import com.dnake.v700.sys;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;

@SuppressLint("HandlerLeak")
public class BaseLabel extends Activity {
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

		Window w = this.getWindow();
		WindowManager.LayoutParams lp = w.getAttributes();
		lp.screenBrightness = 1;
		w.setAttributes(lp);
	}

	@Override
	public void onStart() {
		super.onStart();
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

					onTimer();

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
		} else {
			this.tStart();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (bFinish && WakeTask.timeout()) {
			this.tStop();
			finish();
		} else {
			this.tStart();
		}
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
	}

	public int orientation() {
		WindowManager wm = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE));
		return wm.getDefaultDisplay().getRotation();
	}
}
