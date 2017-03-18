package com.zhenlaidian.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by TCB on 2016/4/19.
 * xulu
 */
public class BoWeiListEntity implements Serializable {
/**
 *
 "state":"1",
  workid  //工作段编号 //签到工作编号 上班流水编号
 "errmsg":"",
 comid //车场编号
 cname 车场名称
 "data":[
 {
 "id":"370",
 "ber_name":"11111",
 "orderid":"",
 "car_number":""
 },
 */
    private String state;
    private String errmsg;
    private String workid;
    private String comid;
    private String cname;
    private ArrayList<InVehicleInfo> data;
    private ArrayList<CarTypeItem> car_type;

    @Override
    public String toString() {
        return "BoWeiListEntity{" +
                "state='" + state + '\'' +
                ", errmsg='" + errmsg + '\'' +
                ", workid='" + workid + '\'' +
                ", comid='" + comid + '\'' +
                ", cname='" + cname + '\'' +
                ", data=" + data +
                ", car_type=" + car_type +
                '}';
    }

    public ArrayList<CarTypeItem> getCar_type() {
        return car_type;
    }

    public void setCar_type(ArrayList<CarTypeItem> car_type) {
        this.car_type = car_type;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getComid() {
        return comid;
    }

    public void setComid(String comid) {
        this.comid = comid;
    }

    public String getWorkid() {
        return workid;
    }

    public void setWorkid(String workid) {
        this.workid = workid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public ArrayList<InVehicleInfo> getData() {
        return data;
    }

    public void setData(ArrayList<InVehicleInfo> data) {
        this.data = data;
    }


}
