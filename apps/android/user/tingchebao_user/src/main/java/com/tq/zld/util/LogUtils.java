package com.tq.zld.util;

import android.util.Log;

import com.tq.zld.BuildConfig;

/**
 * 日志记录
 */
public class LogUtils {

    @Deprecated
    public static void v(Class clazz, String msg) {
        if (BuildConfig.LOG_DEBUG) {
            Log.v(clazz.getSimpleName(), msg);
        }
    }

    @Deprecated
    public static void d(Class clazz, String msg) {
        if (BuildConfig.LOG_DEBUG) {
            Log.d(clazz.getSimpleName(), msg);
        }
    }

    /**
     * info，打印行号
     * @param msg
     */
    public static void i(String msg){
        if (BuildConfig.LOG_DEBUG) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            Log.i(tag, msg);
        }
    }

    @Deprecated
    public static void i(Class clazz, String msg) {
        if (BuildConfig.LOG_DEBUG) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            Log.i(tag, msg);
        }
    }

    @Deprecated
    public static void e(Class clazz, String msg) {
        if (BuildConfig.LOG_DEBUG) {
            Log.e(clazz.getSimpleName(), msg);
        }
    }

    /**
     * error，打印行号
     * @param msg
     */
    public static void e(String msg){
        if (BuildConfig.LOG_DEBUG) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            Log.e(tag, msg);
        }
    }

    /**
     * warn，打印行号
     * @param msg
     */
    public static void w(String msg){
        if (BuildConfig.LOG_DEBUG) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            Log.w(tag, msg);
        }
    }
    /**
     * debug，打印行号
     * @param msg
     */
    public static void d(String msg){
        if (BuildConfig.LOG_DEBUG) {
            StackTraceElement caller = getCallerStackTraceElement();
            String tag = generateTag(caller);
            Log.d(tag, msg);
        }
    }

    @Deprecated
    public static void w(Class clazz, String msg) {
        if (BuildConfig.LOG_DEBUG) {
            Log.w(clazz.getSimpleName(), msg);
        }
    }

    private static StackTraceElement getCallerStackTraceElement() {
        return Thread.currentThread().getStackTrace()[4];
    }

    private static String generateTag(StackTraceElement caller) {
        String callerClazzName = caller.getClassName(); // 获取到类名
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        String tag = String.format("%s.%s:%dL", callerClazzName, caller.getMethodName(),caller.getLineNumber()); // 替换
//        tag = "tcb" + ":" + tag;
        return tag;
    }

}