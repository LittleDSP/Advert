package com.konka.advert.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.konka.advert.utils.ADLogUtil;

public class XmlAdUpdateInfo {
	private String LOGTAG = ADLogUtil.LOGTAG;
	
	protected String Manufacture;
    protected String ModelID;
    protected String HW_version;
    protected String SW_server_version;
    protected String SW_stb_version;
    protected String AD_server_version;
    protected String AD_stb_version;
    protected String verifyFile;
	
//    protected List<String> urlList;
    protected List<Scope> scopeList;
    protected List<AdImageInfo> adImgList;
    
    public XmlAdUpdateInfo()
    {
//    	urlList = new ArrayList<String>();
    	scopeList =new ArrayList<XmlAdUpdateInfo.Scope>();
    	adImgList =new ArrayList<XmlAdUpdateInfo.AdImageInfo>();
    }
    
    public String getManufacture()
    {
    	return Manufacture;
    }
    
    public String getModelID()
    {
    	return ModelID;
    }

    public String getHwVersion()
    {
    	return HW_version;
    }
    
    public String getSwServerVersion()
    {
    	return SW_server_version;
    }
    
    public String getSwStbVersion()
    {
    	return SW_stb_version;
    }
    
    public String getAdServerVersion()
    {
    	return AD_server_version;
    }
    
    public String getAdStbVersion()
    {
    	return AD_stb_version;
    }
    
    public String getVerifyFile()
    {
    	return verifyFile;
    }
    
    public List<Scope> getScopeList()
    {
    	return scopeList;
    }
    
    public List<AdImageInfo> getAdImgInfoList()
    {
    	return adImgList;
    }
    
    public void printAll()
    {
    	Log.i(LOGTAG, "--------------------------------------------------");
    	Log.i(LOGTAG, "    " + Manufacture);
    	Log.i(LOGTAG, "    " + ModelID);
    	Log.i(LOGTAG, "    " + HW_version);
    	Log.i(LOGTAG, "    " + SW_server_version);
    	Log.i(LOGTAG, "    " + SW_stb_version);
    	Log.i(LOGTAG, "    " + AD_server_version);
    	Log.i(LOGTAG, "    " + AD_stb_version);
    	Log.i(LOGTAG, "    " + verifyFile);
    	
    	Log.i(LOGTAG, "------scope------");
    	Iterator<Scope> iterator = scopeList.iterator();
    	while(iterator.hasNext()) {
    		Scope mscope = iterator.next();
    		Log.i(LOGTAG, "    " + mscope.startSN);
        	Log.i(LOGTAG, "    " + mscope.startSN);
    	}
    	
    	Log.i(LOGTAG, "------ADImage------");    	
    	Iterator<AdImageInfo> iteratorAd = adImgList.iterator();
    	while(iteratorAd.hasNext()) {
    		AdImageInfo mAdimg = iteratorAd.next();
    		Log.i(LOGTAG, "    " + mAdimg.name);
        	Log.i(LOGTAG, "    " + mAdimg.update);
        	Log.i(LOGTAG, "    " + mAdimg.url);
        	Log.i(LOGTAG, "    " + mAdimg.md5Key);
        	Log.i(LOGTAG, "    " + mAdimg.downloadPath);
        	Log.i(LOGTAG, "    " + mAdimg.destPath);
    	}
    	
    	Log.i(LOGTAG, "--------------------------------------------------");
    }
    
    public class Scope {
    	 protected String startSN;
         protected String endSN;
         protected List<String> index;
         
         protected Scope() 
         {
             index = new ArrayList<String>();
         }
         
         public String getStartSN()
         {
        	 return startSN;
         }
         
         public String getEndSN()
         {
        	 return endSN;
         }
    }
    
    public class AdImageInfo {
    	protected String name;
    	protected String update;
    	protected String file;
    	protected String url;
    	protected String md5Key;
    	protected String downloadPath;
    	protected String destPath = "/data/advert/";
    	
    	protected AdImageInfo()
    	{
    		
    	}
    	
    	public String getName()
    	{
    		return name;
    	}
    	
    	public boolean isUpdate()
    	{
    		if("true".equals(update)) {
    			return true;
    		} else {
    			return false;
    		}
    	}
    	
    	public String getFile()
    	{
    		return file;
    	}
    	
    	public String getUrl()
    	{
    		return url;
    	}
    	
    	public String getMd5Key()
    	{
    		return md5Key;
    	}
    	
    	public String getDownloadPath()
    	{
    		return downloadPath;
    	}
    	
    	public String getDestPath()
    	{
    		return destPath;
    	}
    }
}
