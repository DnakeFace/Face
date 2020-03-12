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
import android.graphics.Color;
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

	public static RelativeLayout mFaceCross1080p = null;
	public static RelativeLayout mFaceCross720p = null;
	public static TextView mFaceThermal = null;

	public static SysProtocol.FaceData mFaceData[] = new SysProtocol.FaceData[4];
	public static ImageView mOsdFace[] = new ImageView[4];
	public static TextView mOsdText[] = new TextView[4];

	public static Queue<SysProtocol.FaceData> mResult = new LinkedList<SysProtocol.FaceData>();

	private static int mThermalMode = -1;

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

		mFaceCross1080p = (RelativeLayout) layout.findViewById(R.id.osd_face_cross_1080);
		mFaceCross720p = (RelativeLayout) layout.findViewById(R.id.osd_face_cross_720);
		mFaceThermal = (TextView) layout.findViewById(R.id.osd_face_thermal);

		if (sys.lcd() != 0 && sys.sdt() == 0) {
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

		ThermalData thermal = mThermal.poll();
		SysProtocol.FaceData d = mResult.poll();
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
				if (thermal == null) {
					if (d.mask == -1 || d.mask == 1) {
						Sound.play(Sound.OrderFaceOK);
					} else {
						mFaceMaskTs = System.currentTimeMillis();
						Sound.play(Sound.OrderFaceMaskErr);
					}
				}
			}
			mResultName.setText(mOsdName + d.name);
			mResultSim.setText(mOsdSimilarity + d.sim);
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

		if (mCaptureHave) {
			onCaptureDisplay();
			mCaptureHave = false;
		}

		if (mResultWaitTs != 0 && Math.abs(System.currentTimeMillis() - mResultWaitTs) > 5 * 1000) {
			mResultWaitTs = 0;
			mResultLayout.setVisibility(View.INVISIBLE);
		}

		if (mThermalMode != sys.thermal()) {
			mThermalMode = sys.thermal();
			if (mThermalMode == 1) {
				if (SysTalk.mCameraHeight == 1080) {
					if (mFaceCross1080p != null)
						mFaceCross1080p.setVisibility(View.VISIBLE);
				} else {
					if (mFaceCross720p != null)
						mFaceCross720p.setVisibility(View.VISIBLE);
				}
				if (mFacePrompt != null)
					mFacePrompt.setText(R.string.face_normal_thermal);
			}
		}

		if (thermal != null && mThermalWaitTs == 0) {
			if (mFaceThermal != null) {
				String mask = "";
				if (thermal.mValue+0.049 > thermal.mThreshold) {
					mFaceThermal.setTextColor(Color.rgb(255, 0, 0));
					Sound.play(Sound.thermal_alarm, false);
				} else {
					mFaceThermal.setTextColor(Color.rgb(0, 0, 255));
					if (thermal.mData != null) {
						if (thermal.mData.mask == 0) {
							mFaceMaskTs = System.currentTimeMillis();
							Sound.play(Sound.OrderFaceMaskErr);
							mFaceMaskTs = System.currentTimeMillis();
							mask = mOsdMaskErr;
							mFaceThermal.setTextColor(Color.rgb(255, 0, 0));
						} else {
							Sound.play(Sound.OrderThermalOK);
						}
					} else {
						Sound.play(Sound.OrderThermalOK);
					}
				}
				String s = SysTalk.mContext.getString(R.string.face_osd_thermal) + String.format("%.1f ℃", thermal.mValue);
				if (mask.length() > 0)
					s += " " + mask;
				mFaceThermal.setText(s);
			}
			mThermalWaitTs = System.currentTimeMillis();
		} else if (mThermalWaitTs != 0 && sys.ts(mThermalWaitTs) > 3000) {
			mThermalWaitTs = 0;
			if (mFaceThermal != null) {
				mFaceThermal.setText("");
			}
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
					if (mFaceData[i].name.getBytes().length == mFaceData[i].name.length()) { // 全部是ASCII字符
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
						mOsdText[n].setText(s + " - " + mFaceData[i].sim);
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

		for (int i = 0; i < (mCaptureData.length - 1); i++) {
			mCaptureData[i] = mCaptureData[i + 1];
		}
		int n = mCaptureData.length - 1;
		mCaptureData[n] = d;
		mCaptureData[n].name = String.format(mOsdCapture, mCaptureCount);
		mCaptureHave = true;
	}

	public static void onCaptureDisplay() {
		for (int i = 0; i < mCaptureData.length; i++) {
			if (mCaptureData[i] != null) {
				int n = (mCaptureData.length - 1) - i;
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

	public static class ThermalData {
		public float mValue;
		public float mThreshold;
		SysProtocol.FaceData mData;
	}
	public static Queue<ThermalData> mThermal = new LinkedList<ThermalData>();
	public static long mThermalWaitTs = 0;

	public static void onThermal(float value, float threshold, SysProtocol.FaceData fd) {
		if (value > 30) {
			ThermalData d = new ThermalData();
			d.mValue = value;
			d.mThreshold = threshold;
			d.mData = fd;
			mThermal.offer(d);
		}
	}
}
