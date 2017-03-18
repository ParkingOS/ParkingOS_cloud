package com.zhenlaidian.engine;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.LeaveAdapter;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.bean.LeaveOrder;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.MyLog;

import java.util.ArrayList;

/**
 * 弹出现金收费的dialog;
 */
public class ShowCashDealDialog {

    private int position;
    private Context context;
    private ArrayList<LeaveOrder> orders;
    private LeaveAdapter adapter;

    public ShowCashDealDialog() {
        super();
    }

    public ShowCashDealDialog(Context context, ArrayList<LeaveOrder> orders,
                              LeaveAdapter adapter, int position) {
        this.context = context;
        this.orders = orders;
        this.adapter = adapter;
        this.position = position;
    }

    public void showCashDealDialog() {
        AlertDialog.Builder builder = new Builder(context);
        builder.setIcon(R.drawable.app_icon_32);
        builder.setTitle("现金收费提醒");
        builder.setMessage("确认此订单使用现金收费吗?");
        builder.setPositiveButton("确定", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // action=ordercash&token=*&orderid=*
                // 请求服务器把订单置为已支付;
                // 点击确定删除该订单条目;
                getCashService();
//				orders.remove(position);
//				adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("取消", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "取消误操作", 1).show();
            }
        });
        builder.create().show();
    }

    //<?xml version="1.0" encoding="gb2312"?><content><info>现金收费成功!</info></content>
    //http://s.zhenlaidian.com/zld/collectorrequest.do?action=ordercash&token=badb1a55399f40b65b0a3bf98003b479&orderid=1&total=*；
    public void getCashService() {
        // 把现金收费信息提交给服务器
        // action=ordercash&token=*&orderid=*
        String path = Config.getUrl(context);
        String url = path + "collectorrequest.do?action=ordercash&token=" + BaseActivity.token + "&orderid=" + (orders.get(position).getOrderid() + "&total=" + orders.get(position).getTotal() + "&out=json");
        AQuery aQuery = new AQuery(context);
        System.out.println("提交收现金的URL是——>>" + url);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (object != null) {
                    MyLog.i("ShowCashDealDialog---->>>", "info的信息" + object);
                    if (object.equals("1")) {
                        orders.remove(position);
                        adapter.notifyDataSetChanged();
                        //　牵涉到泊车的时候, 判断 如果订单内容不为空的时候 不能清空订单,
                        if (orders.size() < 1) {
                            LeaveActivity activity = (LeaveActivity) context;
                            activity.displayQrcodeView();
                        }
                        Toast.makeText(context, "现金收费提交成功", 0).show();
                    } else {
                        Toast.makeText(context, "现金收费提交失败", 0).show();
                    }
                } else {
                    Toast.makeText(context, "现金收费提交失败", 0).show();
                }
            }
        });
    }

}
