package com.tq.zld.bean;

public class Message {

	public static final int TYPE_PAY_FAILED = 0;// 支付失败提醒
	public static final int TYPE_BONUS = 1;// 红包提醒
	public static final int TYPE_AUTO_PAY = 2;// 自动支付提醒
	// public static final int TYPE_REGISTER = 3;// 注册提醒
	public static final int TYPE_PARKING = 4;// 停车入场提醒
	public static final int TYPE_ACTIVITY = 5;// 活动提醒
	public static final int TYPE_RECOMMEND = 6;// 推荐停车员
	public long id;
	public int type;
	public int hasread;// 是否已读：0未读，1已读
	public String ctime;
	public String title;
	public String content;

}
