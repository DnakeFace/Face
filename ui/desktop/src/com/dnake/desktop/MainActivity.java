package com.dnake.desktop;

import com.dnake.misc.SysSpecial;
import com.dnake.v700.sys;
import com.dnake.widget.Button2;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

public class MainActivity extends BaseLabel {
	private int mSdtUsed = 0;
	private RelativeLayout mSdtLayout = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		int ui_visibility = this.getWindow().getDecorView().getSystemUiVisibility();
		if ((ui_visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
			this.getWindow().getDecorView().setSystemUiVisibility(ui_visibility | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}

		Button2 b = (Button2) this.findViewById(R.id.main_apps);
		if (b != null) {
			b.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent it = new Intent(MainActivity.this, AppsLabel.class);
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(it);
				}
			});
		}

		mSdtLayout = (RelativeLayout)this.findViewById(R.id.main_sdt_layout);

		Intent it = new Intent("com.dnake.broadcast");
		it.putExtra("event", "com.dnake.boot");
		sendBroadcast(it);

		it = new Intent(this, SysService.class);
		this.startService(it);

		if (sys.lcd.orientation() != this.getRequestedOrientation()) {
			if (sys.lcd.orientation() == 0)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			else if (sys.lcd.orientation() == 1)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			else if (sys.lcd.orientation() == 2)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			else if (sys.lcd.orientation() == 3)
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		bFinish = false;
		tPeriod = 1000;
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		this.finish();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onTimer() {
		int ui_visibility = this.getWindow().getDecorView().getSystemUiVisibility();
		if ((ui_visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
			this.getWindow().getDecorView().setSystemUiVisibility(ui_visibility | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}

		if (mSdtUsed == 0 && SysSpecial.third == 0 && sys.sdt() > 0) {
			mSdtUsed = 1;
			if (mSdtLayout != null)
				mSdtLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true;
		return super.onKeyDown(keyCode, event);
	}
}
