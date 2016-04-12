/************************************************************************************************
 * LGI CONFIDENTIAL                                                 							*
 * Copyright 2013 LGI                              												*
 * 																								*
 * This class is an implementation of 															*
 * {@link com.libertyglobal.secureconnect.wifiprofileconfiguration.AbstractWiFiProfileBuilder}	*
 * for creating and configuring Wi-Fi profiles on devices running Android OS version between	* 
 *  Froyo(2.2.x)	and Ice-Cream Sandwich(4.0.x) 												*
 ************************************************************************************************/

package com.polarcape.secureconnect.wifiprofileconfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;

import android.content.Context;
import android.net.wifi.WifiConfiguration;

import com.polarcape.secureconnect.WiFiConfiguratorApplication;
import com.polarcape.secureconnect.certintaller.Certificate;
import com.polarcape.secureconnect.certintaller.CertificateHelper;
import com.polarcape.secureconnect.util.LogUtil;

public class WiFiProfileBuilder_APILevel17 extends AbstractWiFiProfileBuilder{
	
	private static final String TAG = "SecureConnect->WiFiProfileBuilder_APILevel17";
	private static final String AUTH_PREFIX = "auth=";
	
	public WiFiProfileBuilder_APILevel17(Context context){
		super(context);
	}

	@Override
	public boolean configureProfile(Certificate certificate) {
		// For future use if every profile needs different certificates
		return false;
	}

	/**
	 * Android OS version between Ice-Cream Sandwich(4.0.x) and Froyo(2.2.x) specific implementation of 
	 * {@link com.libertyglobal.secureconnect.wifiprofileconfiguration.AbstractWiFiProfileBuilder#configureProfile()} 
	 * 
	 *  @return boolean indicating success/failure in the profile configuration process
	 */
	@Override
	public boolean configureProfile(String username, String password) {
		
		boolean isProfileConfigured = false;
		
		ArrayList<Certificate> certList = WiFiConfiguratorApplication.getCertList();
		ArrayList<WiFiProfile> profileList = WiFiConfiguratorApplication.getWiFiProfileList();
		mProfileList.clear();
		int count = 0;
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
			
			
	        // Enterprise Settings
	        // Need access to non-public APIs via Java Reflection
	        try {
	            Class<?> [] WifiConfigurationHiddenClasses = WifiConfiguration.class.getClasses();
	            Class<?> EnterpriseFieldClass = null;
	            for (Class<?> wcClass : WifiConfigurationHiddenClasses){
	            	if (wcClass.getName().equals(ENTERPRISEFIELD_CLASSNAME)) {
	                	EnterpriseFieldClass = wcClass;
	                    break;
	                }
	            }
	           
	            Field EAPType = null;
	            Field Phase2AuthenticationType = null;
	            Field CACert = null;
	            Field ClientCert = null;
	            Field Identity = null;
	            Field AnonymousIdentity = null;
	            Field Password = null;
	            Field PrivateKey = null;
	            Field PrivateKeyNew = null;

	            if(EnterpriseFieldClass != null){
	            	Field[] EnterpriseFields = WifiConfiguration.class.getFields();
	            	if(EnterpriseFields != null && EnterpriseFields.length >0){
	            		for (Field wcefField : EnterpriseFields){
	                		if (wcefField.getName().equals(Keys.EAP_KEY))
	                			EAPType = wcefField;
	                		else if (wcefField.getName().equals(Keys.PHASE2_KEY))
	                			Phase2AuthenticationType = wcefField;
	                		else if (wcefField.getName().equals(Keys.CA_CERT_KEY))
	                			CACert = wcefField;
	                        else if (wcefField.getName().equals(Keys.CLIENT_CERT_KEY))
	                        	ClientCert = wcefField;
	                        else if (wcefField.getName().equals(Keys.IDENTITY_KEY))
	                        	Identity = wcefField;
	                        else if (wcefField.getName().equals(Keys.ANON_IDENTITY_KEY))
	                			AnonymousIdentity = wcefField;
	                        else if (wcefField.getName().equals(Keys.PASSWORD_KEY))
	                        	Password = wcefField;
	                        else if (wcefField.getName().equals(Keys.OLD_PRIVATE_KEY_NAME))
	                        	PrivateKey = wcefField; 
	                        else if (wcefField.getName().equals(Keys.PRIVATE_KEY_ID_KEY))
	                        	PrivateKeyNew = wcefField;   
	                	}
	                	
	                	//getting the method setValue() from WiFiConfiguration.EnterpriseField class
	                	Method EnterpriseFieldMethodSetValue = null;
	                    for(Method m: EnterpriseFieldClass.getMethods())
	                        if(m.getName().trim().equals("setValue"))
	                        	EnterpriseFieldMethodSetValue = m;      		
	                    
	                    if(EnterpriseFieldMethodSetValue != null){
	                    	 
	                    	
	                    	/*EAP Method*/
	                    	if(EAPType != null){
	                    		//EnterpriseFieldMethodSetValue.invoke(EAPType.get(configuration),SecuritySettings.EAP_PROTOCOL_TYPE_PEAP);
	                    		// NONE,PWD,PEAP,TLS,TTLS
	                    		if(profile.getEAPMethod().equalsIgnoreCase("PEAP"))
	                    			EnterpriseFieldMethodSetValue.invoke(EAPType.get(configuration),SecuritySettings.EAP_PROTOCOL_TYPE_PEAP);
	                    		else if(profile.getEAPMethod().equalsIgnoreCase("TLS"))
	                    			EnterpriseFieldMethodSetValue.invoke(EAPType.get(configuration),SecuritySettings.EAP_PROTOCOL_TYPE_TLS);
	                    		else if(profile.getEAPMethod().equalsIgnoreCase("TTLS"))
	                    			EnterpriseFieldMethodSetValue.invoke(EAPType.get(configuration),SecuritySettings.EAP_PROTOCOL_TYPE_TTLS);
	                    	}
		
	                    	/*EAP Phase 2 Authentication*/
	                    	if(Phase2AuthenticationType != null) {
	                    		//EnterpriseFieldMethodSetValue.invoke(Phase2AuthenticationType.get(configuration),
	                        		//SecuritySettings.EAP_PHASE2_AUTHENTICATION_TYPE);
	                    	// NONE,PAP,MSCHAP,MSCHAPV2,GTC
	                    		if(profile.getPhase2Auth().equalsIgnoreCase("GTC"))
	                    			EnterpriseFieldMethodSetValue.invoke(Phase2AuthenticationType.get(configuration),AUTH_PREFIX+SecuritySettings.EAP_PHASE2_AUTHENTICATION_TYPE_GTC);
	                    		else if(profile.getPhase2Auth().equalsIgnoreCase("MSCHAP"))
	                    			EnterpriseFieldMethodSetValue.invoke(Phase2AuthenticationType.get(configuration),AUTH_PREFIX+SecuritySettings.EAP_PHASE2_AUTHENTICATION_TYPE_MSCHAP);
	                    		else if(profile.getPhase2Auth().equalsIgnoreCase("MSCHAPV2"))
	                    			EnterpriseFieldMethodSetValue.invoke(Phase2AuthenticationType.get(configuration),AUTH_PREFIX+SecuritySettings.EAP_PHASE2_AUTHENTICATION_TYPE_MSCHAPV2);
	                    		else if(profile.getPhase2Auth().equalsIgnoreCase("PAP"))
	                    			EnterpriseFieldMethodSetValue.invoke(Phase2AuthenticationType.get(configuration),AUTH_PREFIX+SecuritySettings.EAP_PHASE2_AUTHENTICATION_TYPE_PAP);
	                    		else 
	                    			EnterpriseFieldMethodSetValue.invoke(Phase2AuthenticationType.get(configuration),AUTH_PREFIX+SecuritySettings.EAP_PHASE2_AUTHENTICATION_TYPE_NONE);	                    	
	                    	}
	                    	
	                    		if(android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.GINGERBREAD_MR1){
		                    		/*EAP CA Certificate*/
			                    	String profileCACertificateName = profile.getCACert();
			                    	if(profileCACertificateName != null && profileCACertificateName.length() > 0){
			                    		 for(Certificate c : certList){
			                    			 if(profileCACertificateName.equalsIgnoreCase(c.mFileName) && c.mCertType_CACert
			                    					 && c.mCertificate_java_x509.getSerialNumber().equals(new BigInteger(profile.getLinking_Cert_SerialNo()))){
			                    				 if(CACert != null){
			                    					 EnterpriseFieldMethodSetValue.invoke(
			                    							 CACert.get(configuration), CertificateHelper.KEYSTORE_URI + CertificateHelper.CA_CERTIFICATE + c.mInstallName+"CA");
			                    					 break;
			                    				 }
			                    			 }
			                    		 }
			                    	}
			                    	
			                    	/*EAP Client certificate*/	
			                    	String profileCLientCertificateName = profile.getClientCert();
			                    	if(profileCLientCertificateName != null && profileCLientCertificateName.length() > 0){
			                    		 for(Certificate c : certList){
			                    			 if(profileCLientCertificateName.equalsIgnoreCase(c.mFileName)
			                    					 && c.mCertType_UserCert && c.mPrivateKeyEntry != null){
			                    				 if(ClientCert != null){
			                    					 EnterpriseFieldMethodSetValue.invoke(
			                    							 ClientCert.get(configuration),CertificateHelper.KEYSTORE_URI + CertificateHelper.USER_CERTIFICATE + c.mInstallName+"USER");
			                    				 }
			                    				 if(PrivateKeyNew != null){
			     	        						//for JB and JB_MR1 client private key is represented by field named "key_id" instead of "private_key" 
			     	        						//in old configurations.Pease see WifiEnterpriseConfig.java in AOSP code
			     	        						EnterpriseFieldMethodSetValue.invoke(
			     	        								PrivateKeyNew.get(configuration),CertificateHelper.USER_PRIVATE_KEY + c.mInstallName+"USER");
			     	        					}
			     	        					/*EAP Private key*/
			     	        					// This is in else-if block because in some devices(like Samsung Galaxy SIII mini) both the filelds PrivateKeyNew and PrivateKey are found
			     	        					// and we need to set ONLY PrivateKeyNew for these devices to link the certificate.
			     	        					else if(PrivateKey != null){
			     	        						EnterpriseFieldMethodSetValue.invoke(
			     	        								PrivateKey.get(configuration), CertificateHelper.KEYSTORE_URI + CertificateHelper.USER_PRIVATE_KEY + c.mInstallName+"USER");	
			     	        					}
			                    				break;	
			                    			 }
			                    		 }
			                    	}
		                    	}else{
		                    		/*EAP CA Certificate*/
			                    	String profileCACertificateName = profile.getCACert();
			                    	if(profileCACertificateName != null && profileCACertificateName.length() > 0){
			                    		 for(Certificate c : certList){
			                    			 if(profileCACertificateName.equalsIgnoreCase(c.mFileName)){
			                    				 if(CACert != null && c.mCertificate_java_x509.getSerialNumber().equals(new BigInteger(profile.getLinking_Cert_SerialNo()))){
			                    					 EnterpriseFieldMethodSetValue.invoke(
			                    							 CACert.get(configuration), CertificateHelper.KEYSTORE_URI + CertificateHelper.CA_CERTIFICATE + c.mInstallName);
			                    					 break;
			                    				 }
			                    			 }
			                    		 }
			                    	}
			                    	
			                    	/*EAP Client certificate - currently unused*/	
			                    	String profileCLientCertificateName = profile.getClientCert();
			                    	if(profileCLientCertificateName != null && profileCLientCertificateName.length() > 0){
			                    		 for(Certificate c : certList){
			                    			 if(profileCLientCertificateName.equalsIgnoreCase(c.mFileName)){
			                    				 if(ClientCert != null){
			                    					 EnterpriseFieldMethodSetValue.invoke(
			                    							 ClientCert.get(configuration),CertificateHelper.KEYSTORE_URI + CertificateHelper.USER_CERTIFICATE + c.mInstallName);
			                    				 }
			                    				 if(PrivateKeyNew != null){
			     	        						//for JB and JB_MR1 client private key is represented by field named "key_id" instead of "private_key" 
			     	        						//in old configurations.Pease see WifiEnterpriseConfig.java in AOSP code
			     	        						EnterpriseFieldMethodSetValue.invoke(
			     	        								PrivateKeyNew.get(configuration),CertificateHelper.USER_PRIVATE_KEY + c.mInstallName);
			     	        					}
			     	        					/*EAP Private key*/
			     	        					// This is in else-if block because in some devices(like Samsung Galaxy SIII mini) both the filelds PrivateKeyNew and PrivateKey are found
			     	        					// and we need to set ONLY PrivateKeyNew for these devices to link the certificate.
			     	        					else if(PrivateKey != null){
			     	        						EnterpriseFieldMethodSetValue.invoke(
			     	        								PrivateKey.get(configuration), CertificateHelper.KEYSTORE_URI + CertificateHelper.USER_PRIVATE_KEY + c.mInstallName);	
			     	        					}
			                    				break;	
			                    			 }
			                    		 }
			                    	}
		                    	}	                
	                    	
	                       /*EAP Identity*/
	                        if(Identity != null)
	                        	EnterpriseFieldMethodSetValue.invoke(Identity.get(configuration),profile.getUsername());
	                        /*EAP Anonymous Identity*/
	                        if(AnonymousIdentity != null)
	                        	//EnterpriseFieldMethodSetValue.invoke(AnonymousIdentity.get(configuration),"");
	                        /*EAP Password*/
	                        if(Password != null)
	                        	EnterpriseFieldMethodSetValue.invoke(Password.get(configuration),profile.getPassword());
	                        
	                        	isProfileConfigured = true;
	            	}else{
	                	LogUtil.e(TAG + "ERROR 1: configureProfile() -> This device is currently not supported.Set Method Not found.");
	                	isProfileConfigured = false;
	                }
	                }else{
	                	LogUtil.e(TAG + "ERROR 2: configureProfile() -> This device is currently not supported.EnterpriseFieldClass Fields Not found.");
	                	isProfileConfigured = false;
	                }
	            }else{
	            	LogUtil.e(TAG + "ERROR 3: configureProfile() -> This device is currently not supported.EnterpriseFieldClass Not found.");
	            	isProfileConfigured = false;
	            }
	        }catch(Exception e){
	        	LogUtil.e(TAG+"ERROR 4: configureProfile() -> "+e.toString());
	        	isProfileConfigured = false;
	        }
			
	        mProfileList.add(configuration);
	        count = count + 1;
		}
        return isProfileConfigured;
	}
}
