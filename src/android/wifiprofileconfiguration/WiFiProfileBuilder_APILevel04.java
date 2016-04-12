/************************************************************************************************
 * LGI CONFIDENTIAL                                                 							*
 * Copyright 2013 LGI                              												*
 * 																								*
 * This class is an implementation of 															*
 * {@link com.libertyglobal.secureconnect.wifiprofileconfiguration.AbstractWiFiProfileBuilder}	*
 * for creating and configuring Wi-Fi profiles on devices running Android OS version 			* 
 * Eclair(2.1.x) and below.																		*
 ************************************************************************************************/

package com.polarcape.secureconnect.wifiprofileconfiguration;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;

import android.content.Context;
import android.net.wifi.WifiConfiguration;

import com.polarcape.secureconnect.WiFiConfiguratorApplication;
import com.polarcape.secureconnect.certintaller.Certificate;
import com.polarcape.secureconnect.certintaller.CertificateHelper;
import com.polarcape.secureconnect.util.LogUtil;

public class WiFiProfileBuilder_APILevel04 extends AbstractWiFiProfileBuilder{
	
	private static final String TAG = "SecureConnect->WiFiProfileBuilder_APILevel04";
	private static final String AUTH_PREFIX = "auth=";
	
	public WiFiProfileBuilder_APILevel04(Context context){
		super(context);
	}

	@Override
	public boolean configureProfile(Certificate certificate) {
		// For future use if every profile needs different certificates
		return false;
	}

	/**
	 * Android OS version Eclair(2.1.x) and below specific implementation of 
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
	            Class<?>[] WifiConfigurationHiddenClasses = WifiConfiguration.class.getClasses();
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
	                	}
	                    
	                    /*EAP Method*/
	            		if(EAPType != null){
	            			//EAPType.set(configuration,SecuritySettings.EAP_PROTOCOL_TYPE_PEAP);
	            			// NONE,PWD,PEAP,TLS,TTLS
                    		if(profile.getEAPMethod().equalsIgnoreCase("PEAP"))
                    			EAPType.set(configuration,SecuritySettings.EAP_PROTOCOL_TYPE_PEAP);
                    		else if(profile.getEAPMethod().equalsIgnoreCase("TLS"))
                    			EAPType.set(configuration,SecuritySettings.EAP_PROTOCOL_TYPE_TLS);
                    		else if(profile.getEAPMethod().equalsIgnoreCase("TTLS"))
                    			EAPType.set(configuration,SecuritySettings.EAP_PROTOCOL_TYPE_TTLS);	            			
	            		}
	            			
	                    /*EAP Phase 2 Authentication*/
	            		if(Phase2AuthenticationType != null){
	            			//Phase2AuthenticationType.set(configuration,SecuritySettings.EAP_PHASE2_AUTHENTICATION_TYPE);
	            			// NONE,PAP,MSCHAP,MSCHAPV2,GTC
                    		if(profile.getPhase2Auth().equalsIgnoreCase("GTC"))
                    			Phase2AuthenticationType.set(configuration,AUTH_PREFIX+SecuritySettings.EAP_PHASE2_AUTHENTICATION_TYPE_GTC);
                    		else if(profile.getPhase2Auth().equalsIgnoreCase("MSCHAP"))
                    			Phase2AuthenticationType.set(configuration,AUTH_PREFIX+SecuritySettings.EAP_PHASE2_AUTHENTICATION_TYPE_MSCHAP);
                    		else if(profile.getPhase2Auth().equalsIgnoreCase("MSCHAPV2"))
                    			Phase2AuthenticationType.set(configuration,AUTH_PREFIX+SecuritySettings.EAP_PHASE2_AUTHENTICATION_TYPE_MSCHAPV2);
                    		else if(profile.getPhase2Auth().equalsIgnoreCase("PAP"))
                    			Phase2AuthenticationType.set(configuration,AUTH_PREFIX+SecuritySettings.EAP_PHASE2_AUTHENTICATION_TYPE_PAP);
                    		else 
                    			Phase2AuthenticationType.set(configuration,AUTH_PREFIX+SecuritySettings.EAP_PHASE2_AUTHENTICATION_TYPE_NONE);	   
	            		}	            				                	
	                	
	                	for(Certificate c : certList){
	    					if(c.mCertType_CACert){
	    						/*EAP CA Certificate*/
	    		                //String certName = CertificateInstaller.getCertName(certificate.mCertificate_java_x509);
	    						if(CACert != null && profile.getCACert().equalsIgnoreCase(c.mFileName)
	    								&& c.mCertificate_java_x509.getSerialNumber().equals(new BigInteger(profile.getLinking_Cert_SerialNo())))
	    							CACert.set(configuration, CertificateHelper.KEYSTORE_URI + CertificateHelper.CA_CERTIFICATE +	c.mInstallName);
	    					}else if(c.mCertType_UserCert){
	    						/*EAp Client certificate*/
	    						if(ClientCert != null && profile.getClientCert().equalsIgnoreCase(c.mFileName))
	    							ClientCert.set(configuration, CertificateHelper.KEYSTORE_URI + CertificateHelper.USER_CERTIFICATE +c.mInstallName);
	    						 /*EAP Private key*/
	    						if(PrivateKey != null && profile.getClientCert().equalsIgnoreCase(c.mFileName))
	    							PrivateKey.set(configuration, CertificateHelper.KEYSTORE_URI + CertificateHelper.USER_PRIVATE_KEY +c.mInstallName);
	    					}
	    				}
	                	
	                   /*EAP Identity*/
	                	 if(Identity != null)
	                		 Identity.set(configuration,profile.getUsername());
	                    /*EAP Anonymous Identity*/
	                	 if(AnonymousIdentity != null)
	                		// AnonymousIdentity.set(configuration,"");
	                    /*EAP Password*/
	                	 if(Password != null)
	                		 Password.set(configuration, profile.getPassword());
	                	 
	                	 isProfileConfigured = true;
	                	 
	            	}else{
	                	LogUtil.e(TAG + "This device is currently not supported.EnterpriseFieldClass Fields Not found.");
	                	isProfileConfigured = false;
	                }
	            }else{
	            	LogUtil.e(TAG + "This device is currently not supported.EnterpriseFieldClass Not found.");
	            	isProfileConfigured = false;
	            }
	        }catch(Exception e){
	        	LogUtil.e(TAG+"ERROR : "+e.toString());
	        	isProfileConfigured = false;
	        }
	        
	        mProfileList.add(configuration);
		}
        return isProfileConfigured;
	}
	
}
