package com.zhenlaidian.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.LostOrderRecordInfo;
import com.zhenlaidian.ui.HistoryOrderLostOrderActivity;
import com.zhenlaidian.ui.LostOrderRecordDetailsActivity;
import com.zhenlaidian.util.TimeTypeUtil;

import java.util.ArrayList;

/**
 * 历史订单--逃单列表适配器;
 */
public class HistoryOrderLostOrderAdapter extends BaseAdapter {

    private ArrayList<LostOrderRecordInfo> orders = null;
    private Context context;

    public HistoryOrderLostOrderAdapter(Context context) {
        this.context = context;
    }

    public void addOrder(ArrayList<LostOrderRecordInfo> orders) {
        if (this.orders == null) {
            this.orders = orders;
        } else {
            this.orders.clear();
            this.orders = orders;
        }
    }

    public void onItemClick(int position) {
        Intent intent = new Intent(context, LostOrderRecordDetailsActivity.class);
        String orderid = orders.get(position).getOrder_id();
        intent.putExtra("orderid", orderid);
        context.startActivity(intent);
    }

    public void onItemLongClick(int position, HistoryOrderLostOrderActivity activity) {
        if (orders.get(position).getCar_number() != null) {
            activity.lostOrderDialog(orders.get(position).getCar_number(), orders.get(position), position);
        }
    }

    public void removeOrder(int position) {
        orders.remove(position);
        this.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return orders.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return orders.get(position);
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
            convertView = View.inflate(context, R.layout.history_order_item, null);
            holder = new ViewHolder();
            holder.tv_money = (TextView) convertView.findViewById(R.id.tv_history_item_money);
            holder.tv_parkTime = (TextView) convertView.findViewById(R.id.tv_history_item_parkTime);
            holder.tv_plateNumber = (TextView) convertView.findViewById(R.id.tv_history_item_plateNumber);
            holder.tv_duration = (TextView) convertView.findViewById(R.id.tv_history_item_duration);
            holder.tv_lostorder = (TextView) convertView.findViewById(R.id.tv_history_order_loset_order);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//		Typeface face = Typeface.createFromAsset(context.getAssets(),"Font/Roboto/Roboto-Black.ttf");
//		holder.tv_money.setTypeface(face);
        if (orders.get(position).getTotal() != null) {
            holder.tv_money.setText(orders.get(position).getTotal());
        }
        if (orders.get(position).getCreate_time() != null && orders.get(position).getEnd_time() != null) {
            holder.tv_parkTime.setText(TimeTypeUtil.getStringTime(Long.parseLong(orders.get(position).getCreate_time())));
            holder.tv_duration.setText(TimeTypeUtil.getTimeString(Long.parseLong(orders.get(position).getCreate_time()), Long.parseLong(orders.get(position).getEnd_time())));
        }
        if (orders.get(position).getCar_number() != null) {
            holder.tv_plateNumber.setText(orders.get(position).getCar_number());
        }
        holder.tv_lostorder.setBackgroundResource(R.drawable.lost_stamp);
        return convertView;
    }

    private static class ViewHolder {
        TextView tv_money;
        TextView tv_parkTime;
        TextView tv_plateNumber;
        TextView tv_duration;
        TextView tv_lostorder;
    }
}
