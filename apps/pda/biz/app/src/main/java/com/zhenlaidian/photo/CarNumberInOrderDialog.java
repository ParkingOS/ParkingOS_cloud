package com.zhenlaidian.photo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zhenlaidian.R;
import com.zhenlaidian.util.PlayerVoiceUtil;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

/**
 * 手机扫牌生成订单界面;
 */
public class CarNumberInOrderDialog extends Dialog {

    private TextView tv_time;
    private TextView tv_date;
    private Button bt_cancle;
    private Button bt_ok;
    private Context context;

    public CarNumberInOrderDialog(Context context) {
        super(context);
        this.context = context;
    }

    public CarNumberInOrderDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.nfc_new_order_dialog);
        tv_time = (TextView) this.findViewById(R.id.tv_nfc_new_time);
        tv_date = (TextView) this.findViewById(R.id.tv_date);
        bt_cancle = (Button) this.findViewById(R.id.bt_cancle);
        bt_ok = (Button) this.findViewById(R.id.bt_ok);
        setView();
        SharedPreferences sp = context.getSharedPreferences("voiceBroadcast", Context.MODE_PRIVATE);
        boolean openBroadcast = sp.getBoolean("broadcast", true);
        if (openBroadcast) {
            new PlayerVoiceUtil(context, R.raw.create_order).play();
        }
    }

    @SuppressLint("SimpleDateFormat")
    public void setView() {
        java.util.Date date = new java.util.Date();
        SimpleDateFormat dateaf = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat timef = new SimpleDateFormat("HH:mm");
        tv_time.setText(timef.format(date));
        tv_date.setText(dateaf.format(date));
        bt_cancle.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                CarNumberInOrderDialog.this.dismiss();
            }
        });

        bt_ok.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View v) {

                if (context != null) {
                    CheckNumberActivity activity = (CheckNumberActivity) context;
                    try {
                        activity.madeOrder();
                    } catch (UnsupportedEncodingException e) {
                        Toast.makeText(context, "提交车牌字符转码异常！", 0).show();
                        e.printStackTrace();
                    }
                }
                CarNumberInOrderDialog.this.dismiss();
            }
        });

    }

}
