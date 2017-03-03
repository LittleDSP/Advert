package com.konka.advert.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.konka.advert.ADLogUtil;

public class XmlParser extends DefaultHandler{
	
	private String LOGTAG = ADLogUtil.LOGTAG;
	private StringBuilder builder;
	private XmlParserListener listener = null;
	private XmlAdUpdateInfo mAdUpdateInfo = null;
	private XmlAdUpdateInfo.Scope mScope;
	private XmlAdUpdateInfo.AdImageInfo mAdImg;
	
    private static final String Manufacture = "Manufacture";
    private static final String ModelID = "Model";
    private static final String HW_Version = "HW_version";
    private static final String SW_server_version = "SW_server_version";
    private static final String SW_stb_version = "SW_stb_version";
    private static final String AD_server_version = "AD_server_version";
    private static final String AD_stb_version = "AD_stb_version";
    private static final String verifyFile = "verifyFile";
    private static final String Period = "Period";
    private static final String Control = "Control";
    private static final String scope = "scope";
    private static final String index = "index";
    private static final String adImg = "AdImage";
	
	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
        builder = new StringBuilder();
        mAdUpdateInfo = new XmlAdUpdateInfo();
        Log.i(LOGTAG, "startDocument");
		if(null != listener) {
			listener.onStart();
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		super.endElement(uri, localName, qName);
		
		 if(Manufacture.equals(qName)) {
			 mAdUpdateInfo.Manufacture = builder.toString();
		 } else if(ModelID.equals(qName)) {
			 mAdUpdateInfo.ModelID = builder.toString();			 
		 } else if(HW_Version.equals(qName)) {
			 mAdUpdateInfo.HW_version = builder.toString();			 
		 } else if(SW_server_version.equals(qName)) {
			 mAdUpdateInfo.SW_server_version = builder.toString();			 
		 } else if(SW_stb_version.equals(qName)) {
			 mAdUpdateInfo.SW_stb_version = builder.toString();			 
		 } else if(AD_server_version.equals(qName)) {
			 mAdUpdateInfo.AD_server_version = builder.toString();			 
		 } else if(AD_stb_version.equals(qName)) {
			 mAdUpdateInfo.AD_stb_version = builder.toString();			 
		 } else if(scope.equals(qName)) {
			 mAdUpdateInfo.scopeList.add(mScope);			 
		 } else if("name".equals(qName) && mAdImg != null) {
			 mAdImg.name = builder.toString();
		 } else if("update".equals(qName) && mAdImg != null) {
			 mAdImg.update = builder.toString();
		 } else if("url".equals(qName) && mAdImg != null) {
			 mAdImg.url = builder.toString();
		 } else if("md5_key".equals(qName) && mAdImg != null) {
			 mAdImg.md5Key = builder.toString();
		 } else if("download_path".equals(qName) && mAdImg != null) {
			 mAdImg.downloadPath = builder.toString();
		 } else if("dest_path".equals(qName) && mAdImg != null) {
			 mAdImg.destPath = builder.toString();
		 } else if(adImg.equals(qName)) {
			 mAdUpdateInfo.adImgList.add(mAdImg);
		 } else if(verifyFile.equals(qName)) {
			 mAdUpdateInfo.verifyFile = builder.toString();
		 }
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		super.startElement(uri, localName, qName, attributes);
		if(scope.equals(qName)) {
			mScope = mAdUpdateInfo.new Scope();
			mScope.startSN = attributes.getValue("start");
			mScope.endSN = attributes.getValue("end");
		} else if(adImg.equals(qName)) {
			mAdImg = mAdUpdateInfo.new AdImageInfo();
		}
		builder.setLength(0);
	}
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		super.characters(ch, start, length);
		builder.append(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
		if(null != listener) {
			listener.onSuccess(mAdUpdateInfo);
		}
	}

    public void setXmlParserListener(XmlParserListener listener) {
        this.listener = listener;
    }
}
