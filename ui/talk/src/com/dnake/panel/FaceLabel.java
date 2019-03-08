package com.dnake.panel;

import java.util.Date;

import com.dnake.misc.FaceCompare;
import com.dnake.misc.FaceNormal;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;
import com.dnake.widget.ZXing;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
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

	private long mOsdTs = 0;
	private int mOsdWidth = 0, mOsdHeight = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.face);

		if (sys.lcd() != 0 && this.getRequestedOrientation() == 0) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		onOsdStart();
	}

	@Override
	public void onTimer() {
		super.onTimer();

		if (sys.h3c() != 0) // H3C模式不退出识别界面
			mTs = System.currentTimeMillis();
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

		if (this.getWindow().getDecorView().getSystemUiVisibility() != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
			this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}

		if (mOsdWidth != mOsdLayout.getWidth() || mOsdHeight != mOsdLayout.getHeight()) {
			mOsdWidth = mOsdLayout.getWidth();
			mOsdHeight = mOsdLayout.getHeight();
			mStartVo = true;
		}

		if (mStartVo) {
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

		FaceCompare.onTimer();
		FaceNormal.onTimer();
	}

	public static String mWxUuid = "";
	@Override
	public void onStart() {
		super.onStart();
		this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		mContext = this;
		mStartVo = true;
		mTs = System.currentTimeMillis();
		mOsdLayout.setVisibility(View.VISIBLE);

		FaceNormal.onStart();
		FaceCompare.onStart();

		if (sys.lcd() != 0) {
			dxml p = new dxml();
			p.load("/dnake/cfg/wx_access.xml");
			ImageView qr = (ImageView) mOsdLayout.findViewById(R.id.osd_wx_qr2d);
			if (p.getInt("/sys/enable", 0) != 0 && qr != null) { // 微云门禁二维码
				String server = p.getText("/sys/server");
				String uuid = p.getText("/sys/uuid");
				String s = "http://" + server + "/weixin/api/wx_scan.php?api=wx_unlock.php&data=" + uuid;
				qr.setImageBitmap(ZXing.QR2D(s, 100));
				qr.setVisibility(View.VISIBLE);
				mWxUuid = uuid;
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		mContext = null;
		mOsdLayout.setVisibility(View.INVISIBLE);

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setInt("/params/x", 0);
		p.setInt("/params/y", 0);
		p.setInt("/params/w", 0);
		p.setInt("/params/h", 0);
		req.to("/face/osd", p.toString());
	}

	@Override
	public void onRestart() {
		super.onRestart();
		mContext = this;
		mStartVo = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onKey(String key) {
		super.onKey(key);
	}

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

		FaceNormal.onOsdStart(mOsdLayout);
		FaceCompare.onOsdStart(mOsdLayout);
	}
}
