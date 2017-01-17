package com.tq.zld.util;

import java.util.Calendar;

/**
 * Created by Gecko on 2015/10/13.
 */
public class TimeUtils {
    private static long currentDayMillis = 0;
    private TimeUtils(){}

    /**
     * 停车券剩余毫秒数，转换成剩余天数
     * @param limitDayMillis
     * @return
     */
    public static long millisToDay(long limitDaySecond){
        //传进来的是秒
        limitDaySecond *= 1000;
        if (currentDayMillis == 0) {
            Calendar c = Calendar.getInstance();
            c.clear(Calendar.HOUR);//修复不能充值到零点BUG，java官方给出方案。两个都调用就能重置。
            c.clear(Calendar.HOUR_OF_DAY);
            c.clear(Calendar.MINUTE);
            c.clear(Calendar.SECOND);
            currentDayMillis = c.getTimeInMillis();
        }

        long limitDay = 0;
        if (limitDaySecond - currentDayMillis > 0) {
            limitDay = (limitDaySecond - currentDayMillis) / (24 * 60 * 60 * 1000);
        }

        return limitDay;

    }
}
