package com.dnake.special;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.dnake.misc.OkHttpConnect;
import com.dnake.special.SysProtocol.FaceData;
import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;
import com.dnake.v700.utils;

@SuppressLint("NewApi") public class YmsProtocol {
	public static int m_enabled = 0;

	public static String m_url = "http://59.110.168.146:9000/yms";
	public static String m_app_id = "100071";
	public static String m_app_key = "yQFBfe";
	public static String m_app_secret = "ZzAVri2MR3yi";
	public static String m_app_phone = "dstest";
	public static String m_app_token = null;
	public static long m_token_ts = 0;

	public static String m_devid = null;
	public static String m_school_id = null;

	public static void load() {
		dxml p = new dxml();
		p.load("/dnake/bin/special.xml");
		m_enabled = p.getInt("/sys/yms", 0);
		if (m_enabled == 0)
			return;

		p.load("/dnake/cfg/yms.xml");
		m_url = p.getText("/sys/url", m_url);
		m_school_id = p.getText("/sys/school", m_school_id);
		m_app_id = p.getText("/sys/app/id", m_app_id);
		m_app_key = p.getText("/sys/app/key", m_app_key);
		m_app_secret = p.getText("/sys/app/secret", m_app_secret);
		m_app_phone = p.getText("/sys/app/phone", m_app_phone);

		m_devid = p.getText("/sys/devid");
		if (m_devid == null) {
			byte s[] = utils.readFile("/var/etc/serial");
			if (s != null) {
				try {
					m_devid = new String(s, "UTF-8");
				} catch (UnsupportedEncodingException e) {
				}
			}
		}

		File f = new File("/dnake/data/yms");
		if (f != null) {
			if (!f.exists())
				f.mkdir();
		}

		byte d[] = utils.readFile("/dnake/data/yms/person.json");
		if (d != null) {
			try {
				m_person = new JSONArray(new String(d));
			} catch (JSONException e) {
			}
		} else {
			m_person = null;
		}

		d = utils.readFile("/dnake/data/yms/face.json");
		if (d != null) {
			try {
				m_face = new JSONArray(new String(d));
			} catch (JSONException e) {
			}
		} else {
			m_face = new JSONArray();
		}

		Thread t = new Thread(new ProcessThread());
		t.start();
	}

	public static void save() {
		dxml p = new dxml();
		p.setText("/sys/url", m_url);
		p.setText("/sys/school", m_school_id);
		p.setText("/sys/devid", m_devid);
		p.setText("/sys/app/id", m_app_id);
		p.setText("/sys/app/key", m_app_key);
		p.setText("/sys/app/secret", m_app_secret);
		p.setText("/sys/app/phone", m_app_phone);
		p.save("/dnake/cfg/yms.xml");
	}

	public static void doToken() {
		try {
			String url = m_url + "/speedway/auth/V1";
			String ts = String.valueOf(System.currentTimeMillis());

			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(m_app_key.getBytes());
			md5.update(ts.getBytes());
			md5.update(m_app_secret.getBytes());
			byte d[] = md5.digest();
			StringBuffer sb = new StringBuffer("");
			for (int offset = 0; offset < d.length; offset++) {
				String s = Integer.toHexString(0xFF & d[offset]);
				if (s.length() < 2)
					sb.append("0");
				sb.append(s);
			}

			String data = "appId=" + m_app_id + "&appKey=" + m_app_key
					+ "&timestamp=" + ts + "&deviceId=" + m_devid + "&sign="
					+ sb.toString();
			OkHttpConnect c = new OkHttpConnect();
			c.doPost(url, data);
			if (c.mCode == 200 && c.mBody != null) {
				System.err.println("YmsProtocol.doToken:");
				System.err.println(new String(c.mBody));
				try {
					JSONObject json = new JSONObject(new String(c.mBody));
					int code = json.getInt("code");
					if (code == 200) {
						m_app_token = json.getString("data");
					} else {
						m_app_token = null;
					}
				} catch (JSONException e) {
				}
			} else {
				m_app_token = null;
			}
		} catch (NoSuchAlgorithmException e) {
		}
	}

	public static void doRegister() {
		if (m_app_token == null)
			return;

		String url = m_url + "/speedway/device/V1";
		String data = "appId=" + m_app_id + "&token="
				+ m_app_token + "&deviceId=" + m_devid + "&phone="
				+ m_app_phone + "&deviceType=0";

		OkHttpConnect c = new OkHttpConnect();
		c.doPost(url, data);
		if (c.mCode == 200 && c.mBody != null) {
			System.err.println("YmsProtocol.doRegister:");
			System.err.println(new String(c.mBody));
			try {
				JSONObject json = new JSONObject(new String(c.mBody));
				int code = json.getInt("code");
				if (code == 200) {
					JSONObject d = json.getJSONObject("data");
					if (d != null) {
						m_school_id = d.getString("schoolId");
						save();
						System.err.println("YmsProtocol.doRegister m_school_id: " + m_school_id);
					}
				}
			} catch (JSONException e) {
			}
		}
	}

	public static void doQuery() {
		if (m_app_token == null)
			return;

		String url = m_url + "/speedway/device/V1?appId=" + m_app_id + "&token=" + m_app_token + "&deviceId=" + m_devid;

		OkHttpConnect c = new OkHttpConnect();
		c.doGet(url);
		if (c.mCode == 200 && c.mBody != null) {
			try {
				JSONObject json = new JSONObject(new String(c.mBody));
				int code = json.getInt("code");
				if (code == 200) {
					JSONObject d = json.getJSONObject("data");
					if (d != null) {
						m_school_id = d.getString("schoolId");
						save();
						System.err.println("YmsProtocol.doQuery m_school_id: " + m_school_id);
					}
				}
			} catch (JSONException e) {
			}
		}
	}

	public static void doDelete() {
		if (m_app_token == null)
			return;

		String url = m_url + "/speedway/device/V1?appId=" + m_app_id + "&token=" + m_app_token + "&deviceId=" + m_devid;
		OkHttpConnect c = new OkHttpConnect();
		c.doDelete(url);

		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setInt("/params/mode", 2);
		req.to("/face/default", p.toString());

		try {
			String[] cmd = new String[] {"sh", "-c", "rm -rf /dnake/data/yms/*"};
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}

		m_school_id = null;
		save();

		m_face = new JSONArray();
		m_person = null;
	}

	private static JSONArray m_person = null;
	public static long m_person_ts = 0;
	private static JSONArray m_face = null;

	public static void doPerson() {
		if (m_app_token == null || m_school_id == null)
			return;

		System.err.println("YmsProtocol.doPerson");

		String url = m_url + "/speedway/person/V1?appId=" + m_app_id + "&token=" + m_app_token + "&deviceId=" + m_devid + "&schoolId=" + m_school_id;

		OkHttpConnect c = new OkHttpConnect();
		c.doGet(url);
		if (c.mCode == 200 && c.mBody != null) {
			try {
				JSONObject json = new JSONObject(new String(c.mBody));
				int code = json.getInt("code");
				if (code == 200) {
					JSONObject d = json.getJSONObject("data");
					if (d != null) {
						JSONArray dt = d.getJSONArray("deleteList");
						for(int i=0; i<dt.length(); i++) {
							String id = dt.getString(i);
							if (id != null) {
								doFaceDelete(id);
							}
						}

						JSONArray p = d.getJSONArray("list");
						for(int i=0; i<p.length(); i++) {
							JSONObject o = p.getJSONObject(i);
							String userId = o.getString("userId");
							String imgUrl = o.getString("imgUrl");
							if (imgUrl != null && imgUrl.length() > 10 && !doEquals(userId, imgUrl)) {
								System.err.println("YmsProtocol.doPerson url: "+imgUrl);
								OkHttpConnect dw = new OkHttpConnect();
								if (dw.doGet(imgUrl) == 200) {
									String s = "/dnake/data/yms/"+userId+".jpg";
									Bitmap b = BitmapFactory.decodeByteArray(dw.mBody, 0, dw.mBody.length);
									utils.save(b, s);
									doFaceAdd(userId, s);
								}
							}
						}
						m_person = p;
						utils.writeFile(m_person.toString().getBytes(), "/dnake/data/yms/person.json");
					}
				}
			} catch (JSONException e) {
			}
		}
	}

	private static long m_heartbeat_ts = 0;
	private static long m_heartheat_period = 60;

	public static void doHeartbeat() {
		if (m_app_token == null || m_school_id == null)
			return;

		String url = m_url + "/speedway/heartbeat/V1";
		String data = "appId=" + m_app_id + "&token=" + m_app_token + "&deviceId=" + m_devid;

		OkHttpConnect c = new OkHttpConnect();
		c.doPost(url, data);
		if (c.mCode == 200 && c.mBody != null) {
			try {
				JSONObject json = new JSONObject(new String(c.mBody));
				int code = json.getInt("code");
				if (code == 200) {
					JSONObject obj = json.getJSONObject("data");
					if (obj != null) {
						m_heartheat_period = obj.getInt("period");
						if (m_heartheat_period < 10)
							m_heartheat_period = 10;
					}
				} else {
				}
			} catch (JSONException e) {
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static void doLogger(FaceData d) {
		if (m_app_token == null || m_school_id == null || d == null || d.id < 0 || d.from != 1)
			return;

		JSONObject obj = YmsProtocol.queryFaceId(d.id);
		if (obj != null) {
			try {
				String url = m_url + "/speedway/access/V1";

				String userId = obj.getString("userId");

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				d.bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);

				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String data = "appId=" + m_app_id + "&token=" + m_app_token + "&schoolId=" + m_school_id + "&deviceId=" + m_devid + "&userId="+userId;
				data += "&imageName="+System.currentTimeMillis()+".jpg&accessTime="+sf.format(new Date())+"&accessType=0";
				data += "&image="+URLEncoder.encode(Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));

				OkHttpConnect c = new OkHttpConnect();
				c.doPost(url, data);
				if (c.mBody != null) {
					System.err.println("YmsProtocol.doLogger:");
					System.err.println(new String(c.mBody));
				}
			} catch (JSONException e) {
			}
		} else {
			System.err.println("YmsProtocol.doLogger id("+d.id+") not found.....");
			dmsg req = new dmsg();
			dxml p = new dxml();
			p.setInt("/params/id", d.id);
			p.setInt("/params/uid", 0);
			req.to("/face/ex/del", p.toString());
		}
	}

	private static boolean doEquals(String uid, String url) {
		if (m_person == null)
			return false;

		File f = new File("/dnake/data/yms/"+uid+".jpg");
		if (f != null) {
			if (!f.exists())
				return false;
		}

		for(int i=0; i<m_person.length(); i++) {
			try {
				JSONObject o = m_person.getJSONObject(i);
				String id = o.getString("userId");
				String u = o.getString("imgUrl");
				if (uid.equals(id) && url.equals(u)) {
					return true;
				}
			} catch (JSONException e) {
			}
		}
		return false;
	}

	private static void doFaceAdd(String uid, String url) {
		String jpeg = "/var/"+uid+".jpg";
		byte d[] = utils.readFile(url);
		utils.writeFile(d, jpeg);

		for(int i=0; i<m_face.length(); i++) {
			try {
				JSONObject obj = m_face.getJSONObject(i);
				String id = obj.getString("userId");
				if (uid.equals(id)) {
					int fid = obj.getInt("fid");
					dmsg req = new dmsg();
					dxml p = new dxml();
					p.setInt("/params/id", fid);
					p.setInt("/params/uid", 0);
					p.setInt("/params/rid", (int)(Math.random()*100000000));
					p.setText("/params/url", jpeg);
					req.to("/face/ex/jpeg", p.toString());
					System.err.println("YmsProtocol.doFaceAdd2 userId:"+uid+" fid:"+fid);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		try {
			int fid = (int)(Math.random()*100000000+10000);
			JSONObject obj = new JSONObject();
			obj.put("userId", uid);
			obj.put("fid", fid);
			m_face.put(obj);
			utils.writeFile(m_face.toString().getBytes(), "/dnake/data/yms/face.json");

			dmsg req = new dmsg();
			dxml p = new dxml();
			p.setInt("/params/id", fid);
			p.setInt("/params/uid", 0);
			p.setInt("/params/rid", (int)(Math.random()*100000000));
			p.setText("/params/url", jpeg);
			req.to("/face/ex/jpeg", p.toString());
			System.err.println("YmsProtocol.doFaceAdd userId:"+uid+" fid:"+fid);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			return;
		} catch (JSONException e) {
		}

		File f = new File(jpeg);
		if (f != null) {
			if (f.exists())
				f.delete();
		}
	}

	private static void doFaceDelete(String uid) {
		for(int i=0; i<m_face.length(); i++) {
			try {
				JSONObject obj = m_face.getJSONObject(i);
				String id = obj.getString("userId");
				if (uid.equals(id)) {
					int fid = obj.getInt("fid");
					dmsg req = new dmsg();
					dxml p = new dxml();
					p.setInt("/params/id", fid);
					p.setInt("/params/uid", 0);
					req.to("/face/ex/del", p.toString());
					System.err.println("YmsProtocol.doFaceDelete userId:"+uid+" fid:"+fid);
					m_face.remove(i);

					utils.writeFile(m_face.toString().getBytes(), "/dnake/data/yms/face.json");
					return;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public static JSONObject queryFaceId(int id) {
		if (m_enabled == 0 || m_person == null)
			return null;
		String uid = null;
		for(int i=0; i<m_face.length(); i++) {
			try {
				JSONObject obj = m_face.getJSONObject(i);
				int fid = obj.getInt("fid");
				if (fid == id) {
					uid = obj.getString("userId");
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (uid != null) {
			for(int i=0; i<m_person.length(); i++) {
				try {
					JSONObject obj = m_person.getJSONObject(i);
					String userId = obj.getString("userId");
					if (uid.equals(userId)) {
						return obj;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static void process() {
		if (m_app_token == null) {
			if (sys.ts(m_token_ts) > 2*60*1000) {
				m_token_ts = System.currentTimeMillis();
				doToken();
			}
		} else {
			if (sys.ts(m_token_ts) > 4*60*60*1000) {
				m_token_ts = System.currentTimeMillis();
				doToken();
			}

			if (sys.ts(m_person_ts) > 5*60*1000) {
				//5分钟自动同步一次人员信息
				m_person_ts = System.currentTimeMillis();
				doPerson();
			}

			if (sys.ts(m_heartbeat_ts) > m_heartheat_period*1000) {
				m_heartbeat_ts = System.currentTimeMillis();
				doHeartbeat();
			}
		}
	}

	public static class ProcessThread implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(20*1000);
			} catch (InterruptedException e) {
			}

			while (true) {
				YmsProtocol.process();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
