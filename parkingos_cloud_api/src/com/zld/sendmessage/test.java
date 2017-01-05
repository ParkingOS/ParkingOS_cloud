package com.zld.sendmessage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

//该Demo主要解决Java在Linux等环境乱码的问题
public class test {
	public static void main(String[] args) throws UnsupportedEncodingException{
		//输入软件序列号和密码
				String sn="SDK-CSL-010-00270";//换上您自己的序列号
				String pwd="016829";//换上您自己的密码
				String mobiles="18101333937";
				String content=URLEncoder.encode("您的验证码是2209 【宜行扬州】", "utf8");
				
		
				Client client=new Client();
				String result_mt = client.mdSmsSend_u(mobiles, content, "", "", "");
				if(result_mt.startsWith("-")||result_mt.equals(""))//发送短信，如果是以负号开头就是发送失败。
				{
					System.out.print("发送失败！返回值为："+result_mt+"请查看webservice返回值对照表");
					return;
				}
				//输出返回标识，为小于19位的正数，String类型的。记录您发送的批次。
				else
				{
					System.out.print("发送成功，返回值为："+result_mt);
				}
		
		
	
	}
}
