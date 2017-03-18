package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
 * 上周排行榜;
 */
@SuppressLint("NewApi")
public class LastRankingactivity extends BaseActivity {
    public static final String TAG = "LastRankingactivity";
    private TextView tv_myrank;//我的排名
    private TextView tv_myscroe;//我的积分
    private TextView tv_title;//标题
    private ListView lv_rank;
    private Button bt_last_rank;
    private DrawerLayout drawerLayout;
    private LinearLayout ll_last_freshen;
    private RankingAdapter adapter;
    private LinearLayout ll_ranking;
    private TextView tv_null;
    private TextView tv_mark_details;//积分详情
    private TextView tv_show_my_rank;//我的上周排名(提示：)
    private TextView tv_show_my_scroe;//我的上周积分(提示：)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.ranking_activity_main);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        adapter = new RankingAdapter(this);
        initView();
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);//关闭抽屉
        getRankInfo();
    }

    public void initView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.ranking_draw_layout);
        tv_myrank = (TextView) findViewById(R.id.tv_ranking_myrank);
        tv_myscroe = (TextView) findViewById(R.id.tv_ranking_myscroe);
        tv_title = (TextView) findViewById(R.id.tv_ranking_tital);
        lv_rank = (ListView) findViewById(R.id.lv_ranking_activity);
        bt_last_rank = (Button) findViewById(R.id.bt_ranking_lastrank);
        tv_null = (TextView) findViewById(R.id.tv_ranking_null);
        ll_ranking = (LinearLayout) findViewById(R.id.ll_ranking_activity);
        ll_last_freshen = (LinearLayout) findViewById(R.id.ll_ranking_freshen);
        tv_mark_details = (TextView) findViewById(R.id.tv_ranking_details);
        tv_show_my_rank = (TextView) findViewById(R.id.tv_ranking_my_ranking);
        tv_show_my_scroe = (TextView) findViewById(R.id.tv_ranking_my_scroe);
    }

    public void setView(ArrayList<RankingInfo> infos) {
        boolean flag = false;
        for (RankingInfo rankingInfo : infos) {
            if (rankingInfo.getUin().equals(useraccount)) {
                tv_myrank.setText("第 " + rankingInfo.getSort() + " 名");
                tv_myscroe.setText(rankingInfo.getScore());
                flag = true;
            }
        }
        if (flag == false) {
            tv_myrank.setText("您不在排名中！");
            tv_myscroe.setText("点击详情查看");
        }
        tv_title.setText("上周排行榜");
        bt_last_rank.setVisibility(View.GONE);
        ll_last_freshen.setVisibility(View.GONE);
        tv_show_my_rank.setText("我的上周排名：");
        tv_show_my_scroe.setText("我的上周积分：");
        tv_mark_details.setVisibility(View.INVISIBLE);
        // 点击进入积分详情
//		tv_mark_details.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(LastRankingactivity.this, MyScoreActivity.class);
//				startActivity(intent);
//			}
//		});
    }

    public void setAdapter() {
        lv_rank.setAdapter(adapter);
    }

    //	http://s.zhenlaidian.com/zld/collectorsort.do?action=query&type=client&week=last
//test:http://127.0.0.1/zld/collectorrequest.do?action=score&token=dc27939d5721bd58cd268479d667af33&week=
    public void getRankInfo() {
        String path = baseurl;
        String url = path + "collectorrequest.do?action=score&token=" + token + "&week=last";
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", 0).show();
            return;
        }
        MyLog.w("RankingActivity", "访问上周排行榜的URL--->" + url);
        AQuery aQuery = new AQuery(this);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取上周排行榜...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (object != null && object != "") {
                    MyLog.i("RankingActivity", "获取到的上周排名信息为--->" + object.toString());
                    dialog.dismiss();
                    Gson gson = new Gson();
                    ArrayList<RankingInfo> infos = gson.fromJson(object, new TypeToken<ArrayList<RankingInfo>>() {
                    }.getType());
                    if (infos != null && !infos.isEmpty()) {
                        MyLog.i("RankingActivity", "解析到的上周排名信息为--->" + infos.toString());
                        setView(infos);
                        adapter.setLastRankInfo(infos, LastRankingactivity.this);
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

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                LastRankingactivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
