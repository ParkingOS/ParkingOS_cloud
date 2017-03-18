package com.zhenlaidian.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by xulu on 2016/10/24.
 */
public class HuizongTotal implements Serializable {
    String errmsg;
    ArrayList<HuizongItems> infos;
    String result;

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public ArrayList<HuizongItems> getInfos() {
        return infos;
    }

    public void setInfos(ArrayList<HuizongItems> infos) {
        this.infos = infos;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


    @Override
    public String toString() {
        return "HuizongTotal{" +
                "errmsg='" + errmsg + '\'' +
                ", infos=" + infos +
                ", result='" + result + '\'' +
                '}';
    }
}
