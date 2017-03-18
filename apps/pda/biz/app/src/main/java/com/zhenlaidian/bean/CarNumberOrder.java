package com.zhenlaidian.bean;

public class CarNumberOrder {
//	{"total":"4.0","carnumber":"川ZL1Z11","duration":"21分钟","etime":"19:58","btime":"19:37","uin":"-1","orderid":"48260","collect":"2.0","discount":"2.0"}
	
	public String total;//总金额
	public String carnumber;//车牌号
	public String etime;//结束时间
	public String btime;//开始时间
	public String orderid;//订单编号
	public String collect;//结算金额
	public String discount;//优惠金额
	public String duration;//时长
	public String uin;//-1未绑定 其他都是绑定
	public String hascard;//是否有车牌
	public String handcash;//0 不按次手输结算；1 按次手输结算；
	public String collect0;//按次收费；
	public String limitday;//按月卡收费；
	
	public CarNumberOrder() {
		super();
	}
	
	public String getHascard() {
		return hascard;
	}

	public void setHascard(String hascard) {
		this.hascard = hascard;
	}

	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
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
	public String getCollect() {
		return collect;
	}
	public void setCollect(String collect) {
		this.collect = collect;
	}
	public String getDiscount() {
		return discount;
	}
	public void setDiscount(String discount) {
		this.discount = discount;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getUin() {
		return uin;
	}
	public void setUin(String uin) {
		this.uin = uin;
	}

	public String getHandcash() {
		return handcash;
	}

	public void setHandcash(String handcash) {
		this.handcash = handcash;
	}

	public String getCollect0() {
		return collect0;
	}

	public void setCollect0(String collect0) {
		this.collect0 = collect0;
	}

	public String getLimitday() {
		return limitday;
	}

	public void setLimitday(String limitday) {
		this.limitday = limitday;
	}

	@Override
	public String toString() {
		return "CarNumberOrder [total=" + total + ", carnumber=" + carnumber
				+ ", etime=" + etime + ", btime=" + btime + ", orderid="
				+ orderid + ", collect=" + collect + ", discount=" + discount
				+ ", duration=" + duration + ", uin=" + uin + ", hascard="
				+ hascard + ", handcash=" + handcash + ", collect0=" + collect0
				+ ", limitday=" + limitday + "]";
	}




	
}
