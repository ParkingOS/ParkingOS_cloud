package com.zld.pojo;

import java.io.Serializable;

public class GenPosOrderResp extends BaseResp implements Serializable {
	private Long orderid = -1L;
	private String btime;//停车开始时间
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderid) {
		this.orderid = orderid;
	}
	public String getBtime() {
		return btime;
	}
	public void setBtime(String btime) {
		this.btime = btime;
	}
	@Override
	public String toString() {
		return "GenOrderResp [orderid=" + orderid + ", btime=" + btime + "]";
	}
	
}
