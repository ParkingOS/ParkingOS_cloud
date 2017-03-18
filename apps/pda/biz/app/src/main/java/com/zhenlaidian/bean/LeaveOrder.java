package com.zhenlaidian.bean;

import java.io.Serializable;

@SuppressWarnings("serial")
public class LeaveOrder implements Serializable {
	//
	// <content id='1'>
	// <info>
	// <id>142</id>
	// <issale>1</issale>
	// <total>12.00</total>
	// <carnumber>京A54321</carnumber>
	// <etime>14:17</etime>
	// <btime>10:46</btime>
	// <state>0</state>
	// <mtype>1</mtype>
	// <orderid>47</orderid>
	// </info>
	// <info>
	// {"mtype":2,"info":{"total":"1.00","duration":"null","carnumber":"京F8KR99","etime":"20:08","state":"1",
	// "btime":"20:03","orderid":"1506"}}
	private String state;// 0未支付.1.待支付 2.已支付，-1结算失败；
	private String carnumber;// 车牌
	private String total;// 金额
	private String btime;// 开始时间
	private String etime;// 开始时间
	private int id;// 消息编号
	private String orderid;// 订单号
	private String mtype;// 消息类型 -1 token无效 0 离场订单；
	private String issale;// 是否优惠 0否.1是
	private int maxid;// 当前最大的curri；
	private String duration;// 泊车距离
	private String prefee;// BLE照牌结算参数-预支付金额；
	private String collect;// BLE照牌结算参数-补差价；
	private String errmsg;// BLE照牌结算参数--错误信息；
	private String rcount;// 当天的打赏次数；
	private String uin;// 车主编号；
	private String limit;// 积分是否达到上限；0未达到。1达到上限
	private String mobile;// 推荐奖（被推荐的车主）
	private String fivelimit;// 0可继续发5元券 1：不能发
	private String fivescore;// 此次发5元券消耗积分
	private String message;//消息内容

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCarnumber() {
		return carnumber;
	}

	public void setCarnumber(String carnumber) {
		this.carnumber = carnumber;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getBtime() {
		return btime;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public String getMtype() {
		return mtype;
	}

	public void setMtype(String mtype) {
		this.mtype = mtype;
	}

	public String getIssale() {
		return issale;
	}

	public void setIssale(String issale) {
		this.issale = issale;
	}

	public int getMaxid() {
		return maxid;
	}

	public void setMaxid(int maxid) {
		this.maxid = maxid;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getFivelimit() {
		return fivelimit;
	}

	public void setFivelimit(String fivelimit) {
		this.fivelimit = fivelimit;
	}

	public String getFivescore() {
		return fivescore;
	}

	public void setFivescore(String fivescore) {
		this.fivescore = fivescore;
	}

	public String getPrefee() {
		return prefee;
	}

	public void setPrefee(String prefee) {
		this.prefee = prefee;
	}

	public String getCollect() {
		return collect;
	}

	public void setCollect(String collect) {
		this.collect = collect;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	public LeaveOrder() {
		super();
	}

	public String getRcount() {
		return rcount;
	}

	public void setRcount(String rcount) {
		this.rcount = rcount;
	}

	public String getUin() {
		return uin;
	}

	public void setUin(String uin) {
		this.uin = uin;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	@Override
	public String toString() {
		return "LeaveOrder{" +
				"state='" + state + '\'' +
				", carnumber='" + carnumber + '\'' +
				", total='" + total + '\'' +
				", btime='" + btime + '\'' +
				", etime='" + etime + '\'' +
				", id=" + id +
				", orderid='" + orderid + '\'' +
				", mtype='" + mtype + '\'' +
				", issale='" + issale + '\'' +
				", maxid=" + maxid +
				", duration='" + duration + '\'' +
				", prefee='" + prefee + '\'' +
				", collect='" + collect + '\'' +
				", errmsg='" + errmsg + '\'' +
				", rcount='" + rcount + '\'' +
				", uin='" + uin + '\'' +
				", limit='" + limit + '\'' +
				", mobile='" + mobile + '\'' +
				", fivelimit='" + fivelimit + '\'' +
				", fivescore='" + fivescore + '\'' +
				", message='" + message + '\'' +
				'}';
	}

}
