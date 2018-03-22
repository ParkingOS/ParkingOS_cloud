package com.zld.struts.admin;

import com.zld.CustomDefind;
import com.zld.impl.MongoDbUtils;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.Check;
import com.zld.utils.RequestUtil;
import com.zld.utils.StringUtils;
import com.zld.utils.ZLDType;
import org.apache.log4j.Logger;
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
 * 登录，总管理员，停车场后台管理员，财务等角色可以登录
 * @author Administrator
 *
 */
public class LoginAction extends Action{

	@Autowired
	private DataBaseService daService;

	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	MongoDbUtils mongoDbUtils;
	private Logger logger = Logger.getLogger(LoginAction.class);

	@SuppressWarnings("unchecked")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action= RequestUtil.getString(request, "action");
		logger.error(action);
		if(action.equals("out")){//退出
			String ip = StringUtils.getIpAddr(request);
			Long uin = (Long)request.getSession().getAttribute("loginuin");
			Long comid = (Long)request.getSession().getAttribute("comid");
			if(uin!=null)
				mongoDbUtils.saveLogs(request,0, 0, uin+"退出登录");
			request.getSession().invalidate();
			return mapping.findForward("fail");
		}
		String username =RequestUtil.processParams(request, "username");
		String pass =RequestUtil.processParams(request, "pass");
		String sql = "select * from user_info_tb where state=? and ";//";
		Object [] values = null;
		if(Check.checkUin(username)){
			values=new Object[]{0,Long.valueOf(username),pass};
			sql+=" id=? and password=?" ;
		}else{
			values=new Object[]{0,username,pass};
			sql +=" strid=? and password=? ";
		}
		request.getSession().setAttribute("unionId", CustomDefind.getValue("UNIONID"));
		request.getSession().setAttribute("custumgroup", CustomDefind.getValue("CUSTUMGROUP"));
		String target = "success";
		logger.error(sql+">"+StringUtils.objArry2String(values));
		Map<String, Object> user = daService.getPojo(sql, values);
		logger.error(user);
		if(user==null){
			request.setAttribute("errormessage", "帐号或密码不正确!");
			request.setAttribute("username", username);
			return mapping.findForward("fail");
		}
		String logourl = "images/logo_top.png?a=111";
		Long role = -1L;
		if(user.get("auth_flag") != null){
			role = Long.valueOf(user.get("auth_flag").toString());
		}
		Long roleId = (Long)user.get("role_id");
		Map<String, Object> roleMap = daService.getMap("select * from user_role_tb where id =?",
				new Object[]{roleId});
		request.getSession().setAttribute("isadmin", 0);//是否是管理员 0否1是
		request.getSession().setAttribute("loginroleid", roleId);//角色 -1没有
		request.getSession().setAttribute("adminid", -1L);//当前角色所属的管理员账户
		request.getSession().setAttribute("supperadmin",0);//是否总管理员 0否1是
		request.getSession().setAttribute("loginuin",user.get("id"));
		request.getSession().setAttribute("comid",user.get("comid"));
		logger.error(roleMap);
		if(roleMap!=null){
			if(roleId == 0 || roleId == 8){
				request.getSession().setAttribute("supperadmin",1);//是否总管理员 0否1是
			}
		}
		logger.error(roleId);
		if(roleId!=null && roleId > -1){
			Map<String, Object> orgMap = pgOnlyReadService.getMap("select name from zld_orgtype_tb where id=? ",
					new Object[]{roleMap.get("oid")});
			if(orgMap == null){
				request.setAttribute("errormessage", "组织类型不存在！");
				target="fail";
			}else{
				request.getSession().setAttribute("oid", roleMap.get("oid"));//该登录角色所属组织类型
				target ="parkmanage";
				String orgname = (String)orgMap.get("name");
				logger.error(orgname);
				if(orgname.contains("车场")){
					request.getSession().setAttribute("isadmin", 1);//是否是管理员 0否1是
					request.setAttribute("cloudname", " 智慧停车云-车场云 ");
					Long count = pgOnlyReadService.getLong("select count(id) from com_info_tb where state=? and id=? ",
							new Object[] { 0, user.get("comid") });

					if(count == 0){
						request.setAttribute("errormessage", "车场不存在或者车场未通过审核!");
						target="fail";
					}else{
						Map<String, Object> comMap = daService.getMap("select chanid from com_info_tb where id=? ",
								new Object[]{user.get("comid")});
						if(comMap != null && comMap.get("chanid") != null){
							Long chanid = (Long)comMap.get("chanid");
							if(chanid > 0){
								Map<String, Object> map = daService.getMap("select * from logo_tb where type=? and orgid=? ",
										new Object[]{0, chanid});
								if(map != null&& map.get("url_fir") != null){
									logourl = "cloudlogo.do?action=downloadlogo&type=0&orgid="+chanid+"&number=0&r="+Math.random();
								}
							}
						}
					}
				}else if(orgname.contains("渠道")){
					request.getSession().setAttribute("isadmin", 1);//是否是管理员 0否1是
					request.setAttribute("cloudname", "智慧停车云-渠道云 ");
					request.getSession().setAttribute("comid",0L);
					request.getSession().setAttribute("chanid",user.get("chanid"));
					Long chancount = pgOnlyReadService.getLong("select count(id) from org_channel_tb where id=? and state=? ",
							new Object[]{user.get("chanid"), 0});
					if(chancount == 0){
						request.setAttribute("errormessage", "渠道不存在!");
						target="fail";
					}else{
						Map<String, Object> map = daService.getMap("select * from logo_tb where type=? and orgid=? ",
								new Object[]{0, user.get("chanid")});
						if(map != null&& map.get("url_fir") != null){
							logourl = "cloudlogo.do?action=downloadlogo&type=0&orgid="+user.get("chanid")+"&number=0&r="+Math.random();
						}
					}
				}else if(orgname.contains("集团")){
					request.getSession().setAttribute("isadmin", 1);//是否是管理员 0否1是
					request.setAttribute("cloudname", "智慧城市云 ");
					request.getSession().setAttribute("comid",0L);
					request.getSession().setAttribute("groupid",user.get("groupid"));
					Long groupcount = pgOnlyReadService.getLong("select count(id) from org_group_tb where id=? and state=? ",
							new Object[]{user.get("groupid"), 0});
					logger.error(">>>>>>>>>>>>>"+groupcount);
					if(groupcount == 0){
						request.setAttribute("errormessage", "集团不存在!");
						target="fail";
					}
				}else if(orgname.contains("城市")){
					request.getSession().setAttribute("isadmin", 1);//是否是管理员 0否1是
					request.setAttribute("cloudname", "智慧停车云-城市云 ");
					request.getSession().setAttribute("comid",0L);
					request.getSession().setAttribute("cityid",user.get("cityid"));
					Long groupcount = pgOnlyReadService.getLong("select count(id) from org_city_merchants where id=? and state=? ",
							new Object[]{user.get("cityid"), 0});
					if(groupcount == 0){
						request.setAttribute("errormessage", "城市不存在!");
						target="fail";
					}
				}
			}
			List<Map<String, Object>> authList = null;
			if(roleId == 0){//总管理员拥有所有权限
				authList = daService.getAll("select actions,id auth_id,nname,pid,url,sort,sub_auth childauths from auth_tb where oid=? and state=? ",
						new Object[]{roleMap.get("oid"), 0});
				logger.error(authList);
				if(authList != null){
					for(Map<String, Object> map : authList){
						if(map.get("childauths") != null){
							String childauths = (String)map.get("childauths");
							if(!childauths.equals("")){
								String[] subs = childauths.split(",");
								String subauth = null;
								for(int i=0; i<subs.length; i++){
									if(i == 0){
										subauth = "" + i;
									}else{
										subauth += ","+i;
									}
								}
								map.put("sub_auth", subauth);
							}
						}
					}
				}
			}else{
				//读取权限
				authList = daService.getAll("select a.actions,auth_id,ar.sub_auth,nname,a.pid,a.url,a.sort " +
						"from auth_role_tb ar left join" +
						" auth_tb a on ar.auth_id=a.id " +
						" where role_id=? order by  a.sort " , new Object[]{roleId});
			}

			request.getSession().setAttribute("ishdorder", user.get("order_hid"));
			request.getSession().setAttribute("authlist", authList);
			request.getSession().setAttribute("menuauthlist", StringUtils.createJson(authList));
			//该组织下的所有功能列表
			List<Map<String, Object>> allAuthList = daService.getAll("select * from auth_tb where oid=? and state=? ",
					new Object[]{roleMap.get("oid"), 0});
			request.getSession().setAttribute("allauth", allAuthList);
		}else {
			//role: 0总管理员，1停车场后台管理员 ，2车场收费员，3财务，4车主  5市场专员 6录入员
			if(role.intValue()==ZLDType.ZLD_COLLECTOR_ROLE||role.intValue()==ZLDType.ZLD_CAROWER_ROLE||role.intValue() == ZLDType.ZLD_KEYMEN){//车场收费员及车主不能登录后台
				request.setAttribute("errormessage", "没有查询后台数据权限，请联系管理员!");
				target="fail";
			}else if(role.intValue()==ZLDType.ZLD_PARKADMIN_ROLE){
				target ="parkmanage";
				request.setAttribute("cloudname", "智慧停车云-车场云 ");
				Long count = pgOnlyReadService.getLong(
						"select count(id) from com_info_tb where state=? and id=? ",
						new Object[] { 0, user.get("comid") });
				if(count == 0){
					request.setAttribute("errormessage", "车场不存在或者车场未通过审核!");
					target="fail";
				}
			}else if(role.intValue()==ZLDType.ZLD_ACCOUNTANT_ROLE){
				target ="finance";
			}else if(role.intValue()==ZLDType.ZLD_CARDOPERATOR){
				target ="cardoperator";
			}else if(role.intValue()==ZLDType.ZLD_MARKETER){//市场专员 登录后台
				request.getSession().setAttribute("marketerid",user.get("id"));
				target ="marketer";
			}else if(role.intValue()==ZLDType.ZLD_RECORDER||role.intValue()==ZLDType.ZLD_KEFU||role.intValue()==ZLDType.ZLD_QUERYKEFU){
				target = "recorder";
			}else 	if(role==0){//总管理员
				request.getSession().setAttribute("supperadmin",1);
			}else{
				request.setAttribute("errormessage", "没有登录权限!");
				target="fail";
			}
		}
		request.getSession().setAttribute("role",role );
		logger.error(role);
		request.getSession().setAttribute("userinfo",user);
		request.getSession().setAttribute("userid", username);
		String nickname = "";
		if(user.get("nickname") != null){
			nickname = (String)user.get("nickname");
			if(nickname.length() > 4){
				nickname = nickname.substring(0, 4) + "...";
			}
		}
		request.getSession().setAttribute("nickname", nickname);

		String logContent = username+"登录，返回:"+target;
		String ip = StringUtils.getIpAddr(request);
		mongoDbUtils.saveLogs(request, 0, 0,logContent);
//		List<Object[]> valuesList = ReadFile.praseFile();
//		int result = daService.bathInsert("insert into com_info_tb (longitude,latitude,company_name,address,type) values(?,?,?,?,?)",
//				valuesList, new int[]{3,3,12,12,4});
//		System.out.println(result);
		request.setAttribute("logourl", logourl);
		logger.error("over...."+target);
		return mapping.findForward(target);
	}

}