package com.zld.weixinpay.utils.util;
/**
 * <pre>
 * Title:         HttpRequestProxy.java
 * Project:     HP-Common
 * Type:        com.hengpeng.common.web.HttpRequestProxy
 * Author:        benl
 * Create:         2007-7-3 上午03:07:07
 * Copyright:     Copyright (c) 2007
 * Company:
 * <pre>
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * <pre>
 * HTTP请求代理类
 * </pre>
 * 
 * @author benl
 * @version 1.0, 2007-7-3
 */
public class HttpRequestProxy
{
    /**
     * 连接超时
     */
    private static int connectTimeOut = 1000;

    /**
     * 读取数据超时
     */
    private static int readTimeOut = 1000;

    /**
     * 请求编码
     */
    private static String requestEncoding = "utf-8";

    private static Logger logger = Logger.getLogger(HttpRequestProxy.class);

    /**
     * <pre>
     * 发送带参数的GET的HTTP请求
     * </pre>
     * 
     * @param reqUrl HTTP请求URL
     * @param parameters 参数映射表
     * @return HTTP响应的字符串
     */
    public static String doGet(String reqUrl, Map parameters,
            String recvEncoding)
    {
        HttpURLConnection url_con = null;
        String responseContent = null;
        try
        {
            StringBuffer params = new StringBuffer();
            for (Iterator iter = parameters.entrySet().iterator(); iter
                    .hasNext();)
            {
                Entry element = (Entry) iter.next();
                params.append(element.getKey().toString());
                params.append("=");
                params.append(URLEncoder.encode(element.getValue().toString(),
                        HttpRequestProxy.requestEncoding));
                params.append("&");
            }

            if (params.length() > 0)
            {
                params = params.deleteCharAt(params.length() - 1);
            }

            URL url = new URL(reqUrl);
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("GET");
            System.setProperty("sun.net.client.defaultConnectTimeout", String
                    .valueOf(HttpRequestProxy.connectTimeOut));// （单位：毫秒）jdk1.4换成这个,连接超时
            System.setProperty("sun.net.client.defaultReadTimeout", String
                    .valueOf(HttpRequestProxy.readTimeOut)); // （单位：毫秒）jdk1.4换成这个,读操作超时
            // url_con.setConnectTimeout(5000);//（单位：毫秒）jdk
            // 1.5换成这个,连接超时
            // url_con.setReadTimeout(5000);//（单位：毫秒）jdk 1.5换成这个,读操作超时
            url_con.setDoOutput(true);
            byte[] b = params.toString().getBytes();
            url_con.getOutputStream().write(b, 0, b.length);
            url_con.getOutputStream().flush();
            url_con.getOutputStream().close();

            InputStream in = url_con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in,
                    recvEncoding));
            String tempLine = rd.readLine();
            StringBuffer temp = new StringBuffer();
            String crlf=System.getProperty("line.separator");
            while (tempLine != null)
            {
                temp.append(tempLine);
                temp.append(crlf);
                tempLine = rd.readLine();
            }
            responseContent = temp.toString();
            rd.close();
            in.close();
        }
        catch (IOException e)
        {
            logger.error("网络故障", e);
        }
        finally
        {
            if (url_con != null)
            {
                url_con.disconnect();
            }
        }

        return responseContent;
    }

    /**
     * <pre>
     * 发送不带参数的GET的HTTP请求
     * </pre>
     * 
     * @param reqUrl HTTP请求URL
     * @return HTTP响应的字符串
     */
    public static String doGet(String reqUrl, String recvEncoding)
    {
        HttpURLConnection url_con = null;
        String responseContent = null;
        try
        {
            StringBuffer params = new StringBuffer();
            String queryUrl = reqUrl;
            int paramIndex = reqUrl.indexOf("?");

            if (paramIndex > 0)
            {
                queryUrl = reqUrl.substring(0, paramIndex);
                String parameters = reqUrl.substring(paramIndex + 1, reqUrl
                        .length());
                String[] paramArray = parameters.split("&");
                for (int i = 0; i < paramArray.length; i++)
                {
                    String string = paramArray[i];
                    int index = string.indexOf("=");
                    if (index > 0)
                    {
                        String parameter = string.substring(0, index);
                        String value = string.substring(index + 1, string
                                .length());
                        params.append(parameter);
                        params.append("=");
                        params.append(URLEncoder.encode(value,
                                HttpRequestProxy.requestEncoding));
                        params.append("&");
                    }
                }

                params = params.deleteCharAt(params.length() - 1);
            }

            URL url = new URL(queryUrl);
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("GET");
            System.setProperty("sun.net.client.defaultConnectTimeout", String
                    .valueOf(HttpRequestProxy.connectTimeOut));// （单位：毫秒）jdk1.4换成这个,连接超时
            System.setProperty("sun.net.client.defaultReadTimeout", String
                    .valueOf(HttpRequestProxy.readTimeOut)); // （单位：毫秒）jdk1.4换成这个,读操作超时
            // url_con.setConnectTimeout(5000);//（单位：毫秒）jdk
            // 1.5换成这个,连接超时
            // url_con.setReadTimeout(5000);//（单位：毫秒）jdk 1.5换成这个,读操作超时
            url_con.setDoOutput(true);
            byte[] b = params.toString().getBytes();
            url_con.getOutputStream().write(b, 0, b.length);
            url_con.getOutputStream().flush();
            url_con.getOutputStream().close();
            InputStream in = url_con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in,
                    recvEncoding));
            String tempLine = rd.readLine();
            StringBuffer temp = new StringBuffer();
            String crlf=System.getProperty("line.separator");
            while (tempLine != null)
            {
                temp.append(tempLine);
                temp.append(crlf);
                tempLine = rd.readLine();
            }
            responseContent = temp.toString();
            rd.close();
            in.close();
        }
        catch (IOException e)
        {
            logger.error("网络故障", e);
        }
        finally
        {
            if (url_con != null)
            {
                url_con.disconnect();
            }
        }

        return responseContent;
    }

    /**
     * <pre>
     * 发送带参数的POST的HTTP请求
     * </pre>
     * 
     * @param reqUrl HTTP请求URL
     * @param parameters 参数映射表
     * @return HTTP响应的字符串
     */
    public static String doPost(String reqUrl, Map parameters,
            String recvEncoding)
    {
        HttpURLConnection url_con = null;
        String responseContent = null;
        try
        {
        	logger.error("==postting....");
            StringBuffer params = new StringBuffer();
            for (Iterator iter = parameters.entrySet().iterator(); iter.hasNext();)
            {
                Entry element = (Entry) iter.next();
               // logger.error("params:"+element.getKey().toString());
                params.append(element.getKey().toString());
                
                params.append("=");
               // logger.error("value:"+element.getValue().toString());
               // params.append(URLEncoder.encode(element.getValue().toString(),HttpRequestProxy.requestEncoding));
                params.append(element.getValue().toString());
                params.append("&");
            }
            if (params.length() > 0)
            {
                params = params.deleteCharAt(params.length() - 1);
            }

            URL url = new URL(reqUrl);
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("POST");
//            System.setProperty("sun.net.client.defaultConnectTimeout", String
//                    .valueOf(HttpRequestProxy.connectTimeOut));// （单位：毫秒）jdk1.4换成这个,连接超时
//            System.setProperty("sun.net.client.defaultReadTimeout", String
//                    .valueOf(HttpRequestProxy.readTimeOut)); // （单位：毫秒）jdk1.4换成这个,读操作超时
            url_con.setConnectTimeout(10000);//（单位：毫秒）jdk1.5换成这个,连接超时
            url_con.setReadTimeout(10000);//（单位：毫秒）jdk 1.5换成这个,读操作超时
            url_con.setDoOutput(true);
            logger.error("url:"+reqUrl);
            logger.error("params:"+params.toString());
            byte[] b = params.toString().getBytes();
            url_con.getOutputStream().write(b, 0, b.length);
            url_con.getOutputStream().flush();
            url_con.getOutputStream().close();
            int rescode = url_con.getResponseCode();
			logger.error("getResponseCode:" + rescode);
			responseContent = rescode+"";
            InputStream in = url_con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in,
                    recvEncoding));
            String tempLine = rd.readLine();
            StringBuffer tempStr = new StringBuffer();
            String crlf=System.getProperty("line.separator");
            while (tempLine != null)
            {
                tempStr.append(tempLine);
                tempStr.append(crlf);
                tempLine = rd.readLine();
            }
            responseContent = tempStr.toString();
            rd.close();
            in.close();
        }
        catch (IOException e)
        {
            logger.error("网络故障", e);
        }
        finally
        {
            if (url_con != null)
            {
                url_con.disconnect();
            }
        }
        return responseContent;
    }
    public static String doPostToMp(String reqUrl, Map parameters, String recvEncoding) {
		HttpURLConnection url_con = null;
		String responseContent = null;
		try {
			//logger.error("==postting....");
			StringBuffer params = new StringBuffer();
			for (Iterator iter = parameters.entrySet().iterator(); iter.hasNext();) {
				Entry element = (Entry) iter.next();
				// logger.error("params:"+element.getKey().toString());
				params.append(element.getKey().toString());
				params.append("=");
				// logger.error("value:"+element.getValue().toString());
				// params.append(URLEncoder.encode(element.getValue().toString(),HttpRequestProxy.requestEncoding));
				params.append(element.getValue().toString());
				params.append("\n");
			}
			if (params.length() > 0) {
				params = params.deleteCharAt(params.length() - 1);
			}
			byte[] b = params.toString().getBytes(recvEncoding);
			//logger.error("content size:===========" + b.length);
			URL url = new URL(reqUrl);
			url_con = (HttpURLConnection) url.openConnection();
			url_con.setRequestMethod("POST");
			// System.setProperty("sun.net.client.defaultConnectTimeout", String
			// .valueOf(HttpRequestProxy.connectTimeOut));//
			// （单位：毫秒）jdk1.4换成这个,连接超时
			// System.setProperty("sun.net.client.defaultReadTimeout", String
			// .valueOf(HttpRequestProxy.readTimeOut)); //
			// （单位：毫秒）jdk1.4换成这个,读操作超时
			url_con.setConnectTimeout(60000);// （单位：毫秒）jdk1.5换成这个,连接超时
			url_con.setReadTimeout(60000);// （单位：毫秒）jdk 1.5换成这个,读操作超时
			url_con.setDoOutput(true);
			
			// logger.error("params:"+params.toString());

			url_con.getOutputStream().write(b, 0, b.length);

			url_con.getOutputStream().flush();
			url_con.getOutputStream().close();
			int rescode = url_con.getResponseCode();
			logger.debug("url:" + reqUrl+",getResponseCode:" + rescode);
			responseContent = rescode + "";
			return rescode + "";
			// InputStream in = url_con.getInputStream();
			// BufferedReader rd = new BufferedReader(new InputStreamReader(in,
			// recvEncoding));
			// String tempLine = rd.readLine();
			// StringBuffer tempStr = new StringBuffer();
			// String crlf=System.getProperty("line.separator");
			// while (tempLine != null)
			// {
			// tempStr.append(tempLine);
			// tempStr.append(crlf);
			// tempLine = rd.readLine();
			// }
			// responseContent = tempStr.toString();
			// rd.close();
			// in.close();
		} catch (IOException e) {
			logger.error("网络故障", e);
		} finally {
			if (url_con != null) {
				url_con.disconnect();
			}
		}
		return responseContent;
	}

    /**
     * @return 连接超时(毫秒)
     * @see com.hengpeng.common.web.HttpRequestProxy#connectTimeOut
     */
    public static int getConnectTimeOut()
    {
        return HttpRequestProxy.connectTimeOut;
    }

    /**
     * @return 读取数据超时(毫秒)
     * @see com.hengpeng.common.web.HttpRequestProxy#readTimeOut
     */
    public static int getReadTimeOut()
    {
        return HttpRequestProxy.readTimeOut;
    }

    /**
     * @return 请求编码
     * @see com.hengpeng.common.web.HttpRequestProxy#requestEncoding
     */
    public static String getRequestEncoding()
    {
        return requestEncoding;
    }

    /**
     * @param connectTimeOut 连接超时(毫秒)
     * @see com.hengpeng.common.web.HttpRequestProxy#connectTimeOut
     */
    public static void setConnectTimeOut(int connectTimeOut)
    {
        HttpRequestProxy.connectTimeOut = connectTimeOut;
    }

    /**
     * @param readTimeOut 读取数据超时(毫秒)
     * @see com.hengpeng.common.web.HttpRequestProxy#readTimeOut
     */
    public static void setReadTimeOut(int readTimeOut)
    {
        HttpRequestProxy.readTimeOut = readTimeOut;
    }

    /**
     * @param requestEncoding 请求编码
     * @see com.hengpeng.common.web.HttpRequestProxy#requestEncoding
     */
    public static void setRequestEncoding(String requestEncoding)
    {
        HttpRequestProxy.requestEncoding = requestEncoding;
    }
    
    public static void main(String[] args)
    {
        Map map = new HashMap();
        map.put("actionType", "1");
//        map.put("issueId", "33");
        String temp = HttpRequestProxy.doPost("http://192.168.0.99/AgentPortal/autoHandler", map, "GBK");
        System.out.println("返回的消息是:"+temp);
        
    }
}