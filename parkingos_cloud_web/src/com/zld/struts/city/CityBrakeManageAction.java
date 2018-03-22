package com.zld.struts.city;

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
import java.util.List;
import java.util.Map;

public class CityBrakeManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

	@SuppressWarnings({ "rawtypes", "unused" })
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(cityid == null && groupid == null){
			return null;
		}
		if(cityid == null) cityid = -1L;
		if(groupid == null) groupid = -1L;

		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select * from com_brake_tb where " ;
			String countSql = "select count(*) from com_brake_tb where " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " comid in ("+preParams+") ";
				countSql += " comid in ("+preParams+") ";
				params.addAll(parks);

				count = pgOnlyReadService.getCount(countSql,params);
				if(count>0){
					list = pgOnlyReadService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
				}
			}
			setWorksite(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("query")){
			String sql = "select * from com_brake_tb where " ;
			String countSql = "select count(*) from com_brake_tb where " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_brake_tb");
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " comid in ("+preParams+") ";
				countSql += " comid in ("+preParams+") ";
				params.addAll(parks);

				if(sqlInfo!=null){
					countSql+=" and "+ sqlInfo.getSql();
					sql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				count = pgOnlyReadService.getCount(countSql,params);
				if(count>0){
					list = pgOnlyReadService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
				}
			}
			setWorksite(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			int r = createBrake(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editBrake(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("delete")){
			int r = deleteBrake(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}

		return null;
	}

	private int deleteBrake(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "selids", -1L);
		int r = daService.update("delete from com_brake_tb where id=? ",
				new Object[]{id});
		return r;
	}

	private int editBrake(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		Long passid = RequestUtil.getLong(request, "passid", -1L);
		String brake_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "brake_name"));
		String serial = RequestUtil.getString(request, "serial");
		String ip = RequestUtil.getString(request, "ip");
		if(comid == -1){
			return -1;
		}
		if(passid == -1){
			return -2;
		}
		int r = daService.update("update com_brake_tb set comid=?,passid=?,brake_name=?,serial=?,ip=? where id=? ",
				new Object[]{comid, passid, brake_name, serial, ip, id});
		return r;
	}

	private int createBrake(HttpServletRequest request){
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		Long passid = RequestUtil.getLong(request, "passid", -1L);
		String brake_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "brake_name"));
		String serial = RequestUtil.getString(request, "serial");
		String ip = RequestUtil.getString(request, "ip");
		if(comid == -1){
			return -1;
		}
		if(passid == -1){
			return -2;
		}
		String sql = "insert into com_brake_tb(passid,brake_name,serial,ip,comid) values(?,?,?,?,?)";
		int re = daService.update(sql, new Object[]{passid,brake_name,serial,ip,comid});
		return re;
	}

	private void setWorksite(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> passList = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> map : list){
				passList.add(map.get("passid"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}
			List<Map<String, Object>> list2 = pgOnlyReadService.getAllMap("select worksite_id,id from com_pass_tb where id in ("+preParams+")", passList);


			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					Long id = (Long)map.get("passid");
					for(Map<String, Object> map2 : list2){
						Long passid = (Long)map2.get("id");
						if(id.intValue() == passid.intValue()){
							map.put("worksite_id", map2.get("worksite_id"));
							break;
						}
					}
				}
			}
		}
	}
}
