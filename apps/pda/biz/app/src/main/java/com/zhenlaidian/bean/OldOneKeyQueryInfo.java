package com.zhenlaidian.bean;

public class OldOneKeyQueryInfo {

	// {"ccount":"8","ocount":"11","total":"177.75"}

	private String ccount;
	private String ocount;
	private String tcount;
	private String total;

	public OldOneKeyQueryInfo() {
		super();
	}

	public String getCcount() {
		return ccount;
	}

	public void setCcount(String ccount) {
		this.ccount = ccount;
	}

	public String getOcount() {
		return ocount;
	}

	public void setOcount(String ocount) {
		this.ocount = ocount;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getTcount() {
		return tcount;
	}

	public void setTcount(String tcount) {
		this.tcount = tcount;
	}

	@Override
	public String toString() {
		return "OneKeyQueryInfo [ccount=" + ccount + ", ocount=" + ocount + ", tcount=" + tcount + ", total=" + total + "]";
	}
}
