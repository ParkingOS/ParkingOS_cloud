package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CityHotAreaManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		if(uin == null || cityid == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			request.setAttribute("cityid", cityid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from city_hotarea_tb where cityid=? and state=? " ;
			String countSql = "select count(*) from city_hotarea_tb where cityid=? and state=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"city_hotarea_tb");
			List<Object> params = new ArrayList<Object>();
			params.add(cityid);
			params.add(0);
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("export")){
			String sql = "select * from city_hotarea_tb where cityid=? and state=? " ;
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"city_hotarea_tb");
			List<Object> params = new ArrayList<Object>();
			params.add(cityid);
			params.add(0);
			if(sqlInfo!=null){
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			List<Map<String, Object>> list  = pgOnlyReadService.getAll(sql +" order by create_time desc ",params, 0, 0);
			if(list!=null&&!list.isEmpty()){
				String heards[] = new String[]{"编号","名称","详细地址","描述","状态","新建日期","创建人"};
				List<List<String>> bodyList = new ArrayList<List<String>>();
				for(Map<String, Object> map : list){
					List<String> valueList = new ArrayList<String>();
					valueList.add(map.get("id")+"");
					valueList.add(map.get("name")+"");
					valueList.add(map.get("adress")+"");
					valueList.add(map.get("reason")+"");
					Integer state = (Integer)map.get("state");
					if(state!=null){
						switch (state) {
							case 0:
								valueList.add("新建");
								break;
							case 1:
								valueList.add("已删除");
								break;
							default:
								break;
						}
					}else {
						valueList.add("");
					}

					Long htime = (Long)map.get("create_time");
					if(htime!=null){
						valueList.add(TimeTools.getTime_yyyyMMdd_HHmmss(htime*1000));
					}else {
						valueList.add("");
					}
					valueList.add(map.get("create_user")+"");
					bodyList.add(valueList);
				}
				String fname = "热点区域" + com.zld.utils.TimeTools.getDate_YY_MM_DD();
				fname = StringUtils.encodingFileName(fname);
				java.io.OutputStream os;
				try {
					os = response.getOutputStream();
					response.reset();
					response.setHeader("Content-disposition", "attachment; filename="
							+ fname + ".xls");
					ExportExcelUtil importExcel = new ExportExcelUtil("热点区域",
							heards, bodyList);
					importExcel.createExcelFile(os);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else if(action.equals("create")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			String adress = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "adress"));
			String reason = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "reason"));
			Long createUid = (Long)request.getSession().getAttribute("loginuin");
			Integer state = RequestUtil.getInteger(request, "state", 0);
			if(cityid > 0){
				Long ntime  =System.currentTimeMillis()/1000;
				int r = daService.update("insert into city_hotarea_tb(name,adress,reason,state,cityid,create_time,create_user) " +
								"values(?,?,?,?,?,?,?)",
						new Object[]{name,adress,reason,state,cityid,ntime,createUid});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "name"));
			String adress = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "adress"));
			String reason = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "reason"));
			Long updateUid = (Long)request.getSession().getAttribute("loginuin");
			Integer state = RequestUtil.getInteger(request, "state", 0);
			if(id > 0){
				int r = daService.update("update city_hotarea_tb set name=?,adress=?,reason=?,state=?,update_user=?,update_time=? where id=? ",
						new Object[]{name, adress, reason, state,updateUid,System.currentTimeMillis()/1000,id});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Long deleteUid = (Long)request.getSession().getAttribute("loginuin");
			if(id>0){
				int r = daService.update("update city_hotarea_tb set state=?,delete_user=?,delete_time=? where id=? ",
						new Object[]{1,deleteUid,System.currentTimeMillis()/1000,id});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("parkdetail")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			request.setAttribute("hotid", id);
			return mapping.findForward("parklist");
		}else if(action.equals("parkquery")){
			String sql = "select id as comid,company_name from com_info_tb where hotarea_id=? and state<>? ";
			String countSql = "select count(*) from com_info_tb where hotarea_id=? and state<>? ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			Long hotid = RequestUtil.getLong(request, "hotid", -1L);
			List<Object> params = new ArrayList<Object>();
			params.add(hotid);
			params.add(1);
			count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"comid");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("parkcreate")){
			Long hotid = RequestUtil.getLong(request, "hotid", -1L);
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			int r = daService.update("update com_info_tb set hotarea_id=? where id=? ",
					new Object[]{hotid, comid});
			AjaxUtil.ajaxOutput(response, r + "");
			return null;
		}else if(action.equals("parkdelete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			int r = daService.update("update com_info_tb set hotarea_id=? where id=? ",
					new Object[]{-1, id});
			AjaxUtil.ajaxOutput(response, r + "");
		}
		return null;
	}
}
