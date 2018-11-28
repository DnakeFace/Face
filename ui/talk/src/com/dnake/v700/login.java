package com.dnake.v700;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class login {
	public static long ts = 0;
	public static Boolean ok = false;
	public static int timeout = 30*1000;

	public static Boolean passwd(String s) {
		if (s.equals(nvc.admin.passwd())) {
			ts = System.currentTimeMillis();
			ok = true;

			login.settings(1);
			return true;
		}
		return false;
	}

	public static Boolean ok() {
		if (ok && Math.abs(System.currentTimeMillis()-ts) < timeout)
			return true;
		ok = false;
		return false;
	}

	public static void refresh() {
		if (ok && Math.abs(System.currentTimeMillis()-ts) > timeout)
			ok = false;
		ts = System.currentTimeMillis();
	}

	public static Boolean timeout() {
		if (ok && Math.abs(System.currentTimeMillis()-ts) < timeout)
			return false;
		ok = false;
		return true;
	}

	public static void settings(int ok) {
		try {
			FileOutputStream out = new FileOutputStream("/var/etc/login");
			String s = String.valueOf(ok);
			out.write(s.getBytes());
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
