package com.zhenlaidian.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.RecommendRecord;
import com.zhenlaidian.ui.person_account.MyWalletActivity;
import com.zhenlaidian.util.MyLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 展示推荐记录列表页面;包含推荐收费员和推荐车主;
 */
public class RecommendRecordActivity extends BaseActivity implements OnClickListener {

    private TextView tvSee;
    private TextView tvPageNull;
    private TextView tvTotal;
    private ListView lvRecord;
    private String rType = null;

    private RecommendRecordAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.activity_recommend_record);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        if (getIntent().getExtras() != null) {
            rType = getIntent().getExtras().getString("type");
        }
        initView();
        if (rType != null && rType.equals("0")) {
            actionBar.setTitle("开通车主记录");
            getRecords("0");
        } else {
            actionBar.setTitle("推荐收费员记录");
            getRecords("1");
        }
    }

    private void getRecords(String type) {
        //collectorrequest.do?action=recominfo&token=222f4d6252ab65c27a338579b2d2040e &type=0车主1车场；
        String url = baseurl + "collectorrequest.do?action=recominfo&token=" + token + "&type=" + type;
        MyLog.w("RecommendRecordAdapter", "推荐记录的url---" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "正在获取记录信息...", true, true);
        dialog.setCanceledOnTouchOutside(false);
        new AQuery(this).ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (!TextUtils.isEmpty(object)) {
                    dialog.dismiss();
                    MyLog.i("RecommendRecordActivity", "获取推荐记录的信息是：--->> " + object);
                    try {
                        ArrayList<RecommendRecord> records = new Gson().fromJson(object,
                                new TypeToken<ArrayList<RecommendRecord>>() {
                                }.getType());
                        if (records != null && records.size() > 0) {
                            mAdapter.setRecords(records);
                            refreshView(records, "");
                        } else {
                            if (rType != null && rType.equals("0")) {
                                refreshView(null, "您还没有开通过车主会员，快去开通吧！");
                            } else {
                                refreshView(null, "您还没有推荐收费员，快去推荐吧！");
                            }
                        }
                    } catch (Exception e) {
                        refreshView(null, "数据解析异常，请稍后重试！");
                        e.printStackTrace();
                    }
                } else {
                    dialog.dismiss();
                    refreshView(null, "网络超时，请稍后重试！");
                }
            }
        });
    }

    private void refreshView(ArrayList<RecommendRecord> records, String errmsg) {
        MyLog.w("RecommendRecordActivity", "获取推荐记录错误信息：--->> " + errmsg);
        if (TextUtils.isEmpty(errmsg)) {// 数据正常
            tvTotal.setText(Html.fromHtml("共获得奖励 <font color='#329762'><big>" + getTotal(records) + "</big></font> 元"));
            tvPageNull.setVisibility(View.GONE);
        } else {
            tvPageNull.setText(errmsg);
            tvPageNull.setVisibility(View.VISIBLE);
        }
    }

    private int getTotal(ArrayList<RecommendRecord> records) {
        if (records == null || records.size() == 0) {
            return 0;
        }
        int total = 0;
        if (rType != null && rType.equals("0")) {
            for (RecommendRecord record : records) {
                if ("1".equals(record.state)) {
                    total += 5;
                }
            }
        } else {
            for (RecommendRecord record : records) {
                if ("1".equals(record.state)) {
                    total += 30;
                }
            }
        }
        return total;
    }

    private void initView() {
        tvPageNull = (TextView) findViewById(R.id.tv_recommend_record_null);
//		tvPageNull.setVisibility(View.VISIBLE);
        tvSee = (TextView) findViewById(R.id.tv_recommend_record_gotosee);
        tvSee.setOnClickListener(this);
        tvSee.setText(Html.fromHtml("<font color='#5F75DA'><u>去查看</u></font>"));
//		tvPageNull.setText("您还没有推荐过收费员，快去推荐吧！");
        tvTotal = (TextView) findViewById(R.id.tv_recommend_record_total);
        lvRecord = (ListView) findViewById(R.id.lv_recommend_record);
        mAdapter = new RecommendRecordAdapter();
        lvRecord.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    class RecommendRecordAdapter extends BaseAdapter {

        private ArrayList<RecommendRecord> records;

        public void setRecords(ArrayList<RecommendRecord> records) {
            // 先排序
            Collections.sort(records, new Comparator<RecommendRecord>() {

                @Override
                public int compare(RecommendRecord lhs, RecommendRecord rhs) {
                    try {
                        long lhsState = Long.parseLong(lhs.state);
                        long rhsState = Long.parseLong(rhs.state);
                        if (lhsState > rhsState) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return 0;
                    }
                }
            });
            this.records = records;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return records == null ? 0 : records.size();
        }

        @Override
        public Object getItem(int position) {
            return records == null ? null : records.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RecommendRecord record = (RecommendRecord) getItem(position);
            if (record == null) {
                return null;
            }
            if (convertView == null) {
                convertView = new TextView(RecommendRecordActivity.this);
                ((TextView) convertView).setGravity(Gravity.CENTER_HORIZONTAL);
                ((TextView) convertView).setTextColor(Color.DKGRAY);
                int padding = 15;
                ((TextView) convertView).setPadding(padding * 3, padding, padding, padding);
                ((TextView) convertView).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                ((TextView) convertView).setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            }
            // uin==null是等待车主注册 uin！=null是原来的处理 state=3车主重复注册，奖励取消，money==null是5元
            if (rType != null && rType.equals("0")) {
                if (record.uin != null && record.state != null && !"null".equals(record.uin)) {
                    if ("0".equals(record.state)) {//state 是否成功:0审核中,1成功,2在黑名单中
                        ((TextView) convertView).setText(String.format("会员（%1$s）,%2$s", "尾号" + record.uin.substring(7), "等待车主完善信息或支付一元停车费"));
                    } else if ("1".equals(record.state)) {
                        ((TextView) convertView).setText(String.format("会员（%1$s）,%2$s", "尾号" + record.uin.substring(7), "获得奖励" + record.money + "元"));
                    } else if ("2".equals(record.state)) {
                        ((TextView) convertView).setText(String.format("会员（%1$s）,%2$s", "尾号" + record.uin.substring(7), "账户有刷单嫌疑,奖励取消"));
                    } else if ("3".equals(record.state)) {
                        ((TextView) convertView).setText(String.format("会员（%1$s）,%2$s", "尾号" + record.uin.substring(7), "车主重复注册，奖励取消"));
                    } else {
                        ((TextView) convertView).setText(String.format("会员（%1$s）,%2$s", "尾号" + record.uin.substring(7), "等待车主完成一元在线支付"));
                    }
                } else {
                    if (record.uin != null && "null".equals(record.uin)) {
                        if ("3".equals(record.state)) {
                            ((TextView) convertView).setText(String.format("会员（%1$s）,%2$s", "编号" + record.nid, "车主重复注册，奖励取消"));
                        } else if ("1".equals(record.state)) {
                            ((TextView) convertView).setText(String.format("会员（%1$s）,%2$s", "编号" + record.nid, "获得奖励" + record.money + "元"));
                        } else if ("2".equals(record.state)) {
                            ((TextView) convertView).setText(String.format("会员（%1$s）,%2$s", "编号" + record.nid, "账户有刷单嫌疑,奖励取消"));
                        } else {
                            ((TextView) convertView).setText(String.format("会员（%1$s）,%2$s", "编号" + record.nid, "等待车主注册"));
                        }
                    }
                }
            } else {
//				 state , -- 状态 0审核中，1成功
                if (record.uin != null && record.state != null) {
                    if ("1".equals(record.state)) {
                        ((TextView) convertView).setText(String.format("收费员（%1$s）,%2$s", "编号" + record.uin, "获得奖励" + record.money + "元"));
                    } else {
                        ((TextView) convertView).setText(String.format("收费员（%1$s）,%2$s", "编号" + record.uin, "审核中..."));
                    }
                }
            }
            return convertView;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_recommend_record_gotosee:
                Intent intent = new Intent(getApplicationContext(), MyWalletActivity.class);
                startActivity(intent);
                break;
        }
    }

}
