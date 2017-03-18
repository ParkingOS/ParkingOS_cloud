/**
 * 
 */
package com.zhenlaidian.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.OwnerPayLogsInfo.OwnerPayLogLists;
import com.zhenlaidian.ui.person_account.CarOwnersPayLogActivity;
import com.zhenlaidian.util.DataUtils;

/**
 * @author zhangyunfei 2015年9月19日 车主支付记录
 */
public class OwnersPayLogAdapter extends BaseAdapter {
	public ArrayList<OwnerPayLogLists> infos;
	private Context context;
	private String carnumber;// 查询的车牌号（显示其它车牌号为红色）；

	public OwnersPayLogAdapter(Context context) {
		this.context = context;
	}

	public void addInfo(ArrayList<OwnerPayLogLists> infos, CarOwnersPayLogActivity activity, int count, String carnumber) {
		this.carnumber = carnumber;
		if (infos.size() == 20) {
			activity.setPageNumber();
		}
		if (this.infos == null) {
			this.infos = infos;
			activity.setAdapter();
		} else {
			this.infos.remove(null);
			this.infos.addAll(infos);
			this.notifyDataSetChanged();
		}
		if (this.infos.size() % 20 == 0 && this.infos.size() < count) {
			this.infos.add(null);
			this.notifyDataSetChanged();
		}
	}

	public void cleanInfo() {
		if (this.infos != null) {
			this.infos.clear();
			this.notifyDataSetChanged();
		}
	}

	public OwnerPayLogLists getInfo(int position) {
		if (position <= 0) {
			return null;
		}
		return infos.get(position - 1);
	}

	@Override
	public int getCount() {
		return infos == null ? 0 : infos.size();
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
		// {{"id":"29504","uin":"20551","amount":"2.00","type":"0","create_time":"1442487418",
		// "remark":"停车费_京QLL122","target":"4","orderid":"795868","carnumber":"京QLL122"},{}]}
		ViewHolder holder;
		if (convertView == null) {
			convertView = View.inflate(context, R.layout.item_owners_pay_log, null);
			holder = new ViewHolder();
			holder.tv_payment_money = (TextView) convertView.findViewById(R.id.tv_item_paylog_money);
			holder.tv_payment_note = (TextView) convertView.findViewById(R.id.tv_item_paylog_title);
			holder.tv_payment_target = (TextView) convertView.findViewById(R.id.tv_item_paylog_carnumber);
			holder.tv_payment_ymd = (TextView) convertView.findViewById(R.id.tv_item_paylog_ymd);
			holder.tv_payment_hms = (TextView) convertView.findViewById(R.id.tv_item_paylog_hms);
			holder.tv_paymen_loadmore = (TextView) convertView.findViewById(R.id.tv_payment_item_loadmore);
			holder.ll_item = (LinearLayout) convertView.findViewById(R.id.ll_item_paylog);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (infos.get(position) == null) {
			holder.ll_item.setVisibility(View.GONE);
			holder.tv_paymen_loadmore.setVisibility(View.VISIBLE);
		} else {
			holder.ll_item.setVisibility(View.VISIBLE);
			holder.tv_paymen_loadmore.setVisibility(View.GONE);
			if (!TextUtils.isEmpty(infos.get(position).getAmount())) {
				holder.tv_payment_money.setText("+ " + infos.get(position).getAmount());
			}
			if (!TextUtils.isEmpty(infos.get(position).getCarnumber())) {
				if (carnumber.equals(infos.get(position).getCarnumber())) {
					holder.tv_payment_target.setText(infos.get(position).getCarnumber());
					holder.tv_payment_target.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
				} else {
					holder.tv_payment_target.setText("支付车牌：" + infos.get(position).getCarnumber());
					holder.tv_payment_target.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_red));
				}
			}
			if (!TextUtils.isEmpty(infos.get(position).getRemark())) {
				holder.tv_payment_note.setText(infos.get(position).getRemark());
			}
			if (!TextUtils.isEmpty(infos.get(position).getCreate_time())) {
				String time = infos.get(position).getCreate_time();
				time = DataUtils.toTimestamp(time + "000");
				String[] sourceStrArray = time.split(" ");
				// 年月日yyyy-MM.dd
				holder.tv_payment_ymd.setText(sourceStrArray[0]);
				// 时分秒HH:mm:ss
				holder.tv_payment_hms.setText(sourceStrArray[1]);
			}
		}
		return convertView;
	}

	private static class ViewHolder {
		TextView tv_payment_money;
		TextView tv_payment_note;
		TextView tv_payment_target;
		TextView tv_payment_ymd;
		TextView tv_payment_hms;
		TextView tv_paymen_loadmore;
		LinearLayout ll_item;

	}
}
