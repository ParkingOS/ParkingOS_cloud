/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2014 All Rights Reserved.
 */
package com.zld.utils;

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import pay.AlipayAPIClientFactory;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;

@Component
public class ToAlipayQrTradePay {
	Logger logger = Logger.getLogger(ToAlipayQrTradePay.class);
	/**
	 * 
	 * @param args
	 */
	/*public static void main(String[] args) {
		//201504210011041195
		String out_trade_no="20150528207426"; //商户唯一订单号
		String total_amount="0.01";
		String subject = "测试扫码付订单";
		qrPay(out_trade_no,total_amount,subject,"支付订单",1029L);
	}
	*/
	
	/**
	 *  二维码下单支付
	 * @param out_trade_no
	 * @param auth_code
	 * @author jinlong.rhj
	 * @date 2015年4月28日
	 * @version 1.0
	 * @return 
	 */
	public String qrPay(String out_trade_no,String total_amount,
			String title,String body,Long uid) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time_expire= sdf.format(System.currentTimeMillis()+3600*1000);
		
		StringBuilder sb = new StringBuilder();
		sb.append("{\"out_trade_no\":\"" + out_trade_no + "\",");
		sb.append("\"total_amount\":\""+total_amount+"\",\"discountable_amount\":\"0.00\",");
		sb.append("\"subject\":\""+title+"\",\"body\":\""+body+"\",");
		//sb.append("\"goods_detail\":[{\"goods_id\":\"apple-01\",\"goods_name\":\"ipad\",\"goods_category\":\"7788230\",\"price\":\"88.00\",\"quantity\":\"1\"}],");
		sb.append("\"goods_detail\":[],");
		sb.append("\"operator_id\":\""+uid+"\",\"store_id\":\"zld1117\",\"terminal_id\":\"t_001\",");
		sb.append("\"time_expire\":\""+time_expire+"\"}");
		logger.error("支付宝支付二维码请求参数："+sb.toString());

		AlipayClient alipayClient = AlipayAPIClientFactory.getAlipayClient();

		// 使用SDK，构建群发请求模型
		AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
		request.setBizContent(sb.toString());
		//回调地址：测试
		request.setNotifyUrl("http://yxiudongyeahnet.vicp.cc/zld/rechage");
		//回调地址：正式
		//request.setNotifyUrl("http://s.tingchebao.com/zld/rechage");
//		request.putOtherTextParam("ws_service_url", "http://unitradeprod.t15032aqcn.alipay.net:8080");
		String qr = "";
		try {
			AlipayTradePrecreateResponse response = null;
			// 使用SDK，调用交易下单接口
			response = alipayClient.execute(request);
			if(response!=null){
				logger.error("支付宝支付二维码返回是否成功："+response.isSuccess());
				logger.error("支付宝支付二维码返回："+response.getMsg());
				logger.error("支付宝支付二维码返回结果："+response.getBody());
				if( response.isSuccess()){
					qr=response.getQrCode();
				}else {
					logger.error("错误码："+response.getSubCode());
					logger.error("错误描述："+response.getSubMsg());
				}
			}else {
				logger.error("支付宝支付二维码返回错误");
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		return qr;
	}
	


}
