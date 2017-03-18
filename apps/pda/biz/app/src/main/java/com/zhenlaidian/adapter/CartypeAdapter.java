package com.zhenlaidian.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.CarTypeItem;

import java.util.ArrayList;

/**
 * Created by xulu on 2016/10/24.
 */
public class CartypeAdapter extends BaseAdapter {
    ArrayList<CarTypeItem> car_type;
    Context context;
    LayoutInflater inflater;

    public CartypeAdapter(ArrayList<CarTypeItem> car_type, Context context) {
        this.car_type = car_type;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return car_type != null && car_type.size() > 0 ? car_type.size() : 0;
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
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_cartype_layout, null);
            holder.text = ((TextView) convertView.findViewById(R.id.textcartype));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.text.setText(car_type.get(position).getName());
        return convertView;
    }

    private class ViewHolder {
        TextView text;
    }
}
