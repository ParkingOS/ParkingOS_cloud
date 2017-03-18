//package com.zhenlaidian.ui;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v7.app.ActionBar;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.baidu.location.BDLocation;
//import com.baidu.location.BDLocationListener;
//import com.baidu.location.LocationClient;
//import com.baidu.location.LocationClientOption;
//import com.baidu.location.LocationClientOption.LocationMode;
//import com.baidu.mapapi.SDKInitializer;
//import com.baidu.mapapi.map.BaiduMap;
//import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
//import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
//import com.baidu.mapapi.map.MapStatus;
//import com.baidu.mapapi.map.MapStatusUpdateFactory;
//import com.baidu.mapapi.map.MapView;
//import com.baidu.mapapi.map.MyLocationData;
//import com.baidu.mapapi.model.LatLng;
//import com.baidu.mapapi.search.core.SearchResult;
//import com.baidu.mapapi.search.geocode.GeoCodeResult;
//import com.baidu.mapapi.search.geocode.GeoCoder;
//import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
//import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
//import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
//import com.zhenlaidian.R;
//import com.zhenlaidian.util.MyLog;
//
///**
// * 获取用户当前位置附近的某个准确的地理位置。
// *
// * 使用方法： 1、使用者通过startActivityForResult(Intent intent, int
// * requestCode)方法开启此Activity。 2、结果可从onActivityResult(int requestCode, int
// * resultCode, Intent data)方法的data参数中获取。
// *
// * 说明： requestCode = Activity.RESULT_OK resultCode = Activity.RESULT_OK
// * 获取地址字符串信息： String address = data.getStringExtra("address"); 获取经纬度信息： double[]
// * latlng =
// * data.getDoubleArrayExtra("latlng");//double[0]:latitude,double[1]:longitude
// *
// * @author Clare
// *
// */
//public class LocationActivity extends BaseActivity {
//
//	private static final String TAG = "LocationActivity";
//
//	private MapView mMapView;
//	private BaiduMap mBaiduMap;
//	private ImageView myLocBtn;// 地图上我的位置按钮，点击定位到我的位置
//	private MyLocationData data;// 地图上“我的位置”数据
//	public LocationClient mLocationClient = null;
//	public BDLocationListener myListener = new MyLocationListener();
//	private GeoCoder mGeoCoder;
//	private boolean hasLocated = false;
//	private TextView tv_result;// 显示当前地图中心点的地址信息
//	private Button btn_ok;// "完成"按钮
//	private ActionBar actionBar;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		actionBar = getSupportActionBar();
//		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
//				ActionBar.DISPLAY_HOME_AS_UP);
//		actionBar.setDisplayHomeAsUpEnabled(true);
//		actionBar.show();
//		SDKInitializer.initialize(getApplicationContext());
//		setContentView(R.layout.activity_location);
//		tv_result = (TextView) findViewById(R.id.tv_location_result);
//		btn_ok = (Button) findViewById(R.id.btn_location_ok);
//		btn_ok.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if (tv_result.getTag() == null) {
//					Toast.makeText(LocationActivity.this, "正在定位中，请稍后...", 0).show();
//					return;
//				}
//				// 数据是使用Intent返回
//				Intent intent = new Intent();
//				// 把返回数据存入Intent
//				intent.putExtra("address", tv_result.getText());
//				intent.putExtra("latlng", (double[]) tv_result.getTag());
//				// 设置返回数据
//				setResult(RESULT_OK, intent);
//				// 关闭Activity
//				finish();
//			}
//		});
//		mGeoCoder = GeoCoder.newInstance();
//		mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
//
//					@Override
//					public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
//						MyLog.w(TAG,"GetReverseGeoCodeResult: --->> "+ (SearchResult.ERRORNO.NO_ERROR == result.error));
//						if (SearchResult.ERRORNO.NO_ERROR == result.error) {
//							tv_result.setText(result.getAddress());
//						}
//					}
//
//					@Override
//					public void onGetGeoCodeResult(GeoCodeResult arg0) {
//					}
//				});
//		mMapView = (MapView) findViewById(R.id.mapview);
//		mBaiduMap = mMapView.getMap();
//		mBaiduMap.setBuildingsEnabled(false);
//		mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {
//
//			@Override
//			public void onMapStatusChangeStart(MapStatus arg0) {
//			}
//
//			@Override
//			public void onMapStatusChangeFinish(MapStatus newStatus) {
//				updatePosition(newStatus.target);
//			}
//
//			@Override
//			public void onMapStatusChange(MapStatus arg0) {
//			}
//		});
//
//		mBaiduMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {
//
//			@Override
//			public void onMapLoaded() {
//				Toast.makeText(LocationActivity.this, "请拖动地图，选择停车场位置！",Toast.LENGTH_SHORT).show();
//			}
//		});
//		myLocBtn = (ImageView) findViewById(R.id.iv_location_myposition);
//		myLocBtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (data != null) {
//					LatLng newPosition = new LatLng(data.latitude,data.longitude);
//					mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(newPosition, 19f));
//					updatePosition(newPosition);
//				} else {
//					Toast.makeText(LocationActivity.this, "正在获取我的位置...", 0).show();
//				}
//			}
//		});
//		double[] parkLatLng = getIntent().getDoubleArrayExtra("parkLatLng");
//		if (parkLatLng != null) {
//			MyLog.w("修改停车场位置的经纬度是", "纬度：" + parkLatLng[0] + "经度：" + parkLatLng[1]);
//			LatLng oldPosition = new LatLng(parkLatLng[0], parkLatLng[1]);
//			mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(oldPosition, 19f));
//			updatePosition(oldPosition);
//			hasLocated = true;
//		}
//		mBaiduMap.setMyLocationEnabled(true);
//		mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
//		mLocationClient.registerLocationListener(myListener); // 注册监听函数
//		LocationClientOption option = new LocationClientOption();
//		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
//		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
//		option.setScanSpan(5000);// 设置发起定位请求的间隔时间为2000ms
//		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
//		option.setOpenGps(true);
//		mLocationClient.setLocOption(option);
//		mLocationClient.start();
//		mLocationClient.requestLocation();
//	}
//
//	private void updatePosition(LatLng newPosition) {
//		tv_result.setTag(new double[] { newPosition.latitude,newPosition.longitude });
//		ReverseGeoCodeOption option = new ReverseGeoCodeOption();
//		option.location(newPosition);
//		mGeoCoder.reverseGeoCode(option);
//	}
//
//	// actionBar的点击回调方法
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		switch (item.getItemId()) {
//		case android.R.id.home:
//			LocationActivity.this.finish();
//			return true;
//		default:
//			return super.onOptionsItemSelected(item);
//		}
//	}
//
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//		mGeoCoder.destroy();
//		if (mLocationClient != null && mLocationClient.isStarted()){
//			mLocationClient.stop();
//		}
//		mMapView.onDestroy();
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//		mMapView.onResume();
//	}
//
//	@Override
//	public void onPause() {
//		super.onPause();
//		mMapView.onPause();
//	}
//
//	private boolean firstTime = true;
//
//	class MyLocationListener implements BDLocationListener {
//
//		@Override
//		public void onReceiveLocation(BDLocation arg0) {
//			if (arg0 == null) {
//				return;
//			}
//			data = new MyLocationData.Builder().latitude(arg0.getLatitude())
//					.longitude(arg0.getLongitude()).build();
//			mBaiduMap.setMyLocationData(data);
//			if (firstTime && !hasLocated) {
//				MyLog.w(TAG, "my location: --->> " + arg0.getAddrStr());
//				mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(
//						new LatLng(arg0.getLatitude(), arg0.getLongitude()), 19));
//				tv_result.setText(arg0.getAddrStr());
//				tv_result.setTag(new double[] { arg0.getLatitude(),arg0.getLongitude() });
//				firstTime = false;
//			}
//		}
//	}
//}
