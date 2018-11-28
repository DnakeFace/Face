package com.dnake.misc;

import android.annotation.SuppressLint;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

@SuppressLint("DefaultLocale")
public class SysCard {
	public static class Data {
		public int build;
		public int unit;
		public int floor;
		public int family;
		public String card;
	}

	public static int MAX = 10*10000;

	public static Data mData[] = new Data[MAX];
	public static int mMax = 0;

	private static String url = "/dnake/cfg/sys_card.txt";

	private static void loadCard(String ss) {
		if (mMax >= MAX)
			return;

		try {
			StringTokenizer tk = new StringTokenizer(ss, " ");
			String s = tk.nextToken();
			String c = tk.nextToken();

			if (s == null || c == null || c.length() == 0 || c.length() > 32)
				return;

			tk = new StringTokenizer(s, "-");
			String b = tk.nextToken();
			String u = tk.nextToken();
			String f = tk.nextToken();
			String r = tk.nextToken();
			if (b == null || u == null || f == null || r == null)
				return;

			mData[mMax] = new Data();
			mData[mMax].build = Integer.parseInt(b);
			mData[mMax].unit = Integer.parseInt(u);
			mData[mMax].floor = Integer.parseInt(f);
			mData[mMax].family = Integer.parseInt(r);
			mData[mMax].card = c;
			mMax++;
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	public static Data verify(String card) {
		for(int i=0; i<mMax; i++) {
			if (mData[i] != null && mData[i].card.equalsIgnoreCase(card))
				return mData[i];
		}
		return null;
	}

	private static long mSaveTs = 0;

	public static void process() {
		if (mSaveTs != 0 && Math.abs(System.currentTimeMillis()-mSaveTs) >= 2000) {
			mSaveTs = 0;
			save();
		}
	}

	public static void rm(String card) {
		for(int i=0; i<mMax; i++) {
			if (mData[i] != null && mData[i].card.equalsIgnoreCase(card)) {
				mMax--;
				mData[i] = mData[mMax];
				mSaveTs = System.currentTimeMillis();
				break;
			}
		}
	}

	public static void add(Data d) {
		if (mMax >= MAX || d.card == null || d.card.length() == 0)
			return;

		mSaveTs = System.currentTimeMillis();
		for(int i=0; i<mMax; i++) {
			if (mData[i] != null && mData[i].card.equalsIgnoreCase(d.card)) {
				mData[i].build = d.build;
				mData[i].unit = d.unit;
				mData[i].floor = d.floor;
				mData[i].family = d.family;
				return;
			}
		}
		mData[mMax] = new Data();
		mData[mMax].card = d.card;
		mData[mMax].build = d.build;
		mData[mMax].unit = d.unit;
		mData[mMax].floor = d.floor;
		mData[mMax].family = d.family;
		mMax++;
	}

	public static void load() {
		mMax = 0;
		for(int i=0; i<MAX; i++)
			mData[i] = null;

		try {
			String s;
			FileInputStream in = new FileInputStream(url);
			BufferedReader r = new BufferedReader(new InputStreamReader(in));
			while ((s = r.readLine()) != null) {
				if (s.length() > 8) {
					loadCard(s);
				}
			}
			r.close();
			in.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	public static void save() {
		try {
			FileOutputStream out = new FileOutputStream(url);
			for(int i=0; i<mMax; i++) {
				Data d = mData[i];
				String s = String.format("%d-%d-%d-%d %s\n", d.build, d.unit, d.floor, d.family, d.card);
				out.write(s.getBytes());
			}
			out.flush();
			out.getFD().sync();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void reset() {
		mMax = 0;
		for(int i=0; i<MAX; i++)
			mData[i] = null;
	}
}
