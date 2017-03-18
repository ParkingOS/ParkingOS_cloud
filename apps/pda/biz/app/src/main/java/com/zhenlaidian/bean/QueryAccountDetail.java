package com.zhenlaidian.bean;

public class QueryAccountDetail {

	private String money;
	private String mtype;
	private String create_time;
	private String note;
	private String target;
	public String getMoney() {
		return money;
	}
	public void setMoney(String money) {
		this.money = money;
	}
	
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getMtype() {
		return mtype;
	}
	public void setMtype(String mtype) {
		this.mtype = mtype;
	}
	public QueryAccountDetail() {
		super();
	}
	@Override
	public String toString() {
		return "QueryAccountDetail [money=" + money + ", mtype=" + mtype
				+ ", create_time=" + create_time + ", note=" + note
				+ ", target=" + target + "]";
	}
	
	
}
