/************************************************************************************
 * LGI CONFIDENTIAL                                                 				*
 * Copyright 2013 LGI                              									*
 * 																					*
 * Singleton Factory class which provides objects representing Android OS version	* 
 * specific implementation of 														*
 * {@link com.libertyglobal.secureconnect.certinstaller.AbstractCertificateInstaller}	*
 ************************************************************************************/

package com.polarcape.secureconnect.certintaller;

import android.content.Context;

public class CertInstallerFactory {
	
	private static CertInstallerFactory mCertInstallerFactory;
	private static Context mContext;
	
	private CertInstallerFactory() {}
	
	/**
	 * API to get instance of CertInstallerFactory singleton class
	 * 
	 * @param context
	 * @return CertInstallerFactory instance
	 */
	public static synchronized CertInstallerFactory getInstance(Context context) {
		if(mCertInstallerFactory == null || (mContext.hashCode()!= context.hashCode())){
			mCertInstallerFactory = new CertInstallerFactory();
			mContext = context;
		}
		return mCertInstallerFactory;
	}
	
	/**
	 * API to get object representing Android OS version specific concrete
	 * implementation of {@link com.libertyglobal.secureconnect.certinstaller.AbstractCertificateInstaller}
	 * 
	 * @param AOSVersion Device`s Android OS Version
	 * @return AbstractCertificateInstaller reference representing Android OS version specific concrete
	 * implementation of {@link com.libertyglobal.secureconnect.certinstaller.AbstractCertificateInstaller}  
	 */
	public AbstractCertificateInstaller getCertificateInstaller(int AOSVersion) {
		AbstractCertificateInstaller certIntsaller = null;

		if(AOSVersion <= android.os.Build.VERSION_CODES.GINGERBREAD_MR1){
			certIntsaller = new CertificateInstaller_APILevel10(mContext);
		}else if(AOSVersion > android.os.Build.VERSION_CODES.GINGERBREAD_MR1){
			certIntsaller = new CertificateInstaller_APILevel18(mContext);
		}
		return certIntsaller;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new CloneNotSupportedException();
	}
	
}
	
