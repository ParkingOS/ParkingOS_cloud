package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Created by xulu on 2016/8/24.
 */
public class BaseResponCard implements Serializable{
    //    {"result":"1","errmsg":"结算成功"}
    String result;
    String errmsg;
    String duration;

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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "BaseResponCard{" +
                "result='" + result + '\'' +
                ", errmsg='" + errmsg + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
