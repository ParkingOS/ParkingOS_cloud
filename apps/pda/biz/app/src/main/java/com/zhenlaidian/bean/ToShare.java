package com.zhenlaidian.bean;
//<content>
//<busy>0</busy> 
//<info>success</info> 
//</content>
public class ToShare {

	private int busy;
	private String info;
	public ToShare() {
		super();
	}
	public int getBusy() {
		return busy;
	}
	public void setBusy(int busy) {
		this.busy = busy;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	@Override
	public String toString() {
		return "ToShare [busy=" + busy + ", info=" + info + "]";
	}
	
	
}
