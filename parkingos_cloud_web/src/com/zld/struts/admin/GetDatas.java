package com.zld.struts.admin;

import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.impl.CommonMethods;
import com.zld.service.PgOnlyReadService;
import com.zld.utils.*;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


public class GetDatas extends Action{

	@Autowired
	PgOnlyReadService dataBaseService;
	@Autowired
	private PgOnlyReadService readService;
	@Autowired
	CommonMethods commonMethods;

	Logger logger = Logger.getLogger(GetDatas.class);

	@SuppressWarnings("unused")
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.processParams(request, "action");
		String userId = (String)request.getSession().getAttribute("userid");

		if(userId==null&&action.indexOf("lott")==-1){
			response.sendRedirect("login.do");
			return null;
		}
		if(action.equals("markets")){
			List<Map<String,Object>> tradsList = dataBaseService.getAll("select id,nickname from user_info_tb where state =? and comid=? and (auth_flag=? or auth_flag=?) ",
					new Object[]{0,0,5,11});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("nickname")+"\"}";
				}
			}

			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getChannels")){
			Long id =RequestUtil.getLong(request, "id", -1L);
			List<Map<String, Object>> channels = commonMethods.getChannels(id+"",null);
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(channels!=null&&channels.size()>0){
				for(Map map : channels){
					result+=",{\"value_no\":\""+map.get("value_no")+"\",\"value_name\":\""+map.get("value_name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
			return null;
		}else if(action.equals("getMonitors")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			List<Map<String, Object>> channels = commonMethods.getMonitors(id + "", null);
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if (channels != null && channels.size() > 0) {
				for (Map map : channels) {
					result += ",{\"value_no\":\"" + map.get("value_no") + "\",\"value_name\":\"" + map.get("value_name") + "\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
			return null;
		}else if(action.equals("getpark")){
			Long id =RequestUtil.getLong(request, "id", -1L);
			List<Map<String,Object>> tradsList = dataBaseService.getAll("select id,company_name from com_info_tb  where uid =? ",
					new Object[]{id});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("company_name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getcartype")){
			Long id =RequestUtil.getLong(request, "id", -1L);
			Long groupid =RequestUtil.getLong(request, "groupid", -1L);
			List<Map<String,Object>> tradsList =null;
			if(id!=-1){
				tradsList=dataBaseService.getAll("select id,name from car_type_tb  where comid =? and is_delete=? ",new Object[]{id,0});
			}else if(groupid!=-1){
				tradsList=dataBaseService.getAll("select id,name from car_type_tb  where comid in (select " +
						"id from com_info_tb where groupid=?) and is_delete=? ",new Object[]{groupid,0});
			}
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getuser")){
			Long id =RequestUtil.getLong(request, "id", -1L);
			// Long uin = (Long)request.getSession().getAttribute("loginuin");
			List<Map <String,Object>> tradsList= dataBaseService.getAll("select id,nickname from user_info_tb where comid=? ",
					new Object[]{id});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("nickname")+"\"}";
				}
			}

			result+="]";
			result = result.replace("null", "");
			AjaxUtil.ajaxOutput(response, result);
		}
		else if(action.equals("getuserbyuin")){
			//Long id =RequestUtil.getLong(request, "id", -1L);
			//Long uin = (Long)request.getSession().getAttribute("loginuin");
			List<Map<String,Object>> tradsList=null;
			Long cityid = (Long)request.getSession().getAttribute("cityid");
			Long comid = (Long)request.getSession().getAttribute("comid");
			Long groupid = (Long)request.getSession().getAttribute("groupid");

			if(cityid!=null && cityid > 0)
			{
				tradsList = dataBaseService.getAll("select id,nickname from user_info_tb where cityid=? ",
						new Object[]{cityid});
			}
			if(groupid!=null && groupid > 0)
			{
				tradsList = dataBaseService.getAll("select id,nickname from user_info_tb where groupid=? ",
						new Object[]{groupid});
			}
			if(comid!=null && comid>0)
			{
				tradsList = dataBaseService.getAll("select id,nickname from user_info_tb where comid=? ",
						new Object[]{comid});
			}
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("nickname")+"\"}";
				}
			}
			result+="]";
			result = result.replace("null", "");
			logger.error("getuserbyuin cityid:"+cityid+",comid:"+comid+",groupid:"+groupid);
			AjaxUtil.ajaxOutput(response, result);
		}
		else if(action.equals("getvalue")){
			String type = RequestUtil.getString(request, "type");
			Long id =RequestUtil.getLong(request, "id", -1L);
			String name =id+"";
			if(type.equals("parkname")){
				name = ParkingMap.getParkName(id);
				if(name==null){
					Map comMap = dataBaseService.getMap("select company_name from com_info_tb where id =?", new Object[]{id});
					if(comMap!=null&&comMap.get("company_name")!=null)
						name = (String)comMap.get("company_name");
					ParkingMap.putParkName(id, name);
				}
			}else if(type.equals("parkername")){
				name = ParkingMap.getUserName(id);
				if(name==null){
					Map comMap = dataBaseService.getMap("select company_name from com_info_tb where id =?", new Object[]{id});
					if(comMap!=null&&comMap.get("company_name")!=null)
						name = (String)comMap.get("company_name");
					ParkingMap.putParkName(id, name);
				}
			}
			if(name!=null)
				AjaxUtil.ajaxOutput(response, name);
			else {
				AjaxUtil.ajaxOutput(response, "");
			}
		}else if(action.equals("getpname")){
			Long comid = RequestUtil.getLong(request, "id", -1L);
			List<Map<String,Object>>  pList = null;
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(comid!=-1){
				Long ntime = System.currentTimeMillis()/1000;
				String sql = "select id,p_name from product_package_tb where (comid=? ";
				List comsList = dataBaseService.getAll("select * from com_info_tb where pid = ?",new Object[]{comid});
				Object[] parm = new Object[comsList.size()+1];
				parm[0] = comid;
				List<Object> params = new ArrayList<Object>();
				params.add(comid);
				for (int i = 1; i < parm.length; i++) {
					long comidoth = Long.parseLong(((Map)comsList.get(i-1)).get("id")+"");
					parm[i] = comidoth;
					params.add(comidoth);
					sql += " or comid = ? ";
				}
//				params.add(0);
//				params.add(ntime+30*24*60*60);
				params.add(0);
				/*pList = dataBaseService.getAll(sql +") and is_delete=? and limitday >? and state=? ",params,1,Integer.MAX_VALUE);*/
				pList = dataBaseService.getAll(sql +") and is_delete=? ",params,1,Integer.MAX_VALUE);
				if(pList!=null&&pList.size()>0){
					for(Map map : pList){
						result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("p_name")+"\"}";
					}
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getpackage")){
			Long groupid = RequestUtil.getLong(request, "id", -1L);
			List<Map<String,Object>>  pList = null;
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(groupid!=-1){
				Long ntime = System.currentTimeMillis()/1000;
				String sql = "select id,p_name from product_package_tb where comid in(select id from com_info_tb " +
						"where groupid =?  ";
				pList = dataBaseService.getAll(sql +") and is_delete=? ",new Object[]{groupid,0});
				if(pList!=null&&pList.size()>0){
					for(Map map : pList){
						result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("p_name")+"\"}";
					}
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getpass")){//获取通道列表
			Long worksite_id = RequestUtil.getLong(request, "id", -1L);
			String sql = "select * from com_pass_tb where worksite_id=?";
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAll(sql, new Object[]{worksite_id});
			String result = "[{\"value_no\":\"\",\"value_name\":\"请选择\"}";
			if(!list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("passname")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getcompass")){//获取车场所有通道列表
			Long comid = RequestUtil.getLong(request, "id", -1L);
			String sql = "select * from com_pass_tb where comid=? and state= ? ";
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAll(sql, new Object[]{comid,0});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(!list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("passname")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getsubcoms")){//获取车场以及子车场
			Long comid = RequestUtil.getLong(request, "id", -1L);
			String sql = "select * from com_info_tb where pid=? or id = ? order by id desc";
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAll(sql, new Object[]{comid,comid});
			String result = "[{\"value_no\":\"\",\"value_name\":\"请选择\"}";
			if(!list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("company_name")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}
		/*else if(action.equals("addlott")){//保存中奖
			Long orderId = RequestUtil.getLong(request, "orderid", -1L);
			Integer lott = RequestUtil.getInteger(request, "lott", -1);
			Long uin = dataBaseService.getLong("select uin from order_tb where id=?",new Object[]{orderId});
			//是否是
			Long count  = dataBaseService.getLong("select count(Id) from lottery_tb where orderid=? and lottery_result>?",
					new Object[]{orderId,-1});
			int ret =0;
			if(count>0){//未设置过奖品
				ret=-1;
			}else {

				List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
				//写入用户明细
				Map<String, Object> userAccountSqlMap = new HashMap<String, Object>();
				//更新抽奖结果
				Map<String, Object> lotterySqlMap = new HashMap<String, Object>();
				//更新车主账户
				Map<String, Object> userSqlMap = new HashMap<String, Object>();
				userSqlMap.put("sql", "update user_info_tb set balance = balance+? where id=? ");
				userSqlMap.put("values", new Object[]{lott+1,uin});
				if(lott<3)
					bathSql.add(userSqlMap);

				userAccountSqlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type) values(?,?,?,?,?,?)");
				userAccountSqlMap.put("values",  new Object[]{uin,lott+1,0,System.currentTimeMillis()/1000,"充值",8});
				if(lott<3)
					bathSql.add(userAccountSqlMap);

				lotterySqlMap.put("sql", "update lottery_tb set lottery_result = ? ,create_time=? where orderid=?");
				lotterySqlMap.put("values", new Object[]{lott,System.currentTimeMillis()/1000,orderId});
				bathSql.add(lotterySqlMap);
				//批量更新
				boolean result = dataBaseService.bathUpdate(bathSql);
				if(result)
					ret=1;
			}
			AjaxUtil.ajaxOutput(response, ret+"");
		}*/
		else if(action.equals("lottery")){//查询是否可以抽奖
			Long orderId = RequestUtil.getLong(request, "id", -1L);
			Map orderMap = dataBaseService.getMap("select uin from order_tb where id  = ? ", new Object[]{orderId});
			Long uin = -1L;
			if(orderMap!=null&&orderMap.get("uin")!=null)
				uin= (Long)orderMap.get("uin");
			Long count = dataBaseService.getLong("select count(id) from lottery_tb where uin =? and create_time>? and lottery_result>? ",
					new Object[]{uin,TimeTools.getToDayBeginTime(),0});
			if(count>0){//今日已抽奖
				count=0L;
				//System.out.println(">>>>>uin:"+uin+",今天已抽过奖!");
			}else {
				count  = dataBaseService.getLong("select count(Id) from lottery_tb where orderid=? and lottery_result<?",
						new Object[]{orderId,0});
				/*if(count<1){
					System.out.println(">>>>>orderid:"+orderId+",今天已抽过奖!");
				}else {
					System.out.println(">>>>>orderid:"+orderId+",今天可以过奖!");
				}*/
			}
			System.out.println(">>>>orderid:"+orderId);
			AjaxUtil.ajaxOutput(response, count+"");
			//http://192.168.199.240/zld/getdata.do?action=lottery&id=386
		}else if(action.equals("getbonustypes")){
			List<Map<String,Object>> tradsList = dataBaseService.getAll("select id,name from bonus_type_tb where state =? order by id ",
					new Object[]{1});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		} else if (action.equals("getauditors")) {
			List<Map<String, Object>> list = dataBaseService
					.getAll("select id,nickname from user_info_tb where (auth_flag=? or auth_flag=?) and state=? ",
							new Object[]{0, 7, 0});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("nickname")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getcompass")){
			String id = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "id"));
			String sql = "select c.id,c.passname,w.worksite_name from com_pass_tb c left join com_worksite_tb w on c.worksite_id=w.id ";
			List<Object> params  = new ArrayList<Object>();
			if(!id.equals("")){
				params.add(Long.valueOf(id));
				sql +=" where c.comid=? ";
			}
			List<Map<String,Object>> tradsList = dataBaseService.getAllMap(sql, params);
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("passname")+"("+map.get("worksite_name")+")\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getIbeaconPark")){
			List<Map<String,Object>> tradsList = dataBaseService.getAll("select id,company_name from com_info_tb where state=? and etc =?", new Object[]{0, 1});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("company_name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getWorksitePark")){
			List<Map<String,Object>> tradsList = dataBaseService.getAll("select id,company_name from com_info_tb where state=? and id in(select comid from com_worksite_tb)",new Object[]{0});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("company_name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getworksite")){
			Long comid = RequestUtil.getLong(request, "id", -1L);
			List<Map<String,Object>> tradsList =null;
			if(comid>0)
				tradsList = dataBaseService.getAll("select id,worksite_name from com_worksite_tb where state=? and comid =?",new Object[]{0,comid});
			else
				tradsList = dataBaseService.getAll("select id,worksite_name from com_worksite_tb where state=? ", new Object[]{0});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("worksite_name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getcity")){
			Map<Integer , String> localDataMap = GetLocalCode.localDataMap;
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"全部\"}";
			if(localDataMap != null){
				String city = CustomDefind.getValue("CITY");
				if(city != null){
					String cities[] = city.split(",");
					for(int i=0; i<cities.length; i++){
						result+=",{\"value_no\":\""+cities[i]+"\",\"value_name\":\""+localDataMap.get(Integer.valueOf(cities[i]))+"\"}";
					}
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getauth")){
			Long authId = RequestUtil.getLong(request, "authid", -1L);
			List<Map<String, Object>> authList=(List<Map<String, Object>>)request.getSession().getAttribute("authlist");
			String auths ="";
			if(authList!=null)
				for(Map<String, Object> aMap : authList){
					Long aid =(Long)aMap.get("auth_id");
					if(aid.equals(authId)){
						auths = (String)aMap.get("sub_auth");
					}
				}
			AjaxUtil.ajaxOutput(response, auths);
		}else if(action.equals("getauthmenu")){
			Long authId = RequestUtil.getLong(request, "authid", -1L);
			List<Map<String, Object>> authList=(List<Map<String, Object>>)request.getSession().getAttribute("authlist");
			List<Map<String, Object>> menuList = new ArrayList<Map<String,Object>>();
			if(authList!=null){
				for(Map<String, Object> aMap : authList){
					Long pid =(Long)aMap.get("pid");
					if(pid.equals(authId)){
						Long aid = (Long)aMap.get("auth_id");
						List<Map<String, Object>> subList = getSubAuth(authList, aid);
						menuList.add(aMap);
						if(!subList.isEmpty())
							aMap.put("subauth", StringUtils.getJson(subList));
					}
				}
				Collections.sort(menuList,new Comparator<Map<String, Object>>() {
					public int compare(Map<String, Object> o1,
									   Map<String, Object> o2) {
						Integer aid1=(Integer)o1.get("sort");
						Integer aid2=(Integer)o2.get("sort");
						Integer comp = (aid1-aid2);
						return comp;
					}
				});
			}
			String ret =  StringUtils.getJson(menuList);
			System.err.println(ret);
			AjaxUtil.ajaxOutput(response, ret);
		}else if(action.equals("getprodsum")){
			Long prodId = RequestUtil.getLong(request, "p_name", -1L);
			Integer months = RequestUtil.getInteger(request, "months", 0);

			Double total = commonMethods.getProdSum(prodId, months);
			AjaxUtil.ajaxOutput(response, total+"");
		}else if(action.equals("getchans")){
			String sql = "select * from org_channel_tb where state=?";
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAll(sql, new Object[]{0});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(!list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("name")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("cities")){
			String sql = "select * from org_city_merchants where state=?";
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAll(sql, new Object[]{0});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(!list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("name")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getcoms")){
			Long groupid = RequestUtil.getLong(request, "groupid", -1L);
			Long cityid = RequestUtil.getLong(request, "cityid", -1L);
			List<Object> params = new ArrayList<Object>();
			params.add(1);
			List<Object> parks = null;
			if(cityid > 0){
				parks = commonMethods.getparks(cityid);
			}else if(groupid > 0){
				parks = commonMethods.getParks(groupid);
			}
			String result = "[{\"value_no\":\"-999\",\"value_name\":\"没有车场可选\"}]";
			if(cityid<0&&groupid<0){
				AjaxUtil.ajaxOutput(response, result);
				return null;
			}
			if(parks != null && !parks.isEmpty()){
				params.addAll(parks);
				String preParams  ="";
				for(Object parkid : parks){
					if(preParams.equals(""))
						preParams ="?";
					else
						preParams += ",?";
				}
				List<Map<String, Object>> list = dataBaseService.getAllMap("select id value_no,company_name value_name from com_info_tb where state!=? and id in ("+preParams+") order by id ", params);
				if(list != null && !list.isEmpty()){
					result = StringUtils.createJson(list);
				}
				AjaxUtil.ajaxOutput(response, result);
			}else{
				AjaxUtil.ajaxOutput(response, "[]");
			}
		}else if(action.equals("getparkname")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String name = "";
			if(id>0){
				Map parkMap = dataBaseService.getMap("select company_name from com_info_tb where id =?", new Object[]{id});
				if(parkMap!=null&&!parkMap.isEmpty())
					name =  parkMap.get("company_name")+"";
			}
			AjaxUtil.ajaxOutput(response,name);
		}else if(action.equals("orgtree")){
			List<Map<String, Object>> orgList = dataBaseService.getAll("select * from zld_orgtype_tb where state=? order by sort",
					new Object[]{0});
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> curlist = new ArrayList<Map<String,Object>>();
			Map<String, Object> rootMap = new HashMap<String, Object>();
			rootMap.put("sysid", "root_0");
			rootMap.put("treeid", 0);
			rootMap.put("name", "组织类型");
			rootMap.put("id", 0L);
			curlist.add(rootMap);
			while(curlist != null && !curlist.isEmpty()){
				list.addAll(curlist);
				curlist = setTreeList(orgList, curlist);
			}

			int selnode = 0;//初始状态被选中的节点
			if(list.size() > 0){
				selnode = 1;
			}
			for(Map<String, Object> map : list){
				Integer treeid = (Integer)map.get("treeid");
				Long oid = (Long)map.get("id");
				if(treeid == 0){
					map.put("selnode", selnode);
				}else{
					map.put("url", "function.do?action=functree&oid="+oid);
				}
			}
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
		}else if(action.equals("orgmanage")){
			List<Map<String, Object>> orgList = dataBaseService.getAll("select * from zld_orgtype_tb where state=? order by sort",
					new Object[]{0});
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> curlist = new ArrayList<Map<String,Object>>();
			Map<String, Object> rootMap = new HashMap<String, Object>();
			rootMap.put("sysid", "root_0");
			rootMap.put("treeid", 0);
			rootMap.put("name", "组织类型");
			rootMap.put("id", 0L);
			curlist.add(rootMap);
			while(curlist != null && !curlist.isEmpty()){
				list.addAll(curlist);
				curlist = setTreeList(orgList, curlist);
			}

			int selnode = 0;//初始状态被选中的节点
			for(Map<String, Object> map : list){
				Integer treeid = (Integer)map.get("treeid");
				Long oid = (Long)map.get("id");
				if(treeid == 0){
					map.put("selnode", selnode);
				}
				map.put("url", "orgmanage.do?&pid="+oid);
			}
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
		}else if(action.equals("orgrole")){
			List<Map<String, Object>> orgList = dataBaseService.getAll("select * from zld_orgtype_tb where state=? order by sort",
					new Object[]{0});
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> curlist = new ArrayList<Map<String,Object>>();
			Map<String, Object> rootMap = new HashMap<String, Object>();
			rootMap.put("sysid", "root_0");
			rootMap.put("treeid", 0);
			rootMap.put("name", "组织类型");
			rootMap.put("id", 0L);
			curlist.add(rootMap);
			while(curlist != null && !curlist.isEmpty()){
				list.addAll(curlist);
				curlist = setTreeList(orgList, curlist);
			}

			int selnode = 0;//初始状态被选中的节点
			if(list.size() > 0){
				selnode = 1;
			}
			for(Map<String, Object> map : list){
				Integer treeid = (Integer)map.get("treeid");
				Long oid = (Long)map.get("id");
				if(treeid == 0){
					map.put("selnode", selnode);
				}else{
					map.put("url", "orgrole.do?action=orgrole&oid="+oid);
				}
			}
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
		}else if(action.equals("functree")){
			Long oid = RequestUtil.getLong(request, "oid", -1L);
			if(oid > 0){
				List<Map<String, Object>> funcList = dataBaseService.getAll("select * from auth_tb where state=? and oid=? order by sort",
						new Object[]{0, oid});
				List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
				List<Map<String, Object>> curlist = new ArrayList<Map<String,Object>>();
				Map<String, Object> rootMap = new HashMap<String, Object>();
				rootMap.put("sysid", "root_0");
				rootMap.put("treeid", 0);
				rootMap.put("nname", "功能管理");
				rootMap.put("id", 0L);
				curlist.add(rootMap);
				while(curlist != null && !curlist.isEmpty()){
					list.addAll(curlist);
					curlist = setTreeList(funcList, curlist);
				}

				int selnode = 0;//初始状态被选中的节点
				for(Map<String, Object> map : list){
					Integer treeid = (Integer)map.get("treeid");
					Long pid = (Long)map.get("id");
					if(treeid == 0){
						map.put("selnode", selnode);
					}
					map.put("url", "function.do?pid="+pid+"&oid="+oid);
					map.put("name", map.get("nname"));
				}
				AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
			}
		}else if(action.equals("getorg")){
			String sql = "select * from zld_orgtype_tb where state=? ";
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAll(sql, new Object[]{0});
			String result = "[{\"value_no\":\"0\",\"value_name\":\"无\"}";
			if(!list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("name")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getfunc")){
			String sql = "select * from auth_tb where state=? ";
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAll(sql, new Object[]{0});
			String result = "[{\"value_no\":\"0\",\"value_name\":\"无\"}";
			if(!list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("nname")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("citysetting")){
			Long cityid = RequestUtil.getLong(request, "cityid", -1L);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			Map<String, Object> rootMap = new HashMap<String, Object>();
			rootMap.put("sysid", "root_0");
			rootMap.put("treeid", 0);
			rootMap.put("name", "城市管理");
			rootMap.put("id", 0L);
			list.add(rootMap);
			int selnode = 0;//初始状态被选中的节点
			if(list.size() > 0){
				selnode = 1;
			}
			rootMap.put("selnode", selnode);
			Map<String, Object> child1 = new HashMap<String, Object>();
			child1.put("sysid", "0_1");
			child1.put("treeid", "1");
			child1.put("name", "员工管理");
			child1.put("url", "citymember.do?cityid="+cityid);
			list.add(child1);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
		}else if(action.equals("groupsetting")){
			Long groupid = RequestUtil.getLong(request, "groupid", -1L);
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			Map<String, Object> rootMap = new HashMap<String, Object>();
			rootMap.put("sysid", "root_0");
			rootMap.put("treeid", 0);
			rootMap.put("name", "运营集团管理");
			rootMap.put("id", 0L);
			list.add(rootMap);
			int selnode = 0;//初始状态被选中的节点
			if(list.size() > 0){
				selnode = 1;
			}
			rootMap.put("selnode", selnode);
			Map<String, Object> child1 = new HashMap<String, Object>();
			child1.put("sysid", "0_1");
			child1.put("treeid", "1");
			child1.put("name", "员工管理");
			child1.put("url", "groupmember.do?groupid="+groupid);
			list.add(child1);
			Map<String, Object> child2 = new HashMap<String, Object>();
			child2.put("sysid", "0_2");
			child2.put("treeid", "2");
			child2.put("name", "提现账户");
			child2.put("url", "");
			list.add(child2);
			Map<String, Object> child3 = new HashMap<String, Object>();
			child3.put("sysid", "2_201");
			child3.put("treeid", "201");
			child3.put("name", "公司账户");
			child3.put("url", "comaccount.do?type=0&groupid="+groupid);
			list.add(child3);
			Map<String, Object> child4 = new HashMap<String, Object>();
			child4.put("sysid", "2_202");
			child4.put("treeid", "202");
			child4.put("name", "对公账户");
			child4.put("url", "comaccount.do?type=2&groupid="+groupid);
			list.add(child4);
			Map<String, Object> child5 = new HashMap<String, Object>();
			child5.put("sysid", "2_203");
			child5.put("treeid", "203");
			child5.put("name", "个人账户");
			child5.put("url", "comaccount.do?type=1&groupid="+groupid);
			list.add(child5);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
		}else if(action.equals("epaysetting")){
			Long statsid = RequestUtil.getLong(request, "statsid", -1L);
			Integer seltype = RequestUtil.getInteger(request, "seltype", -1);
			String btime = RequestUtil.processParams(request, "btime");
			String etime = RequestUtil.processParams(request, "etime");
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			Map<String, Object> rootMap = new HashMap<String, Object>();
			rootMap.put("sysid", "root_0");
			rootMap.put("treeid", 0);
			rootMap.put("name", "电子账户明细");
			rootMap.put("id", 0L);
			list.add(rootMap);
			int selnode = 0;//初始状态被选中的节点
			if(list.size() > 0){
				selnode = 1;
			}
			rootMap.put("selnode", selnode);
			Map<String, Object> child1 = new HashMap<String, Object>();
			child1.put("sysid", "0_1");
			child1.put("treeid", "1");
			child1.put("name", "运营集团电子账户明细");
			child1.put("url", "statsaccount.do?&statsid="+statsid+
					"&btime="+btime+"&etime="+etime+"&seltype="+seltype+"&from=4");
			list.add(child1);
			Map<String, Object> child2 = new HashMap<String, Object>();
			child2.put("sysid", "0_2");
			child2.put("treeid", "2");
			child2.put("name", "停车场电子账户明细");
			child2.put("url", "statsaccount.do?&statsid="+statsid+
					"&btime="+btime+"&etime="+etime+"&seltype="+seltype+"&from=3");
			list.add(child2);
			Map<String, Object> child3 = new HashMap<String, Object>();
			child3.put("sysid", "0_3");
			child3.put("treeid", "3");
			child3.put("name", "收费员电子账户明细");
			child3.put("url", "statsaccount.do?&statsid="+statsid+
					"&btime="+btime+"&etime="+etime+"&seltype="+seltype+"&from=2");
			list.add(child3);
			Map<String, Object> child4 = new HashMap<String, Object>();
			child4.put("sysid", "0_4");
			child4.put("treeid", "4");
			child4.put("name", "城市商户电子账户明细");
			child4.put("url", "statsaccount.do?&statsid="+statsid+
					"&btime="+btime+"&etime="+etime+"&seltype="+seltype+"&from=5");
			list.add(child4);
			AjaxUtil.ajaxOutput(response, StringUtils.createJson(list));
		}else if(action.equals("getgroups")){
			String sql = "select * from org_group_tb where state=? ";
			Long cityid = RequestUtil.getLong(request, "cityid", -1L);
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			if(cityid>0){
				params.add(cityid);
				sql += " and cityid=? ";
			}
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAllMap(sql, params);
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(!list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("name")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getareas")){
			Long groupid = RequestUtil.getLong(request, "id", -1L);
			String sql = "select * from org_area_tb where state=? and groupid=? ";
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(groupid);
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAllMap(sql, params);
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(!list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("name")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getcitygroups")){
			Long cityid = RequestUtil.getLong(request, "cityid", -1L);
			if(cityid==-1)
				cityid = RequestUtil.getLong(request, "id", -1L);
			String sql = "select * from org_group_tb where state=? and cityid=? ";
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(cityid);
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAllMap(sql, params);
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(cityid > 0 && !list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("name")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getarea")){
			Long areaid = RequestUtil.getLong(request, "areaid", -1L);
			Map<String, Object> areaMap = dataBaseService.getMap("select name from org_area_tb where id=? ", new Object[]{areaid});
			String name = "";
			if(areaMap != null && areaMap.get("name") != null){
				name = (String)areaMap.get("name");
			}
			AjaxUtil.ajaxOutput(response, name);
		}else if(action.equals("getberthseg")){
			Long comid = RequestUtil.getLong(request, "id", -1L);
			String sql = "select * from com_berthsecs_tb where is_active=? and comid=? ";
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(comid);
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAllMap(sql, params);
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(comid > 0 && !list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("berthsec_name")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getberth")){
			Long comid = RequestUtil.getLong(request, "id", -1L);
			String sql = "select * from com_park_tb c where berthsec_id = ? and  is_delete=?";
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			params.add(0);
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAllMap(sql, params);
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(comid > 0 && !list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("cid")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getinspects")){
			Long comid = RequestUtil.getLong(request, "id", -1L);
//			String sql = "select i.inspector_id,u.nickname from work_berthsec_tb b,work_inspector_tb i,user_info_tb u where b.berthsec_id = ? and  b.is_delete=? and " +
//					"i.inspect_group_id = b.inspect_group_id and i.state=? and u.state=? and u.id = i.inspector_id ";
			String sql = "select i.inspector_id,u.nickname from work_inspector_tb i,user_info_tb u where i.inspect_group_id in(\n" +
					"select inspect_group_id from work_berthsec_tb  where berthsec_id = ?" +
					" and is_delete=? and inspect_group_id>?) and i.state=? and u.state=? and u.id = i.inspector_id";

			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			params.add(0);
			params.add(0);
			params.add(0);
			params.add(0);
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAllMap(sql, params);
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(comid > 0 && !list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("inspector_id")+"\",\"value_name\":\""+map.get("nickname")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getinspectsbygroup")){
			Long comid = RequestUtil.getLong(request, "groupid", -1L);
//			 String sql = "select i.inspector_id,u.nickname from work_berthsec_tb b,work_inspector_tb i,user_info_tb u where b.berthsec_id  in(select id from com_berthsecs_tb where comid in(select id from com_info_tb where groupid = ?)) and  b.is_delete=? and " +
//					 "i.inspect_group_id = b.inspect_group_id and i.state=? and u.state=? and  u.id = i.inspector_id ";
			String sql = "select w.inspector_id,u.nickname from work_inspector_tb w,user_info_tb u where w.inspect_group_id in" +
					"(select w.inspect_group_id from com_berthsecs_tb c ,work_berthsec_tb w where c.comid in(select id from " +
					"com_info_tb where groupid =? ) and w.berthsec_id = c.id and w.inspect_group_id>-1) and u.id = w.inspector_id " ;
			//"i.state=? and u.state=?";
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
//			 params.add(0);
//			 params.add(0);
//			 params.add(0);
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAllMap(sql, params);
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(comid > 0 && !list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("inspector_id")+"\",\"value_name\":\""+map.get("nickname")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getdici")){
			Long comid = RequestUtil.getLong(request, "id", -1L);
			String sql = "select * from dici_tb where id not in (select dici_id from com_park_tb where comid=? ) and comid=? and is_delete=? ";
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			List<Object> params = new ArrayList<Object>();
			params.add(comid);
			params.add(comid);
			params.add(0);
			list = dataBaseService.getAllMap(sql, params);
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(comid > 0 && !list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("code")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getorgusers")){//查询组织类型下的人
			Long groupId = RequestUtil.getLong(request, "groupid", -1L);
			Long cityId = RequestUtil.getLong(request, "cityid", -1L);
			String sql = "select id,nickname from user_info_tb where state=? ";
			Object[] values = null;
			if(groupId!=-1&&cityId!=-1){
				sql +=" and  groupid=? and cityid=? ";
				values = new Object[]{0,groupId,cityId};
			}else if(groupId!=-1){
				sql +=" and groupid =? ";
				values = new Object[]{0,groupId};
			}else if(cityId!=-1){
				sql +=" and cityid=? ";
				values = new Object[]{0,cityId};
			}
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(values!=null){
				List<Map<String, Object>> list = dataBaseService.getAll(sql, values);
				if(list!=null&&!list.isEmpty()){
					for(Map<String, Object> map : list){
						String nickName =(String) map.get("nickname");
						nickName = nickName==null?(map.get("id")+""):nickName;
						result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+nickName+"\"}";
					}
				}
			}
			AjaxUtil.ajaxOutput(response, result+"]");
		}else if(action.equals("getcollectbygroupid")){
			Long groupid = RequestUtil.getLong(request, "groupid", -1L);
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			list = dataBaseService.getAll("select id,nickname from user_info_tb where groupid=? and state=? ", new Object[]{groupid,0});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(!list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("nickname")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}
		else if(action.equals("getcollectors")){
			Long comid = RequestUtil.getLong(request, "id", -1L);
//			Map<String, Object> groupMap = dataBaseService.getMap("select groupid from com_info_tb where id=? and groupid>? ",
//					new Object[]{comid, 0});
			List<Object> params = new ArrayList<Object>();
			String sql = "select * from user_info_tb where state<>? and (comid=? ";
			params.add(1);
			params.add(comid);
//			if(groupMap != null){
//				Long groupid = (Long)groupMap.get("groupid");
//				sql += " or groupid=?";
//				params.add(groupid);
//			}
			sql += " )";
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			logger.error(sql);
			logger.error(params);
			list = dataBaseService.getAllMap(sql, params);
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(!list.isEmpty()){
				for(Map map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("nickname")+"\"}";
				}
			}
			result += "]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getfreereasons")){
			Long comid = RequestUtil.getLong(request, "id", -1L);
			List<Map<String, Object>> tradsList = dataBaseService.getAll("select id,name from free_reasons_tb where comid=? ",
					new Object[]{comid});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map<String, Object> map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getparks")){
			Long groupid = RequestUtil.getLong(request, "id", -1L);
			List<Map<String, Object>> list = null;
			if(groupid > 0){
				list = dataBaseService.getAll("select id,company_name from com_info_tb where groupid=? and state!=? order by id ",
						new Object[]{groupid, 1});
			}
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(list != null && list.size() > 0){
				for(Map<String, Object> map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("company_name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}
		else if(action.equals("getauthidbyurl"))
		{
			String urlString=RequestUtil.getString(request, "url");
			List<Map<String, Object>> authList=(List<Map<String, Object>>)request.getSession().getAttribute("authlist");
			List<Map<String, Object>> menuList = new ArrayList<Map<String,Object>>();
			Long aid =-1L;
			if(authList!=null){
				for(Map<String, Object> aMap : authList){
					String url = (String)aMap.get("url");
					if(url.equals(urlString)){
						aid= (Long)aMap.get("auth_id");
					}
				}
			}
			AjaxUtil.ajaxOutput(response, aid+"");
		}else if(action.equals("getkeyvalues")){
			String name = RequestUtil.getString(request, "name");
			Long cityid = RequestUtil.getLong(request, "cityid", 1L);
			List<Map<String, Object>> list = null;
			list = dataBaseService.getAll("select key,value from dictionary_content_tb where cityid=? and did = " +
							"(select id from dictionary_type_tb where name=?) ",
					new Object[]{cityid,name});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(list != null && list.size() > 0){
				for(Map<String, Object> map : list){
					result+=",{\"value_no\":\""+map.get("key")+"\",\"value_name\":\""+map.get("value")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getCarType")){//获取车型列表
			Long comid = RequestUtil.getLong(request, "id", -1L);
			List<Map<String, Object>> retList = commonMethods.getCarType(comid);
			String result = StringUtils.getJson(retList);
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getcitymer")){//查询城市商户
			List<Map<String, Object>> list = null;
			list = dataBaseService.getAll("select id,cname from org_city_merchants where state=? ",	new Object[]{0});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(list != null && list.size() > 0){
				for(Map<String, Object> map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("cname")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getparkbygroupid")){//查询城市商户
			Long groupid = RequestUtil.getLong(request, "groupid", -1L);
			if(groupid==-1)
				groupid=RequestUtil.getLong(request, "id", -1L);
			List<Map<String, Object>> list = null;
			list = dataBaseService.getAll("select id,company_name from com_info_tb where groupid=? and state=?",	new Object[]{groupid,0});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(list != null && list.size() > 0){
				for(Map<String, Object> map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("company_name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getallcitygroups")){
			List<Map<String, Object>> list = null;
			list = dataBaseService.getAll("select g.id gid,g.name gname,c.id cid,c.name cname  from org_group_tb g " +
					" left join org_city_merchants c on g.cityid=c.id where c.state=? and g.state=? ",	new Object[]{0,0});
			String result ="";
			if(list!=null&&!list.isEmpty()){
				List<Map<String, Object>> resultMap = new ArrayList<Map<String,Object>>();
				for(Map<String, Object> map : list){
					Long cid = (Long)map.get("cid");
					boolean ishave = false;
					for(Map<String, Object>map2 : resultMap){
						Long cid1 = (Long) map2.get("cid");
						if(cid.equals(cid1)){
							List<Map<String, Object>> groupList =(List<Map<String, Object>>)map2.get("groups");
							Map<String, Object> group = new HashMap<String, Object>();
							group.put("gid", map.get("gid"));
							group.put("gname", map.get("gname"));
							groupList.add(group);
							ishave = true;
						}
					}
					if(!ishave){
						List<Map<String, Object>> groupList =new ArrayList<Map<String,Object>>();
						Map<String, Object> group = new HashMap<String, Object>();
						group.put("gid", map.get("gid"));
						group.put("gname", map.get("gname"));
						groupList.add(group);
						Map<String, Object> city = new HashMap<String, Object>();
						city.put("cid", map.get("cid"));
						city.put("cname", map.get("cname"));
						city.put("groups", groupList);
						resultMap.add(city);
					}
				}
				AjaxUtil.ajaxOutput(response,JsonUtil.createJson(resultMap));
			}
		}else if(action.equals("getgroupidbyparkid")){//根据车场编号查询集团编号
			Long parkid = RequestUtil.getLong(request, "parkid", -1L);
			String result ="{}";
			if(parkid>0){
				Map<String, Object> groupMap =  dataBaseService.getMap("select groupid,company_name from com_info_tb where id=? ",
						new Object[]{parkid});
				if(groupMap!=null){
					result = "[{\"groupid\":\""+groupMap.get("groupid")+"\",\"parkname\":\""+groupMap.get("company_name")+"\"}]";
				}
			}
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getberthsegbygroupid")){//根据车场编号查询集团编号
			Long groupid = RequestUtil.getLong(request, "groupid", -1L);
			List<Map<String, Object>> list = null;
			list = dataBaseService.getAll("select id,berthsec_name from com_berthsecs_tb where comid in(select id from com_info_tb where groupid = ?) and is_active=?", new Object[]{groupid,0});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(list != null && list.size() > 0){
				for(Map<String, Object> map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("berthsec_name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("gettasktype")){
			String tasktype = CustomDefind.TASKTYPE;
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tasktype!=null){
				String res[] = tasktype.split("\\|");
				for(int i=0;i<res.length;i++){
					result+=",{\"value_no\":\""+i+"\",\"value_name\":\""+res[i]+"\"}";
				}
				result+="]";

			}
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("getdetailtype")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			if(id>=0){
				String key = "TASKDETAIl"+id;
				String taskdetail = CustomDefind.getValue(key);
				String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
				if(taskdetail!=null){
					String res[] = taskdetail.split("\\|");
					for(int i=0;i<res.length;i++){
						result+=",{\"value_no\":\""+i+"\",\"value_name\":\""+res[i]+"\"}";
					}
					result+="]";

				}
				AjaxUtil.ajaxOutput(response, result);
			}
		}
		else if(action.equals("getdetailtypename")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			int value = RequestUtil.getInteger(request, "value", -1);
			if(id>=0){
				String key = "TASKDETAIl"+id;
				String taskdetail = CustomDefind.getValue(key);
				String result = "";
				if(taskdetail!=null){
					String res[] = taskdetail.split("\\|");
					result = res[value];

				}
				AjaxUtil.ajaxOutput(response, result);
			}
		}
		else if(action.equals("getcid")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			Map map = dataBaseService.getMap("select cid from com_park_tb where id = ?", new Object[]{id});
			String result = "未知";
			if(map!=null&&map.get("cid")!=null)
				result = map.get("cid")+"";
			AjaxUtil.ajaxOutput(response, result);
		}
		else if(action.equals("getparksbygroup")){//根据集团编号查询车场编号
			Long groupid = RequestUtil.getLong(request, "id", -1L);
			List<Map<String, Object>> list = null;
			list = dataBaseService.getAll("select id,company_name from com_info_tb where groupid = ? and state = ? ", new Object[]{groupid,0});
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(list != null && list.size() > 0){
				for(Map<String, Object> map : list){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("company_name")+"\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}
		else if(action.equals("getcompassbygroupid")){//根据集团编号查询车场编号
			String id = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "id"));
			String sql = "select c.id,c.passname,w.worksite_name from com_pass_tb c left join com_worksite_tb w on c.worksite_id=w.id ";
			List<Object> params  = new ArrayList<Object>();
			if(!id.equals("")){
				params.add(Long.valueOf(id));
				sql +=" where c.comid in(select id from com_info_tb where groupid = ?) ";
			}
			List<Map<String,Object>> tradsList = dataBaseService.getAllMap(sql,params);
			String result = "[{\"value_no\":\"-1\",\"value_name\":\"请选择\"}";
			if(tradsList!=null&&tradsList.size()>0){
				for(Map map : tradsList){
					result+=",{\"value_no\":\""+map.get("id")+"\",\"value_name\":\""+map.get("passname")+"("+map.get("worksite_name")+")\"}";
				}
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}else if(action.equals("nickname")){
			Long id = RequestUtil.getLong(request, "id", -1L);
			String sql = "select id,nickname from user_info_tb where id=? ";
			String nickname = "";
			if(id > 0){
				Map<String, Object> map = readService.getMap(sql, new Object[]{id});
				if(map != null && map.get("nickname") != null){
					nickname = (String)map.get("nickname");
				}
			}
			AjaxUtil.ajaxOutput(response, nickname);
		}
		else if(action.equals("getticketunit")){//根据减免劵类型获得减免劵单位
			Long ticketType = RequestUtil.getLong(request, "id", -1L);// 1-时长 2-金额
			String result = "[";
			if(ticketType == 1){
				result+="{\"value_no\":\"1\",\"value_name\":\"分钟\"}";
				result+=",{\"value_no\":\"2\",\"value_name\":\"小时\"}";
				result+=",{\"value_no\":\"3\",\"value_name\":\"天\"}";
			}
			if(ticketType == 2){
				result+="{\"value_no\":\"4\",\"value_name\":\"元\"}";
			}
			result+="]";
			AjaxUtil.ajaxOutput(response, result);
		}
		return null;
	}

	private List<Map<String, Object>> setTreeList(List<Map<String, Object>> allList,List<Map<String, Object>> lastList){
		List<Map<String, Object>> curList = new ArrayList<Map<String,Object>>();
		if(allList != null && !allList.isEmpty()){
			int i = 0;
			for(Map<String, Object> map : lastList){
				Integer treeid = (Integer)map.get("treeid");
				Long oid = (Long)map.get("id");
				for(Map<String, Object> map2 : allList){
					Long pid = (Long)map2.get("pid");
					if(oid.intValue() == pid.intValue()){
						i++;
						map2.put("sysid", treeid + "_" + (treeid *100 +i));
						map2.put("treeid", treeid *100 +i);
						curList.add(map2);
					}
				}
			}
		}
		return curList;
	}


	private List<Map<String, Object>> getSubAuth(List<Map<String, Object>> authList,Long authId){
		List<Map<String, Object>> ret = new ArrayList<Map<String,Object>>();
		for(Map<String, Object> aMap : authList){
			Long pid =(Long)aMap.get("pid");
			if(pid.equals(authId)){
				ret.add(aMap);
			}
		}
		return ret;
	}
}
