package pay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlipayUtil {
	
	public static final String  SIGN_ALGORITHMS = "SHA1WithRSA";
	/** 
     * 除去数组中的空值和签名参数
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    public static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }

    /** 
     * 把数组所有元素排序，并按照“参数=参数值”的模式用“&”字符拼接成字符串
     * @param params 需要排序并参与字符拼接的参数组
     * @return 拼接后字符串
     */
    public static String createLinkString(Map<String, String> params) {

        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        String prestr = "";

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);

            if (i == keys.size() - 1) {//拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }

        return prestr;
    }
    
    /**
     * 支付宝消息验证地址
     */
    private static final String HTTPS_VERIFY_URL = "https://mapi.alipay.com/gateway.do?service=notify_verify&";

    /**
     * 验证消息是否是支付宝发出的合法消息
     * @param params 通知返回来的参数数组
     * @return 验证结果
     */
    public static boolean verify(Map<String, String> params) {

        //判断responsetTxt是否为true，isSign是否为true
        //responsetTxt的结果不是true，与服务器设置问题、合作身份者ID、notify_id一分钟失效有关
        //isSign不是true，与安全校验码、请求时的参数格式（如：带自定义参数等）、编码格式有关
    	String responseTxt = "true";
		if(params.get("notify_id") != null) {
			String notify_id = params.get("notify_id");
			responseTxt = verifyResponse(notify_id);
		}
	    String sign = "";
	    if(params.get("sign") != null) {
	    	sign = params.get("sign");
	    	
	    }
	    boolean isSign = getSignVeryfy(params, sign);

        //写日志记录（若要调试，请取消下面两行注释）
        //String sWord = "responseTxt=" + responseTxt + "\n isSign=" + isSign + "\n 返回回来的参数：" + AlipayCore.createLinkString(params);
	    //AlipayCore.logResult(sWord);

        if (isSign && responseTxt.equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 根据反馈回来的信息，生成签名结果
     * @param Params 通知返回来的参数数组
     * @param sign 比对的签名结果
     * @return 生成的签名结果
     */
	public static boolean getSignVeryfy(Map<String, String> Params, String sign) {
    	//过滤空值、sign与sign_type参数
    	Map<String, String> sParaNew =paraFilter(Params);
        //获取待签名字符串
        String preSignStr = createLinkString(sParaNew);
        //获得签名验证结果
       // System.err.println(preSignStr);
        boolean isSign = false;
        if(AlipayConfig.sign_type.equals("RSA")){
        	isSign = verify(preSignStr, sign, AlipayConfig.ali_public_key, AlipayConfig.input_charset);
        }
      //  System.err.println("alipay verify:"+isSign);
        return isSign;
    }
	  /**
     * 根据反馈回来的信息，生成签名结果 -----扫码支付的publickey不一样
     * @param Params 通知返回来的参数数组
     * @param sign 比对的签名结果
     * @return 生成的签名结果
     */
	public static boolean getQrSignVeryfy(Map<String, String> Params, String sign) {
    	//过滤空值、sign与sign_type参数
    	Map<String, String> sParaNew =paraFilter(Params);
        //获取待签名字符串
        String preSignStr = createLinkString(sParaNew);
        //获得签名验证结果
       //ystem.err.println("qr verify:"+preSignStr);
        boolean isSign = false;
        if(AlipayConfig.sign_type.equals("RSA")){
        	isSign = verify(preSignStr, sign, AlipayConfig.ALIPUBLICKEY4QR, AlipayConfig.input_charset);
        }
       // System.err.println("alipay verify:"+isSign);
        return isSign;
    }
    /**
    * 获取远程服务器ATN结果,验证返回URL
    * @param notify_id 通知校验ID
    * @return 服务器ATN结果
    * 验证结果集：
    * invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空 
    * true 返回正确信息
    * false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
    */
    private static String verifyResponse(String notify_id) {
        //获取远程服务器ATN结果，验证是否是支付宝服务器发来的请求

        String partner = AlipayConfig.partner;
        String veryfy_url = HTTPS_VERIFY_URL + "partner=" + partner + "&notify_id=" + notify_id;

        return checkUrl(veryfy_url);
    }
    /**
    * 获取远程服务器ATN结果
    * @param urlvalue 指定URL路径地址
    * @return 服务器ATN结果
    * 验证结果集：
    * invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空 
    * true 返回正确信息
    * false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
    */
    private static String checkUrl(String urlvalue) {
        String inputLine = "";

        try {
            URL url = new URL(urlvalue);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection
                .getInputStream()));
            inputLine = in.readLine().toString();
        } catch (Exception e) {
            e.printStackTrace();
            inputLine = "";
        }

        return inputLine;
    }
    
	/**
	* RSA验签名检查
	* @param content 待签名数据
	* @param sign 签名值
	* @param ali_public_key 支付宝公钥
	* @param input_charset 编码格式
	* @return 布尔值
	*/
	public static boolean verify(String content, String sign, String ali_public_key, String input_charset)
	{
		try 
		{
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	        byte[] encodedKey = Base64.decode(ali_public_key);
	        PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

		
			java.security.Signature signature = java.security.Signature
			.getInstance(SIGN_ALGORITHMS);
		
			signature.initVerify(pubKey);
			signature.update( content.getBytes(input_charset) );
		
			boolean bverify = signature.verify( Base64.decode(sign) );
			return bverify;
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	* RSA签名
	* @param content 待签名数据
	* @param privateKey 商户私钥
	* @param input_charset 编码格式
	* @return 签名值
	*/
	/*public static String sign(Map<String, String> parMap)
	{
		
        try 
        {
        	//content = new String(Base64.decode(content));
        	String content = "partner=\"2088411488582814\"&seller_id=\"caiwu@zhenlaidian.com\"&out_trade_no=\"041616213922193\"&subject=\"测试的商品\"&body=\"该测试商品的详细描述\"&total_fee=\"0.01\"&notify_url=\"http://service.yzjttcgs.com/zld/rechage\"&service=\"mobile.securitypay.pay\"&payment_type=\"1\"&_input_charset=\"utf-8\"&it_b_pay=\"30m\"&return_url=\"m.alipay.com\"";
        	String preSignStr = createLinkString(parMap);
        	PKCS8EncodedKeySpec priPKCS8 	= new PKCS8EncodedKeySpec( Base64.decode(AlipayConfig.private_key) ); 
        	KeyFactory keyf 				= KeyFactory.getInstance("RSA");
        	PrivateKey priKey 				= keyf.generatePrivate(priPKCS8);

            java.security.Signature signature = java.security.Signature
                .getInstance(SIGN_ALGORITHMS);

            signature.initSign(priKey);
            signature.update( content.getBytes(AlipayConfig.input_charset) );

            byte[] signed = signature.sign();
            
            return Base64.encode(signed);
        }
        catch (Exception e) 
        {
        	e.printStackTrace();
        }
        
        return null;
    }*/
	
	public static void main(String[] args) {
		
		/*
		 * buyer_id=2088702201663304
		trade_no=2014081250497230
		body=真来电（北京）移动科技有限公司
		use_coupon=N
		notify_time=2014-08-12 19:36:49
		subject=停车宝账户充值
		sign_type=RSA
		is_total_fee_adjust=Y
		notify_type=trade_status_sync
		out_trade_no=081219364012156
		trade_status=WAIT_BUYER_PAY
		discount=0.00
		sign=K5zNFe4y2pkhh2ORv+uRkQrQYobUkQR6hkhnkQzvpqUPjCJ8AV6g/WsaISE1Ilh+4iRvsJAL8OMdfVmkFCTbGgEjH/QzFmf+TzHEsaeYU9MljlqypmlIYvoL3muMF7cK+qJNP3SQplgesdWPA49G54ESb1zr/I2URXkd/Pi8XVM=
		gmt_create=2014-08-12 19:36:48
		buyer_email=ggchaifeng@gmail.com
		price=0.01
		total_fee=0.01
		seller_id=2088411488582814
		quantity=1
		seller_email=caiwu@zhenlaidian.com
		notify_id=c644ad6244f709f8893efa9f43c92dd93o
		payment_type=1
		 */
		Map<String, String> map = new HashMap<String, String>();
		map.put("buyer_id", "2088702201663304");
		map.put("trade_no", "2014081250497230");
		map.put("body", "真来电（北京）移动科技有限公司");
		map.put("use_coupon", "N");
		map.put("notify_time", "2014-08-12 19:36:49");
		map.put("subject", "停车宝账户充值");
		map.put("sign_type", "RSA");
		map.put("is_total_fee_adjust", "Y");
		map.put("notify_type", "trade_status_sync");
		map.put("out_trade_no", "081219364012156");
		map.put("trade_status", "WAIT_BUYER_PAY");
		map.put("discount", "0.00");
		map.put("sign", "K5zNFe4y2pkhh2ORv+uRkQrQYobUkQR6hkhnkQzvpqUPjCJ8AV6g/WsaISE1Ilh+4iRvsJAL8OMdfVmkFCTbGgEjH/QzFmf+TzHEsaeYU9MljlqypmlIYvoL3muMF7cK+qJNP3SQplgesdWPA49G54ESb1zr/I2URXkd/Pi8XVM=");
		map.put("gmt_create", "2014-08-12 19:36:48");
		map.put("buyer_email", "ggchaifeng@gmail.com");
		map.put("price", "0.01");
		map.put("total_fee", "0.01");
		map.put("seller_id", "2088411488582814");
		map.put("quantity", "1");
		map.put("seller_email", "caiwu@zhenlaidian.com");
		map.put("notify_id", "c644ad6244f709f8893efa9f43c92dd93o");
		map.put("payment_type", "1");
		String sign = "K5zNFe4y2pkhh2ORv+uRkQrQYobUkQR6hkhnkQzvpqUPjCJ8AV6g/WsaISE1Ilh+4iRvsJAL8OMdfVmkFCTbGgEjH/QzFmf+TzHEsaeYU9MljlqypmlIYvoL3muMF7cK+qJNP3SQplgesdWPA49G54ESb1zr/I2URXkd/Pi8XVM=";
		System.out.println(getSignVeryfy(map, sign));
	}
	
}
