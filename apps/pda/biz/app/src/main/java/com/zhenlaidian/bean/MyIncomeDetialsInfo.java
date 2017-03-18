package com.zhenlaidian.bean;

import java.util.ArrayList;

public class MyIncomeDetialsInfo {

	// {"total":"117.08","info":[{"money":"0.01","mtype":"0","create_time":"1420342299","note":"停车费","target":"京G00000"}]}
	String total;
	ArrayList<QueryAccountDetail> info;

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public ArrayList<QueryAccountDetail> getInfo() {
		return info;
	}

	public void setInfo(ArrayList<QueryAccountDetail> info) {
		this.info = info;
	}

	public MyIncomeDetialsInfo() {
		super();
	}

	@Override
	public String toString() {
		return "MyIncomeDetialsInfo [total=" + total + ", info=" + info + "]";
	}

}
