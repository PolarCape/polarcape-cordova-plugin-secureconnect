/*********************************************************************
 * LGI CONFIDENTIAL                                                 *
 * Copyright 2013 LGI                              					*
 * 
 * Base class for maintaining global application state.
 *********************************************************************/

package com.polarcape.secureconnect;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;

import com.polarcape.secureconnect.certintaller.Certificate;
import com.polarcape.secureconnect.wifiprofileconfiguration.WiFiProfile;

public class WiFiConfiguratorApplication extends Application{

	private static Application sApp = null;
	private static Context context = null;
	private static ArrayList<Certificate> sCertList = new ArrayList<Certificate>();
	private static ArrayList<WiFiProfile> sProfileList = new ArrayList<WiFiProfile>();
	private static String certificateInstallName = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		sApp = this;
	}
	
	/** Return the Android application`s instance 
	 *
	 * @return Application
	 */
	public static Application getApplication(){
		return sApp;
	}
	
	 /** 
     * This sets a reference to the currently displayed activity.
     * @param currentActivity Currently displayed activity  
     */
	
	
	 /** 
     * This API sets the list of certificates to be installed.
     * Used by {@link com.polarcape.secureconnect.certintaller.libertyglobal.secureconnect.certinstaller.CertificateHelper#generateCertList(Context context)} 
     *  
     * @param parsedCertificateList The list of certificates parsed from assets folder 
     * by the CertificateHelper class. 
     * 
     */
	public static void setCertList(ArrayList<Certificate> parsedCertificateList){
		sCertList = parsedCertificateList;
	}
	
	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		WiFiConfiguratorApplication.context = context;
	}

	/** Return the list of certificates to be installed.
	 * Used by classes implementing 
	 * {@link com.libertyglobal.secureconnect.wifiprofileconfiguration.AbstractWiFiProfileBuilder#configureProfile()} 
	 * 
	 * @return Arraylist of {@link com.libertyglobal.secureconnect.certinstaller.Certificate}
	 */
	public static ArrayList<Certificate> getCertList(){
		return sCertList;
	}
	
	/** This API is called 
	 * 1. To set the list of Wi-Fi profiles that need to be configured by reading the assets/configuration.xml file by  
	 * {@link com.libertyglobal.secureconnect.util.WiFiConfiguratorUtil#readProfileConfigurations(Context)}
	 * <BR>
	 * 2. To read the list of Wi-Fi profiles and create subsequent WifiConfiguration objects to be set in the device by
	 * classes implementing {@link com.libertyglobal.secureconnect.wifiprofileconfiguration.AbstractWiFiProfileBuilder#configureProfile()}
	 * 
	 * @return ArrayList of {@link com.libertyglobal.secureconnect.wifiprofileconfiguration.WiFiProfile} 
	 * objects.
	 */
	public static ArrayList<WiFiProfile> getWiFiProfileList(){
		return sProfileList;
	}

	public static void setCertificateInstallName(String installName){		
		certificateInstallName = installName;
	}
	
	public static String getCertificateInstallName(){		
		return certificateInstallName;
	}
}
