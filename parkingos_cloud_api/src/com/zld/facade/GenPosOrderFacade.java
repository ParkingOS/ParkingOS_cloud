package com.zld.facade;

import com.zld.pojo.GenPosOrderFacadeReq;
import com.zld.pojo.GenPosOrderFacadeResp;

public interface GenPosOrderFacade {
	/**
	 * pos机生成订单
	 * @param req
	 * @return
	 */
	public GenPosOrderFacadeResp genPosOrder(GenPosOrderFacadeReq req);
}
