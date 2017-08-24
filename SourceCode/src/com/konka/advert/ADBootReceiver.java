package com.konka.advert;

import com.konka.advert.service.ADUpdateService;
import com.konka.advert.utils.ADLogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class ADBootReceiver extends BroadcastReceiver{

	private String LOGTAG = ADLogUtil.LOGTAG;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		Log.i(LOGTAG, ADLogUtil.getDebugInfo() + "---" + intent.getAction());
		
		if( "android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) ) {
			context.startService(new Intent(context, ADUpdateService.class));
		}
	}

}
