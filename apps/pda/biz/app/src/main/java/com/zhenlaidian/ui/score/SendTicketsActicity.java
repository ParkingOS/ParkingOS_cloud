/**
57：收费员发停车券时选车主：按照本周打赏次数倒叙排列
collectorrequest.do?action=rewardlist&token=&page=&size=
返回值类型：JSON数组
uin:车主ID
rcount：本周打赏次数
rmoney：本周打赏金额
carnumber：车主车牌号（为null时表示没有车牌号未知）

58：收费员发停车券时选车主：按照本周停车次数倒叙排列
collectorrequest.do?action=parkinglist&token=&page=&size=
返回参数类型：JSON数组
uin:车主ID
pcount：本周停车次数
carnumber：车主车牌号（为null时表示没有车牌号未知）
 * 
 */
package com.zhenlaidian.ui.score;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.SendTicketsAdapter;
import com.zhenlaidian.bean.RedPacketInfo;
import com.zhenlaidian.bean.SelectUserInfo;

import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.SharedPreferencesUtils;

/**
 * 选择要发送的车主
 * @author zhangyunfei 2015年7月14日
 */
public class SendTicketsActicity extends BaseActivity {

	private TextView tv_already_reward;// 打赏过的（按钮）
	private TextView tv_already_parking;// 停过车的（按钮）
	private TextView tv_selected;// 已选择的车主
	private TextView tv_residue_score;// 发送后剩余的积分
	private ListView lv_users;// 收费员列表
	// private CheckBox cb_select_null;// 全部选
	public CheckBox cb_select_all;// 全选
	private Button bt_send;// 发送券
	private int count = 0;// 服务区条目总数
	private int visiblecount = 0;// 可见条目数
	private int page = 1;
	public SendTicketsAdapter adapter;
	public int currpage = 1;// 当前列表；1打赏过的 ,2停过车的
	public RedPacketInfo redpacket;// 选券后传过来的；
	private String myScore;// 我的积分
	public int selectedNum;// 已选择的条目
	public Boolean mCbischecked = false;// 全选是否勾上；

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		setContentView(R.layout.acticity_send_tickets);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
//		SysApplication.getInstance().addActivity(this);
		redpacket = (RedPacketInfo) getIntent().getExtras().getSerializable("redpacket");
		myScore = SharedPreferencesUtils.getIntance(SendTicketsActicity.this).getMyScore();
		initView();
		adapter = new SendTicketsAdapter(this);
		getRewardInfo(false);
	}

	public void setAdapter() {
		lv_users.setAdapter(adapter);
	}

	public void setPageNumber() {
		page++;
	}

	public void setAlreadySelectUser(int count, Boolean add) {
		if (add) {
			selectedNum = selectedNum + count;
			tv_selected.setText("已选择" + selectedNum + "名车主,消耗积分" + selectedNum * Double.parseDouble(redpacket.getScore()));
		} else {
			selectedNum = selectedNum - count;
			tv_selected.setText("已选择" + selectedNum + "名车主,消耗积分" + selectedNum * Double.parseDouble(redpacket.getScore()));
		}
		Double score = Double.parseDouble(myScore) - (selectedNum * Double.parseDouble(redpacket.getScore()));
		tv_residue_score.setText("发送后积分剩余：" + String.format("%.2f", score));
	}

	// 获取我的积分够不够选择下一个人
	public Boolean getScore() {
		Double score = Double.parseDouble(myScore) - (selectedNum * Double.parseDouble(redpacket.getScore()));
		return score >= Double.parseDouble(redpacket.getScore());
	}

	public void initView() {
		tv_already_reward = (TextView) findViewById(R.id.tv_send_ticket_already_reward);
		tv_already_parking = (TextView) findViewById(R.id.tv_send_ticket_already_parking);
		tv_selected = (TextView) findViewById(R.id.tv_send_ticket_selected);
		tv_residue_score = (TextView) findViewById(R.id.tv_send_ticket_residue_score);
		// cb_select_null = (CheckBox)
		// findViewById(R.id.cb_send_ticket_select_null);
		cb_select_all = (CheckBox) findViewById(R.id.cb_send_ticket_select_all);
		bt_send = (Button) findViewById(R.id.bt_send_ticket_send);
		lv_users = (ListView) findViewById(R.id.lv_send_ticket_users);
		tv_residue_score.setText("发送后积分剩余：" + myScore);
		tv_already_reward.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// TODO 打赏过的（按钮）
				cb_select_all.setChecked(false);
				tv_already_reward.setBackgroundResource(R.drawable.bt_red_left);
				tv_already_reward.setTextColor(getResources().getColor(R.color.white));
				tv_already_parking.setBackgroundColor(getResources().getColor(R.color.transparency));
				tv_already_parking.setTextColor(getResources().getColor(R.color.red_package));
				currpage = 1;
				page = 1;
				selectedNum = 0;
				tv_selected.setText("请选择要发给的车主");
				tv_residue_score.setText("发送后积分剩余：" + myScore);
				getRewardInfo(true);
			}
		});
		tv_already_parking.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// TODO 停过车的（按钮）
				cb_select_all.setChecked(false);
				tv_already_reward.setBackgroundColor(getResources().getColor(R.color.transparency));
				tv_already_reward.setTextColor(getResources().getColor(R.color.red_package));
				tv_already_parking.setBackgroundResource(R.drawable.bt_red_right);
				tv_already_parking.setTextColor(getResources().getColor(R.color.white));
				currpage = 2;
				page = 1;
				selectedNum = 0;
				tv_selected.setText("请选择要发给的车主");
				tv_residue_score.setText("发送后积分剩余：" + myScore);
				getParkingInfo(true);
			}
		});
		if (redpacket.getBmoney().equals("5")) {
			cb_select_all.setVisibility(View.INVISIBLE);
		} else {
			cb_select_all.setVisibility(View.VISIBLE);
		}
		cb_select_all.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (adapter.getCount() == 0) {
					cb_select_all.setChecked(!isChecked);
					return;
				}
				if (Double.parseDouble(myScore) >= adapter.getCount() * Double.parseDouble(redpacket.getScore())) {
					if (isChecked) {
						mCbischecked = false;
						selectedNum = 0;
						setAlreadySelectUser(adapter.getCount(), true);
						adapter.selectAll();
						setAdapter();
						adapter.notifyDataSetChanged();
					} else {
						if (!mCbischecked) {
							setAlreadySelectUser(adapter.getCount(), false);
							adapter.selectNull();
							setAdapter();
							adapter.notifyDataSetChanged();
						}
					}
				} else {
					if (isChecked) {
						buttonView.setChecked(false);
					}
					Toast.makeText(SendTicketsActicity.this, "剩余积分不足，不能全选", 0).show();
				}

			}
		});
		bt_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 发送停车券给车主
				if (TextUtils.isEmpty(adapter.getUins())) {
					Toast.makeText(SendTicketsActicity.this, "未选择车主", 0).show();
				} else {
					bt_send.setClickable(false);
					sendRedPacket(adapter.getUins());
				}
			}
		});
		lv_users.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.e("SendTicketsActicity",
						"选择的位置= " + position + "id= " + id + "FirstVisiblePosition=" + lv_users.getFirstVisiblePosition());
				int po = position - lv_users.getFirstVisiblePosition();
				RelativeLayout rl = (RelativeLayout) parent.getChildAt(po);
				CheckBox box = (CheckBox) rl.getChildAt(2);
				box.setChecked(!box.isChecked());
			}

		});
		lv_users.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE:// 当不滚动时
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {// 判断滚动到底部
						if (count > visiblecount) {
							if (currpage == 1) {// currpage 当前列表；1打赏过的 ,2停过车的
								setPageNumber();
								getRewardInfo(false);
							} else {
								setPageNumber();
								getParkingInfo(false);
							}
						}
					}
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				visiblecount = totalItemCount;
			}
		});
	}

	// 57：收费员发停车券时选车主：按照本周打赏次数倒叙排列
	// collectorrequest.do?action=rewardlist&token=&page=&size=
	public void getRewardInfo(final Boolean clear) {
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络", 0).show();
			return;
		}
		String url = baseurl + "collectorrequest.do?action=rewardlist&token=" + token + "&page=" + page;
		Log.e("SendTicketsActicity", "获取打赏过的车主URL--->" + url);
		AQuery aQuery = new AQuery(this);
		final ProgressDialog dialog = ProgressDialog.show(this, "获取打赏过的车主", "请求网络中...", true, true);
		dialog.setCanceledOnTouchOutside(false);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				dialog.dismiss();
				if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
					Log.e("SendTicketsActicity", "获取到打赏过我的车主--->" + object);
					Gson gson = new Gson();
					SelectUserInfo info = gson.fromJson(object, SelectUserInfo.class);
					Log.e("SendTicketsActicity", "解析到打赏过我的车主--->" + info.toString());
					if (info != null) {
						count = info.getCount();
						adapter.addOrders(info.getInfo(), clear);
					}
				} else {
					switch (status.getCode()) {
					case 500:
						Toast.makeText(SendTicketsActicity.this, "服务器错误！", 0).show();
						break;
					case 404:
						Toast.makeText(SendTicketsActicity.this, "服务器不可用！", 0).show();
						break;
					}
					Toast.makeText(SendTicketsActicity.this, "网络请求错误！", 0).show();
				}
			}
		});
	}

	// 58：收费员发停车券时选车主：按照本周停车次数倒叙排列
	// collectorrequest.do?action=parkinglist&token=&page=&size=
	public void getParkingInfo(final Boolean clear) {
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络", 0).show();
			return;
		}
		String url = baseurl + "collectorrequest.do?action=parkinglist&token=" + token + "&page=" + page;
		Log.e("SendTicketsActicity", "获取本周停过车的URL--->" + url);
		AQuery aQuery = new AQuery(this);
		final ProgressDialog dialog = ProgressDialog.show(this, "获取本周停过车的车主", "请求网络中...", true, true);
		dialog.setCanceledOnTouchOutside(false);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				dialog.dismiss();
				if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
					Log.e("SendTicketsActicity", "获取到本周停过车的车主--->" + object);
					Gson gson = new Gson();
					SelectUserInfo info = gson.fromJson(object, SelectUserInfo.class);
					Log.e("SendTicketsActicity", "解析到本周停过车的车主--->" + info.toString());
					if (info != null) {
						count = info.getCount();
						adapter.addOrders(info.getInfo(), clear);
					}
				} else {
					switch (status.getCode()) {
					case 500:
						Toast.makeText(SendTicketsActicity.this, "服务器错误！", 0).show();
						break;
					case 404:
						Toast.makeText(SendTicketsActicity.this, "服务器不可用！", 0).show();
						break;
					}
					Toast.makeText(SendTicketsActicity.this, "网络请求错误！", 0).show();
				}
			}
		});
	}

	// 55：收费员用赏金积分发停车券
	// collectorrequest.do?action=sendticket&token=5286f078c6d2ecde9b30929f77771149&bmoney=3&score=1&uins=21616,21577,21554
	// 输入参数：
	// bmoney：金额 (单价)
	// score：消耗的积分 {单价}
	// uins:选中的车主，用英文逗号分隔
	// 返回参数：
	// -1：出错了
	// -2：未选择车主
	// -3：赏金积分不足
	// 1：成功
	public void sendRedPacket(String uins) {
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络", 0).show();
			return;
		}
		String url = baseurl + "collectorrequest.do?action=sendticket&token=" + token + "&bmoney="
				+ redpacket.getBmoney() + "&score=" + redpacket.getScore() + "&uins=" + uins;
		Log.e("SendTicketsActicity", "发停车券的URL--->" + url);
		AQuery aQuery = new AQuery(this);
		final ProgressDialog dialog = ProgressDialog.show(this, "发送停车券", "发送中...", true, true);
		dialog.setCanceledOnTouchOutside(false);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				dialog.dismiss();
				if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
					Log.e("SendTicketsActicity", "获取到发停车券--->" + object);
					switch (Integer.parseInt(object)) {
					case 1:
						hintDialog();
						break;
					case -1:
						Toast.makeText(SendTicketsActicity.this, "出错了", 0).show();
						break;
					case -2:
						Toast.makeText(SendTicketsActicity.this, "未选择车主", 0).show();
						break;
					case -3:
						Toast.makeText(SendTicketsActicity.this, "赏金积分不足", 0).show();
						break;
					}

				} else {
					switch (status.getCode()) {
					case 500:
						Toast.makeText(SendTicketsActicity.this, "服务器错误！", 0).show();
						break;
					case 404:
						Toast.makeText(SendTicketsActicity.this, "服务器不可用！", 0).show();
						break;
					}
					Toast.makeText(SendTicketsActicity.this, "网络请求错误！", 0).show();
				}
				bt_send.setClickable(true);
			}
		});
	}

	public void hintDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("发送成功！");
		builder.setCancelable(false);
		// builder.setMessage("您还要继续发券吗？").setCancelable(false).setPositiveButton("继续发",
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int id) {
		// dialog.dismiss();
		// }
		// })
		builder.setNegativeButton("知道了", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
//				SysApplication.getInstance().finishActivity();
				Intent intent = new Intent(SendTicketsActicity.this, RewardScoreActivity.class);
				startActivity(intent);
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			SendTicketsActicity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
