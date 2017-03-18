package com.zhenlaidian.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamTools {
	
	    /** 
	     * @方法功能 InputStream 转为 byte
	     * @return 字节数组 
	     * @throws Exception 
	     */  
	    public static byte[] inputStream2Byte(InputStream inStream)  
	            throws Exception {  
	        // ByteArrayOutputStream outSteam = new ByteArrayOutputStream();  
	        // byte[] buffer = new byte[1024];  
	        // int len = -1;  
	        // while ((len = inStream.read(buffer)) != -1) {  
	        // outSteam.write(buffer, 0, len);  
	        // }  
	        // outSteam.close();  
	        // inStream.close();  
	        // return outSteam.toByteArray();  
	        int count = 0;  
	        while (count == 0) {  
	            count = inStream.available();  
	        }  
	        byte[] b = new byte[count];  
	        inStream.read(b);  
	        return b;  
	    }  
	  
	    /** 
	     * @方法功能 byte 转为 InputStream 
	     * @return InputStream
	     * @throws Exception 
	     */  
	    public static InputStream byte2InputStream(byte[] b) throws Exception {  
	        InputStream is = new ByteArrayInputStream(b);  
	        return is;  
	    }  
	    
	    
	    /**
	    * 将一个字符串转化为输入流
	    */
	    public static InputStream getStringStream(String sInputString){
	    if (sInputString != null && !sInputString.trim().equals("")){
	    try{
	    ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());
	    return tInputStringStream;
	    }catch (Exception ex){
	    ex.printStackTrace();
	    }
	    }
	    return null;
	    }
	     
	    /**
	    * 将一个输入流转化为字符串
	    */
	    public static String getStreamString(InputStream tInputStream){
	    if (tInputStream != null){
	    try{
	    BufferedReader tBufferedReader = new BufferedReader(new InputStreamReader(tInputStream));
	    StringBuffer tStringBuffer = new StringBuffer();
	    String sTempOneLine = new String("");
	    while ((sTempOneLine = tBufferedReader.readLine()) != null){
	    tStringBuffer.append(sTempOneLine);
	    }
	    return tStringBuffer.toString();
	    }catch (Exception ex){
	    ex.printStackTrace();
	    }
	    }
	    return null;
	    }
}
