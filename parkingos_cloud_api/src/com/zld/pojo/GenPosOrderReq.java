package com.zld.pojo;

import java.io.Serializable;

public class GenPosOrderReq implements Serializable {
	private Long orderId = -1L;//有的生成订单之前会预取订单号
	private String carNumber;//车牌号
	private Berth berth;//
	private String imei;//手机串号
	private Long userId = -1L;//用户编号
	private Integer cType = 2;//订单生成方式 2：录入车牌 5：月卡会员
	private Long workId = -1L;//上班编号
	private Integer version = -1;//版本号
	private Long berthOrderId = -1L;//绑定的车检器订单编号
	private Long startTime;//订单开始时间
	private Long uid = -1L;//收费员编号
	private Long parkId = -1L;//车场编号
	private Long groupId = -1L;//运营集团编号
	private Integer carType = 0;//车辆类型
	private Long curTime = System.currentTimeMillis()/1000;//当前时间
	//---------预付参数-------------//
	private String nfc_uuid;//刷卡预支付时的卡号
	private Double prepay = 0d;//预付金额
	private Integer bindcard = 0;//预支付用到的参数：0:客户端弹出绑定车主手机号的提示框 1：不弹出提示框直接刷卡预付
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getWorkId() {
		return workId;
	}
	public void setWorkId(Long workId) {
		this.workId = workId;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public Long getBerthOrderId() {
		return berthOrderId;
	}
	public void setBerthOrderId(Long berthOrderId) {
		this.berthOrderId = berthOrderId;
	}
	public Long getStartTime() {
		return startTime;
	}
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		this.uid = uid;
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
	public Integer getcType() {
		return cType;
	}
	public void setcType(Integer cType) {
		this.cType = cType;
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
	public Berth getBerth() {
		return berth;
	}
	public void setBerth(Berth berth) {
		this.berth = berth;
	}
	public Long getCurTime() {
		return curTime;
	}
	public Integer getCarType() {
		return carType;
	}
	public void setCarType(Integer carType) {
		this.carType = carType;
	}
	@Override
	public String toString() {
		return "GenOrderReq [orderId=" + orderId + ", carNumber=" + carNumber
				+ ", berth=" + berth + ", imei=" + imei + ", userId=" + userId
				+ ", cType=" + cType + ", workId=" + workId + ", version="
				+ version + ", berthOrderId=" + berthOrderId + ", startTime="
				+ startTime + ", uid=" + uid + ", parkId=" + parkId
				+ ", groupId=" + groupId + ", carType=" + carType
				+ ", curTime=" + curTime + ", nfc_uuid=" + nfc_uuid
				+ ", prepay=" + prepay + ", bindcard=" + bindcard + "]";
	}
}
