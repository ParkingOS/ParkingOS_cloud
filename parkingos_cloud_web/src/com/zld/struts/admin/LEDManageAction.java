package com.zld.struts.admin;

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

public class LEDManageAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	@Autowired
	private CommonMethods commonMethods;

	private Logger logger = Logger.getLogger(LEDManageAction.class);

	/*
	 * LED设置
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comId = (Long)request.getSession().getAttribute("comid");
		String nickname = request.getSession().getAttribute("loginuin")+"";
		Integer authId = RequestUtil.getInteger(request, "authid", -1);
		request.setAttribute("authid", authId);
		if(nickname == null){
			response.sendRedirect("login.do");
			return null;
		}
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		if(authId>0){//车场云后台调用时
			comid =comId;
		}
		if(action.equals("")){
			request.setAttribute("comid", comid);
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select l.*,cp.worksite_id from com_led_tb l,com_pass_tb cp where l.passid=cp.id and cp.comid=? order by id";
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
		}else if(action.equals("create")){
			String ledip = RequestUtil.processParams(request, "ledip");
			String ledport = RequestUtil.processParams(request, "ledport");
			String leduid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "leduid"));
			Integer movemode = RequestUtil.getInteger(request, "movemode", -1);
			Integer movespeed = RequestUtil.getInteger(request, "movespeed", -1);
			Long dwelltime = RequestUtil.getLong(request, "dwelltime", -1L);
			Integer ledcolor = RequestUtil.getInteger(request, "ledcolor", -1);
			Integer showcolor = RequestUtil.getInteger(request, "showcolor", -1);
			Integer typeface = RequestUtil.getInteger(request, "typeface", -1);
			Integer typesize = RequestUtil.getInteger(request, "typesize", -1);
			String matercont = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "matercont"));
			Long passid = RequestUtil.getLong(request, "passid", -1L);
			Integer width = RequestUtil.getInteger(request, "width", 128);
			Integer height = RequestUtil.getInteger(request, "height", 32);
			Integer type = RequestUtil.getInteger(request, "type", 0);
			Integer rsport = RequestUtil.getInteger(request, "rsport", 1);
			if(passid == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			if(movemode == -1) movemode = null;
			if(movespeed == -1) movespeed = null;
			if(dwelltime == -1) dwelltime = null;
			if(ledcolor == -1) ledcolor = null;
			if(showcolor == -1) showcolor = null;
			if(typeface == -1) typeface = null;
			if(typesize == -1) typesize = null;

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("comid", comid);
			map.put("passid", passid);
			map.put("ledip", ledip);
			map.put("ledport", ledport);
			map.put("leduid", leduid);
			map.put("movemode", movemode);
			map.put("movespeed", movespeed);
			map.put("dwelltime", dwelltime);
			map.put("ledcolor", ledcolor);
			map.put("showcolor", showcolor);
			map.put("typeface", typeface);
			map.put("typesize", typesize);
			map.put("matercont", matercont);
			map.put("width", width);
			map.put("height", height);
			map.put("type", type);
			map.put("rsport", rsport);
			Integer result = commonMethods.createLED(request, map);
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("edit")){
			String ledip = RequestUtil.processParams(request, "ledip");
			String ledport = RequestUtil.processParams(request, "ledport");
			String leduid = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "leduid"));
			Integer movemode = RequestUtil.getInteger(request, "movemode", -1);
			Integer movespeed = RequestUtil.getInteger(request, "movespeed", -1);
			Long dwelltime = RequestUtil.getLong(request, "dwelltime", -1L);
			Integer ledcolor = RequestUtil.getInteger(request, "ledcolor", -1);
			Integer showcolor = RequestUtil.getInteger(request, "showcolor", -1);
			Integer typeface = RequestUtil.getInteger(request, "typeface", -1);
			Integer typesize = RequestUtil.getInteger(request, "typesize", -1);
			String matercont = AjaxUtil.decodeUTF8(RequestUtil.processParams(request, "matercont"));
			Long passid = RequestUtil.getLong(request, "passid", -1L);
			Long ledid = RequestUtil.getLong(request, "id", -1L);
			Integer width = RequestUtil.getInteger(request, "width", 128);
			Integer height = RequestUtil.getInteger(request, "height", 32);
			Integer type = RequestUtil.getInteger(request, "type", 0);
			Integer rsport = RequestUtil.getInteger(request, "rsport", 1);
			if(passid == -1 || ledid == -1){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			if(movemode == -1) movemode = null;
			if(movespeed == -1) movespeed = null;
			if(dwelltime == -1) dwelltime = null;
			if(ledcolor == -1) ledcolor = null;
			if(showcolor == -1) showcolor = null;
			if(typeface == -1) typeface = null;
			if(typesize == -1) typesize = null;
			//编辑
			String sql = "update com_led_tb set ledip=?,ledport=?,leduid=?,movemode=?,movespeed=?,dwelltime=?,ledcolor=?,showcolor=?,typeface=?,typesize=?,matercont=?,passid=?,width=?,height=?,type=?,rsport=? where id=?";
			int re = daService.update(sql, new Object[]{ledip,ledport,leduid,movemode,movespeed,dwelltime,ledcolor,showcolor,typeface,typesize,matercont,passid,width,height,type,rsport,ledid});
			if(re == 1){
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_led_tb",ledid,System.currentTimeMillis()/1000,1});
					logger.error("parkadmin or admin:"+nickname+" edit comid:"+comid+" led ,add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+nickname+" edit comid:"+comid+" led ");
				}
				mongoDbUtils.saveLogs(request, 0, 3, "修改了（comid:"+comid+"）的LED："+ledip+":"+ledport);
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
		}else if(action.equals("delete")){
			Long cameraid = RequestUtil.getLong(request, "selids", -1L);
			Map ledMap = daService.getMap("select * from com_led_tb where id=?",  new Object[]{cameraid});
			String sql = "delete from com_led_tb where id=?";
			int result = daService.update(sql, new Object[]{cameraid});
			if(result == 1){
				if(publicMethods.isEtcPark(comid)){
					int r = daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"com_led_tb",cameraid,System.currentTimeMillis()/1000,2});
					logger.error("parkadmin or admin:"+nickname+" delete comid:"+comid+" led ,add sync ret:"+r);
				}else{
					logger.error("parkadmin or admin:"+nickname+" delete comid:"+comid+" led ");
				}
				mongoDbUtils.saveLogs(request, 0, 4, "删除了（comid:"+comid+"）的LED："+ledMap);
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "0");
			}
		}
		return null;
	}
}
