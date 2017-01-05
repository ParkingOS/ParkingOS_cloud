package com.zld.struts.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import pay.Constants;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zld.AjaxUtil;
import com.zld.CustomDefind;
import com.zld.easemob.apidemo.EasemobIMUsers;
import com.zld.easemob.main.HXHandle;
import com.zld.impl.MemcacheUtils;
import com.zld.impl.PublicMethods;
import com.zld.service.DataBaseService;
import com.zld.service.LogService;
import com.zld.utils.Base64forjs;
import com.zld.utils.Check;
import com.zld.utils.HttpProxy;
import com.zld.utils.JsonUtil;
import com.zld.utils.RequestUtil;
import com.zld.utils.ResultMap;
import com.zld.utils.StringUtils;
import com.zld.utils.TimeTools;
import com.zld.utils.ZldMap;
import com.zld.wxpublic.util.CommonUtil;

/*****
 * 打灰机游戏
 * @author Laoyao
 * 20150803
 * 统计人数：select count(*) from user_info_tb where id in(select uin from ticket_tb where id in(select tid from flygame_pool_tb where tid is not null))
 */
public class FlyGameAction extends Action{
	
	@Autowired
	private DataBaseService daService;
	@Autowired
	private PublicMethods publicMethods;
	@Autowired
	private MemcacheUtils memcacheUtils;
	@Autowired
	private LogService logService;
	Logger logger = Logger.getLogger(getClass());
	
	private Integer times = 9;
	private boolean isonline=true;
//	private Integer times = 1000;
//	private boolean isonline=false;

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String action = RequestUtil.getString(request, "action");
		Long uin = RequestUtil.getLong(request, "uin", -1L);
		//微信公众号文章游戏入口
		if(action.equals("togame")){
			String info[]=getOpenid(request);
			if(info!=null&&info.length==2){
				String openid=info[0];
				String acc_token = info[1];
				Map<String, Object> userMap= getUserByOpenid(openid);
				if(userMap!=null&&!userMap.isEmpty()){
					uin= (Long)userMap.get("id");
					action="pregame";
				}else {
					request.setAttribute("openid", openid);
					request.setAttribute("acc_token", acc_token);
					request.setAttribute("action", "getticket");
					request.setAttribute("topwords", "需要输入手机号才能去打灰机");
					request.setAttribute("mbgimg", "reamrk_bg");
					return mapping.findForward("addmobile");
				}
			}else {
				request.setAttribute("pic", "error_net.png");
				request.setAttribute("type", "0");
				return mapping.findForward("error");
			}
		}else if(action.equals("getticket")){//新用户打灰机，注册并发一张2元券打灰机
			String mobile = RequestUtil.getString(request, "mobile");
			String openid = RequestUtil.getString(request, "openid");
			String acc_token = RequestUtil.getString(request, "acc_token");
			if(Check.checkMobile(mobile)){
				String wxname ="";
				String wxurl ="";
				String url = "https://api.weixin.qq.com/sns/userinfo?access_token="+acc_token+"&openid="+openid+"&lang=zh_CN";
				String result = CommonUtil.httpsRequest(url, "GET", null);
				net.sf.json.JSONObject retmap =null;
				if(result!=null){
					retmap = net.sf.json.JSONObject.fromObject(result);
				}
				logger.error(">>>>>>>>return wxuserinfo map :"+retmap);
				if(retmap != null && retmap.get("nickname") != null){
					wxname = retmap.getString("nickname");
					if(wxname!=null)
						wxname = wxname.replace("'", "").replace("\"", "");
					wxurl = retmap.getString("headimgurl");
				}
				
				Map<String, Object> userMap=daService.getMap("select id from user_info_tb where mobile=? and auth_flag=? ",
						new Object[] { mobile,4 });
				if(userMap!=null&&!userMap.isEmpty()){
					uin = (Long)userMap.get("id");
					//记录下openid
					int ret = daService.update("update user_info_tb set wxp_openid=?,wx_name=?,wx_imgurl=? where id=? ", new Object[]{openid,wxname,wxurl,uin});
					logger.error(mobile+"打飞机用户,手机号已注册，更新openid:"+openid+",uin="+uin+"，ret:"+ret);
					action="pregame";
				}
				if(uin==null||uin==-1){
					uin = publicMethods.regUser(mobile,1000L,-1L,true);
					if(uin!=null&&uin>0){
						int ret = daService.update("update user_info_tb set wxp_openid=?,wx_name=?,wx_imgurl=? where id=? ", new Object[]{openid,wxname,wxurl,uin});
						Long ntime = System.currentTimeMillis()/1000;
						ret = daService.update("insert into ticket_tb (create_time,limit_day,money,state,uin) values(?,?,?,?,?) ",
								new Object[]{ntime,ntime+3*24*60*60-1,2,0,uin});
						logger.error("打飞机注册新用户，送一张两元停车券，ret:"+ret);
						if(ret==1)
							action="pregame";
					}
				}
				
			}
		}
		if(uin==-1)
			uin = RequestUtil.getLong(request, "fuin", -1L);
		String mobile= RequestUtil.getString(request, "mobile");
		if(uin==-1&&!mobile.equals("")){
			uin = getUinByMobile(mobile);
		}
		if(uin==null||uin==-1){
			if(action.indexOf("bonus")==-1){
				request.setAttribute("pic", "error_net.png");
				request.setAttribute("type", "0");
				return mapping.findForward("error");
			}
		}
		String target = null;
		logger.error("uin:"+uin+",tid:"+request.getParameter("tid")+",action:"+action);
		if(action.equals("pregame")){//准备游戏
			request.setAttribute("uin", uin);
			request.setAttribute("touin", request.getParameter("touin"));
			request.setAttribute("agin", request.getParameter("agin"));
			Map userMap = daService.getMap("select wxp_openid from user_info_tb where id =? ", new Object[]{uin});
			request.setAttribute("openid", userMap.get("wxp_openid"));
			Long count = daService.getLong("select count(id) from user_liuyan_tb where  tuin=?  and is_read=? ", new Object[]{uin,0});
			request.setAttribute("mesgcount", count);
			target = "pregame";
		}else if(action.equals("getdata")){
			String data  = getData(request,uin);
			AjaxUtil.ajaxOutput(response, data);
			//logger.error(uin+",data:"+data);
			return null;
		}else if(action.equals("friend")){//我的好友
			String friends = friends(request,uin);
			AjaxUtil.ajaxOutput(response, friends);
			return null;
		}else if(action.equals("addfriend")){//打中车主灰机，加为好机友
			Long  touin= RequestUtil.getLong(request, "touin", -1L);
			Long sid = RequestUtil.getLong(request, "sid", -1L);
			int r =0;
			if(uin>0){
				r = addFriend(uin, touin,sid);
			}
			AjaxUtil.ajaxOutput(response, r+"");
			return null;
		}else if(action.equals("message")){//查看所有消息
			String result =getMessage(request);
			AjaxUtil.ajaxOutput(response, result);
			return null;
		}else if(action.equals("sendmessge")){//发送消息给好友
			Long fuin = RequestUtil.getLong(request, "fuin", -1L);
			Long tuin = RequestUtil.getLong(request, "tuin", -1L);
			String message = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "message"));
			int ret = daService.update("insert into user_liuyan_tb(fuin,tuin,message,ctime,is_read) values (?,?,?,?,?)", 
					new Object[]{fuin,tuin,message,System.currentTimeMillis()/1000,0});
			if(ret==1){
				String result =getMessage(request);
				AjaxUtil.ajaxOutput(response, result);
				String carNumber = publicMethods.getCarNumber(uin);
				if(carNumber!=null&&carNumber.length()==7)
					carNumber =carNumber.substring(0,4)+"**"+carNumber.substring(6);
				Map userMap = daService.getMap("select wx_name from user_info_tb where id =? ", new Object[]{uin});
				String wxName = "停车宝车主";
				if(userMap!=null){
					wxName = (String)userMap.get("wx_name");
					if(wxName!=null)
						wxName=wxName.replace("'", "").replace("\"", "");
				}
				sendLeaveWeiXinMesg(wxName,fuin, tuin, "车主"+carNumber+"给您留言了", message);
			}
			return null;
		}else if(action.equals("usetickets")){//选择停车券
			String tickets=getTickets(request);
			AjaxUtil.ajaxOutput(response, tickets);
			return null;
		}else if(action.equals("score")){//查看我的成绩
			String scores = score(request,uin);
			//logger.error(uin+",scores:"+scores);
			AjaxUtil.ajaxOutput(response, scores);
			return null;
		}else if(action.equals("play")){//开始游戏
			Long count = daService.getLong("select count(id) from flygame_pool_tb where tid in (select id from ticket_tb where uin =?) and ctime>=?", new Object[]{uin,TimeTools.getToDayBeginTime()});
			//logger.error(count);
			if(count>times){
				request.setAttribute("pic", "error_times.png");
				request.setAttribute("type", "0");
				return mapping.findForward("error");
			}
			int ret = play(request,uin);
			Integer width= RequestUtil.getInteger(request, "s_width", 0);
			//logger.error(width);
			if(ret==-1)
				return mapping.findForward("error");
			if(width==0)
				return mapping.findForward("game");
			else {
				return mapping.findForward("wgame");
			}
		}else if(action.equals("getscore")){//游戏结果
			String result = getScore(request,uin);
			logger.error(uin+",result:"+result);
			if(result!=null)
				AjaxUtil.ajaxOutput(response, "["+result+"]");
			return null;
		}else if(action.equals("toshare")){
			toShare(request,uin);
			target="toshare";
		}else if(action.equals("recordbets")){//记录现有子弹数
			//Long tid = RequestUtil.getLong(request, "tid", -1L);
//			Integer bets = RequestUtil.getInteger(request, "bnums", 0);
			//cacheBets(uin, 0,0, tid);
			AjaxUtil.ajaxOutput(response, "1");
			return null;
		}else if(action.equals("recordhits")){//记录广告链接打开次数
			Long gid = RequestUtil.getLong(request, "gid", -1L);
			if(gid!=-1){
				int ret = daService.update("update advert_tb set hits=hits+? where id =? ", new Object[]{1,gid});
				logger.error(">>>>uin+"+uin+",hits,gid:"+gid+",ret:"+ret);
			}
			AjaxUtil.ajaxOutput(response, "1");
		}else if(action.equals("getbid")){//客户端分享，取红包编号
			Long sid = RequestUtil.getLong(request, "sid", -1L);//打灰机成绩编号
			String carid="";
			Map carMap = daService.getMap("select car_number from car_info_Tb " +
					" where uin=? ", new Object[]{uin});
			if(carMap!=null&&carMap.get("car_number")!=null&&!carMap.get("car_number").toString().equals(""))
				carid = (String)carMap.get("car_number");
			if(carid!=null&&carid.length()==7)
				carid = carid.substring(0,4)+"**"+carid.substring(6);
			Long bid = backBonus(uin,sid,CustomDefind.getValue("FLYGAMGE_BONUS_CONTENT"));//分享红包
			request.setAttribute("bonusid", bid);
			request.setAttribute("carnumber", carid);
			AjaxUtil.ajaxOutput(response, "{\"carid\":\""+carid+"\",\"bid\":\""+bid+"\",\"words\":\""+CustomDefind.getValue("FLYGAMGE_BONUS_CONTENT")+"\"}");
			return null;
		}else if(action.equals("tidscore")){//本轮积分
			Long tid = RequestUtil.getLong(request, "tid", -1L);
			String result = tidScore(tid);
			AjaxUtil.ajaxOutput(response, result);
			return null;
		}else if(action.equals("viewscore")){
			String dtype = RequestUtil.getString(request,"dtype");//数据类型，all所有车友，friend我的机友
			request.setAttribute("uin",uin);
			if(!dtype.equals("")){
				String ret = getScoreData(dtype,uin);
				AjaxUtil.ajaxOutput(response,ret);
				return null;
			}else{
				Long count = daService.getLong("select count(id) from order_ticket_tb where ctime >? and uin=? and type=? ",
						new Object[]{TimeTools.getToDayBeginTime(),uin,6});
				request.setAttribute("ishare", count);
				target="score";
			}
			logger.error(uin);
		}else if(action.equals("scoredetail")){
			request.setAttribute("uin",uin);
			request.setAttribute("itsort", request.getParameter("itsort"));
			Integer type = RequestUtil.getInteger(request, "type", -1);
			String data = scoreDetail(request,uin);
			if(type>0){//查看本轮战绩
				AjaxUtil.ajaxOutput(response, "["+data+"]");
				return null;
			}
			target="scoredetail";
		}else if(action.equals("tosharescore")){//分享战绩
			request.setAttribute("uin",uin);
			request.setAttribute("dtype",request.getParameter("dtype"));
			otShareScore(request,uin);
			target="sharescore";
		}else if(action.equals("viewbonus")){
			request.setAttribute("uin",uin);
			viewBonus(request);
			target="viewshare";
		}else if(action.equals("getbonus")){//取战绩分享红包
			Long bid = RequestUtil.getLong(request, "bid", -1L);
			//分享车主的微信头像
			Map<String,Object> tempMap = daService.getMap("select u.wx_imgurl from user_info_tb u where u.id = ?",new Object[]{uin});
			String wxImgUrl= (String)tempMap.get("wx_imgurl");
			if(wxImgUrl==null||"".equals(wxImgUrl)||"null".equals(wxImgUrl))
				wxImgUrl="images/flygame/logo.png";
			request.setAttribute("carowenurl", wxImgUrl);
			
			if(!mobile.equals("")){//用户输入完手机号后注册用户
				String acc_token = RequestUtil.getString(request, "acc_token");
				String openid = RequestUtil.getString(request, "openid");
				Map userMap = daService.getMap("select * from user_info_tb where mobile=? and auth_flag=?",new Object[]{mobile,4});
				Long newuin=-1L;
				String wximgurl =null;
				if(userMap!=null){
					newuin =(Long)userMap.get("id");
					wximgurl =(String)userMap.get("wx_imgurl");
				}else {
					newuin=publicMethods.regUser(mobile, 1000L, -1L, true);
				}
				if(userMap==null||userMap.get("wx_name")==null||wximgurl==null || wximgurl.length()<1 ){
					String url = "https://api.weixin.qq.com/sns/userinfo?access_token="+acc_token+"&openid="+openid+"&lang=zh_CN";
					String result = CommonUtil.httpsRequest(url, "GET", null);
					net.sf.json.JSONObject retmap =null;
					if(result!=null){
						retmap = net.sf.json.JSONObject.fromObject(result);
					}
					logger.error(">>>>>>>>return wxuserinfo map :"+retmap);
					if(retmap != null && retmap.get("nickname") != null){
						String wxname = retmap.getString("nickname");
						if(wxname!=null)
							wxname = wxname.replace("'", "").replace("\"", "");
						String wxurl = retmap.getString("headimgurl");
						if(wxname!=null){
							//保存到数据库
							int rets = daService.update("update user_info_tb set wx_imgurl=? ,wx_name=?,wxp_openid=? where id = ? ", new Object[]{wxurl,wxname,openid,newuin});
							logger.error(">>>uin save wxname("+wxname+") and wxurl("+wxurl+"):"+rets);
						}
					}
				}
				getBonus(request,bid,newuin,1);
				target="bonusret";
			}else {
				String auth_range = RequestUtil.getString(request, "authrange");
				String info[]=getOpenid(request);
				if(info!=null&&info.length==2){
					String openid=info[0];
					String acc_token = info[1];
					Map<String, Object> userMap= getUserByOpenid(openid);
					if(userMap!=null&&!userMap.isEmpty()){
						String wximgurl =null;
						wximgurl =(String)userMap.get("wx_imgurl");
						//非授权微信
						if(auth_range.equals("")){
							//没有微信头像及微信名称，需要用户授权
							if(userMap==null||userMap.get("wx_name")==null||wximgurl==null || wximgurl.length()<1 ){
								//用户授权
								String authurl =   "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXPUBLIC_APPID+"&redirect_uri=http%3A%2F%2F"+Constants.WXPUBLIC_REDIRECTURL+"%2Fzld%2Fflygame.do%3Faction%3Dgetbonus%26uin%3D"+uin+"%26bid%3D"+bid+
										"%26authrange%3Dtingchebao&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
								response.sendRedirect(authurl);
								return null;
							}
						}
						getBonus(request,bid,(Long)userMap.get("id"),0);
						target="bonusret";
					}else {
						if(auth_range.equals("")){
							String authurl =   "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+Constants.WXPUBLIC_APPID+"&redirect_uri=http%3A%2F%2F"+Constants.WXPUBLIC_REDIRECTURL+"%2Fzld%2Fflygame.do%3Faction%3Dgetbonus%26uin%3D"+uin+"%26bid%3D"+bid+
									"%26authrange%3Dtingchebao&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
							System.err.println(authurl);
							response.sendRedirect(authurl);
							return null;
						}
						request.setAttribute("openid", openid);
						request.setAttribute("acc_token", acc_token);
						request.setAttribute("bid", bid);
						request.setAttribute("action", "getbonus");
						request.setAttribute("mbgimg", "reamrk_score_bg");
						request.setAttribute("topwords", "送你4张停车券，可抵扣36发打灰机子弹");
						target="addmobile";
					}
				}else {
					request.setAttribute("pic", "error_net.png");
					request.setAttribute("type", "0");
					target="error";
				}
			}
		}
		return mapping.findForward(target);
	}

	private String tidScore(Long tid) {
		Long ntime =TimeTools.getToDayBeginTime();
		String result = "[{\"score\":\"0\",\"sort\":\"0\"}]";
		if(tid>0){
			String sql ="select sum(db_bullet_score+empty_bullet_score+gift_score+balance_score+ticket_score+cloud_score+crow_score+bets_double_score+bets_halve_score+score_double_score+" +
							"score_halve_score+catapult_full_score+catapult_halve_score+float_score) score,tid " +
					" from flygame_score_anlysis_tb  where ctime > ? group by tid order by score desc";
			Object[] params =new Object[]{ntime};
			List all = daService.getAll(sql, params);
			if(all!=null&&!all.isEmpty()){
				for(int i=0;i<all.size();i++){
					Map map = (Map)all.get(i);
					Long id = (Long)map.get("tid");
					if(tid.equals(id)){
						result="[{\"score\":\""+map.get("score")+"\",\"sort\":\""+(i+1)+"\"}]";
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * 领取战绩分享红包
	 * @param request
	 * @param bid 红包编号
	 * @param uin 车主账户
	 * @param type 0老用户1新用户
	 */
	private void getBonus(HttpServletRequest request, Long bid, Long uin,int type) {
		request.setAttribute("isnew", type);
		Long ntime = System.currentTimeMillis()/1000;
		Long count = daService.getLong("select count(id) from order_ticket_detail_tb where otid=? and uin=?", new Object[]{bid,uin});
		request.setAttribute("ticket_bg", "ticket.png");
		request.setAttribute("uin", uin);
		if(count==0){//没有领过
			Map bMap = daService.getMap("select * from order_ticket_tb where id =? and type=?", new Object[]{bid,6});
			if(bMap!=null&&!bMap.isEmpty()){
				Long exptime = (Long)bMap.get("exptime");
				if(exptime<ntime){//已过期
					request.setAttribute("isover", 1);
				}else {
					Long ttime = TimeTools.getToDayBeginTime();
					Long ticketId = daService.getkey("seq_ticket_tb");
					String tsql = "insert into ticket_tb (id,create_time,limit_day,money,state,uin,type) values(?,?,?,?,?,?,?) ";
					int ret = daService.update(tsql, new Object[]{ticketId,ttime,ttime+3*24*60*60,2,0,uin,type});
					logger.error(uin+","+bid+",领取战绩分享红包,写停车券表，ret:"+ret);
					ret = daService.update("insert into order_ticket_detail_tb(otid,uin,amount,ttime,type,btype,ticketid) " +
							" values (?,?,?,?,?,?,?)", new Object[]{bid,uin,2,ntime,type,6,ticketId});
					logger.error(uin+","+bid+","+ticketId+",领取战绩分享红包,记入红包表,ret:"+ret);
					request.setAttribute("ishave", ret);
					request.setAttribute("topwords", "停车券可以兑换子弹打灰机");
					if(type==1){//新用户
						String sql = "insert into ticket_tb (create_time,limit_day,money,state,uin) values(?,?,?,?,?) ";
						List<Object[]> insertvalues = new ArrayList<Object[]>();
						Object[] v1 = new Object[]{ntime,ntime+3*24*60*60-1,1,0,uin};
						Object[] v2 = new Object[]{ntime,ntime+3*24*60*60-1,2,0,uin};
						Object[] v3 = new Object[]{ntime,ntime+3*24*60*60-1,3,0,uin};
						Object[] v4 = new Object[]{ntime,ntime+3*24*60*60-1,4,0,uin};
						insertvalues.add(v1);insertvalues.add(v2);insertvalues.add(v3);insertvalues.add(v4);
						int result= daService.bathInsert(sql, insertvalues, new int[]{4,4,4,4,4});
						if(result>0){
							logService.insertUserMesg(1, uin, "恭喜您获得四张停车券!", "红包提醒");
						}
						request.setAttribute("topwords", "送你4张停车券，可抵扣35发打灰机子弹");
					}
					request.setAttribute("isover", 0);
				}
			}else {
				request.setAttribute("isover", 1);
			}
			
		}else {
			request.setAttribute("topwords", "停车券可以兑换子弹打灰机");
			if(type==1)
				request.setAttribute("topwords", "送你4张停车券，可抵扣35发打灰机子弹");
			request.setAttribute("ticket_bg", "haveget.png");
			request.setAttribute("isover", 1);
		}
	}
	
	private void viewBonus(HttpServletRequest request) {
		Long id = RequestUtil.getLong(request, "id", -1L);
		if(id!=-1){
			Map bmMap= daService.getMap("select * from order_ticket_tb where id = ? ", new Object[]{id});
			if(bmMap!=null&&!bmMap.isEmpty()){
				Long uin =(Long)bmMap.get("uin");
				Map<String,Object> tempMap = daService.getMap("select u.wx_imgurl from user_info_tb u where u.id = ?",new Object[]{uin});
				String wxImgUrl= (String)tempMap.get("wx_imgurl");
				if(wxImgUrl==null||"".equals(wxImgUrl)||"null".equals(wxImgUrl))
					wxImgUrl="images/flygame/logo.png";
				request.setAttribute("wximgurl", wxImgUrl);
				request.setAttribute("words", bmMap.get("bwords"));
				request.setAttribute("bid", id);
				request.setAttribute("bakurl", Constants.WXPUBLIC_REDIRECTURL);
				request.setAttribute("appid", Constants.WXPUBLIC_APPID);
			}
		}
	}

	private void otShareScore(HttpServletRequest request,Long uin) {
		Map<String, String> _result = new HashMap<String, String>();
		try {
			_result = publicMethods.getJssdkApiSign(request);
		}catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(result);
		//jssdk权限验证参数
		request.setAttribute("appid", Constants.WXPUBLIC_APPID);
		request.setAttribute("nonceStr", _result.get("nonceStr"));
		request.setAttribute("timestamp", _result.get("timestamp"));
		request.setAttribute("signature", _result.get("signature"));
		
		Long id  = daService.getkey("seq_order_ticket_tb");
		String sql = "insert into order_ticket_tb (id,uin,order_id,money,bnum,ctime,exptime,bwords,type) values(?,?,?,?,?,?,?,?,?)";
		Object []values = null;
		Long ctime = System.currentTimeMillis()/1000;
		Long exptime = ctime + 24*60*60;
		String title = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "words"));
		values = new Object[]{id,uin,-1L,0,0,ctime,exptime,CustomDefind.getValue("FLYGAMGE_BONUS_CONTENT"),6};
		int ret = daService.update(sql, values);
		logger.error(">>>>>战绩红包,ret :"+ret+",bonusid:"+id);
		request.setAttribute("bonusid", id);
		request.setAttribute("btitle", title);
		request.setAttribute("bwords",CustomDefind.getValue("FLYGAMGE_BONUS_CONTENT"));
		request.setAttribute("bakurl", Constants.WXPUBLIC_REDIRECTURL);
	}

	/**
	 * 积分详情
	 * @param request
	 * @param uin
	 */
	private String scoreDetail(HttpServletRequest request,Long uin) {
		Long ntime = System.currentTimeMillis()/1000;
		Long tid = RequestUtil.getLong(request, "tid", -1L);
		String sql = "select gift_score s14, gift_count c14, float_score s13,0 c13,catapult_halve_count c12,catapult_halve_score s12,catapult_full_count c11,catapult_full_score s11," +
				"score_halve_count c10,score_halve_score s10,score_double_count c9,score_double_score s9,bets_halve_count c8," +
				"bets_halve_score s8,bets_double_score s7," +
				"bets_double_count c7, cloud_count c6,cloud_score s6," +
				"crow_count c5,crow_score s5," +
				"db_bullet_count c4,db_bullet_score s4," +
				"empty_bullet_count c3,empty_bullet_score s3," +
				"balance_count c2,balance_score s2," +
				"ticket_count c1,ticket_score s1 from flygame_score_anlysis_tb where  uin=? " ;
		Object [] params = null;
		if(tid>0){
			sql +=" and tid=? ";
			params = new Object[]{uin,tid};
		}else {
			sql +=" and ctime > ?  ";
			params = new Object[]{uin,ntime};
		}
		Map<String, Object> dataMap = daService.getMap( sql,params);
		if(dataMap==null||dataMap.isEmpty())
			dataMap = ZldMap.getMap(new String[]{"c14","c13","c12","c11","c10","c9","c8","c7","c6","c5","c4","c3","c2","c1","s14","s13","s12",
					"s11","s10","s9","s8","s7","s6","s5","s4","s3","s2","s1"}, new Object[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0});
		ResultMap rMap = new ResultMap(dataMap);
		String data = "{";
		//System.err.println(dataMap);
		if(dataMap!=null&&!dataMap.isEmpty()){
			String ret = tidScore(tid);
			data+="\"14\":{\"count\":\""+dataMap.get("c14")+"\",\"score\":\""+dataMap.get("s14")+"\"},";
			data+="\"13\":{\"count\":\""+dataMap.get("c13")+"\",\"score\":\""+dataMap.get("s13")+"\"},";
			data+="\"12\":{\"count\":\""+dataMap.get("c12")+"\",\"score\":\""+dataMap.get("s12")+"\"},";
			data+="\"11\":{\"count\":\""+dataMap.get("c11")+"\",\"score\":\""+dataMap.get("s11")+"\"},";
			data+="\"10\":{\"count\":\""+dataMap.get("c10")+"\",\"score\":\""+dataMap.get("s10")+"\"},";
			data+="\"9\":{\"count\":\""+dataMap.get("c9")+"\",\"score\":\""+dataMap.get("s9")+"\"},";
			data+="\"8\":{\"count\":\""+dataMap.get("c8")+"\",\"score\":\""+dataMap.get("s8")+"\"},";
			data+="\"7\":{\"count\":\""+dataMap.get("c7")+"\",\"score\":\""+dataMap.get("s7")+"\"},";
			data+="\"6\":{\"count\":\""+dataMap.get("c6")+"\",\"score\":\""+dataMap.get("s6")+"\"},";
			data+="\"5\":{\"count\":\""+dataMap.get("c5")+"\",\"score\":\""+dataMap.get("s5")+"\"},";
			data+="\"4\":{\"count\":\""+dataMap.get("c4")+"\",\"score\":\""+dataMap.get("s4")+"\"},";
			data+="\"3\":{\"count\":\""+dataMap.get("c3")+"\",\"score\":\""+dataMap.get("s3")+"\"},";
			data+="\"2\":{\"count\":\""+dataMap.get("c2")+"\",\"score\":\""+dataMap.get("s2")+"\"},";
			data+="\"1\":{\"count\":\""+dataMap.get("c1")+"\",\"score\":\""+dataMap.get("s1")+"\"},";
			data+=ret.substring(2,ret.length()-2);
		}
		data += "}";
		System.out.println(data);
		Map<String,Object> tempMap = daService.getMap("select u.wx_imgurl,c.car_number from user_info_tb u left join" +
				" car_info_tb c on c.uin=u.id where u.id = ?",new Object[]{uin});
		
		rMap = new ResultMap(tempMap);
		String carNumber =rMap.getString("car_number");
		String wxName = rMap.getString("wx_name");
		String wxImgUrl= rMap.getString("wx_imgurl");
			
		request.setAttribute("wxname",wxName);
		if("".equals(wxImgUrl))
			wxImgUrl="images/flygame/logo.png";
		if(carNumber!=null&&carNumber.length()==7)
			carNumber = carNumber.substring(0,3)+"***"+carNumber.substring(6);
		else {
			carNumber="停车宝车主";
		}
		request.setAttribute("wximgurl",wxImgUrl);
		request.setAttribute("car",carNumber);
		request.setAttribute("data", data);
		return data;
	}
	/**
	 * 积分排名,每个车主只显示当天最高一次成绩
	 */
	private String getScoreData(String type,Long uin){
		List<Map<String,Object>> list = null;
		Long ntime = TimeTools.getToDayBeginTime();
		String sql ="select sum(db_bullet_score+empty_bullet_score+gift_score+balance_score" +
				"+ticket_score+cloud_score+crow_score+bets_double_score+bets_halve_score+score_double_score+" +
							"score_halve_score+catapult_full_score+catapult_halve_score+float_score) score,uin,tid " +
				" from flygame_score_anlysis_tb  where ctime > ?  ";
		Object[] params =new Object[]{ntime};
		if(type.equals("friend")){
			sql +=" and (uin in( select buin from user_friend_tb where euin=?) or uin=?) ";
			params = new Object[]{ntime,uin,uin};
		}
		sql +=" group by uin,tid order by score desc ";
		list = daService.getAll(sql,params);
		//System.out.println(list);
		List<Map<String,Object>> onlyList= new ArrayList<Map<String,Object>>();
		List<Long> uinList = new ArrayList<Long>();
		if(list!=null&&!list.isEmpty()){
			for (int i=0;i<list.size();i++){
				Map<String,Object> map = list.get(i);
				Long _uin=(Long)map.get("uin");
				if(uinList.contains(_uin)){
					
					continue;
				}
				else
					uinList.add(_uin);
				Map<String,Object> tempMap =ZldMap.getUser(_uin);
				if(tempMap==null){
					tempMap=daService.getMap("select u.wx_name,u.wx_imgurl,c.car_number from user_info_tb u left join" +
							" car_info_tb c on c.uin=u.id where u.id = ?",new Object[]{_uin});
					if(tempMap!=null)
						ZldMap.putUser(_uin, tempMap);
				}
				
				String carNumber = "";
				String wxName = "";
				String wxImgUrl= "";
				if(tempMap!=null){
					carNumber = (String)tempMap.get("car_number");
					wxName = (String)tempMap.get("wx_name");
					wxImgUrl= (String)tempMap.get("wx_imgurl");
				}
				if(wxName==null||"null".equals(wxName))
					wxName="";
				wxName=wxName.replace("'", "");
				wxName=wxName.replace("\"", "");
				map.put("wxname",wxName);
				if(wxImgUrl==null||"".equals(wxImgUrl)||"null".equals(wxImgUrl))
					wxImgUrl="images/flygame/logo.png";
				if(carNumber!=null&&carNumber.length()==7)
					carNumber = carNumber.substring(0,3)+"***"+carNumber.substring(6);
				else {
					carNumber="停车宝车主";
				}
				map.put("wximgurl",wxImgUrl);
				map.put("car",carNumber);
				onlyList.add(map);
			}
			Collections.sort(onlyList, new Comparator<Map<String, Object>>() {
				public int compare(Map<String, Object> o1, Map<String, Object> o2) {
					return StringUtils.formatDouble(o2.get("score")).compareTo(StringUtils.formatDouble(o1.get("score")));
				}
			});
			while (onlyList.size()>100) {
				onlyList.remove(onlyList.size()-1);
			}
		}
//		logger.error(list);
		return StringUtils.createJson(onlyList);
	}

	private Long getUinByMobile(String mobile) {
		Map usrMap= daService.getMap("select id from user_info_Tb where mobile=? and auth_flag=? ", new Object[]{mobile,4});
		if(usrMap!=null&&usrMap.get("id")!=null)
			return (Long)usrMap.get("id");
		return -1L;
	}

	/**
	 * 取子弹数及飞机
	 * @param request
	 * @param uin
	 * @return
	 */
	private String getData(HttpServletRequest request,Long uin) {
		Long tid = RequestUtil.getLong(request, "tid",-1L);
		Map<String, Object> map = new HashMap<String, Object>();
		Integer bons = 0;
		//读取剩余子弹缓存  uin=tid_number
		Map<Long, String> userBetMap = memcacheUtils.doMapLongStringCache("fly_game_bullets", null, null);
		//System.err.println(tid+":"+userBetMap);
		if(userBetMap!=null){//缓存不为空
			if(userBetMap.get(uin)!=null){//缓存中有这个车主的
				String bs = userBetMap.get(uin);
				String []cacheRets = bs.split("_");
				if(Long.valueOf(cacheRets[0]).equals(tid)){//传入的停车券与缓存中一致
					bons = Integer.valueOf(cacheRets[1]);
				}else {//传入的停车券与缓存中不一致，是新停车券，计算子弹数
					bons = getBets(uin,tid);
				}
			}else {
				bons = getBets(uin,tid);
			}
		}else {
			bons = getBets(uin,tid);
		}
		map.put("bullet_count", bons);//子弹数量
		putPlan(map);
		int index = 0;
		if(bons==0)
			map.put("bullet_gold_count", 0);
		if(bons>0)
			index=cacheBets(uin, bons,5, tid);//加三个金刚弹
		map.put("index", index);
		System.out.println(map);
		return StringUtils.createJson(map);
	}

	/**
	 * 计算子弹数和保存券到池中
	 * @param uin
	 * @param tid
	 * @return
	 */
	private int getBets(Long uin,Long tid){
		int bets=0;
		Map ticketMap = daService.getMap("select money,resources from ticket_tb where id =? and uin=? and state =? ",
				new Object[]{tid,uin,0});
		if(ticketMap!=null&&ticketMap.get("money")!=null){
			Integer money = (Integer)ticketMap.get("money");
			Integer resources = (Integer)ticketMap.get("resources");
			if(money!=null&&money>0){
				bets = money*4-1;
				if(resources!=null&&resources==1)
					bets=bets*4;
				//把车主停车券放入到奖池中
				int	ret  = daService.update("update ticket_tb set state=?,utime=? " +
						"where id=? ",  new Object[]{1,System.currentTimeMillis()/1000,tid});
				logger.error("车主停车券状态改为已使用,ret:"+ret);
				if(ret==1&&resources==0){
					ret = daService.update("insert into flygame_pool_tb (ptype,tid,count,ctime) values(?,?,?,?)", 
							new Object[]{1,tid,1,System.currentTimeMillis()/1000});
					logger.error("车主停车券放入到奖池中"+ticketMap+",ret:"+ret);
				}else {
					logger.error(uin+",ticketid:"+tid+",是购买券，不放入到打飞机停车券池中了。。");
				}
			}
		}
		return bets;
	}
	
	private void putPlan(Map<String, Object> map){
		if(isonline){
			map.put("plane_empty", 8);//空灰机
			map.put("plane_gift", 0);//礼品灰机
			map.put("plane_ticket", 6);//券灰机
			map.put("plane_cash", 1);//现金灰机
			map.put("bullet_gold_count", 5);//金刚弹
			map.put("fly_bird", true);//是否有鸟
		}else {
			map.put("plane_empty", 0);//空灰机
			map.put("plane_gift", 2);//礼品灰机
			map.put("plane_ticket", 2);//券灰机
			map.put("plane_cash", 1);//现金灰机
			map.put("bullet_gold_count", 5);//金刚弹
			map.put("fly_bird", true);//是否有鸟
		}
	}
	
	private void toShare(HttpServletRequest request,Long uin) {
		Map<String, String> _result = new HashMap<String, String>();
		try {
			_result = publicMethods.getJssdkApiSign(request);
		}catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(result);
		//jssdk权限验证参数
		request.setAttribute("appid", Constants.WXPUBLIC_APPID);
		request.setAttribute("nonceStr", _result.get("nonceStr"));
		request.setAttribute("timestamp", _result.get("timestamp"));
		request.setAttribute("signature", _result.get("signature"));
		String carid="";
		Map carMap = daService.getMap("select car_number from car_info_Tb " +
				" where uin=? ", new Object[]{uin});
		if(carMap!=null&&carMap.get("car_number")!=null&&!carMap.get("car_number").toString().equals(""))
			carid = (String)carMap.get("car_number");
		if(carid!=null&&carid.length()==7)
			carid = carid.substring(0,4)+"**"+carid.substring(6);
		Long sid = RequestUtil.getLong(request, "sid", -1L);
		String title = AjaxUtil.decodeUTF8(RequestUtil.getString(request, "title"));
		Long bid = backBonus(uin,sid,CustomDefind.getValue("FLYGAMGE_BONUS_CONTENT"));//分享红包
		request.setAttribute("bonusid", bid);
		request.setAttribute("carnumber", carid);
		Long tid = RequestUtil.getLong(request, "tid", -1L);
		//Integer bets = RequestUtil.getInteger(request, "bnums",-1);
		request.setAttribute("tid", tid);
		request.setAttribute("uin", uin);
		request.setAttribute("title", title);
		request.setAttribute("words", CustomDefind.getValue("FLYGAMGE_BONUS_CONTENT"));
		request.setAttribute("page", RequestUtil.getString(request, "page"));//页面来源
		cacheBets(uin, 0,0, tid);
	}

	private String getScore(HttpServletRequest request,Long uin) {
		
		Long tid = RequestUtil.getLong(request, "tid", -1L);
		Integer bets = RequestUtil.getInteger(request, "bnums", -1);
		Integer gbets = RequestUtil.getInteger(request, "gbnums", -1);
		String type = RequestUtil.getString(request, "type");
		String  token = RequestUtil.getString(request, "token");
		Integer score = 0;
		//1，打死云，加3积分，
		//2，打死乌鸦，加30积分，
		if(type.equals("cloud")){
			score=3;
		}else if(type.equals("crow")){
			score=50;
		}
		
		logger.error(">>>"+uin+"打灰机：,tid:"+tid+",type:"+type+",bets:"+bets+",gbets:"+gbets+",token:"+token);
		if(bets<0){
			logger.error(">>>error::bet<0"+uin+"打灰机：,tid:"+tid+",type:"+type+",bets:"+bets);
			return null;
		}
		
		Map<Long, String> userBetMap = memcacheUtils.doMapLongStringCache("fly_game_bullets", null, null);
		//检查子弹数量是不是在减少，如果没有减少，可能是在浏览器中非法调用
		if(userBetMap!=null){
			if(userBetMap.get(uin)!=null){
				String lastBets = userBetMap.get(uin);
				logger.error(uin+",tid:"+tid+",bets："+bets+",cache:"+lastBets);
				if(lastBets.indexOf("_")!=-1){
					String prelbets = lastBets.split("_")[1];//普通子弹
					String pregbets = lastBets.split("_")[2];//金刚弹
					int index = Integer.parseInt(lastBets.split("_")[3]);
					if(index>15) index=15;
					String thisToken = Base64forjs.keystrs[index].substring(index)+"==";
					logger.error("tihstoken:"+thisToken);
					String pretid = lastBets.split("_")[0];
					if(	token.equals(thisToken)&&
							Check.isNumber(prelbets)&&Check.isNumber(pretid)){
						int prelt = Integer.valueOf(prelbets);
						int pregt = Integer.valueOf(pregbets);
						Long pid = Long.valueOf(pretid);
						// 21990=33432_3_2_10
						if(!tid.equals(pid)||(bets>prelt&&gbets>pregt)||bets>320||(bets+gbets)>=(pregt+prelt)){
							logger.error("flygame record bets error >>>>>>>error>>>>>调用错误,可能在非法调用，返回.... ,uin="+uin+",tid:"+tid+",bnums:"+bets);
							return null;
						}
					}else {
						return null;
					}
				}
			}else {
				logger.error(uin+",缓存中没有子弹 ,可能是非法调用 。。。。");
				return null;
			}
		}else {
			logger.error(uin+",没有缓存了.....");
			return null;
		}
		
		//保存子弹 
		int index = cacheBets(uin, bets, gbets,tid);
		
		String result ="{\"type\":\"0\",\"ptype\":0,\"info\":\"打中了一个空灰机\",\"isbz\":false,\"index\":"+index+"}";
		if(type.equals("plane_empty"))
			return result;
		
		//飞机被激怒，进入暴走模式，会撞击弹弓。余额飞机，暴走概率1/2。停车券飞暴走概率四分之一,福袋 1/8
		int rand = 0;
		if(type.equals("plane_ticket")){
			rand = new Random().nextInt(4);
		}else if(type.equals("plane_cash")){
			rand = new Random().nextInt(2);
		}else if(type.equals("fly_bird")){
			rand = new Random().nextInt(8);
		}
		if(rand==1){//进入暴走模式
			result="{\"isbz\":true,\"ptype\":0,\"index\":"+index+"}";
			logger.error("type:"+type+",进入暴走模式");
			return result;
		}
		Map ret = null;//
		if(score==0){//不是打中云或乌鸦
			ret = getTicketMap(type,uin);
			if(ret==null||ret.isEmpty()){//已经用完了。
				reloadTickets(type);//重置一批
				ret = getTicketMap(type,uin);
			}
		}
		//System.out.println(ret);
		Map userMap = daService.getMap("select wx_name,is_auth from user_info_tb where id =? ", new Object[]{uin});
		Integer isAuth = 0;//是否验证过
		if(userMap!=null&&userMap.get("is_auth")!=null)
			isAuth=(Integer)userMap.get("is_auth");
		String catapultBlud ="";//弹弓血量
		
		if(ret!=null&&!ret.isEmpty()){
			Integer ptype =(Integer)ret.get("ptype");
			if(ptype==4){
				Long acount = daService.getLong("select count from flygame_pool_tb where ptype=? ", new Object[]{5});
				ret.put("dcount", acount);
				ret.put("bcount", ret.get("count"));
			}else if(ptype==5){
				Long acount = daService.getLong("select count from flygame_pool_tb where ptype=? ", new Object[]{4});
				ret.put("bcount",acount);
				ret.put("dcount", ret.get("count"));
			}else if(ptype==6){
				ret.put("dcount", ret.get("count"));
			}
			String remark = "";
			//String turl ="";
			int r= 0;
			if(ptype==1){
				r= daService.update("update flygame_pool_tb set count=count-? where id=? ", new Object[]{1,ret.get("id")});
				logger.error(">>>"+uin+"打中了,type:"+type+",ptype:"+ptype+",车主停车券，修改数据："+r);
			}
			Long ntime = TimeTools.getToDayBeginTime();
			Double money = StringUtils.formatDouble(ret.get("money"));
			//奖券类型：0停车宝停车券 1车主停车券 2余额券 3广告券 4清空福袋 5翻倍福袋
			if(ptype==1){//打中了车主券
				Map tMap=daService.getMap("select limit_day,money,pmoney,uin,resources from ticket_tb where id =? ", new Object[]{ret.get("tid")});
				boolean hasUser=true;
				Long touin =-1L;
				if(tMap==null||tMap.isEmpty()){
					tMap = new HashMap<String,Object>();
					tMap.put("money", 1);
					tMap.put("uin", -1L);
					tMap.put("limit_day", ntime+3*24*60*60);
					hasUser=false;
					logger.error("停车券不存在 >>>>id:"+ret.get("tid"));
				}else{
					touin =(Long)tMap.get("uin");
					Long putday = (Long)ret.get("ctime");
					putday = TimeTools.getBeginTime(putday);
					Long LastLimitDay  = (Long)tMap.get("limit_day");
					Long limtday = LastLimitDay-putday;
					if(limtday<24*60*60-1)
						limtday = 24*60*60-1L;
					tMap.put("limit_day",ntime+limtday);
				}
				money = StringUtils.formatDouble(tMap.get("money"));
				Double pmoney = StringUtils.formatDouble(tMap.get("pmoney"));
				Integer resources =(Integer)tMap.get("resources");
				
				if(resources==null)
					resources=0;
				r= daService.update("insert into ticket_tb (create_time,limit_day,money,pmoney,state,uin,type,resources) values(?,?,?,?,?,?,?,?) ",
						new Object[]{ntime,tMap.get("limit_day"),tMap.get("money"),pmoney,0,uin,0,resources});
				//logger.error(">>>"+uin+"游戏打中了券,ticketid:"+tMap.get("id")+"，写入数据库："+r);
				if(r==1){
					if(hasUser){
						//被打中的车主车牌
						String carNumber=publicMethods.getCarNumber(touin);
						Map<String, Object> userMap2 = daService.getMap("select is_auth,wx_imgurl,wx_name from user_info_tb where id=? ",new Object[] {touin});
						if(resources==1){//购买的停车券
							String car = carNumber;
							if(car.length()==7)
								car=car.substring(0,3)+"***"+car.substring(6);
							result="{\"type\":\"2\",\"ptype\":"+ptype+",\"info\":\"恭喜打中车主"+car+"\",\"money\":\""+tMap.get("money")+"\",\"backmoney\":\""+tMap.get("pmoney")+"\",\"touin\":\""+touin+"\"}";
						}else{
							String wxname = (String)userMap2.get("wx_name");
							if(wxname==null||wxname.equals(""))
								wxname="停车宝车主";
							wxname= wxname.replace("'","");
							wxname= wxname.replace("\"","");
							String imgurl = (String)userMap2.get("wx_imgurl");
							if(imgurl==null||wxname.equals(""))
								imgurl="images/flygame/logo.png";
							result="{\"type\":\"1\",\"ptype\":"+ptype+",\"info\":\"恭喜打中\",\"money\":\""+tMap.get("money")+"\",\"wxname\":\""+wxname+"\",\"touin\":\""+tMap.get("uin")+"\",\"imgurl\":\""+imgurl+"\",\"isauth\":\""+userMap2.get("is_auth")+"\"}";
						}
						if(carNumber!=null&&!"".equals(carNumber)&&carNumber.length()==7)
							carNumber = carNumber.substring(0,4)+"**"+carNumber.substring(6);
						remark = tMap.get("money")+"元车主灰机";
					//	result="{\"type\":\"1\",\"info\":\"恭喜打中"+carNumber+"的停车券灰机\",\"money\":\""+tMap.get("money")+"\",\"touin\":\""+tMap.get("uin")+"\"}";
						//缓存给被打中灰机的车主消息
						carNumber=publicMethods.getCarNumber(uin);
						if(carNumber!=null&&!"".equals(carNumber)&&carNumber.length()==7)
							carNumber = carNumber.substring(0,4)+"**"+carNumber.substring(6);
						cacheMessage(touin, "车主"+carNumber+"打中了你放飞的停车券", 0);
						//发微信消息给被打中灰机的车主
						String wxName = "停车宝车主";
						if(userMap!=null){
							wxName = (String)userMap.get("wx_name");
							if(wxName!=null)
								wxName=wxName.replace("'", "").replace("\"", "");
						}
					}else {
						result="{\"type\":\"0\",\"ptype\":"+ptype+",\"info\":\"恭喜打中了一只停车券灰机\",\"money\":\"1\"}";
						remark ="1元车主灰机";
					}
					//sendWeiXinMesg(wxName,touin,"车主"+carNumber+"打中了你的灰机", "我打中了你放飞的"+tMap.get("money")+"元停车券灰机","点击查看我的好机友");
					//sendWeiXinMesg(touin, "我打中了你放飞的"+tMap.get("money")+"元停车券灰机","车主"+carNumber+"打中了你的灰机");
				}
				
			}else if(ptype==0){//打中了停车宝代金券
				r= daService.update("insert into ticket_tb (create_time,limit_day,money,state,uin,type) values(?,?,?,?,?,?) ",
						new Object[]{ntime,ntime+3*24*60*60-1,ret.get("money"),0,uin,0});
				logger.error(">>>"+uin+"游戏打中了代金券，写入数据库："+r);
				if(r==1){
					Integer con = StringUtils.formatDouble(ret.get("money")).intValue();
					result="{\"type\":\"0\",\"ptype\":"+ptype+",\"info\":\"恭喜打中了一只停车券灰机\",\"money\":\""+con+"\"}";
					if(con>=3){
						String carNumber=publicMethods.getCarNumber(uin);
						if(carNumber!=null&&carNumber.length()==7)
							carNumber = carNumber.substring(0,4)+"**"+carNumber.substring(6);
						cacheMessage(null, "车主"+carNumber+"打中了"+con+"元停车券", 1);
					}
				}
				remark = StringUtils.formatDouble(ret.get("money")).intValue()+"元停车券灰机";
			}else if(ptype==2){//打中了停车宝现金券
				if(isAuth==1){//已认证车主
					List<Map<String, Object>> bathSql = new ArrayList<Map<String,Object>>();
					//更新用户余额
					Map<String, Object> userSqlMap = new HashMap<String, Object>();
					//更新停车场余额
					Map<String, Object> userAccountlMap = new HashMap<String, Object>();
					
					userSqlMap.put("sql", "update user_info_tb  set balance =balance+? where id=?");
					userSqlMap.put("values", new Object[]{ret.get("money"),uin});
					
					userAccountlMap.put("sql", "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type) values(?,?,?,?,?,?)");
					userAccountlMap.put("values", new Object[]{uin,ret.get("money"),0,System.currentTimeMillis()/1000,"打灰机中奖",8});
					
					bathSql.add(userSqlMap);
					bathSql.add(userAccountlMap);
					
					boolean red= daService.bathUpdate(bathSql);
					logger.error(">>>"+uin+"游戏打现金券，写入数据库："+red);
					Double con = StringUtils.formatDouble(ret.get("money"));
					result="{\"type\":\"2\",\"ptype\":"+ptype+",\"info\":\"恭喜打中了一只余额灰机\",\"money\":\""+con+"\"}";
					if(con>=1){
						String carNumber=publicMethods.getCarNumber(uin);
						if(carNumber!=null&&carNumber.length()==7)
							carNumber = carNumber.substring(0,4)+"**"+carNumber.substring(6);
						cacheMessage(null, "车主"+carNumber+"打中了"+con+"现金", 1);
					}
					
				}else {//未认证车主
					result="{\"type\":\"-1\",\"ptype\":"+ptype+",\"info\":\"恭喜打中了一只余额灰机\",\"money\":\""+ret.get("money")+"\"}";
				}
				if(money>=1)
					remark = money.intValue()+"元余额灰机";
				else {
					remark = money+"元余额灰机";
				}
			}else if(ptype==3){//打中了广告券
				Long aid = (Long)ret.get("aid");
				if(aid!=null){
					Map amMap= daService.getMap("select * from advert_tb where id=? ", new Object[]{aid});
					if(aid==1){//优乐
						String price=getThirdResult(uin,(String)amMap.get("aurl"));
						logger.error("uin:"+uin+",price:"+price);
						if(price!=null&&!"".equals(price)){
							if(price.equals("27.99")){//新用户
								result="{\"type\":\"1\",\"ptype\":"+ptype+",\"gid\":\""+aid+"\",\"info\":\"恭喜您打中"+amMap.get("tname")+"灰机\",\"price\":\""+price+"\",\"url\":\""+amMap.get("appurl")+"\"}";
							}else {//老用户
								result="{\"type\":\"0\",\"ptype\":"+ptype+",\"gid\":\""+aid+"\",\"info\":\"恭喜您打中"+amMap.get("tname")+"灰机\",\"price\":\""+price+"\",\"url\":\""+amMap.get("appurl")+"\"}";
							}
							int cr = daService.update("update advert_tb set open = open+? where id =? ", new Object[]{1,aid});
							logger.error(">>>>uin+"+uin+",opens,gid:"+aid+",ret:"+cr);
							remark =amMap.get("tname")+"灰机";
							//turl="http://a.app.qq.com/o/simple.jsp?pkgname=com.youlemobi.customer";
						}
					}else if(aid==2){//人人车
						remark =amMap.get("tname")+"礼包";
						//turl="http://m.renrenche.com/liantong/";
						int cr = daService.update("update advert_tb set open = open+? where id =? ", new Object[]{1,aid});
						logger.error(">>>>uin+"+uin+",opens,gid:"+aid+",ret:"+cr);
						result="{\"type\":\"2\",\"ptype\":"+ptype+",\"gid\":\""+aid+"\",\"info\":\"恭喜您打中"+amMap.get("tname")+"礼包\",\"url\":\""+amMap.get("appurl")+"\"}";
					}else if(aid==3||aid==4){//1818投资
						remark =amMap.get("tname")+"灰机";
						int cr = daService.update("update advert_tb set open = open+? where id =? ", new Object[]{1,aid});
						logger.error(">>>>uin+"+uin+",opens,gid:"+aid+",ret:"+cr);
						//turl="http://mp.weixin.qq.com/s?__biz=MjM5MjgxOTY4OA==&mid=208360229&idx=1&sn=df9c59a4d36233c65f5fef2baf373141&scene=0#rd";
						result="{\"type\":\""+aid+"\",\"ptype\":"+ptype+",\"gid\":\""+aid+"\",\"info\":\"恭喜您打中"+amMap.get("tname")+"灰机\",\"url\":\""+amMap.get("appurl")+"\"}";
					}
				}
			}else if(ptype==4){//打中了停车宝清空福袋 
				if(!isFirstHitBird(uin)){
					r = daService.update("update ticket_tb set state=? where uin=? and type=? and state=? and resources=? ", new Object[]{1,uin,0,0,0});
					logger.error(">>>"+uin+"游戏打清空福袋，写入数据库："+r);
					result="{\"type\":\"0\",\"ptype\":"+ptype+",\"info\":\"对不起，你打中了一个清空福袋\",\"bcount\":\""+ret.get("bcount")+"\",\"dcount\":\""+ret.get("dcount")+"\"}";
				}else {
					result="{\"type\":\"-1\",\"ptype\":"+ptype+",\"info\":\"对不起，你打中了一个清空福袋\",\"bcount\":\""+ret.get("bcount")+"\",\"dcount\":\""+ret.get("dcount")+"\"}";
				}
				String carNumber=publicMethods.getCarNumber(uin);
				if(carNumber!=null&&carNumber.length()==7)
					carNumber = carNumber.substring(0,4)+"**"+carNumber.substring(6);
				cacheMessage(null, "车主"+carNumber+"打中了清空福袋，所有停车券清空", 1);
				remark ="清空券灰机";
			}else if(ptype==5){//打中了停车宝翻倍福袋 
				if(!isFirstHitBird(uin)){
					//券最高翻倍到12元，原来券额在8元以上的不处理了，分两条sql执行
					//5元以下停车券，     update ticket_tb set money=money*2,umoney=umoney*2 where uin=21776 and type=0 and state=0 and money<5
					//5元以上11元以下券 update ticket_tb set money=8,umoney=(umoney/money)*8 where uin= 21776 and type=0 and state=0 and money between 5 and 11 
					int r1 = daService.update("update ticket_tb set money=? where uin= ? and type=? and state=? and resources=? and limit_day>=? and money between ? and ?",
							new Object[]{8,uin,0,0,0,TimeTools.getToDayBeginTime(),5,7});
					r = daService.update("update ticket_tb set money=money*? where uin=? and type=? and state=? and resources=? and limit_day>=? and money<? ", 
							new Object[]{2,uin,0,0,0,TimeTools.getToDayBeginTime(),5});
					logger.error(">>>"+uin+"游戏打翻倍福袋，写入数据库："+r+",7-11元券更新了："+r1);
					result="{\"type\":\"1\",\"ptype\":"+ptype+",\"info\":\"恭喜打中了一只翻倍福袋\",\"bcount\":\""+ret.get("bcount")+"\",\"dcount\":\""+ret.get("dcount")+"\"}";
				}else {
					result="{\"type\":\"-1\",\"ptype\":"+ptype+",\"info\":\"恭喜打中了一只翻倍福袋\",\"bcount\":\""+ret.get("bcount")+"\",\"dcount\":\""+ret.get("dcount")+"\"}";
				}
				String carNumber=publicMethods.getCarNumber(uin);
				if(carNumber!=null&&carNumber.length()==7)
					carNumber = carNumber.substring(0,4)+"**"+carNumber.substring(6);
				cacheMessage(null, "车主"+carNumber+"打中了翻倍福袋，所有停车券金额翻倍", 1);
				remark ="双倍券灰机";
			}else if(ptype==6){//打中了第二关入口
				logger.error(">>>"+uin+"游戏第二关入口");
				result="{\"type\":\"2\",\"info\":\"恭喜打中了第二关入口\",\"dcount\":\""+ret.get("dcount")+"\",\"count\":\"500\"}";
				remark ="第二关入口";
			}else if(ptype==7){
				logger.error(">>>"+uin+"子弹翻倍福袋");
				result="{\"type\":\"0\",\"ptype\":"+ptype+",\"info\":\"恭喜打中了一只子弹翻倍福袋\"}";
				remark ="子弹翻倍福袋";
				bets  = bets*2;
				gbets = gbets*2;
				index = cacheBets(uin,bets,gbets, tid);
			}else if(ptype==8){
				logger.error(">>>"+uin+"子弹减半道具");
				result="{\"type\":\"0\",\"ptype\":"+ptype+",\"info\":\"很遗憾打中了一只子弹减半道具\"}";
				remark ="子弹减半道具";
				if(bets%2==0)
					bets= bets/2;
				else 
					bets = bets/2+1;
				if(gbets%2==0)
					gbets= gbets/2;
				else 
					gbets = gbets/2+1;
				index = cacheBets(uin,bets,gbets, tid);
			}else if(ptype==9){
				logger.error(">>>"+uin+"积分翻倍福袋");
				result="{\"type\":\"0\",\"ptype\":"+ptype+",\"info\":\"恭喜打中了一只积分翻倍福袋\"}";
				remark ="积分翻倍福袋";
			}else if(ptype==10){
				logger.error(">>>"+uin+"积分减半道具");
				result="{\"type\":\"0\",\"ptype\":"+ptype+",\"info\":\"很遗憾打中了一只积分减半道具\"}";
				remark ="积分减半道具";
			}else if(ptype==11){
				logger.error(">>>"+uin+"弹弓满血福袋");
				result="{\"type\":\"0\",\"ptype\":"+ptype+",\"info\":\"恭喜打中了一只弹弓满血福袋\"}";
				remark ="弹弓满血福袋";
				catapultBlud="full";
			}else if(ptype==12){
				logger.error(">>>"+uin+"弹弓血量减半道具");
				result="{\"type\":\"0\",\"ptype\":"+ptype+",\"info\":\"很遗憾打中了一只弹弓血量减半道具\"}";
				remark ="弹弓血量减半道具";
				catapultBlud="halve";
			}
			
			Long fstId = daService.getkey("seq_flygame_score_tb");
			int rs = daService.update("insert into flygame_score_tb (id,uin,fgid,remark,ptype,money,ctime) values(?,?,?,?,?,?,?)",
						new Object[]{fstId,uin,ret.get("id"),remark,ptype,money,System.currentTimeMillis()/1000});
			logger.error(">>>flygame,uin="+uin+",写入成绩："+rs);
			
			String sql = "update flygame_score_anlysis_tb set ";
			//0停车宝停车券 1车主停车券 2余额券 3广告券 4清空福袋 5翻倍福袋
			
			if(ptype==1||ptype==0){
				double s = money*0.1;
				sql += " ticket_count=ticket_count+1,ticket_score=ticket_score+"+s;
			}else if(ptype==2){
				double s = money*0.5;
				sql += " balance_count=balance_count+1,balance_score=balance_score+"+s;
			}else if(ptype==3){
				sql += " gift_count=gift_count+1,gift_score=gift_score+1.5";
			}else if(ptype==4){
				sql += " empty_bullet_count=empty_bullet_count+1,empty_bullet_score=empty_bullet_score-1";
			}else if(ptype==5){
				sql += " db_bullet_count=db_bullet_count+1,db_bullet_score=db_bullet_score+1";
			}else if(ptype==6){
				sql += " second_count=second_count+1,second_score=second_score+2";
			}else if(ptype==7){
				sql += " bets_double_count=bets_double_count+1,bets_double_score=bets_double_score+1";
			}else if(ptype==8){
				sql += " bets_halve_count=bets_halve_count+1,bets_halve_score=bets_halve_score-1";
			}else if(ptype==9){
				sql += " score_double_count=score_double_count+1,score_double_score=score_double_score+1";
			}else if(ptype==10){
				sql += " score_halve_count=score_halve_count+1,score_halve_score=score_halve_score-1";
			}else if(ptype==11){
				sql += " catapult_full_count=catapult_full_count+1,catapult_full_score=catapult_full_score+1";
			}else if(ptype==12){
				sql += " catapult_halve_count=catapult_halve_count+1,catapult_halve_score=catapult_halve_score-1";
			}
			rs = daService.update(sql+" where uin=? and tid=? ", new Object[]{uin,tid });
			logger.error(uin+">>>>>更新积分统计："+rs);
			if(rs==0){
				rs = daService.update("insert into flygame_score_anlysis_tb(uin,ctime,db_bullet_count,db_bullet_score,empty_bullet_count,empty_bullet_score," +
						"gift_count,gift_score,balance_count,balance_score,ticket_count,ticket_score,second_count,second_score,tid) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
						new Object[]{uin,System.currentTimeMillis()/1000,0,0.0,0,0.0,0,0.0,0,0.0,0,0.0,0,0.0,tid});
				logger.error(uin+">>>>>初始化积分统计："+rs);
				rs = daService.update(sql+" where uin=? and tid=? ", new Object[]{uin,tid});
				logger.error(uin+">>>>>更新积分统计："+rs);
			}
			if(rs==1){//处理积分加倍或减半
				if(ptype==9||ptype==10){
					sql ="select sum(db_bullet_score+empty_bullet_score+gift_score+balance_score+ticket_score" +
							"+cloud_score+crow_score+bets_double_score+bets_halve_score+score_double_score+" +
							"score_halve_score+catapult_full_score+catapult_halve_score+float_score) score " +
							" from flygame_score_anlysis_tb  where tid = ?  ";
					Object[] params =new Object[]{tid};
					Map scroeMap = daService.getMap(sql, params);
					Double floatScore=0.0;
					Double allScore = StringUtils.formatDouble(scroeMap.get("score"));
					if(ptype==9){
						floatScore = StringUtils.formatDouble(allScore);
					}else if(ptype==10){
						floatScore = StringUtils.formatDouble(allScore/2)*(-1);
					}
					sql = "update flygame_score_anlysis_tb set  float_score=float_score+? where tid=? " ;
					rs  = daService.update(sql, new Object[]{floatScore,tid});
					logger.error(uin+">>>>>处理积分加倍或减半：ptype(9加倍10减半):"+ptype+",allscore:"+allScore+",floatscore:"+floatScore+",ret:"+rs);
				}
			}
			result = result.substring(0,result.length()-1)+",\"sid\":\""+fstId+"\"}";
		}else if(score>0){
			String sql = "update flygame_score_anlysis_tb set ";
			if(type.equals("cloud")){
				sql += " cloud_count=cloud_count+? ,cloud_score=cloud_score+? ";
			}else if(type.equals("crow")){
				sql += " crow_count=crow_count+? ,crow_score=crow_score+?";
			}
			int rs = daService.update(sql+" where uin=? and tid=? ", new Object[]{1,score,uin,tid });
			logger.error(uin+">>>>>更新积分统计："+rs);
			if(rs==0){
				rs = daService.update("insert into flygame_score_anlysis_tb(uin,ctime,db_bullet_count,db_bullet_score,empty_bullet_count,empty_bullet_score," +
						"gift_count,gift_score,balance_count,balance_score,ticket_count,ticket_score,second_count,second_score,tid) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
						new Object[]{uin,System.currentTimeMillis()/1000,0,0.0,0,0.0,0,0.0,0,0.0,0,0.0,0,0.0,tid});
				logger.error(uin+">>>>>初始化积分统计："+rs);
				rs = daService.update(sql+" where uin=? and tid=? ", new Object[]{1,score,uin,tid});
				logger.error(uin+">>>>>更新积分统计："+rs);
			}
		}
		//查本轮积分
		String sql ="select sum(db_bullet_score+empty_bullet_score+gift_score+balance_score+ticket_score" +
				"+cloud_score+crow_score+bets_double_score+bets_halve_score+score_double_score" +
				"+score_halve_score+catapult_full_score+catapult_halve_score+float_score) score " +
				" from flygame_score_anlysis_tb  where tid = ?  ";
		Object[] params =new Object[]{tid};
		Map scroeMap = daService.getMap(sql, params);
		result = result.substring(0,result.length()-1)+",\"score\":\""+scroeMap.get("score")+"\"," +
				"\"isbz\":false,\"bullets\":"+bets+",\"goldbullets\":"+gbets+",\"catapult\":\""+catapultBlud+"\",\"index\":"+index+"}";
		
		String mesg = getCacheMesg(uin);
		//有消息
		if(mesg!=null&&!"".equals(mesg)){
			result = result.substring(0,result.length()-1)+",\"mesgs\":"+mesg+"}";
		}
		//System.err.println(result);
		return result;
	}
	
	
	//取所有缓存消息（20分钟内的消息）
	private String getCacheMesg(Long uin) {
		//个人消息
		Map<Long, String> userCacheMap = memcacheUtils.doMapLongStringCache("flygame_usermesg_cache", null, null);
		//公众消息
		Map<Long, String> publicCacheMap = memcacheUtils.doMapLongStringCache("flygame_publicmesg_cache", null, null);
		//logger.error(userCacheMap);
		//logger.error(publicCacheMap);
		String mesgs = "[\"购买券不再翻倍，不再清空\",\"购买券子弹数是普通券4倍\",\"打死绿帽乌鸦积50分\",";
		if(userCacheMap!=null){
			String mymesgs = userCacheMap.get(uin);
			if(mymesgs!=null)
				mesgs += "\""+mymesgs.split("_")[1]+"\",";
		}
		if(publicCacheMap!=null&&!publicCacheMap.isEmpty()){
			for(Long key : publicCacheMap.keySet()){
				mesgs+="\""+publicCacheMap.get(key)+"\",";
			}
		}
		mesgs += "\"购买券不再翻倍，不再清空\",\"购买券子弹数是普通券4倍\",\"打死绿帽乌鸦积50分\"]";
		return mesgs;
	}

	/*是否第一次打中福袋*/
	private boolean isFirstHitBird(Long uin){
		Long count= daService.getLong("select count(Id) from flygame_score_tb where uin =? and fgid in" +
				"(select id from flygame_pool_tb where ptype in(?,?))", new Object[]{uin,4,5});
		if(count>0)
			return false;
		return true;
	}

	private Long backBonus(Long uin,Long sid,String content) {
		Long id  = daService.getkey("seq_order_ticket_tb");
		String sql = "insert into order_ticket_tb (id,uin,order_id,money,bnum,ctime,exptime,bwords,type) values(?,?,?,?,?,?,?,?,?)";
		Object []values = null;
		Long ctime = System.currentTimeMillis()/1000;
		Long exptime = ctime + 24*60*60;
		values = new Object[]{id,uin,sid,12,8,ctime,exptime,content,5};
		int ret = daService.update(sql, values);
		logger.error(">>>>>打灰机红包,money :12/8, sid:"+sid+",ret :"+ret+",bonusid:"+id);
		if(ret==1&&sid>0){
			//记录成绩已经分享
			ret = daService.update("update flygame_score_tb set is_operate=? where id =? ", new Object[]{1,sid});
			logger.error(">>>>>打灰机分享 sid:"+sid+",ret :"+ret+",bonusid:"+id);
		}
		return id;
	}

	private int addFriend(Long uin, Long touin,Long sid) throws Exception {
		if(touin==-1){//没有好友时，从打中的券中查原放飞券的车主
			Map uMap = daService.getMap("select uin from ticket_tb where id =(select tid from flygame_pool_tb " +
					"where id =(select fgid from flygame_score_tb where id =?)) ", new Object[]{sid});
			if(uMap!=null&&!uMap.isEmpty()){
				touin = (Long)uMap.get("uin");
			}
		}
		if(uin.equals(touin)){
			//记录成绩已经加了好机友
			int r = daService.update("update flygame_score_tb set is_operate=? where id =? ", new Object[]{1,sid});
			logger.error(">>>>>打灰机加为好机友  sid:"+sid+",ret :"+r);
			return 1;
		}
		else if(touin==-1){
			return 0;
		}
		Long count = daService.getLong("select count(ID) from user_friend_tb where buin=? and euin=? ", new Object[]{uin,touin});
		int r =0;
		if(count==0){
			r = daService.update("insert into user_friend_tb (buin,euin,ctime) values(?,?,?)", 
					new Object[]{uin,touin,System.currentTimeMillis()/1000});
			logger.error(">>>>>加入好机友：buin:"+uin+",euin:"+touin);
			r += daService.update("insert into user_friend_tb (buin,euin,ctime) values(?,?,?)", 
					new Object[]{touin,uin,System.currentTimeMillis()/1000});
			logger.error(">>>>>加入好机友：buin:"+touin+",euin:"+uin);
			
			if(r==2){
				r = daService.update("update flygame_score_tb set is_operate=? where id =? ", new Object[]{1,sid});
				logger.error(">>>>>打灰机加为好机友  sid:"+sid+",ret :"+r);
				//更新到环信
				//自己是否已注册到环信
				boolean uf =false;//自己注册结果
				boolean tf =false;//机友注册结果
				uf = HXHandle.reg("hx"+uin,  publicMethods.getHXpass(uin));
				if(uf){
					logger.error(uin+">>>>> 注册环信成功");
					//更新到zld数据库
					int ret = daService.update("update user_info_tb set hx_name=? ,hx_pass=? where id =? ", 
							new Object[]{"hx"+uin, publicMethods.getHXpass(uin),uin});
					logger.error(touin+">>>>> 注册环信结果："+ret);
				}
				tf = HXHandle.reg("hx"+touin,  publicMethods.getHXpass(touin));
				if(tf){
					logger.error(touin+">>>>> 注册环信成功");
					//更新到zld数据库
					int ret = daService.update("update user_info_tb set hx_name=? ,hx_pass=? where id =? ", 
							new Object[]{"hx"+touin, publicMethods.getHXpass(touin),touin});
					logger.error(touin+">>>>> 注册环信结果："+ret);
				}
				//if(toUinCount==1||uinCount==1){//有一个未注册过环信，在环信登记为好友
				ObjectNode objectNode = EasemobIMUsers.addFriendSingle("hx"+uin, "hx"+touin);
				if(objectNode!=null){
					String statusCode = JsonUtil.getJsonValue(objectNode.toString(), "statusCode");
					if(statusCode.equals("200")){
						logger.error(uin+",环信加好友成功,touin:"+touin+",ret:"+objectNode.toString());
						//发消息.....................
						String mesg = "我打中了您放飞的停车券，加您为好机友了~";
						objectNode=EasemobIMUsers.sendMsg("hx"+uin, "hx"+touin, mesg);
						if(objectNode!=null){
							logger.error(uin+",环信加好友成功发送消息，内容："+mesg+",uin:"+uin+",touin:"+touin+",ret:"+objectNode.toString());
							objectNode=EasemobIMUsers.sendMsg("hx"+touin, "hx"+uin, "我们已经是好友，通过打飞机添加");
							if(objectNode!=null){
								logger.error(uin+",环信加好友成功发送消息，内容：我们已经是好友，通过打飞机添加,uin:"+touin+",touin:"+uin+",ret:"+objectNode.toString());
							}
						}
						int rs = daService.update("update user_friend_tb set is_add_hx=? where  buin=? or euin=?  ", new Object[]{1,uin,uin});
						logger.error(uin+">>>>>>更新zld好友表，ret:"+rs);
					}else {
						logger.error(uin+",环信加好友失败,touin:"+touin+",ret:"+objectNode.toString());
					}
				}
				//}
			}
			
			Map userMap = daService.getMap("select wx_name,wxp_openid from user_info_tb where id=?  ", new Object[]{uin});
			String wxName = "";
			if(userMap!=null){
				wxName = (String)userMap.get("wx_name");
				if(wxName!=null)
					wxName=wxName.replace("'", "").replace("\"", "");
			}
			String carNumber=publicMethods.getCarNumber(uin);
			if(carNumber!=null&&!"".equals(carNumber)&&carNumber.length()==7)
				carNumber = carNumber.substring(0,4)+"**"+carNumber.substring(6);
			sendWeiXinMesg(wxName,touin, "车主"+carNumber+"加您为好机友", "我打中了您放飞的停车券，加您为好机友了~","点击查看好机友");
		}else {
			r = daService.update("update flygame_score_tb set is_operate=? where id =? ", new Object[]{1,sid});
			logger.error(">>>>>打灰机加为好机友  sid:"+sid+",ret :"+r);
			return count.intValue();
		}
		return r;
	}

	private Map getTicketMap(String type,Long uin){
		String sql = "";
		Object []values = null;
		Long count=0L;
		if(type.equals("plane_gift")){
			sql = "select * from flygame_pool_tb where ptype =? order by random() limit ? ";
			values= new Object[]{3,1};
		}else if(type.equals("plane_ticket")){
			Long userCount =  daService.getLong( "select count(id) from flygame_pool_tb where ptype=?  and count=? ",new Object[]{1,1});
			int i = new Random().nextInt(2);
			if(userCount==null||userCount==0)
				i=1;
			//i=0;
			if(i==1){
				Long tcbcount = daService.getLong("select max(count) from flygame_pool_tb where ptype=? ", new Object[]{0});
				if(tcbcount<1){
					reloadTickets("plane_ticket");
					tcbcount=5000L;
				}
				int c = new Random().nextInt(tcbcount.intValue());
				if(c==0)
					c=1;
				logger.error(uin+",plane_ticket,tingchebao ticket,random:"+c);
				//System.err.println(tcbcount.intValue()+":"+c);
				sql = "select * from flygame_pool_tb where ptype =? and count>=? order by random() limit ?";
				values= new Object[]{0,c,1};
			}else {
				sql = "select * from flygame_pool_tb where ptype=? and count=?  order by random() limit ?";
				values= new Object[]{1,1,1};
			}
		}else if(type.equals("plane_cash")){
			Long allcount = daService.getLong("select max(count) from flygame_pool_tb where ptype=? ", new Object[]{2});
			if(allcount<1){
				reloadTickets("plane_cash");
				allcount=5000L;
			}
			int c = new Random().nextInt(allcount.intValue());
			if(c==0)
				c=1;
			logger.error(uin+",plane_cash,random:"+c);
			sql = "select * from flygame_pool_tb where ptype =? and count>=?  order by random() limit ?";
			values= new Object[]{2,c,1};
		}else if(type.equals("fly_bird")){
			//先查一下500张第二关的券有没有，没有时，就只选另两种福袋了
			count =0L;// daService.getLong("select count from flygame_pool_tb where ptype =?  ", new Object[]{6});
		/*	int [] random = new int[]{4,5,4,5,4,5,4,5,4,5,4,5,6};//4清空福袋 5翻倍福袋 6第二关入口
			int index = new Random().nextInt(13);//清空福袋 3/7  翻倍福袋 3/7 第二关入口1/7
			if(count==0){
				random = new int[]{4,4,5};
				index = new Random().nextInt(3);
			}
			int t = random[index];
			logger.error(uin+",fly_bird,type="+t+",4清空福袋 5翻倍福袋 6第二关入口");
			sql = "select * from flygame_pool_tb where ptype =? ";
			values= new Object[]{t};
			if(t!=6)//不是第二关入口时，查一个另两种福袋还有没有，如果没有，更新数量
				count = daService.getLong("select count from flygame_pool_tb where ptype =?  ", new Object[]{t});
			if(count<1){
				int r=daService.update("update flygame_pool_tb set count=? where ptype=? ",new Object[]{10000,t});
				logger.error(uin+",fly_bird,type="+t+",没有可选的了，更新记录ret:"+r);
			}*/
		}
		Map resultMap = null;
		if(type.equals("fly_bird")){
			Integer rand = new Random().nextInt(9);
			logger.error(type+":"+rand+",0-1清空,2翻倍,3子弹翻倍,4子弹减半,5积分翻倍,6积分减半,7弹弓满血,8弹弓血量减半");
			Integer ptype = 4;
			//rand:0-1清空,2翻倍,3子弹翻倍,4子弹减半,5积分翻倍,6积分减半,7弹弓满血,8弹弓血量减半
			if(rand==2)
				ptype = 5;
			else if(rand>2)
				ptype = rand+4;
			//ptype:4清空,5翻倍,7子弹翻倍,8子弹减半,9积分翻倍,10积分减半,11弹弓满血,12弹弓血量减半
			resultMap = ZldMap.getMap(new String[]{"ptype"}, new Object[]{ptype});
		}else {
			resultMap =  daService.getMap(sql, values);
		}
		return resultMap;
	}
	
	private void reloadTickets(String type){
		Long ntime =System.currentTimeMillis()/1000;
		if(type.equals("plane_ticket")){
			String sql = "insert into flygame_pool_tb(ptype,money,count,ctime) values(?,?,?,?) ";
			List<Object[]> values  =new ArrayList<Object[]>();
			values.add(new Object[]{0,1.0,50000,ntime});
			values.add(new Object[]{0,2.0,30000,ntime});
			values.add(new Object[]{0,3.0,10000,ntime});
			values.add(new Object[]{0,4.0,500,ntime});
			values.add(new Object[]{0,5.0,300,ntime});
			values.add(new Object[]{0,6.0,100,ntime});
			values.add(new Object[]{0,7.0,50,ntime});
			values.add(new Object[]{0,8.0,30,ntime});
			values.add(new Object[]{0,9.0,10,ntime});
			values.add(new Object[]{0,10.0,5,ntime});
			values.add(new Object[]{0,11.0,3,ntime});
			values.add(new Object[]{0,12.0,1,ntime});
			daService.bathInsert(sql, values, new int[]{4,3,4,4});
		}else if(type.equals("plane_cash")){
			String sql = "insert into flygame_pool_tb(ptype,money,count,ctime) values(?,?,?,?) ";
			List<Object[]> values  =new ArrayList<Object[]>();
			values.add(new Object[]{2,0.1,50000,ntime});
			values.add(new Object[]{2,0.2,30000,ntime});
			values.add(new Object[]{2,0.5,10000,ntime});
			values.add(new Object[]{2,1.0,5000,ntime});
			values.add(new Object[]{2,2.0,300,ntime});
			values.add(new Object[]{2,3.0,100,ntime});
			values.add(new Object[]{2,4.0,50,ntime});
			values.add(new Object[]{2,5.0,30,ntime});
			values.add(new Object[]{2,6.0,10,ntime});
			values.add(new Object[]{2,7.0,5,ntime});
			values.add(new Object[]{2,8.0,3,ntime});
			values.add(new Object[]{2,9.0,1,ntime});
			daService.bathInsert(sql, values, new int[]{4,3,4,4});
		}
	}
	
	private int play(HttpServletRequest request,Long uin) {
		request.setAttribute("uin", uin);
		Long tid = RequestUtil.getLong(request, "tid",-1L);
		request.setAttribute("tid", tid);
		String mesg = getCacheMesg(uin);
		request.setAttribute("messages", mesg);
		return 0;
	}
	
	private String friends(HttpServletRequest request,Long uin) {
		List<Map<String, Object>> fList = daService.getAll("select euin,u.wx_name,u.wx_imgurl from user_friend_tb f left join user_info_tb u on f.euin=u.id where buin=? ", new Object[]{uin});
		Map userMap = daService.getMap("select wx_name,wx_imgurl from user_info_tb where id =? ", new Object[]{uin});
		if(fList!=null)
			fList.add(0,userMap);
		else{
			fList = new ArrayList<Map<String,Object>>();
			fList.add(userMap);
		}
		if(fList!=null&&!fList.isEmpty()){
			for(Map<String, Object> map: fList){
				String url = (String)map.get("wx_imgurl");
				String name  = (String)map.get("wx_name");
				if(name!=null){
					name=name.replace("\'", "").replace("\"", "");
					map.put("wx_name",name);
				}
				if(url==null||"".equals(url))
					map.put("wx_imgurl", "images/bunusimg/defaulthead.png");
				if(name==null||"".equals(name)||"null".equals(name))
					map.put("wx_name", "停车宝车主");
				Long count = daService.getLong("select count(id) from user_liuyan_tb where fuin=? and tuin=?  and is_read=? ", new Object[]{map.get("euin"),uin,0});
				map.put("mesgs", count);
			}
		}
		return StringUtils.createJson(fList);
	}

	private String score(HttpServletRequest request,Long uin) {
		List<Map<String, Object>> list = daService.getAll("select f.id,f.ctime,f.remark,f.ptype,f.is_operate,f.money,t.aid " +
				" from flygame_score_tb f left join flygame_pool_tb t on f.fgid=t.id " +
				" where f.uin=? and f.ctime>? and f.ptype<>? order by f.id ", new Object[]{uin,TimeTools.getToDayBeginTime(),6});
		System.err.println(list);
		if(list!=null&&!list.isEmpty()){
			for(Map<String, Object> map : list){
				Long ctime =(Long)map.get("ctime");
				if(ctime!=null)
					map.put("ctime", TimeTools.getTime_yyyyMMdd_HHmm(ctime*1000).substring(5));
			}
		}
		Map userMap = daService.getMap("select wx_name,wx_imgurl from user_info_tb where id =? ", new Object[]{uin});
		String wxname = (String)userMap.get("wx_name");
		if(wxname!=null){
			wxname = wxname.replace("'", "").replace("\"", "");
			userMap.put("wx_name", wxname);
		}
		if(list!=null)
			list.add(0,userMap);
		else{
			list = new ArrayList<Map<String,Object>>();
			list.add(userMap);
		}
		Long count  = daService.getLong("select count(id) from flygame_score_tb where ptype=? and uin=? ", new Object[]{6,uin});
		if(count>0){
			Map<String, Object> secondCardMap = ZldMap.getMap(new String[]{"ptype","count","remark"}, new Object[]{6,count,"第二关入场券"});
			list.add(secondCardMap);
		}
		Map allScoreMap = daService.getMap("select sum(db_bullet_score) s1,sum(empty_bullet_score) s2,sum(gift_score) s3" +
				",sum(balance_score) s4,sum(ticket_score) s5,sum(second_score) s6,sum(cloud_score) s7,sum(crow_score) s8 ," +
				" sum(bets_double_score) s9,sum(score_double_score) s10,sum(catapult_full_score) s11, sum(catapult_halve_score) s12," +
				" sum(bets_halve_score) s13,sum(score_halve_score) s14,sum(float_score) s15 " +
				" from flygame_score_anlysis_tb where uin = ?",new Object[]{uin});
		Double allScoreDouble = 0.0;
		if(allScoreDouble!=null&&!allScoreMap.isEmpty()){
			allScoreDouble  = StringUtils.formatDouble(StringUtils.formatDouble(allScoreMap.get("s1"))+StringUtils.formatDouble(allScoreMap.get("s2"))+
					StringUtils.formatDouble(allScoreMap.get("s3"))+StringUtils.formatDouble(allScoreMap.get("s4"))+
					StringUtils.formatDouble(allScoreMap.get("s5"))+StringUtils.formatDouble(allScoreMap.get("s6"))+
					StringUtils.formatDouble(allScoreMap.get("s7"))+StringUtils.formatDouble(allScoreMap.get("s8"))+
					StringUtils.formatDouble(allScoreMap.get("s9"))+StringUtils.formatDouble(allScoreMap.get("s10"))+
					StringUtils.formatDouble(allScoreMap.get("s11"))+StringUtils.formatDouble(allScoreMap.get("s12"))+
					StringUtils.formatDouble(allScoreMap.get("s13"))+StringUtils.formatDouble(allScoreMap.get("s14"))+
					StringUtils.formatDouble(allScoreMap.get("s15")));
		}
		Map<String, Object> scoreMap = ZldMap.getMap(new String[]{"ptype","count","remark"}, new Object[]{20,allScoreDouble,"我的总积分"});
		list.add(scoreMap);
		System.out.println(list);
		return StringUtils.createJson(list);
	}

	private String getTickets(HttpServletRequest request) {
		Long uin = RequestUtil.getLong(request, "uin", -1L);
		if(uin!=-1){
			List ticketsList = daService.getAll("select  id,money,resources from ticket_tb where uin = ? and state =? and type=? and limit_day>=? order by money ",
					new Object[]{uin,0,0,TimeTools.getToDayBeginTime()});
			return StringUtils.createJson(ticketsList);
		}
		return "[]";
	}

	private String getMessage(HttpServletRequest request){
		Long fuin = RequestUtil.getLong(request, "fuin", -1L);
		Long tuin = RequestUtil.getLong(request, "tuin", -1L);
		Integer mtype = RequestUtil.getInteger(request, "mtype", 0);//0,查询全部消息，1查询是否有新消息
		logger.error("mtype:"+mtype);
		if(mtype==1){
			Long count = daService.getLong("select count(id) from user_liuyan_tb where fuin=? and tuin=?  and is_read=? ", new Object[]{tuin,fuin,0});
			if(count==0)
				return "[]";
					
		}
		List<Map<String, Object>> fList = daService.getAll("select * from user_liuyan_tb f  where (fuin=? and tuin=?)" +
				" or (fuin=? and tuin=?  ) order by id ", new Object[]{fuin,tuin,tuin,fuin});
		if(fList!=null){
			for(Map<String, Object> fMap : fList){
				String message = (String)fMap.get("message");//消息行数
				int length = message.getBytes().length;
				fMap.put("wleng",length);
				if(length%20==0)
					length = length/20;
				else
					length = length/20+1;
				fMap.put("leng",length);
			}
			List<Map<String, Object>> uList = daService.getAll("select id,wx_name,wx_imgurl from user_info_tb where id in(?,?)", new Object[]{fuin,tuin}); 
			if(uList!=null&&!uList.isEmpty()){
				for(Map<String, Object> map: uList){
					String url = (String)map.get("wx_imgurl");
					String name  = (String)map.get("wx_name");
					if(name!=null){
						name=name.replace("'", "").replace("\"", "");
						map.put("wx_name",name);
					}
					if(url==null||"".equals(url))
						map.put("wx_imgurl", "images/bunusimg/defaulthead.png");
					if(name==null||"".equals(name)||"null".equals(name))
						map.put("wx_name", "停车宝车主");
					fList.add(0,map);
				}
			}
			System.err.println(fList);
		}
		daService.update("update user_liuyan_tb set is_read=? where fuin=? and tuin=? and is_read=?", new Object[]{1,tuin,fuin,0});
		return StringUtils.createJson(fList);
	}
	/**
	 * @param uin 车主
	 * @param bets 子弹数
	 * @param tid 停车券编号
	 * @param type 0初始化缓存，1更新缓存，需要检查合法
	 */
	private int cacheBets(Long uin,Integer bets,Integer goldbets,Long tid){
		int r = new Random().nextInt(20);
		Map<Long, String> userBetMap = memcacheUtils.doMapLongStringCache("fly_game_bullets", null, null);
		if(userBetMap==null){
			userBetMap=new HashMap<Long, String>();
		}
		boolean isCanCached = true;
		/*if(type==1){
			String lastBets = userBetMap.get(uin);
			logger.error(uin+",tid:"+tid+",bets："+bets+",cache:"+lastBets);
			if(lastBets==null){
				isCanCached=false;
			}else if(lastBets.indexOf("_")!=-1){
				String prelbets = lastBets.split("_")[1];//普通子弹
				String pregbets = lastBets.split("_")[2];//金刚弹
				String pretid = lastBets.split("_")[0];
				if(Check.isNumber(prelbets)&&Check.isNumber(pretid)){
					int prelt = Integer.valueOf(prelbets);
					int pregt = Integer.valueOf(pregbets);
					Long pid = Long.valueOf(pretid);
					// 21990=33432_3_2_10
					if(!tid.equals(pid)||(bets>prelt&&goldbets>pregt)||bets>320){
						isCanCached=false;
						logger.error("flygame record bets error >>>>>>>error>>>>>调用错误,可能在非法调用，返回.... ,uin="+uin+",tid:"+tid+",bnums:"+bets);
					}
				}else {
					isCanCached=false;
				}
			}else {
				isCanCached = false;
			}
		}*/
		if(isCanCached){
			if(bets==0&&goldbets==0)
				userBetMap.remove(uin);
			else
				userBetMap.put(uin, tid+"_"+bets+"_"+goldbets+"_"+r);
			//logger.error(">>>uin："+uin+",tid:"+tid+",bets:"+bets);
			memcacheUtils.doMapLongStringCache("fly_game_bullets", userBetMap, "update");
		}
		return r;
	}
	
	private  String getThirdResult(Long uin,String url){
		String mobile= "";
		Map temMap  = daService.getMap("select mobile,car_number from user_info_tb u left join car_info_tb c on c.uin=u.id where u.id =? ", new Object[]{uin});
		String car_number="";
		if(temMap!=null){
			if(temMap.get("car_number")!=null){
				car_number=(String)temMap.get("car_number");
			}
			if(temMap.get("mobile")!=null){
				mobile=(String)temMap.get("mobile");
			}
		}
		String timeStr = TimeTools.getTimeYYYYMMDDHHMMSS();
		timeStr = "youle"+timeStr.substring(4,8);
		String params = "";
		try {
			//System.out.println(timeStr);
			params="phone="+mobile+"&carNum="+AjaxUtil.encodeUTF8(car_number)+"&sign="+StringUtils.MD5(StringUtils.MD5(mobile)+timeStr);
			if(!params.equals("")){
				String result = new HttpProxy().doGet(url+"?"+params);
				logger.error(">>>>thrid url"+url+"?"+params+" ,res:"+result);
				String state = JsonUtil.getJsonValue(result, "status");
				if(state.equals("0")){//领取成功
					JSONObject data = JsonUtil.getJsonObjValue(result, "data");
					String price = data.getString("price");
					return price;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		//{"status":0,"msg":"领取成功","data":{"price":"27.99"}}
		//{"status":"-2","msg":"已经领取"}
		//{"status":0,"msg":"领取成功","data":{"price":9}}
		return "";
		
	}
	//消息缓存
	private void cacheMessage(Long uin,String message,int type){
		Long time = System.currentTimeMillis()/1000;//20分钟前
		Long pretime =time-20*60;//20分钟前
		if(type==0){//车主缓存
			Map<Long, String> userCacheMap = memcacheUtils.doMapLongStringCache("flygame_usermesg_cache", null, null);
			if(userCacheMap==null){//缓存为空初始化并保存
				userCacheMap = new HashMap<Long, String>();
				userCacheMap.put(uin, time+"_"+message);
				memcacheUtils.doMapLongStringCache("flygame_usermesg_cache", userCacheMap, "update");
			}else {//缓存不为空时，查时间大于20分钟的删除，同时保存车主消息
				Map<Long, String> tempCacheMap= new HashMap<Long, String>();
				for(Long u : userCacheMap.keySet()){
					String mesg = userCacheMap.get(u);
					Long t = Long.valueOf(mesg.split("_")[0]);
					if(t>pretime)//只留小于20分钟的消息
						tempCacheMap.put(u,mesg);
				}
				tempCacheMap.put(uin, time+"_"+message);
				memcacheUtils.doMapLongStringCache("flygame_usermesg_cache", tempCacheMap, "update");
				userCacheMap = null;
			}
		}else if(type==1){//公众缓存
			Map<Long, String> publicCacheMap = memcacheUtils.doMapLongStringCache("flygame_publicmesg_cache", null, null);
//			publicCacheMap = null;
			if(publicCacheMap==null){//缓存为空初始化并保存
				publicCacheMap = new HashMap<Long, String>();
				publicCacheMap.put(time, message);
				memcacheUtils.doMapLongStringCache("flygame_publicmesg_cache", publicCacheMap, "update");
			}else {//缓存不为空时，查时间大于20分钟的删除，同时保存公众消息
				Map<Long, String> tempCacheMap= new HashMap<Long, String>();
				for(Long t : publicCacheMap.keySet()){
					if(t>pretime)//只留小于20分钟的消息
						tempCacheMap.put(t,publicCacheMap.get(t));
				}
				tempCacheMap.put(time,message);
				memcacheUtils.doMapLongStringCache("flygame_publicmesg_cache", tempCacheMap, "update");
				publicCacheMap = null;
			}
		}
	}
	//加好机友消息
	private void sendWeiXinMesg(String myName,Long touin,String title,String mesg,String remark){
		Map userMap = daService.getMap("select wx_name,wxp_openid from user_info_tb where id=?  ", new Object[]{touin});
		String openid = null;
		String wxName = myName;
		if(userMap!=null){
			openid = (String)userMap.get("wxp_openid");
			//wxName = (String)userMap.get("wx_name");
		}
		if(openid!=null&&!openid.equals("")){
			Map<String, String> baseinfo = new HashMap<String, String>();
			//String url = "http://s.tingchebao.com/zld/flygame.do?action=pregame&agin=mesg&touin="+uin;
			String url = "http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/flygame.do?action=pregame&agin=friend&uin="+touin;
			
			baseinfo.put("url", url);
			baseinfo.put("openid", openid);
			baseinfo.put("top_color", "#000000");
			baseinfo.put("templeteid", Constants.WXPUBLIC_FLYGMAMEMESG_ID);
			
			List<Map<String, String>> orderinfo = new ArrayList<Map<String,String>>();
			Map<String, String> first = new HashMap<String, String>();
			first.put("keyword", "first");//标题 车主XX打中了你的灰机。
			first.put("value", title);
			first.put("color", "#000000");
			
			Map<String, String> keyword1 = new HashMap<String, String>();
			keyword1.put("keyword", "keyword1");//来源
			keyword1.put("value", "打灰机");
			keyword1.put("color", "#000000");
			
			Map<String, String> keyword2 = new HashMap<String, String>();
			keyword2.put("keyword", "keyword2");//姓名 
			keyword2.put("value", wxName);
			keyword2.put("color", "#000000");
			
			Map<String, String> keyword3 = new HashMap<String, String>();
			keyword3.put("keyword", "keyword3");//时间
			keyword3.put("value", TimeTools.getTime_yyyyMMdd_HHmm(System.currentTimeMillis()));
			keyword3.put("color", "#000000");
			
			Map<String, String> keyword4 = new HashMap<String, String>();
			keyword4.put("keyword", "keyword4");//留言
			keyword4.put("value", mesg);//"我打中了你放飞的3元停车券灰机，成为了你的好机友。");
			keyword4.put("color", "#000000");
			
			Map<String, String> remarks = new HashMap<String, String>();
			remarks.put("keyword", "remark");//点击本消息进行操作
			remarks.put("value", remark);//查看我的好机友);
			remarks.put("color", "#000000");
			
			orderinfo.add(first);
			orderinfo.add(keyword1);
			orderinfo.add(keyword2);
			orderinfo.add(keyword3);
			orderinfo.add(keyword4);
			orderinfo.add(remarks);
			
			try {
				publicMethods.sendWXTempleteMsg(baseinfo, orderinfo);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	//留言消息
	private void sendLeaveWeiXinMesg(String myName,Long fuin,Long touin,String title,String remark){
		Map userMap = daService.getMap("select wx_name,wxp_openid from user_info_tb where id=?  ", new Object[]{touin});
		String openid = null;
		String wxName = myName;
		if(userMap!=null){
			openid = (String)userMap.get("wxp_openid");
			//wxName = (String)userMap.get("wx_name");
		}
		if(openid!=null&&!openid.equals("")){
			Map<String, String> baseinfo = new HashMap<String, String>();
			//String url = "http://s.tingchebao.com/zld/flygame.do?action=pregame&agin=mesg&touin="+uin;
			String url = "http://"+Constants.WXPUBLIC_REDIRECTURL+"/zld/flygame.do?action=pregame&agin=leavemesg&uin="+touin+"&touin="+fuin;
			
			baseinfo.put("url", url);
			baseinfo.put("openid", openid);
			baseinfo.put("top_color", "#000000");
			baseinfo.put("templeteid", Constants.WXPUBLIC_LEAVE_MESG_ID);
			
			List<Map<String, String>> orderinfo = new ArrayList<Map<String,String>>();
			Map<String, String> first = new HashMap<String, String>();
			first.put("keyword", "first");//标题 车主XX打中了你的灰机。
			first.put("value", title);
			first.put("color", "#000000");
			
			Map<String, String> keyword1 = new HashMap<String, String>();
			keyword1.put("keyword", "keynote1");//姓名 
			keyword1.put("value", wxName);
			keyword1.put("color", "#000000");
			
			Map<String, String> keyword2 = new HashMap<String, String>();
			keyword2.put("keyword", "keynote2");//时间
			keyword2.put("value", TimeTools.getTime_yyyyMMdd_HHmm(System.currentTimeMillis()));
			keyword2.put("color", "#000000");
			
			Map<String, String> remarks = new HashMap<String, String>();
			remarks.put("keyword", "remark");//点击本消息进行操作
			remarks.put("value", "留言内容："+remark);//留言内容
			remarks.put("color", "#000000");
			
			orderinfo.add(first);
			orderinfo.add(keyword1);
			orderinfo.add(keyword2);
			orderinfo.add(remarks);
			
			try {
				publicMethods.sendWXTempleteMsg(baseinfo, orderinfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 调用微信接口，取用户的openid
	 * @param request
	 * @return [opedid,access_token]
	 */
	private String[] getOpenid(HttpServletRequest request){
		String code = RequestUtil.processParams(request, "code");
		logger.error(">>>>>>>>code:"+code+",comfig appid:");
		if(code==null||"".equals(code))
			return null;
		String appid = Constants.WXPUBLIC_APPID;
		String secret=Constants.WXPUBLIC_SECRET;
		String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appid+"&secret="+secret+"&code="+code+"&grant_type=authorization_code";
		logger.error(">>>>>>>>access_token_url:"+access_token_url);
		String result = CommonUtil.httpsRequest(access_token_url, "GET", null);
		net.sf.json.JSONObject map =null;
		if(result!=null){
			map = net.sf.json.JSONObject.fromObject(result);
		}
		if(map == null || map.get("errcode") != null){
			return null;
		}
		String openid = (String)map.get("openid");
		String accToken = (String)map.get("access_token");
		logger.error(">>>>>>>>return map :"+map);
		return new String[]{openid,accToken};
	}
	/**
	 * 根据openid 查用户手机
	 * @param openid
	 * @return
	 */
	private Map getUserByOpenid(String openid){
		Map<String, Object> userMap= null;
		if(openid!=null&&!openid.equals("")){
			userMap = daService.getMap("select id, mobile,wx_name,wx_imgurl from user_info_tb where state=? and auth_flag=? and wxp_openid=? ",
							new Object[] { 0, 4, openid });
		}
		return userMap;
	}
}
