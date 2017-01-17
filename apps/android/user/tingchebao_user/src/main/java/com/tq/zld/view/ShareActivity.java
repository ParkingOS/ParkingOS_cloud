package com.tq.zld.view;

import java.util.HashMap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.ShareInfo;
import com.tq.zld.pay.Keys;
import com.tq.zld.util.LogUtils;

public class ShareActivity extends BaseActivity implements OnClickListener {

	private View tvWXTimeLine;
	private View tvWXSession;
	private View btnCancel;

	private IWXAPI iWXApi;

	private ShareInfo sInfo;
	private String bonusId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().setBackgroundDrawableResource(android.R.color.black);
		// WindowManager.LayoutParams lp = getWindow().getAttributes();
		// lp.alpha = 0.7f;
		// getWindow().setAttributes(lp);
		setContentView(R.layout.activity_share);
		initView();
		iWXApi = WXAPIFactory.createWXAPI(this, Keys.WXPAY_APPID);
		iWXApi.registerApp(Keys.WXPAY_APPID);
		if (!iWXApi.isWXAppInstalled()) {
			Toast.makeText(this, "请先安装微信客户端！", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		if (!iWXApi.isWXAppSupportAPI()) {
			Toast.makeText(this, "请先升级微信客户端！", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		bonusId = getIntent().getStringExtra("bonusId");
	}

	// private void showProgressDialog() {
	// if (dialog == null) {
	// dialog = ProgressDialog.show(this, "", "请稍后...", false, true);
	// dialog.setCanceledOnTouchOutside(false);
	// }
	// if (!dialog.isShowing()) {
	// dialog.show();
	// }
	// }
	//
	// private void dismissProgressDialog() {
	// if (dialog != null && dialog.isShowing()) {
	// dialog.dismiss();
	// }
	// }

	private void getShareContent(final int reqScene) {
		// http://127.0.0.1/zld/carowner.do?action=obparms&mobile=15375242041
		if (sInfo == null || sInfo.thumbImage == null) {
			showProgressDialog("", true, false);
			String url = TCBApp.mServerUrl + "carowner.do";
			LogUtils.i(getClass(), "获取分享内容url: --->> " + url);
			HashMap<String, String> params = new HashMap<>();
			String action = TextUtils.isEmpty(bonusId) ? "hbparms" : "obparms";
			params.put("action", action);
			params.put("mobile", TCBApp.mMobile);
			new AQuery(this).ajax(url, params, String.class,
					new AjaxCallback<String>() {
						@Override
						public void callback(String url, String object,
								AjaxStatus status) {
							super.callback(url, object, status);
							LogUtils.i(ShareActivity.class,
									"获取分享内容result: --->> " + object);
							if (!TextUtils.isEmpty(object)) {
								try {
									sInfo = new Gson().fromJson(object,
											ShareInfo.class);
									if (sInfo != null
											&& !TextUtils
													.isEmpty(sInfo.description)) {
										ImageLoader
												.getInstance()
												.loadImage(
														TCBApp.mServerUrl
																+ sInfo.imgurl,
														new ImageLoadingListener() {

															@Override
															public void onLoadingStarted(
																	String arg0,
																	View arg1) {
															}

															@Override
															public void onLoadingFailed(
																	String arg0,
																	View arg1,
																	FailReason arg2) {
																shareToWX(reqScene);
															}

															@Override
															public void onLoadingComplete(
																	String arg0,
																	View arg1,
																	Bitmap arg2) {
																sInfo.thumbImage = arg2;
																shareToWX(reqScene);
															}

															@Override
															public void onLoadingCancelled(
																	String arg0,
																	View arg1) {
															}
														});
									} else {
										dismissProgressDialog();
									}
								} catch (Exception e) {
									dismissProgressDialog();
									Toast.makeText(ShareActivity.this,
											"数据异常，请稍后重试...", Toast.LENGTH_SHORT)
											.show();
									e.printStackTrace();
								}
							} else {
								dismissProgressDialog();
								Toast.makeText(ShareActivity.this,
										"网络异常，请稍后重试...", Toast.LENGTH_SHORT)
										.show();
							}
						}
					});
		} else {
			shareToWX(reqScene);
		}
	}

	private void initView() {
		tvWXTimeLine = findViewById(R.id.tv_share_wx_timeline);
		tvWXTimeLine.setOnClickListener(this);
		tvWXSession = findViewById(R.id.tv_share_wx_session);
		tvWXSession.setOnClickListener(this);
		btnCancel = findViewById(R.id.btn_share_cancel);
		btnCancel.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		dismissProgressDialog();
		super.onResume();
	}

	private void shareToWX(int reqScene) {

		WXWebpageObject webObject = new WXWebpageObject();
		webObject.webpageUrl = TCBApp.mServerUrl + sInfo.url + "&id=" + bonusId;

		WXMediaMessage msg = new WXMediaMessage(webObject);
		msg.mediaObject = webObject;
		msg.title = sInfo.title;
		msg.description = sInfo.description;
		if (sInfo.thumbImage == null) {
			sInfo.thumbImage = BitmapFactory.decodeResource(getResources(),
					R.mipmap.ic_launcher);
		}
		msg.setThumbImage(sInfo.thumbImage);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.scene = reqScene;
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;

		iWXApi.sendReq(req);

		// TODO 隐藏分享界面
		finish();
		overridePendingTransition(0, 0);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_share_wx_timeline:
			if (TextUtils.isEmpty(TCBApp.mMobile)) {
				Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(TCBApp.getAppContext(),
						LoginActivity.class));
				return;
			}
			getShareContent(SendMessageToWX.Req.WXSceneTimeline);
			break;
		case R.id.tv_share_wx_session:
			if (TextUtils.isEmpty(TCBApp.mMobile)) {
				Toast.makeText(this, "请先登录！", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(TCBApp.getAppContext(),
						LoginActivity.class));
				return;
			}
			getShareContent(SendMessageToWX.Req.WXSceneSession);
			break;
		case R.id.btn_share_cancel:
			finish();
			overridePendingTransition(0, 0);
			break;
		}
	}
}
