package com.tq.zld.adapter;

import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Coupon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TicketAdapter extends BaseAdapter {

    private ArrayList<Coupon> mCouponList;
    private int mSelectedPosition = -1;

    private boolean isShowLimit = true;

    public void setData(int page, ArrayList<Coupon> coupons) {

        // 将选券界面的所有券设置为“当前”状态
        if (coupons != null && coupons.size() > 0) {
            for (Coupon coupon : coupons) {
                if (!TextUtils.isEmpty(coupon.iscanuse)) {
                    coupon.state = "0";
                    coupon.exp = "1";
                }
                if (!TextUtils.isEmpty(coupon.cname)
                        && !coupon.cname.contains("车场")) {
                    coupon.cname += "停车场";
                }

                coupon.desc = coupon.desc.replace(",", "\n");
            }
        }

        if (page == 1) {
            this.mCouponList = coupons;
        } else {
            this.mCouponList.addAll(coupons);
        }
        notifyDataSetChanged();
    }

    public void setShowLimit(boolean b){
        this.isShowLimit = b;
    }

    @Override
    public int getCount() {
        return mCouponList == null ? 0 : mCouponList.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return "1".equals(((Coupon) getItem(position)).iscanuse);
    }

    @Override
    public Coupon getItem(int position) {
        return mCouponList == null ? null : mCouponList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Coupon coupon = (Coupon) getItem(position);
        if (convertView == null) {
            convertView = View.inflate(TCBApp.getAppContext(),
                    R.layout.listitem_ticket, null);
            holder = new ViewHolder();
            holder.mMainInfoView = convertView
                    .findViewById(R.id.rl_listitem_ticket_maininfo);
            holder.mMoneyTextView = (TextView) convertView
                    .findViewById(R.id.tv_listitem_ticket_money);
            holder.mTicketTypeTextView = (TextView) convertView
                    .findViewById(R.id.tv_listitem_ticket_type);
            holder.mParkTextView = (TextView) convertView
                    .findViewById(R.id.tv_listitem_ticket_park);
            holder.mValidityTextView = (TextView) convertView
                    .findViewById(R.id.tv_listitem_ticket_validity);
            holder.mRuleTextView = (TextView) convertView
                    .findViewById(R.id.tv_listitem_ticket_rule);
            holder.mTicketStateTextView = (TextView) convertView
                    .findViewById(R.id.tv_listitem_ticket_state);
            holder.mTicketLimit = (TextView) convertView.findViewById(R.id.tv_listitem_ticket_limit);
            holder.mBuyedView = (ImageView) convertView.findViewById(R.id.iv_listitem_ticket_isbuy);
            Calendar c = Calendar.getInstance();
            c.clear(Calendar.HOUR);//修复不能充值到零点BUG，java官方给出方案。两个都调用就能重置。
            c.clear(Calendar.HOUR_OF_DAY);
            c.clear(Calendar.MINUTE);
            c.clear(Calendar.SECOND);
            holder.mCurrentDay = c.getTimeInMillis();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 设置选中状态背景
        if (position == mSelectedPosition) {
            // convertView.setSelected(true);
//            holder.mMainInfoView.setBackgroundResource(R.drawable.bg_listitem_ticket_selected);
            holder.mTicketLimit.setVisibility(View.VISIBLE);
            if (isShowLimit) {
                holder.mTicketLimit.setText(String.format("本次停车可抵扣%.1f元",coupon.limit));
                holder.mTicketLimit.setBackgroundResource(R.drawable.bg_ticket_select_mask);
            } else {
                holder.mTicketLimit.setBackgroundResource(R.drawable.bg_listitem_ticket_selected);
                holder.mTicketLimit.setText("");
            }
        } else {
            // convertView.setSelected(false);
//            holder.mMainInfoView.setBackgroundResource(R.drawable.selector_listitem_ticket_background);
            holder.mTicketLimit.setVisibility(View.GONE);
        }

        // 设置金额
        holder.mMoneyTextView.setText(String.valueOf(coupon.money));

        // 设置有效期
        holder.mLimitDay = Long.parseLong(coupon.limitday) * 1000;
        holder.mValidityTextView.setText("有效期至："
                + holder.mFormatter.format(new Date(holder.mLimitDay)));

        // 设置使用规则
        holder.mRuleTextView.setText(Html.fromHtml(coupon.desc));

        // 是否专用券
        switch (coupon.type) {
            case "0":
                holder.mParkTextView.setVisibility(View.GONE);
                holder.mTicketTypeTextView.setText("普通停车券");
                holder.mTicketTypeTextView.setTextColor(Color.BLACK);
                break;
            case "1":
                holder.mParkTextView.setText(String.format("限%s使用", coupon.cname));
                holder.mParkTextView.setVisibility(View.VISIBLE);
                holder.mTicketTypeTextView.setText("专用停车券");
                holder.mTicketTypeTextView.setTextColor(holder.green);
                break;
            default:
                break;
        }

        // “当前”或“历史”
        if ("0".equals(coupon.state) && "1".equals(coupon.exp)) {

            // 未使用，未过期

            if ("0".equals(coupon.iscanuse)) {

                // 选券界面不可选的券
                convertView.setActivated(false);
                holder.mTicketTypeTextView.setTextColor(holder.gray);
            } else {
                // 其他：当前或者选券界面可选券
                convertView.setActivated(true);
            }
            long limitDay = (holder.mLimitDay - holder.mCurrentDay) / (24 * 60 * 60 * 1000);
            if (limitDay == 0) {
                holder.mTicketStateTextView.setText("今天到期");
            } else {
                holder.mTicketStateTextView.setText(String.format("剩余%d天到期",
                        limitDay));
            }
            holder.mTicketStateTextView.setTextColor(holder.green);

        } else {
            // 已使用或者已过期
            holder.mTicketTypeTextView.setTextColor(holder.gray);
            convertView.setActivated(false);
            if ("1".equals(coupon.state)) {
                // 已使用
                holder.mTicketStateTextView.setText("已使用");
                holder.mTicketStateTextView.setTextColor(holder.green);
            } else if ("0".equals(coupon.exp)) {
                // 已过期
                holder.mTicketStateTextView.setText("已过期");
                holder.mTicketStateTextView.setTextColor(holder.red);
            }
        }

        // 设置该券是否购买
        if (1 == coupon.isbuy) {
            holder.mBuyedView.setVisibility(View.VISIBLE);
        } else {
            holder.mBuyedView.setVisibility(View.GONE);
        }

        return convertView;
    }

    /**
     * 设置选中的条目，取消选择，传-1
     *
     * @param position 选中条目位置，不选传-1
     */
    public void setSelection(int position) {
        this.mSelectedPosition = position;
        // if (getCount() > 0) {
        notifyDataSetChanged();
        // }
    }

    /**
     * 返回选中的条目位置
     *
     * @return
     */
    public int getSelection() {
        return mSelectedPosition;
    }

    /**
     * 获取选中停车券，未选中任何券，返回id为"-1"的空券，不返null
     *
     * @return
     */
    public Coupon getSelectedItem() {
        Coupon c = (Coupon) getItem(0);
        if (mSelectedPosition == -1) {
            c.id = "-1";
        } else {
            c = (Coupon) getItem(mSelectedPosition);
        }
        if (c == null) {
            c = new Coupon();
            c.id = "-1";
        }
        return c;
    }

    static class ViewHolder {
        View mMainInfoView;
        TextView mMoneyTextView;// 金额
        TextView mParkTextView;// 车场名称
        TextView mValidityTextView;// 有效期至 2015-03-02
        TextView mRuleTextView;// 使用规则
        TextView mTicketTypeTextView;// 停车券类型：普通，专用，微信打折
        TextView mTicketStateTextView;// 停车券状态：已使用，已过期
        TextView mTicketLimit;//抵扣的limit
        ImageView mBuyedView;// 是否购买，1是，其他否
        long mLimitDay;
        long mCurrentDay;
        SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd",
                Locale.CHINA);
        int red = TCBApp.getAppContext().getResources()
                .getColor(R.color.text_red);
        int green = TCBApp.getAppContext().getResources()
                .getColor(R.color.text_green);
        int gray = TCBApp.getAppContext().getResources()
                .getColor(R.color.text_gray);
    }
}
