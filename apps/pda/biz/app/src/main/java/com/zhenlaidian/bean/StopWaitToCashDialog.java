package com.zhenlaidian.bean;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.zhenlaidian.R;
import com.zhenlaidian.engine.ShowNfcFinishOrder;
import com.zhenlaidian.photo.CarOrderActivity;

public class StopWaitToCashDialog extends Dialog {
    private ShowNfcFinishOrder finishOrder;
    private CarOrderActivity activity;
    private Context context;
    private Button bt_cancle;
    private Button bt_cash_order;
    private NfcOrder nfcOrder;
    private Boolean iscash;
    private View view;
    private String orderid;
    private String collect;

    public StopWaitToCashDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    public StopWaitToCashDialog(Context context, ShowNfcFinishOrder finishOrder, NfcOrder nfcOrder, Boolean iscash) {
        super(context);
        this.context = context;
        this.finishOrder = finishOrder;
        this.nfcOrder = nfcOrder;
        this.iscash = iscash;
    }

    public StopWaitToCashDialog(Context context, CarOrderActivity activity, NfcOrder nfcOrder,String orderid,String collect, Boolean iscash) {
        super(context);
        this.context = context;
        this.activity = activity;
        this.nfcOrder = nfcOrder;
        this.iscash = iscash;
        this.orderid = orderid;
        this.collect =collect;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.dialog_stop_wait_to_cash);
        bt_cancle = (Button) findViewById(R.id.bt_stop_wait_dialog_cancle);
        bt_cash_order = (Button) findViewById(R.id.bt_stop_wait_dialog_cash);
        view = findViewById(R.id.view_stop_wait_dialog_view);
        setView();
        setCanceledOnTouchOutside(false);
    }

    public void setView() {
        if (iscash) {
            bt_cash_order.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
        } else {
            bt_cash_order.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
        }
        bt_cancle.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                StopWaitToCashDialog.this.dismiss();
            }
        });
        bt_cash_order.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (finishOrder == null) {
                    activity.sumitCahsOrder(nfcOrder,orderid,collect,StopWaitToCashDialog.this,"1");
                } else {
                    finishOrder.submitCash(nfcOrder.getCollect(), StopWaitToCashDialog.this, "1", nfcOrder.getCarnumber());
                }
            }
        });

    }
}
