package com.zhenlaidian.bean;

import java.util.List;

//{"tweek":"0","lweek":"5","total":"5","items:"[{"mobile":"15375242041","create_time":"2014-07-12 18:00"}]}
public class RecommendInfo {
	private String tweek;
	private String lweek;
	private String total;
	private List<Recommend> items;

	public String getTweek() {
		return tweek;
	}

	public void setTweek(String tweek) {
		this.tweek = tweek;
	}

	public String getLweek() {
		return lweek;
	}

	public void setLweek(String lweek) {
		this.lweek = lweek;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public List<Recommend> getItems() {
		return items;
	}

	public void setItems(List<Recommend> items) {
		this.items = items;
	}


	@Override
	public String toString() {
		return "RecommendInfo [tweek=" + tweek + ", lweek=" + lweek
				+ ", total=" + total + ", items=" + items + "]";
	}

	

}
