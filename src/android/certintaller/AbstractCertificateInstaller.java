/********************************************************************
 * LGI CONFIDENTIAL                                                 *
 * Copyright 2013 LGI                              					*
 * 																	*
 * Abstract base class for installing certificates.					*
 * In order to install a certificate one must extend this class and *
 * provide AOS version specific implementations for the abstract	* 
 * method {@link #installCertificate(Certificate cert)}				*
 ********************************************************************/

package com.polarcape.secureconnect.certintaller;

import android.content.Context;

public abstract class AbstractCertificateInstaller {

	public Context mContext;
	
	public AbstractCertificateInstaller(Context context){
		mContext = context;
	}
	
	/**API which needs to be implemented by every class extending this class
	 * to provide Android OS version specific implementation of 
	 * Certificate Installation process */
	public abstract void installCertificate(Certificate cert); 
			
}
