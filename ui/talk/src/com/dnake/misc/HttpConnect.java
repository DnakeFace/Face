package com.dnake.misc;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.annotation.SuppressLint;

public class HttpConnect {

	public boolean mFinished = false;
	public int mResult = -1;
	public String mContentType = null;

	private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String arg0, SSLSession arg1) {
			return true;
		}
	};

	private static final TrustManager[] TrustAllCerts = new TrustManager[] { new X509TrustManager() {
		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[] {};
		}

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}
	} };

	@SuppressLint("TrulyRandom")
	private static void setTrustAllHosts(HttpsURLConnection h) {
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, TrustAllCerts, new java.security.SecureRandom());
			SSLSocketFactory f = sc.getSocketFactory();
			h.setSSLSocketFactory(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		h.setHostnameVerifier(DO_NOT_VERIFY);
	}

	public void onBody(int result, String body) {
	}

	protected void doGet(String url) {
		String body = new String();
		int result = 404;
		try {
			URL u = new URL(url);
			HttpURLConnection http = (HttpURLConnection) u.openConnection();
			if (mContentType != null)
				http.setRequestProperty("Content-Type", mContentType);
			http.setConnectTimeout(30 * 1000);
			http.setReadTimeout(30 * 1000);
			result = http.getResponseCode();
			if (result == 200) {
				InputStreamReader in = new InputStreamReader(
						http.getInputStream());
				BufferedReader bReader = new BufferedReader(in);
				while (true) {
					String s = bReader.readLine();
					if (s == null)
						break;
					body += s;
				}
				in.close();
			} else {
				InputStreamReader in = new InputStreamReader(
						http.getErrorStream());
				BufferedReader bReader = new BufferedReader(in);
				while (true) {
					String s = bReader.readLine();
					if (s == null)
						break;
					body += s;
				}
				in.close();
			}
			http.disconnect();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		onBody(result, body);
		mResult = result;
	}

	protected void doGetHttps(String url) {
		String body = new String();
		int result = 404;
		try {
			URL u = new URL(url);
			HttpsURLConnection http = (HttpsURLConnection) u.openConnection();
			if (mContentType != null)
				http.setRequestProperty("Content-Type", mContentType);
			setTrustAllHosts(http);
			http.setConnectTimeout(10 * 1000);
			http.setReadTimeout(10 * 1000);
			result = http.getResponseCode();
			if (result == 200) {
				InputStreamReader in = new InputStreamReader(
						http.getInputStream());
				BufferedReader bReader = new BufferedReader(in);
				while (true) {
					String s = bReader.readLine();
					if (s == null)
						break;
					body += s;
				}
				in.close();
			} else {
				InputStreamReader in = new InputStreamReader(
						http.getErrorStream());
				BufferedReader bReader = new BufferedReader(in);
				while (true) {
					String s = bReader.readLine();
					if (s == null)
						break;
					body += s;
				}
				in.close();
			}
			http.disconnect();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		onBody(result, body);
		mResult = result;
	}

	protected void doPost(String url, String data) {
		String body = new String();
		int result = 404;
		try {
			URL u = new URL(url);
			HttpURLConnection http = (HttpURLConnection) u.openConnection();
			if (mContentType != null)
				http.setRequestProperty("Content-Type", mContentType);
			http.setConnectTimeout(10 * 1000);
			http.setReadTimeout(10 * 1000);

			if (data != null) {
				byte[] d = data.getBytes();
				http.setRequestMethod("POST");
				http.setRequestProperty("Content-Length",
						String.valueOf(d.length));
				http.setDoOutput(true);
				DataOutputStream out = new DataOutputStream(
						http.getOutputStream());
				out.write(d);
				out.flush();
				out.close();
			}

			result = http.getResponseCode();
			if (result == 200) {
				InputStreamReader in = new InputStreamReader(
						http.getInputStream());
				BufferedReader bReader = new BufferedReader(in);
				while (true) {
					String s = bReader.readLine();
					if (s == null)
						break;
					body += s;
				}
				in.close();
			} else {
				InputStreamReader in = new InputStreamReader(
						http.getErrorStream());
				BufferedReader bReader = new BufferedReader(in);
				while (true) {
					String s = bReader.readLine();
					if (s == null)
						break;
					body += s;
				}
				in.close();
			}
			http.disconnect();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		onBody(result, body);
		mResult = result;
	}

	public void doDelete(String url, String data) {
		String body = new String();
		int result = 404;
		try {
			URL u = new URL(url);
			HttpURLConnection http = (HttpURLConnection) u.openConnection();
			if (mContentType != null)
				http.setRequestProperty("Content-Type", mContentType);
			http.setConnectTimeout(10 * 1000);
			http.setReadTimeout(10 * 1000);
			http.setRequestMethod("DELETE");

			if (data != null) {
				byte[] d = data.getBytes();
				http.setRequestProperty("Content-Length",
						String.valueOf(d.length));
				http.setDoOutput(true);
				DataOutputStream out = new DataOutputStream(
						http.getOutputStream());
				out.write(d);
				out.flush();
				out.close();
			}

			result = http.getResponseCode();
			if (result == 200) {
				InputStreamReader in = new InputStreamReader(
						http.getInputStream());
				BufferedReader bReader = new BufferedReader(in);
				while (true) {
					String s = bReader.readLine();
					if (s == null)
						break;
					body += s;
				}
				in.close();
			} else {
				InputStreamReader in = new InputStreamReader(
						http.getErrorStream());
				BufferedReader bReader = new BufferedReader(in);
				while (true) {
					String s = bReader.readLine();
					if (s == null)
						break;
					body += s;
				}
				in.close();
			}
			http.disconnect();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		onBody(result, body);
		mResult = result;
	}

	protected void doPostHttps(String url, String data) {
		String body = new String();
		int result = 404;
		try {
			URL u = new URL(url);
			HttpsURLConnection http = (HttpsURLConnection) u.openConnection();
			if (mContentType != null)
				http.setRequestProperty("Content-Type", mContentType);
			setTrustAllHosts(http);
			http.setConnectTimeout(10 * 1000);
			http.setReadTimeout(10 * 1000);

			if (data != null) {
				byte[] d = data.getBytes();
				http.setRequestMethod("POST");
				http.setRequestProperty("Content-Length",
						String.valueOf(d.length));
				http.setDoOutput(true);
				DataOutputStream out = new DataOutputStream(
						http.getOutputStream());
				out.write(d);
				out.flush();
				out.close();
			}

			result = http.getResponseCode();
			if (result == 200) {
				InputStreamReader in = new InputStreamReader(
						http.getInputStream());
				BufferedReader bReader = new BufferedReader(in);
				while (true) {
					String s = bReader.readLine();
					if (s == null)
						break;
					body += s;
				}
				in.close();
			} else {
				InputStreamReader in = new InputStreamReader(
						http.getErrorStream());
				BufferedReader bReader = new BufferedReader(in);
				while (true) {
					String s = bReader.readLine();
					if (s == null)
						break;
					body += s;
				}
				in.close();
			}
			http.disconnect();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		onBody(result, body);
		mResult = result;
	}

	public void get(final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				mResult = -1;
				mFinished = false;
				if (url.startsWith("https://"))
					doGetHttps(url);
				else
					doGet(url);
				mFinished = true;
			}
		}).start();
	}

	public void post(final String url, final String body) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				mResult = -1;
				mFinished = false;
				if (url.startsWith("https://"))
					doPostHttps(url, body);
				else
					doPost(url, body);
				mFinished = true;
			}
		}).start();
	}
}
