package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.AllOrder;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.TimeTypeUtil;

import java.util.Date;

/**
 * 逃单详情界面;
 */
public class LostOrderRecordDetailsActivity extends BaseActivity {

    private TextView tv_money;
    private TextView tv_plateNumber;
    private TextView tv_parkingTime;
    private TextView tv_call;
    private TextView tv_orderNumber;
    //	private TextView tv_orderStatus;
    private TextView tv_inTime;
    private TextView tv_outTime;
    private Button bt_delete_order;
    private Button bt_change_cash;
    private String orderid;
    private AllOrder order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.lost_order_record_details_activity);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        orderid = (String) getIntent().getExtras().get("orderid");
        initView();
        getOrderInfo();
    }

    public void initView() {
        tv_money = (TextView) findViewById(R.id.tv_lost_order_details_money);
        tv_plateNumber = (TextView) findViewById(R.id.tv_lost_order_details_carNumber);
        tv_parkingTime = (TextView) findViewById(R.id.tv_lost_order_details_parkingTime);
        tv_call = (TextView) findViewById(R.id.tv_lost_order_details_call);
        tv_orderNumber = (TextView) findViewById(R.id.tv_lost_order_details_orderNumber);
//		tv_orderStatus = (TextView) findViewById(R.id.tv_lost_order_details_orderStatus);
        tv_inTime = (TextView) findViewById(R.id.tv_lost_order_details_inTime);
        tv_outTime = (TextView) findViewById(R.id.tv_lost_order_details_outTime);
        bt_delete_order = (Button) findViewById(R.id.bt_lost_order_details_delete);
        bt_change_cash = (Button) findViewById(R.id.bt_lost_order_details_cash_up);
    }

    @SuppressLint("SimpleDateFormat")
    public void setView(final AllOrder order) {

        if (!TextUtils.isEmpty(order.getCarnumber())) {
            tv_plateNumber.setText(order.getCarnumber());
        } else {
            tv_plateNumber.setText("车牌号未知");
        }
        if (order.getEnd() != null && order.getBegin() != null) {
            long endtime = Long.parseLong(order.getEnd());
            long begintime = Long.parseLong(order.getBegin());
            String time = TimeTypeUtil.getTimeString(begintime, endtime);
            tv_parkingTime.setText("停车" + time);
            tv_inTime.setText((new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                    .format(new Date(begintime * 1000)));
            tv_outTime.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new Date(endtime * 1000)));
            tv_orderNumber.setText(order.getOrderid());
        }
        tv_call.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (order.getMobile() != null && !order.getMobile().equals("null")) {
                    Intent phoneintent = new Intent(
                            "android.intent.action.CALL", Uri.parse("tel:" + order.getMobile()));
                    startActivity(phoneintent);
                } else {
                    Toast.makeText(LostOrderRecordDetailsActivity.this, "电话号码为空", 0).show();
                }
            }
        });
        tv_money.setText(order.getTotal());
        bt_delete_order.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 删除逃单
                SharedPreferences autologin = getSharedPreferences("autologin", Context.MODE_PRIVATE);
                String role = autologin.getString("role", "0");
                if (role != null && role.equals("1")) {
                    cashLostOrder(order.getOrderid());
                } else {
                    Toast.makeText(LostOrderRecordDetailsActivity.this, "请用管理员账号登录！", 0).show();
                }
            }
        });
        bt_change_cash.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 调价并结算逃单
                Intent intent = new Intent(LostOrderRecordDetailsActivity.this, PriceChangesAndCash.class);
                intent.putExtra("allorder", order);
                startActivity(intent);
                LostOrderRecordDetailsActivity.this.finish();
            }
        });
    }

    public void getOrderInfo() {
        AQuery aQuery = new AQuery(LostOrderRecordDetailsActivity.this);
        String path = baseurl;
        // collectorrequest.do?action=orderdetail&token=*&orderid=*
        String url = path + "collectorrequest.do?action=orderdetail&token=" + token
                + "&orderid=" + orderid + "&out=json";
        MyLog.w("LostOrderRecordDetailsActivity", "url>>>>>" + url);
        if (IsNetWork.IsHaveInternet(this)) {
            final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取历史订单数据...", true, true);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (!TextUtils.isEmpty(object)) {
                        dialog.dismiss();
                        Gson gson = new Gson();
                        AllOrder order = gson.fromJson(object, AllOrder.class);
                        MyLog.i("HistoryOrderDetailsActivity", "解析的历史订单详情" + order.toString());
                        if (order != null && !TextUtils.isEmpty(order.getOrderid())) {
                            setView(order);
                        } else {
                            Toast.makeText(getApplicationContext(), "网络请求失败", 0).show();
                        }
                    } else {
                        dialog.dismiss();
                    }
                }
            });
        } else {
            Toast.makeText(this, "请检查网络!", 0).show();
        }
    }

    //	移除逃单 cobp.do?action=handleescorder&orderid=99&comid=1130&total=
    public void cashLostOrder(String orderid) {
        String path = baseurl;
        String url = path + "cobp.do?action=handleescorder&orderid=" + orderid + "&comid=" + comid + "&total=" + "0";
        MyLog.w("LostOrderRecordDetailsActivity", "移除逃单的URL--->" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "移除逃单", "移除逃单数据请求中...", true, true);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && object != null) {
                    dialog.dismiss();
                    MyLog.i("LostOrderRecordDetailsActivity", "结算逃单的结果--->" + object);
                    if (object.equals("1")) {
                        LostOrderRecordDetailsActivity.this.finish();
                    } else {
                        Toast.makeText(LostOrderRecordDetailsActivity.this, "移除逃单失败，请稍后再试！", 0).show();
                    }
                } else {
                    dialog.dismiss();
                    switch (status.getCode()) {
                        case -101:
                            Toast.makeText(LostOrderRecordDetailsActivity.this, "网络错误！--移除逃单记录失败！", 0).show();
                            break;
                        case 500:
                            Toast.makeText(LostOrderRecordDetailsActivity.this, "服务器错误！--移除逃单记录失败！", 0).show();
                            break;
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                LostOrderRecordDetailsActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
