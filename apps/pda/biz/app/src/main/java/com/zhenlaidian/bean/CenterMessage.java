package com.zhenlaidian.bean;

public class CenterMessage {

//获取到的通知消息是：-->>[{"id":"1","type":"0","hasread":"0","ctime":"1421930677","content":"你推荐的车主（手机尾号643）注册成功，你获得５元奖励。","uin":"1000005","title":"推荐提醒"}]
	
	
	public String id;
	public String type;   //-- 0推荐提醒 1 活动提醒
	public String hasread;// 是否已读：0未读，1已读
	public String ctime;
	public String title;
	public String content;
	public String uin;
	
	public CenterMessage() {
		super();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHasread() {
		return hasread;
	}

	public void setHasread(String hasread) {
		this.hasread = hasread;
	}

	public String getCtime() {
		return ctime;
	}
	public void setCtime(String ctime) {
		this.ctime = ctime;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getUin() {
		return uin;
	}
	public void setUin(String uin) {
		this.uin = uin;
	}

	@Override
	public String toString() {
		return "CenterMessage [id=" + id + ", type=" + type + ", hasread="
				+ hasread + ", ctime=" + ctime + ", title=" + title
				+ ", content=" + content + ", uin=" + uin + "]";
	}
	
	
}
