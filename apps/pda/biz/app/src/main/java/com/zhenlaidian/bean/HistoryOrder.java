package com.zhenlaidian.bean;

import java.util.ArrayList;

public class HistoryOrder {

	private String count;
	private String price;
	private ArrayList<AllOrder> info;

	public HistoryOrder() {
		super();
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public ArrayList<AllOrder> getInfo() {
		return info;
	}

	public void setInfo(ArrayList<AllOrder> info) {
		this.info = info;
	}

	@Override
	public String toString() {
		return "HistoryOrder [count=" + count + ", price=" + price + ", info=" + info + "]";
	}

}
