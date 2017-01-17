package com.tq.zld.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tq.zld.R;
import com.tq.zld.bean.MyItemInfo;
import com.tq.zld.view.account.BoughtProductActivity;

public class MyItemAdapter extends BaseAdapter {
	private ArrayList<MyItemInfo> infos;
	private Context context;

	public MyItemAdapter(Context context) {
		super();
		this.context = context;
	}

	public void setinfos(ArrayList<MyItemInfo> infos, BoughtProductActivity activity) {

		if (this.infos == null) {
			this.infos = infos;
			activity.setAdapter();
		} else {
			this.infos.addAll(infos);
			this.notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		return infos.size();
	}

	@Override
	public Object getItem(int position) {
		return infos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MyItemInfo info = infos.get(position);
		ViewHolder mHolder = null;
		if (convertView == null) {
			convertView = View.inflate(context, R.layout.my_item_item, null);
			mHolder = new ViewHolder();
			initview(convertView, mHolder);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		mHolder.tv_limitdate.setText(info.getLimitdate());
		mHolder.tv_limitday.setText(info.getLimitday());
		mHolder.tv_limittime.setText(info.getLimittime());
		mHolder.tv_name.setText(Html.fromHtml(info.getName() + "-<small>"
				+ info.getParkname() + "</small>"));
		mHolder.tv_price.setText("￥" + info.getPrice());
		return convertView;
	}

	private void initview(View view, ViewHolder holder) {
		holder.tv_limitdate = (TextView) view
				.findViewById(R.id.tv_myitem_item_limitdate);
		holder.tv_limitday = (TextView) view
				.findViewById(R.id.tv_myitem_item_limitday);
		holder.tv_limittime = (TextView) view
				.findViewById(R.id.tv_myitem_item_limittime);
		holder.tv_name = (TextView) view.findViewById(R.id.tv_myitem_item_name);
		holder.tv_price = (TextView) view
				.findViewById(R.id.tv_myitem_item_price);
	}

	static class ViewHolder {
		TextView tv_limitdate;
		TextView tv_limitday;
		TextView tv_limittime;
		TextView tv_name;
		TextView tv_price;
	}
}
