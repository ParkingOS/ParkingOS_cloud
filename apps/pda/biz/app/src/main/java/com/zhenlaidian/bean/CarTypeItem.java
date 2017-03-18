package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Created by xulu on 2016/10/24.
 */
public class CarTypeItem implements Serializable{
    String id;
    String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CarTypeItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
