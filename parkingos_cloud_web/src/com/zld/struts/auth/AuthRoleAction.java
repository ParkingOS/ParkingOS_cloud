package com.zld.struts.auth;

import com.zld.AjaxUtil;
import com.zld.impl.MongoDbUtils;
import com.zld.service.DataBaseService;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
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


/**
 * 权限管理---设置角色权限
 * @author Gecko
 *
 */
public class AuthRoleAction extends Action {

	@Autowired
	private DataBaseService daService;
	@Autowired
	private MongoDbUtils mongoDbUtils;

	private Logger logger = Logger.getLogger(AuthRoleAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		String target = null;
		if(action.equals("editauthrole")){
			Long roleId = RequestUtil.getLong(request, "roleid", -1L);
			Long oid = RequestUtil.getLong(request, "oid", -1L);
			//查询所权限
			List<Map<String, Object>> allAuthsList = daService.getAll("select id,pid,nname as name,sub_auth from auth_tb " +
					"where state =? and oid=? order by id ",new Object[]{0,oid});
			//查自己权限
			List<Map<String, Object>> ownAuthsList = daService.getAll("select auth_id,ar.sub_auth,pid from auth_role_tb ar" +
					" left join auth_tb at on ar.auth_id= at.id where role_id =? ",new Object[]{roleId});
			//查角色名称
			Map userRoleMap = daService.getMap("select role_name from user_role_tb where id=? ", new Object[]{roleId});
			String own = StringUtils.createJson(ownAuthsList);
			request.setAttribute("rolename",userRoleMap.get("role_name"));
			request.setAttribute("ownauths",own.replace("null", ""));
			request.setAttribute("allauths", StringUtils.createJson(allAuthsList));
			request.setAttribute("roleid", roleId);
			target = "editauthrole";
		}else if(action.equals("edit")){
			Long roleid = RequestUtil.getLong(request, "roleid", -1L);
			String auths = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "auths"));
			String as[] = auths.split("\\|");
			Map<Long, String> aMap = new HashMap<Long, String>();
			for(int i=0;i<as.length;i++){
				String a = as[i];
				if(a.indexOf(",")!=-1){
					String subs[] = a.split(",");
					Long aid = Long.valueOf(subs[0]);
					if(aMap.containsKey(aid)){
						String v = aMap.get(aid);
						if(v.length()>0)
							v +=",";
						v +=subs[1];
						aMap.put(aid, v);
					}else {
						aMap.put(aid, subs[1]);
					}
				}else {
					aMap.put(Long.valueOf(a), "");
				}
			}

			int ret = daService.update("delete from auth_role_tb where role_id=?", new Object[]{roleid});
			logger.error("-----> authrole，roleid:"+roleid+":原权限删除，ret:"+ret);
			String sql = "insert into auth_role_tb (role_id,auth_id,sub_auth) values(?,?,?)";
			List<Object[]>lists = new ArrayList<Object[]>();
			for(Long key :aMap.keySet()){
				Object[] values = new Object[]{roleid,key,aMap.get(key)};
				lists.add(values);
			}
			ret = daService.bathInsert(sql, lists, new int []{4,4,12});
			logger.error("-----> authrole，roleid:"+roleid+":更新权限，ret:"+ret);
			if(ret>0){
				ret = 1;
				mongoDbUtils.saveLogs(request, 0, 3, "修改了角色权限:"+roleid);
			}
			AjaxUtil.ajaxOutput(response, ret+"");

			return null;
		}
		return mapping.findForward(target);
	}

}
