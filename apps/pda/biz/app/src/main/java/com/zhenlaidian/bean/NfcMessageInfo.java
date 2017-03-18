package com.zhenlaidian.bean;

public class NfcMessageInfo {

	// {"mtype":2,"info":{"total":"1.00","duration":"null","carnumber":"京F8KR99",
	// "etime":"20:08","state":"1","btime":"20:03","orderid":"1506"}}
	private String total;// 金额
	private String duration;// 时长
	private String carnumber;// 车牌
	private String etime;// 结束时间
	private String state; // 0未支付.1.待支付.2 支付完成；
	private String btime;// 开始时间
	private String orderid;//订单号

	public NfcMessageInfo() {
		super();
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getCarnumber() {
		return carnumber;
	}

	public void setCarnumber(String carnumber) {
		this.carnumber = carnumber;
	}

	public String getEtime() {
		return etime;
	}

	public void setEtime(String etime) {
		this.etime = etime;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getBtime() {
		return btime;
	}

	public void setBtime(String btime) {
		this.btime = btime;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	@Override
	public String toString() {
		return "NfcMessageInfo [total=" + total + ", duration=" + duration
				+ ", carnumber=" + carnumber + ", etime=" + etime + ", state="
				+ state + ", btime=" + btime + ", orderid=" + orderid + "]";
	}

}
