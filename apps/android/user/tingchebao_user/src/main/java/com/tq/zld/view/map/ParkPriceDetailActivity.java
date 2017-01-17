package com.tq.zld.view.map;

import java.util.Locale;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.PriceDetail;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.BaseActivity;

public class ParkPriceDetailActivity extends BaseActivity {

	private TextView tv_dayDefaultTime;
	private TextView tv_dayFirstTime;
	private TextView tv_dayFirstPrice;
	private TextView tv_dayPrice;
	private TextView tv_dayTime;
	private TextView tv_dayUnit;
	private TextView tv_dayTips;
	private TextView tv_nightDefaultTime;
	private TextView tv_nightFirstTime;
	private TextView tv_nightFirstPrice;
	private TextView tv_nightTime;
	private TextView tv_nightPrice;
	private TextView tv_nightUnit;
	private TextView tv_nightTips;
	private TextView tv_nightdisabled;
	private TextView tv_vehicleType;
	private TextView tv_page_null;// 空评论页面的显示文本
	private View rl_page_null;// 评论为空时显示的页面
	private View llDayPrice;// 白天价格布局
	private View llNightPrice;// 夜晚价格布局

	private String parkId;

	private PriceDetail mPrice;
	private boolean showCar = true;// 当前显示的是小车价格还是大车价格

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_park_price);
		parkId = getIntent().getStringExtra("parkid");
		initToolbar();
		initView();
		getPriceInfo();
	}

	/**
	 * 获取当前停车场的价格详情
	 * http://192.168.1.148/zld/parkedit.do?action=queryprice&comid=3
	 */
	private void getPriceInfo() {
		final ProgressDialog dialog = ProgressDialog.show(this, "", "请稍候...",
				false, true);
		dialog.setCanceledOnTouchOutside(false);
		AQuery aQuery = new AQuery(ParkPriceDetailActivity.this);
		String url = TCBApp.mServerUrl + "parkedit.do?action=queryprice&comid="
				+ parkId;
		LogUtils.i(ParkPriceDetailActivity.class, "getPriceDetail url: --->> "
				+ url);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				LogUtils.i(ParkPriceDetailActivity.class,
						"getPriceDetail result: --->> " + object);
				dialog.dismiss();
				if (!TextUtils.isEmpty(object)) {
					try {
						mPrice = new Gson().fromJson(object, PriceDetail.class);
						if (mPrice != null) {
							fillData(mPrice);
						} else {
							showNullView();
						}
					} catch (Exception e) {
						showNullView();
						e.printStackTrace();
					}
				}
				super.callback(url, object, status);
			}
		});
	}

	private void showNullView() {
		llDayPrice.setVisibility(View.GONE);
		llNightPrice.setVisibility(View.GONE);
		tv_page_null.setText("未获取到价格详细信息，\n请以现场实际价格为准。");
		rl_page_null.setVisibility(View.VISIBLE);
	}

	protected void fillData(PriceDetail mPrice) {
		try {
			// Drawable tv_vehicleTypeDrawable = null;
			String b_time = mPrice.b_time;
			if (1 == b_time.length()) {
				b_time = "0" + b_time;
			}
			String free_time = mPrice.free_time;
			String nfree_time = mPrice.nfree_time;
			String tips = "备注：实际价格信息可能根据现场有一定调整。";
			if (!"0".equals(free_time)) {
				if ("1".equals(mPrice.fpay_type)) {
					tips = "备注：%1$d分钟内免费\n\t\t\t\t超过%1$d分钟，则扣除这%1$d分钟再计算停车时长";
				} else {
					tips = "备注：%1$d分钟内免费\n\t\t\t\t超过%1$d分钟，则这%1$d分钟也计费";
				}
				tv_dayTips.setText(String.format(tips,
						Integer.parseInt(free_time)));
			} else {
				tv_dayTips.setText(tips);
			}
			tv_dayTime
					.setText("日间（" + b_time + ":00—" + mPrice.e_time + ":00）");
			tv_dayFirstTime.setText(formatTime(mPrice.first_times) + "内");
			String fPrice = 0 == Double.parseDouble(mPrice.fprice) ? mPrice.price
					: mPrice.fprice;
			tv_dayFirstPrice.setText(fPrice);
			tv_dayDefaultTime.setText(formatTime(mPrice.first_times) + "外");
			tv_dayPrice.setText(mPrice.price);
			tv_dayUnit.setText("元/" + mPrice.unit + "分钟");

			llDayPrice.setVisibility(View.VISIBLE);
			if ("0".equals(mPrice.isnight)) {
				if (Double.parseDouble(mPrice.nprice) < 0) {
					tv_nightFirstPrice.setText("/");
					tv_nightPrice.setText("/");
					tv_nightUnit.setText("/");
					tv_nightTips.setText("备注：该车场暂未设置夜间价格，请以现场收费为准！");
				} else {
					String ntips = "备注：实际价格信息可能根据现场有一定调整。";
					if (!"0".equals(nfree_time)) {
						if ("1".equals(mPrice.nfpay_type)) {
							ntips = "备注：%1$d分钟内免费\n\t\t\t\t超过%1$d分钟，则扣除这%1$d分钟再计算停车时长";
						} else {
							ntips = "备注：%1$d分钟内免费\n\t\t\t\t超过%1$d分钟，则这%1$d分钟也计费";
						}
						tv_nightTips.setText(String.format(ntips,
								Integer.parseInt(nfree_time)));
					} else {
						tv_nightTips.setText(ntips);
					}

					tv_nightTime.setText("夜间（" + mPrice.e_time + ":00—"
							+ b_time + ":00）");
					tv_nightFirstTime.setText(formatTime(mPrice.nfirst_times)
							+ "内");
					String nfPrice = 0 == Double.parseDouble(mPrice.nfprice) ? mPrice.nprice
							: mPrice.nfprice;
					tv_nightFirstPrice.setText(nfPrice);
					tv_nightDefaultTime.setText(formatTime(mPrice.nfirst_times)
							+ "外");
					tv_nightPrice.setText(mPrice.nprice);
					tv_nightUnit.setText("元/" + mPrice.nunit + "分钟");
				}
				tv_nightdisabled.setVisibility(View.GONE);
			} else {
				tv_nightdisabled.setVisibility(View.VISIBLE);
			}
			llNightPrice.setVisibility(View.VISIBLE);
			if (showCar) {
				// TODO 设置显示小车信息
				// tv_vehicleTypeDrawable = getResources().getDrawable(
				// R.drawable.ic_car);
				// tv_vehicleTypeDrawable.setBounds(0, 0,
				// tv_vehicleTypeDrawable.getMinimumWidth(),
				// tv_vehicleTypeDrawable.getMinimumHeight());
				// tv_vehicleType.setCompoundDrawables(tv_vehicleTypeDrawable,
				// null,
				// null, null);
				// tv_vehicleType
				// .setText(Html
				// .fromHtml("<big><b>小车</b></big><br><small><font color='#D5D5D5'>（点击切换）</font></small>"));
			} else {
				// TODO 设置大车价格
				// tv_vehicleTypeDrawable = getResources().getDrawable(
				// R.drawable.ic_truck);
				// tv_vehicleTypeDrawable.setBounds(0, 0,
				// tv_vehicleTypeDrawable.getMinimumWidth(),
				// tv_vehicleTypeDrawable.getMinimumHeight());
				// tv_vehicleType.setCompoundDrawables(tv_vehicleTypeDrawable,
				// null,
				// null, null);
				// tv_vehicleType
				// .setText(Html
				// .fromHtml("<big><b>大车</b></big><br><small><font color='#D5D5D5'>（点击切换）</font></small>"));
			}
			// 显示界面
			rl_page_null.setVisibility(View.GONE);
		} catch (Exception e) {
			showNullView();
			LogUtils.i(getClass(),
					"--->> priceView Exception has been caught <<---");
			e.printStackTrace();
		}
	}

	/**
	 * 将分钟数格式化为小时：120分钟-->2小时；30分钟-->30分钟；70分钟-->1小时10分钟
	 * 
	 * @param time
	 *            待格式化的分钟数
	 * @return 可能为null
	 */
	private String formatTime(String time) {
		if (!TextUtils.isDigitsOnly(time)) {
			return null;
		}
		if ("0".equals(time) || "60".equals(time)) {
			return "首小时";
		}
		int source = Integer.parseInt(time);
		int hour = source / 60;
		int minute = source % 60;
		if (hour == 0) {
			return "首" + source + "分钟";
		}
		if (minute == 0) {
			return "首" + hour + "小时";
		}
		return String.format(Locale.CHINA, "首%1$d小时%2$d分钟", hour, minute);
	}

	private void initView() {
		llDayPrice = findViewById(R.id.ll_park_price_day);
		llDayPrice.setVisibility(View.GONE);
		llNightPrice = findViewById(R.id.ll_park_price_night);
		llNightPrice.setVisibility(View.GONE);
		rl_page_null = findViewById(R.id.rl_page_null);
		tv_page_null = (TextView) findViewById(R.id.tv_page_null);
		tv_dayTime = (TextView) findViewById(R.id.tv_park_price_daytime);
		tv_dayPrice = (TextView) findViewById(R.id.tv_park_price_dayprice);
		tv_dayUnit = (TextView) findViewById(R.id.tv_park_price_dayunit);
		tv_nightTime = (TextView) findViewById(R.id.tv_park_price_nighttime);
		tv_nightPrice = (TextView) findViewById(R.id.tv_park_price_nightprice);
		tv_nightUnit = (TextView) findViewById(R.id.tv_park_price_nightunit);
		tv_dayFirstPrice = (TextView) findViewById(R.id.tv_park_price_dayfirstprice);
		tv_nightFirstPrice = (TextView) findViewById(R.id.tv_park_price_nightfirstprice);
		tv_dayDefaultTime = (TextView) findViewById(R.id.tv_park_price_daydefaulttime);
		tv_dayFirstTime = (TextView) findViewById(R.id.tv_park_price_dayfirsttime);
		tv_nightDefaultTime = (TextView) findViewById(R.id.tv_park_price_nightdefaulttime);
		tv_nightFirstTime = (TextView) findViewById(R.id.tv_park_price_nightfirsttime);
		tv_nightdisabled = (TextView) findViewById(R.id.tv_park_price_nightdisabled);
		tv_dayTips = (TextView) findViewById(R.id.tv_park_price_day_tips);
		tv_nightTips = (TextView) findViewById(R.id.tv_park_price_night_tips);
		// TODO 切换大小车显示不同价格
		// tv_vehicleType = (TextView)
		// findViewById(R.id.tv_park_price_vehicle_type);
		// tv_vehicleType.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO 切换大小车价格显示
		// // showCar = !showCar;
		// // fillData(mPrice);
		// }
		// });
	}

	private void initToolbar() {
		Toolbar bar = (Toolbar) findViewById(R.id.widget_toolbar);
		bar.setTitle("价格详情");
		setSupportActionBar(bar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		bar.setNavigationOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}
}
