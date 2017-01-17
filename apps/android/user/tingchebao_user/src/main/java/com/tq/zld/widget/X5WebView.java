package com.tq.zld.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.utils.TbsLog;
import com.tq.zld.BuildConfig;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.util.DensityUtils;

import java.util.ArrayList;
import java.util.List;

public class X5WebView extends WebView {

    private ProgressBar progressbar;
    private List<String> whiteList = new ArrayList<>();

    public X5WebView(Context context){
        super(context);
        initProgressBar();
        initSetting();
    }

    public X5WebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initProgressBar();
        initSetting();
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean ret = super.drawChild(canvas, child, drawingTime);

        switch (BuildConfig.BUILD_TYPE) {
            case "debug":
            case "alpha":
                drawX5Type(canvas);
        }

        return ret;
    }

    /**
     * 显示X5内核是否开启
     * @param canvas
     */
    private void drawX5Type(Canvas canvas){
        canvas.save();
        Paint paint = new Paint();
        paint.setColor(0x7fff0000);
        paint.setTextSize(24.f);
        paint.setAntiAlias(true);

        if (getX5WebViewExtension() != null) {
            canvas.drawText("X5 core:" + WebView.getTbsCoreVersion(getContext()), 10, 50, paint);
        } else {
            canvas.drawText("Sys Core", 10, 50, paint);
        }
        canvas.restore();
    }

    private void initProgressBar(){
        progressbar = new ProgressBar(getContext(), null,
                android.R.attr.progressBarStyleHorizontal);
        progressbar.setMax(100);
        progressbar.setProgressDrawable(new ColorDrawable(getResources()
                .getColor(R.color.primary_green)));
        progressbar.setBackgroundColor(Color.TRANSPARENT);
        progressbar.setLayoutParams(new RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                DensityUtils.dip2px(getContext(), 3)));
        addView(progressbar);
    }

    /**
     * 各种设置
     */
    private void initSetting(){
        //添加白名单，
        whiteList.add(TCBApp.mServerUrl);
        whiteList.add("renrenche.com");
        whiteList.add("app.qq.com");
        whiteList.add("weixin.qq.com");

        this.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                if(BuildConfig.BUILD_TYPE.equals("release")) {
                    if (isWhite(url)) {
                        webView.loadUrl(url);
                        return true;
                    } else {
                        Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        TCBApp.getAppContext().startActivity(in);
                        return true;
                    }
                }

                return super.shouldOverrideUrlLoading(webView,url);
            }

        });

//        this.setWebChromeClient(new WebChromeClient());
        this.setWebChromeClient(new ProgressWebChromeClient(this.progressbar));

        // 各种设置
        if (this.getX5WebViewExtension() != null) {
            this.getX5WebViewExtension().invokeMiscMethod("someExtensionMethod", new Bundle());
        } else {
            TbsLog.e("robins", "CoreVersion");
        }

        WebSettings webSetting = this.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        webSetting.setLoadWithOverviewMode(true);
        webSetting.setAppCacheEnabled(true);
        webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(this.getContext().getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(this.getContext().getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(this.getContext().getDir("geolocation", 0).getPath());
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
    }

    public void addWhiteList(String url){
        if (!TextUtils.isEmpty(url)){
            whiteList.add(url);
        }
    }

    private boolean isWhite(String targetUrl){
        for (String url : whiteList){
            if (targetUrl.contains(url)){
                return true;
            }
        }

        return false;
    }

    public static class ProgressWebChromeClient extends WebChromeClient {

        private ProgressBar mProgressBar;

        public ProgressWebChromeClient(ProgressBar progressBar) {
            this.mProgressBar = progressBar;
        }

        @Override
        public final void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                mProgressBar.setVisibility(GONE);
            } else {
                if (mProgressBar.getVisibility() == GONE)
                    mProgressBar.setVisibility(VISIBLE);
                mProgressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public boolean onJsAlert(WebView webView, String url, String message, JsResult jsResult) {
            Toast.makeText(TCBApp.getAppContext(), message, Toast.LENGTH_SHORT).show();
            jsResult.confirm();
            return true;
        }
    }

}