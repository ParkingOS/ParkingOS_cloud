package com.zhenlaidian.bean;

public class RecommendRecord {

	public String uin;// 推荐的停车员编号uin为null的话是微信的；
	public String money;// 推荐奖励金额；
	public String nid;// 微信用户的虚拟id；
	public String state;// 是否成功:,0审核中,1成功2在黑名单中

	// uin==null是等待车主注册 uin！=null是原来的处理 state=3车主重复注册，奖励取消，money==null是5元

	public RecommendRecord() {
		
	}
}
