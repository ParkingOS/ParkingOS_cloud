package com.zld.struts.city;

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

public class CityPublishManageAction extends Action {
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
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(cityid == null){
			return null;
		}
		if(cityid == null) cityid = -1L;
		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select i.*,a.ad,a.begin_time,a.end_time,a.isactive,a.publish_time from induce_tb i left join induce_ad_tb a on i.id=a.induce_id where i.state=? and i.cityid=? and (i.type=? or i.type=?) and i.is_delete=? " ;
			String countSql = "select count(i.*) from induce_tb i left join induce_ad_tb a on i.id=a.induce_id where i.state=? and i.cityid=? and (i.type=? or i.type=?) and i.is_delete=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
			SqlInfo sqlInfo2 = getSuperSqlInfo2(request);
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"induce_tb","i",new String[]{"comid","isactive"});
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(cityid);
			params.add(0);
			params.add(1);
			params.add(0);
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			if(sqlInfo1 != null){
				countSql+=" and "+ sqlInfo1.getSql();
				sql +=" and "+sqlInfo1.getSql();
				params.addAll(sqlInfo1.getParams());
			}
			if(sqlInfo2 != null){
				countSql+=" and "+ sqlInfo2.getSql();
				sql +=" and "+sqlInfo2.getSql();
				params.addAll(sqlInfo2.getParams());
			}
			count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
			}
			setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("bathpublish")){
			int r = bathpublish(request, uin);
			AjaxUtil.ajaxOutput(response, r + "");
		}

		return null;
	}

	private int bathpublish(HttpServletRequest request, Long publishor){
		String ids = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "ids"));
		String message = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "message"));
		if(message.equals("")) message = null;
		if(ids.equals("")){
			return -1;
		}
		String begin_time = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "begin_time"));
		String end_time = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "end_time"));
		Long btime = null;
		Long etime = null;
		if(!begin_time.equals("")){
			btime = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(begin_time);
		}
		if(!end_time.equals("")){
			etime = TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(end_time);
		}
		Long ntime = System.currentTimeMillis()/1000;
		String[] idList = ids.split(",");
		List<Object> params1 = new ArrayList<Object>();
		params1.add(message);
		params1.add(1);
		params1.add(ntime);
		params1.add(btime);
		params1.add(etime);
		String preParams  ="";
		for(int i = 0; i< idList.length; i++){
			Long induce_id = Long.valueOf(idList[i]);
			params1.add(induce_id);
			if(preParams.equals(""))
				preParams ="?";
			else
				preParams += ",?";
		}
		int r = daService.update("update induce_ad_tb set ad=?,isactive=?,publish_time=?,begin_time=?,end_time=? where induce_id in ("+preParams+") ", params1);
		if(r > 0){
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			for(int i = 0; i< idList.length; i++){
				Long induce_id = Long.valueOf(idList[i]);
				Map<String, Object> hismap = new HashMap<String, Object>();
				hismap.put("sql", "insert into induce_ad_history_tb(induce_id,create_time,begin_time,end_time,ad,creator_id) values(?,?,?,?,?,?) ");
				hismap.put("values", new Object[]{induce_id, ntime, btime, etime, message, publishor});
				bathSql.add(hismap);
			}
			boolean b = daService.bathUpdate(bathSql);
		}
		if(r > 0){
			return 1;
		}
		return 0;
	}

	private void setList(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> creators = new ArrayList<Object>();
			List<Object> updators = new ArrayList<Object>();
			List<Object> induceids = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> map : list){
				creators.add(map.get("creator_id"));
				updators.add(map.get("updator_id"));
				induceids.add(map.get("id"));
				map.put("hcount", 0);
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}

			List<Map<String, Object>> list2 = pgOnlyReadService.getAllMap("select id,nickname from user_info_tb where id in ("+preParams+")", creators);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list){
					Long creator_id = (Long)map.get("creator_id");
					for(Map<String, Object> map2 : list2){
						Long id = (Long)map2.get("id");
						if(creator_id.intValue() == id.intValue()){
							map.put("creator_name", map2.get("nickname"));
							break;
						}
					}
				}
			}

			list2 = pgOnlyReadService.getAllMap("select id,nickname from user_info_tb where id in ("+preParams+")", updators);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list){
					Long creator_id = (Long)map.get("updator_id");
					for(Map<String, Object> map2 : list2){
						Long id = (Long)map2.get("id");
						if(creator_id.intValue() == id.intValue()){
							map.put("update_name", map2.get("nickname"));
							break;
						}
					}
				}
			}


			list2 = pgOnlyReadService.getAllMap("select induce_id,count(id) hcount from induce_ad_history_tb where induce_id in ("+preParams+") group by induce_id ", induceids);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list){
					Long id = (Long)map.get("id");
					for(Map<String, Object> map2 : list2){
						Long induce_id = (Long)map2.get("induce_id");
						if(induce_id.intValue() == id.intValue()){
							map.put("hcount", map2.get("hcount"));
							break;
						}
					}
				}
			}
		}
	}

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		Integer comid = RequestUtil.getInteger(request, "comid_start", -1);
		SqlInfo sqlInfo1 = null;
		if(comid > -1){
			sqlInfo1 = new SqlInfo(" i.id in (select induce_id from induce_park_tb where comid=?) ",new Object[]{comid});
		}
		return sqlInfo1;
	}
	private SqlInfo getSuperSqlInfo2(HttpServletRequest request){
		Integer isactive = RequestUtil.getInteger(request, "isactive_start", -1);
		SqlInfo sqlInfo1 = null;
		if(isactive > -1){
			sqlInfo1 = new SqlInfo(" a.isactive=? ",new Object[]{isactive});
		}
		return sqlInfo1;
	}
}
