package com.zld.struts.admin;

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


/**
 * 车型设定
 * @author Administrator
 *
 */
public class FreeReasonsAction extends Action{

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
		if(comid==null){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("")){
//			Map<String, Object> comInfoMap = daService.getMap("select car_type from com_info_tb where id =? ", new Object[]{comid});
//			request.setAttribute("cartype",comInfoMap.get("car_type"));
			request.setAttribute("comid", request.getParameter("comid"));
			return mapping.findForward("list");
		}else if(action.equals("query")){
			String sql = "select * from free_reasons_tb where comid=?  ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			//System.out.println(sqlInfo);
			List list = daService.getAll(sql+" order by sort,id desc",new Object[]{comid});
			int count =0;
			if(list!=null)
				count = list.size();
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){//添加帐号
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			Integer sort = RequestUtil.getInteger(request, "sort", 0);
			int result=0;
			try {
				Long nextid = daService.getLong(
						"SELECT nextval('seq_free_reasons_tb'::REGCLASS) AS newid", null);
				result = daService.update("insert into free_reasons_tb (id,comid,name,sort)" +
								" values(?,?,?,?)",
						new Object[]{nextid,comid,name,sort});
				if(result==1&&publicMethods.isEtcPark(comid)){
					daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"free_reasons_tb",nextid,System.currentTimeMillis()/1000,0});
				}
				if(result==1)
					mongoDbUtils.saveLogs(request, 0, 2, "添加了车场（"+comid+"）免费原因:"+name+",编号："+nextid);
			} catch (Exception e) {
				if(e.getMessage().indexOf("free_reasons_tb_comid_mtype_key")!=-1)
					result=-2;
				//e.printStackTrace();
			}
			AjaxUtil.ajaxOutput(response, result+"");
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			Integer sort = RequestUtil.getInteger(request, "sort", 0);
			int	result = daService.update("update free_reasons_tb set name =?,sort=? where id=?",
					new Object[]{name,sort,id});
			if(result==1&&publicMethods.isEtcPark(comid)){
				daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"free_reasons_tb",id,System.currentTimeMillis()/1000,1});
			}
			if(result==1)
				mongoDbUtils.saveLogs(request, 0, 3, "修改了车场（"+comid+"）免费原因,编号："+id);
			AjaxUtil.ajaxOutput(response, ""+result);
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map freeMap = daService.getMap("select * from  free_reasons_tb  where id=?",new Object[]{id});
			int	result = daService.update("delete from  free_reasons_tb  where id=?",
					new Object[]{id});
			if(result==1&&publicMethods.isEtcPark(comid)){
				daService.update("insert into sync_info_pool_tb(comid,table_name,table_id,create_time,operate) values(?,?,?,?,?)", new Object[]{comid,"free_reasons_tb",id,System.currentTimeMillis()/1000,2});
			}
			if(result==1)
				mongoDbUtils.saveLogs(request, 0, 4, "删除了车场（"+comid+"）免费原因:"+freeMap);
			AjaxUtil.ajaxOutput(response, ""+result);
		}
		return null;
	}

}
