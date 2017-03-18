package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Created by xulu on 2016/8/30.
 */
public class CardInfo implements Serializable{
    /**
     *  "carnumber":"",
     "errmsg":"查询成功",
     "mobile":"",
     "result":1
     */
    Card card;
    String carnumber;
    String errmsg;
    String mobile;
    String result;
    String group_name;

    @Override
    public String toString() {
        return "CardInfo{" +
                "card=" + card +
                ", carnumber='" + carnumber + '\'' +
                ", errmsg='" + errmsg + '\'' +
                ", mobile='" + mobile + '\'' +
                ", result='" + result + '\'' +
                ", group_name='" + group_name + '\'' +
                '}';
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public String getCarnumber() {
        return carnumber;
    }

    public void setCarnumber(String carnumber) {
        this.carnumber = carnumber;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
