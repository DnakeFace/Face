package com.dnake.misc;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import Invs.Termb;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dnake.panel.FaceLabel;
import com.dnake.panel.R;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;
import com.dnake.v700.utils;
import com.dnake.widget.Button2;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.ToolUtils;
import com.zkteco.android.biometric.nidfpsensor.NIDFPFactory;
import com.zkteco.android.biometric.nidfpsensor.NIDFPSensor;
import com.zkteco.android.biometric.nidfpsensor.exception.NIDFPException;

public class FaceCompare {
	public static class Data {
		public String mName; // 姓名
		public String mSex; // 性别
		public String mNation; // 民族
		public String mBirthday; // 生日
		public String mAddress; // 户口所在地
		public String mDepart; // 发证机关
		public String mSTs; // 有效时间 开始
		public String mSTp; // 有效时间 结束
		public String mID; // 身份证号
		public String mUrl; // 照片
		public Bitmap mBmp;// 图片
	}

	private static Handler mEvent;
	public static List<Data> mData = new LinkedList<Data>();

	public static long mTs = 0; // 开始比对时间
	public static String mId; // 身份证ID
	public static Bitmap mBmp; // 识别
	public static int mGate; // 阈值
	public static int mSimilarity; // 相似度
	public static long mWaitTs = 0; // 结果显示时间
	public static int mFinger = 0; // 是否指纹识别

	public static void start() {
		mEvent = new Handler() {
			@Override
			public void handleMessage(Message m) {
				super.handleMessage(m);
				if (m.what == 100)
					onDetectProcess((Data)m.obj);
				else if (m.what == 101)
					onResultProcess();
			}
		};
	}

	public static void onStart() {
		if (sys.sdt() == 0) {
			mLogo.setVisibility(View.GONE);
			mOsdTips.setVisibility(View.GONE);
		} else {
			mLogo.setVisibility(View.VISIBLE);
			mOsdTips.setVisibility(View.VISIBLE);
			mOsdPrompt.setText("请将证件放到读卡器上");
		}
	}

	public static void onStop() {
		onFingerStop();
	}

	public static void onTimer() {
		if (Math.abs(System.currentTimeMillis() - mTs) > 5 * 1000) {
			if (mData.size() > 0) {
				onFingerStop();

				Data d = mData.get(0);
				dxml p = new dxml();
				p.setInt("/sys/data", mSimilarity);
				p.setInt("/sys/gate", mGate);
				p.setText("/sys/name", d.mName);
				p.setText("/sys/sex", d.mSex);
				p.setText("/sys/nation", d.mNation);
				p.setText("/sys/birthday", d.mBirthday);
				p.setText("/sys/address", d.mAddress);
				p.setText("/sys/depart", d.mDepart);
				p.setText("/sys/ts", d.mSTs);
				p.setText("/sys/tp", d.mSTp);
				p.setText("/sys/id", d.mID);
				SDTLogger.eInsert(p, d.mBmp, mBmp);
				SysProtocol.sdtVerify(d, false, mBmp);

				mOsdTitle.setText("证件对比失败");
				mOsdPrompt.setText("证件对比失败");
				mOsdResult.setVisibility(View.VISIBLE);
				mWaitTs = System.currentTimeMillis();

				dmsg req = new dmsg();
				req.to("/face/sdt/reset", null);
				onFingerStop();
				mData.clear();

				Handler e = new Handler() {
					@Override
					public void handleMessage(Message msg) {
						super.handleMessage(msg);
						Sound.play(Sound.sdt_failed, false);
					}
				};
				e.sendEmptyMessageDelayed(0, 100);
			}
		}
		if (mWaitTs != 0 && Math.abs(System.currentTimeMillis() - mWaitTs) > 3 * 1000) {
			mWaitTs = 0;
			mData.clear();
			mOsdP1.setImageBitmap(null);
			mOsdP2.setImageBitmap(null);
			mOsdName.setText("");
			mOsdId.setText("");
			mOsdResult.setVisibility(View.INVISIBLE);
			onFingerStop();

			mOsdPrompt.setText("请将证件放到读卡器上");
		}
	}

	public static RelativeLayout mOsdResult = null;
	public static RelativeLayout mOsdTips = null;
	public static TextView mOsdPrompt = null;
	public static TextView mOsdTitle = null;
	public static TextView mOsdId = null;
	public static TextView mOsdName = null;
	public static ImageView mOsdP1 = null;
	public static ImageView mOsdP2 = null;
	public static ImageView mLogo = null;

	public static void onOsdStart(RelativeLayout layout) {
		mOsdResult = (RelativeLayout) layout.findViewById(R.id.osd_sid_result);
		mOsdResult.setVisibility(View.INVISIBLE);

		mOsdTips = (RelativeLayout) layout.findViewById(R.id.osd_sid_tips);
		mOsdPrompt = (TextView) layout.findViewById(R.id.osd_sid_prompt);
		mOsdTitle = (TextView) layout.findViewById(R.id.osd_sid_result_title);

		mOsdName = (TextView) layout.findViewById(R.id.osd_sid_result_name);
		mOsdId = (TextView) layout.findViewById(R.id.osd_sid_result_id);
		mOsdP1 = (ImageView) layout.findViewById(R.id.osd_sid_result_p1);
		mOsdP2 = (ImageView) layout.findViewById(R.id.osd_sid_result_p2);

		mLogo = (ImageView) layout.findViewById(R.id.osd_sid_logo);

		Button2 b = (Button2) layout.findViewById(R.id.osd_sid_back);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (FaceLabel.mContext != null)
					FaceLabel.mContext.finish();
			}
		});
	}

	public static void onDetect(String body) {
		ProcessThread pt = new ProcessThread();
		pt.body = body;
		Thread t = new Thread(pt);
		t.start();
	}

	private static void onDetectProcess(Data d) {
		mOsdName.setText(d.mName);
		mOsdId.setText(d.mID);
		mOsdP1.setImageBitmap(d.mBmp);
		mOsdP2.setImageBitmap(null);
		mOsdResult.setVisibility(View.INVISIBLE);
		mOsdPrompt.setText("证件信息读取成功，请正视屏幕");
		mTs = System.currentTimeMillis();
		mWaitTs = 0;
		onFingerStart();
		SysProtocol.sdtSwipe(d);
	}

	@SuppressWarnings("deprecation")
	private static void onResultProcess() {
		Date dt = new Date();
		int ts = (dt.getYear() + 1900) * 10000 + (dt.getMonth() + 1) * 100 + dt.getDate();
		boolean ok = false, timeout = false;
		for (int i = 0; i < mData.size(); i++) {
			Data d = mData.get(i);
			if (mId.equalsIgnoreCase(d.mID)) {
				if (!d.mSTp.equals("长期") && ts > Integer.parseInt(d.mSTp)) {
					timeout = true;
					mOsdTitle.setText("证件已过期");
					mOsdPrompt.setText("证件已过期");

					dxml p2 = new dxml();
					p2.setInt("/sys/data", mSimilarity);
					p2.setInt("/sys/gate", mGate);
					p2.setText("/sys/name", d.mName);
					p2.setText("/sys/sex", d.mSex);
					p2.setText("/sys/nation", d.mNation);
					p2.setText("/sys/birthday", d.mBirthday);
					p2.setText("/sys/address", d.mAddress);
					p2.setText("/sys/depart", d.mDepart);
					p2.setText("/sys/ts", d.mSTs);
					p2.setText("/sys/tp", d.mSTp);
					p2.setText("/sys/id", d.mID);
					SDTLogger.eInsert(p2, d.mBmp, mBmp);
					SysProtocol.sdtVerify(d, false, mBmp);
				} else {
					mOsdP2.setImageBitmap(mBmp);

					if (mSimilarity >= mGate) {
						ok = true;
						mOsdResult.setVisibility(View.VISIBLE);
						mWaitTs = System.currentTimeMillis();

						if (mFinger != 0) {
							mOsdTitle.setText("指纹对比通过");
							mOsdPrompt.setText("指纹对比通过");
						} else {
							mOsdTitle.setText("证件对比通过");
							mOsdPrompt.setText("证件对比通过");
						}

						dxml p2 = new dxml();
						p2.setInt("/sys/data", mSimilarity);
						p2.setInt("/sys/gate", mGate);
						p2.setText("/sys/name", d.mName);
						p2.setText("/sys/sex", d.mSex);
						p2.setText("/sys/nation", d.mNation);
						p2.setText("/sys/birthday", d.mBirthday);
						p2.setText("/sys/address", d.mAddress);
						p2.setText("/sys/depart", d.mDepart);
						p2.setText("/sys/ts", d.mSTs);
						p2.setText("/sys/tp", d.mSTp);
						p2.setText("/sys/id", d.mID);
						SDTLogger.insert(p2, d.mBmp, mBmp);
						SysProtocol.sdtVerify(d, true, mBmp);

						dmsg req = new dmsg();
						req.to("/face/unlock", null);
					}
				}
				break;
			}
		}
		if (ok || timeout) {
			mData.clear();
			onFingerStop();
			Handler e = new Handler() {
				@Override
				public void handleMessage(Message m) {
					super.handleMessage(m);
					if (m.what == 1) {
						Sound.play(Sound.sdt_invalid, false);
					} else {
						onLedGreen();
						Sound.play(Sound.sdt_success, false);
					}
				}
			};
			e.sendEmptyMessageDelayed(timeout ? 1 : 0, 100);
		}
	}

	public static void onResult(String body) {
		dxml p = new dxml(body);
		mId = p.getText("/params/id");
		mSimilarity = p.getInt("/params/data", 0);
		mGate = p.getInt("/params/gate", 0);
		mBmp = BitmapFactory.decodeFile(p.getText("/params/url"));
		if (mEvent != null)
			mEvent.sendEmptyMessage(101);
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

	private static class ProcessThread implements Runnable {
		public String body;

		@Override
		public void run() {
			mFingerData = utils.readFile("/var/wltlib/zw.txt");
			byte[] wlt = utils.readFile("/var/wltlib/zp.wlt");
			if (wlt != null) {
				dxml p = new dxml();
				p.parse(body);

				byte[] bmp = Termb.Wlt2Bmp(wlt);
				utils.writeFile(bmp, "/var/wltlib/zp.bmp");
				Runtime rt = Runtime.getRuntime();
				try {
					rt.exec("chmod 0777 /var/wltlib/zp.bmp").waitFor();
				} catch (Exception e) {
				}

				if (FaceLabel.mContext == null) {
					Intent it = new Intent(SysTalk.mContext, FaceLabel.class);
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					SysTalk.mContext.startActivity(it);
				}

				Data d = new Data();
				d.mName = p.getText("/params/name");
				d.mSex = p.getText("/params/sex");
				d.mNation = p.getText("/params/nation");
				d.mBirthday = p.getText("/params/birthday");
				d.mAddress = p.getText("/params/address");
				d.mDepart = p.getText("/params/depart");
				d.mSTs = p.getText("/params/ts");
				d.mSTp = p.getText("/params/tp");
				d.mID = p.getText("/params/id");
				d.mUrl = "/var/wltlib/zp.bmp";
				d.mBmp = BitmapFactory.decodeFile(d.mUrl);
				mData.add(d);

				dxml p2 = new dxml();
				dmsg req = new dmsg();
				p2.setText("/params/url", "/var/wltlib/zp.bmp");
				p2.setText("/params/id", d.mID);
				req.to("/face/sdt/start", p2.toString());

				for(int i=0; i<20; i++) {
					if (FaceLabel.mContext != null)
						break;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
				if (mEvent != null) {
					Message m = mEvent.obtainMessage(100, d);
					mEvent.sendMessage(m);
				}
			}
		}
	}

	private static NIDFPSensor mNIDFPSensor;
	private static boolean mFingerRun = false;
	private static byte[] mFingerData;

	private static class FingerThread extends Thread {
		@Override
		public void run() {
			byte fdata[] = new byte[1024];
			byte[] fraw = new byte[256 * 360];
			byte[] fbak = new byte[256 * 360];
			System.arraycopy(mFingerData, 0, fdata, 0, 1024);

			mFingerRun = true;
			while (mFingerRun) {
				boolean ok = false;
				try {
					mNIDFPSensor.GetFPRawData(0, fraw);
					byte[] quality = new byte[1];
					int ret = mNIDFPSensor.getQualityScore(fraw, quality);
					if (ret!=1 || quality[0]<45) {
					} else {
						System.arraycopy(fraw, 0, fbak, 0, fraw.length);

						byte[] finger = new byte[512];
		                System.arraycopy(fdata, 0, finger, 0, 512);
		                float score = mNIDFPSensor.ImageMatch(0, fraw, finger);
		                if (score > 0.99) {
		                	ok = true;
		                } else {
		                	System.arraycopy(fdata, 512, finger, 0, 512);
		                    score = mNIDFPSensor.ImageMatch(0, fraw, finger);
		                    if (score > 0.99) {
		                    	ok = true;
		                    }
		                }
					}
				} catch (NIDFPException e) {
					e.printStackTrace();

					int usb = fingerUsb()-2;
					dxml p = new dxml();
					dmsg req = new dmsg();
					p.setInt("/params/index", usb);
					p.setInt("/params/onoff", 0);
					req.to("/control/v170/usb", p.toString());

					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
					}

					p.setInt("/params/index", usb);
					p.setInt("/params/onoff", 1);
					req.to("/control/v170/usb", p.toString());
					break;
				}

				if (ok) {
					if (mData.size() > 0) {
						Data d = mData.get(0);
						Bitmap bmp = ToolUtils.renderCroppedGreyScaleBitmap(fbak, 256, 360);
						if (bmp != null) {
							utils.save(bmp, "/var/finger.jpg");

							mId = d.mID;
							mSimilarity = 99;
							mGate = 99;
							mBmp = bmp;
							if (mEvent != null)
								mEvent.sendEmptyMessage(101);
						}
					}
					break;
				}
				for(int i=0; i<10 && mFingerRun; i++) {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
					}
				}
			}

			try {
				mNIDFPSensor.close(0);
			} catch (NIDFPException e) {
			}
			mNIDFPSensor = null;
			mFingerData = null;
		}
	}

	public static boolean onFingerStart() {
		if (!mFingerRun && mFingerData != null) {
			Map<String, Object> fp = new HashMap<String, Object>();
			fp.put(ParameterHelper.PARAM_KEY_VID, 4292);
			fp.put(ParameterHelper.PARAM_KEY_PID, 33230);
			mNIDFPSensor = NIDFPFactory.createNIDFPSensor(FaceLabel.mContext, TransportType.USBSCSI, fp);
			try {
				mNIDFPSensor.open(0);
			} catch (NIDFPException e) {
				return false;
			}
			new FingerThread().start();
			return true;
		}
		return false;
	}

	public static void onFingerStop() {
		mFingerRun = false;
		mFingerData = null;
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
	}

	public static int fingerUsb() {
		String id = "81ce";
		for(int i=1; i<5; i++) {
			String s = "/sys/bus/usb/devices/4-1."+i+"/idProduct";
			byte d[] = utils.readFile(s);
			if (d!=null) {
				boolean ok = true;
				for(int k=0; k<4; k++) {
					if (d[k] != id.getBytes()[k]) {
						ok = false;
						break;
					}
				}
				if (ok)
					return i;
			}
		}
		return -1;
	}
}
