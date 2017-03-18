package com.zhenlaidian.bean;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MyParkAccountDetai implements Serializable{
	
	private String c;
	private String a;
	private String t;
	private String t1;
	private String t2;
	public String getC() {
		return c;
	}
	public void setC(String c) {
		this.c = c;
	}
	public String getA() {
		return a;
	}
	public void setA(String a) {
		this.a = a;
	}
	public String getT() {
		return t;
	}
	public void setT(String t) {
		this.t = t;
	}
	public String getT1() {
		return t1;
	}
	public void setT1(String t1) {
		this.t1 = t1;
	}
	public String getT2() {
		return t2;
	}
	public void setT2(String t2) {
		this.t2 = t2;
	}
	public MyParkAccountDetai(String c, String a, String t, String t1, String t2) {
		super();
		this.c = c;
		this.a = a;
		this.t = t;
		this.t1 = t1;
		this.t2 = t2;
	}
	public MyParkAccountDetai() {
		super();
	}
	@Override
	public String toString() {
		return "MyParkAccountDetai [c=" + c + ", a=" + a + ", t=" + t + ", t1="
				+ t1 + ", t2=" + t2 + "]";
	}
}
