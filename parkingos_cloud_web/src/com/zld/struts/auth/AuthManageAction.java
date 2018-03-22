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
 * 权限管理---权限列表
 * @author Gecko
 *
 */
public class AuthManageAction extends Action {

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
			Integer oid = RequestUtil.getInteger(request, "oid", 8);//默认加载车场的
			List<Map<String, Object>> list = daService.getAll("select id,name from zld_orgtype_tb ", new Object[]{});
			if(list != null){
				for(Map<String, Object> map : list){
					Long id = (Long)map.get("id");
					String name = (String)map.get("name");
					if(name.contains("车场")){
						request.setAttribute("org_comid", id);
					}else if(name.contains("BOSS")){
						request.setAttribute("org_tcbid", id);
					}else if(name.contains("渠道")){
						request.setAttribute("org_chanid", id);
					}else if(name.contains("集团")){
						request.setAttribute("org_groupid", id);
					}
				}
			}
			request.setAttribute("oid", oid);
			target="authlist";
		}else if(action.equals("query")){
			String sql = "select * from auth_tb  where oid =? ";
			Integer oid = RequestUtil.getInteger(request, "oid", 8);//默认加载车场的
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			//System.out.println(sqlInfo);
			List list = daService.getAll(sql+" order by sort ",new Object[]{oid});
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
			if(type.equals("auths")){
				Long oid = RequestUtil.getLong(request, "oid", 8L);
				list = 	daService.getAll("select id as value_no,nname as value_name from auth_tb where oid=? ",new Object[]{oid});
				Map<String, Object> firstMap = new HashMap<String, Object>();
				firstMap.put("value_no", "0");
				firstMap.put("value_name", "无");
				if(list.isEmpty())
					list = new ArrayList<Map<String, Object>>();
				list.add(0,firstMap);
			}else if(type.equals("allorgtype")){
				list = daService.getAll("select id as value_no,name as value_name from zld_orgtype_tb",null);
			}
			result= StringUtils.createJson(list);
			if(type.equals("state")){
				result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"},{\"value_no\":\"0\",\"value_name\":\"正常\"},{\"value_no\":\"1\",\"value_name\":\"禁用\"}]";
			}
			AjaxUtil.ajaxOutput(response, result);
			return null;
		}else if(action.equals("create")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "nname"));
			Long pid = RequestUtil.getLong(request, "pid", 0L);
			Long oid = RequestUtil.getLong(request, "oid", 0L);
			Integer state = RequestUtil.getInteger(request, "type", 0);
			String subAuth = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "sub_auth"));
			String actions = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "actions"));
			String url = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "url"));
			int ret = 0;
			ret = daService.update("insert into auth_tb (nname,pid,nid,sub_auth,url,state,oid,actions) values(?,?,?,?,?,?,?,?) ",
					new Object[]{name,pid,0,subAuth,url,state,oid,actions});
			AjaxUtil.ajaxOutput(response, ret+"");
			return null;
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "nname"));
			Long pid = RequestUtil.getLong(request, "pid", 0L);
			Long oid = RequestUtil.getLong(request, "oid", 0L);
			Integer state = RequestUtil.getInteger(request, "type", 0);
			String subAuth = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "sub_auth"));
			String url = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "url"));
			String actions = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "actions"));
			int	result = daService.update("update auth_tb set nname =?,pid=?,sub_auth=?,url=?,state=?,oid=?,actions=? where id=?",
					new Object[]{name,pid,subAuth,url,state,oid,actions,id});
			AjaxUtil.ajaxOutput(response, ""+result);
			return null;
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			int	result = daService.update("delete from  auth_tb  where id=?",
					new Object[]{id});
			AjaxUtil.ajaxOutput(response, ""+result);
			return null;
		}
		return mapping.findForward(target);
	}

}
