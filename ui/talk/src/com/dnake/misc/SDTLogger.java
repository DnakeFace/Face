package com.dnake.misc;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;

@SuppressLint({ "DefaultLocale", "SdCardPath" })
@SuppressWarnings("deprecation")
public class SDTLogger {
	public static int MAX = 5000;
	public static long mData[] = new long[MAX];
	public static int mIdx = 0;

	public static class Data {
		public String id;
		public String name;
		public String sex;
		public String nation;
		public String birthday;
		public String address;
		public String depart;
		public String ts;
		public String tp;
		public String photo;
		public String camera;
		public String dt;
		public String data;
		public String gate;
	}

	public static Data load(long idx) {
		String s = String.valueOf(idx);
		Data d = new Data();
		dxml p = new dxml();
		p.load("/sdcard/sdt/"+s+".xml");
		d.id = p.getText("/sys/id");
		d.name = p.getText("/sys/name");
		d.sex = p.getText("/sys/sex");
		d.nation = p.getText("/sys/nation");
		d.birthday = p.getText("/sys/birthday");
		d.address = p.getText("/sys/address");
		d.depart = p.getText("/sys/depart");
		d.ts = p.getText("/sys/ts");
		d.tp = p.getText("/sys/tp");
		d.data = p.getText("/sys/data");
		d.gate = p.getText("/sys/gate");
		d.photo = "/sdcard/sdt/"+s+"_0.jpg";
		d.camera = "/sdcard/sdt/"+s+"_1.jpg";
		d.dt = s.substring(0, 4)+"-"+s.substring(4, 6)+"-"+s.substring(6, 8)+" "+s.substring(8, 10)+":"+s.substring(10, 12)+":"+s.substring(12, 14);
		return d;
	}

	public static void load() {
		File f = new File("/sdcard/sdt");
		if (f != null) {
			if (!f.exists())
				f.mkdir();
			File[] fs = f.listFiles();
			if (fs != null) {
				for(int i=0; i<fs.length; i++) {
					if(!fs[i].isDirectory()) {
						int e = fs[i].getName().indexOf(".xml");
						if (e > 0) {
							if (mIdx < MAX) {
								String s = fs[i].getName().substring(0, e);
								mData[mIdx] = Long.parseLong(s);
								mIdx++;
							} else {
								String s = fs[i].getName().substring(0, e);
								File fd = new File("/sdcard/sdt/"+s+"_0.jpg");
								if (fd.exists())
									fd.delete();
								fd = new File("/sdcard/sdt/"+s+"_1.jpg");
								if (fd.exists())
									fd.delete();
								fd = new File("/sdcard/sdt/"+fs[i].getName());
								if (fd.exists())
									fd.delete();
							}
						}
					}
				}
				Arrays.sort(mData, 0, mIdx);
			}
		}

		f = new File("/sdcard/sdt_err");
		if (f != null) {
			if (!f.exists())
				f.mkdir();
			File[] fs = f.listFiles();
			if (fs != null) {
				for(int i=0; i<fs.length; i++) {
					if(!fs[i].isDirectory()) {
						int e = fs[i].getName().indexOf(".xml");
						if (e > 0) {
							if (mErrIdx < ERR_MAX) {
								String s = fs[i].getName().substring(0, e);
								mErrData[mErrIdx] = Long.parseLong(s);
								mErrIdx++;
							} else {
								String s = fs[i].getName().substring(0, e);
								File fd = new File("/sdcard/sdt_err/"+s+"_0.jpg");
								if (fd.exists())
									fd.delete();
								fd = new File("/sdcard/sdt_err/"+s+"_1.jpg");
								if (fd.exists())
									fd.delete();
								fd = new File("/sdcard/sdt_err/"+fs[i].getName());
								if (fd.exists())
									fd.delete();
							}
						}
					}
				}
				Arrays.sort(mErrData, 0, mErrIdx);
			}
		}
	}

	public static void insert(dxml p, Bitmap b1, Bitmap b2) {
		Date d = new Date();
		String id = String.format("%04d%02d%02d%02d%02d%02d", d.getYear()+1900, d.getMonth()+1, d.getDate(), d.getHours(), d.getMinutes(), d.getSeconds());
		String url = "/sdcard/sdt/"+id+".xml";
		p.save(url);
		if (b1 != null)
			utils.save(b1, "/sdcard/sdt/"+id+"_0.jpg");
		if (b2 != null) {
			if (b2.getWidth() > 256) {
				float scale = ((float) 256) / b2.getWidth();
				Matrix matrix = new Matrix();
				matrix.postScale(scale, scale);
				b2 = Bitmap.createScaledBitmap(b2, (int)(b2.getWidth()*scale), (int) (b2.getHeight()*scale), true);
			}
			utils.save(b2, "/sdcard/sdt/"+id+"_1.jpg");
		}
		if (mIdx >= MAX) {
			File f = new File("/sdcard/sdt/"+mData[0]+".xml");
			if (f!=null && f.exists())
				f.delete();
			f = new File("/sdcard/sdt/"+mData[0]+"_0.jpg");
			if (f!=null && f.exists())
				f.delete();
			f = new File("/sdcard/sdt/"+mData[0]+"_1.jpg");
			if (f!=null && f.exists())
				f.delete();
			f = new File("/sdcard/sdt/"+mData[0]+".m");
			if (f!=null && f.exists())
				f.delete();

			for(int i=0; i<(mIdx-1); i++) {
				mData[i] = mData[i+1];
			}
			mIdx = MAX-1;
		}
		mData[mIdx] = Long.parseLong(id);
		mIdx++;

		dmsg req = new dmsg();
		req.to("/upgrade/sync", null);
	}

	public static int ERR_MAX = 1000;
	public static long mErrData[] = new long[ERR_MAX];
	public static int mErrIdx = 0;

	public static Data eLoad(long idx) {
		String s = String.valueOf(idx);
		Data d = new Data();
		dxml p = new dxml();
		p.load("/sdcard/sdt_err/"+s+".xml");
		d.id = p.getText("/sys/id");
		d.name = p.getText("/sys/name");
		d.sex = p.getText("/sys/sex");
		d.nation = p.getText("/sys/nation");
		d.birthday = p.getText("/sys/birthday");
		d.address = p.getText("/sys/address");
		d.depart = p.getText("/sys/depart");
		d.ts = p.getText("/sys/ts");
		d.tp = p.getText("/sys/tp");
		d.data = p.getText("/sys/data");
		d.gate = p.getText("/sys/gate");
		d.photo = "/sdcard/sdt_err/"+s+"_0.jpg";
		d.camera = "/sdcard/sdt_err/"+s+"_1.jpg";
		d.dt = s.substring(0, 4)+"-"+s.substring(4, 6)+"-"+s.substring(6, 8)+" "+s.substring(8, 10)+":"+s.substring(10, 12)+":"+s.substring(12, 14);
		return d;
	}

	public static void eInsert(dxml p, Bitmap b1, Bitmap b2) {
		Date d = new Date();
		String id = String.format("%04d%02d%02d%02d%02d%02d", d.getYear()+1900, d.getMonth()+1, d.getDate(), d.getHours(), d.getMinutes(), d.getSeconds());
		String url = "/sdcard/sdt_err/"+id+".xml";
		p.save(url);
		if (b1 != null)
			utils.save(b1, "/sdcard/sdt_err/"+id+"_0.jpg");
		if (b2 != null)
			utils.save(b2, "/sdcard/sdt_err/"+id+"_1.jpg");
		if (mErrIdx >= ERR_MAX) {
			File f = new File("/sdcard/sdt_err/"+mData[0]+".xml");
			if (f!=null && f.exists())
				f.delete();
			f = new File("/sdcard/sdt_err/"+mData[0]+"_0.jpg");
			if (f!=null && f.exists())
				f.delete();
			f = new File("/sdcard/sdt_err/"+mData[0]+"_1.jpg");
			if (f!=null && f.exists())
				f.delete();
			f = new File("/sdcard/sdt_err/"+mData[0]+".m");
			if (f!=null && f.exists())
				f.delete();

			for(int i=0; i<(mErrIdx-1); i++) {
				mErrData[i] = mErrData[i+1];
			}
			mErrIdx = ERR_MAX-1;
		}
		mErrData[mErrIdx] = Long.parseLong(id);
		mErrIdx++;

		dmsg req = new dmsg();
		req.to("/upgrade/sync", null);
	}
}
