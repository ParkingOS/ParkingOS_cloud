package com.zhenlaidian.bean;

import java.io.Serializable;
import java.util.Arrays;

//NFC--->结算订单返回的数据时：
//{"info":"1","total":"0.00","duration":"1分钟","carnumber":"车牌号未知","hascard":"1","isedit":"0","etime":"14:18","prepay":"0.0",
//	"handcash":"0","btime":"14:18","uin":"-1","orderid":"786117","collect":"0.0"}

@SuppressWarnings("serial")
public class NfcOrder implements Serializable {

    public String total;// 总金额
    public String etime;// 结束时间
    public String etimestr;// 结束时间含日期
    public String btime;// 开始时间
    public String btimestr;// 开始时间含日期
    public String orderid;// 订单编号
    public String collect;// 结算金额
    public String discount;// 优惠金额
    public String duration;// 时长
    public String uin;// -1未绑定 其他都是绑定
    public String carnumber;// 车牌号
    public String hascard;// 0 没有车牌 1 有车牌
    public String handcash;// 0 不按次手输结算；1 按次手输结算；
    public String collect0;// 按次收费；
    public String limitday;// 按月卡收费；
    public double[] collect1;// 多价格按次；
    public String[] cards;// 多车牌；
    public String isedit;// 按时价格是否可编辑 0否，1是
    public String info;// 刷卡返回的操作类型；-1卡位注册；-2有逃单；-3订单已支付;2直接结算；1结算定单；3土桥生成订单；0生成订单；
    public String own;// 自己车场的逃单数；
    public String other;// 别人车场的逃单数；
    public String ctime;// 直接结算订单创建时间；
    public String uuid;// 直接结算订单回传uuid；
    public String prepay;// 预付费金额；
    private String netError;// 网络错误原因（跟服务器无关字段）
    private String isfast;// 是否极速通，0否1是
    private String errmsg;// 错误原因


    public NfcOrder() {
        super();
    }

    public String getCarnumber() {
        return carnumber;
    }

    public String getPrepay() {
        return prepay;
    }

    public void setPrepay(String prepay) {
        this.prepay = prepay;
    }

    public void setCarnumber(String carnumber) {
        this.carnumber = carnumber;
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

    public String getIsfast() {
        return isfast;
    }

    public void setIsfast(String isfast) {
        this.isfast = isfast;
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

    public double[] getCollect1() {
        return collect1;
    }

    public void setCollect1(double[] collect1) {
        this.collect1 = collect1;
    }

    public String getIsedit() {
        return isedit;
    }

    public void setIsedit(String isedit) {
        this.isedit = isedit;
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

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNetError() {
        return netError;
    }

    public void setNetError(String netError) {
        this.netError = netError;
    }

    public String[] getCards() {
        return cards;
    }

    public void setCards(String[] cards) {
        this.cards = cards;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getEtimestr() {
        return etimestr;
    }

    public void setEtimestr(String etimestr) {
        this.etimestr = etimestr;
    }

    public String getBtimestr() {
        return btimestr;
    }

    public void setBtimestr(String btimestr) {
        this.btimestr = btimestr;
    }

    @Override
    public String toString() {
        return "NfcOrder{" +
                "total='" + total + '\'' +
                ", etime='" + etime + '\'' +
                ", etimestr='" + etimestr + '\'' +
                ", btime='" + btime + '\'' +
                ", btimestr='" + btimestr + '\'' +
                ", orderid='" + orderid + '\'' +
                ", collect='" + collect + '\'' +
                ", discount='" + discount + '\'' +
                ", duration='" + duration + '\'' +
                ", uin='" + uin + '\'' +
                ", carnumber='" + carnumber + '\'' +
                ", hascard='" + hascard + '\'' +
                ", handcash='" + handcash + '\'' +
                ", collect0='" + collect0 + '\'' +
                ", limitday='" + limitday + '\'' +
                ", collect1=" + Arrays.toString(collect1) +
                ", cards=" + Arrays.toString(cards) +
                ", isedit='" + isedit + '\'' +
                ", info='" + info + '\'' +
                ", own='" + own + '\'' +
                ", other='" + other + '\'' +
                ", ctime='" + ctime + '\'' +
                ", uuid='" + uuid + '\'' +
                ", prepay='" + prepay + '\'' +
                ", netError='" + netError + '\'' +
                ", isfast='" + isfast + '\'' +
                ", errmsg='" + errmsg + '\'' +
                '}';
    }
}
