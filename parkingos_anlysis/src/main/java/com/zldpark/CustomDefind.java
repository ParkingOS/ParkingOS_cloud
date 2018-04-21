package com.zldpark;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 读取配置文件
 * @author Administrator
 *
 */
public class CustomDefind {

	//private static String PATH = ;
	
	public static String CUSTOMPARKIDS = getValue("CUSTOMPARKIDS");
	public static String ISLOTTERY = getValue("ISLOTTERY");
	public static String MONGOADDRESS = getValue("MONGOADDRESS");
	public static String PARKBACK = getValue("PARKBACK");
	public static String SERVERIP = getValue("SERVERIP");
	public static String PARKID = getValue("PARKID");
	public static String FTPUSER = getValue("FTPUSER");
	public static String FTPPWD = getValue("FTPPWD");
	public static String TESTCOMID = getValue("TESTCOMID");
	public static String FTPPORT = getValue("FTPPORT");
	public static String DIR = getValue("DIR");
	public static String getValue(String key){
		String fileName ="config.properties";
		//System.out.println(">>>00>>>>config file path:"+fileName);
		Properties properties = new Properties();
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("bank.txt");
			properties.load(is);
			return properties.getProperty(key);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "0";
	}
	
	public void reSetConfig() {
		CUSTOMPARKIDS = getValue("CUSTOMPARKIDS");
		ISLOTTERY = getValue("ISLOTTERY");
	}
}
