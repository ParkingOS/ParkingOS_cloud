package com.zhenlaidian.bean;

//{"share_time":"27","uin":"1000005","nickname":"张先生","cname":"中关村信息大厦停车场","sort":"1"}
public class RankingInfo {

	private String share_time;// 分享次数
	private String uin; // 账号
	private String nickname; // 收费员
	private String cname; // 停车场名称
	private String sort; // 排名
	private String score;// 积分
	// [{"score":"66.40","uin":"10443","nickname":"parkadmin","cname":"新侨饭店"},
	// {"score":"52.80","uin":"10442","nickname":"晏雨鹏","cname":"华普花园"},
	// {"score":"13.60","uin":"10444","nickname":"瞿寒","cname":"新世界百货"},
	// {"score":"-8.20","uin":"1000005","nickname":"张先生","cname":"中关村"}]

	public RankingInfo() {
		super();
	}

	public String getScore() {
		return score;
	}

	public void setScore(String scroe) {
		this.score = scroe;
	}

	public String getShare_time() {
		return share_time;
	}

	public void setShare_time(String share_time) {
		this.share_time = share_time;
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

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	@Override
	public String toString() {
		return "RankingInfo [share_time=" + share_time + ", uin=" + uin
				+ ", nickname=" + nickname + ", cname=" + cname + ", sort="
				+ sort + ", score=" + score + "]";
	}

}
