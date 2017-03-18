package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuCompat;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.google.gson.reflect.TypeToken;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.RankingAdapter;
import com.zhenlaidian.bean.RankingInfo;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

import java.util.ArrayList;

/**
 * 积分排行榜界面;
 */
@SuppressLint("InlinedApi")
public class RankingActivity extends BaseActivity {

    private ImageView ivHasRead;// 标识规则是否读过的小红点
    private TextView tv_myrank;// 我的排名
    private TextView tv_myscroe;// 我的积分
    private ListView lv_rank;
    private Button bt_last_rank;
    private LinearLayout ll_last_rule;// 规则
    private TextView tv_mark_details;// 积分详情
    private LinearLayout ll_ranking;
    private TextView tv_null;
    private RankingAdapter adapter;
    private ArrayList<RankingInfo> infos;
    private String myRank;// 我的排名
    private long lasttime;
    private SharedPreferences config;// 当前用户的配置文件
    private SharedPreferences autologin;// 存储当前用户信息
    private long updateTime;// 积分规则最后更新时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.ranking_activity);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();

        initView();
        adapter = new RankingAdapter(this);
        autologin = getSharedPreferences("autologin", MODE_PRIVATE);
        config = getSharedPreferences(autologin.getString("account", ""), MODE_PRIVATE);
        getRankInfo();
    }

    public void initView() {
        ivHasRead = (ImageView) findViewById(R.id.iv_ranking_hasread);
        tv_myrank = (TextView) findViewById(R.id.tv_ranking_myrank);
        tv_myscroe = (TextView) findViewById(R.id.tv_ranking_myscroe);
        lv_rank = (ListView) findViewById(R.id.lv_ranking_activity);
        tv_null = (TextView) findViewById(R.id.tv_ranking_null);
        ll_ranking = (LinearLayout) findViewById(R.id.ll_ranking_activity);
        bt_last_rank = (Button) findViewById(R.id.bt_ranking_lastrank);
        ll_last_rule = (LinearLayout) findViewById(R.id.ll_ranking_freshen);
        tv_mark_details = (TextView) findViewById(R.id.tv_ranking_details);
        bt_last_rank.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RankingActivity.this, LastRankingactivity.class);
                startActivity(intent);
            }
        });
        // 点击进入积分规则
        ll_last_rule.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RankingActivity.this, ScoreRuleActivity.class);
                startActivity(intent);
                // 记录积分规则最后更新时间
                config.edit().putLong("ruleUpdateTime", updateTime).commit();
            }
        });
        // 点击进入积分详情
        tv_mark_details.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RankingActivity.this, MyScoreActivity.class);
                intent.putExtra("rank", myRank);
                startActivity(intent);
            }
        });
    }

    public void setView(ArrayList<RankingInfo> infos) {
        boolean flag = false;
        for (RankingInfo rankingInfo : infos) {
            if (rankingInfo.getUin().equals(useraccount)) {
                tv_myrank.setText("第 " + rankingInfo.getSort() + " 名");
                myRank = rankingInfo.getSort();
                tv_myscroe.setText(rankingInfo.getScore());
                flag = true;
            }
        }
        if (flag == false) {
            tv_myrank.setText("您不在排名中！");
            tv_myscroe.setText("点击详情查看");
        }
    }

    public void setAdapter() {
        lv_rank.setAdapter(adapter);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ranking_refresh, menu);
        MenuCompat.setShowAsAction(menu.findItem(R.id.refresh), MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                RankingActivity.this.finish();
                return true;
            case R.id.refresh:
                if (System.currentTimeMillis() - lasttime >= 5000 && infos != null) {
                    infos.clear();
                    adapter.notifyDataSetChanged();
                    getRankInfo();
                }
                lasttime = System.currentTimeMillis();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // http://s.zhenlaidian.com/zld/collectorsort.do?action=query&type=client&week=last
    // test:http://127.0.0.1/zld/collectorrequest.do?action=score&token=dc27939d5721bd58cd268479d667af33&week=
    public void getRankInfo() {
        String path = baseurl;
        String url = path + "collectorrequest.do?action=score&token=" + token;
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", 0).show();
            return;
        }
        MyLog.w("RankingActivity", "访问排行榜的URL--->" + url);
        AQuery aQuery = new AQuery(this);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取本周排行榜...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (!TextUtils.isEmpty(object)) {
                    MyLog.v("RankingActivity", "获取到的排名信息为--->" + object.toString());
                    dialog.dismiss();
                    Gson gson = new Gson();
                    infos = gson.fromJson(object, new TypeToken<ArrayList<RankingInfo>>() {
                    }.getType());
                    if (infos != null && !infos.isEmpty()) {
                        MyLog.i("RankingActivity", "解析到的排名信息为--->" + infos.toString());
                        setView(infos);
                        adapter.setRankInfo(infos, RankingActivity.this);
                    } else {
                        tv_null.setVisibility(View.VISIBLE);
                        ll_ranking.setVisibility(View.INVISIBLE);
                    }
                } else {
                    dialog.dismiss();
                }
            }

        });
    }

    public void onResume() {
        super.onResume();
        checkIfRuleHasUpdated();
    }

    private void checkIfRuleHasUpdated() {
        new AQuery(this).ajax(baseurl + "lalarule", String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                updateTime = config.getLong("ruleUpdateTime", 0);
                if (!TextUtils.isEmpty(object) && TextUtils.isDigitsOnly(object)) {
                    MyLog.w("RankingActivity", "Score Rule Update Time: --->> " + object);
                    updateTime = Long.parseLong(object);
                }
                checkHasReadRule(updateTime);
                super.callback(url, object, status);
            }
        });
    }

    /**
     * 检查是否读过积分规则：读过会在本地名为当前用户帐户名的SharedPrefrences中存储名为“hasReadRule”
     * 的boolean值为true
     */
    private void checkHasReadRule(long updateTime) {
        if (config.getLong("ruleUpdateTime", 0) < updateTime) {
            ivHasRead.setVisibility(View.VISIBLE);
        } else {
            ivHasRead.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}
