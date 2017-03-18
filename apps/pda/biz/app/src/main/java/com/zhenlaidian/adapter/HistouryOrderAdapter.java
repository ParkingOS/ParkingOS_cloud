package com.zhenlaidian.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.AllOrder;
import com.zhenlaidian.ui.HistoryOrderActivity;
import com.zhenlaidian.ui.HistoryOrderDetailsActivity;
import com.zhenlaidian.ui.LostOrderRecordDetailsActivity;
import com.zhenlaidian.util.MyLog;

import java.util.ArrayList;

/**
 * 历史订单适配器;
 */
public class HistouryOrderAdapter extends BaseAdapter {

    private ArrayList<AllOrder> orders;
    private Context context;

    public HistouryOrderAdapter(Context context) {
        this.context = context;
    }

    // 添加从网络上获取的订单数据；
    public void addOrders(ArrayList<AllOrder> orders, int totalCount, HistoryOrderActivity activity) {
        if (orders == null) {
            return;
        }
        if (orders.size() == 10) {
            activity.setPageNumber();
            System.out.println("CurrentOrderAdapter" + "返回的分页数据等于10条。pagenumber++");
        }
        if (this.orders == null) {
            this.orders = orders;
            activity.setAdapter();
            MyLog.w("HistoryOrderAdapter", "当前orders为null---设置适配器");
        } else {
            this.orders.remove(null);
            this.orders.addAll(orders);
        }
        MyLog.w("HistouryOrderAdapter--", "当前显示的历史订单总数：" + this.orders.size() + "云端总数：" + totalCount);
        if (this.orders.size() % 10 == 0 && this.orders.size() < totalCount) {
            this.orders.add(null);
        }

        this.notifyDataSetChanged();
    }

    // 订单条目的点击事件。
    public void onItemClick(int position, HistoryOrderActivity historyOrderActivity, String date, String ptype) {
        if (orders.size() > position) {
            AllOrder allOrder = orders.get(position);
            if (allOrder == null) {
                historyOrderActivity.getHistoryOrders(date, ptype);
            } else {
                if (orders.get(position).getState().equals("2")) {//去逃单详情界面
                    Intent intent = new Intent(historyOrderActivity, LostOrderRecordDetailsActivity.class);
                    String orderid = orders.get(position).getId();
                    intent.putExtra("orderid", orderid);
                    historyOrderActivity.startActivity(intent);
                } else {
                    Intent intent = new Intent(historyOrderActivity, HistoryOrderDetailsActivity.class);
                    String orderid = orders.get(position).getId();
                    intent.putExtra("orderid", orderid);
                    intent.putExtra("ismonthuser", orders.get(position).getIsmonthuser());
                    intent.putExtra("iscard", orders.get(position).getIs_card());
                    MyLog.i("HistoryOrderDetailsActivity", "点击条目的position是" + position + "点单号是" + orderid);
                    historyOrderActivity.startActivity(intent);
                }
            }
        }

    }

    public void removeOrders() {
        if (this.orders != null) {
            this.orders.clear();
            this.notifyDataSetChanged();
        }

    }

    public void getOrdes(HistoryOrderActivity activity) {
        if (this.orders == null) {
            this.notifyDataSetChanged();
            activity.setNullView();
        }
    }

    public void clearOrder(HistoryOrderActivity activity) {
        if (this.orders != null && this.orders.size() > 0) {
            this.orders.clear();
            this.notifyDataSetChanged();
            activity.setView("0", "0");
        }
    }

    public AllOrder getAllOrders(int position) {
        AllOrder allOrder = orders.get(position - 1);
        return allOrder;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return orders.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.history_order_item, null);
            holder = new ViewHolder();
            holder.tv_money = (TextView) convertView.findViewById(R.id.tv_history_item_money);
            holder.tv_parkTime = (TextView) convertView.findViewById(R.id.tv_history_item_parkTime);
            holder.tv_plateNumber = (TextView) convertView.findViewById(R.id.tv_history_item_plateNumber);
            holder.tv_duration = (TextView) convertView.findViewById(R.id.tv_history_item_duration);
            holder.tv_lostorder = (TextView) convertView.findViewById(R.id.tv_history_order_loset_order);
            holder.ll_history_whole = (LinearLayout) convertView.findViewById(R.id.ll_history_order_item);
            holder.tv_history_loadmore = (TextView) convertView.findViewById(R.id.tv_history_order_loadmore);
            holder.monthnumber = ((ImageView) convertView.findViewById(R.id.monthnumber));
            holder.bondcard = (ImageView) convertView.findViewById(R.id.bondcard);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
//		MyLog.i("HistoryOrderAapter", "orders.size()=:"+orders.size()+"  position = :"+position);
        if (orders.size() > position && orders.get(position) == null) {
            holder.tv_lostorder.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            holder.ll_history_whole.setVisibility(View.GONE);
            holder.tv_history_loadmore.setVisibility(View.VISIBLE);
        } else if (orders.size() == 0) {
            return convertView;
        } else {
            if (orders.size() < position) {
                return convertView;
            }
            if (!TextUtils.isEmpty(orders.get(position).getIsmonthuser())) {
                if (orders.get(position).getIsmonthuser().equals("1")) {
                    holder.monthnumber.setVisibility(View.VISIBLE);
                } else {
                    holder.monthnumber.setVisibility(View.GONE);
                }
            } else {
                holder.monthnumber.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(orders.get(position).getIs_card()) && orders.get(position).getIs_card().equals("1")) {
                holder.bondcard.setVisibility(View.VISIBLE);
            } else {
                holder.bondcard.setVisibility(View.GONE);
            }

            holder.ll_history_whole.setVisibility(View.VISIBLE);
            holder.tv_history_loadmore.setVisibility(View.GONE);

            if (orders.get(position).getTotal() != null) {
                holder.tv_money.setText(orders.get(position).getTotal());
            }
            if (orders.get(position).getBtime() != null) {
                holder.tv_parkTime.setText(orders.get(position).getBtime());
            }
            if (orders.get(position).getCarnumber() != null && !orders.get(position).getCarnumber().equals("null")) {
                holder.tv_plateNumber.setText(orders.get(position).getCarnumber());
            } else {
                holder.tv_plateNumber.setText("车牌号未知");
            }
            if (orders.get(position).getDuration() != null) {
                holder.tv_duration.setText(orders.get(position).getDuration());
            }
            if (orders.get(position).getState() != null && orders.get(position).getState().equals("2")) {
                holder.tv_lostorder.setBackgroundResource(R.drawable.lost_stamp);
//				MyLog.i("HistouryOrderAdapter", "发现逃单车牌："+orders.get(position).getCarnumber());
            } else {
                if (orders.get(position).getPtype() != null && orders.get(position).getPtype().equals("2")) {
                    holder.tv_lostorder.setBackgroundResource(R.drawable.mobile_pay);
                } else if (orders.get(position).getPtype() != null && orders.get(position).getPtype().equals("3")) {
                    holder.tv_lostorder.setBackgroundResource(R.drawable.month_pay);
                } else {
                    holder.tv_lostorder.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
                }
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView tv_money;
        TextView tv_parkTime;
        TextView tv_plateNumber;
        TextView tv_duration;
        TextView tv_lostorder;
        LinearLayout ll_history_whole;
        TextView tv_history_loadmore;
        ImageView monthnumber;
        ImageView bondcard;
    }
}
