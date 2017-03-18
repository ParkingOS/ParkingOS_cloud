package com.zhenlaidian.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

@SuppressLint("SimpleDateFormat")
public class TimeTypeUtil {
    public static final int SECONDS_IN_DAY = 60 * 60 * 24;
    public static final long MILLIS_IN_DAY = 1000L * SECONDS_IN_DAY;

    // 计算出两个时间差
    public static String processTwo(long startMil, long endMil) {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(startMil);
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(endMil);

        StringBuilder time = new StringBuilder();
        int year = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);
        if (year != 0) {
            time.append(year).append("年");
        }
        int month = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);
        if (month != 0) {
            time.append(month).append("月");
        }
        int day = c2.get(Calendar.DAY_OF_MONTH) - c1.get(Calendar.DAY_OF_MONTH);
        if (day != 0) {
            time.append(day).append("日");
        }
        int hour = c2.get(Calendar.HOUR_OF_DAY) - c1.get(Calendar.HOUR_OF_DAY);
        time.append(hour).append("小时");
        int min = c2.get(Calendar.MINUTE) - c1.get(Calendar.MINUTE);
        time.append(min).append("分");
        int sec = c2.get(Calendar.SECOND) - c1.get(Calendar.SECOND);
        time.append(sec).append("秒");
        return time.toString();
    }

    // 传入一个时间毫秒值--计算出传入时间和当前时间相差的时间；
    public static String process(long startMil) {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(startMil);
        Calendar c2 = Calendar.getInstance();
        StringBuilder time = new StringBuilder();
        int year = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);
        if (year != 0) {
            time.append(year).append("年");
        }
        int month = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);
        if (month != 0) {
            time.append(month).append("月");
        }
        int day = c2.get(Calendar.DAY_OF_MONTH) - c1.get(Calendar.DAY_OF_MONTH);
        if (day != 0) {
            time.append(day).append("日");
        }
        int hour = c2.get(Calendar.HOUR_OF_DAY) - c1.get(Calendar.HOUR_OF_DAY);
        time.append(hour).append("小时");
        int min = c2.get(Calendar.MINUTE) - c1.get(Calendar.MINUTE);
        time.append(min).append("分");
        int sec = c2.get(Calendar.SECOND) - c1.get(Calendar.SECOND);
        time.append(sec).append("秒");
        return time.toString();
    }

    /*
     * 计算两个时间的差值
     */
    public static String getTimeString(Long start, Long end) {
        Long date = (end - start) / (3600 * 24);
        // Log.e("TimeTypeUtil", "停车的天数为："+ date);
        Long hour = ((end - start) % 86400) / 3600;
        Long minute = ((end - start) % 3600) / 60;
        String result = "";
        if (date == 0) {
            if (hour == 0)
                result = minute + "分";
            else
                result = hour + "小时" + minute + "分";
        } else {
            result = date + "天" + hour + "小时" + minute + "分";
        }
        return result;
    }

    public static String getTime(Long start) {
        // Date date = new Date(System.currentTimeMillis());
        Long now = System.currentTimeMillis() / 1000;
        Long date = (now - start) / (3600 * 24);
        // Log.e("TimeTypeUtil", "停车的天数为："+ date);
        Long hour = ((now - start) % 86400) / 3600;
        Long minute = ((now - start) % 3600) / 60;
        String result = "";
        if (date == 0) {
            if (hour == 0)
                result = minute + "分";
            else
                result = hour + "小时" + minute + "分";
        } else {
            result = date + "天" + hour + "小时" + minute + "分";
        }
        return result;
    }

    /**
     * 格式化时间戳。2015.12.30 13:22
     *
     * @param time
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getStringTime(Long time) {

        SimpleDateFormat dateaf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return dateaf.format(time * 1000);
    }

    /**
     * 格式化系统当前时间;格式为月日 12/23 12:10
     * @param time
     * @return
     */
    public static String getMothDay(Long time) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm");
        return dateFormat.format(time);
    }

    /**
     * 计算传入时间与本地时间差值；
     *
     * @param time
     * @return
     */
    public static Long getDifferenceTime(Long time) {
        Long now = System.currentTimeMillis();
        return time - now;
    }

    /**
     * 判断两个时间毫秒值是否在一天；
     *
     * @param ms1
     * @param ms2
     * @return
     */
    public static boolean isSameDayOfMillis(final long ms1, final long ms2) {
        final long interval = ms1 - ms2;
        return interval < MILLIS_IN_DAY && interval > -1L * MILLIS_IN_DAY && toDay(ms1) == toDay(ms2);
    }

    private static long toDay(long millis) {
        return (millis + TimeZone.getDefault().getOffset(millis)) / MILLIS_IN_DAY;
    }

    /**
     * 判断一个毫秒时间是上午还是下午
     *
     * @param millis
     * @return
     */
    public static Boolean isAM(long millis) {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(millis);
        int am = c1.get(Calendar.AM_PM);
        if (am == 0) {// 结果为“0”是上午 结果为“1”是下午
            return true;
        } else {
            return false;
        }
    }
}
