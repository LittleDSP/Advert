package com.konka.advert;

import com.konka.advert.utils.ADLogUtil;
import com.platform.curr.PlatfUtil;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class MainActivity extends Activity {

	private String LOGTAG = ADLogUtil.LOGTAG;
	
	private TextView txvAdVersion = null;
	private TextView txvAdServUrl = null;
	
	private String nativeAdVersion = null;
	private String nativeAdServUrl = null;
	private SharedPreferences adPref = null;
	private String adDbaseName = "ad_database";
	private PlatfUtil mPlatUtil = null;
	private SharedPreferences.Editor prefEditor = null;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Log.i(LOGTAG, ADLogUtil.getDebugInfo());
        
        txvAdVersion = (TextView)findViewById(R.id.txview_adversion);
        txvAdServUrl = (TextView)findViewById(R.id.txview_adservurl);
        
        adPref = MainActivity.this.getSharedPreferences(adDbaseName, MODE_PRIVATE);
        prefEditor = adPref.edit();
        mPlatUtil = new PlatfUtil();
    }

    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
    	/***
    	 * priority 0: get advertisement information from the app database.
    	 * priority 1: get advertisement information from the system properties.
    	 *     ---precondition: if (priority 0) have no data.
    	 ***/
    	nativeAdVersion = adPref.getString("ad_version", "noValue");
    	nativeAdServUrl = adPref.getString("ad_url", "noValue");
    	
    	if("noValue".equals(nativeAdVersion)) {
    		nativeAdVersion = mPlatUtil.getManufactureADVersion();
    		prefEditor.putString("ad_version", nativeAdVersion);
			prefEditor.commit();
    	}
    	if("noValue".equals(nativeAdServUrl)) {
    		nativeAdServUrl = mPlatUtil.getManufactureADServerURL();
    		prefEditor.putString("ad_url", nativeAdServUrl);
			prefEditor.commit();
    	}
    	
    	txvAdVersion.setText(nativeAdVersion);
    	txvAdServUrl.setText(nativeAdServUrl);
    	
		super.onResume();
	}
}
