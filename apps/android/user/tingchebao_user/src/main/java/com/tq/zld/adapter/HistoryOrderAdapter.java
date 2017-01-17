package com.tq.zld.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tq.zld.R;
import com.tq.zld.bean.HistoryOrder;

public class HistoryOrderAdapter extends BaseAdapter {

    private ArrayList<HistoryOrder> orders;
    private Context context;

    public HistoryOrderAdapter(Context context) {
        super();
        this.context = context;
    }

    // 设置数据
    public void setData(int page, ArrayList<HistoryOrder> orders) {

        if (page == 1 && this.orders != null) {
            this.orders.clear();
        }

        if (this.orders == null) {
            this.orders = orders;
        } else {
            this.orders.addAll(orders);
        }
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return orders == null ? 0 : orders.size();
    }

    @Override
    public Object getItem(int position) {
        return orders == null ? null : orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HistoryOrder itemData = orders.get(position);
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = View.inflate(context, R.layout.history_order_item, null);
            holder = new ViewHolder();
            holder.moneyView = (TextView) view
                    .findViewById(R.id.tv_history_item_money);
            holder.dateView = (TextView) view
                    .findViewById(R.id.tv_history_item_parkdate);
            holder.nameView = (TextView) view
                    .findViewById(R.id.tv_history_item_parkname);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        holder.moneyView.setText(itemData.getTotal());
        holder.nameView.setText(itemData.getParkname());
        holder.dateView.setText(itemData.getDate());
        return view;
    }

    static class ViewHolder {
        TextView moneyView;
        TextView nameView;
        TextView dateView;
    }
}
