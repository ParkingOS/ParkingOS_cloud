package com.tq.zld.view.map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.ShareInfo;
import com.tq.zld.pay.Keys;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.NetWorkUtils;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.LoginActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ParkingRedPacketsActivity extends BaseActivity {

    public static final String ARG_PID = "pid";

    private String bid; // tips(bonusid)支付成功返回编号；
    private TextView tv_redpackets_number; // 停车卷张数；
    private TextView tv_redpackets_money; // 停车卷金额；
    private TextView tv_redpackets_big_money; // 停车卷金额放大效果；
    private EditText et_redpackets_blessing; // 分享祝福语；
    private Button bt_redpackets_send_friend; // 发送给朋友按钮；
    private ShareInfo mShareInfo;
    private IWXAPI iWXApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_redpackets);
        initToolbar();
        bid = getIntent().getStringExtra(ARG_PID);
        initView();
        getParkingPacketsInfo();
        iWXApi = WXAPIFactory.createWXAPI(this, Keys.WXPAY_APPID);
        iWXApi.registerApp(Keys.WXPAY_APPID);
        if (!iWXApi.isWXAppInstalled()) {
            Toast.makeText(this, "请先安装微信客户端！", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!iWXApi.isWXAppSupportAPI()) {
            Toast.makeText(this, "请先升级微信客户端！", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.holo_red_dark));
//            getWindow().setNavigationBarColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar_parking_red_packets);
        bar.setTitle("停车劵礼包");
        bar.inflateMenu(R.menu.menu_parking_red_packets);
        bar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_white);
        bar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        bar.setOnMenuItemClickListener(new OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_coupon_help) {
                    startCouponHelpActivity();
                    return true;
                }
                return false;
            }
        });
    }

    public void initView() {
        tv_redpackets_number = (TextView) findViewById(R.id.tv_redpackets_number);
        tv_redpackets_money = (TextView) findViewById(R.id.tv_redpackets_total_money);
        tv_redpackets_big_money = (TextView) findViewById(R.id.tv_redpackets_big_money);
        et_redpackets_blessing = (EditText) findViewById(R.id.et_redpackets_blessing);
        bt_redpackets_send_friend = (Button) findViewById(R.id.bt_redpackets_send_friend);
        bt_redpackets_send_friend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO 点击调用分享到微信好友
                if (TextUtils.isEmpty(TCBApp.mMobile)) {
                    Toast.makeText(ParkingRedPacketsActivity.this, "请先登录！",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(TCBApp.getAppContext(),
                            LoginActivity.class));
                    return;
                }
                if (mShareInfo == null) {
                    Toast.makeText(ParkingRedPacketsActivity.this, "网络错误！",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                getShareContent(SendMessageToWX.Req.WXSceneSession);
            }
        });
    }

    private void getShareContent(final int reqScene) {

        String imgUrl = "";
        if (mShareInfo != null) {
            imgUrl = mShareInfo.imgurl;
        }

        ImageLoader.getInstance().loadImage(
                TCBApp.mServerUrl + imgUrl,
                new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String arg0, View arg1) {
                    }

                    @Override
                    public void onLoadingFailed(String arg0, View arg1,
                                                FailReason arg2) {
                        shareToWX(reqScene);
                    }

                    @Override
                    public void onLoadingComplete(String arg0, View arg1,
                                                  Bitmap arg2) {
                        mShareInfo.thumbImage = arg2;
                        shareToWX(reqScene);
                    }

                    @Override
                    public void onLoadingCancelled(String arg0, View arg1) {
                    }
                });
    }

    private void setView() {
        tv_redpackets_big_money.setText("￥" + mShareInfo.total);
        tv_redpackets_money.setText(mShareInfo.total);
        if (mShareInfo.bnum != null) {
            tv_redpackets_number.setText(mShareInfo.bnum);
        }
        if (mShareInfo.description != null) {
            String desc = mShareInfo.description;
            et_redpackets_blessing.setText(desc);
            et_redpackets_blessing.setSelection(desc.length());
        }
    }

    // 获取停车卷礼包的接口：
    // carowner.do?action=obparms&mobile=15801482463&bid=1
    // result: --->>
    // {"imgurl":"images/bonus/order_bonu.png","title":"二百多家车场通用的停车券，duang~","description":"祝你新年一路发发发.....",
    // "url":"carowner.do?action=getobonus","total":"66.00","bnum":"30"};
    private void getParkingPacketsInfo() {
        String url = TCBApp.mServerUrl + "carowner.do?action=obparms&mobile="
                + TCBApp.mMobile + "&bid=" + bid;
        LogUtils.i(getClass(), "getParkingPacketsInfo url: --->> " + url);
        if (NetWorkUtils.IsHaveInternet(this)) {
            AQuery aQuery = new AQuery(this);
            final ProgressDialog dialog = ProgressDialog.show(this, "",
                    "请稍候...", true, true);
            dialog.setCanceledOnTouchOutside(false);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {
                @Override
                public void callback(String url, String object,
                                     AjaxStatus status) {
                    dialog.dismiss();
                    LogUtils.i(ParkingRedPacketsActivity.class,
                            "ParkingRedPacketsActivity result: --->> " + object);
                    if (!TextUtils.isEmpty(object)) {
                        try {
                            mShareInfo = new Gson().fromJson(object,
                                    ShareInfo.class);
                            if (mShareInfo != null
                                    && !TextUtils.isEmpty(mShareInfo.total)) {
                                setView();
                            }
                        } catch (Exception e) {
                            Toast.makeText(ParkingRedPacketsActivity.this,
                                    "数据异常，请稍后重试...", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ParkingRedPacketsActivity.this,
                                "网络异常，请稍后再试...", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        dismissProgressDialog();
        super.onResume();
    }

    private void shareToWX(int reqScene) {
        String sharetext = "";
        try {
            sharetext = URLEncoder.encode(et_redpackets_blessing.getText()
                    .toString(), "utf-8");
            sharetext = URLEncoder.encode(sharetext, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        WXWebpageObject webObject = new WXWebpageObject();
        webObject.webpageUrl = TCBApp.mServerUrl + mShareInfo.url + "&id=" + bid
                + "&words=" + sharetext;

        WXMediaMessage msg = new WXMediaMessage(webObject);
        msg.mediaObject = webObject;
        msg.title = mShareInfo.title;
        if (et_redpackets_blessing.getText() != null) {
            msg.description = et_redpackets_blessing.getText().toString();
        } else {
            msg.description = "";
        }
        if (mShareInfo.thumbImage == null) {
            mShareInfo.thumbImage = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher);
        }
        msg.setThumbImage(mShareInfo.thumbImage);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = reqScene;
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;

        iWXApi.sendReq(req);

        // TODO 隐藏分享界面
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, "帮助")
                .setIcon(R.drawable.ic_action_help_white)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    private void startCouponHelpActivity() {
        Intent intent = new Intent(getApplicationContext(), WebActivity.class);
        intent.putExtra(WebActivity.ARG_TITLE, "停车券帮助");
//        intent.putExtra(WebActivity.ARG_URL, TCBApp.mServerUrl + "ticket.jsp");
        intent.putExtra(WebActivity.ARG_URL, getString(R.string.url_coupon_help));
        startActivity(intent);
    }
}
