package com.zld.struts.channel;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
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

public class ChannelParkManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private PublicMethods publicMethods;

	@SuppressWarnings("rawtypes")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long chanid = (Long)request.getSession().getAttribute("chanid");
		Integer supperadmin = (Integer)request.getSession().getAttribute("supperadmin");//是否是超级管理员
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		if(uin==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(supperadmin == 1){//来自超级管理员
			chanid = RequestUtil.getLong(request, "chanid", -1L);
		}

		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select c.*,g.name gname from com_info_tb c left join org_group_tb g on c.groupid=g.id where c.chanid=? and c.state<>? " ;
			String countSql = "select count(*) from com_info_tb c left join org_group_tb g on c.groupid=g.id where c.chanid=? and c.state<>? " ;

			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(chanid);
			params.add(1);
			Long count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("query")){
			String sql = "select c.*,g.name gname from com_info_tb c left join org_group_tb g on c.groupid=g.id where c.chanid=? and c.state<>? ";
			String countSql = "select count(*) from com_info_tb c left join org_group_tb g on c.groupid=g.id where c.chanid=? and c.state<>? ";
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_info","c",new String[]{});
			List<Object> params = new ArrayList<Object>();
			params.add(chanid);
			params.add(1);
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
			int r = createPark(request, chanid);
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
		}

		return null;
	}

	//注册停车场
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Integer editPark(HttpServletRequest request){
		Long comid = RequestUtil.getLong(request, "id", -1L);
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
		String sql = "update com_info_tb set company_name=?,address=?,phone=?,mcompany=?,parking_total=?,update_time=?,state=?,etc=? where id=? ";
		int r = daService.update(sql, new Object[]{company, address, phone, mcompany, parking_total, time, state, etc, comid});
		if(r == 1){
			if(publicMethods.isEtcPark(Long.valueOf(comid))){
				r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate,state) values(?,?,?,?,?,?)", new Object[]{comid,"com_info_tb",comid,System.currentTimeMillis()/1000,1,1});
			}
		}
		return r;
	}

	//注册停车场
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Integer createPark(HttpServletRequest request, Long chanid){
		Long time = System.currentTimeMillis()/1000;
		String company =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "company_name"));
		company = company.replace("\r", "").replace("\n", "");
		String address =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		address = address.replace("\r", "").replace("\n", "");
		String phone =RequestUtil.processParams(request, "phone");
		String mcompany =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "mcompany"));
		Integer parking_total =RequestUtil.getInteger(request, "parking_total", 0);
		Integer etc = RequestUtil.getInteger(request, "etc", 0);
		Long comId = daService.getLong("SELECT nextval('seq_com_info_tb'::REGCLASS) AS newid",null);

		List<Map> sqlsList = new ArrayList<Map>();
		Map comMap = new HashMap();
		String comsql = "insert into com_info_tb(id,company_name,address,phone,create_time,mcompany,parking_total,update_time,chanid,state,etc)" +
				" values(?,?,?,?,?,?,?,?,?,?,?)";
		Object[] comvalues = new Object[]{comId,company,address,phone,time,mcompany,parking_total,time,chanid,0,etc};
		comMap.put("sql", comsql);
		comMap.put("values", comvalues);

		sqlsList.add(comMap);

		boolean r =  daService.bathUpdate(sqlsList);
		if(r){
			if(etc == 2){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("comid", comId);
				map.put("cname", company);
				commonMethods.createDefDevice(request, map);
			}
			return 1;
		}else {
			return -1;
		}
	}

	@SuppressWarnings("rawtypes")
	private List<Object> getChildOrg(Long chanid){
		List<Object> orgList = new ArrayList<Object>();
		orgList.add(chanid);
		List<Map<String, Object>> list = daService.getAll("select id from zld_organize_tb where pid=? ", new Object[]{chanid});
		if(list != null && !list.isEmpty()){
			for(Map map : list){
				orgList.add(map.get("id"));
			}
		}
		return orgList;
	}
}
