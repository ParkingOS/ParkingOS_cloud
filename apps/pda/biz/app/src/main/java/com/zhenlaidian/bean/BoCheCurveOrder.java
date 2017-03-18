package com.zhenlaidian.bean;

import java.io.Serializable;

public class BoCheCurveOrder implements Serializable{
	
	
	//返回：{btime:03-25 19:15,time:03-25 20:19,dur:2小时,total:5}
	private String btime;//:开始时间，
	private String etime;//:结束时间
	private String dur;//：时长，以小时计
	private String total;//:收费金额
	public String getBtime() {
		return btime;
	}
	public void setBtime(String btime) {
		this.btime = btime;
	}
	public String getEtime() {
		return etime;
	}
	public void setEtime(String etime) {
		this.etime = etime;
	}
	public String getDur() {
		return dur;
	}
	public void setDur(String dur) {
		this.dur = dur;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public BoCheCurveOrder() {
		super();
	}
	public BoCheCurveOrder(String btime, String etime, String dur, String total) {
		super();
		this.btime = btime;
		this.etime = etime;
		this.dur = dur;
		this.total = total;
	}
	@Override
	public String toString() {
		return "BoCheCurveOrder [btime=" + btime + ", etime=" + etime
				+ ", dur=" + dur + ", total=" + total + "]";
	}
}
