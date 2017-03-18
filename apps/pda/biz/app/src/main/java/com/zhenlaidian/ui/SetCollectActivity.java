/**
 * 
 */
package com.zhenlaidian.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.zhenlaidian.R;
import com.zhenlaidian.util.CheckUtils;
import com.zhenlaidian.util.DateTimePickDialogUtil;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.TimeTypeUtil;

/**
 * 设置主页收费二维码的金额
 * 
 * @author zhangyunfei 2015年8月12日
 */
public class SetCollectActivity extends BaseActivity {

	private TextView tv_set_collect_intime; // 入场时间；
	private TextView tv_set_collect_outtime; // 出场时间；
	private RelativeLayout rl_set_collect_check_intime; // 调整时间
	private RelativeLayout rl_set_collect_check_outtime; // 调整时间
	private TextView tv_set_collect_duration; // 停车时长
	private TextView tv_set_collect_set_duration; // 点击设置停车时长
	private TextView et_set_collect_total;
	private TextView tv_set_collect_delete_total; // 删除设置金额
	private Button bt_set_collect_finish; // 完成设置
	private LinearLayout ll_set_duration;// 设置时长
	private String initStartTime; // 初始化开始时间
	private String initEndTime; // 初始化结束时间
	private String intime;
	private String outtime;
	private String duration;
	private String total;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		setContentView(R.layout.activity_set_collect);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		mgetIntent();
		initView();
	}

	@SuppressLint("SimpleDateFormat")
	public void mgetIntent() {
		if (getIntent().getExtras() != null) {
			total = getIntent().getExtras().getString("total");
			duration = getIntent().getExtras().getString("duration");
			String times = getIntent().getExtras().getString("intime");
			if (TextUtils.isEmpty(times)) {
				return;
			}
			// 格式化时间"09月12日17:09—09月16日 19:30"--提取出入场时间和出场时间加上年份；
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy年");
				Date curDate = new Date(System.currentTimeMillis());
				String year = formatter.format(curDate);
				int i = times.length();
				int j = times.indexOf("—");
				intime = year + times.substring(0, j - 1).trim();
				outtime = year + times.substring(j + 1, i).trim();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	@SuppressLint({ "CutPasteId", "SimpleDateFormat" })
	public void initView() {
		tv_set_collect_intime = (TextView) findViewById(R.id.tv_set_collect_intime);
		tv_set_collect_outtime = (TextView) findViewById(R.id.tv_set_collect_outtime);
		rl_set_collect_check_intime = (RelativeLayout) findViewById(R.id.rl_set_collect_check_intime);
		rl_set_collect_check_outtime = (RelativeLayout) findViewById(R.id.rl_set_collect_check_outtime);
		tv_set_collect_duration = (TextView) findViewById(R.id.tv_set_collect_duration);
		tv_set_collect_set_duration = (TextView) findViewById(R.id.tv_set_collect_set_duration);
		et_set_collect_total = (TextView) findViewById(R.id.et_set_collect_total);
		tv_set_collect_delete_total = (TextView) findViewById(R.id.tv_set_collect_delete_total);
		bt_set_collect_finish = (Button) findViewById(R.id.bt_set_collect_finish);
		ll_set_duration = (LinearLayout) findViewById(R.id.ll_set_collect_set_duration);
		ll_set_duration.setVisibility(View.GONE);

		if (TextUtils.isEmpty(intime) && TextUtils.isEmpty(outtime)) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
			Date curDate = new Date(System.currentTimeMillis());
			intime = formatter.format(curDate);
			outtime = formatter.format(curDate);
			tv_set_collect_intime.setText(intime);
			tv_set_collect_outtime.setText(outtime);
		} else {
			tv_set_collect_intime.setText(intime);
			tv_set_collect_outtime.setText(outtime);
			tv_set_collect_duration.setText(duration);
			et_set_collect_total.setText(total);
		}

		rl_set_collect_check_intime.setOnClickListener(new OnClickListener() {
			// 更改入场时间
			public void onClick(View v) {
				if (tv_set_collect_outtime.getText() != null) {
					initStartTime = tv_set_collect_intime.getText().toString().trim();
				}
				DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(SetCollectActivity.this, initStartTime);
				dateTimePicKDialog.dateTimePicKDialog(tv_set_collect_intime);
			}
		});

		rl_set_collect_check_outtime.setOnClickListener(new OnClickListener() {
			// 更改出场时间
			public void onClick(View v) {
				if (tv_set_collect_outtime.getText() != null) {
					initEndTime = tv_set_collect_outtime.getText().toString().trim();
				}
				DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(SetCollectActivity.this, initEndTime);
				dateTimePicKDialog.dateTimePicKDialog(tv_set_collect_outtime);
			}
		});
		tv_set_collect_delete_total.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 删除设置金额
				setResult(4);
				SetCollectActivity.this.finish();
			}
		});
		bt_set_collect_finish.setOnClickListener(new OnClickListener() {
			// 完成设置
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!TextUtils.isEmpty(et_set_collect_total.getText().toString().trim())) {
					if (!CheckUtils.CollectChecked(et_set_collect_total.getText().toString().trim())) {
						Toast.makeText(SetCollectActivity.this, "价格输入有误，请重新输入！", 0).show();
						return;
					}
					String income = tv_set_collect_intime.getText().toString().trim().substring(5);
					String outcome = tv_set_collect_outtime.getText().toString().trim().substring(5);
					Intent intent = new Intent();
					if (ll_set_duration.getVisibility() == View.VISIBLE) {
						intent.putExtra("duration", tv_set_collect_duration.getText().toString().trim());
						intent.putExtra("intime", income + " — " + outcome);
						intent.putExtra("total", et_set_collect_total.getText().toString().trim());
					} else {
						intent.putExtra("duration", "0分钟");
						intent.putExtra("intime", "");
						intent.putExtra("total", et_set_collect_total.getText().toString().trim());
					}
					setResult(3, intent);
				} else {
					setResult(4);
				}
				SetCollectActivity.this.finish();
			}
		});
		tv_set_collect_set_duration.setOnClickListener(new OnClickListener() {
			// 点击设置时长
			@Override
			public void onClick(View v) {
				if (ll_set_duration.getVisibility() == View.VISIBLE) {
					tv_set_collect_set_duration.setText("设置时长");
					Drawable drawable = getResources().getDrawable(R.drawable.set_collect_on);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_set_collect_set_duration.setCompoundDrawables(drawable, null, null, null);
					ll_set_duration.setVisibility(View.GONE);
					et_set_collect_total.setEnabled(true);
				} else {
					tv_set_collect_set_duration.setText("不设置时长");
					et_set_collect_total.setText("");
					Drawable drawable = getResources().getDrawable(R.drawable.set_collect_off);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
					tv_set_collect_set_duration.setCompoundDrawables(drawable, null, null, null);
					ll_set_duration.setVisibility(View.VISIBLE);
					et_set_collect_total.setEnabled(false);
				}
			}
		});
	}

	public void setDuration() {
		if (!TextUtils.isEmpty(tv_set_collect_intime.getText().toString())) {
			if (!TextUtils.isEmpty(tv_set_collect_outtime.getText().toString())) {
				String outTime = getDuration(tv_set_collect_intime.getText().toString(), tv_set_collect_outtime.getText()
						.toString());
				tv_set_collect_duration.setText(outTime);
			}
		}
	}

	/**
	 * 获取停车时长
	 */
	public String getDuration(String inTime, String outTime) {
		if (TextUtils.isEmpty(inTime) || TextUtils.isEmpty(outTime)) {
			return "";
		}
		Calendar incalendar = getCalendarByInintData(inTime);
		Calendar outcalendar = getCalendarByInintData(outTime);
		long start = incalendar.getTimeInMillis() / 1000;
		long end = outcalendar.getTimeInMillis() / 1000;
		if (start > end) {
			Toast.makeText(SetCollectActivity.this, "入场时间大于出场时间！", 0).show();
			et_set_collect_total.setText("");
			tv_set_collect_intime.setText(intime);
			tv_set_collect_outtime.setText(outtime);
			return "";
		}
		if ((end - start) > 864000) {
			Toast.makeText(SetCollectActivity.this, "停车时长最大支持10天！", 0).show();
			et_set_collect_total.setText("");
			tv_set_collect_intime.setText(intime);
			tv_set_collect_outtime.setText(outtime);
			return "";
		}
		getOneQuery(start, end);
		String time = TimeTypeUtil.getTimeString(start, end);
		return time;
	}

	// collectorrequest.do?action=countprice&token=316e83c1c4be66a17d27a5003a785837&btime=?&etime=?
	public void getOneQuery(Long btime, Long etime) {
		String path = baseurl;
		String url = path + "collectorrequest.do?action=countprice&token=" + token + "&btime=" + btime + "&etime=" + etime;
		MyLog.w("SetCollectActivity", "获取价格URL--->" + url);
		AQuery aQuery = new AQuery(this);
		final ProgressDialog dialog = ProgressDialog.show(this, "联网计算价格...", "请求中...", true, true);
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络", 0).show();
			return;
		}
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				// TODO Auto-generated method stub
				super.callback(url, object, status);
				dialog.dismiss();
				if (!TextUtils.isEmpty(object)) {
					MyLog.i("SetCollectActivity", "获取价格是：--->" + object);
					try {
						JSONObject json = new JSONObject(object);
						if (json != null) {
							et_set_collect_total.setText(json.getString("total"));
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					switch (status.getCode()) {
					case 500:
						Toast.makeText(SetCollectActivity.this, "服务器错误！", 0).show();
						return;
					case 404:
						Toast.makeText(SetCollectActivity.this, "服务器不可用！", 0).show();
						return;
					}
					Toast.makeText(SetCollectActivity.this, "网络请求错误-请重新调整时间！", 0).show();
				}
			}
		});
	}

	/**
	 * 实现将初始日期时间2012年07月02日 16:45 拆分成年 月 日 时 分 秒,并赋值给calendar
	 * 
	 * @param initDateTime
	 *            初始日期时间值 字符串型
	 * @return Calendar
	 */
	private Calendar getCalendarByInintData(String initDateTime) {
		Calendar calendar = Calendar.getInstance();

		// 将初始日期时间2012年07月02日 16:45 拆分成年 月 日 时 分 秒
		String date = spliteString(initDateTime, "日", "index", "front"); // 日期
		String time = spliteString(initDateTime, "日", "index", "back"); // 时间

		String yearStr = spliteString(date, "年", "index", "front"); // 年份
		String monthAndDay = spliteString(date, "年", "index", "back"); // 月日

		String monthStr = spliteString(monthAndDay, "月", "index", "front"); // 月
		String dayStr = spliteString(monthAndDay, "月", "index", "back"); // 日

		String hourStr = spliteString(time, ":", "index", "front"); // 时
		String minuteStr = spliteString(time, ":", "index", "back"); // 分

		int currentYear = Integer.valueOf(yearStr.trim()).intValue();
		int currentMonth = Integer.valueOf(monthStr.trim()).intValue() - 1;
		int currentDay = Integer.valueOf(dayStr.trim()).intValue();
		int currentHour = Integer.valueOf(hourStr.trim()).intValue();
		int currentMinute = Integer.valueOf(minuteStr.trim()).intValue();

		calendar.set(currentYear, currentMonth, currentDay, currentHour, currentMinute);
		return calendar;
	}

	/**
	 * 截取子串
	 * 
	 * @param srcStr
	 *            源串
	 * @param pattern
	 *            匹配模式
	 * @param indexOrLast
	 * @param frontOrBack
	 * @return
	 */
	public static String spliteString(String srcStr, String pattern, String indexOrLast, String frontOrBack) {
		String result = "";
		int loc = -1;
		if (indexOrLast.equalsIgnoreCase("index")) {
			loc = srcStr.indexOf(pattern); // 取得字符串第一次出现的位置
		} else {
			loc = srcStr.lastIndexOf(pattern); // 最后一个匹配串的位置
		}
		if (frontOrBack.equalsIgnoreCase("front")) {
			if (loc != -1)
				result = srcStr.substring(0, loc); // 截取子串
		} else {
			if (loc != -1)
				result = srcStr.substring(loc + 1, srcStr.length()); // 截取子串
		}
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			SetCollectActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
