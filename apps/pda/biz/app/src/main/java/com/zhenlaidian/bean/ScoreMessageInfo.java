/**
 * 
 */
package com.zhenlaidian.bean;

/**
 * @author Administrator 2015年8月1日
 */
public class ScoreMessageInfo {

	// carnumber 车牌
	// score 积分
	// tmoney 用券金额；

	private String carnumber;
	private Double score;
	private String tmoney;

	public ScoreMessageInfo() {
		super();
	}

	public String getCarnumber() {
		return carnumber;
	}

	public void setCarnumber(String carnumber) {
		this.carnumber = carnumber;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public String getTmoney() {
		return tmoney;
	}

	public void setTmoney(String tmoney) {
		this.tmoney = tmoney;
	}

	@Override
	public String toString() {
		return "ScoreMessageInfo [carnumber=" + carnumber + ", score=" + score + ", tmoney=" + tmoney + "]";
	}

}
