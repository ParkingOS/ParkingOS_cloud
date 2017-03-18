package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Created by TCB on 2016/4/19.
 * xulu
 */
public class BoWeiStateEntity implements Serializable{
    /**
     * "mesg":[
     {
     "id":"370",
     "state":"1"
     },
     */

    String id;
    String state;
    String berthorderid;

    public String getBerthorderid() {
        return berthorderid;
    }

    public void setBerthorderid(String berthorderid) {
        this.berthorderid = berthorderid;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "BoWeiStateEntity{" +
                "id='" + id + '\'' +
                ", state='" + state + '\'' +
                ", berthorderid='" + berthorderid + '\'' +
                '}';
    }
}
