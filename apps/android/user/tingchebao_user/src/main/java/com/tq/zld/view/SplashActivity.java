package com.tq.zld.view;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.easemob.chat.EMChatManager;
import com.igexin.sdk.PushManager;
import com.tq.zld.BuildConfig;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.PayResult;
import com.tq.zld.im.HXSDKHelperImpl;
import com.tq.zld.im.adapter.EMCallBackAdapter;
import com.tq.zld.im.bean.User;
import com.tq.zld.im.db.UserDao;
import com.tq.zld.im.lib.HXSDKHelper;
import com.tq.zld.util.IMUtils;
import com.tq.zld.util.IOUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.fragment.SplashFragment;
import com.tq.zld.view.holder.MenuHolder;
import com.tq.zld.view.manager.IMManager;
import com.tq.zld.view.map.MapActivity;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.SyncListener;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;
import com.umeng.fb.model.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends BaseActivity implements SplashFragment.OnFragmentInteractionListener {

    /**
     * Splash界面版本，每次更换后需递增，初始值为1
     */
    public static final int SPLASH_VERSION = 2;

    private boolean isGuideInterfaceShowing = false;// 标记界面是否有对话框在显示或者正在显示引导界面

    private AlertDialog mAlwaysFinishActivitiesDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initSplash();
        setContentView(R.layout.activity_splash);

        // 是否显示引导界面
        if (TCBApp.getAppContext().readInt(R.string.sp_splash_version, 0) != SPLASH_VERSION) {

            //清空旧版本判断是否显示过Splash界面标志位
            TCBApp.getAppContext().getConfigPrefs().edit().remove("showGuideInterface").apply();
            showGuideInterface();
        }
        deleteDB();

        deleteRemarkPic();
    }

    /**
     * 删除旧版本标记停车未知图片的目录:
     * ./Android/data/包名/files/Pictures/remark/orderid.jpg
     */
    private void deleteRemarkPic() {
        File pictureDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (pictureDir != null && pictureDir.exists()) {
            File remarkDir = new File(pictureDir, "remark");
            if (remarkDir.exists()) {
                IOUtils.deleteDirectory(remarkDir);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 初始化个推服务
        startGeTuiPush();

        // 初始化友盟反馈
        syncFeedback();

        // 检查有无新消息
        checkNewMessage();

        // 导入离线地图数据
        initOfflineMapData();
    }

    // 同步友盟反馈更新提醒
    private void syncFeedback() {
        final FeedbackAgent agent = new FeedbackAgent(this);
        new Thread(new Runnable() {

            @Override
            public void run() {
                UserInfo userInfo = agent.getUserInfo();
                if (userInfo == null) {
                    userInfo = new UserInfo();
                }
                Map<String, String> contact = userInfo.getContact();
                if (contact == null) {
                    contact = new HashMap<>();
                }
                if (!contact.containsKey("phone")) {
                    contact.put("phone", TCBApp.mMobile);
                }
                userInfo.setContact(contact);
                agent.setUserInfo(userInfo);
                agent.updateUserInfo();
            }
        }).start();
        Conversation conversation = agent.getDefaultConversation();
        LogUtils.i(getClass(), "本地友盟反馈会话ID：--->> " + conversation.getId());
        conversation.sync(new SyncListener() {

            @Override
            public void onSendUserReply(List<Reply> arg0) {
            }

            @Override
            public void onReceiveDevReply(List<Reply> arg0) {
                if (arg0 != null && arg0.size() > 0) {
                    LogUtils.i(SplashActivity.class,
                            "友盟更新消息：--->> " + arg0.size());
                    MenuHolder.getInstance().refreshMenu(R.id.rl_menu_feedback,
                            true);
                }
            }
        });
    }

    private void initOfflineMapData() {
        MKOfflineMap offlineMap = new MKOfflineMap();
        offlineMap.init(new MKOfflineMapListener() {
            @Override
            public void onGetOfflineMapState(int arg0, int arg1) {
            }
        });
        offlineMap.importOfflineData();
        offlineMap.destroy();
    }

    private void startGeTuiPush() {
        PushManager pushManager = PushManager.getInstance();
        pushManager.initialize(this);
        if (!pushManager.isPushTurnedOn(this)) {
            pushManager.turnOnPush(this);
        }

        // 更新ClientID
        String cid = pushManager.getClientid(this);
        if (!TextUtils.isEmpty(TCBApp.mMobile) && !TextUtils.isEmpty(cid)) {
            Map<String, String> params = new HashMap<>();
            params.put("cid", cid);
            params.put("mobile", TCBApp.mMobile);
            params.put("action", "addcid");
            updateClientId(params);
        }
    }

    // 检查消息中心有无新消息
    private void checkNewMessage() {
        if (!TextUtils.isEmpty(TCBApp.mMobile)) {
            HashMap<String, String> params = new HashMap<>();
            params.put("action", "getmesg");
            params.put("mobile", TCBApp.mMobile);

            // 修复可能出现的类型转换异常
            SharedPreferences accountPreference = getSharedPreferences(TCBApp.mMobile,
                    MODE_PRIVATE);
            String maxId = "0";
            if (accountPreference.contains(getString(R.string.sp_recently_msg_id))) {
                try {
                    maxId = String.valueOf(accountPreference.getLong(getString(R.string.sp_recently_msg_id), 0));
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    maxId = String.valueOf(accountPreference.getInt(getString(R.string.sp_recently_msg_id), 0));
                    accountPreference.edit().putLong(getString(R.string.sp_recently_msg_id), Long.parseLong(maxId)).apply();
                }
            }
            params.put("maxid", maxId);
            String url = URLUtils.genUrl(TCBApp.mServerUrl + "carowner.do", params);
            StringRequest request = new StringRequest(url, new Response.Listener<String>() {

                @Override
                public void onResponse(String result) {

                    if (!TextUtils.isEmpty(result)
                            && TextUtils.isDigitsOnly(result)
                            && Integer.parseInt(result) > 0) {
                        MenuHolder.getInstance().refreshMenu(
                                R.id.rl_menu_message, true);
                    }
                }
            }, null);
            TCBApp.getAppContext().addToRequestQueue(request);
        }
    }

    public void updateClientId(Map<String, String> params) {
        if (!TextUtils.isEmpty(params.get("cid"))) {
            params.put("hx","1");
            String url = URLUtils.genUrl(TCBApp.mServerUrl + "carlogin.do", params);
            LogUtils.i("update client id url: --->>" + url);
            //{"reuslt":"1","hxname":"hx21770","hxpass":"d9109c07cb54ad183914a39a9e68e5b6"}
            JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    LogUtils.d("-->" + jsonObject.toString());
                    try {
                        String result = jsonObject.getString("result");
                        if (!PayResult.PAY_RESULT_SUCCESS.equals(result)){
                            LogUtils.e("--->> update client id failed !!!");
                        }
                        final String username = jsonObject.getString("hxname");
                        final String password = jsonObject.getString("hxpass");
                        String imgurl = jsonObject.getString("wximgurl");

                        if (!TextUtils.isEmpty(imgurl)) {
                            //缓存头像地址在，侧边栏打开的时候，设置，如果没有获取到的话。
                            TCBApp.getAppContext().saveString(R.string.sp_im_image_url, String.format("%s@.@%s",TCBApp.mMobile,imgurl));
                            MenuHolder.getInstance().refreshPhotoView(imgurl);
                        }
                        //TODO 这里需要环信登陆，虽然在这个位置不恰当，没有比这个更好的位置了。
                        if (BuildConfig.IM_DEBUG && !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)){
                            //缓存环信
                            IMUtils.saveHXAccount(username, password);

                            if (HXSDKHelperImpl.getInstance().isLogined()) {
                                String cuser = EMChatManager.getInstance().getCurrentUser();
                                String hxid = HXSDKHelper.getInstance().getHXId();
                                LogUtils.i(String.format("cuser:%s,hxid:%s", cuser, hxid));
                                if (HXSDKHelperImpl.getInstance().getHXId().equals(username)) {
                                    LogUtils.i("自动登录！");
                                    EMChatManager.getInstance().loadAllConversations();
                                    //缓存环信
                                    IMUtils.saveHXAccount(username, password);
                                } else {
                                    HXSDKHelper.getInstance().logout(true, new EMCallBackAdapter() {
                                        @Override
                                        public void onSuccess() {
                                            LogUtils.e("环信账户不对，退出环信成功");
                                            loginHX(username, password);
                                        }
                                    });
                                }

                            } else {
                                loginHX(username, password);
                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, null);
            TCBApp.getAppContext().addToRequestQueue(request);

        } else {
            String url = URLUtils.genUrl(TCBApp.mServerUrl + "carlogin.do", params);
            LogUtils.i("update client id url: --->>" + url);
            StringRequest request = new StringRequest(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String object) {
                    if (!PayResult.PAY_RESULT_SUCCESS.equals(object)) {
                        LogUtils.e(SplashActivity.class, "--->> update client id failed !!!");
                    }
                }
            }, null);
            TCBApp.getAppContext().addToRequestQueue(request);
        }

    }

    private void loginHX(final String username, final String password){
//        new Thread(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                    }
//                }
//        ).start();

        LogUtils.i(String.format("准备登录>>%s:%s",username,password));
        EMChatManager.getInstance().login(username, password, new EMCallBackAdapter() {
            @Override
            public void onSuccess() {
                LogUtils.i("登录成功>>");
                //缓存用户名密码
                TCBApp.hxsdkHelper.setHXId(username);
                TCBApp.hxsdkHelper.setPassword(password);
                //缓存环信
                IMUtils.saveHXAccount(username, password);

                //加载数据
                EMChatManager.getInstance().loadAllConversations();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(TCBApp.getAppContext(), "登录成功！", Toast.LENGTH_SHORT).show();
//                    }
//                });

                //已经进入主页面之后再登录，初始化HX
                if (IMManager.getInstance() != null) {
                    LogUtils.i("MapActivity 已经！");
                    IMManager.getInstance().initHX();
                } else {
                    LogUtils.i("MapActivity 未初始化");
                }
            }

            @Override
            public void onProgress(int i, String s) {
                LogUtils.d("登录中>>" + s + ">" + i);
            }

            @Override
            public void onError(int i, String s) {
                LogUtils.w("登录失败>>" + s + ">" + i);
            }
        });
    }

    private boolean deleteDB() {
        File databasesDir = new File(getFilesDir().getParentFile().getPath()
                + "/databases/");// databases目录路径

        // ------------------v1.0.13及以下版本更新出现的打开数据库错误的BUG----------------
        if (databasesDir.exists()) {
            File[] dbFiles = databasesDir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    // 数据库名称为：zld_20140802.db , zld_20140802.db-journal
                    return filename.contains("zld_201");
                }
            });
            if (dbFiles != null && dbFiles.length > 0) {
                for (File file : dbFiles) {
                    if (file.delete())
                        LogUtils.i("delete DB: --->> " + file.getName());
                }
            }
        }
        return true;
    }

    public void openMap() {
        Intent intent = new Intent(TCBApp.getAppContext(), MapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    /**
     * 显示引导界面
     */
    private void showGuideInterface() {
        isGuideInterfaceShowing = true;

        GuidePagerAdapter adapter = new GuidePagerAdapter(getSupportFragmentManager());

        // 初始化页面指示器
        LinearLayout llIndicators = (LinearLayout) this
                .findViewById(R.id.ll_splash_indicators);
        llIndicators.setVisibility(View.VISIBLE);
        final ImageView[] ivIndicators = new ImageView[adapter.getCount()];
        addIndicator(ivIndicators, llIndicators);

        // 初始化ViewPager
        ViewPager viewPager = (ViewPager) this
                .findViewById(R.id.vp_splash);
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                // 遍历数组让当前选中图片下的小圆点设置颜色
                for (int i = 0; i < ivIndicators.length; i++) {
                    ivIndicators[arg0]
                            .setBackgroundResource(R.drawable.ic_page_indicator_focused_green);
                    if (arg0 != i) {
                        ivIndicators[i]
                                .setBackgroundResource(R.drawable.ic_page_indicator_unfocused);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    private void addIndicator(ImageView[] imageViews, LinearLayout llIndicators) {

        /**
         * 有几张图片 下面就显示几个小圆点
         */
        for (int i = 0; i < imageViews.length; i++) {
            LinearLayout.LayoutParams margin = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            // 设置每个小圆点距离左边的间距
            margin.setMargins(15, 0, 15, 0);
            ImageView imageView = new ImageView(this);
            // 设置每个小圆点的宽高
//            imageView.setLayoutParams(new LinearLayout.LayoutParams(25, 25));
            imageViews[i] = imageView;
            if (i == 0) {
                // 默认选中第一张图片
                imageViews[i]
                        .setBackgroundResource(R.drawable.ic_page_indicator_focused_green);
            } else {
                // 其他图片都设置未选中状态
                imageViews[i]
                        .setBackgroundResource(R.drawable.ic_page_indicator_unfocused);
            }
            llIndicators.addView(imageViews[i], margin);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //TODO 此接口用于Activity和Fragment间数据交互
    }

    private class GuidePagerAdapter extends FragmentPagerAdapter {

        public GuidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 4;
        }

        /**
         * Return the Fragment associated with a specified position.
         *
         * @param position
         */
        @Override
        public Fragment getItem(int position) {
            return SplashFragment.newInstance(position);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkIfAlwaysFinishActivities()) {
            // 用户设置了“不保留活动”
            showAlwaysFinishActivitiesDialog(true);
            return;
        } else {
            showAlwaysFinishActivitiesDialog(false);
        }
        if (!isGuideInterfaceShowing) {
            openMap();
        }
    }

    private void showAlwaysFinishActivitiesDialog(boolean show) {
        if (show) {
            if (mAlwaysFinishActivitiesDialog == null) {
                mAlwaysFinishActivitiesDialog = new AlertDialog.Builder(this)
                        .setTitle("注意！")
                        .setMessage("检测到系统设置中的“不保留活动”选项被开启，为保证软件的正常运行，请您关闭此选项！")
                        .setPositiveButton("现在就去设置", new OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Intent developerSettingsIntent = new Intent(
                                        Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                                startActivity(developerSettingsIntent);
                                dialog.dismiss();
                            }
                        }).setCancelable(true)
                        .setOnCancelListener(new OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                                Toast.makeText(SplashActivity.this,
                                        "请修改“不保留活动”设置项！", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }).create();
                mAlwaysFinishActivitiesDialog.setCanceledOnTouchOutside(false);
            }
            mAlwaysFinishActivitiesDialog.show();
        } else {
            if (mAlwaysFinishActivitiesDialog != null
                    && mAlwaysFinishActivitiesDialog.isShowing()) {
                mAlwaysFinishActivitiesDialog.dismiss();
            }
        }
    }

    /**
     * 检查用户是否设置了“设置”->“开发者选项”->“不保留活动”选项
     *
     * @return
     */
    private boolean checkIfAlwaysFinishActivities() {
        String alwaysFinishActivities = Global.ALWAYS_FINISH_ACTIVITIES;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            alwaysFinishActivities = Settings.System.ALWAYS_FINISH_ACTIVITIES;
        }
        ContentResolver cv = this.getContentResolver();
        String ifAlwaysFinishActivities = android.provider.Settings.System
                .getString(cv, alwaysFinishActivities);
        return "1".equals(ifAlwaysFinishActivities);
    }
}