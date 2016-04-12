/********************************************************************************************
 * LGI CONFIDENTIAL                                                 						*
 * Copyright 2013 LGI                              											*
 * 																							*
 * Singleton Factory class which provides objects representing Android OS version specific	* 
 * implementation of 																		*
 * @link com.lgi.wificonfigurationtool.wifiprofileconfiguration.AbstractWiFiProfileBuilder}	*
 ********************************************************************************************/

package com.polarcape.secureconnect.wifiprofileconfiguration;

import android.content.Context;

public class WiFiProfileBuilderFactory {
	
	private static WiFiProfileBuilderFactory mWiFiProfileConfiguratorFactory;
	private static Context mContext;
	
	private WiFiProfileBuilderFactory() {}
	
	/**
	 * API to get instance of WiFiProfileConfiguratorFactory singleton class
	 * 
	 * @param context
	 * @return WiFiProfileConfiguratorFactory instance
	 */	
	public static synchronized WiFiProfileBuilderFactory getInstance(Context context){
		if(mWiFiProfileConfiguratorFactory == null 
				|| (mContext.hashCode()!= context.hashCode())){
			mWiFiProfileConfiguratorFactory = new WiFiProfileBuilderFactory();
			mContext = context;
		}
		return mWiFiProfileConfiguratorFactory;
	}
	
	/**
	 * API to get object representing Android OS version specific concrete
	 * implementation of {@link com.libertyglobal.secureconnect.wifiprofileconfiguration.AbstractWiFiProfileBuilder}
	 * 
	 * @param AOSVersion Device`s Android OS Version
	 * @return AbstractWiFiProfileBuilder reference representing Android OS version specific concrete
	 * implementations of {@link com.libertyglobal.secureconnect.wifiprofileconfiguration.AbstractWiFiProfileBuilder}  
	 */
	public AbstractWiFiProfileBuilder getConfigurator(int AOSVersion) {
		AbstractWiFiProfileBuilder configurator = null;

		if(AOSVersion < android.os.Build.VERSION_CODES.ECLAIR){
			//using reflection
			configurator = new WiFiProfileBuilder_APILevel04(mContext);
			
		}else if(AOSVersion >= android.os.Build.VERSION_CODES.ECLAIR
				&& AOSVersion < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){			
			//using reflection
			configurator = new WiFiProfileBuilder_APILevel17(mContext);	
			
		}else if(AOSVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){			
			//using standard Android API
			configurator = new WiFiProfileBuilder_APILevel18(mContext);			
		}
		return configurator;		
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new CloneNotSupportedException();
	}
	
}
