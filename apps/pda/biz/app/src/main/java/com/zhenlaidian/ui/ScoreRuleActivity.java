package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zhenlaidian.R;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

/**
 * 调用webview展示经常变化的规则;
 */
public class ScoreRuleActivity extends BaseActivity {

    public WebView webView;
    public String url;
    public int type;

    @SuppressWarnings("deprecation")
    @SuppressLint({"SetJavaScriptEnabled", "SdCardPath"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = this;
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.score_rule_activity);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        type = getIntent().getIntExtra("type", 0);
        switch (type) {
            case 0:
                url = getString(R.string.rule);
                break;
            case 1:
                actionBar.setTitle("通知详情");
                // 最好改为服务器返回链接!!!
                url = "http://mp.weixin.qq.com/s?__biz=MzA4MTAxMzA2Mg==&mid=208349486&idx=1&sn=a59b4c5250cc4ca34941750d1bffc925#rd";
                break;
            case 2:
                actionBar.setTitle("积分用途说明");
                url = SharedPreferencesUtils.getIntance(this).getScoreUrl();
                break;
            case 3:
                actionBar.setTitle("停车券用途说明");
                url = SharedPreferencesUtils.getIntance(this).getTicketUrl();
                break;
            default:
                url = getString(R.string.rule);
                break;
        }

        webView = (WebView) findViewById(R.id.web_view);
        WebSettings setting = webView.getSettings();
        setting.setPluginState(PluginState.ON);
        setting.setJavaScriptEnabled(true);
        webView.setScrollBarStyle(0);
        webView.setWebViewClient(new WebViewClient() {

            @SuppressWarnings("unused")
            public void onProgressChanged(WebView view, int newProgress) {
                // activity的进度是0 to 10000 (both inclusive),所以要*100
                activity.setProgress(newProgress * 100);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                MyLog.w("ScoreRuleActivity", "点击到了网页中的url" + url);
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                ScoreRuleActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
