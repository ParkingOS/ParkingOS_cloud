package com.zld.pojo;

import java.io.Serializable;

public class BindCardReq implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Long cardId = -1L;//卡片编号
	private String nfc_uuid;//nfc卡内置唯一编号
	private String mobile;//要绑定的会员的手机号
	private String carNumber;//要绑定的会员的车牌号
	private Long binder;//绑定操作人的账号
	private Long groupId = -1L;//绑定人所在的运营集团
	private Long curTime = System.currentTimeMillis()/1000;

	public Long getCurTime() {
		return curTime;
	}
	public Long getCardId() {
		return cardId;
	}
	public void setCardId(Long cardId) {
		if(cardId == null)
			cardId = -1L;
		this.cardId = cardId;
	}
	public String getNfc_uuid() {
		return nfc_uuid;
	}
	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getCarNumber() {
		return carNumber;
	}
	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}
	public Long getBinder() {
		return binder;
	}
	public void setBinder(Long binder) {
		if(binder == null)
			binder = -1L;
		this.binder = binder;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	@Override
	public String toString() {
		return "BindCardReq [cardId=" + cardId + ", nfc_uuid=" + nfc_uuid
				+ ", mobile=" + mobile + ", carNumber=" + carNumber
				+ ", binder=" + binder + ", groupId=" + groupId + ", curTime="
				+ curTime + "]";
	}

}
