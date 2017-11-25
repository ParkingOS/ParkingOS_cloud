package com.zld.impl;

import com.zld.pojo.*;
import com.zld.service.PgOnlyReadService;
import com.zld.service.StatsAccountService;
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

@Service("parkUserCash")
public class StatsParkUserCashAccountServiceImpl implements StatsAccountService {
	@Autowired
	private PgOnlyReadService readService;

	Logger logger = Logger.getLogger(StatsParkUserCashAccountServiceImpl.class);

	@Override
	public StatsAccountResp statsAccount(StatsReq req){
		//logger.error(req.toString());
		StatsAccountResp resp = new StatsAccountResp();
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
				column = "uin";//按收费员编号统计
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
			params.add(0);
			params.add(startTime);
			params.add(endTime);
			params.add(0);//停车费（非预付）
			params.add(1);//预付停车费
			params.add(2);//预付退款（预付超额）
			params.add(3);//预付补缴（预付不足）
			params.add(4);//追缴停车费
			params.addAll(idList);
			String sql = "select sum(amount) summoney,target,"+column+" from parkuser_cash_tb where " +
					" is_delete=? and create_time between ? and ? and target in (?,?,?,?,?) " +
					" and "+column+" in ("+preParams+") group by "+column+",target ";
			logger.error(sql+":"+params);
			List<Map<String, Object>> list = readService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				List<Object> existIds = new ArrayList<Object>();//列表已存在的主键
				List<StatsAccount> accounts = new ArrayList<StatsAccount>();
				for(Map<String, Object> map : list){
					Long id = (Long)map.get(column);//主键
					Integer target = (Integer)map.get("target");//操作类型
					Double summoney = Double.valueOf(map.get("summoney") + "");//金额

					StatsAccount account = null;
					if(existIds.contains(id)){
						for(StatsAccount statsAccount : accounts){
							long statsId = statsAccount.getId();
							if(id.intValue() == statsId){//查找匹配的主键
								account = statsAccount;
								break;
							}
						}
					}else{
						existIds.add(id);
						account = new StatsAccount();
						account.setId(id);
						accounts.add(account);
					}
					switch (target) {
						case 0://停车费（非预付）
							account.setParkingFee(summoney);
							break;
						case 1://预付停车费
							account.setPrepayFee(summoney);
							break;
						case 2://预付退款（预付超额）
							account.setRefundFee(summoney);
							break;
						case 3://预付补缴（预付不足）
							account.setAddFee(summoney);
							break;
						case 4://追缴停车费
							account.setPursueFee(summoney);
							break;
						default:
							break;
					}
				}
				resp.setAccounts(accounts);
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
			int type = req.getType();//0：按收费员编号统计 1：按车场编号统计 2：按泊位段编号查询 3：按泊位查询
			int pageNum = req.getPageNum();
			int pageSize = req.getPageSize();
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
				column = "uin";//按收费员编号统计
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
			params.add(0);
			params.add(startTime);
			params.add(endTime);
			params.add(0);//停车费（非预付）
			params.add(1);//预付停车费
			params.add(2);//预付退款（预付超额）
			params.add(3);//预付补缴（预付不足）
			params.add(4);//追缴停车费
			params.add(id);
			String sql = "select * from parkuser_cash_tb where is_delete=? and create_time between ? and ? " +
					" and target in (?,?,?,?,?) and "+column+"=? ";
			String countSql = "select count(id) from parkuser_cash_tb where is_delete=? and create_time between ? and ? " +
					" and target in (?,?,?,?,?) and "+column+"=? ";
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
