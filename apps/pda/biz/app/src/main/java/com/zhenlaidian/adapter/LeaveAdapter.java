package com.zhenlaidian.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.LeaveOrder;
import com.zhenlaidian.engine.ShowCashDealDialog;
import com.zhenlaidian.engine.ShowIbeaconCashDialog;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.MyLog;

import java.util.ArrayList;

//离场订单条目适配器
public class LeaveAdapter extends BaseAdapter {

    ArrayList<LeaveOrder> orders;
    private Context context;

    public LeaveAdapter(Context context) {
        this.context = context;
        this.orders = new ArrayList<LeaveOrder>();
    }

    public void addOrder(LeaveOrder order) {
        if (this.orders.size() == 0) {
            this.orders.add(order);
            if (context != null) {
                LeaveActivity activity = (LeaveActivity) context;
                activity.setAdapter();
            }
        } else {
            this.orders.add(order);
            this.notifyDataSetChanged();
        }
    }

    public int getorders() {
        return orders == null ? 0 : orders.size();
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.leave_item, null);
            holder = new ViewHolder();
            holder.mposition = position;
            holder.tv_carnumber = (TextView) convertView.findViewById(R.id.tv_leaveitem_number);
            holder.tv_total = (TextView) convertView.findViewById(R.id.tv_leaveitem_money);
            holder.tv_duration = (TextView) convertView.findViewById(R.id.tv_leaveitem_time);
            holder.tv_state = (TextView) convertView.findViewById(R.id.tv_leaveitem_state);
            holder.tv_pay = (TextView) convertView.findViewById(R.id.tv_leaveitem_pay);
            holder.tv_change_add = (TextView) convertView.findViewById(R.id.tv_leaveitem_change_money_add);
            holder.tv_change_subtract = (TextView) convertView.findViewById(R.id.tv_leaveitem_change_money_subtract);
            holder.et_change_money_total = (EditText) convertView.findViewById(R.id.et_leaveitem_change_money_money);
            holder.tv_nofree_accomplish = (TextView) convertView.findViewById(R.id.tv_leaveItem_nofree_accomplish);// 确定放行
            holder.ll_allorder = (LinearLayout) convertView.findViewById(R.id.ll_leave_order);// 隐藏非博客订单
            holder.ll_change_money = (LinearLayout) convertView.findViewById(R.id.ll_leaveitem_change_money);// 调价订单控件
            holder.ll_final_money = (LinearLayout) convertView.findViewById(R.id.ll_leaveitem_final_money);// 正常订单控件
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            holder.mposition = position;
        }
        // 引用字体
        Typeface face = Typeface.createFromAsset(context.getAssets(),
                "Font/Roboto/Roboto-Black.ttf");
        holder.tv_carnumber.setTypeface(face);
        if (orders.get(position).getCarnumber() != null) {
            holder.tv_carnumber.setText(orders.get(position).getCarnumber());
        }
        holder.tv_total.setTypeface(face);
        if (orders.get(position).getTotal() != null && orders.get(position).getState() != null) {
            if (Integer.parseInt(orders.get(position).getState()) != 1) {
                holder.tv_total.setText(orders.get(position).getTotal());
            } else {
                holder.et_change_money_total.setText(orders.get(position).getTotal());
            }
        }
        holder.tv_duration.setTypeface(face);
        if (orders.get(position).getBtime() != null && orders.get(position).getEtime() != null) {
            holder.tv_duration.setText(orders.get(position).getBtime() + "-"
                    + orders.get(position).getEtime());
        }

        // *****************《state订单支付状态》****-------->>>>>>>>// 0未支付.1.待支付 2.已支付，-1结算失败；
        if (orders.get(position).getState() != null && Integer.parseInt(orders.get(position).getState()) == 1) { // 1 代表ibeacon可调价订单；
            holder.ll_allorder.setVisibility(View.VISIBLE);
            holder.ll_change_money.setVisibility(View.VISIBLE);
            holder.ll_final_money.setVisibility(View.GONE);
            holder.tv_state.setVisibility(View.GONE);
            holder.tv_pay.setVisibility(View.GONE);
            holder.tv_nofree_accomplish.setText("确认放行");
            holder.et_change_money_total.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub
                    MyLog.i("leaveadapter", "orders.size()" + orders.size() + "点击的position=" + position + "输入完成后" + s.toString() + "Mposition=" + holder.mposition);
                    orders.get(holder.mposition).setTotal(s.toString());
                }
            });
            holder.tv_change_add.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO 点击对金额做 +1 处理；
                    try {
                        Double cmoney = Double.parseDouble(holder.et_change_money_total.getText().toString());
                        holder.et_change_money_total.setText((cmoney + 1.0) + "");
                        orders.get(holder.mposition).setTotal((cmoney + 1.0) + "");
                    } catch (Exception e) {
                        MyLog.w("LeaveAdapter", "点击对金额做加上1处理——类型转换异常");
                    }
                }
            });
            holder.tv_change_subtract.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO 点击对金额做 -1 处理；
                    try {
                        Double cmoney = Double.parseDouble(holder.et_change_money_total.getText().toString());
                        if (cmoney >= 1.0) {
                            holder.et_change_money_total.setText((cmoney - 1.0) + "");
                            orders.get(holder.mposition).setTotal((cmoney - 1.0) + "");
                        }
                    } catch (Exception e) {
                        MyLog.w("LeaveAdapter", "点击对金额做减去1处理——类型转换异常");
                    }
                }
            });
            holder.tv_nofree_accomplish.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO 点击去结算Ibeacon订单；
                    orders.get(holder.mposition).setTotal(holder.et_change_money_total.getText().toString() == null ? "0.0" : holder.et_change_money_total.getText().toString());
                    ShowIbeaconCashDialog ibeaconDialog = new ShowIbeaconCashDialog(context, orders, LeaveActivity.adapter, holder.mposition);
                    ibeaconDialog.showIbeaconCashDialog();
                    try {
                        View view = ((Activity) context).getWindow().peekDecorView();
                        if (view != null) {
                            InputMethodManager inputmanger = (InputMethodManager) ((Activity) context).getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputmanger.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                    } catch (Exception e) {
                        MyLog.w("leaveAdapter", "关闭输入法异常");
                    }
                }
            });
        } else if (orders.get(position).getState() != null && Integer.parseInt(orders.get(position).getState()) == 2) {
            holder.ll_allorder.setVisibility(View.VISIBLE);
            holder.ll_change_money.setVisibility(View.GONE);
            holder.ll_final_money.setVisibility(View.VISIBLE);
            // 2 代表已支付
            holder.tv_state.setText("已支付");
            holder.tv_pay.setVisibility(View.VISIBLE);
            holder.tv_state.setTextColor(context.getResources().getColor(
                    R.color.tv_leaveItem_state_green));
            holder.tv_nofree_accomplish.setText("确认放行");
            // 无优惠状态下的accomplish点击事件;
            holder.tv_nofree_accomplish.setTag(position);
            holder.tv_nofree_accomplish.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = (Integer) v.getTag();
                    MyLog.i("LeaveAdapter", "我的position是" + position);
                    LeaveOrder leaveOrder = orders.get(position);
                    MyLog.i("LeaveAdapter", "orders中position位置的order" + leaveOrder.toString());
                    orders.remove(position);
                    LeaveAdapter.this.notifyDataSetChanged();
                    // 当没用订单的时候可以用二维码图代替此列表订单
                    if (orders.size() < 1) {
                        LeaveActivity activity = (LeaveActivity) context;
                        activity.displayQrcodeView();
                    }
                }
            });
        } else if (orders.get(position).getState() != null && Integer.parseInt(orders.get(position).getState()) == -1) {
            // -1 代表支付失败
            holder.ll_allorder.setVisibility(View.VISIBLE);
            holder.ll_change_money.setVisibility(View.GONE);
            holder.ll_final_money.setVisibility(View.VISIBLE);
            holder.tv_state.setText("支付失败");
            holder.tv_state.setTextColor(context.getResources().getColor(
                    R.color.tv_leaveItem_state_red));
            holder.tv_pay.setVisibility(View.GONE);
            holder.tv_nofree_accomplish.setText("收现金");
            holder.tv_nofree_accomplish.setTag(position);
            holder.tv_nofree_accomplish.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = (Integer) v.getTag();
                    LeaveOrder leaveOrder = orders.get(position);
                    ShowCashDealDialog cashDealDialog = new ShowCashDealDialog(v.getContext(),
                            orders, LeaveActivity.adapter, position);
                    cashDealDialog.showCashDealDialog();
                }
            });
        } else if (orders.get(position).getState() != null && Integer.parseInt(orders.get(position).getState()) == -2) {
            // -2 代表LBE通道照牌支付失败
            holder.ll_allorder.setVisibility(View.VISIBLE);
            holder.ll_change_money.setVisibility(View.GONE);
            holder.ll_final_money.setVisibility(View.VISIBLE);
            holder.tv_state.setText("支付失败");
            holder.tv_state.setTextColor(context.getResources().getColor(
                    R.color.tv_leaveItem_state_red));
            holder.tv_pay.setVisibility(View.GONE);
            holder.tv_nofree_accomplish.setText("收现金");
            holder.tv_duration.setText("车主余额不足!");
            holder.tv_nofree_accomplish.setTag(position);
            holder.tv_nofree_accomplish.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = (Integer) v.getTag();
                    orders.remove(position);
                    LeaveAdapter.this.notifyDataSetChanged();
                    // 当没用订单的时候可以用二维码图代替此列表订单
                    if (orders.size() < 1) {
                        LeaveActivity activity = (LeaveActivity) context;
                        activity.displayQrcodeView();
                    }
                }
            });
        } else {
            MyLog.i("LeaveAdapter", "订单状态为-->>" + orders.get(position).getState());
        }

        return convertView;
    }

    private static class ViewHolder {
        private int mposition;
        private TextView tv_nofree_accomplish;
        private TextView tv_carnumber;
        private TextView tv_total;
        private TextView tv_duration;
        private TextView tv_state;
        private TextView tv_pay;
        private LinearLayout ll_allorder;// 隐藏接单
        private LinearLayout ll_change_money;// ibeacon可调价订单控件；
        private LinearLayout ll_final_money;// 正常订单控件；
        private EditText et_change_money_total; // 调价金额
        private TextView tv_change_add; // 调价加上 +
        private TextView tv_change_subtract;// 调价减去 -

    }

}
