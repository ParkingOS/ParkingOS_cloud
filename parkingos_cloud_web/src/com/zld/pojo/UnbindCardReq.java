package com.zld.pojo;

import java.io.Serializable;

public class UnbindCardReq implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Long cardId = -1L;
	private Long unBinder = -1L;//解绑操作人
	private Long groupId = -1L;//操作人所在的运营集团
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
	@Override
	public String toString() {
		return "UnbindCardReq [cardId=" + cardId + ", unBinder=" + unBinder
				+ ", groupId=" + groupId + ", curTime=" + curTime + "]";
	}

}
