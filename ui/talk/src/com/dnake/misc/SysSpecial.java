package com.dnake.misc;

import com.dnake.v700.dxml;

public class SysSpecial {
	public static int third = 0;

	public static void load() {
		dxml p = new dxml();
		p.load("/dnake/bin/special.xml");
		third = p.getInt("/sys/third", 0);
	}
}
