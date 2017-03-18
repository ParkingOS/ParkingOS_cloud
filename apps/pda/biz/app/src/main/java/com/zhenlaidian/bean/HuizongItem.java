package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * Created by xulu on 2016/10/24.
 * "fontColor":"#000000",
 "fontSize":14,
 "name":"签到时间:",
 "value":"16-10-24 14:54"
 */
public class HuizongItem implements Serializable{
    String fontColor;
    String fontSize;
    String name;
    String value;

    @Override
    public String toString() {
        return "HuizongItem{" +
                "fontColor='" + fontColor + '\'' +
                ", fontSize='" + fontSize + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
