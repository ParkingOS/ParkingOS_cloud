package com.tq.zld.view.map;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.TCBApp;
import com.tq.zld.R;
import com.tq.zld.bean.MonthlyPay;
import com.tq.zld.bean.ParkInfo;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.LoginActivity;
import com.tq.zld.widget.StickyScrollView;
import com.tq.zld.widget.StickyScrollView.OnScrollListener;
import com.tq.zld.wxapi.WXPayEntryActivity;

public class MonthlyPayDetailActivity extends BaseActivity implements
        OnClickListener, OnScrollListener {

    private MonthlyPay data;

    private View llBuy;// 屏幕中间“购买”布局
    private View llBuyTop;// 屏幕顶部“购买”布局
    private StickyScrollView ssvRootView;// 根布局
    private ImageView ivPhoto;// 顶部照片
    private ImageView ivType;// 照片左上角标识包月产品类型
    private ImageView ivPhone;// 停车场图片
    private TextView tvName;// 包月产品名称
    private TextView tvNumber;// 产品剩余数量
    private TextView tvPrice;// 产品现价
    private TextView tvPriceTop;// 产品现价（顶部）
    private TextView tvPrice0;// 产品原价
    private TextView tvPrice0Top;// 产品原价（顶部）
    private TextView tvBuy;// “立即购买”按钮
    private TextView tvBuyTop;// “立即购买”按钮（顶部）
    private TextView tvParkName;// 停车场名称
    private TextView tvAddress;// 停车场地址
    // private TextView tvDistance;// 距离
    private TextView tvPraise;// 赞的个数
    private TextView tvDisparage;// 贬的个数
    private TextView tvCommentNum;// 评论数
    private TextView tvResume;// 产品描述
    private TextView tvLimitTime;// 使用时间
    private TextView tvLimitDay;// 有效期
    private TextView tvReserved;// 车位是否固定
    private WebView wvNotice;// 购买须知

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthlypay_detail);
        initActionBar();
        data = getIntent().getParcelableExtra("monthlypay");
        initView();
    }

    @Override
    protected void onResume() {
        if (data != null) {
            getMonthlyPay(data.id);
        }
        super.onResume();
    }

    /**
     * 获取包月产品详情
     * http://192.168.199.209/zld/getpark.do?action=getpdetail&pid=3&mobile=""
     *
     * @param id
     */
    private void getMonthlyPay(String id) {
        String url = TCBApp.mServerUrl + "getpark.do";
        Map<String, String> params = new HashMap<>();
        params.put("action", "getpdetail");
        params.put("pid", id);
        params.put("mobile", TCBApp.mMobile);
        LogUtils.i(getClass(), "getMonthlyPay url: --->> " + url + "\n"
                + params.toString());
        final ProgressDialog dialog = ProgressDialog.show(this, "", "请稍候...",
                false, true);
        dialog.setCanceledOnTouchOutside(false);
        new AQuery(this).ajax(url, params, String.class,
                new AjaxCallback<String>() {
                    @Override
                    public void callback(String url, String object,
                                         AjaxStatus status) {
                        LogUtils.i(getClass(), "getMonthlyPay result: --->> "
                                + object);
                        dialog.dismiss();
                        if (!TextUtils.isEmpty(object)) {
                            try {
                                Gson gson = new Gson();
                                MonthlyPay result = gson.fromJson(object,
                                        MonthlyPay.class);
                                if (result != null && result.parkinfo != null) {
                                    data.parkinfo = new ParkInfo();
                                    data.parkinfo.name = result.parkinfo.name;
                                    data.parkinfo.id = result.parkinfo.id;
                                    data.parkinfo.addr = result.parkinfo.addr;
                                    data.resume = result.resume;
                                    data.limitday = result.limitday;
                                    data.isbuy = result.isbuy;
                                    updateView(data);
                                } else {
                                    Toast.makeText(
                                            MonthlyPayDetailActivity.this,
                                            "数据获取异常！", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(MonthlyPayDetailActivity.this,
                                    "网络异常！", Toast.LENGTH_SHORT).show();
                        }
                        super.callback(url, object, status);
                    }
                });
    }

    private void updateView(MonthlyPay data) {
        // 加载图片
        ArrayList<String> photoUrls = data.photoUrl;
        if (photoUrls != null && photoUrls.size() > 0) {
            ImageLoader.getInstance().displayImage(
                    getString(R.string.url_release) + photoUrls.get(0), ivPhoto);
        }
        // 设置产品名称
        tvName.setText(data.name);

        // 设置价格
        Spanned price = Html.fromHtml("<big><big><font color='#329762'>"
                + data.price + "</font></big></big>元");
        tvPrice.setText(price);
        tvPriceTop.setText(price);// 顶部布局价格

        // 设置原价
        if (TextUtils.isEmpty(data.price0) || "0.00".equals(data.price0)) {
            tvPrice0.setVisibility(View.GONE);
            tvPrice0Top.setVisibility(View.GONE);
        } else {
            String price0 = String.format(
                    getString(R.string.monthlypay_childview_price0),
                    data.price0);
            tvPrice0.setText(price0);
            tvPrice0.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            // 顶部布局原价
            tvPrice0Top.setText(price0);
            tvPrice0Top.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // 设置剩余数量
        tvNumber.setText(String.format(
                getString(R.string.monthlypay_childview_number), data.number));

        // 设置包月卡类型 & 使用时间
        // public String type;产品类型：全天包月（0），夜间包月（1），日间包月（2）
        int typeid = R.drawable.img_monthlypay_full;
        String limitTime = "全天（00:00-24:00）";
        switch (data.type) {
            case "1":
                typeid = R.drawable.img_monthlypay_night;
                limitTime = String.format("夜间（%s）", data.limittime);
                break;
            case "2":
                typeid = R.drawable.img_monthlypay_day;
                limitTime = String.format("日间（%s）", data.limittime);
                break;
        }
        ivType.setBackgroundResource(typeid);
        tvLimitTime.setText(limitTime);

        // 设置可不可以购买
        if ("1".equals(data.isbuy)) {
            tvBuy.setText("已购买");
            tvBuy.setBackgroundResource(R.color.bg_gray);
            tvBuy.setClickable(false);
            // 设置顶部布局“购买”按钮
            tvBuyTop.setText("已购买");
            tvBuyTop.setBackgroundResource(R.color.bg_gray);
            tvBuyTop.setClickable(false);
        } else {
            tvBuy.setText("立即购买");
            tvBuy.setBackgroundResource(R.color.red);
            tvBuy.setClickable(true);
            // 设置顶部布局“购买”按钮
            tvBuyTop.setText("立即购买");
            tvBuyTop.setBackgroundResource(R.color.red);
            tvBuyTop.setClickable(true);
        }

        // 设置产品描述
        tvResume.setText(TextUtils.isEmpty(data.resume) ? "本停车场环境优雅，服务周到，欢迎来此停车！"
                : data.resume);

        // 设置有效期
        String limitDay = TextUtils.isEmpty(data.limitday) ? data.limitday
                : String.valueOf(System.currentTimeMillis() / 1000 + 31536000);
        tvLimitDay.setText(String.format(
                getString(R.string.monthlypay_detail_limitday),
                SimpleDateFormat.getDateInstance().format(
                        new Date(Long.parseLong(limitDay) * 1000))));

        // 设置车位是否固定：0不固定；1固定
        String reserved = "0".equals(data.reserved) ? getString(R.string.monthlypay_detail_unreserved)
                : getString(R.string.monthlypay_detail_reserved);
        tvReserved.setText(reserved);
        ssvRootView.setVisibility(View.VISIBLE);
    }

    private void initView() {
        ssvRootView = (StickyScrollView) findViewById(R.id.ssv_monthlypay_detail_rootview);
        ssvRootView.setOnScrollListener(this);
        ssvRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // 这一步很重要，使得上面的购买布局和下面的购买布局重合
                        onScroll(ssvRootView.getScrollY());
                    }
                });
        ssvRootView.scrollTo(0, 0);
        ivPhoto = (ImageView) findViewById(R.id.iv_monthlypay_detail_photo);
        ivPhoto.setOnClickListener(this);
        ivType = (ImageView) findViewById(R.id.iv_monthlypay_detail_type);
        ivPhone = (ImageView) findViewById(R.id.iv_monthlypay_detail_phone);
        ivPhone.setOnClickListener(this);
        tvName = (TextView) findViewById(R.id.tv_monthlypay_detail_name);
        tvNumber = (TextView) findViewById(R.id.tv_monthlypay_detail_number);

        // 中间“购买”布局
        llBuy = findViewById(R.id.ll_monthlypay_detail_buy);
        tvPrice = (TextView) llBuy
                .findViewById(R.id.tv_monthlypay_detail_price);
        tvPrice0 = (TextView) llBuy
                .findViewById(R.id.tv_monthlypay_detail_price0);
        tvBuy = (TextView) llBuy.findViewById(R.id.tv_monthlypay_detail_buy);
        tvBuy.setOnClickListener(this);
        // 顶部“购买”布局
        llBuyTop = findViewById(R.id.ll_monthlypay_detail_buy_top);
        tvPriceTop = (TextView) llBuyTop
                .findViewById(R.id.tv_monthlypay_detail_price);
        tvPrice0Top = (TextView) llBuyTop
                .findViewById(R.id.tv_monthlypay_detail_price0);
        tvBuyTop = (TextView) llBuyTop
                .findViewById(R.id.tv_monthlypay_detail_buy);
        tvBuyTop.setOnClickListener(this);

        tvAddress = (TextView) findViewById(R.id.tv_monthlypay_detail_address);
        tvAddress.setOnClickListener(this);
        tvParkName = (TextView) findViewById(R.id.tv_monthlypay_detail_parkname);
        tvParkName.setOnClickListener(this);
        // tvDistance = (TextView)
        // findViewById(R.id.tv_monthlypay_detail_distance);
        tvPraise = (TextView) findViewById(R.id.tv_monthlypay_detail_praise);
        tvPraise.setOnClickListener(this);
        tvDisparage = (TextView) findViewById(R.id.tv_monthlypay_detail_disparage);
        tvDisparage.setOnClickListener(this);
        tvCommentNum = (TextView) findViewById(R.id.tv_monthlypay_detail_commentnum);
        tvCommentNum.setOnClickListener(this);
        tvLimitTime = (TextView) findViewById(R.id.tv_monthlypay_detail_limittime);
        tvResume = (TextView) findViewById(R.id.tv_monthlypay_detail_resume);
        tvLimitDay = (TextView) findViewById(R.id.tv_monthlypay_detail_limitday);
        tvReserved = (TextView) findViewById(R.id.tv_monthlypay_detail_reserved);
        wvNotice = (WebView) findViewById(R.id.wv_monthlypay_detail_notice);
        // 设置webview参数
        WebSettings settings = wvNotice.getSettings();
        // settings.setUseWideViewPort(true);
        // settings.setLoadWithOverviewMode(true);
        settings.setDisplayZoomControls(false);
        settings.setBuiltInZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        wvNotice.loadUrl(TCBApp.mServerUrl + "presume.jsp");
    }

    private void initActionBar() {
        ActionBar mActionBar = getActionBar();
        mActionBar.setTitle("月卡详情");
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_monthlypay_detail_photo:
                // TODO 查看更多图片
                // ArrayList<String> photoUrls = data.photoUrl;
                // if (photoUrls != null && photoUrls.size() > 0) {
                // Intent intent = new Intent(getApplicationContext(),
                // ParkPhotoActivity.class);
                // intent.putStringArrayListExtra("photoUrls", photoUrls);
                // startActivity(intent);
                // }
                break;
            case R.id.tv_monthlypay_detail_buy:
                // TODO 进入确认购买界面
                if (TextUtils.isEmpty(TCBApp.mMobile)) {
                    Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    return;
                }
                Intent intent = new Intent(this, MonthlyPayBuyActivity.class);
                intent.putExtra("name", data.name);
                intent.putExtra("parkname", data.parkinfo.name);
                intent.putExtra("type", data.type);
                intent.putExtra(WXPayEntryActivity.ARG_MONTYLYPAY_ID, data.id);
                intent.putExtra("limitday", data.limitday);
                intent.putExtra("price", data.price);
                startActivity(intent);
                break;
            case R.id.tv_monthlypay_detail_praise:
            case R.id.tv_monthlypay_detail_disparage:
            case R.id.tv_monthlypay_detail_commentnum:
                // Intent commentIntent = new Intent(getApplicationContext(),
                // ParkCommentActivity.class);
                // commentIntent.putExtra("parkId", data.parkinfo.id);
                // startActivity(commentIntent);
                // TODO 跳转到评论？？跳毛
                break;
            // 打开停车场详情界面
            case R.id.tv_monthlypay_detail_address:
            case R.id.tv_monthlypay_detail_parkname:
                Intent parkDetailIntent = new Intent(getApplicationContext(),
                        ParkActivity.class);
                parkDetailIntent.putExtra(ParkActivity.ARG_ID, data.parkinfo.id);
                parkDetailIntent
                        .putExtra(ParkActivity.ARG_NAME, data.parkinfo.name);
                startActivity(parkDetailIntent);
                break;
        }
    }

    @Override
    public void onScroll(int scrollY) {
        int mBuyLayout2ParentTop = Math.max(scrollY, llBuy.getTop());
        llBuyTop.layout(0, mBuyLayout2ParentTop, llBuyTop.getWidth(),
                mBuyLayout2ParentTop + llBuyTop.getHeight());
    }

}
