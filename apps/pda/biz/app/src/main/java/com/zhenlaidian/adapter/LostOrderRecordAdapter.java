package com.zhenlaidian.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.LostOrderRecordInfo;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LostOrderRecordActivity;
import com.zhenlaidian.util.TimeTypeUtil;

import java.util.ArrayList;

/**
 * 逃单记录适配器;
 */
public class LostOrderRecordAdapter extends BaseAdapter {

    private ArrayList<LostOrderRecordInfo> orders;
    private Context context;


    public LostOrderRecordAdapter() {
        super();
    }

    public LostOrderRecordAdapter(Context context) {
        super();
        this.context = context;
    }

    public void addOrders(ArrayList<LostOrderRecordInfo> orders) {
        this.orders = orders;
    }

    public void deleteLostOrder(int position) {
        orders.remove(position);
        this.notifyDataSetChanged();
    }

    public void onItemClick(int position, LostOrderRecordActivity activity) {
        if (orders.get(position).getComid().equals(BaseActivity.comid)) {
            activity.cashLostOrder(orders.get(position));
        }
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Object getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.lost_order_record_item, null);
            holder = new ViewHolder();
            holder.tv_cash = (TextView) convertView.findViewById(R.id.tv_lost_order_record_cash);
            holder.tv_parkname = (TextView) convertView.findViewById(R.id.tv_lost_order_record_parkname);
            holder.tv_time = (TextView) convertView.findViewById(R.id.tv_lost_order_record_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (orders.get(position).getComid() != null && orders.get(position).getComid().equals(BaseActivity.comid)) {
            holder.tv_parkname.setText("我的车场");
            holder.tv_cash.setText("结算");
        } else {
            holder.tv_parkname.setText(orders.get(position).getParkname());
        }
        if (orders.get(position).getCreate_time() != null) {
            String time = TimeTypeUtil.getStringTime(Long.parseLong(orders.get(position).getCreate_time()));
            holder.tv_time.setText(time);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tv_parkname;
        TextView tv_time;
        TextView tv_cash;
    }
}
