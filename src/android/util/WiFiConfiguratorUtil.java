/************************************************************************
 * LGI CONFIDENTIAL                                                 	*
 * Copyright 2013 LGI                              						*
 * 																		*
 * Wi-Fi Configuration Utility class responsible for 					*
 * 1. Showing dialogue in error scenarios								*
 * 2. Reading Wi-Fi profiles(which are to be configured in the device)	*
 * and their corresponding settings from assets/configuration.xml file. *
 ************************************************************************/

package com.polarcape.secureconnect.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.content.res.AssetManager;

import com.polarcape.secureconnect.WiFiConfiguratorApplication;
import com.polarcape.secureconnect.wifiprofileconfiguration.WiFiProfile;

public class WiFiConfiguratorUtil {

	private static final String TAG = "SecureConnect->WiFiConfiguratorUtil";
	public static final int CREDENTIAL_INPUT_SUCCESS = 0;
	public static final int CREDENTIAL_INPUT_FAILURE = 1;


	/**
	 * API showing dialogue prompting user for credentials
	 * 
	 * @param activity
	 * @param messageHandler 
	 * @param messageID Message to be displayed
	 */
	//GK: removed showCredentialDialog
	
	/**
	 * API to read Wi-Fi profiles(which are to be configured in the device)
	 * and their corresponding settings from assets/configuration.xml file.
	 * 
	 * @param context
	 * @return boolean indicating read success/failure
	 */
	public static boolean readProfileConfigurations(Context context) {
		AssetManager assetManager = context.getResources().getAssets();
		SAXParserFactory spfac = SAXParserFactory.newInstance();
		SAXParser sp;
		InputStream is;
		try {
			is = assetManager.open("configuration.xml");
			sp = spfac.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {

				// SAX parser methods
				public void startDocument() {
					WiFiConfiguratorApplication.getWiFiProfileList().clear();
				}

				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {

					// <wifi_profile>
					if (qName.equalsIgnoreCase("wifi_profile")) {
						WiFiProfile profile = setProfileDetails(attributes);
						WiFiConfiguratorApplication.getWiFiProfileList().add(
								profile);
					}

				}

				// Parser utility methods
				private WiFiProfile setProfileDetails(Attributes attributes) {
					WiFiProfile profile = new WiFiProfile();

					profile.setSSIDName(attributes.getValue("ssid_name"));
					profile.setSecurityType(attributes
							.getValue("security_type"));
					profile.setEAPMethod(attributes.getValue("eap_method"));
					profile.setPhase2Auth(attributes.getValue("phase2_auth"));
					profile.setCACert(attributes.getValue("ca_certificate"));
					profile.setClientCert(attributes
							.getValue("client_certificate"));
					profile.setLinking_Cert_SerialNo((attributes.getValue("linking_cert_serial")));

					return profile;
				}
			};
			sp.parse(is, handler);
		} catch (ParserConfigurationException e) {
			LogUtil.e(TAG + "readProfileConfigurations()-> error : "
					+ e.toString());
		} catch (SAXException e) {
			LogUtil.e(TAG + "readProfileConfigurations()-> error : "
					+ e.toString());
		} catch (IOException e) {
			LogUtil.e(TAG + "readProfileConfigurations()-> error : "
					+ e.toString());
		}

		return (WiFiConfiguratorApplication.getWiFiProfileList().size() > 0) ? true
				: false;
	}

}
