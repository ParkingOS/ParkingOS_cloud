package com.zld.facade;

import com.zld.pojo.AutoPayPosOrderFacadeReq;
import com.zld.pojo.AutoPayPosOrderResp;
import com.zld.pojo.ManuPayPosOrderFacadeReq;
import com.zld.pojo.ManuPayPosOrderResp;
import com.zld.pojo.PayEscapePosOrderFacadeReq;
import com.zld.pojo.PayEscapePosOrderResp;

public interface PayPosOrderFacade {
	/**
	 * 自动支付pos机订单，如金额不足则不结算订单，直接返回，因为后面会有其他手动支付方式，如现金支付，刷卡支付等
	 * @param req
	 * @return
	 */
	public AutoPayPosOrderResp autoPayPosOrder(AutoPayPosOrderFacadeReq req);
	
	/**
	 * 手动结算POS机订单，此接口依赖autoPayPosOrder接口，
	 * 只有autoPayPosOrder接口不能结算的时候才会调用该接口。
	 * @param req
	 * @return
	 */
	public ManuPayPosOrderResp manuPayPosOrder(ManuPayPosOrderFacadeReq req);
	
	/**
	 * 追缴POS机订单
	 * @param req
	 * @return
	 */
	public PayEscapePosOrderResp payEscapePosOrder(PayEscapePosOrderFacadeReq req);
}
