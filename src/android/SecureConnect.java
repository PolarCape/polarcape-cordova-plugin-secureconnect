package com.polarcape.secureconnect;

import java.util.ArrayList;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.polarcape.secureconnect.certintaller.AbstractCertificateInstaller;
import com.polarcape.secureconnect.certintaller.CertInstallerFactory;
import com.polarcape.secureconnect.certintaller.Certificate;
import com.polarcape.secureconnect.certintaller.CertificateHelper;
import com.polarcape.secureconnect.util.LogUtil;
import com.polarcape.secureconnect.util.WiFiConfiguratorUtil;
import com.polarcape.secureconnect.wifiprofileconfiguration.AbstractWiFiProfileBuilder;
import com.polarcape.secureconnect.wifiprofileconfiguration.WiFiProfileBuilderFactory;


public class SecureConnect extends CordovaPlugin {
	
	private static final String TAG = "SecureConnect";
	
	private AbstractCertificateInstaller mCertIntsaller = null;
	private AbstractWiFiProfileBuilder mWiFiProfileBuilder = null;
	
	private ArrayList<Certificate> mCertList = new ArrayList<Certificate>();
    
	private int installedCertificateIndex = 0;	
	
	boolean isProfileReadProcessSuccess = false;
	boolean isCertGenProcessSuccess = false;
	
	String username = "";
	String password = "";
	
	public SecureConnect() {
	}
	
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("installCertificates")) {
			username = (String) args.getJSONObject(0).get("username");
			password = (String) args.getJSONObject(0).get("password");
			Context context = cordova.getActivity();
			installWifiCertificates(context);
			//PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, installWifiCertificates(context));
			//pluginResult.setKeepCallback(true);
			//callbackContext.sendPluginResult(pluginResult);
			return true;
		}else if(action.equals("userHasLockScreen")){
			int lockType = LockType.getCurrent(cordova.getActivity().getContentResolver());
			if(lockType == LockType.NONE_OR_SLIDER){
				PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "false");
				pluginResult.setKeepCallback(true);
				callbackContext.sendPluginResult(pluginResult);
				return true;
			}else{
				PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "true");
				pluginResult.setKeepCallback(true);
				callbackContext.sendPluginResult(pluginResult);
				return true;
			}
		}
		return false;
	}
	
	public JSONObject installWifiCertificates(Context context){
		try {
			WiFiConfiguratorUtil.readProfileConfigurations(context);
			CertificateHelper.generateCertList(context);
			WiFiConfiguratorApplication.setContext(context);
			mCertList = WiFiConfiguratorApplication.getCertList();
			if(mCertList == null ||  mCertList.size() <=0){
				//show error dialogue
				LogUtil.e(TAG + " ERROR :init() -> No certificates found.");
				return new JSONObject();
			}else{
				if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1
						&& android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
					// AOSP framework allows to read system logs
					//from which we can determine the certificate installation name
					initiateStandardCertificateInstallationProcess(context);
					
				}else if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
						&& android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){
					// AOSP framework DO NOT allow to read system logs
					//We have to rely on user providing pre designated certificate name 'wifi'
					initiateStandardCertificateInstallationProcess(context);
					
				}else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
					//from API level 18 we do not need to install certificate manually
					// certificate can be passed to a WiFi profile as an X509 certificate
					//instance,which will then gets installed in the deice internally.
					if(CertificateHelper.isKeyStoreUnlockNeededAPI18()){
						
						Intent intent = new Intent("com.android.credentials.UNLOCK");
						this.cordova.startActivityForResult((CordovaPlugin) this, intent, CertificateHelper.CREDENTIAL_UNLOCK_CODE);
			           	//cordova.getActivity().startActivityForResult(intent,CertificateHelper.CREDENTIAL_UNLOCK_CODE);
					}else{
						if(mCertIntsaller == null){
							mCertIntsaller = CertInstallerFactory.getInstance(context).getCertificateInstaller(android.os.Build.VERSION.SDK_INT);
						}
						installCertificate(installedCertificateIndex);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONObject();
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		System.out.println("REQUEST CODE: " + requestCode + " ; RESULT CODE: " +resultCode);
		if(resultCode==0){
			if(mCertIntsaller == null){
				mCertIntsaller = CertInstallerFactory.getInstance(cordova.getActivity()).getCertificateInstaller(android.os.Build.VERSION.SDK_INT);
			}
			installCertificate(installedCertificateIndex);
			super.onActivityResult(requestCode, resultCode, intent);
		}
		
	};
	
	private void initiateStandardCertificateInstallationProcess(final Context context) {		
		//get certificate installer instance for this AOS version
		if(mCertIntsaller == null){
			mCertIntsaller = CertInstallerFactory.getInstance(context).getCertificateInstaller(android.os.Build.VERSION.SDK_INT);
		}
		if (mCertIntsaller != null) {					
			if(mCertList != null && mCertList.size() >0){				
				try {
					//Iterate on the list of certificates and install them if not already installed.
					//for ICS & up proceed with certificate installation process
					for (int index = 0; index < mCertList.size(); index++) {
						
						installedCertificateIndex = index;
						boolean isInstallProcessStarted = installCertificate(index);
						if (isInstallProcessStarted)
							return;
					}
					// all certificates are pre-installed,proceed with WiFi
					// profile creation
//					showCredentialInputDialog();
				}catch (Exception e) {
					LogUtil.e(TAG + "ERROR 1:initializeViews() ->  Certificate Installation Error " +e.toString());
//					displayStatusMessage(R.string.device_not_supported);
				}
			}else{
				//show error dialogue
				LogUtil.e(TAG + " ERROR 2:initializeViews() -> No certificates found.");
//				displayStatusMessage(R.string.device_not_supported);
			}
		} else{
			LogUtil.e(TAG + " ERROR 3:initializeViews() ->  CertiFicate Installer not found for this device.");
//			displayStatusMessage(R.string.device_not_supported);
		}
	}
	
	private boolean installCertificate(int index){
		//show progress dialogue
		boolean isInstallProcessStarted = false;
		String aliasName = CertificateHelper.isCertInstalled(mCertList.get(index));
		if(aliasName == null){
			//mCertIntsaller.installCertificate(mCertList.get(index));
			initiateWiFiProfileCreationProcess();
			isInstallProcessStarted = true;
		}else{
			//this code does not get executed
			mCertList.get(index).mAliasName = aliasName;
			// TODO
			mCertList.get(index).mInstallName = aliasName;
			isInstallProcessStarted = false;
		}
		return isInstallProcessStarted;
	}
	
	private void initiateWiFiProfileCreationProcess(){
		// get profile builder instance for this AOS version
		mWiFiProfileBuilder = WiFiProfileBuilderFactory.getInstance(
				cordova.getActivity()).getConfigurator(android.os.Build.VERSION.SDK_INT);
				
		boolean isProfileConfigured = mWiFiProfileBuilder.configureProfile(username,password);
		if(isProfileConfigured){
			mWiFiProfileBuilder.saveProfile();
		}
	}
	
	
}