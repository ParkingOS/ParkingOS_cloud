package com.zld.tcp.client;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.zld.utils.StringUtils;

public class TcpRequest {
	//读超时时间设置
	private final int TIME_OUT=30000;
	//记录实时读取响应数据时间
	private long time=0;
	private final String HOST="yztcjk.gmtx.com";
	private final int PORT=2222;
	//合作者应用Id
	private String hzzAppId="zntc2016hx";
	//合作者应用编码
	private String hzzAppCode="321011zntchx";
	//消息主体加解密密钥
	private String miyao="T5Zq2V7r01nHTxftIb84L1TFFSaN0olqcH54DmBclzgJcv0pzC2go0WqtC6IpLjf";
	//存储响应头信息
	private Map<String,String> resheadproperties=new HashMap<String,String>();
	private boolean isRun=true;
	
	public TcpRequest(){}
	Socket socket=null;
	BufferedInputStream input=null;
    BufferedOutputStream output=null;
	//缓存读取到的响应内容
	ByteArrayOutputStream b=new ByteArrayOutputStream();
	/**
	*@param action 对应请求动作
	*@param requestBody 请求协议主体内容
	**/
	public ResponseEntity request(String action,String requestBody){
		
	
		try{
			String nstr=UUID.randomUUID().toString().trim();
			long timestamp=new Date().getTime()/1000;
			String str=nstr+hzzAppCode+timestamp;
			//将拼接的字符串做sha1签名
			str=new SHA1().sha1(str);
			int contentLength=0;
			socket=new Socket(HOST,PORT);
			input=new BufferedInputStream(socket.getInputStream());
			output=new BufferedOutputStream(socket.getOutputStream());
			output.write("gmipp 1.0 request 321011 \r\n".getBytes("utf-8"));
			output.write(("action "+action+" \r\n").getBytes("utf-8"));
			output.write(("timeStamp "+timestamp+" \r\n").getBytes("utf-8"));
			output.write(("nonceStr "+nstr+" \r\n").getBytes("utf-8"));
			output.write(("hzzAppId "+hzzAppId+" \r\n").getBytes("utf-8"));
			output.write(("encryStr "+str+" \r\n").getBytes("utf-8")); 
			if(requestBody!=null&&!requestBody.trim().equals("")){
				requestBody=new DES().encrypt(requestBody.trim(), miyao);
				byte[] requestBodyBodyData=requestBody.trim().getBytes("utf-8");
				output.write(("contentLength "+requestBodyBodyData.length+" \r\n\r\n").getBytes("utf-8"));
				output.write(requestBodyBodyData);
			}else{
				output.write(("contentLength 0 \r\n\r\n").getBytes("utf-8"));
			}
			output.flush();
			time=System.currentTimeMillis();
			while(isRun){
				//如果读取超时或者响应协议头部内容长度超出8000字节(非正常请求)
				if(System.currentTimeMillis()-time>TIME_OUT||b.size()>8000){
					isRun=false;
					break;
				}
				int ava=input.available();
				if(ava==0){
					try{Thread.currentThread().sleep(200);}catch(Exception ex){}
					continue;
				}
				if(ava<=-1){
					break;
				}
				byte[] data=new byte[ava];
			    int n=input.read(data);
			    if(n==-1){
			    	break;
			    }
			    time=System.currentTimeMillis();
			    b.write(data,0,n);
		    	b.flush();
		    	String responstr=new String(b.toByteArray(),"utf-8");
		    	if(responstr.indexOf("\r\n\r\n")>0){
					//区分读取到的响应头和响应主体
		    		int len=responstr.substring(0,responstr.indexOf("\r\n\r\n")+4).getBytes("utf-8").length;
			    	byte[] dd=b.toByteArray();
			    	b.reset();
			    	b.write(dd,len,dd.length-len);
			    	String headstr=responstr.substring(0,responstr.indexOf("\r\n\r\n"));
			    	b.flush();
			    	
			    	String[] resheads=headstr.split("\r\n");
			    	for(int i=1;i<resheads.length;i++){
			    		String[] hs=resheads[i].trim().split(" ");
			    		resheadproperties.put(hs[0].trim(),hs[1].trim());
			    	}
					//读取响应消息主体内容
			    	String resbody=readResponseBody(Integer.parseInt(resheadproperties.get("contentLength").trim()));
			    	ResponseEntity entity=new ResponseEntity();
			    	if(resbody!=null&&!resbody.trim().equals("")){
						//解密响应消息主体
			    		resbody=new DES().decrypt(resbody, miyao);
			    		entity.setJsonResult(resbody);
			    	}
			    	String statusCode=resheadproperties.get("statusCode");
			    	String message=resheadproperties.get("message");
			    	entity.setStatusCode(statusCode);
			    	entity.setMessage(message);
			    	
			    	return entity;
			    }
			}
		}catch(Throwable ex){
			ex.printStackTrace();
		}finally{
			try {if(output!=null){output.close();}}catch(Throwable e){e.printStackTrace();}
			try {if(input!=null){input.close();}}catch(Throwable e){e.printStackTrace();}
			try {if(socket!=null){socket.close();}}catch(Throwable e){e.printStackTrace();}
		}
		return null;
	}

	//解析响应主体内容
	private String readResponseBody(int rescontentlength)throws Throwable{
		String bodyStr="";
		if(rescontentlength<=0){
			return bodyStr;
		}
		if(b.size()==rescontentlength){
			try{
				bodyStr=new String(b.toByteArray(),"utf-8");
			}catch(Exception ex){}
			return bodyStr;
		}
		while(isRun){
			if(System.currentTimeMillis()-time>10000){
				isRun=false;
				throw new Exception("连接中断");
			}
			int ava=input.available();
			if(ava==0){
				try{Thread.currentThread().sleep(200);}catch(Exception ex){}
				continue;
			}
			if(ava<=-1){
				throw new Exception("连接中断");
			}
			byte[] data=new byte[ava];
		    int n=input.read(data);
		    if(n==-1){
		    	throw new Exception("连接中断");
		    }
			//记录每次读取到数据的时间
		    time=System.currentTimeMillis();
		    b.write(data,0,n);
	    	b.flush();
	    	bodyStr=new String(b.toByteArray(),"utf-8");
		    if(b.size()==rescontentlength){
		    	try{
					bodyStr=new String(b.toByteArray(),"utf-8");
				}catch(Exception ex){}
		    	isRun=false;
		    	break;
		    }
		}
		return bodyStr;
	}
	
	public static void main(String[] args) {
		TcpRequest tcpRequest = new TcpRequest();
		Map<String,Object> paramMap = new HashMap<String, Object>();
		//注册用户：
//		paramMap.put("loginName", "21921");
//		paramMap.put("mobile", "18560603731");
//		paramMap.put("password", "21921");
//		paramMap.put("license", "鲁BY337C");
		// tcpRequest.request("register","");
		//充值
		paramMap.put("usrId", "21913");
		paramMap.put("stampTime", "1475727331");
		paramMap.put("money", "0");
		//tcpRequest.request("recharge", "");
		//屏幕分布数据查询接口
		//tcpRequest.request("screen_queryinfo", "");
		ResponseEntity entity = tcpRequest.request("recharge", StringUtils.createJson(paramMap));
		System.err.println("statusCode:"+entity.getStatusCode());//0正常返回
		System.err.println("josndata:"+entity.getJsonResult());
		System.err.println("message:"+entity.getMessage());
	}
}