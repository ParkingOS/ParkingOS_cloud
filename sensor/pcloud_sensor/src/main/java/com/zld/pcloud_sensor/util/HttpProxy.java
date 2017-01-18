package com.zld.pcloud_sensor.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
//参考http://blog.csdn.net/catoop/article/details/50352334
public class HttpProxy {
	private static CloseableHttpClient httpClient;
	private static RequestConfig defaultRequestConfig;
	private final static Object syncLock = new Byte[]{};
	
	public static CloseableHttpClient getHttpClient(){
		if(httpClient != null){
			return httpClient;
		}
		synchronized (syncLock) {
			if(httpClient == null){
				httpClient = createHttpClient();
			}
		}
		return httpClient;
	}
	
	public static CloseableHttpClient createHttpClient(){
		//HttpURLConnection不支持连接池
		//连接池
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		//设置连接池的最大连接数
		cm.setMaxTotal(200);
		//参考：http://blog.csdn.net/shootyou/article/details/6415248
		//每个路由(route)最大连接数,这里route的概念可以理解为 运行环境机器 到 目标机器的一条线路。
		//举例来说，我们使用HttpClient的实现来分别请求 www.baidu.com 的资源和 www.bing.com 
		//的资源那么他就会产生两个route。这里为什么要特别提到route最大连接数这个参数呢，因为这个参数的默认值为2，
		//如果不设置这个参数值默认情况下对于同一个目标机器的最大并发连接只有2个！
		//这意味着如果你正在执行一个针对某一台目标机器的抓取任务的时候，哪怕你设置连接池的最大连接数为200，
		//但是实际上还是只有2个连接在工作，其他剩余的198个连接都在等待，都是为别的目标机器服务的。
		//该值设置过小会报ConnectionPoolTimeoutException: Timeout waiting for connection from pool
		cm.setDefaultMaxPerRoute(200);
		/*设置单个路由的最大连接数量
		HttpHost localhost = new HttpHost("locahost", 80);
	    cm.setMaxPerRoute(new HttpRoute(localhost), 80);//对本机80端口的socket连接上限是80*/
		defaultRequestConfig = RequestConfig.custom()
				  // 设置读取超时
				  .setSocketTimeout(5000)
				  // 设置连接超时
				  .setConnectTimeout(5000)
				  // 设置从连接池获取连接实例的超时  
				  .setConnectionRequestTimeout(5000)
				  // 在提交请求之前 测试连接是否可用
				  .setStaleConnectionCheckEnabled(true)
				  .build();
		HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
			
			@Override
			public boolean retryRequest(IOException exception, 
					int executionCount, HttpContext context) {
				if (executionCount >= 2) {//如果已经重试了2次，就放弃
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof InterruptedIOException) {// 超时
                    return false;
                }
                if (exception instanceof UnknownHostException) {// 目标服务器不可达
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                    return false;
                }
                HttpClientContext clientContext = HttpClientContext
                        .adapt(context);
                HttpRequest request = clientContext.getRequest();
                //如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
				return false;
			}
		};
		httpClient = HttpClients.custom()
			    .setDefaultRequestConfig(defaultRequestConfig)
			    .setConnectionManager(cm).setRetryHandler(retryHandler)
			    .build();
		return httpClient;
	}
	
	public static String get(String url){
		HttpGet httpGet = new HttpGet(url);
		InputStream inputStream = null;
		try {
			//参考：http://blog.csdn.net/bhq2010/article/details/9210007
			//设置request header也是很重要的，比如设置User-Agent可以将抓取程序伪装成浏览器，
			//骗过一些网站对爬虫的检查，设置Accept-Encoding为gzip可以建议站点以压缩格式传输数据、节省带宽等等
			httpGet.addHeader("Accept", "text/html");  
			httpGet.addHeader("Accept-Charset", "utf-8");  
			httpGet.addHeader("Accept-Encoding", "gzip");  
			httpGet.addHeader("Accept-Language", "en-US,en");  
			httpGet.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.160 Safari/537.22"); 
			HttpResponse response = getHttpClient().execute(httpGet);
			inputStream = response.getEntity().getContent();
			if(inputStream != null){
				String result = IOUtils.toString(inputStream);
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				inputStream.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * POST 请求，返回字符
	 * @param url
	 * @param params
	 * @return
	 */
	public static String post(String url, Map<String,String> params){
		HttpPost httpost = new HttpPost(url);
		InputStream inputStream = null;
		try {
			setPostParams(httpost, params);
			HttpResponse response = getHttpClient().execute(httpost);
			inputStream = response.getEntity().getContent();
			if(inputStream != null){
				String result = IOUtils.toString(inputStream);
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
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
