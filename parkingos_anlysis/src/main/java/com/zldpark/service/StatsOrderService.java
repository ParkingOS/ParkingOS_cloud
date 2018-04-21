package com.zldpark.service;

import com.zldpark.pojo.StatsOrderResp;
import com.zldpark.pojo.StatsReq;

public interface StatsOrderService {
	/**
	 * 订单统计
	 * @param req
	 * @return
	 */
	public StatsOrderResp statsOrder(StatsReq req);
}
