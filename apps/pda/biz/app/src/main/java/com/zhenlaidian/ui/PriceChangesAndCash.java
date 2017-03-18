package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.AllOrder;
import com.zhenlaidian.bean.LostOrderRecordInfo;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.TimeTypeUtil;

import java.sql.Date;

/**
 * 逃单调整价格后结算;
 */
public class PriceChangesAndCash extends BaseActivity {

    public LostOrderRecordInfo order;
    public AllOrder allorder;
    public TextView tv_carnumber;
    public TextView tv_intime;
    public TextView tv_losttime;
    public TextView tv_duration;
    public TextView tv_price;// 原价格
    public EditText et_changetotal;// 调整后总价
    public Button bt_cancle;
    public Button bt_cashing;
    public String carnumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.price_changes_and_cash_activity);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        initVeiw();
        if (order != null) {
            SetView();
        } else {
            SetHisotryOrderView();
        }

    }

    public void initVeiw() {
        order = (LostOrderRecordInfo) getIntent().getExtras().getSerializable("lostorder");
        allorder = (AllOrder) getIntent().getExtras().getSerializable("allorder");
        carnumber = getIntent().getExtras().getString("carnumber");
        tv_carnumber = (TextView) findViewById(R.id.tv_price_changes_cash_carnumber);
        tv_intime = (TextView) findViewById(R.id.tv_price_changes_cash_intime);
        tv_losttime = (TextView) findViewById(R.id.tv_price_changes_cash_losttime);
        tv_duration = (TextView) findViewById(R.id.tv_price_changes_cash_duration);
        tv_price = (TextView) findViewById(R.id.tv_price_changes_cash_total);
        et_changetotal = (EditText) findViewById(R.id.et_price_changes_cash_changetotal);
        bt_cancle = (Button) findViewById(R.id.bt_price_changes_cash_cancle);
        bt_cashing = (Button) findViewById(R.id.bt_price_changes_cash_cashing);
    }

    public void SetView() {
        if (carnumber != null) {
            tv_carnumber.setText(carnumber);
        }
        if (order.getCreate_time() != null && order.getEnd_time() != null) {
            tv_intime.setText(TimeTypeUtil.getStringTime(Long.parseLong(order.getCreate_time())));
            tv_losttime.setText(TimeTypeUtil.getStringTime(Long.parseLong(order.getEnd_time())));
            tv_duration.setText(TimeTypeUtil.getTimeString(Long.parseLong(order.getCreate_time()),
                    Long.parseLong(order.getEnd_time())));
        }
        if (order.getTotal() != null) {
            tv_price.setText(order.getTotal());
        }
        bt_cancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PriceChangesAndCash.this.finish();
            }
        });
        bt_cashing.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String changeTotal = et_changetotal.getText().toString();
                MyLog.w("LostOrderRecordActivity", "order类型的逃单--->" + "id=" + order.getId() + "Comid=" + order.getComid()
                        + "Order_id=" + order.getOrder_id());
                if (!TextUtils.isEmpty(changeTotal)) {
                    cashLostOrder(order.getOrder_id(), changeTotal);
                } else {
                    Toast.makeText(PriceChangesAndCash.this, "请输入调整价格后再结算", 0).show();
                }
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    public void SetHisotryOrderView() {
        if (allorder.getCarnumber() != null) {
            tv_carnumber.setText(allorder.getCarnumber());
        }
        if (allorder.getEnd() != null && allorder.getBegin() != null) {
            long endtime = Long.parseLong(allorder.getEnd());
            long begintime = Long.parseLong(allorder.getBegin());
            String time = TimeTypeUtil.getTimeString(begintime, endtime);
            tv_duration.setText(time);
            tv_intime.setText((new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(begintime * 1000)));
            tv_losttime.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(endtime * 1000)));
        }
        if (allorder.getTotal() != null) {
            tv_price.setText(allorder.getTotal());
        }
        bt_cancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PriceChangesAndCash.this.finish();
            }
        });
        bt_cashing.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String changeTotal = et_changetotal.getText().toString();
                MyLog.w("LostOrderRecordActivity", "allorder类型的逃单--->" + "id=" + allorder.getId());
                if (!TextUtils.isEmpty(changeTotal)) {
                    cashLostOrder(allorder.getId(), changeTotal);
                } else {
                    Toast.makeText(PriceChangesAndCash.this, "请输入调整价格后再结算", 0).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                PriceChangesAndCash.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // 结算逃单 cobp.do?action=handleescorder&id=222&orderid=99&comid=1130&total=
    public void cashLostOrder(String orderid, String changeTotal) {
        String path = baseurl;
        String url = path + "cobp.do?action=handleescorder&orderid=" + orderid + "&comid=" + comid + "&total="
                + changeTotal;
        MyLog.w("LostOrderRecordActivity", "结算逃单的URL--->" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "结算逃单", "结算数据请求中...", true, true);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && object != null) {
                    dialog.dismiss();
                    MyLog.i("LostOrderRecordActivity", "结算逃单的结果--->" + object);
                    if (object.equals("1")) {
                        PriceChangesAndCash.this.finish();
                    } else {
                        Toast.makeText(PriceChangesAndCash.this, "结算逃单失败，请稍后再试！", 0).show();
                    }
                } else {
                    dialog.dismiss();
                    switch (status.getCode()) {
                        case -101:
                            Toast.makeText(PriceChangesAndCash.this, "网络错误！--查看逃单记录失败！", 0).show();
                            break;
                        case 500:
                            Toast.makeText(PriceChangesAndCash.this, "服务器错误！--查看逃单记录失败！", 0).show();
                            break;
                    }
                }
            }
        });
    }

}
