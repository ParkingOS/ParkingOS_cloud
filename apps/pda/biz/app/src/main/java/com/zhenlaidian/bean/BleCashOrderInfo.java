package com.zhenlaidian.bean;

public class BleCashOrderInfo {

	// {"state":"-1","errmsg":"车牌没有注册!"}
	public String state;
	public String errmsg;

	public BleCashOrderInfo() {
		super();
	}

	@Override
	public String toString() {
		return "BleCashOrderInfo [state=" + state + ", errmsg=" + errmsg + "]";
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

}
