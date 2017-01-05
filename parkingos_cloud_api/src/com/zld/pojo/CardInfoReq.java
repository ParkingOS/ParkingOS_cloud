package com.zld.pojo;

import java.io.Serializable;

public class CardInfoReq implements Serializable {
	private String nfc_uuid;//
	private Long groupId = -1L;//查询卡片的收费员所属运营集团

	public String getNfc_uuid() {
		return nfc_uuid;
	}

	public void setNfc_uuid(String nfc_uuid) {
		this.nfc_uuid = nfc_uuid;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	@Override
	public String toString() {
		return "CardInfoReq [nfc_uuid=" + nfc_uuid + ", groupId=" + groupId
				+ "]";
	}
	
}
