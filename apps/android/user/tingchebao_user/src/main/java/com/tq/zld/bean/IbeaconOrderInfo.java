package com.tq.zld.bean;

public class IbeaconOrderInfo {
    // {"result":"0","is_auth":"0","info":"车主手机号为空"} result 0失败 1成功 state0入口1出口-1通道不存在
    public String result;
    public String state;
    public String info;

    public IbeaconOrderInfo() {
        super();
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "IbeaconOrderInfo [result=" + result + ", is_auth=" + state
                + ", info=" + info + "]";
    }
}
