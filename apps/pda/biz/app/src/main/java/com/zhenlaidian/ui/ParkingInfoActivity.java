package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.zhenlaidian.bean.DrawerItemInfo;
import com.zhenlaidian.bean.ParkingInfo;
import com.zhenlaidian.engine.DrawerOnItemClick;
import com.zhenlaidian.ui.park_account.ParkingAccountActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

/**
 * 停车场界面;
 */
public class ParkingInfoActivity extends BaseActivity {

    private DrawerLayout drawerLayout = null;
    private ListView lv_left_drawer;
    private ImageView iv_call_phone;
    private TextView tv_parking_info;// 停车场信息
    private LinearLayout ll_parking_account;// 停车场账户
    private LinearLayout ll_parking_price;// 停车场价格
    private ActionBarDrawerToggle mDrawerToggle;
    private ActionBar actionBar;
    private TextView today_cost_tv;
    private TextView tv_parkname;//车场名字
    private TextView tv_parktotal;//车位总数
    private TextView tv_parktype;//车场类型
    private SharedPreferences autologin;
    private ParkingInfo parkingInfo;
    private String role;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parking_info_activity);
        drawerLayout = (DrawerLayout) findViewById(R.id.parkingdrawer_layout);
        lv_left_drawer = (ListView) findViewById(R.id.left_drawer);
        lv_left_drawer.setAdapter(new DrawerAdapter(DrawerItemInfo.getInstance(), ParkingInfoActivity.this));
        lv_left_drawer.setOnItemClickListener(new DrawerOnItemClick(this, drawerLayout,this));
        lv_left_drawer.setScrollingCacheEnabled(false);// 设置抽屉的listview不能滑动；
        initActionBar();
        initView();
        if (token == null) {
            Intent intent = new Intent(this, LeaveActivity.class);
            startActivity(intent);
        } else {
            getParkInfo();
        }
        setView();
    }

    public void initView() {
        tv_parking_info = (TextView) findViewById(R.id.tv_parking_parking_info);
        ll_parking_account = (LinearLayout) findViewById(R.id.ll_parking_parking_account);
        ll_parking_price = (LinearLayout) findViewById(R.id.ll_parking_parking_price);
        today_cost_tv = (TextView) findViewById(R.id.today_cost_tv);
        tv_parkname = (TextView) findViewById(R.id.tv_parking_parking_name);
        tv_parktotal = (TextView) findViewById(R.id.tv_parking_parking_total);
        tv_parktype = (TextView) findViewById(R.id.tv_parking_parkinginfo_type);
        iv_call_phone = (ImageView) findViewById(R.id.iv_parking_info_phone);
    }

    public void setView() {

        autologin = getSharedPreferences("autologin", Context.MODE_PRIVATE);
        role = autologin.getString("role", "0");

        tv_parking_info.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParkingInfoActivity.this, ParkingActivity.class);
                if (parkingInfo != null && parkingInfo.getName() != null) {
                    intent.putExtra("parkinfo", parkingInfo);
                }
                startActivity(intent);
            }
        });

        ll_parking_account.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (role.equals("2")) {
                    Toast.makeText(ParkingInfoActivity.this, "抱歉,您没有权限查看详情！", Toast.LENGTH_SHORT).show();
                } else {
//					Toast.makeText(ParkingInfoActivity.this,"此功能尚未完善,敬请期待！", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ParkingInfoActivity.this,
                            ParkingAccountActivity.class);
                    startActivity(intent);
                }

            }
        });
        ll_parking_price.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent priceintent = new Intent(ParkingInfoActivity.this, ShowPriceSettingActivity.class);
                ParkingInfoActivity.this.startActivity(priceintent);
            }
        });
        iv_call_phone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneintent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + "01056450585"));
                startActivity(phoneintent);
            }
        });
    }

    public void setParkingInfo(ParkingInfo parkingInfo) {

        if (parkingInfo.getName() != null) {
            tv_parkname.setText(parkingInfo.getName());
        }

        if (parkingInfo.getParkingtotal() != null) {
            tv_parktotal.setText("车位数：" + parkingInfo.getParkingtotal());
        }

        if (parkingInfo.getParktype() != null && parkingInfo.getParktype().equals("地面")) {  //0:地面1:地下2占道
            tv_parktype.setBackgroundResource(R.drawable.parktype_ground);
        } else if (parkingInfo.getParktype() != null && parkingInfo.getParktype().equals("地下")) {
            tv_parktype.setBackgroundResource(R.drawable.parktype_underground);
        } else if (parkingInfo.getParktype() != null && parkingInfo.getParktype().equals("占道")) {
            tv_parktype.setBackgroundResource(R.drawable.parktype_use_road);
        }
    }

    public void initActionBar() {
        drawerLayout.setDrawerListener(new MyDrawerListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.ic_drawer_am, R.string.hello_world,
                R.string.hello_world);
        mDrawerToggle.syncState();
        actionBar = getSupportActionBar();
        actionBar.setTitle("停车场");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setDisplayHomeAsUpEnabled(true);
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
            actionBar.setTitle("停车场");
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

    // http://s.zhenlaidian.com/zld/collectorrequest.do?action=cominfo&token=*
    public void getParkInfo() {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取信息失败，请检查网络！", 0).show();
            return;
        }
        AQuery aQuery = new AQuery(ParkingInfoActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=cominfo&token="
                + token + "&out=json";
        MyLog.w("ParkingInfoActivity-->>", "请求车场信息的URL-->>" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取车场信息数据...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (object != null) {
                    getAccount();
                    dialog.dismiss();
                    Gson gson = new Gson();
                    parkingInfo = gson.fromJson(object, ParkingInfo.class);
                    MyLog.i("ParkingInfoActivity-->>", "解析的车场信息为" + parkingInfo.toString());
                    if (parkingInfo != null && parkingInfo.getParkingtotal() != null) {
                        setParkingInfo(parkingInfo);
                    }
                } else {
                    getAccount();
                    dialog.dismiss();
                }
            }

        });
    }

    public void getAccount() {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取信息失败，请检查网络！", 0).show();
            return;
        }
        AQuery aQuery = new AQuery(ParkingInfoActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=getparkaccount&token=" + token;
        MyLog.w("ParkingInfoActivity", "获取账户信息的url" + url);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (object != null && object.length() < 15) {
                    today_cost_tv.setText("您今日收费" + object + "元");
                    MyLog.i("ParkingInfoActivity", "解析账户余额是：" + object);
                } else {
                    today_cost_tv.setText("");
                    return;
                }
            }
        });
    }


    // 抽屉的回调方法；
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.w("ParkingInfoActivity", "onDestroy----->>>");
    }
    // // ActionBar添加菜单条目；
    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // super.onCreateOptionsMenu(menu);
    // // 添加菜单项；
    // MenuItem add = menu.add(0, 0, 0, "add");
    // // 绑定到ActionBar
    // add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    // return true;
    // }

}
