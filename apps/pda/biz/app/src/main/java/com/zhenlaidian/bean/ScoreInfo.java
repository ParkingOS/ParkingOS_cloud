package com.zhenlaidian.bean;

public class ScoreInfo {

//获取到的积分信息为--->{"lala_scroe":"0","nfc_score":"0.0","praise_scroe":"0","pai_score":"0","online_scroe":"0.8","uin":"1000004","recom_scroe":"0","sign_score":"0","score":"0.8","cashscore":"0"}

	// {"lala_scroe":"2", 拉拉队积分
	// "nfc_score":"0", nfc卡积分
	// "sign_score":"0",扫牌积分
	// "praise_scroe":"0", 差评扣分
	// "score":"2"} 总积分
	// "cashscore":"2"} 兑换积分
	//tv_online_scroe 在岗积分
	//recom_scroe 推荐积分
	
	private String lala_scroe;
	private String nfc_score;
	private String sign_score;
	private String praise_scroe;
	private String score;
	private String cashscore;
	private String online_scroe;
	private String recom_scroe;

	public String getLala_scroe() {
		return lala_scroe;
	}

	public void setLala_scroe(String lala_scroe) {
		this.lala_scroe = lala_scroe;
	}

	public String getNfc_score() {
		return nfc_score;
	}

	public void setNfc_score(String nfc_score) {
		this.nfc_score = nfc_score;
	}

	public String getSign_score() {
		return sign_score;
	}

	public void setSign_score(String sign_score) {
		this.sign_score = sign_score;
	}

	public String getPraise_scroe() {
		return praise_scroe;
	}

	public void setPraise_scroe(String praise_scroe) {
		this.praise_scroe = praise_scroe;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getCashscore() {
		return cashscore;
	}

	public void setCashscore(String cashscore) {
		this.cashscore = cashscore;
	}

	public String getOnline_scroe() {
		return online_scroe;
	}

	public void setOnline_scroe(String online_scroe) {
		this.online_scroe = online_scroe;
	}

	public String getRecom_scroe() {
		return recom_scroe;
	}

	public void setRecom_scroe(String recom_scroe) {
		this.recom_scroe = recom_scroe;
	}

	public ScoreInfo() {
		super();
	}

	@Override
	public String toString() {
		return "ScoreInfo [lala_scroe=" + lala_scroe + ", nfc_score="
				+ nfc_score + ", sign_score=" + sign_score + ", praise_scroe="
				+ praise_scroe + ", score=" + score + ", cashscore="
				+ cashscore + ", online_scroe=" + online_scroe
				+ ", recom_scroe=" + recom_scroe + "]";
	}

	


}
