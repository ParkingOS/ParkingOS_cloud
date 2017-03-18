package com.zhenlaidian.bean;

//<?xml version="1.0" encoding="gb2312"?><content><total>26</total><free>17</free><busy>9</busy></content>
public class ShaerUiInfo {
	private String total;
	private String  free;
	private String  busy;
	public ShaerUiInfo() {
		super();
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public String getFree() {
		return free;
	}
	public void setFree(String free) {
		this.free = free;
	}
	public String getBusy() {
		return busy;
	}
	public void setBusy(String busy) {
		this.busy = busy;
	}
	@Override
	public String toString() {
		return "ShaerUiInfo [total=" + total + ", free=" + free + ", busy="
				+ busy + "]";
	}

	
}
