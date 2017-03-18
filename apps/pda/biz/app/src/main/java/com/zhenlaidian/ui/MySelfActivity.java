package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.DrawerAdapter;
import com.zhenlaidian.bean.DrawerItemInfo;
import com.zhenlaidian.bean.MyAccountInfo;
import com.zhenlaidian.bean.SysApplication;
import com.zhenlaidian.engine.DrawerOnItemClick;
import com.zhenlaidian.ui.person_account.MyWalletActivity;
import com.zhenlaidian.ui.score.RewardScoreActivity;
import com.zhenlaidian.util.CheckUtils;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 主页导航 我的界面;
 */
@SuppressWarnings("deprecation")
public class MySelfActivity extends BaseActivity {
    ActionBar actionBar;
    private DrawerLayout drawerLayout = null;
    private ListView lv_left_drawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private String account;
    private String username;
    private TextView tv_wallet;
    private TextView tv_name;
    private TextView tv_mobile;
    private TextView tv_account;
    private TextView tv_change;
    private ImageView iv_call_phone;
    private TextView tv_role;
    private RelativeLayout rl_wallet;
    private RelativeLayout rl_ranking;
    private RelativeLayout rl_reommend;
    private RelativeLayout rl_comment;
    private Boolean changeme = false;
    private RelativeLayout rl_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_home_activity);
        SysApplication.getInstance().addActivity(this);
        initView();
        initActionBar();
        lv_left_drawer.setAdapter(new DrawerAdapter(DrawerItemInfo.getInstance(), MySelfActivity.this));
        lv_left_drawer.setOnItemClickListener(new DrawerOnItemClick(this, drawerLayout,this));
        lv_left_drawer.setScrollingCacheEnabled(false);
        setButton();
        getAccountInfo();
        getBalanceInfo();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (changeme) {
            getAccountInfo();
            changeme = false;
        }
    }

    public void initView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_my_home_drawer_layout);
        lv_left_drawer = (ListView) findViewById(R.id.ll_my_home_left_drawer);
        tv_wallet = (TextView) findViewById(R.id.tv_myself_balance);
        tv_name = (TextView) findViewById(R.id.tv_myself_name);
        tv_mobile = (TextView) findViewById(R.id.tv_myself_mobile);
        tv_account = (TextView) findViewById(R.id.tv_myself_account);
        tv_change = (TextView) findViewById(R.id.tv_myself_change);
        tv_role = (TextView) findViewById(R.id.tv_myself_role);
        rl_wallet = (RelativeLayout) findViewById(R.id.rl_myself_wallet);
        rl_ranking = (RelativeLayout) findViewById(R.id.rl_myself_ranking);
        rl_reommend = (RelativeLayout) findViewById(R.id.rl_myself_reommend);
        rl_comment = (RelativeLayout) findViewById(R.id.rl_myself_comment);
        rl_setting = (RelativeLayout) findViewById(R.id.rl_myself_setting);
        iv_call_phone = (ImageView) findViewById(R.id.iv_myhome_photo);
    }

    public void setView(MyAccountInfo info) {
        if (info.getPic() != null) {
            if (info.getPic().equals("0")) {
                SharedPreferencesUtils.getIntance(this).setIsCardCheck(false);
            } else if (info.getPic().equals("1")) {
                SharedPreferencesUtils.getIntance(this).setIsCardCheck(true);
            }
        }
        if (info.getMobile() != null&& !info.getMobile().equals("null")) {
            tv_mobile.setText(info.getMobile());
        }
        if (info.getName() != null) {
            tv_name.setText(info.getName());
            username = info.getName();
        }
        if (info.getRole() != null && info.getRole().equals("管理员")) {
            tv_role.setBackgroundResource(R.drawable.me_controller);
        } else {
            tv_role.setBackgroundResource(R.drawable.me_toll_collector);
        }
        if (info.getUin() != null) {
            tv_account.setText(info.getUin());
            account = info.getUin();
        }
    }

    public void setButton() {
        tv_change.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 点击去我的信息界面
                Intent intent = new Intent(MySelfActivity.this, MyHomeActivity.class);
                startActivity(intent);
                changeme = true;
            }
        });
        rl_ranking.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-点击去排行榜界面
//				Intent rankingIntent = new Intent(MySelfActivity.this, RankingActivity.class);
//				startActivity(rankingIntent);
                Intent intent = new Intent(MySelfActivity.this, RewardScoreActivity.class);
                startActivity(intent);
            }
        });
        rl_reommend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-点击去推荐界面
                Intent recommendIntent = new Intent(MySelfActivity.this, RecommendCashierActivity.class);
                startActivity(recommendIntent);
            }
        });
        rl_comment.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-点击去我收到的评论；
                Intent recommendIntent = new Intent(MySelfActivity.this, MyReceivedCommentActivity.class);
                startActivity(recommendIntent);
            }
        });
        rl_setting.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-点击去设置界面
                Intent setIntent = new Intent(MySelfActivity.this, SetActivity.class);
                startActivity(setIntent);
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

    public void initActionBar() {
        drawerLayout.setDrawerListener(new MyDrawerListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_am, R.string.hello_world,
                R.string.hello_world);
        mDrawerToggle.syncState();
        actionBar = getSupportActionBar();
        actionBar.setTitle("我");
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
            actionBar.setTitle("我");
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

    // http://127.0.0.1/zld/collectorrequest.do?action=myinfo&token=747077c9c5456091217f16b36a50403f
    // {"name":"赵威","uin":"10414","role":"管理员","mobile":"18710233083",pic }
    public void getAccountInfo() {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "我的信息获取失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }
        String path = baseurl;
        String url = path + "collectorrequest.do?action=myinfo&token=" + token;
        MyLog.w("我的信息--URL---->>>", url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取我的信息...", true, true);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                super.callback(url, object, status);
                MyLog.v("获取的我的信息内容是---->>>", object);
                if (object != null && object != "") {
                    dialog.dismiss();
                    Gson gson = new Gson();
                    MyAccountInfo info = gson.fromJson(object, MyAccountInfo.class);
                    MyLog.i("解析到我的信息内容是---->>>", info.toString());
                    if (info != null) {
                        setView(info);
                    }
                } else {
                    dialog.dismiss();
                }
            }
        });
    }

    // http://192.168.199.240/zld/useraccount.do?action=getaccount&uid=11340
    // useraccount.do?action=getaccount&uid=11340
    public void getBalanceInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("autologin", Context.MODE_PRIVATE);
        String uid = sharedPreferences.getString("account", null);
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "余额信息获取失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }
        String path = baseurl;
        String url = path + "useraccount.do?action=getaccount&uid=" + uid;
        MyLog.w("余额信息--URL---->>>", url);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                super.callback(url, object, status);
                if (!TextUtils.isEmpty(object) && object.length() < 15) {
                    MyLog.i("获取余额信息内容是---->>>", object);
                    if (object.toString() != null) {
                        setBalanceView(object);
                        saveBanlance(object);
                    }
                } else {
                    // dialog.dismiss();
                }
            }
        });
    }

    /**
     * 我的余额信息
     *
     * @param
     */
    private void setBalanceView(final String banlance) {
        tv_wallet.setText(banlance);
        rl_wallet.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-我的钱包
                SharedPreferences myWalletFirst = getSharedPreferences("my_wallet_first", Context.MODE_PRIVATE);
                boolean is_first = myWalletFirst.getBoolean("is_first", true);
                if (is_first) {
                    saveFirstRecord();
                    buildFirstHintDialog();
                } else {
                    String name = tv_name.getText().toString().trim();
                    String mobile = tv_mobile.getText().toString().trim();
                    // 点击钱包,匹配姓名和手机号的正则
                    if (name == null || name.equals("请添加真实姓名") || mobile == null || mobile.equals("请添加手机号")) {
                        buildMatchHintDialog();
                        return;
                    } else {
                        if (isNameRight(name) && CheckUtils.MobileChecked(mobile)) {
                            Intent intent = new Intent(MySelfActivity.this, MyWalletActivity.class);
                            intent.putExtra("username", username);
                            intent.putExtra("account", account);
                            startActivity(intent);
                        } else {
                            buildMatchHintDialog();
                        }
                    }
                }
            }
        });
    }

    /**
     * 第一次点击钱包,弹出提示框
     */
    private void buildFirstHintDialog() {
        new AlertDialog.Builder(MySelfActivity.this).setTitle("友情提示").setIcon(R.drawable.app_icon_32)
                .setMessage("请确保你的手机号和姓名同排行榜上的一致,否则无法提现领奖。").setPositiveButton("知道了", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-进入我的钱包
                String name = tv_name.getText().toString().trim();
                String mobile = tv_mobile.getText().toString().trim();
                if (name == null || name.equals("请添加真实姓名") || mobile == null || mobile.equals("请添加手机号")) {
                    return;
                }
                Intent intent = new Intent(MySelfActivity.this, MyWalletActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("account", account);
                startActivity(intent);
            }
        }).setNegativeButton("去修改", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO 点击去我的信息界面；
                Intent intent = new Intent(MySelfActivity.this, MyHomeActivity.class);
                startActivity(intent);
            }
        }).create().show();
    }

    /**
     * 保存余额
     */
    private void saveBanlance(String banlance) {
        SharedPreferencesUtils.getIntance(this).setBanlance(banlance);
    }

    // /**
    // * 获取余额信息
    // */
    // private void getBanlance() {
    // SharedPreferencesUtils spu = new
    // SharedPreferencesUtils(MySelfActivity.this, "banlance");
    // banlance = spu.getBanlance();
    // }

    /**
     * 保存第一次点击钱包的记录
     */
    private void saveFirstRecord() {
        SharedPreferences myWalletFirst = getSharedPreferences("my_wallet_first", Context.MODE_PRIVATE);
        Editor firstedit = myWalletFirst.edit();
        firstedit.putBoolean("is_first", false);
        firstedit.commit();
    }

    /**
     * 点击钱包,匹配姓名和手机号的正则失败,弹出提示框
     */
    private void buildMatchHintDialog() {
        new AlertDialog.Builder(MySelfActivity.this).setTitle("友情提示").setIcon(R.drawable.app_icon_32)
                .setMessage("请填写正确的姓名和手机号,否则无法提现！").setNegativeButton("去修改", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO 点击去我的信息界面；
                Intent intent = new Intent(MySelfActivity.this, MyHomeActivity.class);
                startActivity(intent);
            }
        }).create().show();
    }

    /**
     * 匹配姓名正则
     */
    private boolean isNameRight(String name) {
        String check = "^[\u4e00-\u9fa5]*$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(name);
        return matcher.matches();
    }

}
