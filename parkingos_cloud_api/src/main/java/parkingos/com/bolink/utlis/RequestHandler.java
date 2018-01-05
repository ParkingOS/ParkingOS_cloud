package parkingos.com.bolink.utlis;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jdom.JDOMException;
import parkingos.com.bolink.utlis.weixinpay.utils.MD5Util;
import parkingos.com.bolink.utlis.weixinpay.utils.TenpayHttpClient;
import parkingos.com.bolink.utlis.weixinpay.utils.TenpayUtil;
import parkingos.com.bolink.utlis.weixinpay.utils.XMLUtil;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.*;


/*
 '微信支付服务器签名支付请求请求类
 '============================================================================
 'api说明：
 'init(app_id, app_secret, partner_key, app_key);
 '初始化函数，默认给一些参数赋值，如cmdno,date等。
 'setKey(key_)'设置商户密钥
 'getLasterrCode(),获取最后错误号
 'GetToken();获取Token
 'getTokenReal();Token过期后实时获取Token
 'createMd5Sign(signParams);生成Md5签名
 'genPackage(packageParams);获取package包
 'createSHA1Sign(signParams);创建签名SHA1
 'sendPrepay(packageParams);提交预支付
 'getDebugInfo(),获取debug信息
 '============================================================================
 '*/
public class RequestHandler {
	/** Token获取网关地址地址 */
	private String tokenUrl;
	/** 预支付网关url地址 */
	private String gateUrl;
	/** 查询支付通知网关URL */
	private String notifyUrl;
	/** 商户参数 */
	private String appid;
	private String appkey;
	private String partnerkey;
	private String appsecret;
	private String key;
	/** 请求的参数 */
	private SortedMap parameters;
	/** Token */
	private String Token;
	private String charset;
	/** debug信息 */
	private String debugInfo;
	private String last_errcode;

	private HttpServletRequest request;

	private HttpServletResponse response;

	/**
	 * 初始构造函数。
	 *
	 * @return
	 */
	public RequestHandler(HttpServletRequest request,
						  HttpServletResponse response) {
		this.last_errcode = "0";
		this.request = request;
		this.response = response;
		this.charset = "UTF-8";
		this.parameters = new TreeMap();
		// 获取Token网关
		tokenUrl = "https://api.weixin.qq.com/cgi-bin/token";
		// 提交预支付单网关
		gateUrl = "https://api.mch.weixin.qq.com/pay/unifiedorder";
		// 验证notify支付订单网关
		notifyUrl = "https://gw.tenpay.com/gateway/simpleverifynotifyid.xml";
	}

	/**
	 * 初始化函数。
	 */
	public void init(String app_id, String app_secret, String app_key,
					 String partner, String key) {
		this.last_errcode = "0";
		this.Token = "token_";
		this.debugInfo = "";
		this.appkey = app_key;
		this.appid = app_id;
		this.partnerkey = partner;
		this.appsecret = app_secret;
		this.key = key;
	}

	public void init() {
	}

	/**
	 * 获取最后错误号
	 */
	public String getLasterrCode() {
		return last_errcode;
	}

	/**
	 *获取入口地址,不包含参数值
	 */
	public String getGateUrl() {
		return gateUrl;
	}

	/**
	 * 获取参数值
	 *
	 * @param parameter
	 *            参数名称
	 * @return String
	 */
	public String getParameter(String parameter) {
		String s = (String) this.parameters.get(parameter);
		return (null == s) ? "" : s;
	}

	/**
	 * 设置密钥
	 */
	public void setKey(String key) {
		this.key = key;
	}



	// 特殊字符处理
	public String UrlEncode(String src) throws UnsupportedEncodingException {
		return URLEncoder.encode(src, this.charset).replace("+", "%20");
	}

	// 获取package带参数的签名包
	public String genPackage(SortedMap<String, String> packageParams)
			throws UnsupportedEncodingException {
		String sign = createSign(packageParams,"shenzhenboliankejiyouxiangongsis");
//test   shenzhenboliankejiyouxiangongsis
//		StringBuffer sb = new StringBuffer();
//		Set es = packageParams.entrySet();
//		Iterator it = es.iterator();
//		while (it.hasNext()) {
//			Map.Entry entry = (Map.Entry) it.next();
//			String k = (String) entry.getKey();
//			String v = (String) entry.getValue();
//			sb.append(k + "=" + UrlEncode(v) + "&");
//		}
//
//		// 去掉最后一个&
//		String packageValue = sb.append("sign=" + sign).toString();
//		System.out.println("packageValue=" + packageValue);
		String packageValue="<xml>";
		// <trade_type><![CDATA[APP]]></trade_type>
		for(String key : packageParams.keySet()){
			packageValue +="<"+key+"><![CDATA["+packageParams.get(key)+"]]></"+key+">";
		}
		packageValue +="<sign><![CDATA["+sign+"]]></sign></xml>";
		return packageValue;
	}

	/**
	 * 创建md5摘要,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 */
	public String createSign(SortedMap<String, String> packageParams) {
		StringBuffer sb = new StringBuffer();
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k)
					&& !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=d7b993008827a203659ca008372382fe");
		System.out.println("md5 sb:" + sb);
		String sign = MD5Util.MD5Encode(sb.toString(), this.charset)
				.toUpperCase();

		return sign;

	}
	/**
	 * 创建md5摘要,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 */
	public String createSign(SortedMap<String, String> packageParams,String privatekey) {
		StringBuffer sb = new StringBuffer();
		List<String> keys = new ArrayList<String>(packageParams.keySet());
		Collections.sort(keys);
		for(String key :keys) {
			String k = key;
			String v = packageParams.get(key);
			if (null != v && !"".equals(v) && !"sign".equals(k)
					&& !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" +privatekey);
		System.out.println("md5 sb:" + sb);
		String sign = MD5Util.MD5Encode(sb.toString(), this.charset)
				.toUpperCase();
		System.err.println("sign===========>>>>>"+sign);


		return sign;

	}
	/**
	 * 创建md5摘要,规则是:按参数名称a-z排序,遇到空值的参数不参加签名。
	 */
	public String _createSign(SortedMap<String, String> packageParams) {
		StringBuffer sb = new StringBuffer();
		List<String> keys = new ArrayList<String>(packageParams.keySet());
		Collections.sort(keys);
		for(String key :keys) {
			String k = key;
			String v = packageParams.get(key);
			if (null != v && !"".equals(v) && !"sign".equals(k)
					&& !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=d7b993008827a203659ca008372382fe");
		System.out.println("md5 sb:" + sb);
		String sign = MD5Util.MD5Encode(sb.toString(), this.charset)
				.toUpperCase();

		return sign;

	}


	// 提交预支付
	public String sendPrepay(String postData,String token) {
		String prepayid = "";
		// 转换成json
		Gson gson = new Gson();
		/* String postData =gson.toJson(packageParams); */
		/*String postData = "{";
		Set es = packageParams.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (k != "appkey") {
				if (postData.length() > 1)
					postData += ",";
				postData += "\"" + k + "\":\"" + v + "\"";
			}
		}
		postData += "}";*/
		// 设置链接参数
		String requestUrl = this.gateUrl + "?access_token=" + token;
		System.out.println("post url=" + requestUrl);
		System.out.println("post data=" + postData);
		TenpayHttpClient httpClient = new TenpayHttpClient();
		httpClient.setReqContent(requestUrl);
		String resContent = "";

		if (httpClient.callHttpPost(requestUrl, postData)) {
			try {
				resContent = httpClient.getResContent();
				Map<String, String> map = XMLUtil.doXMLParse(resContent);
				if ("SUCCESS".equals(map.get("return_code"))) {
					prepayid = map.get("prepay_id");
				} else {
					System.out.println("get prepayid err ,info =" + map);
				}
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 设置debug info
			System.out.println("res json=" + resContent);
		}
		return prepayid;
	}
	// 提交发送红包
	public String   sendBons(String url,String postData) {
		// 设置链接参数
		String requestUrl = url ;
		System.out.println("post url=" + requestUrl);
		System.out.println("post data=" + postData);
		TenpayHttpClient httpClient = new TenpayHttpClient();
		httpClient.setReqContent(requestUrl);
		String resContent =ssl(url,postData);
//		try {
//			resContent = httpClient.getResContent();
//			Map<String, String> map = XMLUtil.doXMLParse(resContent);
//			System.err.println(map);
//			//{total_amount=100, result_code=SUCCESS, mch_id=1481594592, mch_billno=20170918140353,
//			//err_code=SUCCESS, send_listid=1000041701201709183000098887266, wxappid=wxe9f12586b0e3b7d0,
//			//err_code_des=发放成功, return_msg=发放成功, re_openid=oWxRWwPddMAmsAlwZ5xw1oDViwfs, return_code=SUCCESS}
//		} catch( Exception e) {
//			e.printStackTrace();
//		}
		return resContent;
	}
	private String ssl(String url,String data){
		StringBuffer message = new StringBuffer();
		try {
			KeyStore keyStore  = KeyStore.getInstance("PKCS12");
			FileInputStream instream = new FileInputStream(new File("/data/jtom/webapps/zld/WEB-INF/classes/apiclient_cert.p12"));
			keyStore.load(instream, "1481594592".toCharArray());
			// Trust own CA and all self-signed certs   1480042232
			SSLContext sslcontext = SSLContexts.custom()
					.loadKeyMaterial(keyStore, "1481594592".toCharArray())
					.build();
			// Allow TLSv1 protocol only
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					sslcontext,
					new String[] { "TLSv1" },
					null,
					SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
			CloseableHttpClient httpclient = HttpClients.custom()
					.setSSLSocketFactory(sslsf)
					.build();
			HttpPost httpost = new HttpPost(url);

			httpost.addHeader("Connection", "keep-alive");
			httpost.addHeader("Accept", "*/*");
			httpost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			httpost.addHeader("Host", "api.mch.weixin.qq.com");
			httpost.addHeader("X-Requested-With", "XMLHttpRequest");
			httpost.addHeader("Cache-Control", "max-age=0");
			httpost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
			httpost.setEntity(new StringEntity(data, "UTF-8"));
			System.out.println("executing request" + httpost.getRequestLine());

			CloseableHttpResponse response = httpclient.execute(httpost);
			try {
				HttpEntity entity = response.getEntity();

				System.out.println("----------------------------------------");
				System.out.println(response.getStatusLine());
				if (entity != null) {
					System.out.println("Response content length: " + entity.getContentLength());
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(entity.getContent(),"UTF-8"));
					String text;
					while ((text = bufferedReader.readLine()) != null) {
						message.append(text);
					}

				}
				EntityUtils.consume(entity);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				response.close();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return message.toString();
	}
	/**
	 * 创建package签名
	 */
	public boolean createMd5Sign(String signParams) {
		StringBuffer sb = new StringBuffer();
		Set es = this.parameters.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			String v = (String) entry.getValue();
			if (!"sign".equals(k) && null != v && !"".equals(v)) {
				sb.append(k + "=" + v + "&");
			}
		}

		// 算出摘要
		String enc = TenpayUtil.getCharacterEncoding(this.request,
				this.response);
		String sign = MD5Util.MD5Encode(sb.toString(), enc).toLowerCase();

		String tenpaySign = this.getParameter("sign").toLowerCase();

		// debug信息
		this.setDebugInfo(sb.toString() + " => sign:" + sign + " tenpaySign:"
				+ tenpaySign);

		return tenpaySign.equals(sign);
	}

	/**
	 * 设置debug信息
	 */
	protected void setDebugInfo(String debugInfo) {
		this.debugInfo = debugInfo;
	}
	public void setPartnerkey(String partnerkey) {
		this.partnerkey = partnerkey;
	}
	public String getDebugInfo() {
		return debugInfo;
	}
	public String getKey() {
		return key;
	}

}
