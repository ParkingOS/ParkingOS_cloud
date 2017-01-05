package com.zld.pojo;

public class StatsWorkRecordInfo {
	private String name;
	private Object value;
	private int fontSize = 14;//字体大小
	private String fontColor = "#000000";//字体颜色
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public int getFontSize() {
		return fontSize;
	}
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	public String getFontColor() {
		return fontColor;
	}
	public void setFontColor(String fontColor) {
		this.fontColor = fontColor;
	}
	@Override
	public String toString() {
		return "StatsWorkRecordInfo [name=" + name + ", value=" + value
				+ ", fontSize=" + fontSize + ", fontColor=" + fontColor + "]";
	}
	
	
}
