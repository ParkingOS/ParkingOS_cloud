package com.zld.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;

import pay.AlipayUtil;

import com.zld.AjaxUtil;
import com.zld.CustomDefind;

public class HttpProxy {
	static Logger logger = Logger.getLogger(HttpProxy.class);
	
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
			}else {
				System.err.println(method.getResponseBodyAsString());
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
		  //  System.out.println(state);
			if(state==HttpStatus.SC_OK){
				result= post.getResponseBodyAsString();
			}else {
				result = state+"";
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
	

	
	public  String doTeldPost(String url,String hearder,String content){
		HttpClient httpClient = new HttpClient();
		PostMethod post = new PostMethod(url);
		int state = 0;
		String result = "";
		try {
		    httpClient.setConnectionTimeout(1000*20);
		    httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY), new UsernamePasswordCredentials("Bearer", hearder));
//		    httpClient.getState().setCredentials(
//		            new AuthScope("api.wyqcd.cn", 8004, "realm"),
//		            new UsernamePasswordCredentials("username", "password")
//		        );
		    httpClient.getParams().setAuthenticationPreemptive(true);
		    Header header = new Header("Authorization", "Bearer "+hearder);
		    post.addRequestHeader(header);
		    post.setDoAuthentication(true);
		    post.addRequestHeader("Content-Type", "application/json;charset=utf-8");
		   // post.addParameter("content", content);
		    RequestEntity requestEntity = new StringRequestEntity(content);
		    post.setRequestEntity(requestEntity);
		    state = httpClient.executeMethod(post);
		  //  System.out.println(state);
			if(state==HttpStatus.SC_OK){
				result= post.getResponseBodyAsString();
			}else {
				result = state+"";
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
	
	public static void main(String[] args) {
		//compay();
		//cominfo();
		//user();
		//plot();
		//etc();
		//hd();
		queryPark();
		//gjsit();
		//getToken();
//		getSite();
		//testbus();
		//insertsenor();
		//callweixin();
	}
	private static void callweixin(){
		String url = "http://service.yzjttcgs.com/zld/api/weixincallback/notify";
		Map<String, String> paramsMap = new HashMap<String, String>();
		//paramsMap.put("CarOutTime", "2016-04-19 17:34:22");
		paramsMap.put("CarInTime", "2016-04-19 17:34:22");
		paramsMap.put("Indicate", "23");
		System.out.println(new HttpProxy().doPost(url,paramsMap));
	}
	private static void insertsenor(){
		String url = "http://180.150.188.224:8080/zld/api/hdinfo/InsertCarAdmission";
		//String url = "http://180.150.188.224:8080/zld/api/hdinfo/InsertCarEntrance";
		Map<String, String> paramsMap = new HashMap<String, String>();
		//paramsMap.put("CarOutTime", "2016-04-19 17:34:22");
		paramsMap.put("CarInTime", "2016-04-19 17:34:22");
		paramsMap.put("Indicate", "23");
		//paramsMap.put("SensorNumber", "A000000915");//A000000915 A000000911
		paramsMap.put("SensorNumber", "A000000911");
		System.out.println(new HttpProxy().doPost(url,paramsMap));
		
//		String url = "http://127.0.0.1/zld/api/hdinfo/InsertCarEntrance";
//		Map<String, String> paramsMap = new HashMap<String, String>();
//		paramsMap.put("CarOutTime", "2016-04-19 17:37:22");
//		paramsMap.put("Indicate", "1111");
//		paramsMap.put("SensorNumber", "5006000302");//5006000302 A008008A10 A007005A09
//		System.out.println(new HttpProxy().doPost(url,paramsMap));
	}
	
	private static void testbus() {
//		String url ="http://127.0.0.1/zld/api/bus/";
//		Map<String, String> paramsMap = new HashMap<String, String>();
//		paramsMap.put("action", "getnearbystatinfo");
//		paramsMap.put("lng", "119.394150");
//		paramsMap.put("lat", "32.387352");
//		paramsMap.put("range", "200");
//		System.out.println(doPost(url,paramsMap));
		
//		String url ="http://127.0.0.1/zld/api/third/bus";
//		Map<String, String> paramsMap = new HashMap<String, String>();
//		paramsMap.put("action", "getstatbyid");
//		paramsMap.put("routeid", "-1");
//		paramsMap.put("stationid", "103593");
//		System.out.println(doPost(url,paramsMap));
		
//		String url ="http://127.0.0.1/zld/api/third/bus";
//		Map<String, String> paramsMap = new HashMap<String, String>();
//		paramsMap.put("action", "getstatbyname");
//		paramsMap.put("stationname",AjaxUtil.encodeUTF8("月亮"));
//		System.out.println(doPost(url,paramsMap));
		
//		String url ="http://127.0.0.1/zld/api/third/bus";
//		Map<String, String> paramsMap = new HashMap<String, String>();
//		paramsMap.put("action", "routestatdata");
//		paramsMap.put("routeid","2023");
//		paramsMap.put("time","1459134636");
//		System.out.println(doPost(url,paramsMap));
		
//		String url ="http://121.40.130.8/zld/api/third/bike";
//		Map<String, String> paramsMap = new HashMap<String, String>();
//		paramsMap.put("action", "queryall");
//		paramsMap.put("lng", "119.394150");
//		paramsMap.put("lat", "32.387352");
//		System.out.println(doPost(url,paramsMap));
		
//		String url ="http://127.0.0.1/zld/api/third/bike";
//		Map<String, String> paramsMap = new HashMap<String, String>();
//		paramsMap.put("action", "detail");
//		paramsMap.put("id", "118");
//		System.out.println(doPost(url,paramsMap));
		
		String url ="http://121.40.136.236/FindCar/FindCarService.asmx";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("parkId", "123");
		paramsMap.put("startId", "1");
		paramsMap.put("endId", "9");
		System.out.println(new HttpProxy().doPost(url,paramsMap));
		
	}
	
	//查询充电桩信息
	private static void getSite(){
		//String url ="http://api.wyqcd.cn:8004/api/Sta/PostSta";
		String url ="http://open.teld.cn/api/Sta/PostSta";
		//String url ="http://127.0.0.1/zld/carinter.do?action=RouteStatData";
		String signKey = "15JBfEs6QnRPiLMlN3SXZrLq9UvXXdY7";
		String token ="5lNrkNSfaYOdWukxnwT30oYjZh-D9MRA0GyBOZbNv5B7BJVppFrbrfHPrk0Tu3ZGVSk5c4wqy__4laCayl5T3ELMGPecN8l87TmT5j5j4h2XCksGU_-bsRUlXYVVISdOI4eoDudEQxyDDC4URP9Gz_lohsiDZasryE51DHEMEbc7GZh4wfoqMl1M2Fs5nUCLhrov0Tx5wduQxHpoItZ15uOG-o6z8bH5St0iMi8lkkgge8fk";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("province","江苏");
		paramsMap.put("city","扬州");
		paramsMap.put("region", "");
		paramsMap.put("type","" );
		paramsMap.put("opState", "");
		paramsMap.put("pageNo", "1");
		paramsMap.put("pageSize", "100");
		String linkedParam=StringUtils.createLinkedJson(paramsMap,1);
		System.out.println(linkedParam);
		String sign =null;
		try {
			String signedStr = "requestMsg="+linkedParam+signKey;
			System.out.println(signedStr);
			sign= StringUtils.MD5(new String(signedStr.getBytes("utf-8"))).toLowerCase();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("sign:"+sign);
		String queryDes="";
		try {
			queryDes = new String(ZldDesUtils.encrypt(linkedParam));
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("queryDes:"+queryDes);
		String queryParam = "{\"requestMsg\":\""+queryDes+"\",\"sign\":\""+sign+"\"}";
		System.out.println("queryparams:"+queryParam);
		System.out.println(new HttpProxy().doTeldPost(url, token, queryParam));
		//76b549e3eb69b5d3c4f08ea018137626
		//t5mgBt8c0VldHN5aZ3IU/DcihCWs0A2uB8fgDM6KBAH1bMXGHjo4fR+ydNTFvo67tNCIx699VDWuS4a+EegHharGTxBTrO0c
		//{"requestMsg":"t5mgBt8c0VldHN5aZ3IU/DcihCWs0A2uB8fgDM6KBAH1bMXGHjo4fR+ydNTFvo67tNCIx699VDWuS4a+EegHharGTxBTrO0c","sign":"76b549e3eb69b5d3c4f08ea018137626"}
		//{"requestMsg":"t5mgBt8c0VldHN5aZ3IU/DcihCWs0A2uB8fgDM6KBAH1bMXGHjo4fR+ydNTFvo67tNCIx699VDWuS4a+EegHharGTxBTrO0c","sign":"76b549e3eb69b5d3c4f08ea018137626"}
	}
	//充电桩接口测试 
	private static void getToken(){
		//String url ="http://api.wyqcd.cn:8004/OAuth/Token";
		String url ="http://open.teld.cn/OAuth/Token";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("grant_type", "client_credentials");
//		paramsMap.put("client_id", "teldtub1y1pdffh3svod");
//		paramsMap.put("client_secret", "qXid3bLHOi");
		paramsMap.put("client_id", "teldhh20cdbpb2umuocw");
		paramsMap.put("client_secret", "Y0iFg61V12");
		System.out.println(new HttpProxy().doPost(url,paramsMap));
	}
	//公交接口测试 
	private static void gjsit(){
		String time = TimeTools.gettime();
		//String url ="http://218.91.52.117:8999/BusService/Require_RouteStatData/?RouteID=107154&TimeStamp="+AjaxUtil.encodeUTF8(time);
		
		String url ="http://218.91.52.117:8999/BusService/QueryDetail_ByRouteID/?RouteID=111101&SegmentID=100210";
		System.out.println(url);
		System.out.println(new HttpProxy().doGet(url));
	}
	//查询车场
	private static void queryPark(){
		String url ="http://s.tingchebao.com/zld/api/parkinfo/queryPark";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("local", "beijing");
		paramsMap.put("time", "1462032000");
		System.out.println(new HttpProxy().doPost(url,paramsMap));
	}
	//车位
	private static void hd(){
		String url ="http://121.40.130.8/zld/api/hdinfo/uploadhd";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("parkingNo", "568f60458bbfe44143eac3a77");
		paramsMap.put("type", "10");
		paramsMap.put("plot_id", "A10001");
		paramsMap.put("status", "1");
		paramsMap.put("chanid", "1008");
		paramsMap.put("operTime", "1456137299");
		paramsMap.put("operate", "0");
		paramsMap.put("token", "5794f6adb44a6751304cdece77d8fba2");
		String params = AlipayUtil.createLinkString(paramsMap);
		System.out.println("<<<<<<"+params);
		try {
			paramsMap.put("sign", StringUtils.MD5(params+"key="+CustomDefind.getValue("RSA1008")).toUpperCase());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(new HttpProxy().doPost(url,paramsMap));
	}
	//车位
	private static void etc(){
		String url ="http://121.40.130.8/zld/api/baseinfo/uploadetc";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("uuid", "568f60458bbfe44143eac3a11");
		paramsMap.put("car_number", AjaxUtil.encodeUTF8("苏K30894"));
		paramsMap.put("card_id", "10001");
		paramsMap.put("name", AjaxUtil.encodeUTF8("张三丰"));
		paramsMap.put("mobile", "1356137299");
		paramsMap.put("balance", "59.25");
		paramsMap.put("chanid", "1008");
		paramsMap.put("operate", "2");
		paramsMap.put("token", "5794f6adb44a6751304cdece77d8fba2");
		String params = AlipayUtil.createLinkString(paramsMap);
		System.out.println("<<<<<<"+params);
		try {
			paramsMap.put("sign", StringUtils.MD5(params+"key="+CustomDefind.getValue("RSA1008")).toUpperCase());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(new HttpProxy().doPost(url,paramsMap));
	}
	//收费员
	private static void user(){
		String url ="http://121.40.130.8/zld/api/baseinfo/uploadparkuser";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("uuid", "568f60458bbfe44143eac3a11");
		paramsMap.put("chanid", "1008");
		paramsMap.put("company_id", "1009");
		paramsMap.put("reg_time", "1456137299");
		paramsMap.put("mobile", "1356137299");
		paramsMap.put("sex", "0");
		paramsMap.put("nickname", AjaxUtil.encodeUTF8("张三丰"));
		paramsMap.put("email", "ywo@yeh.net");
		paramsMap.put("md5pass", "568f60458bbfe44133");
		paramsMap.put("token", "5794f6adb44a6751304cdece77d8fba2");
		paramsMap.put("operate", "0");
		String params = AlipayUtil.createLinkString(paramsMap);
		System.out.println("<<<<<<"+params);
		try {
			paramsMap.put("sign", StringUtils.MD5(params+"key="+CustomDefind.getValue("RSA1008")).toUpperCase());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(new HttpProxy().doPost(url,paramsMap));
	}
	//车位
	private static void plot(){
		String url ="http://222.222.221.189/zld/api/business/uploadparkstatus";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("parkingNo", "202600000000000000000000000000000000");
		paramsMap.put("berth", "568f22");
		paramsMap.put("token", "16bc44fd8ee8bb379396c5d16e25ed58");
		paramsMap.put("chanid", "1008");
		paramsMap.put("operate", "1");
		String params = AlipayUtil.createLinkString(paramsMap);
		System.out.println("<<<<<<"+params);
		try {
			paramsMap.put("sign", StringUtils.MD5(params+"key="+CustomDefind.getValue("RSA1008")).toUpperCase());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(new HttpProxy().doPost(url,paramsMap));
	}
	
	//运营公司
	private static void compay(){
		String url ="http://121.40.130.8/zld/api/baseinfo/uploadcompany";
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("company_name", AjaxUtil.encodeUTF8("扬州中学停车场3"));
		paramsMap.put("corporation", AjaxUtil.encodeUTF8("方式联系方"));
		paramsMap.put("phone", "051855668899");
		paramsMap.put("address", AjaxUtil.encodeUTF8("扬州中学3"));
		paramsMap.put("create_time", "1456137299");
		paramsMap.put("chanid", "1008");
		paramsMap.put("city_merchants_id", "10000");
		paramsMap.put("uuid", "568f60458bbfe44143eac3a71");
		paramsMap.put("token", "72cb0524ddaa7e430b8c96aea89b199a");
		paramsMap.put("update_time", "1456137299");
		paramsMap.put("operate", "0");
		String params = AlipayUtil.createLinkString(paramsMap);
		System.out.println("<<<<<<"+params);
		try {
			paramsMap.put("sign", StringUtils.MD5(params+"key="+CustomDefind.getValue("RSA1008")).toUpperCase());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(new HttpProxy().doPost(url,paramsMap));
	}
	//停车场
	private static void cominfo(){
		String url ="http://yxiudongyeahnet.vicp.cc/zld/api/baseinfo/uploadpark";
		Map<String, String> paramsMap = new HashMap<String, String>();
		//address=测试测试&chanid=1008&city_merchants_id=321000&company_id=1009
		//&create_time=1457693090&gps=119.91753,93.1778040&operate=0&parkingName=asd&parkingNo=35rf788
		//&parkingType=1&parking_total=20&state=0&timeStamp=1457693090&token=29f5bf9e4134492d5cd8d937e4870c9e
		paramsMap.put("parkingNo", "568f60458bbfe44143eac3a77");
		paramsMap.put("parkingName", AjaxUtil.encodeUTF8("扬州中学停车场3"));
		paramsMap.put("phone", "051855668899");
		paramsMap.put("fax", "051855668899");
		paramsMap.put("address", AjaxUtil.encodeUTF8("测试测试"));
		paramsMap.put("zipcode", "321000");
		paramsMap.put("homepage", AjaxUtil.encodeUTF8("http://www.tinchebao.com"));
		paramsMap.put("remarks", AjaxUtil.encodeUTF8("测试一下"));
		paramsMap.put("gps", "145.613705,45.6137299");
		paramsMap.put("parkingType", "1");
		paramsMap.put("parking_total", "1000");
		paramsMap.put("share_number", "800");
		paramsMap.put("mobile", "1356137299");
		paramsMap.put("mcompany", AjaxUtil.encodeUTF8("扬州物业1"));
		paramsMap.put("type", "1");
		paramsMap.put("stop_type", "1");
		paramsMap.put("state", "0");
		paramsMap.put("city", "321000");
		paramsMap.put("record_number", "ICP00000");
		paramsMap.put("chanid", "1008");
		paramsMap.put("city_merchants_id", "10000");
		paramsMap.put("company_id", "1009");
		paramsMap.put("create_time", "1456137299");
		paramsMap.put("update_time", "1456137288");
		paramsMap.put("operate", "0");
		paramsMap.put("token", "5794f6adb44a6751304cdece77d8fba2");
		//paramsMap.put("update_time", "1456137299");
		//paramsMap.put("lon", "145.613729");
		String params = AlipayUtil.createLinkString(paramsMap);
		System.out.println("1:"+params);
		try {
			paramsMap.put("sign", StringUtils.MD5(params+"key="+CustomDefind.getValue("RSA1008")).toUpperCase());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(new HttpProxy().doPost(url,paramsMap));
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
	
}
