package com.zhenlaidian.bean;

import java.util.List;

public class OnekeyQueryParkInfo {

	// {"total（总收费）":20275.81,"mmoeny（手机收费）":20270.81,"cashmoney（现金收费）":5.0,"detail（各收费员收费详情）":
	// [{"nickname":"黄世辉","total（手机支 付）":"342.64","uid":"11802","cash（现金）":"5.0"}]}

	public String total;
	public String mmoeny;
	public String cashmoney;
	public List<ParkeEmployeeInfo> detail;

	public OnekeyQueryParkInfo() {
		super();
	}

	public OnekeyQueryParkInfo(String total, String mmoeny, String cashmoney, List<ParkeEmployeeInfo> detail) {
		super();
		this.total = total;
		this.mmoeny = mmoeny;
		this.cashmoney = cashmoney;
		this.detail = detail;
	}

	public List<ParkeEmployeeInfo> getDetail() {
		return detail;
	}

	public void setDetail(List<ParkeEmployeeInfo> detail) {
		this.detail = detail;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getMmoeny() {
		return mmoeny;
	}

	public void setMmoeny(String mmoeny) {
		this.mmoeny = mmoeny;
	}

	public String getCashmoney() {
		return cashmoney;
	}

	public void setCashmoney(String cashmoney) {
		this.cashmoney = cashmoney;
	}

	@Override
	public String toString() {
		return "OnekeyQueryParkInfo [total=" + total + ", mmoeny=" + mmoeny + ", cashmoney=" + cashmoney + ", detail=" + detail
				+ "]";
	}

}
