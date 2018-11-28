package com.dnake.misc;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;

public class sCaller {
	public static int NONE = 0;
	public static int QUERY = 1;
	public static int CALL = 2;
	public static int RINGING = 3;
	public static int TALK = 4;
	public static int STOP = 5;

	public static class Time {
		public static int QUERY = 1500;
		public static int CALL = 10*1000;
		public static int MONITOR = 30*1000;
	}

	public static int running = 0;
	private static long mTs;
	public static String mId;
	public static String mIp;

	public static Boolean bStop = false;

	public static void query(String id) {
		sys.qResult.sip.url = null;
		sys.qResult.d600.ip = null;
		sys.qResult.d600.host = null;

		sCaller.mId = id;

		bStop = false;
		running = QUERY;
		mTs = System.currentTimeMillis();

		if (sys.talk.limit != 0) //特殊密码锁定，限制呼叫
			return;

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/id", id);
		req.to("/talk/sip/query", p.toString());
	}

	public static void start(String url) {
		sys.qResult.result = 0;
		bStop = false;
		running = CALL;
		mTs = System.currentTimeMillis();

		if (sys.talk.limit != 0) //特殊密码锁定，限制呼叫
			return;

		dmsg req = new dmsg();
		dxml p = new dxml();

		p.setText("/params/url", url);
		int ss = url.indexOf('@');
		if (ss > 0)
			mId = url.substring(4, ss);
		req.to("/talk/sip/call", p.toString());

		sCaller.jpeg();
	}

	public static void m700(String url) {
		sys.qResult.result = 0;
		bStop = false;
		running = CALL;
		mTs = System.currentTimeMillis();

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/url", url);
		p.setInt("/params/type", 1);
		req.to("/talk/sip/call", p.toString());
	}

	public static void q600(String id) {
		sys.qResult.d600.ip = null;
		sys.qResult.d600.host = null;

		sCaller.mId = id;
		bStop = false;
		running = QUERY;
		mTs = System.currentTimeMillis();

		if (sys.talk.limit != 0) //特殊密码锁定，限制呼叫
			return;

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/name", id);
		req.to("/talk/device/query", p.toString());
	}

	public static void s600(String host, String ip) {
		sys.qResult.result = 0;
		sys.qResult.d600.ip = null;
		sys.qResult.d600.host = null;

		sCaller.mId = host;
		sCaller.mIp = ip;

		bStop = false;
		running = CALL;
		mTs = System.currentTimeMillis();

		if (sys.talk.limit != 0) //特殊密码锁定，限制呼叫
			return;

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/name", host);
		p.setText("/params/ip", ip);
		req.to("/talk/call", p.toString());
	}

	public static void m600(String host, String ip) {
		sys.qResult.result = 0;
		bStop = false;
		running = CALL;

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/name", host);
		p.setText("/params/ip", ip);
		req.to("/talk/monitor", p.toString());
	}

	public static void reset() {
		running = NONE;
		bStop = false;
		mIp = null;
	}

	public static int timeout() {
		return (int)Math.abs(System.currentTimeMillis()-mTs);
	}

	public static void refresh() {
		mTs = System.currentTimeMillis();
	}

	public static void jpeg() {
		dmsg req = new dmsg();
		req.to("/talk/vi/snapshot", null);
	}

	public static class logger {
		public static int ANSWER = 0;
		public static int FAILED = 1;
		public static int UNLOCK = 2;
		public static int CALL = 3;
		public static int END = 4;
	}

	//mode 0:已接听  1:未接听  2:开锁  3:呼叫  4:结束
	public static void logger(int mode) {
		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setText("/params/event_url", "/msg/talk/logger");
		p.setText("/params/to", mId);
		p.setInt("/params/mode", mode);
		req.to("/talk/center/to", p.toString());
	}
}
