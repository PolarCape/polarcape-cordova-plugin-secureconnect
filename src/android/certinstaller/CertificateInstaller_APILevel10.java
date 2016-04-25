/********************************************************************************************
 * LGI CONFIDENTIAL                                                 						*
 * Copyright 2013 LGI                              											*
 * 																							*
 * This class is an implementation of 														*
 * {@link com.libertyglobal.secureconnect.certinstaller.AbstractCertificateInstaller			*
 * for installing certificates on devices running AOS version Gingerbread(2.3.x) and below	*
 ********************************************************************************************/

package com.polarcape.secureconnect.certintaller;




import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class CertificateInstaller_APILevel10 extends AbstractCertificateInstaller{
	
	private static final String TAG = "SecureConnect->CertificateInstaller_APILevel10";
	
	public CertificateInstaller_APILevel10(Context context){
		super(context);
	}

	/**
	 *  API to create an populate(with certificate) the intent 
	 *  which will invoke the AOS version Gingerbread(2.3.x) and below
	 *  specific Android Certificate Installation Framework
	 * 
	 * @param certificate ({@link com.libertyglobal.secureconnect.certinstaller.Certificate}) which will be put as BUNDLE EXTRA in the 
	 * created intent
	 * @return Intent generated intent
	 */
	Intent createSystemInstallIntent(Certificate cert) {		
		
		/*Silent installation*/
		Intent intent = null;
		intent = new Intent("android.credentials.SYSTEM_INSTALL");
		intent.setClassName("com.android.settings","com.android.settings.CredentialInstaller");

		if(cert.mCertType_CACert == true){						
			//CA Certificate
			intent.putExtra(CertificateHelper.CA_CERTIFICATE + cert.mInstallName+"CA",
		    		CertificateHelper.convertToPem(cert.mCertificate_java_x509));	            
			}else if(cert.mCertType_UserCert == true) {				
		    	//User Certificate
				if(cert.mPrivateKeyEntry != null) {
					intent.putExtra(CertificateHelper.USER_PRIVATE_KEY + cert.mInstallName+"USER",
							CertificateHelper.convertToPem(cert.mPrivateKeyEntry.getPrivateKey()));
				}
				intent.putExtra(CertificateHelper.USER_CERTIFICATE + cert.mInstallName+"USER",
		            		CertificateHelper.convertToPem(cert.mCertificate_java_x509));
			}
		return intent;
    }

	/**
	 * AOS Version GingerBread(2.3.x) and below specific implementation of 
	 * {@link com.libertyglobal.secureconnect.certinstaller.AbstractCertificateInstaller#installCertificate(Certificate)}  
	 */
	@Override
	public void installCertificate(Certificate cert) {
		try {
			
				((Activity) mContext).startActivityForResult(
						createSystemInstallIntent(cert),
						CertificateHelper.INSTALL_KEYCHAIN_CODE);
			
		}catch(ActivityNotFoundException e){
			Log.e(TAG, "ERROR: " + e.toString());
		} 		
	}	
}








// Below code is just for ref`erence


/*Silent installation - Applicable only for GB and below devices*/

/*Intent intent = null;
intent = new Intent(CertificateHelper.CERTIFICATE_INSTALL_ACTION_PRE_ICS_ALTERNATE);
intent.setClassName("com.android.settings","com.android.settings.CredentialInstaller");


if(cert.mCertType_CACert == true){						
	//CA Certificate
	intent.putExtra(CertificateHelper.CA_CERTIFICATE + cert.mInstallName,
    		CertificateHelper.convertToPem(cert.mCertificate_java_x509));	            
	}else if(cert.mCertType_UserCert == true){				
    	//User Certificate
		if(cert.mPrivateKeyEntry != null)
			intent.putExtra(CertificateHelper.USER_PRIVATE_KEY + cert.mInstallName,
					CertificateHelper.convertToPem(cert.mPrivateKeyEntry.getPrivateKey()));
        
		intent.putExtra(CertificateHelper.USER_CERTIFICATE + cert.mInstallName,
            		CertificateHelper.convertToPem(cert.mCertificate_java_x509));
	}
return intent;*/


/* Standard Installation */
/*
 
 Intent intent = new Intent(CertificateHelper.CERTIFICATE_INSTALL_ACTION_PRE_ICS);
		intent.setClassName(CertificateHelper.CERTIFICATE_INSTALL_ACTIVITY_PACKAGE_NAME_PRE_ICS, CertificateHelper.CERTIFICATE_INSTALL_ACTIVITY_NAME_PRE_ICS);
        intent.putExtra(CertificateHelper.EXTRA_NAME, certificate.mInstallName);
        if (certificate.mCertificateExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_PFX)
                || certificate.mCertificateExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_P12)) {
        	 
        	intent.putExtra(CertificateHelper.EXTRA_CERTTYPE_PKCS12, certificate.mCertificate_bytearray);
			
        } else if (certificate.mCertificateExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_CER)
                || certificate.mCertificateExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_CERT)
                || certificate.mCertificateExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_DER)
                || certificate.mCertificateExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_PEM)) {
        	 
        	intent.putExtra(CertificateHelper.EXTRA_CERTTYPE_CERTIFICATE, certificate.mCertificate_bytearray);
        }        
        return intent;	
 
 
 */