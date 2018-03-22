package com.zld.struts.city;

import com.zld.AjaxUtil;
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

public class CityAlertManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;

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
			String sql = "select * from com_alert_tb where cityid=? " ;
			String countSql = "select count(*) from com_alert_tb where cityid=? " ;
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Long count = 0L;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_alert_tb");

			List<Object> params = new ArrayList<Object>();
			params.add(cityid);

			if(sqlInfo!=null){
				countSql+=" and "+ sqlInfo.getSql();
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			count = daService.getCount(countSql,params);
			if(count>0){
				list = daService.getAll(sql +" order by create_time desc ",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
		}else if(action.equals("export")){
			String sql = "select * from com_alert_tb where cityid=? " ;
			SqlInfo sqlInfo = RequestUtil.customSearch(request,"com_alert_tb");
			List<Object> params = new ArrayList<Object>();
			params.add(cityid);
			if(sqlInfo!=null){
				sql +=" and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			List<Map<String, Object>> list  = pgOnlyReadService.getAll(sql +" order by create_time desc ",params, 0, 0);
			if(list!=null&&!list.isEmpty()){
				String heards[] = new String[]{"编号","来源","状态","类型","内容","处理时间","处理人"};
				List<List<String>> bodyList = new ArrayList<List<String>>();
				for(Map<String, Object> map : list){
					List<String> valueList = new ArrayList<String>();
					valueList.add(map.get("id")+"");
					valueList.add(map.get("source")+"");
					Integer state = (Integer)map.get("state");
					if(state!=null){
						switch (state) {
							case 0:
								valueList.add("新建");
								break;
							case 1:
								valueList.add("已审核");
								break;
							case 2:
								valueList.add("已发布");
								break;
							case 3:
								valueList.add("已取消");
								break;
							default:
								break;
						}
					}else {
						valueList.add("");
					}
					valueList.add(map.get("type")+"级");
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
				String fname = "告警信息" + com.zld.utils.TimeTools.getDate_YY_MM_DD();
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
			Integer type = RequestUtil.getInteger(request, "type", 0);
			String source = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "source"));
			if(cityid > 0){
				int r = daService.update("insert into com_alert_tb (source,type,state,create_time,content,cityid)" +
								"values(?,?,?,?,?,?)",
						new Object[]{source,type,0,System.currentTimeMillis()/1000,content,cityid});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String pda = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "pda"));
			Long groupid = RequestUtil.getLong(request, "groupid", -1L);
			Long uid = RequestUtil.getLong(request, "uid", -1L);
			Integer state = RequestUtil.getInteger(request, "state", 0);
			Long updateUid = (Long)request.getSession().getAttribute("loginuin");

			if(groupid > 0){
				int r = daService.update("update com_alert_tb set pda=?,state=?,groupid=?,uid=?,update_user=?,update_time=? where id=? ",
						new Object[]{pda, state, groupid, uid,updateUid,System.currentTimeMillis()/1000,id});
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "-1");
		}else if(action.equals("send")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Long deleteUid = (Long)request.getSession().getAttribute("loginuin");
			int r = daService.update("update com_alert_tb set state=?,handle_user=?,handle_time=? where id=? ",
					new Object[]{2,deleteUid,System.currentTimeMillis()/1000, id});
			/**
			 * 处理发布逻辑..........
			 */
			AjaxUtil.ajaxOutput(response, r + "");
		}
		return null;
	}
}
