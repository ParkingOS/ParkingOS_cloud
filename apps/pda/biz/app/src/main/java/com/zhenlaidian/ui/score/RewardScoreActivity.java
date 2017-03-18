package com.zhenlaidian.ui.score;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.RewardScoreInfo;

import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.ui.ScoreRuleActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.SharedPreferencesUtils;

/**
 * 打赏积分榜
 * 
 * @author zhangyunfei 2015年7月14日
 */
public class RewardScoreActivity extends BaseActivity {

	private TextView tv_taday_score;// 今日积分
	private TextView tv_taday_score_ranking;// 今日积分排名
	private TextView tv_my_score; // 剩余积分
	private TextView tv_reward_what; // 什么是专用券
	private LinearLayout ll_taday_score;
	private LinearLayout ll_details;// 查看积分用途与说明
	private Button bt_send_tackets;// 发送专用券

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		setContentView(R.layout.reward_list);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
//		SysApplication.getInstance().addActivity(this);
		initView();
	}

	public void initView() {
		tv_taday_score = (TextView) findViewById(R.id.tv_reward_list_taday_score);
		tv_taday_score_ranking = (TextView) findViewById(R.id.tv_reward_list_taday_score_ranking);
		tv_my_score = (TextView) findViewById(R.id.tv_reward_my_score);
		tv_reward_what = (TextView) findViewById(R.id.tv_reward_what);
		ll_taday_score = (LinearLayout) findViewById(R.id.ll_reward_list_taday_score);
		ll_details = (LinearLayout) findViewById(R.id.ll_reward_list_details);
		bt_send_tackets = (Button) findViewById(R.id.bt_reward_send_tackets);
		tv_reward_what.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 专用卷说明；
				Intent intent = new Intent(RewardScoreActivity.this, ScoreRuleActivity.class);
				intent.putExtra("type", 3);
				startActivity(intent);
			}
		});
		ll_taday_score.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 打赏积分排行榜；
				Intent intent = new Intent(RewardScoreActivity.this, RewardRankingActivity.class);
				startActivity(intent);
			}
		});
		ll_details.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 积分用途说明；
				Intent intent = new Intent(RewardScoreActivity.this, ScoreRuleActivity.class);
				intent.putExtra("type", 2);
				startActivity(intent);

			}
		});

	}

	public void setView(RewardScoreInfo info) {

		if (!TextUtils.isEmpty(info.getScoreurl())) {
			SharedPreferencesUtils.getIntance(this).setScoreUrl(info.getScoreurl());
		}
		if (!TextUtils.isEmpty(info.getTicketurl())) {
			SharedPreferencesUtils.getIntance(this).setTicketUrl(info.getTicketurl());
		}
		tv_taday_score.setText(info.getTodayscore());
		if (info.getRank() != null) {
			tv_taday_score_ranking.setText(info.getRank().equals("0") ? "您未进入排名" : "今日排名第 " + info.getRank());
		}
		if (info.getRemainscore() != null) {
			SharedPreferencesUtils.getIntance(this).setMyScore(info.getRemainscore());
			tv_my_score.setText("我的剩余积分：" + info.getRemainscore());
		}
		bt_send_tackets.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 给车主发专用券
				String myScore = SharedPreferencesUtils.getIntance(RewardScoreActivity.this).getMyScore();
				if (Double.parseDouble(myScore) >= 6.00) {
					Intent intent = new Intent(RewardScoreActivity.this, SelectTicketActicity.class);
					startActivity(intent);
				} else {
					Toast.makeText(RewardScoreActivity.this, "积分不足无法送券", 0).show();
				}
			}
		});
	}

	// collectorrequest.do?action=rewardscore&token=
	public void getScoreInfo() {
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络", 0).show();
			return;
		}
		String url = baseurl + "collectorrequest.do?action=rewardscore&token=" + token;
		Log.e("RewardScoreActivity", "获取积分的URL--->" + url);
		final ProgressDialog dialog = ProgressDialog.show(this, "获取积分", "获取我的积分中...", true, true);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
					dialog.dismiss();
					Log.e("RewardScoreActivity", "获取到积分--->" + object);
					Gson gson = new Gson();
					RewardScoreInfo info = gson.fromJson(object, RewardScoreInfo.class);
					Log.e("RewardScoreActivity", "解析到积分--->" + info.toString());
					if (info != null && info.getTodayscore() != null) {
						setView(info);
					}
				} else {
					dialog.dismiss();
					switch (status.getCode()) {
					case 500:
						Toast.makeText(RewardScoreActivity.this, "服务器错误！", 0).show();
						break;
					case 404:
						Toast.makeText(RewardScoreActivity.this, "服务器不可用！", 0).show();
						break;
					}
					Toast.makeText(RewardScoreActivity.this, "网络请求错误！", 0).show();
				}
			}
		});
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getScoreInfo();
	}

	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			RewardScoreActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
