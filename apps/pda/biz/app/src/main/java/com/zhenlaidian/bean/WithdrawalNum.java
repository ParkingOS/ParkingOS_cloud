package com.zhenlaidian.bean;

public class WithdrawalNum {

	private String result;
	private String times;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getTimes() {
		return times;
	}

	public void setTimes(String times) {
		this.times = times;
	}

	@Override
	public String toString() {
		return "WithdrawalNum [result=" + result + ", times=" + times + "]";
	}

	public WithdrawalNum() {
		super();
		// TODO Auto-generated constructor stub
	}

}
