package com.zldpark.schedule;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.zldpark.service.DataBaseService;
import com.zldpark.service.PgOnlyReadService;
import com.zldpark.utils.StringUtils;
import com.zldpark.utils.TimeTools;


public class TicketSchedule extends TimerTask {

	private DataBaseService dataBaseService;
	private PgOnlyReadService pgOnlyReadService;

	public TicketSchedule(DataBaseService dataBaseService,PgOnlyReadService pgOnlyReadService ){
		this.dataBaseService = dataBaseService;
		this.pgOnlyReadService = pgOnlyReadService;
	}

	private static Logger logger = Logger.getLogger(TicketSchedule.class);

	@Override
	public void run() {
		logger.error("开始下行数据定时任务....");
		backTicketLimit();
	}

	private void backTicketLimit(){
		logger.error("开始回收优惠券定时任务....");
		List<Map<String , Object>> sqlMaps = new ArrayList<Map<String,Object>>();
		List<Map<String , Object>> delsqlMaps = new ArrayList<Map<String,Object>>();
		Long date = TimeTools.getLongMilliSeconds();
		List<Object> list = new ArrayList<Object>();
		list.add(date);
		//查询待回收减免劵集合
//		TicketTb ticketConditions = new TicketTb();
//		ticketConditions.setState(0);
//		List<SearchBean> searchBeans = new ArrayList<SearchBean>();
//		SearchBean searchBean = new SearchBean();
//		searchBean.setFieldName("limit_day");
//		searchBean.setOperator(FieldOperator.LESS_THAN);
//		searchBean.setEndValue(date);
//		searchBeans.add(searchBean);
//		PageOrderConfig pageOrderConfig = new PageOrderConfig();
//		pageOrderConfig.setPageInfo(null,null);
//		List<TicketTb> ticketTbs = commonDao.selectListByConditions(ticketConditions,searchBeans,pageOrderConfig);//待回收减免劵集合
		List<Map<String, Object>> ticketList = pgOnlyReadService.getAll("select * from ticket_tb " +
				"where state=? and limit_day < ? ", new Object[]{0,date});
		//logger.error("回收优惠券定时任务....待回收集合:"+ticketTbs);
		logger.error("回收优惠券定时任务....待回收集合:"+ticketList);
		List<Map<String, Object>> backShops = new ArrayList<Map<String,Object>>();//回收减免劵集合
		ArrayList<Long> shopid_list = new ArrayList<Long>();//shop_id集合
//		for (TicketTb ticketTb : ticketTbs) {
//			if (!shopid_list.contains(ticketTb.getShopId()))
//				shopid_list.add(ticketTb.getShopId());
//		}
		for(Map<String, Object> map: ticketList ){
			Long shopId = (Long)map.get("shop_id");
			if(!shopid_list.contains(shopId))
				shopid_list.add(shopId);
		}
		logger.error("回收优惠券定时任务....待回收shopId集合:"+shopid_list);
		//遍历shop_id 集合
		for(int i=0; i<shopid_list.size();i++){
			Map<String, Object> map = new HashMap<String,Object>();
			Long shop_id = shopid_list.get(i);//商户id
			//根据shop_id 查询商户信息
//			ShopTb shopConditions = new ShopTb();
//			shopConditions.setId(shop_id);
//			ShopTb shopTb = (ShopTb)commonDao.selectObjectByConditions(shopConditions);
			Map<String, Object> shop = pgOnlyReadService.getMap("select * from shop_tb " +
					"where id = ? ", new Object[]{shop_id});
			map.put("shop_id",shop_id);
			map.put("name",shop.get("name"));
			map.put("comid",shop.get("comid"));
//			map.put("name",shopTb.getName());
//			map.put("comid",shopTb.getComid());
			Integer money = 0;//回收时长额度
			Integer umoney = 0; //回收金额额度
			Integer ecount = 0; //回收全免额度
			Integer type = 0;//商户类型
//			for(int j=0; j<ticketTbs.size();j++){
//				if(shop_id == ticketTbs.get(j).getShopId().longValue()){
//					money += ticketTbs.get(j).getMoney();
//					umoney += ticketTbs.get(j).getUmoney().intValue();
//					type = ticketTbs.get(j).getType();
//					if(type==4){
//						ecount++;
//					}
//				}
//			}
			for(int j=0;j<ticketList.size();j++){
				Map<String, Object> ticketMap = ticketList.get(j);
				Long sid = (Long)ticketMap.get("shop_id");
				if(shop_id.equals(sid)){
					money += (Integer)ticketMap.get("money");
					Double um  =StringUtils.formatDouble(ticketMap.get("umoney"));
					umoney +=um.intValue();
					type =(Integer)ticketMap.get("type");
					if(type==4){
						ecount++;
					}
				}
			}
			Integer money_total = (Integer)shop.get("ticket_limit")+money;//回收后时长额度  =现有额度+回收额度
			Integer umoney_total =(Integer)shop.get("ticket_money") + umoney; //回收后金额额度  =现有额度+回收额度
			Integer ecount_total = (Integer)shop.get("ticketfree_limit") + ecount; //回收后金额额度  =现有额度+回收额度
//			Integer money_total = shopTb.getTicketLimit() + money;//回收后时长额度  =现有额度+回收额度
//			Integer umoney_total = shopTb.getTicketMoney() + umoney; //回收后金额额度  =现有额度+回收额度
//			Integer ecount_total = shopTb.getTicketfreeLimit() + ecount; //回收后金额额度  =现有额度+回收额度
			map.put("money",money);
			map.put("umoney",umoney);
			map.put("ecount",ecount);
			map.put("money_total",money_total);
			map.put("umoney_total",umoney_total);
			map.put("ecount_total",ecount_total);
			backShops.add(map);
		}
		logger.error("回收优惠券定时任务....回收集合:"+backShops);
		//遍历待回收集合，执行回收任务
		for(Map<String,Object> map : backShops){
			//回收金额
//			ShopTb shopConditions = new ShopTb();
//			shopConditions.setId((Long)map.get("shop_id"));
//			ShopTb shopTb  = new ShopTb();
//			shopTb.setTicketLimit((int)map.get("money_total"));//回收后时长额度
//			shopTb.setTicketMoney((int)map.get("umoney_total"));
//			shopTb.setTicketfreeLimit((int)map.get("ecount_total"));
//			Integer backResult = commonDao.updateByConditions(shopTb,shopConditions);
			
			int ret = dataBaseService.update("update shop_tb set ticket_limit=?," +
					"ticket_money=?,ticketfree_limit=? where id =? ", 
					new Object[]{map.get("money_total"),map.get("umoney_total"),map.get("ecount_total"),map.get("shop_id")});
			
			//logger.error("回收优惠券定时任务.."+(Long)map.get("shop_id")+"..回收结果:"+backResult);
			logger.error("回收优惠券定时任务.."+(Long)map.get("shop_id")+"..回收结果:"+ret);

			// 记录回收流水
//			ShopAccountTb shopAccountTb = new ShopAccountTb();
//			shopAccountTb.setShopId(Integer.parseInt(map.get("shop_id")+""));
//			shopAccountTb.setShopName((String)map.get("name"));
//			shopAccountTb.setTicketLimit((int)(map.get("money")));
//			shopAccountTb.setTicketfreeLimit((int)(map.get("ecount")));
//			shopAccountTb.setTicketMoney((int)(map.get("umoney")));
//			shopAccountTb.setOperateTime(System.currentTimeMillis() / 1000);
//			shopAccountTb.setParkId((Long)map.get("comid"));
//			shopAccountTb.setOperateType(2);
//			Integer saveAccount = commonDao.insert(shopAccountTb);
			int r = dataBaseService.update("insert into shop_account_tb(shop_id,shop_name,ticket_limit," +
					"ticketfree_limit,ticket_money,operate_time,park_id,operator_type) values(?,?,?,?,?,?,?,?)",
					new Object[]{map.get("shop_id"),map.get("name"),map.get("money"),map.get("ecount"),
					map.get("umoney"),System.currentTimeMillis() / 1000,map.get("comid"),2});
			logger.error("回收优惠券定时任务.."+(Long)map.get("shop_id")+"..记录流水结果:"+r);
//			logger.error("回收优惠券定时任务.."+(Long)map.get("shop_id")+"..记录流水结果:"+saveAccount);
		}

		//删除过期未用减免劵
		for(Map<String, Object> map: ticketList){
//			TicketTb ticket = new TicketTb();
//			ticket.setState(2);
//			TicketTb updateTicket = new TicketTb();
//			updateTicket.setId(ticketTb.getId());
//
//			Integer delTicket = commonDao.updateByConditions(ticket,updateTicket);
			int ret = dataBaseService.update("update ticket_tb set state= ? where id = ? ", new Object[]{2,map.get("id")});
			logger.error("回收优惠券定时任务..."+map.get("id")+"...删除:"+ret);
		}
		//删除过期未用减免劵
//		for(TicketTb ticketTb : ticketTbs){
//			TicketTb ticket = new TicketTb();
//			ticket.setState(2);
//			TicketTb updateTicket = new TicketTb();
//			updateTicket.setId(ticketTb.getId());
//
//			Integer delTicket = commonDao.updateByConditions(ticket,updateTicket);
//			logger.error("回收优惠券定时任务..."+ticketTb.getId()+"...删除:"+delTicket);
//		}
	}

}
