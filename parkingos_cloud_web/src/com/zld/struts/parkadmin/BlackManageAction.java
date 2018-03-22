package com.zld.struts.parkadmin;

import com.zld.AjaxUtil;
import com.zld.impl.MongoDbUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * 黑名单管理
 * @author Administrator
 *
 */
public class BlackManageAction extends Action{

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		Long comid = RequestUtil.getLong(request, "comid", -1L);
		Integer authId = RequestUtil.getInteger(request, "authid",-1);
		request.setAttribute("authid", authId);
		if(comid==-1){
			comid = (Long)request.getSession().getAttribute("comid");
		}
		if(comid==null||comid==-1){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
			request.setAttribute("comid", request.getParameter("comid"));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from zld_black_tb where comid=? and state =?  ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			//System.out.println(sqlInfo);
			List list = daService.getAll(sql+" order by id desc",new Object[]{comid,0});
			int count =0;
			if(list!=null)
				count = list.size();
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){//添加车型
			String remark = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "remark"));
			String carNumber =  AjaxUtil.decodeUTF8(RequestUtil.getString(request, "car_number"));
			String operator =  AjaxUtil.decodeUTF8(RequestUtil.getString(request, "operator"));
			if("".equals(operator)){
				operator = request.getSession().getAttribute("loginuin")+"";
			}
			Long ntime = System.currentTimeMillis()/1000;
			int result=0;
			String blackUUID = comid+"_"+new Random().nextInt(1000000);
			try {
				Long nextid = daService.getLong(
						"SELECT nextval('seq_zld_black_tb'::REGCLASS) AS newid", null);
				result = daService.update("insert into zld_black_tb (id,comid,ctime,utime,uin,state,remark,car_number,black_uuid,operator)" +
								" values(?,?,?,?,?,?,?,?,?,?)",
						new Object[]{nextid,comid,ntime,ntime,-1L,0,remark,carNumber,blackUUID,operator});
				if(result==1&&publicMethods.isEtcPark(comid)){
					daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"zld_black_tb",nextid,System.currentTimeMillis()/1000,0});
				}
				if(result==1)
					mongoDbUtils.saveLogs(request, 0, 2, "添加了黑名单:"+comid+"):"+carNumber);
			} catch (Exception e) {
				e.printStackTrace();
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String remark = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "remark"));
			String carNumber =  AjaxUtil.decodeUTF8(RequestUtil.getString(request, "car_number"));
			Long ntime = System.currentTimeMillis()/1000;
			Integer state = RequestUtil.getInteger(request, "state", 0);
			String operator =  AjaxUtil.decodeUTF8(RequestUtil.getString(request, "operator"));
			if("".equals(operator)){
				operator = request.getSession().getAttribute("loginuin")+"";
			}
			int	result = daService.update("update zld_black_tb set utime =?,state=?,remark=?,car_number=?,operator=? where id=?",
					new Object[]{ntime,state,remark,carNumber,operator,id});
			if(result==1&&publicMethods.isEtcPark(comid)){
				daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"zld_black_tb",id,System.currentTimeMillis()/1000,1});
			}
			if(result==1)
				mongoDbUtils.saveLogs(request, 0, 3, "修改了黑名单(车场编号:"+comid+"):"+carNumber);
			AjaxUtil.ajaxOutput(response, ""+result);
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map carMap = daService.getMap("select * from  zld_black_tb  where id=?",	new Object[]{id});
			int	result = daService.update("update   zld_black_tb set state=?  where id=?",
					new Object[]{1,id});
			if(result==1&&publicMethods.isEtcPark(comid)){
				daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"zld_black_tb",id,System.currentTimeMillis()/1000,2});
			}
			if(result==1)
				mongoDbUtils.saveLogs(request, 0, 4, "删除了黑名单(车场编号:"+comid+"):"+carMap);
			AjaxUtil.ajaxOutput(response, ""+result);
		}
		return null;
	}

}
