package com.zld.struts.auth;

import com.zld.AjaxUtil;
import com.zld.service.DataBaseService;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
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


/**
 * 权限管理---组织机构管理
 * @author Gecko
 *
 */
public class AuthOrganizAction extends Action {

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
			target="orglist";
		}else if(action.equals("query")){
			String sql = "select * from zld_organize_tb   ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			//System.out.println(sqlInfo);
			List list = daService.getAll(sql+" order by id ",null);
			int count =0;
			if(list!=null)
				count = list.size();
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("getdata")){
			String type = RequestUtil.getString(request, "type");
			List<Map<String, Object>> list = null;
			String result = "[]";
			if(type.equals("allorgs")){
				list = 	daService.getAll("select id as value_no,name as value_name from zld_organize_tb",null);
				Map<String, Object> firstMap = new HashMap<String, Object>();
				firstMap.put("value_no", "0");
				firstMap.put("value_name", "无");
				if(list.isEmpty())
					list = new ArrayList<Map<String, Object>>();
				list.add(0,firstMap);
			}
			else if(type.equals("allorgtype")){
				list = daService.getAll("select id as value_no,name as value_name from zld_orgtype_tb",null);
			}else if(type.equals("roles")){
				list = daService.getAll("select id as value_no,role_name as value_name from user_role_tb",null);
			}
			result= StringUtils.createJson(list);
			if(type.equals("state")){
				result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"},{\"value_no\":\"0\",\"value_name\":\"正常\"},{\"value_no\":\"1\",\"value_name\":\"禁用\"}]";
			}
			AjaxUtil.ajaxOutput(response, result);
			return null;
		}else if(action.equals("create")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			Long pid = RequestUtil.getLong(request, "pid", 0L);
			Long type = RequestUtil.getLong(request, "type", 0L);
			int ret = 0;
			ret = daService.update("insert into zld_organize_tb (name,pid,type) values(?,?,?) ", new Object[]{name,pid,type});
			AjaxUtil.ajaxOutput(response, ret+"");
			return null;
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Long pid = RequestUtil.getLong(request, "pid", -1L);
			Long type = RequestUtil.getLong(request, "type", 0L);
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "name"));
			int	result = daService.update("update zld_organize_tb set name =?,pid=?,type=? where id=?",
					new Object[]{name,pid,type,id});
			AjaxUtil.ajaxOutput(response, ""+result);
			return null;
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			int	result = daService.update("delete from  zld_organize_tb  where id=?",
					new Object[]{id});
			AjaxUtil.ajaxOutput(response, ""+result);
			return null;
		}
		return mapping.findForward(target);
	}

}
