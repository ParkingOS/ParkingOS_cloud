package parkingos.com.bolink.schedule;

import com.zld.common_dao.dao.CommonDao;
import com.zld.common_dao.enums.FieldOperator;
import com.zld.common_dao.qo.PageOrderConfig;
import com.zld.common_dao.qo.SearchBean;
import org.apache.log4j.Logger;
import parkingos.com.bolink.beans.ShopAccountTb;
import parkingos.com.bolink.beans.ShopTb;
import parkingos.com.bolink.beans.TicketTb;
import parkingos.com.bolink.utlis.TimeTools;

import java.util.*;


public class TicketSchedule extends TimerTask {

	private CommonDao commonDao;

	public TicketSchedule(CommonDao commonDao ){
		this.commonDao = commonDao;
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
		TicketTb ticketConditions = new TicketTb();
		ticketConditions.setState(0);
		List<SearchBean> searchBeans = new ArrayList<SearchBean>();
		SearchBean searchBean = new SearchBean();
		searchBean.setFieldName("limit_day");
		searchBean.setOperator(FieldOperator.LESS_THAN);
		searchBean.setEndValue(date);
		searchBeans.add(searchBean);
		PageOrderConfig pageOrderConfig = new PageOrderConfig();
		pageOrderConfig.setPageInfo(null,null);
		List<TicketTb> ticketTbs = commonDao.selectListByConditions(ticketConditions,searchBeans,pageOrderConfig);//待回收减免劵集合
		logger.error("回收优惠券定时任务....待回收集合:"+ticketTbs);
		List<Map<String, Object>> backShops = new ArrayList<Map<String,Object>>();//回收减免劵集合
		ArrayList<Long> shopid_list = new ArrayList<Long>();//shop_id集合
		for (TicketTb ticketTb : ticketTbs) {
			if (!shopid_list.contains(ticketTb.getShopId()))
				shopid_list.add(ticketTb.getShopId());
		}
		logger.error("回收优惠券定时任务....待回收shopId集合:"+shopid_list);
		//遍历shop_id 集合
		for(int i=0; i<shopid_list.size();i++){
			Map<String, Object> map = new HashMap<String,Object>();
			long shop_id = shopid_list.get(i);//商户id
			//根据shop_id 查询商户信息
			ShopTb shopConditions = new ShopTb();
			shopConditions.setId(shop_id);
			ShopTb shopTb = (ShopTb)commonDao.selectObjectByConditions(shopConditions);
			map.put("shop_id",shop_id);
			map.put("name",shopTb.getName());
			map.put("comid",shopTb.getComid());
			Integer money = 0;//回收时长额度
			Integer umoney = 0; //回收金额额度
			Integer ecount = 0; //回收全免额度
			Integer type = 0;//商户类型
			for(int j=0; j<ticketTbs.size();j++){
				if(shop_id == ticketTbs.get(j).getShopId().longValue()){
					money += ticketTbs.get(j).getMoney();
					umoney += ticketTbs.get(j).getUmoney().intValue();
					type = ticketTbs.get(j).getType();
					if(type==4){
						ecount++;
					}
				}
			}
			Integer money_total = shopTb.getTicketLimit() + money;//回收后时长额度  =现有额度+回收额度
			Integer umoney_total = shopTb.getTicketMoney() + umoney; //回收后金额额度  =现有额度+回收额度
			Integer ecount_total = shopTb.getTicketfreeLimit() + ecount; //回收后金额额度  =现有额度+回收额度
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
			ShopTb shopConditions = new ShopTb();
			shopConditions.setId((Long)map.get("shop_id"));
			ShopTb shopTb  = new ShopTb();
			shopTb.setTicketLimit((int)map.get("money_total"));//回收后时长额度
			shopTb.setTicketMoney((int)map.get("umoney_total"));
			shopTb.setTicketfreeLimit((int)map.get("ecount_total"));
			Integer backResult = commonDao.updateByConditions(shopTb,shopConditions);
			logger.error("回收优惠券定时任务.."+(Long)map.get("shop_id")+"..回收结果:"+backResult);

			// 记录回收流水
			ShopAccountTb shopAccountTb = new ShopAccountTb();
			shopAccountTb.setShopId(Integer.parseInt(map.get("shop_id")+""));
			shopAccountTb.setShopName((String)map.get("name"));
			shopAccountTb.setTicketLimit((int)(map.get("money")));
			shopAccountTb.setTicketfreeLimit((int)(map.get("ecount")));
			shopAccountTb.setTicketMoney((int)(map.get("umoney")));
			shopAccountTb.setOperateTime(System.currentTimeMillis() / 1000);
			shopAccountTb.setParkId((Long)map.get("comid"));
			shopAccountTb.setOperateType(2);
			Integer saveAccount = commonDao.insert(shopAccountTb);
			logger.error("回收优惠券定时任务.."+(Long)map.get("shop_id")+"..记录流水结果:"+saveAccount);
		}

		//删除过期未用减免劵
		for(TicketTb ticketTb : ticketTbs){
			TicketTb ticket = new TicketTb();
			ticket.setState(2);
			TicketTb updateTicket = new TicketTb();
			updateTicket.setId(ticketTb.getId());

			Integer delTicket = commonDao.updateByConditions(ticket,updateTicket);
			logger.error("回收优惠券定时任务..."+ticketTb.getId()+"...删除:"+delTicket);
		}

	}

}
