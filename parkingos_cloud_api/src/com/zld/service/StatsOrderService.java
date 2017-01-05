package com.zld.service;

import org.springframework.stereotype.Service;

import com.zld.pojo.StatsOrderResp;
import com.zld.pojo.StatsReq;
@Service
public interface StatsOrderService {
	/**
	 * ¶©µ¥Í³¼Æ
	 * @param req
	 * @return
	 */
	public StatsOrderResp statsOrder(StatsReq req);
}
