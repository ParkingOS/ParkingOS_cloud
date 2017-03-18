package com.zhenlaidian.ui.score;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.RewardRankingInfo;
import com.zhenlaidian.bean.RewardRankingInfo.RankingInfo;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.IsNetWork;

/**
 * 打赏排行榜
 * 
 * @author zhangyunfei 2015年7月15日
 */
public class RewardRankingActivity extends BaseActivity {

	private ViewPager viewPager;// 页卡内容
	private ImageView imageView;// 动画图片
	private TextView textView1, textView2;
	private List<View> views;// Tab页面列表
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private View view1, view2;// 各个页卡
	private RankingAdapter adapter;
	private ListView lv_score;// 今日积分
	private ListView lv_reward;// 本周赏金
	public int page = 1;
	private int count = 0;// 服务区条目总数
	private int visiblecount = 0;// 可见条目数
	private int currpage = 1;// 当前页面:1积分榜 2赏金榜；

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		setContentView(R.layout.activity_reward_ranking);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		adapter = new RankingAdapter();
		InitImageView();
		InitTextView();
		InitViewPager();
		lv_score.setAdapter(adapter);
		lv_reward.setAdapter(adapter);
		getScoreInfo();
	}

	/**
	 * 初始化数据
	 */
	@SuppressLint({ "InflateParams", "CutPasteId" })
	private void InitViewPager() {
		viewPager = (ViewPager) findViewById(R.id.viewpager_reward_ranking);
		views = new ArrayList<View>();
		LayoutInflater inflater = getLayoutInflater();
		view1 = inflater.inflate(R.layout.page_reward_ranking_score, null);
		view2 = inflater.inflate(R.layout.page_reward_ranking_score, null);
		lv_score = (ListView) view1.findViewById(R.id.lv_reward_ranking_today_score);
		lv_reward = (ListView) view2.findViewById(R.id.lv_reward_ranking_today_score);
		views.add(view1);
		views.add(view2);
		viewPager.setAdapter(new MyViewPagerAdapter(views));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		lv_score.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				// 当不滚动时
				case OnScrollListener.SCROLL_STATE_IDLE:
					// 判断滚动到底部
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						Log.e("lv_score", "count= " + count + "----visiblecount= " + visiblecount);
						if (count > visiblecount) {
							if (currpage == 1) {
								page++;
								getScoreInfo();
							} else {
								page++;
								getRewardScoreInfo();
							}
						}
					}
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				visiblecount = totalItemCount;
			}
		});
		lv_reward.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				// 当不滚动时
				case OnScrollListener.SCROLL_STATE_IDLE:
					// 判断滚动到底部
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						Log.e("lv_reward", "count= " + count + "----visiblecount= " + visiblecount);
						if (count > visiblecount) {
							if (currpage == 1) {
								page++;
								getScoreInfo();
							} else {
								page++;
								getRewardScoreInfo();
							}
						}
					}
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				visiblecount = totalItemCount;
			}
		});

	}

	/**
	 * 初始化头标
	 */

	private void InitTextView() {
		textView1 = (TextView) findViewById(R.id.tv_reward_ranking_today_score);
		textView2 = (TextView) findViewById(R.id.tv_reward_ranking_week_reward);
		textView1.setOnClickListener((android.view.View.OnClickListener) new MyOnClickListener(0));
		textView2.setOnClickListener((android.view.View.OnClickListener) new MyOnClickListener(1));
	}

	/**
	 * 2 * 初始化动画，这个就是页卡滑动时，下面的横线也滑动的效果，在这里需要计算一些数据
	 */

	private void InitImageView() {
		imageView = (ImageView) findViewById(R.id.iv_reward_ranking);
		bmpW = BitmapFactory.decodeResource(this.getResources(), R.drawable.viewpage).getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / 2 - bmpW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		imageView.setImageMatrix(matrix);// 设置动画初始位置
	}

	/**
	 * 
	 * 头标点击监听 3
	 */
	private class MyOnClickListener implements android.view.View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		public void onClick(View v) {
			viewPager.setCurrentItem(index);
		}
	}

	public class MyViewPagerAdapter extends PagerAdapter {
		private List<View> mListViews;

		public MyViewPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mListViews.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mListViews.get(position), 0);
			return mListViews.get(position);
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
		int two = one * 2;// 页卡1 -> 页卡3 偏移量

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		public void onPageSelected(int arg0) {

			Animation animation = new TranslateAnimation(one * currIndex, one * arg0, 0, 0);
			currIndex = arg0;
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
			imageView.startAnimation(animation);
			switch (viewPager.getCurrentItem()) {
			case 0:
				textView1.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
				textView2.setTextColor(getResources().getColor(R.color.input_dialog_black));
				adapter.clearInfo();
				page = 1;
				currpage = 1;
				getScoreInfo();
				break;
			case 1:
				textView2.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
				textView1.setTextColor(getResources().getColor(R.color.input_dialog_black));
				adapter.clearInfo();
				page = 1;
				currpage = 2;
				getRewardScoreInfo();
				break;

			}
		}
	}

	// collectorrequest.do?action=rscorerank&token=
	public void getScoreInfo() {
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络", 0).show();
			return;
		}
		String url = baseurl + "collectorrequest.do?action=rscorerank&token=" + token + "&page=" + page;
		Log.e("RewardRankingActivity", "获取今日积分榜的URL--->" + url);
		final ProgressDialog dialog = ProgressDialog.show(this, "获取积分榜", "加载中...", true, true);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
					dialog.dismiss();
					Log.e("RewardRankingActivity", "获取到今日积分榜--->" + object);
					Gson gson = new Gson();
					RewardRankingInfo info = gson.fromJson(object, RewardRankingInfo.class);
					Log.e("RewardRankingActivity", "解析到今日积分榜--->" + info.toString());
					if (info != null && !TextUtils.isEmpty(info.getCount())) {
						count = Integer.parseInt(info.getCount());
						adapter.addInfo(info.getInfo(), count);
					} else {
						Toast.makeText(RewardRankingActivity.this, "解析今日积分榜出误！", 0).show();
					}
				} else {
					dialog.dismiss();
					switch (status.getCode()) {
					case 500:
						Toast.makeText(RewardRankingActivity.this, "服务器错误！", 0).show();
						break;
					case 404:
						Toast.makeText(RewardRankingActivity.this, "服务器不可用！", 0).show();
						break;
					}
					Toast.makeText(RewardRankingActivity.this, "网络请求错误！", 0).show();
				}
			}
		});
	}

	// collectorrequest.do?action=rewardrank&token=&page=&size=
	public void getRewardScoreInfo() {
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络", 0).show();
			return;
		}
		String url = baseurl + "collectorrequest.do?action=rewardrank&token=" +token + "&page=" + page;
		Log.e("RewardRankingActivity", "获取本周赏金榜的URL--->" + url);
		final ProgressDialog dialog = ProgressDialog.show(this, "获取赏金榜", "加载中...", true, true);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
					dialog.dismiss();
					Log.e("RewardRankingActivity", "获取到本周赏金榜--->" + object);
					Gson gson = new Gson();
					RewardRankingInfo info = gson.fromJson(object, RewardRankingInfo.class);
					Log.e("RewardRankingActivity", "解析到本周赏金榜--->" + info.toString());
					if (info != null && !TextUtils.isEmpty(info.getCount())) {
						count = Integer.parseInt(info.getCount());
						adapter.addInfo(info.getInfo(), count);
					} else {
						Toast.makeText(RewardRankingActivity.this, "解析本周赏金榜出误！", 0).show();
					}
				} else {
					dialog.dismiss();
					switch (status.getCode()) {
					case 500:
						Toast.makeText(RewardRankingActivity.this, "服务器错误！", 0).show();
						break;
					case 404:
						Toast.makeText(RewardRankingActivity.this, "服务器不可用！", 0).show();
						break;
					}
					Toast.makeText(RewardRankingActivity.this, "网络请求错误！", 0).show();
				}
			}
		});
	}

	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			RewardRankingActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public class RankingAdapter extends BaseAdapter {
		private ArrayList<RankingInfo> infos = new ArrayList<RewardRankingInfo.RankingInfo>();

		public void clearInfo() {
			if (infos != null) {
				infos.clear();
			}
		}

		public void addInfo(ArrayList<RankingInfo> infos, int count) {
			this.infos.remove(null);
			this.infos.addAll(infos);
			if (this.infos.size() % 20 == 0 && this.infos.size() < count) {
				this.infos.add(null);
			}
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return infos.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return infos.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = View.inflate(RewardRankingActivity.this, R.layout.item_reward_rank, null);
				holder = new ViewHolder();
				holder.tv_money = (TextView) convertView.findViewById(R.id.tv_item_reward_money);
				holder.tv_parkname = (TextView) convertView.findViewById(R.id.tv_item_reward_parkname);
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_item_reward_name);
				holder.tv_rank = (TextView) convertView.findViewById(R.id.tv_item_reward_rank);
				holder.tv_addmore = (TextView) convertView.findViewById(R.id.tv_item_reward_addmore);
				holder.ll_item = (LinearLayout) convertView.findViewById(R.id.ll_item_reward_item);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (infos.get(position) == null) {
				holder.ll_item.setVisibility(View.GONE);
				holder.tv_addmore.setVisibility(View.VISIBLE);
			} else {
				holder.ll_item.setVisibility(View.VISIBLE);
				holder.tv_addmore.setVisibility(View.GONE);
				if (infos.get(position).getUin().equals(useraccount)) {
					holder.tv_parkname.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));// 字体加粗
					holder.tv_parkname.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
					holder.tv_name.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));// 字体加粗
					holder.tv_name.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
					holder.tv_rank.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));// 字体加粗
					holder.tv_rank.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
					holder.tv_rank.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));// 字体加粗
					holder.tv_rank.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
				} else {
					holder.tv_parkname.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));// 字体正常
					holder.tv_parkname.setTextColor(getResources().getColor(R.color.black));
					holder.tv_name.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));// 字体正常
					holder.tv_name.setTextColor(getResources().getColor(R.color.black));
					holder.tv_rank.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));// 字体正常
					holder.tv_rank.setTextColor(getResources().getColor(R.color.black));
					holder.tv_rank.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));// 字体正常
					holder.tv_rank.setTextColor(getResources().getColor(R.color.black));
				}
				if (infos.get(position).getSort() != null) {
					holder.tv_rank.setText(infos.get(position).getSort());
				}
				if (infos.get(position).getNickname() != null) {
					holder.tv_name.setText(infos.get(position).getNickname());
				}
				if (infos.get(position).getCname() != null) {
					holder.tv_parkname.setText(infos.get(position).getCname());
				}
				if (infos.get(position).getScore() != null) {
					holder.tv_money.setText(infos.get(position).getScore() + "分");
				}
				if (infos.get(position).getMoney() != null) {
					holder.tv_money.setText(infos.get(position).getMoney() + "元");
				}
			}
			return convertView;
		}

		private class ViewHolder {
			TextView tv_money;
			TextView tv_parkname;
			TextView tv_name;
			TextView tv_rank;// 排名
			LinearLayout ll_item;// 条目显示
			TextView tv_addmore;// 加载更多
		}
	}
}
