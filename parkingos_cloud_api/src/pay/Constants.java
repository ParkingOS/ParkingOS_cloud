package pay;

public class Constants {
	// ----------------------微信支付Key--------------------------------------
	//----------------------停车宝--------------------------------------
	public static final String WXPAY_APPID = PayConfigDefind.getValue("WXPAY_APPID");
	public static final String WXPAY_PARTNERID = PayConfigDefind.getValue("WXPAY_PARTNERID");
	public static final String WXPAY_APPSECRET = PayConfigDefind.getValue("WXPAY_APPSECRET");
	public static final String WXPAY_PARTNERKEY = PayConfigDefind.getValue("WXPAY_PARTNERKEY");
	public static final String WXPAY_APPKEY = PayConfigDefind.getValue("WXPAY_APPKEY");

	public static final String WXPAY_GETTOKEN_URL = PayConfigDefind.getValue("WXPAY_GETTOKEN_URL")+"&appid="+WXPAY_APPID+"&secret=" + WXPAY_APPSECRET;//"https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
			//+ WXPAY_APPID + "&secret=" + WXPAY_APPSECRET;
	public static final String WXPAY_GETPREPAYID_URL =PayConfigDefind.getValue("WXPAY_GETPREPAYID_URL");// "https://api.weixin.qq.com/pay/genprepay";
	


	public static  String WXPUBLIC_APPID =PayConfigDefind.getValue("WXPUBLIC_APPID");
	public static  String WXPUBLIC_SECRET = PayConfigDefind.getValue("WXPUBLIC_SECRET");
	public static  String WXPUBLIC_REDIRECTURL =PayConfigDefind.getValue("WXPUBLIC_REDIRECTURL");
	
	public static  String LOCAL_NAME =PayConfigDefind.getValue("LOCAL_NAME");// "zld";
	
//	public static  String WXPUBLIC_REDIRECTURL = "192.168.199.239";
//	public static  String LOCAL_NAME = "zldi";
	
	public static  String WXPUBLIC_S_DOMAIN = PayConfigDefind.getValue("WXPUBLIC_S_DOMAIN");
	
	public static final String WXPUBLIC_MCH_ID =PayConfigDefind.getValue("WXPUBLIC_MCH_ID");
	public static final String WXPUBLIC_APPKEY =PayConfigDefind.getValue("WXPUBLIC_APPKEY");
	//获取access_token
	public static String WXPUBLIC_GETTOKEN_URL =PayConfigDefind.getValue("WXPUBLIC_GETTOKEN_URL")+"&appid="+ WXPUBLIC_APPID + "&secret=" + WXPUBLIC_SECRET;//"https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
			//+ WXPUBLIC_APPID + "&secret=" + WXPUBLIC_SECRET;
	//统一支付接口
	public static String WXPUBLIC_UNIFIEDORDER = PayConfigDefind.getValue("WXPUBLIC_UNIFIEDORDER");
	//通知地址
	public static String WXPUBLIC_NOTIFY_URL =PayConfigDefind.getValue("WXPUBLIC_NOTIFY_URL");
	
	//退款地址
	public static String WXPUBLIC_BACK_URL =PayConfigDefind.getValue("WXPUBLIC_BACK_URL");
	
	//通知地址(测试)
//	public static final String WXPUBLIC_NOTIFY_URL = "http://wang151068941.oicp.net/zld/wxphandle";
	
	public static String WXPUBLIC_SUCCESS_NOTIFYMSG_ID = "dhyfJiJAhe8iZE39HD2m5U--_ynhrlrgA";//订单支付成功
	
	public static String WXPUBLIC_FAIL_NOTIFYMSG_ID = "-H0";//订单支付失败
	
	public static String WXPUBLIC_BONUS_NOTIFYMSG_ID = "";//打赏通知
	//未付款订单通知
	public static String WXPUBLIC_ORDER_NOTIFYMSG_ID = "";
	
	public static String WXPUBLIC_BACK_NOTIFYMSG_ID = "-ejXVs31B0lMn42ftpN8";//退款
	
	public static String WXPUBLIC_TICKET_ID = "";//获得代金券通知
	
	public static String WXPUBLIC_AUDITRESULT_ID = "DP2IHNX-";//审核结果通知
	
	public static String WXPUBLIC_FLYGMAMEMESG_ID = "";//名片交换通知，打灰机加好机友
	
	public static String WXPUBLIC_LEAVE_MESG_ID = "IS-";//留言
	
	public static class ShowMsgActivity {
		public static final String STitle = "showmsg_title";
		public static final String SMessage = "showmsg_message";
		public static final String BAThumbData = "showmsg_thumb_data";
	}
}
