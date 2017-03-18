package com.zhenlaidian.engine;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.LeaveAdapter;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.bean.IbeaconCashInfo;
import com.zhenlaidian.bean.LeaveOrder;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.MyLog;

import java.util.ArrayList;

/**
 * 弹出蓝牙订单结算的对话框;
 */

public class ShowIbeaconCashDialog {

    private int position;
    private Context context;
    private ArrayList<LeaveOrder> orders;
    private LeaveAdapter adapter;

    public ShowIbeaconCashDialog() {
        super();
    }

    public ShowIbeaconCashDialog(Context context, ArrayList<LeaveOrder> orders,
                                 LeaveAdapter adapter, int position) {
        this.context = context;
        this.orders = orders;
        this.adapter = adapter;
        this.position = position;
    }

    public void showIbeaconCashDialog() {
        AlertDialog.Builder builder = new Builder(context);
        builder.setIcon(R.drawable.app_icon_32);
        builder.setTitle("结算订单提醒");
        builder.setMessage(orders.get(position).getCarnumber() + " 确认结算价格：" + orders.get(position).getTotal() + " 元");
        builder.setPositiveButton("确定", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                getCashService();
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

    // ibencon调价后结算的接口  ibeaconhandle.do?action=payorder&id=&total=
//	id:订单编号//total:订单金额
//	返回：{\"result\":\"2\",\"info\":\"已支付过，不能重复支付\"}
//	result 0失败 1成功 2：重复支付 
//	info:提示信息
    public void getCashService() {
        String path = Config.getUrl(context);
        String url = path + "ibeaconhandle.do?action=payorder&id=" + orders.get(position).getOrderid() + "&total=" + orders.get(position).getTotal();
        AQuery aQuery = new AQuery(context);
        Log.e("ShowIbecconCashDialog", "ibencon调价后结算接口URL——>>" + url);
        final ProgressDialog dialog = ProgressDialog.show(context, "结算中...", "正在结算订单...", true, true);
        dialog.setCanceledOnTouchOutside(false);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (!TextUtils.isEmpty(object)) {
                    dialog.dismiss();
                    MyLog.e("ShowCashDealDialog---->>>", "info的信息" + object);
                    Gson gson = new Gson();
                    IbeaconCashInfo info = gson.fromJson(object, IbeaconCashInfo.class);
                    if (info != null && "1".equals(info.result)) {
                        removeOrder();
                        Toast.makeText(context, "收费结算成功", 0).show();
                    } else if (info != null && "0".equals(info.result)) {
                        showDialog(info.info);
                        removeOrder();
                    } else if (info != null && "2".equals(info.result)) {
                        removeOrder();
                        Toast.makeText(context, "收费结算失败--" + info.getInfo(), 0).show();
                    } else {
                        showDialog("");
                        removeOrder();
                    }
                } else {
                    dialog.dismiss();
                    Toast.makeText(context, "网络错误！收费结算失败", 0).show();
                }
            }
        });
    }

    public void removeOrder() {
        orders.remove(position);
        adapter.notifyDataSetChanged();
        if (orders.size() < 1) {
            LeaveActivity activity = (LeaveActivity) context;
            activity.displayQrcodeView();
        }
    }

    public void showDialog(String info) {
        new AlertDialog.Builder(context).setIcon(R.drawable.app_icon_32)
                .setTitle("自动支付失败！").setMessage(orders.get(position).getCarnumber() + " " + info + ",无法完成自动支付，请收取现金(同时将订单金额调至0元结算掉)")
                .setNegativeButton("知道了", null).setCancelable(false).create().show();
    }
}
