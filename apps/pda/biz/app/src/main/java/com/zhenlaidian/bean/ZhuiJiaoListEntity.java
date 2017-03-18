package com.zhenlaidian.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by TCB on 2016/4/21.
 * xulu
 * 逃单记录
 */
public class ZhuiJiaoListEntity implements Serializable {
    /**
     * "result":"0",
     * errmsg
     * "orders":[
     * {
     * "orderid":"841808",
     * "start":"1461206101",
     * "end":"1461206247",
     * "car_number":"沪F55555",
     * "total":"1.00",
     * "prepay":"1.0",
     * "duartion":"2分钟"
     * }
     * ]
     */

    String result;
    String errmsg;
    ArrayList<ZhuiJiaoItemEntity> orders;
    //2016.5.10增加 返回是否会员，5代表会员
    String ismonthuser;
    //2016.5.12增加 预取编号
    String orderid;

    public String getIsmonthuser() {
        return ismonthuser;
    }

    public void setIsmonthuser(String ismonthuser) {
        this.ismonthuser = ismonthuser;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public ArrayList<ZhuiJiaoItemEntity> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<ZhuiJiaoItemEntity> orders) {
        this.orders = orders;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    @Override
    public String toString() {
        return "ZhuiJiaoListEntity{" +
                "result='" + result + '\'' +
                ", errmsg='" + errmsg + '\'' +
                ", orders=" + orders +
                ", ismonthuser='" + ismonthuser + '\'' +
                ", orderid='" + orderid + '\'' +
                '}';
    }
}
