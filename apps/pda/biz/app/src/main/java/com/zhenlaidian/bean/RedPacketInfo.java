package com.zhenlaidian.bean;

import java.io.Serializable;

/**
 * @author Administrator 2015年7月16日
 */
@SuppressWarnings("serial")
public class RedPacketInfo implements Serializable {
	// collectorrequest.do?action=bonusinfo&token=
	// [{"type":0,"bmoney":8,"bnum":3,"score":15,"remark":"拼手气礼包"},{"type":1,"bmoney":5,"score":20,"remark":"单张停车券"}]
	// type=0表示红包
	// type=1表示停车券
	// bmoney：金额
	// bnum：红包个数
	// score：消耗的积分数
	// remark：描述
	private String type;
	private String bmoney;
	private String bnum;
	private String score;
	private String remark;
	private String limit;// 五元券超出限制后不能发送:0未超出。1超出；

	public RedPacketInfo() {
		super();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBmoney() {
		return bmoney;
	}

	public void setBmoney(String bmoney) {
		this.bmoney = bmoney;
	}

	public String getBnum() {
		return bnum;
	}

	public void setBnum(String bnum) {
		this.bnum = bnum;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	@Override
	public String toString() {
		return "RedPacketInfo [type=" + type + ", bmoney=" + bmoney + ", bnum=" + bnum + ", score=" + score + ", remark="
				+ remark + ", limit=" + limit + "]";
	}

}
