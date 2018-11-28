package com.dnake.v700;

public class nvc {

	public static class admin {
		public static String passwd() {
			dxml p = new dxml();
			p.load("/dnake/cfg/nvc.xml");
			return p.getText("/sys/admin/passwd", "123456");
		}

		public static void passwd(String s) {
			dxml p = new dxml();
			p.load("/dnake/cfg/nvc.xml");
			p.setText("/sys/admin/passwd", s);
			p.save("/dnake/cfg/nvc.xml");
		}
	}
}
