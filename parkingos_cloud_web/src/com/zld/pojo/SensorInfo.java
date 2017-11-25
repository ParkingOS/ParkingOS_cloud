package com.zld.pojo;

import java.io.Serializable;

public class SensorInfo implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private String id;
	private int status = 0;//0：无车 1：有车
	private int x0 = 0;//车检器初始化X0
	private int y0 = 0;//车检器初始化Y0
	private int z0 = 0;//车检器初始化Z0
	private int x = 0;//车检器最新X
	private int y = 0;//车检器最新Y
	private int z = 0;//车检器最新Z
	private int rate = -1;//有车的概率值
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getX0() {
		return x0;
	}
	public void setX0(int x0) {
		this.x0 = x0;
	}
	public int getY0() {
		return y0;
	}
	public void setY0(int y0) {
		this.y0 = y0;
	}
	public int getZ0() {
		return z0;
	}
	public void setZ0(int z0) {
		this.z0 = z0;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getZ() {
		return z;
	}
	public void setZ(int z) {
		this.z = z;
	}
	public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	@Override
	public String toString() {
		return "SensorInfo [id=" + id + ", status=" + status + ", x0=" + x0
				+ ", y0=" + y0 + ", z0=" + z0 + ", x=" + x + ", y=" + y
				+ ", z=" + z + ", rate=" + rate + "]";
	}
}
