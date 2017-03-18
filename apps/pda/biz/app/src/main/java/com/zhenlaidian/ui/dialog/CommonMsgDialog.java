package com.zhenlaidian.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.zhenlaidian.R;

/**
 * Created by TCB on 2016/4/18
 * xulu
 */
public class CommonMsgDialog extends Dialog{
    private Context context;
    private TextView txtTitle;
    private TextView txtContent;
    private TextView cacel;
    private TextView ok;

    public CommonMsgDialog(Context context) {
        super(context);
        this.context = context;
    }
    private String title,content;
    private Handler handler;
    private String type;
    public CommonMsgDialog(Context context,String title,String content,Handler handler,String type) {
        super(context,R.style.nfcfinishdialog);
        this.context = context;
        this.title = title;
        this.content = content;
        this.handler = handler;
        this.type = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_dialog_escape_confirm_layout);

        txtTitle = ((TextView) findViewById(R.id.dialog_title));
        txtContent = ((TextView) findViewById(R.id.dialog_content));
        cacel = ((TextView) findViewById(R.id.dialog_cancel));
        ok = ((TextView) findViewById(R.id.dialog_ok));

        txtTitle.setText(title);
        txtContent.setText(content);

        cacel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message m = new Message();
                m.what = 11;
                m.obj = type;
                handler.sendMessage(m);
                dismiss();
            }
        });
    }
}
