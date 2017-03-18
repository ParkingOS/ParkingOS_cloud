package com.zhenlaidian.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.BaseResponse;
import com.zhenlaidian.bean.Card;
import com.zhenlaidian.bean.CardInfo;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

/**
 * Created by xulu on 2016/9/13.
 */
public class CardChargeActivity extends BaseActivity {
    IntentFilter fileter = new IntentFilter("READ_UUID");
    BroadcastReceiver receve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_cardcharge_layout);
        actionBar.show();
        initView();
    }

    EditText uuid, chargemoney;
    TextView msgs;
    TextView submit;

    private void initView() {
        uuid = ((EditText) findViewById(R.id.edit_uuid));
        uuid.setText(getIntent().getStringExtra("uuid"));
        chargemoney = ((EditText) findViewById(R.id.edit_charge));
        chargemoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chargemoney.removeTextChangedListener(this);
                String money = s.toString();

                if (money.contains(".")) {
                    if (money.equals(".")) {
                        money = "0.";
                    } else {
                        String[] code = money.split("");
                        money = "";
                        int pcout = 0;
                        for (String c : code) {
                            if (c.equals(".")) {
                                pcout++;
                            }
                            if (pcout > 1 && c.equals(".")) {

                            } else {
                                money += c;
                            }

                        }
                        String[] point = money.split("\\.");
                        if (point.length > 1) {
                            String tail = point[1];
                            if (tail.length() > 2) {
                                tail = tail.substring(0, 2);
                            }
                            money = point[0] + "." + tail;
                        }
                    }

                }
                chargemoney.setText(money);
                chargemoney.setSelection(money.length());
                chargemoney.addTextChangedListener(this);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        msgs = ((TextView) findViewById(R.id.text_msg));
        submit = ((TextView) findViewById(R.id.btn_submitcharge));
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //充值按钮
                if (CommontUtils.checkString(uuid.getText().toString())) {
                    if (CommontUtils.checkString(chargemoney.getText().toString())) {
                        ChargeCard();
                    } else {
                        Toast.makeText(CardChargeActivity.this, "请填写充值金额！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CardChargeActivity.this, "请先刷卡！", Toast.LENGTH_SHORT).show();
                }
            }
        });
        uuid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 1) {
                    msgs.setText("");
                }
            }
        });
        if (TextUtils.isEmpty(uuid.getText().toString())) {
            msgs.setText("请刷卡");
        } else {
            msgs.setText("");
        }
        receve = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (CommontUtils.checkString(intent.getStringExtra("uuid")))
                    uuid.setText(intent.getStringExtra("uuid"));
            }
        };
        registerReceiver(receve, fileter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receve);
    }

    /**
     * 查询卡信息
     * collectorrequest.do?action=cardinfo
     * &token=&version=&uuid=
     * <p/>
     * result --0：失败 1：成功
     * errmsg：原因
     */

    public void QurrayCard(String uid, final boolean isprint) {
        AQuery aQuery = new AQuery(this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=cardinfo&token="
                + token + "&uuid=" + uid
                + "&version=" + CommontUtils.getVersion(context) + "&out=json";
        MyLog.i("opencard-->>", "查询的URL-->>" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "正在查询...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                dialog.dismiss();
                MyLog.i("opencard-->>", "json--" + object);
                if (object != null) {
                    Gson gson = new Gson();
                    CardInfo info = gson.fromJson(object, CardInfo.class);
                    if (info.getResult().equals("1")) {
                        if (isprint) {
                            printcharge(info);
                        }
                        if (info.getCard() != null) {
                            if (!TextUtils.isEmpty(info.getCard().getCard_number())) {
                                uuid.setText(info.getCard().getNfc_uuid());
                                uuid.setSelection(info.getCard().getNfc_uuid().length());
                            }
                        }
                    } else {
                        Toast.makeText(context, info.getErrmsg(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "返回数据错误！", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    /**
     * 充值卡片
     * collectorrequest.do?action=chargecard&token=&version=&uuid=&money=
     * <p/>
     * result --0：失败 1：成功
     * errmsg：原因
     */

    public void ChargeCard() {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取信息失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }
        AQuery aQuery = new AQuery(CardChargeActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=chargecard&token="
                + token + "&uuid=" + uuid.getText().toString()
                + "&version=" + CommontUtils.getVersion(context)
                + "&money=" + chargemoney.getText().toString()
                + "&out=json";
        MyLog.i("ChargeCard-->>", "充值的URL-->>" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "正在充值...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                dialog.dismiss();
                MyLog.i("ChargeCard-->>", "json--" + object);
                if (object != null) {
                    Gson gson = new Gson();
                    BaseResponse info = gson.fromJson(object, BaseResponse.class);
                    if (info.getResult().equals("1")) {
//                        msgs.setText("充值" + chargemoney.getText().toString() + "元成功");
                        Toast.makeText(context, "充值" + chargemoney.getText().toString() + "元成功！", Toast.LENGTH_LONG).show();
                        QurrayCard(uuid.getText().toString(), true);

                    } else {
                        Toast.makeText(context, info.getErrmsg(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, "返回数据错误！", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    /**
     * 激活卡后打印出小票
     */
    private void printcharge(CardInfo info) {
        Card card = info.getCard();
        String Sname = "";
        String gang = "";
        if (SharedPreferencesUtils.getIntance(this).getisprintName()) {
            Sname = getStringFromPreference("name");
            gang = "-";
        }

        String str = "充值凭证\n\n" +
                "收费单位：" + info.getGroup_name() + "\n" +
                "终端编号：" + CommontUtils.GetHardWareAddress(context) + "\n" +
                "收费员：" + Sname;
        if (CommontUtils.Is910()) {
            if (!TextUtils.isEmpty(SharedPreferencesUtils.getIntance(context).getmobile())) {
                str += "(" + SharedPreferencesUtils.getIntance(context).getmobile() + ")";
            }
        }
        str += gang + SharedPreferencesUtils.getIntance(this).getAccount();
        str += "\n" +
                "卡号：" + card.getNfc_uuid() + "\n" +
                "卡面号：" + card.getCard_number() + "\n" +
                "充值金额：￥" + chargemoney.getText() + "\n" +
                "卡片余额：￥" + card.getBalance() + "\n" +
                "操作时间：" + CommontUtils.getTimespanss() + "\n\n";
        PullMsgService.sendMessage(str, context);
        chargemoney.setText("");
    }
}
