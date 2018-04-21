package com.zldpark.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.zldpark.impl.CommonMethods;
import com.zldpark.service.DataBaseService;
import com.zldpark.service.PgOnlyReadService;
import com.zldpark.utils.Constants;
import com.zldpark.utils.HttpProxy;
import com.zldpark.utils.MemcacheUtils;
import com.zldpark.utils.StringUtils;
import com.zldpark.utils.TimeTools;

public class ParkSchedule implements Runnable {
	
	private DataBaseService dataBaseService;
	private PgOnlyReadService pgOnlyReadService;
	private MemcacheUtils memcacheUtils;
	
	public ParkSchedule(DataBaseService dataBaseService, PgOnlyReadService pgOnlyReadService,
			MemcacheUtils memcacheUtils, CommonMethods commonMethods){
		this.dataBaseService = dataBaseService;
		this.pgOnlyReadService = pgOnlyReadService;
		this.memcacheUtils = memcacheUtils;
	}

	private static Logger log = Logger.getLogger(ParkSchedule.class);

	@Override
	public void run() {
		// TODO Auto-generated method stub

		log.error("********************开始1天一次的定时任务***********************");
		//一天统计一次
		//获取今天的开始时间
		Long todaybeigintime = TimeTools.getToDayBeginTime();
		try {
			mobilePayStart(todaybeigintime);
			//统计注册量
			registerStart(todaybeigintime);
			//手机管理近三日订单统计
			order3Start(todaybeigintime);
			//直付统计
			directPayStart(todaybeigintime);
			//统计礼包
			anlysisBonus(todaybeigintime);
			//微信支付宝交易新增用户统计
			consumeStart(todaybeigintime);
			//购买的停车券到期退款
			backTicketMoney();
			//根据车场每天得出单量计算补贴额
			allowanceByPark(todaybeigintime);
			
			anlyCharge(todaybeigintime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.error("********************结束1天一次的定时任务***********************");
	}
	
	
	
	private void allowanceByPark(Long time){
		try {
			log.error("allowanceByPark>>>开始根据订单量计算明天车场补贴额>>>");
			Map<Long, String> limitMap = new HashMap<Long, String>();
			Long allcount = 0L;
			List<Map<String, Object>> list = pgOnlyReadService
					.getAll("select count(id) ocount,comid from order_tb where state=? and pay_type=? and comid>? and end_time between ? and ? group by comid ",
							new Object[] { 1, 2, 0, time - 24*60*60, time });
			if(list != null && !list.isEmpty()){
				for(Map<String, Object> map : list){
					Long count = (Long)map.get("ocount");
					allcount += count;
				}
				log.error("allowanceByPark>>>昨天总单量allcount:"+allcount);
				if(allcount > 0){
					Double allowance = StringUtils.formatDouble(getAllowance(time));//3000d;
					log.error("allowanceByPark>>>今日补贴总额度:"+allowance);
//				if(allowance<1000||allowance>3000)
//					allowance=1000d;
//				log.error("allowanceByPark>>>今日补贴总额度（实际）:"+allowance);
					for(Map<String, Object> map : list){
						Long comid = (Long)map.get("comid");
						Long count = (Long)map.get("ocount");
						Double limit = StringUtils.formatDouble(((double)count/(double)allcount) * allowance);
						limitMap.put(comid, limit + "");
					}
				}
			}
			log.error("allowanceByPark>>>limitMap:"+limitMap.size());
			memcacheUtils.doMapLongStringCache("allow_park_limit", limitMap, "update");
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	//2015-11-05 开始，每天减100,到100停止
	private Double getAllowance(Long time) {
		Long baseTime = 1446652800L;//2015-11-05
		Long abs = time-baseTime;
		Long t  = abs/(24*60*60);
		log.error(">>>>>补贴递减100的倍数："+t);
		if(t>0){
			Double retDouble= 500d-t*100;
			if(retDouble<0d)
				retDouble=0d;
			return retDouble;
		}
		return 100.0;
	}
	private void anlyFlyGameScore(Long ntime){
		log.error(">>>>>>>>开妈游戏积分统计......");
		Long btime =ntime  - 24*60*60;//分析一天的游戏战绩
		List allList = pgOnlyReadService.getAll("select * from flygame_score_tb where ctime between ? and ?", new Object[]{btime,ntime});
		if(allList!=null&&!allList.isEmpty()){
			Map<Long,Map<Integer,Map<String, Double>>> alldataMap = new HashMap<Long,Map<Integer, Map<String,Double>>>();
			for(int i=0;i<allList.size();i++){
				Map<String,Object> map =(Map) allList.get(i);
				Long uin = (Long)map.get("uin");
				Map<Integer,Map<String, Double>> dataMap = alldataMap.get(uin);
				if(dataMap==null){
					dataMap=new HashMap<Integer, Map<String,Double>>();
					for(int j=1;j<7;j++){
						Map<String, Double> m = new HashMap<String, Double>();
						m.put("score", 0.0);
						m.put("count", 0.0);
						dataMap.put(j, m);
					}
					alldataMap.put(uin, dataMap);
				}
				Double money =StringUtils.formatDouble(map.get("money"));
				//0停车宝停车券 1车主停车券 2余额券 3广告券 4清空福袋 5翻倍福袋
				Integer ptype = (Integer)map.get("ptype");
				if(ptype==0)
					ptype=1;
				Map<String, Double> sMap = dataMap.get(ptype);
				Double score = sMap.get("score");
				if(ptype==1){
					score +=money*0.1;
				}else if(ptype==2){
					score +=money*0.5;
				}else if(ptype==3){
					score +=0.3;
				}else if(ptype==4){
					score +=-1;
				}else if(ptype==5){
					score +=1;
				}else if(ptype==6){
					score +=2;
				}
				sMap.put("score", StringUtils.formatDouble(score));
				sMap.put("count", sMap.get("count")+1);
			}
			//写入数据库
			if(!alldataMap.isEmpty()){
				String sql ="insert into flygame_score_anlysis_tb(uin,ctime,db_bullet_count,db_bullet_score,empty_bullet_count,empty_bullet_score," +
						"gift_count,gift_score,balance_count,balance_score,ticket_count,ticket_score,second_count,second_score) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				List<Object[]> values = new ArrayList<Object[]>();
				for(Long uin: alldataMap.keySet()){
					//0停车宝停车券 1车主停车券 2余额券 3广告券 4清空福袋 5翻倍福袋
					Map<Integer,Map<String, Double>> dataMap = alldataMap.get(uin);
					values.add(new Object[]{uin,ntime,dataMap.get(5).get("count").intValue(),dataMap.get(5).get("score")
							,dataMap.get(4).get("count").intValue(),dataMap.get(4).get("score")
							,dataMap.get(3).get("count").intValue(),dataMap.get(3).get("score")
							,dataMap.get(2).get("count").intValue(),dataMap.get(2).get("score")
							,dataMap.get(1).get("count").intValue(),dataMap.get(1).get("score")
							,dataMap.get(6).get("count").intValue(),dataMap.get(6).get("score")});
				}
				if(!values.isEmpty()){
					int ret = dataBaseService.bathInsert(sql, values, new int[]{4,4,4,3,4,3,4,3,4,3,4,3,4,3});
					log.error(">>>>>>>>游戏积分统计完毕，共插入"+ret+"条");
				}
			}
		}
	}
	
	private void backTicketMoney() {
		try {
			log.error("处理购买停车券过期退款.....");
			List allList = pgOnlyReadService.getAll("select * from ticket_tb where limit_day<? and state=? and resources=? and is_back_money=? ",
					new Object[]{System.currentTimeMillis()/1000,0,1,0});
			String sql = "insert into user_account_tb(uin,amount,type,create_time,remark,pay_type,target) values(?,?,?,?,?,?,?)";
			List<Object[]> values = new ArrayList<Object[]>();
			//每个车主的退款总金额
			Map<Long, Double> uinMoneyMap = new HashMap<Long, Double>();
			Map<Long, Integer> uinMoneyCount = new HashMap<Long, Integer>();
			if(allList!=null&&!allList.isEmpty()){
				log.error("退款："+allList);
				for(int i=0;i<allList.size();i++){
					Map map = (Map)allList.get(i);
					Long uin =(Long)map.get("uin");
					Double money = StringUtils.formatDouble(map.get("pmoney"));
					if(money==null||money==0)
						continue;
					if(uinMoneyMap.containsKey(uin)){
						uinMoneyMap.put(uin, uinMoneyMap.get(uin)+money);
						uinMoneyCount.put(uin, uinMoneyCount.get(uin)+1);
					}else {
						uinMoneyMap.put(uin, money);
						uinMoneyCount.put(uin, 1);
					}
					values.add(new Object[]{uin,money,0,System.currentTimeMillis()/1000+i,"停车券退款",13,3});
				}
				if(values.size()>0){
					int r = dataBaseService.bathInsert(sql, values, new int[]{4,3,4,4,12,4,4});
					if(r>0){
						log.error(">>>>>>>退款：写入账户明细："+r+"条");
						sql = "update user_info_Tb set balance =balance+? where id=?";
						values.clear();
						log.error("退款：写入账户,"+uinMoneyMap);
						for(Long uLong : uinMoneyMap.keySet()){
							values.add(new Object[]{uinMoneyMap.get(uLong),uLong});
						}
						if(values.size()>0){
							r = dataBaseService.bathInsert(sql, values, new int[]{3,4});
							log.error(">>>>>>>>>>退款：写入账户："+r+"条");
							if(r>0){
								// is_back_money integer DEFAULT 0, -- 车主购买的停车券，未使用过期退款，0未退款，1已退款
								r = dataBaseService.update("update ticket_tb set is_back_money=? where  limit_day<? and state=? and resources=? and is_back_money=? ",
										new Object[]{1,System.currentTimeMillis()/1000,0,1,0});
								log.error(">>>>>>>>>>退款：更新停车券状态："+r+"条");
								for(Long uLong : uinMoneyMap.keySet()){
									log.error(">>>>>>发公众号消息给："+uLong);
									sendMesgToWeixin(uLong, StringUtils.formatDouble(uinMoneyMap.get(uLong)),uinMoneyCount.get(uLong));
									int ret = dataBaseService.update("insert into user_message_tb (type,ctime,content,uin,title) values (?,?,?,?,?)",
											new Object[]{9,System.currentTimeMillis()/1000,"您有"+uinMoneyCount.get(uLong)+"张购买的停车券到期，相应的退款金额"+uinMoneyMap.get(uLong)+"元已经退款到您的账户余额，注意查收。",uLong,"购买的停车券过期，获得相应退款"});
									log.error(">>>>>>写车主消息表："+uLong);
								}
							}
						}
					}
				}
			}else {
				log.error("退款数：0");
			}
			log.error("处理购买停车券过期退款结束.....");
		} catch (Exception e) {
			log.error(e);
		}
	}
	/**
	 * 发送退款消息到公众号
	 * @param uin
	 * @param openid
	 * @param back
	 */
	private void sendMesgToWeixin(Long uin,Double back,Integer count){
		String openid ="";
		Map userMap = pgOnlyReadService.getMap("select wxp_openid from user_info_tb where id =? ",new Object[]{uin});
		if(userMap!=null&&!userMap.isEmpty())
			openid = (String)userMap.get("wxp_openid");
		try {
			if(openid!=null&&!openid.equals("")){
				log.error(">>>>>>>>>>>预支付后现金结算订单退回预支付款   微信推消息,uin:"+uin+",openid:"+openid);
				String first = "购买的停车券到期退款";
				Map<String, String> baseinfo = new HashMap<String, String>();
				List<Map<String, String>> orderinfo = new ArrayList<Map<String,String>>();
				String url = "http://s.tingchebao.com/zld/wxpaccount.do?action=balance&openid="+openid;
				baseinfo.put("url", url);
				baseinfo.put("openid", openid);
				baseinfo.put("top_color", "#000000");
				baseinfo.put("templeteid",Constants.WXPUBLIC_BACK_NOTIFYMSG_ID);
				Map<String, String> keyword1 = new HashMap<String, String>();
				keyword1.put("keyword", "orderProductPrice");
				keyword1.put("value",count+"张，共"+back+"元");
				keyword1.put("color", "#000000");
				orderinfo.add(keyword1);
				Map<String, String> keyword2 = new HashMap<String, String>();
				keyword2.put("keyword", "orderProductName");
				keyword2.put("value", "停车券到期退款");
				keyword2.put("color", "#000000");
				orderinfo.add(keyword2);
				Map<String, String> keyword3 = new HashMap<String, String>();
				keyword3.put("keyword", "orderName");
				keyword3.put("value", "");
				keyword3.put("color", "#000000");
				orderinfo.add(keyword3);
				Map<String, String> keyword4 = new HashMap<String, String>();
				keyword4.put("keyword", "Remark");
				keyword4.put("value", "点击详情查账户余额！");
				keyword4.put("color", "#000000");
				orderinfo.add(keyword4);
				Map<String, String> keyword5 = new HashMap<String, String>();
				keyword5.put("keyword", "first");
				keyword5.put("value", first);
				keyword5.put("color", "#000000");
				orderinfo.add(keyword5);
				StringUtils.sendWXTempleteMsg(baseinfo, orderinfo,getWXPAccessToken());
			}
		} catch (Exception e) {
			log.error("退回成功，消息发送失败");
			e.printStackTrace();
		}
	}
	
	public  String getWXPAccessToken(){
		String access_token = memcacheUtils.getWXPublicToken();
		if(access_token.equals("notoken")){
			String url = Constants.WXPUBLIC_GETTOKEN_URL;
			//从weixin接口取access_token
			String result = new HttpProxy().doGet(url);
			log.error("wxpublic_access_token json:"+result);
			access_token =StringUtils.getJsonValue(result, "access_token");//result.substring(17,result.indexOf(",")-1);
			log.error("wxpublic_access_token:"+access_token);
			//保存到缓存 
			memcacheUtils.setWXPublicToken(access_token);
		}
		log.error("微信公众号access_token："+access_token);
		return access_token;
	}
	/*
	 * 每周一，积分不足1000的收费员积分置成1000
	 */
	private void resetRewardScore(Long createtime){
		log.error("reset rewardscore to 1000 every monday, write detail>>>begining.......");
		List<Map<String, Object>> rewardscoreList = pgOnlyReadService
				.getAll("select id,reward_score from user_info_tb where reward_score<? and state=? and (auth_flag=? or auth_flag=?) ",
						new Object[] { 1000, 0, 1, 2 });
		if(rewardscoreList != null){
			String sql = "insert into reward_account_tb(uin,score,type,create_time,remark,target) values (?,?,?,?,?,?)";
			String sql1 = "update user_info_tb set reward_score=? where id=? ";
			List<Object[]> values = new ArrayList<Object[]>();
			List<Object[]> values1 = new ArrayList<Object[]>();
			for(Map<String, Object> map : rewardscoreList){
				Long uin = (Long)map.get("id");
				Double rewardscore = Double.valueOf(map.get("reward_score") + "");
				Double addscore = com.zldpark.utils.StringUtils.formatDouble(1000 - rewardscore);
				Object[] va = new Object[6];
				Object[] va1 = new Object[2];
				va[0] = uin;
				va[1] = addscore;
				va[2] = 0;
				va[3] = createtime;
				va[4] = "停车宝充值"+addscore+"积分";
				va[5] = 3;
				values.add(va);
				
				va1[0] = 1000d;
				va1[1] = uin;
				values1.add(va1);
			}
			dataBaseService.bathInsert(sql, values, new int []{4,3,4,4,12,4});
			dataBaseService.bathInsert(sql1, values1, new int []{3,4});
			log.error("reset rewardscore to 1000 every monday, write detail>>>end:"+rewardscoreList.size());
		}
	}
	
	/*
	 * 开始统计
	 */
	private void consumeStart(Long nextTime){
		try {
			int consumecount = dataBaseService.update("delete from consume_anlysis_tb where create_time=? ",
					new Object[]{nextTime - 24*60*60});
			log.error("删除微信支付宝交易新增用户统计==="+consumecount+"条");
			System.err.println("开始微信支付宝新增用户统计："+TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis()));
			List<Object> wxuins = new ArrayList<Object>();
			List<Object> wxpuins = new ArrayList<Object>();
			List<Object> zfbuins = new ArrayList<Object>();
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			list = pgOnlyReadService.getAll("select uin,pay_type from user_account_tb where type=? and (pay_type=? or pay_type=? or pay_type=?) group by uin,pay_type order by pay_type desc ",
					new Object[]{0,1,2,9});
			for(Map<String, Object> map : list){
				Integer pay_type = (Integer)map.get("pay_type");
				Long uin = (Long)map.get("uin");
				if(pay_type == 9){//微信公众号用户
					wxpuins.add(uin);
				}else if(pay_type == 2){//微信充值用户
					wxuins.add(uin);
				}else if(pay_type == 1){//支付宝充值用户
					zfbuins.add(uin);
				}
			}
			List<Map<String, Object>> list2 = new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> list3 = new ArrayList<Map<String,Object>>();
			List<Object> duins = new ArrayList<Object>();
			List<Object> ouins = new ArrayList<Object>();
			String sql1 = "select uin,min(create_time) mintime from user_account_tb where type=? and uid>? group by uin order by mintime desc ";
			String sql2 = "select uin,min(end_time) mintime from order_tb where pay_type=? and state=? group by uin order by mintime desc ";
			list2 = pgOnlyReadService.getAll(sql1, new Object[]{1,0});
			for(Map<String, Object> map : list2){
				Long mintime = (Long)map.get("mintime");
				Long uin = (Long)map.get("uin");
				if(mintime > (nextTime - 24*60*60)){
					duins.add(uin);
				}else{
					break;
				}
			}
			list3 = pgOnlyReadService.getAll(sql2, new Object[]{2,1});
			for(Map<String, Object> map : list3){
				Long mintime = (Long)map.get("mintime");
				Long uin = (Long)map.get("uin");
				if(mintime > (nextTime - 24*60*60)){
					ouins.add(uin);
				}else{
					break;
				}
			}
			List<Object> newwx = new ArrayList<Object>();
			List<Object> newwxp = new ArrayList<Object>();
			List<Object> newzfb = new ArrayList<Object>();
			for(Object object : duins){
				if(wxuins.contains(object)){
					newwx.add(object);
				}
				if(wxpuins.contains(object)){
					newwxp.add(object);
				}
				if(zfbuins.contains(object)){
					newzfb.add(object);
				}
			}
			for(Object object : ouins){
				if(wxuins.contains(object) && !newwx.contains(object)){
					newwx.add(object);
				}
				if(wxpuins.contains(object) && !newwxp.contains(object)){
					newwxp.add(object);
				}
				if(zfbuins.contains(object) && !newzfb.contains(object)){
					newzfb.add(object);
				}
			}
			int wxcount = newwx.size();
			int zfbcount = newzfb.size();
			int wxpcount = newwxp.size();
			
			log.error("微信支付宝新增用户统计开始写库...共1条");
			System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"微信支付宝新增用户统计开始写库...共1条");
			String sql = "insert into consume_anlysis_tb(create_time,wx_total,zfb_total,wxp_total) values(?,?,?,?)";
			dataBaseService.update(sql, new Object[]{nextTime - 24*60*60,wxcount,zfbcount,wxpcount});
			System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"微信支付宝新增用户统计写库完成...");
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	/**
	 * 统计红包转化
	 * @param todaybeigintime
	 */
	private void anlysisBonus(Long todaybeigintime) {
		try {
			int bonuscount = dataBaseService.update("delete from reg_anlysis_tb where ctime=?", new Object[]{todaybeigintime-24*60*60});
			log.error("删除红包统计数据==="+bonuscount+"条");
			/**
			 *   id bigint NOT NULL,
			  bonus_num integer, -- 红包数
			  reg_num integer, -- 注册数（车牌有效）
			  amount integer, -- 金额
			  pv_number integer, -- pv数
			  hit_number integer, -- 点击数
			  down_num integer, -- 下载数
			  ctime bigint, -- 日期
			  atype integer, 1今日头条，2传单红包，3节日红包，998直付红包，999收费员推荐，1000交易红包
			  order_num integer, -- 产生消费数
			  --红包统计
				select * from user_account_tb where uin in( --消费情况
				select id from user_info_tb where mobile in( --注册用户
				select mobile from bonus_record_tb where ctime between 1420560000 and 1420646400) --产生红包数
				and reg_time  between 1420560000 and 1420646400 and auth_flag=4  )
			 */
			log.error("删除红包统计数据==="+bonuscount+"条");
			//1今日红包数：
			List<Map<String, Object>> bList = pgOnlyReadService.getAll("select count(id) count,bid from bonus_record_tb where ctime between ? and ? group by bid ",
					new Object[]{todaybeigintime-24*60*60,todaybeigintime}) ;
			/*1.8
			 * 1;5155
			2;5432
			418;1
			25;2
			 */
			log.error(bList);
			//2注册数（有效车牌）:
			List<Map<String, Object>> rList = pgOnlyReadService.getAll("select count(ID) count,media from user_info_tb where id in(select uin from " +
					" car_info_tb) and  reg_time  between ? and ? and auth_flag=? and media>?  group by media ", 
					new Object[]{todaybeigintime-24*60*60,todaybeigintime,4,0});
			/*1.8
			 * 15;999
			83;1
			9;2
			2;1000
			 */
			log.error(rList);
			
			
			//分析并写入数据  1000={}
			Map<String, List<Long>> dataMap = new HashMap<String, List<Long>>();
			//分析红包数量
			if(bList!=null&&!bList.isEmpty()){
				for(Map<String, Object> map : bList){
					Long bid = (Long)map.get("bid");
					if(bid!=null&&bid>999){
						bid = 1000L;
					}else if(bid==null)
						continue;
					Long count = (Long)map.get("count");
					if(bid!=null){
						if(dataMap.containsKey(bid+"")){
							List<Long> dList = dataMap.get(bid+"");
							Long cLong = dList.get(0);
							//log.error(cLong);
							cLong = cLong+count;
							//log.error(dList);
							dList.remove(0);
							dList.add(cLong);
							//dataMap.put(bid+"",dList);
						}else {
							List<Long> dList = new ArrayList<Long>();
							dList.add(count);
							dataMap.put(bid+"", dList);
						}
					}
				}
			}
			log.error(dataMap);
			//分析注册数量 
			if(rList!=null&&!rList.isEmpty()){
				for(Map<String,Object> map : rList){
					Integer media = (Integer)map.get("media");
					if(media==null)
						continue;
					Long count = (Long)map.get("count");
					String mkey = media+"";
					if(dataMap.containsKey(mkey)){
						List<Long> dList = dataMap.get(mkey);
						dList.add(count);
					}else {
						List<Long> dList = new ArrayList<Long>();
						dList.add(0L);
						dList.add(count);
						dataMap.put(mkey, dList);
					}
				}
				for(String key : dataMap.keySet()){
					List<Long> vList = dataMap.get(key);
					if(vList.size()==1)
						vList.add(0L);
					dataMap.put(key, vList);
				}
			}
			
			log.error(dataMap);
			//3当日产生订单数
			if(dataMap!=null&&dataMap.size()>0){
				String sql = "insert into reg_anlysis_tb (bonus_num,reg_num,order_num,atype,ctime) values(?,?,?,?,?)";
				List<Object[]> values = new ArrayList<Object[]>();
				for(String key : dataMap.keySet()){
					Long ocount = pgOnlyReadService.getLong("select count(distinct uin) from user_account_tb where uin in(select id from user_info_Tb " +
							"where reg_time  between ?  and ? and auth_flag=? and media=? ) and type=? and remark like ? and create_time between ? and ? ", 
							new Object[]{todaybeigintime-24*60*60,todaybeigintime,4,Integer.valueOf(key),1,"停车费%",todaybeigintime-24*60*60,todaybeigintime});
					List<Long> vlList = dataMap.get(key);
					Object[] valuObjects = new Object[]{vlList.get(0),vlList.get(1),ocount,Integer.valueOf(key),todaybeigintime-24*60*60};
					values.add(valuObjects);
					System.out.println("sql="+sql+",params:"+objArry2String(valuObjects));
				}
				//写入数据库
				if(values.size()>0){
					int ret = dataBaseService.bathInsert(sql, values, new int[]{4,4,4,4,4});
					log.error("红包统计数据结束，共生成==="+ret+"条");
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		
	}
	public  String objArry2String(Object[] values){
		StringBuffer rBuffer = new StringBuffer();
		if(values!=null&&values.length>0){
			for(Object o : values){
				rBuffer.append(o+",");
			}
		}
		return rBuffer.toString();
	}
	//每15分钟时刻开始
	private Long getTime (){
		Long time = System.currentTimeMillis()/1000;
		time = time -time%(15*60) ;
		return time;
	}
	
	//每10分钟开始
	private Long gethTime(){
		Long time = System.currentTimeMillis()/1000;
		time = time -time%(10*60) ;
		return time;
	}
	
	/*
	 * 开始统计
	 */
	private void hasparkerStart(Long nextTime){
		System.err.println("开始收费员在岗可支付车场统计："+TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis()));
		Long count = pgOnlyReadService.getLong("select count(*) total from com_info_tb where epay=? and is_hasparker=?",
				new Object[]{1,1});
		String sql = "insert into hasparker_anlysis_tb(anlysis_time,total) values (?,?)";
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"收费员在岗可支付车场统计开始写库...");
		dataBaseService.update(sql, new Object[]{nextTime,count});
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"收费员在岗可支付车场统计写库完成...");
	}
	
	/*
	 * 统计收费员在线情况,在岗积分
	 * @param nextTime
	 */
	
	private void anlysisParkerOnline(Long ntime){
		try {
			//心跳集合中
			log.error(">>>>开始查询所有收费员和巡查员....");
			List<Map<String,Object>> uinMap = pgOnlyReadService.getAll("select id from user_info_tb where auth_flag in(?,?,?)", 
					new Object[]{1,2,16});
			List<Long> uinList = new ArrayList<Long>();
			if(uinMap != null && !uinMap.isEmpty()){
				for(Map<String,Object> uMap : uinMap){
					uinList.add((Long)uMap.get("id"));
				}
			}
			Map<Long , Long> userMapCache = memcacheUtils.readParkerTokentimCache(uinList);
			Map<Long , Long> userMap = new HashMap<Long, Long>();
			//过滤掉心跳时间超过10分钟的收费员
			if(userMapCache != null && !userMapCache.isEmpty()){
				for(Long key : userMapCache.keySet()){
					if(userMapCache.get(key) > ntime - 10*60){
						userMap.put(key,userMapCache.get(key));
					}
				}
			}
			log.error(">>>>>>当前在线人数："+userMap.size());
			//缓存中小于10分钟在位的收费员，认为是在线的收费员。其它可以认为是已下线且离岗。
			//查询上传过经纬度的收费员，在 用户上传地点表中
			String sql ="select uid ,is_onseat ,max(ctime) ctime from user_local_tb group by uid ,is_onseat order by uid";
			
			List<Map<String,Object>>  uidList = dataBaseService.getAll(sql,null);
			log.error(">>>>上传经纬度大小 ："+uidList.size());
			
			Map<Long, String> uList = new HashMap<Long, String>();
			for(Map<String,Object> map : uidList){
				Long uid = (Long)map.get("uid");
				Integer isOnseat=(Integer)map.get("is_onseat");
				Long ctime = (Long)map.get("ctime");
				if(uList.containsKey(uid)){
					String v = uList.get(uid);
					Long ptime = Long.valueOf(v.split("_")[0]);
					if(ctime>ptime){
						uList.put(uid,ctime+"_"+isOnseat);
					}
				}else {
					uList.put(uid,ctime+"_"+isOnseat);
				}
			}
			
			//在心跳集合中，但不在用户上传地点表中时，是老版本用户，在心跳包中，就是在线且在岗，//新版本收费员，要查询最后上传经纬度时间小于当前时间30分钟以内的认为在岗
			//在岗集合
			List<Long> onlineList = new ArrayList<Long>();
			
			for(Long key : userMap.keySet()){
				if(!uList.containsKey(key)){//在心跳集合中，但不在用户上传地点表中时，是老版本用户，在心跳包中，就是在线且在岗
					onlineList.add(key);
				}else {//在心跳集合中，在用户上传地点表中时，查最近一次上传经纬度的时间 ，如果是30分钟前的，认为是在线，30分钟以内的是在岗
					String v = uList.get(key);
					Long time =Long.valueOf(v.split("_")[0]);
					Integer isOnseat = Integer.valueOf(v.split("_")[1]);
					if(isOnseat==1&&time>(ntime-20*60))
						onlineList.add(key);
				}
			}
			//1,更新收费员为不在线
			//2、在缓存中10分钟以内时更新为在线
			//3、更新符合在岗的收费员
			//1，2暂不处理，只写入在岗的收费员
			int a = dataBaseService.update("update user_info_tb set online_flag=? where auth_flag in(?,?) and online_flag=? ", new Object[]{22,1,2,23});
			System.err.println(">>>>>>更新了"+a+"条在线人员");
			if(onlineList.size()>0){
				System.out.println(">>>>>>当前在岗人数："+onlineList.size());
				String preParms = "";
				List<Object> values = new ArrayList<Object>();
				for(Long u : onlineList){
					if(preParms.equals(""))
						preParms = "?";
					else
						preParms +=",?";
					values.add(u);
				}
				values.add(0,1);
				values.add(0,2);
				values.add(0,23);
				int b = dataBaseService.updateParamList("update user_info_tb set online_flag=? where auth_flag in(?,?) and id in("+preParms+") ", values);
				log.error(">>>>>>更新了"+b+"条在岗人员");
			}
			
			//统计可立即支付的车场
			//1查询在线的收费员的车场编号
			List onlineUserComList = dataBaseService.getAll("select distinct comid from user_info_tb where online_flag=? ", new Object[]{23});
			log.error(">>>>更新车场");
			if(onlineUserComList!=null&&onlineUserComList.size()>0){
				List<Object> params = new ArrayList<Object>();
				String prePra = "";
				for(int i=0;i<onlineUserComList.size();i++){
					Map map = (Map)onlineUserComList.get(i);
					params.add(Long.valueOf(map.get("comid")+""));
					if(i!=0)
						prePra+=",";
					prePra +="?";
				}
				//	System.out.println(params);
				//System.err.println(prePra);
				if(params!=null){
					params.add(0,1);
					//2更新所有车场不可支付
					int i = dataBaseService.update("update com_info_tb set is_hasparker =?", new Object[]{0});
					//3更新符合有收费员在岗的车场
					int r = dataBaseService.updateParamList("update com_info_tb set is_hasparker =? where id in("+prePra+")", params);
					log.error(">>>>>停车场在岗设置 >>>重置了"+i+"个车场，更新了"+r+"个车场");
				}
			}
			
			//分析收费在线情况，满足连续1小时在线的加1积分
			scroe(onlineList);
		} catch (Exception e) {
			log.error("anlysisParkerOnline",e);
		}
	}
	
	/*
	 * 开始统计
	 */
	//rewrite
	private void directPayStart(Long nextTime){
		try {
			int directcount = dataBaseService.update("delete from directpay_anlysis_tb where create_time=?",
					new Object[]{nextTime - 24*60*60});
			log.error("删除直付数据==="+directcount+"条");
//		System.err.println("开始直付统计："+TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis()));
//		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
//		list =dataBaseService.getAll("select * from user_account_tb where target=? and uid>? and create_time between ? and ? order by create_time",
//				new Object[]{1,0,nextTime-(24*60*60),nextTime-1});
//		Map<String, Integer> directPayMap = new HashMap<String, Integer>();
//		nextTime = nextTime-24*60*60;
//		for(Map<String, Object> map : list){
//			String key = nextTime+"";
//			if(directPayMap.containsKey(key)){
//				Integer count = directPayMap.get(key);
//				directPayMap.put(key, count + 1);
//			}else{
//				directPayMap.put(key, 1);
//			}
//		}
			Long count = dataBaseService.getLong("select count(id) from order_tb where " +
					" create_time between ? and ? and c_type=? and pay_type=?", 
					new Object[]{nextTime-(24*60*60),nextTime-1,4,2}) ;
			if(count==null)
				count=0L;
			int ret = dataBaseService.update("insert into directpay_anlysis_tb(create_time,total) values (?,?)", new Object[]{nextTime-24*60*60,count});
			log.error(">>>>>直付统计，时间："+TimeTools.getTime_yyyyMMdd_HHmmss((nextTime-24*60*60)*1000)+",数量："+count+",写入结果:"+ret);
//		List<Object[]> values = new ArrayList<Object[]>();
//		for(String key: directPayMap.keySet()){	
//			Object[] va = new Object[2];
//			va[0]=Long.valueOf(key);
//			Integer total = directPayMap.get(key);
//			va[1]=total;
//			values.add(va);
//		}
//		log.error("直付统计开始写库...共"+values.size()+"条");
//		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"直付统计开始写库...，共"+values.size()+"条");
//		dataBaseService.bathInsert(sql, values, new int []{4,4});
//		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"直付统计写库完成...");
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	/*
	 * 初始化数据
	 */
	private void directPayInit(){
		System.err.println("开始计算直付统计："+TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis()));
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		list =dataBaseService.getAll("select * from user_account_tb where target=? and uid>? order by create_time",
				new Object[]{1,0});
		Map<String, Integer> directPayMap = new HashMap<String, Integer>();
		for(Map<String, Object> map : list){
			Long ctime = (Long)map.get("create_time");
			ctime = TimeTools.getBeginTime(ctime*1000);
			String key = ctime+"";
			if(directPayMap.containsKey(key)){
				Integer count = directPayMap.get(key);
				directPayMap.put(key, count + 1);
			}else{
				directPayMap.put(key, 1);
			}
		}
		String sql = "insert into directpay_anlysis_tb(create_time,total) values (?,?)";
		List<Object[]> values = new ArrayList<Object[]>();
		for(String key: directPayMap.keySet()){
			Object[] va = new Object[2];
			va[0]=Long.valueOf(key);
			Integer total = directPayMap.get(key);
			va[1]=total;
			values.add(va);
		}
		log.error("初始化直付统计......");
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"直付统计开始写库...，共"+values.size()+"条");
		dataBaseService.bathInsert(sql, values, new int []{4,4});
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"直付统计写库完成...");
	}
	
	/*
	 * 开始计算
	 */
	private void order3Start(Long nextTime){
		try {
			System.err.println("开始近三日订单统计："+TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis()));
			List<Map<String, Object>> list1 = new ArrayList<Map<String,Object>>();
			List<Map<String, Object>> list2 = new ArrayList<Map<String,Object>>();
			String sql1 = "select imei,count(1) order_3 from order_tb where imei!='' and create_time between ? and ? group by imei";
			list1 = dataBaseService.getAll(sql1, new Object[]{nextTime-3*(24*60*60),nextTime-1});
			String sql2 = "select imei,sum(total) money_3 from order_tb where imei!='' and end_time between ? and ? and pay_type=? and total>? group by imei";
			list2 = dataBaseService.getAll(sql2, new Object[]{nextTime-3*(24*60*60),nextTime-1,2,0});
			List<Map<String , Object>> sqlMaps = new ArrayList<Map<String,Object>>();
			boolean b = false;
			for(Map<String, Object> map : list1){
				String imei = (String)map.get("imei");
				for(Map<String, Object> map2 : list2){
					String imei2 = (String)map2.get("imei");
					if(imei.equals(imei2)){
						map.put("money_3", map2.get("money_3"));
						break;
					}
				}
				Long order_3 = (Long)map.get("order_3");
				Map<String, Object> sqlMap = new HashMap<String, Object>();
				sqlMap.put("sql", "update mobile_tb set order_3=?,money_3=? where imei=?");
				sqlMap.put("values", new Object[]{map.get("order_3"),map.get("money_3"),map.get("imei")});
				sqlMaps.add(sqlMap);
			}
			b= dataBaseService.bathUpdate_order_3(sqlMaps);
			log.error("近三日订单统计完成...");
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	private void anlyCharge(Long nextTime){
		try {
			log.error("=========开始统计收费情况==========");
			//按车场统计现金收费
			List<Map<String, Object>> list1 = new ArrayList<Map<String,Object>>();
			String sql1 = "select sum(b.amount) csum,a.comid from order_tb a,parkuser_cash_tb b where a.end_time between" +
					" ? and ? and a.state=? and a.id=b.orderid and b.type=? and a.uid> ? group by a.comid ";
			list1 = dataBaseService.getAll(sql1, new Object[]{nextTime-(24*60*60), nextTime, 1, 0, 0});
			//电子收费--车场账户
			List<Map<String, Object>> list2 = new ArrayList<Map<String,Object>>();
			String sql2 = "select sum(a.amount) psum,o.comid from order_tb o,park_account_tb a where o.id=a.orderid and o.end_time between ? and ? " +
					" and a.type= ? and a.source=? and o.uid>? group by o.comid ";
			list2 = dataBaseService.getAll(sql2, new Object[]{nextTime-(24*60*60), nextTime, 0, 0, 0});
			//电子收费--收费员账户
			List<Map<String, Object>> list3 = new ArrayList<Map<String,Object>>();
			String sql3 = "select sum(a.amount) psum,o.comid from order_tb o,parkuser_account_tb a where o.id=a.orderid and o.end_time between ? and ? " +
					" and a.type=? and a.target=? and a.remark like ? and o.uid>? group by o.comid ";
			list3 = dataBaseService.getAll(sql3, new Object[]{nextTime-(24*60*60), nextTime, 0, 4, "停车费%", 0});
			//电子收费--运营集团账户
			List<Map<String, Object>> list4 = new ArrayList<Map<String,Object>>();
			String sql4 = "select sum(g.amount) gsum,o.comid from order_tb o, group_account_tb g where o.id=g.orderid and o.end_time between ? and ? " +
					" and g.type=? and g.source=? and o.uid>? group by o.comid ";
			list4 = dataBaseService.getAll(sql4, new Object[]{nextTime-(24*60*60), nextTime, 0, 0, 0});
			//电子收费--城市商户账户
			List<Map<String, Object>> list5 = new ArrayList<Map<String,Object>>();
			String sql5 = "select sum(c.amount) csum,o.comid from order_tb o, city_account_tb c where o.id=c.orderid and o.end_time between ? and ? " +
					" and c.type=? and c.source=? and o.uid>? group by o.comid ";
			list5 = dataBaseService.getAll(sql5, new Object[]{nextTime-(24*60*60), nextTime, 0, 0, 0});
			
			Map<String, Map<String, Object>> cMap = new HashMap<String, Map<String, Object>>();
			nextTime = nextTime-24*60*60;
			if(list1 != null && !list1.isEmpty()){
				for(Map<String, Object> map : list1){
					Map<String, Object> map2 = new HashMap<String, Object>();
					String key = map.get("comid") + "";
					if(cMap.containsKey(key)){
						Map<String, Object> map3 = cMap.get(key);
						map3.put("cash", map.get("csum"));
					}else{
						map2.put("cash", map.get("csum"));
						cMap.put(key, map2);
					}
				}
			}
			
			if(list2 != null && !list2.isEmpty()){
				for(Map<String, Object> map : list2){
					Map<String, Object> map2 = new HashMap<String, Object>();
					String key = map.get("comid") + "";
					if(cMap.containsKey(key)){
						Map<String, Object> map3 = cMap.get(key);
						map3.put("epay_park", map.get("psum"));
					}else{
						map2.put("epay_park", map.get("psum"));
						cMap.put(key, map2);
					}
				}
			}
			
			if(list3 != null && !list3.isEmpty()){
				for(Map<String, Object> map : list3){
					Map<String, Object> map2 = new HashMap<String, Object>();
					String key = map.get("comid") + "";
					if(cMap.containsKey(key)){
						Map<String, Object> map3 = cMap.get(key);
						map3.put("epay_collector", map.get("psum"));
					}else{
						map2.put("epay_collector", map.get("psum"));
						cMap.put(key, map2);
					}
				}
			}
			
			if(list4 != null && !list4.isEmpty()){
				for(Map<String, Object> map : list4){
					Map<String, Object> map2 = new HashMap<String, Object>();
					String key = map.get("comid") + "";
					if(cMap.containsKey(key)){
						Map<String, Object> map3 = cMap.get(key);
						map3.put("epay_group", map.get("gsum"));
					}else{
						map2.put("epay_group", map.get("gsum"));
						cMap.put(key, map2);
					}
				}
			}
			
			if(list5 != null && !list5.isEmpty()){
				for(Map<String, Object> map : list5){
					Map<String, Object> map2 = new HashMap<String, Object>();
					String key = map.get("comid") + "";
					if(cMap.containsKey(key)){
						Map<String, Object> map3 = cMap.get(key);
						map3.put("epay_city", map.get("csum"));
					}else{
						map2.put("epay_city", map.get("csum"));
						cMap.put(key, map2);
					}
				}
			}
			String sql = "insert into collect_anlysis_tb(create_time,comid,cash,epay_collector,epay_park,epay_group,epay_city) values (?,?,?,?,?,?,?)";
			List<Object[]> values = new ArrayList<Object[]>();
			for(String key: cMap.keySet()){
				Object[] va = new Object[7];
				va[0] = nextTime;
				va[1] = Long.valueOf(key);
				Map<String, Object> map = cMap.get(key);
				Double cash = 0d;
				Double epay_collector = 0d;
				Double epay_park = 0d;
				Double epay_group = 0d;
				Double epay_city = 0d;
				if(map.get("cash") != null){
					cash = Double.valueOf(map.get("cash") + "");
				}
				if(map.get("epay_collector") != null){
					epay_collector = Double.valueOf(map.get("epay_collector") + "");
				}
				if(map.get("epay_park") != null){
					epay_park = Double.valueOf(map.get("epay_park") + "");
				}
				if(map.get("epay_group") != null){
					epay_group = Double.valueOf(map.get("epay_group") + "");
				}
				if(map.get("epay_city") != null){
					epay_city = Double.valueOf(map.get("epay_city") + "");
				}
				va[2] = cash;
				va[3] = epay_collector;
				va[4] = epay_park;
				va[5] = epay_group;
				va[6] = epay_city;
				values.add(va);
			}
			
			int r = dataBaseService.bathInsert(sql, values, new int []{4,4,3,3,3,3,3});
			log.error("r:"+r);
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	/*
	 * 开始统计
	 */
	private void registerStart(Long nextTime){
		try {
			int regcount = dataBaseService.update("delete from register_anlysis_tb where reg_time=?",
					new Object[]{nextTime - 24*60*60});
			log.error("删除注册数据==="+regcount+"条");
			System.err.println("开始车主注册统计："+TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis()));
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			list =dataBaseService.getAll("select u.*,c.car_number from user_info_tb u left join car_info_tb c on u.id=c.uin where u.auth_flag=? and u.reg_time between ? and ? order by u.reg_time",
					new Object[]{4,nextTime-(24*60*60),nextTime-1});
			Map<String, Map<String, Object>> regMap = new HashMap<String, Map<String, Object>>();
			nextTime = nextTime-24*60*60;
			for(Map<String, Object> map : list){
				Map<String, Object> map2 = new HashMap<String, Object>();
				String key = nextTime+"";
				if(regMap.containsKey(key)){
					Map<String, Object> map3 = regMap.get(key);
					Integer hascarnumber = (Integer)map3.get("hascarnumber");
					Integer allcarowner = (Integer)map3.get("allcarowner");
					if(map.get("car_number") != null){
						map3.put("hascarnumber", hascarnumber + 1);
					}
					map3.put("allcarowner", allcarowner + 1);
					regMap.put(key, map3);
				}else{
					if(map.get("car_number") != null){
						map2.put("hascarnumber", 1);
					}else{
						map2.put("hascarnumber", 0);
					}
					map2.put("allcarowner", 1);
					regMap.put(key, map2);
				}
			}
			String sql = "insert into register_anlysis_tb(reg_time,reg_count,carnumber_count) values (?,?,?)";
			List<Object[]> values = new ArrayList<Object[]>();
			for(String key: regMap.keySet()){
				Object[] va = new Object[3];
				va[0]=Long.valueOf(key);
				Map<String, Object> map = regMap.get(key);
				va[1] = map.get("allcarowner");
				va[2] = map.get("hascarnumber");
				values.add(va);
			}
			log.error("车主注册统计开始写库...共"+values.size()+"条");
			System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"车主注册统计开始写库...，共"+values.size()+"条");
			dataBaseService.bathInsert(sql, values, new int []{4,4,4});
			System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"车主注册统计写库完成...");
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	/*
	 * 初始化数据
	 */
	private void registerInit(){
		System.err.println("开始计算车主注册："+TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis()));
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		list =dataBaseService.getAll("select u.*,c.car_number from user_info_tb u left join car_info_tb c on u.id=c.uin where u.auth_flag=? order by reg_time",
				new Object[]{4});
		Map<String, Map<String, Object>> registerMap = new HashMap<String, Map<String, Object>>();
		for(Map<String, Object> map : list){
			Map<String, Object> map2 = new HashMap<String, Object>();
			Long rtime = (Long)map.get("reg_time");
			rtime = TimeTools.getBeginTime(rtime*1000);
			String key = rtime+"";
			if(registerMap.containsKey(key)){
				Map<String, Object> map3 = registerMap.get(key);
				Integer hascarnumber = (Integer)map3.get("hascarnumber");
				Integer allcarowner = (Integer)map3.get("allcarowner");
				if(map.get("car_number") != null){
					map3.put("hascarnumber", hascarnumber + 1);
				}
				map3.put("allcarowner", allcarowner + 1);
				registerMap.put(key, map3);
			}else{
				if(map.get("car_number") != null){
					map2.put("hascarnumber", 1);
				}else{
					map2.put("hascarnumber", 0);
				}
				map2.put("allcarowner", 1);
				registerMap.put(key, map2);
			}
		}
		String sql = "insert into register_anlysis_tb(reg_time,reg_count,carnumber_count) values (?,?,?)";
		List<Object[]> values = new ArrayList<Object[]>();
		for(String key: registerMap.keySet()){
			Object[] va = new Object[3];
			va[0]=Long.valueOf(key);
			Map<String, Object> map = registerMap.get(key);
			va[1] = map.get("allcarowner");
			va[2] = map.get("hascarnumber");
			values.add(va);
		}
		log.error("初始化车主注册统计......");
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"车主注册统计开始写库...，共"+values.size()+"条");
		dataBaseService.bathInsert(sql, values, new int []{4,4,4});
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"车主注册统计写库完成...");
	}
	
	/*
	 * 开始统计
	 */
	private void mobilePayStart(Long nextTime){
		try {
			int mobilecount = dataBaseService.update("delete from mobilepay_anlysis_tb where create_time=?",
					new Object[]{nextTime - 24*60*60});
			log.error("删除手机支付数据==="+mobilecount+"条");
			System.err.println("开始计算手机支付统计："+TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis()));
			List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
			list =dataBaseService.getAll("select * from order_tb where state=? and pay_type=? and end_time between ? and ? and c_type !=? and total>=? order by end_time",
					new Object[]{1,2,nextTime-(24*60*60),nextTime-1,4,1});
			Map<String, Integer> mobilepayMap = new HashMap<String, Integer>();
			nextTime = nextTime-24*60*60;
			for(Map<String, Object> map : list){
				Long comId = (Long)map.get("comid");
				String key = comId+"_"+nextTime;
				if(mobilepayMap.containsKey(key)){
					Integer count = mobilepayMap.get(key);
					mobilepayMap.put(key, count + 1);
				}else{
					mobilepayMap.put(key, 1);
				}
			}
			String sql = "insert into mobilepay_anlysis_tb(comid,create_time,mobilepay_count) values (?,?,?)";
			List<Object[]> values = new ArrayList<Object[]>();
			for(String key: mobilepayMap.keySet()){
				Object[] va = new Object[3];
				va[0]=Long.valueOf(key.split("_")[0]);
				va[1]=Long.valueOf(key.split("_")[1]);
				Integer mobilepay_count = mobilepayMap.get(key);
				va[2]=mobilepay_count;
				values.add(va);
			}
			log.error("手机支付统计开始写库...共"+values.size()+"条");
			System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"开始写库...，共"+values.size()+"条");
			dataBaseService.bathInsert(sql, values, new int []{4,4,4});
			System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"写库完成...");
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	/*
	 * 初始化数据
	 */
	private void mobilePayInit(){
		System.err.println("开始计算手机支付："+TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis()));
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		list =dataBaseService.getAll("select * from order_tb where state=? and pay_type=? and total>=? order by end_time",
				new Object[]{1,2,1});
		Map<String, Integer> mobilepayMap = new HashMap<String, Integer>();
		for(Map<String, Object> map : list){
			Long comId = (Long)map.get("comid");
			Long etime = (Long)map.get("end_time");
			etime = TimeTools.getBeginTime(etime*1000);
			String key = comId+"_"+etime;
			if(mobilepayMap.containsKey(key)){
				Integer count = mobilepayMap.get(key);
				mobilepayMap.put(key, count + 1);
			}else{
				mobilepayMap.put(key, 1);
			}
		}
		String sql = "insert into mobilepay_anlysis_tb(comid,create_time,mobilepay_count) values (?,?,?)";
		List<Object[]> values = new ArrayList<Object[]>();
		for(String key: mobilepayMap.keySet()){
			Object[] va = new Object[3];
			va[0]=Long.valueOf(key.split("_")[0]);
			va[1]=Long.valueOf(key.split("_")[1]);
			Integer mobilepay_count = mobilepayMap.get(key);
			va[2]=mobilepay_count;
			values.add(va);
		}
		log.error("初始化手机支付统计......");
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"开始写库...，共"+values.size()+"条");
		dataBaseService.bathInsert(sql, values, new int []{4,4,4});
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"写库完成...");
	}
	
	/**
	 * 开始统计
	 */
	private void start(Long nextTime){
		System.err.println("开始计算："+TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis()));
		List<Map<String, Object>> lalaList =dataBaseService.getAll("select * from share_log_tb where create_time between ? and ?  order by create_time",
				new Object[]{nextTime-899,nextTime});
		List<Map<String, Object>> orderList =dataBaseService.getAll("select * from order_tb  where state=? order by create_time",
				new Object[]{0});
		
		Map<String, List<Integer>> comLalaMap = new HashMap<String,  List<Integer>>();
		for(Map<String, Object> map : lalaList){
			Long comId = (Long)map.get("comid");
			Integer number = (Integer)map.get("s_number");
			String key = comId+"_"+nextTime;
			if(comLalaMap.containsKey(key)){
				List<Integer> nList = comLalaMap.get(key);
				nList.add(number);
			}else {
				List<Integer> nList = new ArrayList<Integer>();
				nList.add(number);
				comLalaMap.put(key, nList);
			}
		}
		for(String key : comLalaMap.keySet()){
			List<Integer> lnum = comLalaMap.get(key);
			if(lnum.size()>1){
				Integer total =0;
				for(Integer num : lnum){
					total +=num;
				}
				total = total/lnum.size();
				lnum.clear();
				lnum.add(total);
			}
		}
		
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"处理完了lala");
		for(Map<String, Object> map : orderList){
			Long comId = (Long)map.get("comid");
			String key = comId+"_"+nextTime;
			if(comLalaMap.containsKey(key)){//有这个时间点的分享数据
				List<Integer> nlInteger = comLalaMap.get(key);
				if(nlInteger.size()==2){//已经在这个时间点有占用泊位时，原数值加1
					Integer c = nlInteger.get(1);
					c=c+1;
					nlInteger.remove(1);
					nlInteger.add(c);
				}else {//没有在这个时间点有占用泊位时，置1个泊位占用
					nlInteger.add(1);
				}
			}else {//没有这个时间点的分享数据
				List<Integer> _nlIntegers=new ArrayList<Integer>();
				_nlIntegers.add(0);
				_nlIntegers.add(1);
				comLalaMap.put(key, _nlIntegers);
			}
		}
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"处理完了订单");
		//System.out.println("一天的数据："+comLalaMap+","+comLalaMap.size());
		
		/*
		 * 开始写库
		 *  create_time bigint,
			  comid bigint,
			  share_count integer DEFAULT 0,
			  free_count integer DEFAULT 0,
		 */
		String sql = "insert into park_anlysis_tb (create_time,comid,share_count,used_count) values (?,?,?,?)";
		List<Object[]> values = new ArrayList<Object[]>();
		System.out.println(comLalaMap.size());
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"合并lala及占用数");
		for(String key: comLalaMap.keySet()){
			//System.out.println(key);
			
			Object[] va = new Object[4];
			va[0]=Long.valueOf(key.split("_")[1]);
			va[1]=Long.valueOf(key.split("_")[0]);
			List<Integer> nList = comLalaMap.get(key);
			va[2]=nList.get(0);
			if(nList.size()>1){
				va[3]=nList.get(1);
			}else {
				va[3]=0;
			}
			values.add(va);
		}
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"开始写库...，共"+values.size()+"条");
		dataBaseService.bathInsert(sql, values, new int []{4,4,4,4});
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"写库完成...");
		
	}
	/**
	 * 初始化数据
	 */
	private void init(){
		System.err.println("开始计算："+TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis()));
		//Long start = 1405699200L;//2014-7-19 15:54:21  1405699200
		List<Map<String, Object>> lalaList =dataBaseService.getAll("select * from share_log_tb order by create_time",
				new Object[]{});
		List<Map<String, Object>> orderList =dataBaseService.getAll("select * from order_tb  order by create_time",
				new Object[]{});
		/*
		 *   comid bigint,
			  s_number integer DEFAULT 0,
			  create_time bigint,
		 * 写入park_anlysis_tb表
		 *  create_time bigint,
			  comid bigint,
			  share_count integer DEFAULT 0,
			  free_count integer DEFAULT 0,
		 * 
		 */
		Map<String, List<Integer>> comLalaMap = new HashMap<String,  List<Integer>>();
		/*
		 * 以15分钟为单位统计车场的lala次数：
		 * 726_1405772100=[118, 114, 123],
			 702_1405764000=[85], 
			 728_1405769400=[90], 
			 773_1405773900=[800], 
			 643_1405759500=[127],
			 702_1405779300=[78], 
			 726_1405776600=[126, 126, 125],
		 */
		for(Map<String, Object> map : lalaList){
			Long ctime = (Long)map.get("create_time");
			Long comId = (Long)map.get("comid");
			Integer number = (Integer)map.get("s_number");
			ctime = ctime -ctime%(15*60)+15*60;
			String key = comId+"_"+ctime;
			if(comLalaMap.containsKey(key)){
				List<Integer> nList = comLalaMap.get(key);
				nList.add(number);
			}else {
				List<Integer> nList = new ArrayList<Integer>();
				nList.add(number);
				comLalaMap.put(key, nList);
			}
		}
		
		//System.out.println("一天的数据："+comLalaMap+","+comLalaMap.size());
		/*
		 * 全并lala次数，取平均值
		 *   726_1405772100=[(118+114+123)/3],
			 702_1405764000=[85], 
			 728_1405769400=[90], 
			 773_1405773900=[800], 
			 643_1405759500=[127],
			 702_1405779300=[78], 
			 726_1405776600=[(126+126+125)/3]
		 */
		for(String key : comLalaMap.keySet()){
			List<Integer> lnum = comLalaMap.get(key);
			if(lnum.size()>1){
				Integer total =0;
				for(Integer num : lnum){
					total +=num;
				}
				total = total/lnum.size();
				lnum.clear();
				lnum.add(total);
			}
			/*Long kt = Long.valueOf(key.split("_")[1]);
			String nextKey =key.split("_")[0]+"_"+ (kt+15*60);
			if(!comLalaMap.containsKey(nextKey))
				comLalaMap.put(nextKey, lnum);*/
		}
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"处理完了lala");
		//System.out.println("一天的数据："+comLalaMap+","+comLalaMap.size());
		/*
		 * 统计占用泊位数：
		 * 订单开始时间:2014-09-13 05:05:00
			订单结束时间:2014-09-13 07:13:04
			以下时间点上应该是占用了一个泊位数
			2014-09-13 05:15:00
			2014-09-13 05:30:00
			2014-09-13 05:45:00
			2014-09-13 06:00:00
			2014-09-13 06:15:00
			2014-09-13 06:30:00
			2014-09-13 06:45:00
			2014-09-13 07:00:00
		 *
		 * 循环加入到每个时间点上,已经在这个时间点有占用泊位时，原数值加1
		 */
		//Map<String, Integer> comorderMap = new HashMap<String,  Integer>();
		for(Map<String, Object> map : orderList){
			Long etime = (Long)map.get("end_time");
			if(etime==null)
				continue;
			Long ctime = (Long)map.get("create_time");
			Long comId = (Long)map.get("comid");
			Long [] bt = getTimeInOff(ctime,etime);
			for(Long b: bt){//循环加入到每个时间点上,已经在这个时间点有占用泊位时，原数值加1
				String key = comId+"_"+b;
				if(comLalaMap.containsKey(key)){//有这个时间点的分享数据
					List<Integer> nlInteger = comLalaMap.get(key);
					if(nlInteger.size()==2){//已经在这个时间点有占用泊位时，原数值加1
						Integer c = nlInteger.get(1);
						c=c+1;
						nlInteger.remove(1);
						nlInteger.add(c);
					}else {//没有在这个时间点有占用泊位时，置1个泊位占用
						nlInteger.add(1);
					}
				}else {//没有这个时间点的分享数据
					List<Integer> _nlIntegers=new ArrayList<Integer>();
					_nlIntegers.add(0);
					_nlIntegers.add(1);
					comLalaMap.put(key, _nlIntegers);
					/*Long lastKey = null;
					Long lastTime = null;
					for(String _key : comLalaMap.keySet()){
						Long cid = Long.valueOf(_key.split("_")[0]);
						if(cid.intValue()==comId.intValue()){
							Long kt = Long.valueOf(_key.split("_")[1]);
							Long lt = b-kt;
							if(lastKey==null){
								lastKey = lt;
								lastTime = kt;
							}else {
								if(lt<lastKey){
									lastKey = lt;
									lastTime = kt;
								}
							}
						}
					}
					if(lastTime==null){
						
					}else {
						List<Integer> nlInteger = comLalaMap.get(comId+"_"+lastTime);
						List<Integer> _nlIntegers=new ArrayList<Integer>();
						_nlIntegers.add(nlInteger.get(0));
						_nlIntegers.add(1);
						comLalaMap.put(key, _nlIntegers);
					}*/
				}
			}
		}
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"处理完了订单");
		//System.out.println("一天的数据："+comLalaMap+","+comLalaMap.size());
		
		/*
		 * 开始写库
		 *  create_time bigint,
			  comid bigint,
			  share_count integer DEFAULT 0,
			  free_count integer DEFAULT 0,
		 */
		String sql = "insert into park_anlysis_tb (create_time,comid,share_count,used_count) values (?,?,?,?)";
		List<Object[]> values = new ArrayList<Object[]>();
		System.out.println(comLalaMap.size());
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"合并lala及占用数");
		for(String key: comLalaMap.keySet()){
			//System.out.println(key);
			
			Object[] va = new Object[4];
			va[0]=Long.valueOf(key.split("_")[1]);
			va[1]=Long.valueOf(key.split("_")[0]);
			List<Integer> nList = comLalaMap.get(key);
			va[2]=nList.get(0);
			if(nList.size()>1){
				va[3]=nList.get(1);
			}else {
				va[3]=0;
			}
			values.add(va);
		}
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"开始写库...，共"+values.size()+"条");
		dataBaseService.bathInsert(sql, values, new int []{4,4,4,4});
		System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(System.currentTimeMillis())+"写库完成...");
	}
	
	/**
	 * 取订单时间区间内的可取值
	 * @param start 开始时间
	 * @param end 结束时间
	 * start = 1410555900 =2014-09-13 05:05:00
	 * end = 1410567784 = 2014-09-13 08:23:04
	 * @return [];
	 */
	
	private  Long [] getTimeInOff(Long start,Long end){
		start = start-start%(15*60)  +15*60;
		end   = end -end%(15*60);
		Long s = end-start;
		int t = s.intValue()/(15*60);
		Long [] times = new Long[t+1];
		for(int i=0;i<=t;i++){
			times[i]=start+i*15*60;
		}
		return times;
	}
	/**
	 * 十分钟统计一次在线积分 
	 * @param uin
	 */
	private void scroe(List<Long> uinList){
		log.error("统计积分");
		Long btime = TimeTools.getToDayBeginTime();
		List<Map<String, Object>> allUserList = dataBaseService.getAll("select uin from collector_scroe_tb where create_time=?", new Object[]{btime});
		List<Map<String, Object>> allepayUserList = dataBaseService.getAll("select id from user_info_tb where comid in(select id from com_info_Tb where epay=?)", new Object[]{1});
		//需要更新的收费员
		List<Long> updateUinList = new ArrayList<Long>();
		if(allUserList!=null&&!allUserList.isEmpty()){
			for(Map<String, Object> map : allUserList){
				updateUinList.add((Long)map.get("uin"));
			}
		}
		//所有可支付车场的收费员才可以积分
		List<Long> allepayUser = new ArrayList<Long>();
		if(allepayUserList!=null&&!allepayUserList.isEmpty()){
			for(Map<String, Object> map : allepayUserList){
				allepayUser.add((Long)map.get("id"));
			}
		}
		//log.error("统计积分,收费员人"+updateUinList);
		//更新的SQL语句 
		String updateSql ="update collector_scroe_tb  set online_scroe=online_scroe+? where uin=? and create_time=? ";
		//新建的SQL语句
		String inserSql = "insert into collector_scroe_tb (uin,lala_scroe,nfc_score,praise_scroe,create_time,pai_score,online_scroe) values (?,?,?,?,?,?,?)";
		//更新的值		
		List<Object[]> updateValues= new ArrayList<Object[]>();
		//新建的值	
		List<Object[]> insertValues= new ArrayList<Object[]>();
		//判断在是否中更新的集合中，在调用 更新，不在调用 新建
		for(Long uin:uinList){
			if(!allepayUser.contains(uin)){
				log.error("--->>不可支付车场没有积分 ："+uin);
				continue;
			}
			if(updateUinList.contains(uin)){
				updateValues.add(new Object[]{0.05,uin,btime});
			}else {
				insertValues.add(new Object[]{uin,0,0.0,0,btime,0,0.05});
			}
		}
		//更新
		if(updateValues.size()>0){
			int ut = dataBaseService.bathInsert(updateSql,updateValues,new int[]{3,4,4});
			log.error("更新了"+ut+"条积分");
		}
		//新建
		if(insertValues.size()>0){
			int it =dataBaseService.bathInsert(inserSql,insertValues,new int[]{4,4,3,4,4,4,3});
			log.error("新插入了"+it+"条积分");
		}
	}
	
	public static void main(String[] args) {
		
	/*	Map<Long , Long> userMap = new HashMap<Long, Long>();
		userMap.put(1L, 1419928500L);
		userMap.put(2L, 1419928500L-15*60);
		userMap.put(3L, 1419928500L-9*60);
		userMap.put(4L, 1419928500L-16*60);
		//过滤掉心跳时间超过10分钟的收费员
		Long ntime = System.currentTimeMillis()/1000-15*60;
		Map<Long , Long> userMap1 = new HashMap<Long, Long>();
		for(Long key : userMap.keySet()){
			if(userMap.get(key)>ntime)
				userMap1.put(key,userMap.get(key));
		}
				
		System.err.println(userMap1);*/
//		Long eLong = 1410563584l;
//		Long [] r = (getTimeInOff(1410555900L,eLong));
//		System.out.println("b:"+TimeTools.getTime_yyyyMMdd_HHmmss(1410555900000L));
//		for(Long s : r){
//			System.out.println(TimeTools.getTime_yyyyMMdd_HHmmss(s*1000));
//		}
//		System.out.println("e:"+TimeTools.getTime_yyyyMMdd_HHmmss(eLong*1000));
	}
}
