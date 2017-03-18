
package com.zhenlaidian.decode;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.zhenlaidian.util.CommontUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


/**
 * <pre>
 * 功能说明: UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告.
 * 日期:	2015年3月30日
 * 开发者:	HZC
 *
 * 历史记录
 *    修改内容：
 *    修改人员：
 *    修改日期： 2015年3月30日
 * </pre>
 */
public class CrashHandler implements UncaughtExceptionHandler {

    public static final String TAG = "CrashHandler";

    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //CrashHandler实例
    private static CrashHandler INSTANCE = new CrashHandler();
    //程序的Context对象
    private Context mContext;
    //用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }

//            Intent intent = new Intent(mContext, LoginActivity.class);
//            PendingIntent restartIntent = PendingIntent.getActivity(
//                    mContext, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
            //退出程序
//            AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
//                    restartIntent); // 1秒钟后重启应用
//            ((MyApplication) mContext).closeActivity();
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
        //收集设备参数信息
        collectDeviceInfo(mContext);
        //保存日志文件
        saveCrashInfo2File(ex);
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(CommontUtils.getTimespanss());
        sb.append("CrashHandler>>>>");
        sb.append(result);
        Log.e("---", result);
        try {
//            String fileName = FileUtil.getSDCardPath()
//                    + "/tcb" + "/" + "zldlog.txt";
            File file = CommontUtils.createSDFile(mContext);
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                String path = FileUtil.getSDCardPath() + "/tcb/";
//                File dir = new File(path);
//                if (!dir.exists()) {
//                    dir.mkdirs();
//                }
                String fileName = file.getAbsolutePath();
                FileOutputStream fos = new FileOutputStream(fileName, true);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
            return "";
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }
        return null;
    }

//    public static void WriteLog(Context mContext,String str) {
//        CommontUtils.writeSDFile(mContext,"",str);
//        WriteLog(str);
//        StringBuffer sb = new StringBuffer();
//        sb.append(CommontUtils.getTimespanss());
//        sb.append("  LOG  :");
//        sb.append(str);
//        sb.append("\n");
//        try {
//            File file = CommontUtils.createSDFile(mContext);
//            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                String fileName = file.getAbsolutePath();
//                FileOutputStream fos = new FileOutputStream(fileName, true);
//                fos.write(sb.toString().getBytes());
//                fos.close();
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "an error occured while writing file...", e);
//        }
//    }

//    public static void WriteLog(String str) {
//        try {
//            String fileName = CrashHandler.getSDCardPath() + "/tingchebao" + "/" + "tcbdata.txt";
//            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                String path = CrashHandler.getSDCardPath() + "/tingchebao/";
//                File dir = new File(path);
//                if (!dir.exists()) {
//                    dir.mkdirs();
//                }
//                FileOutputStream fos = new FileOutputStream(fileName, true);
//                fos.write((CommontUtils.getTimespanss()+str+"\n").getBytes());
//                fos.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 获取SDCard的目录路径功能
//     *
//     * @return
//     */
//    public static String getSDCardPath() {
//        String result = null;
//        try {
//            // 判断SDCard是否存在
//            boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
//            Log.i("存在sdcard路径：", "" + sdcardExist);
//            if (sdcardExist) {
//                File sdcardDir = Environment.getExternalStorageDirectory();
//                result = sdcardDir.toString();
//            }
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return result;
//    }

    public static void WriteLog(Context context,String str) {
        try {
//            writeSDFile(context,"  "+CommontUtils.PhoneModel(),str );
            String fileName = CrashHandler.getSDCardPath() + "/tcb" + "/" + "shoufeiyuan.txt";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = CrashHandler.getSDCardPath() + "/tcb/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(fileName, true);
                fos.write((CommontUtils.getTimespanss()+str+"\n").getBytes());
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void writeSDFile(Context context, String describe, String str) {
        try {
            File file = getOutputMediaFile(context);
            FileWriter fw = new FileWriter(file.getAbsolutePath(), true);
            fw.write(CommontUtils.getTimespanss() + describe + "-->" + str + "\n");
            fw.flush();
            fw.close();
            System.out.println(fw);
        } catch (Exception e) {
        }
    }
    /**
     * 获取SD卡路径
     *
     * @return
     */
    public static String getSDCardPath() {
        String result = null;
        try {
            // 判断SDCard是否存在
            boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            Log.i("存在sdcard路径：", "" + sdcardExist);
            if (sdcardExist) {
                File sdcardDir = Environment.getExternalStorageDirectory();
                result = sdcardDir.toString();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public static File getOutputMediaFile(Context mContext) {
        File mediaStorageDir = null;
        if (Environment.getExternalStorageState() != null) {
//			File dir = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
//			mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "TestCameraFile");
            mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "/TingCheBao");
            Log.d("MyCameraApp", " if create directory" + mediaStorageDir);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory");
                    return null;
                }
            }
        } else {
            mediaStorageDir = new File(mContext.getCacheDir(), "TestTingCheBao");
            Log.d("MyCameraApp", " else create directory" + mediaStorageDir);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory");
                    return null;
                }
            }
            Log.d("MyCameraApp", "路径为" + mediaStorageDir);
        }
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
//        if (type == MEDIA_TYPE_IMAGE) {
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "shoufei"+ ".txt");
//        } else {
//            return null;
//        }
        Log.d("MyCameraApp", " return directory" + mediaFile.getAbsolutePath());
        return mediaFile;
    }
}