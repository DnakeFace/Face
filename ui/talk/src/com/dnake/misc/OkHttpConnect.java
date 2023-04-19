package com.dnake.misc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class OkHttpConnect {
	public int mCode;
	public byte mBody[] = null;

	public String mAuthorization = null;

	public int doGet(String url) {
		int result = 404;

		mBody = null;
		try {
			okhttp3.Request.Builder b = new okhttp3.Request.Builder();
			if (mAuthorization != null) {
				b.header("Authorization", mAuthorization);
			}
			b.header("Content-Type", "application/json; charset=utf-8");
			b.header("Cache-Control", "no-cache");
			okhttp3.Request request = b.url(url).get().build();
			OkHttpClient c = new OkHttpClient();
			okhttp3.Response r = c.newCall(request).execute();
			if (r.isSuccessful()) {
				mBody = r.body().bytes();
			}
			result = r.code();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mCode = result;
		return result;
	}

	public int doDelete(String url) {
		int result = 404;

		mBody = null;
		try {
			okhttp3.Request.Builder b = new okhttp3.Request.Builder();
			if (mAuthorization != null) {
				b.header("Authorization", mAuthorization);
			}
			b.header("Content-Type", "application/json");
			b.header("Cache-Control", "no-cache");
			okhttp3.Request request = b.url(url).delete().build();
			OkHttpClient c = new OkHttpClient();
			okhttp3.Response r = c.newCall(request).execute();
			if (r.isSuccessful()) {
				mBody = r.body().bytes();
			}
			result = r.code();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mCode = result;
		return result;
	}

	public int doPost(String url, String data) {
		int result = 404;

		mBody = null;
		try {
			RequestBody rBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), data);
			okhttp3.Request.Builder b = new okhttp3.Request.Builder();
			if (mAuthorization != null) {
				b.header("Authorization", mAuthorization);
			}
			b.header("Cache-Control", "no-cache");
			okhttp3.Request request = b.url(url).post(rBody).build();
			OkHttpClient c = new OkHttpClient.Builder().writeTimeout(60, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
			okhttp3.Response r = c.newCall(request).execute();
			if (r.isSuccessful()) {
				mBody = r.body().bytes();
			}
			result = r.code();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mCode = result;
		return result;
	}
}
