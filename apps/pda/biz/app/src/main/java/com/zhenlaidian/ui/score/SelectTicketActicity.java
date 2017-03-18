package com.zhenlaidian.ui.score;

import java.util.ArrayList;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.RedPacketInfo;

import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.SharedPreferencesUtils;

/**
 * 选择优惠券
 * 
 * @author zhangyunfei 2015年7月14日
 */
public class SelectTicketActicity extends BaseActivity {

	private TextView tv_red_packet1;// 红包一 内容
	private TextView tv_red_packet2;// 红包二 内容
	private TextView tv_single_ticket1;// 单张券一 内容
	private TextView tv_single_ticket2;// 单张券二 内容

	private TextView tv_red_packet1_bg;// 红包一 选中效果
	private TextView tv_red_packet2_bg;// 红包二 选中效果
	private TextView tv_single_ticket1_bg;// 单张券一 选中效果
	private TextView tv_single_ticket2_bg;// 单张券二 选中效果

	private RelativeLayout rl_red_packet1;// 红包一
	private RelativeLayout rl_red_packet2;// 红包二
	private RelativeLayout rl_single_ticket1;// 单张券一
	private RelativeLayout rl_single_ticket2;// 单张券二
	private ArrayList<RedPacketInfo> info;
	private RedPacketInfo redpacket;
	private Button send_packet;
	private String myScore;// 我的积分
	private TextView tv_fivehint;// 选择5元券提示；

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		setContentView(R.layout.activity_select_ticket);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
//		SysApplication.getInstance().addActivity(this);
		myScore = SharedPreferencesUtils.getIntance(SelectTicketActicity.this).getMyScore();
		initView();
	}

	public void initView() {
		tv_red_packet1 = (TextView) findViewById(R.id.tv_select_red_packet1);
		tv_red_packet2 = (TextView) findViewById(R.id.tv_select_red_packet2);
		tv_single_ticket1 = (TextView) findViewById(R.id.tv_select_single_ticket1);
		tv_single_ticket2 = (TextView) findViewById(R.id.tv_select_single_ticket2);
		rl_red_packet1 = (RelativeLayout) findViewById(R.id.rl_select_red_packet1);
		rl_red_packet2 = (RelativeLayout) findViewById(R.id.rl_select_red_packet2);
		rl_single_ticket1 = (RelativeLayout) findViewById(R.id.rl_select_single_ticket1);
		rl_single_ticket2 = (RelativeLayout) findViewById(R.id.rl_select_single_ticket2);
		tv_red_packet1_bg = (TextView) findViewById(R.id.tv_rl_select_red_packet1);
		tv_red_packet2_bg = (TextView) findViewById(R.id.tv_rl_select_red_packet2);
		tv_single_ticket1_bg = (TextView) findViewById(R.id.tv_rl_select_single_ticket1);
		tv_single_ticket2_bg = (TextView) findViewById(R.id.tv_rl_select_single_ticket2);
		send_packet = (Button) findViewById(R.id.bt_send_packet_to_user);
		tv_fivehint = (TextView) findViewById(R.id.tv_rl_select_red_packet_fivehint);
	}

	public void setViewCleck() {
		rl_red_packet1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 红包一 8元选中效果
				tv_red_packet1_bg.setBackgroundResource(R.drawable.red_ticket_sel);
				tv_red_packet2_bg.setBackgroundResource(R.drawable.red_ticket_off);
				tv_single_ticket1_bg.setBackgroundResource(R.drawable.red_ticket_off);
				tv_single_ticket2_bg.setBackgroundResource(R.drawable.red_ticket_off);
				redpacket = info.get(2);
				tv_fivehint.setVisibility(View.INVISIBLE);
			}
		});
		rl_red_packet2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 红包二 18元选中效果
				tv_red_packet1_bg.setBackgroundResource(R.drawable.red_ticket_off);
				tv_red_packet2_bg.setBackgroundResource(R.drawable.red_ticket_sel);
				tv_single_ticket1_bg.setBackgroundResource(R.drawable.red_ticket_off);
				tv_single_ticket2_bg.setBackgroundResource(R.drawable.red_ticket_off);
				redpacket = info.get(3);
				tv_fivehint.setVisibility(View.INVISIBLE);
			}
		});
		rl_single_ticket1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 单张券一 3元选中效果
				tv_red_packet1_bg.setBackgroundResource(R.drawable.red_ticket_off);
				tv_red_packet2_bg.setBackgroundResource(R.drawable.red_ticket_off);
				tv_single_ticket1_bg.setBackgroundResource(R.drawable.red_ticket_sel);
				tv_single_ticket2_bg.setBackgroundResource(R.drawable.red_ticket_off);
				redpacket = info.get(0);
				tv_fivehint.setVisibility(View.INVISIBLE);
			}
		});
		rl_single_ticket2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 单张券二 5元选中效果
				tv_red_packet1_bg.setBackgroundResource(R.drawable.red_ticket_off);
				tv_red_packet2_bg.setBackgroundResource(R.drawable.red_ticket_off);
				tv_single_ticket1_bg.setBackgroundResource(R.drawable.red_ticket_off);
				tv_single_ticket2_bg.setBackgroundResource(R.drawable.red_ticket_sel);
				redpacket = info.get(1);
				tv_fivehint.setVisibility(View.VISIBLE);
				if (redpacket.getLimit() != null && "1".equals(redpacket.getLimit())) {
					tv_fivehint.setText("今日五元券发放数量已达到上限");
				} else {
					tv_fivehint.setText("5元券每次只能使用一张,每次积分消耗递增20");

				}
			}
		});
		send_packet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 去选择要发送的车主
				// type=0表示红包
				// type=1表示停车券
				if (redpacket.getType().equals("0")) {
					if (Double.parseDouble(myScore) >= Double.parseDouble(redpacket.getScore())) {
						Intent intent = new Intent(SelectTicketActicity.this, ParkingRedPacketsActivity.class);
						intent.putExtra("redpacket", redpacket);
						startActivity(intent);
					} else {
						Toast.makeText(SelectTicketActicity.this, "积分不足了！", 0).show();
					}
				} else {
					if (redpacket.getLimit() != null && "1".equals(redpacket.getLimit()) && "5".equals(redpacket.getBmoney())) {
						Toast.makeText(SelectTicketActicity.this, "今日五元券发放数量已达到上限", 0).show();
					} else {
						if (Double.parseDouble(myScore) >= Double.parseDouble(redpacket.getScore())) {
							Intent intent = new Intent(SelectTicketActicity.this, SendTicketsActicity.class);
							intent.putExtra("redpacket", redpacket);
							startActivity(intent);
						} else {
							Toast.makeText(SelectTicketActicity.this, "积分不足了！", 0).show();
						}
					}
				}
			}
		});
	}

	public void setView(ArrayList<RedPacketInfo> info) {
		if (info == null || info.size() < 4) {
			return;
		}
		setViewCleck();
		if (info.get(0) != null) {
			rl_single_ticket1.setTag(info.get(0));
			tv_single_ticket1.setText(info.get(0).getBmoney() + "元\n消耗积分" + info.get(0).getScore());
		}
		if (info.get(1) != null) {
			rl_single_ticket2.setTag(info.get(1));
			tv_single_ticket2.setText(info.get(1).getBmoney() + "元\n消耗积分" + info.get(1).getScore());
		}
		if (info.get(2) != null) {
			rl_red_packet1.setTag(info.get(2));
			tv_red_packet1.setText(info.get(2).getBmoney() + "元/" + info.get(2).getBnum() + "个\n消耗积分" + info.get(2).getScore());
		}
		if (info.get(3) != null) {
			rl_red_packet2.setTag(info.get(3));
			tv_red_packet2.setText(info.get(3).getBmoney() + "元/" + info.get(3).getBnum() + "个\n消耗积分" + info.get(3).getScore());
		}
		tv_red_packet1_bg.setBackgroundResource(R.drawable.red_ticket_off);
		tv_red_packet2_bg.setBackgroundResource(R.drawable.red_ticket_off);
		tv_single_ticket1_bg.setBackgroundResource(R.drawable.red_ticket_sel);
		tv_single_ticket2_bg.setBackgroundResource(R.drawable.red_ticket_off);
		redpacket = info.get(0);
	}

	// 获取红包详情collectorrequest.do?action=bonusinfo&token=
	public void getRewardScoreInfo() {
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络", 0).show();
			return;
		}
		String url = baseurl + "collectorrequest.do?action=bonusinfo&token=" + token;
		Log.e("RewardRankingActivity", "获取红包详情的URL--->" + url);
		final ProgressDialog dialog = ProgressDialog.show(this, "获取红包详情", "加载中...", true, true);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
					dialog.dismiss();
					Log.e("RewardRankingActivity", "获取到红包详情--->" + object);
					Gson gson = new Gson();
					info = gson.fromJson(object, new TypeToken<ArrayList<RedPacketInfo>>() {
					}.getType());
					Log.e("RewardRankingActivity", "解析到红包详情--->" + info.toString());
					setView(info);
				} else {
					dialog.dismiss();
					switch (status.getCode()) {
					case 500:
						Toast.makeText(SelectTicketActicity.this, "服务器错误！", 0).show();
						break;
					case 404:
						Toast.makeText(SelectTicketActicity.this, "服务器不可用！", 0).show();
						break;
					}
					Toast.makeText(SelectTicketActicity.this, "网络请求错误！", 0).show();
				}
			}
		});
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		tv_fivehint.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getRewardScoreInfo();
	}

	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			SelectTicketActicity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
