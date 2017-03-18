package com.zhenlaidian.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.ZhuiJiaoOrderAdapter;
import com.zhenlaidian.bean.BaseResponse;
import com.zhenlaidian.bean.ZhuiJiaoItemEntity;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.ui.dialog.CommonMsgDialog;
import com.zhenlaidian.ui.dialog.PayCardDialog;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.Constant;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.ReadCardUtil;
import com.zhenlaidian.util.SharedPreferencesUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by TCB on 2016/4/17.
 */
public class ZhuiJiaoOrderActivity extends BaseActivity implements View.OnClickListener {

    private ArrayList<ZhuiJiaoItemEntity> entity = new ArrayList<ZhuiJiaoItemEntity>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_zhuijiao_order_layout);
        entity = (ArrayList<ZhuiJiaoItemEntity>) getIntent().getSerializableExtra("list");
        ReadCardUtil.InitReader();
        initView();
    }

    private ZhuiJiaoOrderAdapter adapter;
    private ListView listview;
    private TextView txtOrdernum, txtOrderTotal, txtYijiao, txtQianfei;
    private TextView txtCash;
    private CheckBox cb_checkall;
    private void initView() {
        txtOrdernum = ((TextView) findViewById(R.id.zhuijiao_ordernum));
        txtOrderTotal = ((TextView) findViewById(R.id.zhuijiao_ordernumall));
        txtYijiao = ((TextView) findViewById(R.id.zhuijiao_yijiao));
        txtQianfei = ((TextView) findViewById(R.id.zhuijiao_qianfei));
        cb_checkall = ((CheckBox) findViewById(R.id.cb_checkall));

        listview = ((ListView) findViewById(R.id.zhuijiao_list_orders));
        adapter = new ZhuiJiaoOrderAdapter(context, entity);
        listview.setAdapter(adapter);

        txtCash = (TextView) findViewById(R.id.moneycash);
        txtCash.setOnClickListener(this);
        findViewById(R.id.moneycard).setOnClickListener(this);
        findViewById(R.id.moneyqrcode).setOnClickListener(this);
        countMoney();
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (entity.get(position).ischeck()) {
                    entity.get(position).setIscheck(false);
                } else {
                    entity.get(position).setIscheck(true);
                }

                adapter.notifyDataSetChanged();
                countMoney();
            }
        });
        cardDialog = new PayCardDialog(this, "提示", h);
        cardDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isread = true;
            }
        });

        cb_checkall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if (CommontUtils.checkList(entity)) {
                        for (int i = 0; i < entity.size(); i++) {
                            entity.get(i).setIscheck(true);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    countMoney();
                    cb_checkall.setText("全不选");
                }else{
                    if (CommontUtils.checkList(entity)) {
                        for (int i = 0; i < entity.size(); i++) {
                            entity.get(i).setIscheck(false);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    countMoney();
                    cb_checkall.setText("全选");
                }
            }
        });
    }

    private double nowOwe;

    private void countMoney() {
        int ordernum = 0;
        double total = 0;
        double prepay = 0;
        double owen = 0;
        for (int i = 0; i < entity.size(); i++) {
            if (entity.get(i).ischeck()) {
                ordernum++;
                total += Double.parseDouble(entity.get(i).getTotal());
                prepay += Double.parseDouble(entity.get(i).getPrepay());
//                owen+=Double.parseDouble(entity.get(i).getTotal());
            }
        }
//        private TextView txtOrdernum, txtOrderTotal, txtYijiao, txtQianfei;
        txtOrdernum.setText("订单数：" + ordernum);
        txtOrderTotal.setText("订单总额：" + CommontUtils.doubleTwoPoint(total) + "元");
        txtYijiao.setText("已缴：" + CommontUtils.doubleTwoPoint(prepay) + "元");
        txtQianfei.setText("欠费：" + CommontUtils.doubleTwoPoint(total - prepay) + "元");
        nowOwe = total - prepay;
    }

    PayCardDialog cardDialog;
    Thread cardthread;
    boolean isread = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.moneycash:
//                startActivity(new Intent(context, ZhuiJiaoOrderActivity.class));
                list = new ArrayList<ZhuiJiaoItemEntity>();
                for (int i = 0; i < entity.size(); i++) {
                    if (entity.get(i).ischeck()) {
                        list.add(entity.get(i));
                    }
                }
                if (CommontUtils.checkList(list)) {
                    CommonMsgDialog cashDialog = new CommonMsgDialog(this, "订单提醒", "确认现金结算", h, "cash");
                    cashDialog.show();
                } else {
                    CommontUtils.toast(context, "请选中至少一条记录");
                }


                break;
            case R.id.moneycard:
                ReadCardUtil.StartReadCard(cardDialog,h);
                list = new ArrayList<ZhuiJiaoItemEntity>();
                for (int i = 0; i < entity.size(); i++) {
                    if (entity.get(i).ischeck()) {
                        list.add(entity.get(i));
                    }
                }
                if (cout <= 0) {
                    try {
                        JiesuanZhuijiao("0", "1");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.moneyqrcode:

                break;
            case R.id.escape:

                break;
        }
    }

    private int cout = 0;

//    private void StartReadCard() {
//        if (CommontUtils.Is910()) {
//            cardDialog.show();
//            isread = false;
//            piccReader.open();
//            cardthread = new Thread() {
//                @Override
//                public void run() {
//                    super.run();
//                    while (!isread) {
//                        byte CardType[] = new byte[2];
//                        byte Atq[] = new byte[14];
//                        char SAK = 1;
//                        byte sak[] = new byte[1];
//                        sak[0] = (byte) SAK;
//                        byte SN[] = new byte[10];
//                        int scan_card = piccReader.request(CardType, Atq);
//                        if (scan_card > 0) {
//                            int SNLen = piccReader.antisel(SN, sak);
//                            Message msg = h.obtainMessage(Constant.MSG_FOUND_UID);
//                            msg.obj = CommontUtils.bytesToHexString(SN, SNLen);
//                            h.sendMessage(msg);
//                            isread = true;
//                        }
//                        try {
//                            Thread.sleep(300);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            };
//            cardthread.start();
//        }
//    }

    ArrayList<ZhuiJiaoItemEntity> list;
    private android.os.Handler h = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 11:
                    //dialog中点击了确定
                    String type = (String) msg.obj;
                    try {
                        JiesuanZhuijiao("0", "0");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constant.MSG_FOUND_UID:
                    cardDialog.dismiss();
                    String uid = (String) msg.obj;
                    list = new ArrayList<ZhuiJiaoItemEntity>();
                    for (int i = 0; i < entity.size(); i++) {
                        if (entity.get(i).ischeck()) {
                            list.add(entity.get(i));
                        }
                    }
                    if (CommontUtils.checkList(list)) {
                        cardnum = uid;
                        try {
                            JiesuanZhuijiao("0", "1");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        CommontUtils.toast(context, "请选中至少一条记录");
                    }
                    break;
                case 1222:
                    PullMsgService.sendMessage((String)msg.obj, context);
                    FinishAction();
                    break;
            }
        }
    };

    String cardnum = "";

    /**
     * //http://127.0.0.1/zld/collectorrequest.do?
     * action=payescorder&token=53aa954da7de01e1e7439fb386c41234&orderlist=
     * 追缴提交结算
     * 刷卡结算新增的参数
     * bindcard    0 提示激活 1：不提示激活直接刷卡
     * paytype 0默认现金 1刷卡
     * uuid
     */
    public void JiesuanZhuijiao(String bindcard, String paytype) throws UnsupportedEncodingException {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取信息失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<ZhuiJiaoItemEntity>>() {
        }.getType(); // 指定集合对象属性
        String json = gson.toJson(list, type);
        String jsonturn = URLEncoder.encode(json, "utf-8");
        AQuery aQuery = new AQuery(ZhuiJiaoOrderActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=payescorder&token="
                + token + "&orderlist=" + URLEncoder.encode(jsonturn, "utf-8")
                + "&bid=" + getStringFromPreference("bowei")
                + "&workid=" + SharedPreferencesUtils.getIntance(this).getworkid()
                + "&bindcard=" + bindcard + "&paytype=" + paytype + "&uuid=" + cardnum
                + "&version=" + CommontUtils.getVersion(context) + "&out=json";
        MyLog.w("InputCarNumberActivity-->>", "提交缴费的URL-->>" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "结算中...", "结算未缴数据...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                dialog.dismiss();
                if (object != null) {
                    Gson g = new Gson();
                    BaseResponse response = g.fromJson(object, BaseResponse.class);

                    MyLog.i("ParkingInfoActivity-->>", "提交缴费" + object);
                    if (response.getResult().equals("1")) {
                        txtCash.setOnClickListener(null);
                        new AlertDialog.Builder(ZhuiJiaoOrderActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                .setMessage(CommontUtils.checkString(response.getErrmsg()) ? response.getErrmsg() : "打印小票").
                                setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (PullMsgService.CanPrint) {
                                            print();
                                        } else {
                                            FinishAction();
                                        }
                                    }
                                }).setCancelable(false).create().show();
                    } else if ("-6".equals(response.getResult())) {
                        //卡片未激活，需要跳转到开卡界面 opencard
                        new AlertDialog.Builder(ZhuiJiaoOrderActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                .setMessage("卡片未绑定，是否现在绑定？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(ZhuiJiaoOrderActivity.this, OpenCardActivity.class);
                                i.putExtra("cardid", cardnum);
                                i.putExtra("type", "-6");
                                startActivity(i);
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    JiesuanZhuijiao("1", "1");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).setCancelable(false).create().show();
                    } else if ("-5".equals(response.getResult())) {
                        //卡片未激活，需要跳转到开卡界面 不激活无法使用
                        new AlertDialog.Builder(ZhuiJiaoOrderActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                .setMessage("卡片未激活，无法使用！").setPositiveButton("现在激活", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(ZhuiJiaoOrderActivity.this, OpenCardActivity.class);
                                i.putExtra("cardid", cardnum);
                                i.putExtra("type", "-5");
                                startActivity(i);
                            }
                        }).setNegativeButton("取消", null).setCancelable(false).create().show();
                    } else {
                        //没有逃单情况，走正常流程
                        if (cout > 0 || response.getErrmsg().contains("余额不足")) {
                            if (response.getErrmsg().contains("余额不足")){
                                if (cardDialog.isShowing()) {
                                    cardDialog.dismiss();
                                }
                                cout--;
                            }

                            Toast.makeText(context, response.getErrmsg(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    CommontUtils.toast(context, "结算失败");
                }
                cout++;
            }

        });
    }


    private void print() {
//        printact();
        String url = baseurl + "collectorrequest.do?action=getservertime&token=" + token;
        AQuery aQuery = new AQuery(ZhuiJiaoOrderActivity.this);
        final ProgressDialog dialog = ProgressDialog.show(ZhuiJiaoOrderActivity.this, "", "处理中...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                // TODO Auto-generated method stub
                super.callback(url, object, status);
                dialog.dismiss();
                if (object != null && object != "") {
                    printact(object);
                } else {
                    printact(System.currentTimeMillis() + "");
                }
            }
        });
    }
    private void printact(String mili){

        /**
         * 序号 1
         欠费时间     2016-04-28 15:23
         停车时长   34分钟
         金额    4元
         */
        double total = 0;
        double pre = 0;
        String printstr = Constant.OWEHead;
        printstr += "车牌号：" + entity.get(0).getCar_number() + "\n";
        for (int i = 0; i < entity.size(); i++) {
            if(entity.get(i).ischeck()){
                //只打印已缴
                Double d = Double.parseDouble(entity.get(i).getTotal()) - Double.parseDouble(entity.get(i).getPrepay());
                BigDecimal b = new BigDecimal(d);
                double owemoney = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                printstr += "序号：" + (i + 1) + "\n" +
                        "欠费泊位段：" + entity.get(i).getBerthsec_name() + "\n" +
                        "进场时间：" + CommontUtils.Unix2TimeS(entity.get(i).getStart()) + "\n" +
                        "出场时间：" + CommontUtils.Unix2TimeS(entity.get(i).getEnd()) + "\n" +
                        "停车时长：" + entity.get(i).getDuartion() + "\n" +
                        "欠费金额：" + owemoney + "\n" +
                        "状态：" + (entity.get(i).ischeck() ? "已缴" : "未缴") +
                        "\n";
            }
            total += Double.parseDouble(entity.get(i).getTotal());
            pre += Double.parseDouble(entity.get(i).getPrepay());
        }

        printstr +=
                "补缴地点：" + SharedPreferencesUtils.getIntance(context).getberthsec_name() + "\n" +
                        "补缴时间：" + CommontUtils.Mili2TimeMin(mili) + "\n" +
                        "实收金额：" + CommontUtils.doubleTwoPoint(nowOwe) + "\n" +
                        "剩余欠费：" + CommontUtils.doubleTwoPoint(total - pre - nowOwe) + "\n" +
                        Constant.FOOT + "\n" + "\n" + "\n";
        Message m = new Message();
        m.what = 1222;
        m.obj = printstr;
        h.sendMessage(m);
    }


    private void FinishAction() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ReadCardUtil.StopReading();
    }
}