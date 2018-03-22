package com.zld.struts.anlysis;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class EmployeeCountAction extends Action {
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;
	@Autowired
	private DataBaseService daService;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	public ActionForward execute(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response) throws Exception{
		String action = RequestUtil.processParams(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		Long groupid = (Long)request.getSession().getAttribute("groupid");
		Long comid=(Long)request.getSession().getAttribute("comid");
		Integer pageNum = RequestUtil.getInteger(request, "page", 1);
		Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
		if(uin == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(cityid == null) cityid = -1L;
		if(groupid == null) groupid = -1L;
		if(action.equals("")){
			SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			request.setAttribute("btime", df2.format(System.currentTimeMillis()));
			request.setAttribute("etime",  df2.format(System.currentTimeMillis()));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			/*SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
			String nowtime= df2.format(System.currentTimeMillis());
			String sql = "select a.comid , sum(total)as total,count(comid) as count , b.company_name from   no_payment_tb  as a  left join  com_info_tb as b on a.comid=b.id  where a.create_time between ? and ? and a.state=? ";
			String countSql ="select count(*)  from   no_payment_tb  as a  left join  com_info_tb as b on a.comid=b.id  where a.create_time between ? and ? and a.state=?"; 
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> list = null;
			Long count = 0L;
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			if(btime.equals(""))
				btime = nowtime;
			if(etime.equals(""))
				etime = nowtime;
			Long b = TimeTools.getLongMilliSecondFrom_HHMMDD(btime)/1000;
			Long e =  TimeTools.getLongMilliSecondFrom_HHMMDDHHmmss(etime+" 23:59:59");
			List<Object> params = new ArrayList<Object>();
			params.add(b);
			params.add(e);
			params.add(0);
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}else if(comid > 0){
				parks.add(comid);
			}
			if(parks != null && !parks.isEmpty()){
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				sql += " and a.comid in ("+preParams+") ";
				countSql += " and a.comid in ("+preParams+") ";
				params.addAll(parks);
				sql += "group  by a.comid,b.company_name";
				//countSql += "group  by a.comid,b.company_name ";
				//list = pgOnlyReadService.getAllMap(sql,params);
			    count=daService.getCount(countSql, params);
				if(count>0){
					list = daService.getAll(sql, params, pageNum, pageSize);
				}
			}
			//setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			
			//json = StringUtils.createJson(list);
			AjaxUtil.ajaxOutput(response, json);*/
			List<Map<String, Object>> list = null;
			String json = JsonUtil.Map2Json(null,pageNum,0, "","id");
			AjaxUtil.ajaxOutput(response, json);
		}
		return null;
	}



}
