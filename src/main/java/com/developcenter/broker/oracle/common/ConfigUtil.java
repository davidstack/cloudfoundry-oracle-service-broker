package com.developcenter.broker.oracle.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;




public class ConfigUtil {
	   
	
	private  static  Properties  property=new Properties();
	
	private static ConfigUtil instance=new ConfigUtil();
	
	private ConfigUtil()
	{
		loadProperties();
	}
	
	public static ConfigUtil getInstance()
	{
		return instance;
	}

	private void loadProperties() {

		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			InputStream is = loader.getResourceAsStream("oracle.properties");
			property.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getOracleUrl() {

		return property.getProperty("oracleurl");
	}
	
	public String getOracleDbPath()
	{
		return property.getProperty("oracledbdir");
	}
	public String getOracleDbUserName()
	{
		return property.getProperty("oracleusername");
	}
	public String getOracleDbPassWord()
	{
		return property.getProperty("oraclepassword");
	}
	public String getServiceNodes()
	{
		return null;
	}
	
	public String getOracleRoleName()
	{
		return property.getProperty("oraclerole");
	}
//	public static void main(String[] args) {
//		
//		System.out.println(new Timestamp());
//	}
}
