package com.tq.zld.bean;

public class IbeaconInOutInfo {

//	返回：{"inout":"0","uid":"-1",} inout:入场/出场，0入口，1出口 -1通道不存在，2进出一体车场    orderID=0生成订单那大于零就结算；
//	uid:收费员编号，不处理，生成或结算订单时传回orderid=0没有订单。有具体值就是订单编号；
	public String inout;
	public String uid;
	public String orderid;
	public String name;
	public String parkname;
	public IbeaconInOutInfo() {
		super();
	}
	public String getInout() {
		return inout;
	}
	public void setInout(String inout) {
		this.inout = inout;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getOrderid() {
		return orderid;
	}
	public void setOrderid(String orderid) {
		this.orderid = orderid;
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
	@Override
	public String toString() {
		return "IbeaconInOutInfo [inout=" + inout + ", uid=" + uid + ", orderid=" + orderid
				+ ", name=" + name + ", parkname=" + parkname + "]";
	}
	
	
}
