package com.zld.struts.group;

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

public class  WorkGroupManageAction extends Action {
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
			return mapping.findForward("list");
		}else if(action.equals("addberthsec")){
			Long work_group_id = RequestUtil.getLong(request, "work_group_id", -1L);
			request.setAttribute("work_group_id", work_group_id);
			return mapping.findForward("berthseclist");
		}else if(action.equals("addemployee")){
			Long work_group_id = RequestUtil.getLong(request, "work_group_id", -1L);
			request.setAttribute("work_group_id", work_group_id);
			return mapping.findForward("employeelist");
		}else if(action.equals("addinspector")){
			Long work_group_id = RequestUtil.getLong(request, "work_group_id", -1L);
			request.setAttribute("work_group_id", work_group_id);
			return mapping.findForward("inspectorlist");
		}else if(action.equals("saveberthsec")){
			Long work_group_id = RequestUtil.getLong(request, "work_group_id", -1L);
			request.setAttribute("work_group_id", work_group_id);
			return mapping.findForward("saveberthseclist");
		}else if(action.equals("saveemployee")){
			Long work_group_id = RequestUtil.getLong(request, "work_group_id", -1L);
			request.setAttribute("work_group_id", work_group_id);
			return mapping.findForward("saveemployeelist");
		}else if(action.equals("saveinspector")){
			Long work_group_id = RequestUtil.getLong(request, "work_group_id", -1L);
			request.setAttribute("work_group_id", work_group_id);
			return mapping.findForward("saveinspectorlist");
		}else if(action.equals("berthsecquery")){
			String sql = "select  a.id ,b.berthsec_name ,a.work_group_id,a.state  from   work_berthsec_tb as a  left join  com_berthsecs_tb as b on a.berthsec_id=b.id  where a.work_group_id=? and a.is_delete=?  ";
			String countSql = "select count(*)from work_berthsec_tb as a  left join  com_berthsecs_tb as b on a.berthsec_id=b.id where a.work_group_id=? and a.is_delete=? ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			Long work_group_id = RequestUtil.getLong(request, "work_group_id", -1L);
			List<Object> params = new ArrayList<Object>();
			params.add(work_group_id);
			params.add(0);
			count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by a.id desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("employeequery")){
			String sql = "select  a.id ,b.nickname ,a.work_group_id,b.auth_flag,a.employee_id  from   work_employee_tb  as a  left join  user_info_tb  as b on a.employee_id=b.id  where a.work_group_id=? and a.state=? and b.auth_flag =? ";
			String countSql = "select count(*)from  work_employee_tb  as a  left join  user_info_tb  as b on a.employee_id=b.id  where a.work_group_id=? and a.state=? and b.auth_flag =? ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			Long work_group_id = RequestUtil.getLong(request, "work_group_id", -1L);
			List<Object> params = new ArrayList<Object>();
			params.add(work_group_id);
			params.add(0);
			params.add(2);
			count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by a.id desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("inspectorquery")){
			String sql = "select  a.id ,b.nickname ,a.work_group_id,b.auth_flag  from   work_inspector_tb  as a  left join  user_info_tb  as b on a.inspector_id=b.id  where a.work_group_id=? and a.state=? and b.auth_flag =? ";
			String countSql = "select count(*)from  work_inspector_tb  as a  left join  user_info_tb  as b on a.inspector_id=b.id  where a.work_group_id=? and a.state=? and b.auth_flag =? ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			Long work_group_id = RequestUtil.getLong(request, "work_group_id", -1L);
			List<Object> params = new ArrayList<Object>();
			params.add(work_group_id);
			params.add(0);
			params.add(16);
			count = pgOnlyReadService.getCount(countSql,params);
			if(count>0){
				list = pgOnlyReadService.getAll(sql +" order by a.id desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("insertberthsec")){
			int r = createBerthsec(request);
			AjaxUtil.ajaxOutput(response, "" + r);

		}else if(action.equals("insertemployee")){
			int r = createEmployee(request);
			AjaxUtil.ajaxOutput(response, "" + r);

		}else if(action.equals("insertinspector")){
			int r = createInspector(request);
			AjaxUtil.ajaxOutput(response, "" + r);

		}else if(action.equals("quickquery")){
			String sql = "select  * from  work_group_tb  where company_id=? " ;
			String countSql = "select count(*) from work_group_tb where company_id=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			List<Object> parks = null;
			params.add(groupid);
			count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql + " order by create_time desc ", params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("query")){
			String sql = "select * from com_berthsecs_tb where is_active=? " ;
			String countSql = "select count(*) from com_berthsecs_tb where is_active=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_berthsecs_tb");
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
				sql += "  comid in ("+preParams+") ";
				countSql += "  comid in ("+preParams+") ";
				params.addAll(parks);
				if(sqlInfo!=null){
					countSql+=" and "+ sqlInfo.getSql();
					sql +=" and "+sqlInfo.getSql();
					params.addAll(sqlInfo.getParams());
				}
				count = daService.getCount(countSql,params);
				if(count>0){
					list = daService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("queryallberthsec")){
			String sql = "select * from com_berthsecs_tb  where id not in(   select berthsec_id from  work_berthsec_tb where is_delete=? and work_group_id>?) and is_active=? " ;
			String countSql = "select count(*) from com_berthsecs_tb  where id not in(   select berthsec_id from  work_berthsec_tb where is_delete=? and work_group_id>?) and  is_active=?  " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			List<Object> parks = null;
			params.add(0);
			params.add(0);
			params.add(0);
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
				sql += " and comid in ("+preParams+") ";
				countSql += "and comid in ("+preParams+") ";
				params.addAll(parks);
				count = daService.getCount(countSql, params);
				if(count>0){
					list = daService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
				}
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("queryallemployee")){
			String sql = "select * from user_info_tb  where id not in(   select employee_id from  work_employee_tb where state=0) and auth_flag=?  and groupid=? and state=?" ;
			String countSql = "select count(*) from user_info_tb  where id not in(   select employee_id from  work_employee_tb where state=0) and  auth_flag=? and groupid=? and state=?" ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(2);
			params.add(groupid);
			params.add(0);

			count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql ,params, pageNum, pageSize);
			}

			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("queryallinspector")){
			String sql = "select * from user_info_tb  where id not in(   select inspector_id from  work_inspector_tb where state=0) and auth_flag=?  and groupid=? and state=?" ;
			String countSql = "select count(*) from user_info_tb  where id not in(   select inspector_id from  work_inspector_tb where state=0) and  auth_flag=? and groupid=? and state=?" ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List list = null;
			Long count = 0L;
			List<Object> params = new ArrayList<Object>();
			params.add(16);
			params.add(groupid);
			params.add(0);

			count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql ,params, pageNum, pageSize);
			}

			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("create")){
			int r = createGroup(request);
			AjaxUtil.ajaxOutput(response, "" + r);
		}else if(action.equals("edit")){
			int r = editGroup(request);
			AjaxUtil.ajaxOutput(response, "" + r);
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			daService.update("delete from  work_berthsec_tb  where work_group_id=?",
					new Object[]{id});
			daService.update("delete from work_employee_tb  where work_group_id=? ",
					new Object[]{id});
			int r = daService.update("delete from work_group_tb where id=? ",
					new Object[]{id});
			AjaxUtil.ajaxOutput(response, "" + r);
		}
		else if(action.equals("deleteberthsec")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map berthMap = daService.getMap("select berthsec_id from work_berthsec_tb  where id=? ",
					new Object[]{id});
			if(berthMap!=null&&berthMap.get("berthsec_id")!=null){
				Map map = daService.getMap("select * from parkuser_work_record_tb where berthsec_id = ? and end_time is null", new Object[]{(Long)berthMap.get("berthsec_id")});
				if(map!=null){
					AjaxUtil.ajaxOutput(response, "该泊位段有人正在上班，不能删除！");
					return null;
				}
			}
			int r = daService.update("delete from  work_berthsec_tb  where id=? and state=? ",
					new Object[]{id,0});
			AjaxUtil.ajaxOutput(response, "" + r);
		}
		else if(action.equals("deleteemployee")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map employeeMap = daService.getMap("select employee_id from work_employee_tb  where id=? ",
					new Object[]{id});
			if(employeeMap!=null&&employeeMap.get("employee_id")!=null){
				Map map = daService.getMap("select * from parkuser_work_record_tb where uid = ? and end_time is null", new Object[]{(Long)employeeMap.get("employee_id")});
				if(map!=null){
					AjaxUtil.ajaxOutput(response, "该收费员正在上班，不能删除！");
					return null;
				}
			}
			int r = daService.update("delete from work_employee_tb  where id=? ",
					new Object[]{id});
			AjaxUtil.ajaxOutput(response, "" + r);
		}
		else if(action.equals("deleteinspector")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			int r = daService.update("delete from work_inspector_tb  where id=? ",
					new Object[]{id});
			AjaxUtil.ajaxOutput(response, "" + r);
		}

		return null;
	}

	private int editGroup(HttpServletRequest request){
		Long id = RequestUtil.getLong(request, "id", -1L);
		String workgroup_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "workgroup_name"));
		Integer is_active = RequestUtil.getInteger(request, "is_active", 0);
		int r = daService.update("update work_group_tb set workgroup_name=?,update_time=?,is_active=? where id=? ",
				new Object[]{workgroup_name,  System.currentTimeMillis()/1000,is_active, id});
		return r;
	}

	private int createGroup(HttpServletRequest request){
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		String workgroup_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "workgroup_name"));
		Integer is_active=RequestUtil.getInteger(request, "is_active",0);
		int r = daService.update("insert into work_group_tb(workgroup_name,create_time,is_active,company_id) values(?,?,?,?)",
				new Object[]{workgroup_name,System.currentTimeMillis()/1000,1,groupid});
		return r;
	}
	private int createBerthsec(HttpServletRequest request){
		String ids =  AjaxUtil.decodeUTF8(RequestUtil.getString(request, "ids"));
		Long work_group_id = RequestUtil.getLong(request, "work_group_id", -1L);
		String cids []= ids.split(",");
		String paramssimp = "";
		List<Object> params = new ArrayList<Object>();
		int r=0;
		for(String id : cids){
			long ID = Long.parseLong(id);
			r=daService.update("insert into work_berthsec_tb(work_group_id,berthsec_id,state) values(?,?,?)",
					new Object[]{work_group_id,ID,0});
			//paramssimp +=",?";
			//params.add(Long.valueOf(id));
		}
		//paramssimp = paramssimp.substring(1);
		//int r=1;
		/*int r = daService.update("insert into work_group_tb(workgroup_name,create_time,is_active,company_id) values(?,?,?,?)", 
				new Object[]{workgroup_name,System.currentTimeMillis()/1000,is_active,groupid});*/
		return r;
	}
	private int createEmployee(HttpServletRequest request){
		String ids =  AjaxUtil.decodeUTF8(RequestUtil.getString(request, "ids"));
		Long work_group_id = RequestUtil.getLong(request, "work_group_id", -1L);
		String cids []= ids.split(",");
		String paramssimp = "";
		List<Object> params = new ArrayList<Object>();
		int r=0;
		for(String id : cids){
			long ID = Long.parseLong(id);
			r=daService.update("insert into work_employee_tb(work_group_id,employee_id,state) values(?,?,?)",
					new Object[]{work_group_id,ID,0});
			//paramssimp +=",?";
			//params.add(Long.valueOf(id));
		}
		//paramssimp = paramssimp.substring(1);
		//int r=1;
		/*int r = daService.update("insert into work_group_tb(workgroup_name,create_time,is_active,company_id) values(?,?,?,?)", 
				new Object[]{workgroup_name,System.currentTimeMillis()/1000,is_active,groupid});*/
		return r;
	}
	private int createInspector(HttpServletRequest request){
		String ids =  AjaxUtil.decodeUTF8(RequestUtil.getString(request, "ids"));
		Long work_group_id = RequestUtil.getLong(request, "work_group_id", -1L);
		String cids []= ids.split(",");
		String paramssimp = "";
		List<Object> params = new ArrayList<Object>();
		int r=0;
		for(String id : cids){
			long ID = Long.parseLong(id);
			r=daService.update("insert into work_inspector_tb(work_group_id,inspector_id,state) values(?,?,?)",
					new Object[]{work_group_id,ID,0});
			//paramssimp +=",?";
			//params.add(Long.valueOf(id));
		}
		//paramssimp = paramssimp.substring(1);
		//int r=1;
		/*int r = daService.update("insert into work_group_tb(workgroup_name,create_time,is_active,company_id) values(?,?,?,?)",
				new Object[]{workgroup_name,System.currentTimeMillis()/1000,is_active,groupid});*/
		return r;
	}
}
