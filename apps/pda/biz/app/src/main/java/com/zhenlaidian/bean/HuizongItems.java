package com.zhenlaidian.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by xulu on 2016/10/24.
 */
public class HuizongItems implements Serializable{
    String fontColor;
    String fontSize;
    String name;
    String value;
    ArrayList<HuizongItem> content;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public ArrayList<HuizongItem> getContent() {
        return content;
    }

    public void setContent(ArrayList<HuizongItem> content) {
        this.content = content;
    }

    public HuizongItems() {
        super();
    }

    @Override
    public String toString() {
        return "HuizongItems{" +
                "fontColor='" + fontColor + '\'' +
                ", fontSize='" + fontSize + '\'' +
                ", name='" + name + '\'' +
                ", content=" + content +
                '}';
    }
}
