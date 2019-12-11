package com.dnake.special;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.utils;

import fi.iki.elonen.NanoHTTPD;

public class NetposaHttpd extends NanoHTTPD {

	public NetposaHttpd(int port) {
		super(port);
		File f = new File("/dnake/data/netposa");
		if (!f.exists())
			f.mkdir();
		System.err.println("NetposaHttpd: " + port);
	}

	public static int mRid = 0;
	public static boolean mResultOK = false;
	public static int mResultCode = -1;

	private Response doFacePicManager(IHTTPSession session, String body) {
		String ack = "";
		try {
			JSONObject json = new JSONObject(body);
			String type = json.getString("type");
			String faceID = json.getString("faceID");
			System.err.println("faceID: " + faceID);
			System.err.println("type: " + type);
			String net_u = "/dnake/data/netposa/" + faceID + ".xml";
			dxml net_c = new dxml();
			net_c.load(net_u);
			int id = net_c.getInt("/sys/id", (int)(Math.random()*0x7FFFFFFF));
			String net_u2 = "/dnake/data/netposa/" + id + ".xml";
			if (type.equals("0")) { // 删除
				dmsg req = new dmsg();
				dxml p = new dxml();
				p.setInt("/params/id", id);
				p.setInt("/params/uid", 0);
				req.to("/face/ex/del", p.toString());
				File f = new File(net_u);
				if (f.exists())
					f.delete();
				f = new File(net_u2);
				if (f.exists())
					f.delete();
			} else if (type.equals("1")) {
				String faceContent = json.getString("faceContent");
				String credentialType = json.getString("credentialType"); // 证件类型
				String credentialNo = json.getString("credentialNo"); // 证件号码

				byte[] pic = Base64.decode(faceContent, Base64.DEFAULT);
				String url = "/var/" + id + ".jpg";
				utils.writeFile(pic, url);

				mRid = (int) (Math.random()*0xFFFFFFF);
				mResultOK = false;
				mResultCode = -1;

				dmsg req = new dmsg();
				dxml p = new dxml();
				p.setInt("/params/id", id);
				p.setInt("/params/uid", 0);
				p.setInt("/params/rid", mRid);
				p.setInt("/params/black", 0);
				p.setText("/params/url", url);
				req.to("/face/ex/jpeg", p.toString());
				for(int i=0; i<20; i++) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
					if (mResultOK)
						break;
				}

				net_c.setInt("/sys/id", id);
				net_c.setText("/sys/faceID", faceID);
				net_c.setText("/sys/credentialType", credentialType);
				net_c.setText("/sys/credentialNo", credentialNo);
				net_c.save(net_u);
				net_c.save(net_u2);
			}

			JSONObject r = new JSONObject();
			if (mResultCode == 0 || mResultCode == 200) {
				r.put("result", "0");
				r.put("msg", "OK");
			} else {
				r.put("result", "-2");
				if (mResultCode == 400)
					r.put("msg", "Not face detected!");
				else
					r.put("msg", "Face image decode error!");
			}
			r.put("faceID", faceID);
			ack = r.toString();
		} catch (JSONException e) {
			JSONObject r = new JSONObject();
			try {
				r.put("result", "-1");
				r.put("msg", e.getMessage());
			} catch (JSONException e1) {
			}
			ack = r.toString();
		}
		return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json; charset=utf-8", ack);
	}

	@Override
	public Response serve(IHTTPSession session) {
		Response result = newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "application/json; charset=utf-8", "");
		Method method = session.getMethod();
		if (Method.POST.equals(method)) {
			Map<String, String> map = new HashMap<String, String>();
			String body = "";
			try {
				session.parseBody(map);
				body = map.get("postData");
			} catch (IOException e) {
			} catch (ResponseException e) {
			}
			String uri = session.getUri();
			System.err.println("uri: " + uri);
			// System.err.println("body: " + body);
			if (uri.endsWith("/v2/facePicManager")) {
				result = this.doFacePicManager(session, body);
			}
		}
		return result;
	}
}
