package com.zld.pojo;

import java.io.Serializable;
import java.util.List;

public class StatsCardResp extends BaseResp implements Serializable {
	private List<StatsCard> cards;

	public List<StatsCard> getCards() {
		return cards;
	}

	public void setCards(List<StatsCard> cards) {
		this.cards = cards;
	}

	@Override
	public String toString() {
		return "StatsCardResp [cards=" + cards + "]";
	}


}
