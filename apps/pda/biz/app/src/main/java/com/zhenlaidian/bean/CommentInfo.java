package com.zhenlaidian.bean;

public class CommentInfo {
	// [{"info":"测试评论收费员","user":"京N78532","date":"06-13","time":"17:31"},
	public String info;
	public String user;
	public String ctime;
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getCtime() {
		return ctime;
	}
	public void setCtime(String ctime) {
		this.ctime = ctime;
	}
	@Override
	public String toString() {
		return "CommentInfo [info=" + info + ", user=" + user + ", ctime=" + ctime + "]";
	}

	

}
