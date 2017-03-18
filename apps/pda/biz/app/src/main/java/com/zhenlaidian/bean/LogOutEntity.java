package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Created by TCB on 2016/4/20.
 * xulu
 */
public class LogOutEntity implements Serializable{
    String onwork_time; //上岗时间
    String outwork_time;//离岗时间
    String incar; //进车
    String outcar;//出车
    String total_fee;//现金总收费
    String prepay;//预付
    String history_prepay;//垫付--历史预付，2016.6.22海祥改为垫付
    String upmoney;//上交
    String epay;//电子支付
    String result;
    //2016.6.6增加
    String pursue_cash;//追缴现金
    String pursue_epay;//追缴电子支付
	//2016.6.22增加
	String pursue;//追缴总额
    //2016.6.24增加 出场缴费=实收+拒缴费用
    String rece_fee;//出场缴费
    //2016.6.25增加 card_pay 刷卡收费
    String card_pay;

    @Override
    public String toString() {
        return "LogOutEntity{" +
                "onwork_time='" + onwork_time + '\'' +
                ", outwork_time='" + outwork_time + '\'' +
                ", incar='" + incar + '\'' +
                ", outcar='" + outcar + '\'' +
                ", total_fee='" + total_fee + '\'' +
                ", prepay='" + prepay + '\'' +
                ", history_prepay='" + history_prepay + '\'' +
                ", upmoney='" + upmoney + '\'' +
                ", epay='" + epay + '\'' +
                ", result='" + result + '\'' +
                ", pursue_cash='" + pursue_cash + '\'' +
                ", pursue_epay='" + pursue_epay + '\'' +
                ", pursue='" + pursue + '\'' +
                ", rece_fee='" + rece_fee + '\'' +
                ", card_pay='" + card_pay + '\'' +
                '}';
    }

    public String getCard_pay() {
        return card_pay;
    }

    public void setCard_pay(String card_pay) {
        this.card_pay = card_pay;
    }

    public String getRece_fee() {
        return rece_fee;
    }

    public void setRece_fee(String rece_fee) {
        this.rece_fee = rece_fee;
    }

    public String getPursue() {
        return pursue;
    }

    public void setPursue(String pursue) {
        this.pursue = pursue;
    }

    public String getPursue_cash() {
        return pursue_cash;
    }

    public void setPursue_cash(String pursue_cash) {
        this.pursue_cash = pursue_cash;
    }

    public String getPursue_epay() {
        return pursue_epay;
    }

    public void setPursue_epay(String pursue_epay) {
        this.pursue_epay = pursue_epay;
    }

    public String getEpay() {
        return epay;
    }

    public void setEpay(String epay) {
        this.epay = epay;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getOnwork_time() {
        return onwork_time;
    }

    public void setOnwork_time(String onwork_time) {
        this.onwork_time = onwork_time;
    }

    public String getOutwork_time() {
        return outwork_time;
    }

    public void setOutwork_time(String outwork_time) {
        this.outwork_time = outwork_time;
    }

    public String getIncar() {
        return incar;
    }

    public void setIncar(String incar) {
        this.incar = incar;
    }

    public String getOutcar() {
        return outcar;
    }

    public void setOutcar(String outcar) {
        this.outcar = outcar;
    }

    public String getTotal_fee() {
        return total_fee;
    }

    public void setTotal_fee(String total_fee) {
        this.total_fee = total_fee;
    }

    public String getPrepay() {
        return prepay;
    }

    public void setPrepay(String prepay) {
        this.prepay = prepay;
    }

    public String getHistory_prepay() {
        return history_prepay;
    }

    public void setHistory_prepay(String history_prepay) {
        this.history_prepay = history_prepay;
    }

    public String getUpmoney() {
        return upmoney;
    }

    public void setUpmoney(String upmoney) {
        this.upmoney = upmoney;
    }
}
