package com.konka.advert.xml;

import org.xml.sax.SAXParseException;

public interface XmlParserListener {
    void onError(SAXParseException e);
    void onStart();
    void onSuccess(XmlAdUpdateInfo info);
}
