package com.tq.zld.util;

/**
 * Author：ClareChen
 * E-mail：ggchaifeng@gmail.com
 * Date：  15/7/30 上午11:50
 */
public class MathUtils {

    /**
     * 去掉double后面的.0或.00等无用字段
     *
     * @param d
     * @return
     */
    public static String parseIntString(double d) {
        String str = String.valueOf(d);
        return parseIntString(str);
//        int i = (int) d;
//        return d == i ? String.valueOf(i) : String.valueOf(d);
    }

    /**
     * 去掉double后面的.0或.00等无用字段
     *
     * @param number 字符串格式的double数字
     * @return
     */
    public static String parseIntString(String number) {
        LogUtils.i(MathUtils.class, "original double: --->> " + number);
        if (number.contains(".")) {
            while (number.endsWith("0")) {
                number = number.substring(0, number.length() - 1);
            }
            if (number.endsWith(".")) {
                number = number.substring(0, number.length() - 1);
            }
        }
        LogUtils.i(MathUtils.class, "parsed double: --->> " + number);
        return number;
    }
}
