package com.zhenlaidian.printer;

/**
 * Created by zhangyunfei on 15/10/10.
 * 停车宝离场凭条
 */
public class TcbCheckCarOut {
    public final String style = "2,1,2,1";//格式控制字符;
    public final String carout = "  车辆离场凭条\n";
    public final String partline = "- - - - - - - - - - - - - - - ";
    private String carnumber;//车牌号
    private String orderid;//订单号
    private String intime;//入场时间
    private String outtime;//离场时间
    private String duration;//停车时长
    private String meterman;//收费员
    private String colloct;//结算金额

    public TcbCheckCarOut() {
    }

    public TcbCheckCarOut(String orderid,String carnumber, String intime, String outtime, String duration,String meterman, String colloct) {
        this.orderid = orderid;
        this.carnumber = carnumber;
        this.intime = intime;
        this.outtime = outtime;
        this.duration = duration;
        this.meterman = meterman;
        this.colloct = colloct;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getStyle() {
        return style;
    }

    public String getCarnumber() {
        return carnumber;
    }

    public void setCarnumber(String carnumber) {
        this.carnumber = carnumber;
    }

    public String getIntime() {
        return intime;
    }

    public void setIntime(String intime) {
        this.intime = intime;
    }

    public String getOuttime() {
        return outtime;
    }

    public void setOuttime(String outtime) {
        this.outtime = outtime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getMeterman() {
        return meterman;
    }

    public void setMeterman(String meterman) {
        this.meterman = meterman;
    }

    public String getColloct() {
        return colloct;
    }

    public void setColloct(String colloct) {
        this.colloct = colloct;
    }
}
