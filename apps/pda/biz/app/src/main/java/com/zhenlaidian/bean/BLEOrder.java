package com.zhenlaidian.bean;

public class BLEOrder {

	public String lp;// 车牌
	public String cn;// 卡号 cardNumber
	public String fee;// 金额
	public String inout;// 进出类型:进口in出口out；

	public String getLp() {
		return lp;
	}

	public void setLp(String lp) {
		this.lp = lp;
	}

	public String getFee() {
		return fee;
	}

	public void setFee(String fee) {
		this.fee = fee;
	}

	public String getInout() {
		return inout;
	}

	public void setInout(String inout) {
		this.inout = inout;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public BLEOrder() {
		super();
	}

	@Override
	public String toString() {
		return "BLEOrder [lp=" + lp + ", cn=" + cn + ", fee=" + fee + ", inout=" + inout + "]";
	}
	
}
