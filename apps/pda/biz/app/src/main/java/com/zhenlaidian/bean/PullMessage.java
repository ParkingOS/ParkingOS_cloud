package com.zhenlaidian.bean;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//{"mtype":2,"info":{"total":"1.00","duration":"null","carnumber":"京F8KR99",
//"etime":"20:08","state":"1","btime":"20:03","orderid":"1506"}}
public class PullMessage {
	private String  mtype;// 0收到离场订单支付消息，-1token无效；
	private JsonObject info;
	private JsonArray mesgs;
	
	
	public PullMessage() {
		super();
	}
	
	public String getMtype() {
		return mtype;
	}

	public JsonArray getMesgs() {
		return mesgs;
	}

	public void setMesgs(JsonArray mesgs) {
		this.mesgs = mesgs;
	}

	public void setMtype(String mtype) {
		this.mtype = mtype;
	}

	public JsonObject getInfo() {
		return info;
	}

	public void setInfo(JsonObject info) {
		this.info = info;
	}

	@Override
	public String toString() {
		return "PullMessage{" +
				"mtype='" + mtype + '\'' +
				", info=" + info +
				", mesgs=" + mesgs +
				'}';
	}
}
