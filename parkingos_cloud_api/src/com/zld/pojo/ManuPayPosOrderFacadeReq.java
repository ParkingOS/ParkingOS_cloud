package com.zld.pojo;

import java.io.Serializable;

public class ManuPayPosOrderFacadeReq implements Serializable {
	private Long orderId;// 订单对象
	private Long uid = -1L;// 收费员编号
	private String imei;// 手机串号
	private Double money = 0d;// 结算金额
	private String nfc_uuid;//刷卡结算的卡片编号
	private Integer payType = 0;//支付方式 0：现金支付 1：刷卡支付
	private Integer version = -1;// 版本号
	private Integer bindcard = 0;//0:客户端弹出绑定车主手机号的提示框 1：不弹出提示框直接刷卡预付
	private Long groupId = -1L;//收费员所在的运营集团编号
	private Long endTime = -1L;//订单结束时间
	private Long curTime = System.currentTimeMillis() / 1000;
	public String getNfc_uuid() {
		return nfc_uuid;
	}
	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}
	public Integer getPayType() {
		return payType;
	}
	public void setPayType(Integer payType) {
		this.payType = payType;
	}
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		this.uid = uid;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public Double getMoney() {
		return money;
	}
	public void setMoney(Double money) {
		this.money = money;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Long getCurTime() {
		return curTime;
	}
	public void setCurTime(Long curTime) {
		this.curTime = curTime;
	}
	public Integer getBindcard() {
		return bindcard;
	}
	public void setBindcard(Integer bindcard) {
		this.bindcard = bindcard;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
	@Override
	public String toString() {
		return "ManuPayPosOrderFacadeReq [orderId=" + orderId + ", uid=" + uid
				+ ", imei=" + imei + ", money=" + money + ", nfc_uuid="
				+ nfc_uuid + ", payType=" + payType + ", version=" + version
				+ ", bindcard=" + bindcard + ", groupId=" + groupId
				+ ", endTime=" + endTime + ", curTime=" + curTime + "]";
	}
	
}
