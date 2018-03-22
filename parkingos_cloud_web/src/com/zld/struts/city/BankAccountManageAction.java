package com.zld.struts.city;

import com.zld.AjaxUtil;
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

public class BankAccountManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;

	@SuppressWarnings({ "rawtypes", "unused" })
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Integer type = RequestUtil.getInteger(request, "type", 0);//账户类型 0:公司，1个人 2对公
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
			request.setAttribute("cityid", cityid);
			request.setAttribute("type", type);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from com_account_tb where type=? " ;
			String countSql = "select count(id) from com_account_tb where type=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_account_tb");
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(type);
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
			}
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
}
