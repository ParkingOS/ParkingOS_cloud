package com.zhenlaidian.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.QueryAccountDetail;
import com.zhenlaidian.ui.park_account.ParkPaymentDetailActivity;
import com.zhenlaidian.ui.person_account.PaymentDetailActivity;
import com.zhenlaidian.util.DataUtils;

import java.util.ArrayList;

/**
 * 收支明细适配器;
 */
public class PaymentDetailAdapter extends BaseAdapter {

    private ArrayList<QueryAccountDetail> allDetail = null;
    private Context context;

    public PaymentDetailAdapter(Context context) {
        this.context = context;
    }

    public void addInfo(ArrayList<QueryAccountDetail> allDetail, PaymentDetailActivity activity, int count) {
        if (allDetail.size() == 20) {
            activity.setPageNumber();
        }
        if (this.allDetail == null) {
            this.allDetail = allDetail;
            activity.setAdapter();
        } else {
            this.allDetail.remove(null);
            this.allDetail.addAll(allDetail);
            this.notifyDataSetChanged();
        }
        if (this.allDetail.size() % 20 == 0 && this.allDetail.size() < count) {
            this.allDetail.add(null);
            this.notifyDataSetChanged();
        }
    }

    public void addInfo(ArrayList<QueryAccountDetail> allDetail, ParkPaymentDetailActivity activity, int count) {
        if (allDetail.size() == 20) {
            activity.setPageNumber();
        }
        if (this.allDetail == null) {
            this.allDetail = allDetail;
            activity.setAdapter();
        } else {
            this.allDetail.remove(null);
            this.allDetail.addAll(allDetail);
            this.notifyDataSetChanged();
        }
        if (this.allDetail.size() % 20 == 0 && this.allDetail.size() < count) {
            this.allDetail.add(null);
            this.notifyDataSetChanged();
        }
    }

    public QueryAccountDetail getInfo(int position) {
        if (position <= 0) {
            return new QueryAccountDetail();
        }
        return allDetail.get(position - 1);
    }

    public void clearInfo() {
        if (this.allDetail != null) {
            this.allDetail.clear();
            this.notifyDataSetChanged();
        }
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
            holder.tv_paymen_loadmore = (TextView) convertView.findViewById(R.id.tv_payment_item_loadmore);
            holder.ll_item = (LinearLayout) convertView.findViewById(R.id.ll_payment_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (allDetail.get(position) == null) {
            holder.ll_item.setVisibility(View.GONE);
            holder.tv_paymen_loadmore.setVisibility(View.VISIBLE);
        } else {
            holder.ll_item.setVisibility(View.VISIBLE);
            holder.tv_paymen_loadmore.setVisibility(View.GONE);

            if (allDetail.get(position).getMtype() != null && allDetail.get(position).getMoney() != null) {   /*Mtype= 0 或者 2 收入 ，1 提现*/
                if (allDetail.get(position).getMtype().equals("0") || allDetail.get(position).getMtype().equals("2")) {
                    holder.tv_payment_money.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                    holder.tv_payment_money.setText("+" + allDetail.get(position).getMoney());
                } else if (allDetail.get(position).getMtype().equals("1")) {
                    holder.tv_payment_money.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_red));
                    holder.tv_payment_money.setText("-" + allDetail.get(position).getMoney());
                }
            }
            if (allDetail.get(position).getNote() != null) {
                holder.tv_payment_note.setText(allDetail.get(position).getNote());
            }
            if (allDetail.get(position).getTarget() != null) {
                holder.tv_payment_target.setText(allDetail.get(position).getTarget());
            }
            if (allDetail.get(position).getCreate_time() != null) {
                String time = allDetail.get(position).getCreate_time();
                time = DataUtils.toTimestamp(time + "000");
                String[] sourceStrArray = time.split(" ");
                //年月日yyyy-MM.dd
                holder.tv_payment_ymd.setText(sourceStrArray[0]);
                //时分秒HH:mm:ss
                holder.tv_payment_hms.setText(sourceStrArray[1]);
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView tv_payment_money;
        TextView tv_payment_note;
        TextView tv_payment_target;
        TextView tv_payment_ymd;
        TextView tv_payment_hms;
        TextView tv_paymen_loadmore;
        LinearLayout ll_item;

    }
}
