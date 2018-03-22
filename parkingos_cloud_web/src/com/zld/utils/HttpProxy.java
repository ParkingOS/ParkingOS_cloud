package com.zld.utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

public class HttpProxy {

	/**
	 * GET 请求，返回字符
	 * @param url
	 * @return
	 */
	public  String doGet(String url){
		HttpClient httpClient = new HttpClient();
		HttpMethod method = new GetMethod(url);
		try {
			httpClient.setConnectionTimeout(1000*20);
			httpClient.executeMethod(method);
			if(method.getStatusCode()==200){
				return method.getResponseBodyAsString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(method!=null)
				method.releaseConnection();
		}
		return null;
	}
	/**
	 * POST 请求，返回字符
	 * @param url
	 * @param params
	 * @return
	 */
	public  String doPost(String url,Map<String,String> params){
		HttpClient httpClient = new HttpClient();
		PostMethod post = new PostMethod(url);
		int state = 0;
		String result = "";
		try {

			NameValuePair[] pairs = new NameValuePair[params.size()];
			int i = 0;
			for(String key : params.keySet()){
				pairs[i]=new NameValuePair(key,params.get(key));
				i++;
			}
			post.setRequestBody( pairs);
			httpClient.setConnectionTimeout(1000*20);
			state = httpClient.executeMethod(post);
			if(state==HttpStatus.SC_OK){
				result= post.getResponseBodyAsString();
			}
			post.releaseConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(post!=null)
				post.releaseConnection();
		}
		return result;
	}

	public  String doPostJson(String url,Map<String,Object> params){
		HttpClient httpClient = new HttpClient();
		PostMethod post = new PostMethod(url);
		int state = 0;
		String result = "";
		try {
			String content = StringUtils.createJson(params);
			System.out.println(content);
			RequestEntity requestEntity = new StringRequestEntity(content, null, "utf-8");
			post.setRequestEntity(requestEntity);
			post.setRequestHeader("Accept", "application/json");
			post.setRequestHeader("Content-type", "application/json");
//			post.setEntity(new StringEntity(entity));
//			post.setHeader("Accept", "application/json");
//			post.setHeader(, "application/json");
			httpClient.setConnectionTimeout(1000*20);
			state = httpClient.executeMethod(post);
			if(state==HttpStatus.SC_OK){
				result= post.getResponseBodyAsString();
			}
			post.releaseConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(post!=null)
				post.releaseConnection();
		}
		return result;
	}
	/**
	 * POST2 请求，返回字符
	 * @param url
	 * @param params
	 * @return
	 */
	public  String doPostTwo(String url,Map<String,Object> params){
		System.err.println(">>>>>>>>>>>http url:"+url);
		HttpClient httpClient = new HttpClient();
		PostMethod post = new PostMethod(url);
		int state = 0;
		String result = "";
		try {

			NameValuePair[] pairs = new NameValuePair[params.size()];
			int i = 0;
			for(String key : params.keySet()){
				pairs[i]=new NameValuePair(key,String.valueOf(params.get(key)));
				i++;
			}
			post.setRequestBody( pairs);
			httpClient.setConnectionTimeout(50000);
			httpClient.getHttpConnectionManager().getParams().setSoTimeout(50000);
			state = httpClient.executeMethod(post);
			if(state==HttpStatus.SC_OK){
				//result= post.getResponseBodyAsString();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						post.getResponseBodyAsStream()));
				StringBuffer stringBuffer = new StringBuffer();
				String str = "";
				while ((str = br.readLine()) != null) {
					stringBuffer.append(str);
				}
				String line = stringBuffer.toString();
				System.err.println(">>>>>>>>>>>>>>>>>>http doPost result:"+line);
				result=line;
			}
			post.releaseConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(post!=null)
				post.releaseConnection();
		}
		return result;
	}
}
