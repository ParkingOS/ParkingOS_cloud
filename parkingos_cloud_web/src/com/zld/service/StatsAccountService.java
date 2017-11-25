package com.zld.service;

import com.zld.pojo.AccountReq;
import com.zld.pojo.AccountResp;
import com.zld.pojo.StatsAccountResp;
import com.zld.pojo.StatsReq;

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
