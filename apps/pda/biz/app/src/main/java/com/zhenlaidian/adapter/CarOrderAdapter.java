package com.zhenlaidian.adapter;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.AllOrder;
import com.zhenlaidian.photo.CarOrderActivity;
import com.zhenlaidian.util.MyLog;

import java.io.File;
import java.util.ArrayList;

/**
 * 离场结算订单适配器;
 */
public class CarOrderAdapter extends BaseAdapter {

    private ArrayList<AllOrder> orders;
    private Context context;
    private boolean isresult;
    private CarOrderActivity activity;

    public CarOrderAdapter(Context context) {
        this.context = context;
        this.activity = (CarOrderActivity) context;
    }


    public void addOrders(ArrayList<AllOrder> orders, int totalCount, CarOrderActivity activity, boolean isresult) {
        this.isresult = isresult;

        if (orders == null) {
            return;
        }
        if (orders.size() == 20) {
            activity.setPageNumber();
            System.out.println("CarOrderAdapter" + "返回的分页数据等于10条。pagenumber++");
        }
        if (this.orders == null) {
            this.orders = orders;
            activity.setAdapter();
        } else {
            if (isresult) {
                this.orders.clear();
                this.orders.addAll(orders);
            } else {
                this.orders.remove(null);
                this.orders.addAll(orders);
            }
        }
        MyLog.i("CarOrderAdapter", "本地订单的个数是" + this.orders.size() + "当前订单的长度是" + totalCount);
        if (this.orders.size() % 20 == 0 && this.orders.size() < totalCount) {
            MyLog.i("CurrentOrderAdapter", "添加加载更多按钮");
            this.orders.add(null);
        }
        MyLog.i("CarOrderAdapter", "本地订单详情" + this.orders.toString());
        this.notifyDataSetChanged();
    }

    //订单条目的点击事件。
    public void onItemClick(int position, CarOrderActivity activity) {
        AllOrder allOrder = orders.get(position);
        if (allOrder == null) {
            if (isresult) {
                activity.queryOrder();
            } else {
                activity.getCarOrder();
            }
        }
//			else {
//			if (orders.get(position).getId() != null) {
//				activity.cashOrder(orders.get(position));
//			}
//		}
    }

    public void deleteOrder(AllOrder order) {
        this.orders.remove(order);
        this.notifyDataSetChanged();
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_car_order, null);
            holder = new ViewHolder();
            holder.rl_order = (RelativeLayout) convertView.findViewById(R.id.car_order_item_order);
            holder.tv_cash = (TextView) convertView.findViewById(R.id.car_order_item_cash);
            holder.tv_number = (TextView) convertView.findViewById(R.id.car_order_item_number);
            holder.tv_orderid = (TextView) convertView.findViewById(R.id.car_order_item_orderid);
            holder.tv_intime = (TextView) convertView.findViewById(R.id.car_order_item_intime);
            holder.tv_loadmore = (TextView) convertView.findViewById(R.id.car_order_item_loadmore);
            holder.tv_isvip = (TextView) convertView.findViewById(R.id.car_order_item_isvip);
            holder.tv_hasimg = (TextView) convertView.findViewById(R.id.car_order_item_hasimg);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (orders.get(position) == null) {
            holder.rl_order.setVisibility(View.GONE);
            holder.tv_loadmore.setVisibility(View.VISIBLE);
        } else {
            holder.rl_order.setVisibility(View.VISIBLE);
            holder.tv_loadmore.setVisibility(View.GONE);
            holder.tv_orderid.setText(orders.get(position).getId());
            if (TextUtils.isEmpty(orders.get(position).getCarnumber())){
                holder.tv_number.setText("车牌号未知");
            }else{
                holder.tv_number.setText(orders.get(position).getCarnumber());
            }
            holder.tv_intime.setText(orders.get(position).getBtime());
            if (orders.get(position).getUin() != null && !"-1".equals(orders.get(position).getUin())) {
                holder.tv_isvip.setVisibility(View.VISIBLE);
            } else {
                holder.tv_isvip.setVisibility(View.INVISIBLE);
            }
            String SDState = Environment.getExternalStorageState();
            if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(Environment.getExternalStorageDirectory() +
                        "/TingCheBao", orders.get(position).getId() + ".jpeg");
                if (file.exists()) {
                    holder.tv_hasimg.setVisibility(View.VISIBLE);
                } else {
                    holder.tv_hasimg.setVisibility(View.INVISIBLE);
                }
            }
            holder.tv_cash.setTag(position);
            holder.tv_cash.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // 点击条目内的结算按钮；
                    if (TextUtils.isEmpty(orders.get((Integer) v.getTag()).getTotal())) {
                        activity.cashOrder(orders.get((Integer) v.getTag()));
                    } else {
                        activity.cashIbeaconOrder(orders.get((Integer) v.getTag()));
                    }
                }
            });
            holder.tv_hasimg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //点击去查看大图;
                    File file = new File(Environment.getExternalStorageDirectory() +
                            "/TingCheBao", orders.get(position).getId() + ".jpeg");
                    activity.showMaxImgDialog(file);
                }
            });

        }
        return convertView;
    }

    private static class ViewHolder {
        TextView tv_number;
        TextView tv_orderid;
        TextView tv_intime;
        TextView tv_isvip;
        TextView tv_hasimg;
        TextView tv_cash;
        TextView tv_loadmore;
        RelativeLayout rl_order;
    }
}
