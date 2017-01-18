package com.zld.pcloud_sensor;


public class Constants {
	public static String SNESOR_SIGN = "PCLOUD-SENSOR-SERVER";//xmemcache加一个头部标识，防止重复
	
	public static String TB_CLIENT = "TB_CLIENT";//天泊通信通道标识
	
	public static String TB_ADDR = "139.196.229.89";//天泊服务地址
	
	public static int TB_PORT = 6001;//天泊服务端口
	
	public static int SENSOR_PORT = 780;//车检器中间件服务地址
	
	public static String DOMAIN = "180.150.188.224:8080";
	
	public static String SITE_HEART_URL = "http://" + DOMAIN + "/zld/api/hdinfo/InsertTransmitter";//基站心跳接口
	
	public static String SENSOR_HEART_URL = "http://" + DOMAIN + "/zld/api/hdinfo/InsertSensor";//车检器心跳接口
	
	public static String SENSOR_IN_CAR = "http://" + DOMAIN + "/zld/api/hdinfo/InsertCarAdmission";//车检器监测到有车接口
	
	public static String SENSOR_OUT_CAR = "http://" + DOMAIN + "/zld/api/hdinfo/InsertCarEntrance";//车检器监测无车接口
}
