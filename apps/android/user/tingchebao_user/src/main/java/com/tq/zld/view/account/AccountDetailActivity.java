package com.tq.zld.view.account;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;

import com.rey.material.widget.TabPageIndicator;
import com.tq.zld.R;
import com.tq.zld.view.BaseActivity;
import com.tq.zld.view.fragment.AccountDetailFragment;

public class AccountDetailActivity extends BaseActivity {

	ViewPager mViewPager;
	TabPageIndicator mTitlTabs;
	private AccountDetailPageAdapter mAccountDetailPageAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_detail);
		initToolbar();
		initView();
	}

	private void initToolbar() {
		Toolbar bar = (Toolbar) findViewById(R.id.toolbar_account_detail);
        bar.setBackgroundColor(getResources().getColor(R.color.bg_toolbar));
        bar.setTitle("我的交易明细");
		setSupportActionBar(bar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		bar.setNavigationOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	private void initView() {
		mViewPager = (ViewPager) findViewById(R.id.vp_account_detail);
		mAccountDetailPageAdapter = new AccountDetailPageAdapter(
				getSupportFragmentManager());
		mViewPager.setAdapter(mAccountDetailPageAdapter);
		mTitlTabs = (TabPageIndicator) findViewById(R.id.tab_account_detail);
		mTitlTabs.setViewPager(mViewPager, 0);
	}

	class AccountDetailPageAdapter extends FragmentPagerAdapter {

		private final String[] mTitles = new String[] { "全 部", "充 值", "消 费" };

		public AccountDetailPageAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return mTitles.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mTitles[position];
		}

		@Override
		public Fragment getItem(int arg0) {
			int type = 0;
			switch (arg0) {
			case 0:
				type = AccountDetailFragment.TYPE_ALL;
				break;
			case 1:
				type = AccountDetailFragment.TYPE_RECHARGE;
				break;
			case 2:
				type = AccountDetailFragment.TYPE_PAY;
				break;
			}
			return AccountDetailFragment.newInstance(type);
		}
	}
}
