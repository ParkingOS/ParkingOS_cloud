package com.zhenlaidian.util;

import java.text.DecimalFormat;

public class DataTypeChange {
	
    //把double类型的字符串保留两位小数返回；
	public static Double formatDouble(Object value) {
		if (isDouble(value + "")) {
			DecimalFormat df = new DecimalFormat("#.00");
			String dv = df.format(Double.valueOf(value + ""));
			if (isDouble(dv))
				return Double.valueOf(dv);
		}
		return 0.0d;
	}

	//判断是否是double类型的数据；
	public static boolean isDouble(String value) {
		if (value == null)
			return false;
		try {
			@SuppressWarnings("unused")
			Double d = Double.valueOf(value);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static int toInt(String value){
		try {
			int aa = Integer.parseInt(value);
			return aa;
		} catch (Exception e) {
			return 1;
		}
	}
	
}
