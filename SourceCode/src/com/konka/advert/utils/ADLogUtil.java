package com.konka.advert.utils;

public class ADLogUtil {
	public static final String LOGTAG = "KKADVERT";
	private static int index = 3;
	
	public static String getMethodName(){
		String methodName = Thread.currentThread().getStackTrace()[index].getMethodName();
		return methodName;
	}
	
	public static String getClassName(){
		String className = Thread.currentThread().getStackTrace()[index].getClassName();
		return className;
	}
	
	public static int getLineNum(){
		int lineNum = Thread.currentThread().getStackTrace()[index].getLineNumber();
		return lineNum;
	}
	
	public static String getDebugInfo(){
		String methodName = Thread.currentThread().getStackTrace()[index].getMethodName();
		String className = Thread.currentThread().getStackTrace()[index].getClassName();
		int lineNum = Thread.currentThread().getStackTrace()[index].getLineNumber();
		String debugInfo = className + "." + methodName + "." + lineNum;
		return debugInfo;
	}
}
