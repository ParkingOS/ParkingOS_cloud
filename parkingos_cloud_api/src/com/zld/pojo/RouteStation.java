package com.zld.pojo;

import java.util.List;

public class RouteStation {
	private String isNewData;
	private int routeID;
	private String routeName;
	private String timeStamp;
	private String routeMemo;
	private String routeType;
	
	public List<Segment> segmentList;
	
	public String getIsNewData() {
		return isNewData;
	}
	public void setIsNewData(String isNewData) {
		this.isNewData = isNewData;
	}
	public int getRouteID() {
		return routeID;
	}
	public void setRouteID(int routeID) {
		this.routeID = routeID;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String getRouteMemo() {
		return routeMemo;
	}
	public void setRouteMemo(String routeMemo) {
		this.routeMemo = routeMemo;
	}
	public List<Segment> getSegmentList() {
		return segmentList;
	}
	public void setSegmentList(List<Segment> segmentList) {
		this.segmentList = segmentList;
	}
	public String getRouteName() {
		return routeName;
	}
	public void setRouteName(String routeName) {
		this.routeName = routeName;
	}
	public String getRouteType() {
		return routeType;
	}
	public void setRouteType(String routeType) {
		this.routeType = routeType;
	}


}
