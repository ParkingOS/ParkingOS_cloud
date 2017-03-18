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
import com.zhenlaidian.util.MyLog;

import java.util.ArrayList;

/**
 * 当前订单列表的条目适配器
 */
public class CurrentOrderAdapter extends BaseAdapter {
    private ArrayList<AllOrder> orders;
    private Context context;

    public CurrentOrderAdapter(Context context) {
        this.context = context;
    }

    // 添加从网络上获取的订单数据；
    public void addOrders(ArrayList<AllOrder> orders, int totalCount, CurrentOrderActivity activity) {
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
            MyLog.i("CurrentOrderAdapter", "本地数据null。设置adapter");
        } else {
            this.orders.remove(null);
            this.orders.addAll(orders);
            MyLog.i("CurrentOrderAdapter", "有本地数据。添加数据");
        }
        MyLog.i("CurrentOrderAdapter", "本地订单的个数是" + this.orders.size() + "当前订单的长度是" + totalCount);
        if (this.orders.size() % 10 == 0 && this.orders.size() < totalCount) {
            MyLog.i("CurrentOrderAdapter", "添加加载更多按钮");
            this.orders.add(null);
        }
        this.notifyDataSetChanged();
        MyLog.i("CurrentOrderAdapter", "本地订单详情" + this.orders.toString());
        // if (this.orders != null && this.orders.size() > 0) {
        // setActivityView(activity);
        // }else {
        // activity.setView("0","0");
        // }
    }

    // 改变订单界面的总数量和总金额；
    public void setActivityView(CurrentOrderActivity activity) {
        String number;
        if (this.orders.contains(null)) {
            number = String.valueOf(this.orders.size() - 1);
        } else {
            number = String.valueOf(this.orders.size());
        }
        double money = 0;
        for (int i = 0; i < this.orders.size(); i++) {
            if (this.orders.get(i) != null) {
                money += Double.parseDouble(this.orders.get(i).getTotal());
            }
        }
        MyLog.w("CurrentOrderAdapter", "当前订单的总数量：" + number + "  当前订单的总金额：" + money);
        activity.setView(number, String.valueOf(money));
    }

    public void removeOrders() {
        if (this.orders != null) {
            this.orders.clear();
            this.notifyDataSetChanged();
        }
    }

    // 订单条目的点击事件。
    public void onItemClick(int position, CurrentOrderActivity activity) {
        if (orders.size() > position) {
            AllOrder allOrder = orders.get(position);
            if (allOrder == null) {
                activity.getOrder();
            } else {
                Intent intent = new Intent(activity, CurrentOrderDetailsActivity.class);
                String orderid = orders.get(position).getId();
                intent.putExtra("orderid", orderid);
                intent.putExtra("duration", orders.get(position).getDuration());
                MyLog.i("CurrentOrderAdapter", "点击条目的position是" + position + "点单号是" + orderid);
                activity.startActivity(intent);
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

    ;

    public void removeOrder(int position, CurrentOrderActivity activity) {
        orders.remove(position);
        this.notifyDataSetChanged();
        // if (this.orders != null && this.orders.size() > 0) {
        // setActivityView(activity);
        // }else {
        // activity.setView("0","0");
        // }

    }

    public void getOrdes(CurrentOrderActivity activity) {
        if (this.orders == null) {
            activity.setNullView();
            activity.setView("0", "0");
        }
    }

    public AllOrder getAllOrders(int position) {
        AllOrder allOrder = orders.get(position - 1);
        return allOrder;
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
            // holder.ll_carnumber = (LinearLayout)
            // convertView.findViewById(R.id.ll_current_order_carnumber);
            // holder.ll_detail = (LinearLayout)
            // convertView.findViewById(R.id.ll_current_order_detial);
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
            MyLog.i("CurrentOrderAdapter", "getView中添加loadmore按钮");
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
        // LinearLayout ll_carnumber;
        // LinearLayout ll_detail;
        TextView tv_alreadTime;
        TextView tv_inTime;
        TextView tv_money;
        TextView tv_plateNumber;
        LinearLayout ll_whole;
        TextView tv_loadmore;
    }

}
