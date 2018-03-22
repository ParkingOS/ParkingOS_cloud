package com.zld.struts.city;

import com.zld.AjaxUtil;
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

public class City3PLAccountAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
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
		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select a.*,w.state from tingchebao_account_tb a left join withdrawer_tb w on a.withdraw_id=w.id where 1=1 " ;
			String countSql = "select count(a.*) from tingchebao_account_tb a left join withdrawer_tb w on a.withdraw_id=w.id where 1=1 " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"tcb_account", "a", new String[]{"state"});
			SqlInfo sqlInfo1 = getSuperSqlInfo(request);
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
			count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by a.create_time desc ",params, pageNum, pageSize);
			}
			setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}

		return null;
	}

	private void setList(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> uids = new ArrayList<Object>();
			String preParams  ="";
			for(Map<String, Object> map : list){
				uids.add(map.get("uin"));
				if(preParams.equals(""))
					preParams ="?";
				else
					preParams += ",?";
			}

			List<Map<String, Object>> list2 = pgOnlyReadService.getAllMap("select id,nickname from user_info_tb where id in ("+preParams+") ", uids);
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list){
					Long uid = (Long)map.get("uin");
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

	private SqlInfo getSuperSqlInfo(HttpServletRequest request){
		Integer state = RequestUtil.getInteger(request, "state_start", -1);
		SqlInfo sqlInfo1 = null;
		if(state > -1){
			sqlInfo1 = new SqlInfo(" w.state=? ",new Object[]{state});
		}
		return sqlInfo1;
	}
}
