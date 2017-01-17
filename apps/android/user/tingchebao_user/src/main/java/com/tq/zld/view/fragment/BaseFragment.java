package com.tq.zld.view.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;

import com.tq.zld.util.LogUtils;

public abstract class BaseFragment extends Fragment {

	private FragmentManager fm;

	@Override
	public void onResume() {
		super.onResume();
		LogUtils.d("--->> onResume <<---");
		setActivityTitle();
	}

	/**
	 * 设置当前页面的标题
	 */
	protected abstract String getTitle();

	/**
	 * 替换当前显示的Fragment
	 * 
	 * @param fragment
	 *            待替换的Fragment
	 * @param addToBackStatus
	 *            是否添加到返回栈
	 */
	public void replace(int res, Fragment fragment, boolean addToBackStatus) {
		if (fm == null) {
			fm = getActivity().getSupportFragmentManager();
		}
		FragmentTransaction ft = fm.beginTransaction();
		String tag = fragment.getClass().getSimpleName();
		if (addToBackStatus) {
			ft.add(res, fragment,tag);
			ft.hide(this);
			ft.addToBackStack(tag);
		} else {
			ft.replace(res, fragment, tag);
		}
		ft.commit();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtils.d("--->> onCreate <<---");
	}

	@Override
	public void onPause() {
		super.onPause();
		LogUtils.d("--->> onPause <<---");
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		setActivityTitle();
		LogUtils.d("--->> onAttach <<---");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LogUtils.d("--->> onDestroy <<---");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		LogUtils.d("--->> onDetach <<---");
	}

	@Override
	public void onStart() {
		super.onStart();
		LogUtils.d("--->> onStart <<---");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		LogUtils.d("--->> onActivityCreated <<---");
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			setActivityTitle();
		}
		LogUtils.d("--->> onHiddenChanged: " + hidden);
	}

	private void setActivityTitle() {
		if (!TextUtils.isEmpty(getTitle())) {
			getActivity().setTitle(getTitle());
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		LogUtils.d("--->> onStop <<---");
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		LogUtils.d("--->> onViewCreated <<---");
	}
}
