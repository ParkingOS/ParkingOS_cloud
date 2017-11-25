package com.zld.pojo;

import java.io.Serializable;

public class RegCardReq implements Serializable {
	private String nfc_uuid;//卡片内置的唯一硬件编号
	private String cardNo;//卡面号（印在卡面上的编号）
	private Long regId = -1L;//开卡人账号
	private Double money = 100d;//初始化金额
	private Long groupId = -1L;//运营集团编号
	private String cardName;//卡片名称
	private String device;//开卡设备
	private Long curTime = System.currentTimeMillis()/1000;
	public String getNfc_uuid() {
		return nfc_uuid;
	}
	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public Long getRegId() {
		return regId;
	}
	public void setRegId(Long regId) {
		this.regId = regId;
	}
	public Double getMoney() {
		return money;
	}
	public void setMoney(Double money) {
		this.money = money;
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
	public String getCardName() {
		return cardName;
	}
	public void setCardName(String cardName) {
		this.cardName = cardName;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}

	@Override
	public String toString() {
		return "RegCardReq [nfc_uuid=" + nfc_uuid + ", cardNo=" + cardNo
				+ ", regId=" + regId + ", money=" + money + ", curTime="
				+ curTime + "]";
	}
}
