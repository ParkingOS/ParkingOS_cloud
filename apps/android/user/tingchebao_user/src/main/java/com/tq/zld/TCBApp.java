package com.tq.zld;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.tq.zld.im.HXSDKHelperImpl;
import com.tq.zld.util.LogUtils;
import com.umeng.analytics.AnalyticsConfig;

public class TCBApp extends Application {

    /**
     * 用户当前地理位置，实时更新。
     */
    public static LatLng mLocation;// 用户当前位置
    public static String mServerUrl;// 服务器地址

    public static boolean mIsEngineInitSuccess = false;// 导航是否初始化成功
    public static String mMobile;
    public static String mHX;

    private static TCBApp mApplication;
    private SharedPreferences mConfigSP;

    private RequestQueue mRequests;

    public static HXSDKHelperImpl hxsdkHelper = new HXSDKHelperImpl();

    // getApplicationContext();
    public static synchronized TCBApp getAppContext() {
        return mApplication;
    }

    /**
     * 获取服务器地址：本地或线上
     *
     * @return
     */
    private String getServerUrl() {
        switch (BuildConfig.BUILD_TYPE) {
            case "beta":
            case "release":
                return getString(R.string.url_release);
            case "debug":
                // 测试写个本地账号，省去登录步骤
//                saveString(R.string.sp_mobile, "13641309140");
        }
        return readString(R.string.sp_url_server, getString(R.string.url_release));
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //突破65k限制
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
        mConfigSP = getSharedPreferences("config", MODE_PRIVATE);
        mServerUrl = getServerUrl();
        mApplication = this;
        mMobile = readString(R.string.sp_mobile, "");
        mHX = readString(R.string.sp_hx,"");
        LogUtils.i("友盟渠道名称: --->> " + AnalyticsConfig.getChannel(this));

        initImageLoader();

        //初始化环信
        if(BuildConfig.IM_DEBUG) {
            hxsdkHelper.onInit(mApplication);
        }

    }

    public void initImageLoader() {
        if (ImageLoader.getInstance().isInited()) {
            return;
        }
        // File cacheDir = StorageUtils.getCacheDirectory(this);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this)
                // .memoryCacheExtraOptions(480, 800) // default = device screen
                // dimensions
                // .discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75)
                .taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR)
                .threadPoolSize(3)
                        // default
                        // .threadPriority(Thread.NORM_PRIORITY - 1)
                        // default
                        // .tasksProcessingOrder(QueueProcessingType.FIFO)
                        // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                        // .memoryCacheSize(2 * 1024 * 1024)
                        // .discCache(new UnlimitedDiscCache(cacheDir))
                        // default
                        // .discCacheSize(50 * 1024 * 1024).discCacheFileCount(100)
                        // .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                        // default
                        // .imageDownloader(new BaseImageDownloader(this)) // default
                        // .imageDecoder(new BaseImageDecoder()) // default
                        // .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                        // default
                .build();
        ImageLoader.getInstance().init(config);
    }

    public RequestQueue getRequestQueue() {
        if (mRequests == null) {
            synchronized (TCBApp.class) {
                if (mRequests == null) {
                    mRequests = Volley.newRequestQueue(this);
                }

            }
        }
        return mRequests;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified then
     * it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? req.getUrl() : tag);

        LogUtils.d(String.format("Adding request to queue: --->> %s", req.getUrl()));

        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue, if tag is specified then
     * it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, Object tag) {
        // set the default tag if tag is empty
        req.setTag(tag == null ? req.getUrl() : tag);

        LogUtils.d(String.format("Adding request to queue: --->> %s", req.getUrl()));

        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     */
    public <T> void addToRequestQueue(Request<T> req) {
        addToRequestQueue(req, "");
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important to
     * specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequests != null) {
            LogUtils.i(getClass(), "cancel pending request: --->> " + tag.toString());
            mRequests.cancelAll(tag);
        }
    }

    /**
     * 保存boolean到默认的名为“config”的SharedPreferences文件中
     * 此方法为异步保存
     *
     * @param keyResId key的资源ID，一般保存在/values/sharedprefs_keys.xml资源文件中
     * @param value
     * @return
     */
    public void saveBoolean(@NonNull int keyResId, @NonNull boolean value) {
        mConfigSP.edit().putBoolean(getString(keyResId), value).apply();
    }

    /**
     * 保存boolean到默认的名为“config”的SharedPreferences文件中
     * 此方法为同步保存
     *
     * @param keyResId key的资源ID，一般保存在/values/sharedprefs_keys.xml资源文件中
     * @param value
     * @return
     */
    public void saveBooleanSync(@NonNull int keyResId, @NonNull boolean value) {
        mConfigSP.edit().putBoolean(getString(keyResId), value).commit();
    }

    /**
     * 保存int到默认的名为“config”的SharedPreferences文件中
     * 此方法为异步保存
     *
     * @param keyResId key的资源ID，一般保存在/values/sharedprefs_keys.xml资源文件中
     * @param value
     * @return
     */
    public void saveInt(@NonNull int keyResId, @NonNull int value) {
        mConfigSP.edit().putInt(getString(keyResId), value).apply();
    }

    /**
     * 保存int到默认的名为“config”的SharedPreferences文件中
     * 此方法为同步保存
     *
     * @param keyResId key的资源ID，一般保存在/values/sharedprefs_keys.xml资源文件中
     * @param value
     * @return
     */
    public void saveIntSync(@NonNull int keyResId, @NonNull int value) {
        mConfigSP.edit().putInt(getString(keyResId), value).commit();
    }

    /**
     * 保存long到默认的名为“config”的SharedPreferences文件中
     * 此方法为异步保存
     *
     * @param keyResId key的资源ID，一般保存在/values/sharedprefs_keys.xml资源文件中
     * @param value
     * @return
     */
    public void saveLong(@NonNull int keyResId, @NonNull long value) {
        mConfigSP.edit().putLong(getString(keyResId), value).apply();
    }

    /**
     * 保存long到默认的名为“config”的SharedPreferences文件中
     * 此方法为同步保存
     *
     * @param keyResId key的资源ID，一般保存在/values/sharedprefs_keys.xml资源文件中
     * @param value
     * @return
     */
    public void saveLongSync(@NonNull int keyResId, @NonNull long value) {
        mConfigSP.edit().putLong(getString(keyResId), value).commit();
    }

    /**
     * 保存String到默认的名为“config”的SharedPreferences文件中
     * 此方法为异步保存
     *
     * @param keyResId key的资源ID，一般保存在/values/sharedprefs_keys.xml资源文件中
     * @param value
     * @return
     */
    public void saveString(@NonNull int keyResId, @NonNull String value) {
        mConfigSP.edit().putString(getString(keyResId), value).apply();
    }

    /**
     * 保存String到默认的名为“config”的SharedPreferences文件中
     * 此方法为同步保存
     *
     * @param keyResId key的资源ID，一般保存在/values/sharedprefs_keys.xml资源文件中
     * @param value
     * @return
     */
    public void saveStringSync(@NonNull int keyResId, @NonNull String value) {
        mConfigSP.edit().putString(getString(keyResId), value).commit();
    }

    /**
     * 从默认的名为“config”的SharedPreferences文件中读取boolean值
     *
     * @param keyResId     key的资源ID，一般保存在/values/sharedprefs_keys.xml资源文件中
     * @param defaultValue
     * @return
     */
    public boolean readBoolean(@NonNull int keyResId, boolean defaultValue) {
        return mConfigSP.getBoolean(getString(keyResId), defaultValue);
    }

    /**
     * 从默认的名为“config”的SharedPreferences文件中读取int值
     *
     * @param keyResId     key的资源ID，一般保存在/values/sharedprefs_keys.xml资源文件中
     * @param defaultValue
     * @return
     */
    public int readInt(@NonNull int keyResId, int defaultValue) {
        return mConfigSP.getInt(getString(keyResId), defaultValue);
    }

    /**
     * 从默认的名为“config”的SharedPreferences文件中读取long值
     *
     * @param keyResId     key的资源ID，一般保存在/values/sharedprefs_keys.xml资源文件中
     * @param defaultValue
     * @return
     */
    public long readLong(@NonNull int keyResId, long defaultValue) {
        return mConfigSP.getLong(getString(keyResId), defaultValue);
    }

    /**
     * 从默认的名为“config”的SharedPreferences文件中读取String值
     *
     * @param keyResId     key的资源ID，一般保存在/values/sharedprefs_keys.xml资源文件中
     * @param defaultValue
     * @return
     */
    public String readString(@NonNull int keyResId, String defaultValue) {
        return mConfigSP.getString(getString(keyResId), defaultValue);
    }

    /**
     * 获取默认的名为“config”的SharedPreferences文件
     *
     * @return
     */
    public SharedPreferences getConfigPrefs() {
        return mConfigSP;
    }

    /**
     * 获取当前账户的SharedPreferences文件
     *
     * @return
     */
    public SharedPreferences getAccountPrefs() {
        return getSharedPreferences(TCBApp.mMobile, MODE_PRIVATE);
    }
}