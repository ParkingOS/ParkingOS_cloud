package com.tq.zld.view.map;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.webkit.JavascriptInterface;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.smtt.sdk.QbSdk;
import com.tq.zld.BuildConfig;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.ShareInfo;
import com.tq.zld.pay.Keys;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.MainActivity;
import com.tq.zld.widget.X5WebView;

/**
 * X5内核Web页面
 */
public class X5WebActivity extends BaseActivity {
    public static final String ARG_URL = "url";
    public static final String ARG_TITLE = "title";

    private X5WebView mWebView;
    private RelativeLayout mRootView;
    private String mUrl;
    private String mTitle;

    // 微信分享
    private IWXAPI iWXApi;
    private ShareInfo mShareInfo;
    private View mShareView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 在条件满足时开启硬件加速
        try {
            if (Integer.parseInt(android.os.Build.VERSION.SDK) >= 11) {
                getWindow()
                        .setFlags(
                                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_web_x5);
        mUrl = getIntent().getStringExtra(ARG_URL);
        mTitle = getIntent().getStringExtra(ARG_TITLE);

        initToolbar();

        mRootView = (RelativeLayout) findViewById(R.id.rl_web_rootview);
        //如果没有初始化，X5内核只需要初始化一次，之后就可以使用X5webview
        if (!TCBApp.getAppContext().readBoolean(R.string.sp_flag_x5_init, false)) {
            QbSdk.preInit(this, new QbSdk.PreInitCallback() {
                @Override
                public void onCoreInitFinished() {
                    switch (BuildConfig.BUILD_TYPE) {
                        case "release":
                        case "beta":
                            LogUtils.i(X5WebActivity.class, "--->> onCoreInitFinished");
                            break;
                        default:
                            Toast.makeText(getBaseContext(), "onCoreInitFinished", Toast.LENGTH_SHORT).show();
                            break;

                    }
                    TCBApp.getAppContext().saveBoolean(R.string.sp_flag_x5_init, true);
                    initWebview();
                }

                @Override
                public void onViewInitFinished() {
                    switch (BuildConfig.BUILD_TYPE) {
                        case "release":
                        case "beta":
                            LogUtils.i(X5WebActivity.class, "--->> onViewInitFinished");
                            break;
                        default:
                            Toast.makeText(getBaseContext(), "onViewInitFinished", Toast.LENGTH_SHORT).show();
                            break;

                    }
                }
            });
        } else {
            initWebview();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.destroy();
        }
    }

    private void initToolbar() {
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar_web);
        bar.setTitle(mTitle);
        if (mTitle.equals("停车挑战")) {
            // 游戏界面
            bar.setBackgroundColor(Color.BLACK);
            bar.setTitleTextColor(Color.WHITE);
            bar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_white);
        } else {
            bar.setBackgroundColor(getResources().getColor(R.color.bg_toolbar));
        }
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initWebview() {
        mWebView = new X5WebView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.BELOW, R.id.toolbar_web);
        mRootView.addView(mWebView, 1, params);

        LogUtils.i(getClass(), "WebActivity url: --->> " + mUrl);
        if (mTitle.equals("停车挑战") || mTitle.equals("打灰机")) {
            mWebView.addJavascriptInterface(this, "share");
        }
        mWebView.loadUrl(mUrl);
    }

    @JavascriptInterface
    public void share(String url, String title, String desc, String imageUrl) {
        iWXApi = WXAPIFactory.createWXAPI(X5WebActivity.this, Keys.WXPAY_APPID);
        iWXApi.registerApp(Keys.WXPAY_APPID);
        if (!iWXApi.isWXAppInstalled()) {
            Toast.makeText(X5WebActivity.this, "请先安装微信客户端！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!iWXApi.isWXAppSupportAPI()) {
            Toast.makeText(X5WebActivity.this, "请先升级微信客户端！", Toast.LENGTH_SHORT).show();
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showShareView();
            }
        });
        mShareInfo = new ShareInfo();
        mShareInfo.title = title;
        mShareInfo.imgurl = imageUrl;
        mShareInfo.url = url;
        mShareInfo.description = desc;
        mShareInfo.thumbImage = ImageLoader.getInstance().loadImageSync(imageUrl, DisplayImageOptions.createSimple());
    }

    @JavascriptInterface
    public void buyTicket() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_BUY_TICKET);
        startActivity(intent);
    }

    /**
     * 网页中直接关闭此界面
     */
    @JavascriptInterface
    public void closeWebView() {
        finish();
    }

    private void showShareView() {
        if (mShareView == null) {
            ViewStub shareView = (ViewStub) findViewById(R.id.vs_web_share);
            shareView.setOnInflateListener(new ViewStub.OnInflateListener() {
                @Override
                public void onInflate(ViewStub stub, View inflated) {
                    inflated.findViewById(R.id.tv_web_share_wx_session).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shareToWX(SendMessageToWX.Req.WXSceneSession);
                        }
                    });
                    inflated.findViewById(R.id.tv_web_share_wx_timeline).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shareToWX(SendMessageToWX.Req.WXSceneTimeline);
                        }
                    });
                    mShareView = X5WebActivity.this.findViewById(R.id.vs_web_share_inflated);
                }
            });
            shareView.inflate();
        } else {
            mShareView.setVisibility(View.VISIBLE);
        }
    }

    private void shareToWX(int reqScene) {

        if (mShareInfo == null) {
            return;
        }

        LogUtils.i(getClass(), "share to WeiXin info: --->> " + mShareInfo.toString());

        WXWebpageObject webObject = new WXWebpageObject();
        webObject.webpageUrl = mShareInfo.url;

        WXMediaMessage msg = new WXMediaMessage(webObject);
        msg.mediaObject = webObject;
        msg.title = mShareInfo.title;
        msg.description = mShareInfo.description;
        if (mShareInfo.thumbImage == null) {
            mShareInfo.thumbImage = BitmapFactory.decodeResource(getResources(), R.drawable.ic_share_flygame);
        }
        msg.setThumbImage(mShareInfo.thumbImage);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = reqScene;
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;

        iWXApi.sendReq(req);

        mShareView.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        LogUtils.i(WebActivity.class,
                "当前页面地址：--->> " + mWebView.getUrl());
        // 如果当前分享界面处于展示中，则隐藏
        if (mShareView != null && View.GONE != mShareView.getVisibility()) {
            mShareView.setVisibility(View.GONE);
            return;
        }
        String url = mWebView.getUrl();
        if (!TextUtils.isEmpty(url)) {
            url = url.replace("#", "");
        }
        // 当web页面可以回退，并且当前页面不是跟页面时才可以回退
        if (mWebView.canGoBack() && !mUrl.contains(url)) {
            mWebView.goBack();
        } else {
            if (mTitle.equals("停车挑战")) {
                setResult(RESULT_OK);
            }
            super.onBackPressed();
        }
    }

}
