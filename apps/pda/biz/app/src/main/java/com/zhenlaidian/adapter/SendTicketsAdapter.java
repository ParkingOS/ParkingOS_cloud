/**
 *
 */
package com.zhenlaidian.adapter;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.zhenlaidian.R;
import com.zhenlaidian.bean.SelectUserInfo;
import com.zhenlaidian.bean.SelectUserInfo.SelectUser;
import com.zhenlaidian.ui.score.SendTicketsActicity;
import com.zhenlaidian.util.MyLog;

import java.util.ArrayList;

/**
 * 选择车主发送停车券;
 *
 * @author Administrator 2015年7月17日
 */
public class SendTicketsAdapter extends BaseAdapter {
    private ArrayList<SelectUser> infos;
    private SendTicketsActicity activity;

    public SendTicketsAdapter(SendTicketsActicity activity) {
        this.activity = activity;
    }

    // 添加从网络上获取的订单数据；
    public void addOrders(ArrayList<SelectUserInfo.SelectUser> infos, Boolean clear) {
        if (infos == null) {
            return;
        }
        if (clear) {
            this.infos.clear();
        }
        for (int i = 0; i < infos.size(); i++) {
            infos.get(i).setSelect(false);
        }
        if (infos.size() == 20) {
            activity.setPageNumber();
            System.out.println("SendTicketsAdapter" + "返回的分页数据等于20条。pagenumber++");
        }
        if (this.infos == null) {
            this.infos = infos;
            activity.setAdapter();
            MyLog.i("SendTicketsAdapter", "当前SelectUser为null---设置适配器");
        } else {
            this.infos.addAll(infos);
            this.notifyDataSetChanged();
        }
    }

    /**
     * 全选列表
     */
    public void selectAll() {
        if (this.infos != null && this.infos.size() > 0) {
            for (int i = 0; i < infos.size(); i++) {
                infos.get(i).setSelect(true);
            }
        }
    }

    /**
     * 取消全选列表
     */
    public void selectNull() {
        if (this.infos != null && this.infos.size() > 0) {
            for (int i = 0; i < infos.size(); i++) {
                infos.get(i).setSelect(false);
            }
        }
    }

    public String getUins() {
        String uins = "";
        if (this.infos != null && this.infos.size() > 0) {
            for (int i = 0; i < infos.size(); i++) {
                if (infos.get(i).getSelect()) {
                    if (uins.equals("")) {
                        uins = uins + infos.get(i).getUin();
                    } else {
                        uins = uins + "," + infos.get(i).getUin();
                    }
                }
            }
        }
        return uins;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return infos == null ? 0 : infos.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @SuppressLint({"NewApi", "ViewHolder"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(activity, R.layout.item_send_tickets_user, null);
        TextView tv_carnumber = (TextView) view.findViewById(R.id.tv_item_send_ticket_carnumber);
        TextView tv_detials = (TextView) view.findViewById(R.id.tv_item_send_ticket_detials);
        CheckBox cb_select = (CheckBox) view.findViewById(R.id.cb_item_send_ticket);
        if (infos.get(position).getCarnumber() != null) {
            tv_carnumber.setText(infos.get(position).getCarnumber());
        } else {
            tv_carnumber.setText("车牌号未知");
        }
        if (infos.get(position).getRcount() != null && infos.get(position).getRmoney() != null) {
            tv_detials.setText("最近7天给你打赏" + infos.get(position).getRcount() + "笔共" + infos.get(position).getRmoney() + "元");
        }
        if (infos.get(position).getPcount() != null) {
            tv_detials.setText("本周来停车" + infos.get(position).getPcount() + "次");
        }
        if (infos.get(position).getSelect()) {
            cb_select.setChecked(true);
        } else {
            cb_select.setChecked(false);
        }
        cb_select.setTag(position);
        cb_select.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (activity.redpacket.getBmoney().equals("5")) {
                    MyLog.i("SendTicketsAdapter5元", "isChecked = " + isChecked + "选择的位置" + buttonView.getTag());
                    if (isChecked) {
                        if (activity.getScore()) {
                            for (int i = 0; i < infos.size(); i++) {
                                if (infos.get(i).getSelect()) {
                                    infos.get(i).setSelect(false);
                                    activity.setAlreadySelectUser(1, false);
                                }
                            }
                            activity.setAlreadySelectUser(1, true);
                            infos.get((int) buttonView.getTag()).setSelect(true);
                            SendTicketsAdapter.this.notifyDataSetChanged();
                        } else {
                            buttonView.setChecked(false);
                            Toast.makeText(activity, "积分已不足！", 0).show();
                        }
                    } else {
                        activity.setAlreadySelectUser(1, false);
                        infos.get((int) buttonView.getTag()).setSelect(false);
                    }
                } else {
                    MyLog.i("SendTicketsAdapter3元", "isChecked = " + isChecked + "选择的位置" + buttonView.getTag());
                    if (isChecked) {
                        if (activity.getScore()) {
                            activity.setAlreadySelectUser(1, true);
                            infos.get((int) buttonView.getTag()).setSelect(true);
                            boolean isCheckedAll = true;
                            for (int i = 0; i < infos.size(); i++) {
                                if (!infos.get(i).getSelect()) {
                                    isCheckedAll = false;
                                    return;
                                }
                            }
                            if (isCheckedAll) {
                                activity.cb_select_all.setChecked(true);
                            }
                        } else {
                            buttonView.setChecked(false);
                            Toast.makeText(activity, "积分已不足！", 0).show();
                        }
                    } else {
                        if (activity.cb_select_all.isChecked()) {
                            activity.mCbischecked = true;
                            activity.cb_select_all.setChecked(false);
                        }
                        activity.setAlreadySelectUser(1, false);
                        infos.get((int) buttonView.getTag()).setSelect(false);
                    }
                }
            }
        });
        return view;
    }
}
