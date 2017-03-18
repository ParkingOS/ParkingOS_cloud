package com.zhenlaidian.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.ScoreInfo;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

/**
 * 我的积分界面;
 */
public class MyScoreActivity extends BaseActivity {

    private TextView tv_total_score;//总积分
    private TextView tv_my_rank;//我的排名
    private TextView tv_pull_score;//拉拉队积分
    private TextView tv_nfv_score;//nfc积分
    private TextView tv_sign_score;//扫牌积分
    private TextView tv_negative_score;//差评扣分
    private TextView tv_online_scroe;//在岗时长积分
    private TextView tv_recom_scroe;//推荐积分
    private TextView tv_exchange_score;//已兑换积分
    private Button bt_thisweek_mark;//本周积分
    private Button bt_total_mark;//总积分
    private String myRank;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        setContentView(R.layout.my_score_activity);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        myRank = getIntent().getStringExtra("rank");
        initView();
        if (myRank != null) {
            tv_my_rank.setText("我的本周排名" + myRank + "名");
        } else {
            tv_my_rank.setText("本周不在排名中！");
        }
        getScoreInfo("toweek");
    }

    private void initView() {
        tv_total_score = (TextView) findViewById(R.id.tv_total_rank);
        tv_my_rank = (TextView) findViewById(R.id.tv_mark_my_score);
        tv_pull_score = (TextView) findViewById(R.id.tv_mark_pull_score);
        tv_nfv_score = (TextView) findViewById(R.id.tv_mark_nfv_score);
        tv_sign_score = (TextView) findViewById(R.id.tv_mark_sign_score);
        tv_negative_score = (TextView) findViewById(R.id.tv_mark_negative_score);
        tv_exchange_score = (TextView) findViewById(R.id.tv_mark_exchange_score);
        tv_online_scroe = (TextView) findViewById(R.id.tv_mark_online_scroe);
        tv_recom_scroe = (TextView) findViewById(R.id.tv_mark_recom_score);
        bt_thisweek_mark = (Button) findViewById(R.id.bt_mark_thisweek_score);
        bt_total_mark = (Button) findViewById(R.id.bt_mark_total_score);
        bt_thisweek_mark.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                bt_thisweek_mark.setBackgroundResource(R.drawable.my_mark_left_white);
                bt_thisweek_mark.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
                bt_total_mark.setBackgroundResource(R.drawable.my_mark_right_green);
                bt_total_mark.setTextColor(getResources().getColor(R.color.white));
                if (myRank != null) {
                    tv_my_rank.setText("我的本周排名" + myRank + "名");
                } else {
                    tv_my_rank.setText("本周不在排名中！");
                }
                tv_my_rank.setClickable(false);
                getScoreInfo("toweek");
                setNullView();

            }
        });
        bt_total_mark.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                bt_thisweek_mark.setBackgroundResource(R.drawable.my_mark_left_green);
                bt_thisweek_mark.setTextColor(getResources().getColor(R.color.white));
                bt_total_mark.setBackgroundResource(R.drawable.my_mark_right_white);
                bt_total_mark.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
                tv_my_rank.setText("总积分兑换规则");
                tv_my_rank.setClickable(true);
                tv_my_rank.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
//						Toast.makeText(MyScoreActivity.this, "总积分计算规则", 0).show();
                    }
                });
                getScoreInfo("total");
                setNullView();
            }
        });
    }

    public void setView(ScoreInfo info) {

        if (info.getScore() != null) {
            tv_total_score.setText(info.getScore());
        } else {
            tv_total_score.setText("0");
        }
        if (info.getCashscore() != null) {
            tv_exchange_score.setText(info.getCashscore());
        } else {
            tv_exchange_score.setText("0");
        }
        if (info.getLala_scroe() != null) {
            tv_pull_score.setText(info.getLala_scroe());
        } else {
            tv_pull_score.setText("0");
        }
        if (info.getNfc_score() != null) {
            tv_nfv_score.setText(info.getNfc_score());
        } else {
            tv_nfv_score.setText("0");
        }
        if (info.getSign_score() != null) {
            tv_sign_score.setText(info.getSign_score());
        } else {
            tv_sign_score.setText("0");
        }
        if (info.getPraise_scroe() != null) {
            tv_negative_score.setText(info.getPraise_scroe());
        } else {
            tv_negative_score.setText("0");
        }
        if (info.getRecom_scroe() != null) {
            tv_recom_scroe.setText(info.getRecom_scroe());
        } else {
            tv_recom_scroe.setText("0");
        }
        if (info.getOnline_scroe() != null) {
            tv_online_scroe.setText(info.getOnline_scroe());
        } else {
            tv_online_scroe.setText("0");
        }
    }

    public void setNullView() {
        tv_total_score.setText("");
        tv_exchange_score.setText("");
        tv_pull_score.setText("");
        tv_nfv_score.setText("");
        tv_sign_score.setText("");
        tv_negative_score.setText("");
        tv_online_scroe.setText("");
    }

    //http://127.0.0.1/zld/collectorrequest.do?action=score&token=9711b22da53375d5b9cd2b8f2daa02d8&week=&detail=total
    //(toweek,total) //toweek 本周积分 total 总积分
    public void getScoreInfo(String detail) {
        String path = baseurl;
        String url = path + "collectorrequest.do?action=score&token=" + token + "&detail=" + detail;
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", 0).show();
            return;
        }
        MyLog.w("MyScoreActivity", "访问我的积分的URL--->" + url);
        AQuery aQuery = new AQuery(this);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取我的积分信息...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {

                if (object != null && object != "") {
                    dialog.dismiss();
                    MyLog.i("MyScoreActivity", "获取到的积分信息为--->" + object.toString());
                    Gson gson = new Gson();
                    ScoreInfo scoreInfo = gson.fromJson(object, ScoreInfo.class);
                    if (scoreInfo != null) {
                        setView(scoreInfo);
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
                MyScoreActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
