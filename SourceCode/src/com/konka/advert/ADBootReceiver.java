package com.konka.advert;

import com.konka.advert.service.ADUpdateService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class ADBootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.i(ADLogUtil.LOGTAG, intent.getAction());
		if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			Log.i(ADLogUtil.LOGTAG, "ADBootReceiver enter");
			context.startService(new Intent(context, ADUpdateService.class));
		}
	}

}
