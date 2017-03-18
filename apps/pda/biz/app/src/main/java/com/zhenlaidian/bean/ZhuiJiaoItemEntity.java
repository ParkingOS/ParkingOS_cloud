package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Created by TCB on 2016/4/21.
 * xulu
 * 逃单记录
 */
public class ZhuiJiaoItemEntity implements Serializable{
    /**
     * orders":[
     {
     "orderid":"841808",
     "start":"1461206101",
     "end":"1461206247",
     "car_number":"沪F55555",
     "total":"1.00",
     "prepay":"1.0",
     "duartion":"2分钟"
     berth_name
     2016.8.18增加 欠费图片
     picurls:string,string,string
     */

    String orderid;
    String start;
    String end;
    String car_number;
    String total;
    String prepay;
    String duartion;
    String berthsec_name;
    boolean ischeck;
    String picurls;

    @Override
    public String toString() {
        return "ZhuiJiaoItemEntity{" +
                "orderid='" + orderid + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", car_number='" + car_number + '\'' +
                ", total='" + total + '\'' +
                ", prepay='" + prepay + '\'' +
                ", duartion='" + duartion + '\'' +
                ", berthsec_name='" + berthsec_name + '\'' +
                ", ischeck=" + ischeck +
                ", picurls='" + picurls + '\'' +
                '}';
    }

    public String getPicurls() {
        return picurls;
    }

    public void setPicurls(String picurls) {
        this.picurls = picurls;
    }

    public String getBerthsec_name() {
        return berthsec_name;
    }

    public void setBerthsec_name(String berthsec_name) {
        this.berthsec_name = berthsec_name;
    }

    public boolean ischeck() {
        return ischeck;
    }

    public void setIscheck(boolean ischeck) {
        this.ischeck = ischeck;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getCar_number() {
        return car_number;
    }

    public void setCar_number(String car_number) {
        this.car_number = car_number;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getPrepay() {
        return prepay;
    }

    public void setPrepay(String prepay) {
        this.prepay = prepay;
    }

    public String getDuartion() {
        return duartion;
    }

    public void setDuartion(String duartion) {
        this.duartion = duartion;
    }
}
