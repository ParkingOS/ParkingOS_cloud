package com.zhenlaidian.bean;

import java.io.Serializable;

public class Order implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String money;// 金额
	private String plateNunber;// 车牌号
	private String mobileNo;// 手机号
	private String orderNo;// 订单号
	private String orderStatus;// 订单状态
	private String inTime;// 入场时间
	private String price;// 车场价格 元/小时
	private String parkingTime;// 停车时间
	private String alreadyTime;// 已停时间
	private String timeQuantum;//停车时间段
	private String outTime; 
	public Order(String money, String plateNunber, String mobileNo,
			String orderNo, String orderStatus, String inTime, String price,
			String parkingTime, String alreadyTime,String timeQuantum,String outTime) {
		super();
		this.money = money;
		this.plateNunber = plateNunber;
		this.mobileNo = mobileNo;
		this.orderNo = orderNo;
		this.orderStatus = orderStatus;
		this.inTime = inTime;
		this.price = price;
		this.parkingTime = parkingTime;
		this.alreadyTime = alreadyTime;
		this.timeQuantum = timeQuantum;
		this.outTime = outTime;
	}

	public Order() {
		super();
	}
	
	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public String getPlateNunber() {
		return plateNunber;
	}

	public void setPlateNunber(String plateNunber) {
		this.plateNunber = plateNunber;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public String getInTime() {
		return inTime;
	}

	public void setInTime(String inTime) {
		this.inTime = inTime;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getParkingTime() {
		return parkingTime;
	}

	public void setParkingTime(String parkingTime) {
		this.parkingTime = parkingTime;
	}

	public String getAlreadyTime() {
		return alreadyTime;
	}

	public void setAlreadyTime(String alreadyTime) {
		this.alreadyTime = alreadyTime;
	}

	public String getTimeQuantum() {
		return timeQuantum;
	}

	public void setTimeQuantum(String timeQuantum) {
		this.timeQuantum = timeQuantum;
	}

	
	public String getOutTime() {
		return outTime;
	}

	public void setOutTime(String outTime) {
		this.outTime = outTime;
	}

	@Override
	public String toString() {
		return "Order [money=" + money + ", plateNunber=" + plateNunber
				+ ", mobileNo=" + mobileNo + ", orderNo=" + orderNo
				+ ", orderStatus=" + orderStatus + ", inTime=" + inTime
				+ ", price=" + price + ", parkingTime=" + parkingTime
				+ ", alreadyTime=" + alreadyTime + ", timeQuantum="
				+ timeQuantum +outTime + "]";
	}

	


}
