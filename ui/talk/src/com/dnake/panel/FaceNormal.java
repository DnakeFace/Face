package com.dnake.panel;

import java.util.LinkedList;
import java.util.Queue;

import com.dnake.misc.Sound;
import com.dnake.misc.SysTalk;
import com.dnake.panel.R;
import com.dnake.special.SysProtocol;
import com.dnake.v700.sys;
import com.dnake.v700.utils;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("DefaultLocale")
public class FaceNormal {
	public static LinearLayout mLeftData = null;
	public static LinearLayout mRightData = null;

	public static RelativeLayout mResultLayout = null;
	public static TextView mResultName;
	public static TextView mResultSim;
	public static TextView mResultStatus;
	public static ImageView mResultBmp;

	public static TextView mFacePrompt = null;

	public static SysProtocol.FaceData mFaceData[] = new SysProtocol.FaceData[4];
	public static ImageView mOsdFace[] = new ImageView[4];
	public static TextView mOsdText[] = new TextView[4];

	public static Queue<SysProtocol.FaceData> mResult = new LinkedList<SysProtocol.FaceData>();

	public static Queue<SysProtocol.PlateResult> mPlateResult = new LinkedList<SysProtocol.PlateResult>();

	public static void onStart() {
		if (sys.lcd.orientation() != 0) {
			mLeftData.setVisibility(View.GONE);
			mRightData.setVisibility(View.GONE);
		} else {
			if (sys.sdt() != 0) {
				mLeftData.setVisibility(View.GONE);
				mRightData.setVisibility(View.GONE);
			}
		}
		mResultLayout.setVisibility(View.INVISIBLE);

		onResultDisplay();
		onCaptureDisplay();
	}

	private static String mOsdName = "";
	private static String mOsdSimilarity = "";
	private static String mOsdWhite = "";
	private static String mOsdBlack = "";
	private static String mOsdCapture = "";
	private static String mOsdMaskErr = "";

	public static void onOsdStart(RelativeLayout layout) {
		mOsdName = SysTalk.mContext.getString(R.string.face_normal_name);
		mOsdSimilarity = SysTalk.mContext.getString(R.string.face_normal_similarity);
		mOsdWhite = SysTalk.mContext.getString(R.string.face_normal_white);
		mOsdBlack = SysTalk.mContext.getString(R.string.face_normal_black);
		mOsdCapture = SysTalk.mContext.getString(R.string.face_normal_capture);
		mOsdMaskErr = SysTalk.mContext.getString(R.string.face_osd_mask_err);

		mLeftData = (LinearLayout) layout.findViewById(R.id.osd_face_data);
		mRightData = (LinearLayout) layout.findViewById(R.id.osd_face_capture);

		mOsdFace[0] = (ImageView) layout.findViewById(R.id.osd_face_f0);
		mOsdText[0] = (TextView) layout.findViewById(R.id.osd_face_t0);
		mCaptureFace[0] = (ImageView) layout.findViewById(R.id.osd_face_cf0);
		mCaptureText[0] = (TextView) layout.findViewById(R.id.osd_face_ct0);

		mOsdFace[1] = (ImageView) layout.findViewById(R.id.osd_face_f1);
		mOsdText[1] = (TextView) layout.findViewById(R.id.osd_face_t1);
		mCaptureFace[1] = (ImageView) layout.findViewById(R.id.osd_face_cf1);
		mCaptureText[1] = (TextView) layout.findViewById(R.id.osd_face_ct1);

		mOsdFace[2] = (ImageView) layout.findViewById(R.id.osd_face_f2);
		mOsdText[2] = (TextView) layout.findViewById(R.id.osd_face_t2);
		mCaptureFace[2] = (ImageView) layout.findViewById(R.id.osd_face_cf2);
		mCaptureText[2] = (TextView) layout.findViewById(R.id.osd_face_ct2);

		mOsdFace[3] = (ImageView) layout.findViewById(R.id.osd_face_f3);
		mOsdText[3] = (TextView) layout.findViewById(R.id.osd_face_t3);
		mCaptureFace[3] = (ImageView) layout.findViewById(R.id.osd_face_cf3);
		mCaptureText[3] = (TextView) layout.findViewById(R.id.osd_face_ct3);

		mResultLayout = (RelativeLayout) layout.findViewById(R.id.osd_face_result);
		mResultBmp = (ImageView) layout.findViewById(R.id.osd_face_result_p);
		mResultName = (TextView) layout.findViewById(R.id.osd_face_result_name);
		mResultSim = (TextView) layout.findViewById(R.id.osd_face_result_sim);
		mResultStatus = (TextView) layout.findViewById(R.id.osd_face_result_status);

		mFacePrompt = (TextView) layout.findViewById(R.id.osd_face_prompt);

		if (sys.lcd.orientation() != 0 && sys.sdt() == 0) { //竖屏未接身份证读卡头
			RelativeLayout r = (RelativeLayout) layout.findViewById(R.id.osd_face_tips);
			if (r != null) {
				r.setVisibility(View.VISIBLE);
			}
			r = (RelativeLayout) layout.findViewById(R.id.osd_face_title);
			if (r != null) {
				r.setVisibility(View.VISIBLE);
			}
		}
	}

	private static long mResultWaitTs = 0;

	public static void onTimer() {
		if (mResultLayout == null)
			return;

		SysProtocol.FaceData d = null;
		synchronized(mResult) {
			d = mResult.poll();
		}
		if (d != null) {
			if (d.black) {
				mResultStatus.setText(mOsdBlack);
				mResultName.setTextColor(0xFFFF0000);
				mResultSim.setTextColor(0xFFFF0000);
				mResultStatus.setTextColor(0xFFFF0000);
			} else {
				mResultStatus.setText(mOsdWhite);
				mResultName.setTextColor(0xFF000000);
				mResultSim.setTextColor(0xFF000000);
				mResultStatus.setTextColor(0xFF000000);
				if (d.mask == -1 || d.mask == 1) {
					Sound.play(Sound.OrderFaceOK);
				} else {
					mFaceMaskTs = System.currentTimeMillis();
					Sound.play(Sound.OrderFaceMaskErr);
				}
			}
			mResultName.setText(mOsdName + d.name);
			mResultSim.setText(mOsdSimilarity + d.sim);

			if (d.bmp == null && d.data != null) {
				d.bmp = BitmapFactory.decodeByteArray(d.data, 0, d.data.length);
				d.data = null;
			}
			mResultBmp.setImageBitmap(d.bmp);

			mResultLayout.setVisibility(View.VISIBLE);

			for (int i = 0; i < (mFaceData.length - 1); i++) {
				mFaceData[i] = mFaceData[i + 1];
			}
			mFaceData[mFaceData.length - 1] = d;

			onResultDisplay();
			onLedGreen();
			SysProtocol.face(d);

			mResultWaitTs = System.currentTimeMillis();
		}

		SysProtocol.PlateResult pd = null;
		synchronized(mPlateResult) {
			pd = mPlateResult.poll();
		}
		if (pd != null) {
			d = new SysProtocol.FaceData();
			d.name = pd.text;
			if (pd.data != null) {
				d.bmp = BitmapFactory.decodeByteArray(pd.data, 0, pd.data.length);
			}
			d.mask = -1;
			d.sim = 0;
			d.type = 100;
			for (int i = 0; i < (mFaceData.length - 1); i++) {
				mFaceData[i] = mFaceData[i + 1];
			}
			mFaceData[mFaceData.length - 1] = d;

			onResultDisplay();
		}

		if (mCaptureHave) {
			onCaptureDisplay();
			mCaptureHave = false;
		}

		if (mResultWaitTs != 0 && Math.abs(System.currentTimeMillis() - mResultWaitTs) > 5 * 1000) {
			mResultWaitTs = 0;
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
				if (mFaceData[i].black) {
					mOsdText[n].setTextColor(0xFFFF0000);
				} else {
					mOsdText[n].setTextColor(0xFFFFFFFF);
				}
				if (mFaceData[i].name != null) {
					String s = "";
					if (mFaceData[i].type == 100 || mFaceData[i].name.getBytes().length == mFaceData[i].name.length()) { // 全部是ASCII字符或车牌
						s = mFaceData[i].name;
					} else {
						s = mFaceData[i].name.substring(0, 1);
						for (int k = 1; k < mFaceData[i].name.length(); k++) {
							s += "X";
						}
					}
					if (mFaceData[i].mask != -1) {
						if (mFaceData[i].mask == 0) {
							mOsdText[n].setText(s + " - " + mFaceData[i].sim + " - " + mOsdMaskErr);
							mOsdText[n].setTextColor(0xFF0000FF);
						} else {
							mOsdText[n].setText(s + " - " + mFaceData[i].sim);
						}
					} else {
						if (mFaceData[i].type == 100) {
							mOsdText[n].setText(s);
						} else {
							mOsdText[n].setText(s + " - " + mFaceData[i].sim);
						}
					}
				}
			}
		}
	}

	public static boolean mCaptureHave = false;
	public static SysProtocol.FaceData mCaptureData[] = new SysProtocol.FaceData[4];
	public static ImageView mCaptureFace[] = new ImageView[4];
	public static TextView mCaptureText[] = new TextView[4];
	public static int mCaptureCount = 0;
	public static long mFaceMaskTs = 0;

	public static void onFaceCapture(SysProtocol.FaceData d) {
		if (mCaptureCount > 10000)
			mCaptureCount = 0;
		mCaptureCount++;

		if (d.mask == 0 && sys.ts(mFaceMaskTs) > 3000) { //没戴口罩
			mFaceMaskTs = System.currentTimeMillis();
			Sound.play(Sound.OrderFaceMaskErr);
		}

		synchronized (mResultLayout) {
			for (int i = 0; i < (mCaptureData.length - 1); i++) {
				mCaptureData[i] = mCaptureData[i + 1];
			}
			int n = mCaptureData.length - 1;
			mCaptureData[n] = d;
			mCaptureData[n].name = String.format(mOsdCapture, mCaptureCount);
			mCaptureHave = true;
		}
	}

	public static void onCaptureDisplay() {
		synchronized (mResultLayout) {
			for (int i = 0; i < mCaptureData.length; i++) {
				if (mCaptureData[i] != null) {
					int n = (mCaptureData.length - 1) - i;
					if (mCaptureData[i].bmp == null && mCaptureData[i].data != null) {
						mCaptureData[i].bmp = BitmapFactory.decodeByteArray(mCaptureData[i].data, 0, mCaptureData[i].data.length);
						mCaptureData[i].data = null;
					}
					mCaptureFace[n].setImageBitmap(mCaptureData[i].bmp);
					mCaptureText[n].setTextColor(0xFFFFFFFF);
					if (mCaptureData[i].mask != -1) {
						if (mCaptureData[i].mask == 0) {
							mCaptureText[n].setText(mOsdMaskErr);
							mCaptureText[n].setTextColor(0xFF0000FF);
						} else {
							mCaptureText[n].setText(mCaptureData[i].name);
						}
					} else {
						mCaptureText[n].setText(mCaptureData[i].name);
					}
				} else {
					mCaptureFace[i].setImageBitmap(null);
					mCaptureText[i].setText("");
				}
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
