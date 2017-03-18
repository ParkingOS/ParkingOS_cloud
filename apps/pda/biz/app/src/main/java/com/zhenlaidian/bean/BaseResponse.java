package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Created by TCB on 2016/4/21.
 * xulu
 */
public class BaseResponse implements Serializable{
//    {"result":"1","errmsg":"结算成功"}
    String result;
    String errmsg;

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

    @Override
    public String toString() {
        return "BaseResponse{" +
                "result='" + result + '\'' +
                ", errmsg='" + errmsg + '\'' +
                '}';
    }
}
