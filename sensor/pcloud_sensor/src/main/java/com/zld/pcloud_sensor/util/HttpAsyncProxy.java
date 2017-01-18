package com.zld.pcloud_sensor.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.reactor.ConnectingIOReactor;

import com.zld.pcloud_sensor.handler.IHandler;


public class HttpAsyncProxy {
	private static CloseableHttpAsyncClient httpClient;
	private static RequestConfig defaultRequestConfig;
	private final static Object syncLock = new Byte[]{};
	
	public static CloseableHttpAsyncClient getHttpClient(){
		if(httpClient != null){
			return httpClient;
		}
		synchronized (syncLock) {//对象锁，防并发
			if(httpClient == null){//防止重复创建对象
				httpClient = createHttpClient();
			}
		}
		return httpClient;
	}
	
	public static CloseableHttpAsyncClient createHttpClient(){
		try {
			//连接池
			IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
					.setConnectTimeout(5000)
					.setSoKeepAlive(true)
					.build();
			ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
			PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
			//设置连接池的最大连接数
			cm.setMaxTotal(150);
			cm.setDefaultMaxPerRoute(150);
			
			defaultRequestConfig = RequestConfig.custom()
					  // 设置读取超时
					  .setSocketTimeout(5000)//为了保持长连接关闭该参数，设置该参数会在5秒后关闭该连接
					  // 连接超时
					  .setConnectTimeout(5000)
					  // 设置从连接池获取连接实例的超时 
					  .setConnectionRequestTimeout(5000)
					  // 在提交请求之前 测试连接是否可用  
					  .setStaleConnectionCheckEnabled(true)
					  .build();
			httpClient = HttpAsyncClients.custom()
					.setConnectionManager(cm)
					.setDefaultRequestConfig(defaultRequestConfig)
					.build();
			httpClient.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return httpClient;
	}
	
	public static void get(String url, final IHandler handler){
		HttpGet httpGet = new HttpGet(url);
		try { 
			getHttpClient().execute(httpGet, new FutureCallback<HttpResponse>(){
				@Override
				public void completed(HttpResponse result) {
					InputStream inputStream = null;
					try {
						inputStream = result.getEntity().getContent();
						if(inputStream != null){
							String respBody = IOUtils.toString(inputStream);
							handler.completed(respBody);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							if(inputStream != null){
								inputStream.close();
							}
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				}

				@Override
				public void failed(Exception ex) {
					handler.failed(ex);
				}

				@Override
				public void cancelled() {
					handler.cancelled();
				}
				
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * POST 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String post(String url, Map<String, String> params, final IHandler handler){
		HttpPost httpost = new HttpPost(url);
		try {
			setPostParams(httpost, params);
			getHttpClient().execute(httpost, new FutureCallback<HttpResponse>(){
				@Override
				public void completed(HttpResponse result) {
					InputStream inputStream = null;
					try {
						inputStream = result.getEntity().getContent();
						if(inputStream != null){
							String respBody = IOUtils.toString(inputStream);
							handler.completed(respBody);
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							if(inputStream != null){
								inputStream.close();
							}
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				}

				@Override
				public void failed(Exception ex) {
					handler.failed(ex);
				}

				@Override
				public void cancelled() {
					handler.cancelled();
				}
				
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void setPostParams(HttpPost httpost,
            Map<String, String> params) {
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for (String key : params.keySet()) {
            nvps.add(new BasicNameValuePair(key, params.get(key)));
        }
        try {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
