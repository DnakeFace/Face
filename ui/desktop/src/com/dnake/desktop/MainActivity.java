package com.dnake.desktop;

import com.dnake.misc.SysSpecial;
import com.dnake.v700.sys;
import com.dnake.widget.Button2;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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

		if (sys.lcd() != 0 && this.getRequestedOrientation() == 0) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
	}

	@Override
	public void onStart() {
		super.onStart();
		this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		bFinish = false;
		tPeriod = 1000;
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onTimer() {
		if (this.getWindow().getDecorView().getSystemUiVisibility() != View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) {
			this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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
