package com.zhenlaidian.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.HistoryOrderLostOrderAdapter;
import com.zhenlaidian.bean.LostOrderRecordInfo;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

import java.util.ArrayList;

/**
 * 历史订单逃单界面;
 */
public class HistoryOrderLostOrderActivity extends BaseActivity {

    private ListView lv_lostordre;
    private Button bt_weekorder;
    private Button bt_monthorder;
    private Button bt_allorder;
    private TextView tv_total_number;
    private HistoryOrderLostOrderAdapter adapter;
    private String page = "week";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.history_order_lost_order_activity);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        adapter = new HistoryOrderLostOrderAdapter(this);
        initVeiw();
        setView();
    }

    public void initVeiw() {
        lv_lostordre = (ListView) findViewById(R.id.lv_lost_order_order);
        bt_weekorder = (Button) findViewById(R.id.bt_lost_order_week_order);
        bt_monthorder = (Button) findViewById(R.id.bt_lost_order_month_order);
        bt_allorder = (Button) findViewById(R.id.bt_lost_order_all_order);
        tv_total_number = (TextView) findViewById(R.id.tv_lost_order_total_number);
    }

    public void setView() {
        bt_weekorder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 本周逃单
                bt_weekorder.setTextColor(getResources().getColor(R.color.white));
                bt_weekorder.setBackgroundResource(R.drawable.histery_order_today_blue);
                bt_allorder.setTextColor(getResources().getColorStateList(R.color.tv_parknumber_blue));
                bt_allorder.setBackgroundResource(R.drawable.histery_order_yesterday_white);
                bt_monthorder.setTextColor(getResources().getColorStateList(R.color.tv_parknumber_blue));
                bt_monthorder.setBackgroundResource(R.drawable.lost_order_write);
                getHistoryOrders("week");
                page = "week";
            }
        });
        bt_monthorder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 本月逃单
                bt_weekorder.setTextColor(getResources().getColor(R.color.tv_parknumber_blue));
                bt_weekorder.setBackgroundResource(R.drawable.histery_order_today_white);
                bt_allorder.setTextColor(getResources().getColorStateList(R.color.tv_parknumber_blue));
                bt_allorder.setBackgroundResource(R.drawable.histery_order_yesterday_white);
                bt_monthorder.setTextColor(getResources().getColorStateList(R.color.white));
                bt_monthorder.setBackgroundResource(R.drawable.lost_order_blue);
                getHistoryOrders("month");
                page = "month";

            }
        });
        bt_allorder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 所有逃单
                bt_weekorder.setTextColor(getResources().getColor(R.color.tv_parknumber_blue));
                bt_weekorder.setBackgroundResource(R.drawable.histery_order_today_white);
                bt_allorder.setTextColor(getResources().getColorStateList(R.color.white));
                bt_allorder.setBackgroundResource(R.drawable.histery_order_yesterday_blue);
                bt_monthorder.setTextColor(getResources().getColorStateList(R.color.tv_parknumber_blue));
                bt_monthorder.setBackgroundResource(R.drawable.lost_order_write);
                getHistoryOrders("");
                page = "";
            }
        });

        lv_lostordre.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                adapter.onItemClick(position);
            }
        });
        lv_lostordre.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           int position, long id) {
                // TODO Auto-generated method stub
                adapter.onItemLongClick(position, HistoryOrderLostOrderActivity.this);
                return false;
            }
        });
    }

    //选择当前订单长按的操作；
    public void lostOrderDialog(final String carnumber, final LostOrderRecordInfo order, final int position) {
        final CharSequence[] items = {"调整价格并结算", "移除逃单 (管理员权限)"};
        AlertDialog.Builder builder = new Builder(this);
        builder.setIcon(R.drawable.app_icon_32);
        builder.setTitle(carnumber);
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if (which == 0) {
                    MyLog.w("HistoryOrderLostOrderActivity", "调整价格并结算");
                    Intent intent = new Intent(HistoryOrderLostOrderActivity.this, PriceChangesAndCash.class);
                    intent.putExtra("lostorder", order);
                    intent.putExtra("carnumber", carnumber);
                    startActivity(intent);
                } else {
                    MyLog.w("HistoryOrderLostOrderActivity", "移除逃单");
                    SharedPreferences autologin = getSharedPreferences("autologin", Context.MODE_PRIVATE);
                    String role = autologin.getString("role", "0");
                    if (role != null && role.equals("1")) {
                        cashLostOrder(order.getOrder_id(), position);
                    } else {
                        Toast.makeText(HistoryOrderLostOrderActivity.this, "请用管理员账号登录！", 0).show();
                    }

                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(HistoryOrderLostOrderActivity.this, "取消操作", 1).show();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                HistoryOrderLostOrderActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getHistoryOrders(String date) {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取订单失败，请检查网络！", 0).show();
            return;
        }
        AQuery aQuery = new AQuery(this);
        //cobp.do?action=queryescorde&comid=10&type=type:month本月，week：本周,其它或空：所有
        String path = baseurl;

        String url = path + "cobp.do?action=queryescorder&comid=" + comid + "&type=" + date;
        MyLog.w("逃单查询--URL---->>>", url);
        final ProgressDialog dialog = ProgressDialog.show(this, "查询逃单", "数据请求中...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                MyLog.i("获取到的逃单数据为----", object);
                if (object != null && object != "" && status.getCode() == 200) {
                    dialog.dismiss();
                    Gson gson = new Gson();
                    ArrayList<LostOrderRecordInfo> orders = gson.fromJson(object, new TypeToken<ArrayList<LostOrderRecordInfo>>() {
                    }.getType());
                    adapter.addOrder(orders);
                    lv_lostordre.setAdapter(adapter);
                    tv_total_number.setText(orders.size() + "");
                } else {
                    dialog.dismiss();
                    switch (status.getCode()) {
                        case -101:
                            Toast.makeText(HistoryOrderLostOrderActivity.this, "网络错误！--查看逃单记录失败！", 0).show();
                            break;
                        case 500:
                            Toast.makeText(HistoryOrderLostOrderActivity.this, "服务器错误！--查看逃单记录失败！", 0).show();
                            break;
                    }
                }

            }
        });
    }

    //	移除逃单 cobp.do?action=handleescorder&id=222&orderid=99&comid=1130&total=
    public void cashLostOrder(String orderid, final int position) {
        String path = baseurl;
        String url = path + "cobp.do?action=handleescorder&orderid=" + orderid + "&comid=" + comid + "&total=" + "0";
        MyLog.w("HistoryOrderLostOrderActivity", "移除逃单的URL--->" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "移除逃单", "移除逃单数据请求中...", true, true);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && object != null) {
                    dialog.dismiss();
                    Log.e("LostOrderRecordActivity", "结算逃单的结果--->" + object);
                    if (object.equals("1")) {
                        adapter.removeOrder(position);
                    } else {
                        Toast.makeText(HistoryOrderLostOrderActivity.this, "移除逃单失败，请稍后再试！", 0).show();
                    }
                } else {
                    dialog.dismiss();
                    switch (status.getCode()) {
                        case -101:
                            Toast.makeText(HistoryOrderLostOrderActivity.this, "网络错误！--移除逃单记录失败！", 0).show();
                            break;
                        case 500:
                            Toast.makeText(HistoryOrderLostOrderActivity.this, "服务器错误！--移除逃单记录失败！", 0).show();
                            break;
                    }
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(HistoryOrderLostOrderActivity.this, HistoryOrderActivity.class);
            startActivity(intent);
            HistoryOrderLostOrderActivity.this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onResume() {
        super.onResume();
        getHistoryOrders(page);
    }

}
          