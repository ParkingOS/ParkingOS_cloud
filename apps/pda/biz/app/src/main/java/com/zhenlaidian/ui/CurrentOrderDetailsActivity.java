package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.ImgAdapter;
import com.zhenlaidian.bean.AllOrder;
import com.zhenlaidian.bean.BaseResponse;
import com.zhenlaidian.bean.CarTypeItem;
import com.zhenlaidian.bean.ZhuiJiaoItemEntity;
import com.zhenlaidian.bean.ZhuiJiaoListEntity;
import com.zhenlaidian.camera.CameraActivity;
import com.zhenlaidian.engine.GetParkPosition;
import com.zhenlaidian.engine.SelectParkingPositionDialog;
import com.zhenlaidian.photo.InputCarNumberActivity;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.ui.dialog.MonthNumberDialog;
import com.zhenlaidian.ui.fragment.FragmentShowIMG;
import com.zhenlaidian.util.CameraBitmapUtil;
import com.zhenlaidian.util.CheckUtils;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.Constant;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;
import com.zhenlaidian.util.VoiceSynthesizerUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 当前订单详情;
 */
@SuppressLint("SimpleDateFormat")
public class CurrentOrderDetailsActivity extends BaseActivity implements GetParkPosition {

    private TextView tv_money;
    private TextView tv_carNumber;
    private TextView tv_parkingTime;
    private TextView tv_call;
    private TextView tv_orderNumber;
    private TextView tv_orderStatus;
    private TextView tv_inTime;
    private TextView tv_parking_position;//选择车位
    private RelativeLayout rl_look_carimg;
    private String orderid;//订单号
//    private String duration;//时长
    private AllOrder order;
    private Button bt_delete;//删除订单
    private Button bt_cash_up;//现金结算
//    private SharedPreferences autologin;
//    private String role;
    private String ismonthuser;
    private ImageView monthnumber;
    private TextView txtduration;
    private String iscard;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.activity_current_order_details);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        orderid = getIntent().getExtras().getString("orderid");
//        duration = getIntent().getExtras().getString("duration");
//        autologin = getSharedPreferences("autologin", Context.MODE_PRIVATE);
//        role = autologin.getString("role", "0");
        ismonthuser = getIntent().getStringExtra("ismonthuser");
        iscard = getIntent().getStringExtra("iscard");
        initView();
        getOrderinfo();

        voice = new VoiceSynthesizerUtil(context);
        photonum1 = SharedPreferencesUtils.getIntance(CurrentOrderDetailsActivity.this).getphotoset(1);
        photonum2 = SharedPreferencesUtils.getIntance(CurrentOrderDetailsActivity.this).getphotoset(2);
    }

    private TextView tv_current_order_details_pre;
    private RelativeLayout ln_orderdetail_vp;
    private ViewPager vp_orderdetail;
    private ImageView po1, po2, po3;
    private Button btnfra;
    private ImageView bondcard;

    public void initView() {
        tv_money = (TextView) findViewById(R.id.tv_current_order_details_money);
        tv_parking_position = (TextView) findViewById(R.id.tv_current_order_details_position);
        tv_carNumber = (TextView) findViewById(R.id.tv_current_order_details_carNumber);
        tv_parkingTime = (TextView) findViewById(R.id.tv_current_order_details_parkingTime);
        tv_call = (TextView) findViewById(R.id.tv_current_order_details_call);
        tv_orderNumber = (TextView) findViewById(R.id.tv_current_order_details_orderNumber);
        tv_orderStatus = (TextView) findViewById(R.id.tv_current_order_details_orderStatus);
        tv_inTime = (TextView) findViewById(R.id.tv_current_order_details_inTime);
        bt_delete = (Button) findViewById(R.id.bt_current_order_delete);
        bt_cash_up = (Button) findViewById(R.id.bt_current_order_cash_up);
        rl_look_carimg = (RelativeLayout) findViewById(R.id.rl_current_order_look_carimg);
        monthnumber = ((ImageView) findViewById(R.id.monthnumber));
        bondcard = ((ImageView) findViewById(R.id.bondcard));
        txtduration = ((TextView) findViewById(R.id.tv_current_order_details_duration));
        tv_current_order_details_pre = ((TextView) findViewById(R.id.tv_current_order_details_pre));
        vp_orderdetail = ((ViewPager) findViewById(R.id.vp_orderdetail));
        ln_orderdetail_vp = ((RelativeLayout) findViewById(R.id.ln_orderdetail_vp));
        po1 = ((ImageView) findViewById(R.id.gallery_p1));
        po2 = ((ImageView) findViewById(R.id.gallery_p2));
        po3 = ((ImageView) findViewById(R.id.gallery_p3));
        btnfra = ((Button) findViewById(R.id.fragment_btn));
        btnfra.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ln_orderdetail_vp.setVisibility(View.GONE);
            }
        });
    }

    @SuppressLint("DefaultLocale")
    public void setView() {
        if (ismonthuser.equals("5")) {
            monthnumber.setVisibility(View.VISIBLE);
        } else {
            monthnumber.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(iscard) && iscard.equals("1")) {
            bondcard.setVisibility(View.VISIBLE);
        } else {
            bondcard.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(order.getPark())) {
            tv_parking_position.setText("车位:" + order.getPark());

        } else {
            tv_parking_position.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //给订单选择车位:
                    SelectParkingPositionDialog dialog = new SelectParkingPositionDialog(
                            CurrentOrderDetailsActivity.this, order.getCarnumber(), orderid, CurrentOrderDetailsActivity.this);
                    dialog.show();
                }
            });
        }
        if (order.getTotal() != null) {
            try {
                double d = Double.parseDouble(order.getTotal());
                String result = String.format("%.2f", d);
                tv_money.setText(result);
            } catch (Exception e) {
                tv_money.setText(order.getTotal());
            }
        }
        if (!TextUtils.isEmpty(order.getCarnumber()) && !order.getCarnumber().equals("null") && !order.getCarnumber().equals("车牌号未知")) {
            tv_carNumber.setText(order.getCarnumber());
            try {
                getParkInfo(order.getCarnumber(), false);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            tv_carNumber.setText("点击添加车牌");
            tv_carNumber.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CurrentOrderDetailsActivity.this, InputCarNumberActivity.class);
                    intent.putExtra("add", "add");
                    intent.putExtra("orderid", orderid);
                    startActivity(intent);
                    CurrentOrderDetailsActivity.this.finish();
                }
            });
        }
        txtduration.setText(order.getDuration() + "");
        if (order.getOrderid() != null) {
            tv_orderNumber.setText(order.getOrderid());
        }
        if (order.getState() != null) {
            tv_orderStatus.setText(order.getState());
        }
        if (order.getBegin() != null) {
//            tv_inTime.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(order.getBegin()) * 1000)));
            tv_inTime.setText(CommontUtils.Unix2Time(order.getBegin()));
        }
        if (order.getPrepay() != null) {
            tv_current_order_details_pre.setText(order.getPrepay());
        }
        tv_call.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (order.getMobile() != null && CheckUtils.MobileChecked(order.getMobile())) {
                    Intent phoneintent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + order.getMobile()));
                    startActivity(phoneintent);
                } else {
                    Toast.makeText(CurrentOrderDetailsActivity.this, "电话号码为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        bt_delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 置为逃单
                //补打进场小票
                print();
            }
        });

        bt_cash_up.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 现金结算订单
                if (order != null) {
                    payposorder(order.getOrderid(), order.getTotal(), order.getEnd());
//                    getOrderinfo(true);
                }
                if (ismonthuser.equals("5")) {
//                    cashOrder();

                    voice.playText("此车为月卡用户");
                }
            }
        });
        rl_look_carimg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击查看车辆图片;
                String SDState = Environment.getExternalStorageState();
                if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                    File file = new File(Environment.getExternalStorageDirectory() + "/TingCheBao", order.getOrderid() + "in0.jpeg");
                    if (file.exists()) {
//                        showMaxImgDialog(file);
//                        showGalleryDialog();
                        ShowImgFragment();
                    } else {
                        Toast.makeText(CurrentOrderDetailsActivity.this, "文件不存在!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CurrentOrderDetailsActivity.this, "内存卡不存在!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        car_type = order.getCar_type();
        if (!TextUtils.isEmpty(car_type)) {
            String car_typejson = getStringFromPreference("car_type");
            if (!TextUtils.isEmpty(car_typejson)) {
                Gson gson = new Gson();
                ArrayList<CarTypeItem> car_typeArr = gson.fromJson(car_typejson, new TypeToken<ArrayList<CarTypeItem>>() {
                }.getType());
                if (car_typeArr != null && car_typeArr.size() > 0) {
                    for (CarTypeItem item : car_typeArr) {
                        if (item.getId().equals(car_type)) {
                            tv_call.setText("" + item.getName());
                            tv_call.setOnClickListener(null);
                        }
                    }
                }
            }
        }
    }

    String car_type;
    private VoiceSynthesizerUtil voice;

    public void ShowImgFragment() {
        ln_orderdetail_vp.setVisibility(View.VISIBLE);
        listFra.clear();
        ImgAdapter adapter = new ImgAdapter(getSupportFragmentManager(), listFra, context);
        for (int i = 0; i < SharedPreferencesUtils.getIntance(CurrentOrderDetailsActivity.this).getphotoset(0); i++) {
            File file = new File(Environment.getExternalStorageDirectory() + "/TingCheBao", order.getOrderid() + "in" + i + ".jpeg");
            if (file.exists()) {
                MyLog.i("CurrentOrderDetailsActivity", "file：" + file.getAbsolutePath());
                Fragment fragment = new FragmentShowIMG();
                Bundle b = new Bundle();
                b.putString("path", "file://" + file.getAbsolutePath());
                fragment.setArguments(b);
                listFra.add(fragment);
            } else {
//                  Toast.makeText(CurrentOrderDetailsActivity.this, "文件不存在!", Toast.LENGTH_SHORT).show();
            }
        }
        vp_orderdetail.setAdapter(adapter);
        setPoint(listFra.size(), 0);
        vp_orderdetail.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setPoint(listFra.size(), position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setPoint(int count, int index) {
        switch (count) {
            case 1:
                po1.setVisibility(View.VISIBLE);
                po2.setVisibility(View.GONE);
                po3.setVisibility(View.GONE);
                po1.setBackgroundResource(R.drawable.splash_focus);
                break;
            case 2:
                po1.setVisibility(View.VISIBLE);
                po2.setVisibility(View.VISIBLE);
                po3.setVisibility(View.GONE);
                switch (index) {
                    case 0:
                        po1.setBackgroundResource(R.drawable.splash_focus);
                        po2.setBackgroundResource(R.drawable.splash_blur);
                        break;
                    case 1:
                        po1.setBackgroundResource(R.drawable.splash_blur);
                        po2.setBackgroundResource(R.drawable.splash_focus);
                        break;
                }
                break;
            case 3:
                po1.setVisibility(View.VISIBLE);
                po2.setVisibility(View.VISIBLE);
                po3.setVisibility(View.VISIBLE);
                switch (index) {
                    case 0:
                        po1.setBackgroundResource(R.drawable.splash_focus);
                        po2.setBackgroundResource(R.drawable.splash_blur);
                        po3.setBackgroundResource(R.drawable.splash_blur);
                        break;
                    case 1:
                        po1.setBackgroundResource(R.drawable.splash_blur);
                        po2.setBackgroundResource(R.drawable.splash_focus);
                        po3.setBackgroundResource(R.drawable.splash_blur);
                        break;
                    case 2:
                        po1.setBackgroundResource(R.drawable.splash_blur);
                        po2.setBackgroundResource(R.drawable.splash_blur);
                        po3.setBackgroundResource(R.drawable.splash_focus);
                        break;
                }
                break;
        }
    }

    private List<Fragment> listFra = new ArrayList<Fragment>();

    //collectorrequest.do?action=orderdetail&token=&orderid="
    public void getOrderinfo() {
        // 从服务器获取数据.设置到界面上;
        AQuery aQuery = new AQuery(CurrentOrderDetailsActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=orderdetail&token=" + token
                + "&orderid=" + orderid
                + "&brethorderid=" + getStringFromPreference("berthorderid")
                + "&out=json";
        MyLog.i("CurrentOrderDetailsActivity", "url>>>>>" + url);
        if (IsNetWork.IsHaveInternet(this)) {
            final ProgressDialog dialog = ProgressDialog.show(this, "加载中...",
                    "获取当前订单数据...", true, true);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (!TextUtils.isEmpty(object)) {
                        dialog.dismiss();
                        MyLog.i("CurrentOrderDetailsActivity", "返回的当前订单详情：" + object);
                        Gson gson = new Gson();
                        order = gson.fromJson(object, AllOrder.class);
                        MyLog.i("CurrentOrderDetailsActivity", "解析的当前订单详情：" + order.toString());
                        if (order != null && !TextUtils.isEmpty(order.getOrderid())) {
                            setView();
                        } else {
                            Toast.makeText(getApplicationContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        dialog.dismiss();
                        return;
                    }
                }

            });
        } else {
            Toast.makeText(this, "请检查网络!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 5、payposorder.do
     * 月卡用户直接结算，余额用户判断余额和是否可自动支付，支付成功时，收费员和车主应该收到消息，
     * 车主消息是个推推送的。支付失败后，到下一个页面，选择现金或扫码支付，现金支付要调用ordercash.do，这个接口不处理现金结算
     */
    public void payposorder(String orderid, String total, String endtime) {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取订单失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }
        AQuery aQuery = new AQuery(this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=payposorder&token=" + token
                + "&orderid=" + orderid + "&total=" + total + "&imei=" + imei
                + "&workid=" + SharedPreferencesUtils.getIntance(this).getworkid()
                + "&berthorderid=" + getStringFromPreference("berthorderid") + "&ismonthuser=" + ismonthuser + "&out=json"
                + "&endtime=" + endtime + "&version=" + CommontUtils.getVersion(CurrentOrderDetailsActivity.this);
        MyLog.w("payposorder的URl-->>", url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "提交结算数据...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            public void callback(String url, String object, AjaxStatus status) {
                MyLog.d("CurrentOrderDetailsActivity", "payposorder结算返回的结果是-->>" + object);
                if (object != null) {
                    dialog.dismiss();
                    //1.3.4之前的逻辑
//                    result:1 月卡支付成功  2余额支付成功，3 预收金额可以支付，直接调用ordercash.do完成订单结算
//                    {result:3,errmsg:预收金额可以支付};
//                    {result:2,errmsg:预收金额：1元，余额支付：9元};
//                    {result:1,errmsg:月卡支付成功};
//                    {result:0,errmsg:结算失败，订单不存在 };
//                    {result:-1,errmsg:超出自动支付限额：+limitMoney+元}
//                    {result:-2,errmsg:车主余额不足};
//                    {result:-3,errmsg:车主未注册};
//                    {result:-4,errmsg:车主余额支付失败};
                    //1.3.5以后的逻辑
                    //-1:结算失败
                    //1：月卡结算成功
                    //2：电子支付成功
                    //3：现金预支付（金额足够）或者0元结算成功
                    //4：刷卡支付成功
                    //
                    Gson gson = new Gson();
                    BaseResponse response = gson.fromJson(object, BaseResponse.class);
                    MyLog.i("CurrentOrderDetailsActivity", "payposorder返回结果！" + response.getResult());
                    if (response.getResult().equals("1")) {
                        putStringToPreference("berthorderid", "succeed");
                        MonthNumberDialog dialog = new MonthNumberDialog(CurrentOrderDetailsActivity.this,
                                R.style.nfcfinishdialog, order.getDuration(), order.getCarnumber(), h);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);
                        dialog.show();
                    } else if (response.getResult().equals("2")) {
                        putStringToPreference("berthorderid", "succeed");
                        if (CommontUtils.checkString(response.getErrmsg())) {
                            voice.playText("" + response.getErrmsg());
                            new AlertDialog.Builder(CurrentOrderDetailsActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                    .setMessage(response.getErrmsg()).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    prient("临时停车", "余额支付");
                                }
                            }).setCancelable(false).create().show();
                        }
                    } else if (response.getResult().equals("3")) {
                        if (CommontUtils.checkString(response.getErrmsg())) {
                            voice.playText("结算订单" + order.getTotal() + "元");
                            new AlertDialog.Builder(CurrentOrderDetailsActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                    .setMessage(response.getErrmsg()).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (photonum1 > 0) {
                                        Intent i = new Intent(CurrentOrderDetailsActivity.this, CameraActivity.class);
                                        i.putExtra("num", photonum1);
                                        startActivityForResult(i, Constant.BACK_FROM_CAMERA_OUT);
                                    } else {
                                        if (PullMsgService.CanPrint) {
                                            prient("临时停车", "现金支付");
                                        } else {
                                            try {
                                                getParkInfo(order.getCarnumber(), true);
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }).setCancelable(false).create().show();
                        }
                    } else if (response.getResult().equals("4")) {
                        if (CommontUtils.checkString(response.getErrmsg())) {
                            voice.playText("" + response.getErrmsg());
                            new AlertDialog.Builder(CurrentOrderDetailsActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                    .setMessage(response.getErrmsg()).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    prient("临时停车", "刷卡支付");
                                }
                            }).setCancelable(false).create().show();
                        }
                    } else {
                        if (SharedPreferencesUtils.getIntance(context).getprint_order_place2() == 1) {
                            print();
                        }
                        Intent i = new Intent(context, OrderJieSuanActivity.class);
                        i.putExtra("detail", order);
                        startActivity(i);
                        finish();
                    }
                } else {
                    dialog.dismiss();
                    return;
                }
            }
        });
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                CurrentOrderDetailsActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //GetParkPosition接口回调,得到绑定的车位,设置车位不可点击;
    @Override
    public void getParkPosition(String position) {
        tv_parking_position.setText("车位:" + position);
        tv_parking_position.setClickable(false);
    }

    private void print() {
        String Sname="";
        String gang = "";
        if (SharedPreferencesUtils.getIntance(this).getisprintName()) {
            Sname = getStringFromPreference("name");
            gang = "-";
        }
        String printstr = "***********订单明细联**********\n\n" +
                SharedPreferencesUtils.getIntance(context).getprint_signInHead() + "\n" +
                "停车场：" + SharedPreferencesUtils.getIntance(context).getParkname() +
                "—" + SharedPreferencesUtils.getIntance(context).getberthsec_name() + "\n" +
                "收费员：" + Sname;
        if (CommontUtils.Is910()) {
            if (!TextUtils.isEmpty(SharedPreferencesUtils.getIntance(context).getmobile())) {
                printstr += "(" + SharedPreferencesUtils.getIntance(context).getmobile() + ")";
            }
        }
        printstr += gang + useraccount;
        String stoptype = "临时停车";
        if (ismonthuser.equals("5")) {
            stoptype = "月卡用户";
        }
        printstr += "\n" + "车位：" + order.getPark() + "\n" +
                "车牌号：" + order.getCarnumber() + "\n" +
                "停车类型：" + stoptype + "\n" +
                "进场时间：" + CommontUtils.Unix2TimeS(order.getBegin()) + "\n" +
                "停车时长：" + order.getDuration() + "\n" +
                "停车费用：" + order.getTotal() + "元\n" +
                "预收金额：" + order.getPrepay() + "元\n" +
                Constant.FOOT +
                SharedPreferencesUtils.getIntance(context).getprint_signIn() + "\n\n\n\n\n";
        PullMsgService.sendMessage(printstr, context);
    }


    private void FinishAction() {
        setResult(RESULT_OK);
        finish();
    }

    private Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1000:
                    prient("月卡用户", "");
                    break;
            }
        }
    };

    //打印凭条
    public void prient(String parktype, String paytype) {
        //将已处理的车位号存入，回到泊位列表时移除这个车位号消息

        double payactual = 0;
        if (CommontUtils.checkString(order.getTotal()) && CommontUtils.checkString(order.getPrepay())) {
            payactual = CommontUtils.doubleTwoPoint(Double.parseDouble(order.getTotal()) - Double.parseDouble(order.getPrepay()));
        }
        String pay;
        if (payactual < 0) {
            pay = "应退：" + (-payactual) + "元";
        } else {
            pay = "出场缴费：" + payactual + "元";
        }
        String Sname;
        String gang = "-";
        if (SharedPreferencesUtils.getIntance(this).getisprintName()) {
            Sname = getStringFromPreference("name");
        } else {
            Sname = "";
            gang = "";
        }
        String str = Constant.HEADOut +
//                "收费单位：" + SharedPreferencesUtils.getIntance(context).getprint_signInHead() + "\n" +
                SharedPreferencesUtils.getIntance(context).getprint_signOutHead() + "\n" +
                "停车场：" + SharedPreferencesUtils.getIntance(context).getParkname() +
                "—" + SharedPreferencesUtils.getIntance(context).getberthsec_name() + "\n" +
                "收费员：" + Sname;
        if (CommontUtils.Is910()) {
            if (!TextUtils.isEmpty(SharedPreferencesUtils.getIntance(context).getmobile())) {
                str += "(" + SharedPreferencesUtils.getIntance(context).getmobile() + ")";
            }
        }
        str += gang + useraccount;
        str += "\n" + "车位：" + order.getPark() + "\n" +
                "车牌号：" + order.getCarnumber() + "\n" +
                "停车类型：" + parktype + "\n" +
                "进场时间：" + CommontUtils.Unix2TimeS(order.getBegin()) + "\n" +
                "出场时间：" + CommontUtils.Unix2TimeS(order.getEnd()) + "\n" +
                "停车时长：" + order.getDuration() + "\n";

        if (!parktype.equals("月卡用户")) {
            str += "支付方式：" + paytype + "\n" +
                    "订单金额：" + order.getTotal() + "元\n" +
                    "预缴金额：" + order.getPrepay() + "元\n" +
                    pay + "\n\n";
        }
        str += Constant.FOOT + SharedPreferencesUtils.getIntance(context).getprint_signOut() + "\n\n\n\n\n";
        if (!ismonthuser.equals("5")) {
            if (payactual != 0) {
                PullMsgService.sendMessage(str, context);
            } else {
                if (Double.parseDouble(order.getPrepay()) > 0) {
                    PullMsgService.sendMessage(str, context);
                }
            }
        }
//        sendMessage(qrbitmap);

        try {
            getParkInfo(order.getCarnumber(), true);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查逃单
     * //http://127.0.0.1/zld/collectorrequest.do?
     * action=getecsorder&token=ca67649c7a6c023e08b0357658c08c3d&car_number=
     */
    private ZhuiJiaoListEntity listEntity = new ZhuiJiaoListEntity();
    private ArrayList<ZhuiJiaoItemEntity> entity = new ArrayList<ZhuiJiaoItemEntity>();

    public void getParkInfo(String carnumber, final boolean needjump) throws UnsupportedEncodingException {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取信息失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }
        String car = URLEncoder.encode(carnumber, "utf-8");
        AQuery aQuery = new AQuery(CurrentOrderDetailsActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=getecsorder&token="
                + token + "&car_number=" + URLEncoder.encode(car, "utf-8") + "&berthid=" +
                SharedPreferencesUtils.getIntance(this).getberthid() + "&out=json";
        MyLog.w("InputCarNumberActivity-->>", "检查逃单的URL-->>" + url);
//        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取车场信息数据...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (object != null) {
                    Gson gson = new Gson();
                    listEntity = gson.fromJson(object, ZhuiJiaoListEntity.class);
                    MyLog.i("ParkingInfoActivity-->>", "解析的逃单" + listEntity.toString());
                    if (needjump) {
                        //结算完订单后查询逃单
                        if (listEntity.getResult().equals("0")) {
                            //有逃单，跳转到追缴界面
                            if (getBooleanFromPreference("next")) {
                                //如果已标记下次缴费，走正常流程
                                FinishAction2();
                            } else {
                                entity = listEntity.getOrders();
                                Intent i = new Intent(context, ZhuiJiaoListActivity.class);
                                putStringToPreference("carnumber", order.getCarnumber());
                                i.putExtra("list", entity);
                                i.putExtra("from", "jiesuan");
                                startActivityForResult(i, Constant.BACK_FROM_OWE);
                            }
                        } else {
                            //没有逃单情况，走正常流程
                            FinishAction2();
                        }
                    } else {
                        //进入页面即查询逃单
                        double total = 0;
                        double prepay = 0;
                        entity = listEntity.getOrders();
                        if (entity != null && entity.size() > 0) {
                            entity = listEntity.getOrders();
                            for (int i = 0; i < entity.size(); i++) {
                                total += Double.parseDouble(entity.get(i).getTotal());
                                prepay += Double.parseDouble(entity.get(i).getPrepay());
                            }
                            tv_parkingTime.setText("欠费：" + CommontUtils.doubleTwoPoint(total - prepay) + "元");
                        }
                    }
                } else {
                    if (needjump) {
                        FinishAction2();
                    }

                }
            }

        });
    }

    private void FinishAction2() {
        putBooleanToPreference("next", false);
        putStringToPreference("boweistate", order.getBerthnumber());
        setResult(RESULT_OK);
        finish();
    }

    private int photonum1, photonum2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyLog.i("INCarDialogActivity", "onActivityResult-------->");
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constant.BACK_FROM_OWE:
                    FinishAction();
                    break;
                case Constant.BACK_FROM_CAMERA_OUT:
                    listPath = data.getStringArrayListExtra("list");
                    if (CommontUtils.checkList(listPath)) {
                        for (int i = 0; i < listPath.size(); i++) {
                            String SDState = Environment.getExternalStorageState();
                            if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                                File dir = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                (new File(listPath.get(i))).renameTo(new File(dir.getAbsolutePath(), order.getOrderid() + "out" + i + ".jpeg"));
                                CameraBitmapUtil.upload(context, i, order.getOrderid(), 1);
                            }
                        }
                    }
                    if (PullMsgService.CanPrint) {
                        prient("临时停车", "现金支付");
                    } else {
                        try {
                            getParkInfo(order.getCarnumber(), true);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

            }

        }
    }

    private ArrayList<String> listPath = new ArrayList<String>();
}
