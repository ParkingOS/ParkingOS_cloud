package com.zld.wxpublic.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pay.Constants;

import com.zld.AjaxUtil;
import com.zld.weixinpay.utils.util.MD5Util;
import com.zld.weixinpay.utils.util.Sha1Util;
import com.zld.weixinpay.utils.util.TenpayUtil;
import com.zld.weixinpay.utils.util.XMLUtil;



public class PayCommonUtil {
	private static Logger log = LoggerFactory.getLogger(PayCommonUtil.class);
	public static String CreateNoncestr(int length) {
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String res = "";
		for (int i = 0; i < length; i++) {
			Random rd = new Random();
			res += chars.indexOf(rd.nextInt(chars.length() - 1));
		}
		return res;
	}

	public static String CreateNoncestr() {
		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String res = "";
		for (int i = 0; i < 16; i++) {
			Random rd = new Random();
			res += chars.charAt(rd.nextInt(chars.length() - 1));
		}
		return res;
	}
	/**
	 * @Description：sign签名
	 * @param characterEncoding 编码格式
	 * @param parameters 请求参数
	 * @return
	 */
	public static String createSign(String characterEncoding,SortedMap<Object,Object> parameters){
		StringBuffer sb = new StringBuffer();
		Set es = parameters.entrySet();
		Iterator it = es.iterator();
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String k = (String)entry.getKey();
			Object v = entry.getValue();
			if(null != v && !"".equals(v) 
					&& !"sign".equals(k) && !"key".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		sb.append("key=" + Constants.WXPUBLIC_APPKEY);
		String sign = MD5Util.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
		return sign;
	}
	/**
	 * @Description：将请求参数转换为xml格式的string
	 * @param parameters  请求参数
	 * @return
	 */
	public static String getRequestXml(SortedMap<Object,Object> parameters){
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		Set es = parameters.entrySet();
		Iterator it = es.iterator();
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String k = (String)entry.getKey();
			String v = (String)entry.getValue();
			if ("attach".equalsIgnoreCase(k)||"body".equalsIgnoreCase(k)||"sign".equalsIgnoreCase(k)) {
				sb.append("<"+k+">"+"<![CDATA["+v+"]]></"+k+">");
			}else {
				sb.append("<"+k+">"+v+"</"+k+">");
			}
		}
		sb.append("</xml>");
		return sb.toString();
	}
	/**
	 * @Description：返回给微信的参数
	 * @param return_code 返回编码
	 * @param return_msg  返回信息
	 * @return
	 */
	public static String setXML(String return_code, String return_msg) {
		return "<xml><return_code><![CDATA[" + return_code
				+ "]]></return_code><return_msg><![CDATA[" + return_msg
				+ "]]></return_msg></xml>";
	}
	
	/**
	 * 返回处理结果给财付通服务器。
	 * 
	 * @param msg
	 * Success or fail
	 * @throws IOException
	 */
	public static void sendToCFT(String msg, HttpServletResponse response) throws IOException {
		String strHtml = msg;
		PrintWriter out = response.getWriter();
		out.println(strHtml);
		out.flush();
		out.close();

	}
	
	/*
     * 获取JS-SDK使用权限签名
     */
    public static Map<String, String> sign(String jsapi_ticket, String url) {
        Map<String, String> ret = new HashMap<String, String>();
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String string1;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsapi_ticket +
                  "&noncestr=" + nonce_str +
                  "&timestamp=" + timestamp +
                  "&url=" + url;
        System.out.println(string1);

        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        
        ret.put("url", url);
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);

        return ret;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
    
    /*
	 * @Description：获取JSAPI支付参数
	 * @param addressip 订单生成的机器IP
	 * @param fee 金额
	 * @param body 商品描述
	 * @param attach 附加数据
	 * @param openid 用户标识
	 */
	public static SortedMap<Object, Object> getPayParams(String addressip,
			Double fee, String body, String attach, String openid)
			throws Exception {
		// 当前时间 yyyyMMddHHmmss
		String currTime = TenpayUtil.getCurrTime();
		// 8位日期
		String strTime = currTime.substring(8, currTime.length());
		// 四位随机数
		String strRandom = TenpayUtil.buildRandom(4) + "";
		// 10位序列号,可以自行调整。
		String strReq = strTime + strRandom;
		// 订单号，此处用时间加随机数生成，商户根据自己情况调整，只要保持全局唯一就行
		String out_trade_no = strReq;
		// 设置package订单参数
		SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
		packageParams.put("appid", Constants.WXPUBLIC_APPID);
		packageParams.put("mch_id", Constants.WXPUBLIC_MCH_ID); // 设置商户号
		packageParams.put("nonce_str", PayCommonUtil.CreateNoncestr());
		body = AjaxUtil.decodeUTF8(body);
		packageParams.put("body", body); // 商品描述
		packageParams.put("out_trade_no", out_trade_no); // 商户订单号
		packageParams.put("total_fee", String.valueOf(Integer.parseInt(new java.text.DecimalFormat("0").format(fee*100)))); // 商品总金额,以分为单位
		packageParams.put("spbill_create_ip",addressip);
		packageParams.put("notify_url",Constants.WXPUBLIC_NOTIFY_URL); // 通知地址
		packageParams.put("trade_type", "JSAPI");
		packageParams.put("openid", openid);
		
		packageParams.put("attach", attach);
		String sign = PayCommonUtil.createSign("UTF-8", packageParams);
		packageParams.put("sign", sign);
		
		String requestXML = PayCommonUtil.getRequestXml(packageParams);
		String result =CommonUtil.httpsRequest(Constants.WXPUBLIC_UNIFIEDORDER, "POST", requestXML);

		Map<String, String> map = XMLUtil.doXMLParse(result);//解析微信返回的信息，以Map形式存储便于取值
		SortedMap<Object,Object> params = new TreeMap<Object,Object>();
		String timestamp = Sha1Util.getTimeStamp();
		params.put("appId", Constants.WXPUBLIC_APPID);
        params.put("timeStamp", timestamp);
        params.put("nonceStr", PayCommonUtil.CreateNoncestr());
        params.put("package", "prepay_id="+map.get("prepay_id"));
        params.put("signType", "MD5");
        String paySign = PayCommonUtil.createSign("UTF-8", params);
        params.put("packageValue", "prepay_id="+map.get("prepay_id"));//这里用packageValue是预防package是关键字在js获取值出错
        params.put("paySign", paySign);//paySign的生成规则和Sign的生成规则一致
		return params;
	}
	
	/*
	 * @Description：发送模板消息
	 * @param msg 消息
	 * @param accesstoken
	 */
	public static void sendMessage(String msg, String accesstoken){
		String sendUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accesstoken;
		String result = CommonUtil.httpsRequest(sendUrl, "POST", msg);
	}
}
