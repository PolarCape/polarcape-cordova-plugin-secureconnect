/********************************************************************
 * LGI CONFIDENTIAL                                                 *
 * Copyright 2013 LGI                              					*
 * 																	*
 * Abstract base class for creating Wi-Fi profiles in the device	*
 * In order to create and configure a Wi-Fi profile one must extend *
 * this class and provide AOS version specific implementations for 	*
 * the abstract method {@link #configureProfile()}					*
 ********************************************************************/

package com.polarcape.secureconnect.wifiprofileconfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.lgi.myupc.ch.R;
import com.polarcape.secureconnect.certintaller.Certificate;
import com.polarcape.secureconnect.util.LogUtil;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public abstract class AbstractWiFiProfileBuilder {

	private static final String TAG = "SecureConnect->AbstractWiFiProfileBuilder";
	public Context mContext;
    public static final String ENTERPRISEFIELD_CLASSNAME = "android.net.wifi.WifiConfiguration$EnterpriseField";
    public static ArrayList<WifiConfiguration> mProfileList = new ArrayList<WifiConfiguration>();

    /**********************EAP PROFILE KEYS****************************/ 
	public static class Keys {
		    
		 public static final String EAP_KEY             = "eap";
		 public static final String PHASE2_KEY          = "phase2";
		 public static final String IDENTITY_KEY        = "identity";
		 public static final String ANON_IDENTITY_KEY   = "anonymous_identity";
		 public static final String PASSWORD_KEY        = "password";
		 public static final String CLIENT_CERT_KEY     = "client_cert";
		 public static final String CA_CERT_KEY         = "ca_cert";
		 public static final String SUBJECT_MATCH_KEY   = "subject_match";
		 public static final String ENGINE_KEY          = "engine";
		 public static final String ENGINE_ID_KEY       = "engine_id";
		 public static final String PRIVATE_KEY_ID_KEY  = "key_id";
		 public static final String OLD_PRIVATE_KEY_NAME  = "private_key";
	}
	
	public static class SecuritySettings{		
		
		/*WiFi security type */
	    public static final int SECURITY_TYPE_IEEE8021X = WifiConfiguration.KeyMgmt.IEEE8021X;
	    public static final int SECURITY_TYPE_WPA_EAP = WifiConfiguration.KeyMgmt.WPA_EAP;
	    public static final int SECURITY_TYPE_WPA_PSK = WifiConfiguration.KeyMgmt.WPA_PSK;
	    
	    /*EAP method type */
	    public static final String EAP_PROTOCOL_TYPE_PEAP = "PEAP";
	    public static final String EAP_PROTOCOL_TYPE_TTLS = "TTLS";
		public static final Object EAP_PROTOCOL_TYPE_PWD = "PWD";
		public static final Object EAP_PROTOCOL_TYPE_TLS = "TLS";
		public static final Object EAP_PROTOCOL_TYPE_NONE = "None";
		
		/*EAP phase 2 authentication type */
		public static final Object EAP_PHASE2_AUTHENTICATION_TYPE_PAP = "PAP";
		public static final Object EAP_PHASE2_AUTHENTICATION_TYPE_MSCHAPV2 = "MSCHAPV2";
		public static final Object EAP_PHASE2_AUTHENTICATION_TYPE_MSCHAP = "MSCHAP";
		public static final Object EAP_PHASE2_AUTHENTICATION_TYPE_GTC = "GTC";
		public static final Object EAP_PHASE2_AUTHENTICATION_TYPE_NONE = "None";
	}

	
    /**********************WiFi EAP Profile Configuration Settings****************************/	
	
	public AbstractWiFiProfileBuilder(Context context){
		mContext = context;
	}

	/**For future use if every profile needs different certificates*/
	public abstract boolean configureProfile(Certificate certificate); 
	
	/**API which needs to be implemented by every class extending this class
	 * to provide Android OS version specific implementation of 
	 * creating and configuring a Wi-Fi profile on the user`s device */
	public abstract boolean configureProfile(String username, String password); 
	
	/**
	 * API to save a configured Wi-Fi profile on user`s device
	 */
	public boolean saveProfile(){
		int priority = 100;
		boolean isSuccess = true;
		WifiManager wifiManag = (WifiManager) mContext.getSystemService(
				 Context.WIFI_SERVICE);
		if(wifiManag != null){
			 boolean isWiFiEnabled = wifiManag.isWifiEnabled();
			 
			 //check if WiFi is disabled
			 if(!isWiFiEnabled){
				 isWiFiEnabled = wifiManag.setWifiEnabled(true);
				 //Enabling WiFi takes some time in Android.
				 //Wait for WiFi to get enabled(with a 5 second timeout).
				 int timeout = 0;
				 while(wifiManag.getWifiState() != WifiManager.WIFI_STATE_ENABLED && timeout<500){				
					 timeout = timeout + 1;
					 try {
						Thread.currentThread().sleep(10);
					} catch (InterruptedException e) {
						LogUtil.e(TAG + "saveProfile()->error ="+ e.toString());
					}
				 }
			 }
			 
			 //check if the Wi-Fi is enabled or the previous operation
			 // has timed out\			 
			 if(wifiManag.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
				for (WifiConfiguration conf : mProfileList) {
					// remove profile with existing name(if any) before creating
					// new one
					removeProfile(conf.SSID);
					//set priority for automatic connection
					conf.priority = priority++;	
					int netId = wifiManag.addNetwork(conf);
					boolean isProfileConfigured = wifiManag.saveConfiguration();
					if (!isProfileConfigured) {
						Toast.makeText(
								mContext,
								String.format(mContext.getResources().getString(R.string.profile_creation_Failure_msg),conf.SSID),
								Toast.LENGTH_LONG).show();
						LogUtil.e(TAG
								+ "saveProfile()->Failed to create profile with SSID "
								+ conf.SSID);
					}
					// enabling the network.
					boolean isNetworkEnabled = wifiManag.enableNetwork(netId,false);
					//enable to check created WiFi profiles
					//printWiFiConfigDump(conf,0);
				}
			 }else{
				 isSuccess = false;
			 }			 
		}else{
			isSuccess = false;
		}
		return isSuccess;
	}
	
	/**
	 * API to remove an existing Wi-Fi profile from user`s device
	 */
	public void removeProfile(String SSID){
		 WifiManager wifiManag = (WifiManager) mContext.getSystemService(
				 Context.WIFI_SERVICE);
		 if(wifiManag != null){
			 List<WifiConfiguration> list =  wifiManag.getConfiguredNetworks();
			 if(list != null){
				 for(WifiConfiguration w : list){
			    	   if(w.SSID.equals(SSID)){
			    		   wifiManag.removeNetwork(w.networkId);
			    	   }
			       }
			 }
		 }
	}

	/** The below APIs are not used currently but are kept for future debugging */	
	
	private void setIP(WifiConfiguration wc){
		try {
			Class<?> [] WifiConfigurationHiddenClasses = WifiConfiguration.class.getClasses();
			Field ipassignment = WifiConfiguration.class.getField("ipAssignment");			
			ipassignment.set(wc, Enum.valueOf((Class<Enum>) ipassignment.getType().asSubclass(Enum.class), "DHCP"));						
		}catch(Exception e){
			Log.e(TAG,"setIP"+e.toString());
		}
	}
	
	private String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }
	
	private void changePWD(String SSID){
		 WifiManager wifiManag = (WifiManager) mContext.getSystemService(
				 Context.WIFI_SERVICE);
		 if(wifiManag != null){
			 List<WifiConfiguration> list =  wifiManag.getConfiguredNetworks();
			 if(list != null){
				 for(WifiConfiguration w : list){
			    	   if(w.SSID.equals(SSID)){
			    		   try {
				   	            Class<?> [] WifiConfigurationHiddenClasses = WifiConfiguration.class.getClasses();
				   	            Class<?> EnterpriseFieldClass = null;
				   	            for (Class<?> wcClass : WifiConfigurationHiddenClasses){
				   	            	if (wcClass.getName().equals(ENTERPRISEFIELD_CLASSNAME)) {
				   	                	EnterpriseFieldClass = wcClass;
				   	                    break;
				   	                }
				   	            }
				   	            Field Password = null;


				   	            if(EnterpriseFieldClass != null){
				   	            	Field[] EnterpriseFields = WifiConfiguration.class.getFields();
				   	            	if(EnterpriseFields != null && EnterpriseFields.length >0){
				   	            		for (Field wcefField : EnterpriseFields){
				   	                        if (wcefField.getName().equals(Keys.PASSWORD_KEY))
				   	                        	Password = wcefField;
	 
				   	                	}
				   	                	
				   	                	//getting the method setValue() from WiFiConfiguration.EnterpriseField class
				   	                	Method EnterpriseFieldMethodSetValue = null;
				   	                    for(Method m: EnterpriseFieldClass.getMethods())
				   	                        if(m.getName().trim().equals("setValue"))
				   	                        	EnterpriseFieldMethodSetValue = m;        	
				   	                    
				   	                    if(EnterpriseFieldMethodSetValue != null){
				   	                  if(Password != null){
				                        	EnterpriseFieldMethodSetValue.invoke(Password.get(w), "\"kuntal\"");
				                        	wifiManag.updateNetwork(w);
				                        	saveNetwork(w);
				                        	wifiManag.saveConfiguration();
				   	                  }
				   	            	}
				   	            }
				   	        }
				    		   }catch(Exception e){
				   	        	LogUtil.e(TAG+"ERROR 4: configureProfile() -> "+e.toString());
				   	        }
			    	   }
			       }
			 }
		 }
	}
	
	private void testProfilePassword(String SSID){
		 WifiManager wifiManag = (WifiManager) mContext.getSystemService(
				 Context.WIFI_SERVICE);
		 if(wifiManag != null){
			 List<WifiConfiguration> list =  wifiManag.getConfiguredNetworks();
			 if(list != null){
				 for(WifiConfiguration w : list){
			    	   if(w.SSID.equals(SSID)){
			    		   try {
			   	            Class<?> [] WifiConfigurationHiddenClasses = WifiConfiguration.class.getClasses();
			   	            Class<?> EnterpriseFieldClass = null;
			   	            for (Class<?> wcClass : WifiConfigurationHiddenClasses){
			   	            	if (wcClass.getName().equals(ENTERPRISEFIELD_CLASSNAME)) {
			   	                	EnterpriseFieldClass = wcClass;
			   	                    break;
			   	                }
			   	            }
			   	           
			   	            Field Phase2AuthenticationType = null;
			   	            Field Identity = null;
			   	            Field Password = null;


			   	            if(EnterpriseFieldClass != null){
			   	            	Field[] EnterpriseFields = WifiConfiguration.class.getFields();
			   	            	if(EnterpriseFields != null && EnterpriseFields.length >0){
			   	            		for (Field wcefField : EnterpriseFields){
			   	                		if (wcefField.getName().equals(Keys.PHASE2_KEY))
			   	                			Phase2AuthenticationType = wcefField;

			   	                        else if (wcefField.getName().equals(Keys.IDENTITY_KEY))
			   	                        	Identity = wcefField;
			   	                        else if (wcefField.getName().equals(Keys.PASSWORD_KEY))
			   	                        	Password = wcefField;
 
			   	                	}
			   	                	
			   	                	//getting the method setValue() from WiFiConfiguration.EnterpriseField class
			   	                	Method EnterpriseFieldMethodSetValue = null;
			   	                    for(Method m: EnterpriseFieldClass.getMethods())
			   	                        if(m.getName().trim().equals("value"))
			   	                        	EnterpriseFieldMethodSetValue = m;      		
			   	                    
			   	                    if(EnterpriseFieldMethodSetValue != null){
			   		
			   	                    	/*EAP Phase 2 Authentication*/
			   	                    	if(Phase2AuthenticationType != null) {
			   	                    		Object o = EnterpriseFieldMethodSetValue.invoke(
			   	                    				Phase2AuthenticationType.get(w));
			   	                    	}

			   	                     if(Identity != null){
			   	                        	Object uid = EnterpriseFieldMethodSetValue.invoke(Identity.get(w));
			   	                     }
	
			   	                     if(Password != null){
			   	                        	Object pwd = EnterpriseFieldMethodSetValue.invoke(Password.get(w));
			   	                     }
			   	            	}
			   	            }
			   	        }
			    		   }catch(Exception e){
			   	        	LogUtil.e(TAG+"ERROR 4: configureProfile() -> "+e.toString());
			   	        }
			    	   }
			       }
			 }
		 }
	}
	
	private void saveNetwork(WifiConfiguration wc){
		 WifiManager wifiManag = (WifiManager) mContext.getSystemService(
				 Context.WIFI_SERVICE);
		 try {
			 Method saveNetwork = null;
             for(Method m: WifiManager.class.getMethods()){
            	 if(m.getName().trim().equals("saveNetwork")){
                	 saveNetwork = m;
            	 break;
            	 }
             }
             saveNetwork.invoke(wifiManag,wc);  
		 }catch(Exception e){
	        	LogUtil.e(TAG+"ERROR 4: configureProfile() -> "+e.toString());
		 }
		
	}
	
	private void printStatusandPrioprityToLog(){
		 WifiManager wifiManag = (WifiManager) mContext.getSystemService(
				 Context.WIFI_SERVICE);
		 if(wifiManag != null){
   			 List<WifiConfiguration> list =  wifiManag.getConfiguredNetworks();
   			 if(list != null){
   				 for(WifiConfiguration w : list){
   					Log.e(TAG,"ProfileName-> "+w.SSID);
   					Log.e(TAG,"STATUS-> "+w.status);
   					Log.e(TAG,"Priority-> "+w.priority);
   					
   			       }
   			 }
   		 }
	}
	
	@SuppressLint("NewApi") private void printPWDToLog(){
		 WifiManager wifiManag = (WifiManager) mContext.getSystemService(
				 Context.WIFI_SERVICE);
		 
		 if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
			 if(wifiManag != null){
       			 List<WifiConfiguration> list =  wifiManag.getConfiguredNetworks();
       			 if(list != null){
       				 for(WifiConfiguration w : list){
       					Log.e(TAG,"ProfileName-> "+w.SSID);
       					Log.e(TAG,"ProfileUID-> "+w.enterpriseConfig.getIdentity());
       					Log.e(TAG,"ProfilePWD-> "+w.enterpriseConfig.getPassword());
       			       }
       			 }
			 }
		 }else{
			 try {
		            Class<?> [] WifiConfigurationHiddenClasses = WifiConfiguration.class.getClasses();
		            Class<?> EnterpriseFieldClass = null;
		            for (Class<?> wcClass : WifiConfigurationHiddenClasses){
		            	if (wcClass.getName().equals(ENTERPRISEFIELD_CLASSNAME)) {
		                	EnterpriseFieldClass = wcClass;
		                    break;
		                }
		            }
		            Field Identity = null;
		            Field Password = null;
		            if(EnterpriseFieldClass != null){
		            	Field[] EnterpriseFields = WifiConfiguration.class.getFields();
		            	if(EnterpriseFields != null && EnterpriseFields.length >0){
		            		for (Field wcefField : EnterpriseFields){
		            			if (wcefField.getName().equals(Keys.IDENTITY_KEY))
		                        	Identity = wcefField;
		                        else if (wcefField.getName().equals(Keys.PASSWORD_KEY))
		                        	Password = wcefField;
		                	}
		                	
		                	Method EnterpriseFieldMethodValue = null;
		                    for(Method m: EnterpriseFieldClass.getMethods()){
		                    	if(m.getName().trim().equals("value")){
		                    		EnterpriseFieldMethodValue = m; 
		                    		break;
		                    	}
		                        	 
		                    }
		                            			                    
		                    if(wifiManag != null){
		           			 List<WifiConfiguration> list =  wifiManag.getConfiguredNetworks();
		           			 if(list != null){
		           				 for(WifiConfiguration w : list){
		           					Log.e(TAG,"ProfileName-> "+w.SSID);
		           					if(EnterpriseFieldMethodValue != null){
		       	                     if(Identity != null){
		       	                        	Object uid = EnterpriseFieldMethodValue.invoke(Identity.get(w));
		       	                        	if(uid != null)
		       	                        	Log.e(TAG,"Identity-> "+uid.toString());
		       	                     }
		       	                     if(Password != null){
		       	                        	Object pwd = EnterpriseFieldMethodValue.invoke(Password.get(w));
		       	                        	if(pwd != null)
		       	                        	Log.e(TAG,"Password-> "+pwd.toString());
		       	                     }
		       	            	}
		           			       }
		           			 }
		           		 }
		                    
		                    
		            }
		        }
	 		   }catch(Exception e){
		        	LogUtil.e(TAG+"ERROR 4: configureProfile() -> "+e.toString());
		        }
		 }
		 
		 
		 
	}
	
	public static void printWiFiConfigDump(WifiConfiguration wc,int when){
		if(when == 0){
		Log.e("TAG->Testing","-----------------------------------------------------------------");
		Log.e("TAG->Testing","WiFi Conf == "+ wc.toString());
		Log.e("TAG->Testing","-----------------------------------------------------------------");
		}else if(when == 1){
			Log.e("TAG->Testing-Scanning Connected WiFi","-----------------------------------------------------------------");
			Log.e("TAG->Testing","WiFi Conf == "+ wc.toString());
			Log.e("TAG->Testing-Scanning Connected WiFi","-----------------------------------------------------------------");
		}
	}
	
}
