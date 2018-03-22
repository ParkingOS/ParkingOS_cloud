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

public class CityBerthManageAction extends Action {
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
			request.setAttribute("cityid", cityid);
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select c.*,d.did from com_park_tb c left join dici_tb d on c.dici_id= d.id where c.is_delete=? " ;
			String countSql = "select count(c.*) from com_park_tb c left join dici_tb d on c.dici_id= d.id where c.is_delete=?  " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(0);
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
				sql += " and c.comid in ("+preParams+") ";
				countSql += " and c.comid in ("+preParams+") ";
				params.addAll(parks);
				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("query")){
			String sql = "select c.*,d.did from com_park_tb c left join dici_tb d on c.dici_id= d.id where c.is_delete=? " ;
			String countSql = "select count(c.*) from com_park_tb c left join dici_tb d on c.dici_id= d.id where c.is_delete=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_park_tb","c",new String[]{"did"});
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(0);
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
				sql += " and (c.comid in ("+preParams+") ";
				countSql += " and (c.comid in ("+preParams+") ";
				params.addAll(parks);

				List<Object> berthseg = commonMethods.getBerthSeg(parks);
				if(berthseg != null && !berthseg.isEmpty()){
					preParams  ="";
					for(Object berthsegid : berthseg){
						if(preParams.equals(""))
							preParams ="?";
						else
							preParams += ",?";
					}

					sql += " or berthsec_id in ("+preParams+") ";
					countSql += " or berthsec_id in ("+preParams+") ";
					params.addAll(berthseg);
				}
				sql += " ) ";
				countSql += " ) ";

				if(sqlInfo!=null){
					countSql+=" and "+ sqlInfo.getSql();
					sql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}

				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by c.id desc ",params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			int r = createBerth(request, cityid, groupid);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editBerth(request, cityid, groupid);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Long count = pgOnlyReadService.getLong("select count(id) from com_park_tb where id=? and dici_id>? and is_delete=? ",
					new Object[]{id, 0, 0});
			if(count > 0){
				AjaxUtil.ajaxOutput(response, "-2");
				return null;
			}
			int r = daService.update("update com_park_tb set is_delete=? where id=? ",
					new Object[]{1, id});
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("getdici")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map<String, Object> diciMap = pgOnlyReadService.getMap("select code from dici_tb where id=? ",
					new Object[]{id});
			AjaxUtil.ajaxOutput(response, diciMap.get("code") + "");
		}else if(action.equals("tobindsensor")){
			Long berthid = RequestUtil.getLong(request, "berthid", -1L);
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			request.setAttribute("comid", comid);
			request.setAttribute("berthid", berthid);
			return mapping.findForward("bindsensor");
		}else if(action.equals("querysensor")){
			Long berthid = RequestUtil.getLong(request, "berthid", -1L);
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"dici_tb");
			String sql = "select * from dici_tb where id not in (select dici_id from com_park_tb where is_delete=? and dici_id>? ) and comid=? and is_delete=? ";
			String countSql = "select count(id) from dici_tb where id not in (select dici_id from com_park_tb where is_delete=? and dici_id>? ) and comid=? and is_delete=? ";
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(0);
			params.add(comid);
			params.add(0);
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			if(comid > 0){
				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by operate_time desc ",params, pageNum, pageSize);
				}
			}

			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("bindsensor")){
			Long berthid = RequestUtil.getLong(request, "berthid", -1L);
			Long sensorid = RequestUtil.getLong(request, "id", -1L);
			if(berthid > 0 && sensorid > 0){
				Long r = pgOnlyReadService.getLong("select count(id) from com_park_tb where dici_id=? and id<>? and is_delete=? ",
						new Object[]{sensorid, berthid, 0});
				if(r > 0){
					AjaxUtil.ajaxOutput(response, "-2");
					return null;
				}
				int re = daService.update("update com_park_tb set dici_id=? where id=? ",
						new Object[]{sensorid, berthid});
				AjaxUtil.ajaxOutput(response, re + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("unbindsensor")){
			Long berthid = RequestUtil.getLong(request, "id", -1L);
			int r = daService.update("update com_park_tb set dici_id=? where id=? ", new Object[]{-1, berthid});
			AjaxUtil.ajaxOutput(response, r + "");
		}
		return null;
	}

	private int editBerth(HttpServletRequest request, Long cityid, Long groupid){
		Long id = RequestUtil.getLong(request, "id", -1L);
		String cid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "cid"));
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		Double longitude = RequestUtil.getDouble(request, "longitude", 0d);
		Double latitude = RequestUtil.getDouble(request, "latitude", 0d);
		Map<String, Object> berthMap = pgOnlyReadService.getMap("select comid from com_park_tb where id=? ", new Object[]{id});
		Long comid = (Long)berthMap.get("comid");
		int count = checkBerth(id, cid, comid);
		if(count < 0){
			return count;
		}
		int r = daService.update("update com_park_tb set cid=?,address=?,longitude=?,latitude=? where id=? ",
				new Object[]{cid, address, longitude, latitude, id});
		return r;
	}

	private int createBerth(HttpServletRequest request, Long cityid, Long groupid){
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		if(comid == -1){
			return -1;
		}
		Long berthsegid = RequestUtil.getLong(request, "berthsec_id", -1L);
		String cid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "cid"));
		String address = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "address"));
		Double longitude = RequestUtil.getDouble(request, "longitude", 0d);
		Double latitude = RequestUtil.getDouble(request, "latitude", 0d);
		int count = checkBerth(-1L, cid, comid);
		if(count < 0){
			return count;
		}
		int r = daService.update("insert into com_park_tb(comid,cid,state,address,longitude,latitude,berthsec_id,create_time) values(?,?,?,?,?,?,?,?) ",
				new Object[]{comid, cid, 0, address, longitude, latitude, berthsegid, System.currentTimeMillis()/1000});
		return r;
	}

	private int checkBerth(Long berthid, String berth, Long comid){
		Long count = pgOnlyReadService.getLong("select count(id) from com_park_tb where is_delete=? and id<>? and comid=? and cid=? ",
				new Object[]{0, berthid, comid, berth});
		if(count > 0){
			return -2;
		}
		return 0;
	}
}
