package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
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
import java.util.List;
import java.util.Map;

public class CityParkAccountAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
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
		}else if(action.equals("query")){
			String sql = "select a.*,w.state from park_account_tb a left join withdrawer_tb w on a.withdraw_id=w.id where " ;
			String countSql = "select count(a.*) from park_account_tb a left join withdrawer_tb w on a.withdraw_id=w.id where " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"park_account","a", new String[]{"state"});
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
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
				sql += " a.comid in ("+preParams+") ";
				countSql += " a.comid in ("+preParams+") ";
				params.addAll(parks);

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
				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by a.create_time desc ",params, pageNum, pageSize);
				}
			}
			setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}

		return null;
	}

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		Integer state = RequestUtil.getInteger(request, "state_start", -1);
		SqlInfo sqlInfo1 = null;
		if(state > -1){
			sqlInfo1 = new SqlInfo(" w.state=? ",new Object[]{state});
		}
		return sqlInfo1;
	}

	private void setList(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> uids = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> map : list){
				uids.add(map.get("uid"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}

			List<Map<String, Object>> list2 = pgOnlyReadService.getAllMap("select id,nickname from user_info_tb where id in ("+preParams+") ", uids);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list){
					Long uid = (Long)map.get("uid");
					for(Map<String, Object> map2 : list2){
						Long id = (Long)map2.get("id");
						if(uid.intValue() == id.intValue()){
							map.put("nickname", map2.get("nickname"));
							break;
						}
					}
				}
			}
		}
	}
}
