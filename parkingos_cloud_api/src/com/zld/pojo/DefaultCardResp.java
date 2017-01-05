package com.zld.pojo;

import java.io.Serializable;

public class DefaultCardResp extends BaseResp implements Serializable {
	private Card card;

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	@Override
	public String toString() {
		return "DefaultCardResp [card=" + card + "]";
	}
	
}
