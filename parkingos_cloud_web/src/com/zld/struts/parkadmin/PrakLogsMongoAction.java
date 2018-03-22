package com.zld.struts.parkadmin;

import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import com.zld.AjaxUtil;
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


/**
 * 车场云操作日志
 * @author Administrator
 *
 */
public class PrakLogsMongoAction extends Action{
	@Autowired
	private MongoDbUtils mongoDbUtils;
	@Autowired
	private PgOnlyReadService onlyReadService;
	Logger logger = Logger.getLogger(PrakLogsMongoAction.class);
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
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			BasicDBObject conditions =null;
			conditions = getConditions(request, comid);
			if(cityid!=null){
				conditions.remove("comId");
				conditions.put("cityid", cityid);
			}
			if(groupid!=null){
				conditions.remove("comId");
				conditions.put("groupid",groupid);
			}


			BasicDBObject sort = new BasicDBObject("time",-1);

			Long count = mongoDbUtils.queryMongoDbCount("zld_logs", conditions);
			List<Map<String, Object>> retList =null;
			if(count>0){
				retList=mongoDbUtils.queryMongoDbResult("zld_logs", conditions,sort, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(retList,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("export")){

			BasicDBObject conditions =null;
			conditions = getConditions(request, comid);
			if(cityid!=null){
				conditions.remove("comId");
				conditions.put("cityid", cityid);
			}
			if(groupid!=null){
				conditions.remove("comId");
				conditions.put("groupid",groupid);
			}

			BasicDBObject sort = new BasicDBObject("time",-1);

			List<Map<String, Object>>list=mongoDbUtils.queryMongoDbResult("zld_logs", conditions,sort, 0, 0);
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

	private BasicDBObject getConditions(HttpServletRequest request,Long comid){
		Long uin = RequestUtil.getLong(request, "uin_start", -1L);
		Integer otype =RequestUtil.getInteger(request, "otype_start", -1);
		String uri = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "uri_start"));
		Integer itype =RequestUtil.getInteger(request, "itype", -1);
		String timeOperate = RequestUtil.getString(request, "time");
		String btime = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "time_start"));
		String etime = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "time_end"));

		BasicDBObject conditions = new BasicDBObject();
		BasicDBObject sort = new BasicDBObject("time",-1);
		conditions.put("comId", comid);
		if(uin>-1)
			conditions.put("uin", uin);
		if(otype>-1)
			conditions.put("otype",otype);
		if(itype>-1)
			conditions.put("itype",itype);
		if(!uri.equals("")&&!uri.equals("-1"))
			conditions.put("uri", uri);
		if(btime!=null&&!"".equals(btime)){
			if(timeOperate.equals("between")&&etime!=null&&!"".equals(etime)){//between
				conditions.append("time",
						new BasicDBObject(QueryOperators.GTE,TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime))
								.append(QueryOperators.LTE, TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime)));
			}else if(timeOperate.equals("1")){//>=
				conditions.put("time", new BasicDBObject(QueryOperators.GTE,TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime)));
			}else if(timeOperate.equals("3")){//=
				conditions.put("time", TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime));
			}else if(timeOperate.equals("2")){//<=
				conditions.put("time", new BasicDBObject(QueryOperators.LTE,TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(btime)));
			}
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
		oMap.put("/cartype.do", "车型设定");
		oMap.put("/bindcartype.do", "绑定车型");
		oMap.put("/groupbindcartype.do", "绑定车型");
		return oMap;
	}
}
