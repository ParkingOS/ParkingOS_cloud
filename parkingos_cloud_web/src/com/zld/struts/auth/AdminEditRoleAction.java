package com.zld.struts.auth;

import com.zld.AjaxUtil;
import com.zld.impl.MongoDbUtils;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.JsonUtil;
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
import java.util.*;

public class AdminEditRoleAction extends Action {

	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private MongoDbUtils mongoDbUtils;
	private Logger logger = Logger.getLogger(AuthRoleAction.class);
	@SuppressWarnings("unchecked")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = (Long)request.getSession().getAttribute("loginuin");//登录的用户id
		Long oid = (Long)request.getSession().getAttribute("oid");//登录角色所属组织类型
		Long loginroleid = (Long)request.getSession().getAttribute("loginroleid");//登录角色
		String target = null;
		request.setAttribute("authid", request.getParameter("authid"));
		if(action.equals("")){
			target = "adminrolelist";
		}else if(action.equals("query")){
			String sql = "select * from user_role_tb where adminid =? and state=? order by id ";
			String fieldsstr = RequestUtil.processParams(request, "fieldsstr");
			//System.out.println(sqlInfo);
			List<Map<String, Object>> list = daService.getAll(sql,new Object[]{uin, 0});
			int count =0;
			if(list != null){
				count = list.size();
				setList(list);
			}
			String json = JsonUtil.Map2Json(list,1,count, fieldsstr,"id");
			AjaxUtil.ajaxOutput(response, json);
			return null;
		}else if(action.equals("create")){
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "role_name"));
			Integer state = RequestUtil.getInteger(request, "state", 0);
			String resume = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "resume"));
			Integer func = RequestUtil.getInteger(request, "func", -1);
			int is_inspect = 0;
			int is_collector = 0;
			int is_opencard = 0;
			switch (func) {
				case 1:
					is_collector = 1;
					break;
				case 2:
					is_inspect = 1;
					break;
				case 3:
					is_opencard = 1;
					break;
				default:
					break;
			}
			int ret = daService.update("insert into user_role_tb(role_name,state,oid," +
							"adminid,type,resume,is_inspect,is_collector,is_opencard) values(?,?,?,?,?,?,?,?,?)",
					new Object[] { name, state, oid, uin, 1, resume, is_inspect, is_collector, is_opencard });
			if(ret == 1){
				mongoDbUtils.saveLogs(request, 0, 2, "添加了角色："+name);
			}
			AjaxUtil.ajaxOutput(response, ret + "");
			return null;
		}else if(action.equals("edit")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String name = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "role_name"));
			Integer state = RequestUtil.getInteger(request, "state", 0);
			Integer func = RequestUtil.getInteger(request, "func", -1);
			String resume = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "resume"));

			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			int auth_flag = -1;
			int is_inspect = 0;
			int is_collector = 0;
			int is_opencard = 0;
			switch (func) {
				case 1:
					auth_flag = 2;
					is_collector = 1;
					break;
				case 2:
					auth_flag = 16;
					is_inspect = 1;
					break;
				case 3:
					auth_flag = 17;
					is_opencard = 1;
					break;
				default:
					break;
			}
			logger.error("id:"+id+",auth_flag:"+auth_flag+",is_inspect:"+is_inspect
					+",is_collector:"+is_collector+",is_opencard:"+is_opencard);
			//更新用户余额
			Map<String, Object> userRoleSqlMap = new HashMap<String, Object>();
			userRoleSqlMap.put("sql", "update user_role_tb set state=?," +
					"role_name=?,resume=?,is_inspect=?,is_collector=?,is_opencard=? where id =? ");
			userRoleSqlMap.put("values", new Object[]{state,name,resume,is_inspect,
					is_collector,is_opencard,id});
			bathSql.add(userRoleSqlMap);
			Map<String, Object> userSqlMap = new HashMap<String, Object>();
			userSqlMap.put("sql", "update user_info_tb set auth_flag=? where role_id =? ");
			userSqlMap.put("values", new Object[]{auth_flag, id});
			bathSql.add(userSqlMap);
			boolean b = daService.bathUpdate2(bathSql);
			if(b){
				mongoDbUtils.saveLogs(request, 0, 3, "修改了角色："+name);
				AjaxUtil.ajaxOutput(response, "1");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
			return null;
		}else if(action.equals("delete")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			if(id > 0){
				Long count = daService.getLong("select count(id) from user_info_tb where role_id=? and state<>? ",
						new Object[]{id, 1});
				if(count > 0){
					AjaxUtil.ajaxOutput(response, "-1");
					return null;
				}
				int r = daService.update("update user_role_tb set state=? where id=? ",
						new Object[]{1, id});
				if(r == 1){
					mongoDbUtils.saveLogs(request, 0, 4, "删除了角色："+id);
				}
				AjaxUtil.ajaxOutput(response, r + "");
				return null;
			}
			AjaxUtil.ajaxOutput(response, "0");
			return null;
		}else if(action.equals("editrole")){
			Long roleId = RequestUtil.getLong(request, "roleid", -1L);
			//查询所权限
			List<Map<String, Object>> allAuthsList = daService.getAll("select id,pid,nname as name,sub_auth from auth_tb where state =? order by id ",new Object[]{0});
			//查父权限
			List<Map<String, Object>> parentAuthsList = daService.getAll("select auth_id,sub_auth from auth_role_tb where role_id =? ",new Object[]{loginroleid});
			//查自己权限
			List<Map<String, Object>> ownAuthsList = daService.getAll("select auth_id,ar.sub_auth,pid from auth_role_tb ar" +
					" left join auth_tb at on ar.auth_id= at.id where role_id =? ",new Object[]{roleId});
			//查角色名称
			Map userRoleMap = daService.getMap("select role_name from user_role_tb where id=? ", new Object[]{roleId});

			List<Map<String, Object>> subList = new ArrayList<Map<String,Object>>();
			for(Map<String, Object> map : parentAuthsList){
				Long autId = (Long)map.get("auth_id");
				String subAuth = (String)map.get("sub_auth");
				for(Map<String, Object> amap : allAuthsList){
					Long id = (Long)amap.get("id");
					String sub_auth = (String)amap.get("sub_auth");
					if(autId.equals(id)){
						if(subAuth!=null&&!subAuth.equals("")){
							String s1[]=subAuth.split(",");
							String s2[]=sub_auth.split(",");
							String newSubAuth= "";
							if(s2.length>0){
								for(String index : s1){
									if(!newSubAuth.equals(""))
										newSubAuth +=",";
									Integer in = Integer.valueOf(index);
									if(in>s2.length-1){
//										if(s2.length>1)
//											newSubAuth +=index;
									}else {
										newSubAuth  +=s2[Integer.valueOf(index)];
									}
								}
							}
							amap.put("sub_auth", newSubAuth);
						}
						subList.add(amap);
						break;
					}
				}
			}
			Collections.sort(subList,new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> o1,
								   Map<String, Object> o2) {
					Long id1 = (Long)o1.get("id");
					Long id2 = (Long)o2.get("id");
					Long index = id1-id2;
					return index.intValue();
				}
			});
			String own = StringUtils.createJson(ownAuthsList);
			request.setAttribute("rolename",userRoleMap.get("role_name"));
			request.setAttribute("ownauths",own.replace("null", ""));
			request.setAttribute("allauths", StringUtils.createJson(subList));
			request.setAttribute("roleid", roleId);
			target = "editauthrole";
		}else if(action.equals("precollectset")){//读取收费员设置
			Long roleid = RequestUtil.getLong(request, "roleid", -1L);
			Map collectSetMap = daService.getMap("select * from collector_set_tb where " +
					" role_id=? order by id desc limit ? ", new Object[]{roleid, 1});
			request.setAttribute("data", StringUtils.createJson(collectSetMap));
			request.setAttribute("roleid", roleid);
			target = "collectset";
		}else if(action.equals("collectset")){//保存收费员设置
			Long id = RequestUtil.getLong(request, "role_id", -1L);
			Long roleId = RequestUtil.getLong(request, "roleid", -1L);
			Integer photoset1 = RequestUtil.getInteger(request, "photoset1", 0);
			Integer photoset2 = RequestUtil.getInteger(request, "photoset2", 0);
			Integer photoset3 = RequestUtil.getInteger(request, "photoset3", 0);
			String prepayset1=RequestUtil.getString(request, "prepayset1");
			String prepayset2=RequestUtil.getString(request, "prepayset2");
			String prepayset3=RequestUtil.getString(request, "prepayset3");
			String print_sign1=AjaxUtil.decodeUTF8(RequestUtil.getString(request, "print_sign1"));
			String print_sign2=AjaxUtil.decodeUTF8(RequestUtil.getString(request, "print_sign2"));
			String print_sign3=AjaxUtil.decodeUTF8(RequestUtil.getString(request, "print_sign3"));
			String print_sign4=AjaxUtil.decodeUTF8(RequestUtil.getString(request, "print_sign4"));
			Integer changePrePay = RequestUtil.getInteger(request, "change_prepay", 0);
			Integer view_plot = RequestUtil.getInteger(request, "view_plot", 0);
			Integer isprepay = RequestUtil.getInteger(request, "isprepay", 0);
			Integer hidedetail = RequestUtil.getInteger(request, "hidedetail", 0);
			Integer is_sensortime = RequestUtil.getInteger(request, "is_sensortime", 0);
			String collpwd = RequestUtil.processParams(request, "collpwd");
			String signpwd = RequestUtil.processParams(request, "signpwd");
			Integer signout_valid = RequestUtil.getInteger(request, "signout_valid", 0);
			Integer is_show_card = RequestUtil.getInteger(request, "is_show_card", 0);
			Integer print_order_place2 = RequestUtil.getInteger(request, "print_order_place2", 0);
			Integer is_duplicate_order = RequestUtil.getInteger(request, "is_duplicate_order", 1);
			Integer is_print_name = RequestUtil.getInteger(request, "is_print_name", 1);
			if(collpwd.equals("")) collpwd = null;
			int ret =0;
			String photoset ="["+photoset1+","+photoset2+","+photoset3+"]";
			if(prepayset1.equals("")){
				prepayset1="0";
			}
			if(prepayset2.equals("")){
				prepayset2="0";
			}
			if(prepayset3.equals("")){
				prepayset3="0";
			}
			String prepayset="["+prepayset1+","+prepayset2+","+prepayset3+"]";

			if(print_sign1.equals("")){
				print_sign1=" ";
			}
			if(print_sign2.equals("")){
				print_sign2=" ";
			}
			if(print_sign3.equals("")){
				print_sign3=" ";
			}
			if(print_sign4.equals("")){
				print_sign4=" ";
			}
			String print_sign="[\""+print_sign1+"\",\""+print_sign2+"\",\""+print_sign3+"\",\""+print_sign4+"\"]";
			if(id==-1){//新建
				ret = daService.update("insert into collector_set_tb(photoset,prepayset,print_sign,change_prepay," +
								"view_plot,role_id,isprepay,hidedetail,is_sensortime,password,signout_password,signout_valid," +
								"is_show_card,print_order_place2,is_duplicate_order,is_print_name) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
						new Object[]{photoset, prepayset, print_sign, changePrePay, view_plot, roleId, isprepay,
								hidedetail, is_sensortime, collpwd, signpwd, signout_valid, is_show_card, print_order_place2,
								is_duplicate_order,is_print_name});
			}else {//更新
				ret = daService.update("update collector_set_tb set photoset=?,prepayset=?,print_sign=?,change_prepay=?," +
								"view_plot=?,isprepay=?,hidedetail=?,is_sensortime=?,password=?,signout_password=?,signout_valid=?," +
								"is_show_card=?,print_order_place2=?,is_duplicate_order=?,is_print_name=? where role_id=?",
						new Object[]{photoset, prepayset, print_sign, changePrePay, view_plot, isprepay, hidedetail,
								is_sensortime, collpwd, signpwd, signout_valid, is_show_card, print_order_place2, is_duplicate_order,
								is_print_name, roleId});
			}
			AjaxUtil.ajaxOutput(response, "设置成功，请关闭当前页面");
		}
		return mapping.findForward(target);
	}

	private void setList(List<Map<String, Object>> list){
		try {
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					Integer is_inspect = (Integer)map.get("is_inspect");
					Integer is_collector = (Integer)map.get("is_collector");
					Integer is_opencard = (Integer)map.get("is_opencard");

					int func = -1;
					if(is_collector == 1){//收费功能
						func = 1;
					}else if(is_inspect == 1){//巡查功能
						func = 2;
					}else if(is_opencard == 1){//开卡功能
						func = 3;
					}
					map.put("func", func);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
