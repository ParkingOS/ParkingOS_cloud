package com.zhenlaidian.bean;

public class IbeaconCashInfo {
//	返回：{\"result\":\"2\",\"info\":\"已支付过，不能重复支付\"}
//	result 0失败 1成功 2错误：重复支付 
//	info:提示信息
	public String result;
	public String info;
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public IbeaconCashInfo() {
		super();
	}
	@Override
	public String toString() {
		return "IbeaconCashInfo [result=" + result + ", info=" + info + "]";
	}
	
	
}
