package com.zhenlaidian.bean;

public class ShareInfo {
	// 56：收费员用赏金积分发红包
	// collectorrequest.do?action=sendbonus&token=5286f078c6d2ecde9b30929f77771149&bmoney=12&bnum=8&score=1
	// 输入参数：
	// bmoney：红包金额
	// bnum：红包个数
	// score：消耗积分
	// 返回值类型：JSON对象
	// result：1：成功 -1：出错了 -3：赏金积分不足
	// bonusid：红包ID
	// cname：车场名称

	public String result;
	public String bonusid;
	public String cname;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getBonusid() {
		return bonusid;
	}

	public void setBonusid(String bonusid) {
		this.bonusid = bonusid;
	}

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	public ShareInfo() {
		super();
	}

	@Override
	public String toString() {
		return "ShareInfo [result=" + result + ", bonusid=" + bonusid + ", cname=" + cname + "]";
	}

}
