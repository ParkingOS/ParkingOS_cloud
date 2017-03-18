package com.zhenlaidian.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.LostOrderRecordAdapter;
import com.zhenlaidian.bean.LostOrderRecordInfo;
import com.zhenlaidian.util.MyLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * 逃单列表界面;
 */
public class LostOrderRecordActivity extends BaseActivity {

    public String carnumber;
    public ListView lv_lost_order;
    public LostOrderRecordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.lost_order_record_activity);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        carnumber = getIntent().getExtras().getString("carnumber");
        lv_lost_order = (ListView) findViewById(R.id.lv_lost_order_record);
        adapter = new LostOrderRecordAdapter(this);

        lv_lost_order.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                adapter.onItemClick(position, LostOrderRecordActivity.this);

            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                LostOrderRecordActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //查看逃单记录 cobp.do?action=viewescdetail&comid=10&carnumber=苏H33442
    public void queryLostOrder() throws UnsupportedEncodingException {
        String path = baseurl;
        String carnumberEncoder = URLEncoder.encode(carnumber, "utf-8");
        String url = path + "cobp.do?action=viewescdetail&carnumber=" + URLEncoder.encode(carnumberEncoder, "utf-8")
                + "&comid=" + comid;
        MyLog.w("LostOrderRecordActivity", "查看逃单记录的URL--->" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "查看逃单记录", "请求逃单数据中...", true, true);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && object != null) {
                    dialog.dismiss();
                    MyLog.i("LostOrderRecordActivity", "查看逃单记录的结果--->" + object);
                    Gson gson = new Gson();
                    ArrayList<LostOrderRecordInfo> orders = gson.fromJson(object, new TypeToken<ArrayList<LostOrderRecordInfo>>() {
                    }.getType());
                    adapter.addOrders(orders);
                    lv_lost_order.setAdapter(adapter);
                } else {
                    dialog.dismiss();
                    switch (status.getCode()) {
                        case -101:
                            Toast.makeText(LostOrderRecordActivity.this, "网络错误！--查看逃单记录失败！", 0).show();
                            break;
                        case 500:
                            Toast.makeText(LostOrderRecordActivity.this, "服务器错误！--查看逃单记录失败！", 0).show();
                            break;
                    }
                }
            }
        });
    }

    public void cashLostOrder(LostOrderRecordInfo order) {
        Intent intent = new Intent(this, PriceChangesAndCash.class);
        intent.putExtra("lostorder", order);
        intent.putExtra("carnumber", carnumber);
        startActivity(intent);
    }


    @Override
    public void onResume() {
        super.onResume();
        try {
            queryLostOrder();
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(LostOrderRecordActivity.this, "查询逃单记录-车牌字符解码异常！", 0).show();
            e.printStackTrace();
        }
    }

}
