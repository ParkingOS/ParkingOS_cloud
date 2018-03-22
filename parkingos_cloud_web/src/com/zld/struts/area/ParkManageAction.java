package com.zld.struts.area;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
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

public class ParkManageAction extends Action {
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
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		Long chanid = (Long)request.getSession().getAttribute("chanid");
		Integer supperadmin = (Integer)request.getSession().getAttribute("supperadmin");//是否是超级管理员
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(cityid == null) cityid = -1L;
		if(groupid == null) groupid = -1L;
		if(chanid == null) chanid = -1L;

		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select * from com_info_tb where state<>? " ;
			String countSql = "select count(*) from com_info_tb where state<>? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(1);
			List<Object> groups = new ArrayList<Object>();
			if(cityid != null && cityid > 0){//当城市角色登录的时候
				groups = commonMethods.getGroups(cityid);//查询该城市所辖的运营集团
				if(groups != null && !groups.isEmpty()){
					String preParams  ="";
					for(Object grouid : groups){
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}
					sql += " and ( groupid in ("+preParams+") ";
					countSql += " and ( groupid in ("+preParams+") ";
					params.addAll(groups);

					List<Object> areas = commonMethods.getAreas(groups);//查询城市直辖的区域和城市所辖的运营集团所辖的区域
					if(areas != null && !areas.isEmpty()){
						preParams = "";
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
				}
			}

			if(groupid != null && groupid > 0){//当运营集团角色登录的时候
				groups.add(groupid);
				if(groups != null && !groups.isEmpty()){
					String preParams  ="";
					for(Object grouid : groups){
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}
					sql += " and ( groupid in ("+preParams+") ";
					countSql += " and ( groupid in ("+preParams+") ";
					params.addAll(groups);

					List<Object> areas = commonMethods.getAreas(groups);//查询城市直辖的区域和城市所辖的运营集团所辖的区域
					if(areas != null && !areas.isEmpty()){
						preParams = "";
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
				}
			}
			Long count = 0L;
			if(groups != null && !groups.isEmpty()){
				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
				}
			}

			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("query")){
			String sql = "select * from com_info_tb where state<>? " ;
			String countSql = "select count(*) from com_info_tb where state<>? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_info");
			List list = null;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(1);
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}

			List<Object> groups = new ArrayList<Object>();
			if(cityid != null && cityid > 0){//当城市角色登录的时候
				groups = commonMethods.getGroups(cityid);//查询该城市所辖的运营集团
				if(groups != null && !groups.isEmpty()){
					String preParams  ="";
					for(Object grouid : groups){
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}
					sql += " and ( groupid in ("+preParams+") ";
					countSql += " and ( groupid in ("+preParams+") ";
					params.addAll(groups);

					List<Object> areas = commonMethods.getAreas(groups);//查询城市直辖的区域和城市所辖的运营集团所辖的区域
					if(areas != null && !areas.isEmpty()){
						preParams = "";
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
				}
			}

			if(groupid != null && groupid > 0){//当运营集团角色登录的时候
				groups.add(groupid);
				if(groups != null && !groups.isEmpty()){
					String preParams  ="";
					for(Object grouid : groups){
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}
					sql += " and ( groupid in ("+preParams+") ";
					countSql += " and ( groupid in ("+preParams+") ";
					params.addAll(groups);

					List<Object> areas = commonMethods.getAreas(groups);//查询城市直辖的区域和城市所辖的运营集团所辖的区域
					if(areas != null && !areas.isEmpty()){
						preParams = "";
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
				}
			}
			Long count = 0L;
			if(groups != null && !groups.isEmpty()){
				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			Long areaid = -1L;
			if(cityid > 0 || chanid > 0){//如果是城市登录或者渠道登录，获取传入的运营集团编号
				groupid = RequestUtil.getLong(request, "groupid", -1L);
			}
			if(cityid > 0 || chanid > 0 || groupid > 0){
				areaid = RequestUtil.getLong(request, "areaid", -1L);
			}
			int r = createPark(request, groupid, cityid, areaid, chanid);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			Long areaid = -1L;
			if(cityid > 0 || chanid > 0){//如果是城市登录或者渠道登录，获取传入的运营集团编号
				groupid = RequestUtil.getLong(request, "groupid", -1L);
			}
			if(cityid > 0 || chanid > 0 || groupid > 0){
				areaid = RequestUtil.getLong(request, "areaid", -1L);
			}
			int r = editPark(request, groupid, areaid);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			int r = daService.update("update com_info_tb set state=? where id=? ",
					new Object[]{1, id});
			AjaxUtil.ajaxOutput(response, r + "");
		}
		return null;
	}

	private int editPark(HttpServletRequest request, Long groupid, Long areaid){
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
		String sql = "update com_info_tb set company_name=?,address=?,phone=?,mcompany=?,parking_total=?,update_time=?,state=?,etc=?,areaid=?,groupid=? where id=? ";
		int r = daService.update(sql, new Object[]{company, address, phone, mcompany, parking_total, time, state, etc, areaid, groupid, comid});
		return r;
	}

	//注册停车场
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Integer createPark(HttpServletRequest request, Long groupid, Long cityid, Long areaid, Long chanid){
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
		List<Map> sqlsList = new ArrayList<Map>();
		Map comMap = new HashMap();
		String comsql = "insert into com_info_tb(id,company_name,address,phone,create_time,mcompany,parking_total,update_time,groupid,state,chanid,cityid,chanid,areaid,etc)" +
				" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] comvalues = new Object[]{comId,company,address,phone,time,mcompany,parking_total,time,groupid,state,chanid,cityid,chanid,areaid,etc};
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
}
