/************************************************************************************************
 * LGI CONFIDENTIAL                                                 							*
 * Copyright 2013 LGI                              												*
 * 																								*
 * This class is an implementation of 															*
 * {@link com.libertyglobal.secureconnect.wifiprofileconfiguration.AbstractWiFiProfileBuilder}	*
 * for creating and configuring Wi-Fi profiles on devices running Android OS version 			*
 * JellyBean(4.1.x) & above.																	*
 ************************************************************************************************/
package com.polarcape.secureconnect.wifiprofileconfiguration;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;

import com.polarcape.secureconnect.WiFiConfiguratorApplication;
import com.polarcape.secureconnect.certintaller.Certificate;

public class WiFiProfileBuilder_APILevel18 extends AbstractWiFiProfileBuilder{

	public WiFiProfileBuilder_APILevel18(Context context){
		super(context);
	}
	
	@Override
	public boolean configureProfile(Certificate certificate) {
		// For future use if every profile needs different certificates
		return false;
	}

	/**
	 * AOS Version JellyBean(4.1.x) and above specific implementation of 
	 * {@link com.libertyglobal.secureconnect.wifiprofileconfiguration.AbstractWiFiProfileBuilder#configureProfile()} 
	 * 
	 *  @return boolean indicating success/failure in the profile configuration process
	 */
	@SuppressLint("NewApi") @Override
	public boolean configureProfile(String username, String password) {
		boolean isProfileConfigured = false;
		ArrayList<Certificate> certList = WiFiConfiguratorApplication.getCertList();
		ArrayList<WiFiProfile> profileList = WiFiConfiguratorApplication.getWiFiProfileList();
		mProfileList.clear();
		for(WiFiProfile profile : profileList){
			
			profile.setUsername(username);
			profile.setPassword(password);
			
			WifiConfiguration configuration = new WifiConfiguration();
			configuration.SSID = profile.getSSIDName();
			configuration.hiddenSSID = false;
			

			configuration.allowedKeyManagement.clear();
			
			if(profile.getSecurityType().equalsIgnoreCase("IEEE8021X")){
				configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
				// this is being set to allow automatic connection
				configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
			}
			else if(profile.getSecurityType().equalsIgnoreCase("WPA_EAP"))
				configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
			else if(profile.getSecurityType().equalsIgnoreCase("WPA_PSK"))
				configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			else
				configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			
			

			// NONE,PWD,PEAP,TLS,TTLS
			if(profile.getEAPMethod().equalsIgnoreCase("PEAP"))
				configuration.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
			else if(profile.getEAPMethod().equalsIgnoreCase("PWD"))
				configuration.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PWD);
			else if(profile.getEAPMethod().equalsIgnoreCase("TLS"))
				configuration.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
			else if(profile.getEAPMethod().equalsIgnoreCase("TTLS"))
				configuration.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TTLS);
			else
				configuration.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.NONE);

			
			
			// NONE,PAP,MSCHAP,MSCHAPV2,GTC
			if(profile.getPhase2Auth().equalsIgnoreCase("GTC"))
				configuration.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.GTC);
			else if(profile.getPhase2Auth().equalsIgnoreCase("MSCHAP"))
				configuration.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAP);
			else if(profile.getPhase2Auth().equalsIgnoreCase("MSCHAPV2"))
				configuration.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
			else if(profile.getPhase2Auth().equalsIgnoreCase("PAP"))
				configuration.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.PAP);
			else 
				configuration.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.NONE);
			
        		/*EAP CA Certificate*/
            	String profileCACertificateName = profile.getCACert();
            	
            	//get the CA Certificate instance
            	X509Certificate CACertificate = null;
            	 for(Certificate c : certList){
            		 if(profileCACertificateName.equalsIgnoreCase(c.mFileName)
        					 && c.mCertType_CACert  
        					 && c.mCertificate_java_x509.getSerialNumber().equals(
        							 new BigInteger(profile.getLinking_Cert_SerialNo()))){
            			 CACertificate = c.mCertificate_java_x509;
            		 }
            	 }
            	
            	if(profileCACertificateName != null && profileCACertificateName.length() > 0){           		
            		 configuration.enterpriseConfig.setCaCertificate(CACertificate);
            	}
            	
            	/*EAP Client certificate*/	
            	String profileCLientCertificateName = profile.getClientCert();
            	if(profileCLientCertificateName != null && profileCLientCertificateName.length() > 0){
            		 for(Certificate c : certList){
            			 if(profileCLientCertificateName.equalsIgnoreCase(c.mFileName)
            					 && c.mCertType_UserCert && c.mPrivateKeyEntry != null){	 
            				 configuration.enterpriseConfig.setClientKeyEntry(
            						 c.mPrivateKeyEntry.getPrivateKey(),c.mCertificate_java_x509);
            				break;	
            			 }
            		 }
            	}
			
			// Identity
        	configuration.enterpriseConfig.setIdentity(profile.getUsername());

			// Anonymous Identity
        	//configuration.enterpriseConfig.setAnonymousIdentity("");

			// Password
        	configuration.enterpriseConfig.setPassword(profile.getPassword());
			
			mProfileList.add(configuration);	
			
		}

		isProfileConfigured = true;

		return isProfileConfigured;
	}
}