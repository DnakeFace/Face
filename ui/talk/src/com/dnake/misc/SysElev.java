package com.dnake.misc;

import com.dnake.v700.dmsg;
import com.dnake.v700.dxml;
import com.dnake.v700.sys;
import com.dnake.v700.vt_uart;

public class SysElev {

	public static void unit(int f, int r, int to) {
		byte[] d = new byte[256];
		d[0] = (byte)0xA0;
		d[1] = (byte)(((to/10)<<4) | (to%10));
		d[2] = (byte)0x00;
		d[3] = (byte)0x4F;
		d[4] = (byte)0xFF;
		d[5] = (byte)(((f/10)<<4) | (f%10));
		d[6] = (byte)(((r/10)<<4) | (r%10));
		d[7] = 0;

		for (int i=0; i<7; i++)
			d[7] += d[i];
		d[7] ^= d[0];
		vt_uart.tx(d, 8);
	}

	public static void wall(int elev, int to, int b, int u, int f, int r) {
		byte[] d = new byte[256];
		short ck = 0;
		int t;

		d[0] = (byte)0xB0;
		d[1] = (byte)0x55;
		d[2] = (byte)0xAA;
		d[3] = (byte)13;
		d[4] = (byte)0xA0;
		d[5] = (byte)(((to/10)<<4) | (to%10));
		d[6] = (byte)0xFF;
		d[7] = (byte)0;
		t = sys.panel.index;
		d[8] = (byte)(((t/10)<<4) | (t%10));
		d[9] = (byte)0xFF;
		t = b/100;
		d[10] = (byte)(((t/10)<<4) | (t%10));
		t = b%100;
		d[11] = (byte)(((t/10)<<4) | (t%10));
		d[12] = (byte)(((u/10)<<4) | (u%10));
		d[13] = (byte)(((f/10)<<4) | (f%10));
		d[14] = (byte)(((r/10)<<4) | (r%10));

		for (int i=4; i<15; i++)
			ck += d[i];
		d[15] = (byte)((ck>>8)&0xFF);
		d[16] = (byte)(ck&0xFF);

		vt_uart.tx(d, 17);
	}

	public static void appoint(int elev, int direct, int f, int r) {
		byte[] data = new byte[256];
		data[0] = (byte)0xA2;
		data[1] = (byte)(elev+1);
		data[2] = (byte)direct;
		data[3] = (byte)0x4F;
		data[4] = (byte)0xFF;
		data[5] = (byte)(((f/10)<<4) | (f%10));
		data[6] = (byte)(((r/10)<<4) | (r%10));
		data[7] = 0;
		for (int i=0; i<7; i++)
			data[7] += data[i];
		data[7] ^= data[0];

		vt_uart.tx(data, 8);
	}

	public static void permit(int elev, int f, int r) {
		byte[] data = new byte[256];

		data[0] = (byte)0xA1;
		data[1] = (byte)0x55;
		data[2] = (byte)(((f/10)<<4) | (f%10));
		data[3] = (byte)0x4F;
		data[4] = (byte)0xFF;
		data[5] = (byte)(((f/10)<<4) | (f%10));
		data[6] = (byte)(((r/10)<<4) | (r%10));
		data[7] = 0;

		for (int i=0; i<7; i++)
			data[7] += data[i];
		data[7] ^= data[0];

		vt_uart.tx(data, 8);
	}

	public static void visit(int f, int r, int f2, int r2) {
		byte[] data = new byte[256];

		data[0] = (byte)0xA8;
		data[1] = (byte)(((f/10)<<4) | (f%10));
		data[2] = (byte)(((r/10)<<4) | (r%10));
		data[3] = (byte)0x4F;
		data[4] = (byte)0xFF;
		data[5] = (byte)(((f2/10)<<4) | (f2%10));
		data[6] = (byte)(((r2/10)<<4) | (r2%10));
		data[7] = 0;

		for (int i=0; i<7; i++)
			data[7] += data[i];
		data[7] ^= data[0];

		vt_uart.tx(data, 8);
	}

	public static class ProcessThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				SysElev.process();

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static Thread sThread = null;

	public static final int MAX = 16;
	public static String url[] = new String[MAX];
	public static long uTs[] = new long[MAX];

	public static void start() {
		for(int i=0; i<MAX; i++) {
			url[i] = null;
			uTs[i] = 0;
		}
		if (sThread == null) {
			ProcessThread p = new ProcessThread();
			sThread = new Thread(p);
			sThread.start();
		}
	}

	public static void join(String u) {
		for(int i=0; i<MAX; i++) {
			if (url[i] != null && url[i].equals(u)) {
				uTs[i] = System.currentTimeMillis();
				return;
			}
		}
		for(int i=0; i<MAX; i++) {
			if (url[i] == null) {
				url[i] = u;
				uTs[i] = System.currentTimeMillis();
				return;
			}
		}
	}

	public static long qTs = 0;
	public static void process() {
		Boolean have = false;
		for(int i=0; i<MAX; i++) {
			if (url[i] != null && Math.abs(System.currentTimeMillis()-uTs[i]) >= 30*1000)
				url[i] = null;
			if (url[i] != null)
				have = true;
		}
		if (have && Math.abs(System.currentTimeMillis()-qTs) >= 2*1000) {
			for(int i=0; i<3; i++)
				SysElev.query(i);
		}
	}

	public static void query(int elev) {
		byte[] d = new byte[256];
		short ck = 0;
		int t;

		d[0] = (byte)0xB0;
		d[1] = (byte)0x55;
		d[2] = (byte)0xAA;
		d[3] = (byte)10;
		d[4] = (byte)0xA3;
		d[5] = (byte)elev;
		d[6] = (byte)0xFF;
		t = sys.talk.building/100;
		d[7] = (byte)(((t/10)<<4) | (t%10));
		t = sys.talk.building%100;
		d[8] = (byte)(((t/10)<<4) | (t%10));
		t = sys.talk.unit;
		d[9] = (byte)(((t/10)<<4) | (t%10));
		d[10] = 0x01;
		d[11] = 0x01;

		for (int i=4; i<12; i++)
			ck += d[i];
		d[12] = (byte)((ck>>8)&0xFF);
		d[13] = (byte)(ck&0xFF);

		vt_uart.tx(d, 14);

		int idx = 0;
		for(int n=0; n<10; n++) {
			byte d2[] = new byte[256];
			int ret = vt_uart.rx(d2, 40);
			if (ret > 0) {
				for(int i=0; i<ret; i++)
					d[idx+i] = d2[i];
				idx += ret;

				if (idx >= 18 && d[0] == (byte)0xB0 && d[1] == (byte)0x55 && d[2] == (byte)0xAA && d[4] == (byte)0xA6) {
					ck = 0;
					for (int i=4; i<17; i++)
						ck += d[i]&0xFF;

					if ((byte) (ck >> 8) == d[17] && (byte) (ck & 0xFF) == d[18]){
						int direct = d[12];
						int sign = (d[13] == 0x2B ? 1 : -1);
						String s = String.format("%c%c%c", d[14], d[15], d[16]);

						if (direct == 0x04)
							direct = -1;

						dmsg req = new dmsg();
						dxml p = new dxml();
						p.setInt("/params/data/params/index", elev);
						p.setInt("/params/data/params/direct", direct);
						p.setInt("/params/data/params/sign", sign);
						p.setText("/params/data/params/display", s);
						p.setText("/params/data/params/event_url", "/elev/data");

						for(int i=0; i<MAX; i++) {
							if (url != null) {
								p.setText("/params/to", url[i]);
								req.to("/talk/sip/sendto", p.toString());
							}
						}
					}
					break;
				}
			}
		}
	}
}
