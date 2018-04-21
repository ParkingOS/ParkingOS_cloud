package com.zldpark.service;

import com.zldpark.pojo.StatsCardResp;
import com.zldpark.pojo.StatsReq;

public interface StatsCardService {
	/**
	 * 统计卡片
	 * @param req
	 * @return
	 */
	public StatsCardResp statsCard(StatsReq req);
}
