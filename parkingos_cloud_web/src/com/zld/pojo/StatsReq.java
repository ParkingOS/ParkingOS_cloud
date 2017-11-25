package com.zld.pojo;

import java.io.Serializable;
import java.util.List;

public class StatsReq implements Serializable {
	private long startTime = -1;//查询起始时间
	private long endTime = -1;//查询结束时间
	private List<Object> idList;
	private int type;//0：按收费员编号统计 1：按车场编号统计 2：按泊位段编号查询 3：按泊位查询

	public List<Object> getIdList() {
		return idList;
	}
	public void setIdList(List<Object> idList) {
		this.idList = idList;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	@Override
	public String toString() {
		return "StatsReq [startTime=" + startTime + ", endTime=" + endTime
				+ ", idList=" + idList + ", type=" + type + "]";
	}

}
