package com.zhenlaidian.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.MyParkAccountDetai;
import com.zhenlaidian.ui.person_account.PaymentDetailActivity;
import com.zhenlaidian.util.DataUtils;

import java.util.ArrayList;

/**
 * 我的账户详情适配器;
 */

public class MyAccountDetailAdapter extends BaseAdapter {

    private ArrayList<MyParkAccountDetai> allDetail;
    private Context context;

    public MyAccountDetailAdapter(Context context, ArrayList<MyParkAccountDetai> allDetail) {
        this.context = context;
        this.allDetail = allDetail;
    }

    // 订单条目的点击事件。
    public void onItemClick(int position, PaymentDetailActivity paymentDetailActivity) {

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return allDetail.isEmpty() ? 0 : allDetail.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return allDetail.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.payment_item, null);
            holder = new ViewHolder();
            holder.tv_payment_money = (TextView) convertView.findViewById(R.id.tv_payment_money);
            holder.tv_payment_note = (TextView) convertView.findViewById(R.id.tv_payment_note);
            holder.tv_payment_target = (TextView) convertView.findViewById(R.id.tv_payment_target);
            holder.tv_payment_ymd = (TextView) convertView.findViewById(R.id.tv_payment_ymd);
            holder.tv_payment_hms = (TextView) convertView.findViewById(R.id.tv_payment_hms);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (allDetail.get(position).getA() != null) {//金额
            if (allDetail.get(position).getT().equals("1")) {
                holder.tv_payment_money.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_red));
                holder.tv_payment_money.setText("-" + allDetail.get(position).getA());
            } else {
                holder.tv_payment_money.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                holder.tv_payment_money.setText("+" + allDetail.get(position).getA());
            }
        }
        if (allDetail.get(position).getT1() != null) {//提现
            holder.tv_payment_note.setText(allDetail.get(position).getT1());
        }
        if (allDetail.get(position).getT2() != null) {//银行卡类型
            holder.tv_payment_target.setText(allDetail.get(position).getT2());
        }
        String time = allDetail.get(position).getC();//时间
        if (time != null) {
            time = DataUtils.toTimestamp(time + "000");
            String[] sourceStrArray = time.split(" ");
            //年月日yyyy-MM.dd
            holder.tv_payment_ymd.setText(sourceStrArray[0]);
            //时分秒HH:mm:ss
            holder.tv_payment_hms.setText(sourceStrArray[1]);
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView tv_payment_money;
        TextView tv_payment_note;
        TextView tv_payment_target;
        TextView tv_payment_ymd;
        TextView tv_payment_hms;
    }
}
