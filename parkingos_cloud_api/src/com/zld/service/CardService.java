package com.zld.service;

import com.zld.pojo.ActCardReq;
import com.zld.pojo.BaseResp;
import com.zld.pojo.BindCardReq;
import com.zld.pojo.CardChargeReq;
import com.zld.pojo.CardInfoReq;
import com.zld.pojo.CardInfoResp;
import com.zld.pojo.DefaultCardReq;
import com.zld.pojo.DefaultCardResp;
import com.zld.pojo.RegCardReq;
import com.zld.pojo.UnbindCardReq;

public interface CardService {
	
	/**
	 * 获取卡片信息
	 * @param req
	 * @return
	 */
	public CardInfoResp getCardInfo(CardInfoReq req);
	
	/**
	 * 开卡，卡片入库（只有开卡程序有此功能）
	 * @param req
	 * @return
	 */
	public BaseResp regCard(RegCardReq req);
	
	/**
	 * 激活卡片（收费员程序和开卡程序都有此功能）
	 * @return
	 */
	public BaseResp actCard(ActCardReq req);
	
	/**
	 * 绑定会员(手机号)（收费员程序和开卡程序都有此功能）
	 * @param req
	 * @return
	 */
	public BaseResp bindUserCard(BindCardReq req);
	
	/**
	 * 绑定车牌(只有车牌)（收费员程序和开卡程序都有此功能）
	 * @param req
	 * @return
	 */
	public BaseResp bindPlateCard(BindCardReq req);
	
	/**
	 * 卡片充值（收费员程序和开卡程序都有此功能）
	 * @param cardChargeReq
	 * @return
	 */
	public BaseResp cardCharge(CardChargeReq cardChargeReq);
	
	/**
	 * 注销卡片（只有开卡程序都有此功能）
	 * @return
	 */
	public BaseResp returnCard(UnbindCardReq req);
	
	/**
	 * 取会员绑定的余额最多的卡片
	 * @return
	 */
	public DefaultCardResp getDefaultCard(DefaultCardReq req);
}
