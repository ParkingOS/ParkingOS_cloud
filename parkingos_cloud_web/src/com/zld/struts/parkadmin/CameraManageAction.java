package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.impl.CommonMethods;
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

public class CameraManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	@Autowired
	private CommonMethods commonMethods;

	private Logger logger = Logger.getLogger(CameraManageAction.class);
	/*
	 * 通道设置
	 */
	@Override
	public ActionForward execute(ActionMapping mapping,ActionForm form,HttpServletRequest request,HttpServletResponse response) throws Exception{
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
			String sql = "select cc.*,cp.worksite_id from com_camera_tb cc,com_pass_tb cp where cc.passid=cp.id and cp.comid=? order by id";
			String sqlcount = "select count(1) from com_camera_tb cc,com_pass_tb cp where cc.passid=cp.id and cp.comid=?";
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
		}else if(action.equals("create")){
			String camera_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "camera_name"));
			String ip = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "ip"));
			String port = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "port"));
			String cusername = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "cusername"));
			String manufacturer = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "manufacturer"));
			Long passid = RequestUtil.getLong(request, "passid", -1L);
			if(passid == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("comid", comid);
			map.put("passid", passid);
			map.put("camera_name", camera_name);
			map.put("ip", ip);
			map.put("port", port);
			map.put("cusername", cusername);
			map.put("manufacturer", manufacturer);
			Integer result = commonMethods.createCamera(request, map);
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("edit")){
			String camera_name = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "camera_name"));
			String ip = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "ip"));
			String port = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "port"));
			String cusername = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "cusername"));
			String manufacturer = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "manufacturer"));
			Long passid = RequestUtil.getLong(request, "passid", -1L);
			Long cameraid = RequestUtil.getLong(request, "id", -1L);
			if(passid == -1 || cameraid == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			//编辑
			String sql = "update com_camera_tb set camera_name=?,ip=?,port=?,cusername=?,manufacturer=?,passid=? where id=?";
			int re = daService.update(sql, new Object[]{camera_name,ip,port,cusername,manufacturer,passid,cameraid});
			if(re == 1){
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_camera_tb",cameraid,System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+nickname+" edit comid:"+comid+" camera, add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+nickname+" edit comid:"+comid+" camera");
				}
				mongoDbUtils.saveLogs( request,0, 3, "修改了摄像头:"+camera_name);
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("delete")){
			Long cameraid = RequestUtil.getLong(request, "selids", -1L);
			String sql = "delete from com_camera_tb where id=?";
			Map camMap = daService.getMap("select * from com_camera_tb where id =? ", new Object[]{cameraid});
			int result = daService.update(sql, new Object[]{cameraid});
			if(result == 1){
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_camera_tb",cameraid,System.currentTimeMillis()/1000,2});
					logger.error("parkadmin or admin:"+nickname+" delete comid:"+comid+" camera,add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+nickname+" delete comid:"+comid+" camera");
				}
				mongoDbUtils.saveLogs( request,0, 4, "删除了摄像头:"+camMap);
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("editpass")){
			Long cameraid = RequestUtil.getLong(request, "id", -1L);
			String newpass = RequestUtil.processParams(request, "newpass");
			String confirmpass = RequestUtil.processParams(request, "confirmpass");
			if(!newpass.equals(confirmpass) || cameraid == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			String sql = "update com_camera_tb set cpassword=? where id=?";
			int result = daService.update(sql, new Object[]{newpass,cameraid});
			if(result == 1){
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_camera_tb",cameraid,System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+nickname+" edit password comid:"+comid+" camera"+",add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+nickname+" edit password comid:"+comid+" camera");
				}
				mongoDbUtils.saveLogs( request,0, 3, "重置了摄像头密码，编号"+cameraid+"，新密码"+newpass);
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
		}
		return null;
	}
	public static void main(String[] args) {
		int a=3;
		if(a>1){
			System.out.println(a);
		}if(a>2){
			System.out.println(a);
		}
	}
}
