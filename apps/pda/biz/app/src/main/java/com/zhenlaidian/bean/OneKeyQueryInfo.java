package com.zhenlaidian.bean;

public class OneKeyQueryInfo {

	// {"totalmoney":0.0,"mobilemoney":0.0,"cashmoney":0.0,"mycount":0.0,"parkaccout":0.0,"timeordercount":0,
	// "timeordermoney":0.0,"epayordercount":0,"epaymoney":0.0}

	private String totalmoney;// 今日共收费
	private String mobilemoney;// 手机收费
	private String cashmoney;// 现金收费
	private String mycount;// 我的账户
	private String parkaccout;// 车场账户
	private String timeordercount;// 计时计费订单数
	private String timeordermoney;// 计时计费钱
	private String epayordercount;// 直付订单数
	private String epaymoney;// 直付钱

	public OneKeyQueryInfo() {
		super();
	}

	public OneKeyQueryInfo(String totalmoney, String mobilemoney, String cashmoney, String mycount, String parkaccout,
			String timeordercount, String timeordermoney, String epayordercount, String epaymoney) {
		super();
		this.totalmoney = totalmoney;
		this.mobilemoney = mobilemoney;
		this.cashmoney = cashmoney;
		this.mycount = mycount;
		this.parkaccout = parkaccout;
		this.timeordercount = timeordercount;
		this.timeordermoney = timeordermoney;
		this.epayordercount = epayordercount;
		this.epaymoney = epaymoney;
	}

	public String getTotalmoney() {
		return totalmoney;
	}

	public void setTotalmoney(String totalmoney) {
		this.totalmoney = totalmoney;
	}

	public String getMobilemoney() {
		return mobilemoney;
	}

	public void setMobilemoney(String mobilemoney) {
		this.mobilemoney = mobilemoney;
	}

	public String getCashmoney() {
		return cashmoney;
	}

	public void setCashmoney(String cashmoney) {
		this.cashmoney = cashmoney;
	}

	public String getMycount() {
		return mycount;
	}

	public void setMycount(String mycount) {
		this.mycount = mycount;
	}

	public String getParkaccout() {
		return parkaccout;
	}

	public void setParkaccout(String parkaccout) {
		this.parkaccout = parkaccout;
	}

	public String getTimeordercount() {
		return timeordercount;
	}

	public void setTimeordercount(String timeordercount) {
		this.timeordercount = timeordercount;
	}

	public String getTimeordermoney() {
		return timeordermoney;
	}

	public void setTimeordermoney(String timeordermoney) {
		this.timeordermoney = timeordermoney;
	}

	public String getEpayordercount() {
		return epayordercount;
	}

	public void setEpayordercount(String epayordercount) {
		this.epayordercount = epayordercount;
	}

	public String getEpaymoney() {
		return epaymoney;
	}

	public void setEpaymoney(String epaymoney) {
		this.epaymoney = epaymoney;
	}

	@Override
	public String toString() {
		return "OneKeyQueryInfo [totalmoney=" + totalmoney + ", mobilemoney=" + mobilemoney + ", cashmoney=" + cashmoney
				+ ", mycount=" + mycount + ", parkaccout=" + parkaccout + ", timeordercount=" + timeordercount
				+ ", timeordermoney=" + timeordermoney + ", epayordercount=" + epayordercount + ", epaymoney=" + epaymoney + "]";
	}

}
