package com.tq.zld.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import com.tq.zld.service.ObtainMsgService;

/**
 * 使用AlarmManager实现轮询，API 19以后，由于系统实现所有repeat方式的Alarm唤醒时间不固定，故此方法轮询带有很大不确定性。
 * 已废弃。
 */
@Deprecated
public class TimerUtils {

    private static AlarmManager manager;
    private static PendingIntent pendingIntent;

    /**
     * 开始轮询消息
     *
     * @param context
     * @param firsts  首次取消息的时间（单位：s）
     * @param seconds 轮询间隔（单位：s）
     */
    @Deprecated
    public static void startPollingService(Context context, int firsts,
                                           int seconds) {
        manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ObtainMsgService.class);
        intent.setAction("com.tq.zld.obtainMsg");
        pendingIntent = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // 触发服务的起始时间;
        long triggerAtTime = SystemClock.elapsedRealtime() + firsts * 1000;
        // 使用AlarmManager的setRepeating方法设置定期执行的时间间隔(seconds秒)和需要执行的service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
        }
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,
                seconds * 1000, pendingIntent);
    }

    /**
     * 停止轮询消息
     *
     * @param context
     */
    @Deprecated
    public static void stopPollingService(Context context) {
        // 取消正在执行的服务
        if (manager == null) {
            manager = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
        }
        // 包装需要执行Service的Intent
        if (pendingIntent == null) {
            Intent service = new Intent(context, ObtainMsgService.class);
            service.setAction("com.tq.zld.obtainMsg");
            pendingIntent = PendingIntent.getService(context, 0, service,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        manager.cancel(pendingIntent);
    }
}
