package com.dnake.v700;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.dnake.misc.SysTalk;

import android.annotation.SuppressLint;
import android.content.res.Configuration;

@SuppressLint("DefaultLocale")
public final class sys {

	public static int version_major = 1; // 主版本
	public static int version_minor = 3; // 次版本
	public static int version_minor2 = 0; // 次版本2

	public static String version_date = "20181206"; // 日期

	public static String version_ex = "(std)"; // 扩展标注

	public static float scaled = 1.0f;

	public static int mute = 0;

	public static boolean factory = false; //生产测试模式

	public static String url = "/dnake/cfg/sys.xml";
	private static String url_b = "/dnake/data/sys.xml";

	public static class qResult {
		public sip sip = new sip();
		public d600 d600 = new d600();
		public v170 v170 = new v170();
		public int result = 0;

		public class sip {
			public String url = null;
			public int proxy;
		}

		public class d600 {
			public String ip = null;
			public String host = null;
		}

		public class v170 {
			public String card = null;
		}
	}

	public static qResult qResult = new qResult();

	public static final class talk {
		public static int building = 1;
		public static int unit = 1;
		public static int floor = 11;
		public static int family = 11;

		public static String server = new String("192.168.12.40");
		public static String passwd = new String("123456");

		public static int timeout = 120;
		public static int onu_arp = 0;

		public static int limit = 0; // 限制呼叫、刷卡
	}

	public static final class sip {
		public static int enable = 0;
		public static String proxy = new String("sip:192.168.12.40");
		public static String realm = new String("192.168.12.40");
		public static String user = new String("100");
		public static String passwd = new String("123456");
		public static String outbound = new String("sip:");

		public static final class stun {
			public static String ip = new String("192.168.12.40");
			public static int port = 5060;
		}

		public static int host2id = 1;
		public static int bitrate = 768000;
	}

	public static final class panel {
		public static int mode = 0; // 0: 单元门口机 1:围墙机 2:小门口机
		public static int index = 1; // 编号
		public static int nUnit = 0; // 围墙机不输入单元号
		public static int forward = 0; // 转呼模式 0:顺序呼叫 1:同时呼叫
		public static int ringing = 35; // 振铃时间
	}

	public static final class video {
		public static int format = 1; // 0: QVGA 1:VGA 2:高清
	}

	public static final class feed {
		public static int max = 0; // RTSP视频源URL数量
		public static String url[] = new String[4];
	}

	public static final class payload {
		public static int H264 = 102;
	}

	public static String id() {
		String s = null;
		if (panel.mode == 0)
			s = String.format("%d%02d%02d%02d", talk.building, talk.unit, 99, panel.index);
		else if (panel.mode == 1)
			s = String.format("2%04d", panel.index);
		else if (panel.mode == 2)
			s = String.format("%d%03d%02d%02d%02d", panel.index, talk.building, talk.unit, talk.floor, talk.family);
		return s;
	}

	public static void id(String s) {
		if (panel.mode == 0) {
			String b, u, f, r;
			int sz = s.length();
			r = s.substring(sz - 2, sz);
			f = s.substring(sz - 4, sz - 4 + 2);
			u = s.substring(sz - 6, sz - 6 + 2);
			b = s.substring(0, sz - 6);

			talk.building = Integer.parseInt(b);
			talk.unit = Integer.parseInt(u);
			talk.floor = Integer.parseInt(f);
			talk.family = Integer.parseInt(r);
			panel.index = Integer.parseInt(r);
		} else if (panel.mode == 1) {
			int id = Integer.parseInt(s);
			panel.index = id % 10000;
		} else if (panel.mode == 2) {
			String b, u, f, r, n;
			int sz = s.length();
			r = s.substring(sz - 2, sz);
			f = s.substring(sz - 4, sz - 4 + 2);
			u = s.substring(sz - 6, sz - 6 + 2);
			b = s.substring(sz - 9, sz - 9 + 3);
			n = s.substring(0, sz - 9);

			talk.building = Integer.parseInt(b);
			talk.unit = Integer.parseInt(u);
			talk.floor = Integer.parseInt(f);
			talk.family = Integer.parseInt(r);
			panel.index = Integer.parseInt(n);
		}
	}

	public static void load() {
		dxml p = new dxml();

		boolean result = p.load(url);
		if (!result)
			result = p.load(url_b);

		if (result) {
			talk.building = p.getInt("/sys/talk/building", 1);
			talk.unit = p.getInt("/sys/talk/unit", 1);
			talk.floor = p.getInt("/sys/talk/floor", 1);
			talk.family = p.getInt("/sys/talk/family", 1);
			talk.server = p.getText("/sys/talk/server", talk.server);
			talk.passwd = p.getText("/sys/talk/passwd", talk.passwd);
			talk.timeout = p.getInt("/sys/talk/timeout", 300);
			talk.onu_arp = p.getInt("/sys/talk/onu_arp", 0);
			talk.limit = p.getInt("/sys/talk/limit", 0);

			sip.enable = p.getInt("/sys/sip/ex_enable", 0);
			sip.proxy = p.getText("/sys/sip/proxy", sip.proxy);
			sip.realm = p.getText("/sys/sip/realm", sip.realm);
			sip.user = p.getText("/sys/sip/ex_user", sip.user);
			sip.passwd = p.getText("/sys/sip/passwd", sip.passwd);

			sip.stun.ip = p.getText("/sys/stun/ip", sip.stun.ip);
			sip.stun.port = p.getInt("/sys/stun/port", 5060);

			sip.host2id = p.getInt("/sys/sip/host2id", 1);
			sip.bitrate = p.getInt("/sys/sip/bitrate", 768000);

			panel.mode = p.getInt("/sys/panel/mode", 0);
			panel.index = p.getInt("/sys/panel/index", 1);
			panel.nUnit = p.getInt("/sys/panel/nUnit", 0);
			panel.forward = p.getInt("/sys/panel/forward", 0);
			panel.ringing = p.getInt("/sys/panel/ringing", 35);

			video.format = p.getInt("/sys/video/format", 1);

			feed.max = p.getInt("/sys/feed/max", 0);
			for (int i = 0; i < feed.max; i++) {
				feed.url[i] = p.getText("/sys/feed/url" + i);
			}
			payload.H264 = p.getInt("/sys/payload/h264", 102);
		} else
			save();
	}

	public static void save() {
		dxml p = new dxml();

		if (panel.mode == 1) {
			talk.building = 0;
			talk.unit = 0;
		}

		p.setInt("/sys/talk/building", talk.building);
		p.setInt("/sys/talk/unit", talk.unit);
		p.setInt("/sys/talk/floor", talk.floor);
		p.setInt("/sys/talk/family", talk.family);
		p.setText("/sys/talk/server", talk.server);
		p.setText("/sys/talk/passwd", talk.passwd);
		p.setInt("/sys/talk/timeout", talk.timeout);
		p.setInt("/sys/talk/onu_arp", talk.onu_arp);
		p.setInt("/sys/talk/limit", talk.limit);

		p.setInt("/sys/sip/ex_enable", sip.enable);
		p.setText("/sys/sip/proxy", sip.proxy);
		p.setText("/sys/sip/realm", sip.realm);
		p.setText("/sys/sip/ex_user", sip.user);
		p.setText("/sys/sip/passwd", sip.passwd);

		p.setText("/sys/stun/ip", sip.stun.ip);
		p.setInt("/sys/stun/port", sip.stun.port);

		p.setInt("/sys/sip/host2id", sip.host2id);
		p.setInt("/sys/sip/bitrate", sip.bitrate);

		p.setInt("/sys/panel/mode", panel.mode);
		p.setInt("/sys/panel/index", panel.index);
		p.setInt("/sys/panel/nUnit", panel.nUnit);
		p.setInt("/sys/panel/forward", panel.forward);
		p.setInt("/sys/panel/ringing", panel.ringing);

		p.setInt("/sys/video/format", video.format);
		p.setInt("/sys/feed/max", feed.max);
		for (int i = 0; i < feed.max; i++) {
			if (feed.url[i] != null) {
				p.setText("/sys/feed/url" + i, feed.url[i]);
			}
		}
		p.setInt("/sys/payload/h264", payload.H264);

		p.save(url);
		p.save(url_b);

		dmsg req = new dmsg();
		req.to("/talk/setid", null);
		req.to("/control/set_id", null);
	}

	public static void reset() {
		talk.building = 1;
		talk.unit = 1;
		talk.floor = 99;
		talk.family = 1;
		talk.server = "192.168.12.40";
		talk.passwd = "123456";
		talk.timeout = 2 * 60;

		sip.proxy = "sip:192.168.12.40";
		sip.realm = "192.168.12.40";
		sip.passwd = "123456";

		sip.stun.ip = "192.168.12.40";
		sip.stun.port = 5060;

		sip.enable = 0;
		sip.user = "100";

		sip.host2id = 1;
		sip.bitrate = 768000;

		panel.mode = 0;
		panel.index = 1;
		panel.nUnit = 0;
		panel.forward = 0;
		panel.ringing = 35;
	}

	public static long ts(long val) {
		return Math.abs(System.currentTimeMillis() - val);
	}

	public static int sLimit = -1;

	public static int limit() {
		if (sLimit != -1)
			return sLimit;

		int limit = 0;
		try {
			FileInputStream in = new FileInputStream("/dnake/bin/limit");
			byte[] data = new byte[256];
			int ret = in.read(data);
			if (ret > 0) {
				String s = new String();
				char[] d = new char[1];
				for (int i = 0; i < ret; i++) {
					if (data[i] >= '0' && data[i] <= '9') {
						d[0] = (char) data[i];
						s += new String(d);
					} else
						break;
				}
				limit = Integer.parseInt(s);
			}
			in.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		sLimit = limit;
		return limit;
	}

	// 0:横屏 1:竖屏
	private static int mLcd = -1;
	public static int lcd() {
		if (mLcd == -1) {
			dxml p = new dxml();
			p.load("/system/etc/special.xml");
			mLcd = p.getInt("/sys/lcd", 0);
		}
		return mLcd;
	}

	public static int lan_carrier() {
		int ok = 0;
		try {
			FileInputStream in = new FileInputStream("/sys/class/net/eth0/carrier");
			byte[] data = new byte[32];
			int ret = in.read(data);
			if (ret > 0) {
				if (data[0] == '1')
					ok = 1;
			}
			in.close();
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			// e.printStackTrace();
		}
		return ok;
	}

	// 0:简体中文 1:英文 2:繁体中文
	public static int language() {
		if (SysTalk.mContext == null)
			return 0;
		Configuration c = SysTalk.mContext.getResources().getConfiguration();
		String s = c.locale.getLanguage();
		String s2 = c.locale.getCountry();
		if (s.equalsIgnoreCase("zh")) {
			if (s2.equalsIgnoreCase("TW"))
				return 2;
			else
				return 0;
		} else if (s.equalsIgnoreCase("en"))
			return 1;
		return 0;
	}

	public static void language(int val) {
		if (language() == val)
			return;

		String s = "zh", s2 = "CN";
		switch (val) {
		case 0:
			s = "zh";
			s2 = "CN";
			break;
		case 1:
			s = "en";
			s2 = "US";
			break;
		case 2:
			s = "zh";
			s2 = "TW";
			break;
		}

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/language", s);
		p.setText("/params/country", s2);
		req.to("/settings/locale", p.toString());
	}

	public static int sdt() {
		int sdt = 0;
		try {
			FileInputStream in = new FileInputStream("/var/etc/sdt");
			byte [] data = new byte[256];
			int ret = in.read(data);
			if (ret > 0) {
				String s = new String();
				char [] d = new char[1];
				for(int i=0; i<ret; i++) {
					if (data[i] >= '0' && data[i] <= '9') {
						d[0] = (char) data[i];
						s += new String(d);
					} else
						break;
				}
				sdt = Integer.parseInt(s);
			}
			in.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return sdt;
	}

	private static int mH3C = -1;
	public static int h3c() {
		if (mH3C == -1) {
			dxml p = new dxml();
			p.load("/dnake/bin/special.xml");
			mH3C = p.getInt("/sys/h3c", 0);
		}
		return mH3C;
	}
}
