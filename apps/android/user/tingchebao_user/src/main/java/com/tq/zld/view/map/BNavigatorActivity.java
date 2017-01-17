package com.tq.zld.view.map;

import java.util.HashMap;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.comapi.mapcontrol.BNMapController;
import com.baidu.navisdk.comapi.routeplan.BNRoutePlaner;
import com.baidu.navisdk.comapi.tts.BNTTSPlayer;
import com.baidu.navisdk.comapi.tts.BNavigatorTTSPlayer;
import com.baidu.navisdk.comapi.tts.IBNTTSPlayerListener;
import com.baidu.navisdk.model.datastruct.LocData;
import com.baidu.navisdk.model.datastruct.SensorData;
import com.baidu.navisdk.ui.routeguide.BNavigator;
import com.baidu.navisdk.ui.routeguide.IBNavigatorListener;
import com.baidu.navisdk.ui.widget.RoutePlanObserver;
import com.baidu.navisdk.ui.widget.RoutePlanObserver.IJumpToDownloadListener;
import com.baidu.nplatform.comapi.map.MapGLSurfaceView;
import com.umeng.analytics.MobclickAgent;

public class BNavigatorActivity extends Activity {

	public static final String ARG_FROM_WHERE = "fromwhere";
	public static final String ARG_ENDPOINT = "endpoint";

	public static final String VALUE_FROM_MAP = "首页";
	public static final String VALUE_FROM_PARKDETAIL = "车场详情";

	private HashMap<String, String> youmengEventParams;
	private LocData currentPosition;

	private IBNavigatorListener mBNavigatorListener = new IBNavigatorListener() {

		@Override
		public void onYawingRequestSuccess() {
			// TODO 偏航请求成功

		}

		@Override
		public void onYawingRequestStart() {
			// TODO 开始偏航请求

		}

		@Override
		public void onPageJump(int jumpTiming, Object arg) {
			// TODO 页面跳转回调
			if (IBNavigatorListener.PAGE_JUMP_WHEN_GUIDE_END == jumpTiming) {
				// 友盟事件统计：导航（id：3）
				youmengEventParams.put("result", "成功");
				MobclickAgent.onEvent(BNavigatorActivity.this, "3");
				finish();
			} else if (IBNavigatorListener.PAGE_JUMP_WHEN_ROUTE_PLAN_FAIL == jumpTiming) {
				youmengEventParams.put("result", "失败");
				MobclickAgent.onEvent(BNavigatorActivity.this, "3");
				finish();
			}
		}

		@Override
		public void notifyGPSStatusData(int arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void notifyLoacteData(LocData arg0) {
			currentPosition = arg0;
		}

		@Override
		public void notifyNmeaData(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void notifySensorData(SensorData arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void notifyStartNav() {
			// TODO Auto-generated method stub
			BaiduNaviManager.getInstance().dismissWaitProgressDialog();
		}

		@Override
		public void notifyViewModeChanged(int arg0) {
			// TODO Auto-generated method stub

		}

	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// 创建NmapView
		MapGLSurfaceView nMapView = BaiduNaviManager.getInstance()
				.createNMapView(this);

		// 创建导航视图
		View navigatorView = BNavigator.getInstance().init(
				BNavigatorActivity.this, getIntent().getExtras(), nMapView);

		// 填充视图
		setContentView(navigatorView);

		// 初始化友盟事件统计参数
		youmengEventParams = new HashMap<String, String>();
		youmengEventParams.put("from",
				getIntent().getStringExtra(ARG_FROM_WHERE));

		BNavigator.getInstance().setListener(mBNavigatorListener);
		BNavigator.getInstance().startNav();
		// 初始化TTS. 开发者也可以使用独立TTS模块，不用使用导航SDK提供的TTS
		BNTTSPlayer.initPlayer();
		// 设置TTS播放回调
		BNavigatorTTSPlayer.setTTSPlayerListener(new IBNTTSPlayerListener() {

			@Override
			public int playTTSText(String arg0, int arg1) {
				// 开发者可以使用其他TTS的API
				return BNTTSPlayer.playTTSText(arg0, arg1);
			}

			@Override
			public void phoneHangUp() {
				// 手机挂断
				BNavigatorTTSPlayer.resumeVoiceTTSOutput();
				// BNavigatorTTSPlayer.setPhoneIn(false);
			}

			@Override
			public void phoneCalling() {
				// 通话中
				BNavigatorTTSPlayer.pauseVoiceTTSOutput();
				// BNavigatorTTSPlayer.setPhoneIn(true);
			}

			@Override
			public int getTTSState() {
				// 开发者可以使用其他TTS的API,
				return BNTTSPlayer.getTTSState();
			}
		});

		BNRoutePlaner.getInstance().setObserver(
				new RoutePlanObserver(this, new IJumpToDownloadListener() {

					@Override
					public void onJumpToDownloadOfflineData() {
						// TODO Auto-generated method stub

					}
				}));

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		BNavigator.getInstance().resume();
		super.onResume();
		BNMapController.getInstance().onResume();
	};

	@Override
	public void onPause() {
		BNavigator.getInstance().pause();
		super.onPause();
		BNMapController.getInstance().onPause();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		BNavigator.getInstance().onConfigurationChanged(newConfig);
		super.onConfigurationChanged(newConfig);
	}

	public void onBackPressed() {
		// 计算导航是否成功
		double[] endPoint = getIntent().getDoubleArrayExtra(ARG_ENDPOINT);
		if (endPoint != null && endPoint.length == 2 && currentPosition != null) {
			String result = 300 > DistanceUtil.getDistance(new LatLng(
					currentPosition.latitude, currentPosition.longitude),
					new LatLng(endPoint[0], endPoint[1])) ? "成功" : "失败";
			youmengEventParams.put("result", result);
		} else {
			youmengEventParams.put("result", "失败");
		}
		MobclickAgent.onEvent(BNavigatorActivity.this, "3", youmengEventParams);
		BNavigator.getInstance().onBackPressed();
		// BNTTSPlayer.stopTTS();
	}

	@Override
	public void onDestroy() {
		BNavigator.destory();
		BNRoutePlaner.getInstance().setObserver(null);
		super.onDestroy();
	}
}
