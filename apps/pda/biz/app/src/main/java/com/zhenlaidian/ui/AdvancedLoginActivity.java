package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.device.DeviceManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.zhenlaidian.R;
import com.zhenlaidian.adapter.DrawerAdapter;
import com.zhenlaidian.bean.DrawerItemInfo;
import com.zhenlaidian.engine.DrawerOnItemClick;
import com.zhenlaidian.util.Constant;

/**
 * Created by xulu on 2016/11/9.
 * 高级登录，用于允许home键和下拉屏幕
 */

public class AdvancedLoginActivity extends BaseActivity {
    ActionBar actionBar;
    private DrawerLayout drawerLayout = null;
    private ListView lv_left_drawer;
    private ActionBarDrawerToggle mDrawerToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_advancedlogin);
        initView();
        initActionBar();
        lv_left_drawer.setAdapter(new DrawerAdapter(DrawerItemInfo.getInstance(), AdvancedLoginActivity.this));
        lv_left_drawer.setOnItemClickListener(new DrawerOnItemClick(this, drawerLayout, this));
        lv_left_drawer.setScrollingCacheEnabled(false);

        device.enableHomeKey(false);
        device.enableStatusBar(false);
    }

    RelativeLayout lnhome, lnbar;
    CheckBox cbHome, cbBar;
    DeviceManager device;
    View v;
    AlertDialog dialog;
    EditText password;
    boolean needpop = false;

    private void initView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_my_home_drawer_layout);
        lv_left_drawer = (ListView) findViewById(R.id.ll_my_home_left_drawer);
        findViewById(R.id.lnenable_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbHome.setChecked(true);
            }
        });
        findViewById(R.id.lnenable_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbBar.setChecked(true);
            }
        });
        cbHome = ((CheckBox) findViewById(R.id.enable_home));
        cbBar = ((CheckBox) findViewById(R.id.enable_pull));
        device = new DeviceManager();
        cbHome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                device.enableHomeKey(isChecked);
            }
        });
        cbBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                device.enableStatusBar(isChecked);
            }
        });
        v = LayoutInflater.from(context).inflate(R.layout.x_item_checkpwd, null);
        password = ((EditText) v.findViewById(R.id.pwd));
        dialog = new AlertDialog.Builder(AdvancedLoginActivity.this).setTitle("高级密码").setIcon(R.drawable.app_icon_32)
                .setView(v).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (password.getText().toString().equals(Constant.AdvancePw)) {
                            needpop = false;
                        } else {
                            needpop = true;
                        }
                    }
                })
                .setCancelable(false).create();
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogs) {
                if (needpop) {
                    password.setText("");
                    dialog.show();
                }
            }
        });
    }

    private void initActionBar() {
        drawerLayout.setDrawerListener(new MyDrawerListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_am, R.string.hello_world,
                R.string.hello_world);
        mDrawerToggle.syncState();
        actionBar = getSupportActionBar();
        actionBar.setTitle("高级登录");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
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
            actionBar.setTitle("高级登录");
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

    // actionBar的点击回调方法
    @SuppressLint("RtlHardcoded")
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
        }
        return super.onOptionsItemSelected(item);
    }
}




