package com.dnake.v700;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class vt_uart {

	public static DatagramSocket uart = null;
	public static int tPort = 10060;
	public static int rPort = 10061;

	public static Boolean start() {
		if (uart != null)
			return true;

		try {
			uart = new DatagramSocket(rPort);

			vt_uart_thread u = new vt_uart_thread();
	        Thread thread = new Thread(u);
	        thread.start();

	        return true;
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void setup(int pb, int br) {
		//pb  0:禁用  1:奇校验  2:偶校验
		//br  0:1200 1:2400 2:4800 3:9600 4:19200 5:38400 6:57600 7:115200
		dmsg req = new dmsg();
		dxml p = new dxml();
		p.setInt("/params/parity", pb);
		p.setInt("/params/speed", br);
		p.setInt("/params/mode", 1);
		req.to("/control/vt_uart/setup", p.toString());
	}

	public static void tx(byte[] data, int length) {
		if (uart != null) {
			try {
				DatagramPacket p = new DatagramPacket(data, length, InetAddress.getByName("127.0.0.1"), tPort);
				uart.send(p);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static int rx(byte[] data, int timeout) {
		int len = 0;
		if (uart != null) {
			long ts = System.currentTimeMillis();

			uart_len = 0;
			while(Math.abs(System.currentTimeMillis()-ts) < timeout) {
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (uart_len > 0) {
					if (len > data.length)
						len = data.length;
					else
						len = uart_len;
					System.arraycopy(uart_rx, 0, data, 0, len);
					break;
				}
			}
		}
		return len;
	}

	public static byte[] uart_rx = new byte[8*1024];
	public static int uart_len = 0;

	public static class vt_uart_thread implements Runnable {
		@Override
		public void run() {
			while(true) {
				try {
					byte[] data = new byte[8*1024];
					DatagramPacket p = new DatagramPacket(data, data.length);
					uart.receive(p);
					if (p.getLength() > 0 && uart_len+p.getLength() < uart_rx.length) {
						System.arraycopy(p.getData(), 0, uart_rx, uart_len, p.getLength());
						uart_len += p.getLength();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
