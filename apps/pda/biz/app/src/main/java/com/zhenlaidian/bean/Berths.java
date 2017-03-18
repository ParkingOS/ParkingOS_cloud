package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Created by TCB on 2016/4/18.
 * xulu
 */
public class Berths implements Serializable {

    public String berthsec_name;
    public String id;

    public String getBerthsec_name() {
        return berthsec_name;
    }

    public void setBerthsec_name(String berthsec_name) {
        this.berthsec_name = berthsec_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Berths{" +
                "berthsec_name='" + berthsec_name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}


