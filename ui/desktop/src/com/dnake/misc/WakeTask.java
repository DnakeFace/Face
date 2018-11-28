package com.dnake.misc;

import android.content.Context;
import android.os.PowerManager;

public class WakeTask {
	private static long mTs = 0;
	private static int mTimeout = 30*1000;
	private static PowerManager mPowerM = null;

	public static void onCreate(Context ctx) {
		if (mPowerM == null)
			mPowerM = (PowerManager)ctx.getSystemService(Context.POWER_SERVICE);
	}

	public static Boolean isScreenOn() {
		if (mPowerM == null)
			return false;
		return mPowerM.isScreenOn();
	}

	public static void acquire() {
		if (mPowerM == null)
			return;
		mTs = System.currentTimeMillis();
	}

	public static void release() {
		mTs = 0;
	}

	public static Boolean timeout() {
		if (Math.abs(System.currentTimeMillis()-mTs) < mTimeout)
			return false;
		return true;
	}
}
