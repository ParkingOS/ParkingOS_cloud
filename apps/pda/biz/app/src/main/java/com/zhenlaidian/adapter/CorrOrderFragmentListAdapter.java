package com.zhenlaidian.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.AllOrder;
import com.zhenlaidian.ui.CurrentOrderActivity;
import com.zhenlaidian.ui.CurrentOrderDetailsActivity;
import com.zhenlaidian.ui.fragment.CurrnetOrderListFragment;
import com.zhenlaidian.util.MyLog;

import java.util.ArrayList;

/**
 * 主页fragment当前订单适配器.
 * Created by zhangyunfei on 15/10/27.
 */
public class CorrOrderFragmentListAdapter extends BaseAdapter {

    private ArrayList<AllOrder> orders;
    private Context context;

    public CorrOrderFragmentListAdapter(Context context) {
        this.context = context;
    }

    // 添加从网络上获取的订单数据；
    public void addOrders(ArrayList<AllOrder> orders, int totalCount, CurrnetOrderListFragment fragment) {
        if (orders == null) {
            return;
        }
        if (orders.size() == 10) {
            fragment.setPageNumber();
        }
        if (this.orders == null) {
            this.orders = orders;
            fragment.setAdapter();
        } else {
            this.orders.remove(null);
            this.orders.addAll(orders);
        }
        if (this.orders.size() % 10 == 0 && this.orders.size() < totalCount) {
            this.orders.add(null);
        }
        this.notifyDataSetChanged();
    }


    public void removeOrders() {
        if (this.orders != null) {
            this.orders.clear();
            this.notifyDataSetChanged();
        }
    }

    // 订单条目的点击事件。
    public void onItemClick(int position, CurrnetOrderListFragment fragment) {
        if (orders.size() > position) {
            AllOrder allOrder = orders.get(position);
            if (allOrder == null) {
                fragment.getOrder();
            } else {
                Intent intent = new Intent(fragment.getActivity(), CurrentOrderDetailsActivity.class);
                String orderid = orders.get(position).getId();
                intent.putExtra("orderid", orderid);
                intent.putExtra("duration", orders.get(position).getDuration());
                MyLog.i("CurrentOrderAdapter", "点击条目的position是" + position + "点单号是" + orderid);
                fragment.getActivity().startActivity(intent);
            }
        }
    }

    // 订单条目的长按点击事件。
    public void onItemLongClick(int position, CurrentOrderActivity activity) {
        AllOrder allOrder = orders.get(position);
        if (allOrder == null) {
            activity.getOrder();
        } else {
            String carnumber = orders.get(position).getCarnumber();
            String time = orders.get(position).getBtime();
            String total = orders.get(position).getTotal();
            String orderid = orders.get(position).getId();

            MyLog.i("CurrentOrderAdapter", "点击条目的position是" + position + "车牌号是" + carnumber);
            if (carnumber != null && carnumber.equals("车牌号未知")) {
                activity.OrderNotNumberDialog(time, orderid);
            } else {
                activity.OrderHasNumberDialog(carnumber, total, orderid, position);
            }
        }
    }

    public AllOrder getAllOrders(int position) {
        AllOrder allOrder = orders.get(position - 1);
        return allOrder;
    }

    public AllOrder getAllOrder(int position){
        AllOrder allOrder = orders.get(position);
        return allOrder;
    }


    public void removeOrder(int position, CurrentOrderActivity activity) {
        orders.remove(position);
        this.notifyDataSetChanged();

    }


    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Object getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.current_order_item, null);
            holder = new ViewHolder();
            holder.tv_alreadTime = (TextView) convertView.findViewById(R.id.tv_current_item_alreadyTime);
            holder.tv_inTime = (TextView) convertView.findViewById(R.id.tv_current_item_inTime);
            holder.tv_money = (TextView) convertView.findViewById(R.id.tv_current_item_money);
            holder.tv_plateNumber = (TextView) convertView.findViewById(R.id.tv_current_item_plateNumber);
            holder.ll_whole = (LinearLayout) convertView.findViewById(R.id.ll_current_order_item);
            holder.tv_loadmore = (TextView) convertView.findViewById(R.id.tv_current_order_loadmore);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (orders.get(position) == null) {
            holder.ll_whole.setVisibility(View.GONE);
            holder.tv_loadmore.setVisibility(View.VISIBLE);
        } else {
            holder.ll_whole.setVisibility(View.VISIBLE);
            holder.tv_loadmore.setVisibility(View.GONE);
            Typeface face = Typeface.createFromAsset(context.getAssets(), "Font/Roboto/Roboto-Black.ttf");
            if (orders != null) {
                if (orders.get(position).getDuration() != null) {
                    holder.tv_alreadTime.setText(orders.get(position).getDuration());
                }
                if (orders.get(position).getBtime() != null) {
                    holder.tv_inTime.setText(orders.get(position).getBtime());
                }
                if (orders.get(position).getTotal() != null) {
                    holder.tv_money.setTypeface(face);
                    try {
                        double d = Double.parseDouble(orders.get(position).getTotal());
                        String result = String.format("%.2f", d);
                        holder.tv_money.setText(result);
                    } catch (Exception e) {
                        holder.tv_money.setText(orders.get(position).getTotal());
                    }
                }
                if (orders.get(position).getCarnumber() != null && !orders.get(position).getCarnumber().equals("null")) {
                    MyLog.w("CurrentOrderAdapter", orders.get(position).getCarnumber());
                    holder.tv_plateNumber.setText(orders.get(position).getCarnumber());
                } else {
                    holder.tv_plateNumber.setText("车牌号未知");
                }
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView tv_alreadTime;
        TextView tv_inTime;
        TextView tv_money;
        TextView tv_plateNumber;
        LinearLayout ll_whole;
        TextView tv_loadmore;
    }

}
