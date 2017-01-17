package com.tq.zld.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpClientHelper {
	String serverURL = null;
	String message = null;
	HttpGet httpGet = null;
	HttpClient httpClient = null;
	URL url = null;
	public String respData = null;

	public String getRespData() {
		return this.respData;
	}

	public void setRespData(String resp) {
		this.respData = resp;
	}

	public HttpClientHelper(String url) {
		serverURL = url;
		try {
			this.url = new URL(serverURL);
			LogUtils.i(HttpClientHelper.class, "start url pattern: --->> "
					+ url);
		} catch (MalformedURLException e) {
			LogUtils.i(HttpClientHelper.class, "MalformedURLException");
		}
	}

	public String getRequestURL() {
		return serverURL;
	}

	public void sendHttpRequest() {
		LogUtils.i(HttpClientHelper.class, "sendHttpRequest() start");
		try {
			httpGet = new HttpGet(URLEncoder.encode(getRequestURL(), "utf-8"));
			// httpGet = new HttpGet(getRequestURL());
		} catch (Exception e) {
			LogUtils.i(HttpClientHelper.class, e.getClass().toString());
			// e.printStackTrace();
		}
		LogUtils.i(HttpClientHelper.class,
				"httpGet = new HttpGet(getRequestURL())");
		httpClient = new DefaultHttpClient();
		LogUtils.i(HttpClientHelper.class,
				"httpClient = new DefaultHttpClient()");
		HttpResponse httpResp = null;
		try {
			httpResp = httpClient.execute(httpGet);
			// httpGet.set
			LogUtils.i(HttpClientHelper.class, "httpResp excuted");
			respData = getHttpResponse(httpResp).toString();
			LogUtils.i(HttpClientHelper.class, "respData:" + respData);
		} catch (ClientProtocolException e) {
			LogUtils.i(HttpClientHelper.class, "ClientProtocolException()");
		} catch (IOException e) {
			LogUtils.i(HttpClientHelper.class, "IOException()");
		}
	}

	private String getHttpResponse(HttpResponse resp) {
		if (resp == null) {
			LogUtils.i(HttpClientHelper.class, "respData: null");
			return null;
		}
		HttpEntity httpEntity = resp.getEntity();
		String result = "";
		String line = "";

		try {
			InputStream inputStream = httpEntity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));

			while ((line = reader.readLine()) != null) {
				result += line;
			}
			LogUtils.i(HttpClientHelper.class, "httpHelper - result respdata: "
					+ result);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Toast.makeText(context, result + "", Toast.LENGTH_LONG).show();
		return result;
	}

	public void reqUuidValidate(String uuid) {
		String url = "http://s.zhenlaidian.com/zld/ibeaconhandle.do?action=regibc&uuid=";
		url.concat("uuid:uuid:uuid:uuid:uuid:uuid:uuid");
	}

	public void getUuidValidateResp(String statecCode) {

		// get Uuid validated information from server
		String url = "";
		// stateCode
	}

	public void repParkingInfo(long parkingId) {
		// get parking information
	}

	public void getParkingInfo() {
		// get parking information
	}

	public void reqMapData(double lati, double longi) {
		// get location data,
	}

	public void getMapDataRDesponse() {
		//
	}

	public void getPaymentDetail() {
		//
	}

}
