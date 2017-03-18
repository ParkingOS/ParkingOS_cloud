package com.zhenlaidian.bean;

import java.io.Serializable;

//6.//订单详情
//<?xml version="1.0" encoding="gb2312" ?> 
//- <content>
//  <total>0.0</total> 
//  <carnumber>京GPS223</carnumber> 
//  <state>已结算</state> 
//  <orderid>1</orderid> 
//  <end>1400839200</end> 
//  <mobile>15801482643</mobile> 
//  <begin>1400781600</begin> 
//  </content>


@SuppressWarnings("serial")
public class AllOrder implements Serializable {
    private String total;// 总金额
    private String duration;// 停车时长
    private String carnumber;// 车牌号
    private String btime;// 停车开始时间
    private String ordercount;// 订单总数
    private String errmsg;// 错误原因

    private String state;// 结算状态;
    private String id;// 订单号
    private String orderid;// 订单号(订单详情返回字段)
    private String end;// 结束时间
    private String begin;// 开始时间
    private String mobile;// 手机号
    private String ptype;// 支付方式
    private String uin;// 是否是会员
    private String isfast;// 极速通类型1照牌，2取卡
    private String showepay;// ==4停车时长显示直接支付
    private String park;//停车位名字;
    //2016.4.20 增加
    private String prepay;//预付
    private String berthnumber;//泊位编号

    private String ismonthuser;
    //2016.11.1增加
    private String car_type;
    //2016.11.18 增加 is_card
    private String is_card;
    public AllOrder() {
        super();
    }

    @Override
    public String toString() {
        return "AllOrder{" +
                "total='" + total + '\'' +
                ", duration='" + duration + '\'' +
                ", carnumber='" + carnumber + '\'' +
                ", btime='" + btime + '\'' +
                ", ordercount='" + ordercount + '\'' +
                ", errmsg='" + errmsg + '\'' +
                ", state='" + state + '\'' +
                ", id='" + id + '\'' +
                ", orderid='" + orderid + '\'' +
                ", end='" + end + '\'' +
                ", begin='" + begin + '\'' +
                ", mobile='" + mobile + '\'' +
                ", ptype='" + ptype + '\'' +
                ", uin='" + uin + '\'' +
                ", isfast='" + isfast + '\'' +
                ", showepay='" + showepay + '\'' +
                ", park='" + park + '\'' +
                ", prepay='" + prepay + '\'' +
                ", berthnumber='" + berthnumber + '\'' +
                ", ismonthuser='" + ismonthuser + '\'' +
                ", car_type='" + car_type + '\'' +
                ", is_card='" + is_card + '\'' +
                '}';
    }

    public String getCar_type() {
        return car_type;
    }

    public void setCar_type(String car_type) {
        this.car_type = car_type;
    }

    public String getIsmonthuser() {
        return ismonthuser;
    }

    public void setIsmonthuser(String ismonthuser) {
        this.ismonthuser = ismonthuser;
    }

    public String getBerthnumber() {
        return berthnumber;
    }

    public void setBerthnumber(String berthnumber) {
        this.berthnumber = berthnumber;
    }

    public String getPrepay() {
        return prepay;
    }

    public void setPrepay(String prepay) {
        this.prepay = prepay;
    }

    public String getOrdercount() {
        return ordercount;
    }

    public void setOrdercount(String ordercount) {
        this.ordercount = ordercount;
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

    public String getBtime() {
        return btime;
    }

    public void setBtime(String btime) {
        this.btime = btime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPtype() {
        return ptype;
    }

    public void setPtype(String ptype) {
        this.ptype = ptype;
    }

    public String getUin() {
        return uin;
    }

    public void setUin(String uin) {
        this.uin = uin;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getIsfast() {
        return isfast;
    }

    public void setIsfast(String isfast) {
        this.isfast = isfast;
    }

    public String getShowepay() {
        return showepay;
    }

    public void setShowepay(String showepay) {
        this.showepay = showepay;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public String getPark() {
        return park;
    }

    public void setPark(String park) {
        this.park = park;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getIs_card() {
        return is_card;
    }

    public void setIs_card(String is_card) {
        this.is_card = is_card;
    }
}
