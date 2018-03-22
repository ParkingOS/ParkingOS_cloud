package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.TimeTools;
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

public class CitySIMManageAction extends Action {
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
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(cityid == null && groupid == null){
			return null;
		}
		if(cityid == null) cityid = -1L;
		if(groupid == null) groupid = -1L;

		if(cityid > 0){
			groupid = RequestUtil.getLong(request, "groupid", -1L);
		}
		if(action.equals("")){
			request.setAttribute("cityid", cityid);
			return mapping.findForward("list");
		}else if(action.equals("quickquery")){
			String sql = "select * from sim_tb where state=? " ;
			String countSql = "select count(*) from sim_tb where state=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			List<Object> groups = new ArrayList<Object>();
			if(cityid > 0){//城市商户登录
				groups = commonMethods.getGroups(cityid);
			}else if(groupid > 0){//运营集团登录
				groups.add(groupid);
			}
			if(groups != null && !groups.isEmpty()){
				String preParams  ="";
				for(Object grouid : groups){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " and groupid in ("+preParams+") ";
				countSql += " and groupid in ("+preParams+") ";
				params.addAll(groups);

				count = pgOnlyReadService.getCount(countSql,params);
				if(count>0){
					list = pgOnlyReadService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
				}
			}
			setName(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			int r = createSIM(request, uin, groupid);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("edit")){
			int r = editSIM(request, groupid);
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("delete")){
			int r = deleteSIM(request);
			AjaxUtil.ajaxOutput(response, r + "");
		}

		return null;
	}

	private void setName(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> uins = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> map : list){
				uins.add(map.get("creator_id"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}

			List<Map<String, Object>> list2 = pgOnlyReadService.getAllMap("select id,nickname from user_info_tb where id in ("+preParams+") ", uins);
			for(Map<String, Object> map : list){
				Long creator_id = (Long)map.get("creator_id");
				for(Map<String, Object> map2 : list2){
					Long id = (Long)map2.get("id");
					if(creator_id.intValue() == id.intValue()){
						map.put("nickname", map2.get("nickname"));
						break;
					}
				}
			}
		}
	}

	private int deleteSIM(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		int r = daService.update("update sim_tb set state=? where id=? ",
				new Object[]{1, id});
		return r;
	}

	private int editSIM(HttpServletRequest request, Long groupid){
		Long id = RequestUtil.getLong(request, "id", -1L);
		String pin = RequestUtil.processParams(request, "pin");
		String mobile = RequestUtil.processParams(request, "mobile");
		Double money = RequestUtil.getDouble(request, "money", 0d);
		String limit_time = RequestUtil.processParams(request, "limit_time");
		Long limit = TimeTools.getLongMilliSecondFrom_HHMMDD(limit_time)/1000;
		Integer type = RequestUtil.getInteger(request, "type", 0);
		Long device_id = RequestUtil.getLong(request, "device_id", -1L);
		Map<String, Object> simMap = pgOnlyReadService.getMap("select device_id,type,allot_time from sim_tb where id=? ",
				new Object[]{id});
		Long allot_time = null;
		if(groupid == -1){
			return -1;
		}
		if(device_id > 0 && type == 0){
			return -2;
		}
		if(device_id > 0){
			Integer ptype = (Integer)simMap.get("type");
			Long pdeviceid = (Long)simMap.get("device_id");
			if(simMap.get("allot_time") != null){
				allot_time = (Long)simMap.get("allot_time");
			}
			if(!(ptype.intValue() == type.intValue() && (device_id.intValue() == pdeviceid.intValue()))){
				allot_time = System.currentTimeMillis()/1000;
			}
		}
		int r  = daService.update("update sim_tb set pin=?,mobile=?,money=?,limit_time=?,type=?,device_id=?,allot_time=? where id=? ",
				new Object[]{pin, mobile, money, limit, type, device_id, allot_time, id});
		return r;
	}

	private int createSIM(HttpServletRequest request, Long creator_id, Long groupid){
		String pin = RequestUtil.processParams(request, "pin");
		String mobile = RequestUtil.processParams(request, "mobile");
		Double money = RequestUtil.getDouble(request, "money", 0d);
		String limit_time = RequestUtil.processParams(request, "limit_time");
		Long limit = TimeTools.getLongMilliSecondFrom_HHMMDD(limit_time)/1000;
		Integer type = RequestUtil.getInteger(request, "type", 0);
		Long device_id = RequestUtil.getLong(request, "device_id", -1L);
		Long allot_time = null;
		if(device_id > 0 && type == 0){
			return -2;
		}
		if(device_id > 0){
			allot_time = System.currentTimeMillis()/1000;
		}
		if(groupid == -1){
			return -1;
		}
		int r = daService.update("insert into sim_tb(pin,mobile,money,create_time,limit_time,allot_time,creator_id,type,device_id,groupid) values(?,?,?,?,?,?,?,?,?,?)",
				new Object[]{pin, mobile, money, System.currentTimeMillis()/1000,limit, allot_time, creator_id, type, device_id, groupid});
		return r;
	}
}
