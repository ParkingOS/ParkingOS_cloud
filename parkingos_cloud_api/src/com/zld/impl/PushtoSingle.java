package com.zld.impl;

import java.io.IOException;

import javapns.back.PushNotificationManager;
import javapns.back.SSLConnectionHelper;
import javapns.data.Device;
import javapns.data.PayLoad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.APNTemplate;
import com.gexin.rp.sdk.template.TransmissionTemplate;
import com.zld.service.DataBaseService;
import com.zld.utils.TimeTools;
import com.zld.weixinpay.utils.util.JsonUtil;

/**
 * 推送消息工具(个推)
 * 
 * @author Administrator
 * 
 */

@Repository
public class PushtoSingle {
	@Autowired
	private DataBaseService daService;
	static String appId = "eDaq9HAQg76LPqTpgf3JW4";
	static String appkey = "AIk7nyJx9j5jjqLpUZDxB";
	static String master = "r6NPiypeui7ZeHhLUviHo";
	static String CID = "a5b36ad3351b5e44f5f06fc0eaf3835ce09a12ca0db6746d08d0ef9be6b98c6c";// 推送ID"11600db5ab9b688161273f3f21978125"
	static String host = "http://sdk.open.api.igexin.com/apiex.htm";

	public static void main(String[] args) throws Exception {
		/*
		 * IGtPush push = new IGtPush(host, appkey, master); push.connect();
		 * 
		 * TransmissionTemplate template = TransmissionTemplateDemo();
		 * SingleMessage message = new SingleMessage();
		 * message.setOffline(true); //离线有效时间，单位为毫秒，可选
		 * message.setOfflineExpireTime(24 * 3600 * 1000);
		 * message.setData(template);
		 * 
		 * // List targets = new ArrayList(); Target target1 = new Target(); //
		 * Target target2 = new Target();
		 * 
		 * target1.setAppId(appId); target1.setClientId(CID);
		 * 
		 * IPushResult ret = push.pushMessageToSingle(message, target1);
		 * System.out.println(ret.getResponse().toString());
		 */
		// /////////ios
		String mesgString = "{\"mtype\":\"0\",\"msgid\":\"1\",\"info\":{\"total\":\"0.0\",\"parkname\":\"加密车场\",\"address\":\"北京市海淀区上地三街9号-d座\",\"etime\":\"1414218585\",\"state\":\"0\",\"btime\":\"1414218585\",\"parkid\":\"1475\",\"orderid\":\"176729\"}}";
		String mesg = "{\"mtype\":\"0\",\"info\":{\"total\":\"0.00\",\"parkname\":\"加密车场\",\"etime\":\"1414224048\",\"orderid\":\"176733\"}}";
		IGtPush p = new IGtPush(host, appkey, master);
		APNTemplate template = new APNTemplate();
		// template.setPushInfo("", 1, mesg, "defalut");
		String mtype = JsonUtil.getJsonValue(mesg, "mtype");
		template.setPushInfo("", 1, "zldtext", "defalut", mesg, "", "",
				"default");
		SingleMessage SingleMessage = new SingleMessage();
		SingleMessage.setData(template);
		// 单推
		IPushResult ret = p.pushAPNMessageToSingle(appId, CID, SingleMessage);
		System.out.println(ret.getResponse());
		// //////////ios

		// System.out.println(AjaxUtil.decodeUTF8("%E8%AE%A98%E7%82%B9%E8%A1%A560%25 "));
	}

	public static TransmissionTemplate TransmissionTemplateDemo() {
		TransmissionTemplate template = new TransmissionTemplate();
		template.setAppId(appId);
		template.setAppkey(appkey);
		template.setTransmissionType(1);
		// template.setTransmissionContent("{\"time\":\"2\",\"info\":\"消息测试\"}");
		template.setPushInfo(
				"",
				4,
				"消息测试--"
						+ TimeTools.getTime_yyyyMMdd_HHmmss(System
								.currentTimeMillis()), "default");

		return template;
	}

	// 发送消息android
	public String sendSingle(String cid, String mesg) {
		IGtPush push = new IGtPush(host, appkey, master);
		try {
			push.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		TransmissionTemplate template = new TransmissionTemplate();
		template.setAppId(appId);
		template.setAppkey(appkey);
		template.setTransmissionType(2);
		template.setTransmissionContent(mesg);

		SingleMessage message = new SingleMessage();
		message.setOffline(true);
		// 离线有效时间，单位为毫秒，可选
		message.setOfflineExpireTime(10 * 60 * 1000);
		message.setData(template);
		// List targets = new ArrayList();
		Target target1 = new Target();
		// Target target2 = new Target();

		target1.setAppId(appId);
		target1.setClientId(cid);
		System.err.println(">>>>>>>>>>>>>>>cid:" + cid + "  >>>" + mesg);
		IPushResult ret = push.pushMessageToSingle(message, target1);
		System.err.println("send message>>>>>>>>>>>>>>>>>"
				+ ret.getResponse().toString());
		return ret.getResponse().toString();
	}

	// 发送消息ios
	public String sendIOSmessage(Long uid, String cid, String mesg) {
		/*
		 * IGtPush p = new IGtPush(host, appkey, master); APNTemplate template =
		 * new APNTemplate(); template.setPushInfo("", 1, mesg, "defalut");
		 * 
		 * SingleMessage SingleMessage = new SingleMessage();
		 * SingleMessage.setData(template); //单推 IPushResult ret =
		 * p.pushAPNMessageToSingle(appId, cid, SingleMessage);
		 * System.err.println(ret.getResponse());
		 */

		String mtype = JsonUtil.getJsonValue(mesg, "mtype");
		// 0:订单消息，1：车位预定消息 2:充值购买产品
		String message = "订单消息";
		if (mtype.equals("1")) {
			message = "车位预定消息 ";
		} else if (mtype.equals("2")) {
			message = "充值购买产品 ";
		}// seq_message_tb
		Long msgId = daService.getLong(
				"SELECT nextval('seq_message_tb'::REGCLASS) AS newid", null);
		daService
				.update("insert into message_tb (id,type,create_time,state,content,uin) values(?,?,?,?,?,?)",
						new Object[] { msgId, 1,
								System.currentTimeMillis() / 1000, 0, mesg, uid });
		IGtPush p = new IGtPush(host, appkey, master);
		APNTemplate template = new APNTemplate();

		// template.setPushInfo("mjjioje", 1, mesg, "default");
		try {
			template.setPushInfo("", 1, message, "defalut", msgId + "", "", "",
					"default");
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.err.println(">>>>>>>>>>>>>>>cid:" + cid + "  >>>" + mesg);

		SingleMessage SingleMessage = new SingleMessage();

		SingleMessage.setData(template);
		// 单推
		IPushResult ret = p.pushAPNMessageToSingle(appId, cid, SingleMessage);
		System.out.println(ret.getResponse());
		return ret.getResponse().toString();
	}

	// 向苹果消息服务器发消息
	public void sendMessageByApns(Long uid, String mesg, String cid) {
		boolean isEnterprise= false;
		if(cid.startsWith("E_")){
			isEnterprise=true;
			cid= cid.substring(2);
		}
		
		String filePath = getClass().getResource("/").getPath();
		System.out.println(">>>>>>>>>>>>>cid:"+cid+",message:"+mesg);
		String mtype = JsonUtil.getJsonValue(mesg, "mtype");
		
		// 0:订单消息，1：车位预定消息 2:充值购买产品
		String message = "订单消息";
		if (mtype.equals("1")) {
			message = "车位预定消息 ";
		} else if (mtype.equals("2")) {
			message = "充值购买产品 ";
		}// seq_message_tb
		Long msgId = daService.getLong(
				"SELECT nextval('seq_message_tb'::REGCLASS) AS newid", null);
		daService.update("insert into message_tb (id,type,create_time,state,content,uin) values(?,?,?,?,?,?)",
						new Object[] { msgId, 1,System.currentTimeMillis() / 1000, 0, mesg, uid });
		try {
			//String deviceToken = "b9ae03f915a1f74d2281bd81109b4e3fbd417a6cfbd4f579f892b37b0f25ed69";
			//"a5b36ad3351b5e44f5f06fc0eaf3835ce09a12ca0db6746d08d0ef9be6b98c6c"
			//"b9ae03f915a1f74d2281bd81109b4e3fbd417a6cfbd4f579f892b37b0f25ed69"
			String deviceToken = cid;//"b9ae03f915a1f74d2281bd81109b4e3fbd417a6cfbd4f579f892b37b0f25ed69";
			PayLoad payLoad = new PayLoad();
			payLoad.addAlert(message);
			payLoad.addBadge(1);
			payLoad.addSound("default");
			payLoad.addCustomDictionary("payload", msgId+"");

			PushNotificationManager pushManager = PushNotificationManager.getInstance();
			//pushManager.removeDevice("iPhone");
			String device = ""+System.currentTimeMillis();
			pushManager.addDevice(device, deviceToken);
			
			String host = "gateway.push.apple.com"; // 苹果推送服务器
			// String host= "gateway.sandbox.push.apple.com"; //测试用的苹果推送服务器
			int port = 2195;
			
			String certificatePath = filePath + "apns-dev-cert.p12"; // 刚才在mac系统下导出的证书
			if(isEnterprise)//企业版，调用不同的证书
				certificatePath = filePath + "apns-dev-cert_enterprise.p12"; // 刚才在mac系统下导出的证书
			System.out.println(">>>>>>>>>>>>>>>"+certificatePath);
			String certificatePassword = "tingchebao";
			pushManager.initializeConnection(host, port, certificatePath,
					certificatePassword,SSLConnectionHelper.KEYSTORE_TYPE_PKCS12);
			// Send Push
			Device client = pushManager.getDevice(device);
			pushManager.sendNotification(client, payLoad);
			pushManager.stopConnection();
			pushManager.removeDevice(device);
			System.out.println(">>>>>>>>>>>>>>>>>>>>>push ios message succeed!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("e.getMessage() = " + e.getMessage());
		}
	}
}
