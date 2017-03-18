package com.zhenlaidian.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.DrawerItemInfo;

import java.util.ArrayList;

/**
 * 导航抽屉适配器;
 */
public class DrawerAdapter extends BaseAdapter {
    private ArrayList<DrawerItemInfo> lists;
    private Context context;


    public DrawerAdapter(ArrayList<DrawerItemInfo> lists, Context context) {
        this.lists = lists;
        this.context = context;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.drawer_item, null);
            holder = new ViewHolder();
            holder.tv_drawer = (TextView) convertView.findViewById(R.id.tv_drawer_item);
            holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_drawicon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.iv_icon.setImageDrawable(context.getResources().getDrawable(lists.get(position).getId()));
        holder.tv_drawer.setText(lists.get(position).getName());
        return convertView;
    }

    private static class ViewHolder {
        TextView tv_drawer;
        ImageView iv_icon;
    }
}
