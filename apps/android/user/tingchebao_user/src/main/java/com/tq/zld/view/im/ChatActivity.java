package com.tq.zld.view.im;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.im.bean.User;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.fragment.ChatFragment;
import com.tq.zld.view.holder.MenuHolder;

/**
 * Created by GT on 2015/9/17.
 */
public class ChatActivity extends BaseActivity {

    private ChatFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initToolbar();
        initData(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        TCBApp.getAppContext().hxsdkHelper.pushActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        TCBApp.getAppContext().hxsdkHelper.popActivity(this);
    }

    private void initToolbar() {
        Toolbar mBar = (Toolbar) findViewById(R.id.widget_toolbar);
        mBar.setBackgroundColor(getResources().getColor(R.color.bg_toolbar));
        setSupportActionBar(mBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mBar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initData(Intent intent){
        User friend = intent.getParcelableExtra(ChatFragment.USER);
        String plate = friend.getPlate();
        if (TextUtils.isEmpty(plate)) {
            plate = "车牌未知";
        }
        setTitle(plate);
        fragment = new ChatFragment();
        fragment.setArguments(intent.getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment,
                        fragment.getClass().getSimpleName()).commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtils.i("onNewIntent");
        boolean b = intent.getBooleanExtra(ChatFragment.IS_NOTIFICATION, false);
        if (b) {
            LogUtils.i("IS_NOTIFICATION");
            //清楚侧滑菜单中的消息提示
            MenuHolder.getInstance().refreshMenu(R.id.rl_menu_friend, false);
            initData(intent);
        }

    }

}
