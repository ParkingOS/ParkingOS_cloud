package com.zhenlaidian.bean;

public class CarNumberMadeOrder {

	String info;//0 -生成订单失败！
	String own;//自己车场逃单
	String other;//别的车场逃单
	public CarNumberMadeOrder() {
		super();
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getOwn() {
		return own;
	}
	public void setOwn(String own) {
		this.own = own;
	}
	public String getOther() {
		return other;
	}
	public void setOther(String other) {
		this.other = other;
	}
	@Override
	public String toString() {
		return "CarNumberMadeOrder [info=" + info + ", own=" + own + ", other="
				+ other + "]";
	}
	
	
	
}
