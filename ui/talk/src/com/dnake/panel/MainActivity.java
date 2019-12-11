package com.dnake.panel;

import com.dnake.misc.Sound;
import com.dnake.misc.SysTalk;
import com.dnake.v700.login;
import com.dnake.v700.sys;
import com.dnake.widget.Button2;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

public class MainActivity extends BaseLabel {
	private Context mContext = this;
	private EditText mPasswd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button2 b;
		b = (Button2) this.findViewById(R.id.main_btn_logger);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (login.ok()) {
					Intent it = new Intent(MainActivity.this, SDTLoggerLabel.class);
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(it);
				} else {
					LayoutInflater inflater = getLayoutInflater();
					View layout = inflater.inflate(R.layout.login, (ViewGroup) findViewById(R.id.login));
					mPasswd = (EditText) layout.findViewById(R.id.login_passwd);

					Builder b = new AlertDialog.Builder(mContext);
					b.setView(layout);
					b.setTitle(R.string.login_title);
					b.setPositiveButton(R.string.login_passwd_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String passwd = mPasswd.getText().toString();
							if (login.passwd(passwd)) {
								Intent it = new Intent(MainActivity.this, SDTLoggerLabel.class);
								it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(it);
							} else {
								Sound.play(Sound.OrderPasswdErr);
							}
						}
					});
					b.setNegativeButton(R.string.login_passwd_cancel, null);

					AlertDialog ad = b.create();
					ad.setCanceledOnTouchOutside(false);
					ad.show();
				}
			}
		});

		b = (Button2) this.findViewById(R.id.main_btn_err);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (login.ok()) {
					Intent it = new Intent(MainActivity.this, SDTErrLabel.class);
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(it);
				} else {
					LayoutInflater inflater = getLayoutInflater();
					View layout = inflater.inflate(R.layout.login, (ViewGroup) findViewById(R.id.login));
					mPasswd = (EditText) layout.findViewById(R.id.login_passwd);

					Builder b = new AlertDialog.Builder(mContext);
					b.setView(layout);
					b.setTitle(R.string.login_title);
					b.setPositiveButton(R.string.login_passwd_ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String passwd = mPasswd.getText().toString();
							if (login.passwd(passwd)) {
								Intent it = new Intent(MainActivity.this, SDTErrLabel.class);
								it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(it);
							} else {
								Sound.play(Sound.OrderPasswdErr);
							}
						}
					});
					b.setNegativeButton(R.string.login_passwd_cancel, null);

					AlertDialog ad = b.create();
					ad.setCanceledOnTouchOutside(false);
					ad.show();
				}
			}
		});

		b = (Button2) this.findViewById(R.id.main_btn_setup);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SetupLabel.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

		if (SysTalk.mContext == null) {
			sys.load();
			Intent it = new Intent(this, SysTalk.class);
			this.startService(it);
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
}
