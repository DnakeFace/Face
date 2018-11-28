package com.dnake.panel;

import com.dnake.v700.dmsg;
import com.dnake.v700.sys;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

@SuppressWarnings("deprecation")
@SuppressLint("Wakelock")
public class WakeTask {
	private static long mTs = 0;
	private static int mTimeout = 30*1000;

	private static WakeLock wLock = null;
	private static PowerManager mPowerM = null;
	private static KeyguardLock kLock = null;
	private static Thread mThread = null;

	public static void onCreate(Context ctx) {
		if (mPowerM == null)
			mPowerM = (PowerManager)ctx.getSystemService(Context.POWER_SERVICE);

		if (kLock == null) {
			kLock = ((KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE)).newKeyguardLock("CQC");
			kLock.disableKeyguard();
		}

		if (mThread == null) {
			ProcessThread pt = new ProcessThread();
			mThread = new Thread(pt);
			mThread.start();
		}
	}

	public static void onDestroy() {
		if (wLock != null) {
			wLock.release();
			wLock = null;
		}
		if (kLock != null) {
			kLock.reenableKeyguard();
			kLock = null;
		}
	}

	public static void acquire() {
		if (mPowerM == null)
			return;

		synchronized (mThread) {
			if (wLock == null) {
				wLock = mPowerM.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "v700_panel");
				wLock.acquire();
			}
			mTs = System.currentTimeMillis();
		}
	}

	public static void release() {
		synchronized (mThread) {
			if (wLock != null) {
				wLock.release();
				wLock = null;
				dmsg req = new dmsg();
				req.to("/face/stop", null);
			}
			mTs = 0;
		}
	}

	public static Boolean timeout() {
		if (Math.abs(System.currentTimeMillis()-mTs) < mTimeout)
			return false;
		return true;
	}

	public static Boolean isScreenOn() {
		if (mPowerM == null)
			return false;
		return mPowerM.isScreenOn();
	}

	public static class ProcessThread implements Runnable {
		@Override
		public void run() {
			while(true) {
				if ((!mPowerM.isScreenOn() && wLock != null) || (mTs != 0 && sys.ts(mTs) >= 60*1000))
					WakeTask.release();

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
