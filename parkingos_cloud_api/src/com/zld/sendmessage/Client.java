package com.zld.sendmessage;


import java.io.*;
import java.net.*;
import java.security.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

	/*
	 * webservice服务器定义
	 */
	 // 我们所有的Demo，都是在GBK环境下测试的。
	 //如果您的系统是utf-8,调用注册方法可能不成功。
	 //java.io.IOException: Server returned HTTP response code: 400 for URL: http://sdk2.zucp.net:8060/webservice.asmx。
	 //如果出现上述400错误，请参考第105行。
	 //如果您的系统是utf-8，收到的短信可能是乱码，请参考第298行
	 //可以根据您的需要自行解析下面的地址
	 //http://sdk2.zucp.net:8060/webservice.asmx?wsdl   http://sdk2.zucp.net:8060/webservice.asmx
	private String serviceURL = "http://sdk.entinfo.cn:8060/webservice.asmx";
//	String sn="SDK-CSL-010-00270";//换上您自己的序列号
//	String pwd="016829";//换上您自己的密码
	private String sn = "SDK-CSL-010-00270";// 序列号
	private String password = "016829";// 密码
	private String pwd = "";

	/*
	 * 构造函数
	 */
	public Client()
			throws UnsupportedEncodingException {
//		this.sn = sn;
//		this.password = password;
		this.pwd = this.getMD5(sn + password);
	}

	/*
	 * 方法名称：getMD5 
	 * 功    能：字符串MD5加密 
	 * 参    数：待转换字符串 
	 * 返 回 值：加密之后字符串
	 */
	public String getMD5(String sourceStr) throws UnsupportedEncodingException {
		String resultStr = "";
		try {
			byte[] temp = sourceStr.getBytes();
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(temp);
			// resultStr = new String(md5.digest());
			byte[] b = md5.digest();
			for (int i = 0; i < b.length; i++) {
				char[] digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
						'9', 'A', 'B', 'C', 'D', 'E', 'F' };
				char[] ob = new char[2];
				ob[0] = digit[(b[i] >>> 4) & 0X0F];
				ob[1] = digit[b[i] & 0X0F];
				resultStr += new String(ob);
			}
			return resultStr;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * 方法名称：register 
	 * 功    能：注册 
	 * 参    数：对应参数 省份，城市，行业，企业名称，联系人，电话，手机，电子邮箱，传真，地址，邮编 
	 * 返 回 值：注册结果（String）
	 */
	public String register(String province, String city, String trade,
			String entname, String linkman, String phone, String mobile,
			String email, String fax, String address, String postcode) {
		String result = "";
		String soapAction = "http://tempuri.org/Register";
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		xml += "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">";
		xml += "<soap12:Body>";
		xml += "<Register xmlns=\"http://tempuri.org/\">";
		xml += "<sn>" + sn + "</sn>";
		xml += "<pwd>" + password + "</pwd>";
		xml += "<province>" + province + "</province>";
		xml += "<city>" + city + "</city>";
		xml += "<trade>" + trade + "</trade>";
		xml += "<entname>" + entname + "</entname>";
		xml += "<linkman>" + linkman + "</linkman>";
		xml += "<phone>" + phone + "</phone>";
		xml += "<mobile>" + mobile + "</mobile>";
		xml += "<email>" + email + "</email>";
		xml += "<fax>" + fax + "</fax>";
		xml += "<address>" + address + "</address>";
		xml += "<postcode>" + postcode + "</postcode>";
		xml += "<sign></sign>";
		xml += "</Register>";
		xml += "</soap12:Body>";
		xml += "</soap12:Envelope>";

		URL url;
		try {
			url = new URL(serviceURL);

			URLConnection connection = url.openConnection();
			HttpURLConnection httpconn = (HttpURLConnection) connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(xml.getBytes("GBK"));
			//bout.write(xml.getBytes("GBK"));
			byte[] b = bout.toByteArray();
			httpconn.setRequestProperty("Content-Length", String
					.valueOf(b.length));
			httpconn.setRequestProperty("Content-Type",
					"text/xml; charset=gb2312");
			httpconn.setRequestProperty("SOAPAction", soapAction);
			httpconn.setRequestMethod("POST");
			httpconn.setDoInput(true);
			httpconn.setDoOutput(true);

			OutputStream out = httpconn.getOutputStream();
			out.write(b);
			out.close();

			InputStreamReader isr = new InputStreamReader(httpconn
					.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			while (null != (inputLine = in.readLine())) {
				Pattern pattern = Pattern
						.compile("<RegisterResult>(.*)</RegisterResult>");
				Matcher matcher = pattern.matcher(inputLine);
				while (matcher.find()) {
					result = matcher.group(1);
				}
			}
			in.close();
			return new String(result.getBytes(), "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/*
	 * 方法名称：chargeFee 
	 * 功    能：充值 
	 * 参    数：充值卡号，充值密码 
	 * 返 回 值：操作结果（String）
	 */
	public String chargeFee(String cardno, String cardpwd) {
		String result = "";
		String soapAction = "http://tempuri.org/ChargUp";
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		xml += "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">";
		xml += "<soap12:Body>";
		xml += "<ChargUp xmlns=\"http://tempuri.org/\">";
		xml += "<sn>" + sn + "</sn>";
		xml += "<pwd>" + password + "</pwd>";
		xml += "<cardno>" + cardno + "</cardno>";
		xml += "<cardpwd>" + cardpwd + "</cardpwd>";
		xml += "</ChargUp>";
		xml += "</soap12:Body>";
		xml += "</soap12:Envelope>";

		URL url;
		try {
			url = new URL(serviceURL);

			URLConnection connection = url.openConnection();
			HttpURLConnection httpconn = (HttpURLConnection) connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(xml.getBytes());
			byte[] b = bout.toByteArray();
			httpconn.setRequestProperty("Content-Length", String
					.valueOf(b.length));
			httpconn.setRequestProperty("Content-Type",
					"text/xml; charset=gb2312");
			httpconn.setRequestProperty("SOAPAction", soapAction);
			httpconn.setRequestMethod("POST");
			httpconn.setDoInput(true);
			httpconn.setDoOutput(true);

			OutputStream out = httpconn.getOutputStream();
			out.write(b);
			out.close();

			InputStreamReader isr = new InputStreamReader(httpconn
					.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			while (null != (inputLine = in.readLine())) {
				Pattern pattern = Pattern
						.compile("<ChargUpResult>(.*)</ChargUpResult>");
				Matcher matcher = pattern.matcher(inputLine);
				while (matcher.find()) {
					result = matcher.group(1);
				}
			}
			in.close();
			// return result;
			return new String(result.getBytes(), "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/*
	 * 方法名称：getBalance 
	 * 功    能：获取余额 
	 * 参    数：无 
	 * 返 回 值：余额（String）
	 */
	public String getBalance() {
		String result = "";
		String soapAction = "http://tempuri.org/balance";
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		xml += "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">";
		xml += "<soap:Body>";
		xml += "<balance xmlns=\"http://tempuri.org/\">";
		xml += "<sn>" + sn + "</sn>";
		xml += "<pwd>" + pwd + "</pwd>";
		xml += "</balance>";
		xml += "</soap:Body>";
		xml += "</soap:Envelope>";

		URL url;
		try {
			url = new URL(serviceURL);

			URLConnection connection = url.openConnection();
			HttpURLConnection httpconn = (HttpURLConnection) connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(xml.getBytes());
			byte[] b = bout.toByteArray();
			httpconn.setRequestProperty("Content-Length", String
					.valueOf(b.length));
			httpconn.setRequestProperty("Content-Type",
					"text/xml; charset=gb2312");
			httpconn.setRequestProperty("SOAPAction", soapAction);
			httpconn.setRequestMethod("POST");
			httpconn.setDoInput(true);
			httpconn.setDoOutput(true);

			OutputStream out = httpconn.getOutputStream();
			out.write(b);
			out.close();

			InputStreamReader isr = new InputStreamReader(httpconn
					.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			while (null != (inputLine = in.readLine())) {
				Pattern pattern = Pattern
						.compile("<balanceResult>(.*)</balanceResult>");
				Matcher matcher = pattern.matcher(inputLine);
				while (matcher.find()) {
					result = matcher.group(1);
				}
			}
			in.close();
			return new String(result.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/*
	 * 方法名称：mt 
	 * 功    能：发送短信 ,传多个手机号就是群发，一个手机号就是单条提交
	 * 参    数：mobile,content,ext,stime,rrid(手机号，内容，扩展码，定时时间，唯一标识)
	 * 返 回 值：唯一标识，如果不填写rrid将返回系统生成的
	 */
	public String mt(String mobile, String content, String ext, String stime,
			String rrid) {
		String result = "";
		String soapAction = "http://tempuri.org/mt";
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		xml += "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">";
		xml += "<soap:Body>";
		xml += "<mt xmlns=\"http://tempuri.org/\">";
		xml += "<sn>" + sn + "</sn>";
		xml += "<pwd>" + pwd + "</pwd>";
		xml += "<mobile>" + mobile + "</mobile>";
		xml += "<content>" + content + "</content>";
		xml += "<ext>" + ext + "</ext>";
		xml += "<stime>" + stime + "</stime>";
		xml += "<rrid>" + rrid + "</rrid>";
		xml += "</mt>";
		xml += "</soap:Body>";
		xml += "</soap:Envelope>";
//System.out.println(sn+"  "+pwd+"  "+mobile+"  "+content);
		URL url;
		try {
			url = new URL(serviceURL);

			URLConnection connection = url.openConnection();
			HttpURLConnection httpconn = (HttpURLConnection) connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(xml.getBytes());
			//如果您的系统是utf-8,这里请改成bout.write(xml.getBytes("GBK"));

			byte[] b = bout.toByteArray();
			httpconn.setRequestProperty("Content-Length", String
					.valueOf(b.length));
			httpconn.setRequestProperty("Content-Type",
					"text/xml; charset=gb2312");
			httpconn.setRequestProperty("SOAPAction", soapAction);
			httpconn.setRequestMethod("POST");
			httpconn.setDoInput(true);
			httpconn.setDoOutput(true);

			OutputStream out = httpconn.getOutputStream();
			out.write(b);
			out.close();

			InputStreamReader isr = new InputStreamReader(httpconn
					.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			while (null != (inputLine = in.readLine())) {
				Pattern pattern = Pattern.compile("<mtResult>(.*)</mtResult>");
				Matcher matcher = pattern.matcher(inputLine);
				while (matcher.find()) {
					result = matcher.group(1);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/*
	 * 方法名称：mo 
	 * 功    能：接收短信 
	 * 参    数：无 
	 * 返 回 值：接收到的信息
	 */
	public String mo() {
		String result = "";
		String soapAction = "http://tempuri.org/mo";
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		xml += "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">";
		xml += "<soap:Body>";
		xml += "<mo xmlns=\"http://tempuri.org/\">";
		xml += "<sn>" + sn + "</sn>";
		xml += "<pwd>" + pwd + "</pwd>";
		xml += "</mo>";
		xml += "</soap:Body>";
		xml += "</soap:Envelope>";

		URL url;
		try {
			url = new URL(serviceURL);

			URLConnection connection = url.openConnection();
			HttpURLConnection httpconn = (HttpURLConnection) connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(xml.getBytes());
			byte[] b = bout.toByteArray();
			httpconn.setRequestProperty("Content-Length", String
					.valueOf(b.length));
			httpconn.setRequestProperty("Content-Type",
					"text/xml; charset=gb2312");
			httpconn.setRequestProperty("SOAPAction", soapAction);
			httpconn.setRequestMethod("POST");
			httpconn.setDoInput(true);
			httpconn.setDoOutput(true);

			OutputStream out = httpconn.getOutputStream();
			out.write(b);
			out.close();

			InputStream isr = httpconn.getInputStream();
			StringBuffer buff = new StringBuffer();
			byte[] byte_receive = new byte[10240];
			for (int i = 0; (i = isr.read(byte_receive)) != -1;) {
				buff.append(new String(byte_receive, 0, i));
			}
			isr.close();
			String result_before = buff.toString();
			int start = result_before.indexOf("<moResult>");
			int end = result_before.indexOf("</moResult>");
			result = result_before.substring(start + 10, end);

			return result;
			
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/*
 * 方法名称：gxmt 功 能：发送个性短信 ，即给不同的手机号发送不同的内容，手机号和内容用英文的逗号对应好
	 * 参数：mobile,content,ext,stime,rrid(手机号，内容，扩展码，定时时间，唯一标识) 返 回
	 * 值：唯一标识，如果不填写rrid将返回系统生成的
	 */
	public String gxmt(String mobile, String content, String ext, String stime,
			String rrid) {
		String result = "";
		String soapAction = "http://tempuri.org/gxmt";
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		xml += "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">";
		xml += "<soap:Body>";
		xml += "<gxmt xmlns=\"http://tempuri.org/\">";
		xml += "<sn>" + sn + "</sn>";
		xml += "<pwd>" + pwd + "</pwd>";
		xml += "<mobile>" + mobile + "</mobile>";
		xml += "<content>" + content + "</content>";
		xml += "<ext>" + ext + "</ext>";
		xml += "<stime>" + stime + "</stime>";
		xml += "<rrid>" + rrid + "</rrid>";
		xml += "</gxmt>";
		xml += "</soap:Body>";
		xml += "</soap:Envelope>";

		URL url;
		try {
			url = new URL(serviceURL);

			URLConnection connection = url.openConnection();
			HttpURLConnection httpconn = (HttpURLConnection) connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(xml.getBytes());
			byte[] b = bout.toByteArray();
			httpconn.setRequestProperty("Content-Length", String
					.valueOf(b.length));
			httpconn.setRequestProperty("Content-Type",
					"text/xml; charset=gb2312");
			httpconn.setRequestProperty("SOAPAction", soapAction);
			httpconn.setRequestMethod("POST");
			httpconn.setDoInput(true);
			httpconn.setDoOutput(true);

			OutputStream out = httpconn.getOutputStream();
			out.write(b);
			out.close();

			InputStreamReader isr = new InputStreamReader(httpconn
					.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			while (null != (inputLine = in.readLine())) {
				Pattern pattern = Pattern
						.compile("<gxmtResult>(.*)</gxmtResult>");
				Matcher matcher = pattern.matcher(inputLine);
				while (matcher.find()) {
					result = matcher.group(1);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	
	public String UnRegister() {
		String result = "";
		String soapAction = "http://tempuri.org/UnRegister";
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		xml += "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">";
		xml += "<soap12:Body>";
		xml += "<UnRegister xmlns=\"http://tempuri.org/\">";
		xml += "<sn>" + sn + "</sn>";
		xml += "<pwd>" + password + "</pwd>";		
		xml += "</UnRegister>";
		xml += "</soap12:Body>";
		xml += "</soap12:Envelope>";
		URL url;
		try {
			url = new URL(serviceURL);

			URLConnection connection = url.openConnection();
			HttpURLConnection httpconn = (HttpURLConnection) connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(xml.getBytes());
			byte[] b = bout.toByteArray();
			httpconn.setRequestProperty("Content-Length", String
					.valueOf(b.length));
			httpconn.setRequestProperty("Content-Type",
					"text/xml; charset=utf-8");
			httpconn.setRequestProperty("SOAPAction", soapAction);
			httpconn.setRequestMethod("POST");
			httpconn.setDoInput(true);
			httpconn.setDoOutput(true);

			OutputStream out = httpconn.getOutputStream();
			out.write(b);
			out.close();

			InputStreamReader isr = new InputStreamReader(httpconn
					.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			while (null != (inputLine = in.readLine())) {
				Pattern pattern = Pattern
						.compile("<UnRegisterResult>String</UnRegisterResult>");
				Matcher matcher = pattern.matcher(inputLine);
				while (matcher.find()) {
					result = matcher.group(1);
				}
			}
			in.close();
			return new String(result.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	
	
	/*
	 * 方法名称：UDPPwd 
	 * 功    能：修改密码
	 * 参    数：新密码
	 * 返 回 值：操作结果（String）
	 */
	public String UDPPwd(String newPwd) {
		String result = "";
		String soapAction = "http://tempuri.org/UDPPwd";
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		xml += "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">";
		xml += "<soap12:Body>";
		xml += "<UDPPwd  xmlns=\"http://tempuri.org/\">";
		xml += "<sn>" + sn + "</sn>";
		xml += "<pwd>" + password + "</pwd>";
		xml += "<newpwd>" + newPwd + "</newpwd>";
		xml += "</UDPPwd>";
		xml += "</soap12:Body>";
		xml += "</soap12:Envelope>";

		URL url;
		try {
			url = new URL(serviceURL);

			URLConnection connection = url.openConnection();
			HttpURLConnection httpconn = (HttpURLConnection) connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(xml.getBytes());
			byte[] b = bout.toByteArray();
			httpconn.setRequestProperty("Content-Length", String
					.valueOf(b.length));
			httpconn.setRequestProperty("Content-Type",
					"text/xml; charset=gb2312");
			httpconn.setRequestProperty("SOAPAction", soapAction);
			httpconn.setRequestMethod("POST");
			httpconn.setDoInput(true);
			httpconn.setDoOutput(true);

			OutputStream out = httpconn.getOutputStream();
			out.write(b);
			out.close();

			InputStreamReader isr = new InputStreamReader(httpconn
					.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			while (null != (inputLine = in.readLine())) {
				Pattern pattern = Pattern
						.compile("<UDPPwdResult>(.*)</UDPPwdResult>");
				Matcher matcher = pattern.matcher(inputLine);
				while (matcher.find()) {
					result = matcher.group(1);
				}
			}
			in.close();
			// return result;
			return new String(result.getBytes(), "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/*
	 * 方法名称：mt 
	 * 功    能：发送短信 ,传多个手机号就是群发，一个手机号就是单条提交
	 * 参    数：mobile,content,ext,stime,rrid(手机号，内容，扩展码，定时时间，唯一标识)
	 * 返 回 值：唯一标识，如果不填写rrid将返回系统生成的
	 */
	public String mdSmsSend_u(String mobile, String content, String ext, String stime,
			String rrid) {
		String result = "";
		String soapAction = "http://tempuri.org/mdSmsSend_u";
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
		xml += "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">";
		xml += "<soap:Body>";
		xml += "<mdSmsSend_u xmlns=\"http://tempuri.org/\">";
		xml += "<sn>" + sn + "</sn>";
		xml += "<pwd>" + pwd + "</pwd>";
		xml += "<mobile>" + mobile + "</mobile>";
		xml += "<content>" + content + "</content>";
		xml += "<ext>" + ext + "</ext>";
		xml += "<stime>" + stime + "</stime>";
		xml += "<rrid>" + rrid + "</rrid>";
		xml += "</mdSmsSend_u>";
		xml += "</soap:Body>";
		xml += "</soap:Envelope>";
//System.out.println(sn+"  "+pwd+"  "+mobile+"  "+content);
		URL url;
		try {
			url = new URL(serviceURL);

			URLConnection connection = url.openConnection();
			HttpURLConnection httpconn = (HttpURLConnection) connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(xml.getBytes("GBK"));
			//如果您的系统是utf-8,这里请改成bout.write(xml.getBytes("GBK"));

			byte[] b = bout.toByteArray();
			httpconn.setRequestProperty("Content-Length", String
					.valueOf(b.length));
			httpconn.setRequestProperty("Content-Type",
					"text/xml; charset=gb2312");
			httpconn.setRequestProperty("SOAPAction", soapAction);
			httpconn.setRequestMethod("POST");
			httpconn.setDoInput(true);
			httpconn.setDoOutput(true);

			OutputStream out = httpconn.getOutputStream();
			out.write(b);
			out.close();

			InputStreamReader isr = new InputStreamReader(httpconn
					.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			while (null != (inputLine = in.readLine())) {
				Pattern pattern = Pattern.compile("<mdSmsSend_uResult>(.*)</mdSmsSend_uResult>");
				Matcher matcher = pattern.matcher(inputLine);
				while (matcher.find()) {
					result = matcher.group(1);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}


}



