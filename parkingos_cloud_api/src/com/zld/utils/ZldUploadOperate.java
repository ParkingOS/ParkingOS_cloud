package com.zld.utils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;

@Repository
public class ZldUploadOperate {
	/**
	 * 处理数据
	 * @param context
	 * @param paramMap
	 * @param string
	 * @param type 0注册 1更新 2删除 -1时取operate值
	 * @return
	 */
	public Map<String, Object> handleData(ServletContext context,Map<String, String> paramMap,String params,
			String tableName, int type) {
		Map<String, Object> returnMap =new HashMap<String, Object>();
		if(type==-1){//上传，更新和删除在同一接口中
			String operate = paramMap.get("operate");
			if(operate!=null&&Check.isNumber(operate)){
				type = Integer.valueOf(operate);
				//paramMap.remove("operate");
			}
			if(type<0||type>2){
				returnMap.put("status", "2");
				returnMap.put("resultCode", "100");
				returnMap.put("message", "检查参数operate是否合法,期望值：0,1,2，实际："+operate);
				returnMap.put("data", "{}");
			}
		}
		String message="注册成功";
		if(type==1){
			message="更新成功";
		}else if(type==2){
			message="删除成功";
		}
		if(validateToken(paramMap.get("token"), context)){//token有效
			if(ZldUploadUtils.validateSign(params)){//签名有效
				Map<String, Object> preUpdateMap = ZldUploadUtils.getData(paramMap,tableName,type);
				String errmesg = (String)preUpdateMap.get("errmesg");
				if(errmesg!=null&&!"".equals(errmesg)){//有错误，返回
					returnMap.put("status", "2");
					returnMap.put("resultCode", "100");
					returnMap.put("message", preUpdateMap.get("errmesg"));
					returnMap.put("data", "{}");
				}else {//没有错误，处理数据库
					String sql = (String)preUpdateMap.get("sql");
					Object[] sqlParams = (Object[])preUpdateMap.get("params");
					int ret = 0;
					try {
						ret =update(sql, sqlParams, context);
					} catch (Exception e) {
						System.out.println(e.getMessage());
						if(e.getMessage().indexOf("order_tb_uin_create_time_end_time_key")!=-1){
							returnMap.put("status", "1");
							returnMap.put("resultCode", "0");
							returnMap.put("message", "此信息已重复");
							returnMap.put("data", "{}");
						}else {
							ret =-1;
							String err = "请检查参数是否正确";
							returnMap.put("status", "2");
							returnMap.put("resultCode", "500");
							returnMap.put("message", "写入数据错误:"+err);
							returnMap.put("data", "{}");
						}
					}
					if(ret==1){//数据库操作成功
						returnMap.put("status", "1");
						returnMap.put("resultCode", "0");
						returnMap.put("message", message);
						returnMap.put("data", "{}");
					}else if(ret==0){//数据库操作错误
						returnMap.put("status", "2");
						returnMap.put("resultCode", "500");
						returnMap.put("message", "写入数据错误");
						returnMap.put("data", "{}");
					}
				}
			}else {//签名无效
				returnMap.put("status", "2");
				returnMap.put("resultCode", "102");
				returnMap.put("message", "签名无效");
				returnMap.put("data", "{}");
			}
		}else {
			returnMap.put("status", "2");
			returnMap.put("resultCode", "101");
			returnMap.put("message", "token无效");
			returnMap.put("data", "{}");
		}
		System.out.println(returnMap);
		return returnMap;
	}

	
	//验证token
	private boolean validateToken(String token,ServletContext context){
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		PgOnlyReadService daService = (PgOnlyReadService) ctx.getBean("pgOnlyReadService");
		Map comMap = daService.getPojo("select * from user_session_tb where token=?", new Object[]{token});
		if(comMap!=null){
			return true;
		}
		return false;
	}
	
	//写入数据库
	private int update(String sql,Object[] parmas,ServletContext context){
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		int ret = daService.update(sql, parmas);
		return ret;
	}
	//根据车场uuid查车场ID
	public String getComIdByParkUUID(String uuid,ServletContext context){
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		Map comMap = daService.getPojo("select id from com_info_tb where park_uuid=?", new Object[]{uuid});
		if(comMap!=null&&comMap.get("id")!=null){
			return comMap.get("id")+"";
		}
		return "-1";
	}
	//根据收费员uuid查询收费员编号
	public String getUserIdByUUID(String uuid,ServletContext context){
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		Map comMap = daService.getPojo("select id from user_info_tb where uuid=?", new Object[]{uuid});
		if(comMap!=null&&comMap.get("id")!=null){
			return comMap.get("id")+"";
		}
		return "-1";
	}
	
	//根据uuid查询运营公司编号
	public String getCompanyIddByUUID(String uuid,ServletContext context){
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		Map comMap = daService.getPojo("select id from company_tb where uuid=?", new Object[]{uuid});
		if(comMap!=null&&comMap.get("id")!=null){
			return comMap.get("id")+"";
		}
		return "-1";
	}
	//根据uuid查询泊位段编号
	public String getBerthsecIdIdByUUID(String uuid,ServletContext context){
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		Map comMap = daService.getPojo("select id from com_berthsecs_tb where uuid=?", new Object[]{uuid});
		if(comMap!=null&&comMap.get("id")!=null){
			return comMap.get("id")+"";
		}
		return "-1";
	}
	//根据uuid查询泊位段编号
	public Long getUinByCarNumber(String car_number,ServletContext context){
		ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
		DataBaseService daService = (DataBaseService) ctx.getBean("dataBaseService");
		Map comMap = daService.getPojo("select uin from car_info_tb where car_number=?", new Object[]{car_number});
		if(comMap!=null&&comMap.get("uin")!=null){
			return (Long)comMap.get("uin");
		}
		return -1L;
	}
}
