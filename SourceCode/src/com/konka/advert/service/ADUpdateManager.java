package com.konka.advert.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.google.gson.Gson;
import com.konka.advert.utils.ADLogUtil;
import com.konka.advert.utils.FormatUtils;
import com.konka.advert.xml.XmlAdUpdateInfo;
import com.konka.advert.xml.XmlAdUpdateInfo.AdImageInfo;
import com.konka.advert.xml.XmlAdUpdateInfo.Scope;
import com.konka.advert.xml.XmlParser;
import com.konka.advert.xml.XmlParserListener;
import com.platform.curr.PlatfUtil;
import com.swcom.servutils.commtool.encryptutils.ShaUtils;
import com.swcom.servutils.commtool.fileutils.FileUtils;
import com.swcom.servutils.commtool.ldutils.FileLoad;
import com.swcom.servutils.commtool.ldutils.LoadCBfun;
import com.swcom.servutils.commtool.ldutils.LoadList;
import com.swcom.servutils.commtool.ldutils.LoadList.LoadInfo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class ADUpdateManager {
	private String LOGTAG = ADLogUtil.LOGTAG;
	protected Context mContext;
	
	private Timer timer = null;
	private TimerTask mTask = null;
	private long DELAY_TIME = 60*1000;		//1 minute
	private long PERIOD_TIME = 6*60*60*1000;	//6h
	
	private FileLoad loadServ = null;
	private boolean multDownloadReady = false;
	private String BKP_FILE_NAME = "breakpoint.txt";
	private String XML_DOWNLOAD_XID = "xmlDownloadXid";
	private String xmlDloadPath = null;
	private String imgDloadPath = null;
	private XmlAdUpdateInfo mXmlInfo = null;
	
	private String nativeManufacture = null; 
	private String nativeModelId = null; 
	private String nativeSN = null;
	private String nativeAdVersion = null;
	private String nativeAdServUrl = null;

	private HashMap<String, Integer> xidHashMap;
	private enum xidHandleCmd {
					INSERT,
					DELETE,
					QUERY,
					ISEMPTY,
					RESET
					};
	
	public ADUpdateManager(Context context)
	{
		mContext = context;
		imgDloadPath = mContext.getFilesDir().getAbsolutePath() + "/Download";
		File file = new File(imgDloadPath);
		if(!file.exists()) {
			file.mkdirs();
		}
		xmlDloadPath = imgDloadPath + "/kk_advert.xml";
		
		SharedPreferences adPref = mContext.getSharedPreferences("ad_database", Activity.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = adPref.edit();
		PlatfUtil mPlatUtil = new PlatfUtil();
		
		nativeManufacture = mPlatUtil.getManufacture();
		nativeModelId = mPlatUtil.getManufactureModeId();
		nativeSN = mPlatUtil.getManufactureSerialNumber();
		Log.i(LOGTAG, "nativeManufacture=" + nativeManufacture + "---" + "nativeModelId=" + nativeModelId
				+ "---" + "nativeSN=" + nativeSN);
		
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
		Log.i(LOGTAG, "nativeAdVersion=" + nativeAdVersion + "---" + "nativeAdServUrl=" + nativeAdServUrl);
		
		timer = new Timer();
		mTask = new MyTimerTask();
		
		xidHashMap = new HashMap<String, Integer>();
	}
	
	public synchronized void invoke()
	{
		timer.cancel();
		mTask.cancel();
		timer = new Timer();
		mTask = new MyTimerTask();
		timer.schedule(mTask, DELAY_TIME, PERIOD_TIME);
	}
	
	private void checkServerXml()
	{
		if(null == loadServ) {
			loadServ = new FileLoad();
		}
		
		Log.i(LOGTAG, "XML_DOWNLOAD_XID=" + XML_DOWNLOAD_XID 
				+ "---" + "xmlDloadPath=" + xmlDloadPath
				+ "---" + "nativeAdServUrl=" + nativeAdServUrl);
		downloadFile(XML_DOWNLOAD_XID, xmlDloadPath, nativeAdServUrl);		
	}
	
	private void parserAdXml()
	{
		File file = new File(xmlDloadPath);
		if(true != file.exists()) {
			Log.e(LOGTAG, "advert xml file is not exists. file=" + xmlDloadPath);
			return;
		}		
		
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = saxFactory.newSAXParser();
			XmlParser xmlParser = new XmlParser();
			xmlParser.setXmlParserListener(new XmlListener());
			try {
				saxParser.parse(file, xmlParser);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	private void checkUpdate()
	{
		if(nativeManufacture.equals(mXmlInfo.getManufacture())) {
			if(nativeModelId.equals(mXmlInfo.getModelID())) {
				long adNatVersion = FormatUtils.fromString2Long(nativeAdVersion);
				long adSerVersion = FormatUtils.fromString2Long(mXmlInfo.getAdServerVersion());
				long adSerStbVersion = FormatUtils.fromString2Long(mXmlInfo.getAdStbVersion());
				if( (adNatVersion < adSerVersion) && ( (adNatVersion==adSerStbVersion) || (0==adSerStbVersion) ) ) {
					long curSN;
					try {
					curSN = FormatUtils.fromString2Long(nativeSN);
					if(-1==curSN) {
						Log.e(LOGTAG, "curSN is null !!!"); 
						return;
					}
					} catch (Exception e){
						Log.e(LOGTAG, "curSN error !!!");
						return;
					}
					boolean inside = false;
					for(Scope scope:mXmlInfo.getScopeList()) {
						long startSN = FormatUtils.fromString2Long(scope.getStartSN());
						long endSN = FormatUtils.fromString2Long(scope.getEndSN());
						if(curSN >= startSN && curSN <= endSN) {
							inside = true;
						}
						else {
							Log.e(LOGTAG, "curSN not inside:" + "curSN=" + curSN 
									+ "---" + "startSN=" + startSN 
									+ "---" + "endSN=" +endSN);
						}
					}
					if(true == inside) {
						beginDownloadImg(mXmlInfo);
					}	
				} else {
					Log.i(LOGTAG, "ad version is not matching:" + "nativeAdVersion=" + adNatVersion 
							+ "---" + "adSerVersion=" + adSerVersion
							+ "---" + "adSerStbVersion" + adSerStbVersion);
				}
			} else {
				Log.i(LOGTAG, "ModelId is not matching:" + "nativeModelId=" + nativeModelId 
						+ "---" + "servModelId=" + mXmlInfo.getModelID());
			}
		} else {
			Log.i(LOGTAG, "Manufacture is not matching:" + "nativeManufacture=" + nativeManufacture 
					+ "---" + "servManufacture=" + mXmlInfo.getManufacture());
		}
	}
	
	private synchronized void setMultDownloadReady(boolean flag)
	{
		multDownloadReady = flag;
	}
	
	private boolean getMultDownloadReady()
	{
		return multDownloadReady;
	}
	
	private void beginDownloadImg(XmlAdUpdateInfo info)
	{
		Iterator<AdImageInfo> iterator = info.getAdImgInfoList().iterator();
		setMultDownloadReady(true);
		handleXid(xidHandleCmd.RESET, null);
		String downloadDes;
		while(iterator.hasNext()) {
			XmlAdUpdateInfo.AdImageInfo mAdImgInfo = iterator.next();
			if(true == mAdImgInfo.isUpdate()) {
				downloadDes = imgDloadPath + "/" + mAdImgInfo.getFile();
				downloadFile(mAdImgInfo.getName(), downloadDes, mAdImgInfo.getUrl());
			}
		}
		setMultDownloadReady(false);
	}
	
	private void downloadFile(String name, String dloadPath, String url)
	{
		LoadList loList = null;
		LoadList.LoadInfo loInfo = null;
		String xid = name;
		String downlaodPath = dloadPath;
		
		String contt = FileUtils.ReadFileToStr(BKP_FILE_NAME);
		if(contt != null)
		{
			Gson gson = new Gson();
			loList = gson.fromJson(contt, LoadList.class);
			if(loList != null)
			{
				loInfo = loList.getLoadInfo(xid);
			}
		}
		
		if(null == loInfo)	
		{
			loInfo  = new LoadList.LoadInfo();
			loInfo.setXID(xid);
			loInfo.setTargetFile(downlaodPath);
			loInfo.setSupportBP(LoadList.YES);
			loInfo.setLoadType(LoadList.DOWNLOAD);
			loInfo.setUnitLength("5242880"); //5M*1024*1024=5242880
			loInfo.setUrl(url);
		}
		
		loadServ.SetCBfun(new FileLoadCBfun());
		loadServ.SetConfigFilePath(BKP_FILE_NAME);
		loadServ.Request(loInfo);
		
		handleXid(xidHandleCmd.INSERT, xid);
	}
	
	synchronized boolean handleXid(xidHandleCmd cmd, String xid)
	{
		switch(cmd) {
			case INSERT:
				xidHashMap.put(xid, 1);
				break;
			case DELETE:
				xidHashMap.remove(xid);
				break;
			case QUERY:
				xidHashMap.containsKey(xid);
				return true;
			case ISEMPTY:
				return xidHashMap.isEmpty();
			case RESET:
				xidHashMap.clear();
			default:
				break;
		}
		
		return true;
	}
	
	private void verifyImg()
	{
		Iterator<AdImageInfo> iterator;
		AdImageInfo info;
		boolean veriryOk = false; 
		String img;
		String md5Key;
		String downPath;
		String destPath;
		
		iterator = mXmlInfo.getAdImgInfoList().iterator();
		while(iterator.hasNext()) {
			info = iterator.next();
			if( info.isUpdate() ) {
				img = imgDloadPath + "/" + info.getFile();
				if(!(new File(img).exists())) {
					veriryOk = false;
					Log.i(LOGTAG, "verify fail, dowanload file no exist:" + "file=" + img);
					break;
				}
				md5Key = info.getMd5Key();
				if( 0 != ShaUtils.CheckFile(ShaUtils.MD5, img, md5Key) ){
					veriryOk = false;
					Log.i(LOGTAG, "verify fail:\n    "
										+ "file=" + img + "\n    "
										+ "md5key=" + md5Key);
					break;
				} else {
					veriryOk = true;
				}
			}
			
		}
		
		if(true == veriryOk) {
			iterator = mXmlInfo.getAdImgInfoList().iterator();
			//begin copy files
			writeResult("verifyOK=false\n");
			while(iterator.hasNext()) {
				info = iterator.next();
				if( info.isUpdate() ) {
					downPath = imgDloadPath + "/" + info.getFile();
					destPath = info.getDestPath() + "/" + info.getFile();
					Log.i(LOGTAG, "copy file:\n    "
							+ "downPath=" + downPath + "\n    "
							+ "destPath=" + destPath);
					//chmodFile(info.getDestPath());
					FileUtils.copyFile(downPath, destPath, true);
				}
			}
			
			veriryOk = false;
			iterator = mXmlInfo.getAdImgInfoList().iterator();
			while(iterator.hasNext()) {
				info = iterator.next();
				if( info.isUpdate() ) {
					img = info.getDestPath() + "/" + info.getFile();
					md5Key = info.getMd5Key();
					if( 0 != ShaUtils.CheckFile(ShaUtils.MD5, img, md5Key) ){
						veriryOk = false;
						Log.i(LOGTAG, "verify fail:\n    "
								+ "file=" + img + "\n    "
								+ "md5key=" + md5Key);
						break;
					} else {
						chmodFile(img);
						veriryOk = true;
					}
				}
			}
			
			if(true == veriryOk) {				
				writeResult("verifyOK=true\n");
				updateVersionInfo();
			}
		}
	}
	
	private void writeResult(String res)
	{
		String vf = mXmlInfo.getVerifyFile();
		Log.i(LOGTAG, "Write verify result:" + res + " to file=" + vf);
		if(null != vf) {
			FileUtils.delFile(vf);
			FileUtils.WriteToFile(vf, res);
			chmodFile(vf);
		}
	}
	
	private void updateVersionInfo()
	{
		SharedPreferences adPref = mContext.getSharedPreferences("ad_database", Activity.MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = adPref.edit();		
		nativeAdVersion = mXmlInfo.getAdServerVersion();
		prefEditor.putString("ad_version", nativeAdVersion);
		prefEditor.commit();
	}
	
	private void chmodFile(String fileName)
	{
		//File f = new File(fileName);
		String prog = "busybox chmod 644 " + fileName;
		try {
			Runtime.getRuntime().exec(prog);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.i(LOGTAG, "ADUpdateManager timer task run");
			checkServerXml();
		}
		
	}
	
	class XmlListener implements XmlParserListener {

		@Override
		public void onError(SAXParseException e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSuccess(XmlAdUpdateInfo info) {
			// TODO Auto-generated method stub
			if(null == info) {
				Log.e(LOGTAG, "pareser advert xml info is null.");
				return;
			}
			info.printAll();
			mXmlInfo = info;
			checkUpdate();
		}
	}
	
	class FileLoadCBfun extends LoadCBfun {

		@Override
		public void DownloadFailure(LoadInfo info, String step, String result,
				String param) {
			// TODO Auto-generated method stub
			Log.i(LOGTAG, ADLogUtil.getDebugInfo() + "---" + info.getXID() 
					+ "---" + "step=" + step
					+ "---" + "param" + param);
			if(null != info.getXID()) {
				handleXid(xidHandleCmd.DELETE, info.getXID());
			}
			super.DownloadFailure(info, step, result, param);
		}

		@Override
		public void DownloadResponse(LoadInfo info, String step, String result,
				String param) {
			// TODO Auto-generated method stub
			Log.i(LOGTAG, ADLogUtil.getDebugInfo() + "---" + info.getXID() 
													+ "---" + "res=" + result
													+ "---" + "param" + param);
			if( "Succeed".equals(result) ) {
					if(null != info.getXID()) {
						handleXid(xidHandleCmd.DELETE, info.getXID());
						
						if( XML_DOWNLOAD_XID.equals(info.getXID()) ) {
							parserAdXml();
						} else {
							if(false == getMultDownloadReady()) {
								if(handleXid(xidHandleCmd.ISEMPTY, null)) {
									verifyImg();
								}
							}
						}
					}
						
				}
			super.DownloadResponse(info, step, result, param);
		}		
	}

}
