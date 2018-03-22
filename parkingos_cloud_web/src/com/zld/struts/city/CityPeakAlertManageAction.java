package com.zld.struts.city;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CityPeakAlertManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private CommonMethods commonMethods;

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		request.setAttribute("authid", request.getParameter("authid"));
		Long cityid = (Long)request.getSession().getAttribute("cityid");
		if(uin == null || cityid == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			request.setAttribute("cityid", cityid);
			request.setAttribute("from", RequestUtil.processParams(request, "from"));
			List<Map<String, Object>> authList = (List<Map<String, Object>>)request.getSession().getAttribute("authlist");
			if(authList != null){
				for(Map<String, Object> map : authList){
					if(map.get("url") != null){
						String url = (String)map.get("url");
						if(url.contains("cityindex.do")){
							request.setAttribute("index_authid", map.get("auth_id"));
						}
					}
				}
			}
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from city_peakalert_tb where (cityid=? " ;
			String countSql = "select count(*) from city_peakalert_tb where (cityid=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"city_peakalert_tb");
			List<Object> params = new ArrayList<Object>();
			params.add(cityid);
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
				sql += " or comid in ("+preParams+") ";
				countSql += " or comid in ("+preParams+") ";
				params.addAll(parks);
			}
			sql += " )";
			countSql += " )";
			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
			}
			setList(list);
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("export")){
			String sql = "select * from city_peakalert_tb where cityid=? " ;
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"city_peakalert_tb");
			List<Object> params = new ArrayList<Object>();
			params.add(cityid);
			if(sqlInfo!=null){
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			List<Map<String, Object>> list  = pgOnlyReadService.getAll(sql +" order by create_time desc ",params, 0, 0);
			if(list!=null&&!list.isEmpty()){
				String heards[] = new String[]{"编号","标题","状态","内容","处理时间","处理人"};
				List<List<String>> bodyList = new ArrayList<List<String>>();
				for(Map<String, Object> map : list){
					List<String> valueList = new ArrayList<String>();
					valueList.add(map.get("id")+"");
					valueList.add(map.get("title")+"");
					Integer state = (Integer)map.get("state");
					if(state!=null){
						switch (state) {
							case 0:
								valueList.add("新建");
								break;
							case 1:
								valueList.add("已处理");
								break;
							default:
								break;
						}
					}else {
						valueList.add("");
					}
					valueList.add(map.get("content")+"");
					Long htime = (Long)map.get("handle_time");
					if(htime!=null){
						valueList.add(TimeTools.getTime_yyyyMMdd_HHmmss(htime*1000));
					}else {
						valueList.add("");
					}
					valueList.add(map.get("handle_user")+"");
					bodyList.add(valueList);
				}
				String fname = "高峰告警信息" + com.zld.utils.TimeTools.getDate_YY_MM_DD();
				fname = StringUtils.encodingFileName(fname);
				java.io.OutputStream os;
				try {
					os = response.getOutputStream();
					response.reset();
					response.setHeader("Content-disposition", "attachment; filename="
							+ fname + ".xls");
					ExportExcelUtil importExcel = new ExportExcelUtil("告警信息",
							heards, bodyList);
					importExcel.createExcelFile(os);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else if(action.equals("create")){
			String content = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "content"));
			String title = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "title"));
			if(cityid > 0){
				int r = daService.update("insert into city_peakalert_tb (title,state,create_time,content,cityid)" +
								"values(?,?,?,?,?)",
						new Object[]{title,0,System.currentTimeMillis()/1000,content,cityid});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("send")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Long deleteUid = (Long)request.getSession().getAttribute("loginuin");
			int r = daService.update("update city_peakalert_tb set state=?,handle_user=?,handle_time=? where id=? ",
					new Object[]{2,deleteUid,System.currentTimeMillis()/1000, id});
			/**
			 * 处理发布逻辑..........
			 */
			AjaxUtil.ajaxOutput(response, r + "");
		}
		return null;
	}

	private void setList(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			for(Map<String, Object> map : list){
				Long ntime = System.currentTimeMillis()/1000;
				Long create_time = ntime;
				Long end_time = ntime;
				if(map.get("create_time") != null){
					create_time = (Long)map.get("create_time");
				}
				if(map.get("handle_time") != null){
					end_time = (Long)map.get("handle_time");
				}
				String duration = StringUtils.getTimeString(end_time - create_time);
				map.put("duration", duration);
			}
		}
	}
}
