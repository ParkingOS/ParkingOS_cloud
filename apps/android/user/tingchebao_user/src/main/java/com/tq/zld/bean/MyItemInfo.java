package com.tq.zld.bean;

public class MyItemInfo {
	private String limitdate;
	private String limitday;
	private String limittime;
	private String name;
	private String parkname;
	private String price;

	public MyItemInfo() {
		super();
	}

	public String getLimitdate() {
		return limitdate;
	}

	public void setLimitdata(String limitdate) {
		this.limitdate = limitdate;
	}

	public String getLimitday() {
		return limitday;
	}

	public void setLimitday(String limitday) {
		this.limitday = limitday;
	}

	public String getLimittime() {
		return limittime;
	}

	public void setLimittime(String limittime) {
		this.limittime = limittime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParkname() {
		return parkname;
	}

	public void setParkname(String parkname) {
		this.parkname = parkname;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "MyItemInfo [limitdata=" + limitdate + ", limitday=" + limitday
				+ ", limittime=" + limittime + ", name=" + name + ", parkname="
				+ parkname + ",  price=" + price + "]";
	}

}
