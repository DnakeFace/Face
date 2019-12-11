package com.dnake.v700;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import android.content.Intent;
import android.graphics.BitmapFactory;

import com.dnake.misc.SysAccess;
import com.dnake.misc.SysCard;
import com.dnake.misc.Sound;
import com.dnake.misc.SysTalk;
import com.dnake.misc.sCaller;
import com.dnake.panel.BaseLabel;
import com.dnake.panel.FaceCompare;
import com.dnake.panel.FaceLabel;
import com.dnake.panel.FaceNormal;
import com.dnake.panel.TalkLabel;
import com.dnake.panel.WakeTask;
import com.dnake.special.NetposaHttpd;
import com.dnake.special.SysProtocol;
import com.dnake.special.SysSpecial;
import com.dnake.special.YmsProtocol;

public class devent {
	private static List<devent> elist = null;
	public static Boolean boot = false;

	public String url;

	public devent(String url) {
		this.url = url;
	}

	public void process(String xml) {
	}

	public static void event(String url, String xml) {
		Boolean err = true;
		if (boot && elist != null) {
			devent e;

			Iterator<devent> it = elist.iterator();
			while (it.hasNext()) {
				e = it.next();
				if (url.equals(e.url)) {
					e.process(xml);
					err = false;
					break;
				}
			}
		}
		if (err)
			dmsg.ack(480, null);
	}

	public static void setup() {
		elist = new LinkedList<devent>();

		devent de;
		de = new devent("/ui/run") {
			@Override
			public void process(String body) {
				if (BaseLabel.mTimerWDT != 0 && Math.abs(System.currentTimeMillis()-BaseLabel.mTimerWDT) > 1*1000)
					return; //界面无响应
				dmsg.ack(200, null);
			}
		};
		elist.add(de);

		de = new devent("/ui/version") {
			@Override
			public void process(String body) {
				dxml p = new dxml();
				String v = String.valueOf(sys.version_major) + "." + sys.version_minor + "." + sys.version_minor2;
				v = v + " " + sys.version_date + " " + sys.version_ex;
				p.setText("/params/version", v);
				p.setInt("/params/proxy", sys.qResult.sip.proxy);
				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/ui/talk/start") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);

				sCaller.reset();
				sCaller.running = sCaller.RINGING;
				sCaller.mId = p.getText("/params/host");
				sCaller.refresh();

				TalkLabel.mMode = TalkLabel.IN;
				SysTalk.start();
			}
		};
		elist.add(de);

		de = new devent("/ui/talk/stop") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				sCaller.bStop = true;
			}
		};
		elist.add(de);

		de = new devent("/ui/talk/play") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				SysTalk.play();
				sCaller.running = sCaller.TALK;
				sCaller.refresh();
			}
		};
		elist.add(de);

		de = new devent("/ui/sip/ringing") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);

				sCaller.mId = p.getText("/params/host");
				sCaller.running = sCaller.RINGING;
				sCaller.refresh();

				TalkLabel.mMode = TalkLabel.OUT;
				SysTalk.start();
			}
		};
		elist.add(de);

		de = new devent("/ui/sip/register") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				sys.qResult.sip.proxy = p.getInt("/params/register", 0);
			}
		};
		elist.add(de);

		de = new devent("/ui/sip/query") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				if (sys.qResult.sip.url == null) {
					dxml p = new dxml();
					p.parse(body);
					sys.qResult.sip.url = new String(p.getText("/params/url"));
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/device/query") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				if (sys.qResult.d600.ip == null) {
					dxml p = new dxml();
					p.parse(body);
					sys.qResult.d600.host = p.getText("/params/name");
					sys.qResult.d600.ip = p.getText("/params/ip");
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/sip/result") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				sys.qResult.result = p.getInt("/params/result", 0);
			}
		};
		elist.add(de);

		de = new devent("/ui/broadcast/data") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				//dxml p = new dxml();
				//p.parse(body);
			}
		};
		elist.add(de);

		de = new devent("/ui/ipwatchd") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				int result = p.getInt("/params/result", 0);
				String ip = p.getText("/params/ip");
				String mac = p.getText("/params/mac");
				SysTalk.ipMacErr(result, ip, mac);
			}
		};
		elist.add(de);

		de = new devent("/ui/touch/event") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				WakeTask.acquire();
				login.refresh();
				FaceLabel.mTouchTs = System.currentTimeMillis();
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/key") {
			private long ts = 0;

			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				String key = p.getText("/params/data");
				if (key.charAt(0) == '*') {
					if (Math.abs(System.currentTimeMillis() - ts) > 3 * 1000) {
						dmsg req = new dmsg();
						dxml p2 = new dxml();
						p2.setInt("/params/key", 4);
						req.to("/settings/key", p2.toString());
					}
					ts = System.currentTimeMillis();
				}
				SysTalk.Keys.offer(key);
				SysTalk.touch(key);
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/ekey") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				WakeTask.acquire();

				dxml p = new dxml();
				p.parse(body);
				int io = p.getInt("/params/io", 0);
				int data = p.getInt("/params/data", 0);
				if (data == 1)
					Sound.play(Sound.OrderPress);

				if (io == 1 && data == 0) { //快速注册特殊标志
					File f = new File("/dnake/bin/fastr");
					if (f!=null && f.exists()) {
						dmsg req = new dmsg();
						req.to("/face/fast/register", null);
					}
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/dio") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				WakeTask.acquire();

				dxml p = new dxml();
				p.parse(body);
				int io = p.getInt("/params/io", 0);
				int data = p.getInt("/params/data", 0);
				System.err.println("io:"+io+" data:"+data);
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/keyCall") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml();
				p.parse(body);
				if (p.getInt("/params/data", 0) == 0) { // 按下
					WakeTask.acquire();
					SysTalk.Keys.offer("X");
					SysTalk.touch("X");
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/unlock") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				dmsg req = new dmsg();
				req.to("/face/unlock", body);
				Sound.play(Sound.OrderUnlock);
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/card") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				WakeTask.acquire();

				dxml p = new dxml(body);
				int iAuth = p.getInt("/params/auth", 1);
				String card = p.getText("/params/data");
				if (card == null)
					card = p.getText("/params/datab");

				SysCard.Data sc = SysCard.verify(card);
				if (iAuth == 1) { //加密验证通过
					if (SysAccess.admin.card != null && SysAccess.admin.card.equalsIgnoreCase("0")) { //设置管理卡
						SysAccess.admin.card = card;
						SysAccess.save();
						Sound.play(Sound.OrderPress);
						return;
					}
					if (card.equalsIgnoreCase(SysAccess.admin.card)) { //管理卡加卡模式
						SysAccess.admin.ts = System.currentTimeMillis();
						Sound.play(Sound.OrderPress);
						return;
					}

					sys.qResult.v170.card = card;
					if (SysAccess.admin.ts != 0) { //管理卡添加
						SysCard.Data d = new SysCard.Data();
						d.card = card;
						d.build = sys.talk.building;
						d.unit = sys.talk.unit;
						d.floor = 1;
						d.family = 1;
						SysCard.add(d);

						SysAccess.admin.ts = System.currentTimeMillis();
						Sound.play(Sound.modify_success, false);
						return;
					}
				} else {
					if (sc != null) { //加密验证失败，疑似复制卡
						if (iAuth == -1) {
							System.err.println("Detect card ["+card+"] copied, delete...");
							SysCard.rm(card);
						}
						sc = null;
					}
				}

				if (sc != null) {
					int b = 0, u = 0, f = 0, r = 0;
					if (sys.panel.mode == 0) { //单元门口机
						b = sys.talk.building;
						u = sys.talk.unit;
						f = sc.floor;
						r = sc.family;
					} else if (sys.panel.mode == 1) { //围墙机
						b = sc.build;
						u = sc.unit;
						f = sc.floor;
						r = sc.family;
					} else if (sys.panel.mode == 2) { //小门口机
						b = sys.talk.building;
						u = sys.talk.unit;
						f = sys.talk.floor;
						r = sys.talk.family;
					}

					SysAccess.unlock(0);
					SysAccess.logger(b, u, f, r, sc.card, 1);

					if (SysAccess.security.enable != 0)
						SysAccess.security(b, u, f, r, sc.card);
					if (sys.panel.mode == 1) //围墙机
						SysAccess.elevWall(b, u, f, r);
					else
						SysAccess.elev(f, r);
				} else {
					//非法卡上报，上海地标
					SysAccess.logger(0, 0, 0, 0, card, 0);
					Sound.play(Sound.OrderCardFailed);
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/elev/appoint") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				dmsg req = new dmsg();
				req.to("/control/elev/appoint", body);
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/elev/permit") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				dmsg req = new dmsg();
				req.to("/control/elev/permit", body);
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/elev/join") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/qr") { //二维码
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml(body);
				String data = p.getText("/params/data");
				if (data != null) {
					if (data.startsWith("acp://"))
						SysSpecial.doACP(data);
					else if (data.startsWith("haina:"))
						SysSpecial.doHaina(data);
					else
						Sound.play(Sound.OrderCardFailed);
				} else
					Sound.play(Sound.OrderCardFailed);
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/exit") { //开门按钮
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml(body);
				int data = p.getInt("/params/data", 0);
				if (data == 0) {
					dmsg req = new dmsg();
					req.to("/face/unlock", null);
					Sound.play(Sound.OrderUnlock);
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/v170/sensor") { //门磁
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
			}
		};
		elist.add(de);

		de = new devent("/ui/center/card") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml(body);
				SysCard.Data d = new SysCard.Data();
				d.card = p.getText("/params/card");
				d.build = p.getInt("/params/build", 0);
				d.unit = p.getInt("/params/unit", 0);
				d.floor = p.getInt("/params/floor", 0);
				d.family = p.getInt("/params/family", 0);
				if (p.getInt("/params/add", 1) == 1)
					SysCard.add(d);
				else
					SysCard.rm(d.card);
			}
		};
		elist.add(de);

		de = new devent("/ui/center/card2") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml(body);
				int total = p.getInt("/params/total", 0);
				for(int i=0; i<total; i++) {
					SysCard.Data d = new SysCard.Data();
					d.card = p.getText("/params/c"+i+"/d");
					if (p.getInt("/params/c"+i+"/a", 1) == 1) {
						d.build = p.getInt("/params/c"+i+"/b", 0);
						d.unit = p.getInt("/params/c"+i+"/u", 0);
						d.floor = p.getInt("/params/c"+i+"/f", 0);
						d.family = p.getInt("/params/c"+i+"/r", 0);
						SysCard.add(d);
					} else
						SysCard.rm(d.card);
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/ir/detect") { //人体感应
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				WakeTask.acquire();

				FaceLabel.mTs = System.currentTimeMillis();
				if (FaceLabel.mContext == null && SysTalk.mBootEnd) {
					Intent it = new Intent(SysTalk.mContext, FaceLabel.class);
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					SysTalk.mContext.startActivity(it);
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/sdt/detect") { // 身份证读取
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				WakeTask.acquire();
				FaceLabel.mTs = System.currentTimeMillis();
				FaceCompare.onDetect(body);
			}
		};
		elist.add(de);

		de = new devent("/ui/sdt/result") { // 人证比对结果
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				FaceCompare.onResult(body);
			}
		};
		elist.add(de);

		de = new devent("/ui/face/reboot") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				FaceLabel.mStartVo = true;
				SysTalk.mBootEnd = true;
			}
		};
		elist.add(de);

		de = new devent("/ui/face/detect") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				WakeTask.acquire();
				FaceLabel.mTs = System.currentTimeMillis();
				if (FaceLabel.mContext == null && sys.ts(FaceLabel.mTouchTs) > 30*1000) {
					Intent it = new Intent(SysTalk.mContext, FaceLabel.class);
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					SysTalk.mContext.startActivity(it);
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/face/result") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml(body);
				FaceNormal.mFaceUid = p.getInt("/params/id", -1);
				FaceNormal.mFaceSim = p.getInt("/params/sim", -1);
				FaceNormal.mFaceBlack = p.getInt("/params/black", 0); // 0:正常 1:黑名单
				FaceNormal.mFaceUrl = p.getText("/params/url");
				FaceNormal.mFaceTs = p.getInt("/params/ts", 0);

				FaceNormal.mFaceGlobal = new SysProtocol.FaceGlobal();
				String jpeg = p.getText("/params/global/url");
				FaceNormal.mFaceGlobal.jpeg = utils.readFile(jpeg);
				FaceNormal.mFaceGlobal.width = p.getInt("/params/global/w", 0);
				FaceNormal.mFaceGlobal.height = p.getInt("/params/global/h", 0);
				FaceNormal.mFaceGlobal.f_x = p.getInt("/params/face/x", 0);
				FaceNormal.mFaceGlobal.f_y = p.getInt("/params/face/y", 0);
				FaceNormal.mFaceGlobal.f_w = p.getInt("/params/face/w", 0);
				FaceNormal.mFaceGlobal.f_h = p.getInt("/params/face/h", 0);

				FaceNormal.mFaceCms = false;
				FaceNormal.mFaceWx = false;
				FaceNormal.mFaceHave = true;
			}
		};
		elist.add(de);

		de = new devent("/ui/cms/result") { //集中管理识别结果
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml(body);
				FaceNormal.mFaceUid = p.getInt("/params/id", 0);
				FaceNormal.mFaceSim = p.getInt("/params/data", 0);
				FaceNormal.mFaceUrl = p.getText("/params/url");
				FaceNormal.mFaceTs = p.getInt("/params/ts", 0);

				FaceNormal.mFaceGlobal = new SysProtocol.FaceGlobal();
				String jpeg = p.getText("/params/global/url");
				FaceNormal.mFaceGlobal.jpeg = utils.readFile(jpeg);
				FaceNormal.mFaceGlobal.width = p.getInt("/params/global/w", 0);
				FaceNormal.mFaceGlobal.height = p.getInt("/params/global/h", 0);
				FaceNormal.mFaceGlobal.f_x = p.getInt("/params/face/x", 0);
				FaceNormal.mFaceGlobal.f_y = p.getInt("/params/face/y", 0);
				FaceNormal.mFaceGlobal.f_w = p.getInt("/params/face/w", 0);
				FaceNormal.mFaceGlobal.f_h = p.getInt("/params/face/h", 0);

				FaceNormal.mFaceCms = true;
				FaceNormal.mFaceWx = false;
				FaceNormal.mFaceHave = true;
			}
		};
		elist.add(de);

		de = new devent("/ui/wx/result") { //微云门禁识别结果
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml(body);
				FaceNormal.mFaceUid = p.getInt("/params/id", 0);
				FaceNormal.mFaceSim = p.getInt("/params/data", 0);
				FaceNormal.mFaceUrl = p.getText("/params/url");
				FaceNormal.mFaceName = p.getText("/params/name");
				FaceNormal.mFaceTs = p.getInt("/params/ts", 0);

				FaceNormal.mFaceGlobal = new SysProtocol.FaceGlobal();
				String jpeg = p.getText("/params/global/url");
				FaceNormal.mFaceGlobal.jpeg = utils.readFile(jpeg);
				FaceNormal.mFaceGlobal.width = p.getInt("/params/global/w", 0);
				FaceNormal.mFaceGlobal.height = p.getInt("/params/global/h", 0);
				FaceNormal.mFaceGlobal.f_x = p.getInt("/params/face/x", 0);
				FaceNormal.mFaceGlobal.f_y = p.getInt("/params/face/y", 0);
				FaceNormal.mFaceGlobal.f_w = p.getInt("/params/face/w", 0);
				FaceNormal.mFaceGlobal.f_h = p.getInt("/params/face/h", 0);

				FaceNormal.mFaceCms = false;
				FaceNormal.mFaceWx = true;
				FaceNormal.mFaceHave = true;
			}
		};
		elist.add(de);

		de = new devent("/ui/face/capture") { //特殊抓拍接口
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml(body);
				String url = p.getText("/params/url");
				if (url != null) {
					SysProtocol.FaceData d = new SysProtocol.FaceData();
					d.id = -1;
					d.name = "";
					d.bmp = BitmapFactory.decodeFile(url);
					d.sim = 0;
					d.identity = "";
					d.black = false;
					d.channel = p.getInt("/params/channel", 0);
					d.ts = p.getInt("/params/ts", 0);

					String jpeg = p.getText("/params/global/url");
					d.global = new SysProtocol.FaceGlobal();
					d.global.jpeg = utils.readFile(jpeg);
					d.global.width = p.getInt("/params/global/w", 0);
					d.global.height = p.getInt("/params/global/h", 0);
					d.global.f_x = p.getInt("/params/face/x", 0);
					d.global.f_y = p.getInt("/params/face/y", 0);
					d.global.f_w = p.getInt("/params/face/w", 0);
					d.global.f_h = p.getInt("/params/face/h", 0);
					SysProtocol.face(d);
					FaceNormal.onFaceCapture(d);
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/face/jpeg") { //jpeg人脸添加结果
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml(body);
				int code = p.getInt("/params/result", 0);
				int rid = p.getInt("/params/rid", 0);
				if (NetposaHttpd.mRid == rid) {
					NetposaHttpd.mResultOK = true;
					NetposaHttpd.mResultCode = code;
				}
			}
		};
		elist.add(de);

		de = new devent("/ui/face/lived") { //活体检测事件
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml(body);
				FaceLabel.FaceLivedData d = new FaceLabel.FaceLivedData();
				d.mode = p.getInt("/params/mode", 0);
				d.url = p.getText("/params/url");
				FaceLabel.mFaceLived.offer(d);
			}
		};
		elist.add(de);

		//快速人脸注册提示，演示用
		de = new devent("/ui/face/fast/register") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				Sound.play(Sound.modify_success, false);
			}
		};
		elist.add(de);

		de = new devent("/ui/web/sys/read") {
			@Override
			public void process(String body) {
				dxml p = new dxml();
				p.setInt("/params/language", sys.language());
				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/ui/web/sys/write") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				dxml p = new dxml(body);
				int val = p.getInt("/params/language", sys.language());
				sys.language(val);
			}
		};
		elist.add(de);

		de = new devent("/ui/web/protocol/read") {
			@Override
			public void process(String body) {
				dxml p = new dxml();
				p.setInt("/params/enable", SysProtocol.mEnable);
				p.setText("/params/host", SysProtocol.mHost);
				p.setText("/params/host2", SysProtocol.mHost2);
				p.setText("/params/code", SysProtocol.mCode);
				p.setInt("/params/protocol", SysProtocol.mProtocol);
				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/ui/web/protocol/write") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml(body);
				SysProtocol.mEnable = p.getInt("/params/enable", SysProtocol.mEnable);
				SysProtocol.mHost = p.getText("/params/host", SysProtocol.mHost);
				SysProtocol.mHost2 = p.getText("/params/host2", SysProtocol.mHost2);
				SysProtocol.mCode = p.getText("/params/code", SysProtocol.mCode);
				SysProtocol.mProtocol = p.getInt("/params/protocol", SysProtocol.mProtocol);
				if (SysProtocol.mHost != null && !SysProtocol.mHost.startsWith("http://") && !SysProtocol.mHost.startsWith("https://"))
					SysProtocol.mHost = "http://"+SysProtocol.mHost;
				SysProtocol.save();
			}
		};
		elist.add(de);

		de = new devent("/ui/web/yms/read") {
			@Override
			public void process(String body) {
				dxml p = new dxml();
				p.setText("/params/school", YmsProtocol.m_school_id);
				p.setText("/params/app/url", YmsProtocol.m_url);
				p.setText("/params/app/id", YmsProtocol.m_app_id);
				p.setText("/params/app/key", YmsProtocol.m_app_key);
				p.setText("/params/app/secret", YmsProtocol.m_app_secret);
				p.setText("/params/app/phone", YmsProtocol.m_app_phone);
				p.setText("/params/devid", YmsProtocol.m_devid);
				dmsg.ack(200, p.toString());
			}
		};
		elist.add(de);

		de = new devent("/ui/web/yms/write") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);

				dxml p = new dxml(body);
				YmsProtocol.m_url = p.getText("/params/app/url", YmsProtocol.m_url);
				YmsProtocol.m_app_id = p.getText("/params/app/id", YmsProtocol.m_app_id);
				YmsProtocol.m_app_key = p.getText("/params/app/key", YmsProtocol.m_app_key);
				YmsProtocol.m_app_secret = p.getText("/params/app/secret", YmsProtocol.m_app_secret);
				YmsProtocol.m_app_phone = p.getText("/params/app/phone", YmsProtocol.m_app_phone);
				YmsProtocol.m_devid = p.getText("/params/devid", YmsProtocol.m_devid);
				YmsProtocol.save();
			}
		};
		elist.add(de);

		de = new devent("/ui/web/yms/control") {
			@Override
			public void process(String body) {
				dmsg.ack(200, null);
				dxml p = new dxml(body);
				String cmd = p.getText("/params/cmd", "");
				if (cmd.equals("add"))
					YmsProtocol.doRegister();
				else if (cmd.equals("delete"))
					YmsProtocol.doDelete();
				else if (cmd.equals("sync"))
					YmsProtocol.m_person_ts = 0;
			}
		};
		elist.add(de);
	}
}
