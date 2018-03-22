package com.zld.service;

import com.zld.pojo.AccountReq;
import com.zld.pojo.AccountResp;
import com.zld.pojo.StatsOrderResp;
import com.zld.pojo.StatsReq;
import org.springframework.stereotype.Service;
@Service
public interface StatsOrderService {
	/**
	 * 订单统计
	 * @param req
	 * @return
	 */
	public StatsOrderResp statsOrder(StatsReq req);

	/**
	 * 查订单
	 * @param req
	 * @return
	 */
	public AccountResp order(AccountReq req);
}
