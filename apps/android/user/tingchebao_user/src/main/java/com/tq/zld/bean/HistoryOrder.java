package com.tq.zld.bean;

public class HistoryOrder {
	// <content count="2">
	// <info>
	// <total>30.00</total>
	// <parkname>中关村信息大厦</parkname>
	// <date>2014-05-25</date>
	// </info>
	// </content>
	private String date;
	private String parkname;
	private String total;
	private String orderid;

	public HistoryOrder() {
		super();
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getParkname() {
		return parkname;
	}

	public void setParkname(String parkname) {
		this.parkname = parkname;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	@Override
	public String toString() {
		return "HistoryOrder [date=" + date + ", parkname=" + parkname
				+ ", total=" + total + ", orderid=" + orderid + "]";
	}
}
