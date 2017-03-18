package com.zhenlaidian.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.MenuCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.zxing.lswss.QRCodeEncoder;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.DrawerAdapter;
import com.zhenlaidian.bean.DrawerItemInfo;
import com.zhenlaidian.engine.DrawerOnItemClick;
import com.zhenlaidian.photo.InputCarNumberActivity;
import com.zhenlaidian.util.SharedPreferencesUtils;

/**
 * 推荐用户界面;
 */
public class RecommendOwnersActivity extends BaseActivity {

    private ActionBar actionBar;
    private DrawerLayout drawerLayout = null;
    private ListView lv_left_drawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView tv_vip;
    private TextView tv_vipcard;
    private ImageView iv_call_phone;
    private ImageView iv_code_img;
    private Bitmap codebmp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.make_vip_card_activity);
        initActionBar();
        initView();
        if (codebmp == null) {
            String codeinfo = SharedPreferencesUtils.getIntance(this).getCode();
            createQrcode(codeinfo);
        }
        lv_left_drawer.setAdapter(new DrawerAdapter(DrawerItemInfo.getInstance(), RecommendOwnersActivity.this));
        lv_left_drawer.setOnItemClickListener(new DrawerOnItemClick(this, drawerLayout,this));
        lv_left_drawer.setScrollingCacheEnabled(false);
    }

    private void initView() {
        tv_vip = (TextView) findViewById(R.id.tv_recommend_owners_vip);
        tv_vipcard = (TextView) findViewById(R.id.tv_recommend_owners_vip_card);
        iv_code_img = (ImageView) findViewById(R.id.iv_recommend_owners_code);
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_make_vip_drawer_layout);
        iv_call_phone = (ImageView) findViewById(R.id.iv_make_vip_phone);
        drawerLayout.setDrawerListener(new MyDrawerListener());
        lv_left_drawer = (ListView) findViewById(R.id.ll_make_vip_left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_am, R.string.hello_world,
                R.string.hello_world);
        mDrawerToggle.syncState();
        tv_vip.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent vipintent = new Intent(RecommendOwnersActivity.this, InputCarNumberActivity.class);
                vipintent.putExtra("add", "recommend");
                startActivity(vipintent);
            }
        });
        tv_vipcard.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecommendOwnersActivity.this, MakeVIPCardActivity.class);
                startActivity(intent);
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

    private void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setTitle("开通车主会员");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressWarnings("deprecation")
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recommend_owners_actionbar, menu);
        MenuCompat.setShowAsAction(menu.findItem(R.id.recommend_owners), MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
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
            actionBar.setTitle("会员开卡");
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

    public void createQrcode(String info) {
        if (codebmp == null) {
            QRCodeEncoder d = new QRCodeEncoder();
            codebmp = d.encode2BitMap(info, 400, 400);
            iv_code_img.setImageBitmap(codebmp);
        } else {
            iv_code_img.setImageBitmap(codebmp);
        }
    }

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
            case R.id.recommend_owners:
                Intent intent = new Intent(RecommendOwnersActivity.this, RecommendRecordActivity.class);
                intent.putExtra("type", "0");
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
