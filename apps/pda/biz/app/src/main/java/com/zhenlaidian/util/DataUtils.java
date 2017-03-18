package com.zhenlaidian.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DataUtils {
	
	/**
	 * 时间戳转换成字符窜
	 */
	public static String toTimestamp(String time) {
		Date d = new Date(Long.parseLong(time));
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM.dd HH:mm:ss");
		return sf.format(d);
	}
}
