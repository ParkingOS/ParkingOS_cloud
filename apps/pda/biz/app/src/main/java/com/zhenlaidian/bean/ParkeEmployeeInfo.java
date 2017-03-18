package com.zhenlaidian.bean;

public class ParkeEmployeeInfo {
	public String nickname;
	public String total;
	public String uid;
	public String cash;

	public ParkeEmployeeInfo() {
		super();
	}

	public ParkeEmployeeInfo(String nickname, String total, String uid, String cash) {
		super();
		this.nickname = nickname;
		this.total = total;
		this.uid = uid;
		this.cash = cash;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getCash() {
		return cash;
	}

	public void setCash(String cash) {
		this.cash = cash;
	}

	@Override
	public String toString() {
		return "ParkeEmployeeInfo [nickname=" + nickname + ", total=" + total + ", uid=" + uid + ", cash=" + cash + "]";
	}

}
