package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.ChoseCarNumPageAdapter;
import com.zhenlaidian.adapter.DrawerAdapter;
import com.zhenlaidian.adapter.InputCarNumberGridAdapter;
import com.zhenlaidian.bean.BaseResponse;
import com.zhenlaidian.bean.Card;
import com.zhenlaidian.bean.CardInfo;
import com.zhenlaidian.bean.DrawerItemInfo;
import com.zhenlaidian.engine.DrawerOnItemClick;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.util.CheckUtils;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.Constant;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.ReadCardUtil;
import com.zhenlaidian.util.SharedPreferencesUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xulu on 2016/8/22.
 */
public class OpenCardActivity extends BaseActivity implements View.OnFocusChangeListener, View.OnClickListener {
    ActionBar actionBar;
    private DrawerLayout drawerLayout = null;
    private ListView lv_left_drawer;
    private ActionBarDrawerToggle mDrawerToggle;
    String cardid = "";
    String type = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_opencard_layout);
        initView();
        initActionBar();
        InitImageView();
        initViewPager();
        lv_left_drawer.setAdapter(new DrawerAdapter(DrawerItemInfo.getInstance(), OpenCardActivity.this));
        lv_left_drawer.setOnItemClickListener(new DrawerOnItemClick(this, drawerLayout, this));
        lv_left_drawer.setScrollingCacheEnabled(false);
        if (!TextUtils.isEmpty(type)) {
            if (type.equals("-6")) {
                textactive.setVisibility(View.GONE);
//                lntext_head.setVisibility(View.GONE);
            }
//            else if(type.equals("-5")){
//                submit.setVisibility(View.GONE);
//                lntext.setVisibility(View.GONE);
//            }
        }
        if (!TextUtils.isEmpty(cardid)) {
            //跳转过来的时候携带卡号
//            edtCard.setText(cardid);

            shuka.setVisibility(View.GONE);
            QurrayCard(cardid, false);
        }
        ReadCardUtil.StartReadCard(mHandler);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.MSG_FOUND_UID:
                    String uid = (String) msg.obj;
                    shuka.setVisibility(View.GONE);
                    edtCard.setKeyListener(null);
//                    Intent intent = new Intent();
//                    intent.setAction("READ_UUID");
//                    intent.putExtra("uuid", uid);
//                    sendBroadcast(intent);
                    QurrayCard(uid, false);
                    break;
            }
        }
    };

    private EditText edtCard, edtPhone, edtCarnum, edtMoney;
    private TextView shuka, submit;

    private LinearLayout lnkeys;
    private ImageView delete;
    private TextView textactive;
    private RelativeLayout lntext;

    private TextView textcharge;

    private void initView() {
        ReadCardUtil.InitReader();
//        piccReader = new PiccManager();
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_my_home_drawer_layout);
        lv_left_drawer = (ListView) findViewById(R.id.ll_my_home_left_drawer);
        lntext = ((RelativeLayout) findViewById(R.id.texts));

        edtCard = ((EditText) findViewById(R.id.edit_id));

        edtPhone = ((EditText) findViewById(R.id.edit_phone));
        edtCarnum = ((EditText) findViewById(R.id.edit_carnum));
        edtMoney = ((EditText) findViewById(R.id.edit_money));
        shuka = ((TextView) findViewById(R.id.text_shuaka));
        submit = ((TextView) findViewById(R.id.text_submit));
        cardid = getIntent().getStringExtra("cardid");
        type = getIntent().getStringExtra("type");
        lnkeys = ((LinearLayout) findViewById(R.id.ln_keys));
        lnkeys.setVisibility(View.GONE);
        edtCarnum.setOnFocusChangeListener(this);
        edtPhone.setOnFocusChangeListener(this);
        edtCard.setOnFocusChangeListener(this);

        delete = ((ImageView) findViewById(R.id.img_delete));
        delete.setOnClickListener(this);
        textactive = ((TextView) findViewById(R.id.text_active));
        textcharge = ((TextView) findViewById(R.id.text_charge));
        textactive.setOnClickListener(this);
        textcharge.setOnClickListener(this);
//        textcharge.setVisibility(View.GONE);
        edtCarnum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edtCarnum.removeTextChangedListener(this);
                String ss = CommontUtils.ToUpperKeys(s.toString());
                edtCarnum.setText(ss);
                edtCarnum.setSelection(ss.length());
                edtCarnum.addTextChangedListener(this);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_delete:
                String str = edtCarnum.getText().toString();
                if (!TextUtils.isEmpty(str)) {
                    edtCarnum.setText(str.substring(0, str.length() - 1));
                }
                break;
            case R.id.text_active:
                //激活卡片
                ActiveCard();
                break;
            case R.id.text_charge:
                //充值卡片
                Intent intent = new Intent(context, CardChargeActivity.class);
                intent.putExtra("uuid", edtCard.getText().toString());
                startActivity(intent);
                break;
            case R.id.text_submit:
                lnkeys.setVisibility(View.GONE);
                if (!TextUtils.isEmpty(edtCard.getText().toString())) {
//                    if (CommontUtils.checkPhoneNum(edtPhone.getText().toString())) {
//                        if (CheckUtils.CarChecked(edtCarnum.getText().toString())) {
                    if (CheckUtils.CarChecked(edtCarnum.getText().toString())) {
                        //调用接口绑定
                        try {
                            OpenCard();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(context, "必须输入车牌号！", Toast.LENGTH_LONG).show();
                    }

//                        } else {
//                            Toast.makeText(context, "请输入正确的车牌号！", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(context, "请输入正确的手机号码！", Toast.LENGTH_SHORT).show();
//                    }
                } else {
                    Toast.makeText(context, "请先刷卡！", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.edit_carnum:
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(view,InputMethodManager.SHOW_FORCED);
                    imm.hideSoftInputFromWindow(edtCarnum.getWindowToken(), 0);
                    lnkeys.setVisibility(View.VISIBLE);
                } else {
                    lnkeys.setVisibility(View.GONE);
                }
                break;
            case R.id.edit_id:
                if (hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(edtCard.getWindowToken(), 0);
                }
//            case R.id.edit_id_head:
//                if (hasFocus) {
//                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(edtCard_head.getWindowToken(), 0);
//                }
            case R.id.edit_phone:
                lnkeys.setVisibility(View.GONE);
                break;
        }
    }

    private ViewPager viewPager;// 页卡内容
    private ImageView imageView;// 动画图片
    private TextView textView1, textView2, textView3;
    private List<View> views;// Tab页面列表
    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度
    private View view1, view2, view3;// 各个页卡

    private void initViewPager() {
        textView1 = (TextView) findViewById(R.id.text1_dialog);
        textView2 = (TextView) findViewById(R.id.text2_dialog);
        textView3 = (TextView) findViewById(R.id.text3_dialog);
        textView1.setOnClickListener(new MyOnClickListener(0));
        textView2.setOnClickListener(new MyOnClickListener(1));
        textView3.setOnClickListener(new MyOnClickListener(2));
        viewPager = (ViewPager) findViewById(R.id.viewpager_dialog_dialog);
        views = new ArrayList<View>();
        LayoutInflater inflater = getLayoutInflater();
        view1 = inflater.inflate(R.layout.x_inputcarnumber_grid_layout, null);
        view2 = inflater.inflate(R.layout.x_inputcarnumber_grid_layout, null);
        view3 = inflater.inflate(R.layout.x_inputcarnumber_grid_layout, null);
        views.add(view1);
        views.add(view2);
        views.add(view3);
        viewPager.setAdapter(new ChoseCarNumPageAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());

        setView1();
        setView2();
        sheView3();
    }

    private void InitImageView() {
        imageView = (ImageView) findViewById(R.id.cursor_dialog);
        bmpW = BitmapFactory.decodeResource(context.getResources(), R.drawable.viewpage).getWidth();// 获取图片宽度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        offset = (screenW / 3 - bmpW) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        imageView.setImageMatrix(matrix);// 设置动画初始位置
    }


    private class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        public void onClick(View v) {
            viewPager.setCurrentItem(index);
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
        int two = one * 2;// 页卡1 -> 页卡3 偏移量

        public void onPageScrollStateChanged(int arg0) {

        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        public void onPageSelected(int arg0) {

            Animation animation = new TranslateAnimation(one * currIndex, one * arg0, 0, 0);
            currIndex = arg0;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(300);
            imageView.startAnimation(animation);
            switch (viewPager.getCurrentItem()) {
                case 0:
                    textView1.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                    textView2.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    textView3.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    break;
                case 1:
                    textView2.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                    textView1.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    textView3.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    break;
                case 2:
                    textView3.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                    textView1.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    textView2.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    break;
            }
        }
    }

    public void setView1() {
        final GridView gv_province = (GridView) view1.findViewById(R.id.input_gridview_dialog);
        gv_province.setSelector(new ColorDrawable(Color.TRANSPARENT));
        final String[] province = new String[]{"京", "沪", "浙", "苏", "粤", "鲁", "晋", "冀", "豫", "川", "渝", "辽", "吉", "黑", "皖", "鄂",
                "湘", "赣", "闽", "陕", "甘", "宁", "蒙", "津", "贵", "云", "桂", "琼", "青", "新", "藏", "港", "澳"};
        ArrayList<String> provinces = new ArrayList<String>();
        for (int i = 0; i < province.length; i++) {
            provinces.add(province[i]);
        }
        InputCarNumberGridAdapter adapter = new InputCarNumberGridAdapter(context, provinces, false);
        gv_province.setAdapter(adapter);
        gv_province.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final int index = edtCarnum.getSelectionStart();
                final Editable editable = edtCarnum.getText();
                editable.insert(index, province[position]);
            }
        });
    }

    public void setView2() {
        final GridView gv_number = (GridView) view2.findViewById(R.id.input_gridview_dialog);
        gv_number.setSelector(new ColorDrawable(Color.TRANSPARENT));
        final String[] number = new String[]{"A", "B", "C", "D", "0", "5", "E", "F", "G", "H", "1", "6", "J", "K", "L", "M",
                "2", "7", "N", "P", "Q", "R", "3", "8", "S", "T", "U", "V", "4", "9", "W", "X", "Y", "Z"};
        ArrayList<String> numbers = new ArrayList<String>();
        for (int i = 0; i < number.length; i++) {
            numbers.add(number[i]);
        }
        InputCarNumberGridAdapter adapter = new InputCarNumberGridAdapter(context, numbers, true);
        gv_number.setAdapter(adapter);
        gv_number.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final int index = edtCarnum.getSelectionStart();
                final Editable editable = edtCarnum.getText();
                editable.insert(index, number[position]);
            }
        });
    }

    public void sheView3() {
        final GridView gv_police = (GridView) view3.findViewById(R.id.input_gridview_dialog);
        gv_police.setSelector(new ColorDrawable(Color.TRANSPARENT));
        final String[] police = new String[]{"军", "空", "海", "北", "沈", "兰", "济", "南", "广", "成", "WJ", "警",
                "消", "边", "水", "电", "林", "通", "使", "学", "无"};
        ArrayList<String> polices = new ArrayList<String>();
        for (int i = 0; i < police.length; i++) {
            polices.add(police[i]);
        }

        InputCarNumberGridAdapter adapter = new InputCarNumberGridAdapter(context, polices, false);
        gv_police.setAdapter(adapter);
        gv_police.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final int index = edtCarnum.getSelectionStart();
                final Editable editable = edtCarnum.getText();
                editable.insert(index, police[position]);

            }
        });
    }

    private void initActionBar() {
        drawerLayout.setDrawerListener(new MyDrawerListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_am, R.string.hello_world,
                R.string.hello_world);
        mDrawerToggle.syncState();
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.activecard);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * 抽屉的监听
     */
    private class MyDrawerListener implements DrawerLayout.DrawerListener {
        @Override
        public void onDrawerOpened(View drawerView) {// 打开抽屉的回调
            mDrawerToggle.onDrawerOpened(drawerView);
            actionBar.setTitle("停车宝");
        }

        @Override
        public void onDrawerClosed(View drawerView) {// 关闭抽屉的回调
            mDrawerToggle.onDrawerClosed(drawerView);
            actionBar.setTitle(R.string.activecard);
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {// 抽屉滑动的回调
            mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
        }

        @Override
        public void onDrawerStateChanged(int newState) {// 抽屉状态改变的回调
            mDrawerToggle.onDrawerStateChanged(newState);
        }
    }

    // actionBar的点击回调方法
    @SuppressLint("RtlHardcoded")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 绑定卡
     * collectorrequest.do?action=bindcard
     * <p/>
     * result --0：失败 1：成功
     * errmsg：原因
     */
    private String cardret = "0";
    long current = 0;

    public void OpenCard() throws UnsupportedEncodingException {
        if (System.currentTimeMillis() - current > 1000) {
            if (!IsNetWork.IsHaveInternet(this)) {
                Toast.makeText(this, "获取信息失败，请检查网络！", Toast.LENGTH_SHORT).show();
                return;
            }
            String car = URLEncoder.encode(edtCarnum.getText().toString(), "utf-8");
            AQuery aQuery = new AQuery(OpenCardActivity.this);
            String path = baseurl;
            String url = path + "collectorrequest.do?action=bindcard&token="
                    + token + "&carnumber=" + URLEncoder.encode(car, "utf-8") + "&uuid=" +
                    edtCard.getText().toString() + "&mobile=" + edtPhone.getText().toString()
                    + "&version=" + CommontUtils.getVersion(context) + "&out=json";
            MyLog.i("opencard-->>", "开卡的URL-->>" + url);
            final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "正在绑定卡片...", true, true);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    dialog.dismiss();
                    if (object != null) {
                        Gson gson = new Gson();
                        BaseResponse response = gson.fromJson(object, BaseResponse.class);
                        cardret = response.getResult();
                        edtCard.setText("");

                        edtCarnum.setText("");
                        edtPhone.setText("");
                        MyLog.i("opencard-->>", "解析的json" + response.toString());
                        if (CommontUtils.checkString(response.getErrmsg())) {
                            new AlertDialog.Builder(OpenCardActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                    .setMessage(response.getErrmsg()).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (cardret.equals("1")) {
                                        finish();
                                    } else {

                                    }
                                }
                            }).setCancelable(false).create().show();
                        }

                    } else {
                        Toast.makeText(context, "返回数据错误！", Toast.LENGTH_SHORT).show();
                    }
                }

            });
            current = System.currentTimeMillis();
        }
    }

    /**
     * 激活卡
     * collectorrequest.do?action=actcard&token=&version=&uuid=
     * <p/>
     * result --0：失败 1：成功
     * errmsg：原因
     */
    long current2 = 0;

    public void ActiveCard() {
//        String path = baseurl;
//        String url = path + "collectorrequest.do?action=actcard&token="
//                + token + "&uuid=" + edtCard.getText().toString()
//                + "&version=" + CommontUtils.getVersion(context) + "&out=json";
//        RequestQueue queue = ((MyApplication)getApplication()).getRequestQueue();
//        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String object) {
//                if (object != null) {
//                    Gson gson = new Gson();
//                    CardInfo info = gson.fromJson(object, CardInfo.class);
//                    cardret = info.getResult();
//                    if (info.getResult().equals("1")) {
////                        try{
//                            Toast.makeText(context, info.getErrmsg(), Toast.LENGTH_SHORT).show();
//                            Message m = new Message();
//                            m.what = 1228;
//                            m.obj = info.getCard().getNfc_uuid();
//                            String uuid = info.getCard().getNfc_uuid();
//                            QurrayCard(uuid, true);
////                        }catch (Exception e){
////                            e.printStackTrace();
////                        }
//
////                            handler.sendMessage(m);
//                    } else {
//                        Toast.makeText(context, info.getErrmsg(), Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(context, "返回数据错误！", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//
//            }
//        });
//        queue.add(request);//将请求添加到请求队列
        if (System.currentTimeMillis() - current2 > 1000) {
            if (!IsNetWork.IsHaveInternet(this)) {
                Toast.makeText(this, "获取信息失败，请检查网络！", Toast.LENGTH_SHORT).show();
                return;
            }
            AQuery aQuery = new AQuery(OpenCardActivity.this);
            String path = baseurl;
            String url = path + "collectorrequest.do?action=actcard&token="
                    + token + "&uuid=" + edtCard.getText().toString()
                    + "&version=" + CommontUtils.getVersion(context) + "&out=json";
            MyLog.i("actcard-->>", "actcard的URL-->>" + url);
//            final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "正在开通卡片...", true, true);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
//                dialog.dismiss();
                    MyLog.i("actcard-->>", "json--" + object);
                    if (object != null) {
                        Gson gson = new Gson();
                        CardInfo info = gson.fromJson(object, CardInfo.class);
                        cardret = info.getResult();
                        if (info.getResult().equals("1")) {
                            Toast.makeText(context, info.getErrmsg(), Toast.LENGTH_SHORT).show();
//                            Message m = new Message();
//                            m.what = 1228;
//                            m.obj = info.getCard().getNfc_uuid();
                            QurrayCard(edtCard.getText().toString(), true);
//                            handler.sendMessage(m);
                        } else {
                            Toast.makeText(context, info.getErrmsg(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "返回数据错误！", Toast.LENGTH_SHORT).show();
                    }
                }

            });
            current2 = System.currentTimeMillis();
        }
    }

    /**
     * 查询卡信息
     * collectorrequest.do?action=cardinfo
     * &token=&version=&uuid=
     * <p/>
     * result --0：失败 1：成功
     * errmsg：原因
     */

    public void QurrayCard(String uuid, final boolean isprint) {
//        if (!IsNetWork.IsHaveInternet(this)) {
//            Toast.makeText(this, "获取信息失败，请检查网络！", Toast.LENGTH_SHORT).show();
//            return;
//        }
        edtMoney.setText("");
        edtCarnum.setText("");
        edtPhone.setText("");
        AQuery aQuery = new AQuery(OpenCardActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=cardinfo&token="
                + token + "&uuid=" + uuid
                + "&version=" + CommontUtils.getVersion(context) + "&out=json";
        MyLog.i("opencard-->>", "查询的URL-->>" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "正在查询...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                dialog.dismiss();
                MyLog.i("opencard-->>", "json--" + object);
                if (object != null) {
                    Gson gson = new Gson();
                    CardInfo info = gson.fromJson(object, CardInfo.class);
                    cardret = info.getResult();
                    if (info.getResult().equals("1")) {
                        if (isprint) {
//                            Message m = new Message();
//                            m.what = 12282;
//                            m.obj = info;
//                            handler.sendMessage(m);
                            printactive(info);
                        }

                        edtMoney.setText(info.getCard().getBalance());
                        if (!TextUtils.isEmpty(info.getMobile())) {
                            edtPhone.setText(info.getMobile());
//                            edtPhone.setSelection(info.getMobile().length());
                        }
                        if (!TextUtils.isEmpty(info.getCarnumber())) {
                            edtCarnum.setText(info.getCarnumber());
                            edtCarnum.setSelection(info.getCarnumber().length());
                        }
                        if (info.getCard() != null) {
                            if (!TextUtils.isEmpty(info.getCard().getCard_number())) {
                                String uid = info.getCard().getNfc_uuid();
                                Intent intent = new Intent();
                                intent.setAction("READ_UUID");
                                intent.putExtra("uuid", uid);
                                sendBroadcast(intent);
                                edtCard.setText(uid);
                                edtCard.setSelection(uid.length());
                            }
                        }
                    } else {
                        Toast.makeText(context, info.getErrmsg(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "返回数据错误！", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ReadCardUtil.StopReading();
    }

    /**
     * 激活卡后打印出小票
     */
    private void printactive(CardInfo info) {
        Card card = info.getCard();
        String Sname = "";
        String gang = "";
        if (SharedPreferencesUtils.getIntance(this).getisprintName()) {
            Sname = getStringFromPreference("name");
            gang = "-";
        }

        String str = "收费凭证\n\n" +
                "收费单位：" + info.getGroup_name() + "\n" +
                "终端编号：" + CommontUtils.GetHardWareAddress(context) + "\n" +
                "收费员：" + Sname;
        if (CommontUtils.Is910()) {
            if (!TextUtils.isEmpty(SharedPreferencesUtils.getIntance(context).getmobile())) {
                str += "(" + SharedPreferencesUtils.getIntance(context).getmobile() + ")";
            }
        }
        str += gang + SharedPreferencesUtils.getIntance(this).getAccount();
        str += "\n" +
                "卡号：" + card.getNfc_uuid() + "\n" +
                "卡面号：" + card.getCard_number() + "\n" +
                "初始金额：￥" + card.getBalance() + "\n" +
                "操作时间：" + CommontUtils.getTimespanss() + "\n\n";
        PullMsgService.sendMessage(str, context);
    }
}
