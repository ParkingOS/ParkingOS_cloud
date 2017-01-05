package com.zld.pojo;

import java.io.Serializable;

public class GenPosOrderFacadeReq implements Serializable {
	private Long orderId = -1L;//有的生成订单之前会预取订单号
	private String carNumber;//车牌号
	private Long berthId = -1L;//泊位编号
	private String imei;//手机串号
	private Integer version = -1;//版本号
	private Long uid = -1L;//收费员编号
	private Long parkId = -1L;//车场编号
	private Long groupId = -1L;//运营集团编号
	private Long curTime = System.currentTimeMillis()/1000;
	//---------预付参数-------------//
	private Integer payType = 0;//预付类型 0:现金预付 1：刷卡预付
	private String nfc_uuid;//刷卡预支付时的卡号
	private Double prepay = 0d;//预付金额
	private Integer bindcard = 0;//预支付用到的参数：0:客户端弹出绑定车主手机号的提示框 1：不弹出提示框直接刷卡预付
	private Integer carType = 0;
	
	public Integer getCarType() {
		return carType;
	}
	public void setCarType(Integer carType) {
		this.carType = carType;
	}
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public String getCarNumber() {
		return carNumber;
	}
	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		this.uid = uid;
	}
	public Long getParkId() {
		return parkId;
	}
	public void setParkId(Long parkId) {
		this.parkId = parkId;
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
	public Double getPrepay() {
		return prepay;
	}
	public void setPrepay(Double prepay) {
		this.prepay = prepay;
	}
	public Integer getBindcard() {
		return bindcard;
	}
	public void setBindcard(Integer bindcard) {
		this.bindcard = bindcard;
	}
	public Long getBerthId() {
		return berthId;
	}
	public void setBerthId(Long berthId) {
		this.berthId = berthId;
	}
	public Integer getPayType() {
		return payType;
	}
	public void setPayType(Integer payType) {
		this.payType = payType;
	}
	public Long getCurTime() {
		return curTime;
	}
	@Override
	public String toString() {
		return "GenPosOrderFacadeReq [orderId=" + orderId + ", carNumber="
				+ carNumber + ", berthId=" + berthId + ", imei=" + imei
				+ ", version=" + version + ", uid=" + uid + ", parkId="
				+ parkId + ", groupId=" + groupId + ", curTime=" + curTime
				+ ", payType=" + payType + ", nfc_uuid=" + nfc_uuid
				+ ", prepay=" + prepay + ", bindcard=" + bindcard
				+ ", carType=" + carType + "]";
	}
}
