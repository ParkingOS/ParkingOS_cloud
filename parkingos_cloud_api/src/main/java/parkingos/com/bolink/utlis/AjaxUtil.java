package parkingos.com.bolink.utlis;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class AjaxUtil {
	static Logger logger = Logger.getLogger(AjaxUtil.class);
	static String logpre = "ajax工具类===>";

	public static void ajaxOutput(HttpServletResponse response,
			Object outPutObj)  {
		try {
			String jsonString = JSONObject.toJSONString(outPutObj);
			response.setContentType("application/json;charset=utf-8");
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "POST,GET");
			PrintWriter printWriter = response.getWriter();
			printWriter.write(jsonString);
			printWriter.flush();
			printWriter.close();
		} catch (IOException e) {
			logger.error(logpre+e.getMessage());
		}
	}

	public static void ajaxOutputWithSnakeCase(HttpServletResponse response,
											   Object outPutObj)  {
		try {
			SerializeConfig config = new SerializeConfig();
			config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
			String jsonString = JSONObject.toJSONString(outPutObj,config);
			response.setContentType("application/json;charset=utf-8");
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "POST,GET");
			PrintWriter printWriter = response.getWriter();
			printWriter.write(jsonString);
			printWriter.flush();
			printWriter.close();
		} catch (IOException e) {
			logger.error(logpre+e.getMessage());
		}
	}

	/**
	 * 解码Ajax urf-8编码后的url形式中文参数 返回UTF-8结果
	 * @param someStr
	 * @return
	 */
	public static String decodeUTF8(String someStr) {
		String newStr = null;
		if(someStr!=null&&someStr.equals(""))
			return "";
		if(someStr!=null&&!someStr.equals("")) {
			try {
				newStr = URLDecoder.decode(someStr,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return newStr;
	}

	/**
	 *编码Ajax urf-8编码后的url形式中文参数 返回UTF-8结果
	 someStr	 */
	public static String encodeUTF8(String someStr) {
		String newStr = null;
		if(someStr!=null&&someStr.equals(""))
			return "";
		if(someStr!=null&&!someStr.equals("")) {
			try {
				newStr = URLEncoder.encode(someStr,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return newStr;
	}
}
