/********************************************************************************************
 * LGI CONFIDENTIAL                                                 						*
 * Copyright 2013 LGI                              											*
 * 																							*
 * This class is an implementation of 														*
 * {@link com.libertyglobal.secureconnect.certinstaller.AbstractCertificateInstaller			*
 * for installing certificates on devices running AOS version JellyBean (4.1.x) and above	*
 ********************************************************************************************/

package com.polarcape.secureconnect.certintaller;

import java.security.cert.CertificateEncodingException;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.security.KeyChain;
import android.util.Log;

import com.lgi.myupc.ch.MyUPC;
import com.polarcape.secureconnect.util.LogUtil;

 public class CertificateInstaller_APILevel18 extends AbstractCertificateInstaller{

	private static final String TAG = "SecureConnect->CertificateInstaller_APILevel18";
	
	public CertificateInstaller_APILevel18(Context context){
		super(context);
	}

	/**
	 *  API to create an populate(with certificate) the intent 
	 *  which will invoke the AOS Version ICS (4.0.x) & above specific 
	 *  Android Certificate Installation Framework
	 * 
	 * @param certificate ({@link com.libertyglobal.secureconnect.certinstaller.Certificate}) which will be put as BUNDLE EXTRA in the 
	 * created intent
	 * @return Intent generated intent
	 */
	@SuppressLint("InlinedApi") Intent createSystemInstallIntent(Certificate certificate) {
		
		Intent intent=new Intent("android.credentials.INSTALL");
        intent.setClassName("com.android.certinstaller",
                "com.android.certinstaller.CertInstallerMain");
        intent.putExtra("install_as_uid",1010);
		
		intent.putExtra(KeyChain.EXTRA_NAME, certificate.mInstallName);	
		
		//bart workaround
		if(certificate.mCertType_CACert){
			try {
				intent.putExtra(KeyChain.EXTRA_CERTIFICATE,
						certificate.mCertificate_java_x509.getEncoded());
			} catch (CertificateEncodingException e) {
				LogUtil.e(TAG + "createSystemInstallIntent()-> error : "+e.toString());
			}
		}else{
			if (certificate.mCertificateExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_PFX)
	                || certificate.mCertificateExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_P12)) {
					
			 intent.putExtra(KeyChain.EXTRA_PKCS12, certificate.mCertificate_bytearray);
				
	        } else if (certificate.mCertificateExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_CER)
	                || certificate.mCertificateExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_CERT)
	                || certificate.mCertificateExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_DER)
				|| certificate.mCertificateExtension
						.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_PEM)) {
			try {
				intent.putExtra(KeyChain.EXTRA_CERTIFICATE,
						certificate.mCertificate_java_x509.getEncoded());
			} catch (CertificateEncodingException e) {
				LogUtil.e(TAG + "createSystemInstallIntent()-> error : "+e.toString());
			}
	        }
		}

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       return intent;		
    }

	/**
	 * AOS Version JellyBean(4.1.x) specific implementation of 
	 * {@link com.libertyglobal.secureconnect.certinstaller.AbstractCertificateInstaller#installCertificate(Certificate)}  
	 */
	@Override
	public void installCertificate(Certificate cert) {
		try {
//			if (mContext instanceof MyUPC) {
//			mContext.startActivity(createSystemInstallIntent(cert));
			
				((MyUPC) mContext).startActivityForResult(
						createSystemInstallIntent(cert)
						,CertificateHelper.INSTALL_KEYCHAIN_CODE);
//			}
		}catch(ActivityNotFoundException e){
			Log.e(TAG, "ERROR: " + e.toString());
		} 
	}
		
}