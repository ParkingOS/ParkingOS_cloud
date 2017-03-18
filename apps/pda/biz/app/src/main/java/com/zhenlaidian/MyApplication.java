package com.zhenlaidian;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Process;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.umeng.analytics.MobclickAgent;
import com.zhenlaidian.decode.CrashHandler;
import com.zhenlaidian.util.MyLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyApplication extends Application {

    public static boolean isrunning = false;// 记录
    public static Context applicationContext;
    private static MyApplication instance;
//    public static RequestQueue queue;
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        instance = this;
        MyLog.w("MyApplication", "onCreate");
//        SDKInitializer.initialize(getApplicationContext());//百度地图初始化;
        initSpeechSynthesizer();// 初始化讯飞语音；
        initImageLoader();// 初始化imageLoader；
//        商米错误日志
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
//        错误日志 保存本地
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
//        queue = new Volley().newRequestQueue(getApplicationContext());
    }
//    public static RequestQueue getRequestQueue() {
//        return queue;
//    }
    public static MyApplication getInstance() {
        return instance;
    }

    public void initSpeechSynthesizer() {
        SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID + "=" + getString(R.string.xunfei_id));
    }

    private void initImageLoader() {
        // TODO Auto-generated method stub
        DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.max_camera)
                .showImageForEmptyUri(R.drawable.max_camera).showImageOnFail(R.drawable.max_camera).resetViewBeforeLoading(false)
                .cacheInMemory(false).cacheOnDisk(false).considerExifParams(false)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).bitmapConfig(Bitmap.Config.ARGB_8888)
                .displayer(new SimpleBitmapDisplayer()).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .memoryCacheExtraOptions(720, 1280).threadPoolSize(3).threadPriority(Thread.MIN_PRIORITY)
                .tasksProcessingOrder(QueueProcessingType.LIFO).denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024)).memoryCacheSize(2 * 1024 * 1024).memoryCacheSizePercentage(13)
                .imageDownloader(new BaseImageDownloader(getApplicationContext())).imageDecoder(new BaseImageDecoder(true))
                .defaultDisplayImageOptions(options).build();
        ImageLoader.getInstance().init(config);
    }

    /**
     * 退出应用程序 后台服务不关闭
     */
    public static void exit() {
        MobclickAgent.onKillProcess(MyApplication.getInstance());
        Process.killProcess(Process.myPid());
    }

    /**
     * 捕获错误信息的handler
     */
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
//            Log.e(TAG, "uncaughtException crash", ex);
            try {
                ex.printStackTrace(new PrintStream(createErrorFile()));
            } catch (FileNotFoundException e) {
//                Log.e(TAG, "创建异常文件失败");
                e.printStackTrace();
            }
        }
    };

    /**
     * 创建异常文件
     */
    public static File createErrorFile() {
        File file = null;
        try {
            String DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sunmi/settings/log/";
            String NAME = getCurrentDateString() + ".txt";
            File dir = new File(DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file = new File(dir, NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 获取当前日期
     */
    public static String getCurrentDateString() {
        String result = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss",
                Locale.getDefault());
        Date nowDate = new Date();
        result = sdf.format(nowDate);
        return result;
    }


}
