package com.zhenlaidian.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.R.id;

import java.util.ArrayList;

/**
 * 自定义车牌输入法中键盘部分的适配器;
 */
public class InputCarNumberDialogAdapter extends BaseAdapter {


    private Context context;
    private ArrayList<String> provinces;
    private Boolean isnumber;


    public InputCarNumberDialogAdapter(Context context, ArrayList<String> provinces, Boolean isnumber) {
        super();
        this.context = context;
        this.provinces = provinces;
        this.isnumber = isnumber;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return provinces.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return provinces.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.dialog_input_carnumber_item, null);
            holder = new ViewHolder();
            holder.textinfo = (TextView) convertView.findViewById(id.tv_textinfo_dialog);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (!isnumber) {
            holder.textinfo.setText(provinces.get(position));
        } else {
            String key = provinces.get(position);
            if (key.equals("0")) {
//				holder.textinfo.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gv_number_selector));
                holder.textinfo.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                holder.textinfo.setText(provinces.get(position));
                return convertView;
            } else if (key.equals("1")) {
//				holder.textinfo.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gv_number_selector));
                holder.textinfo.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                holder.textinfo.setText(provinces.get(position));
                return convertView;
            } else if (key.equals("2")) {
//				holder.textinfo.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gv_number_selector));
                holder.textinfo.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                holder.textinfo.setText(provinces.get(position));
                return convertView;
            } else if (key.equals("3")) {
//				holder.textinfo.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gv_number_selector));
                holder.textinfo.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                holder.textinfo.setText(provinces.get(position));
                return convertView;
            } else if (key.equals("4")) {
//				holder.textinfo.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gv_number_selector));
                holder.textinfo.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                holder.textinfo.setText(provinces.get(position));
                return convertView;
            } else if (key.equals("5")) {
//				holder.textinfo.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gv_number_selector));
                holder.textinfo.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                holder.textinfo.setText(provinces.get(position));
                return convertView;
            } else if (key.equals("6")) {
//				holder.textinfo.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gv_number_selector));
                holder.textinfo.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                holder.textinfo.setText(provinces.get(position));
                return convertView;
            } else if (key.equals("7")) {
//				holder.textinfo.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gv_number_selector));
                holder.textinfo.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                holder.textinfo.setText(provinces.get(position));
                return convertView;
            } else if (key.equals("8")) {
//				holder.textinfo.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gv_number_selector));
                holder.textinfo.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                holder.textinfo.setText(provinces.get(position));
                return convertView;
            } else if (key.equals("9")) {
//				holder.textinfo.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gv_number_selector));
                holder.textinfo.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                holder.textinfo.setText(provinces.get(position));
                return convertView;
            } else {
                holder.textinfo.setText(provinces.get(position));
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView textinfo;
    }

}
