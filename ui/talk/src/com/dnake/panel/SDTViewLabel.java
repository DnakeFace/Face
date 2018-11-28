package com.dnake.panel;

import com.dnake.misc.SDTLogger;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class SDTViewLabel extends BaseLabel {
	public static int mMode = 0;
	public static long mData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sdt_view);
		SDTLogger.Data d;
		if (mMode == 0)
			d = SDTLogger.load(mData);
		else
			d = SDTLogger.eLoad(mData);
		TextView tv = (TextView) this.findViewById(R.id.sdt_name);
		tv.setText(d.name);
		tv = (TextView) this.findViewById(R.id.sdt_sex);
		tv.setText(d.sex);
		tv = (TextView) this.findViewById(R.id.sdt_nation);
		tv.setText(d.nation);
		tv = (TextView) this.findViewById(R.id.sdt_birthday);
		int by = Integer.parseInt(d.birthday.substring(0, 4));
		int bm = Integer.parseInt(d.birthday.substring(4, 6));
		int bd = Integer.parseInt(d.birthday.substring(6, 8));
		tv.setText(String.format("%d 年 %d 月 %d 日", by, bm, bd));
		tv = (TextView) this.findViewById(R.id.sdt_address);
		tv.setText(d.address);
		tv = (TextView) this.findViewById(R.id.sdt_id);
		tv.setText(d.id);

		String s = "";
		if (d.data != null && d.gate != null)
			s = "数值:"+d.data+"  阈值:"+d.gate+"  ";
		s += "时间:"+d.dt;
		tv = (TextView) this.findViewById(R.id.sdt_date);
		tv.setText(s);

		ImageView m = (ImageView) this.findViewById(R.id.sdt_photo);
		Bitmap b = BitmapFactory.decodeFile(d.photo);
		if (b != null)
			m.setImageBitmap(b);
		m = (ImageView) this.findViewById(R.id.sdt_photo2);
		b = BitmapFactory.decodeFile(d.camera);
		if (b != null)
			m.setImageBitmap(b);
	}

	@Override
	public void onTimer() {
		super.onTimer();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onKey(String key) {
		super.onKey(key);
	}
}
