package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Created by TCB on 2016/4/24.
 */
public class BaseRegisBean implements Serializable{
    String result;
    String errmst;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrmst() {
        return errmst;
    }

    public void setErrmst(String errmst) {
        this.errmst = errmst;
    }

    @Override
    public String toString() {
        return "BaseRegisBean{" +
                "result='" + result + '\'' +
                ", errmst='" + errmst + '\'' +
                '}';
    }
}
