package com.zhenlaidian.bean;

import android.content.Context;

import com.zhenlaidian.util.SharedPreferencesUtils;

public class Config {

	
	public static String onlineurl = "http://s.tingchebao.com/zld/";
	public static String onlinemserver= "http://s.tingchebao.com/mserver/";
	//	http://180.150.188.224:8080/zld/ 线上测试服测试环境；
	
	public static  String getUrl(Context context){
		SharedPreferencesUtils utils = SharedPreferencesUtils.getIntance(context);
		String url = utils.getUrl();
		if (url.equals("1")) {
			onlineurl = "http://s.tingchebao.com/zld/";
//			onlineurl = "http://219.159.88.139/zld/";
//			onlineurl = "http://192.168.0.188/zld/";
//			onlineurl = "http://yxiudongyeahnet.vicp.cc:50803/zld/";
//			onlineurl = "https://s.bolink.club/zld/";
//			onlineurl = "http://180.150.188.224:8080/zld/";
//			onlineurl = "http://192.168.199.223:8080/zld/";
//			onlineurl = "http://192.168.199.122/zld/";
//			onlineurl = "http://192.168.199.239:80/zld/";
//			onlineurl = "http://192.168.1.229:80/zld/";
		}else if (url.equals("2")) {
//			onlineurl = "http://192.168.199.240/zld/";
			onlineurl = "http://yxiudongyeahnet.vicp.cc/zld/";
		}else if(url.equals("3")) {
			//王
			onlineurl = "http://192.168.199.239:80/zld/";
		}else if(url.equals("4")) {
			//戴
			onlineurl = "http://192.168.199.153:8088/zld/";
		}else if(url.equals("5")) {
			onlineurl = "http://180.150.188.224:8080/zld/";
		}
		return onlineurl;
	}
	
	public static String getMserver(Context context){
		SharedPreferencesUtils utils = SharedPreferencesUtils.getIntance(context);
		String url = utils.getUrl();
		if (url.equals("1")) {
			onlinemserver = "http://s.tingchebao.com/mserver/";
//			onlinemserver = "http://219.159.88.139/mserver/";
//			onlinemserver = "http://192.168.0.188/mserver/";
//			onlinemserver = "http://yxiudongyeahnet.vicp.cc:50803/mserver/";
//			onlinemserver = "https://s.bolink.club/mserver/";
//			onlinemserver = "http://180.150.188.224:8080/mserver/";
//			onlinemserver = "http://192.168.199.223:8080/mserver/";
//			onlinemserver = "http://192.168.199.239:80/mserver/";
//			onlinemserver = "http://192.168.199.122/mserver/";
//			onlinemserver = "http://192.168.1.229:80/mserver/";
		}else if (url.equals("2")) {
//			onlinemserver = "http://192.168.199.240/mserver/";
			onlineurl = "http://yxiudongyeahnet.vicp.cc/zld/";
		}else if (url.equals("3")) {
			onlinemserver = "http://192.168.199.239:80/mserver/";
		}else if (url.equals("4")) {
			onlinemserver = "http://192.168.199.153:8088/mserver/";
		}else if (url.equals("5")) {
			onlinemserver = "http://180.150.188.224:8080/mserver/";
		}
		return onlinemserver;
	}
}

