package com.zld.pojo;

import java.io.Serializable;

public class UnbindCardReq implements Serializable {
	private String nfc_uuid;
	private Long unBinder = -1L;//注销操作人
	private Long groupId = -1L;//操作人所在的运营集团
	private Long curTime = System.currentTimeMillis()/1000;
	
	public Long getCurTime() {
		return curTime;
	}
	public Long getUnBinder() {
		return unBinder;
	}
	public void setUnBinder(Long unBinder) {
		if(unBinder == null)
			unBinder = -1L;
		this.unBinder = unBinder;
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
	@Override
	public String toString() {
		return "UnbindCardReq [nfc_uuid=" + nfc_uuid + ", unBinder=" + unBinder
				+ ", groupId=" + groupId + ", curTime=" + curTime + "]";
	}
	
}
