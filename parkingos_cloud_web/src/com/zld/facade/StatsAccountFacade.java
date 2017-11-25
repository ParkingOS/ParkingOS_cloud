package com.zld.facade;

import com.zld.pojo.StatsFacadeResp;
import com.zld.pojo.StatsReq;

public interface StatsAccountFacade {
	/**
	 * 统计收费员账目
	 * @param req
	 * @return
	 */
	public StatsFacadeResp statsParkUserAccount(StatsReq req);

	/**
	 * 统计车场账目
	 * @param req
	 * @return
	 */
	public StatsFacadeResp statsParkAccount(StatsReq req);

	/**
	 * 统计泊位段账目
	 * @param req
	 * @return
	 */
	public StatsFacadeResp statsBerthSegAccount(StatsReq req);

	/**
	 * 统计泊位账目
	 * @param req
	 * @return
	 */
	public StatsFacadeResp statsBerthAccount(StatsReq req);

	/**
	 * 统计运营集团账目
	 * @param req
	 * @return
	 */
	public StatsFacadeResp statsGroupAccount(StatsReq req);
}
