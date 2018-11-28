package com.dnake.misc;

import java.util.Date;

import com.dnake.misc.SysProtocol.FaceData;
import com.dnake.panel.R;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;
import com.dnake.v700.utils;

import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FaceNormal {
	public static LinearLayout mLeftData = null;
	public static LinearLayout mRightData = null;

	public static RelativeLayout mResultLayout = null;
	public static TextView mResultName;
	public static TextView mResultSim;
	public static TextView mResultStatus;
	public static ImageView mResultBmp;

	public static FaceData mFaceData[] = new FaceData[4];
	public static ImageView mOsdFace[] = new ImageView[4];
	public static TextView mOsdText[] = new TextView[4];

	public static boolean mFaceHave = false;
	public static int mFaceUid = -1;
	public static int mFaceSim;
	public static int mFaceBlack = 0;
	public static boolean mFaceCms = false;
	public static String mFaceUrl;
	public static Date mFaceTs;
	public static SysProtocol.FaceGlobal mFaceGlobal;

	public static void onStart() {
		if (sys.lcd() != 0) {
			mLeftData.setVisibility(View.GONE);
			mRightData.setVisibility(View.GONE);
		} else {
			if (sys.sdt() != 0) {
				mLeftData.setVisibility(View.GONE);
				mRightData.setVisibility(View.GONE);
			} else {
				mLeftData.setVisibility(View.VISIBLE);
				mRightData.setVisibility(View.VISIBLE);
			}
		}
		mResultLayout.setVisibility(View.INVISIBLE);
		onResultDisplay();
		onCaptureDisplay();
	}

	public static void onOsdStart(RelativeLayout layout) {
		mLeftData = (LinearLayout) layout.findViewById(R.id.osd_face_data);
		mRightData = (LinearLayout) layout.findViewById(R.id.osd_face_capture);

		for (int i = 0; i < 4; i++) {
			mOsdFace[i] = (ImageView) layout.findViewById(R.id.osd_face_f0 + i * 2);
			mOsdText[i] = (TextView) layout.findViewById(R.id.osd_face_t0 + i * 2);

			mCaptureFace[i] = (ImageView) layout.findViewById(R.id.osd_face_cf0 + i * 2);
			mCaptureText[i] = (TextView) layout.findViewById(R.id.osd_face_ct0 + i * 2);
		}

		mResultLayout = (RelativeLayout) layout.findViewById(R.id.osd_face_result);
		mResultBmp = (ImageView) layout.findViewById(R.id.osd_face_result_p);
		mResultName = (TextView) layout.findViewById(R.id.osd_face_result_name);
		mResultSim = (TextView) layout.findViewById(R.id.osd_face_result_sim);
		mResultStatus = (TextView) layout.findViewById(R.id.osd_face_result_status);
	}

	private static long mWaitTs = 0;

	public static void onTimer() {
		if (mResultLayout == null)
			return;

		if (mFaceHave) {
			String name = "";
			String identity = "";
			boolean black = false;

			if (mFaceCms) {
				name = String.valueOf(mFaceUid);
			} else {
				dxml p = new dxml();
				if (mFaceBlack != 0) {
					p.load("/dnake/data/black/" + mFaceUid + ".xml");
					black = true;
				} else {
					p.load("/dnake/data/user/" + mFaceUid + ".xml");
				}
				name = p.getText("/sys/name");
				identity = p.getText("/sys/identity");
			}

			SysProtocol.FaceData d = new SysProtocol.FaceData();
			d.id = mFaceUid;
			d.name = name;
			d.bmp = BitmapFactory.decodeFile(mFaceUrl);
			d.sim = mFaceSim;
			d.identity = identity;
			d.black = black;
			d.global = mFaceGlobal;
			for (int i = 0; i < (mFaceData.length - 1); i++) {
				mFaceData[i] = mFaceData[i + 1];
			}
			mFaceData[mFaceData.length - 1] = d;

			mResultName.setText("姓    名：" + name);
			mResultSim.setText("相似度：" + mFaceSim);
			if (black) {
				mResultStatus.setText("状    态：黑名单");
				mResultName.setTextColor(0xFFFF0000);
				mResultSim.setTextColor(0xFFFF0000);
				mResultStatus.setTextColor(0xFFFF0000);
			} else {
				mResultStatus.setText("状    态：正常");
				mResultName.setTextColor(0xFF000000);
				mResultSim.setTextColor(0xFF000000);
				mResultStatus.setTextColor(0xFF000000);
				if (sys.lcd() != 0)
					Sound.play(Sound.unlock, false);
			}
			mResultBmp.setImageBitmap(d.bmp);
			mResultLayout.setVisibility(View.VISIBLE);
			mWaitTs = System.currentTimeMillis();

			onResultDisplay();
			onLedGreen();

			SysProtocol.face(d);

			mFaceHave = false;
		}
		if (mCaptureHave) {
			onCaptureDisplay();
			mCaptureHave = false;
		}

		if (mWaitTs != 0 && Math.abs(System.currentTimeMillis() - mWaitTs) > 5 * 1000) {
			mWaitTs = 0;
			mResultLayout.setVisibility(View.INVISIBLE);
		}
	}

	public static void onResultDisplay() {
		for (int i = 0; i < mOsdFace.length; i++) {
			mOsdFace[i].setImageBitmap(null);
			mOsdText[i].setText("");
		}
		for (int i = 0; i < mFaceData.length; i++) {
			if (mFaceData[i] != null) {
				int n = (mOsdFace.length - 1) - i;
				mOsdFace[n].setImageBitmap(mFaceData[i].bmp);
				if (mFaceData[i].name != null) {
					String s = "";
					if (mFaceData[i].name.getBytes().length == mFaceData[i].name.length()) { // 全部是ASCII字符
						s = mFaceData[i].name;
					} else {
						s = mFaceData[i].name.substring(0, 1);
						for (int k = 1; k < mFaceData[i].name.length(); k++) {
							s += "X";
						}
					}
					mOsdText[n].setText(s + " - " + mFaceData[i].sim);
				}
				if (mFaceData[i].black)
					mOsdText[n].setTextColor(0xFFFF0000);
				else
					mOsdText[n].setTextColor(0xFFFFFFFF);
			}
		}
	}

	public static boolean mCaptureHave = false;
	public static FaceData mCaptureData[] = new FaceData[4];
	public static ImageView mCaptureFace[] = new ImageView[4];
	public static TextView mCaptureText[] = new TextView[4];
	public static int mCaptureCount = 0;

	public static void onFaceCapture(FaceData d) {
		if (mCaptureCount > 10000)
			mCaptureCount = 0;
		mCaptureCount++;

		for (int i = 0; i < (mCaptureData.length - 1); i++) {
			mCaptureData[i] = mCaptureData[i + 1];
		}
		int n = mCaptureData.length - 1;
		mCaptureData[n] = d;
		mCaptureData[n].name = String.format("抓拍%d", mCaptureCount);
		mCaptureHave = true;
	}

	public static void onCaptureDisplay() {
		for (int i = 0; i < mCaptureData.length; i++) {
			if (mCaptureData[i] != null) {
				int n = (mCaptureData.length - 1) - i;
				mCaptureFace[n].setImageBitmap(mCaptureData[i].bmp);
				mCaptureText[n].setText(mCaptureData[i].name);
			} else {
				mCaptureFace[i].setImageBitmap(null);
				mCaptureText[i].setText("");
			}
		}
	}

	private static void onLedGreen() {
		utils.ioctl.rgb(utils.ioctl.G);
		Handler e = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				utils.ioctl.rgb(utils.ioctl.R);
			}
		};
		e.sendEmptyMessageDelayed(0, 3 * 1000);
	}
}
