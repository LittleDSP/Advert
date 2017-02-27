package com.konka.advert.service;

import com.konka.advert.ADLogUtil;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ADUpdateService extends Service{

	private String LOGTAG = ADLogUtil.LOGTAG; 
	private ADUpdateManager mUpdateManager;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.i(LOGTAG, "ADUpdateService is created");
		mUpdateManager = new ADUpdateManager(getApplicationContext());
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.i(LOGTAG, "ADUpdateService process:" + startId);
		mUpdateManager.invoke();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
