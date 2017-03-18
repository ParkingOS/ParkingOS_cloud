package com.zhenlaidian.bean;

public class NfcPrepaymentOrder {

	// 04-25 15:10:12.352: E/ShowNfcOrder(21299):
	// 预付费订单结算结果--->{"result":"2","prefee":"1002.0","total":"1004.0","collect":"2.0"}
	public String result;// 1成功 -1失败 2需要补差价；
	public String prefee;// 预支付金额
	public String total;// 总金额
	public String collect;// 差价金额

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getPrefee() {
		return prefee;
	}

	public void setPrefee(String prefee) {
		this.prefee = prefee;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getCollect() {
		return collect;
	}

	public void setCollect(String collect) {
		this.collect = collect;
	}

	public NfcPrepaymentOrder() {
		super();
	}

	public NfcPrepaymentOrder(String result, String prefee, String total, String collect) {
		super();
		this.result = result;
		this.prefee = prefee;
		this.total = total;
		this.collect = collect;
	}

	@Override
	public String toString() {
		return "NfcPrepaymentOrder [result=" + result + ", prefee=" + prefee + ", total=" + total
				+ ", collect=" + collect + "]";
	}

}
