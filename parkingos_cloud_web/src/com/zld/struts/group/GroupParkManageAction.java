package com.zld.struts.group;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.TimeTools;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//废弃
public class GroupParkManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

	@SuppressWarnings("rawtypes")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		if(uin==null || groupid == null){
			response.sendRedirect("login.do");
			return null;
		}

		if(action.equals("")){
			request.setAttribute("groupid", groupid);
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select * from com_info_tb where state<>? and (groupid=? " ;
			String countSql = "select count(*) from com_info_tb where state<>? and (groupid=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(1);
			params.add(groupid);
			List<Object> groups = new ArrayList<Object>();
			groups.add(groupid);
			List<Object> areas = commonMethods.getAreas(groups);
			if(areas != null && !areas.isEmpty()){
				String preParams = "";
				for(Object area : areas){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " or areaid in ("+preParams+") ";
				countSql += " or areaid in ("+preParams+") ";
				params.addAll(areas);
			}
			sql += " )";
			countSql += " )";
			Long count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("query")){
			String sql = "select * from com_info_tb where state<>? and (groupid=? " ;
			String countSql = "select count(*) from com_info_tb where state<>? and (groupid=? " ;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_info");
			List<Object> params = new ArrayList<Object>();
			params.add(1);
			params.add(groupid);
			List<Object> groups = new ArrayList<Object>();
			groups.add(groupid);
			List<Object> areas = commonMethods.getAreas(groups);
			if(areas != null && !areas.isEmpty()){
				String preParams = "";
				for(Object area : areas){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " or areaid in ("+preParams+") ";
				countSql += " or areaid in ("+preParams+") ";
				params.addAll(areas);
			}
			sql += " ) ";
			countSql += " ) ";
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			Long count= daService.getCount(countSql, params);
			List list = null;
			if(count>0){
				list = daService.getAll(sql+" order by id desc ", params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			int r = createPark(request, groupid);
			AjaxUtil.ajaxOutput(response, r+"");
		}else if(action.equals("edit")){
			int r = editPark(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("set")){
			Long comid = RequestUtil.getLong(request, "id", -1L);
			request.setAttribute("parkid", comid);
			Map<String, Object> parkMap = daService.getPojo("select * from com_info_tb where id=?",
					new Object[]{comid});
			String info="";
			if(parkMap!=null)
				info ="名称："+parkMap.get("company_name")+"，地址："+parkMap.get("address")+"<br/>创建时间："
						+TimeTools.getTime_yyyyMMdd_HHmm((Long)parkMap.get("create_time")*1000)+"，车位总数："+parkMap.get("parking_total")
						+"，分享车位："+parkMap.get("share_number")+"，经纬度：("+parkMap.get("longitude")+","+parkMap.get("latitude")+")";
			request.setAttribute("parkinfo", info);
			return mapping.findForward("set");
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "selids", -1L);
			int r = daService.update("update com_info_tb set state=? where id=? ",
					new Object[]{1, id});
			AjaxUtil.ajaxOutput(response, r + "");
		}

		return null;
	}

	//注册停车场
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Integer editPark(HttpServletRequest request){
		Long comid = RequestUtil.getLong(request, "id", -1L);
		Long areaid = RequestUtil.getLong(request, "areaid", -1L);
		Long time = System.currentTimeMillis()/1000;
		String company =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "company_name"));
		company = company.replace("\r", "").replace("\n", "");
		String address =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		address = address.replace("\r", "").replace("\n", "");
		String phone =RequestUtil.processParams(request, "phone");
		String mcompany =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "mcompany"));
		Integer parking_total =RequestUtil.getInteger(request, "parking_total", 0);
		Integer state =RequestUtil.getInteger(request, "state", 0);
		Integer etc = RequestUtil.getInteger(request, "etc", 0);
		String sql = "update com_info_tb set company_name=?,address=?,phone=?,mcompany=?,parking_total=?,update_time=?,state=?,etc=?,areaid=? where id=? ";
		int r = daService.update(sql, new Object[]{company, address, phone, mcompany, parking_total, time, state, etc, areaid, comid});
		return r;
	}

	//注册停车场
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Integer createPark(HttpServletRequest request, Long groupid){
		Long areaid = RequestUtil.getLong(request, "areaid", -1L);
		Long time = System.currentTimeMillis()/1000;
		String company =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "company_name"));
		company = company.replace("\r", "").replace("\n", "");
		String address =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		address = address.replace("\r", "").replace("\n", "");
		String phone =RequestUtil.processParams(request, "phone");
		String mcompany =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "mcompany"));
		Integer parking_total =RequestUtil.getInteger(request, "parking_total", 0);
		Integer etc = RequestUtil.getInteger(request, "etc", 0);
		Integer state =RequestUtil.getInteger(request, "state", 0);
		Long comId = daService.getLong("SELECT nextval('seq_com_info_tb'::REGCLASS) AS newid",null);
		String comsql = "insert into com_info_tb(id,company_name,address,phone,create_time,mcompany,parking_total,update_time,groupid,state,areaid,etc)" +
				" values(?,?,?,?,?,?,?,?,?,?,?,?)";
		int r =  daService.update(comsql, new Object[]{comId,company,address,phone,time,mcompany,parking_total,time,groupid,state,areaid,etc});
		if(r == 1){
			if(etc == 2){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("comid", comId);
				map.put("cname", company);
				commonMethods.createDefDevice(request, map);
			}
		}
		return r;
	}
}
