package com.zhenlaidian.photo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.ProvinceGridViewAdapter;
import com.zhenlaidian.bean.CarNumberMadeOrder;
import com.zhenlaidian.bean.EPayMessageDialog;
import com.zhenlaidian.bean.FinishOrderFailDialog;
import com.zhenlaidian.bean.LeaveOrder;
import com.zhenlaidian.bean.MainUiInfo;
import com.zhenlaidian.bean.MakeVipInfo;
import com.zhenlaidian.bean.NfcOrder;
import com.zhenlaidian.bean.NfcPrepaymentOrder;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.ui.LostOrderRecordActivity;
import com.zhenlaidian.util.CheckUtils;
import com.zhenlaidian.util.MyLog;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 手工输入车牌进场;包含车牌输入法和逻辑处理;
 */
@SuppressLint("HerLeak")
public class InputCarNumberActivity extends BaseActivity {
    private SharedPreferences spf;
    private ViewPager viewPager;// 页卡内容
    private ImageView imageView;// 动画图片
    private TextView textView1, textView2, textView3, enlargefont;
    private List<View> views;// Tab页面列表
    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度
    private View view1, view2, view3;// 各个页卡
    private EditText et_carnumber;
    private Button bt_delete_edtext;
    private Button bt_photo;
    private Button bt_ok;
    private String add;// 添加车牌号入口；
    private String carnumber;
    private String orderid;
    private NfcOrder nfcorder;
    private String nfcuid;
    private Animation alphaAnimation;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.input_car_number_activity);
//        SysApplication.getInstance().addActivity(this);
        if (getIntent().getExtras() != null) {
            add = getIntent().getExtras().getString("add");
            carnumber = getIntent().getExtras().getString("carnumber");
            orderid = getIntent().getExtras().getString("orderid");
            nfcorder = (NfcOrder) getIntent().getExtras().get("nfcorder");
            nfcuid = getIntent().getExtras().getString("nfcid");
        }
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                showErrorDialog(msg.obj.toString());
            }
        };
        InitImageView();
        InitTextView();
        InitViewPager();
        hideTypewriting();
        setmView();
        setView1();
        setView2();
        sheView3();
        initHint();

    }

    public void initHint() {
        if (add != null && add.equals("makevip")) {
            getSupportActionBar().setTitle("输入车牌号绑定VIP卡");
            et_carnumber.setHint("输入车主车牌号码");
        } else if (add != null && add.equals("recommend")) {
            getSupportActionBar().setTitle("开通普通会员");
            et_carnumber.setHint("输入车主车牌号码");
        }

    }

    @SuppressLint("InlinedApi")
    @SuppressWarnings("deprecation")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (add != null && "makevip".equals(add)) {

        } else if (add != null && "recommend".equals(add)) {

        } else {
            getMenuInflater().inflate(R.menu.check_scan, menu);
            spf = getSharedPreferences("tingchebao", Context.MODE_PRIVATE);
            if (spf.getBoolean("indefault", false)) {
                MenuCompat.setShowAsAction(menu.findItem(R.id.check_no), MenuItem.SHOW_AS_ACTION_IF_ROOM);
                MenuItem item = menu.findItem(R.id.check_yes);
                item.setVisible(false);
            } else {
                MenuCompat.setShowAsAction(menu.findItem(R.id.check_yes), MenuItem.SHOW_AS_ACTION_IF_ROOM);
                MenuItem item = menu.findItem(R.id.check_no);
                item.setVisible(false);
            }
        }
        return true;
    }

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                InputCarNumberActivity.this.finish();
                return true;
            case R.id.check_yes:
                toDialog(1, "默认手工输入", "您确定将手工输入车牌号设置为默认的输入车牌方式吗?");
                return true;
            case R.id.check_no:
                toDialog(2, "移除默认手工输入", "默认输入车牌方式将改为扫描车牌输入.");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void toDialog(final int car_num, String msg, String stitle) {
        AlertDialog.Builder mBuilder = new Builder(this);
        mBuilder.setMessage(stitle);
        mBuilder.setTitle(msg);
        mBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (car_num == 1) {
                    spf.edit().putBoolean("indefault", true).commit();
                } else {
                    spf.edit().putBoolean("indefault", false).commit();
                }
                arg0.dismiss();
                refresh();
            }
        });
        mBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        });
        mBuilder.create().show();
    }

    /**
     * 刷新 本页面
     */
    private void refresh() {
        this.finish();
        Intent intent = new Intent(InputCarNumberActivity.this, InputCarNumberActivity.class);
        startActivity(intent);
    }

    /**
     * 初始化数据
     */
    private void InitViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        views = new ArrayList<View>();
        LayoutInflater inflater = getLayoutInflater();
        view1 = inflater.inflate(R.layout.input_car_number_province, null);
        view2 = inflater.inflate(R.layout.input_car_number_number, null);
        view3 = inflater.inflate(R.layout.input_car_number_police, null);
        views.add(view1);
        views.add(view2);
        views.add(view3);
        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        et_carnumber = (EditText) findViewById(R.id.et_input_carnumber_number1);

        if (carnumber != null) {
            et_carnumber.setText(carnumber);
        }
        bt_photo = (Button) findViewById(R.id.bt_input_carnumber_photo);
        bt_ok = (Button) findViewById(R.id.bt_input_carnumber_ok);
        if (add != null && add.equals("add")) {
            bt_ok.setText("添加完成");
        } else if (add != null && add.equals("change")) {
            bt_ok.setText("修改完成");
        } else if (add != null && add.equals("cashOrder")) {
            bt_ok.setText("结算订单");
        } else if (add != null && add.equals("makevip")) {
            bt_ok.setText("开通会员");
        } else if (add != null && add.equals("recommend")) {
            bt_ok.setText("开通会员");
        }
        bt_delete_edtext = (Button) findViewById(R.id.bt_input_carnumber_delete);
    }

    public void setmView() {
        if (add != null) {
            if (add.equals("add") || add.equals("change")) {
                bt_photo.setVisibility(View.GONE);
            } else if (add.equals("cashOrder") || add.equals("makevip") || add.equals("recommend")) {
                bt_photo.setText("取消");
                bt_photo.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        InputCarNumberActivity.this.finish();
                    }
                });
            } else {
                bt_photo.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO 切换为拍照录入；
                        Intent intent = new Intent(InputCarNumberActivity.this, MyCaptureActivity.class);
                        startActivity(intent);
                        InputCarNumberActivity.this.finish();
                    }
                });
            }
        } else {
            bt_photo.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO 切换为拍照录入；
                    Intent intent = new Intent(InputCarNumberActivity.this, MyCaptureActivity.class);
                    startActivity(intent);
                    InputCarNumberActivity.this.finish();
                }
            });
        }

        bt_ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 输入完成；
                if (add != null && add.equals("add")) {
                    try {
                        boolean isMatched = CheckUtils.CarChecked(et_carnumber.getText().toString().trim());
                        if (isMatched) {
                            addCarNumber(orderid);
                        } else {
                            Toast.makeText(InputCarNumberActivity.this, "请输入正确的车牌号！", Toast.LENGTH_SHORT).show();
                        }
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(InputCarNumberActivity.this, "手输添加车牌号失败-转码异常！", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                } else if (add != null && add.equals("change")) {

                    try {
                        boolean isMatched = CheckUtils.CarChecked(et_carnumber.getText().toString().trim());
                        if (isMatched) {
                            addCarNumber(orderid);
                        } else {
                            Toast.makeText(InputCarNumberActivity.this, "请输入正确的车牌号！", Toast.LENGTH_SHORT).show();
                        }
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(InputCarNumberActivity.this, "手输修改车牌号失败-转码异常！", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                } else if (add != null && add.equals("cashOrder")) {
                    boolean isMatched = CheckUtils.CarChecked(et_carnumber.getText().toString().trim());
                    if (isMatched) {
                        try {
                            cashNfcorder("0");
                        } catch (UnsupportedEncodingException e) {
                            Toast.makeText(InputCarNumberActivity.this, "nfc添加车牌结算转码异常！", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(InputCarNumberActivity.this, "请输入正确的车牌号！", Toast.LENGTH_SHORT).show();
                    }
                } else if (add != null && add.equals("makevip")) {
                    boolean isMatched = CheckUtils.CarChecked(et_carnumber.getText().toString().trim());
                    if (isMatched) {

                        try {
                            bindPlate(nfcuid, "", "0", "0");
                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            Toast.makeText(InputCarNumberActivity.this, "绑定会员卡符转码异常！", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(InputCarNumberActivity.this, "请输入正确的车牌号！", Toast.LENGTH_SHORT).show();
                    }
                } else if (add != null && add.equals("recommend")) {// 输入车牌号推荐车主；
                    boolean isMatched = CheckUtils.CarChecked(et_carnumber.getText().toString().trim());
                    if (isMatched) {
                        try {
                            registerVip("");
                        } catch (UnsupportedEncodingException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            Toast.makeText(InputCarNumberActivity.this, "注册普通会员符转码异常！", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(InputCarNumberActivity.this, "请输入正确的车牌号！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    boolean isMatched = CheckUtils.CarChecked(et_carnumber.getText().toString().trim());
                    if (isMatched) {
                        try {
                            InputCarNumberActivity.this.madeOrder();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            Toast.makeText(InputCarNumberActivity.this, "提交车牌字符转码异常！", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(InputCarNumberActivity.this, "请输入正确的车牌号！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        et_carnumber.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    viewPager.setCurrentItem(1);
                }
            }
        });
        // et_carnumber.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // // TODO Auto-generated method stub
        // InputMethodManager imm =
        // (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        // imm.hideSoftInputFromWindow(v.getWindowToken(), Toast.LENGTH_SHORT);
        // }
        // });
        bt_delete_edtext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // bt_delete_edtext.setFocusable(true);
                // InputMethodManager mInputMethodManager = (InputMethodManager)
                // getSystemService(Context.INPUT_METHOD_SERVICE);
                // boolean isOpen = mInputMethodManager.isActive();
                // if (isOpen) {
                // mInputMethodManager.hideSoftInputFromWindow(
                // arg0.getWindowToken(), Toast.LENGTH_SHORT);
                // }
                hideTypewriting();
                String carnumber = et_carnumber.getText().toString().trim();
                if (carnumber.length() >= 1) {
                    int index = et_carnumber.getSelectionStart();
                    Editable editable = et_carnumber.getText();
                    if (index >= 1) {
                        editable.delete(index - 1, index);
                    }
                }
            }
        });
        bt_delete_edtext.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                et_carnumber.setText("");
                return false;
            }
        });
    }

    public void bt_input_car_delete(View view) {
        String carnumber = et_carnumber.getText().toString().trim();
        if (carnumber.length() >= 1) {
            int index = et_carnumber.getSelectionStart();
            Editable editable = et_carnumber.getText();
            if (index >= 1) {
                editable.delete(index - 1, index);
            }
        }
    }

    public void hideTypewriting() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_carnumber.getWindowToken(), Toast.LENGTH_SHORT);
        // Android.edittext点击时,隐藏系统弹出的键盘,显示出光标
        // 3.0以下版本可以用editText.setInputType(InputType.TYPE_NULL)来实现。
        // 3.0以上版本除了调用隐藏方法:setShowSoftInputOnFocus(false)
        int sdkInt = Build.VERSION.SDK_INT;// 16 -- 4.1系统
        if (sdkInt >= 11) {
            Class<EditText> cls = EditText.class;
            try {
                Method setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setShowSoftInputOnFocus.setAccessible(false);
                setShowSoftInputOnFocus.invoke(et_carnumber, false);
                setShowSoftInputOnFocus.invoke(et_carnumber, false);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            et_carnumber.setInputType(InputType.TYPE_NULL);
        }
    }

    /**
     * 初始化头标
     */

    private void InitTextView() {
        textView1 = (TextView) findViewById(R.id.text1);
        textView2 = (TextView) findViewById(R.id.text2);
        textView3 = (TextView) findViewById(R.id.text3);
        textView1.setOnClickListener(new MyOnClickListener(0));
        textView2.setOnClickListener(new MyOnClickListener(1));
        textView3.setOnClickListener(new MyOnClickListener(2));
    }

    /**
     * 2 * 初始化动画，这个就是页卡滑动时，下面的横线也滑动的效果，在这里需要计算一些数据
     */

    private void InitImageView() {
        imageView = (ImageView) findViewById(R.id.cursor);
        bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.viewpage).getWidth();// 获取图片宽度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        offset = (screenW / 3 - bmpW) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, Toast.LENGTH_SHORT);
        imageView.setImageMatrix(matrix);// 设置动画初始位置
    }

    // <img src="http://img.my.csdn.net/uploads/201211/10/1352554452_1685.jpg"
    // alt="">

    /**
     * 头标点击监听 3
     */
    private class MyOnClickListener implements OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        public void onClick(View v) {
            viewPager.setCurrentItem(index);
        }
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        public MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), Toast.LENGTH_SHORT);
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    public class MyOnPageChangeListener implements OnPageChangeListener {

        int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
        int two = one * 2;// 页卡1 -> 页卡3 偏移量

        public void onPageScrollStateChanged(int arg0) {

        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        public void onPageSelected(int arg0) {

            Animation animation = new TranslateAnimation(one * currIndex, one * arg0, 0, Toast.LENGTH_SHORT);
            currIndex = arg0;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(300);
            imageView.startAnimation(animation);
            switch (viewPager.getCurrentItem()) {
                case 0:
                    textView1.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
                    textView2.setTextColor(getResources().getColor(R.color.black));
                    textView3.setTextColor(getResources().getColor(R.color.black));
                    break;
                case 1:
                    textView2.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
                    textView1.setTextColor(getResources().getColor(R.color.black));
                    textView3.setTextColor(getResources().getColor(R.color.black));
                    break;
                case 2:
                    textView3.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
                    textView1.setTextColor(getResources().getColor(R.color.black));
                    textView2.setTextColor(getResources().getColor(R.color.black));
                    break;
            }
        }
    }

    public void setView1() {
        final GridView gv_province = (GridView) view1.findViewById(R.id.gridview_province);
        gv_province.setSelector(new ColorDrawable(Color.TRANSPARENT));
        final String[] province = new String[]{"京", "沪", "浙", "苏", "粤", "鲁", "晋", "冀", "豫", "川", "渝", "辽", "吉", "黑", "皖", "鄂",
                "湘", "赣", "闽", "陕", "甘", "宁", "蒙", "津", "贵", "云", "桂", "琼", "青", "新", "藏", "港", "澳", "使", ""};
        ArrayList<String> provinces = new ArrayList<String>();
        for (int i = 0; i < province.length; i++) {
            provinces.add(province[i]);
        }
        ProvinceGridViewAdapter adapter = new ProvinceGridViewAdapter(this, provinces, false);
        gv_province.setAdapter(adapter);
        gv_province.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final int index = et_carnumber.getSelectionStart();
                final Editable editable = et_carnumber.getText();
                TextView tv = (TextView) view.findViewById(R.id.tv_textinfo);
                tv.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View arg0, MotionEvent arg1) {
                        // TODO Auto-generated method stub
                        return false;
                    }
                });

                editable.insert(index, province[position]);
            }
        });
    }

    public void setView2() {
        final GridView gv_number = (GridView) view2.findViewById(R.id.gridview_number);
        gv_number.setSelector(new ColorDrawable(Color.TRANSPARENT));
        final String[] number = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "O", "P", "Q",
                "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        ArrayList<String> numbers = new ArrayList<String>();
        for (int i = 0; i < number.length; i++) {
            numbers.add(number[i]);
        }
        ProvinceGridViewAdapter adapter = new ProvinceGridViewAdapter(this, numbers, true);
        gv_number.setAdapter(adapter);
        gv_number.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final int index = et_carnumber.getSelectionStart();
                final Editable editable = et_carnumber.getText();
                editable.insert(index, number[position]);
            }
        });
    }

    public void sheView3() {
        final GridView gv_police = (GridView) view3.findViewById(R.id.gridview_police);
        gv_police.setSelector(new ColorDrawable(Color.TRANSPARENT));
        final String[] police = new String[]{"军", "空", "海", "北", "沈", "兰", "济", "南", "广", "成", "", "", "", "", "", "WJ", "警",
                "消", "边", "水", "", "电", "林", "通", ""};
        ArrayList<String> polices = new ArrayList<String>();
        for (int i = 0; i < police.length; i++) {
            polices.add(police[i]);
        }

        ProvinceGridViewAdapter adapter = new ProvinceGridViewAdapter(this, polices, false);
        gv_police.setAdapter(adapter);
        gv_police.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final int index = et_carnumber.getSelectionStart();
                final Editable editable = et_carnumber.getText();
                editable.insert(index, police[position]);

            }
        });
    }

    // 按车牌生成订单；正常查询是否有逃单
    // http://192.168.199.240/zld/cobp.do?action=preaddorder&comid=3&uid=100005&carnumber=aaabbdd
    public void madeOrder() throws UnsupportedEncodingException {
        SharedPreferences pfs = this.getSharedPreferences("autologin", Context.MODE_PRIVATE);
        String uid = pfs.getString("account", "");
        String carnumber = URLEncoder.encode(et_carnumber.getText().toString().trim(), "utf-8");
        String path = baseurl;
        String url = path + "cobp.do?action=preaddorder&comid=" + comid + "&uid=" + uid + "&carnumber="
                + URLEncoder.encode(carnumber, "utf-8") + "&imei=" + imei;
        MyLog.w("CheckNumberActivity", "车牌识别生成订单的URL--->" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "生成订单", "提交订单数据中...", true, true);
        dialog.setCanceledOnTouchOutside(false);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && object != null) {
                    dialog.dismiss();
                    MyLog.i("CheckNumberActivity", "车牌识别生成订单的结果--->" + object);
                    Gson gson = new Gson();
                    CarNumberMadeOrder info = gson.fromJson(object, CarNumberMadeOrder.class);
                    if (info.getInfo().equals("1")) {
                        Message msg = new Message();
                        msg.what = 2;
                        msg.obj = new MainUiInfo(true, 4, 1.00);
                        LeaveActivity.handler.sendMessage(msg);
                        InputCarNumberActivity.this.finish();
                    } else if (info.getInfo().equals("0")) {
                        if (info.getOther() != null && info.getOwn() != null) {
                            int parseInt = Integer.parseInt(info.getOwn());
                            int parseInt2 = Integer.parseInt(info.getOther());
                            int num = parseInt + parseInt2;
                            LostOrderDialog(et_carnumber.getText().toString().trim() + "有" + num + "笔逃单,在您的车场逃单" + parseInt
                                    + "次!");
                        } else {
                            Toast.makeText(InputCarNumberActivity.this, "查询逃单信息服务器错误！", Toast.LENGTH_SHORT).show();
                        }
                    } else if (info.getInfo().equals("-1")) {
                        Toast.makeText(InputCarNumberActivity.this, "车场编号错误！", Toast.LENGTH_SHORT).show();
                    } else if (info.getInfo().equals("-2")) {
                        Toast.makeText(InputCarNumberActivity.this, et_carnumber.getText().toString().trim() + "存在未结算订单,请先结算！", Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    dialog.dismiss();
                    switch (status.getCode()) {
                        case -101:
                            Toast.makeText(InputCarNumberActivity.this, "网络错误！--请再次确认车牌生成订单！", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(InputCarNumberActivity.this, "服务器错误！--请再次确认车牌生成订单！", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }

    // 按车牌生成订单； 强制直接生成订单
    // http://192.168.199.240/zld/cobp.do?action=addorder&comid=3&uid=100005&carnumber=aaabbdd
    public void addOrder() throws UnsupportedEncodingException {
        SharedPreferences pfs = this.getSharedPreferences("autologin", Context.MODE_PRIVATE);
        String uid = pfs.getString("account", "");
        String carnumber = URLEncoder.encode(et_carnumber.getText().toString().trim(), "utf-8");
        String path = baseurl;
        String url = path + "cobp.do?action=addorder&comid=" + comid + "&uid=" + uid + "&carnumber="
                + URLEncoder.encode(carnumber, "utf-8") + "&imei=" + imei;
        MyLog.w("CheckNumberActivity", "车牌识别生成订单的URL--->" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "生成订单", "提交订单数据中...", true, true);
        dialog.setCanceledOnTouchOutside(false);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && object != null) {
                    dialog.dismiss();
                    MyLog.i("CheckNumberActivity", "车牌识别生成订单的结果--->" + object);
                    if (object.equals("1")) {
                        Message msg = new Message();
                        msg.what = 2;
                        msg.obj = new MainUiInfo(true, 4, 1.00);
                        LeaveActivity.handler.sendMessage(msg);
                        Toast.makeText(InputCarNumberActivity.this, "手输车牌订单生成,可在当前订单查看！", Toast.LENGTH_SHORT).show();
                        InputCarNumberActivity.this.finish();
                    } else {
                        Toast.makeText(InputCarNumberActivity.this, "" + object, 1).show();
                    }
                } else {
                    dialog.dismiss();
                    switch (status.getCode()) {
                        case -101:
                            Toast.makeText(InputCarNumberActivity.this, "网络错误！--请再次确认车牌生成订单！", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(InputCarNumberActivity.this, "服务器错误！--请再次确认车牌生成订单！", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }

    // 添加修改车牌号；
    // cobp.do?action=addcarnumber&comid=10&orderid=&carnumber=
    public void addCarNumber(String orderid) throws UnsupportedEncodingException {

        String carnumber = URLEncoder.encode(et_carnumber.getText().toString().trim(), "utf-8");
        String path = baseurl;
        String url = path + "cobp.do?action=addcarnumber&comid=" + comid + "&orderid=" + orderid + "&carnumber="
                + URLEncoder.encode(carnumber, "utf-8");
        MyLog.w("InputCarNumberActivity", "手输添加车牌号的URL--->" + url);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && object != null) {
                    MyLog.i("CheckNumberActivity", "车牌识别添加车牌号的结果--->" + object);
                    if (object.equals("1")) {
                        InputCarNumberActivity.this.finish();
                    } else if (object.equals("0")) {
                        Toast.makeText(InputCarNumberActivity.this, "添加失败-车牌号已存在订单！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(InputCarNumberActivity.this, "添加车牌失败" + object, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    switch (status.getCode()) {
                        case -101:
                            Toast.makeText(InputCarNumberActivity.this, "网络错误！--添加车牌失败！", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(InputCarNumberActivity.this, "服务器错误！--添加车牌失败！", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }

    // 有逃单提示对话框；
    public void LostOrderDialog(String warn) {
        AlertDialog.Builder builder = new Builder(this);
        builder.setIcon(R.drawable.app_icon_32);
        builder.setTitle("订单尚未生成");
        builder.setMessage(warn);
        builder.setCancelable(false);
        builder.setPositiveButton("查看", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(InputCarNumberActivity.this, LostOrderRecordActivity.class);
                intent.putExtra("carnumber", et_carnumber.getText().toString().trim());
                startActivity(intent);
                InputCarNumberActivity.this.finish();
            }
        });
        builder.setNegativeButton("继续生成订单", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    addOrder();
                } catch (UnsupportedEncodingException e) {
                    Toast.makeText(InputCarNumberActivity.this, "提交车牌字符转码异常！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        builder.create().show();
    }

    // 把确认结算订单提交给服务器；返回1提交成功；
    // http://192.168.1.102/zld/nfchandle.do?action=completeorder&orderid=78&collect=20&comid=3&imei=&carnumber=
    public void cashNfcorder(String pay) throws UnsupportedEncodingException {
        nfcorder.setCarnumber(et_carnumber.getText().toString().trim());
        SharedPreferences pfs = this.getSharedPreferences("autologin", Context.MODE_PRIVATE);
        String uid = pfs.getString("account", "");
        String carnumber = URLEncoder.encode(et_carnumber.getText().toString().trim(), "utf-8");
        if (nfcorder.getPrepay() != null && !nfcorder.getPrepay().equals("0.0")) {
            cashPrepayOrder(nfcorder.getCollect());
        } else {
            String url = baseurl + "nfchandle.do?action=completeorder&orderid=" + nfcorder.getOrderid() + "&collect="
                    + nfcorder.getCollect() + "&comid=" + comid + "&uid=" + uid + "&imei=" + imei + "&carnumber="
                    + URLEncoder.encode(carnumber, "utf-8") + "&pay=" + pay;
            MyLog.w("ShowNfcOrder", "确认结算NFC订单的URL--->" + url);
            final ProgressDialog dialog = ProgressDialog.show(this, "结算订单", "提交订单数据中...", true, true);
            dialog.setCanceledOnTouchOutside(false);
            AQuery aQuery = new AQuery(this);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (status.getCode() == 200 && object != null) {
                        dialog.dismiss();
                        MyLog.i("InputCarNumberActivity", "订单结算结果--->" + object);
                        if ("1".equals(object) || "2".equals(object) || "3".equals(object) || "4".equals(object)
                                || "5".equals(object)) {
                            Message msg = new Message();
                            msg.what = 2;
                            msg.obj = new MainUiInfo(true, 5, 1.00);
                            LeaveActivity.handler.sendMessage(msg);
                        }
                        if (object.equals("1")) {
                            InputCarNumberActivity.this.finish();
                            Toast.makeText(InputCarNumberActivity.this, "结算成功,等待车主支付！", Toast.LENGTH_SHORT).show();
                        } else if (object.equals("2")) {
                            Message msg = new Message();
                            msg.obj = object;
                            handler.sendMessage(msg);
                            InputCarNumberActivity.this.finish();
                        } else if (object.equals("-2")) {
                            Message msg = new Message();
                            msg.obj = object;
                            handler.sendMessage(msg);
                        } else if (object.equals("3")) {
                            Message msg = new Message();
                            msg.obj = object;
                            handler.sendMessage(msg);
                            InputCarNumberActivity.this.finish();
                        } else if (object.equals("-5")) {
                            dialog.dismiss();
                            StopWait2CashDialog(false);
                        } else if (object.equals("-6")) {
                            dialog.dismiss();
                            StopWait2CashDialog(true);
                        } else {
                            nfcorder.setNetError("添加车牌结算失败" + object);
                            showFinishFailDialog(nfcorder);
                            InputCarNumberActivity.this.finish();
                        }
                    } else {
                        dialog.dismiss();
                        if (status.getCode() == -101) {
                            nfcorder.setNetError(" -101：网络错误");
                            showFinishFailDialog(nfcorder);
                        } else if (status.getCode() == 500) {
                            nfcorder.setNetError(" 500：服务器错误");
                            showFinishFailDialog(nfcorder);
                        } else {
                            nfcorder.setNetError(" 000：网络请求错误");
                            showFinishFailDialog(nfcorder);
                        }
                    }
                }
            });
        }

    }

    public void StopWait2CashDialog(Boolean iscash) {
        View open_dialog_view = View.inflate(this, R.layout.dialog_stop_wait_to_cash, null);
        Button bt_cancle = (Button) open_dialog_view.findViewById(R.id.bt_stop_wait_dialog_cancle);
        Button bt_cash_order = (Button) open_dialog_view.findViewById(R.id.bt_stop_wait_dialog_cash);
        View view = open_dialog_view.findViewById(R.id.view_stop_wait_dialog_view);
        final Dialog stopdailog = new Builder(this).create();
        stopdailog.setCancelable(false);
        if (iscash) {
            bt_cash_order.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
        } else {
            bt_cash_order.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
        }
        bt_cancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 关闭对话框
                stopdailog.dismiss();
                InputCarNumberActivity.this.finish();
            }
        });
        bt_cash_order.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 现金结算；
                try {
                    cashNfcorder("1");
                } catch (UnsupportedEncodingException e) {
                    Toast.makeText(InputCarNumberActivity.this, "nfc添加车牌结算转码异常！", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        stopdailog.show();
        stopdailog.setContentView(open_dialog_view);
    }

    /*
     * 预付费的结算接口；
     * http://127.0.0.1/zld/nfchandle.do?action=doprepayorder&orderid=&
     * collect=20&comid="+ comid
     */
    public void cashPrepayOrder(final String collect) {
        String url = baseurl + "nfchandle.do?action=doprepayorder&orderid=" + nfcorder.getOrderid() + "&collect=" + collect
                + "&comid=" + comid;
        MyLog.w("ShowNfcOrder", "确认结算预付费NFC订单的URL--->" + url);
        AQuery aQuery = new AQuery(this);
        final ProgressDialog pd = ProgressDialog.show(this, "结算中...", "正在提交结算数据...", true, true);
        pd.setCanceledOnTouchOutside(false);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
                    pd.dismiss();
                    MyLog.i("ShowNfcOrder", "预付费订单结算结果--->" + object);
                    Gson gson = new Gson();
                    NfcPrepaymentOrder preorder = gson.fromJson(object, NfcPrepaymentOrder.class);
                    if (preorder != null) {
                        if ("1".equals(preorder.getResult())) {// 1成功 -1失败
                            // 2需要补差价；
                            LeaveOrder order = new LeaveOrder();
                            order.setCarnumber(nfcorder.getCarnumber());
                            order.setTotal(collect);
                            ScanMyCodeCash(order);
                        } else if ("-1".equals(preorder.getResult())) {
                            nfcorder.setNetError(" -1  预付费结算失败");
                            showFinishFailDialog(nfcorder);
                        } else if ("2".equals(preorder.getResult())) {// 2需要补差价；
                            notBalanceDialog(preorder);
                        } else {
                            nfcorder.setNetError("预付费结算失败" + preorder.getResult());
                            showFinishFailDialog(nfcorder);
                        }
                    } else {
                        nfcorder.setNetError("预付费-解析错误" + object);
                        showFinishFailDialog(nfcorder);
                    }
                } else {
                    pd.dismiss();
                    if (status.getCode() == -101) {
                        nfcorder.setNetError(" -101：网络错误");
                        showFinishFailDialog(nfcorder);
                    } else if (status.getCode() == 500) {
                        nfcorder.setNetError(" 500：服务器错误");
                        showFinishFailDialog(nfcorder);
                    } else {
                        nfcorder.setNetError(" 000：网络请求错误");
                        showFinishFailDialog(nfcorder);
                    }
                }
            }
        });
    }

    // 微信预支付成功后的弹框；
    public void ScanMyCodeCash(LeaveOrder order) {
        EPayMessageDialog dialog = new EPayMessageDialog(this, R.style.nfcnewdialog, order, this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void showFinishFailDialog(NfcOrder order) {
        FinishOrderFailDialog failDialog = new FinishOrderFailDialog(this, R.style.nfcnewdialog, order, "nfc");
        failDialog.setCanceledOnTouchOutside(false);
        failDialog.show();
    }

    // 还需要补交现金的对话框；
    public void notBalanceDialog(NfcPrepaymentOrder preorder) {
        View open_dialog_view = View.inflate(this, R.layout.dialog_repayment_money_nfcorder, null);
        TextView tv_collect = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_collect);
        TextView tv_total = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_total);
        TextView tv_prefee = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_prefee);
        TextView tv_collect1 = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_collect1);
        Button bt_nfcorder_ok = (Button) open_dialog_view.findViewById(R.id.bt_dialog_repayment_nfcorder_ok);
        tv_collect.setText(preorder.getCollect() != null ? "还需向车主补收" + preorder.getCollect() + "元现金" : "");
        tv_collect1.setText(preorder.getCollect() != null ? preorder.getCollect() : "");
        tv_total.setText(preorder.getTotal() != null ? "停车费			" + preorder.getTotal() + "元" : "");
        tv_prefee.setText(preorder.getPrefee() != null ? "微信预付		" + preorder.getPrefee() + "元" : "");
        final Dialog openDialog = new Builder(this).create();
        bt_nfcorder_ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openDialog.dismiss();
                InputCarNumberActivity.this.finish();
            }
        });
        openDialog.setCancelable(false);
        openDialog.show();
        openDialog.setContentView(open_dialog_view);
    }

    public void showErrorDialog(String cade) {
        String title;
        String message;
        if (cade.equals("2")) {
            title = "车主余额不足";
            message = et_carnumber.getText().toString().trim() + "停车宝账户余额不足,无法完成自动支付,请通知车主手动支付或者收取现金！";
        } else if (cade.equals("-2")) {
            title = "相同的车牌号已存在订单";
            message = et_carnumber.getText().toString().trim() + "在本车场已存在订单！";
        } else {
            title = "速通卡用户未设置自动支付";
            message = et_carnumber.getText().toString().trim() + "用户没有设置自动支付！";
        }
        new AlertDialog.Builder(this).setIcon(R.drawable.app_icon_32).setTitle(title).setMessage(message)
                .setNegativeButton("知道了", null).setCancelable(false).create().show();
    }

    // 绑定车牌号（注册会员）1成功。-1车主未注册。其它情况失败
    // rgtype://是否重新绑定到其它车主,0或空：否，1:是 //dtype"是否删除原绑定的车主,0或空：否，1:是
    // nfchandle.do?action=reguser&uuid=0428C302773480&carnumber=***&comid=&uid=
    private void bindPlate(String uuid, String mobile, String rgtype, String dtype) throws UnsupportedEncodingException {
        String uid = getSharedPreferences("autologin", Context.MODE_PRIVATE).getString("account", "");
        String number = URLEncoder.encode(et_carnumber.getText().toString().trim(), "utf-8");
        final ProgressDialog dialog = ProgressDialog.show(this, "", "请稍后...", false, true);
        dialog.setCanceledOnTouchOutside(false);
        String urlpath = baseurl + "nfchandle.do?action=reguser" + "&uuid=" + uuid + "&carnumber="
                + URLEncoder.encode(number, "utf-8") + "&rgtype=" + rgtype + "&dtype=" + dtype + "&comid=" + comid
                + "&uid=" + uid + "&mobile=" + mobile;
        MyLog.w("InputCarNumberActivity", "绑定车牌号（注册会员）的url--->" + urlpath);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(urlpath, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && object != null) {
                    dialog.dismiss();
                    MyLog.i("InputCarNumberActivity", "绑定车主VIP卡的结果是--->" + object);
                    Gson gson = new Gson();
                    MakeVipInfo result = gson.fromJson(object, MakeVipInfo.class);
                    if (result != null && result.getResult() != null && result.getInfo() != null) {
                        if (result.getResult().equals("-1")) {
                            showFirstVipDialog();// 车主没有注册过需要填写手机号；
                        } else {
                            showResultDialog(result);
                        }
                    } else {
                        Toast.makeText(InputCarNumberActivity.this, object + ":綁定车主VIP卡失败！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    dialog.dismiss();
                    switch (status.getCode()) {
                        case -101:
                            Toast.makeText(InputCarNumberActivity.this, object + "网络错误！--请再次点击绑定车牌！", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(InputCarNumberActivity.this, object + "服务器错误！--请再次点击绑定车牌！", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }

    // collectorrequest.do?action=reguser&token=6ed161cde6c7149de49d72719f2eb39b&mobile=15801482645&carnumber=123456
    // 只有一个carnumber参数时，是验证车牌号,0车牌为空，-1已存在车牌，1可以注册，
    // 两个参数都有时，验证手机号，手机号已存在时返回-1，不存在时，注册车主,成功返回1，不成功返回-2，-1手机号已存在 ，1注册成功，-2注册不成功
    // 返回：0车牌号不对，-1车牌号已存在(mobile为空时)，手机号已存在（mobile不空时）,1注册成功，-2注册不成功

    public void registerVip(final String mobile) throws UnsupportedEncodingException {

        String number = URLEncoder.encode(et_carnumber.getText().toString().trim(), "utf-8");
        final ProgressDialog dialog = ProgressDialog.show(this, "", "请稍后...", false, true);
        dialog.setCanceledOnTouchOutside(false);
        String urlpath = baseurl + "collectorrequest.do?action=reguser&token=" + token + "&mobile=" + mobile
                + "&carnumber=" + URLEncoder.encode(number, "utf-8");

        MyLog.w("InputCarNumberActivity", "注册普通会员的url--->" + urlpath);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(urlpath, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && object != null) {
                    dialog.dismiss();
                    MyLog.i("InputCarNumberActivity", "注册普通会员的结果是--->" + object);
                    if ("".equals(mobile)) {
                        if (object.equals("0")) {// 车牌号为空
                            Toast.makeText(InputCarNumberActivity.this, "车牌号不完整---请重新输入！", Toast.LENGTH_SHORT).show();
                        } else if (object.equals("1")) {
                            showEidtAsMobile();
                        } else if (object.equals("-1")) {// 车牌号已存在
                            // Toast.makeText(InputCarNumberActivity.this,
                            // "该车牌号已注册过，修改请联系停车宝客服！", 1).show();
                            ShowNoRegisterDialog();
                        }
                    } else {
                        if (object.equals("1")) {
                            showSuccessDialog(mobile);
                        } else if (object.equals("-1")) {// 手机号已存在
                            Toast.makeText(InputCarNumberActivity.this, "该手机号已经注册过车主！", 1).show();
                        } else if (object.equals("-2")) {// 注册失败
                            Toast.makeText(InputCarNumberActivity.this, "注册失败，请重试！", 1).show();
                        }
                    }
                } else {
                    dialog.dismiss();
                    switch (status.getCode()) {
                        case -101:
                            Toast.makeText(InputCarNumberActivity.this, object + "网络错误！--请再次点击开通会员！", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(InputCarNumberActivity.this, object + "服务器错误！--请再次点击开通会员！", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });

    }

    public void ShowNoRegisterDialog() {
        new AlertDialog.Builder(this).setTitle(et_carnumber.getText().toString().trim() + "已注册过！")
                .setIcon(R.drawable.app_icon_32).setMessage("请车主直接登录，有疑问请联系客服010-56450585")
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                }).setCancelable(false).show();
    }

    public void showEidtAsMobile() {
        final View inputVeiw = View.inflate(this, R.layout.input_mobile_dailog, null);
        new AlertDialog.Builder(this).setTitle(et_carnumber.getText().toString().trim() + "还未注册").setIcon(R.drawable.app_icon_32)
                .setMessage("输入车主手机号,给车主发送下载链接,完成注册。").setView(inputVeiw)
                .setPositiveButton("开通会员", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        EditText et_mobile = (EditText) inputVeiw.findViewById(R.id.et_input_mobile);
                        boolean isMatched = CheckUtils.MobileChecked(et_mobile.getText().toString().trim());
                        if (isMatched) {
                            try {
                                registerVip(et_mobile.getText().toString());
                            } catch (UnsupportedEncodingException e1) {
                                e1.printStackTrace();
                                Toast.makeText(InputCarNumberActivity.this, "开通VIP卡失败,车牌字符转码异常！", Toast.LENGTH_SHORT).show();
                            }
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(InputCarNumberActivity.this, "手机号输入有误！", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).setCancelable(false).show();
    }

    public void showSuccessDialog(String mobile) {
        AlertDialog dialog = new AlertDialog.Builder(this).setPositiveButton("对方已收到短信", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                InputCarNumberActivity.this.finish();
            }
        }).setNegativeButton("对方未收到短信？", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                showQrCodeDialog();
            }
        }).create();
        dialog.setTitle("开通成功，等待车主安装注册");
        dialog.setMessage("已向车主" + mobile + "发送下载短信。车主注册成功后，会给您的账户奖励5元。");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void showFirstVipDialog() {
        final View inputVeiw = View.inflate(this, R.layout.input_mobile_dailog, null);
        new AlertDialog.Builder(this).setTitle(et_carnumber.getText().toString().trim() + "还未注册").setIcon(R.drawable.app_icon_32)
                .setMessage(et_carnumber.getText().toString().trim() + " 还没有注册,输入车主手机号,立刻开通VIP会员卡。").setView(inputVeiw)
                .setPositiveButton("开通会员", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        EditText et_mobile = (EditText) inputVeiw.findViewById(R.id.et_input_mobile);
                        boolean isMatched = CheckUtils.MobileChecked(et_mobile.getText().toString().trim());
                        if (isMatched) {
                            try {
                                bindPlate(nfcuid, et_mobile.getText().toString(), "0", "0");
                            } catch (UnsupportedEncodingException e1) {
                                e1.printStackTrace();
                                Toast.makeText(InputCarNumberActivity.this, "开通VIP卡失败,车牌字符转码异常！", Toast.LENGTH_SHORT).show();
                            }
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                                field.setAccessible(true);
                                field.set(dialog, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(InputCarNumberActivity.this, "手机号输入有误！", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).setCancelable(false).show();
    }

    private void showResultDialog(final MakeVipInfo info) {
        AlertDialog dialog = new AlertDialog.Builder(this).setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (info.getResult().equals("1") || info.getResult().equals("-3")) {// 成功，nfc卡未注册
                    InputCarNumberActivity.this.finish();
                } else if (info.getResult().equals("-2")) {// 车主已绑定过，是否重新绑定后，原绑定无效
                    try {
                        bindPlate(nfcuid, "", "0", "1");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        Toast.makeText(InputCarNumberActivity.this, "开通VIP卡失败,车牌字符转码异常！", Toast.LENGTH_SHORT).show();
                    }
                } else if (info.getResult().equals("-4")) {// NFC已绑定过车主京A44886，是否重新绑定新车主
                    try {
                        bindPlate(nfcuid, "", "1", "0");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        Toast.makeText(InputCarNumberActivity.this, "开通VIP卡失败,车牌字符转码异常！", Toast.LENGTH_SHORT).show();
                    }
                } else if (info.getResult().equals("-5")) {// 绑定失败，请稍候重试

                } else if (info.getResult().equals("-6")) {// 绑定失败，此卡与车主已经绑定
                    InputCarNumberActivity.this.finish();
                } else if (info.getResult().equals("-7")) {// 绑定失败，此卡与车主已经绑定
                    InputCarNumberActivity.this.finish();
                } else {
                    InputCarNumberActivity.this.finish();
                }
            }
        }).create();
        dialog.setTitle("VIP会员开卡");
        dialog.setMessage(info.getInfo());
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @SuppressWarnings("deprecation")
    private void showQrCodeDialog() {
        AlertDialog qrCodeDialog = new AlertDialog.Builder(this).create();
        ImageView qrCode = new ImageView(this);
        qrCode.setScaleType(ScaleType.FIT_XY);
        qrCode.setImageResource(R.drawable.img_qrcode_user);
        qrCodeDialog.setView(qrCode);
        qrCodeDialog.setCancelable(true);
        qrCodeDialog.setCanceledOnTouchOutside(false);
        qrCodeDialog.setButton("让车主扫一扫也可下载", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                InputCarNumberActivity.this.finish();
            }
        });
        qrCodeDialog.show();
    }

    public void hideKeyBoard() {
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void et_input_et_hand_input(View view) {
        InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mInputMethodManager.isActive()) {
            mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
