package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
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
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.AllOrder;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.util.CheckUtils;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.Constant;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;
import com.zhenlaidian.util.TimeTypeUtil;

import java.util.Date;

/**
 * 历史订单详情界面;
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("SimpleDateFormat")
public class HistoryOrderDetailsActivity extends BaseActivity {

    private TextView tv_money;
    private TextView tv_plateNumber;
    private TextView tv_parkingTime;
    private TextView tv_call;
    private TextView tv_orderNumber;
    private TextView tv_orderStatus;
    private TextView tv_inTime;
    private TextView tv_outTime;
    private String orderid;
    private Button btnPrint;

    private String iscard;
    private String ismonthuser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.activity_history_order_dttails);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        initView();
        orderid = getIntent().getExtras().getString("orderid");
        ismonthuser = getIntent().getStringExtra("ismonthuser");
        iscard = getIntent().getStringExtra("iscard");
        getOrderInfo();

    }

    // 历史订单——订单详情解析AllOrder [total=-624.0, duration=null, carnumber=京GPS223,
    // btime=null, state=null, orderid=null, end=1400916800, begin=1400979600,
    // mobile=15801482643]

    @SuppressLint("SimpleDateFormat")
    public void setView(final AllOrder order) {
        if (!TextUtils.isEmpty(ismonthuser)&&ismonthuser.equals("1")) {
            monthnumber.setVisibility(View.VISIBLE);
        } else {
            monthnumber.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(iscard)&&iscard.equals("1")) {
            bondcard.setVisibility(View.VISIBLE);
        } else {
            bondcard.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(order.getCarnumber())) {
            tv_plateNumber.setText(order.getCarnumber());
        } else {
            tv_plateNumber.setText("车牌号未知");
        }
        if (order.getEnd() != null && order.getBegin() != null) {
            if (!TextUtils.isEmpty(order.getShowepay())) {
                long endtime = Long.parseLong(order.getEnd());
                long begintime = Long.parseLong(order.getBegin());
                tv_inTime.setText((new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                        .format(new Date(begintime * 1000)));
                tv_outTime.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(new Date(endtime * 1000)));
                tv_orderNumber.setText(order.getOrderid());
                tv_parkingTime.setText(order.getShowepay());
            } else {
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
        }
        tv_call.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (order.getMobile() != null && CheckUtils.MobileChecked(order.getMobile())) {
                    Intent phoneintent = new Intent(
                            "android.intent.action.CALL", Uri.parse("tel:" + order.getMobile()));
                    startActivity(phoneintent);
                } else {
                    Toast.makeText(HistoryOrderDetailsActivity.this, "电话号码为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        tv_orderStatus.setText(order.getState());
        tv_money.setText(order.getTotal());

    }

    private ImageView monthnumber;
    private ImageView bondcard;

    public void initView() {
        monthnumber = ((ImageView) findViewById(R.id.monthnumber));
        bondcard = ((ImageView) findViewById(R.id.bondcard));
        tv_money = (TextView) findViewById(R.id.tv_history_order_details_money);
        tv_plateNumber = (TextView) findViewById(R.id.tv_history_order_details_carNumber);
        tv_parkingTime = (TextView) findViewById(R.id.tv_history_order_details_parkingTime);
        tv_call = (TextView) findViewById(R.id.tv_history_order_details_call);
        tv_orderNumber = (TextView) findViewById(R.id.tv_history_order_details_orderNumber);
        tv_orderStatus = (TextView) findViewById(R.id.tv_history_order_details_orderStatus);
        tv_inTime = (TextView) findViewById(R.id.tv_history_order_details_inTime);
        tv_outTime = (TextView) findViewById(R.id.tv_history_order_details_outTime);

        btnPrint = ((Button) findViewById(R.id.bt_current_order_cash_up));
        btnPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                double payactual = 0;
                if (CommontUtils.checkString(order.getTotal()) && CommontUtils.checkString(order.getPrepay())) {
                    payactual = Double.parseDouble(order.getTotal()) - Double.parseDouble(order.getPrepay());
                }
                String pay;
                if (payactual < 0) {
                    pay = "应退：" + (-payactual) + "元";
                } else {
                    pay = "出场缴费：" + payactual + "元";
                }
                String Sname="";
                String gang = "";
                if (SharedPreferencesUtils.getIntance(HistoryOrderDetailsActivity.this).getisprintName()) {
                    Sname = getStringFromPreference("name");
                    gang = "-";
                }
                String printstr = Constant.HEADOut +
                        SharedPreferencesUtils.getIntance(context).getprint_signOutHead() + "\n" +
                        "停车场：" + SharedPreferencesUtils.getIntance(context).getParkname() +
                        "—" + SharedPreferencesUtils.getIntance(context).getberthsec_name() + "\n" +
                        "收费员：" + Sname;
                if (CommontUtils.Is910()) {
                    if (!TextUtils.isEmpty(SharedPreferencesUtils.getIntance(context).getmobile())) {
                        printstr += "(" + SharedPreferencesUtils.getIntance(context).getmobile() + ")";
                    }
                }
                    printstr += gang + useraccount ;

                printstr += "\n" +
//                        "车位：" + order.getPark() + "\n" +
                        "车牌号：" + order.getCarnumber() + "\n" +
                        "停车类型：临时停车\n" +
                        "进场时间：" + CommontUtils.Unix2TimeS(order.getBegin()) + "\n" +
                        "出场时间：" + CommontUtils.Unix2TimeS(order.getEnd()) + "\n" +
//                        "支付方式：现金\n" +
                        "订单金额：" + order.getTotal() + "元\n" +
                        "预缴金额：" + order.getPrepay() + "元\n" +
                        pay + "\n\n" +
                        Constant.FOOT +
                        SharedPreferencesUtils.getIntance(context).getprint_signOut() + "\n\n\n\n\n";

                PullMsgService.sendMessage(printstr, context);
                finish();
            }
        });
    }

    private AllOrder order;

    public void getOrderInfo() {
        AQuery aQuery = new AQuery(HistoryOrderDetailsActivity.this);
        String path = baseurl;
        // collectorrequest.do?action=orderdetail&token=*&orderid=*
        String url = path + "collectorrequest.do?action=orderdetail&token=" + token
                + "&orderid=" + orderid + "&out=json";
        MyLog.w("HistoryOrderDetailsActivity", "url>>>>>" + url);
        if (IsNetWork.IsHaveInternet(this)) {
            final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取历史订单数据...", true, true);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (!TextUtils.isEmpty(object)) {
                        dialog.dismiss();
                        MyLog.i("HistoryOrderDetailsActivity", "返回的历史订单详情" + object);
                        Gson gson = new Gson();
                        order = gson.fromJson(object, AllOrder.class);
                        MyLog.i("HistoryOrderDetailsActivity", "解析的历史订单详情" + order.toString());
                        if (order != null && !TextUtils.isEmpty(order.getOrderid())) {
                            setView(order);
                        } else {
                            Toast.makeText(getApplicationContext(), "网络请求失败", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        dialog.dismiss();
                    }
                }
            });
        } else {
            Toast.makeText(this, "请检查网络!", Toast.LENGTH_SHORT).show();
        }
    }

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                HistoryOrderDetailsActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
