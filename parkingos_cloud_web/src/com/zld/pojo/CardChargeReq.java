package com.zld.pojo;

import java.io.Serializable;

public class CardChargeReq implements Serializable {
	private Long cardId = -1L;//卡片编号
	private Double money = 0d;//充值金额
	private Integer chargeType = 0;//充值方式：0：现金充值 1：微信公众号充值 2：微信客户端充值 3：支付宝充值 4：预支付退款 5：订单退款
	private Long cashierId = -1L;//收费员编号
	private Long orderId = -1L;//订单编号
	private Long groupId = -1L;//操作人所在运营集团
	private Long curTime = System.currentTimeMillis()/1000;//当前时间

	private String subOrderId;//第三方支付的订单号
	public Long getCardId() {
		return cardId;
	}
	public void setCardId(Long cardId) {
		if(cardId == null)
			cardId = -1L;
		this.cardId = cardId;
	}
	public Double getMoney() {
		return money;
	}
	public void setMoney(Double money) {
		if(money == null)
			money = 0d;
		this.money = money;
	}
	public String getSubOrderId() {
		return subOrderId;
	}
	public void setSubOrderId(String subOrderId) {
		this.subOrderId = subOrderId;
	}
	public Long getCurTime() {
		return curTime;
	}
	public Long getCashierId() {
		return cashierId;
	}
	public void setCashierId(Long cashierId) {
		if(cashierId == null)
			cashierId = -1L;
		this.cashierId = cashierId;
	}
	public Integer getChargeType() {
		return chargeType;
	}
	public void setChargeType(Integer chargeType) {
		this.chargeType = chargeType;
	}

	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		if(orderId == null)
			orderId = -1L;
		this.orderId = orderId;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	@Override
	public String toString() {
		return "CardChargeReq [cardId=" + cardId + ", money=" + money
				+ ", chargeType=" + chargeType + ", cashierId=" + cashierId
				+ ", orderId=" + orderId + ", groupId=" + groupId
				+ ", curTime=" + curTime + ", subOrderId=" + subOrderId + "]";
	}

}
