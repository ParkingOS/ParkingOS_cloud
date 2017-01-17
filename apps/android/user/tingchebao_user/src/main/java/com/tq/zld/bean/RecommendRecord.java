package com.tq.zld.bean;

public class RecommendRecord {

	public String uin;// 推荐的停车员编号
	public String state;// 是否成功:1成功

	public RecommendRecord() {
	}

	public RecommendRecord(String id, String state) {
		this.uin = id;
		this.state = state;
	}
}
