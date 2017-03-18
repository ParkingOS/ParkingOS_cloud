package com.zhenlaidian.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhenlaidian.R;

import java.util.ArrayList;

/**
 * Created by TCB on 2016/4/16.
 * xulu
 */
public class InputCarNumberGridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> provinces;
    private Boolean isnumber;


    public InputCarNumberGridAdapter(Context context, ArrayList<String> provinces, Boolean isnumber) {
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
            convertView = View.inflate(context, R.layout.x_item_inputcarnum_item, null);
            holder = new ViewHolder();
            holder.textinfo = (TextView) convertView.findViewById(R.id.tv_textinfo_dialog);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (!isnumber) {
            holder.textinfo.setText(provinces.get(position));
        } else {
            String key = provinces.get(position);
            if (key.equals("0") || key.equals("1") || key.equals("2") || key.equals("3") || key.equals("4") || key.equals("5") || key.equals("6") || key.equals("7") || key.equals("8") || key.equals("9")) {
//				holder.textinfo.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gv_number_selector));
                holder.textinfo.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                holder.textinfo.setText(provinces.get(position));
//                return convertView;
            } else {
                holder.textinfo.setText(provinces.get(position));
                holder.textinfo.setTextColor(0xff666666);
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView textinfo;
    }
}
