package com.zhenlaidian.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUtils {

	// 车牌号正则
	public static boolean CarChecked(String number) {
		String check = "^[\\u4e00-\\u9fa5]{1}[A-Z_0-9]{5}[A-Z_0-9_\\u4e00-\\u9fa5]$|^WJ\\d{7}$";
		Pattern regex = Pattern.compile(check);
		Matcher matcher = regex.matcher(number);
		return matcher.matches();
	}

	// 手机号正则
	public static boolean MobileChecked(String number) {
		String check = "1([378]|5(?!4))[0-9]{9}";
		Pattern regex = Pattern.compile(check);
		Matcher matcher = regex.matcher(number);
		return matcher.matches();
	}

	// 位置信息正则
	public static boolean LocationChecked(String longitude) {
		String regJD = "(-|\\+)?(180\\.0{4,6}|(\\d{1,2}|1([0-7]\\d))\\.\\d{4,6})";
		Pattern regex = Pattern.compile(regJD);
		Matcher matcher = regex.matcher(longitude);
		return matcher.matches();
	}

	// 金额正则（不能以0开头，保留两位小数。）
	public static boolean CollectChecked(String total) {
		String regJD = "^(\\d|([1-9]\\d+))(\\.\\d{1,2})?$";
		Pattern regex = Pattern.compile(regJD);
		Matcher matcher = regex.matcher(total);
		return matcher.matches();
	}

}
