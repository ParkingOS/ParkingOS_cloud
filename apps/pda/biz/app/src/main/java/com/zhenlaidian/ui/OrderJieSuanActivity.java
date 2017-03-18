package com.zhenlaidian.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.AllOrder;
import com.zhenlaidian.bean.BaseResponCard;
import com.zhenlaidian.bean.LeaveOrder;
import com.zhenlaidian.bean.OrderJieSuanEntity;
import com.zhenlaidian.bean.ZhuiJiaoItemEntity;
import com.zhenlaidian.bean.ZhuiJiaoListEntity;
import com.zhenlaidian.camera.CameraActivity;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.ui.dialog.CommonMsgDialog;
import com.zhenlaidian.ui.dialog.PayCardDialog;
import com.zhenlaidian.ui.dialog.PayQrcodeDialog;
import com.zhenlaidian.util.CameraBitmapUtil;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.Constant;
import com.zhenlaidian.util.IsNetWork;
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
public class OrderJieSuanActivity extends BaseActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_order_jiesuan_layout);
        ReadCardUtil.InitReader();
        initView();
        IntentFilter filter = new IntentFilter("E_PAY");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setLeaveOrder((LeaveOrder) intent.getSerializableExtra("epay"));
            }
        };
        registerReceiver(receiver, filter);
    }

    BroadcastReceiver receiver;
    private AllOrder order;
    private TextView orderNum, orderState, orderIntime, orderOuttime, orderLength, orderMoney, orderPrepay, orderShouldpay;
    private TextView payMoney, payCard, payQrcode, payEscape;

    private void initView() {
        order = (AllOrder) getIntent().getSerializableExtra("detail");
        orderNum = (TextView) findViewById(R.id.tv_current_order_details_orderNumber);
        orderState = (TextView) findViewById(R.id.tv_current_order_details_orderStatus);
        orderIntime = (TextView) findViewById(R.id.tv_current_order_details_inTime);
        orderOuttime = (TextView) findViewById(R.id.tv_current_order_details_outTime);
        orderLength = (TextView) findViewById(R.id.tv_current_order_details_Timelength);
        orderMoney = (TextView) findViewById(R.id.orderjiesuan_txt_money);
        orderPrepay = (TextView) findViewById(R.id.orderjiesuan_txt_premoney);
        orderShouldpay = (TextView) findViewById(R.id.orderjiesuan_txt_shouldpay);

        payMoney = (TextView) findViewById(R.id.moneycash);
        payMoney.setOnClickListener(this);
        findViewById(R.id.moneycard).setOnClickListener(this);
        payQrcode = (TextView) findViewById(R.id.moneyqrcode);
        payQrcode.setOnClickListener(this);
        if (Double.parseDouble(order.getPrepay()) > 0) {
            payQrcode.setVisibility(View.GONE);
        }
        findViewById(R.id.escape).setOnClickListener(this);

        if (order != null) {
            orderNum.setText(order.getOrderid() + "");
            orderState.setText(order.getState() + "");
            orderIntime.setText(CommontUtils.Unix2Time(order.getBegin()) + "");
            orderOuttime.setText(CommontUtils.Unix2Time(order.getEnd()) + "");
            orderLength.setText(order.getDuration() + "");
            orderMoney.setText(order.getTotal() + "");
            orderPrepay.setText(order.getPrepay() + "");
            orderShouldpay.setText((CommontUtils.doubleTwoPoint(Double.parseDouble(order.getTotal()) - Double.parseDouble(order.getPrepay()))) + "");
        }
        cardDialog = new PayCardDialog(this, "提示", h);
        cardDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isread = true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ReadCardUtil.StopReading();
        unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.moneycash:
//                CommonMsgDialog cashDialog = new CommonMsgDialog(this, "订单提醒", "确认现金结算", h, "cash");
//                cashDialog.show();
                voice = new VoiceSynthesizerUtil(context);
                voice.playText("结算订单" + order.getTotal() + "元");

//                String type = (String) msg.obj;
                photonum1 = SharedPreferencesUtils.getIntance(OrderJieSuanActivity.this).getphotoset(1);
                photonum2 = SharedPreferencesUtils.getIntance(OrderJieSuanActivity.this).getphotoset(2);

//                        CommontUtils.toast(context,"确定现金支付");
                //点击结算订单
                //判断是否能够照相，如果能，则先照相，完成以后再调用生成订单接口
//                if (photonum1 > 0) {
////                    takePhoto(numcount1, 1);
//                    Intent i = new Intent(OrderJieSuanActivity.this, CameraActivity.class);
//                    i.putExtra("num", photonum1);
//                    startActivityForResult(i, Constant.BACK_FROM_CAMERA_OUT);
//                } else {
                try {
                    deleteOrder(order.getOrderid(), order.getTotal(), order.getEnd());
//                        getOrderinfo(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                }

                break;
            case R.id.moneycard:
                //跳转到刷卡
                ReadCardUtil.StartReadCard(cardDialog, h);
                break;
            case R.id.moneyqrcode:
//                alibitmap = CommontUtils.addLogo(qrcode,CommontUtils.Drawable2Bitmap(context,R.drawable.alipay));
                String wechatimg = baseurl + "wxpfast.do?action=sweeporder&orderid=" + order.getOrderid() + "&berthorderid=" + getStringFromPreference("berthorderid") + "&endtime=" + order.getEnd();
                //                qrDialog = new PayQrcodeDialog(this, CommontUtils.GetQrBitmap(baseurl + "wxpfast.do?action=sweeporder&orderid=" + order.getOrderid() + "&berthorderid=" + getStringFromPreference("berthorderid") + "&endtime=" + order.getEnd()), h, "提示");
                qrDialog = new PayQrcodeDialog(this, CommontUtils.addLogo(wechatimg, CommontUtils.Drawable2Bitmap(context, R.drawable.wechat)), h, "提示"
                        , order.getOrderid(), order.getTotal(), order.getEnd());
//                qrDialog = new PayQrcodeDialog(this, CommontUtils.GetQrBitmap("http://180.150.188.224:8080/zld/wxpfast.do?action=sweeporder&orderid=" + order.getOrderid() + "&berthorderid=" + getStringFromPreference("berthorderid")+"&endtime="+order.getEnd()), h, "提示");
                qrDialog.show();

                break;
            case R.id.escape:
                CommonMsgDialog escapeDialog = new CommonMsgDialog(this, "车辆未缴确认", "确认要置为未缴？", h, "escape");
                escapeDialog.show();
                break;
        }
    }

    PayCardDialog cardDialog;
    boolean isread = false;
    static PayQrcodeDialog qrDialog;

    private VoiceSynthesizerUtil voice;

    //	http://s.zhenlaidian.com/zld/collectorrequest.do?action=ordercash&token=&orderid=1&total=*；
    //现金结算订单或者删除订单；
    public void deleteOrder(String orderid, String total, String endtime) {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取订单失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }
        AQuery aQuery = new AQuery(this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=ordercash&token=" + token
                + "&orderid=" + orderid + "&total=" + total + "&imei=" + imei +
                "&workid=" + SharedPreferencesUtils.getIntance(this).getworkid() +
                "&berthorderid=" + getStringFromPreference("berthorderid") + "&out=json" +
                "&endtime=" + endtime + "&version=" + CommontUtils.getVersion(OrderJieSuanActivity.this);
        MyLog.w("删除订单的URl-->>", url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "提交现金结算数据...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            public void callback(String url, String object, AjaxStatus status) {
                MyLog.d("CurrentOrderDetailsActivity", "现金结算返回的结果是-->>" + object);
                if (object != null) {
                    dialog.dismiss();
                    Gson gson = new Gson();
                    OrderJieSuanEntity entity = gson.fromJson(object, OrderJieSuanEntity.class);
                    if (entity.getResult().equals("1")) {
                        //结算成功后将车检器订单置 succeed
                        putStringToPreference("berthorderid", "succeed");
                        MyLog.w("CurrentOrderDetailsActivity", "现金结算成功！");
                        payMoney.setOnClickListener(null);
                        if (CommontUtils.checkString(entity.getErrmsg())) {
                            CommontUtils.toast(context, entity.getErrmsg() + "");
                        }
                        if (photonum1 > 0) {
                            Intent i = new Intent(OrderJieSuanActivity.this, CameraActivity.class);
                            i.putExtra("num", photonum1);
                            startActivityForResult(i, Constant.BACK_FROM_CAMERA_OUT);
                        } else {
                            if (PullMsgService.CanPrint) {
                                prient("现金支付");
                            } else {
                                try {
                                    getParkInfo(order.getCarnumber());
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }


                    } else if (object.equals("-1")) {
                        Toast.makeText(OrderJieSuanActivity.this, "" + entity.getErrmsg(), Toast.LENGTH_SHORT).show();
                        OrderJieSuanActivity.this.finish();
                    } else {
                        Toast.makeText(context, "操作失败！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    dialog.dismiss();
                    return;
                }
            }
        });
    }


    //置为未缴：  cobp.do?action=escape&orderid=124614&comid=10&total=100.98；
    public void MakeLostOrder(String orderid, String total) {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "置为未缴失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }
        AQuery aQuery = new AQuery(this);
        String path = baseurl;
        String url = path + "cobp.do?action=escape&orderid=" + orderid + "&comid=" + comid + "&total=" + total +
                "&workid=" + SharedPreferencesUtils.getIntance(this).getworkid() +
                "&uid=" + getStringFromPreference("uid")
                + "&brethorderid=" + getStringFromPreference("berthorderid")
                + "&endtime=" + order.getEnd();
        MyLog.w("置为未缴的URl-->>", url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "提交置为未缴数据...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            public void callback(String url, String object, AjaxStatus status) {
                MyLog.i("CurrentOrderDetailsActivity", "置为未缴返回的结果是-->>" + object);
                if (object != null) {
                    dialog.dismiss();
                    if (object.equals("1")) {
                        //结算成功后将车检器订单置 succeed
                        putStringToPreference("berthorderid", "succeed");
                        if (CommontUtils.checkList(listPath2)) {
                            for (int i = 0; i < listPath2.size(); i++) {
                                String SDState = Environment.getExternalStorageState();
                                if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                                    File dir = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
                                    if (!dir.exists()) {
                                        dir.mkdirs();
                                    }
                                    (new File(listPath2.get(i))).renameTo(new File(dir.getAbsolutePath(), order.getOrderid() + "esc" + i + ".jpeg"));
                                    CameraBitmapUtil.upload(context, i, order.getOrderid(), 2);
                                }
                            }
                        }
                        Toast.makeText(OrderJieSuanActivity.this, "置为未缴成功！", Toast.LENGTH_SHORT).show();
                        OrderJieSuanActivity.this.finish();
                    } else if (object.equals("-1")) {
                        Toast.makeText(OrderJieSuanActivity.this, "置为未缴失败！", Toast.LENGTH_SHORT).show();
                        OrderJieSuanActivity.this.finish();
                    } else if (object.equals("-2")) {
                        Toast.makeText(OrderJieSuanActivity.this, "已置为未缴，不可再次置为未缴！", Toast.LENGTH_SHORT).show();
                        OrderJieSuanActivity.this.finish();
                    } else if (object.equals("-3")) {
                        Toast.makeText(OrderJieSuanActivity.this, "0元订单不可置为未缴！", Toast.LENGTH_SHORT).show();
                        OrderJieSuanActivity.this.finish();
                    } else {
                        Toast.makeText(OrderJieSuanActivity.this, "未知错误！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    dialog.dismiss();
                    Toast.makeText(OrderJieSuanActivity.this, "网络错误！", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    //打印凭条
    public void prient(String payway) {
        //将已处理的车位号存入，回到泊位列表时移除这个车位号消息
        double payactual = 0;
        if (CommontUtils.checkString(order.getTotal()) && CommontUtils.checkString(order.getPrepay())) {
            payactual = CommontUtils.doubleTwoPoint(Double.parseDouble(order.getTotal()) - Double.parseDouble(order.getPrepay()));
        }
        String pay;
        if (payactual < 0) {
            pay = "应退：" + (-payactual) + "元";
        } else {
            pay = "出场缴费：" + payactual + "元";
        }
        String Sname = "";
        String gang = "";
        if (SharedPreferencesUtils.getIntance(this).getisprintName()) {
            Sname = getStringFromPreference("name");
            gang = "-";
        }
        String printstr = Constant.HEADOut +
                SharedPreferencesUtils.getIntance(context).getprint_signOutHead() + "\n" +
                "停车场：" + SharedPreferencesUtils.getIntance(context).getParkname() +
                "—" + SharedPreferencesUtils.getIntance(context).getberthsec_name() + "\n" +
                "收费员：" + Sname;
        if (CommontUtils.Is910()) {
            if (!TextUtils.isEmpty(SharedPreferencesUtils.getIntance(context).getmobile())) {
                printstr += "(" + SharedPreferencesUtils.getIntance(context).getmobile() + ")";
            }
        }
        printstr += gang + useraccount;

        printstr += "\n" +
                "车位：" + order.getPark() + "\n" +
                "车牌号：" + order.getCarnumber() + "\n" +
                "停车类型：临时停车\n" +
                "进场时间：" + CommontUtils.Unix2TimeS(order.getBegin()) + "\n" +
                "出场时间：" + CommontUtils.Unix2TimeS(order.getEnd()) + "\n" +
                "停车时长：" + order.getDuration() + "\n" +
                "支付方式：" + payway + "\n" +
                "订单金额：" + order.getTotal() + "元\n" +
                "预缴金额：" + order.getPrepay() + "元\n" +
                pay + "\n\n" +
                Constant.FOOT +
                SharedPreferencesUtils.getIntance(context).getprint_signOut() + "\n\n\n\n\n";
        if (payactual != 0) {
            PullMsgService.sendMessage(printstr, context);
        } else {
            if (Double.parseDouble(order.getPrepay()) > 0) {
                PullMsgService.sendMessage(printstr, context);
            }
        }
//        sendMessage(qrbitmap);
        try {
            getParkInfo(order.getCarnumber());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void FinishAction() {
        putBooleanToPreference("next", false);
        putStringToPreference("boweistate", order.getBerthnumber());
        finish();
    }

    private int photonum1, photonum2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyLog.i("INCarDialogActivity", "onActivityResult-------->");
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case Constant.BACK_FROM_OWE:
                    FinishAction();
                    break;
                case Constant.BACK_FROM_CAMERA_OUT:
                    listPath1 = data.getStringArrayListExtra("list");
                    if (CommontUtils.checkList(listPath1)) {
                        for (int i = 0; i < listPath1.size(); i++) {
                            String SDState = Environment.getExternalStorageState();
                            if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                                File dir = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                (new File(listPath1.get(i))).renameTo(new File(dir.getAbsolutePath(), order.getOrderid() + "out" + i + ".jpeg"));
                                CameraBitmapUtil.upload(context, i, order.getOrderid(), 1);
                            }
                        }
                    }
                    if (PullMsgService.CanPrint) {
                        prient("现金支付");
                    } else {
                        try {
                            getParkInfo(order.getCarnumber());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
//                    try {
//                        deleteOrder(order.getOrderid(), order.getTotal(), order.getEnd());
////                        getOrderinfo(true);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

                    break;
                case Constant.BACK_FROM_CAMERA_OUT_CARD:
                    listPath1 = data.getStringArrayListExtra("list");


                    if (CommontUtils.checkList(listPath1)) {
                        for (int i = 0; i < listPath1.size(); i++) {
                            String SDState = Environment.getExternalStorageState();
                            if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                                File dir = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                (new File(listPath1.get(i))).renameTo(new File(dir.getAbsolutePath(), order.getOrderid() + "out" + i + ".jpeg"));
                                CameraBitmapUtil.upload(context, i, order.getOrderid(), 1);
                            }
                        }
                    }
                    if (PullMsgService.CanPrint) {
                        prient("刷卡支付");
                    } else {
                        try {
                            getParkInfo(order.getCarnumber());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
                case Constant.BACK_FROM_CAMERA_ESC:
                    listPath2 = data.getStringArrayListExtra("list");
                    try {
                        MakeLostOrder(order.getOrderid(), order.getTotal());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }

        }
    }

    private ArrayList<String> listPath1 = new ArrayList<String>();
    private ArrayList<String> listPath2 = new ArrayList<String>();
    /**
     * 检查逃单
     * //http://127.0.0.1/zld/collectorrequest.do?
     * action=getecsorder&token=ca67649c7a6c023e08b0357658c08c3d&car_number=
     */
    private ZhuiJiaoListEntity listEntity = new ZhuiJiaoListEntity();
    private ArrayList<ZhuiJiaoItemEntity> entity = new ArrayList<ZhuiJiaoItemEntity>();

    public void getParkInfo(String carnumber) throws UnsupportedEncodingException {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取信息失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }
        String car = URLEncoder.encode(carnumber, "utf-8");
        AQuery aQuery = new AQuery(OrderJieSuanActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=getecsorder&token="
                + token + "&car_number=" + URLEncoder.encode(car, "utf-8") + "&berthid=" +
                SharedPreferencesUtils.getIntance(this).getberthid() + "&out=json";
        MyLog.i("CurrentOrdierDetailsActivity", "检查逃单的URL-->>" + url);
//        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取车场信息数据...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (object != null) {
                    Gson gson = new Gson();
                    listEntity = gson.fromJson(object, ZhuiJiaoListEntity.class);
                    MyLog.i("ParkingInfoActivity-->>", "解析的逃单" + listEntity.toString());
                    if (listEntity.getResult().equals("0")) {
                        //有逃单，跳转到追缴界面
                        if (getBooleanFromPreference("next")) {
                            //如果已标记下次缴费，走正常流程
                            FinishAction();
                        } else {
                            entity = listEntity.getOrders();
                            Intent i = new Intent(context, ZhuiJiaoListActivity.class);
                            putStringToPreference("carnumber", order.getCarnumber());
                            i.putExtra("list", entity);
                            i.putExtra("from", "jiesuan");
                            startActivityForResult(i, Constant.BACK_FROM_OWE);
                        }
                    } else {
                        //没有逃单情况，走正常流程
                        FinishAction();
                    }
                } else {
                    FinishAction();
                }
            }

        });
    }


    // 获取到离场订单消息；
    public void setLeaveOrder(LeaveOrder leaveorder) {
        qrDialog.dismiss();
        if (leaveorder != null) {
            prient("手机支付");
        }
    }

    private Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:// 获取到离场订单
                    Toast.makeText(context, "获取到了结算的订单", Toast.LENGTH_LONG).show();
//                    setLeaveOrder(msg);
                case 11:
                    //dialog中点击了确定
                    String type = (String) msg.obj;
                    photonum1 = SharedPreferencesUtils.getIntance(OrderJieSuanActivity.this).getphotoset(1);
                    photonum2 = SharedPreferencesUtils.getIntance(OrderJieSuanActivity.this).getphotoset(2);
                    if (type.equals("cash")) {
//                        CommontUtils.toast(context,"确定现金支付");
                        //点击结算订单
                        //判断是否能够照相，如果能，则先照相，完成以后再调用生成订单接口
                        if (photonum1 > 0) {
//                            takePhoto(numcount1, 1);
                            Intent i = new Intent(OrderJieSuanActivity.this, CameraActivity.class);
                            i.putExtra("num", photonum1);
                            startActivityForResult(i, Constant.BACK_FROM_CAMERA_OUT);
                        } else {
                            try {
                                deleteOrder(order.getOrderid(), order.getTotal(), order.getEnd());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    } else {
//                        CommontUtils.toast(context,"确定置为未缴");
                        if (photonum2 > 0) {
//                            takePhoto(numcount2, 2);
                            Intent i = new Intent(OrderJieSuanActivity.this, CameraActivity.class);
                            i.putExtra("num", photonum2);
                            startActivityForResult(i, Constant.BACK_FROM_CAMERA_ESC);
                        } else {
                            MakeLostOrder(order.getOrderid(), order.getTotal());
                        }

                    }

                    break;
                case 3:
//                    if (PullMsgService.CanPrint) {
//                        prient("微信支付");
//                    } else {
//                        try {
//                            getParkInfo(order.getCarnumber());
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
//                    }

                    break;
                case Constant.MSG_FOUND_UID:
                    cardDialog.dismiss();
                    String uid = (String) msg.obj;
                    cardnum = uid;
                    photonum1 = SharedPreferencesUtils.getIntance(OrderJieSuanActivity.this).getphotoset(1);
//                    if (photonum1 > 0) {
//                        Intent i = new Intent(OrderJieSuanActivity.this, CameraActivity.class);
//                        i.putExtra("num", photonum1);
//                        startActivityForResult(i, Constant.BACK_FROM_CAMERA_OUT_CARD);
//                    } else {

//                            createOrderForPos("0");
                    PayOrderByCard("0");

//                    }
//                    Toast.makeText(context, "卡号：" + uid, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    private String cardnum = "";

    /**
     * 刷卡结算
     * //http://127.0.0.1/zld/collectorrequest.do?
     * collectorrequest.do?action=ordercard
     * orderid：订单编号
     * total：结算金额
     * imei：设备型号
     * version：版本号
     * uuid：卡片唯一编号
     */
    public void PayOrderByCard(String bindcard) {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取信息失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }

        AQuery aQuery = new AQuery(OrderJieSuanActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=ordercard&token=" + token
                + "&uuid=" + cardnum + "&orderid=" + order.getOrderid()
                + "&version=" + CommontUtils.getVersion(OrderJieSuanActivity.this)
                + "&imei=" + CommontUtils.GetHardWareAddress(OrderJieSuanActivity.this)
                + "&total=" + order.getTotal() + "&out=json" + "&endtime=" + order.getEnd()
                + "&bindcard=" + bindcard;
        MyLog.i("CurrentOrderDetailsActivity", "刷卡结算的URL-->>" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "结算中...", "结算刷卡订单...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                dialog.dismiss();
                if (object != null) {
                    Gson gson = new Gson();
                    BaseResponCard entity = gson.fromJson(object, BaseResponCard.class);
                    MyLog.i("CurrentOrderDetailsActivity", "解析的刷卡结果" + entity.toString());
                    if (entity.getResult().equals("1")) {
                        //结算成功后将车检器订单置 succeed
                        putStringToPreference("berthorderid", "succeed");
                        if (CommontUtils.checkString(entity.getErrmsg())) {
                            CommontUtils.toast(context, entity.getErrmsg() + "");
                        }
                        if (photonum1 > 0) {
                            Intent i = new Intent(OrderJieSuanActivity.this, CameraActivity.class);
                            i.putExtra("num", photonum1);
                            startActivityForResult(i, Constant.BACK_FROM_CAMERA_OUT_CARD);
                        } else {
                            if (PullMsgService.CanPrint) {
                                prient("刷卡支付");
                            } else {
                                try {
                                    getParkInfo(order.getCarnumber());
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else if ("-6".equals(entity.getResult())) {
                        //卡片未激活，需要跳转到开卡界面 opencard
                        new AlertDialog.Builder(OrderJieSuanActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                .setMessage("卡片未绑定，是否现在绑定？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(OrderJieSuanActivity.this, OpenCardActivity.class);
                                i.putExtra("cardid", cardnum);
                                i.putExtra("type", "-6");
                                startActivity(i);
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PayOrderByCard("1");
                            }
                        }).setCancelable(false).create().show();
                    } else if ("-5".equals(entity.getResult())) {
                        //卡片未激活，需要跳转到开卡界面 不激活无法使用
                        new AlertDialog.Builder(OrderJieSuanActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                .setMessage("卡片未激活，无法使用！").setPositiveButton("现在激活", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(OrderJieSuanActivity.this, OpenCardActivity.class);
                                i.putExtra("cardid", cardnum);
                                i.putExtra("type", "-5");
                                startActivity(i);
                            }
                        }).setNegativeButton("取消", null).setCancelable(false).create().show();
                    } else if (entity.getResult().equals("-1")) {
                        Toast.makeText(OrderJieSuanActivity.this, "" + entity.getErrmsg(), Toast.LENGTH_SHORT).show();
                        OrderJieSuanActivity.this.finish();
                    } else {
                        Toast.makeText(context, "" + entity.getErrmsg(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    FinishAction();
                }
            }

        });
    }

}
