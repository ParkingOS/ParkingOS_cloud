package com.tq.zld.view.holder;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response.Listener;
import com.baidu.android.bba.common.util.Util;
import com.easemob.chat.EMChatManager;
import com.google.gson.reflect.TypeToken;
import com.igexin.sdk.PushManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.BuildConfig;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Plate;
import com.tq.zld.im.adapter.EMCallBackAdapter;
import com.tq.zld.im.lib.HXSDKHelper;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.IMUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.ToastUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.LoginActivity;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.SplashActivity;
import com.tq.zld.view.im.FriendActivity;
import com.tq.zld.view.manager.IMManager;
import com.tq.zld.view.map.MapActivity;
import com.tq.zld.widget.CircleImageView;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.fragment.FeedbackFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MenuHolder implements OnClickListener {

    private static MenuHolder instance;// 单例
    private MapActivity mActivity;

    private View mAccountView;
    private View mOrderView;
    private View mMessageView;
    private View mFriendView;
    private View mRecommendView;
    private View mFeedbackView;
    private View mSettingView;
    private TextView mPlateView;
    private TextView mMobileView;
    private ImageView mOrderNewView;
    private ImageView mMessageNewView;
    private ImageView mFriendNewView;
    private ImageView mRecommendNewView;
    private ImageView mFeedbackNewView;
    private ImageView mSettingNewView;
    private CircleImageView mPhotoView;
    private Button mServiceTelView;
    private final DisplayImageOptions options;

    private String url = "";
    private String lastUrl = "";
    private int reloginCount = 0;
    private boolean logining = false;

    // private View checkedView;// 当前选中的菜单条目

    private MenuHolder() {
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.img_menu_photo)
                .showImageOnFail(R.drawable.img_menu_photo)
                .showImageOnLoading(R.drawable.img_menu_photo)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        getMenu();
    }

    public static MenuHolder getInstance() {

        if (instance == null) {
            synchronized (MenuHolder.class) {
                if (instance == null) {
                    instance = new MenuHolder();
                }
            }
        }
        return instance;
    }

    public View getMenu() {
        View menuView = View.inflate(TCBApp.getAppContext(), R.layout.menu,
                null);
        menuView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 无需处理，屏蔽触摸事件，防止侧滑菜单被划出时还能操作地图
                return true;
            }
        });
        // 我的账户
        mAccountView = menuView.findViewById(R.id.ll_menu_account);
        mAccountView.setOnClickListener(this);
        mPhotoView = (CircleImageView) menuView.findViewById(R.id.civ_menu_photo);
        mPlateView = (TextView) menuView.findViewById(R.id.tv_menu_plate);
        mMobileView = (TextView) menuView.findViewById(R.id.tv_menu_mobile);

        // 我的订单
        mOrderView = menuView.findViewById(R.id.rl_menu_order);
        mOrderView.setOnClickListener(this);
        mOrderNewView = (ImageView) menuView
                .findViewById(R.id.iv_menu_order_new);

        // 我的消息
        mMessageView = menuView.findViewById(R.id.rl_menu_message);
        mMessageView.setOnClickListener(this);
        mMessageNewView = (ImageView) menuView
                .findViewById(R.id.iv_menu_message_new);

        //我的车友
        mFriendView = menuView.findViewById(R.id.rl_menu_friend);
        mFriendView.setOnClickListener(this);
        mFriendNewView = (ImageView) menuView.findViewById(R.id.iv_menu_friend_new);

        if (!BuildConfig.IM_DEBUG){
            mFriendView.setVisibility(View.GONE);
        }

        // 我要推荐
        mRecommendView = menuView.findViewById(R.id.rl_menu_recommend);
        mRecommendView.setOnClickListener(this);
        mRecommendNewView = (ImageView) menuView
                .findViewById(R.id.iv_menu_recommend_new);

        // 我要反馈
        mFeedbackView = menuView.findViewById(R.id.rl_menu_feedback);
        mFeedbackView.setOnClickListener(this);
        mFeedbackNewView = (ImageView) menuView
                .findViewById(R.id.iv_menu_feedback_new);

        // 设置
        mSettingView = menuView.findViewById(R.id.rl_menu_setting);
        mSettingView.setOnClickListener(this);
        mSettingNewView = (ImageView) menuView
                .findViewById(R.id.iv_menu_setting_new);

        // 联系客服
        mServiceTelView = (Button) menuView
                .findViewById(R.id.tv_menu_servicetel);
        mServiceTelView.setOnClickListener(this);

        refreshAccountInfo();
        return menuView;
    }
    public void setImageUrl(String url) {
        this.url = url;
    }

//    public boolean isImageUrlChange() {
//        LogUtils.i(TCBApp.getAppContext().readString(R.string.sp_im_image_url,""));
//        LogUtils.i(String.format("url = %s,last = %s", this.url, this.lastUrl));
//        if (TextUtils.isEmpty(TCBApp.mMobile)) {
//            return false;
//        }
//
//        if (TextUtils.isEmpty(this.url)) {
//            LogUtils.i("没有头像");
//            this.url = IMUtils.getHead();
//            return true;
//        }
//        return !lastUrl.equals(url);
//    }

    /**
     * 更新头像
     */
    public void refreshPhotoView(String url){
        LogUtils.i("刷新头像：" + url);
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().displayImage(url, mPhotoView, options);
        } else {
            TCBApp.getAppContext().initImageLoader();
        }
//        this.lastUrl = this.url;
    }

    private void getAccountInfo() {
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            return;
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("mobile", TCBApp.mMobile);
        params.put("action", "getcarnumbs");
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "carinter.do", params);
        Listener<ArrayList<Plate>> listener = new Listener<ArrayList<Plate>>() {

            @Override
            public void onResponse(ArrayList<Plate> plates) {

                int state = Plate.STATE_CERTIFY;
                Set<String> plateSet = new HashSet<>();
                String key = TCBApp.getAppContext().getString(R.string.sp_plate_all);
                if (plates != null && plates.size() > 0) {

                    for (Plate plate : plates) {
                        plateSet.add(plate.car_number);
                        if (1 == plate.is_default) {
                            state = plate.is_auth;
                            break;
                        }
                    }
                }
                refreshAccountInfoView2(state, TCBApp.mMobile);
                String key1 = TCBApp.getAppContext().getString(R.string.sp_plate_state);
                TCBApp.getAppContext().getAccountPrefs().edit().putInt(key1, state).putStringSet(key, plateSet).apply();
            }
        };
        TCBApp.getAppContext().addToRequestQueue(
                new GsonRequest<>(url, new TypeToken<ArrayList<Plate>>() {
                }, listener, null));
    }

    /*
     * 设置Menu所属Activity：目前仅支持设置为MapActivity
     */
    public void setActivity(MapActivity activity) {
        this.mActivity = activity;
    }

    /**
     * 设置账户信息
     */
    public void refreshAccountInfo() {
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            mPlateView.setText("登录/注册");
            mPlateView.setBackgroundResource(0);
            mPlateView.setTextColor(Color.BLACK);
            mMobileView.setText("--");
            refreshPhotoView("");
            return;
        }

//        String plate = TCBApp.getAppContext().readString(R.string.sp_plate, "");
        String key = TCBApp.getAppContext().getString(R.string.sp_plate_state);
        int plateState = TCBApp.getAppContext().getAccountPrefs().getInt(key, -100);
        String platesKey = TCBApp.getAppContext().getString(R.string.sp_plate_all);
        Set<String> plates = TCBApp.getAppContext().getAccountPrefs().getStringSet(platesKey, null);
        if (plates == null) {
            // 仅第一次从服务器取状态，以后皆在AccountFragment更新本地数据
            getAccountInfo();
            plateState = Plate.STATE_CERTIFY;
        }
        refreshAccountInfoView2(plateState, TCBApp.mMobile);

        String imgurl = this.url;
        if (TextUtils.isEmpty(this.url)) {
            imgurl = IMUtils.getHead();
        }
        refreshPhotoView(imgurl);

        // TODO 设置头像信息
    }

    private void refreshAccountInfoView2(int plateState, String mobile) {
        switch (plateState) {
            case Plate.STATE_CERTIFY:
                //未认证
                mPlateView.setText("未认证");
                mPlateView.setTextColor(TCBApp.getAppContext().getResources().getColor(R.color.text_red));
                mPlateView.setBackgroundResource(R.drawable.shape_account_plate_certify);
                break;
            case Plate.STATE_CERTIFIED:
                //已认证
                mPlateView.setText("已认证");
                mPlateView.setTextColor(TCBApp.getAppContext().getResources().getColor(R.color.text_green));
                mPlateView.setBackgroundResource(R.drawable.shape_account_plate_certified);
                break;
            case Plate.STATE_CERTIFYING:
                //认证中
                mPlateView.setText("审核中(1-3天)");
                mPlateView.setTextColor(TCBApp.getAppContext().getResources().getColor(R.color.text_orange));
                mPlateView.setBackgroundResource(R.drawable.shape_account_plate_certifying);
                break;
            case Plate.STATE_CERTIFY_FAILED:
                // 认证失败
                mPlateView.setText("认证未通过");
                mPlateView.setTextColor(TCBApp.getAppContext().getResources().getColor(R.color.text_red));
                mPlateView.setBackgroundResource(R.drawable.shape_account_plate_certify);
                break;
            case Plate.STATE_CERTIFY_BLOCKED:
                //车牌无效
                mPlateView.setText("车牌无效");
                mPlateView.setTextColor(TCBApp.getAppContext().getResources().getColor(R.color.text_red));
                mPlateView.setBackgroundResource(R.drawable.shape_account_plate_certify);
                break;
            default:
                break;
        }
        mMobileView.setText(mobile);
    }

    private void refreshAccountInfoView(String plate, String mobile) {
        mPlateView.setText(plate);
        mMobileView.setText(mobile);
    }

    @Override
    public void onClick(View v) {

        mActivity.closeDrawer();

        // if (v == checkedView) {
        // return;
        // }

        // 清除菜单条目的选中状态
        clearSelection();

        refreshMenu(v.getId(), false);

        if (v == mAccountView) {
            onAccountViewClicked();
        } else if (v == mOrderView) {
            onOrderViewClicked();
        } else if (v == mMessageView) {
            onMessageViewClicked();
        } else if(v == mFriendView){
            onFriendViewClicked();
        } else if (v == mRecommendView) {
            onRecommendViewClicked();
        } else if (v == mFeedbackView) {
            onFeedbackViewClicked();
        } else if (v == mSettingView) {
            onSettingViewClicked();
        } else if (v == mServiceTelView) {
            onServiceTelViewClicked();
        }
    }

    private void onServiceTelViewClicked() {
        Uri uri = Uri.parse("tel:"
                + TCBApp.getAppContext().getString(R.string.serviceTel));
        mActivity.startActivity(new Intent(Intent.ACTION_DIAL, uri));
    }

    private void clearSelection() {
        mOrderView.setBackgroundResource(0);
        mMessageView.setBackgroundResource(0);
        mRecommendView.setBackgroundResource(0);
        mFeedbackView.setBackgroundResource(0);
        mSettingView.setBackgroundResource(0);
        // this.checkedView = null;
    }

    private void onSettingViewClicked() {
        // mSettingView.setBackgroundColor(Color.parseColor(TCBApp.getAppContext()
        // .getString(R.color.bg_gray)));
        // this.checkedView = mSettingView;
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            // 用户未登录
            mActivity.startActivity(new Intent(TCBApp.getAppContext(),
                    LoginActivity.class));
            return;
        }
        startMainActivity(MainActivity.FRAGMENT_SETTING);
    }

    private void startMainActivity(int fragmentID) {
        Intent intent = new Intent(TCBApp.getAppContext(), MainActivity.class);
        intent.putExtra(MainActivity.ARG_FRAGMENT, fragmentID);
        mActivity.startActivity(intent);
    }

    private void onFeedbackViewClicked() {
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            // 用户未登录
            mActivity.startActivity(new Intent(TCBApp.getAppContext(),
                    LoginActivity.class));
            return;
        }

        Intent intent = new Intent(TCBApp.getAppContext(), MainActivity.class);
        intent.putExtra(MainActivity.ARG_FRAGMENT,
                MainActivity.FRAGMENT_FEEDBACK);

        // 设置友盟ConversationID
        FeedbackAgent agent = new FeedbackAgent(mActivity);
        Bundle args = new Bundle();
        args.putString(FeedbackFragment.BUNDLE_KEY_CONVERSATION_ID, agent
                .getDefaultConversation().getId());
        intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);

        mActivity.startActivity(intent);
    }

    private void onRecommendViewClicked() {
        // mRecommendView.setBackgroundColor(Color.parseColor(TCBApp
        // .getAppContext().getString(R.color.bg_gray)));
        // this.checkedView = mRecommendView;
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            // 用户未登录
            mActivity.startActivity(new Intent(TCBApp.getAppContext(),
                    LoginActivity.class));
            return;
        }
        startMainActivity(MainActivity.FRAGMENT_SHARE);
    }

    private void onMessageViewClicked() {
        // mMessageView.setBackgroundColor(Color.parseColor(TCBApp.getAppContext()
        // .getString(R.color.bg_gray)));
        // this.checkedView = mMessageView;
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            // 用户未登录
            mActivity.startActivity(new Intent(TCBApp.getAppContext(),
                    LoginActivity.class));
            return;
        }
        startMainActivity(MainActivity.FRAGMENT_MESSAGE_CENTER);
    }

    private void onFriendViewClicked(){
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            // 用户未登录
            mActivity.startActivity(new Intent(TCBApp.getAppContext(),
                    LoginActivity.class));
            return;
        }

        LogUtils.i(String.format("获取缓存环信账号 >> username=%s, password=%s", IMUtils.getUsername(), IMUtils.getPassword()));

        if (!HXSDKHelper.getInstance().isLogined()) {
//            reloginCount++;
//            if (reloginCount > 1) {
//                startGeTuiPush();
//                reloginCount = 0;
//            } else {

//            }
            if (!logining){
                ToastUtils.show(mActivity, "正在获取好友，请稍后...");
                final String username = IMUtils.getUsername();
                final String password = IMUtils.getPassword();
                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
                    logining = true;
                    LogUtils.i(String.format("准备登录>>%s:%s",username,password));
                    EMChatManager.getInstance().login(username, password, new EMCallBackAdapter() {
                        @Override
                        public void onSuccess() {
                            LogUtils.i("登录成功>>");
                            logining = false;
                            //缓存用户名密码
                            TCBApp.hxsdkHelper.setHXId(username);
                            TCBApp.hxsdkHelper.setPassword(password);
                            //缓存环信
                            IMUtils.saveHXAccount(username, password);

                            //加载数据
                            EMChatManager.getInstance().loadAllConversations();

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
            }
            return;
        }

//        startMainActivity(MainActivity.FRAGMENT_FRIEND);
        mActivity.startActivity(new Intent(mActivity, FriendActivity.class));
    }

    private void onOrderViewClicked() {
        // mOrderView.setBackgroundColor(Color.parseColor(TCBApp.getAppContext()
        // .getString(R.color.bg_gray)));
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            // 用户未登录
            mActivity.startActivity(new Intent(TCBApp.getAppContext(),
                    LoginActivity.class));
            return;
        }
        startMainActivity(MainActivity.FRAGMENT_HISTORY_ORDER);
    }

    private void onAccountViewClicked() {
        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            // 用户未登录
            mActivity.startActivity(new Intent(TCBApp.getAppContext(),
                    LoginActivity.class));
            return;
        }
        Intent intent = new Intent(TCBApp.getAppContext(), MainActivity.class);
        intent.putExtra(MainActivity.ARG_FRAGMENT,
                MainActivity.FRAGMENT_ACCOUNT);
        mActivity.startActivity(intent);
    }

    /**
     * 更新菜单条目：是否有新消息
     *
     * @param menuItemId 待更新的菜单ID
     * @param hasNew     是否有新消息
     */
    public void refreshMenu(int menuItemId, boolean hasNew) {
        switch (menuItemId) {
            case R.id.rl_menu_order:
                if (hasNew) {
                    mOrderNewView.setVisibility(View.VISIBLE);
                } else {
                    mOrderNewView.setVisibility(View.GONE);
                }
                break;
            case R.id.rl_menu_message:
                if (hasNew) {
                    mMessageNewView.setVisibility(View.VISIBLE);
                } else {
                    mMessageNewView.setVisibility(View.GONE);
                }
                break;
            case R.id.rl_menu_recommend:
                if (hasNew) {
                    mRecommendNewView.setVisibility(View.VISIBLE);
                } else {
                    mRecommendNewView.setVisibility(View.GONE);
                }
                break;
            case R.id.rl_menu_feedback:
                if (hasNew) {
                    mFeedbackNewView.setVisibility(View.VISIBLE);
                } else {
                    mFeedbackNewView.setVisibility(View.GONE);
                }
                break;
            case R.id.rl_menu_setting:
                if (hasNew) {
                    mSettingNewView.setVisibility(View.VISIBLE);
                } else {
                    mSettingNewView.setVisibility(View.GONE);
                }
                break;
            case R.id.ll_menu_account:
                // TODO 账户信息有更新
                break;
            case R.id.rl_menu_friend:
                if (hasNew) {
                    mFriendNewView.setVisibility(View.VISIBLE);
                } else {
                    mFriendNewView.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void startGeTuiPush() {
        PushManager pushManager = PushManager.getInstance();
        // pushManager.initialize(this);
        if (!pushManager.isPushTurnedOn(mActivity)) {
            pushManager.turnOnPush(mActivity);
        }

        // 更新ClientID
        String cid = pushManager.getClientid(mActivity);
        if (!TextUtils.isEmpty(TCBApp.mMobile) && !TextUtils.isEmpty(cid)) {
            Map<String, String> params = new HashMap<>();
            params.put("cid", cid);
            params.put("mobile", TCBApp.mMobile);
            params.put("action", "addcid");
            new SplashActivity().updateClientId(params);
        }
    }
}