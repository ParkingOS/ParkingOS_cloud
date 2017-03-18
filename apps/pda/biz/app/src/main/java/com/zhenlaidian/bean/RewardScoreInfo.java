package com.zhenlaidian.bean;

/**
 * @author Administrator 2015年7月15日
 */
public class RewardScoreInfo {
	// todayscore：今日积分
	// rank：排名 0 没有入围排行榜
	// remainscore：剩余积分
	public String todayscore;
	public String rank;
	public String remainscore;
	public String scoreurl;
	public String ticketurl;

	public RewardScoreInfo() {
		super();
	}

	public String getTodayscore() {
		return todayscore;
	}

	public void setTodayscore(String todayscore) {
		this.todayscore = todayscore;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getRemainscore() {
		return remainscore;
	}

	public void setRemainscore(String remainscore) {
		this.remainscore = remainscore;
	}

	public String getScoreurl() {
		return scoreurl;
	}

	public void setScoreurl(String scoreurl) {
		this.scoreurl = scoreurl;
	}

	public String getTicketurl() {
		return ticketurl;
	}

	public void setTicketurl(String ticketurl) {
		this.ticketurl = ticketurl;
	}

	@Override
	public String toString() {
		return "RewardScoreInfo [todayscore=" + todayscore + ", rank=" + rank + ", remainscore=" + remainscore + ", scoreurl="
				+ scoreurl + ", ticketurl=" + ticketurl + "]";
	}

}
