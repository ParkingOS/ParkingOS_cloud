package com.zhenlaidian.engine;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.LeaveAdapter;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.bean.FreeOrder;
import com.zhenlaidian.bean.LeaveOrder;
import com.zhenlaidian.ui.BaseActivity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * 优惠处理订单的dialog;
 */
public class ShowYouHuiDialog {
    private int position;
    private Context context;
    private ArrayList<LeaveOrder> orders;
    private LeaveAdapter adapter;
    private int time;

    public ShowYouHuiDialog() {
        super();
    }

    public ShowYouHuiDialog(Context context, ArrayList<LeaveOrder> orders,
                            LeaveAdapter adapter, int position, int time) {
        this.context = context;
        this.orders = orders;
        this.adapter = adapter;
        this.position = position;
        this.time = time;
    }

    public void showUpdataDialog() {
        AlertDialog.Builder builder = new Builder(context);
        builder.setIcon(R.drawable.app_icon_32);
        builder.setTitle("确认优惠提醒");
        if (time == 100) {
            builder.setMessage("确认此订单完全免费吗?");
        } else {
            builder.setMessage("确认此订单优惠" + time + "小时吗");
        }
        builder.setPositiveButton("确定", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // action=tosale&token=*&orderid=*&hour=*
                // 点击确定把优惠信息提交给服务器.服务器返回优惠完成.然后删除该条目;
                getService();
            }
        });
        builder.setNegativeButton("取消", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "取消误操作", 0).show();
            }
        });
        builder.create().show();
    }

    //把免费信息提交给服务器
    //collectorrequest.do?action=tosale&token=809fcc23b158d71f043f5eca52192a9f&orderid=1&hour=1
    public void getService() {
        String path = Config.getUrl(context);
        ;
        String url = path + "collectorrequest.do?action=tosale&token="
                + BaseActivity.token + "&orderid="
                + orders.get(position).getOrderid() + "&hour=" + time;
        AQuery aQuery = new AQuery(context);
        aQuery.ajax(url, byte[].class, new AjaxCallback<byte[]>() {

            @Override
            public void callback(String url, byte[] object,
                                 AjaxStatus status) {
                if (object != null) {
//					String message = new String(object);
//					Log.e("ShowYouHuiDialog", message);
                    InputStream is = new ByteArrayInputStream(object);
                    try {
                        FreeOrder freeOrder = FreeOrderParser.getFreeOrder(is);
                        is.close();
                        System.out.println("" + freeOrder.getInfo() + ">>>>" + freeOrder.getMessage());
                        if (freeOrder.getInfo().equals("success")) {
                            orders.remove(position);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(context, "优惠提交成功", 0).show();
                        } else {
                            Toast.makeText(context, "优惠提交失败", 0).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("ShowYouHuiDialog", "优惠信息解析失败");
                    }
                }
            }

        });
    }


}
