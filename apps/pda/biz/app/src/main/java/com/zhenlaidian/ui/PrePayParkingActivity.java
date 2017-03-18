package com.zhenlaidian.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.zxing.lswss.QRCodeEncoder;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.InCarDialogInfo;
import com.zhenlaidian.camera.CameraActivity;
import com.zhenlaidian.printer.TcbCheckCarIn;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.ui.dialog.PayCardDialog;
import com.zhenlaidian.util.CameraBitmapUtil;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.Constant;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.ReadCardUtil;
import com.zhenlaidian.util.SharedPreferencesUtils;
import com.zhenlaidian.util.VoiceSynthesizerUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by TCB on 2016/4/17.
 * xulu
 */
public class PrePayParkingActivity extends BaseActivity implements View.OnClickListener {
//    private PiccManager piccReader;
    private static final int CLICK_CANCEL = 812;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_prepay_parking_layout);
        actionBar.show();
        ReadCardUtil.InitReader();
        initView();
    }

    private EditText edtMoney;
    private LinearLayout lnMoney;
    private TextView money1, money2, money3;
    private TextView paycash, paycard, payqrcode;


    private void initView() {
        edtMoney = ((EditText) findViewById(R.id.editText));
        lnMoney = ((LinearLayout) findViewById(R.id.prepay_ln_moneys));
        money1 = ((TextView) findViewById(R.id.money1));
        money2 = ((TextView) findViewById(R.id.money2));
        money3 = ((TextView) findViewById(R.id.money3));
        money1.setOnClickListener(this);
        money2.setOnClickListener(this);
        money3.setOnClickListener(this);
        paycash = (TextView) findViewById(R.id.moneycash);
        paycash.setOnClickListener(this);
        findViewById(R.id.moneycard).setOnClickListener(this);
        findViewById(R.id.moneyqrcode).setOnClickListener(this);
        findViewById(R.id.bt_input_carnumber_ok_dialog).setOnClickListener(this);
        findViewById(R.id.bt_input_carnumber_cancel).setOnClickListener(this);

        if (SharedPreferencesUtils.getIntance(this).getchange_prepay().equals("1")) {
            edtMoney.setFocusableInTouchMode(true);
            edtMoney.setFocusable(true);
            edtMoney.requestFocus();
        } else {
            edtMoney.setFocusable(false);
            edtMoney.setFocusableInTouchMode(false);
        }
        edtMoney.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edtMoney.removeTextChangedListener(this);
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
                edtMoney.setText(money);
                edtMoney.setSelection(money.length());
                edtMoney.addTextChangedListener(this);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        money1.setText(SharedPreferencesUtils.getIntance(this).getprepayset(0));
        money2.setText(SharedPreferencesUtils.getIntance(this).getprepayset(1));
        money3.setText(SharedPreferencesUtils.getIntance(this).getprepayset(2));
        cardDialog = new PayCardDialog(this, "提示", mHandler);
        cardDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isread = true;
            }
        });
    }

    private int photonum =  SharedPreferencesUtils.getIntance(this).getphotoset(0);
    private int numcount = 1;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_input_carnumber_ok_dialog:
                //先检测追缴
                //点击生成订单
                //判断是否能够照相，如果能，则先照相，完成以后再调用生成订单接口
                if (photonum > 0) {
//                    takePhoto(1);
                    Intent i = new Intent(PrePayParkingActivity.this, CameraActivity.class);
                    i.putExtra("num", photonum);
                    startActivityForResult(i, Constant.BACK_FROM_CAMERA_IN);
                } else {
                    try {
                        createOrderForPos("0", "0");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.bt_input_carnumber_cancel:
                finish();
                break;
            case R.id.money1:
                edtMoney.setText(money1.getText().toString());
                edtMoney.setSelection(edtMoney.getText().toString().length());
                break;
            case R.id.money2:
                edtMoney.setText(money2.getText().toString());
                edtMoney.setSelection(edtMoney.getText().toString().length());
                break;
            case R.id.money3:
                edtMoney.setText(money3.getText().toString());
                edtMoney.setSelection(edtMoney.getText().toString().length());
                break;
            case R.id.moneycash:
                //跳转到现金支付
//                CommonMsgDialog cashDialog = new CommonMsgDialog(this, "提示", "确认预付现金", mHandler, "cash");
//                cashDialog.show();

//                if (photonum > 0) {
////                    takePhoto(numcount);
//                    Intent i = new Intent(PrePayParkingActivity.this, CameraActivity.class);
//                    i.putExtra("num", photonum);
//                    startActivityForResult(i, Constant.BACK_FROM_CAMERA_IN);
//                } else {
                try {
                    createOrderForPos("0", "0");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
//                }
                break;
            case R.id.moneycard:
                //跳转到刷卡
                ReadCardUtil.StartReadCard(cardDialog,mHandler);
                if (cout <= 0) {
                    try {
                        createOrderForPos("0", "1");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.moneyqrcode:
                //跳转到扫码支付
                Toast.makeText(context,"入场预支付暂不支持扫码！",Toast.LENGTH_LONG).show();
//                PayQrcodeDialog qrDialog = new PayQrcodeDialog(this, CommontUtils.GetQrBitmap("www.baidu.com"), mHandler, "提示");
//                qrDialog.show();
                break;
        }
    }

    private int cout = 0;

    PayCardDialog cardDialog;
//    Thread cardthread;
    boolean isread = false;
    private VoiceSynthesizerUtil voice;
    //POS机生成订单接口;
    //collectorrequest.do?action=posincome&token=2dd4b1b320225dfd4fc44ad6b53fa734&carnumber=
    private String uid;
    private InCarDialogInfo infos;
    private long currentM = 0;
    private String PayTyep = "0";

    public void createOrderForPos(String bindcard, String paytype) throws UnsupportedEncodingException {
//        bindcard：0 提示激活 1：不提示激活直接刷卡
//        paytype:0默认现金 1刷卡
        PayTyep = paytype;
        if (System.currentTimeMillis() - currentM > 1000) {
            SharedPreferences pfs = this.getSharedPreferences("autologin", Context.MODE_PRIVATE);
            uid = pfs.getString("account", "");
            final String carnumber = URLEncoder.encode(getStringFromPreference("carnumber"), "utf-8");
            String url = BaseActivity.baseurl + "collectorrequest.do?action=posincome&token=" +
                    BaseActivity.token + "&carnumber=" + URLEncoder.encode(carnumber, "utf-8") +
                    "&bid=" + getStringFromPreference("bowei") + "&berthid=" + SharedPreferencesUtils.getIntance(this).getberthid() + "&workid=" +
                    SharedPreferencesUtils.getIntance(this).getworkid() + "&prepay=" + edtMoney.getText().toString()
                    + "&berthorderid=" + getStringFromPreference("berthorderid") + "&orderid=" + getStringFromPreference("preorderid")
                    + "&uuid=" + cardnum + "&bindcard=" + bindcard + "&paytype=" + paytype
                    + "&car_type=" + getIntent().getStringExtra("cartype");
            MyLog.w("InCarDialogActivity", "车牌识别生成订单的URL--->" + url);
            final ProgressDialog dialog = ProgressDialog.show(this, "加载中", "提交订单数据...", true, true);
            dialog.setCanceledOnTouchOutside(false);
            AQuery aQuery = new AQuery(this);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (status.getCode() == 200 && object != null) {
                        dialog.dismiss();
                        MyLog.i("InCarDialogActivity", "车牌识别生成订单的结果--->" + object);
                        Gson gson = new Gson();
                        InCarDialogInfo info = gson.fromJson(object, InCarDialogInfo.class);
                        infos = info;
                        if (info != null) {

                            MyLog.d("InCarDialogActivity", info.toString());
//                            if(Integer.parseInt(info.getResult())<=0){
//                               StartReadCard();
//                            }
                            if ("1".equals(info.getResult())) {
                                //生成订单成功后将车检器订单置空
                                putStringToPreference("berthorderid", "");
                                paycash.setOnClickListener(null);
                                putBooleanToPreference("next", false);
                                voice = new VoiceSynthesizerUtil(context);
                                voice.playText("生成订单");
                                if (photonum > 0) {
                                    Intent i = new Intent(PrePayParkingActivity.this, CameraActivity.class);
                                    i.putExtra("num", photonum);
                                    startActivityForResult(i, Constant.BACK_FROM_CAMERA_IN);
                                } else {
                                    if (PullMsgService.CanPrint) {
                                        prient(uid, info);
                                    } else {
                                        FinishAction();
                                    }
                                }
                            } else if ("-6".equals(info.getResult())) {
                                //卡片未激活，需要跳转到开卡界面 opencard
                                new AlertDialog.Builder(PrePayParkingActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                        .setMessage("卡片未绑定，是否现在绑定？").setPositiveButton("现在绑定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(PrePayParkingActivity.this, OpenCardActivity.class);
                                        i.putExtra("cardid", cardnum);
                                        i.putExtra("type", "-6");
                                        startActivity(i);
                                    }
                                }).setNegativeButton("以后再说", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            createOrderForPos("1", "1");
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).setCancelable(false).create().show();
                            } else if ("-5".equals(info.getResult())) {
                                //卡片未激活，需要跳转到开卡界面 不激活无法使用
                                new AlertDialog.Builder(PrePayParkingActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                        .setMessage("卡片未激活，无法使用！").setPositiveButton("现在激活", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(PrePayParkingActivity.this, OpenCardActivity.class);
                                        i.putExtra("cardid", cardnum);
                                        i.putExtra("type", "-5");
                                        startActivity(i);
                                    }
                                }).setNegativeButton("取消", null).setCancelable(false).create().show();
                            } else {
                                if (cout > 0 || info.getErrmsg().contains("余额不足")) {
                                    if (info.getErrmsg().contains("余额不足")) {
                                        if (cardDialog.isShowing()) {
                                            cardDialog.dismiss();
                                        }
                                        cout--;
                                    }
                                    Toast.makeText(context, info.getErrmsg(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    } else {
                        dialog.dismiss();
                        switch (status.getCode()) {
                            case -101:
                                Toast.makeText(context, "网络错误！--请再次确认车牌生成订单！", Toast.LENGTH_SHORT).show();
                                break;
                            case 500:
                                Toast.makeText(context, "服务器错误！--请再次确认车牌生成订单！", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                    cout++;
                }
            });
            currentM = System.currentTimeMillis();
        }
    }


    //打印凭条
    public void prient(String uid, InCarDialogInfo info) {
        //将已处理的车位号存入，回到泊位列表时移除这个车位号消息 泊位id
        TcbCheckCarIn incar = new TcbCheckCarIn();
        incar.setOrderid(info.getOrderid());
//        incar.setCarnumber(tv_add_carnumber.getText().toString());
        incar.setTime(info.getBtime());
        String Sname="";
        String gang = "";
        if (SharedPreferencesUtils.getIntance(this).getisprintName()) {
            Sname = getStringFromPreference("name");
            gang = "-";
        }
        incar.setMeterman(Sname);
        Bitmap qrbitmap = new QRCodeEncoder().encode2BitMap(BaseActivity.baseurl + info.getQrcode(), 240, 240);
        Bitmap imgbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_check);
//        PrinterUitls.getInstance().printerTCBCheckCarIn(incar, qrbitmap, imgbitmap);
        String pre = CommontUtils.checkString(edtMoney.getText().toString()) ? edtMoney.getText().toString() : "0";
        String printstr = Constant.HEADIN +
                SharedPreferencesUtils.getIntance(context).getprint_signInHead() + "\n" +
                "停车场：" + SharedPreferencesUtils.getIntance(context).getParkname() +
                "—" + SharedPreferencesUtils.getIntance(context).getberthsec_name() + "\n" +
                "收费员：" + incar.getMeterman();
        if (CommontUtils.Is910()) {
            if (!TextUtils.isEmpty(SharedPreferencesUtils.getIntance(context).getmobile())) {
                printstr += "(" + SharedPreferencesUtils.getIntance(context).getmobile() + ")";
            }
        }
        printstr += gang + uid;
        printstr += "\n" +
                "车位：" + getStringFromPreference("boweiversion") + "\n" +
                "车牌号：" + getStringFromPreference("carnumber") + "\n" +
                "停车类型：临时停车\n" +
                "进场时间：" + incar.getTime() + "\n" +
                "预收金额：" + pre + "元\n";
        String pay = "";
        if (PayTyep.equals("1")) {
            pay = "刷卡支付";
        } else {
            pay = "现金支付";
        }
        printstr +=
                "支付方式：" + pay + "\n\n" +
                        Constant.FOOT +
                        SharedPreferencesUtils.getIntance(context).getprint_signIn() + "\n\n\n\n\n";
        //记录进场的小票信息
        PullMsgService.sendMessage(printstr, context);
        FinishAction();
    }

    private void FinishAction() {
        putStringToPreference("boweistate", getStringFromPreference("bowei"));
        finish();
    }

    private String cardnum = "";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.MSG_FOUND_UID:
                    cardDialog.dismiss();
                    String uid = (String) msg.obj;
                    cardnum = uid;
                    photonum = SharedPreferencesUtils.getIntance(PrePayParkingActivity.this).getphotoset(0);
//                    if (photonum > 0) {
//                        Intent i = new Intent(PrePayParkingActivity.this, CameraActivity.class);
//                        i.putExtra("num", photonum);
//                        startActivityForResult(i, Constant.BACK_FROM_CAMERA_IN);
//                    } else {
                    try {
                        createOrderForPos("0", "1");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
//                    }
//                    Toast.makeText(context, "卡号：" + uid, Toast.LENGTH_LONG).show();
                    break;
                case CLICK_CANCEL:
                    if (cardDialog.isShowing()) {
                        cardDialog.dismiss();
                    }
                    isread = true;
                    break;
            }
        }
    };
    private ArrayList<String> listPath = new ArrayList<String>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyLog.i("INCarDialogActivity", "onActivityResult-------->");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.BACK_FROM_CAMERA_IN) {
                listPath = data.getStringArrayListExtra("list");
//                try {
//                    createOrderForPos("0");
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }

                if (CommontUtils.checkList(listPath)) {
                    for (int i = 0; i < listPath.size(); i++) {
                        String SDState = Environment.getExternalStorageState();
                        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                            File dir = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
                            if (!dir.exists()) {
                                dir.mkdirs();
                            }
                            (new File(listPath.get(i))).renameTo(new File(dir.getAbsolutePath(), infos.getOrderid() + "in" + i + ".jpeg"));
                            CameraBitmapUtil.upload(context, i, infos.getOrderid(), 0);
                        }
                    }
                }
                if (PullMsgService.CanPrint) {
                    prient(uid, infos);
                } else {
                    FinishAction();
                }

            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ReadCardUtil.StopReading();
    }
}
