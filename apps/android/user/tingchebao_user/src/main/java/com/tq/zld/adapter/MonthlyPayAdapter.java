package com.tq.zld.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.tq.zld.TCBApp;
import com.tq.zld.R;
import com.tq.zld.bean.MonthlyPay;
import com.tq.zld.bean.ParkMonthlyPay;
import com.tq.zld.view.LoginActivity;
import com.tq.zld.view.map.MonthlyPayBuyActivity;
import com.tq.zld.wxapi.WXPayEntryActivity;

public class MonthlyPayAdapter extends BaseExpandableListAdapter {

	private Context context;
	ArrayList<ParkMonthlyPay> infos;

	public MonthlyPayAdapter(Context context) {
		super();
		this.context = context;
		infos = new ArrayList<ParkMonthlyPay>();
	}

	public void setinfos(ArrayList<ParkMonthlyPay> infos) {
		this.infos = infos;
		this.notifyDataSetChanged();
	}

	@Override
	public int getGroupCount() {
		return infos.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return infos.get(groupPosition).monthProducts.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return infos.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return infos.get(groupPosition).monthProducts.get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return Long.parseLong(((MonthlyPay) getChild(groupPosition,
				childPosition)).id);
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		ViewHolder mViewHolder;
		ParkMonthlyPay parkMonthlyPay = (ParkMonthlyPay) getGroup(groupPosition);
		if (convertView == null) {
			convertView = View.inflate(context,
					R.layout.item_monthlypay_elv_groupview, null);
			mViewHolder = new ViewHolder();
			mViewHolder.tv_parkname = (TextView) convertView
					.findViewById(R.id.tv_monthlypay_groupview_parkname);
			mViewHolder.tv_distance = (TextView) convertView
					.findViewById(R.id.tv_monthlypay_groupview_distance);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		mViewHolder.tv_parkname.setText(parkMonthlyPay.company_name);
		String distance = parkMonthlyPay.distance;
		if (distance.startsWith("-")) {
			mViewHolder.tv_distance.setText("未知");
		} else {
			mViewHolder.tv_distance.setText(String.format(
					context.getString(R.string.monthlypay_groupview_distance),
					distance));
		}
		return convertView;
	}

	@Override
	public View getChildView(final int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final ChildViewHolder mChildViewHolder;
		final MonthlyPay monthlyPay = (MonthlyPay) getChild(groupPosition,
				childPosition);
		if (convertView == null) {
			convertView = View.inflate(context,
					R.layout.item_monthlypay_elv_childview, null);
			mChildViewHolder = new ChildViewHolder();
			mChildViewHolder.tv_buy = (TextView) convertView
					.findViewById(R.id.tv_monthlypay_childview_buy);
			mChildViewHolder.tv_name = (TextView) convertView
					.findViewById(R.id.tv_monthlypay_childview_name);
			mChildViewHolder.tv_price = (TextView) convertView
					.findViewById(R.id.tv_monthlypay_childview_price);
			mChildViewHolder.tv_price0 = (TextView) convertView
					.findViewById(R.id.tv_monthlypay_childview_price0);
			mChildViewHolder.tv_price0.getPaint().setFlags(
					Paint.STRIKE_THRU_TEXT_FLAG);
			mChildViewHolder.tv_number = (TextView) convertView
					.findViewById(R.id.tv_monthlypay_childview_number);
			mChildViewHolder.tv_limittime = (TextView) convertView
					.findViewById(R.id.tv_monthlypay_childview_limittime);
			mChildViewHolder.iv_photo = (ImageView) convertView
					.findViewById(R.id.iv_monthlypay_childview_photo);
			mChildViewHolder.iv_type = (ImageView) convertView
					.findViewById(R.id.iv_monthlypay_childview_type);
			// ImageLoaderConfiguration config = new
			// ImageLoaderConfiguration.Builder(
			// context).threadPoolSize(3)
			// .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
			// // .memoryCacheSize(2 * 1024 * 1024)
			// .threadPriority(Thread.NORM_PRIORITY - 1)
			// .denyCacheImageMultipleSizesInMemory().build();
			// mChildViewHolder.mImageLoader.init(config);
			convertView.setTag(mChildViewHolder);
		} else {
			mChildViewHolder = (ChildViewHolder) convertView.getTag();
		}
		mChildViewHolder.tv_name.setText(monthlyPay.name);
		mChildViewHolder.tv_price.setText(Html
				.fromHtml("<big><big><font color='#329762'>"
						+ monthlyPay.price.replace(".00", "")
						+ "</font></big></big>元"));
		if (TextUtils.isEmpty(monthlyPay.price0)
				|| "0.00".equals(monthlyPay.price0)) {
			mChildViewHolder.tv_price0.setVisibility(View.GONE);
		} else {
			mChildViewHolder.tv_price0.setText(String.format(
					context.getString(R.string.monthlypay_childview_price0),
					monthlyPay.price0));
			mChildViewHolder.tv_price0.setVisibility(View.VISIBLE);
		}
		mChildViewHolder.tv_number.setText(String.format(
				context.getString(R.string.monthlypay_childview_number),
				monthlyPay.number));
		mChildViewHolder.tv_limittime.setText(monthlyPay.limittime);
		// public String type;// 产品类型：全天包月（0），夜间包月（1），日间包月（2）
		int typeid = 0;
		switch (monthlyPay.type) {
		case "1":
			typeid = R.drawable.img_monthlypay_night;
			break;
		case "2":
			typeid = R.drawable.img_monthlypay_day;
			break;

		default:
			typeid = R.drawable.img_monthlypay_full;
			break;
		}
		mChildViewHolder.iv_type.setBackgroundResource(typeid);
		// 设置可不可以购买
		if ("1".equals(monthlyPay.isbuy)) {
			mChildViewHolder.tv_buy.setText("已购买");
			mChildViewHolder.tv_buy.setBackgroundResource(R.color.bg_gray);
			mChildViewHolder.tv_buy.setClickable(false);
		} else {
			mChildViewHolder.tv_buy.setText("抢购");
			mChildViewHolder.tv_buy.setBackgroundResource(R.color.red);
			mChildViewHolder.tv_buy.setClickable(true);
			mChildViewHolder.tv_buy.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (TextUtils.isEmpty(TCBApp.mMobile)) {
						Toast.makeText(context, "请先登录！", Toast.LENGTH_SHORT)
								.show();
						context.startActivity(new Intent(context,
								LoginActivity.class));
						return;
					}
					Intent intent = new Intent(context,
							MonthlyPayBuyActivity.class);
					intent.putExtra("name", monthlyPay.name);
					intent.putExtra("type", monthlyPay.type);
					intent.putExtra(WXPayEntryActivity.ARG_MONTYLYPAY_ID, monthlyPay.id);
					intent.putExtra("parkname",
							infos.get(groupPosition).company_name);
					intent.putExtra("limitday", monthlyPay.limitday);
					intent.putExtra("price", monthlyPay.price);
					context.startActivity(intent);
				}
			});
		}
		// TODO 加载图片
		List<String> urls = monthlyPay.photoUrl;
		if (urls != null && urls.size() > 0) {
			DisplayImageOptions options = new DisplayImageOptions.Builder()
					.cacheInMemory(true)
					.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
					.showImageOnLoading(R.drawable.pic_park_ex)
					.resetViewBeforeLoading(true).build();
			ImageLoader.getInstance().displayImage(
					TCBApp.getAppContext().getString(R.string.url_release) + urls.get(0), mChildViewHolder.iv_photo,
					options);
		}
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	static class ViewHolder {
		TextView tv_parkname;
		TextView tv_distance;
	}

	static class ChildViewHolder {

		TextView tv_buy;
		TextView tv_name;
		TextView tv_price;
		TextView tv_price0;
		TextView tv_number;
		TextView tv_limittime;
		ImageView iv_photo;
		ImageView iv_type;
	}
}
