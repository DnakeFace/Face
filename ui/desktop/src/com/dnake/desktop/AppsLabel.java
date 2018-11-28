package com.dnake.desktop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.dnake.v700.sys;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class AppsLabel extends BaseLabel {
	private ArrayList<AppData> mApps = new ArrayList<AppData>();
	private GridView mGrid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.apps);
	}

	@Override
	public void onStart() {
		super.onStart();

		final Handler h = new Handler() {  
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				loadApps();
			}
		};

		TimerTask task = new TimerTask(){
			public void run() {  
				Message message = new Message();
				h.sendMessage(message);
				this.cancel();
			}
		};

		Timer t = new Timer(true);
		t.schedule(task, 100, 100);
	}

	private void loadApps() {
		mGrid = (GridView) findViewById(R.id.app_grid);
		mGrid.setAdapter(new AppsAdapter(this, mApps));
		mGrid.setOnItemClickListener(mListener);

		PackageManager pm = this.getPackageManager(); // 获得PackageManager对象
		Intent it = new Intent(Intent.ACTION_MAIN, null);
		it.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(it, 0);

		Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
		if (mApps != null) {
			mApps.clear();
			for (ResolveInfo reInfo : resolveInfos) {
				String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
				String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
				String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
				Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标

				if (pkgName.contains("com.dnake") && !pkgName.contains("com.dnake.panel"))
                    continue;

				// 为应用程序的启动Activity 准备Intent
				it = new Intent();
				it.setComponent(new ComponentName(pkgName, activityName));

				// 创建一个AppInfo对象，并赋值
				AppData d = new AppData();
				d.text = appLabel;
				d.icon = icon;
				d.intent = it;
				mApps.add(d); // 添加至列表中
			}
		}
	}

	public class AppData {
		public String text;
		public Drawable icon;
		public Intent intent;
	}

	public class AppsAdapter extends ArrayAdapter<AppData> {
		private final LayoutInflater mInflater;

		public AppsAdapter(Context context, ArrayList<AppData> apps) {
			super(context, 0, apps);
			mInflater = LayoutInflater.from(context);
		}

		public View getView(int position, View v, ViewGroup parent) {
			if (v == null)
				v = mInflater.inflate(R.layout.apps_text, parent, false);

			AppData d = mApps.get(position);
			TextView tv = (TextView) v;
			tv.setCompoundDrawablesWithIntrinsicBounds(null, toAppIcon(d.icon), null, null);
			tv.setText(d.text);
			return v;
		}

		public final int getCount() {
			return mApps.size();
		}

		public final AppData getItem(int position) {
			return mApps.get(position);
		}

		public final long getItemId(int position) {
			return position;
		}

		@SuppressWarnings("deprecation")
		public BitmapDrawable toAppIcon(Drawable drawable) {
			int w = (int) (56*sys.scaled);
			int h = (int) (56*sys.scaled);

			Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, w, h);

			drawable.draw(canvas);
			return new BitmapDrawable(bitmap);
		}
	}

	private OnItemClickListener mListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			startActivity(mApps.get(position).intent);
		}

	};
}
