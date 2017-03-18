/**
 * 
 */
package com.zhenlaidian.bean;

/**
 * @author Administrator 2015年7月31日
 * 
 */
public class WatchInfo {
	// 60:首页刷新获取今日收费、今日打赏、今日入场和出场信息
	// collectorrequest.do?action=todayaccount&token=5286f078c6d2ecde9b30929f77771149
	// 返回值类型：JSON对象
	// mobilemoney：今日手机收费金额
	// cashmoney：今日现金收费金额
	// rewardmoney：今日打赏金额
	// todayscore： 今日积分
	// todayin：今日入场数量
	// todayout： 今日出场数量

	private String mobilemoney;
	private String cashmoney;
	private String rewardmoney;
	private String todayscore;
	private String todayin;
	private String todayout;

	public WatchInfo() {
		super();
	}

	public String getMobilemoney() {
		return mobilemoney;
	}

	public void setMobilemoney(String mobilemoney) {
		this.mobilemoney = mobilemoney;
	}

	public String getCashmoney() {
		return cashmoney;
	}

	public void setCashmoney(String cashmoney) {
		this.cashmoney = cashmoney;
	}

	public String getRewardmoney() {
		return rewardmoney;
	}

	public void setRewardmoney(String rewardmoney) {
		this.rewardmoney = rewardmoney;
	}

	public String getTodayscore() {
		return todayscore;
	}

	public void setTodayscore(String todayscore) {
		this.todayscore = todayscore;
	}

	public String getTodayin() {
		return todayin;
	}

	public void setTodayin(String todayin) {
		this.todayin = todayin;
	}

	public String getTodayout() {
		return todayout;
	}

	public void setTodayout(String todayout) {
		this.todayout = todayout;
	}

	@Override
	public String toString() {
		return "WatchInfo [mobilemoney=" + mobilemoney + ", cashmoney=" + cashmoney + ", rewardmoney=" + rewardmoney
				+ ", todayscore=" + todayscore + ", todayin=" + todayin + ", todayout=" + todayout + "]";
	}

}
