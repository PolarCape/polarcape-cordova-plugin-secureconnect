/********************************************************************
 * LGI CONFIDENTIAL                                                 *
 * Copyright 2013 LGI                              					*
 * 																	*
 * This class exposes a logging API which prints some device 		*
 * information along with the error message to identify the device	*
 * where the issue is occurring. 									*
 ********************************************************************/

package com.polarcape.secureconnect.util;

import android.util.Log;

public class LogUtil {
	
	static final String TAG = "SecureConnect->SecureConnect_APP";
	private static String mDeviceInfo;
	
	private LogUtil(){}
	
	/**
	 * This API concats the error message with a device info string generated 
	 * from {@link #generateDeviceInfo()} and prints it to device`s adb logcat
	 * 
	 * @param msg Error message that needs to be printed in the adb logcat.
	 */
	public static void e(String msg) {		
		if(mDeviceInfo == null)
			generateDeviceInfo();		
		Log.e(TAG,msg + mDeviceInfo);
    }

	/**
	 * API to generate a String containing essential 
	 * device information which will help us to identify the 
	 * target device from logs,when some unforeseen issue occurs.
	 */
	private static void generateDeviceInfo() {
		String RELEASE = android.os.Build.VERSION.RELEASE;
		int SDK_INT = android.os.Build.VERSION.SDK_INT;
        String MODEL = android.os.Build.MODEL; 
        String BRAND = android.os.Build.BRAND; 
        String MANUFACTURER = android.os.Build.MANUFACTURER; 
		
        StringBuilder sb = new StringBuilder();
        sb.append(" Device Info : "+
        		"RELEASE : "+ RELEASE+ " " + 
        		"SDK_INT : "+ SDK_INT+ " " + 
        		"MODEL : "+ MODEL+ " " + 
        		"BRAND : "+ BRAND+ " " + 
        		"MANUFACTURER : "+ MANUFACTURER+ " " );
        mDeviceInfo = sb.toString();	
	}
	
	
}
