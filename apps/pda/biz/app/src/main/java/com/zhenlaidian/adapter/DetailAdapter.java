package com.zhenlaidian.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.HuizongItem;
import com.zhenlaidian.bean.HuizongItems;
import com.zhenlaidian.util.MyListView;

import java.util.ArrayList;

/**
 * Created by xulu on 2016/10/25.
 */
public class DetailAdapter extends BaseAdapter {
    ArrayList<HuizongItems> infos;
    Context context;
    LayoutInflater inflater;

    public DetailAdapter(ArrayList<HuizongItems> infos, Context context) {
        this.infos = infos;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return infos != null && infos.size() > 0 ? infos.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewHolder holder;
        if (convertView == null) {
            holder = new viewHolder();
            convertView = inflater.inflate(R.layout.item_detail_layout, null);
            holder.name = ((TextView) convertView.findViewById(R.id.name));
            holder.value = ((TextView) convertView.findViewById(R.id.value));
            holder.listview = ((MyListView) convertView.findViewById(R.id.content));
            convertView.setTag(holder);
        } else {
            holder = ((viewHolder) convertView.getTag());
        }
        HuizongItems items = infos.get(position);
        holder.name.setText(items.getName());
//        holder.name.setTextSize(CommontUtils.px2sp(context, Integer.parseInt(TextUtils.isEmpty(items.getFontSize()) ? "14" : items.getFontSize())));
        holder.name.setTextSize(Integer.parseInt(TextUtils.isEmpty(items.getFontSize()) ? "14" : items.getFontSize()));
        String color;
        if (TextUtils.isEmpty(items.getFontColor())) {
            color = "#000000";
        } else {
            color = items.getFontColor();
        }
        holder.name.setTextColor(Color.parseColor(color));
        holder.value.setTextColor(Color.parseColor(color));

        holder.value.setText(TextUtils.isEmpty(items.getValue()) ? "" : items.getValue());
        holder.value.setTextSize(Integer.parseInt(TextUtils.isEmpty(items.getFontSize()) ? "14" : items.getFontSize()));

        ArrayList<HuizongItem> content = items.getContent();
        if (content != null && content.size() > 0) {
            contentAdapter adapter = new contentAdapter(content, context);
            holder.listview.setVisibility(View.VISIBLE);
            holder.listview.setAdapter(adapter);
        }else{
            holder.listview.setVisibility(View.GONE);
        }
        return convertView;
    }

    private class viewHolder {
        TextView name;
        TextView value;
        MyListView listview;
    }

    private class contentAdapter extends BaseAdapter {
        ArrayList<HuizongItem> content;
        LayoutInflater inflaters;

        public contentAdapter(ArrayList<HuizongItem> content, Context context) {
            this.content = content;
            inflaters = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return content != null && content.size() > 0 ? content.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            viewHolder holder;
            if (convertView == null) {
                holder = new viewHolder();
                convertView = inflater.inflate(R.layout.item_detail_layout, null);
                holder.name = ((TextView) convertView.findViewById(R.id.name));
                holder.value = ((TextView) convertView.findViewById(R.id.value));

                convertView.setTag(holder);
            } else {
                holder = ((viewHolder) convertView.getTag());
            }
            HuizongItem item = content.get(position);
            holder.name.setText(item.getName());
            holder.value.setText(TextUtils.isEmpty(item.getValue()) ? "" : item.getValue());
//            holder.name.setTextSize(CommontUtils.px2sp(context, Integer.parseInt(TextUtils.isEmpty(item.getFontSize()) ? "14" : item.getFontSize())));
            holder.name.setTextSize(Integer.parseInt(TextUtils.isEmpty(item.getFontSize()) ? "14" : item.getFontSize()));
            holder.value.setTextSize(Integer.parseInt(TextUtils.isEmpty(item.getFontSize()) ? "14" : item.getFontSize()));
            String color;
            if (TextUtils.isEmpty(item.getFontColor())) {
                color = "#000000";
            } else {
                color = item.getFontColor();
            }
            holder.name.setTextColor(Color.parseColor(color));
            holder.value.setTextColor(Color.parseColor(color));
            return convertView;
        }
    }
}
