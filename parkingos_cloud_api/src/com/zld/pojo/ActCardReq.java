package com.zld.pojo;

import java.io.Serializable;

public class ActCardReq implements Serializable {
	private String nfc_uuid;
	private Long uid = -1L;
	private Long groupId = -1L;//激活卡片人员所属的运营集团编号
	private Long parkId = -1L;//收费员所在车场编号
	private Long curTime = System.currentTimeMillis()/1000;
	public Long getParkId() {
		return parkId;
	}
	public void setParkId(Long parkId) {
		this.parkId = parkId;
	}
	public String getNfc_uuid() {
		return nfc_uuid;
	}
	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}
	public Long getUid() {
		return uid;
	}
	public void setUid(Long uid) {
		this.uid = uid;
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
		return "ActCardReq [nfc_uuid=" + nfc_uuid + ", uid=" + uid
				+ ", groupId=" + groupId + ", parkId=" + parkId + ", curTime="
				+ curTime + "]";
	}
}
