package com.zld.struts.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;

import pay.Constants;

import com.zld.AjaxUtil;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.utils.RequestUtil;
import com.zld.utils.TimeTools;
import com.zld.wxpublic.util.CommonUtil;

public class CarGameAction extends Action{
	
	@Autowired
	private DataBaseService service;
	@Autowired
	private PublicMethods publicMethods;
	
	private Logger logger = Logger.getLogger(CarGameAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		String target = null;
		if(action.equals("pregame")){//游戏准备
			//http://192.168.199.240/zld/cargame.do?action=pregame&id=38096&uin=21565
			target = preGame(request);
		}else if(action.equals("game")){//开始游戏
			target = game(request);
		}else if(action.equals("scroe")){//积分
			target= scroe(request);
		}else if(action.equals("playgame")){//车主客户端玩游戏入口
			//http://192.168.199.240/zld/cargame.do?action=playgame&mobile=18101333937
			target = carGame(request);
		}else if(action.equals("playagin")){
			//http://192.168.199.240/zld/cargame.do?action=playagin&uin=21565
			target = playAgin(request,null);
		}else if(action.equals("sort")){//我的排行
			target = sort(request);
			//http://192.168.199.240/zld/cargame.do?action=sort
		}else if(action.equals("gamesort")){//车神排行
			target = gameSort(request);
			//http://192.168.199.240/zld/cargame.do?action=sort
		}else if(action.equals("caibouns")){//游戏红包入口
			String ret = caiBouns(request,response);
			if(ret.equals("error"))
				return mapping.findForward(ret);
			return null;
		}else if(action.equals("gamebonus")){//游戏领取红包
			String ret = gameBouns(request);
			return mapping.findForward(ret);
		}else if(action.equals("getgameticket")){
			String ret = getGameTicket(request);
			return mapping.findForward(ret);
		}else if(action.equals("lifeticket")){//复活停车券
			Long tid = RequestUtil.getLong(request, "tid", -1L);
			int ret = 0;
			if(tid>-1){
				ret = service.update("update ticket_tb set state=? where id =? ", new Object[]{0,tid});
			}
			AjaxUtil.ajaxOutput(response, ""+ret);
		}
		return mapping.findForward(target);
	}
	private String gameSort(HttpServletRequest request) {
		Long uin = RequestUtil.getLong(request, "uin", -1L);
		Long btime =TimeTools.getToDayBeginTime();
		List<Map<String, Object>> list = service.getAll("select z.scroe,u.car_number,z.uin from zld_game_tb z " +
				"left join car_info_tb u on z.uin=u.uin where z.ctime >=? order by z.scroe desc,ctime desc limit ? ", new Object[]{btime,50});
		String data = "[";
		int sort=-1;
		int index =0;
		if(list!=null&&!list.isEmpty()){
			for(Map<String, Object> map: list){
				String carNumber = (String)map.get("car_number");
				if(carNumber!=null&&carNumber.length()>6){
					carNumber = carNumber.substring(0,2)+"***"+carNumber.substring(5);
				}else {
					carNumber = "车A***88";
				}
				data +="{\"own\":\""+carNumber+"\",\"score\":\""+map.get("scroe")+"\"},";
				Long guin=(Long)map.get("uin");
				if(guin.equals(uin)&&sort==-1)
					sort=index;
				index ++;
			}
			if(data.endsWith(","))
				data = data.substring(0,data.length()-1);
		}
		
		data+="]";
		request.setAttribute("data", data);
		request.setAttribute("sort", sort+1);
		return "gamesort";
	}
	private String getGameTicket(HttpServletRequest request) {
		String mobile= RequestUtil.processParams(request, "mobile");
		String openid= RequestUtil.processParams(request, "openid");
		Long otdid = RequestUtil.getLong(request, "otdid", -1L);
		Long uin = getUinByMobile(mobile);
		if(uin==null||uin==-1){//新用户，先注册
			Long ntime = System.currentTimeMillis()/1000;
			uin = service.getkey("seq_user_info_tb");
			String strid = "zlduser"+uin;
			//用户表
			String sql= "insert into user_info_tb (id,nickname,password,strid," +
					"reg_time,mobile,auth_flag,comid,media,wxp_openid) " +
					"values (?,?,?,?,?,?,?,?,?,?)";
			Object[] values= new Object[]{uin,"车主",strid,strid,ntime,mobile,4,0,1000,openid};
			int r = service.update(sql,values);
			if(r==1){
				int	eb = service.update("insert into user_profile_tb (uin,low_recharge,limit_money,auto_cash," +
						"create_time,update_time) values(?,?,?,?,?,?)", 
						new Object[]{uin,10,25,1,ntime,ntime});
				logger.error("收费员推荐车主，自动支付设置:"+eb);
			}
			
		}else if(openid!=null&&!"".equals(openid)){
			logger.error(">>保存openid:mobile="+mobile+",ret="+service.update("update user_info_Tb set wxp_openid=? where id =? ", new Object[]{openid,uin}));
			publicMethods.sharkbinduser(openid, uin, 0L);
		}
		//开始抢券了
		int ret = getticket(request, uin, otdid);
		if(ret==1)
			return "togame";
		else if(ret==2){
			request.setAttribute("message", "您已领过停车券");
			return "error";
		}else{
			request.setAttribute("message", "您来晚了，停车券抢完了");
			return "error";
		}
	}
	private String gameBouns(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		if(id==-1){
			request.setAttribute("message", "您来晚了，停车券抢完了");
			return "error";
		}
		String openid = getOpenid(request);
		Map user  = getMobileByOpenid(openid);
		logger.error(user);
		if(user==null){
			request.setAttribute("money", 3);
			request.setAttribute("otdid", id);
			request.setAttribute("openid", openid);
			return "addmobile";
		}else {//已经绑定过公众号，直接人领券
			Long uin = (Long)user.get("id");
			Long cLong = service.getLong("select count(*) from order_ticket_detail_tb where id=? and  uin = ? ", new Object[]{id,uin});
			if(cLong>0){
				request.setAttribute("message", "您来晚了，停车券抢完了");
				return "error";
			}
			int ret = getticket(request,uin,id);
			if(ret==1)
				return "togame";
			else if(ret==2){
				request.setAttribute("message", "您已领过停车券");
				return "error";
			}else{
				request.setAttribute("message", "您来晚了，停车券抢完了");
				return "error";
			}
		}
	}
	private int getticket(HttpServletRequest request,Long uin,Long id) {
		
		//验证是否已领过停车券
		Long count = service.getLong("select count(id) from order_ticket_detail_tb where uin=? and otid=" +
				"(select otid from order_ticket_detail_tb where id=?) ", new Object[]{uin,id});
		
		if(count>0){
			return 2;
		}
		Long ntime = System.currentTimeMillis()/1000;
		Map odmMap = service.getMap("select amount,btype from order_ticket_detail_tb where id =? and uin is null",new  Object[]{id});
		Integer money = 0;
		Integer type = 0;
		if(odmMap!=null){
			money=(Integer)odmMap.get("amount");
			type = (Integer) odmMap.get("btype");
			if(type==null)
				type=0;
			if(type==1)//微信打折券
				type=2;
		}else
			return 0;
		
		int ret = service.update("update order_ticket_detail_tb set uin = ?,ttime=? ,type=?  where id = ? and uin is null", 
				new Object[]{uin,ntime,0,id});
		logger.error(">>>>>>>>抢停车券，车主：uin:"+uin+",金额："+money+",结果 "+ret);
		if(ret == 1&&odmMap!=null){//红包领取成功,写入停车券表
			Long newid = service.getkey("seq_ticket_tb");
			String tsql = "insert into ticket_tb (id,create_time,limit_day,money,state,uin,type) values(?,?,?,?,?,?,?) ";
			ret = service.update(tsql, new Object[]{newid,ntime,ntime+6*24*60*60,money,0,uin,type});
			if(ret>0){
				request.setAttribute("ticketid", newid);
				request.setAttribute("money", money);
				request.setAttribute("uin", uin);
				
				//更新红包表状态 
				Map bMap = service.getMap("select * from order_ticket_tb where id =(select otid from order_ticket_detail_tb where id=?) ", new Object[]{id});
				Long btime = (Long)bMap.get("btime");
				Long otid = (Long) bMap.get("id");
				int result = 0;
				if(btime==null){
					result = service.update("update order_ticket_tb set btime=? where id=?  ", new Object[]{ntime,otid});
					logger.error(">>>开始领取红包...."+result);
				}
				Long acount = service.getLong("select count(id) from order_ticket_detail_tb where otid =? and uin is null", new Object[]{otid});
				if(acount==0){
					result = service.update("update order_ticket_tb set etime = ? where id =? ", new Object[]{ntime,otid});
					logger.error(">>>已领完红包...."+result);
				}
			}
			logger.error(">>>>老用户抢停车券，车主：uin:"+uin+",金额："+money+",写券给用户："+ret);
		}
		return ret;
	}

	private String caiBouns(HttpServletRequest request,
			HttpServletResponse response) {
		Long id  = RequestUtil.getLong(request, "id", -1L);
		if(id==-1){
			request.setAttribute("message", "您来晚了，停车券已抢完了");
			return "error";
		}
		Map otdMap = service.getMap("select etime,money from order_ticket_tb where id =? ", new Object[]{id});
		if(otdMap!=null){
			Long etim = (Long)otdMap.get("etime");
			if(etim!=null){
				request.setAttribute("message", "您来晚了，停车券已抢完了");
				return "error";
			}
			Integer money = (Integer)otdMap.get("money");
			if(money==null||money>0){
				request.setAttribute("message", "您来晚了，停车券已抢完了");
				return "error";
			}
		}else {
			request.setAttribute("message", "您来晚了，停车券已抢完了");
			return "error";
		}
		List<Map<String, Object>> list = service.getAll("select * from order_ticket_detail_tb where otid=? ", new Object[]{id});
		Long otdtid = null;
		if(list.isEmpty()){//拆出红包
			Map otMap = service.getMap("select * from order_ticket_tb where id =? ", new Object[]{id});
			if(otMap!=null){
				Integer number = (Integer)otMap.get("bnum");
				Integer money = (Integer)otMap.get("money");
				if(money<0)
					money = money*-1;
				String sql = "insert into order_ticket_detail_tb(otid,amount,btype) values(?,?,?)";
				List<Object[]> valueList  = new ArrayList<Object[]>();
				for(int i=0;i<number;i++){
					Object [] values = new Object[]{id,money,otMap.get("type")};
					valueList.add(values);
				}
				if(!valueList.isEmpty()){
					int ret = service.bathInsert(sql, valueList, new int[]{4,4,4});
					if(ret>0){
						otdtid = service.getLong("select max(id) from order_ticket_detail_tb where otid=? and uin is null", new Object[]{id});
					}
				}
			}
		}else {
			for(Map<String, Object> map : list){
				Long uin = (Long)map.get("uin");
				if(uin==null){
					otdtid = (Long)map.get("id");
					break;
				}
			}
		}
		if(otdtid!=null){
			try {
				String url=	"https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXPUBLIC_APPID+"&redirect_uri=http%3A%2F%2F"+Constants.WXPUBLIC_REDIRECTURL+"%2Fzld%2Fcargame.do%3Faction%3Dgamebonus%26id%3D"+otdtid+"&response_type=code&scope=snsapi_base&state=123#wechat_redirect";
				response.sendRedirect(url);
				return "";
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			request.setAttribute("message", "您来晚了，停车券已抢完了");
		}
		return "error";
	}

	private String carGame(HttpServletRequest request) {
		String mobile = RequestUtil.getString(request, "mobile");
		Map userMap = service.getMap("select id from user_info_tb where mobile=? and auth_flag=?", new Object[]{mobile,4});
		Long uin = null;
		if(userMap!=null)
			uin = (Long)userMap.get("id");
		return playAgin(request,uin);
	}

	private String sort(HttpServletRequest request) {
		Long uin = RequestUtil.getLong(request, "uin", -1L);
		Integer score = RequestUtil.getInteger(request, "score", -1);
		Long usercount = RequestUtil.getLong(request, "usercount", -1L);
		Long ticketId = RequestUtil.getLong(request, "ticketid", -1L);
		Integer sort = RequestUtil.getInteger(request, "sort", 100);
		Integer type = RequestUtil.getInteger(request, "type", 100);
		/*String isCangame = isCanGame(ticketId, uin);
		if(!isCangame.equals("1")){
			request.setAttribute("message", isCangame);
			return "error";
		}*/
		Long btime =TimeTools.getToDayBeginTime();
		List<Map<String, Object>> list = service.getAll("select z.scroe,u.car_number,z.uin from zld_game_tb z " +
				"left join car_info_tb u on z.uin=u.uin where z.ctime >=? order by z.scroe desc,ctime desc limit ? ", new Object[]{btime,10});
		String data = "[";
		if(sort>10){
			list.remove(9);
			Map<String,Object> map1= new HashMap<String, Object>();
			map1.put("scroe", score);
			map1.put("car_number", publicMethods.getCarNumber(uin));
			list.add(map1);
		}
		if(list!=null&&!list.isEmpty()){
			for(Map<String, Object> map: list){
				String carNumber = (String)map.get("car_number");
				if(carNumber!=null&&carNumber.length()>6){
					carNumber = carNumber.substring(0,2)+"***"+carNumber.substring(5);
				}else {
					carNumber = "车A***88";
				}
				data +="{\"own\":\""+carNumber+"\",\"score\":\""+map.get("scroe")+"\"},";
			}
			if(data.endsWith(","))
				data = data.substring(0,data.length()-1);
		}
		
		data+="]";
		int bnum = (score/sort);
		if(bnum<3)
			bnum=3;
		request.setAttribute("usercount", usercount);
		request.setAttribute("sort", sort);
		request.setAttribute("tnum", bnum);
		request.setAttribute("data", data);
		request.setAttribute("ticketid", ticketId);
		Long bounsid = null;
		Map bMap = service.getMap("select id from order_ticket_tb where uin =? and ctime>? ", 
				new Object[]{uin,System.currentTimeMillis()/1000-10});
		if(bMap!=null&&!bMap.isEmpty())
			bounsid = (Long)bMap.get("id");
		if(bounsid==null){
			if(score>=90)
				bounsid=writeBonus(uin, type,bnum,-2);
//			else {
//				bounsid=writeBonus(uin, type, 3,-1);
//			}
		}
		request.setAttribute("bonusid", bounsid);
		
	//微信公众号JSSDK授权验证
		Map<String, String> result = new HashMap<String, String>();
		try {
			result = publicMethods.getJssdkApiSign(request);
		}catch (Exception e) {
			e.printStackTrace();
			
		}
		//System.out.println(result);
		//jssdk权限验证参数
		request.setAttribute("appid", Constants.WXPUBLIC_APPID);
		request.setAttribute("nonceStr", result.get("nonceStr"));
		request.setAttribute("timestamp", result.get("timestamp"));
		request.setAttribute("signature", result.get("signature"));
		
		if(score>=90)
			return "mysort";
		else 
			return "fail";
	}

	private String playAgin(HttpServletRequest request,Long uin) {
		boolean isweixin =false;//是否微信进入，是：返回打折券,否：客户端 不需要
		if(uin==null){//微信进入
			isweixin =true;
			uin = RequestUtil.getLong(request, "uin", -1l);
		}
		Long count = service.getLong("select count(id) from zld_game_tb where uin =? and ctime>? ", new Object[]{uin,TimeTools.getToDayBeginTime()});
		if(count>2){
			request.setAttribute("message", "您今天已玩过3次");
			return "error";
		}
		request.setAttribute("uin", uin);
		Map carMap = service.getMap("select * from car_info_tb where uin =?", new Object[]{uin});
		if(isweixin&&(carMap==null||carMap.isEmpty())){
			carMap = service.getMap("select mobile from user_info_tb where id =? ", new Object[]{uin});
			request.setAttribute("mobile", carMap.get("mobile"));
			request.setAttribute("uin", uin);
			return "addcar";
		}else {
			String sql = "select id, money,limit_day,type from ticket_tb where uin=? " +
					"and state=? and type=? and money<?  and limit_day>? and resources=?  ";
			Object[] values = new Object[]{uin,0,0,3,TimeTools.getToDayBeginTime(),0,8};
			if(!isweixin){
				sql +=" and type< ? ";
				values =	new Object[]{uin,0,0,3,TimeTools.getToDayBeginTime(),0,2,8};	
			}
			List<Map<String,Object>> ticketList = service.getAll(sql+" order by money,limit_day limit ?",values);
			if(ticketList!=null&&!ticketList.isEmpty()){
				String tickets = "[";
				for(Map<String,Object> map : ticketList){
					Long lday = (Long)map.get("limit_day");
					Long t = lday-TimeTools.getToDayBeginTime();
					Long d = t/(24*60*60) +1;
					tickets +="{\"id\":\""+map.get("id")+"\",\"money\":\""+map.get("money")+"\",\"lday\":\""+d+"\",\"type\":\""+map.get("type")+"\"},";
				}
				if(tickets.equals(","))
					tickets = tickets.substring(0,tickets.length()-1);
				tickets +="]";
				request.setAttribute("tickets", tickets);
			}
			return "tickets";
		}
	}
	/*
	 * 统计积分 
	 */
	private String scroe(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		Long uin = RequestUtil.getLong(request, "uin", -1L);
		Integer scroe = RequestUtil.getInteger(request, "scroe", 0);
		logger.error(">>>scrod ticketid:"+id+",uin:"+uin+",scroe:"+scroe);
		request.setAttribute("uin", uin);
		String ret = "success";
		int type=-1;
		String canGame = isCanGame(id, uin);
		request.setAttribute("score", scroe);
		if(canGame.equals("1")){
			Integer isWin  = 0;
			if(scroe>=90){
				isWin=1;
				type=writeTicket(id,uin);
//				Long bid = writeBonus(uin,type);
//				request.setAttribute("bonusid", bid);
			}else {
				Map<String,Object> ticketMap = service.getMap("select * from ticket_tb where id =?  ", new Object[]{id}) ;
				if(ticketMap!=null){
					type = (Integer)ticketMap.get("type");
				}
				ret = "gamefail";
				service.update("update ticket_tb set state=? where id =? ", new Object[]{1,id});
			}
			Long ntime = System.currentTimeMillis()/1000;
			int result  = service.update("insert into zld_game_tb (uin,scroe,ctime,iswin,type,tid)" +
					"values(?,?,?,?,?,?)", new Object[]{uin,scroe,ntime,isWin,0,id});
			logger.error(">>>game 写表...ret:"+result);
		}else {
			ret="error";
			request.setAttribute("message", canGame);
		}
		request.setAttribute("ctype", type);//0普通停车券，1专用停车券，2微信打折券
		/*if(scroe>=80){//需要分享
			//微信公众号JSSDK授权验证
			Map<String, String> result = new HashMap<String, String>();
			try {
				result = publicMethods.getJssdkApiSign(request);
			}catch (Exception e) {
				e.printStackTrace();
				
			}
			//System.out.println(result);
			//jssdk权限验证参数
			request.setAttribute("appid", Constants.WXPUBLIC_APPID);
			request.setAttribute("nonceStr", result.get("nonceStr"));
			request.setAttribute("timestamp", result.get("timestamp"));
			request.setAttribute("signature", result.get("signature"));
		}*/
		//车主数量，排名 
		Long usercount = service.getLong("select count(id) from user_info_tb where auth_flag=? and state= ? ", new Object[]{4,0});
		Long sort = service.getLong("select count(id) from zld_game_tb where ctime> ? and scroe>?  ", new Object[]{TimeTools.getToDayBeginTime(),scroe});
		request.setAttribute("usercount",usercount);
		request.setAttribute("sort",sort+1);
		request.setAttribute("ticketid",id);
		return ret;
	}

	private String game(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		Long uin = RequestUtil.getLong(request, "uin", -1L);
		logger.error(">>>game ticketid:"+id+",uin:"+uin);
		request.setAttribute("id", id);
		request.setAttribute("uin", uin);
		String canGame = isCanGame(id, uin);
		if(id>0&&uin>0){
			if(canGame.equals("1"))
				return "game";
			else {
				request.setAttribute("message", canGame);
			}
		}
		return "error";
	}


	private String preGame(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		Long uin = RequestUtil.getLong(request, "uin", -1L);
		logger.error(">>>pregame ticketid:"+id+",uin:"+uin);
		request.setAttribute("id", id);
		request.setAttribute("uin", uin);
		if(id>0&&uin>0){
			String canGame = isCanGame(id, uin);
			if(canGame.equals("1"))
				return "pregame";
			else {
				request.setAttribute("message", canGame);
			}
		}
		return "error";
	}
	
	/*
	 * 验证停车券的编号
	 */
	private String isCanGame(Long ticketId,Long uin){
		Long count = service.getLong("select count(id) from zld_game_tb where uin =? and ctime>? ", new Object[]{uin,TimeTools.getToDayBeginTime()});
		if(count>2){
			return "您今天已玩过3次";
		}
		count = service.getLong("select count(id) from zld_game_tb where tid=? and ctime >? ", new Object[]{ticketId,TimeTools.getToDayBeginTime()});
		if(count>0)
			return "停车券已玩过";
		Map tMap = service.getMap("select money,type from ticket_tb where id =? and uin =? and state=? ", new Object[]{ticketId,uin,0}) ;
		
		if(tMap==null||tMap.isEmpty())
			return "停车券已使用";
		else {
			Integer money = (Integer)tMap.get("money");
			Integer type = (Integer)tMap.get("type");
			if(money>6)
				return "停车券已超过翻倍限额";
			if(type==null||type!=0)
				return "停车券类型错误！";
		}
		return "1";  
	}
	/*
	 *奖励
	 */
	private Integer writeTicket(Long cid,Long uin){
		Map<String,Object> ticketMap = service.getMap("select * from ticket_tb where id =?  ", new Object[]{cid}) ;
		Integer type = -1;
		if(ticketMap!=null){
			type = (Integer)ticketMap.get("type");
			Integer money = (Integer)ticketMap.get("money");
			Long comid = (Long)ticketMap.get("comid");
			Long _uin = (Long)ticketMap.get("uin");
			if(uin.intValue()!=_uin.intValue()||type==null)
				return type;
			if(comid==null)
				comid=-1L;
			if(type==0||type==1){//普通停车券
				money = money*2;
				if(money>4)
					money=4;
				int ret = service.update("update ticket_tb set money=? where id=? ", new Object[]{money,cid});
				logger.error(">>>>game 写入停车券记录：uin:"+uin+",ret:"+ret);
			}else if(type==2){//微信打折券
				String sql = "insert into ticket_tb(create_time,limit_day,state,uin,comid,money,type) values(?,?,?,?,?,?,?)";
				Long btime = TimeTools.getToDayBeginTime();
				Long etime = btime+ 6*24*60*60-1;
				int ret = 0;
			/*	List<Object[]> values = new ArrayList<Object[]>();
				values.add(new Object[]{btime,etime,0,uin,comid,money,type});
				if(!values.isEmpty())
					ret = service.bathInsert(sql, values, new int[]{4,4,4,4,4,4,4});
				*/
				ret = service.update(sql, new Object[]{btime,etime,0,uin,comid,money,type});
				logger.error(">>>>game 写入微信打折券记录：uin:"+uin+",time:"+btime+",ret:"+ret);
			}
		}
		return type;
	}
	/*
	 * 写入红包,普通券，每张三元，打折券，每张三折
	 * type=0或1 为普通券，2为打折券
	 */
	private Long writeBonus(Long uin,Integer type,Integer number,Integer money){
		Long key = service.getkey("seq_order_ticket_tb");
		Long ntime = System.currentTimeMillis()/1000;
		String sql = "insert into order_ticket_tb(id,uin,order_id,money,bnum,ctime,exptime,bwords,type) values(?,?,?,?,?,?,?,?,?)";
		Object []values = null;
		if(type==1||type==0){
			values = new Object[]{key,uin,-1,money,number,ntime,ntime+24*60*60,"抢到券可游戏翻倍，赢了还能继续发!",0};
		}else {
			values = new Object[]{key,uin,-1,-3,number,ntime,ntime+24*60*60,"微信支付打折券",1};
		}
		int ret =service.update(sql, values);
		if(ret==1)
			return key;
		return -1L;
	}
	
	/**
	 * 调用微信接口，取用户的openid
	 * @param request
	 * @return
	 */
	
	private String getOpenid(HttpServletRequest request){
		String code = RequestUtil.processParams(request, "code");
		logger.error(">>>>>>>>code:"+code+",comfig appid:");
		if(code==null||"".equals(code))
			return null;
		String appid = Constants.WXPUBLIC_APPID;
		String secret=Constants.WXPUBLIC_SECRET;
		String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appid+"&secret="+secret+"&code="+code+"&grant_type=authorization_code";
		logger.error(">>>>>>>>access_token_url:"+access_token_url);
		String result = CommonUtil.httpsRequest(access_token_url, "GET", null);
		JSONObject map = JSONObject.fromObject(result);
		String openid = (String)map.get("openid");
		logger.error(">>>>>>>>return map :"+map);
		if(openid==null)
			openid="";
		return openid;
	}
	/**
	 * 根据openid 查用户手机
	 * @param openid
	 * @return
	 */
	private Map getMobileByOpenid(String openid){
		Map<String, Object> userMap= null;
		if(openid!=null&&!openid.equals("")){
			userMap = service.getMap("select id, mobile from user_info_tb where state=? and auth_flag=? and wxp_openid=? ",
							new Object[] { 0, 4, openid });
		}
		return userMap;
	}
	
	public Map<String, Object> getUserByMobile(String mobile){
		if(!"".equals(mobile)){
			Map userMap = service.getPojo("select * from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});
			if(userMap!=null){
				return userMap;
			}
		}
		return null;
	}
	
	public Long getUinByMobile(String mobile){
		if(!"".equals(mobile)){
			Map userMap = service.getPojo("select id from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});
			if(userMap!=null){
				return (Long) userMap.get("id");
			}
		}
		return -1L;
	}
	
}
