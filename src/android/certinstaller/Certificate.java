/********************************************************************
 * LGI CONFIDENTIAL                                                 *
 * Copyright 2013 LGI                              					*
 * 																	*
 * Class representing the certificates which needs to be installed	*
 ********************************************************************/

package com.polarcape.secureconnect.certintaller;

import java.security.KeyStore.PrivateKeyEntry;


public class Certificate {

	public byte[] mCertificate_bytearray = null;
	public java.security.cert.X509Certificate mCertificate_java_x509 = null;
	
	public String mAliasName = "";
	public String mFileName = "";
	public String mInstallName = "";	
	public String mCertificateExtension = "";
	
	public boolean mCertType_CACert =  false;
	public boolean mCertType_UserCert = false;
	public PrivateKeyEntry mPrivateKeyEntry = null ;
	
	public boolean isInstalled = false;

	public Certificate(byte[] cert){
		mCertificate_bytearray = cert;
	}
	
	public Certificate(java.security.cert.X509Certificate cert){
		mCertificate_java_x509 = cert;
	}
	
	public Certificate clone(){
		
		Certificate c = new Certificate(this.mCertificate_bytearray);
		c.mCertificate_java_x509 = this.mCertificate_java_x509;
		c.mAliasName = this.mAliasName;
		c.mFileName = this.mFileName;
		c.mInstallName = this.mInstallName;
		c.mCertificateExtension = this.mCertificateExtension;
		c.mCertType_CACert = this.mCertType_CACert;
		c.mCertType_UserCert = this.mCertType_UserCert;
		c.mPrivateKeyEntry = this.mPrivateKeyEntry;
		c.isInstalled = this.isInstalled;
		
		return c;
	}
	
}
