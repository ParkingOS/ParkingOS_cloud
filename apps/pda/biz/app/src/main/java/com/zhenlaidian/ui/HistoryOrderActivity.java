package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.DrawerAdapter;
import com.zhenlaidian.adapter.HistouryOrderAdapter;
import com.zhenlaidian.bean.AllOrder;
import com.zhenlaidian.bean.DrawerItemInfo;
import com.zhenlaidian.bean.HistoryOrder;
import com.zhenlaidian.engine.DrawerOnItemClick;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

/**
 * 历史订单界面;
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("ResourceAsColor")
public class HistoryOrderActivity extends BaseActivity {

    private DrawerLayout drawerLayout = null;
    private ListView lv_history_order;
    private ListView lv_left_drawer;
    private TextView tv_history_null;
    private ImageView iv_call_phone;
    private TextView tv_total_number;// 订单总数
    private TextView tv_total_money;// 订单总金额
    private Button bt_today_order;// 今日订单按钮
    private Button bt_yesterday_order;// 昨日订单按钮
    private HistouryOrderAdapter adapter;
    private int pagenumber = 1;
    private int size = 10;
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar actionBar;
    private String date = "today";// 查询订单的参数：今日或者昨日；
    private String ptype = "";// 访问订单的接口，2 手机支付；3 月卡支付 其他为全部订单；
    private int count = 0;
    private int visiblecount = 0;
    private int first = 0;// 用于判断第一次打开界面；
    private ProgressDialog dialog;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_order_activity);
        initView();
        initActionBar(SharedPreferencesUtils.getIntance(this).getIsShowEpay());
    }
    private LinearLayout lntotal;
    public void initView() {
        lv_history_order = (ListView) findViewById(R.id.lv_history_order);
        tv_history_null = (TextView) findViewById(R.id.tv_history_order_null);
        drawerLayout = (DrawerLayout) findViewById(R.id.history_order_layout);
        lv_left_drawer = (ListView) findViewById(R.id.left_drawer);
        iv_call_phone = (ImageView) findViewById(R.id.iv_history_order_phone);
        adapter = new HistouryOrderAdapter(this);
        lv_left_drawer.setAdapter(new DrawerAdapter(DrawerItemInfo.getInstance(), HistoryOrderActivity.this));
        lv_left_drawer.setOnItemClickListener(new DrawerOnItemClick(this, drawerLayout,this));
        lv_left_drawer.setScrollingCacheEnabled(false);// 设置抽屉的listview不能滑动；
        // 用于上拉加载更多
        // lv_history_order.setPullLoadEnable(true);
        // lv_history_order.setXListViewListener(this);
        // mHandler = new Handler();
        iv_call_phone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneintent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + "01056450585"));
                startActivity(phoneintent);
            }
        });
        lv_history_order.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.onItemClick(position, HistoryOrderActivity.this, date, ptype);
            }
        });
        lv_history_order.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                switch (scrollState) {
                    // 当不滚动时
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        // 判断滚动到底部
                        // Toast.makeText(HistoryOrderActivity.this,
                        // count+":"+count+" viewCount:"+(view.getCount() -
                        // 1)+"visiblecount:"+visiblecount, 3).show();
                        if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                            if (count != visiblecount) {
                                adapter.onItemClick(view.getLastVisiblePosition(), HistoryOrderActivity.this, date, ptype);
                            } else {
                                if (count != 0) {
                                    AllOrder allOrders = adapter.getAllOrders(count);
                                    if (allOrders == null) {
                                        adapter.onItemClick(view.getLastVisiblePosition(), HistoryOrderActivity.this, date, ptype);
                                    }
                                }
                            }
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                visiblecount = totalItemCount;
            }
        });
        tv_total_money = (TextView) findViewById(R.id.tv_history_order_total_money);
        tv_total_number = (TextView) findViewById(R.id.tv_history_order_total_number);
        bt_today_order = (Button) findViewById(R.id.bt_history_order_today_order);
        bt_yesterday_order = (Button) findViewById(R.id.bt_history_order_yesterday_order);
        bt_today_order.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setViewToToday();
                date = "today";
                pagenumber = 1;
                adapter.clearOrder(HistoryOrderActivity.this);
                if (ptype.equals("2")) {
                    getHistoryOrders("today", ptype);// 获取历史订单-本周（手机支付）
                } else if (ptype.equals("3")) {
                    getHistoryOrders("today", ptype);// 获取历史订单-本周（月卡支付）
                } else {
                    getHistoryOrders("today", ptype);// 获取历史订单-本周（全部）
                }
            }
        });
        bt_yesterday_order.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                bt_today_order.setTextColor(getResources().getColorStateList(R.color.app_gray_font));
                bt_today_order.setBackgroundResource(R.drawable.histery_order_today_white1);
                bt_yesterday_order.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
                bt_yesterday_order.setBackgroundResource(R.drawable.histery_order_yesterday_green);
                date = "last";
                pagenumber = 1;
                adapter.clearOrder(HistoryOrderActivity.this);
                if (ptype.equals("2")) {
                    getHistoryOrders("last", ptype);// 获取历史订单-上周（手机支付）
                } else if (ptype.equals("3")) {
                    getHistoryOrders("last", ptype);// 获取历史订单-上周（月卡支付）
                } else {
                    getHistoryOrders("last", ptype);// 获取历史订单-上周（全部）
                }
            }
        });
        lntotal = ((LinearLayout) findViewById(R.id.lntotal));
        if (SharedPreferencesUtils.getIntance(HistoryOrderActivity.this).gethidedetail().equals("1")) {
            lntotal.setVisibility(View.GONE);
        } else {
            lntotal.setVisibility(View.VISIBLE);
        }
    }

    public void setNullView() {
        tv_history_null.setVisibility(View.VISIBLE);
        lv_history_order.setVisibility(View.INVISIBLE);
    }

    public void setView(String number, String money) {
        count = Integer.parseInt(number);
        tv_total_number.setText(number);
        tv_total_money.setText(money);
    }

    public void setAdapter() {
        lv_history_order.setAdapter(adapter);
    }

    public void setPageNumber() {
        pagenumber++;
    }

    public void getHistoryOrders(String date, String ptype) {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取订单失败，请检查网络！", Toast.LENGTH_SHORT).show();
            tv_history_null.setVisibility(View.VISIBLE);
            tv_history_null.setText("获取订单失败，请检查网络！");
            lv_history_order.setVisibility(View.INVISIBLE);
            return;
        }
        AQuery aQuery = new AQuery(this);
        // collectorrequest.do?action=orderhistory&token= &page= &size= &uid=
        // &day= &ptype=
        String path = baseurl;
        SharedPreferences pfs = getSharedPreferences("autologin", Context.MODE_PRIVATE);
        String uid = pfs.getString("account", "");
        String url = path + "collectorrequest.do?action=orderhistory&token=" + token + "&page=" + pagenumber
                + "&size=" + size + "&uid=" + uid + "&day=" + date + "&ptype=" + ptype +"&version=2"+ "&out=json";
        MyLog.w("历史订单--URL---->>>", url);
        if (count == 0) {
            dialog = ProgressDialog.show(this, "加载中...", "获取历史订单数据...", true, true);
        }
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {

                if (object != null) {
                    MyLog.i("当前订单访问页码为", pagenumber + "");
                    System.out.println("订单数据数组的长度" + object.length());
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                    return;
                }
                if (object != null && object.length() > 110) {
                    MyLog.i("获取到历史订单为----", object);
                    lv_history_order.setVisibility(View.VISIBLE);
                    tv_history_null.setVisibility(View.INVISIBLE);
                    Gson gson = new Gson();
                    HistoryOrder orders = gson.fromJson(object, HistoryOrder.class);
                    MyLog.d("解析到的历史订单是----", orders.toString());
                    System.out.println("获取到当历史单的集合的长度" + orders.getInfo().size());
                    if (orders == null || orders.getInfo().size() == 0) {
                        return;
                    }
                    int total = 0;
                    if (orders.getCount() != null) {
                        total = Integer.parseInt(orders.getCount());
                    }
                    if (orders.getCount() != null && orders.getPrice() != null) {
                        setView(orders.getCount(), orders.getPrice());
                    }
                    adapterAddOrder(orders, total);
                } else {
                    adapterGetOrders();
                }
            }
        });

    }

    // 为适配器增加数据；
    public void adapterAddOrder(HistoryOrder orders, int total) {
        adapter.addOrders(orders.getInfo(), total, HistoryOrderActivity.this);
    }

    // 没数据就显示没有历史记录；
    public void adapterGetOrders() {
        adapter.getOrdes(HistoryOrderActivity.this);
    }

    public void initActionBar(String isshowepay) {
        String[] actions;
        if ("1".equals(isshowepay)) {//显示
            actions = new String[]{"全部", "手机支付", "月卡支付", "直接支付", "查看逃单"};
        } else {
            actions = new String[]{"全部", "手机支付", "月卡支付", "查看逃单"};
        }
        drawerLayout.setDrawerListener(new MyDrawerListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_am, R.string.hello_world,
                R.string.hello_world);
        mDrawerToggle.syncState();
        actionBar = getSupportActionBar();
        actionBar.setTitle("我结算的订单");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setDisplayHomeAsUpEnabled(true);
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
                    case 0:
                        ptype = "";
                        pagenumber = 1;
                        adapter.clearOrder(HistoryOrderActivity.this);
                        getHistoryOrders(date, ptype);// 获取历史订单-本周（全部）
                        return true;
                    case 1:
                        ptype = "2";
                        pagenumber = 1;
                        adapter.clearOrder(HistoryOrderActivity.this);
                        getHistoryOrders(date, ptype);// 获取历史订单-本周（手机支付）
                        return true;
                    case 2:
                        ptype = "3";
                        pagenumber = 1;
                        adapter.clearOrder(HistoryOrderActivity.this);
                        getHistoryOrders(date, ptype);// 获取历史订单-本周（月卡支付）
                        return true;
                    case 3:
                        ptype = "4";
                        pagenumber = 1;
                        adapter.clearOrder(HistoryOrderActivity.this);
                        getHistoryOrders(date, ptype);// 获取直接支付订单；
                        return true;
                    case 4:// 进入逃单列表界面；
                        Intent intent = new Intent(HistoryOrderActivity.this, HistoryOrderLostOrderActivity.class);
                        startActivity(intent);
                        HistoryOrderActivity.this.finish();
                        return true;
                }
                return true;
            }
        };
        ActionBar.OnNavigationListener canclenavigationListener = new OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                // TODO Auto-generated method stub
                if (first == 0) {
                    first++;
                    return true;
                }
                switch (itemPosition) {
                    case 0:
                        ptype = "";
                        pagenumber = 1;
                        adapter.clearOrder(HistoryOrderActivity.this);
                        getHistoryOrders(date, ptype);// 获取历史订单-本周（全部）
                        return true;
                    case 1:
                        ptype = "2";
                        pagenumber = 1;
                        adapter.clearOrder(HistoryOrderActivity.this);
                        getHistoryOrders(date, ptype);// 获取历史订单-本周（手机支付）
                        return true;
                    case 2:
                        ptype = "3";
                        pagenumber = 1;
                        adapter.clearOrder(HistoryOrderActivity.this);
                        getHistoryOrders(date, ptype);// 获取历史订单-本周（月卡支付）
                        return true;
                    case 3:// 进入逃单列表界面；
                        Intent intent = new Intent(HistoryOrderActivity.this, HistoryOrderLostOrderActivity.class);
                        startActivity(intent);
                        HistoryOrderActivity.this.finish();
                        return true;
                }
                return true;
            }
        };
        if ("1".equals(isshowepay)) {//显示
            actionBar.setListNavigationCallbacks(arrayadapter, navigationListener);
        } else {
            actionBar.setListNavigationCallbacks(arrayadapter, canclenavigationListener);
        }
    }

    public void setViewToToday() {
        bt_today_order.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
        bt_today_order.setBackgroundResource(R.drawable.histery_order_today_green);
        bt_yesterday_order.setTextColor(getResources().getColorStateList(R.color.app_gray_font));
        bt_yesterday_order.setBackgroundResource(R.drawable.histery_order_yesterday_white1);
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
            actionBar.setTitle("我结算的订单");
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

    // @SuppressWarnings("deprecation")
    // public boolean onCreateOptionsMenu(Menu menu) {
    // getMenuInflater().inflate(R.menu.history_only_lost_order, menu);
    // MenuCompat.setShowAsAction(menu.findItem(R.id.only_lost),
    // MenuItem.SHOW_AS_ACTION_IF_ROOM);
    // return true;
    // }

    // actionBar的点击回调方法
    @SuppressLint({"InlinedApi", "RtlHardcoded"})
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
            case R.id.only_lost:
                Intent intent = new Intent(HistoryOrderActivity.this, HistoryOrderLostOrderActivity.class);
                startActivity(intent);
                HistoryOrderActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // // ActionBar添加菜单项
    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // super.onCreateOptionsMenu(menu);
    // // 添加菜单项；
    // MenuItem add = menu.add(0, 0, 0, "add");
    // // 绑定到ActionBar
    // add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    // return true;
    // }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        pagenumber = 1;
    }

    public void onResume() {
        super.onResume();
        if (date == "today") {
            getHistoryOrders("today", ptype);// 从网络上获取数据
        } else {
            getHistoryOrders("last", ptype);// 从网络上获取数据
        }
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        adapter.removeOrders();
        pagenumber = 1;
        count = 0;
    }
}
