package com.tq.zld.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.OperatingStatement;

public class AccountDetailAdapter extends BaseAdapter {

    private ArrayList<OperatingStatement> infos;
    private static String[] payTypes = new String[]{"余额", "支付宝", "微信", "网银",
            "余额+支付宝", "余额+微信", "余额+网银"};

    public void addData(ArrayList<OperatingStatement> infos) {
        if (this.infos == null) {
            this.infos = new ArrayList<>();
        }
        this.infos.addAll(infos);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return infos == null ? 0 : infos.size();
    }

    @Override
    public Object getItem(int position) {
        return infos == null ? null : infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ViewHolder mHolder = null;
        OperatingStatement operatingStatement = infos.get(position);
        if (convertView != null) {
            view = convertView;
            mHolder = (ViewHolder) view.getTag();
        } else {
            mHolder = new ViewHolder();
            view = View.inflate(TCBApp.getAppContext(),
                    R.layout.listitem_account_detail, null);
            mHolder.tv_subject = (TextView) view
                    .findViewById(R.id.tv_operating_subject);
            mHolder.tv_money = (TextView) view
                    .findViewById(R.id.tv_operating_money);
            mHolder.tv_date = (TextView) view
                    .findViewById(R.id.tv_operating_date);
            mHolder.tv_paytype = (TextView) view
                    .findViewById(R.id.tv_operating_paytype);
            view.setTag(mHolder);
        }
        mHolder.tv_subject.setText(operatingStatement.remark);
        mHolder.tv_subject.setSelected(true);
        String amount = operatingStatement.amount;
        amount = "1".equals(operatingStatement.type) ? "-" + amount : "+"
                + amount;
        int amountColor = "1".equals(operatingStatement.type) ? Color.RED
                : Color.rgb(0x32, 0x97, 0x62);
        mHolder.tv_money.setText(amount);
        mHolder.tv_money.setTextColor(amountColor);
        mHolder.tv_date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.CHINA).format(new Date(Long
                .parseLong(operatingStatement.create_time) * 1000)));

        // 设置支付方式
        if (!TextUtils.isEmpty(operatingStatement.pay_name)) {
            mHolder.tv_paytype.setText(operatingStatement.pay_name);
        } else {
            int payType = Integer.parseInt(operatingStatement.pay_type);
            payType = payType < payTypes.length ? payType : 0;
            mHolder.tv_paytype.setText(payTypes[payType]);
        }
        return view;
    }

    static class ViewHolder {
        TextView tv_subject;
        TextView tv_money;
        TextView tv_date;
        TextView tv_paytype;
    }
}
