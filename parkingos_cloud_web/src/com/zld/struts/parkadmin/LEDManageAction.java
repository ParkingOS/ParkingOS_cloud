package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LEDManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	private Logger logger = Logger.getLogger(LEDManageAction.class);
	/*
	 * LED设置
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		Long nickname = (Long)request.getSession().getAttribute("loginuin");
		request.setAttribute("authid", request.getParameter("authid"));
		if(nickname == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(comid == 0){
			comid = RequestUtil.getLong(request, "comid", -1L);
		}
		if(action.equals("")){
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select l.id,l.ledip,l.ledport,l.passid,cp.worksite_id from com_led_tb l,com_pass_tb cp where l.passid=cp.id and cp.comid=? order by l.id";
			String sqlcount = "select count(1) from com_led_tb l,com_pass_tb cp where l.passid=cp.id and cp.comid=?";
			Long count = daService.getLong(sqlcount, new Object[]{comid});
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			if(count > 0){
				list = daService.getAll(sql, params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("getworksites")){
			String sql = "select * from com_worksite_tb where comid=?";
			List<Map> list = daService.getAll(sql, new Object[]{comid});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"},";
			if(!list.isEmpty()){
				for(Map map : list){
					result+="{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("worksite_name")+"\"},";
				}
				result = result.substring(0, result.length()-1);
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getname")){
			Long passid = RequestUtil.getLong(request, "passid", -1L);
			String sql = "select passname from com_pass_tb where id=?";
			Map<String, Object> map = new HashMap<String, Object>();
			map = daService.getMap(sql, new Object[]{passid});
			AjaxUtil.ajaxOutput(response, map.get("passname")+"");
		}else if(action.equals("edit")){
			String ledip = RequestUtil.processParams(request, "ledip");
			String ledport = RequestUtil.processParams(request, "ledport");
			Long passid = RequestUtil.getLong(request, "passid", -1L);
			Long ledid = RequestUtil.getLong(request, "id", -1L);
			if(passid == -1 || ledid == -1){
				return null;
			}
			//编辑
			String sql = "update com_led_tb set ledip=?,ledport=?,passid=? where id=?";
			int re = daService.update(sql, new Object[]{ledip,ledport,passid,ledid});
			if(re == 1){
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_led_tb",ledid,System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+nickname+" edit comid:"+comid+" camera ,add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+nickname+" edit comid:"+comid+" camera ");
				}
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
		}
		return null;
	}
}
