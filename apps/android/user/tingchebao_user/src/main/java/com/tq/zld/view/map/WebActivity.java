package com.tq.zld.view.map;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
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
import com.tq.zld.view.BaseActivity;
import com.tq.zld.widget.ProgressWebView;

import java.util.regex.Pattern;

public class WebActivity extends BaseActivity {

    public static final String ARG_URL = "url";
    public static final String ARG_TITLE = "title";

    private ProgressWebView mWebView;
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
        // getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_web);
        mUrl = getIntent().getStringExtra(ARG_URL);
        mTitle = getIntent().getStringExtra(ARG_TITLE);
        initToolbar();
        mRootView = (RelativeLayout) findViewById(R.id.rl_web_rootview);
        mWebView = (ProgressWebView) findViewById(R.id.wv_content);
        mWebView.clearCache(true);
        WebSettings settings = mWebView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        // settings.setSupportZoom(true);
        settings.setDisplayZoomControls(false);
        settings.setBuiltInZoomControls(true);
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        // settings.setAppCacheEnabled(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // settings.setAppCacheMaxSize(0);
//        deleteDatabase("webview.db");
//        deleteDatabase("webviewCache.db");
        mWebView.setWebChromeClient(new ProgressWebView.WebChromeClient(mWebView.getProgressbar()) {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                result.confirm();
                return true;
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            // 重写WebView此方法可以控制WebView只打开特定url的网页，其他交由浏览器打开
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(TCBApp.mServerUrl)) {
                    view.loadUrl(url);
                    return true;
                } else {
                    Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(in);
                    return true;
                }
            }
        });
        LogUtils.i(getClass(), "WebActivity url: --->> " + mUrl);
        if (mTitle.equals("停车挑战") || mTitle.equals("打灰机")) {
            mWebView.addJavascriptInterface(this, "share");
        }
        mWebView.loadUrl(mUrl);
    }


    @JavascriptInterface
    public void share(String url, String title, String desc, String imageUrl) {
        iWXApi = WXAPIFactory.createWXAPI(WebActivity.this, Keys.WXPAY_APPID);
        iWXApi.registerApp(Keys.WXPAY_APPID);
        if (!iWXApi.isWXAppInstalled()) {
            Toast.makeText(WebActivity.this, "请先安装微信客户端！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!iWXApi.isWXAppSupportAPI()) {
            Toast.makeText(WebActivity.this, "请先升级微信客户端！", Toast.LENGTH_SHORT).show();
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
                    inflated.findViewById(R.id.tv_web_share_wx_session).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shareToWX(SendMessageToWX.Req.WXSceneSession);
                        }
                    });
                    inflated.findViewById(R.id.tv_web_share_wx_timeline).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shareToWX(SendMessageToWX.Req.WXSceneTimeline);
                        }
                    });
                    mShareView = WebActivity.this.findViewById(R.id.vs_web_share_inflated);
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
        bar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onDestroy() {
        mRootView.removeView(mWebView);
        mWebView.removeAllViews();
        mWebView.destroy();
        super.onDestroy();
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
