package com.zld.service;

import com.zld.pojo.AccountReq;
import com.zld.pojo.AccountResp;
import com.zld.pojo.StatsCardResp;
import com.zld.pojo.StatsReq;

public interface StatsCardService {
	/**
	 * 统计卡片
	 * @param req
	 * @return
	 */
	public StatsCardResp statsCard(StatsReq req);

	/**
	 * 查流水账目明细
	 * @param req
	 * @return
	 */
	public AccountResp account(AccountReq req);
}
