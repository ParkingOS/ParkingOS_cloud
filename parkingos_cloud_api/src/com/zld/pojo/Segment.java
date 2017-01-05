package com.zld.pojo;

import java.util.List;

public class Segment {
	private Integer segmentID;
	private String segmentName;
	private String firstTime;
	private String lastTime;
	private String routePrice;
	private Integer normalTimeSpan;
	private Integer peakTimeSpan;
	private String firtLastShiftInfo;
	private String firtLastShiftInfo2;
	
	public List<Station> stationList;
	
	public String getFirtLastShiftInfo() {
		return firtLastShiftInfo;
	}
	public void setFirtLastShiftInfo(String firtLastShiftInfo) {
		this.firtLastShiftInfo = firtLastShiftInfo;
	}
	public String getFirtLastShiftInfo2() {
		return firtLastShiftInfo2;
	}
	public void setFirtLastShiftInfo2(String firtLastShiftInfo2) {
		this.firtLastShiftInfo2 = firtLastShiftInfo2;
	}

	public Integer getSegmentID() {
		return segmentID;
	}
	public void setSegmentID(Integer segmentID) {
		this.segmentID = segmentID;
	}
	public Integer getNormalTimeSpan() {
		return normalTimeSpan;
	}
	public void setNormalTimeSpan(Integer normalTimeSpan) {
		this.normalTimeSpan = normalTimeSpan;
	}
	public String getSegmentName() {
		return segmentName;
	}
	public void setSegmentName(String segmentName) {
		this.segmentName = segmentName;
	}
	public String getFirstTime() {
		return firstTime;
	}
	public void setFirstTime(String firstTime) {
		this.firstTime = firstTime;
	}
	public String getLastTime() {
		return lastTime;
	}
	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}
	public String getRoutePrice() {
		return routePrice;
	}
	public void setRoutePrice(String routePrice) {
		this.routePrice = routePrice;
	}
	public Integer getPeakTimeSpan() {
		return peakTimeSpan;
	}
	public void setPeakTimeSpan(Integer peakTimeSpan) {
		this.peakTimeSpan = peakTimeSpan;
	}

	public List<Station> getStationList() {
		return stationList;
	}
	public void setStationList(List<Station> stationList) {
		this.stationList = stationList;
	}

}
