package com.tq.zld.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.text.TextUtils;

public class DateUtils {

    public static final String UNIT_YEAR = "年";
    public static final String UNIT_MONTH = "个月";
    public static final String UNIT_DAY = "天";
    public static final String UNIT_HOUR = "小时";
    public static final String UNIT_MINUTE = "分钟";
    public static final String UNIT_SECOND = "秒";
    public static final String UNIT_MILLSECOND = "毫秒";

    private static String getDuration(String begin, String end, int accuracy) {
        if (!TextUtils.isDigitsOnly(begin) || !TextUtils.isDigitsOnly(end)) {
            throw new IllegalArgumentException("begin&end must be digits only!");
        }
        long duration = Long.parseLong(end) - Long.parseLong(begin);
        if (duration < 0) {
            throw new IllegalArgumentException(
                    "the end time should be greater than the begin!");
        }
        Calendar cBegin = Calendar.getInstance();
        cBegin.setTimeInMillis(0);
        Calendar cEnd = Calendar.getInstance();
        cEnd.setTimeInMillis(duration);
        StringBuilder durationStr = new StringBuilder();
        int year = cEnd.get(Calendar.YEAR) - cBegin.get(Calendar.YEAR);
        if (year > 0) {
            durationStr.append(year + UNIT_YEAR);
        }
        int month = cEnd.get(Calendar.MONTH) - cBegin.get(Calendar.MONTH);
        if (month > 0) {
            durationStr.append(month + UNIT_MONTH);
        }
        int day = cEnd.get(Calendar.DAY_OF_MONTH)
                - cBegin.get(Calendar.DAY_OF_MONTH);
        if (day > 0) {
            durationStr.append(day + UNIT_DAY);
        }
        int hour = cEnd.get(Calendar.HOUR_OF_DAY)
                - cBegin.get(Calendar.HOUR_OF_DAY);
        if (hour > 0) {
            durationStr.append(hour + UNIT_HOUR);
        }
        int minute = cEnd.get(Calendar.MINUTE) - cBegin.get(Calendar.MINUTE);
        if (minute > 0) {
            durationStr.append(minute + UNIT_MINUTE);
        }
        int second = cEnd.get(Calendar.SECOND) - cBegin.get(Calendar.SECOND);
        if (second > 0) {
            durationStr.append(second + UNIT_SECOND);
        }
        int mills = cEnd.get(Calendar.MILLISECOND)
                - cBegin.get(Calendar.MILLISECOND);
        if (mills > 0) {
            durationStr.append(mills + UNIT_MILLSECOND);
        }
        LogUtils.i(DateUtils.class, "duration: --->> " + durationStr.toString());
        switch (accuracy) {
            case Calendar.MILLISECOND:
                if (mills != 0) {
                    LogUtils.i(DateUtils.class, "mills: --->> " + mills);
                    return durationStr.substring(
                            0,
                            durationStr.indexOf(UNIT_MILLSECOND)
                                    + UNIT_MILLSECOND.length());
                }
            case Calendar.SECOND:
                if (second != 0) {
                    LogUtils.i(DateUtils.class, "second: --->> " + second);
                    return durationStr
                            .substring(0, durationStr.indexOf(UNIT_SECOND)
                                    + UNIT_SECOND.length());
                }
            case Calendar.MINUTE:
                if (minute != 0) {
                    LogUtils.i(DateUtils.class, "minute: --->> " + minute);
                    return durationStr
                            .substring(0, durationStr.indexOf(UNIT_MINUTE)
                                    + UNIT_MINUTE.length());
                }
            case Calendar.HOUR_OF_DAY:
                if (hour != 0) {
                    LogUtils.i(DateUtils.class, "hour: --->> " + hour);
                    return durationStr.substring(0, durationStr.indexOf(UNIT_HOUR)
                            + UNIT_HOUR.length());
                }
            case Calendar.DAY_OF_MONTH:
                if (day != 0) {
                    LogUtils.i(DateUtils.class, "day: --->> " + day);
                    return durationStr.substring(0, durationStr.indexOf(UNIT_DAY)
                            + UNIT_DAY.length());
                }
            case Calendar.MONTH:
                if (month != 0) {
                    LogUtils.i(DateUtils.class, "month: --->> " + month);
                    return durationStr.substring(0, durationStr.indexOf(UNIT_MONTH)
                            + UNIT_MONTH.length());
                }
            case Calendar.YEAR:
                return year == 0 ? "" : durationStr.substring(0,
                        durationStr.indexOf(UNIT_YEAR) + UNIT_YEAR.length());
        }
        return durationStr.toString();
    }

    /**
     * 获取两个时间点（毫秒值）之间的时间间隔：精确到分钟（Minute）
     *
     * @param begin 起始时间
     * @param end   结束时间：需大于等于begin
     * @return 例：1年1个月1天1小时1分钟
     */
    public static String getMinDuration(String begin, String end) {
        return getDuration(begin, end, Calendar.MINUTE);
    }

    /**
     * 获取两个时间点（毫秒值）之间的时间间隔：精确到秒（Second）
     *
     * @param begin 起始时间：
     * @param end   结束时间：需大于等于begin
     * @return 例：1年1个月1天1小时1分钟1秒
     */
    public static String getSecDuration(String begin, String end) {
        return getDuration(begin, end, Calendar.SECOND);
    }

    /**
     * 获取两个时间点（毫秒值）之间的时间间隔：精确到小时（Hour）
     *
     * @param begin 起始时间：
     * @param end   结束时间：需大于等于begin
     * @return 例：1年1个月1天1小时
     */
    public static String getHourDuration(String begin, String end) {
        return getDuration(begin, end, Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取两个时间点（毫秒值）之间的时间间隔：精确到毫秒（MilliSecond）
     *
     * @param begin 起始时间：
     * @param end   结束时间：需大于等于begin
     * @return 例：1年1个月1天1小时1分钟1秒1毫秒
     */
    public static String getMillsDuration(String begin, String end) {
        return getDuration(begin, end, Calendar.MILLISECOND);
    }

    /**
     * 获取两个时间点（毫秒值）之间的时间间隔：精确到天（Day）
     *
     * @param begin 起始时间：
     * @param end   结束时间：需大于等于begin
     * @return 例：1年1个月1天
     */
    public static String getDayDuration(String begin, String end) {
        return getDuration(begin, end, Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取两个时间点（毫秒值）之间的时间间隔：精确到月（Month）
     *
     * @param begin 起始时间：
     * @param end   结束时间：需大于等于begin
     * @return 例：1年1个月
     */
    public static String getMonthDuration(String begin, String end) {
        return getDuration(begin, end, Calendar.MONTH);
    }

    /**
     * 获取两个时间点（毫秒值）之间的时间间隔：精确到年（Year）
     *
     * @param begin 起始时间：
     * @param end   结束时间：需大于等于begin
     * @return 例：1年
     */
    public static String getYearDuration(String begin, String end) {
        return getDuration(begin, end, Calendar.YEAR);
    }

    /**
     * 获取两个日期之间相差的月份：当两个日期的DAY_OF_MONTH值不一样时，日期end在begin之后且end的DAY_OF_MONTH值也较大，
     * 则相差月数+1。 例：2014.10.09与2015.08.08相差10个月，而2014.10.09与2015.08.10相差11个月
     *
     * @param begin
     * @param end
     * @return 0表示begin=end，正数表示end处于begin之后的日期，负数反之。
     */
    public static int getMonths(Calendar begin, Calendar end) {
        if (begin == null || end == null) {
            throw new IllegalArgumentException("begin&end can't be null!");
        }
        int year = 0;
        if (begin.isSet(Calendar.YEAR) && end.isSet(Calendar.YEAR)) {
            year = end.get(Calendar.YEAR) - begin.get(Calendar.YEAR);
        }
        int month = 0;
        if (begin.isSet(Calendar.MONTH) && end.isSet(Calendar.MONTH)) {
            month = end.get(Calendar.MONTH) - begin.get(Calendar.MONTH);
        }
        int day = 0;
        if (begin.isSet(Calendar.DAY_OF_MONTH)
                && end.isSet(Calendar.DAY_OF_MONTH)) {
            day = end.get(Calendar.DAY_OF_MONTH)
                    - begin.get(Calendar.DAY_OF_MONTH);
        }
        day = day > 0 ? 1 : 0;
        return year * 12 + month + day;
    }


    /**
     * 格式化时间为昨天，前天等格式
     *
     * @param commentTime
     * @return
     */
    public static String formatTime(Calendar commentTime) {

        String template;
        Calendar currentTime = Calendar.getInstance();

        long duration = currentTime.getTimeInMillis() - commentTime.getTimeInMillis();
        if (duration < 86400000) {
            //间隔小于24小时
            if (commentTime.get(Calendar.DAY_OF_MONTH) != currentTime.get(Calendar.DAY_OF_MONTH)) {
                template = "昨天 HH:mm";
            } else {
                template = "今天 HH:mm";
            }
        } else if (duration < 172800000) {
            if (commentTime.get(Calendar.HOUR_OF_DAY) < currentTime.get(Calendar.HOUR_OF_DAY)) {
                template = "昨天 HH:mm";
            } else {
                template = "MM月dd日 HH:mm";
            }
        } else {
            if (commentTime.get(Calendar.YEAR) != currentTime.get(Calendar.YEAR)) {
                template = "yyyy年MM月dd日 HH:mm";
            } else {
                template = "MM月dd日 HH:mm";
            }
        }
        return new SimpleDateFormat(template, Locale.CHINA)
                .format(new Date(commentTime.getTimeInMillis()));
    }
}
