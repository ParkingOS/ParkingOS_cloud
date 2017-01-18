package com.zld.pcloud_sensor.pojo;

public class SensorMsg {
	private String ID;//车位探测器ID
	private int X;//磁场X值
	private int Y;//磁场Y值
	private int Z;//磁场Z值
	private int Q;//信号质量
	private int N;//发送编号
	private int D;//距离值（cm），仅复合车位探测器有
	private int V;//电磁电压
	private String Err;//错误码
	public String getID() {
		return "TB" + ID;
	}
	public void setID(String iD) {
		ID = iD;
	}
	public int getX() {
		return X;
	}
	public void setX(int x) {
		X = x;
	}
	public int getY() {
		return Y;
	}
	public void setY(int y) {
		Y = y;
	}
	public int getZ() {
		return Z;
	}
	public void setZ(int z) {
		Z = z;
	}
	public int getQ() {
		return Q;
	}
	public void setQ(int q) {
		Q = q;
	}
	public int getN() {
		return N;
	}
	public void setN(int n) {
		N = n;
	}
	public int getD() {
		return D;
	}
	public void setD(int d) {
		D = d;
	}
	public int getV() {
		return V;
	}
	public void setV(int v) {
		V = v;
	}
	public String getErr() {
		return Err;
	}
	public void setErr(String err) {
		Err = err;
	}
	@Override
	public String toString() {
		return "SensorMsg [ID=" + ID + ", X=" + X + ", Y=" + Y + ", Z=" + Z
				+ ", Q=" + Q + ", N=" + N + ", D=" + D + ", V=" + V + ", Err="
				+ Err + "]";
	}
}
