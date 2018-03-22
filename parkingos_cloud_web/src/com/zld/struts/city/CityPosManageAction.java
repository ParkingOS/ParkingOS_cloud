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

public class CityPosManageAction extends Action {
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
		if(uin == null || cityid == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			request.setAttribute("cityid", cityid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select m.*,u.nickname from mobile_tb m left join user_info_tb u on m.uid=u.id where 1=1 " ;
			String countSql = "select count(*) from mobile_tb m where 1=1 " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"mobile_tb","m",new String[]{"device_auth"});
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
				sql += " and m.comid in ("+preParams+") ";
				countSql += " and m.comid in ("+preParams+") ";
				params.addAll(parks);
			}
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
		}else if(action.equals("create")){
			Long uid = RequestUtil.getLong(request, "uid", -1L);
			String name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "pda"));
			Long createUid = (Long)request.getSession().getAttribute("loginuin");
			Integer state = RequestUtil.getInteger(request, "state", 0);
			if(groupid > 0){
				Long ntime  =System.currentTimeMillis()/1000;
				int r = daService.update("insert into mobile_tb(pda,state,uid,groupid,create_time,create_user,update_time,update_user,is_deleted) " +
								"values(?,?,?,?,?,?,?,?,?)",
						new Object[]{name,state,uid, groupid,ntime,createUid,ntime,createUid,0});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String pda = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "pda"));
			Long uid = RequestUtil.getLong(request, "uid", -1L);
			Integer state = RequestUtil.getInteger(request, "state", 0);
			Long updateUid = (Long)request.getSession().getAttribute("loginuin");

			if(groupid > 0){
				int r = daService.update("update mobile_tb set pda=?,state=?,groupid=?,uid=?,update_user=?,update_time=? where id=? ",
						new Object[]{pda, state, groupid, uid,updateUid,System.currentTimeMillis()/1000,id});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Long deleteUid = (Long)request.getSession().getAttribute("loginuin");

			int r = daService.update("update mobile_tb set state=?,delete_user=?,delete_time=? where id=? ",
					new Object[]{1,deleteUid,System.currentTimeMillis()/1000, id});
			AjaxUtil.ajaxOutput(response, r + "");
		}else if(action.equals("deviceauth")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Integer auth = RequestUtil.getInteger(request, "device_auth", 0);
			Long loguse = (Long)request.getSession().getAttribute("loginuin");
			Long time = System.currentTimeMillis()/1000;
			int ret =0;
			if(id>0){
				String sql = "update mobile_tb set device_auth=? ,auth_user=?,auth_time=? where id =? ";
				ret = daService.update(sql, new Object[]{auth,loguse,time,id});
			}
			AjaxUtil.ajaxOutput(response, ret+"");
		}
		return null;
	}
}
