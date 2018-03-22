package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
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

public class CityCameraManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;

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
			String sql = "select * from com_camera_tb where " ;
			String countSql = "select count(*) from com_camera_tb where " ;
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
			String sql = "select * from com_camera_tb where " ;
			String countSql = "select count(*) from com_camera_tb where " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_camera_tb");
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
			int r = createCamera(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editCamera(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("delete")){
			int r = deleteCamera(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("editpass")){
			int r = editpass(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private int editpass(HttpServletRequest request){
		Long cameraid = RequestUtil.getLong(request, "id", -1L);
		String newpass = RequestUtil.processParams(request, "newpass");
		String confirmpass = RequestUtil.processParams(request, "confirmpass");
		if(!newpass.equals(confirmpass) || cameraid == -1){
			return -1;
		}
		Map<String, Object> camMap = daService.getMap("select * from com_camera_tb where id =? ",
				new Object[]{cameraid});
		Long comid = -1L;
		if(camMap != null && camMap.get("comid") !=null){
			comid = (Long)camMap.get("comid");
		}
		String sql = "update com_camera_tb set cpassword=? where id=?";
		int result = daService.update(sql, new Object[]{newpass,cameraid});
		if(result == 1){
			if(publicMethods.isEtcPark(comid)){
				int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_camera_tb",cameraid,System.currentTimeMillis()/1000,1});
			}
			mongoDbUtils.saveLogs( request,0, 3, "重置了摄像头密码，编号"+cameraid+"，新密码"+newpass);
		}
		return result;
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

	@SuppressWarnings("unchecked")
	private int deleteCamera(HttpServletRequest request){
		Long cameraid = RequestUtil.getLong(request, "selids", -1L);
		String sql = "delete from com_camera_tb where id=?";
		Map<String, Object> camMap = daService.getMap("select * from com_camera_tb where id =? ",
				new Object[]{cameraid});
		Long comid = -1L;
		if(camMap != null && camMap.get("comid") !=null){
			comid = (Long)camMap.get("comid");
		}
		int result = daService.update(sql, new Object[]{cameraid});
		if(result == 1){
			if(publicMethods.isEtcPark(comid)){
				int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_camera_tb",cameraid,System.currentTimeMillis()/1000,2});
			}
			mongoDbUtils.saveLogs( request,0, 4, "删除了摄像头:"+camMap);
		}
		return result;
	}

	private int editCamera(HttpServletRequest request){
		Long cameraid = RequestUtil.getLong(request, "id", -1L);
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		Long passid = RequestUtil.getLong(request, "passid", -1L);
		String camera_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "camera_name"));
		String ip = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "ip"));
		String port = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "port"));
		String cusername = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "cusername"));
		String manufacturer = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "manufacturer"));
		if(comid == -1){
			return -1;
		}
		if(passid == -1){
			return -2;
		}

		//编辑
		String sql = "update com_camera_tb set camera_name=?,ip=?,port=?,cusername=?,manufacturer=?,passid=?,comid=? where id=?";
		int re = daService.update(sql, new Object[]{camera_name,ip,port,cusername,manufacturer,passid,comid,cameraid});
		if(re == 1){
			if(publicMethods.isEtcPark(comid)){
				int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)",
						new Object[]{comid,"com_camera_tb",cameraid,System.currentTimeMillis()/1000,1});
			}
			mongoDbUtils.saveLogs( request,0, 3, "修改了摄像头:"+camera_name);
		}
		return re;
	}

	private int createCamera(HttpServletRequest request){
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		String camera_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "camera_name"));
		String ip = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "ip"));
		String port = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "port"));
		String cusername = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "cusername"));
		String manufacturer = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "manufacturer"));
		Long passid = RequestUtil.getLong(request, "passid", -1L);
		if(comid == -1){
			return -1;
		}
		if(passid == -1){
			return -2;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("comid", comid);
		map.put("passid", passid);
		map.put("camera_name", camera_name);
		map.put("ip", ip);
		map.put("port", port);
		map.put("cusername", cusername);
		map.put("manufacturer", manufacturer);
		Integer result = commonMethods.createCamera(request, map);
		return result;
	}
}
