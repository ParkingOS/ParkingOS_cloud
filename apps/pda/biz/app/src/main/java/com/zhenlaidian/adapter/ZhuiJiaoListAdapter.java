package com.zhenlaidian.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.ZhuiJiaoItemEntity;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.util.CommontUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TCB on 2016/4/17.
 * xulu
 */
public class ZhuiJiaoListAdapter extends BaseAdapter{

    LayoutInflater inflater;
    ArrayList<ZhuiJiaoItemEntity> entity;
    onClickImg ImgCallBack;

    public ZhuiJiaoListAdapter(Context context,ArrayList<ZhuiJiaoItemEntity> entity,onClickImg ImgCallBack) {
        this.entity = entity;
        this.ImgCallBack = ImgCallBack;
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
            convertView = inflater.inflate(R.layout.x_item_zhuijiao_list_layout,null);
            holder.carnum = (TextView) convertView.findViewById(R.id.item_carnum);
            holder.time = (TextView) convertView.findViewById(R.id.item_time);
            holder.money = (TextView) convertView.findViewById(R.id.item_money);
            holder.length = (TextView) convertView.findViewById(R.id.item_length);
            holder.oweimg = ((ImageView) convertView.findViewById(R.id.item_oweimg));
            convertView.setTag(holder);
        }else{
            holder = (viewHolder) convertView.getTag();
        }
        final ZhuiJiaoItemEntity en = entity.get(position);
        holder.carnum.setText(en.getCar_number());
        holder.time.setText(CommontUtils.Unix2Time(en.getStart()));
        holder.money.setText("￥"+CommontUtils.doubleTwoPoint(Double.parseDouble(en.getTotal()) - Double.parseDouble(en.getPrepay())));
        holder.length.setText(""+en.getDuartion());
        if(!TextUtils.isEmpty(en.getPicurls())){
            holder.oweimg.setVisibility(View.VISIBLE);

            holder.oweimg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<String> lists = new ArrayList<String>();
                    String tmp = en.getPicurls();
                    String[] urls = tmp.split(",");
                    for(int i = 0;i<urls.length;i++){
                        Log.i("tmp",i+":++++++++"+urls[i]);
                        lists.add(BaseActivity.baseurl+urls[i]);
                    }
//
//                    lists.add("111111");
//                    lists.add("22222");
//                    lists.add("333333");
                    ImgCallBack.ClickImg(lists);
                }
            });
        }else{
            holder.oweimg.setVisibility(View.GONE);
        }

        return convertView;
    }
    private class viewHolder{
        TextView carnum,time,money,length;
        ImageView oweimg;
    }
    public interface onClickImg{
        void ClickImg(List<String> paths);
    }
}
