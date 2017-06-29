package com.zld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ibatis.common.resources.Resources;

/**
 * 读取配置文件
 * @author Administrator
 *
 */
public class CustomDefind {

	
	Logger logger = Logger.getLogger(CustomDefind.class);
	//private static String PATH = ;
	public static Map<String, String> configMap = new HashMap<String, String>();
	
	static {
		load();
	}
	
	public static String CUSTOMPARKIDS = getValue("CUSTOMPARKIDS");
	public static String ISLOTTERY = getValue("ISLOTTERY");
	public static String MONGOADDRESS = getValue("MONGOADDRESS");
	public static String SENDTICKET = getValue("SENDTICKET");
	public static String PARKBACK = getValue("PARKBACK");
	public static String ETCPARK = getValue("ETCPARK");
	public static String LOCALMAXVERSION = getValue("LOCALMAXVERSION");
	public static String TASKTYPE = getValue("TASKTYPE");
	public static String WENLINPRICE = getValue("WENLINPRICE");//温岭价格	
	public static String taskdetail0 = getValue("TASKDETAIl0");
	public static String taskdetail1 = getValue("TASKDETAIl1");
	public static String taskdetail2 = getValue("TASKDETAIl2");
	public static String GLCOMIDS1 = getValue("GLCOMIDS1");
	public static String GLCOMIDS2 = getValue("GLCOMIDS2");
	public static String GLCOMIDS3 = getValue("GLCOMIDS3");
	public static String GROUP_CARD_USER = getValue("GROUP_CARD_USER");
	
	public static String ISUPTOUNION = getValue("ISUPTOUNION");//泊链平台地址
	public static String UNIONIP = getValue("UNIONIP");//泊链平台地址
	public static String UNIONID =getValue("UNIONID");//泊链平台账户
	public static String SERVERID = getValue("SERVERID");//泊链平台服务商号
	public static String UNIONKEY = getValue("UNIONKEY");//泊链平台身份密钥
	public static String USERUPMONEY = getValue("USERUPMONEY");//车主在泊链平台的限额
	
	public static String PROFILE = getValue("PROFILE");//开发模式
	
	

	public static String getValue(String key){
		if(configMap.containsKey(key)){
			System.out.println(key+"="+configMap.get(key));
			return configMap.get(key);
		}else {
			return "";
		}
	}
	
	private static void load(){
		String fileName ="config.properties";
		Properties properties = new Properties();
		try {
			File file = Resources.getResourceAsFile(fileName);
			properties.load(new FileInputStream(file));
			for (Map.Entry<Object,Object> e : properties.entrySet()) {
				configMap.put((String) e.getKey(), (String) e.getValue());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void reSetConfig() {
		load();
	}
	
	public static Integer getUseMoney(Double totle,Integer type){
		//Map<Integer, Integer> totalTicketMap = new HashMap<Integer, Integer>();
		Double dfeeTop = Math.ceil(totle);
		Integer feeTop = dfeeTop.intValue();
		//普通券  X：车费金额满 (total) Y：可用券抵扣金额 (common_distotal) 算法：X=Y+2+Y/3 上限是uplimit
		//Double common_distotal = Math.ceil((feeTop - 2)*(3.0/4.0));//向上取整
		Double common_distotal = Math.floor((feeTop - 1)/3.0);//向上取整
		//Double common_distotal = Math.floor((feeTop - 1)/2.0);//向上取整
		if(common_distotal<0)
			return 0;
//		if(common_distotal>12)
//			return 12;
		if(type==0)
			return common_distotal.intValue();
		else {
			return Double.valueOf(Math.floor(3*totle+1)).intValue();
			//return Double.valueOf(Math.floor(totle+1+totle/1.0)).intValue();
			//return Double.valueOf(Math.floor(totle+2+totle/3.0)).intValue();
		}
	}
}
