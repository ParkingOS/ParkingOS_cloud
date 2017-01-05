package com.zld.struts.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.RequestUtil;
import com.zld.utils.TimeTools;

public class TestUtilAction extends Action {
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PgOnlyReadService pgOnlyReadService;
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private PublicMethods publicMethods;

	private Logger logger = Logger.getLogger(TestUtilAction.class);
	/**
	 * weixin
	 */
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		String mobile = RequestUtil.processParams(request, "mobile");
		Long uin = -1L;
		if(!action.equals("")){
			if(mobile.equals("")){
				AjaxUtil.ajaxOutput(response, "-1");
				return null;
			}
			String testmobiles = CustomDefind.getValue("TESTMOBILE");
			if(!testmobiles.contains(mobile)){
				AjaxUtil.ajaxOutput(response, "-2");
				return null;
			}
			Map<String, Object> userMap = pgOnlyReadService
					.getMap("select * from user_info_tb where mobile=? and auth_flag=? ",
							new Object[] { mobile, 4 });
			if(userMap == null){
				AjaxUtil.ajaxOutput(response, "-3");
				return null;
			}
			uin = (Long)userMap.get("id");
		}
		
		if(action.equals("")){
			return mapping.findForward("test");
		}else if(action.equals("allowance")){
			Long today = TimeTools.getToDayBeginTime();
			Map<Long ,String> map = memcacheUtils.doMapLongStringCache("allowance_money", null, null);
			if( map != null && map.get(today) != null){
				Double money = Double.valueOf(map.get(today) + "");
				logger.error("今日补贴总额 ：cache value:"+money+",today:"+today);
				AjaxUtil.ajaxOutput(response, money + "");
			}else{
				AjaxUtil.ajaxOutput(response, "-4");
			}
		}else if(action.equals("clearcache")){
			Integer type = RequestUtil.getInteger(request, "type", 0);
			boolean b = false;
			if(type==0){//清除停车券使用次数 
				Map<Long ,String> map = memcacheUtils.doMapLongStringCache("usetickets_times", null, null);
				if(map!=null){
					if(map.get(uin)!=null){
						map.remove(uin);
						memcacheUtils.doMapLongStringCache("usetickets_times", map, "update");
						b = true;
					}
				}
			}else if(type==1){//清除红包缓存
				Map<Long ,String> map = memcacheUtils.doMapLongStringCache("backtickets_times", null, null);
				if(map!=null){
					if(map.get(uin)!=null){
						map.remove(uin);
						memcacheUtils.doMapLongStringCache("backtickets_times", map, "update");
						b = true;
					}
				}
			}else if(type==2){//清除打赏缓存
				Map<Long ,Long> map = memcacheUtils.doMapLongLongCache("reward_userticket_cache", null, null);
				if(map!=null){
					if(map.get(uin)!=null){
						map.remove(uin);
						memcacheUtils.doMapLongLongCache("reward_userticket_cache", map, "update");
						b = true;
					}
				}
			}else if(type == 3){
				String domain = request.getServerName();
				if(domain.contains("tingchebao.com")){
					AjaxUtil.ajaxOutput(response, "-4");
					return null;
				}
				
				Map<Long ,String> map = memcacheUtils.doMapLongStringCache("allowance_money", null, null);
				Long today = TimeTools.getToDayBeginTime();
				if(map != null && map.get(today) != null){
					map.put(today, "0");
					memcacheUtils.doMapLongStringCache("allowance_money", map, "update");
					b = true;
				}
			}else if(type == 4){
				String domain = request.getServerName();
				if(domain.contains("tingchebao.com")){
					AjaxUtil.ajaxOutput(response, "-4");
					return null;
				}
				Map<Long, String> limitMap = new HashMap<Long, String>();
				memcacheUtils.doMapLongStringCache("allow_park_limit", limitMap, "update");
				b = true;
			}
			if(b){
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "-5");
			}
		}else if(action.equals("clearreward")){
			Long uid = RequestUtil.getLong(request, "uid", -1L);
			if(uid == -1){
				AjaxUtil.ajaxOutput(response, "-4");
				return null;
			}
			int count = daService.update("delete from parkuser_reward_tb where uin=? and ctime>=? and uid=? ",
							new Object[] { uin, TimeTools.getToDayBeginTime(), uid });
			AjaxUtil.ajaxOutput(response, "1");
			return null;
		}else if(action.equals("addticket")){
			Integer begin = RequestUtil.getInteger(request, "begin", 0);
			Integer end = RequestUtil.getInteger(request, "end", 0);
			Integer type = RequestUtil.getInteger(request, "type", 0);
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			if(begin == 0){
				AjaxUtil.ajaxOutput(response, "-4");
				return null;
			}
			
			Long today = TimeTools.getToDayBeginTime();
			Integer resources = 0;
			Integer tickettype = 0;
			if(end == 0){
				end = begin;
			}
			if(type == 1){//专用券
				if(comid == -1){
					AjaxUtil.ajaxOutput(response, "-5");
					return null;
				}
				tickettype = 1;
			}else if(type == 2){
				resources = 1;
			}
			
			List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
			for(int i = begin; i<end + 1; i++){
				Map<String, Object> ticketSqlMap = new HashMap<String, Object>();
				boolean isAuth = publicMethods.isAuthUser(uin);
				//折扣
				Double discount = Double.valueOf(CustomDefind.getValue("NOAUTHDISCOUNT"));
				if(isAuth){
					discount=Double.valueOf(CustomDefind.getValue("AUTHDISCOUNT"));
				}
				Double pmoney = i * discount;
				ticketSqlMap.put("sql", "insert into ticket_tb (create_time,limit_day,money,state,uin,type,comid,resources,pmoney) values(?,?,?,?,?,?,?,?,?)");
				ticketSqlMap.put("values", new Object[]{today, today + 30*24*60*60, i, 0, uin, tickettype, comid, resources,pmoney});
				bathSql.add(ticketSqlMap);
			}
			boolean b = daService.bathUpdate(bathSql);
			if(b){
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "-6");
			}
			return null;
		}else if(action.equals("clearfirst")){
			int r = daService.update("delete from user_account_tb where uin=? ", new Object[]{uin});
			AjaxUtil.ajaxOutput(response, "1");
			return null;
		}else if(action.equals("disbind")){
			int r = daService.update("update user_info_tb set wxp_openid=null where id=? ", new Object[]{uin});
			AjaxUtil.ajaxOutput(response, "1");
		}else if(action.equals("addbalance")){
			Double balance = RequestUtil.getDouble(request, "balance", 0d);
			int r = daService.update("update user_info_tb set balance=? where id=? ",
							new Object[] { balance, uin});
			AjaxUtil.ajaxOutput(response, "1");
		}else if(action.equals("addscore")){
			Double score = RequestUtil.getDouble(request, "score", 0d);
			Long uid = RequestUtil.getLong(request, "uid", -1L);
			if(uid == -1){
				AjaxUtil.ajaxOutput(response, "-4");
				return null;
			}
			int r = daService.update("update user_info_tb set reward_score=? where id=? ",
					new Object[] { score, uid });
			AjaxUtil.ajaxOutput(response, "1");
		}else if(action.equals("clearticket")){
			int r = daService.update("delete from ticket_tb where uin=? ", new Object[]{uin});
			AjaxUtil.ajaxOutput(response, "1");
		}else if(action.equals("viewblack")){
			List<Long> blackUsers = memcacheUtils.doListLongCache("zld_black_users", null, null);
			if(blackUsers!=null){
				if(blackUsers.contains(uin)){
					AjaxUtil.ajaxOutput(response, "1");
					return null;
				}
			}
			AjaxUtil.ajaxOutput(response, "-4");
		}else if(action.equals("towhite")){
			List<Long> blackUsers = memcacheUtils.doListLongCache("zld_black_users", null, null);
			if(blackUsers == null || !blackUsers.contains(uin)){
				AjaxUtil.ajaxOutput(response, "-4");
				return null;
			}
			
			List<Map<String , Object>> sqlMaps = new ArrayList<Map<String,Object>>();
			Map<String, Object> zldblacksqlMap = new HashMap<String, Object>();
			zldblacksqlMap.put("sql", "delete from zld_black_tb where uin=? ");
			zldblacksqlMap.put("values", new Object[]{uin});
			sqlMaps.add(zldblacksqlMap);
			
			List<Map<String, Object>> list = daService
					.getAll("select distinct account from user_payaccount_tb where uin=? ",
							new Object[] { uin });
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					String account = (String)map.get("account");
					Map<String, Object> map2 = new HashMap<String, Object>();
					map2.put("sql", "delete from user_payaccount_tb where account=? ");
					map2.put("values", new Object[]{account});
					sqlMaps.add(map2);
				}
			}
			boolean b = daService.bathUpdate2(sqlMaps);
			//重置缓存
			List<Map<String, Object>> blacklist = daService
					.getAll("select z.uin from zld_black_tb z ,user_info_tb u where z.state=? and u.id = z.uin and u.is_auth = ?",
							new Object[] { 0, 0 });
			List<Long> users = new ArrayList<Long>();
			if(blacklist != null && !blacklist.isEmpty()){
				for(Map<String, Object> map :blacklist){
					Long blackuser = (Long)map.get("uin");
					users.add(blackuser);
				}
				memcacheUtils.doListLongCache("zld_black_users", users, "update");
			}
			AjaxUtil.ajaxOutput(response, "1");
		}else if(action.equals("checkauth")){
			Long count = daService.getLong("select count(*) from user_info_tb where id=? and is_auth=? ", new Object[]{uin, 1});
			if(count > 0){
				AjaxUtil.ajaxOutput(response, "1");
			}else{
				AjaxUtil.ajaxOutput(response, "-4");
			}
		}else if(action.equals("setauth")){
			Integer isauth = RequestUtil.getInteger(request, "isauth", 0);
			int r = daService.update("update user_info_tb set is_auth=? where id=?", new Object[]{isauth,uin});
			AjaxUtil.ajaxOutput(response, "1");
		}else if(action.equals("viewtbypark")){
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			Integer type = RequestUtil.getInteger(request, "type", 0);
			if(comid == -1){
				AjaxUtil.ajaxOutput(response, "-4");
				return null;
			}
			Double money = 0d;
			if(type == 0){//查看该车场今日补贴额
				money = memcacheUtils.readAllowCacheByPark(comid);
			}else if(type == 1){
				money = memcacheUtils.readAllowLimitCacheByPark(comid);
				if(money == null){
					AjaxUtil.ajaxOutput(response, "-5");
					return null;
				}
			}
			AjaxUtil.ajaxOutput(response, money + "");
		}else if(action.equals("settbypark")){
			Long comid = RequestUtil.getLong(request, "comid", -1L);
			Integer type = RequestUtil.getInteger(request, "type", 0);
			Double parklimit = RequestUtil.getDouble(request, "parklimit", 0d);
			if(comid == -1){
				AjaxUtil.ajaxOutput(response, "-4");
				return null;
			}
			String domain = request.getServerName();
			if(domain.contains("tingchebao.com")){
				AjaxUtil.ajaxOutput(response, "-6");
				return null;
			}
			if(type == 0){
				Long today = TimeTools.getToDayBeginTime();
				Map<Long ,String> map = memcacheUtils.doMapLongStringCache("allow_park_money", null, null);
				if( map != null && map.get(comid) != null){
					String info = map.get(comid);
					Long time = Long.valueOf(info.split("_")[0]);//时间
					if(time.intValue() == today.intValue()){
						map.put(comid, time + "_" + 0);
						memcacheUtils.doMapLongStringCache("allow_park_money", map, "update");
					}
				}
			}else if(type == 1){
				Map<Long ,String> map = memcacheUtils.doMapLongStringCache("allow_park_limit", null, null);
				if( map != null && !map.isEmpty()){
					map.put(comid, parklimit + "");
					memcacheUtils.doMapLongStringCache("allow_park_limit", map, "update");
				}else{
					AjaxUtil.ajaxOutput(response, "-5");
					return null;
				}
			}
			AjaxUtil.ajaxOutput(response, "1");
		}else if(action.equals("viewallparklimit")){
			Map<Long ,String> map = memcacheUtils.doMapLongStringCache("allow_park_limit", null, null);
			if(map == null || map.isEmpty()){
				AjaxUtil.ajaxOutput(response, "-4");
				return null;
			}
			AjaxUtil.ajaxOutput(response, map.toString());
		}
		return null;
	}
}
