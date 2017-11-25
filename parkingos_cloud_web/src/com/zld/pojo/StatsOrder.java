package com.zld.pojo;

import java.io.Serializable;

public class StatsOrder implements Serializable {
	//统计分类
	private long id = -1;//统计编号
	//订单统计
	private double escapeFee = 0;//逃单未追缴订单金额
	private double sensorFee = 0;//车检器订单金额

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public double getEscapeFee() {
		return escapeFee;
	}
	public void setEscapeFee(double escapeFee) {
		this.escapeFee = escapeFee;
	}
	public double getSensorFee() {
		return sensorFee;
	}
	public void setSensorFee(double sensorFee) {
		this.sensorFee = sensorFee;
	}
	@Override
	public String toString() {
		return "StatsOrder [id=" + id + ", escapeFee=" + escapeFee
				+ ", sensorFee=" + sensorFee + "]";
	}
}
