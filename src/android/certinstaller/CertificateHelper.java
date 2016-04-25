/********************************************************************
 * LGI CONFIDENTIAL                                                 *
 * Copyright 2013 LGI                              					*
 * 																	*
 * Helper class providing Certificate related APIs for performing	*
 * various operations												*
 ********************************************************************/

package com.polarcape.secureconnect.certintaller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.openssl.PEMWriter;

import com.lgi.myupc.ch.R;
import com.polarcape.secureconnect.WiFiConfiguratorApplication;
import com.polarcape.secureconnect.util.LogUtil;
import com.polarcape.secureconnect.wifiprofileconfiguration.WiFiProfile;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources.NotFoundException;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;


public class CertificateHelper {

	private static final String TAG = "SecureConnect->CertificateHelper";

	public static final String CERTIFICATE_TYPE_EXT_CERT = ".crt";
	public static final String CERTIFICATE_TYPE_EXT_P12 = ".p12";
	public static final String CERTIFICATE_TYPE_EXT_PFX = ".pfx";
	public static final String CERTIFICATE_TYPE_EXT_CER = ".cer";
	public static final String CERTIFICATE_TYPE_EXT_DER = ".der";
	public static final String CERTIFICATE_TYPE_EXT_PEM = ".pem";

	public static final String CERTIFICATE_INSTALL_ACTION_PRE_ICS = "android.credentials.INSTALL";
	public static final String CERTIFICATE_INSTALL_ACTIVITY_PACKAGE_NAME_PRE_ICS = "com.android.certinstaller";
	public static final String CERTIFICATE_INSTALL_ACTIVITY_NAME_PRE_ICS = "com.android.certinstaller.CertInstallerMain";

	public static final String CERTIFICATE_INSTALL_ACTION_PRE_ICS_ALTERNATE = "android.credentials.SYSTEM_INSTALL";
	public static final String CERTIFICATE_INSTALL_ACTIVITY_PACKAGE_NAME_PRE_ICS_ALTERNATE = "com.android.settings";
	public static final String CERTIFICATE_INSTALL_ACTIVITY_NAME_PRE_ICS_ALTERNATE = "com.android.settings.CredentialInstaller";

	public static final int INSTALL_KEYCHAIN_CODE = 1;
	public static final int CREDENTIAL_UNLOCK_CODE = 2;	
	
   /*Keystore prefix*/
	public static final String KEYSTORE_URI = "keystore://";    
   /*Key prefix for CA certificates*/
   public static final String CA_CERTIFICATE = "CACERT_";
   /*Key prefix for User certificates*/
   public static final String USER_CERTIFICATE = "USRCERT_";
   /*Key prefix for user private keys*/
   public static final String USER_PRIVATE_KEY = "USRPKEY_";
   /*Data type for public key*/
   public static final String PUBLIC_KEY = "KEY";   
   /*Data type for private key*/
   public static final String PRIVATE_KEY = "PKEY";
   /*Data type for certificates*/
   public static final String EXTRA_CERTTYPE_CERTIFICATE = "CERT";
   /*Data type for certificates*/
   public static final String EXTRA_CERTTYPE_PKCS12 = "PKCS12";
   /*Data type for certificates*/
   public static final String EXTRA_NAME = "name";
	
   /**
    * Reads and loads the certificates into the program from assets directory
    * 
    * @param context
    * @return true if the certificates could be successfully loaded from assets,false otherwise
    */
	public static boolean generateCertList(Context context){
		ArrayList<Certificate> certificateHolder = new ArrayList<Certificate>();
		try {
			AssetManager assetManager = context.getResources().getAssets();
			List<String> certFiles = Arrays.asList(assetManager.list("certificates"));
			
			String certInstallName = null;
			if(android.os.Build.MANUFACTURER.equalsIgnoreCase("htc"))
				certInstallName = context.getResources().getString(R.string.certificate_name);
			else
				certInstallName = context.getResources().getString(R.string.certificate_name);
			
			if(android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.GINGERBREAD_MR1){
				for(String certFileName : certFiles) {
					InputStream is = assetManager.open("certificates/" + certFileName);
					generateCertInternalAPILevel10(certificateHolder,certFileName,certInstallName,is);
					WiFiConfiguratorApplication.setCertList(certificateHolder);
				}	
			}else{
				for(String certFileName : certFiles) {
					InputStream is = assetManager.open("certificates/" + certFileName);
					generateCertInternalAPILevel10(certificateHolder,certFileName,certInstallName,is);
					WiFiConfiguratorApplication.setCertList(certificateHolder);
				}
			}
			
		} catch (Exception e) {
			LogUtil.e(TAG + " ERROR: " + e.toString());
		} 
		WiFiConfiguratorApplication.setCertList(certificateHolder);
		return (certificateHolder.size() > 0) ? true : false ;
	}

	
	
	private static Certificate generateCertInternalAPILevel10(ArrayList<Certificate> certificateHolder, String certFileNameWithExtension,String certInstallName,InputStream certificateStream){
		Certificate cert = null;
		try {
			BufferedInputStream bis = new BufferedInputStream(certificateStream);
			byte[] certByteArray = new byte[bis.available()];
			bis.read(certByteArray);
			cert = new Certificate(certByteArray);
			
			try {
				//taking first index of '.' while setting the certificate name because 
				//Credential Installer Name popup does not take special characters(sent via EXTRA_NAME)
				//unless entered manually
				String certFileName =certFileNameWithExtension.substring(0,certFileNameWithExtension.indexOf("."));
				String certFileExtension = certFileNameWithExtension.substring(certFileNameWithExtension.lastIndexOf("."), certFileNameWithExtension.length());
				//check if valid cert type
				if(isCertFileAcceptable(certFileNameWithExtension)){
					
					cert.mCertificateExtension = certFileExtension; 
					cert.mFileName = certFileNameWithExtension;
					cert.mInstallName = certInstallName;					
					if(certFileExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_CER) 
							|| certFileExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_CERT)
							|| certFileExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_DER)
								|| certFileExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_PEM) ){									
						
						cert.mCertificate_java_x509 = convert(javax.security.cert.X509Certificate.getInstance(certByteArray));									
						cert.mAliasName = certFileName;
						if(CertificateHelper.isCa(cert.mCertificate_java_x509))
							cert.mCertType_CACert = true;
						else
							cert.mCertType_UserCert = true;
						if(isCertNeedToBeInstalled(cert)){
							certificateHolder.add(cert);
						}			
					}else if(certFileExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_P12)
								|| certFileExtension.equals(CertificateHelper.CERTIFICATE_TYPE_EXT_PFX)){
						if(certFileNameWithExtension.equalsIgnoreCase("upcRsaCert.pfx")){
							CertificateHelper.extractPkcs12CertificateAPILevel10(certificateHolder,cert,"SSL4LGI");
						}		
					}					
				}else{								
					Log.e(TAG, "ERROR: " + "Invalid certificate type "+ certFileExtension);
				}							
			} catch (CertificateException e) {
				Log.e(TAG, "ERROR: " + e.toString());
			}
		} catch (NotFoundException e) {
			LogUtil.e(TAG + " ERROR: " + e.toString());
		} catch (IOException e) {
			LogUtil.e(TAG + " ERROR: " + e.toString());
		} catch (javax.security.cert.CertificateException e) {
			LogUtil.e(TAG + " ERROR: " + e.toString());
		} catch (Exception e) {
			LogUtil.e(TAG + " ERROR: " + e.toString());
		}finally {
			try {
				certificateStream.close();
			} catch (IOException e) {
				LogUtil.e(TAG + " ERROR: " + e.toString());
			}
			certificateStream = null;
		}
		return cert;
	}
	
	/** Checks whether a particular certificate is already installed
	 * 
	 * @param certificate ({@link com.libertyglobal.secureconnect.certinstaller.Certificate})
	 * @return String representing certificate alias name
	 */
	public static String isCertInstalled(Certificate certificate) {
		
		//dont bother if the certificate is already installed as we will not
		//be able to get the name with which it has been installed and hence will 
		//not be able to link it with the profile.
		//Install again.
		return null;	
	}
	
	/** Checks if the KeyStore is in initialized state prior to
	 * certificate installation.
	 * 
	 * @return  whether KeyStore is unlocked (for GB and below devices)
	 */	
	public static boolean isKeyStoreUnlockNeeded(){
		 int code = 't';
	        LocalSocketAddress address = new LocalSocketAddress("keystore", LocalSocketAddress.Namespace.RESERVED);
	        LocalSocket socket = new LocalSocket();
	        try {
	            socket.connect(address);

	            OutputStream out = socket.getOutputStream();
	            out.write(code);
	           
	            out.flush();
	            socket.shutdownOutput();

	            InputStream in = socket.getInputStream();
	            if ((code = in.read()) != 1) {
	                return true;
	            }else{
	            	return false;
	            }
	        } catch (IOException e) {

	        } finally {
	            try {
	                socket.close();
	            } catch (IOException e) {}
	        }
	        return true;
	}
	
	public static boolean isKeyStoreUnlockNeededAPI18(){
		boolean b = false;
		try {
			Class c = "android.security.KeyStore".getClass();
			c = Class.forName("android.security.KeyStore");
			Method getInstance = null;
			Method isUnlocked = null;
			Method state = null;
            for(Method m: c.getMethods()){
                if(m.getName().trim().equals("getInstance"))
                	getInstance = m;
                else  if(m.getName().trim().equals("isUnlocked"))
                	isUnlocked = m;
                else  if(m.getName().trim().equals("state"))
                	state = m;
            }
            Object o = (Object)(getInstance.invoke(null));
            b = (Boolean) isUnlocked.invoke(o);
            Object o1 =  (Object)state.invoke(o);
		}catch(Exception e){
			Log.e(TAG,"isKeyStoreUnlockNeededAPI18 e = "+e.toString());
		}
		return !b;
	}

	
	/**
	 * API to convert a certificate to PEM format
	 * 
	 * @param objects certificates to be converted
	 * @return byte[] pem converted certificated
	 */
	public static byte[] convertToPem(Object... objects) {
		try {
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(bao);
			PEMWriter pw = new PEMWriter(osw);
			for (Object o : objects)
				pw.writeObject(o);
			pw.close();
			return bao.toByteArray();
		} catch (IOException e) {
			// should not occur
			Log.e(TAG, "convertToPem(): " + e);
			throw new RuntimeException(e);
		}
	}
	
	
	private static boolean extractPkcs12CertificateAPILevel10(ArrayList<Certificate> certificateHolder,Certificate certificateInstance,String password) throws Exception {
		java.security.KeyStore keystore = java.security.KeyStore
				.getInstance("PKCS12");
		PasswordProtection passwordProtection = new PasswordProtection(
				password.toCharArray());
		keystore.load(new ByteArrayInputStream(certificateInstance.mCertificate_bytearray),
				passwordProtection.getPassword());

		Enumeration<String> aliases = keystore.aliases();
		if (!aliases.hasMoreElements())
			return false;

		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			KeyStore.Entry entry = keystore.getEntry(alias, passwordProtection);
			Log.d(TAG,"extracted alias = " + alias + ", entry=" + entry.getClass());

			PrivateKeyEntry pke;
			if (entry instanceof PrivateKeyEntry) {
				certificateInstance.mAliasName = alias;
				//certificateInstance.mInstallName = alias;
				pke = ((PrivateKeyEntry) entry);
				
				//storing the privateKeyEntry
				Certificate clientCertificate = certificateInstance.clone();
				clientCertificate.mPrivateKeyEntry = pke;
				//certificateInstance.mPrivateKeyEntry = pke;
				
				//storing the client certificate
 				//certificateInstance.mCertificate_java_x509 = (X509Certificate) pke.getCertificate();
				clientCertificate.mCertificate_java_x509 = (X509Certificate) pke.getCertificate();
				clientCertificate.mCertType_UserCert = true;
				//no need for client certificates
				//certificateHolder.add(clientCertificate);
				
 				java.security.cert.Certificate[] certs = pke
						.getCertificateChain();
				Log.d(TAG, "# certs extracted = " + certs.length);
				List<X509Certificate> caCerts = new ArrayList<X509Certificate>(
						certs.length);
				for (java.security.cert.Certificate c : certs) {
					X509Certificate cert = (X509Certificate) c;
					// we are concerned with only CA certificates
					if (isCa(cert)){						
						Certificate caCertificate = certificateInstance.clone();
						caCertificate.mCertificate_java_x509 = cert;
						caCertificate.mCertType_CACert = true;
						if(isCertNeedToBeInstalled(caCertificate))
							certificateHolder.add(caCertificate);
					}
				}
				Log.d(TAG, "# ca certs extracted = " + caCerts.size());
			}
		}
		return true;
	}
	
	/**Install only those certificates which are valid as per the config files <linking_cert_serial> values*/
	private static boolean isCertNeedToBeInstalled(Certificate cert){
		boolean install = false;
		for(WiFiProfile wp : WiFiConfiguratorApplication.getWiFiProfileList()){
			if(cert.mCertificate_java_x509.getSerialNumber().equals(new BigInteger(wp.getLinking_Cert_SerialNo()))){
				install = true;
				break;
			}					
		}
		return install;
	}
	
	
	
	/** API to check if the certificate is a CA certificate
	 * 
	 * @param certificate in {@link java.security.cert.X509Certificate} format
	 * @return boolean indicating if this certificate is a CA certificate 
	 */
	private static boolean isCa(X509Certificate certificate) {
		try {
			byte[] basicConstraints = certificate.getExtensionValue("2.5.29.19");
			Object obj = new ASN1InputStream(basicConstraints).readObject();
			basicConstraints = ((DEROctetString) obj).getOctets();
			obj = new ASN1InputStream(basicConstraints).readObject();
			return new BasicConstraints((ASN1Sequence) obj).isCA();
		} catch (Exception e) {
			return false;
		}
	}

	/** API to check if the certificate is in Android acceptable format
	 * 
	 * @param filename representing certificate name with extension
	 * @return boolean indicating if the given file is in Android acceptable Certificate format
	 */
	private static boolean isCertFileAcceptable(String filename) {
        
		return (filename.endsWith(CertificateHelper.CERTIFICATE_TYPE_EXT_P12)
				|| filename.endsWith(CertificateHelper.CERTIFICATE_TYPE_EXT_PFX) 
        		|| filename.endsWith(CertificateHelper.CERTIFICATE_TYPE_EXT_CERT) 
        		|| filename.endsWith(CertificateHelper.CERTIFICATE_TYPE_EXT_CER)
        		|| filename.endsWith(CertificateHelper.CERTIFICATE_TYPE_EXT_DER)
        		|| filename.endsWith(CertificateHelper.CERTIFICATE_TYPE_EXT_PEM));
    }

	/** API to convert a javax.security.cert.X509Certificate to java.security.cert.X509Certificate format
	 * 
	 * @param certificate in {@link javax.security.cert.X509Certificate} format
	 * @return {@link java.security.cert.X509Certificate} format certificate
	 */ 
	private static java.security.cert.X509Certificate convert(
			javax.security.cert.X509Certificate certificate) {
		try {
			byte[] encoded = certificate.getEncoded();
			ByteArrayInputStream bis = new ByteArrayInputStream(encoded);
			java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory
					.getInstance("X.509");
			return (java.security.cert.X509Certificate) cf
					.generateCertificate(bis);
		} catch (java.security.cert.CertificateEncodingException e) {
		} catch (javax.security.cert.CertificateEncodingException e) {
		} catch (java.security.cert.CertificateException e) {
		}
		return null;
	}

	/** API to get the certificate Alias name
	 * 
	 * @param certificate in {@link java.security.cert.X509Certificate} format certificate
	 * @return String representing Alias name of the certificate 
	 */
	@SuppressWarnings("unused")
	private static String getCertAliasName(java.security.cert.X509Certificate certificate) {

		String aliasName = null;
		try {
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
				KeyStore ks = KeyStore.getInstance("AndroidCAStore");
	            if (ks != null){
	                ks.load(null, null);
	                Enumeration<String> aliases = ks.aliases();
	                while (aliases.hasMoreElements()){
	                    String alias = (String) aliases.nextElement();
	                    java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) ks.getCertificate(alias);
	                    if (cert.getPublicKey().equals(certificate.getPublicKey())) {
	                    	aliasName  = alias;
	                        break;
	                    }
	                }
	            }
	        } 
		}catch (IOException e) {
	        	Log.e(TAG, "ERROR: " + e.toString());
	        } catch (KeyStoreException e) {
	        	Log.e(TAG, "ERROR: " + e.toString());
	        } catch (NoSuchAlgorithmException e) {
	        	Log.e(TAG, "ERROR: " + e.toString());
	        } catch (java.security.cert.CertificateException e) {
	        	Log.e(TAG, "ERROR: " + e.toString());
	        }
        
		return aliasName;
	}

}