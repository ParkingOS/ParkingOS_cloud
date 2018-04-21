package com.zldpark.service;

import com.zldpark.pojo.AccountReq;
import com.zldpark.pojo.AccountResp;
import com.zldpark.pojo.StatsAccountResp;
import com.zldpark.pojo.StatsReq;

/**
 * 账目统计
 * @author whx
 *
 */
public interface StatsAccountService {
	
	/**
	 * 统计流水账目
	 * @param req
	 * @return
	 */
	public StatsAccountResp statsAccount(StatsReq req);
	
	/**
	 * 查流水账目明细
	 * @param req
	 * @return
	 */
	public AccountResp account(AccountReq req);
	
}
