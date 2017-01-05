package com.zld.pojo;

import java.io.Serializable;

public class CardInfoResp extends BaseResp implements Serializable {
	private Card card;
	private String mobile;
	private String carnumber;
	private String group_name;//运营集团名称
	public Card getCard() {
		return card;
	}
	public void setCard(Card card) {
		this.card = card;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getCarnumber() {
		return carnumber;
	}
	public void setCarnumber(String carnumber) {
		this.carnumber = carnumber;
	}
	public String getGroup_name() {
		return group_name;
	}
	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}
	@Override
	public String toString() {
		return "CardInfoResp [card=" + card + ", mobile=" + mobile
				+ ", carnumber=" + carnumber + ", group_name=" + group_name
				+ "]";
	}
}
