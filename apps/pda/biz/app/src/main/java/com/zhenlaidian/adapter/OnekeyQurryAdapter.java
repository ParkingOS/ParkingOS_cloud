package com.zhenlaidian.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.ParkeEmployeeInfo;

import java.util.List;

/**
 * 一键查询页面列表适配器;
 */
public class OnekeyQurryAdapter extends BaseAdapter {

    private Context context;
    private List<ParkeEmployeeInfo> info;

    public OnekeyQurryAdapter(Context context, List<ParkeEmployeeInfo> info) {
        super();
        this.context = context;
        this.info = info;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return info.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return info.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_onekey_query, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.tv_item_onekey_query_name);
            holder.tcb = (TextView) convertView.findViewById(R.id.tv_item_onekey_query_account_pay);
            holder.cash = (TextView) convertView.findViewById(R.id.tv_item_onekey_query_cash_pay);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(info.get(position).getNickname() == null ? "" : info.get(position).getNickname());
        holder.tcb.setText(info.get(position).getTotal() == null ? "" : info.get(position).getTotal() + "元");
        holder.cash.setText(info.get(position).getCash() == null ? "" : info.get(position).getCash() + "元");

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView tcb;
        TextView cash;

    }
}
