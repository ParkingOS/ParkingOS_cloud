package com.zhenlaidian.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.ZhuiJiaoItemEntity;
import com.zhenlaidian.util.CommontUtils;
import java.util.ArrayList;

/**
 * Created by TCB on 2016/4/17.
 */
public class ZhuiJiaoOrderAdapter extends BaseAdapter{
    LayoutInflater inflater;
    ArrayList<ZhuiJiaoItemEntity> entity;
    public ZhuiJiaoOrderAdapter(Context context,ArrayList<ZhuiJiaoItemEntity> entity) {
        this.entity = entity;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return CommontUtils.checkList(entity)?entity.size():0;
    }

    @Override
    public Object getItem(int position) {
        return entity.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        viewHolder holder;
        if(convertView == null){
            holder = new viewHolder();
            convertView = inflater.inflate(R.layout.x_item_zhuijiao_order_layout,null);
            holder.cbox = ((ImageView) convertView.findViewById(R.id.item_cb));
            holder.positionpoint = ((TextView) convertView.findViewById(R.id.item_positionpoint));
            holder.money = ((TextView) convertView.findViewById(R.id.item_money));
            holder.time = ((TextView) convertView.findViewById(R.id.duration));
            convertView.setTag(holder);
        }else{
            holder = (viewHolder) convertView.getTag();
        }
        ZhuiJiaoItemEntity en = entity.get(position);
        if(en.ischeck()){
            holder.cbox.setBackgroundResource(R.drawable.cb_red_on);
        }else{
            holder.cbox.setBackgroundResource(R.drawable.cb_red_off);
        }
        holder.time.setText(CommontUtils.Unix2Time(en.getStart())+"—"+CommontUtils.Unix2Time(en.getEnd()));
        holder.money.setText("￥"+CommontUtils.doubleTwoPoint(Double.parseDouble(en.getTotal()) - Double.parseDouble(en.getPrepay())));
        holder.positionpoint.setText("站点："+en.getBerthsec_name());
        return convertView;
    }
    private class viewHolder{
        ImageView cbox;
        TextView positionpoint,money,time;
    }
}
