package com.zld.weixinpay.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zld.weixinpay.utils.util.TenpayUtil;


public class PackageRequestHandler extends RequestHandler {

	public PackageRequestHandler(HttpServletRequest request,
			HttpServletResponse response) {
		super(request, response);
	}

	/**
	 * 获取带参数的请求URL
	 * @return String
	 * @throws UnsupportedEncodingException 
	 */
	public String getRequestURL() throws UnsupportedEncodingException {
		
		this.createSign();
		
		StringBuffer sb = new StringBuffer();
		String enc = TenpayUtil.getCharacterEncoding(this.request, this.response);
		Set es = super.getAllParameters().entrySet();
		Iterator it = es.iterator();
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String k = (String)entry.getKey();
			String v = (String)entry.getValue();
			
			sb.append(k + "=" + URLEncoder.encode(v, enc) + "&");
		}
		
		//去掉最后一个&
		String reqPars = sb.substring(0, sb.lastIndexOf("&"));
		// 设置debug信息
		this.setDebugInfo("md5 sb:" + getDebugInfo() + "\r\npackage:" + reqPars);
		return reqPars;
		
	}

}
