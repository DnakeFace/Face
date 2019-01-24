package com.dnake.v700;

import java.util.Locale;

public class sLocale {
	public static String language = "zh";
	public static String country = "CN";
	public static String url = "/dnake/cfg/sys_locale.xml";

	public static void load() {
		dxml p = new dxml();
		if (p.load(url)) {
			language = p.getText("/sys/language", language);
			country = p.getText("/sys/country", country);
		} else
			save();
	}

	public static void save() {
		dxml p = new dxml();
		p.setText("/sys/language", language);
		p.setText("/sys/country", country);
		p.save(url);
	}

	private static Boolean mBoot = true;
	private static long mTs = 0;

	public static void process() {
		if (sys.ts(mTs) < 5*1000)
			return;
		mTs = System.currentTimeMillis();

		Boolean ok = false;
		if (!Locale.getDefault().getLanguage().equals(language) || !Locale.getDefault().getCountry().equals(country))
			ok = true;
		if (mBoot) {
			mBoot = false;
			if (ok) {
				dxml p = new dxml();
				dmsg req = new dmsg();
				p.setText("/params/language", language);
				p.setText("/params/country", country);
				req.to("/settings/locale", p.toString());
			}
		} else {
			if (ok) {
				language = Locale.getDefault().getLanguage();
				country = Locale.getDefault().getCountry();
				save();
			}
		}
	}
}
