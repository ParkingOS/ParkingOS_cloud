package com.zhenlaidian.bean;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.zhenlaidian.R;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

public class RewardDialog extends Dialog {
	private LeaveOrder order;
	private TextView tv_carnumber;
	private TextView tv_money;
	private TextView tv_rcount;// 打赏次数
	private TextView tv_score;// 获得积分
	private TextView tv_score_hint;// 获得积分提示
	private Button bt_thanks_three;
	private Button bt_thanks_five;
	private TextView tv_reward_cancel;// 关闭
	private Context context;
	private LinearLayout ll_reward_score;

	public RewardDialog(Context context) {
		super(context);
	}

	public RewardDialog(Context context, int theme, LeaveOrder order) {
		super(context, theme);
		this.context = context;
		this.order = order;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.dialog_reward);
		initView();
		if (this.order != null) {
			setVeiw();
		}
	}

	public void initView() {
		tv_carnumber = (TextView) findViewById(R.id.tv_reward_carnumber);
		tv_money = (TextView) findViewById(R.id.tv_reward_money);
		tv_rcount = (TextView) findViewById(R.id.tv_reward_rcount);
		tv_score = (TextView) findViewById(R.id.tv_reward_score);
		tv_reward_cancel = (TextView) findViewById(R.id.tv_reward_cancel);
		tv_score_hint = (TextView) findViewById(R.id.tv_reward_score_hint);
		bt_thanks_five = (Button) findViewById(R.id.bt_reward_thanks_five);
		bt_thanks_three = (Button) findViewById(R.id.bt_reward_thanks_three);
		ll_reward_score = (LinearLayout) findViewById(R.id.ll_reward_score);
	}

	public void setVeiw() {
		if (order.getRcount() != null) {
			tv_rcount.setText(Html.fromHtml("您收到今日第 <font color='#e37479'><big>" + order.getRcount() + "</big></font> 笔车主打赏"));
		}
		if (order.getCarnumber() != null) {
			tv_carnumber.setText(order.getCarnumber());
		}
		if (order.getTotal() != null) {
			tv_money.setText(order.getTotal() + "元");
		}
		if (order.getLimit() != null && order.getLimit().equals("1")) {
			ll_reward_score.setVisibility(View.GONE);
			tv_score_hint.setVisibility(View.VISIBLE);
		} else {
			ll_reward_score.setVisibility(View.VISIBLE);
			tv_score_hint.setVisibility(View.GONE);
			if (order.getRcount() != null) {
				Double d = Double.parseDouble(order.getTotal()) * Double.parseDouble(order.getRcount());
				tv_score.setText(String.format("%.2f", d));
			}
		}
		bt_thanks_three.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 感谢他返3元停车券
				bt_thanks_three.setClickable(false);
				bt_thanks_five.setClickable(false);
				sendRedPacket(3, 6);
			}
		});
		bt_thanks_five.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 感谢他返5元停车券
				if (TextUtils.isEmpty(order.getFivelimit()) || TextUtils.isEmpty(order.getFivescore())) {
					Toast.makeText(context, "5元券信息出错，请告知停车宝", 1).show();
					return;
				} else {
					if (order.getFivelimit().equals("0")) {// 0可继续发5元券 1：不能发
						bt_thanks_three.setClickable(false);
						bt_thanks_five.setClickable(false);
						sendRedPacket(5, Integer.parseInt(order.getFivescore()));
						try {
							LeaveActivity activity = (LeaveActivity) context;
							activity.resetQrcode();
							MyLog.d("RewardDialog", "打赏消息返五元券后主页复位收费二维码");
						} catch (Exception e) {
							MyLog.e("RewardDialog", "打赏消息返五元券后复位二维码异常！！！");
						}
					} else {
						Toast.makeText(context, "今日5元券已达到上限(10个)！", 1).show();
					}
				}
			}
		});
		tv_reward_cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				RewardDialog.this.dismiss();
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
	public void sendRedPacket(int money, final int score) {
		if (!IsNetWork.IsHaveInternet(context)) {
			Toast.makeText(context, "请检查网络", 0).show();
			bt_thanks_three.setClickable(true);
			bt_thanks_five.setClickable(true);
			return;
		}
		String url = Config.getUrl(context) + "collectorrequest.do?action=sendticket&token=" + BaseActivity.token + "&bmoney="
				+ money + "&score=" + score + "&uins=" + order.getUin();
		Log.e("RewardDialog", "发停车券的URL--->" + url);
		final ProgressDialog dialog =  ProgressDialog.show(context, "返券中", "正在给车主返券");
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		AQuery aQuery = new AQuery(context);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				dialog.dismiss();
				bt_thanks_three.setClickable(true);
				bt_thanks_five.setClickable(true);
				if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
					Log.e("RewardDialog", "获取到发停车券--->" + object);
					switch (Integer.parseInt(object)) {
					case 1:
						Message msg = new Message();
						msg.what = 2;
						msg.obj = new MainUiInfo(false, 3, Double.parseDouble(score + ".00"));
						LeaveActivity.handler.sendMessage(msg);
						RewardDialog.this.dismiss();
						break;
					case -1:
						Toast.makeText(context, "出错了", 0).show();
						break;
					case -2:
						Toast.makeText(context, "未选择车主", 0).show();
						break;
					case -3:
						Toast.makeText(context, "赏金积分不足", 0).show();
						break;
					}
				} else {
					switch (status.getCode()) {
					case 500:
						Toast.makeText(context, "服务器错误！", 0).show();
						break;
					case 404:
						Toast.makeText(context, "服务器不可用！", 0).show();
						break;
					}
					Toast.makeText(context, "网络请求错误！", 0).show();
				}
			}
		});
	}
}
