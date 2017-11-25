package com.zld.pojo;

import java.io.Serializable;
import java.util.List;

public class StatsOrderResp extends BaseResp implements Serializable {
	private List<StatsOrder> orders;

	public List<StatsOrder> getOrders() {
		return orders;
	}

	public void setOrders(List<StatsOrder> orders) {
		this.orders = orders;
	}

	@Override
	public String toString() {
		return "StatsOrderResp [orders=" + orders + "]";
	}

}
