package com.konka.advert.service;

import java.util.Timer;
import java.util.TimerTask;

import com.konka.advert.ADLogUtil;

import android.content.Context;
import android.util.Log;

public class ADUpdateManager {
	private String LOGTAG = ADLogUtil.LOGTAG;
	protected Context mContext;
	private Timer timer;
	private TimerTask mTask;
	private long tDelay = 3*60*1000;
	private long tPeriod = 10*60*1000;
	
	public ADUpdateManager(Context context)
	{
		mContext = context;
		
		timer = new Timer();
		mTask = new MyTimerTask();
	}
	
	private class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.i(LOGTAG, "ADUpdateManager timer task run");
		}
		
	}

	public synchronized void invoke()
	{
		timer.cancel();
		mTask.cancel();
		timer = new Timer();
		mTask = new MyTimerTask();
		timer.schedule(mTask, tDelay, tPeriod);
	}
	
	private int dowanloadXML()
	{
		return 0;
	}
	
	private int downloadBootLogo()
	{
		
		return 0;
	}
	
	private int downloadBootvideo()
	{
		return 0;
	}
	
	private int downloadDVBpfLogo()
	{
		return 0;
	}
	
	private int copyToUser(String orig, String dest)
	{
		return 0;
	}
	
	private int verify(String file, String key)
	{
		
		return 0;
	}
	
	
	
	
}
