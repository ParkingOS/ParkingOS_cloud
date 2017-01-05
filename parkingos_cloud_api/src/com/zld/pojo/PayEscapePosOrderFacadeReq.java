package com.zld.pojo;

import java.io.Serializable;

public class PayEscapePosOrderFacadeReq implements Serializable {
	private Long orderId;// 订单对象
	private Long uid = -1L;// 收费员编号
	private String imei;// 手机串号
	private Double money = 0d;// 结算金额
	private String nfc_uuid;//刷卡结算的卡片编号
	private Integer payType = 0;//支付方式 0：现金支付 1：刷卡支付
	private Integer version = -1;// 版本号
	private Integer bindcard = 0;//0:客户端弹出绑定车主手机号的提示框 1：不弹出提示框直接刷卡预付
	private Long groupId = -1L;//收费员所在的运营集团编号
	private Long parkId = -1L;//收费员所在的车场
	private Long berthId = -1L;//追缴订单的泊位编号
	
	public Long getParkId() {
		return parkId;
	}
	public void setParkId(Long parkId) {
		this.parkId = parkId;
	}
	public Long getBerthId() {
		return berthId;
	}
	public void setBerthId(Long berthId) {
		this.berthId = berthId;
	}
	private Long curTime = System.currentTimeMillis() / 1000;
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
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Integer getBindcard() {
		return bindcard;
	}
	public void setBindcard(Integer bindcard) {
		this.bindcard = bindcard;
	}
	public Long getCurTime() {
		return curTime;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	@Override
	public String toString() {
		return "PayEscapePosOrderFacadeReq [orderId=" + orderId + ", uid="
				+ uid + ", imei=" + imei + ", money=" + money + ", nfc_uuid="
				+ nfc_uuid + ", payType=" + payType + ", version=" + version
				+ ", bindcard=" + bindcard + ", groupId=" + groupId
				+ ", parkId=" + parkId + ", berthId=" + berthId + ", curTime="
				+ curTime + "]";
	}
}
