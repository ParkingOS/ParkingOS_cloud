package com.tq.zld.adapter;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.RedpacketsInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RedpacketAdapter extends BaseAdapter {

    private ArrayList<RedpacketsInfo> mRedpackets;

    public void setData(ArrayList<RedpacketsInfo> data) {
        if (data == null || data.size() == 0) {
            return;
        }
        mRedpackets = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mRedpackets == null ? 0 : mRedpackets.size();
    }

    @Override
    public Object getItem(int position) {
        return mRedpackets == null ? null : mRedpackets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isEnabled(int position) {
        if (getItem(position) != null) {
            RedpacketsInfo packet = (RedpacketsInfo) getItem(position);
            return 1 == packet.state;
        }
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RedpacketsInfo info = mRedpackets.get(position);
        ViewHolder mHolder;
        if (convertView == null) {
            convertView = View.inflate(TCBApp.getAppContext(), R.layout.item_my_redpackets, null);
            mHolder = new ViewHolder();
            mHolder.iconView = (ImageView) convertView.findViewById(R.id.iv_listitem_red_packet_icon);
            mHolder.limitView = (TextView) convertView.findViewById(R.id.tv_listitem_red_packet_limit);
            mHolder.titleView = (TextView) convertView.findViewById(R.id.tv_listitem_red_packet_title);
            mHolder.stateView = (TextView) convertView.findViewById(R.id.tv_listitem_red_packet_state);
            Resources resources = TCBApp.getAppContext().getResources();
            mHolder.gray = resources.getColor(R.color.text_gray);
            mHolder.red = resources.getColor(R.color.text_red);
            mHolder.yellow = resources.getColor(R.color.text_orange);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        // 设置礼包名称
        mHolder.titleView.setText(info.title);

        // 设置过期时间
        Date d = new Date(info.exptime * 1000);
        SimpleDateFormat sf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.CHINA);
        String time = sf.format(d);
        mHolder.limitView.setText(time + "前领取有效");

        // 设置领取状态
        switch (info.state) {//is_auth  : 0:已过期，1未过期，可以领取，2，已领取
            case 0:
                mHolder.stateView.setTextColor(mHolder.gray);
                mHolder.stateView.setText("已过期");

                // 设置标题字体颜色
                mHolder.titleView.setTextColor(mHolder.gray);
                break;
            case 1:
                mHolder.stateView.setTextColor(mHolder.yellow);
                mHolder.stateView.setText("立即领取>");

                // 设置标题字体颜色
                mHolder.titleView.setTextColor(Color.BLACK);
                break;
            case 2:
                mHolder.stateView.setTextColor(mHolder.red);
                mHolder.stateView.setText("已领取");

                // 设置标题字体颜色
                mHolder.titleView.setTextColor(mHolder.gray);
                break;
            default:
                break;
        }

        // 设置图标
        mHolder.iconView.setImageResource(getIconRes(info.state, info.title));
        return convertView;
    }

    private int getIconRes(int state, String title) {
        switch (state) {
            case 1://待领取
                if (title.contains("充值")) {
                    return R.drawable.img_red_packet_recharge_normal;
                } else if (title.contains("认证")) {
                    return R.drawable.img_red_packet_certify_normal;
                } else if (title.contains("游戏")) {
                    return R.drawable.img_red_packet_game_normal;
                } else {
                    // 默认返回订单红包
                    return R.drawable.img_red_packet_order_normal;
                }
            default:

                // 建议采用BitmapUtils主动做图片灰度处理
                if (title.contains("充值")) {
                    return R.drawable.img_red_packet_recharge_disable;
                } else if (title.contains("认证")) {
                    return R.drawable.img_red_packet_certify_disable;
                } else if (title.contains("游戏")) {
                    return R.drawable.img_red_packet_game_disable;
                } else {
                    // 默认返回订单红包
                    return R.drawable.img_red_packet_order_disable;
                }
        }
    }

    static class ViewHolder {
        ImageView iconView;
        TextView limitView;
        TextView titleView;
        TextView stateView;

        int yellow;
        int gray;
        int red;
    }
}
