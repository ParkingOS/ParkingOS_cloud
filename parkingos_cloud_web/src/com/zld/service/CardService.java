package com.zld.service;

import com.zld.pojo.*;

public interface CardService {

	/**
	 * 绑定会员(手机号)
	 * @param req
	 * @return
	 */
	public BaseResp bindUserCard(BindCardReq req);

	/**
	 * 绑定车牌(只有车牌)
	 * @param req
	 * @return
	 */
	public BaseResp bindPlateCard(BindCardReq req);

	/**
	 * 解绑
	 * @param req
	 * @return
	 */
	public BaseResp unBindCard(UnbindCardReq req);

	/**
	 * 卡片充值
	 * @param cardChargeReq
	 * @return
	 */
	public BaseResp cardCharge(CardChargeReq cardChargeReq);

	/**
	 * 注销卡片
	 * @return
	 */
	public BaseResp returnCard(ReturnCardReq req);
}
