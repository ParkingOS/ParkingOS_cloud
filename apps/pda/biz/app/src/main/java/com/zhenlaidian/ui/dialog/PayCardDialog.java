package com.zhenlaidian.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.zhenlaidian.R;

/**
 * Created by TCB on 2016/4/18.
 * xulu
 */
public class PayCardDialog extends Dialog{
    private Context context;
    private static final int CLICK_CANCEL = 812;
    public PayCardDialog(Context context) {
        super(context);
        this.context = context;
    }

    private String title;
    private Handler handler;

    public PayCardDialog(Context context, String title, Handler handler) {
        super(context, R.style.nfcfinishdialog);
        this.context = context;
        this.title = title;
        this.handler = handler;
    }
    TextView txtTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_dialog_paycard_layout);
        txtTitle = ((TextView) findViewById(R.id.dialog_title));
        txtTitle.setText(title);
        findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(CLICK_CANCEL);
                if(isShowing()){
                    dismiss();
                }
            }
        });
    }
}
