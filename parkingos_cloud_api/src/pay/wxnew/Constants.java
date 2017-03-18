package pay.wxnew;

public class Constants {
	// ----------------------微信支付Key--------------------------------------
	//----------------------停车宝--------------------------------------
//	public static final String WXPAY_APPID = "wx73454d7f61f862a5";
//	public static final String WXPAY_PARTNERID = "1220886701";
//	public static final String WXPAY_APPSECRET = "b3e563822a872e5a37eb692a856ed4ba";
//	public static final String WXPAY_PARTNERKEY = "d7b993008827a203659ca008372382fe";
//	public static final String WXPAY_APPKEY = "kkyTJVjVnxOYDh7hhRQinfxWyIYEDBSRSm72VZWvKyxwpczPjODQpqRHdy3JIHuVJKjlJS0UwaINGBx5HziqfERh0W8tQ3v0aXmheFLIzscSdBA0vrkjkLeoyOffA2PW";
	//----------------------宜行扬州--------------------------------------
	public static final String WXPAY_APPID = "wx485c58b62cbb4dd0";
	public static final String WXPAY_PARTNERID = "1332937401";
	public static final String WXPAY_APPSECRET = "3f3374185d25f417c0f8bc16805b51cf";
	public static final String WXPAY_PARTNERKEY = "982e0572wp19e17886201fmip1e9q8nz";
	public static final String WXPAY_APPKEY = "DB8632F07CB1A54D4D899635F7BEB934";
	
	public static final String WXPAY_GETTOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
			+ WXPAY_APPID + "&secret=" + WXPAY_APPSECRET;
	public static final String WXPAY_GETPREPAYID_URL = "https://api.weixin.qq.com/pay/genprepay";
	
	
}
