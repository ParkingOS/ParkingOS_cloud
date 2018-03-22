package com.zld.struts.parkadmin;

import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * 车场云操作日志
 * @author Administrator
 *
 */
public class SensorLogsAction extends Action{
	@Autowired
	private MongoDbUtils mongoDbUtils;
	@Autowired  CommonMethods commonMethods;
	@Autowired
	private PgOnlyReadService onlyReadService;
	Logger logger = Logger.getLogger(SensorLogsAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		Integer authId = RequestUtil.getInteger(request, "authid",-1);
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", authId);
		if(uin==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			request.setAttribute("comid", request.getParameter("comid"));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			List<Object> parks = new ArrayList<Object>();
			if(groupid!=null){
				parks = commonMethods.getParks(groupid);
			}else if(cityid>0&&cityid!=null){
				parks = commonMethods.getparks(cityid);
			}
			Long comids[]=null;
			if(parks != null && !parks.isEmpty()){
				comids = new Long[parks.size()];
				for(int i=0;i<parks.size();i++){
					comids[i]=(Long)parks.get(i);
				}
			}
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			BasicDBObject conditions =null;
			conditions = getConditions(request, comids);

			BasicDBObject sort = new BasicDBObject("ctime",-1);

			Long count = mongoDbUtils.queryMongoDbCount("zld_hdbeart_logs", conditions);
			List<Map<String, Object>> retList =null;
			if(count>0){
				retList=mongoDbUtils.queryMongoDbResult("zld_hdbeart_logs", conditions,sort, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(retList,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("export")){

			BasicDBObject conditions =null;
			BasicDBObject sort = new BasicDBObject("ctime",-1);

			List<Map<String, Object>>list=mongoDbUtils.queryMongoDbResult("zld_hdbeart_logs", conditions,sort, 0, 0);
			List<List<String>> bodyList = new ArrayList<List<String>>();
			String [] heards = null;
			if(list!=null&&list.size()>0){
				mongoDbUtils.saveLogs( request,0, 5, "导出操作日志："+list.size()+"条");
				//setComName(list);
				String [] f = new String[]{"time","uin","otype","uri","ip","content"};
				heards = new String[]{"操作日期","操作人","操作类型","操作模块","IP地址","内容"};
				Map<Long, String> uinNameMap = new HashMap<Long, String>();
				Map<String, Object> oMap = getOperateType();
				for(Map<String, Object> map : list){
					List<String> values = new ArrayList<String>();
					for(String field : f){
						Object v = map.get(field);
						if(v==null)
							v="";
						if("otype".equals(field)){
							switch(Integer.valueOf(v.toString())){//0:NFC,1:IBeacon,2:照牌   3通道照牌 4直付 5月卡用户
								case 0:values.add("登录");break;
								case 1:values.add("退出");break;
								case 2:values.add("添加");break;
								case 3:values.add("编辑");break;
								case 4:values.add("删除");break;
								case 5:values.add("导出");break;
								case 6:values.add("结算");break;
								default:values.add("");
							}
						}else if("time".equals(field)){
							if(map.get(field)!=null){
								values.add(TimeTools.getTime_yyyyMMdd_HHmmss(Long.valueOf((v.toString()))*1000));
							}else{
								values.add("");
							}
						}else if("uin".equals(field)){
							Long uid =-1L;
							if(Check.isLong(v.toString()))
								uid = Long.valueOf(v.toString());
							if(uinNameMap.containsKey(uid))
								values.add(uinNameMap.get(uid)+"("+v+")");
							else {
								String name = getUinName(uid);
								values.add(name+"("+v+")");
								uinNameMap.put(uid, name);
							}
						}else if("uri".equals(field)){
							if(oMap.containsKey(v.toString()))
								values.add(""+oMap.get(v.toString()));
							else values.add(v.toString());
						}
						else {
							values.add(v.toString());
						}
					}
					bodyList.add(values);
				}
			}
			String fname = "操作日志" + com.zld.utils.TimeTools.getDate_YY_MM_DD();
			fname = StringUtils.encodingFileName(fname);
			java.io.OutputStream os;
			try {
				response.reset();
				response.setHeader("Content-disposition", "attachment; filename="
						+ fname + ".xls");
				response.setContentType("application/x-download");
				os = response.getOutputStream();
				ExportExcelUtil importExcel = new ExportExcelUtil("操作日志",
						heards, bodyList);
				importExcel.createExcelFile(os);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else if(action.equals("getOtype")){
			Map<String, Object> oMap = getOperateType();
			String result = "[{value_no:-1,value_name:\"全部\"}";
			for(String key : oMap.keySet()){
				result+=",{value_no:\""+key+"\",value_name:\""+oMap.get(key)+"\"}";
			}
			result += "]";
			AjaxUtil.ajaxOutput(response,result);
		}
		return null;
	}

	private BasicDBObject getConditions(HttpServletRequest request,Long[] comid){
		String timeOperate = RequestUtil.getString(request, "ctime");
		String btime = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "ctime_start"));
		String etime = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "ctime_end"));
		String transmitternumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "transmitternumber"));
		String battery = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "battery_start"));
		String magnetism = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "magnetism_start"));

		String sensornumber = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "sensornumber"));
		String parkstatus = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "parkstatus_start"));

		BasicDBObject conditions = new BasicDBObject();
		/*if(!transmitternumber.equals("")&&!transmitternumber.equals("-1"))
			conditions.put("transmitternumber", transmitternumber);*/
		if(!magnetism.equals("")&&!magnetism.equals("-1"))
			conditions.put("magnetism", magnetism);
		if(!battery.equals("")&&!battery.equals("-1"))
			conditions.put("battery", battery);
		/*if(!source.equals("")&&!source.equals("-1"))
			conditions.put("source", source);*/
		/*if(!sensornumber.equals("")&&!source.equals("-1"))
			conditions.put("sensornumber", sensornumber);*/
		if(!parkstatus.equals("")&&!parkstatus.equals("-2"))
			conditions.put("parkstatus", parkstatus);
		if(!sensornumber.equals("")&&!sensornumber.equals("-1")){
			Pattern  patternsensor=Pattern.compile(sensornumber,Pattern.CASE_INSENSITIVE);
			conditions.put("sensornumber", patternsensor);
		}
		if(!transmitternumber.equals("")&&!transmitternumber.equals("-1")){
			Pattern  patternsentrans=Pattern.compile(transmitternumber,Pattern.CASE_INSENSITIVE);
			conditions.put("transmitternumber", patternsentrans);
		}

		Pattern  patternsenmag=Pattern.compile(magnetism,Pattern.CASE_INSENSITIVE);
		/*conditions.put("magnetism", patternsenmag);
		Pattern  patternsenbat=Pattern.compile(battery,Pattern.CASE_INSENSITIVE);
		conditions.put("battery", patternsenbat);*/



		if(btime!=null&&!"".equals(btime)){
			if(timeOperate.equals("between")&&etime!=null&&!"".equals(etime)){//between
				conditions.append("ctime",
						new BasicDBObject(QueryOperators.GTE,TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime))
								.append(QueryOperators.LTE, TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime)));
			}else if(timeOperate.equals("1")){//>=
				conditions.put("ctime", new BasicDBObject(QueryOperators.GTE,TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime)));
			}else if(timeOperate.equals("3")){//=
				conditions.put("ctime", TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime));
			}else if(timeOperate.equals("2")){//<=
				conditions.put("ctime", new BasicDBObject(QueryOperators.LTE,TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime)));
			}
		}
		if(comid!=null&&comid.length>0){
			conditions.append("comid",new BasicDBObject(QueryOperators.IN,comid));
		}
		return conditions;
	}


	private String getUinName(Long uin) {
		Map list = onlyReadService.getPojo("select * from user_info_tb where id =?  ",new Object[]{uin});
		String uinName = "";
		if(list!=null&&list.get("nickname")!=null){
			uinName = list.get("nickname")+"";
		}
		return uinName;
	}

	private Map<String, Object> getOperateType(){
		Map<String, Object> oMap = new HashMap<String, Object>();
		oMap.put("/price.do","价格管理");
		oMap.put("/dologin.do","登录");
		oMap.put("/package.do","套餐管理");
		oMap.put("/order.do","订单管理");
		oMap.put("/parklogs.do","日志管理");
		oMap.put("/authrole.do","角色权限");
		oMap.put("/freereasons.do","免费原因");
		oMap.put("/compark.do","车位管理");
		oMap.put("/member.do","员工管理");
		oMap.put("/parkcamera.do","摄像头管理");
		oMap.put("/vipuser.do", "月卡会员");
		oMap.put("/parkinfo.do", "账户管理");
		oMap.put("/shop.do", "商户管理");
		oMap.put("/adminrole.do", "角色管理");
		return oMap;
	}
}
