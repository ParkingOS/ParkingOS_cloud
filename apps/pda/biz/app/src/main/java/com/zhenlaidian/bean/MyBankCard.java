package com.zhenlaidian.bean;

import java.io.Serializable;

public class MyBankCard implements Serializable{
	//[id=null, comid=null, uin=null, card_number=6214830105821184,
	//atype=0, name=黄世辉, mobile=13860132164,bank_name=招商银行, note=null,
	// type=null, state=null, area=北京, bank_pint=招商银行上地支行, user_id=]
	//name card_number  mobile  bank_name area
	private String	id;
	private String	card_number;
	private String	name;
	private String	mobile;
	private String	bank_name;
	private String	area;
	private String	bank_pint;
	private String	user_id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCard_number() {
		return card_number;
	}
	public void setCard_number(String card_number) {
		this.card_number = card_number;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getBank_name() {
		return bank_name;
	}
	public void setBank_name(String bank_name) {
		this.bank_name = bank_name;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getBank_pint() {
		return bank_pint;
	}
	public void setBank_pint(String bank_pint) {
		this.bank_pint = bank_pint;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	@Override
	public String toString() {
		return "MyBankCard [id=" + id + ", card_number=" + card_number
				+ ", name=" + name + ", mobile=" + mobile + ", bank_name="
				+ bank_name + ", area=" + area + ", bank_pint=" + bank_pint
				+ ", user_id=" + user_id + "]";
	}
	public MyBankCard() {
		super();
	}
}
