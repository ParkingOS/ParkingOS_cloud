package com.zld.utils;

import com.zld.AjaxUtil;
import org.apache.log4j.Logger;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;


public class HttpsProxy {
	private static final String METHOD_POST = "POST";
	private static final String DEFAULT_CHARSET = "utf-8";
	static Logger logger = Logger.getLogger(HttpsProxy.class);

	public static String doPost(String url, String params,
								String charset, int connectTimeout, int readTimeout) throws Exception {
		String ctype = "application/json;charset=" + charset;
		byte[] content = {};
		if(params != null){
			content = params.getBytes(charset);
		}

		return doPost(url, ctype, content, connectTimeout, readTimeout);
	}
	public static String doPost(String url, String ctype, byte[] content,
								int connectTimeout,int readTimeout) throws Exception {
		HttpsURLConnection conn = null;
		OutputStream out = null;
		String rsp = null;
		try {
			try{
				SSLContext ctx = SSLContext.getInstance("TLS");
				ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()},
						new SecureRandom());
				SSLContext.setDefault(ctx);

				conn = getConnection(new URL(url), METHOD_POST, ctype);
				conn.setHostnameVerifier(new HostnameVerifier() {
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				});
				conn.setConnectTimeout(connectTimeout);
				conn.setReadTimeout(readTimeout);
			}catch(Exception e){
				logger.error("GET_CONNECTOIN_ERROR, URL = " + url, e);
				throw e;
			}
			try{
				out = conn.getOutputStream();
				out.write(content);
				rsp = getResponseAsString(conn);
			}catch(IOException e){
				logger.error("REQUEST_RESPONSE_ERROR, URL = " + url, e);
				throw e;
			}

		}finally {
			if (out != null) {
				out.close();
			}
			if (conn != null) {
				conn.disconnect();
			}
		}

		return rsp;
	}
	private static class DefaultTrustManager implements javax.net.ssl.X509TrustManager {

		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

	}

	private static HttpsURLConnection getConnection(URL url, String method, String ctype)
			throws IOException {
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		conn.setRequestMethod(method);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "text/xml,text/javascript,text/html");
		conn.setRequestProperty("User-Agent", "stargate");
		conn.setRequestProperty("Content-Type", ctype);
		return conn;
	}

	protected static String getResponseAsString(HttpURLConnection conn) throws IOException {
		String charset = getResponseCharset(conn.getContentType());
		InputStream es = conn.getErrorStream();
		if (es == null) {
			return getStreamAsString(conn.getInputStream(), charset);
		} else {
			String msg = getStreamAsString(es, charset);
			if (msg==null||"".equals(msg)) {
				throw new IOException(conn.getResponseCode() + ":" + conn.getResponseMessage());
			} else {
				throw new IOException(msg);
			}
		}
	}

	private static String getStreamAsString(InputStream stream, String charset) throws IOException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, charset));
			StringWriter writer = new StringWriter();

			char[] chars = new char[256];
			int count = 0;
			while ((count = reader.read(chars)) > 0) {
				writer.write(chars, 0, count);
			}

			return writer.toString();
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}

	private static String getResponseCharset(String ctype) {
		String charset = DEFAULT_CHARSET;

		if (ctype!=null&&!"".equals(ctype)) {
			String[] params = ctype.split(";");
			for (String param : params) {
				param = param.trim();
				if (param.startsWith("charset")) {
					String[] pair = param.split("=", 2);
					if (pair.length == 2) {
						if (pair[1]!=null&&!"".equals(pair[1])) {
							charset = pair[1].trim();
						}
					}
					break;
				}
			}
		}

		return charset;
	}
	public static void main(String[] args) {
//    	addpark();
//    	updatepark();
//    	querypark();
//    	adduser();
//    	updateuser();
		addorder();
		updateorder();
	}
	private static void updateorder(){
		String url = "https://127.0.0.1/api-web/order/updateorder";
//		String url = "https://s.bolink.club/unionapi/order/updateorder";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("order_id", 1007);
		paramMap.put("type",1);//1结算订单  2修改订单金额
		paramMap.put("pay_time", 1484494350);
		paramMap.put("end_time", 1484494350);
		paramMap.put("total", 2);
		paramMap.put("union_id", 200002);
		paramMap.put("rand", Math.random());
		String ret = "";
		try {
			String linkParams = StringUtils.createLinkString(paramMap);
			System.err.println(linkParams);//"WWDD6EFE78E11111"
			String sign =StringUtils.MD5(linkParams+"key=WWDD6EFE78E11111").toUpperCase();
			paramMap.put("sign", sign);
			String param = StringUtils.createJson(paramMap);
			ret = HttpsProxy.doPost(url, param, "utf-8", 20000, 20000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println(ret);
	}
	private static void addorder(){
		String url = "https://127.0.0.1/api-web/order/addorder";
//		String url = "https://s.bolink.club/unionapi/order/addorder";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("order_id", 1007);//"京G9E2R9"
		paramMap.put("plate_number", "京G962R9");
		paramMap.put("start_time", 1486979807);
		paramMap.put("record_time", 1486979807);
		paramMap.put("park_id", "10005");
		paramMap.put("union_id", 200002);
		paramMap.put("rand", Math.random());
		String ret = "";
		try {
			String linkParams = StringUtils.createLinkString(paramMap);
			System.err.println(linkParams);
			String sign =StringUtils.MD5(linkParams+"key=WWDD6EFE78E11111").toUpperCase();
			paramMap.put("sign", sign);
			String param = StringUtils.createJson(paramMap);
			ret = HttpsProxy.doPost(url, param, "utf-8", 20000, 20000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println(ret);
	}
	private static void updateuser(){
		String url = "https://127.0.0.1/api-web/user/updateuser";
		//String url = "https://123.59.42.34/unionapi/user/updateuser";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("user_id","hxrrr1006");
		paramMap.put("type",1);
		paramMap.put("balance", 15.9);
		paramMap.put("union_id", 200002);
		paramMap.put("rand",Math.random());
		String ret = "";
		try {
			String linkParams = StringUtils.createLinkString(paramMap);
			System.err.println(linkParams);
			String sign =StringUtils.MD5(linkParams+"key=WWDD6EFE78E1E9F8").toUpperCase();
			paramMap.put("sign", sign);
			String param = StringUtils.createJson(paramMap);
			ret = HttpsProxy.doPost(url, param, "utf-8", 20000, 20000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println(ret);
	}

	private static void adduser(){
		String url = "https://127.0.0.1/api-web/user/adduser";
//		String url = "https://123.59.42.34/unionapi/user/adduser";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("user_id", "2001223");
		paramMap.put("plate_number", "京G962R0");
		paramMap.put("balance", 55.0);
		paramMap.put("rand", Math.random());
		paramMap.put("union_id", 200001);
		String ret = "";
		try {
			String linkParams = StringUtils.createLinkString(paramMap);
			System.err.println(linkParams);
			String sign =StringUtils.MD5(linkParams+"key=EA2D90FEEF1E9F8E").toUpperCase();
			paramMap.put("sign", sign);
			String param = StringUtils.createJson(paramMap);
			ret = HttpsProxy.doPost(url, param, "utf-8", 20000, 20000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println(ret);
	}
	private static void querypark(){
		String url = "https://127.0.0.1/api-web/park/querypark";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("lng", "123.949889");
		paramMap.put("lat", "32.466666");
		paramMap.put("union_id", 200001);
		paramMap.put("distance", 1000);
		paramMap.put("rand", Math.random());
		String ret = "";//200001 EA2D90FEEF1E9F8E"
		try {
			String linkParams = StringUtils.createLinkString(paramMap);
			System.err.println(linkParams);
			String sign =StringUtils.MD5(linkParams+"key=EA2D90FEEF1E9F8E").toUpperCase();
			paramMap.put("sign", sign);
			String param = StringUtils.createJson(paramMap);
			ret = HttpsProxy.doPost(url, param, "utf-8", 20000, 20000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ret  = AjaxUtil.decodeUTF8(ret);
		System.err.println(ret);
	}


	private static void updatepark(){
		String url = "https://127.0.0.1/api-web/park/updatepark";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("park_id", 10003);
		paramMap.put("type", 1);//1上传余位数，2删除车场 3恢复车场
		paramMap.put("empty_plot", 40);
		paramMap.put("union_id", 200001);
		paramMap.put("rand", Math.random());
		String ret = "";
		try {
			String linkParams = StringUtils.createLinkString(paramMap);
			System.err.println(linkParams);//"EA2D90FEEF1E9F8E"
			String sign =StringUtils.MD5(linkParams+"key=EA2D90FEEF1E9F8E").toUpperCase();
			paramMap.put("sign", sign);
			String param = StringUtils.createJson(paramMap);
			ret = HttpsProxy.doPost(url, param, "utf-8", 20000, 20000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println(ret);
	}


	private static void addpark(){
		String url = "https://127.0.0.1/api-web/park/addpark";
//		String url = "https://123.59.42.34/unionapi/park/addpark";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("park_id", 10007);
		paramMap.put("name", "北京上地三街9号院停车场");
		paramMap.put("address", "北京上地三街9号");
		paramMap.put("phone", "13899884433");
		paramMap.put("lng", "123.946839");
		paramMap.put("lat", "32.464176");
		paramMap.put("total_plot", 90);
		paramMap.put("empty_plot", 56);
		paramMap.put("union_id", 200002);
		paramMap.put("server_id", 800004);
		paramMap.put("rand", Math.random());
		String ret = "";
		try {
			System.err.println(paramMap);
			String linkParams = StringUtils.createLinkString(paramMap);
			System.err.println(linkParams);
			String sign =StringUtils.MD5(linkParams+"key=WWDD6EFE78E1E9F8").toUpperCase();
			System.err.println(sign);
			paramMap.put("sign", sign);
			//param = DesUtils.encrypt(param,"NQ0eSXs720170114");
			String param = StringUtils.createJson(paramMap);
			System.err.println(param);
			ret = HttpsProxy.doPost(url, param, "utf-8", 20000, 20000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println(ret);
	}
}
