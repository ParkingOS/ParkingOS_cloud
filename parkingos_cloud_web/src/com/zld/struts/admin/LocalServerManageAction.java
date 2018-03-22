package com.zld.struts.admin;

import com.zld.AjaxUtil;
import com.zld.impl.MemcacheUtils;
import com.zld.service.DataBaseService;
import com.zld.service.LogService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.SqlInfo;
import com.zld.utils.TimeTools;
import org.apache.log4j.Logger;
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

public class LocalServerManageAction extends Action{

	@Autowired
	private DataBaseService daService;
	@Autowired
	private LogService logService;
	@Autowired
	private MemcacheUtils memcacheUtils;

	private Logger logger = Logger.getLogger(MarketerManageAction.class);

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		String action = RequestUtil.processParams(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		Long chanid = (Long)request.getSession().getAttribute("chanid");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long groupid = (Long)request.getSession().getAttribute("groupid");//登录的用户id
		if(uin==null){
			response.sendRedirect("login.do");
			return null;
		}
		Integer supperadmin = (Integer)request.getSession().getAttribute("supperadmin");//是否是超级管理员
		if(action.equals("")){
			return mapping.findForward("list");
		}else if("quickquery".equals(action)){
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> params = new ArrayList<Object>();
			String sql = "select t.*,c.company_name from local_info_tb t left join com_info_tb c on t.comid=c.id ";
			String countsql = "select count(*) from local_info_tb t left join com_info_tb c on t.comid=c.id  ";
			if(supperadmin == 0 ){
				if(chanid != null && chanid > 0){
					sql += " where c.chanid=? ";
					countsql += " where c.chanid=? ";
					params.add(chanid);
				}else if(groupid != null && groupid > 0){
					sql += " where c.groupid=? ";
					countsql += " where c.groupid=? ";
					params.add(groupid);
				}
			}
			Long count = daService.getCount(countsql, params);
			if(count > 0){
				list = daService.getAll(sql+" order by comid desc", params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("query")){
			String sql = "select * from local_info_tb where   ";
			String countSql = "select count(*) from local_info_tb where " ;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			SqlInfo base = new SqlInfo("1=1", new Object[]{});
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"local_info_tb");
			Object[] values = null;
			List<Object> params = null;
			if(sqlInfo!=null){
				sqlInfo = SqlInfo.joinSqlInfo(base,sqlInfo, 2);
				countSql+=" "+ sqlInfo.getSql();
				sql +=" "+sqlInfo.getSql();
				values = sqlInfo.getValues();
				params = sqlInfo.getParams();
			}else {
				values = base.getValues();
				params= base.getParams();
			}
			//System.out.println(sqlInfo);
			Long count= daService.getLong(countSql, values);
			List list = null;//daService.getPage(sql, null, 1, 20);
			if(count>0){
				list = daService.getAll(sql + " order by id desc", params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if("edit".equals(action)){
			Integer isupdate = RequestUtil.getInteger(request, "is_update", -1);
			String limit_time =RequestUtil.processParams(request, "limit_time");
			String secret =RequestUtil.processParams(request, "secret");
			String remark =AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "remark"));
			Long time = TimeTools.getLongMilliSecondFrom_HHMMDD(limit_time)/1000;
			Integer id = RequestUtil.getInteger(request, "id", -1);
			int r = daService.update("update local_info_tb set is_update=?,limit_time=?,secret=?,remark=? where id = ? ", new Object[]{isupdate,time,secret,remark,id});
			AjaxUtil.ajaxOutput(response, r+"");
		}else if("add".equals(action)){
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			Long count = daService.getLong("select count(id) from local_info_tb where comid = ? ", new Object[]{comid});
			if(count!=null&&count>0){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Integer isupdate = RequestUtil.getInteger(request, "is_update", -1);
			String limit_time =(System.currentTimeMillis()/1000+10*365*24*60*60)+"";//RequestUtil.processParams(request, "limit_time");
			String secret =RequestUtil.processParams(request, "secret");
			Long time = Long.parseLong(limit_time);//TimeTools.getLongMilliSecondFrom_HHMMDD(limit_time);
			int r = daService.update("insert into local_info_tb(comid,is_update,limit_time,secret) values(?,?,?,?)", new Object[]{comid,isupdate,time,secret});
			if(r==1){
				List<Long> tcache = memcacheUtils.doListLongCache("etclocal_park_cache", null, null);
				if(tcache!=null&&!tcache.contains(comid)){
					tcache.add(comid);
				}else {
					tcache = new ArrayList<Long>();
					List all = daService.getAll("select comid from local_info_tb", null);
					for (Object object : all) {
						Map map = (Map)object;
						Long obj = Long.valueOf(map.get("comid")+"");
						tcache.add(obj);
					}
				}
				memcacheUtils.doListLongCache("etclocal_park_cache", tcache, "update");
			}
			AjaxUtil.ajaxOutput(response, r+"");
		}else if(action.equals("checkcom")){
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			if(supperadmin == 1){
				AjaxUtil.ajaxOutput(response, "0");
				return null;
			}
			if(comid > 0){
				if(chanid != null && chanid > 0){
					Long count = daService.getLong("select count(id) from com_info_tb where id=? and chanid=? ",
							new Object[]{comid, chanid});
					if(count > 0){
						count = daService.getLong("select count(id) from local_info_tb where comid=? ", new Object[]{comid});
						if(count == 0){
							AjaxUtil.ajaxOutput(response, "0");
							return null;
						}
						AjaxUtil.ajaxOutput(response, "1");
						return null;
					}
				}else if(groupid != null && groupid > 0){
					Long count = daService.getLong("select count(id) from com_info_tb where id=? and groupid=? ",
							new Object[]{comid, groupid});
					if(count > 0){
						count = daService.getLong("select count(id) from local_info_tb where comid=? ", new Object[]{comid});
						if(count == 0){
							AjaxUtil.ajaxOutput(response, "0");
							return null;
						}
						AjaxUtil.ajaxOutput(response, "1");
						return null;
					}
				}
			}
			AjaxUtil.ajaxOutput(response, "1");
		}else if("findMem".equals(action)){//辅助验证查看memcached
			//180.150.188.224:8080/tcbcloud/localserver.do?action=findMem
			List<Long> tcache = memcacheUtils.doListLongCache("etclocal_park_cache", null, null);
			String res = "";
			if(tcache!=null){
				for (Long long1 : tcache) {
					res +=long1+",";
				}
			}
			AjaxUtil.ajaxOutput(response, res);
		}

		return null;
	}

}
