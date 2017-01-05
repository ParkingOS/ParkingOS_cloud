package com.zld.struts.request;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.zld.AjaxUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.ZldUploadOperate;
import com.zld.utils.ZldUploadUtils;


/**
 * 基础信息数据接口
 * @author Administrator
 *
 */
@Path("baseinfo")
public class ZldBaseInfoApi {
	

	Logger logger = Logger.getLogger(ZldBaseInfoApi.class);

	
	@POST
	@Path("/uploadpark")//停车场
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadPark(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ZldUploadOperate zuo = getZldUploadOperate(context);
		logger.error("add park:params:"+paramMap);
		Map<String, Object> returnMap = zuo.handleData(context,paramMap,params,"com_info_tb",-1);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	@POST
	@Path("/uploadplot")//车位
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadPlot(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ZldUploadOperate zuo = getZldUploadOperate(context);
		Map<String, Object> returnMap =new HashMap<String, Object>();
		if(paramMap.get("park_uuid")!=null){
			String comid =zuo.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			if(comid==null||comid.equals("-1")){
				returnMap.put("status", "2");
				returnMap.put("resultCode", "100");
				returnMap.put("message", "车场信息不合法，找不到所属车场，ParkingNo不合法。");
				returnMap.put("data", "{}");
				logger.error("payorder:error,车场信息不合法，找不到所属车场，返回："+returnMap+"，原始请求:"+params);
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
				return ;
			}else {
				paramMap.put("comid", comid);
				paramMap.remove("park_uuid");
			}
		}
		paramMap.put("create_time", System.currentTimeMillis()/1000+"");
		returnMap =  zuo.handleData(context,paramMap,params,"com_park_tb",-1);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	

	@POST
	@Path("/uploadcompany")// 注册运营公司
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadCompany(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		ZldUploadOperate zuo = getZldUploadOperate(context);
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		Map<String, Object> returnMap =  zuo.handleData(context,paramMap,params,"company_tb",-1);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	
	
	@POST
	@Path("/uploadparkuser")//收费员
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadParkUser(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		ZldUploadOperate zuo = getZldUploadOperate(context);
		Map<String, Object> returnMap =new HashMap<String, Object>();
		if(paramMap.get("park_uuid")!=null){
			String comid =zuo.getComIdByParkUUID(paramMap.get("park_uuid"),context);
			if(comid==null||comid.equals("-1")){
				returnMap.put("status", "2");
				returnMap.put("resultCode", "100");
				returnMap.put("message", "车场信息不合法，找不到所属车场，ParkingNo不合法。");
				returnMap.put("data", "{}");
				logger.error("payorder:error,车场信息不合法，找不到所属车场，返回："+returnMap+"，原始请求:"+params);
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
				return ;
			}else {
				paramMap.put("comid", comid);
				paramMap.remove("park_uuid");
			}
		}
		if(paramMap.get("strid")==null)
			paramMap.put("strid", paramMap.get("uuid"));
		returnMap =  zuo.handleData(context,paramMap,params,"user_info_tb",-1);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	@POST
	@Path("/uploadberthsecs")//泊位段
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadberthsecs(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		ZldUploadOperate zuo = getZldUploadOperate(context);
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		Map<String, Object> returnMap =  zuo.handleData(context,paramMap,params,"com_berthsecs_tb",-1);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	@POST
	@Path("/uploadetc")//电子标签
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)       
	public void uploadetc(String params,
			@Context ServletContext context,
			@Context HttpServletResponse response)throws IOException {
		ZldUploadOperate zuo = getZldUploadOperate(context);
		Map<String, String> paramMap = ZldUploadUtils.stringToMap(params);
		Map<String, Object> returnMap =  zuo.handleData(context,paramMap,params,"com_etc_tb",-1);
		AjaxUtil.ajaxOutput(response, StringUtils.createJson(returnMap));
	}
	
	
	
	private ZldUploadOperate getZldUploadOperate(ServletContext context){
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		ZldUploadOperate zldUploadOperate = (ZldUploadOperate) ctx.getBean("zldUploadOperate");
		return zldUploadOperate;
	}
	
}