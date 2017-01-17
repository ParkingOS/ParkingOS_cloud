package com.tq.zld.view.map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.fragment.RemarkFragment2;

/**
 * Author：ClareChen
 * E-mail：ggchaifeng@gmail.com
 * Date：  15/6/25 下午7:55
 * <p/>
 * 用于展示停车位置界面
 */
public class RemarkActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        setDefaultFragment();
    }

    private void setDefaultFragment() {
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container, new RemarkFragment2()).commit();
    }

    private void initToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.black));
//            getWindow().setNavigationBarColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.widget_toolbar);
        toolbar.setBackgroundColor(Color.BLACK);
        toolbar.setTitle(getString(R.string.label_remark));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.menu_remark);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_remark_delete) {
                    showDeleteDialog();
                    return true;
                }
                return false;
            }
        });
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this).setMessage("确认删除停车位置标记？")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // 删除本地存储的停车标记相关
                        String delete = getString(R.string.sp_remark_delete);
                        String lat = getString(R.string.sp_remark_latitude);
                        String lng = getString(R.string.sp_remark_longitude);
                        String tips = getString(R.string.sp_remark_tips);
                        String time = getString(R.string.sp_remark_time);
                        String located = getString(R.string.sp_remark_located);
                        String floor = getString(R.string.sp_remark_floor);
                        TCBApp.getAppContext().getConfigPrefs().edit().remove(delete).remove(lat).remove(lng)
                                .remove(tips).remove(time).remove(located).remove(floor).commit();
                        Toast.makeText(RemarkActivity.this, "已删除~", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }).setNegativeButton("取消", null).show();
    }

}
