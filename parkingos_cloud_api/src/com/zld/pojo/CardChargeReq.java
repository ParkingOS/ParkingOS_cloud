package com.zld.pojo;

import java.io.Serializable;

public class CardChargeReq implements Serializable {
	private String nfc_uuid;//卡片唯一编号
	private Double money = 0d;//充值金额
	private Integer chargeType = 0;//充值方式：0：现金充值 1：微信公众号充值 2：微信客户端充值 3：支付宝充值 4：预支付退款 5：订单退款 
	private Long cashierId = -1L;//收费员编号
	private Long orderId = -1L;//订单编号
	private Long groupId = -1L;
	private String subOrderId;//第三方支付的订单号
	private Long parkId = -1L;//收费员所在车场编号
	private Long curTime = System.currentTimeMillis()/1000;//当前时间
	
	public Double getMoney() {
		return money;
	}
	public void setMoney(Double money) {
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
		this.orderId = orderId;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	public String getNfc_uuid() {
		return nfc_uuid;
	}
	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}
	public Long getParkId() {
		return parkId;
	}
	public void setParkId(Long parkId) {
		this.parkId = parkId;
	}
	@Override
	public String toString() {
		return "CardChargeReq [nfc_uuid=" + nfc_uuid + ", money=" + money
				+ ", chargeType=" + chargeType + ", cashierId=" + cashierId
				+ ", orderId=" + orderId + ", curTime=" + curTime
				+ ", groupId=" + groupId + ", subOrderId=" + subOrderId
				+ ", parkId=" + parkId + "]";
	}
}
