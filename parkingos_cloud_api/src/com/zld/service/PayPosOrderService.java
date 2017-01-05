package com.zld.service;

import com.zld.pojo.AutoPayPosOrderReq;
import com.zld.pojo.AutoPayPosOrderResp;
import com.zld.pojo.ManuPayPosOrderReq;
import com.zld.pojo.ManuPayPosOrderResp;
import com.zld.pojo.PayEscapePosOrderReq;
import com.zld.pojo.PayEscapePosOrderResp;

public interface PayPosOrderService {
	/**
	 * 自动支付pos机订单
	 * @param req
	 * @return
	 */
	public AutoPayPosOrderResp autoPayPosOrder(AutoPayPosOrderReq req);
	
	/**
	 * 手动结算POS机订单，此接口依赖autoPayPosOrder接口，
	 * 只有autoPayPosOrder接口不能结算的时候才会调用该接口。
	 * @param req
	 * @return
	 */
	public ManuPayPosOrderResp manuPayPosOrder(ManuPayPosOrderReq req);
	
	/**
	 * POS机结算逃单
	 * @param req
	 * @return
	 */
	public PayEscapePosOrderResp payEscapePosOrder(PayEscapePosOrderReq req);
}
