/**
 * 
 */
package com.zhenlaidian.ui.parking;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.InTheVehicleAdapter;
import com.zhenlaidian.bean.InVehicleInfo;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.CurrentOrderDetailsActivity;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.TimeTypeUtil;

/**
 * 在停车辆
 * 
 * @author zhangyunfei 2015年9月9日
 */
public class InTheVehicleActivity extends BaseActivity {

	public ActionBar actionBar;
	public ArrayList<InVehicleInfo> infos;
	public InTheVehicleAdapter adapter;
	public GridView gv_in_vehicle;
	public TextView tv_parking_state;// 停车状态

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		actionBar = getSupportActionBar();
		setContentView(R.layout.activity_in_the_vehicle);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		initView();

	}

	public void initView() {
		gv_in_vehicle = (GridView) findViewById(R.id.gv_in_vehicle);
		tv_parking_state = (TextView) findViewById(R.id.tv_in_vehicle_parking_state);
		gv_in_vehicle.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (TextUtils.isEmpty(infos.get(position).getOrderid())) {
					return;
				}
				Intent intent = new Intent(InTheVehicleActivity.this, CurrentOrderDetailsActivity.class);
				String orderid = infos.get(position).getOrderid();
				intent.putExtra("orderid", orderid);
//				if (!TextUtils.isEmpty(infos.get(position).getBtime()) && TextUtils.isDigitsOnly(infos.get(position).getBtime())) {
//					intent.putExtra("duration", "已停" + TimeTypeUtil.getTime(Long.parseLong(infos.get(position).getBtime())));
//				}
				MyLog.i("InTheVehicleActivity", "点击条目的position是" + position + "点单号是" + orderid);
				InTheVehicleActivity.this.startActivity(intent);
			}
		});
	}

	public void setView() {
		adapter = new InTheVehicleAdapter(this, infos);
		gv_in_vehicle.setAdapter(adapter);
		int j = 0;
//		for (int i = 0; i < infos.size(); i++) {
//			if ("1".equals(infos.get(i).getState())) {
//				j++;
//			}
//		}
		tv_parking_state.setText("停车：" + j + "/" + infos.size());
	}

	// 获取在场车辆信息collectorrequest.do?action=comparks&out=josn&token=5f0c0edb1cc891ac9c3fa248a28c14d5
	public void getInVehicleInfo() {
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络", 0).show();
			return;
		}
		String url = baseurl + "collectorrequest.do?action=comparks&out=josn&token=" + token;
		MyLog.w("InTheVehicleActivity", "获取在场车辆的URL--->" + url);
		AQuery aQuery = new AQuery(this);
		final ProgressDialog dialog = ProgressDialog.show(InTheVehicleActivity.this, "获取在场车辆数据", "获取中...", true, true);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				dialog.dismiss();
				if (!TextUtils.isEmpty(object)) {
					MyLog.d("InTheVehicleActivity", "获取在场车辆结果--->" + object);
					Gson gson = new Gson();
					infos = gson.fromJson(object, new TypeToken<ArrayList<InVehicleInfo>>() {
					}.getType());
					MyLog.i("InTheVehicleActivity", "解析在场车辆结果--->" + infos.toString());
					if (infos != null) {
						setView();
					}
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			InTheVehicleActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getInVehicleInfo();
	}
}
