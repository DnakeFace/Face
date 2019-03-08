package com.dnake.v700;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class eDhcp {
	public static int ePort = 8420;

	public static String body = null;
	public static String from = null;

	public static Boolean discover(String mac) {
		body = null;
		from = null;

		Boolean result = false;
		DatagramSocket s = null;
		try {
			s = new DatagramSocket(ePort);

			dxml tx = new dxml();
			tx.setText("/dhcp/event", "/discover");
			tx.setText("/dhcp/op", "req");
			tx.setText("/dhcp/mac", mac);
			byte[] data = tx.toString().getBytes();

			DatagramPacket dp = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), ePort);
			s.setSoTimeout(500);
			s.send(dp);

			for(int i=0; i<10; i++) {
				data = new byte[8*1024];
				dp = new DatagramPacket(data, data.length);
				s.receive(dp);

				String ss = new String(dp.getData(), 0, dp.getLength());
				dxml rx = new dxml();
				rx.parse(ss);
				String op = rx.getText("/dhcp/op");
				if (mac.equals(rx.getText("/dhcp/mac")) && op != null && op.equalsIgnoreCase("ack")) {
					body = ss;
					from = dp.getAddress().toString();
					if (from != null)
						from = from.substring(1);
					result = true;
					break;
				}
			}
		} catch (SocketException e) {
			//e.printStackTrace();
		} catch (UnknownHostException e) {
			//e.printStackTrace();
		} catch (IOException e) {
			//e.printStackTrace();
		}
		if (s != null)
			s.close();
		return result;
	}

	public static void start() {
		ProcessThread pt = new ProcessThread();
		Thread t = new Thread(pt);
		t.start();
	}

	public static Boolean equals(String rx) {
		dmsg req = new dmsg();
		if (req.to("/settings/lan/query", null) != 200)
			return true;

		dxml lan = new dxml();
		lan.parse(req.mBody);

		dxml p = new dxml();
		p.parse(rx);

		dxml s = new dxml();
		s.setText("/sys/user", p.getText("/dhcp/data/user"));
		s.setText("/sys/passwd", p.getText("/dhcp/data/passwd"));
		s.setInt("/sys/idx", p.getInt("/dhcp/data/idx", 0));
		s.setText("/sys/server", from);

		s.setText("/sys/lan/ip", p.getText("/dhcp/data/lan/ip"));
		s.setText("/sys/lan/mask", p.getText("/dhcp/data/lan/mask"));
		s.setText("/sys/lan/gateway", p.getText("/dhcp/data/lan/gateway"));
		s.setText("/sys/lan/dns", p.getText("/dhcp/data/lan/dns"));
		if (p.getInt("/dhcp/data/sip/enable", 0) != 0) {
			s.setInt("/sys/sip/enable", 1);
			s.setText("/sys/sip/proxy", p.getText("/dhcp/data/sip/proxy"));
			s.setText("/sys/sip/realm", p.getText("/dhcp/data/sip/realm"));
			s.setText("/sys/sip/user", p.getText("/dhcp/data/sip/user"));
			s.setText("/sys/sip/passwd", p.getText("/dhcp/data/sip/passwd"));
		} else
			s.setInt("/sys/sip/enable", 0);

		dxml s2 = new dxml();
		s2.setText("/sys/user", sys.id());
		s2.setText("/sys/passwd", sys.talk.passwd);
		s2.setText("/sys/server", sys.talk.server);

		s2.setText("/sys/lan/ip", lan.getText("/params/ip"));
		s2.setText("/sys/lan/mask", lan.getText("/params/mask"));
		s2.setText("/sys/lan/gateway", lan.getText("/params/gateway"));
		s2.setText("/sys/lan/dns", lan.getText("/params/dns"));
		if (sys.sip.enable != 0) {
			s2.setInt("/sys/sip/enable", 1);
			s2.setText("/sys/sip/proxy", sys.sip.proxy);
			s2.setText("/sys/sip/realm", sys.sip.realm);
			s2.setText("/sys/sip/user", sys.sip.user);
			s2.setText("/sys/sip/passwd", sys.sip.passwd);
		} else
			s2.setInt("/sys/sip/enable", 0);
		if (s.toString().equalsIgnoreCase(s2.toString()))
			return true;
		return false;
	}

	public static void save(String xml) {
		dxml p = new dxml();
		p.parse(xml);

		int tid = p.getInt("/dhcp/data/tid", 0);
		if (tid == 2) //单元门口机
			sys.panel.mode = 0;
		else if (tid == 3) //小门口机
			sys.panel.mode = 2;
		else if (tid == 4) //围墙机
			sys.panel.mode = 1;
		else
			return;

		sys.id(p.getText("/dhcp/data/user"));
		sys.talk.passwd = p.getText("/dhcp/data/passwd");
		sys.talk.server = eDhcp.from;

		if (p.getInt("/dhcp/data/sip/enable", 0) != 0) {
			sys.sip.enable = 1;
			sys.sip.proxy = p.getText("/dhcp/data/sip/proxy");
			sys.sip.realm = p.getText("/dhcp/data/sip/realm");
			sys.sip.user = p.getText("/dhcp/data/sip/user");
			sys.sip.passwd = p.getText("/dhcp/data/sip/passwd");
		} else
			sys.sip.enable = 0;
		sys.save();

		dmsg req = new dmsg();
		dxml lan = new dxml();
		lan.setInt("/params/dhcp", 0);
		lan.setText("/params/ip", p.getText("/dhcp/data/lan/ip"));
		lan.setText("/params/mask", p.getText("/dhcp/data/lan/mask"));
		lan.setText("/params/gateway", p.getText("/dhcp/data/lan/gateway"));
		lan.setText("/params/dns", p.getText("/dhcp/data/lan/dns"));
		req.to("/settings/lan/setup", lan.toString());
	}

	public static class ProcessThread implements Runnable {
		private long ts = 0;

		@Override
		public void run() {
			try {
				Thread.sleep(20*1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			while(true) {
				if (Math.abs(System.currentTimeMillis()-ts) >= 5*60*1000) {
					ts = System.currentTimeMillis();
					if (eDhcp.discover(utils.getLocalMac()) && !eDhcp.equals(eDhcp.body)) {
						eDhcp.save(eDhcp.body);
						System.err.println("eDhcp config change......................");
					}
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
