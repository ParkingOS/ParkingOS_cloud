package com.zld.struts.auth;

import com.zld.AjaxUtil;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 权限管理---组织机构管理
 * @author Gecko
 *
 */
public class AuthOrgTypeAction extends Action {

	@Autowired
	private DataBaseService daService;
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		request.setAttribute("authid", request.getParameter("authid"));
		String target = null;
		if(action.equals("")){
			target="orgtypelist";
		}else if(action.equals("query")){
			String sql = "select * from zld_orgtype_tb ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			//System.out.println(sqlInfo);
			List list = daService.getAll(sql,new Object[]{});
			getAdminRole(list);
			int count =0;
			if(list!=null)
				count = list.size();
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			int ret = 0;
			ret = daService.update("insert into zld_orgtype_tb (name) values(?) ", new Object[]{name});
			AjaxUtil.ajaxOutput(response, ret+"");
			return null;
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			int	result = daService.update("update zld_orgtype_tb set name =? where id=?",
					new Object[]{name,id});
			AjaxUtil.ajaxOutput(response, ""+result);
			return null;
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			int	result = daService.update("delete from  zld_orgtype_tb  where id=?",
					new Object[]{id});
			AjaxUtil.ajaxOutput(response, ""+result);
			return null;
		}else if(action.endsWith("addmanage")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map orgTypeMap = daService.getMap("select name from zld_orgtype_tb where id =?", new Object[]{id});
			int result = daService.update("insert into user_role_tb(role_name,oid,type,state)" +
					"values(?,?,?,?)", new Object[]{orgTypeMap.get("name")+"管理员",id,0,0});
			AjaxUtil.ajaxOutput(response, ""+result);
			return null;
		}

		return mapping.findForward(target);
	}

	private void getAdminRole(List<Map<String, Object>> list){
		if(list != null && !list.isEmpty()){
			List<Object> oidList = new ArrayList<Object>();
			String preParams = "";
			for(Map<String, Object> map : list){
				Long id = (Long)map.get("id");
				oidList.add(id);
				if(preParams.equals("")){
					preParams ="?";
				}else{
					preParams += ",?";
				}
			}
			oidList.add(0);
			oidList.add(0);
			String sql = "select id as roleid,oid from user_role_tb where oid in ("+preParams+") and adminid=? and type=? ";
			List<Map<String, Object>> rList = daService.getAllMap(sql, oidList);
			if(rList != null && !rList.isEmpty()){
				for(Map<String, Object> map : list){
					Long id = (Long)map.get("id");
					for(Map<String, Object> map2 : rList){
						Long oid = (Long)map2.get("oid");
						if(id.intValue() == oid.intValue()){
							Long roleid = (Long)map2.get("roleid");
							map.put("roleid", roleid);
							break;
						}
					}
				}
			}
		}
	}
}
