package com.zhenlaidian.bean;

/**
 * Created by zhangyunfei on 15/10/14.
 */
public class InCarDialogInfo {
//    ｛result:0,errmsg:"",qrcode:kkkelwef99,orderid:122121｝
//    result:0失败，1成功，errmsg:提示内容，qrcode：二维码，orderid:订单编号

    public String result;
    public String errmsg;
    public String qrcode;
    public String orderid;
    public String btime;

    public InCarDialogInfo() {
    }

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

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
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
        return "InCarDialogInfo{" +
                "result='" + result + '\'' +
                ", errmsg='" + errmsg + '\'' +
                ", qrcode='" + qrcode + '\'' +
                ", orderid='" + orderid + '\'' +
                ", btime='" + btime + '\'' +
                '}';
    }
}
