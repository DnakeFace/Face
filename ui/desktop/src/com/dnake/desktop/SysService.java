package com.dnake.desktop;

import com.dnake.misc.SysSpecial;
import com.dnake.misc.WakeTask;
import com.dnake.v700.dmsg;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class SysService extends Service {

	public static Context mContext = null;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mContext = this;

		dmsg.setup_port();
		WakeTask.onCreate(this);
		SysSpecial.load();

		if (android.os.Build.VERSION.SDK_INT >= 19) {
			// android4.4 service会延迟启动，先由桌面提前启动APK Service
			BackgroundThread pt = new BackgroundThread();
			Thread t = new Thread(pt);
			t.start();
		}
	}

	public static class BackgroundThread implements Runnable {
		@Override
		public void run() {
			this.start("com.dnake.eSettings", "com.dnake.v700.settings");
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
			this.start("com.dnake.panel", "com.dnake.misc.SysTalk");
		}

		private void start(String apk, String name) {
			try {
				Intent it = new Intent();
				it.setComponent(new ComponentName(apk, name));
				mContext.startService(it);
			} catch (RuntimeException e) {
			}
		}
	}
}
