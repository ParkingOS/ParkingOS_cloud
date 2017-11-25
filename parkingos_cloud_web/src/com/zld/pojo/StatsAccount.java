package com.zld.pojo;

import java.io.Serializable;

public class StatsAccount implements Serializable {
	//统计分类
	private long id = -1;//统计编号
	//账目统计
	private double parkingFee = 0;//停车费（非预付）
	private double prepayFee = 0;//预付停车费
	private double refundFee = 0;//预付退款（预付超额）
	private double addFee = 0;//预付补缴（预付不足）
	private double pursueFee = 0;//追缴停车费

	public double getParkingFee() {
		return parkingFee;
	}
	public void setParkingFee(double parkingFee) {
		this.parkingFee = parkingFee;
	}
	public double getPrepayFee() {
		return prepayFee;
	}
	public void setPrepayFee(double prepayFee) {
		this.prepayFee = prepayFee;
	}
	public double getRefundFee() {
		return refundFee;
	}
	public void setRefundFee(double refundFee) {
		this.refundFee = refundFee;
	}
	public double getAddFee() {
		return addFee;
	}
	public void setAddFee(double addFee) {
		this.addFee = addFee;
	}
	public double getPursueFee() {
		return pursueFee;
	}
	public void setPursueFee(double pursueFee) {
		this.pursueFee = pursueFee;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return "StatsAccount [id=" + id + ", parkingFee=" + parkingFee
				+ ", prepayFee=" + prepayFee + ", refundFee=" + refundFee
				+ ", addFee=" + addFee + ", pursueFee=" + pursueFee + "]";
	}

}
