package com.zhenlaidian.engine;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

import com.zhenlaidian.R;
import com.zhenlaidian.adapter.LeaveAdapter;
import com.zhenlaidian.bean.LeaveOrder;
import com.zhenlaidian.ui.LeaveActivity;

import java.util.ArrayList;


/**
 * 弹出确认收费完成的dialog
 */
public class ShowConfirmDialog {
    private int position;
    private Context context;
    private ArrayList<LeaveOrder> orders;
    private LeaveAdapter adapter;

    public ShowConfirmDialog() {
        super();
    }

    public ShowConfirmDialog(Context context, ArrayList<LeaveOrder> orders, LeaveAdapter adapter, int position) {
        this.context = context;
        this.orders = orders;
        this.adapter = adapter;
        this.position = position;
    }

    public void showUpdataDialog() {
        AlertDialog.Builder builder = new Builder(context);
        builder.setIcon(R.drawable.app_icon_32);
        builder.setTitle("确认收费提醒");
        builder.setMessage("确认此订单收费完成吗?");
        builder.setPositiveButton("确定", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //点击确定删除该订单条目;
                orders.remove(position);
                adapter.notifyDataSetChanged();
                LeaveActivity activity = (LeaveActivity) context;
                activity.displayQrcodeView();
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
}
