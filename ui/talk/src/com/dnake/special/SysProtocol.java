package com.dnake.special;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import org.json.JSONException;
import org.json.JSONObject;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.Base64;

import com.dnake.misc.HttpConnect;
import com.dnake.panel.FaceCompare;
import com.dnake.special.YmsProtocol;
import com.dnake.v700.dxml;
import com.dnake.v700.utils;

@SuppressLint("DefaultLocale")
public final class SysProtocol {
	public static int mEnable = 0;
	public static String mHost = "";
	public static String mHost2 = "";
	public static String mCode = "";
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

		public int mask; //口罩

		public int from; //0: 本地  1:CMS 2:微信
		public int type;
		public String body;
	}

	public static NetposaHttpd mNetposa = null;

	public static void load() {
		dxml p = new dxml();
		p.load("/dnake/cfg/sdt.xml");
		mEnable = p.getInt("/sys/enable", 0);
		mHost = p.getText("/sys/host", "");
		mHost2 = p.getText("/sys/host2", "");
		mCode = p.getText("/sys/code", "");
		mProtocol = p.getInt("/sys/protocol", 0);

		if (mThread == null) {
			mThread = new Thread(new ProcessThread());
			mThread.start();
		}
		if (mProtocol == 3) {
			if (mNetposa == null) {
				mNetposa = new NetposaHttpd(8080);
				try {
					mNetposa.start();
				} catch (IOException e) {
				}
			}
		}
	}

	public static void save() {
		dxml p = new dxml();
		p.setInt("/sys/enable", mEnable);
		p.setText("/sys/host", mHost);
		p.setText("/sys/host2", mHost2);
		p.setText("/sys/code", mCode);
		p.setInt("/sys/protocol", mProtocol);
		p.save("/dnake/cfg/sdt.xml");

		if (mProtocol == 3) {
			if (mNetposa == null) {
				mNetposa = new NetposaHttpd(8080);
				try {
					mNetposa.start();
				} catch (IOException e) {
				}
			}
		}
	}

	public static void sdtSwipe(FaceCompare.Data d) {
		if (mEnable == 0)
			return;
		if (mProtocol == 1) {
			try {
				JSONObject json = new JSONObject();
				json.put("orgCode", mCode);
				json.put("idCardNo", d.mID);
				json.put("name", d.mName);
				json.put("sex", d.mSex);
				String s = d.mBirthday.substring(0, 4) + "-" + d.mBirthday.substring(4, 6) + "-" + d.mBirthday.substring(6);
				json.put("birthDay", s);
				json.put("address", d.mAddress);

				FaceData fd = new FaceData();
				fd.type = 1;
				fd.body = json.toString();
				mData.add(fd);
			} catch (JSONException e) {
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void sdtVerify(FaceCompare.Data d, boolean result, Bitmap bmp) {
		if (mEnable == 0)
			return;
		if (mProtocol == 1) {
			try {
				Date dt = new Date();
				String ts = String.format("%04d-%02d-%02d %02d:%02d:%02d", dt.getYear() + 1900, dt.getMonth() + 1, dt.getDate(), dt.getHours(), dt.getMinutes(), dt.getSeconds());

				JSONObject json = new JSONObject();
				json.put("orgCode", mCode);
				json.put("idCardNo", d.mID);
				json.put("identifyStatus", result ? "1" : 0);
				json.put("identifyTime", ts);
				if (d.mBmp != null) {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					d.mBmp.compress(Bitmap.CompressFormat.JPEG, 90, os);
					String b64 = "data:image/jpeg;base64,"+Base64.encodeToString(os.toByteArray(), Base64.DEFAULT);
					json.put("idcardImg", b64);
				}
				if (bmp != null) {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.JPEG, 90, os);
					String b64 = "data:image/jpeg;base64," + Base64.encodeToString(os.toByteArray(), Base64.DEFAULT);
					json.put("avatarImg", b64);
				}

				FaceData fd = new FaceData();
				fd.type = 2;
				fd.body = json.toString();
				mData.add(fd);
			} catch (JSONException e) {
			}
		}
	}

	private static void doQXDProtocol(FaceData d) {
		String url = mHost + "/identify/idCardAuth";
		if (d.type == 1)
			url = mHost + "/identify/idCardAuth";
		else if (d.type == 2)
			url = mHost + "/identify/idCardUpload";

		HttpConnect c = new HttpConnect() {
			@Override
			public void onBody(int r, String body) {
				System.err.println(body);
			}
		};
		c.mContentType = "text/plain; charset=utf-8";
		c.post(url, d.body);

		for(int i=0; i<50; i++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			if (c.mFinished)
				break;
		}
		if (c.mResult > 300) //请求失败，重发一次
			c.post(url, d.body);
	}

	private static int mLeUser[] = null;
	private static long mLeTs[] = null;
	private static int mLeMax = 100;

	private static void doLeProtocol(FaceData d) {
		if (d.black)
			return;

		if (mLeUser == null) {
			mLeUser = new int[mLeMax];
			mLeTs = new long[mLeMax];
			for (int i = 0; i < mLeMax; i++) {
				mLeUser[i] = 0;
				mLeTs[i] = 0;
			}
		}

		for (int i = 0; i < mLeMax; i++) {
			if (mLeUser[i] != 0 && Math.abs(System.currentTimeMillis() - mLeTs[i]) >= 1 * 60 * 1000) {
				mLeUser[i] = 0;
				mLeTs[i] = 0;
			}
			if (d.id == mLeUser[i])
				return;
		}
		for (int i = 0; i < mLeMax; i++) {
			if (mLeUser[i] == 0) {
				mLeUser[i] = d.id;
				mLeTs[i] = System.currentTimeMillis();
				break;
			}
		}

		try {
			JSONObject json = new JSONObject();
			json.put("type", "tts");

			JSONObject obj = new JSONObject();
			obj.put("sn", mCode);
			json.put("devices", obj);

			obj = new JSONObject();
			obj.put("text", "欢迎" + d.name + "光临乐融智慧家庭体验馆");
			json.put("data", obj);
			HttpConnect c = new HttpConnect() {
				@Override
				public void onBody(int result, String body) {
					System.err.println("result: " + result);
					System.err.println("body:");
					System.err.println(body);
				}
			};
			c.mContentType = "application/json; charset=utf-8";
			c.post(mHost, json.toString());
		} catch (JSONException e) {
		}
	}

	private static void doNetposaProtocol(FaceData d) {
		try {
			dxml net_c = new dxml();
			net_c.load("/dnake/data/netposa/" + d.id + ".xml");

			JSONObject json = new JSONObject();
			json.put("DeviceID", mCode);

			String mac = utils.getLocalMac();
			mac.replaceAll(":", "");
			json.put("deviceMAC", mac);
			json.put("cameraIP", utils.getLocalIp());

			json.put("faceID", net_c.getText("/sys/faceID"));
			json.put("credentialType", net_c.getInt("/sys/credentialType", 1));
			json.put("credentialNo", net_c.getText("/sys/credentialNo"));

			json.put("similarity", String.valueOf(d.sim / 100.0));
			json.put("compareResult", (d.sim != 0) ? "1" : "0");
			json.put("timeStamp", d.ts);
			json.put("captureType", "1");

			if (d.bmp != null) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				d.bmp.compress(Bitmap.CompressFormat.JPEG, 80, os);
				String b64 = Base64.encodeToString(os.toByteArray(), Base64.DEFAULT);
				json.put("faceCapture", b64); // 人脸小图
				json.put("Width", d.bmp.getWidth());
				json.put("Height", d.bmp.getHeight());
			}
			if (d.global != null && d.global.jpeg != null) {
				String b64 = Base64.encodeToString(d.global.jpeg, Base64.DEFAULT);
				json.put("doorCapture", b64); // 全景图
				json.put("LeftTopX", (double) d.global.f_x);
				json.put("LeftTopY", (double) d.global.f_y);
				json.put("RightBtmX", (double) (d.global.f_x + d.global.f_w));
				json.put("RightBtmY", (double) (d.global.f_y + d.global.f_h));
				json.put("backgroundWidth", d.global.width);
				json.put("backgroundHeight", d.global.height);
			}

			HttpConnect c = new HttpConnect() {
				@Override
				public void onBody(int result, String body) {
				}
			};
			c.mContentType = "application/json; charset=utf-8";
			c.post(mHost, json.toString());
			for (int i = 0; i < 40; i++) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				if (c.mFinished)
					break;
			}
		} catch (JSONException e) {
		}
	}

	private static Queue<FaceData> mData = new LinkedList<FaceData>();
	private static Thread mThread = null;

	private static class ProcessThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				if (mData.size() > 0) {
					FaceData d = mData.poll();
					if (mProtocol == 1) {
						doQXDProtocol(d);
					} else if (mProtocol == 2) { // 乐视协议
						doLeProtocol(d);
					} else if (mProtocol == 3) { //上海东方网力
						doNetposaProtocol(d);
					}

					if (YmsProtocol.m_enabled != 0) {
						YmsProtocol.doLogger(d);
					}
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public static void face(FaceData d) {
		mData.add(d);
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
