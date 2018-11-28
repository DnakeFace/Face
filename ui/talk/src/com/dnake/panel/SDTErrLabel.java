package com.dnake.panel;

import java.util.Calendar;
import java.util.Date;

import com.dnake.misc.SDTLogger;
import com.dnake.widget.Button2;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TableRow;
import android.widget.TextView;

@SuppressLint({ "SdCardPath", "DefaultLocale" })
public class SDTErrLabel extends BaseLabel {
	private static int MAX = 8;
	private TextView mIdx[] = new TextView[MAX];
	private TextView mName[] = new TextView[MAX];
	private TextView mId[] = new TextView[MAX];
	private TextView mTs[] = new TextView[MAX];
	private TableRow mRow[] = new TableRow[MAX];
	private long mData[] = new long[SDTLogger.ERR_MAX];
	private int mCount = 0;
	private int mStart = 0;

	private int mYear, mMonth, mDay;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logger);

		for (int i = 0; i < MAX; i++) {
			mRow[i] = (TableRow) this.findViewById(R.id.logger_data_0 + i * 5);
			mRow[i].setOnClickListener(new RowOnClickListener());

			mIdx[i] = (TextView) this.findViewById(R.id.logger_data_idx_0 + i * 5);
			mName[i] = (TextView) this.findViewById(R.id.logger_data_id_0 + i * 5);
			mId[i] = (TextView) this.findViewById(R.id.logger_data_ts_0 + i * 5);
			mTs[i] = (TextView) this.findViewById(R.id.logger_data_len_0 + i * 5);
		}
		Button2 b = (Button2) this.findViewById(R.id.logger_btn_up);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mStart > 0)
					mStart -= MAX;
				showData();
			}
		});
		b = (Button2) this.findViewById(R.id.logger_btn_down);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mStart + MAX < mCount)
					mStart += MAX;
				showData();
			}
		});

		Button b2 = (Button) this.findViewById(R.id.logger_date);
		b2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Calendar c = Calendar.getInstance();
				DatePickerDialog d = new DatePickerDialog(SDTErrLabel.this, new OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int month, int day) {
						mYear = year;
						mMonth = month + 1;
						mDay = day;
						Button b2 = (Button) findViewById(R.id.logger_date);
						b2.setText(String.format("日期：%04d-%02d-%02d", mYear, mMonth, mDay));
						String s1 = String.format("%04d%02d%02d000000", mYear, mMonth, mDay);
						String s2 = String.format("%04d%02d%02d235959", mYear, mMonth, mDay);
						loadData(Long.parseLong(s1), Long.parseLong(s2));
						showData();
					}
				}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
				d.show();
			}
		});
		Date d = new Date();
		mYear = d.getYear() + 1900;
		mMonth = d.getMonth() + 1;
		mDay = d.getDate();
		b2.setText(String.format("日期：%04d-%02d-%02d", mYear, mMonth, mDay));

		String s1 = String.format("%04d%02d%02d000000", mYear, mMonth, mDay);
		String s2 = String.format("%04d%02d%02d235959", mYear, mMonth, mDay);
		this.loadData(Long.parseLong(s1), Long.parseLong(s2));
		this.showData();
	}

	public void loadData(long start, long end) {
		int n = SDTLogger.mErrIdx - 1;
		mCount = 0;
		while (n >= 0) {
			if (SDTLogger.mErrData[n] >= start && SDTLogger.mErrData[n] <= end) {
				mData[mCount] = SDTLogger.mErrData[n];
				mCount++;
			}
			n--;
		}
	}

	public void showData() {
		for (int i = 0; i < MAX; i++) {
			if (mStart + i < mCount) {
				SDTLogger.Data d = SDTLogger.eLoad(mData[mStart + i]);
				mIdx[i].setText(String.valueOf(mStart + i + 1));
				mName[i].setText(d.name);
				mId[i].setText(d.id);
				mTs[i].setText(d.dt);
			} else {
				mIdx[i].setText("");
				mName[i].setText("");
				mId[i].setText("");
				mTs[i].setText("");
			}
		}
	}

	private final class RowOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			int n = -1;
			for(int i=0; i<MAX; i++) {
				if (mRow[i].getId() == v.getId()) {
					n = i;
					break;
				}
			}
			if (n != -1 && mStart+n < mCount) {
				SDTViewLabel.mMode = 1;
				SDTViewLabel.mData = mData[mStart+n];
				Intent it = new Intent(SDTErrLabel.this, SDTViewLabel.class);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(it);
			}
		}
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
