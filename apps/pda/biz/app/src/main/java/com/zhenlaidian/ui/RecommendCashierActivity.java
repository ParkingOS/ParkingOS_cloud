package com.zhenlaidian.ui;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuCompat;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.zxing.lswss.QRCodeEncoder;
import com.zhenlaidian.R;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * 推荐收费员界面;
 */
public class RecommendCashierActivity extends BaseActivity {

    private TextView tv_warn;
    private TextView tv_msg;
    private Button bt_share;
    private ImageView iv_recommend_cashier;
    private String titleUrl;
    private boolean isShareSDKDialogShowing;// 当前是否正在显示ShareSDK对话框
    private String msg = "让被推荐收费员扫一扫去注册";
    private Bitmap generateQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.recommend_cashier_activity);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        getLoadUrl();
        initView();
    }

    public void initView() {
        tv_warn = (TextView) findViewById(R.id.tv_recommend_cashier_warn);
        tv_msg = (TextView) findViewById(R.id.tv_recommend_cashier_msg);
        bt_share = (Button) findViewById(R.id.bt_recommend_cashier_share);
        iv_recommend_cashier = (ImageView) findViewById(R.id.iv_recommend_cashier);

        SpannableStringBuilder style = new SpannableStringBuilder(msg);
        style.setSpan(new ForegroundColorSpan(Color.RED), 4, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_msg.setText(style);
        bt_share.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                showShare();
            }
        });
    }

    @SuppressWarnings("deprecation")
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recommend_actionbar, menu);
        MenuCompat.setShowAsAction(menu.findItem(R.id.recommend), MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    private void showShare() {
        if (isShareSDKDialogShowing) {
            return;
        }
        if (titleUrl == null) {
            Toast.makeText(this, "网络不好，请重新进入此页面", 0).show();
            return;
        }
        isShareSDKDialogShowing = true;
        File image = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            String path = externalStorageDirectory.getAbsolutePath() + "/TinCheBao/pic/";
            File directory = new File(path);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            image = new File(new File(path), "app_icon.png");
            if (!(image.exists() && image.length() > 0)) {
                copyAppIconToLocal(image);
            }
        }
        ShareSDK.initSDK(this, "35f1018c0ef0");
        OnekeyShare oks = new OnekeyShare();
        // 关闭sso授权
        oks.disableSSOWhenAuthorize();
        // String titleUrl
        // ="http://d.tingchebao.com/download/parkuser/tingchebao_biz.apk";
        // 四个收费员，三个用停车宝。赶紧来下载,注册送10元，每笔赚两元!下载地址？pid=10700
        String text = "四个收费员，三个用停车宝，注册送10元，每笔赚两元，赶紧来下载！";
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(getString(R.string.share));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(titleUrl);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(text);
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        if (image != null) {
            oks.setImagePath(image.getAbsolutePath());
        }
        oks.setImageUrl(titleUrl);
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(titleUrl);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite("停车宝");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://www.tingchebao.com");
        // 设置编辑界面为窗口模式，必须设置，否则报错
        oks.setDialogMode();

        // 启动分享GUI
        oks.show(this);
    }

    public void onResume() {
        super.onResume();
        isShareSDKDialogShowing = false;
    }

    private void copyAppIconToLocal(File file) {
        InputStream istream = null;
        OutputStream ostream = null;
        try {
            AssetManager am = this.getAssets();
            istream = am.open("app_icon.png");
            file.createNewFile();
            ostream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = istream.read(buffer)) > 0) {
                ostream.write(buffer, 0, length);
            }
            istream.close();
            ostream.close();
            MyLog.w("RecommendActivity", "copy appicon success: --->>" + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (istream != null)
                    istream.close();
                if (ostream != null)
                    ostream.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    // http://192.168.199.239/zld/collectorrequest.do?action=regcolmsg&token=收费员推荐收费员
    public void getLoadUrl() {
        String path = baseurl;
        String url = path + "collectorrequest.do?action=regcolmsg&token=" + token;
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", 0).show();
            return;
        }
        MyLog.w("RecommendCashierActivity", "获取下载连接的 URL--->" + url);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && object != null) {
                    MyLog.i("RecommendCashierActivity", "获取下载链接的结果是：--->" + object);
                    titleUrl = object;
                    createQrcode(object);
                } else {
                    Toast.makeText(getApplicationContext(), "推荐码获取失败--重新进入此界面！", 0).show();
                }
            }
        });
    }

    public void createQrcode(String info) {
        if (generateQRCode == null) {
            QRCodeEncoder d = new QRCodeEncoder();
            generateQRCode = d.encode2BitMap(info, 400, 400);
            iv_recommend_cashier.setImageBitmap(generateQRCode);
        } else {
            iv_recommend_cashier.setImageBitmap(generateQRCode);
        }
    }

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                RecommendCashierActivity.this.finish();
                return true;
            case R.id.recommend:
                Intent intent = new Intent(RecommendCashierActivity.this, RecommendRecordActivity.class);
                intent.putExtra("type", "1");
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
