package com.zhenlaidian.ui.person_account;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.zhenlaidian.adapter.MyIncomeDetailsAdapter;
import com.zhenlaidian.bean.MyIncomeDetialsInfo;
import com.zhenlaidian.bean.QueryAccountDetail;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

import java.util.ArrayList;

/**
 * 我的收支明细;
 */
@SuppressLint("InlinedApi")
public class MyIncomeDetailsActivity extends BaseActivity {

    private Button bt_park_money;// 停车费
    private Button bt_back;// 返现
    private Button bt_bonus;// 奖金
    private ListView lv_income;
    private TextView tv_money_type;
    private TextView tv_allmoney;
    private RelativeLayout rl_incomeinfo;
    private MyIncomeDetailsAdapter adapter;
    private ActionBar actionBar;
    private LinearLayout ll_income_detail_null; // 没有数据时显示；
    private String[] actions = new String[]{"今天", "昨天", "本周", "本月"};
    private int first = 0;// 用于判断第一次打开界面；
    private Boolean isbottom = true;// 列表正在滑动时候不能点击三个button；
    private int page = 1;
    private Boolean hasnext;// 是否有下一也信息；
    private ArrayList<QueryAccountDetail> allDetail = null;
    private String acctype; // ；// acctype//0自己,1车场
    private String datetype = "0"; // datetype 0今天，1昨天，2本周，3本月
    private String incom = "0"; // incom 0停车费，1返现 ，2奖金,

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        setContentView(R.layout.my_income_detials_activity);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        initVeiw();
        initActionBar();
        adapter = new MyIncomeDetailsAdapter(this);
        Boolean getdefault = SharedPreferencesUtils.getIntance(this).getdefaultCheck();
        if (getdefault) {
            acctype = "1";
        } else {
            acctype = "0";
        }
        getMyIncomeInfo(acctype, incom, datetype);
    }

    public void initVeiw() {
        bt_park_money = (Button) findViewById(R.id.bt_my_income_detail_park_money);
        bt_bonus = (Button) findViewById(R.id.bt_my_income_detail_bonus);
        bt_back = (Button) findViewById(R.id.bt_my_income_detail_back);
        lv_income = (ListView) findViewById(R.id.lv_my_income_detail);
        tv_money_type = (TextView) findViewById(R.id.tv_my_income_alltext);
        tv_allmoney = (TextView) findViewById(R.id.tv_my_income_all_money);
        ll_income_detail_null = (LinearLayout) findViewById(R.id.ll_my_income_detail_null);
        rl_incomeinfo = (RelativeLayout) findViewById(R.id.rl_my_income_detail);
        bt_park_money.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 点击查看 --0-停车费
                if (!incom.equals("0") && isbottom == true) {
                    page = 1;
                    incom = "0";
                    adapter.clearInfo();
                    getMyIncomeInfo(acctype, incom, datetype);
                    bt_park_money.setBackgroundResource((R.drawable.shape_payment_detail_blue));
                    bt_back.setBackgroundResource((R.drawable.shape_payment_detail_gray));
                    bt_bonus.setBackgroundResource((R.drawable.shape_payment_detail_gray));
                    bt_park_money.setTextColor(Color.WHITE);
                    bt_back.setTextColor(Color.BLACK);
                    bt_bonus.setTextColor(Color.BLACK);
                }
            }
        });
        bt_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 点击查看 -1-返现
                if (!incom.equals("1") && isbottom == true) {
                    page = 1;
                    incom = "1";
                    adapter.clearInfo();
                    getMyIncomeInfo(acctype, incom, datetype);
                    bt_park_money.setBackgroundResource((R.drawable.shape_payment_detail_gray));
                    bt_back.setBackgroundResource((R.drawable.shape_payment_detail_blue));
                    bt_bonus.setBackgroundResource((R.drawable.shape_payment_detail_gray));
                    bt_park_money.setTextColor(Color.BLACK);
                    bt_back.setTextColor(Color.WHITE);
                    bt_bonus.setTextColor(Color.BLACK);
                }
            }
        });
        bt_bonus.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 点击查看 --2-奖金
                if (!incom.equals("2") && isbottom == true) {
                    page = 1;
                    incom = "2";
                    adapter.clearInfo();
                    getMyIncomeInfo(acctype, incom, datetype);
                    bt_park_money.setBackgroundResource((R.drawable.shape_payment_detail_gray));
                    bt_back.setBackgroundResource((R.drawable.shape_payment_detail_gray));
                    bt_bonus.setBackgroundResource((R.drawable.shape_payment_detail_blue));
                    bt_park_money.setTextColor(Color.BLACK);
                    bt_back.setTextColor(Color.BLACK);
                    bt_bonus.setTextColor(Color.WHITE);
                }
            }
        });
        lv_income.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                switch (scrollState) {
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        isbottom = false;
                    case OnScrollListener.SCROLL_STATE_FLING:
                        isbottom = false;
                        break;
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        // 判断滚动到底部
                        isbottom = true;
                        if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                            if (hasnext) {
                                getMyIncomeInfo(acctype, incom, datetype);
                            }
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

    }

    public void initActionBar() {

        ArrayAdapter<String> arrayadapter = new ArrayAdapter<String>(this, R.layout.actionbar_spinner_item,
                R.id.tv_actionbar_spinner_account, actions);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ActionBar.OnNavigationListener navigationListener = new OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                // TODO Auto-generated method stub
                if (first == 0) {
                    first++;
                    return true;
                }
                switch (itemPosition) {
                    case 0: // 今日；
                        page = 1;
                        datetype = "0";
                        adapter.clearInfo();
                        getMyIncomeInfo(acctype, incom, datetype);
                        return true;
                    case 1: // 昨日；
                        page = 1;
                        datetype = "1";
                        adapter.clearInfo();
                        getMyIncomeInfo(acctype, incom, datetype);
                        return true;
                    case 2: // 本周；
                        page = 1;
                        datetype = "2";
                        adapter.clearInfo();
                        getMyIncomeInfo(acctype, incom, datetype);
                        return true;
                    case 3: // 本月；
                        page = 1;
                        datetype = "3";
                        adapter.clearInfo();
                        getMyIncomeInfo(acctype, incom, datetype);
                        return true;
                }
                return true;
            }
        };
        actionBar.setListNavigationCallbacks(arrayadapter, navigationListener);
    }

    public void setAdapter() {
        lv_income.setAdapter(adapter);
    }

    public void setPageNumber() {
        page++;
        hasnext = true;
    }

    public void setNullVeiw() {
        ll_income_detail_null.setVisibility(View.VISIBLE);
        rl_incomeinfo.setVisibility(View.GONE);
    }

    // zld/collectorrequest.do?action=incomanly&acctype=0&incom=0&datetype=0&token=&page=
    // acctype//0自己,1车场
    // incom 0停车费，1返现 ，2奖金,3 全部
    // datetype 0今天，1昨天，2本周，3本月
    public void getMyIncomeInfo(final String acctype, final String incom, final String datetype) {
        hasnext = false;// 访问网络后把是否有下一页置为假。如果信息有20条再置为真；
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "收入详情获取失败，请检查网络！", 0).show();
            return;
        }
        String url = baseurl + "collectorrequest.do?action=incomanly&acctype=" + acctype + "&incom=" + incom + "&datetype="
                + datetype + "&token=" + token + "&page=" + page;
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "查询收入详情...", true, true);
        MyLog.w("MyIncomeDetailsActivity", "获取收入详情的URl--->" + url);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                super.callback(url, object, status);
                if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
                    // Log.e("MyIncomeDetailsActivity",
                    // "请求个人账户收支明细返回的结果--->"+object);
                    dialog.dismiss();
                    Gson gson = new Gson();
                    MyIncomeDetialsInfo info = gson.fromJson(object, MyIncomeDetialsInfo.class);
                    MyLog.i("MyIncomeDetailsActivity", "解析个人账户收支明细的结果--->" + info.toString());
                    if (info != null && info.getTotal() != null && info.getInfo() != null) {
                        allDetail = info.getInfo();
                        tv_allmoney.setText(info.getTotal());
                        String textinfo = gettext();
                        if ("0".equals(acctype)) {
                            tv_money_type.setText(textinfo);
                        } else {
                            SpannableStringBuilder style = new SpannableStringBuilder(textinfo);
                            style.setSpan(new ForegroundColorSpan(Color.GREEN), 0, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tv_money_type.setText(style);
                        }
                    }
                    if (allDetail != null && allDetail.size() >= 1 && allDetail.get(0).getNote() != null) {
                        ll_income_detail_null.setVisibility(View.GONE);
                        rl_incomeinfo.setVisibility(View.VISIBLE);
                        adapter.addInfo(allDetail, MyIncomeDetailsActivity.this);
                    } else {
                        adapter.setView(MyIncomeDetailsActivity.this);
                    }
                } else {
                    dialog.dismiss();
                    adapter.setView(MyIncomeDetailsActivity.this);
                }
            }
        });
    }

    public String gettext() {
        String account = "";// ；// acctype//0自己,1车场
        String date = ""; // datetype 0今天，1昨天，2本周
        String type = ""; // incom 0停车费，1返现 ，2奖金,
        String text = "";
        if ("0".equals(acctype)) {
            account = "";
        } else if ("1".equals(acctype)) {
            account = "停车场";
        }
        if ("0".equals(datetype)) {
            date = "今天";
        } else if ("1".equals(datetype)) {
            date = "昨天";
        } else if ("2".equals(datetype)) {
            date = "本周";
        } else if ("3".equals(datetype)) {
            date = "本月";
        }
        if ("0".equals(incom)) {
            type = "停车费";
        } else if ("1".equals(incom)) {
            type = "返现";
        } else if ("2".equals(incom)) {
            type = "奖金";
        }
        text = account + date + type + "统计:";
        return text;
    }

    @SuppressWarnings("deprecation")
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_income_change, menu);
        Boolean getdefault = SharedPreferencesUtils.getIntance(this).getdefaultCheck();
        if (getdefault) {
            MenuCompat.setShowAsAction(menu.findItem(R.id.income_change_me), MenuItem.SHOW_AS_ACTION_IF_ROOM);
            MenuItem item = menu.findItem(R.id.income_change_parking);
            item.setVisible(false);
            actionBar.setTitle("车场收入统计");
        } else {
            MenuCompat.setShowAsAction(menu.findItem(R.id.income_change_parking), MenuItem.SHOW_AS_ACTION_IF_ROOM);
            MenuItem item = menu.findItem(R.id.income_change_me);
            item.setVisible(false);
            actionBar.setTitle("我的收入统计");
        }
        return true;
    }

    /**
     * 切换查看--车场和自己的弹框提示！
     */
    public void toDialog(final int car_num, String msg, String stitle) {
        AlertDialog.Builder mBuilder = new Builder(this);
        mBuilder.setMessage(stitle);
        mBuilder.setTitle(msg);
        mBuilder.setPositiveButton("去看看", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (car_num == 1) {
                    SharedPreferencesUtils.getIntance(MyIncomeDetailsActivity.this).setdefaultCheck(true);
                } else {
                    SharedPreferencesUtils.getIntance(MyIncomeDetailsActivity.this).setdefaultCheck(false);
                }
                arg0.dismiss();
                refresh();
            }
        });
        mBuilder.setNegativeButton("没兴趣", new DialogInterface.OnClickListener() {

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
        Intent intent = new Intent(MyIncomeDetailsActivity.this, MyIncomeDetailsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                MyIncomeDetailsActivity.this.finish();
                return true;
            case R.id.income_change_parking:
                toDialog(1, "看车场的", "显示的是你帮停车场收的钱，这部分钱直接进入停车场账户，不在你的钱包里。");
                return true;
            case R.id.income_change_me:
                toDialog(2, "看自己的", "显示的进入你自己账户的钱，在你的钱包里，可提现。");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        page = 1;
    }
}
