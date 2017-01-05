package com.zld.struts.request;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import pay.Constants;
import pay.SecurityUtils;

import com.zld.AjaxUtil;
import com.zld.impl.MemcacheUtils;
import com.zld.utils.HttpProxy;
import com.zld.utils.RequestUtil;
import com.zld.weixinpay.utils.util.JsonUtil;
import com.zld.weixinpay.utils.util.RequestHandler;
import com.zld.weixinpay.utils.util.Sha1Util;

public class WeixinPrePayAction extends Action {
	
	private Logger logger = Logger.getLogger(WeixinPrePayAction.class);
	
	
	
	
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		//---------------------------------------------------------
		//'微信支付服务器签名支付请求示例，商户按照此文档进行开发即可
		//'服务端返回数据，App获取到数据后可直接调起微信支付
		
		///http://192.168.0.188/zld/wxpreorder.do?action=preorder&body=test&total_fee=100&attach=15801482643_1_1022_3_20140815
		///http://121.40.130.8/zld/wxpreorder.do?action=preorder&body=%E5%81%9C%E8%BD%A6%E5%AE%9D%E8%B4%A6%E6%88%B7%E5%85%85%E5%80%BC&total_fee=100&attach=15801482643_1_1022_3_20140815
		//---------------------------------------------------------
		String body =RequestUtil.getString(request, "body");
		body = AjaxUtil.decodeUTF8(body);
		String total_fee = RequestUtil.getString(request, "total_fee");
		//用户数据包，包含购买
		String attach =RequestUtil.getString(request, "attach");// "15801482643_1_1022_3_20140815";
		
		String timeStamp = System.currentTimeMillis()/1000+"";
		logger.error("weixin preorder,attach:"+attach+",body:"+body+",total_fee:"+total_fee+",timeStamp="+timeStamp);
		
		boolean debug = false;
		String  notifyUrl="http://s.tingchebao.com/zld/weixihandle";
		
		//String notifyUrl="http://service.yzjttcgs.com/zld/weixihandle";
		if(debug)
			notifyUrl="http://yxiudongyeahnet.vicp.cc/zld/weixihandle";
		//从缓存中取access_token,如果有且没有超过2小时，返回token，没有或已过期返回"notoken"
		String weixinToken = memcacheUtils.getWeixinToken();
		if(weixinToken.equals("notoken")){
			String url = Constants.WXPAY_GETTOKEN_URL;
			//从weixin接口取access_token
			String result = new HttpProxy().doGet(url);
			logger.error("access_token json:"+result);
			//{"access_token":"llllllll","aaa":"sdsfe}
			//取access_token
			weixinToken = JsonUtil.getJsonValue(result, "access_token");//result.substring(17,result.indexOf(",")-1);
			logger.error("access_token:"+weixinToken);
			//保存到缓存 
			memcacheUtils.setWeixinToken(weixinToken);
		}
		//---------------生成订单号 开始------------------------
		//当前时间 yyyyMMddHHmmss
		//String currTime = TenpayUtil.getCurrTime();
		//8位日期
		//String strTime = currTime.substring(8, currTime.length());
		//四位随机数
		//String strRandom = TenpayUtil.buildRandom(4) + "";
		//10位序列号,可以自行调整。
		//String strReq = strTime + strRandom;
		//订单号，此处用时间加随机数生成，商户根据自己情况调整，只要保持全局唯一就行
		//String out_trade_no = strReq;
		//---------------生成订单号 结束------------------------

		//获取提交的商品价格
		//String order_price = request.getParameter("order_price");
		//获取提交的商品名称
		//String product_name = request.getParameter("product_name");

		TreeMap<String, String> outParams = new TreeMap<String, String>();

		RequestHandler reqHandler = new RequestHandler(request, response);
		//TenpayHttpClient httpClient = new TenpayHttpClient();
	    //初始化 
		//reqHandler.init();
		//'reqHandler.init(app_id, app_secret, app_key, partner, partner_key);

		//获取token值 
		//String token = reqHandler.GetToken();
		if (!"".equals(weixinToken)) {
			//=========================
			//生成预支付单
			//=========================
			//设置package订单参数
			
			String out_trade_no =  SecurityUtils.md5(System.currentTimeMillis()+""+new Random().nextInt(10000000));
			
			SortedMap<String, String> packageParams = new TreeMap<String, String>();
			packageParams.put("attach",attach);
			packageParams.put("bank_type", "WX"); //商品描述   
			packageParams.put("body", body); //商品描述   
			packageParams.put("fee_type", "1"); //币种，1人民币   66
			packageParams.put("input_charset", "UTF-8"); //字符编码
			packageParams.put("notify_url", notifyUrl); //接收财付通通知的URL  
			packageParams.put("out_trade_no", out_trade_no); //商家订单号  
			packageParams.put("partner", Constants.WXPAY_PARTNERID); //商户号    
			packageParams.put("spbill_create_ip", request.getRemoteAddr()); //订单生成的机器IP，指用户浏览器端IP  
			packageParams.put("total_fee",  String.valueOf((new BigDecimal(total_fee)
							.multiply(new BigDecimal(100))).intValue())); //商品金额,以分为单位  

			//获取package包
			String packageValue = reqHandler.genPackage(packageParams);

			String noncestr = Sha1Util.getNonceStr();
			//String timestamp = Sha1Util.getTimeStamp();
			String traceid= 15375242041L+ timeStamp;

			//设置支付参数
			SortedMap<String, String> signParams = new TreeMap<String, String>();
			signParams.put("appid", Constants.WXPAY_APPID);
			signParams.put("appkey", Constants.WXPAY_APPKEY);
			signParams.put("noncestr", noncestr);
			signParams.put("package", packageValue);
			signParams.put("timestamp", timeStamp);
			signParams.put("traceid", traceid);

			//生成支付签名，要采用URLENCODER的原始值进行SHA1算法！
			String sign = Sha1Util.createSHA1Sign(signParams);
			//增加非参与签名的额外参数
			signParams.put("app_signature", sign);
			signParams.put("sign_method", "sha1");

			//获取prepayId
			String prepayid = reqHandler.sendPrepay(signParams,weixinToken);
			if (null != prepayid && !"".equals(prepayid)) {//成功取回，把数据返回给客户端，由客户端发请求完成支付。
		 		
		 		List<NameValuePair> _signParams = new LinkedList<NameValuePair>();
		 		_signParams.add(new NameValuePair("appid", Constants.WXPAY_APPID));
		 		_signParams.add(new NameValuePair("appkey",Constants.WXPAY_APPKEY));
		 		_signParams.add(new NameValuePair("noncestr",noncestr));
		 		_signParams.add(new NameValuePair("package", "Sign=WXpay"));
		 		_signParams.add(new NameValuePair("partnerid", Constants.WXPAY_PARTNERID));
		 		_signParams.add(new NameValuePair("prepayid", prepayid));
		 		_signParams.add(new NameValuePair("timestamp", timeStamp));
		 		String _sign = genSign(_signParams);
		 		String rString = "{\"prepayId\":\""+prepayid+"\",\"nonceStr\":\""+noncestr+"\"," +
		 				"\"timeStamp\":\""+timeStamp+"\",\"sign\":\""+_sign+"\"}";
		 		System.err.println(">>>>>>>>>>>"+rString);
		 		AjaxUtil.ajaxOutput(response,rString);
		 	}else {
				AjaxUtil.ajaxOutput(response, "{}");
			}
		} else {
			outParams.put("retcode", "-1");
			outParams.put("retmsg", "错误：获取不到Token");
		}
		return null;
	}
	
	
	
	
	/**
	 * 生成支付签名（app_signature）
	 * 
	 * @param params
	 * @return
	 */
	private String genSign(List<NameValuePair> params) {
		// 1、先将params按字典序排序
		// Collections.sort(params, new Comparator<NameValuePair>() {
		// @Override
		// public int compare(NameValuePair lhs, NameValuePair rhs) {
		//
		// return lhs.getName().compareTo(rhs.getName());
		// }
		// });

		StringBuilder sb = new StringBuilder();

		int i = 0;
		for (; i < params.size() - 1; i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append(params.get(i).getName());
		sb.append('=');
		sb.append(params.get(i).getValue());
		System.err.println(sb.toString());
		return SecurityUtils.sha1(sb.toString());
	}
}
