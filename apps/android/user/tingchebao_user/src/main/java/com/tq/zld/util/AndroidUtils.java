package com.tq.zld.util;

import java.io.DataOutputStream;
import java.util.List;

import com.tq.zld.TCBApp;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;

public class AndroidUtils {

    /**
     * 判断Activity是否正处于栈顶
     *
     * @param activity
     * @return
     */
    public static boolean isActivityForeground(Activity activity) {
        return isActivityForeground(activity, activity.getClass());
    }

    /**
     * 判断Activity是否正处于栈顶
     *
     * @param context
     * @param clazz
     * @return
     */
    public static boolean isActivityForeground(Context context,
                                               Class<? extends Activity> clazz) {
        List<RunningTaskInfo> runningTaskInfos = ((ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1);
        return clazz.getName().equals(
                runningTaskInfos.get(0).topActivity.getClassName());
    }

    /**
     * 获取当前手机号
     *
     * @param context
     * @return
     */
    public static String getPhoneNumber(Context context) {
        TelephonyManager teleMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return teleMgr.getLine1Number();
    }

    /**
     * 判断当前设备是否已root
     *
     * @param context
     * @return
     */
    public static boolean isDeviceRooted(Context context) {
        String cmd = "chmod 777 " + context.getPackageCodePath();
        boolean root = rootCommand(cmd);
        return root;
    }

    private static boolean rootCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 获取应用版本号
     *
     * @return
     */
    public static int getVersionCode() {
        try {
            PackageManager manager = TCBApp.getAppContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(TCBApp.getAppContext()
                    .getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取应用版本名称
     *
     * @return
     */
    public static String getVersionName() {
        try {
            PackageManager manager = TCBApp.getAppContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(TCBApp.getAppContext()
                    .getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取设备的IMEI号
     *
     * @return
     */
    public static String getIMEI() {
        TelephonyManager tm = (TelephonyManager) TCBApp.getAppContext().getSystemService(Activity.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

}