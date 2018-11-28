package com.dnake.desktop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SysReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context ctx, Intent it) {
		String a = it.getAction();
		if (a.equals("android.intent.action.BOOT_COMPLETED")) {
			Intent intent = new Intent(ctx, SysService.class);
			ctx.startService(intent);
		} else if (a.equals("com.dnake.broadcast")) {
		}
	}
}
