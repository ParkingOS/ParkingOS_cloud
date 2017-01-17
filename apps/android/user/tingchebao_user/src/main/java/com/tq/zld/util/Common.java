package com.tq.zld.util;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

/**
 * 通用设置类
 *
 * @author Laoyao 20140606
 */
public class Common {

    /**
     * 验证手机号
     *
     * @param mobilenumber
     * @return
     */
    public static boolean checkMobile(String mobilenumber) {
        if (TextUtils.isEmpty(mobilenumber)) {
            return false;
        }
        Pattern p = Pattern.compile("^[1][3,4,5,7,8]\\d{9}$"); // 验证手机号
        Matcher m = p.matcher(mobilenumber);
        return m.matches();
    }

    /**
     * 验证输入金额是否有效：保留两位小数，非零，且小于10000
     *
     * @param money
     * @return
     */
    public static String checkMoney(String money) {
        if (TextUtils.isEmpty(money)) {
            return "";
        }
        try {
            String template = "^\\d{0,4}$";
            if (money.contains(".")) {
                template = "^\\d{1,4}\\.(\\d{0,2})?$";
                if (money.endsWith(".")) {
                    money += "00";
                }
            }
            Pattern p = Pattern.compile(template);
            Matcher m = p.matcher(money);
            if (m.matches() && Double.parseDouble(money) != 0) {
                return money;
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }

    /**
     * 验证车牌号: 1、将小写转为大写再做匹配 2、仅支持普通民用车牌（如：京A12345）和大使馆车牌（如：使A12345或使123456）
     *
     * @param plate
     * @return
     */
    public static boolean checkPlate(String plate) {
        if (TextUtils.isEmpty(plate)) {
            return false;
        }
        String[] provinces = new String[]{"京", "沪", "浙", "苏", "粤", "鲁", "晋",
                "冀", "豫", "川", "渝", "辽", "吉", "黑", "皖", "鄂", "湘", "赣", "闽",
                "陕", "甘", "宁", "蒙", "津", "贵", "云", "桂", "琼", "青", "新", "藏",
                "港", "澳"};
        Pattern p;
        Matcher m;
        String firstChar = String.valueOf(plate.charAt(0));
        plate = plate.replace(firstChar, "");
        if (Arrays.asList(provinces).contains(firstChar)) {// 普通民用车牌
            p = Pattern.compile("^[A-Z][A-Z_0-9]{5}$");
            m = p.matcher(plate);
            return m.matches();
        } else if ("使".equals(firstChar)) {// 大使馆车牌
            p = Pattern.compile("^[A-Z_0-9]{6}$");
            m = p.matcher(plate);
            return m.matches();
        }
        return false;
    }

    /**
     * 验证车场名称是否符合规则
     *
     * @param name
     * @return
     */
    public static boolean checkParkName(String name) {
        String reg = "[\u4e00-\u9fa5]*[a-z]*[A-Z]*\\d*-*_*";
        return !TextUtils.isEmpty(name) && name.replaceAll(reg, "").length() == 0;
    }
}
