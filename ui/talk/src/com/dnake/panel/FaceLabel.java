package com.dnake.panel;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;
import com.dnake.v700.utils;
import com.dnake.widget.ZXing;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint({ "DefaultLocale", "SimpleDateFormat", "NewApi" })
@SuppressWarnings("deprecation")
public class FaceLabel extends BaseLabel {
	public static FaceLabel mContext = null;
	public static RelativeLayout mOsdLayout = null;
	public static long mTs = 0;
	public static Boolean mStartVo = true;
	public static long mTouchTs = 0;

	private long mOsdTs = 0;
	private int mOsdWidth = 0, mOsdHeight = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.face);

		int ui_visibility = this.getWindow().getDecorView().getSystemUiVisibility();
		if ((ui_visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
			this.getWindow().getDecorView().setSystemUiVisibility(ui_visibility | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}

		onOsdStart();
	}

	@Override
	public void onTimer() {
		super.onTimer();

		if (Math.abs(System.currentTimeMillis() - mTs) < 2 * 60 * 1000) {
			WakeTask.acquire();
		} else {
			finish();
			return;
		}

		if (Math.abs(System.currentTimeMillis() - mOsdTs) > 1000) {
			mOsdTs = System.currentTimeMillis();
			Date d = new Date();
			String s = String.format("%04d-%02d-%02d %02d:%02d:%02d", d.getYear() + 1900, d.getMonth() + 1, d.getDate(), d.getHours(), d.getMinutes(), d.getSeconds());
			TextView tv = (TextView) mOsdLayout.findViewById(R.id.osd_ts);
			tv.setText(s);
		}

		int ui_visibility = this.getWindow().getDecorView().getSystemUiVisibility();
		if ((ui_visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
			this.getWindow().getDecorView().setSystemUiVisibility(ui_visibility | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}

		if (mOsdWidth != mOsdLayout.getWidth() || mOsdHeight != mOsdLayout.getHeight()) {
			mOsdWidth = mOsdLayout.getWidth();
			mOsdHeight = mOsdLayout.getHeight();
			mStartVo = true;
		}

		if (mStartVo && this.hasWindowFocus()) {
			int w = mOsdLayout.getWidth();
			int h = mOsdLayout.getHeight();
			if (w > 16 && h > 16) {
				dmsg req = new dmsg();
				dxml p = new dxml();
				p.setInt("/params/x", 0);
				p.setInt("/params/y", 0);
				p.setInt("/params/w", w);
				p.setInt("/params/h", h);
				req.to("/face/osd", p.toString());
				mStartVo = false;
			}
		}

		if (mFaceLived.size() > 0) {
			FaceLivedData d = mFaceLived.poll();
			if (d.mode == 0) {
				Bitmap bm = BitmapFactory.decodeFile(d.url);
				if (mFaceLivedBmp != null)
					mFaceLivedBmp.setImageBitmap(doBitmapCirle(bm));
				if (mFaceLivedLayout != null)
					mFaceLivedLayout.setVisibility(View.VISIBLE);
				if (mFaceLivedPrompt != null)
					mFaceLivedPrompt.setVisibility(View.VISIBLE);
				mFaceLivedRunning = true;
			} else {
				if (mFaceLivedPrompt != null)
					mFaceLivedPrompt.setVisibility(View.GONE);
				if (mFaceLivedLayout != null)
					mFaceLivedLayout.setVisibility(View.GONE);
				mFaceLivedRunning = false;
			}
		}
		if (mFaceLivedRunning) {
			if (++mFaceLivedCount > 4) {
				mFaceLivedCount = 0;
			}
			if (mFaceLivedLayout != null) {
				Drawable d = this.getResources().getDrawable(mFaceLivedR[mFaceLivedCount]);
				mFaceLivedLayout.setBackground(d);
			}
		}

		FaceCompare.onTimer();
		FaceNormal.onTimer();
	}

	public static String mWxUuid = "";
	private int mWxRid = 0;
	public void onFaceStart() {

		mContext = this;
		mStartVo = true;
		mTs = System.currentTimeMillis();

		if (mFaceLivedLayout != null) {
			mFaceLivedLayout.setVisibility(View.GONE);
		}
		if (mFaceLivedPrompt != null) {
			mFaceLivedPrompt.setVisibility(View.GONE);
		}
		mOsdLayout.setVisibility(View.VISIBLE);

		FaceNormal.onStart();
		FaceCompare.onStart();

		if (sys.lcd.orientation() != 0) {
			dxml p = new dxml();
			p.load("/dnake/cfg/wx_access.xml");
			int enable = p.getInt("/sys/enable", 0);
			String server = p.getText("/sys/server");
			String uuid = p.getText("/sys/uuid");

			ImageView qr = (ImageView) mOsdLayout.findViewById(R.id.osd_wx_qr2d);
			if (enable != 0 && qr != null) { // 微云门禁二维码
				mWxRid = (int) (Math.random()*1000000);

				String s = "https://" + server + "/weixin/api/wx_scan.php?api=wx_unlock.php&data=" + uuid + "&data2=" + mWxRid;
				qr.setImageBitmap(ZXing.QR2D(s, 100));
				qr.setVisibility(View.VISIBLE);
				mWxUuid = uuid;

				utils.writeFile(String.valueOf(mWxRid).getBytes(), "/var/etc/rid");
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		this.onFaceStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		this.onFaceStart();
	}

	@Override
	public void onStop() {
		super.onStop();
		mContext = null;
		mOsdLayout.setVisibility(View.INVISIBLE);
		mFaceLived.clear();

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setInt("/params/x", 0);
		p.setInt("/params/y", 0);
		p.setInt("/params/w", 0);
		p.setInt("/params/h", 0);
		req.to("/face/osd", p.toString());

		if (mWxRid != 0) {
			mWxRid = (int) (Math.random()*1000000);
			utils.writeFile(String.valueOf(mWxRid).getBytes(), "/var/etc/rid");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onKey(String key) {
		super.onKey(key);
	}

	private boolean mFaceLivedRunning = false;
	private int mFaceLivedCount = 0;
	private static RelativeLayout mFaceLivedLayout;
	private static ImageView mFaceLivedBmp;
	private static TextView mFaceLivedPrompt;
	private static int mFaceLivedR[] = new int[5];

	public static class FaceLivedData {
		public int mode;
		public String url;
	}
	public static Queue<FaceLivedData> mFaceLived = new LinkedList<FaceLivedData>();

	@SuppressLint("InflateParams")
	public void onOsdStart() {
		if (mOsdLayout != null)
			return;

		LayoutInflater inflater = LayoutInflater.from(this.getApplication());
		mOsdLayout = (RelativeLayout) inflater.inflate(R.layout.osd, null);

		WindowManager.LayoutParams p = new WindowManager.LayoutParams();
		p.type = WindowManager.LayoutParams.TYPE_PHONE;
		p.format = PixelFormat.RGBA_8888;
		p.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		p.x = 0;
		p.y = 0;
		p.width = WindowManager.LayoutParams.MATCH_PARENT;
		p.height = WindowManager.LayoutParams.MATCH_PARENT;
		WindowManager wm = (WindowManager) this.getApplication().getSystemService(Application.WINDOW_SERVICE);
		wm.addView(mOsdLayout, p);

		mFaceLivedLayout = (RelativeLayout)mOsdLayout.findViewById(R.id.osd_face_lived);
		mFaceLivedBmp = (ImageView)mOsdLayout.findViewById(R.id.osd_face_lived_bmp);
		mFaceLivedPrompt = (TextView) mOsdLayout.findViewById(R.id.osd_face_lived_prompt);
		mFaceLivedR[0] = R.drawable.face_lived_0;
		mFaceLivedR[1] = R.drawable.face_lived_1;
		mFaceLivedR[2] = R.drawable.face_lived_2;
		mFaceLivedR[3] = R.drawable.face_lived_3;
		mFaceLivedR[4] = R.drawable.face_lived_4;

		FaceNormal.onOsdStart(mOsdLayout);
		FaceCompare.onOsdStart(mOsdLayout);
	}

	public static Bitmap doBitmapCirle(Bitmap bmp) {
		//获取bmp的宽高 小的一个做为圆的直径r
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		int r = Math.min(w, h);
	 
		//创建一个paint
		Paint paint = new Paint();
		paint.setAntiAlias(true);
	 
		//新创建一个Bitmap对象newBitmap 宽高都是r
		Bitmap bm = Bitmap.createBitmap(r, r, Bitmap.Config.ARGB_8888);
	 
		//创建一个使用newBitmap的Canvas对象
		Canvas canvas = new Canvas(bm);
	 
		//创建一个Path对象，path添加一个圆 圆心半径均是r / 2， Path.Direction.CW顺时针方向
		Path path = new Path();
		path.addCircle(r / 2, r / 2, r / 2, Path.Direction.CW);
		//canvas绘制裁剪区域
		canvas.clipPath(path);
		//canvas将图画到留下的圆形区域上
		canvas.drawBitmap(bmp, 0, 0, paint);
		return bm;
	}
}
