package com.konka.advert.utils;

import android.util.Log;

public class FormatUtils {
    public static long fromString2Long(String number) throws NumberFormatException {
        long fLong;
        if(null==number) {
        	Log.e(ADLogUtil.LOGTAG, "number is null !!!");
        	return -1;
        }
        number = number.replace(".", "");
        if(number.startsWith("0x") || number.startsWith("0X")) {
            fLong = Long.valueOf(number.substring(2),16);
        } else {
            fLong = Long.valueOf(number,16);
        }
        return fLong;
    }
    
    public static int fromString2Int(String number) throws NumberFormatException {
        int fInt;
        if(number.startsWith("0x") || number.startsWith("0X")) {
            fInt = Integer.valueOf(number.substring(2),16);
        } else {
            fInt = Integer.valueOf(number,16);
        }
        return fInt;
    }
}
