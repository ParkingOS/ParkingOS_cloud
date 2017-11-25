package com.zld.pojo;

import java.io.Serializable;

public class StatsCard extends StatsAccount implements Serializable {
	//卡片统计
	private double regFee = 0;//开卡初始化金额
	private double chargeCashFee = 0;//卡片充值金额
	private double returnFee = 0;//退卡退还金额
	private double actFee = 0;//激活卡片初始化金额
	private long returnCount = 0;//退卡数量
	private long actCount = 0;//激活卡片数量
	private long regCount = 0;//开卡数量
	private long bindCount = 0;//绑定用户数量

	public double getRegFee() {
		return regFee;
	}
	public void setRegFee(double regFee) {
		this.regFee = regFee;
	}
	public double getChargeCashFee() {
		return chargeCashFee;
	}
	public void setChargeCashFee(double chargeCashFee) {
		this.chargeCashFee = chargeCashFee;
	}
	public double getReturnFee() {
		return returnFee;
	}
	public void setReturnFee(double returnFee) {
		this.returnFee = returnFee;
	}
	public double getActFee() {
		return actFee;
	}
	public void setActFee(double actFee) {
		this.actFee = actFee;
	}
	public long getReturnCount() {
		return returnCount;
	}
	public void setReturnCount(long returnCount) {
		this.returnCount = returnCount;
	}
	public long getActCount() {
		return actCount;
	}
	public void setActCount(long actCount) {
		this.actCount = actCount;
	}
	public long getRegCount() {
		return regCount;
	}
	public void setRegCount(long regCount) {
		this.regCount = regCount;
	}
	public long getBindCount() {
		return bindCount;
	}
	public void setBindCount(long bindCount) {
		this.bindCount = bindCount;
	}
	@Override
	public String toString() {
		return "StatsCard [regFee=" + regFee + ", chargeCashFee="
				+ chargeCashFee + ", returnFee=" + returnFee + ", actFee="
				+ actFee + ", returnCount=" + returnCount + ", actCount="
				+ actCount + ", regCount=" + regCount + ", bindCount="
				+ bindCount + "]";
	}

}
