package com.zhenlaidian.bean;

import java.util.ArrayList;

/**
 * @author Administrator 2015年7月15日
 */
public class RewardRankingInfo {

	// sort :排名
	// score:今日积分
	// uin:收费员编号
	// nickname：收费员名称
	// cname：车场名称
	// {"count":2,"info":[{"uin":"10700","score":"12.00","nickname":"张**","cname":"停****停车场","sort":"1"}]}

	private String count;
	private ArrayList<RankingInfo> info;

	public RewardRankingInfo() {
		super();
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public ArrayList<RankingInfo> getInfo() {
		return info;
	}

	public void setInfo(ArrayList<RankingInfo> info) {
		this.info = info;
	}

	@Override
	public String toString() {
		return "RewardRankingInfo [count=" + count + ", info=" + info + "]";
	}

	public class RankingInfo {
		public String score;
		public String uin;
		public String nickname;
		public String money;
		public String sort;
		public String cname;

		public RankingInfo() {
			super();
		}

		public String getScore() {
			return score;
		}

		public void setScore(String score) {
			this.score = score;
		}

		public String getUin() {
			return uin;
		}

		public void setUin(String uin) {
			this.uin = uin;
		}

		public String getNickname() {
			return nickname;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		public String getMoney() {
			return money;
		}

		public void setMoney(String money) {
			this.money = money;
		}

		public String getSort() {
			return sort;
		}

		public void setSort(String sort) {
			this.sort = sort;
		}

		public String getCname() {
			return cname;
		}

		public void setCname(String cname) {
			this.cname = cname;
		}

		@Override
		public String toString() {
			return "RankingInfo [score=" + score + ", uin=" + uin + ", nickname=" + nickname + ", money=" + money + ", sort="
					+ sort + ", cname=" + cname + "]";
		}

	}
}
