package com.zld.pojo;


public class Station {

	private String stationID;
	private String stationName;
	private Integer stationNO;
	private String stationmemo;
	public StationPostion stationPostion;
	
	private String memos;
	public String getMemos() {
		return memos;
	}
	public void setMemos(String memos) {
		this.memos = memos;
	}
	public String getStationID() {
		return stationID;
	}
	public void setStationID(String stationID) {
		this.stationID = stationID;
	}
	public String getStationmemo() {
		return stationmemo;
	}
	public void setStationmemo(String stationmemo) {
		this.stationmemo = stationmemo;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public Integer getStationNO() {
		return stationNO;
	}
	public void setStationNO(Integer stationNO) {
		this.stationNO = stationNO;
	}
	public StationPostion getStationPostion() {
		return stationPostion;
	}
	public void setStationPostion(StationPostion stationPostion) {
		this.stationPostion = stationPostion;
	}
	
}
