package com.tq.zld.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.igexin.sdk.PushManager;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.view.fragment.LoginFragment;
import com.tq.zld.view.fragment.RegisterFragment;
import com.tq.zld.view.holder.MenuHolder;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseActivity {

    private static final int LOGIN = 1;
    private static final int REGISTER = 2;

    public static Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        setDefaultFragment();
        mHandler = new LoginHandler(this);
    }

    private void setDefaultFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment()).commit();
    }

    private void initToolbar() {
        Toolbar mBar = (Toolbar) findViewById(R.id.widget_toolbar);
        mBar.setBackgroundColor(getResources().getColor(R.color.bg_toolbar));
        setSupportActionBar(mBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mBar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onDestroy() {
        mHandler = null;
        super.onDestroy();
    }

    public static class LoginHandler extends Handler {

        private SoftReference<LoginActivity> mActivity;

        public LoginHandler(LoginActivity activity) {
            this.mActivity = new SoftReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (mActivity.get() == null) {
                return;
            }

            mActivity.get().dismissProgressDialog();
            switch (msg.what) {
                case REGISTER:
                    RegisterFragment fragment = new RegisterFragment();
                    Bundle args = new Bundle();
                    args.putString(RegisterFragment.ARG_MOBILE, msg.obj.toString());
                    fragment.setArguments(args);
                    mActivity.get().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
                    break;
                case 3:
                case LOGIN://主动发短信或者手机验证码登陆都会到这里。
                    TCBApp.getAppContext().saveString(R.string.sp_mobile, msg.obj.toString());
                    TCBApp.mMobile = msg.obj.toString();

                    // 刷新侧滑菜单账户信息
                    MenuHolder.getInstance().refreshAccountInfo();

                    startGeTuiPush();
                    mActivity.get().finish();
                    break;
//                case -9:// 车牌号已被注册
//                    new AlertDialog.Builder(mActivity.get())
//                            .setTitle("注意！")
//                            .setMessage(
//                                    "此车牌已经注册过，您可直接使用注册此车牌的手机号进行登录。如有疑问，请联系停车宝客服：\n"
//                                            + TCBApp.getAppContext().getString(
//                                            R.string.serviceTel))
//                            .setPositiveButton("重新填写", null)
//                            .setNegativeButton("联系客服",
//                                    new DialogInterface.OnClickListener() {
//
//                                        @Override
//                                        public void onClick(DialogInterface dialog,
//                                                            int which) {
//                                            Uri uri = Uri
//                                                    .parse("tel:"
//                                                            + TCBApp.getAppContext()
//                                                            .getString(
//                                                                    R.string.serviceTel));
//                                            mActivity.get().startActivity(
//                                                    new Intent(Intent.ACTION_DIAL,
//                                                            uri));
//                                        }
//                                    }).show();
//                    break;
                default:
                    Toast.makeText(TCBApp.getAppContext(), getInfo(msg.what),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        private String getInfo(int code) {
            String info;
            switch (code) {
                case 0:
                case -1:
                case -6:
                case -7:
                    info = "验证码错误！";
                    break;
                case -2:
                    info = "注册失败！";
                    break;
                case 3:
                    info = "车牌保存成功！";
                    break;
                case 4:
                    info = "推荐码填写错误，您可以在账户中心重新提交！";
                    break;
                case -3:
                    info = "给手机发送验证码失败，请重新操作！";
                    break;
                case -5:
                    info = "手机号码错误，请重新操作！";
                    break;
                case -8:
                    info = "车牌保存失败，请重新操作！";
                    break;
                case -9:
                    info = "车牌号已被注册！";
                    break;
                default:
                    info = "网络不给力，请先设置您的网络！";
                    break;
            }
            return info;
        }

        private void startGeTuiPush() {
            PushManager pushManager = PushManager.getInstance();
            // pushManager.initialize(this);
            if (!pushManager.isPushTurnedOn(mActivity.get())) {
                pushManager.turnOnPush(mActivity.get());
            }

            // 更新ClientID
            String cid = pushManager.getClientid(mActivity.get());
            if (!TextUtils.isEmpty(TCBApp.mMobile) && !TextUtils.isEmpty(cid)) {
                Map<String, String> params = new HashMap<>();
                params.put("cid", cid);
                params.put("mobile", TCBApp.mMobile);
                params.put("action", "addcid");
                new SplashActivity().updateClientId(params);
            }
        }
    }
}
