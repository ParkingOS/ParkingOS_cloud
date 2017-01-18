package com.zld.pcloud_sensor.pojo;

import java.util.List;

public class SiteMsg {
	private String CID;//集中器ID
	private String PATH;//"10-11-12",路径，表示通过采集器10到11到12到达集中器
	private String N;//集中器发送编号
	private String SQ;//采集器信号质量
	private String Err;//错误码，正常下无值
	private int V;//电压
	private List<SensorMsg> MAG;//车位探测器信息组
	public String getCID() {
		return "TB" + CID;
	}
	public void setCID(String cID) {
		CID = cID;
	}
	public String getPATH() {
		return PATH;
	}
	public void setPATH(String pATH) {
		PATH = pATH;
	}
	public String getN() {
		return N;
	}
	public void setN(String n) {
		N = n;
	}
	public String getSQ() {
		return SQ;
	}
	public void setSQ(String sQ) {
		SQ = sQ;
	}
	public List<SensorMsg> getMAG() {
		return MAG;
	}
	public void setMAG(List<SensorMsg> mAG) {
		MAG = mAG;
	}
	public String getErr() {
		return Err;
	}
	public void setErr(String err) {
		Err = err;
	}
	public int getV() {
		return V;
	}
	public void setV(int v) {
		V = v;
	}
	@Override
	public String toString() {
		return "SiteMsg [CID=" + CID + ", PATH=" + PATH + ", N=" + N + ", SQ="
				+ SQ + ", Err=" + Err + ", V=" + V + ", MAG=" + MAG + "]";
	}
}
