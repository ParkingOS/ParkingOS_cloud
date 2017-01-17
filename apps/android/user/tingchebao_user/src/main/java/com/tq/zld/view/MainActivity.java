package com.tq.zld.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.tq.zld.R;
import com.tq.zld.view.fragment.AccountFragment;
import com.tq.zld.view.fragment.AddFriendFragment;
import com.tq.zld.view.fragment.BuyTicketFragment;
import com.tq.zld.view.fragment.ChooseParkingFeeCollectorFragment;
import com.tq.zld.view.fragment.ChooseTicketFragment;
import com.tq.zld.view.fragment.CommentParkFragment;
import com.tq.zld.view.fragment.FriendFragment;
import com.tq.zld.view.fragment.HistoryOrderFragment;
import com.tq.zld.view.fragment.InputMoneyFragment;
import com.tq.zld.view.fragment.MessageFragment;
import com.tq.zld.view.fragment.NewFriendFragment;
import com.tq.zld.view.fragment.OrderDetailFragment;
import com.tq.zld.view.fragment.ReceiveMergeFragment;
import com.tq.zld.view.fragment.RechargeFragment;
import com.tq.zld.view.fragment.ResultMergeFragment;
import com.tq.zld.view.fragment.SelectTicketMergeFragment;
import com.tq.zld.view.fragment.SendVerifyFragment;
import com.tq.zld.view.fragment.SettingFragment;
import com.tq.zld.view.fragment.ShareFragment;
import com.tq.zld.view.map.WebActivity;
import com.umeng.fb.fragment.FeedbackFragment;

public class MainActivity extends BaseActivity {

    /**
     * 设置默认显示Fragment
     */
    public static final String ARG_FRAGMENT = "fragment";
    /**
     * 默认显示的Fragment参数
     */
    public static final String ARG_FRAGMENT_ARGS = "args";

    public static final int FRAGMENT_ORDER_DETAIL = 0;
    public static final int FRAGMENT_HISTORY_ORDER = 1;
    public static final int FRAGMENT_ACCOUNT = 2;
    public static final int FRAGMENT_MESSAGE_CENTER = 3;
    public static final int FRAGMENT_SHARE = 4;
    public static final int FRAGMENT_CHOOSE_COLLECTOR = 5;
    public static final int FRAGMENT_INPUT_MONEY = 6;
    public static final int FRAGMENT_SETTING = 7;
    public static final int FRAGMENT_TICKET = 8;
    public static final int FRAGMENT_FEEDBACK = 9;
    public static final int FRAGMENT_COMMENT_PARK = 10;
    public static final int FRAGMENT_RECHARGE = 11;
    public static final int FRAGMENT_BUY_TICKET = 12;
    @Deprecated
    public static final int FRAGMENT_FRIEND = 13;
    public static final int FRAGMENT_RECEIVE_MERGE = 14;
    public static final int FRAGMENT_RESULT_MERGE = 15;
    public static final int FRAGMENT_TICKET_MERGE = 16;
    public static final int FRAGMENT_ADD_FRIEND = 17;
    public static final int FRAGMENT_SEND_VIRIFY = 18;
    @Deprecated
    public static final int FRAGMENT_NEW_FRIEND = 19;

    private Toolbar mBar;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        if (savedInstanceState == null) {
            setDefaultFragment();
        }
    }

    private void setDefaultFragment() {
        Fragment fragment;
        Bundle args = getIntent().getBundleExtra(ARG_FRAGMENT_ARGS);
        switch (getIntent().getIntExtra(ARG_FRAGMENT, FRAGMENT_HISTORY_ORDER)) {
            case FRAGMENT_HISTORY_ORDER:
                fragment = new HistoryOrderFragment();
                break;
            case FRAGMENT_ACCOUNT:
                fragment = new AccountFragment();
                break;
            case FRAGMENT_MESSAGE_CENTER:
                fragment = new MessageFragment();
                break;
            case FRAGMENT_SHARE:
                fragment = new ShareFragment();
                break;
            case FRAGMENT_ORDER_DETAIL:
                fragment = new OrderDetailFragment();
                break;
            case FRAGMENT_CHOOSE_COLLECTOR:
                fragment = new ChooseParkingFeeCollectorFragment();
                break;
            case FRAGMENT_INPUT_MONEY:
                fragment = new InputMoneyFragment();
                break;
            case FRAGMENT_SETTING:
                fragment = new SettingFragment();
                break;
            case FRAGMENT_TICKET:
                fragment = new ChooseTicketFragment();
                break;
            case FRAGMENT_FEEDBACK:
                fragment = FeedbackFragment.newInstance(args
                        .getString(FeedbackFragment.BUNDLE_KEY_CONVERSATION_ID));
                setTitle("反馈");
                break;
            case FRAGMENT_COMMENT_PARK:
                fragment = new CommentParkFragment();
                break;
            case FRAGMENT_RECHARGE:
                fragment = new RechargeFragment();
                break;
            case FRAGMENT_BUY_TICKET:
                fragment = new BuyTicketFragment();
                break;
            case FRAGMENT_FRIEND:
                fragment = new FriendFragment();
                break;
            case FRAGMENT_RECEIVE_MERGE:
                fragment = new ReceiveMergeFragment();
                break;
            case FRAGMENT_RESULT_MERGE:
                fragment = new ResultMergeFragment();
                break;
            case FRAGMENT_TICKET_MERGE:
                fragment = new SelectTicketMergeFragment();
                break;

            case FRAGMENT_ADD_FRIEND:
                fragment = new AddFriendFragment();
                break;

            case FRAGMENT_SEND_VIRIFY:
                fragment = new SendVerifyFragment();
                break;
            case FRAGMENT_NEW_FRIEND:
                fragment = new NewFriendFragment();
                break;

            default:
                fragment = new HistoryOrderFragment();
                break;
        }
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment,
                        fragment.getClass().getSimpleName()).commit();
    }

    private void initToolbar() {
        mBar = (Toolbar) findViewById(R.id.widget_toolbar);
        mBar.setBackgroundColor(getResources().getColor(R.color.bg_toolbar));
        setSupportActionBar(mBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mBar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        disableAllMenuItems();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_recharge_help:
                onRechargeHelpMenuClicked();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 隐藏所有菜单
     */
    public void disableAllMenuItems() {
        for (int i = 0; i < mMenu.size(); i++) {
            MenuItem item = mMenu.getItem(i);
            item.setVisible(false);
            item.setEnabled(false);
        }
    }

    /**
     * 设置某个菜单可用
     *
     * @param menuItemId
     */
    public void setMenuItemEnabled(int menuItemId) {
        MenuItem item = mMenu.findItem(menuItemId);
        if (item != null) {
            item.setEnabled(true);
            item.setVisible(true);
        }
    }

    /**
     * 设置某个菜单不可用
     *
     * @param menuItemId
     */
    public void setMenuItemDisabled(int menuItemId) {
        MenuItem item = mMenu.findItem(menuItemId);
        if (item != null) {
            item.setEnabled(false);
            item.setVisible(false);
        }
    }


    private void onRechargeHelpMenuClicked() {
        Intent intent = new Intent(getApplicationContext(), WebActivity.class);
        intent.putExtra(WebActivity.ARG_TITLE, "账户充值说明");
        intent.putExtra(WebActivity.ARG_URL, getString(R.string.url_recharge_help));
        startActivity(intent);
    }

    /**
     * 获取当前界面的Toolbar
     *
     * @return
     */
    public Toolbar getToolbar() {
        return mBar;
    }
}
