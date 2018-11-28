package com.dnake.misc;

import com.dnake.panel.WakeTask;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

@SuppressLint("DefaultLocale")
public final class SysAccess {

	public static class admin {
		public static String card = "123456";
		public static long ts = 0;
	}

	public static class lock {
		public static String passwd[] = new String[8];
		public static int timeout = 3;
		public static int delay = 0;

		public static Boolean verify(String p) {
			for (int i = 0; i < 8; i++) {
				if (p.equals(passwd[i]))
					return true;
			}
			return false;
		}

		public static class sensor {
			public static long ts = 0;
			public static int st = 0; // 门磁状态 0:关门 1:开门
			public static Boolean have = false;
		}
	}

	public static class elev {
		public static int to = 12;
	}

	public static class security {
		public static int enable = 0;
	}

	public static String url = "/dnake/cfg/sys_access.xml";

	public static void load() {
		dxml p = new dxml();
		if (p.load(url)) {
			admin.card = p.getText("/access/admin/card", "123456");
			lock.timeout = p.getInt("/access/lock/timeout", 3);
			lock.delay = p.getInt("/access/lock/delay", 0);
			for (int i = 0; i < 8; i++)
				lock.passwd[i] = p.getText("/access/lock/p_" + i, "....");

			elev.to = p.getInt("/access/elev/to", 12);
			security.enable = p.getInt("/access/security/enable", 0);
		} else {
			for (int i = 0; i < 8; i++)
				lock.passwd[i] = "....";
			lock.passwd[0] = "0000";
			save();
		}
	}

	public static void save() {
		dxml p = new dxml();

		p.setText("/access/admin/card", admin.card);
		p.setInt("/access/lock/timeout", lock.timeout);
		p.setInt("/access/lock/delay", lock.delay);
		for (int i = 0; i < 8; i++)
			p.setText("/access/lock/p_" + i, lock.passwd[i]);

		p.setInt("/access/elev/to", elev.to);
		p.setInt("/access/security/enable", security.enable);

		p.save(url);
	}

	public static void reset() {
		admin.card = "123456";
		lock.timeout = 3;
		lock.delay = 0;

		elev.to = 12;
		security.enable = 0;
		for (int i = 0; i < 8; i++)
			lock.passwd[i] = "....";
		lock.passwd[0] = "0000";
	}

	public static void process() {
		for(int i=0; i<2; i++) {
			if (lock_ts[i] != 0 && Math.abs(System.currentTimeMillis() - lock_ts[i]) >= lock.timeout * 1000)
				setLock(i, 0);
			if (delay_ts[i] != 0 && Math.abs(System.currentTimeMillis() - delay_ts[i]) >= lock.delay * 1000)
				setLock(i, 1);
		}

		if (admin.ts != 0 && Math.abs(System.currentTimeMillis() - admin.ts) >= 10 * 1000)
			admin.ts = 0;

		if (lock.sensor.ts != 0) {
			if (lock.sensor.st == 0) { // 关门
				if (lock.sensor.have)
					sensorLogger(0);
				lock.sensor.ts = 0;
				lock.sensor.have = false;
			} else { // 开门
				if (Math.abs(System.currentTimeMillis() - lock.sensor.ts) >= 120 * 1000) { // 120秒门没关
					sensorLogger(1);
					lock.sensor.ts = 0;
					lock.sensor.have = true;
				}
			}
		}
	}

	private static long delay_ts[] = {0, 0};
	private static long lock_ts[] = {0, 0};
	private static MediaPlayer player = null;

	public static void unlock(int index, int jpeg) {
		if (sys.talk.limit != 0) //特殊密码锁定，限制呼叫
			return;

		WakeTask.acquire();

		if (lock.delay == 0)
			setLock(index, 1);
		else
			delay_ts[index] = System.currentTimeMillis();

		if (sCaller.running == sCaller.TALK) {
			dmsg req = new dmsg();
			dxml p = new dxml();
			p.setInt("/params/mode", 1);
			p.setText("/params/url", Sound.unlock);
			req.to("/talk/prompt", p.toString());
		} else {
			if (player == null) {
				OnCompletionListener listener = new OnCompletionListener() {
					public void onCompletion(MediaPlayer p) {
						player.reset();
						player.release();
						player = null;
					}
				};
				player = Sound.play(Sound.unlock, false, listener);
			}
		}
		if (jpeg != 0)
			sCaller.jpeg();
	}

	public static void setLock(int index, int onoff) {
		System.out.println("setLock"+index+": " + onoff);

		if (onoff == 0)
			lock_ts[index] = 0;
		else
			lock_ts[index] = System.currentTimeMillis();
		delay_ts[index] = 0;

		dxml p = new dxml();
		dmsg req = new dmsg();
		p.setInt("/params/index", index);
		p.setInt("/params/onoff", onoff);
		req.to("/control/v170/lock", p.toString());
	}

	public static void elev(int f, int r) {
		SysElev.unit(f, r, elev.to);

		dxml p = new dxml();
		dmsg req = new dmsg();
		p.setText("/event/broadcast_url", "elevaction");
		p.setInt("/event/elev/to", elev.to);
		p.setInt("/event/elev/build", sys.talk.building);
		p.setInt("/event/elev/unit", sys.talk.unit);
		p.setInt("/event/elev/floor", f);
		p.setInt("/event/elev/family", r);
		req.to("/talk/broadcast/data", p.toString());
	}

	public static void elevWall(int b, int u, int f, int r) {
		SysElev.wall(0, elev.to, b, u, f, r);

		dxml p = new dxml();
		dmsg req = new dmsg();
		p.setText("/event/broadcast_url", "/elev/wall/action");
		p.setInt("/event/elev/to", elev.to);
		p.setInt("/event/elev/build", b);
		p.setInt("/event/elev/unit", u);
		p.setInt("/event/elev/floor", f);
		p.setInt("/event/elev/family", r);
		req.to("/talk/broadcast/data", p.toString());
	}

	public static void security(int b, int u, int f, int r, String card) {
		dxml p = new dxml();
		dmsg req = new dmsg();

		p.setText("/event/broadcast_url", "/security/setup");
		p.setInt("/event/build", b);
		p.setInt("/event/unit", u);
		p.setInt("/event/floor", f);
		p.setInt("/event/family", r);
		p.setText("/event/card", card);
		p.setInt("/event/mode", sys.panel.mode);
		req.to("/talk/broadcast/data", p.toString());
	}

	public static void logger(int b, int u, int f, int r, String card, int auth) {
		dxml p = new dxml();
		dmsg req = new dmsg();

		if (auth != 0) {
			String s = String.format("S%04d%02d%02d%02d0", b, u, f, r);
			p.setText("/params/id_600", s);
			p.setText("/params/card", card);
			req.to("/control/d600/card/logger", p.toString());

			p.setText("/params/card", card);
			p.setInt("/params/build", b);
			p.setInt("/params/unit", u);
			p.setInt("/params/room", f*100+r);
			req.to("/wx/access/card", p.toString());
		}

		p.setText("/params/event_url", "/msg/access/card");
		p.setText("/params/card", card);
		p.setInt("/params/auth", auth);
		req.to("/talk/center/to", p.toString()); // 700协议卡号上报
	}

	public static void sensorLogger(int st) {
		String s = "";
		switch (sys.panel.mode) {
		case 0: // 单元门口机
			s = String.format("%d%02d%02d%02d", sys.talk.building, sys.talk.unit, sys.talk.floor, sys.panel.index);
			break;
		case 1: // 围墙机
			s = String.format("2%04d", sys.panel.index);
			break;
		default:
			return;
		}

		dxml p = new dxml();
		dmsg req = new dmsg();

		p.setText("/event/broadcast_url", "/access/lock");
		p.setInt("/event/data", st);
		p.setInt("/event/mode", sys.panel.mode);
		p.setText("/event/id", s);
		req.to("/talk/broadcast/data", p.toString());

		dxml p2 = new dxml();
		p2.setText("/params/event_url", "/msg/alarm/trigger");
		p2.setInt("/params/zone", 0);
		p2.setInt("/params/data", st);
		req.to("/talk/center/to", p2.toString()); // 700协议报警上报

		dxml p3 = new dxml();
		p3.setInt("/params/sp", 1<<3);
		req.to("/control/d600/alarm_trigger", p3.toString()); // 600管理软件
	}

	public static void sensorCms(int st) {
		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/event_url", "/msg/access/sensor");
		p.setInt("/params/data", st);
		req.to("/talk/center/to", p.toString());
	}
}
