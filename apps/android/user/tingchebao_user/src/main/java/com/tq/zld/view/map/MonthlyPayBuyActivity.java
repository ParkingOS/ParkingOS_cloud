package com.tq.zld.view.map;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.tq.zld.R;
import com.tq.zld.bean.MonthlyPay;
import com.tq.zld.bean.ParkInfo;
import com.tq.zld.util.DateUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.wxapi.WXPayEntryActivity;

/**
 * 选择月卡时长界面
 * 
 * @author Clare
 * 
 */
public class MonthlyPayBuyActivity extends BaseActivity implements
		OnClickListener {

	private TextView tv_beginDate;// 开始日期
	private TextView tv_endDate;// 结束日期
	private TextView tv_lastTime;// 购买时长
	private TextView tv_type;// 包月卡类型
	private TextView tv_name;// 包月卡名称
	private TextView tv_parkname;// 车场名称
	private TextView tv_limitday;// 有效期
	private TextView tv_total_money;// 共计金额
	private Button btn_buy;// 确认购买

	private Calendar beginDate;// 起始日期
	private int lastTime = 1;// 包月时长
	private Calendar endDate;// 结束日期
	private Calendar limitDay;// 产品有效期限

	private MonthlyPay data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monthlypay_buy);
		beginDate = Calendar.getInstance();
		beginDate.setTime(new Date(System.currentTimeMillis()));
		initActionBar();
		initView();
		setView();
	}

	private MonthlyPay resolveIntent() {
		Intent intent = getIntent();
		data = new MonthlyPay();
		data.id = intent.getStringExtra(WXPayEntryActivity.ARG_MONTYLYPAY_ID);
		data.name = intent.getStringExtra("name");
		data.type = intent.getStringExtra("type");
		data.parkinfo = new ParkInfo();
		data.parkinfo.name = intent.getStringExtra("parkname");
		data.limitday = intent.getStringExtra("limitday");
		limitDay = Calendar.getInstance();
		limitDay.setTimeInMillis(Long.parseLong(data.limitday) * 1000);
		data.price = intent.getStringExtra("price");
		return data;
	}

	private void initActionBar() {
		ActionBar mActionBar = getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setTitle("选择包月时长");
		mActionBar.show();
	}

	private void setView() {
		data = resolveIntent();
		tv_parkname.setText(Html
				.fromHtml("<font color='#D5D5D5'>车场名称：</font><big>"
						+ data.parkinfo.name + "</big>"));
		tv_name.setText(Html.fromHtml("<font color='#D5D5D5'>月卡名称：</font><big>"
				+ data.name + "</big>"));
		String typeStr = "全天包月";
		switch (data.type) {
		case "1":
			typeStr = "夜间包月";
			break;
		case "2":
			typeStr = "日间包月";
			break;
		}
		tv_type.setText(Html.fromHtml("<font color='#D5D5D5'>月卡类型：</font><big>"
				+ typeStr + "</big>"));

		// 设置有效期
		tv_limitday
				.setText(Html.fromHtml("<font color='#D5D5D5'>有效期至：</font><big><font color='#329762'>"
						+ SimpleDateFormat.getDateInstance(
								SimpleDateFormat.LONG, Locale.CHINA).format(
								new Date(Long.parseLong(data.limitday) * 1000))
						+ "</font></big>"));

		tv_beginDate.setText(formatDate(beginDate));
		tv_endDate.setText(formatDate(getEndDate(false)));
		tv_total_money.setText(data.price);
		btn_buy.setOnClickListener(this);
	}

	private void initView() {
		tv_endDate = (TextView) findViewById(R.id.tv_monthlypay_buy_end);
		tv_lastTime = (TextView) findViewById(R.id.tv_monthlypay_buy_duration);
		tv_lastTime.setOnClickListener(this);
		findViewById(R.id.tv_monthlypay_buy_chooseduration).setOnClickListener(
				this);
		tv_type = (TextView) findViewById(R.id.tv_monthlypay_buy_type);
		tv_name = (TextView) findViewById(R.id.tv_monthlypay_buy_name);
		tv_parkname = (TextView) findViewById(R.id.tv_monthlypay_buy_parkname);
		// tv_cardnumber = (TextView)
		// findViewById(R.id.tv_product_info_parknumber);
		tv_limitday = (TextView) findViewById(R.id.tv_monthlypay_buy_limitday);
		tv_total_money = (TextView) findViewById(R.id.tv_monthlypay_buy_money);
		tv_beginDate = (TextView) findViewById(R.id.tv_monthlypay_buy_begin);
		tv_beginDate.setOnClickListener(this);
		findViewById(R.id.tv_monthlypay_buy_choosebegin).setOnClickListener(
				this);
		btn_buy = (Button) findViewById(R.id.btn_monthlypay_buy_ok);
	}

	/**
	 * 计算购买包月产品结束日期
	 * 
	 * @param changeBegin
	 *            true表示，超出月卡有效期时，通过修改起始日期来修正结束日期，false表示通过修改包月时长来修正
	 * @return
	 */
	private Calendar getEndDate(boolean changeBegin) {
		if (endDate == null) {
			endDate = Calendar.getInstance();
		}
		int endMonth = beginDate.get(Calendar.MONTH) + lastTime;
		endDate.set(beginDate.get(Calendar.YEAR), endMonth,
				beginDate.get(Calendar.DAY_OF_MONTH));
		LogUtils.i(getClass(), "endDate: --->> " + formatDate(endDate));

		// 如果结束日期超过产品有效期，则自动将起始日期提前
		if (endDate.after(limitDay)) {
			if (changeBegin) {
				beginDate.setTimeInMillis(limitDay.getTimeInMillis());
				beginDate.set(Calendar.MONTH, limitDay.get(Calendar.MONTH)
						- lastTime);
				tv_beginDate.setText(formatDate(beginDate));
				endDate.setTimeInMillis(limitDay.getTimeInMillis());
				Toast.makeText(this, "结束日期超过产品有效期，已自动更正起始日期！",
						Toast.LENGTH_LONG).show();
			} else {
				int extraMonths = DateUtils.getMonths(limitDay, endDate);
				lastTime -= extraMonths;
				if (lastTime == 0) {
					lastTime = 1;
					tv_lastTime.setText(lastTime + "个月");
					tv_total_money.setText(String.valueOf(lastTime
							* new BigDecimal(data.price).doubleValue()));
					endDate = getEndDate(true);
				} else {
					tv_lastTime.setText(lastTime + "个月");
					tv_total_money.setText(String.valueOf(lastTime
							* new BigDecimal(data.price).doubleValue()));
					endDate = getEndDate(false);
				}
			}
		}
		return endDate;
	}

	/**
	 * 将calendar格式化为字符串，格式为：2014.08.14
	 * 
	 * @param calendar
	 * @return
	 */
	private String formatDate(Calendar calendar) {
		return new SimpleDateFormat("yyyy.MM.dd", Locale.CHINA).format(calendar
				.getTime());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_monthlypay_buy_begin:
		case R.id.tv_monthlypay_buy_choosebegin:
			showDatePickerDialog();
			break;
		case R.id.tv_monthlypay_buy_duration:
		case R.id.tv_monthlypay_buy_chooseduration:
			showNumberPickerDialog();
			break;
		case R.id.btn_monthlypay_buy_ok:
			startWXPayEntryActivity();
			break;
		}
	}

	private void startWXPayEntryActivity() {
		Intent intent = new Intent(MonthlyPayBuyActivity.this,
				WXPayEntryActivity.class);
		intent.putExtra(WXPayEntryActivity.ARG_TOTALFEE, tv_total_money.getText());
		intent.putExtra(WXPayEntryActivity.ARG_SUBJECT, tv_parkname.getText()
				+ "_" + tv_type.getText() + "_" + tv_lastTime.getText());
		intent.putExtra(WXPayEntryActivity.ARG_MONTYLYPAY_ID, data.id);
		intent.putExtra(WXPayEntryActivity.ARG_PRODTYPE,
				WXPayEntryActivity.PROD_MONTHLY_PAY);
		intent.putExtra(WXPayEntryActivity.ARG_MONTYLYPAY_START, tv_beginDate
				.getText().toString().replace(".", ""));
		String duration = tv_lastTime.getText().toString();
		intent.putExtra(WXPayEntryActivity.ARG_MONTYLYPAY_NUMBER,
				duration.substring(0, duration.indexOf("个")));
		finish();
		startActivity(intent);
	}

	private void showNumberPickerDialog() {
		final NumberPicker numberPicker = new NumberPicker(
				MonthlyPayBuyActivity.this);
		numberPicker.setMaxValue(DateUtils.getMonths(beginDate, limitDay));
		numberPicker.setMinValue(1);
		numberPicker.setValue(lastTime);
		AlertDialog.Builder builder = new Builder(MonthlyPayBuyActivity.this);
		builder.setTitle("选择您的包月时长：").setView(numberPicker)
				.setPositiveButton("确定", new Dialog.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						lastTime = numberPicker.getValue();
						tv_lastTime.setText(lastTime + "个月");
						tv_total_money.setText(String.valueOf(lastTime
								* new BigDecimal(data.price).doubleValue()));
						tv_endDate.setText(formatDate(getEndDate(true)));
					}
				}).setNegativeButton("取消", null).show();
	}

	private void showDatePickerDialog() {
		DatePickerDialog mDatePickerDialog = new DatePickerDialog(
				MonthlyPayBuyActivity.this, new OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						beginDate.set(year, monthOfYear, dayOfMonth);
						tv_beginDate.setText(formatDate(beginDate));
						tv_endDate.setText(formatDate(getEndDate(false)));
					}
				}, beginDate.get(Calendar.YEAR), beginDate.get(Calendar.MONTH),
				beginDate.get(Calendar.DAY_OF_MONTH));
		DatePicker datePicker = mDatePickerDialog.getDatePicker();
		Calendar calendar = Calendar.getInstance();
		calendar.clear(Calendar.HOUR_OF_DAY);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);
		datePicker.setMinDate(calendar.getTimeInMillis());
		mDatePickerDialog.updateDate(beginDate.get(Calendar.YEAR),
				beginDate.get(Calendar.MONTH),
				beginDate.get(Calendar.DAY_OF_MONTH));
		// calendar.set(limitDay.get(Calendar.YEAR),
		// limitDay.get(Calendar.MONTH)
		// - lastTime, limitDay.get(Calendar.DAY_OF_MONTH));
		datePicker.setMaxDate(limitDay.getTimeInMillis());
		mDatePickerDialog.show();
	}
}
