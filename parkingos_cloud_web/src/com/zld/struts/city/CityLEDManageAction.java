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

public class CityLEDManageAction extends Action {
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
			String sql = "select * from com_led_tb where " ;
			String countSql = "select count(*) from com_led_tb where " ;
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
			String sql = "select * from com_led_tb where " ;
			String countSql = "select count(*) from com_led_tb where " ;
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
			int r = createLED(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editLED(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("delete")){
			int r = deleteLED(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}

		return null;
	}

	private int deleteLED(HttpServletRequest request){
		Long cameraid = RequestUtil.getLong(request, "selids", -1L);
		Map<String, Object> ledMap = daService.getMap("select * from com_led_tb where id=?",
				new Object[]{cameraid});
		Long comid = -1L;
		if(ledMap != null && ledMap.get("comid") != null){
			comid = (Long)ledMap.get("comid");
		}
		String sql = "delete from com_led_tb where id=?";
		int result = daService.update(sql, new Object[]{cameraid});
		if(result == 1){
			if(publicMethods.isEtcPark(comid)){
				int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_led_tb",cameraid,System.currentTimeMillis()/1000,2});
			}
			mongoDbUtils.saveLogs(request, 0, 4, "删除了（comid:"+comid+"）的LED："+ledMap);
		}
		return result;
	}

	private int editLED(HttpServletRequest request){
		Long ledid = RequestUtil.getLong(request, "id", -1L);
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		Long passid = RequestUtil.getLong(request, "passid", -1L);
		String ledip = RequestUtil.processParams(request, "ledip");
		String ledport = RequestUtil.processParams(request, "ledport");
		String leduid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "leduid"));
		Integer movemode = RequestUtil.getInteger(request, "movemode", -1);
		Integer movespeed = RequestUtil.getInteger(request, "movespeed", -1);
		Long dwelltime = RequestUtil.getLong(request, "dwelltime", -1L);
		Integer ledcolor = RequestUtil.getInteger(request, "ledcolor", -1);
		Integer showcolor = RequestUtil.getInteger(request, "showcolor", -1);
		Integer typeface = RequestUtil.getInteger(request, "typeface", -1);
		Integer typesize = RequestUtil.getInteger(request, "typesize", -1);
		String matercont = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "matercont"));
		Integer width = RequestUtil.getInteger(request, "width", 128);
		Integer height = RequestUtil.getInteger(request, "height", 32);
		Integer type = RequestUtil.getInteger(request, "type", 0);
		Integer rsport = RequestUtil.getInteger(request, "rsport", 1);
		if(movemode == -1) movemode = null;
		if(movespeed == -1) movespeed = null;
		if(dwelltime == -1) dwelltime = null;
		if(ledcolor == -1) ledcolor = null;
		if(showcolor == -1) showcolor = null;
		if(typeface == -1) typeface = null;
		if(typesize == -1) typesize = null;
		if(comid == -1){
			return -1;
		}
		if(passid == -1){
			return -2;
		}

		//编辑
		String sql = "update com_led_tb set ledip=?,ledport=?,leduid=?,movemode=?,movespeed=?,dwelltime=?,ledcolor=?,showcolor=?,typeface=?,typesize=?,matercont=?,passid=?,width=?,height=?,type=?,rsport=?,comid=? where id=?";
		int re = daService.update(sql, new Object[]{ledip,ledport,leduid,movemode,movespeed,dwelltime,ledcolor,showcolor,typeface,typesize,matercont,passid,width,height,type,rsport,comid,ledid});
		if(re == 1){
			if(publicMethods.isEtcPark(comid)){
				int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)",
						new Object[]{comid,"com_led_tb",ledid,System.currentTimeMillis()/1000,1});
			}
			mongoDbUtils.saveLogs(request, 0, 3, "修改了（comid:"+comid+"）的LED："+ledip+":"+ledport);
		}
		return re;
	}

	private int createLED(HttpServletRequest request){
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		Long passid = RequestUtil.getLong(request, "passid", -1L);
		String ledip = RequestUtil.processParams(request, "ledip");
		String ledport = RequestUtil.processParams(request, "ledport");
		String leduid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "leduid"));
		Integer movemode = RequestUtil.getInteger(request, "movemode", -1);
		Integer movespeed = RequestUtil.getInteger(request, "movespeed", -1);
		Long dwelltime = RequestUtil.getLong(request, "dwelltime", -1L);
		Integer ledcolor = RequestUtil.getInteger(request, "ledcolor", -1);
		Integer showcolor = RequestUtil.getInteger(request, "showcolor", -1);
		Integer typeface = RequestUtil.getInteger(request, "typeface", -1);
		Integer typesize = RequestUtil.getInteger(request, "typesize", -1);
		String matercont = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "matercont"));
		Integer width = RequestUtil.getInteger(request, "width", 128);
		Integer height = RequestUtil.getInteger(request, "height", 32);
		Integer type = RequestUtil.getInteger(request, "type", 0);
		Integer rsport = RequestUtil.getInteger(request, "rsport", 1);
		if(movemode == -1) movemode = null;
		if(movespeed == -1) movespeed = null;
		if(dwelltime == -1) dwelltime = null;
		if(ledcolor == -1) ledcolor = null;
		if(showcolor == -1) showcolor = null;
		if(typeface == -1) typeface = null;
		if(typesize == -1) typesize = null;
		if(comid == -1){
			return -1;
		}
		if(passid == -1){
			return -2;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("comid", comid);
		map.put("passid", passid);
		map.put("ledip", ledip);
		map.put("ledport", ledport);
		map.put("leduid", leduid);
		map.put("movemode", movemode);
		map.put("movespeed", movespeed);
		map.put("dwelltime", dwelltime);
		map.put("ledcolor", ledcolor);
		map.put("showcolor", showcolor);
		map.put("typeface", typeface);
		map.put("typesize", typesize);
		map.put("matercont", matercont);
		map.put("width", width);
		map.put("height", height);
		map.put("type", type);
		map.put("rsport", rsport);
		Integer result = commonMethods.createLED(request, map);
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
}
