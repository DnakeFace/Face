package com.dnake.v700;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class sys {
	public static float scaled = 1.0f;

	public static int sLimit = -1;
	public static int limit() {
		if (sLimit != -1)
			return sLimit;

		int limit = 0;
		try {
			FileInputStream in = new FileInputStream("/dnake/bin/limit");
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

	public static class lcd {
		public static int portrait = -1;

		// 0:横屏 1:竖屏
		public static int orientation() {
			if (lcd.portrait < 0) {
				dxml sp = new dxml();
				sp.load("/system/etc/special.xml");
				lcd.portrait = sp.getInt("/sys/lcd", 0);

				dxml p = new dxml();
				p.load("/dnake/cfg/sys.xml");
				lcd.portrait = p.getInt("/sys/lcd/portrait", lcd.portrait);
			}
			return lcd.portrait;
		}
	}
}
