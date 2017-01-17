package com.tq.zld.view.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.rey.material.widget.TabPageIndicator;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.fragment.AccountTicketsFragment;
import com.tq.zld.view.map.WebActivity;

public class AccountTicketsActivity extends BaseActivity {

    ViewPager mViewPager;
    TabPageIndicator mTitleTabs;

    ImageButton mPlayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);
        initToolbar();
        initView();
    }

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case 1:// 右边“帮助”按钮的id
                startCouponHelpActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startCouponHelpActivity() {
        Intent intent = new Intent(getApplicationContext(), WebActivity.class);
        intent.putExtra(WebActivity.ARG_TITLE, "停车券帮助");
//        intent.putExtra(WebActivity.ARG_URL, TCBApp.mServerUrl + "ticket.jsp");
        intent.putExtra(WebActivity.ARG_URL, getString(R.string.url_coupon_help));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, "帮助")
                .setIcon(R.drawable.ic_action_help)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    private void initToolbar() {
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar_ticket);
        bar.setBackgroundColor(getResources().getColor(R.color.bg_toolbar));
        bar.setTitle("我的停车券");
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void setPlayButtonVisibility(int visibility) {
        mPlayButton.setVisibility(visibility);
    }

    private void goToPlay() {
        Intent intent = new Intent(TCBApp.getAppContext(), WebActivity.class);
        intent.putExtra(WebActivity.ARG_TITLE, "停车挑战");
        intent.putExtra(WebActivity.ARG_URL, TCBApp.mServerUrl + "cargame.do?action=playgame&mobile=" + TCBApp.mMobile);
        startActivity(intent);
    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.vp_ticket);
        AccountTicketsPageAdapter mAccountTicketsPageAdapter = new AccountTicketsPageAdapter(
                getSupportFragmentManager());
        mViewPager.setAdapter(mAccountTicketsPageAdapter);
        mTitleTabs = (TabPageIndicator) findViewById(R.id.tab_ticket);
        mTitleTabs.setViewPager(mViewPager, 0);

        mPlayButton = (ImageButton) findViewById(R.id.ib_ticket_play);
        mPlayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPlay();
            }
        });

        findViewById(R.id.btn_ticket_buy).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_BUY_TICKET);
                startActivity(intent);
            }
        });
    }

    class AccountTicketsPageAdapter extends FragmentPagerAdapter {

        private final String[] mTitles = new String[]{"当 前", "历 史"};

        public AccountTicketsPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public Fragment getItem(int position) {
            return AccountTicketsFragment.newInstance(position);
        }
    }
}
