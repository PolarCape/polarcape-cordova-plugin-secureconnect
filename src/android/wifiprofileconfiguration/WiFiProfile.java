/************************************************************************
 * LGI CONFIDENTIAL                                                 	*
 * Copyright 2013 LGI                              						*
 * Class representing the Wi-Fi profiles which needs to be created and	*
 * configured on user`s device.											*
 ************************************************************************/

package com.polarcape.secureconnect.wifiprofileconfiguration;

public class WiFiProfile {
	
	private String SSID_Name = null;
	private String Security_Type = null;
	private String EAP_Method = null;
	private String Phase2_Auth = null;
	private String username = null;
	private String password = null;
	private String CA_Cert = null;
	private String Client_Cert = null;
	private String Linking_Cert_SerialNo = null;
	
	public void setSSIDName(String name){		
		SSID_Name = "\"" + name +"\"";
	}
	
	public void setSecurityType(String type){
		Security_Type = type;
	}
	
	public void setEAPMethod(String method){
		EAP_Method = method;
	}
	
	public void setPhase2Auth(String phase2){
		Phase2_Auth = phase2;
	}
	
	public void setUsername(String name){
		username = name;
	}
	
	public void setPassword(String pwd){
		password = pwd;
	}
	
	public void setCACert(String certName){
		CA_Cert = certName;
	}
	
	public void setClientCert(String certName){
		Client_Cert = certName;
	}
	
	public void setLinking_Cert_SerialNo(String serial){
		Linking_Cert_SerialNo = serial;
	}

	
	public String getSSIDName(){
		return SSID_Name;
	}
	
	public String getSecurityType(){
		return Security_Type;
	}
	
	public String getEAPMethod(){
		return EAP_Method;
	}
	
	public String getPhase2Auth(){
		return Phase2_Auth;
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public String getCACert(){
		return CA_Cert;
	}
	
	public String getClientCert(){
		return Client_Cert;
	}
	
	public String getLinking_Cert_SerialNo(){
		return Linking_Cert_SerialNo;
	}
	
	
	

}
