package com.dnake.special;

import java.util.LinkedList;
import java.util.Queue;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dnake.panel.FaceCompare;
import com.dnake.special.YmsProtocol;
import com.dnake.v700.dxml;

@SuppressLint("DefaultLocale")
public final class SysProtocol {
	public static int mEnable = 0;
	public static String mHost = "";
	public static String mCode = "";
	public static String mData = "";
	public static int mProtocol = 0;

	public static class FaceGlobal { // 全景抓拍
		public byte[] jpeg;
		public int width;
		public int height;
		public int f_x;
		public int f_y;
		public int f_w;
		public int f_h;
	}

	public static class FaceData {
		public int id;
		public String name;
		public Bitmap bmp;
		public int sim;
		public int channel;
		public long ts;
		public String identity; // 身份证号码
		public boolean black;

		public FaceGlobal global;

		public int from; //0: 本地  1:CMS 2:微信
		public int type;
		public String body;

		public int mask; //口罩

		public byte[] data; //图片文件
	}

	public static class PlateResult {
		public int channel;
		public String text;
		public Bitmap bmp;
		public long ts;
		public byte[] data; //图片文件
	}

	public static class ObjectPosition {
		public int label;
		public int x;
		public int y;
		public int w;
		public int h;
	}

	public static class ObjectData {
		public int mChannel;
		public Queue<ObjectPosition> mObject = new LinkedList<ObjectPosition>();
		public byte[] mData;
		public long mTs;
	}

	public static void load() {
		dxml p = new dxml();
		p.load("/dnake/cfg/sdt.xml");
		mEnable = p.getInt("/sys/enable", 0);
		mHost = p.getText("/sys/host", "");
		mCode = p.getText("/sys/code", "");
		mData = p.getText("/sys/data", "");
		mProtocol = p.getInt("/sys/protocol", 0);

		if (mThread == null) {
			mThread = new Thread(new ProcessThread());
			mThread.start();
		}
	}

	public static void save() {
		dxml p = new dxml();
		p.setInt("/sys/enable", mEnable);
		p.setText("/sys/host", mHost);
		p.setText("/sys/code", mCode);
		p.setText("/sys/data", mData);
		p.setInt("/sys/protocol", mProtocol);
		p.save("/dnake/cfg/sdt.xml");
	}

	public static void sdtSwipe(FaceCompare.Data d) {
	}

	public static void sdtVerify(FaceCompare.Data d, boolean result, Bitmap bmp) {
	}

	private static Queue<FaceData> mFaceData = new LinkedList<FaceData>();
	private static Queue<ObjectData> mObjectData = new LinkedList<ObjectData>();
	private static Queue<PlateResult> mPlateData = new LinkedList<PlateResult>();
	private static Thread mThread = null;

	private static class ProcessThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				if (mFaceData.size() > 0) {
					FaceData d = mFaceData.poll();
					if (d == null)
						continue;

					if (d.bmp == null && d.data != null && d.data.length > 0) {
						d.bmp = BitmapFactory.decodeByteArray(d.data, 0, d.data.length);
						d.data = null;
					}

					if (YmsProtocol.m_enabled != 0) {
						YmsProtocol.doLogger(d);
					}
				}
				if (mObjectData.size() > 0) {
					//ObjectData d = mObjectData.poll();
					mObjectData.poll();
				}
				if (mPlateData.size() > 0) {
					//PlateResult d = mPlateData.poll();
					mPlateData.poll();
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public static void face(FaceData d) {
		mFaceData.add(d);
	}

	public static void object(ObjectData d) {
		if (mEnable != 0) {
			mObjectData.add(d);
		}
	}

	public static void plate(PlateResult d) {
		if (mEnable != 0) {
			mPlateData.add(d);
		}
	}

	public static String displayName(int id) {
		String name = null;
		JSONObject obj = YmsProtocol.queryFaceId(id);
		if (obj != null) {
			try {
				name = obj.getString("nickName");
			} catch (JSONException e) {
			}
		}
		return name;
	}
}
