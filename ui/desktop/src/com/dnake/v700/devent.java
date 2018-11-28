package com.dnake.v700;

import java.util.Iterator;
import java.util.List;

public class devent {
	private static List<devent> elist = null;
	public static Boolean boot = false;

	public String url;

	public devent(String url) {
		this.url = url;
	}

	public void process(String xml) {
	}

	public static void event(String url, String xml) {
		Boolean err = true;
		if (boot && elist != null) {
			devent e;

			Iterator<devent> it = elist.iterator();
			while (it.hasNext()) {
				e = it.next();
				if (url.equals(e.url)) {
					e.process(xml);
					err = false;
					break;
				}
			}
		}
		if (err)
			dmsg.ack(480, null);
	}

	public static void setup() {
	}
}
