package com.zhenlaidian.bean;

import java.io.Serializable;

@SuppressWarnings("serial")
public class BankCard implements Serializable {

// 个人账户返回{"bank_name":"中国工商银行","area":"辽宁阜新市","card_number":"6222020********3785","bank_pint":"海州支行","user_id":"210902197711184591"}

//	车场账户返回：{"id":"115","card_number":"6222000200113996540","name":"马永涛","mobile":"13718933660","bank_name":"工商银行","area":"北京","bank_pint":"朝阳东苇路支行","user_id":"32033434390335030432"}
//	atype: 0银行卡，1支付宝，2微信
//	user_id:身份证号
//	没有绑定时返回{}
	private String bank_name;
	private String card_number;
	private String user_id;
	private String area;
	private String bank_pint;
	private String id;
	private String mobile;
	private String atype;
	private String name;
	
	
	public BankCard() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getAtype() {
		return atype;
	}

	public void setAtype(String atype) {
		this.atype = atype;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBank_name() {
		return bank_name;
	}

	public void setBank_name(String bank_name) {
		this.bank_name = bank_name;
	}

	public String getCard_number() {
		return card_number;
	}

	public void setCard_number(String card_number) {
		this.card_number = card_number;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
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

	@Override
	public String toString() {
		return "BankCard [bank_name=" + bank_name + ", card_number="
				+ card_number + ", user_id=" + user_id + ", area=" + area
				+ ", bank_pint=" + bank_pint + ", id=" + id + ", mobile="
				+ mobile + ", atype=" + atype + ", name=" + name + "]";
	}



}
