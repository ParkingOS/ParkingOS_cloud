package com.zhenlaidian.printer;

/**
 * Created by zhangyunfei on 15/10/10.
 * 停车宝入场凭条
 */
public class TcbCheckCarIn {
    //byte[] btTexts = JBluetoothEnCoder.EnCodeStringToPrintBytes("2,1,1",
    // "  车辆离场凭条\n", "..............................\n车牌号:京A44944\n",
    // "时间:10.11 19:30\n管理员:黄大锤\n.............................");

    public final String style= "2,1";//格式控制字符;
    public final String carin = "  车辆入场凭条\n";
    public final String partline = "- - - - - - - - - - - - - - - ";
    private String carnumber;//车牌号
    private String orderid;//订单号
    private String time;//入场时间
    private String meterman;//收费员

    public TcbCheckCarIn() {
    }

    public TcbCheckCarIn(String orderid,String carnumber, String time, String meterman) {
        this.orderid = orderid;
        this.carnumber = carnumber;
        this.time = time;
        this.meterman = meterman;
    }



    public String getCarnumber() {
        return carnumber;
    }

    public void setCarnumber(String carnumber) {
        this.carnumber = carnumber;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMeterman() {
        return meterman;
    }

    public void setMeterman(String meterman) {
        this.meterman = meterman;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }
}
