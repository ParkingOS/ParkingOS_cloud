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

public class CityMoneySetAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long operater = (Long)request.getSession().getAttribute("loginuin");
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		request.setAttribute("authid", request.getParameter("authid"));
		if(operater == null){
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
			String sql = "select * from money_set_tb where " ;
			String countSql = "select count(*) from money_set_tb where " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"money_set_tb");
			List<Map<String, Object>> list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
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
				count = daService.getCount(countSql,params);
				if(count>0){
					list = pgOnlyReadService.getAll(sql +" order by id desc ",params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){//添加帐号
			Integer mtype = RequestUtil.getInteger(request, "mtype", 0);
			Integer giveto = RequestUtil.getInteger(request, "giveto", 0);
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			if(comid < 0){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Long count = daService.getLong("select count(id) from money_set_tb where comid=? and mtype=? ",
					new Object[]{comid, mtype});
			if(count > 0){
				AjaxUtil.ajaxOutput(response, "-2");
				return null;
			}
			int result = daService.update("insert into money_set_tb (comid,mtype,giveto) values(?,?,?)",
					new Object[]{comid,mtype,giveto});
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Integer mtype = RequestUtil.getInteger(request, "mtype", 0);
			Integer giveto = RequestUtil.getInteger(request, "giveto", 0);
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			if(comid < 0){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Long count = daService.getLong("select count(id) from money_set_tb where comid=? and mtype=? and id<>? ",
					new Object[]{comid, mtype ,id});
			if(count > 0){
				AjaxUtil.ajaxOutput(response, "-2");
				return null;
			}
			int	result = daService.update("update money_set_tb set mtype=?,giveto=? where id=? ",
					new Object[]{mtype,giveto,id});
			AjaxUtil.ajaxOutput(response, ""+result);
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			int	result = daService.update("delete from  money_set_tb  where id=?",
					new Object[]{id});
			AjaxUtil.ajaxOutput(response, ""+result);
		}
		return null;
	}
}
