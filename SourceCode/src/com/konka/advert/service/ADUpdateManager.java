package com.konka.advert.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.google.gson.Gson;
import com.konka.advert.ADLogUtil;
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
//import android.os.SystemProperties;

public class ADUpdateManager {
	private String LOGTAG = ADLogUtil.LOGTAG;
	protected Context mContext;
	private Timer timer;
	private TimerTask mTask;
	private long tDelay = 15*1000;
	private long tPeriod = 10*60*1000;
	private String xmlFilePath = "/data/kkadvert/";
	private String xmlFileName = "ad.xml";
	private String xmlFile = xmlFilePath + xmlFileName;
	
	private String xmlUrl = "http://stbupdate.kkapp.com/test/advert/HAN873/HAN873-YILI/kkad_update_des.xml";
	private String xmlDes = xmlFile;
	private String bkpointFile = "/data/kkadvert/breakpoint.txt";
	
	private XmlAdUpdateInfo xmlInfo;
	
	private String nativeManufacture = null; 
	private String nativeModelId = null; 
	private String nativeSN = null;
	private String nativeHwVersion = null;
	private String nativeSwVersion = null;
	private String nativeADVersion = null; //SystemProperties.get("ro.config.server_xml_url");
	
	private HashMap<String, Integer> xidHashMap = new HashMap<String, Integer>();
	private enum xidHandleCmd {
					INSERT,
					DELETC,
					QUERY,
					ISEMPTY
					};
	
	private FileLoad loadServ = null;
	
	public ADUpdateManager(Context context)
	{
		mContext = context;
		
//		localADVersion = SystemProperties.get("ro.config.ad.server_xml_url");
		//nativeADVersion = "0x000100209";
		Log.i(LOGTAG, "persist.nativeADVersion=" + new PlatfUtil().getManufactureADVersion());
		SharedPreferences pref = context.getSharedPreferences("dbase", Activity.MODE_PRIVATE);
		nativeADVersion = pref.getString("ad_version", "noValue");
		if("noValue".equals(nativeADVersion)) {
			nativeADVersion = new PlatfUtil().getManufactureADVersion();
			SharedPreferences.Editor editor = pref.edit();
			editor.putString("ad_version", nativeADVersion);
			editor.commit();
		}
		
		
		Log.i(LOGTAG, "nativeADVersion=" + nativeADVersion);
		timer = new Timer();
		mTask = new MyTimerTask();
	}
	
	private class MyTimerTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.i(LOGTAG, "ADUpdateManager timer task run");
			checkServerXml();
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
	
	private void checkServerXml()
	{
		if(null == loadServ) {
			loadServ = new FileLoad();
		}
		dowanloadXML();		
	}
	
	private int dowanloadXML()
	{
		LoadList loList = null;
		LoadList.LoadInfo loInfo = null;
		String xid = "xmlDownloadXid";
		
		String contt = FileUtils.ReadFileToStr(bkpointFile);
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
			loInfo.setTargetFile(xmlDes);
			loInfo.setSupportBP(LoadList.YES);
			loInfo.setLoadType(LoadList.DOWNLOAD);
			loInfo.setUnitLength("5242880"); //5M*1024*1024=5242880
			loInfo.setUrl(xmlUrl);
		}
		
		loadServ.SetCBfun(new FileLoadCBfun());
		loadServ.SetConfigFilePath(bkpointFile);
		loadServ.Request(loInfo);
		
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
	
	private void parserAdXml()
	{
		File file = new File(xmlFilePath + xmlFileName);
		BufferedReader reader;
		Log.i(LOGTAG, "---------------------ad.xml-------------------------");
		try {
			reader = new BufferedReader(new FileReader(file));

			String rd;
			while ((rd = reader.readLine()) != null) {
				Log.i(LOGTAG, "" + rd);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	
	private void checkUpdate(XmlAdUpdateInfo info)
	{
//		PRODUCT_PROPERTY_OVERRIDES += \
//				persist.sys.konka.stbinfo.stbid=0 \
//				persist.sys.konka.stbinfo.sw=0 \
//				persist.sys.konka.stbinfo.hw=0 \
//				persist.sys.konka.stbinfo.sn=0 \
//				persist.sys.konka.stbinfo.mac=0 \
//				persist.sys.konka.stbinfo.model=0 \
//				persist.sys.konka.stbinfo.mfg=0 \
//				persist.sys.konka.stbinfo.rtime=0 \
//				persist.sys.konka.stbinfo.mn=99016756
		nativeManufacture = "KONKA";
		nativeModelId = "HAN873";
		nativeHwVersion = "0x00000101";
		nativeSwVersion = "0x00010206";
		nativeADVersion = "0x00010208";
		nativeSN = "0x00000005";
		
		if(nativeManufacture.equals(info.getManufacture())) {
			if(nativeModelId.equals(info.getModelID())) {
				long swVersionXml = FormatUtils.fromString2Long(info.getSwServerVersion());
				long swVersionNative = FormatUtils.fromString2Long(nativeSwVersion);
				if(swVersionXml > swVersionNative) {
					long curSN = FormatUtils.fromString2Long(nativeSN);
					boolean inside = false;
					for(Scope scope:info.getScopeList()) {
						long startSN = FormatUtils.fromString2Long(scope.getStartSN());
						long endSN = FormatUtils.fromString2Long(scope.getEndSN());
						if(curSN >= startSN && curSN <= endSN) {
							inside = true;
						}
						else {
							Log.i(LOGTAG, "curSN not inside:" + curSN + startSN + endSN);
						}
					}
					if(true == inside) {
						beginDownload(info);
					}
				}
				else {
					Log.i(LOGTAG, "swVersionNative not inside:" + swVersionNative + swVersionXml);
				}
			}
		}
	}
	
	private void beginDownload(XmlAdUpdateInfo info)
	{
		Iterator<AdImageInfo> iterator = info.getAdImgInfoList().iterator();
		while(iterator.hasNext()) {
			XmlAdUpdateInfo.AdImageInfo mAdImgInfo = iterator.next();
			if(true == mAdImgInfo.isUpdate()) {
				downloadImg(mAdImgInfo.getName(), mAdImgInfo.getDownloadPath(), mAdImgInfo.getUrl());
			}
		}
	}
	
	private void downloadImg(String name, String dloadPath, String url)
	{
		LoadList loList = null;
		LoadList.LoadInfo loInfo = null;
		String xid = name;
		String downlaodPath = dloadPath;
		
		String contt = FileUtils.ReadFileToStr(bkpointFile);
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
		loadServ.SetConfigFilePath(bkpointFile);
		loadServ.Request(loInfo);
		
		handleXid(xidHandleCmd.INSERT, xid);
	}
	
	synchronized boolean handleXid(xidHandleCmd cmd, String xid)
	{
		switch(cmd) {
			case INSERT:
				xidHashMap.put(xid, 1);
				break;
			case DELETC:
				xidHashMap.remove(xid);
				break;
			case QUERY:
				xidHashMap.containsKey(xid);
				return true;
			case ISEMPTY:
				return xidHashMap.isEmpty();
			default:
				break;
		}
		
		return true;
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
//			Log.i(LOGTAG, "onSuccess++++" + info.getManufacture() 
//					+ "---" + info.getModelID()
//					+ "---" + info.getHwVersion()
//					+ "---" + info.getSwServerVersion()
//					+ "---" + info.getSwStbVersion()
//					+ "---" + info.getAdServerVersion()
//					+ "---" + info.getAdStbVersion());
			info.printAll();
			xmlInfo = info;
			checkUpdate(xmlInfo);
		}
		
	}
	
	private void verifyImg(XmlAdUpdateInfo xmlInfo)
	{
		Iterator<AdImageInfo> iterator;
		AdImageInfo mXmlInfo;
		boolean veriryOk = false; 
		String img;
		String md5Key;
		String downPath;
		String destPath;
		
		iterator = xmlInfo.getAdImgInfoList().iterator();
		while(iterator.hasNext()) {
			mXmlInfo = iterator.next();
			if( mXmlInfo.isUpdate() ) {
				img = mXmlInfo.getDownloadPath();
				md5Key = mXmlInfo.getMd5Key();
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
			iterator = xmlInfo.getAdImgInfoList().iterator();
			while(iterator.hasNext()) {
				mXmlInfo = iterator.next();
				if( mXmlInfo.isUpdate() ) {
					downPath = mXmlInfo.getDownloadPath();
					destPath = mXmlInfo.getDestPath();
					FileUtils.copyFile(downPath, destPath, true);
				}
			}
			
			veriryOk = false;
			iterator = xmlInfo.getAdImgInfoList().iterator();
			while(iterator.hasNext()) {
				mXmlInfo = iterator.next();
				if( mXmlInfo.isUpdate() ) {
					img = mXmlInfo.getDestPath();
					md5Key = mXmlInfo.getMd5Key();
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
				Log.i(LOGTAG, "WriteToFile" + xmlInfo.getVerifyFile());
				String vf = xmlInfo.getVerifyFile();
				if(null != vf) {
					FileUtils.WriteToFile(vf, "verifyOk=true");
					chmodFile(vf);
				}
			}
		}
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
	
	class FileLoadCBfun extends LoadCBfun {

		@Override
		public void DownloadFailure(LoadInfo info, String step, String result,
				String param) {
			// TODO Auto-generated method stub
			Log.i(LOGTAG, "FileLoadCBfun---DownloadFailure " + result + " " + step + " " + param);
			Log.i(LOGTAG, "URL=" + info.getUrl());
			super.DownloadFailure(info, step, result, param);
		}

		@Override
		public void DownloadResponse(LoadInfo info, String step, String result,
				String param) {
			// TODO Auto-generated method stub
			Log.i(LOGTAG, "FileLoadCBfun---DownloadResponse---" + info.getXID() + "res=" + result);
			if( "Succeed".equals(result) ) {
				if( "xmlDownloadXid".equals(info.getXID()) ) {
					parserAdXml();
				} else {
					if(null != info.getXID()) {
						handleXid(xidHandleCmd.DELETC, info.getXID());
						if(handleXid(xidHandleCmd.ISEMPTY, null)) {
							verifyImg(xmlInfo);
						}
					}
						
				}
				
			}
			super.DownloadResponse(info, step, result, param);
		}

		@Override
		public void DownloadUpdate(LoadInfo info, boolean finish, long curr,
				long total) {
			// TODO Auto-generated method stub
			super.DownloadUpdate(info, finish, curr, total);
		}

		@Override
		public void UploadFailure(LoadInfo info, String result, String param) {
			// TODO Auto-generated method stub
			super.UploadFailure(info, result, param);
		}

		@Override
		public void UploadResponse(LoadInfo info, String result, String param) {
			// TODO Auto-generated method stub
			super.UploadResponse(info, result, param);
		}

		@Override
		public void UploadUpdate(LoadInfo info, boolean finish, long curr,
				long total) {
			// TODO Auto-generated method stub
			super.UploadUpdate(info, finish, curr, total);
		}
		
	}
	
	
}
