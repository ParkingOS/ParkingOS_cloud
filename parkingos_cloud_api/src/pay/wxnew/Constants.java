package pay.wxnew;

public class Constants {
	// ----------------------微信支付Key--------------------------------------

	//----------------------宜行扬州--------------------------------------
	public static final String WXPAY_APPID = "";
	public static final String WXPAY_PARTNERID = "";
	public static final String WXPAY_APPSECRET = "";
	public static final String WXPAY_PARTNERKEY = "";
	public static final String WXPAY_APPKEY = "";
	
	public static final String WXPAY_GETTOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
			+ WXPAY_APPID + "&secret=" + WXPAY_APPSECRET;
	public static final String WXPAY_GETPREPAYID_URL = "https://api.weixin.qq.com/pay/genprepay";
	
	
}
