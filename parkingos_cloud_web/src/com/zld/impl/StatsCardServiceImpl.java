package com.zld.impl;

import com.zld.pojo.*;
import com.zld.service.PgOnlyReadService;
import com.zld.service.StatsCardService;
import com.zld.utils.ExecutorsUtil;
import com.zld.utils.SqlInfo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service("card")
public class StatsCardServiceImpl implements StatsCardService {
	@Autowired
	private PgOnlyReadService readService;

	Logger logger = Logger.getLogger(StatsCardServiceImpl.class);

	@Override
	public StatsCardResp statsCard(StatsReq req) {
		//logger.error(req.toString());
		StatsCardResp resp = new StatsCardResp();
		try {
			long startTime = req.getStartTime();
			long endTime = req.getEndTime();
			List<Object> idList = req.getIdList();
			int type = req.getType();//0：按收费员编号统计 1：按车场编号统计 2：按泊位段编号查询 3：按泊位查询
			if(startTime <= 0
					|| endTime <= 0
					|| idList == null
					|| idList.isEmpty()){
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;
			}
			String column = null;
			if(type == 0){
				column = "uid";//按收费员编号统计
			}else if(type == 1){
				column = "comid";//按车场编号统计
			}else if(type == 2){
				column = "berthseg_id";//按泊位段编号统计
			}else if(type == 3){
				column = "berth_id";//按泊位编号统计
			}else if(type == 4){
				column = "groupid";
			}
			if(column == null){
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;
			}
			String preParams = "";
			for(int i = 0; i<idList.size(); i++){
				if(i == 0){
					preParams ="?";
				}else{
					preParams += ",?";
				}
			}
			List<Object> params = new ArrayList<Object>();
			params.add(0);//状态正常
			params.add(startTime);
			params.add(endTime);
			params.addAll(idList);
			String sql = "select sum(amount) summoney,count(id) ccount,charge_type,consume_type,type," + column +
					" from card_account_tb where is_delete=? and create_time between ? and ? and " + column +
					" in ("+preParams+") group by " + column + ",charge_type,consume_type,type ";
			List<Map<String, Object>> list = readService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				List<Object> existIds = new ArrayList<Object>();//列表已存在的主键
				List<StatsCard> cards = new ArrayList<StatsCard>();
				for(Map<String, Object> map : list){
					Long id = (Long)map.get(column);
					Double summoney = Double.valueOf(map.get("summoney") + "");
					Long count = (Long)map.get("ccount");
					Integer charge_type = (Integer)map.get("charge_type");//充值方式：0：现金充值 1：微信公众号充值 2：微信客户端充值 3：支付宝充值 4：预支付退款 5：订单退款
					Integer consume_type = (Integer)map.get("consume_type");//消费方式 0：支付停车费（非预付） 1：预付停车费 2：补缴停车费  3：追缴停车费
					Integer cardType = (Integer)map.get("type");//（卡片的生命周期）0：充值 1：消费 2：开卡（卡片初始化，此时的卡片还不能使用） 3：激活卡片（此时卡片方可使用） 4：绑定用户 5：注销卡片

					StatsCard card = null;
					if(existIds.contains(id)){
						for(StatsCard statsCard : cards){
							long statsId = statsCard.getId();
							if(id.intValue() == statsId){//查找匹配的主键
								card = statsCard;
								break;
							}
						}
					}else{
						existIds.add(id);
						card = new StatsCard();
						card.setId(id);
						cards.add(card);//新添加
					}
					switch (cardType) {
						case 0://充值
							if(charge_type == 0){//现金充值
								card.setChargeCashFee(summoney);
							}else if(charge_type == 4){//预付退款
								card.setRefundFee(summoney);
							}
							break;
						case 1://消费
							if(consume_type == 0){//支付停车费（非预付）
								card.setParkingFee(summoney);
							}else if(consume_type == 1){//预付停车费
								card.setPrepayFee(summoney);
							}else if(consume_type == 2){//补缴停车费
								card.setAddFee(summoney);
							}else if(consume_type == 3){//追缴停车费
								card.setPursueFee(summoney);
							}
							break;
						case 2://开卡
							card.setRegFee(summoney);
							card.setRegCount(count);
							break;
						case 3://激活卡片
							card.setActFee(summoney);
							card.setActCount(count);
							break;
						case 4://卡片绑定用户
							card.setBindCount(count);
							break;
						case 5://注销卡片
							card.setReturnFee(summoney);
							card.setReturnCount(count);
							break;
						default:
							break;
					}
				}
				resp.setCards(cards);
				return resp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setResult(-1);
		resp.setErrmsg("系统错误");
		return resp;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public AccountResp account(AccountReq req) {
		//logger.error(req.toString());
		AccountResp resp = new AccountResp();
		try {
			ExecutorService pool = ExecutorsUtil.getExecutorService();//获取线程池
			long startTime = req.getStartTime();
			long endTime = req.getEndTime();
			long id = req.getId();
			int pageNum = req.getPageNum();
			int pageSize = req.getPageSize();
			int type = req.getType();//0：按收费员编号统计 1：按车场编号统计 2：按泊位段编号查询 3：按泊位查询
			SqlInfo sqlInfo = req.getSqlInfo();
			if(startTime <= 0
					|| endTime <= 0
					|| id <= 0){
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;
			}
			String column = null;
			if(type == 0){
				column = "uid";//按收费员编号统计
			}else if(type == 1){
				column = "comid";//按车场编号统计
			}else if(type == 2){
				column = "berthseg_id";//按泊位段编号统计
			}else if(type == 3){
				column = "berth_id";//按泊位编号统计
			}else if(type == 4){
				column = "groupid";
			}
			if(column == null){
				resp.setResult(-1);
				resp.setErrmsg("参数错误");
				return resp;
			}
			ArrayList<Object> params = new ArrayList<Object>();
			params.add(0);//状态正常
			params.add(startTime);
			params.add(endTime);
			params.add(id);
			String sql = "select * from card_account_tb where is_delete=? and create_time between " +
					" ? and ? and "+column+"=? ";
			String countSql = "select count(id) from card_account_tb where is_delete=? and create_time " +
					" between ? and ? and "+column+"=? ";
			if(sqlInfo != null) {
				countSql += " and "+ sqlInfo.getSql();
				sql += " and "+sqlInfo.getSql();
				params.addAll(sqlInfo.getParams());
			}
			sql += " order by create_time desc ";
			QueryCount queryCount = new QueryCount(readService, countSql, params);
			QueryList queryList = new QueryList(readService, sql, params, pageNum, pageSize);
			Future<Long> future0 = pool.submit(queryCount);
			Future<List> future1 = pool.submit(queryList);
			Long count = future0.get();
			List list = future1.get();
			resp.setCount(count);
			resp.setList(list);
			resp.setResult(1);
			return resp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setResult(-1);
		resp.setErrmsg("系统错误");
		return resp;
	}

}
