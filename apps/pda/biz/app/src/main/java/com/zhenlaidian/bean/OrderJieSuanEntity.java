package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Created by TCB on 2016/4/20.
 * xulu
 * 现金结算订单返回的object
 */
public class OrderJieSuanEntity implements Serializable{

    String result;  //1成功，其他失败
    String errmsg; //弹出消息
    String mesg;


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getMesg() {
        return mesg;
    }

    public void setMesg(String mesg) {
        this.mesg = mesg;
    }

    @Override
    public String toString() {
        return "OrderJieSuanEntity{" +
                "result='" + result + '\'' +
                ", errmsg='" + errmsg + '\'' +
                ", mesg='" + mesg + '\'' +
                '}';
    }
}
