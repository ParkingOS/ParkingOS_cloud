package com.tq.zld.view.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.ParkInfo;
import com.tq.zld.protocal.SimpleVolleyErrorListener;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.UGCActivity;

public class AuditFragment extends NetworkFragment<ParkInfo> {

	private CheckBox mPaytypeCheckBox;
	private CheckBox mAddrCheckBox;
	private CheckBox mParknameCheckBox;
	private CheckBox mDescCheckBox;
	private TextView mDescTextView;
	// private TextView mMoreInfoTextView;
	private View mRuleView;
	private View mAuditNextView;
	private Button mAuditButton;
	// private View mMoreInfoView;

	private UGCActivity mActivity;

	private HashMap<String, String> mParams;
	private ParkInfo mPark;
	private ArrayList<String> mParkIDs;

	@Override
	protected String getTitle() {
		return "审核停车场";
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			if (mActivity != null) {
				mActivity.getToolbar().setTitle(getTitle());
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof UGCActivity) {
			mActivity = (UGCActivity) activity;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_audit2, container, false);
	}

	@Override
	public void onClick(View v) {
		if (v == mRuleView) {
			onRuleViewCLicked();
		} else if (v == mAuditNextView) {
			onAuditNextViewClicked();
		} else if (v == mAuditButton) {
			onAuditBtnClicked();
		} else if (v == mDescTextView) {
			onDescTextViewClicked();
		}
		// else if (v == mMoreInfoTextView) {
		// onMoreInfoTextViewClicked();
		// }
	}

	// private void onMoreInfoTextViewClicked() {
	// int start = 0;
	// int end = DensityUtils.dip2px(mActivity, 96);
	// if (mMoreInfoTextView.isSelected()) {
	// int temp;
	// temp = start;
	// start = end;
	// end = temp;
	// }
	// ViewUtils.performAnimate(mMoreInfoView, false, start, end, 200,
	// new AnimatorListener() {
	//
	// @Override
	// public void onAnimationStart(Animator animation) {
	// }
	//
	// @Override
	// public void onAnimationRepeat(Animator animation) {
	// }
	//
	// @Override
	// public void onAnimationEnd(Animator animation) {
	// mMoreInfoTextView.setSelected(!mMoreInfoTextView
	// .isSelected());
	// }
	//
	// @Override
	// public void onAnimationCancel(Animator animation) {
	// }
	// });
	// }

	private void onDescTextViewClicked() {
		if (mDescTextView.getLineCount() < 4) {
			return;
		}
		showDescDialog();
	}

	private void showDescDialog() {
		TextView view = new TextView(mActivity);
		view.setText(mPark.desc);
		new AlertDialog.Builder(mActivity).setView(view).show();
		// AppCompatDialog appCompatDialog = new AppCompatDialog(mActivity);
		// appCompatDialog.setContentView(view);
		// appCompatDialog.show();
	}

	private void onAuditBtnClicked() {

		if ("审核下一个".equals(mAuditButton.getText())) {
			onAuditNextViewClicked();
			return;
		}

		if (mPark == null || mPark.id.startsWith("-")) {
			return;
		}
		String name = mParknameCheckBox.isChecked() ? "1" : "0";
		String addr = mAddrCheckBox.isChecked() ? "1" : "0";
		String free = mPaytypeCheckBox.isChecked() ? "1" : "0";
		String desc = mDescCheckBox.isChecked() ? "1" : "0";
		Map<String, String> params = new HashMap<>();
		params.put("action", "verifypark");
		params.put("id", mPark.id);
		params.put("isname", name);
		params.put("islocal", addr);
		params.put("ispay", free);
		params.put("isresume", desc);
		params.put("mobile", TCBApp.mMobile);
		String url = URLUtils.genUrl(TCBApp.mServerUrl + "carinter.do", params);
		StringRequest request = new StringRequest(url, new Listener<String>() {

			@Override
			public void onResponse(String arg0) {
				if ("1".equals(arg0)) {
					Toast.makeText(mActivity, "感谢您的审核！", Toast.LENGTH_SHORT)
							.show();
					mPark = null;
					mAuditButton.setText("审核下一个");
				} else if ("-1".equals(arg0)) {
					Toast.makeText(mActivity, "您已审核过此车场，不能重复审核！",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mActivity, "提交错误，请稍后再试~", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}, new SimpleVolleyErrorListener());
		TCBApp.getAppContext().addToRequestQueue(request, this);
	}

	private void onRuleViewCLicked() {
		final ProgressDialog dialog = ProgressDialog.show(mActivity, "",
				"请稍候...");
		WebView view = new WebView(mActivity);
		view.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				dialog.dismiss();
				new AlertDialog.Builder(mActivity).setView(view)
						.setPositiveButton("知道了", null).show();
			}
		});
		WebSettings settings = view.getSettings();
		settings.setUseWideViewPort(false);
		settings.setLoadWithOverviewMode(false);
		view.loadUrl(TCBApp.mServerUrl + "carinter.do?action=verifyrule");
	}

	private void onAuditNextViewClicked() {
		mActivity.setCanUpdateMap(true);
		getData();
	}

	@Override
	protected TypeToken<ParkInfo> getBeanListType() {
		return null;
	}

	@Override
	protected Class<ParkInfo> getBeanClass() {
		return ParkInfo.class;
	}

	@Override
	protected String getUrl() {
		return TCBApp.mServerUrl + "carinter.do";
	}

	@Override
	protected Map<String, String> getParams() {

		if (mParams == null) {
			mParams = new HashMap<>();
			mParams.put("action", "preverifypark");
			mParams.put("mobile", TCBApp.mMobile);
			mParams.put("lat", String.valueOf(TCBApp.mLocation.latitude));
			mParams.put("lng", String.valueOf(TCBApp.mLocation.longitude));
		}

		if (mParkIDs != null) {
			mParams.put("ids",
					mParkIDs.toString().replace("[", "").replace("]", "")
							.replace(" ", ""));
		}
		return mParams;
	}

	@Override
	protected void initView(View view) {
		mDescTextView = (TextView) view.findViewById(R.id.tv_audit_desc);
		mDescTextView.setOnClickListener(this);
		mPaytypeCheckBox = (CheckBox) view.findViewById(R.id.cb_audit_paytype);
		mDescCheckBox = (CheckBox) view.findViewById(R.id.cb_audit_desc);
		mParknameCheckBox = (CheckBox) view
				.findViewById(R.id.cb_audit_parkname);
		mAddrCheckBox = (CheckBox) view.findViewById(R.id.cb_audit_addr);
		mRuleView = view.findViewById(R.id.tv_audit_rule);
		mRuleView.setOnClickListener(this);
		mAuditNextView = view.findViewById(R.id.tv_audit_next);
		mAuditNextView.setOnClickListener(this);
		mAuditButton = (Button) view.findViewById(R.id.btn_audit);
		mAuditButton.setOnClickListener(this);
		// mMoreInfoTextView = (TextView) view
		// .findViewById(R.id.tv_audit_moreinfo);
		// mMoreInfoTextView.setOnClickListener(this);
		// mMoreInfoView = view.findViewById(R.id.ll_audit_moreinfo);
	}

	@Override
	public void onNetWorkResponse(ParkInfo response) {
		this.mPark = response;

		if (response != null && !TextUtils.isEmpty(response.id)) {
			int id = 0;
			try {
				id = Integer.parseInt(response.id);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (id > 0) {

				// 记录已审核的车场id
				if (mParkIDs == null) {
					mParkIDs = new ArrayList<>();
				}
				if (!mParkIDs.contains(response.id)) {
					mParkIDs.add(response.id);
				}
				if (mParkIDs.size() > 30) {
					mParkIDs.remove(0);
				}
				LogUtils.i(getClass(), "mParkIDs: --->> " + mParkIDs.toString());
				// 有可审核车场
				mAuditButton.setText("提交审核");
				showDataView();
				mActivity.setCanUpdateMap(true);
				mActivity.setMapMode(response);
				mDescTextView.setText(response.desc);
				boolean checked = "0".equals(response.type);
				mPaytypeCheckBox.setChecked(checked);
			} else {
				switch (id) {
				case -2:// 周围没有可审核车场
					showEmptyView("您周围没有待审核车场哦~", 0, null);
					break;
				case -1:// 已审核超过三个车场
					showEmptyView("您今天已审核足够多的车场啦~\n明天再来试试吧~", 0, null);
					break;
				}
			}
		}
	}

	@Override
	protected int getFragmentContainerResID() {
		return R.id.ugc_content;
	}
}
