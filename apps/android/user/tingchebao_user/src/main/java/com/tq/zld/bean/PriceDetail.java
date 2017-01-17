package com.tq.zld.bean;

public class PriceDetail {

	public String b_time;// 白天时段开始时间（夜间时段结束时间）
	public String e_time;// 白天时段结束时间（夜间时段开始时间）
	public String price;// 白天价格
	public String unit;// 白天计价单位
	public String first_times;// 白天首优惠时长
	public String fprice;// 白天首优惠价格
	public String free_time;// 白天免费时长，单位：分钟
	public String fpay_type;// 白天超过免费时长后，免费时长是否计费？1-->免费；0-->收费
	public String nprice;// 夜间价格
	public String nunit;// 夜间计价单位
	public String nfirst_times;// 夜间首优惠时长
	public String nfprice;// 夜间首优惠价格
	public String nfree_time;// 夜间免费时长，单位：分钟
	public String nfpay_type;// 夜间超过免费时长后，免费时长是否计费？1-->免费；0-->收费
	public String isnight;// 是否支持夜间停车？0-->支持；1-->不支持
}