package com.dnake.misc;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.dnake.panel.FaceLabel;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;

public class SysSpecial {
	public static int third = 0;

	public static void load() {
		dxml p = new dxml();
		p.load("/dnake/bin/special.xml");
		third = p.getInt("/sys/third", 0);
	}

	public static String acpDecode(String data) {
		String s = data.substring(6);
		byte d[] = s.getBytes();
		for(int i=0; i<d.length; i++) {
			if (i > 6)
				d[i] = (byte) (d[i]-3);
			else
				d[i] = (byte) (d[i]-5);
		}
		return (new String(d));
	}

	public static int mAcpSeq = 0;

	//acp://101_7a30f2dd-f67b-4afe-b88c-786d8d753f3a_2-3-501_123
	//      命令_微云门禁ID_房间号_序列号
	public static void doACP(String data) {
		String ss = acpDecode(data);
		String tk[] = new String[32];
		int n = 0;
		try {
			StringTokenizer st = new StringTokenizer(ss, "_");
			while (true) {
				String s = st.nextToken();
				if (s == null)
					break;
				tk[n++] = s;
			}
		} catch (NoSuchElementException e) {
		} catch (NumberFormatException e) {
		}

		int b = 0, u = 0, r = 0;
		boolean ok = false;
		if (n > 0) {
			if (tk[0].endsWith("101") && n >= 4) {
				int seq = Integer.parseInt(tk[3]);
				if (tk[1].endsWith(FaceLabel.mWxUuid) && seq > mAcpSeq) {
					StringTokenizer st = new StringTokenizer(tk[2], "-");
					String s = st.nextToken();
					if (s != null)
						b = Integer.parseInt(s);
					s = st.nextToken();
					if (s != null)
						u = Integer.parseInt(s);
					s = st.nextToken();
					if (s != null)
						r = Integer.parseInt(s);
					mAcpSeq = seq;
					ok = true;
				}
			}
		}
		if (ok) {
			dxml p = new dxml();
			dmsg req = new dmsg();
			p.setText("/params/from", "wx");
			p.setInt("/params/build", b);
			p.setInt("/params/unit", u);
			p.setInt("/params/room", r);
			req.to("/face/unlock", p.toString());
			Sound.play(Sound.unlock, false);
		} else
			Sound.play(Sound.card_err, false);
	}

	//haina:101_2-3-5-1_1_1528439328_1528439508
	//      命令_房号_用户类型_时间_有效期
	public static void doHaina(String data) {
		String tk[] = new String[32];
		int n = 0;
		try {
			StringTokenizer st = new StringTokenizer(data, "_");
			while (true) {
				String s = st.nextToken();
				if (s == null)
					break;
				tk[n++] = s;
			}
		} catch (NoSuchElementException e) {
		} catch (NumberFormatException e) {
		}

		int b = 0, u = 0, f=0, r=0;
		boolean ok = false;
		if (n > 4) {
			if (tk[0].endsWith("101")) {
				boolean match = false;
				//long ts = Long.parseLong(tk[3])*1000;
				long te = (Long.parseLong(tk[4])+2*60)*1000;
				StringTokenizer st = new StringTokenizer(tk[1], "-");
				String s = st.nextToken();
				if (s != null)
					b = Integer.parseInt(s);
				s = st.nextToken();
				if (s != null)
					u = Integer.parseInt(s);
				s = st.nextToken();
				if (s != null)
					f = Integer.parseInt(s);
				s = st.nextToken();
				if (s != null)
					r = Integer.parseInt(s);
				if (sys.panel.mode == 0) { //单元门口机
					if (b == 0 && u == 0)
						match = true;
					else if (b == sys.talk.building && u == sys.talk.unit)
						match = true;
				} else if (sys.panel.mode == 1) //围墙机
					match = true;
				if (System.currentTimeMillis() < te && match)
					ok = true;
			}
		}
		if (ok) {
			dxml p = new dxml();
			dmsg req = new dmsg();
			p.setText("/params/from", "wx");
			p.setInt("/params/build", b);
			p.setInt("/params/unit", u);
			p.setInt("/params/room", f*100+r);
			req.to("/face/unlock", p.toString());
			Sound.play(Sound.unlock, false);
		} else
			Sound.play(Sound.card_err, false);
	}
}
