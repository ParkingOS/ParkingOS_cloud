package com.zld.pojo;

import java.io.Serializable;

public class ReturnCardReq implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Long cardId = -1L;
	private Long unBinder = -1L;//注销操作人
	private Long groupId = -1L;//操作人所在的运营集团
	private Long curTime = System.currentTimeMillis()/1000;
	public Long getCardId() {
		return cardId;
	}
	public void setCardId(Long cardId) {
		this.cardId = cardId;
	}
	public Long getUnBinder() {
		return unBinder;
	}
	public void setUnBinder(Long unBinder) {
		this.unBinder = unBinder;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	public Long getCurTime() {
		return curTime;
	}
	public void setCurTime(Long curTime) {
		this.curTime = curTime;
	}
	@Override
	public String toString() {
		return "ReturnCardReq [cardId=" + cardId + ", unBinder=" + unBinder
				+ ", groupId=" + groupId + ", curTime=" + curTime + "]";
	}
}
