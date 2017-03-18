package com.zhenlaidian.bean;

import java.util.ArrayList;

public class PersonAccountInfo {

	private String count;
	private ArrayList<QueryAccountDetail> info;
	public PersonAccountInfo() {
		super();
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public ArrayList<QueryAccountDetail> getInfo() {
		return info;
	}
	public void setInfo(ArrayList<QueryAccountDetail> info) {
		this.info = info;
	}
	@Override
	public String toString() {
		return "PersonAccountInfo [count=" + count + ", info=" + info + "]";
	}
	
}
