package com.zhenlaidian.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.RankingInfo;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LastRankingactivity;
import com.zhenlaidian.ui.RankingActivity;

public class RankingAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<RankingInfo> infos = null;

	public RankingAdapter(Context context) {
		super();
		this.context = context;
	}

	public void setRankInfo(ArrayList<RankingInfo> infos,RankingActivity activity) {
		if (infos == null || infos.isEmpty()) {
			return;
		}
		if (this.infos == null) {
			this.infos = infos;
			activity.setAdapter();
		} else {
			this.infos = infos;
			this.notifyDataSetChanged();
		}
	}

	public void setLastRankInfo(ArrayList<RankingInfo> infos,
			LastRankingactivity activity) {
		if (infos == null || infos.isEmpty()) {
			return;
		}
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

	public View getView(int position, View convertView, ViewGroup parent) {
		VeiwHolder holder;
		if (convertView == null) {
			convertView= View.inflate(context, R.layout.ranking_item, null);
			holder = new VeiwHolder();
			holder.tv_rank = (TextView) convertView.findViewById(R.id.tv_ranking_item_rank);
			holder.tv_admin_name = (TextView) convertView.findViewById(R.id.tv_ranking_item_admin_name);
			holder.tv_parkname = (TextView) convertView.findViewById(R.id.tv_ranking_item_parkname);
			holder.tv_scroe = (TextView) convertView.findViewById(R.id.tv_ranking_item_mark);
			convertView.setTag(holder);
		} else {
			holder = (VeiwHolder) convertView.getTag();
		}
		if (infos.get(position).getUin().equals(BaseActivity.useraccount)) {
			holder.tv_parkname.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));// 字体加粗
			holder.tv_parkname.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
			holder.tv_admin_name.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));// 字体加粗
			holder.tv_admin_name.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
			holder.tv_rank.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));// 字体加粗
			holder.tv_rank.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
			holder.tv_scroe.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));// 字体加粗
			holder.tv_scroe.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
			
			holder.tv_admin_name.setText(infos.get(position).getNickname());
			holder.tv_rank.setText(infos.get(position).getSort());
			holder.tv_parkname.setText(infos.get(position).getCname());
			holder.tv_scroe.setText(infos.get(position).getScore());
		}else {
			holder.tv_parkname.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));// 字体正常
			holder.tv_parkname.setTextColor(context.getResources().getColor(R.color.black));
			holder.tv_admin_name.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));// 字体正常
			holder.tv_admin_name.setTextColor(context.getResources().getColor(R.color.black));
			holder.tv_rank.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));// 字体正常
			holder.tv_rank.setTextColor(context.getResources().getColor(R.color.black));
			holder.tv_scroe.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));// 字体正常
			holder.tv_scroe.setTextColor(context.getResources().getColor(R.color.black));
			
			holder.tv_admin_name.setText(infos.get(position).getNickname());
			holder.tv_rank.setText(infos.get(position).getSort());
			holder.tv_parkname.setText(infos.get(position).getCname());
			holder.tv_scroe.setText(infos.get(position).getScore());
		}
		return convertView;
	}

	private static class VeiwHolder {
		TextView tv_rank;
		TextView tv_admin_name;
		TextView tv_parkname;
		TextView tv_scroe;
	}
}
