package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.utils.HttpProxy;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	@Autowired
	private CommonMethods commonMethods;

	private Logger logger = Logger.getLogger(PassManageAction.class);

	/*
	 * 通道设置
	 */
	@Override
	public ActionForward execute(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response) throws Exception{
		String action = RequestUtil.processParams(request, "action");
		Long comid = (Long)request.getSession().getAttribute("comid");
		Long operater= (Long)request.getSession().getAttribute("loginuin");
		request.setAttribute("authid", request.getParameter("authid"));
		if(operater == null){
			response.sendRedirect("login.do");
			return null;
		}
		if(comid == 0){
			comid = RequestUtil.getLong(request, "comid", -1L);
		}
		if(action.equals("")){
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("passquery")){
			//String sql = "select cp.*,cw.worksite_name from com_pass_tb cp,com_worksite_tb cw where cp.worksite_id=cw.id and cp.comid=?";
			String sql = "select * from com_pass_tb  where comid=? and state= ? ";
			String countsql = "select count(1) from com_pass_tb where comid=? and state=? ";
			Long count = daService.getLong(countsql, new Object[]{comid,0});
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			List list = null;
			Integer pageNum = RequestUtil.getInteger(request, "page", 1);
			Integer pageSize = RequestUtil.getInteger(request, "rp", 20);
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			params.add(0);
			if(count>0){
				list = daService.getAll(sql+ " order by id desc",params, pageNum, pageSize);
			}
			String json = JsonUtil.Map2Json(list,pageNum,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			String passname = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "passname"));
			String description = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "description"));
			String passtype = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "passtype"));
			int month_set =RequestUtil.getInteger(request, "month_set", -1);
			int month2_set =RequestUtil.getInteger(request, "month2_set", -1);
			Long worksite_id = RequestUtil.getLong(request, "worksite_id", -1L);
			//String channelId = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "channel_id"));
			if(passname.equals("")) passname = null;
			if(description.equals("")) description = null;
			if(passtype.equals("")) passtype = null;
			if(worksite_id == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("comid", comid);
			map.put("worksite_id", worksite_id);
			map.put("passname", passname);
			map.put("passtype", passtype);
			map.put("description", description);
			map.put("month_set", month_set);
			map.put("month2_set", month2_set);
			//map.put("channel_id", channelId);
			Long result = commonMethods.createPass(request, map);
			if(result > 0){
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("edit")){
			Long id =RequestUtil.getLong(request, "id", -1L);
			int month_set =RequestUtil.getInteger(request, "month_set", -1);
			int month2_set =RequestUtil.getInteger(request, "month2_set", -1);
			String passname = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "passname"));
			String description = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "description"));
			String passtype = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "passtype"));
			Long worksite_id = RequestUtil.getLong(request, "worksite_id", -1L);
			String channelId = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "channel_id"));
			if(passname.equals("")) passname = null;
			if(description.equals("")) description = null;
			if(passtype.equals("")) passtype = null;
			if(worksite_id == -1 || id == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			String sql = "update com_pass_tb set worksite_id=?,passname=?,passtype=?,description=?,month_set=?,month2_set=?,channel_id=? where id=?";
			int r = daService.update(sql, new Object[]{worksite_id,passname,passtype,description,month_set,month2_set,channelId,id});
			if(r == 1){
				if(publicMethods.isEtcPark(comid)){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_pass_tb",id,System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+operater+" edit comid:"+comid+" pass ,add sync ret:"+re);
				}else{
					logger.error("parkadmin or admin:"+operater+" edit comid:"+comid+" pass");
				}
				mongoDbUtils.saveLogs( request,0, 3, "修改了通道:"+passname);
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("getworksites")){
			String sql = "select * from com_worksite_tb where comid=?";
			List<Map> list = daService.getAll(sql, new Object[]{comid});
			String result = "[";
			if(!list.isEmpty()){
				for(Map map : list){
					result+="{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("worksite_name")+"\"},";
				}
				result = result.substring(0, result.length()-1);
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("delete")){
			String id =RequestUtil.processParams(request, "selids");
			Map passMap =daService.getMap("select * from com_pass_tb where id =? ", new Object[]{Long.valueOf(id)});
			String sql = "update  com_pass_tb set state=?  where id=?";
			int r = daService.update(sql, new Object[]{1,Long.valueOf(id)});
			if(r == 1){
				if(publicMethods.isEtcPark(comid)){
					int re = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_pass_tb",Long.valueOf(id),System.currentTimeMillis()/1000,2});
					logger.error("parkadmin or admin:"+operater+" delete comid:"+comid+" pass ,add sync ret:"+re);
				}else{
					logger.error("parkadmin or admin:"+operater+" delete comid:"+comid+" pass");
				}
				mongoDbUtils.saveLogs( request,0, 4, "删除了通道:"+passMap);
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("getbrake")){
			Long passid = RequestUtil.getLong(request, "passid", -1L);
			if(passid == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			String sql = "select * from com_brake_tb where passid=?";
			Map<String, Object> map = daService.getMap(sql, new Object[]{passid});
			String result = "";
			if(map != null){
				result = "[{\"brake_name\":\""+map.get("brake_name")+"\",\"serial\":\""+map.get("serial")+"\",\"ip\":\""+map.get("ip")+"\"}]";
			}else{
				result = "[{\"brake_name\":\""+""+"\",\"serial\":\""+""+"\",\"ip\":\""+""+"\"}]";
			}
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("brake")){
			String brake_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "brake_name"));
			String serial = RequestUtil.getString(request, "serial");
			String ip = RequestUtil.getString(request, "ip");
			Long passid = RequestUtil.getLong(request, "passid", -1L);
			if(passid == -1L){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Long count = daService.getLong("select count(1) from com_brake_tb where passid=?", new Object[]{passid});
			if(count > 0){
				//编辑
				String sql = "update com_brake_tb set brake_name=?,serial=?,ip=? where passid=?";
				int re = daService.update(sql, new Object[]{brake_name,serial,ip,passid});
				if(re == 1){
					AjaxUtil.ajaxOutput(response, "1");
				}else{
					AjaxUtil.ajaxOutput(response, "0");
				}
			}else{
				//添加
				String sql = "insert into com_brake_tb(passid,brake_name,serial,ip) values(?,?,?,?)";
				int re = daService.update(sql, new Object[]{passid,brake_name,serial,ip});
				if(re == 1){
					AjaxUtil.ajaxOutput(response, "1");
				}else{
					AjaxUtil.ajaxOutput(response, "0");
				}
			}
			mongoDbUtils.saveLogs( request,0, 2, "添加了通闸:"+brake_name);
		}else if(action.equals("liftrod")){
			//获取通道名称
			String passname = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "passname"));
			//获取通道编号
			String channelId = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "channel_id"));
			//获取道闸指令
			Integer channelOperate = RequestUtil.getInteger(request, "channel_operate", 4);
			System.out.println(passname+channelId+channelOperate);
			//发送消息到收费系统
			HttpProxy httpProxy = new HttpProxy();
			String url = "http://127.0.0.1/zld/sendmsgtopark.do?action=sendliftrodmsg";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("comid", comid);
			params.put("channelName", URLEncoder.encode(passname,"UTF-8"));
			params.put("channelId", URLEncoder.encode(channelId,"UTF-8"));
			params.put("operate", channelOperate);
			String result = httpProxy.doPostTwo(url, params);
			System.out.println(result);
			//发送消息后查询数据库是否完成操作
			Long state = -1L;
			for(int i=0;i<30;i++){
				Thread.sleep(100);
				Map map = daService.getMap("select state from liftrod_info_tb where channel_id=? and operate=? and comid=?",
						new Object[]{channelId,channelOperate,String.valueOf(comid)});
				if(map != null && !map.isEmpty()){
					state = Long.valueOf(String.valueOf(map.get("state")));
				}
				if(state == 1){
					break;
				}else{
					continue;
				}
			}
			if(state == 1){
				result = "1";
			}else{
				result = "0";
			}
			AjaxUtil.ajaxOutput(response, result);
		}
		return null;
	}
}

