package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Created by TCB on 2016/4/25.
 */
public class TodayMoneyEntity implements Serializable{
    /**
     * {"onlinepay":"0.0","cardpay":"0.0","cashpay":"1698.0"}
     */
    private String onlinepay;
    private String cardpay;
    private String cashpay;

    public String getOnlinepay() {
        return onlinepay;
    }

    public void setOnlinepay(String onlinepay) {
        this.onlinepay = onlinepay;
    }

    public String getCardpay() {
        return cardpay;
    }

    public void setCardpay(String cardpay) {
        this.cardpay = cardpay;
    }

    public String getCashpay() {
        return cashpay;
    }

    public void setCashpay(String cashpay) {
        this.cashpay = cashpay;
    }

    @Override
    public String toString() {
        return "TodayMoneyEntity{" +
                "onlinepay='" + onlinepay + '\'' +
                ", cardpay='" + cardpay + '\'' +
                ", cashpay='" + cashpay + '\'' +
                '}';
    }
}
