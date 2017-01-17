package com.tq.zld.util;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.tq.zld.TCBApp;

/**
 * Created by GT on 2015/9/16.
 */
public class ToastUtils {
    private ToastUtils(){}

    static Toast mCache;

    public static void show(Context ctx, String msg) {
        cancel();
        mCache = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT);
        mCache.show();
    }

    /**
     * short时长吐司，默认使用Application作为上下文
     * @param msg
     */
    public static void show(String msg){
        show(TCBApp.getAppContext(),msg);
    }

    /**
     * long时长吐司，默认使用Application作为上下文
     * @param msg
     */
    public static void showLong(String msg){
        Toast.makeText(TCBApp.getAppContext(), msg, Toast.LENGTH_LONG).show();
    }

    public static void cancel(){
        if (mCache != null) {
            mCache.cancel();
        }
    }

}
