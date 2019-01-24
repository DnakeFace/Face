package com.dnake.misc;

import java.util.LinkedList;
import java.util.Queue;

import com.dnake.panel.TalkLabel;
import com.dnake.panel.WakeTask;
import com.dnake.v700.devent;
import com.dnake.v700.dmsg;
import com.dnake.v700.eDhcp;
import com.dnake.v700.sLocale;
import com.dnake.v700.sys;
import com.dnake.v700.utils;
import com.dnake.v700.vt_uart;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

@SuppressLint({ "HandlerLeak", "NewApi" })
public class SysTalk extends Service {

	public static Context mContext = null;
	public static boolean mBootEnd = false;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public static Queue<String> Keys = new LinkedList<String>();

	private static Handler e_touch = new Handler() {
		private MediaPlayer mPlayer = null;

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			WakeTask.acquire();
			for(int i=0; i<3; i++) {
				if (WakeTask.isScreenOn())
					break;
				try {
					Thread.sleep(800);
				} catch (InterruptedException e) {
				}
			}
			if (mPlayer != null) {
				mPlayer.release();
				mPlayer = null;
			}
			OnCompletionListener listener = new OnCompletionListener() {
				public void onCompletion(MediaPlayer p) {
					p.reset();
					p.release();
					mPlayer = null;
				}
			};

			Sound.load();
			if (Sound.key_0_9) {
				String key = (String) msg.obj;
				int n = key.charAt(0)-'0';
				if (n >= 0 && n <= 9) {
					mPlayer = Sound.play(Sound.key[n], false, listener);
				} else
					mPlayer = Sound.play(Sound.press, false, listener);
			} else
				mPlayer = Sound.play(Sound.press, false, listener);

			tBroadcast();
		}
	};

	private static Handler e_start = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			if (TalkLabel.mIntent == null) {
				WakeTask.acquire();
				for(int i=0; i<3; i++) {
					if (WakeTask.isScreenOn())
						break;
					try {
						Thread.sleep(800);
					} catch (InterruptedException e) {
					}
				}

				TalkLabel.mIntent = new Intent(SysTalk.mContext, TalkLabel.class);
				TalkLabel.mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
				SysTalk.mContext.startActivity(TalkLabel.mIntent);
			}
		}
	};

	private static Handler e_play = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (TalkLabel.mContext != null)
				TalkLabel.mContext.play();
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();

		mContext = this;

		dmsg.start("/ui");
		devent.setup();

		WakeTask.onCreate(this);

		sys.load();

		vt_uart.start();
		vt_uart.setup(0, 3);

		devent.boot = true;

		dmsg req = new dmsg();
		req.to("/talk/setid", null);

		SysAccess.load();
		SysCard.load();
		sLocale.load();
		SysSpecial.load();
		SDTLogger.load();
		SysProtocol.load();

		eDhcp.start();
		FaceCompare.start();

		ProcessThread p = new ProcessThread();
		Thread t = new Thread(p);
		t.start();

		Handler e = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				mBootEnd = true;
			}
		};
		e.sendEmptyMessageDelayed(0, 10*1000);

		utils.ioctl.rgb(utils.ioctl.R);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		WakeTask.onDestroy();
	}

	public static class ProcessThread implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(1*1000);
			} catch (InterruptedException e) {
			}
			if (utils.getLocalIp() == null)
				utils.eth0_reset();

			utils.buzzer(100);

			while (true) {
				SysCard.process();
				utils.process();
				sLocale.process();
				try {
					Thread.sleep(40);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void start() {
		e_start.sendMessage(e_start.obtainMessage());
	}

	public static void play() {
		e_play.sendMessage(e_play.obtainMessage());
	}

	public static void touch(String key) {
		Message m = e_touch.obtainMessage();
		m.obj = key;
		e_touch.sendMessage(m);
	}

	public static class ipErr {
		public static int result = 0;
		public static String mac;
	}

	public static void ipMacErr(int result, String ip, String mac) {
		ipErr.result = result;
		ipErr.mac = mac;
		WakeTask.acquire();
	}

	public static void tBroadcast() {
		if (mContext != null) {
			Intent it = new Intent("com.dnake.broadcast");
			it.putExtra("event", "com.dnake.talk.touch");
			mContext.sendBroadcast(it);
		}
	}
}
