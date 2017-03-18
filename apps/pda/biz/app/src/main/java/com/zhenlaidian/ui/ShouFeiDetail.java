package com.zhenlaidian.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.DetailAdapter;
import com.zhenlaidian.bean.BoWeiListEntity;
import com.zhenlaidian.bean.HuizongItem;
import com.zhenlaidian.bean.HuizongItems;
import com.zhenlaidian.bean.HuizongTotal;
import com.zhenlaidian.bean.InVehicleInfo;
import com.zhenlaidian.bean.LogOutEntity;
import com.zhenlaidian.photo.CarOrderActivity;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by xulu on 2016/5/24.
 */
public class ShouFeiDetail extends BaseActivity {

    ListView detaillist;
    DetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_income_alldetail_layout);
        detaillist = ((ListView) findViewById(R.id.list_detail));
        huizongtotal = new HuizongTotal();
        initView();
    }

    private TextView park, berthsec, logintime, nowtime, incar, outcar;
    private TextView paycash, payelec, paycard;
    private TextView paypre, paymine, payesc;
    private TextView upload, printlogout, printdetail;
    private LinearLayout lndetail;
    private TextView payshould;

    private void initView() {

        park = ((TextView) findViewById(R.id.chechang));
        berthsec = ((TextView) findViewById(R.id.berthsec));
        logintime = ((TextView) findViewById(R.id.logintime));
        nowtime = ((TextView) findViewById(R.id.nowtime));
        incar = ((TextView) findViewById(R.id.incar));
        outcar = ((TextView) findViewById(R.id.outcar));
        paycash = ((TextView) findViewById(R.id.moneycash));
        payelec = ((TextView) findViewById(R.id.moneyelec));
        paycard = ((TextView) findViewById(R.id.moneycard));
        paypre = ((TextView) findViewById(R.id.paypre));
        paymine = ((TextView) findViewById(R.id.paymine));
        payesc = ((TextView) findViewById(R.id.payesc));
        payshould = ((TextView) findViewById(R.id.payshould));
        upload = ((TextView) findViewById(R.id.upload));
        printlogout = ((TextView) findViewById(R.id.logout));
        printdetail = ((TextView) findViewById(R.id.printall));
        lndetail = ((LinearLayout) findViewById(R.id.lndetail));
        if (SharedPreferencesUtils.getIntance(ShouFeiDetail.this).gethidedetail().equals("1")) {
//            lndetail.setVisibility(View.GONE);
            detaillist.setVisibility(View.GONE);
            printdetail.setText("显示汇总");
        } else {
//            lndetail.setVisibility(View.VISIBLE);
            detaillist.setVisibility(View.VISIBLE);
            printdetail.setText("打印汇总");
//            setView();
            LogOutDetail("");
        }
        v = LayoutInflater.from(context).inflate(R.layout.x_item_checkpwd, null);
        edtpwd = ((EditText) v.findViewById(R.id.pwd));
//         dialog = new AlertDialog.Builder(ShouFeiDetail.this).create();
        dialog = new AlertDialog.Builder(ShouFeiDetail.this).setTitle("输入密码").setIcon(R.drawable.app_icon_32)
                .setView(v).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (CommontUtils.checkString(edtpwd.getText().toString())) {
                            LogOutDetail(edtpwd.getText().toString());
                        } else {
                            CommontUtils.toast(context, "请输入密码");
                            needagain = true;
                        }
                    }
                }).setCancelable(false).create();
//        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (needagain) {
                    showDialog();
                }
            }
        });
        printdetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (printdetail.getText().toString().equals("显示汇总")) {
                    edtpwd.setText("");
                    dialog.show();
                } else {
//                    if (infodetail != null) {
                    if (huizongtotal != null) {
//                        printDetail(infodetail);
                        printDetail1370(huizongtotal);
                        finish();
                    } else {
                        CommontUtils.toast(context, "暂无数据");
                    }

                }

            }
        });

        //签退需要密码
        printlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInVehicleInfo();
//                if (SharedPreferencesUtils.getIntance(ShouFeiDetail.this).getsignoutvalid().equals("1")) {
//                    edtLogout.setText("");
//                    dialogLogout.show();
//                } else {
//                    new AlertDialog.Builder(ShouFeiDetail.this).setTitle("签退").setIcon(R.drawable.app_icon_32)
//                            .setMessage("确定下班签退").setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            LogOut("");
//                        }
//                    }).create().show();
//
//                }
            }
        });
        v2 = LayoutInflater.from(context).inflate(R.layout.x_item_checkpwd, null);
        edtLogout = ((EditText) v2.findViewById(R.id.pwd));
        dialogLogout = new AlertDialog.Builder(ShouFeiDetail.this).setTitle("输入签退密码").setIcon(R.drawable.app_icon_32)
                .setView(v2).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (CommontUtils.checkString(edtLogout.getText().toString())) {
//                            if (edtLogout.getText().toString().trim().equals("123456")) {
//                                LogOut("");
//                            } else {
//                                CommontUtils.toast(context, "密码错误，请重新输入");
//                                logoutagian = true;
//                            }
                            LogOut(edtLogout.getText().toString());
                        } else {
                            CommontUtils.toast(context, "请输入密码");
                            logoutagian = true;
                        }
                    }
                }).setCancelable(false).create();

        dialogLogout.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (logoutagian) {
                    showDialogOut();
                }
            }
        });
    }

    private boolean needagain = false, logoutagian = false;
    private AlertDialog dialog, dialogLogout;
    private View v2, v;

    private void showDialog() {
        dialog.show();
    }

    private void showDialogOut() {
        dialogLogout.show();
    }

    private EditText edtpwd, edtLogout;

    private void setView(LogOutEntity info) {
//        if (entity != null) {
        park.setText("车场：" + SharedPreferencesUtils.getIntance(context).getParkname());
        berthsec.setText("泊位段：" + SharedPreferencesUtils.getIntance(context).getberthsec_name());
        paycash.setText("现金收费：" + info.getTotal_fee() + "元");
        payelec.setText("电子收费：" + info.getEpay() + "元");
        if (CommontUtils.checkString(info.getCard_pay()))
            paycard.setText("刷卡收费：" + info.getCard_pay() + "元");
        else
            paycard.setText("刷卡收费：0.0元");

//        }
//        double totalesc = new BigDecimal(Double.parseDouble(info.getPursue_cash())+Double.parseDouble(info.getPursue_epay())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        logintime.setText("签到时间：" + info.getOnwork_time());
        nowtime.setText("汇总时间：" + info.getOutwork_time());
        incar.setText("进车：" + info.getIncar());
        outcar.setText("出车：" + info.getOutcar());
        paypre.setText("预付金额：" + info.getPrepay() + "元");
        paymine.setText("垫付金额：" + info.getHistory_prepay() + "元");
        payesc.setText("追缴金额：" + info.getPursue() + "元");
        payshould.setText("订单金额：" + info.getRece_fee() + "元");
        upload.setText(info.getUpmoney() + "元");
    }


    /**
     * http://127.0.0.1/zld/collectorrequest.do?action=workout&token=ca67649c7a6c023e08b0357658c08c3d
     * &berthid=6&workid=
     * 签退
     * from 0 签退 1收费汇总
     * 返回 -2 密码错误 -1 系统错误
     */
    private LogOutEntity infos;

    public void LogOut(String pwd) {
        String path = baseurl;
        String url = path + "collectorrequest.do?action=workout&token=" + token +
                "&berthid=" + SharedPreferencesUtils.getIntance(ShouFeiDetail.this).getberthid() +
                "&workid=" + SharedPreferencesUtils.getIntance(ShouFeiDetail.this).getworkid() +
                "&from=0&collpwd=" + pwd + "&version=" + CommontUtils.getVersion(ShouFeiDetail.this)
                + "&version=" + CommontUtils.getVersion(context);
        MyLog.w("OneKeyQueryActivity", "签退URL--->" + url);
        AQuery aQuery = new AQuery(this);
        final ProgressDialog dialog = ProgressDialog.show(this, "下班签退...", "签退中...", true, true);
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                // TODO Auto-generated method stub
                super.callback(url, object, status);
                dialog.dismiss();
                if (object != null && object != "") {
                    MyLog.v("OneKeyQueryActivity", "签退结果是--->" + object);
                    Gson gson = new Gson();
//                    LogOutEntity info = gson.fromJson(object, LogOutEntity.class);
//                    infos = info;
                    huizongtotal = TransformInfo(object);
//                    if (info.getResult().equals("1")) {
                    if (huizongtotal.getResult().equals("1")) {
                        CommontUtils.toast(context, "签退成功！");
                        //打印小票，回到登陆界面
                        if (PullMsgService.CanPrint) {
                            printandclose();
                        } else {
                            try {
                                CommontUtils.deleteFile(CommontUtils.createSDFile(context));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            FinishAction();
                        }
//                    } else if (info.getResult().equals("-2")) {
                    } else if (huizongtotal.getResult().equals("-2")) {
                        CommontUtils.toast(context, "密码错误");
                    } else {
                        CommontUtils.toast(context, "签退失败");
                    }
                } else {
                    CommontUtils.toast(context, "签退失败！");
                }
            }
        });
    }


    /**
     * http://127.0.0.1/zld/collectorrequest.do?action=workout&token=ca67649c7a6c023e08b0357658c08c3d
     * &berthid=6&workid=
     * 签退
     * from 0 签退 1收费汇总
     * 返回 -2 密码错误 -1 系统错误
     */
//    private LogOutEntity infodetail;
    private HuizongTotal huizongtotal;

    public void LogOutDetail(String pwd) {
        needagain = false;
        String path = baseurl;
        String url = path + "collectorrequest.do?action=workout&token=" + token +
                "&berthid=" + SharedPreferencesUtils.getIntance(ShouFeiDetail.this).getberthid() +
                "&workid=" + SharedPreferencesUtils.getIntance(ShouFeiDetail.this).getworkid() +
                "&from=1&collpwd=" + pwd + "&version=" + CommontUtils.getVersion(ShouFeiDetail.this)
                + "&version=" + CommontUtils.getVersion(context);
        ;
        MyLog.w("ShouFeiDetail", "获取detailURL--->" + url);
        AQuery aQuery = new AQuery(this);
        final ProgressDialog dialog = ProgressDialog.show(this, "获取数据...", "获取中...", true, true);
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                // TODO Auto-generated method stub
                super.callback(url, object, status);
                dialog.dismiss();
                if (object != null && object != "") {
                    needagain = false;
                    MyLog.v("ShouFeiDetail", "获取detail结果是--->" + object);
//                    Gson gson = new Gson();
//                    LogOutEntity info = gson.fromJson(object, LogOutEntity.class);
//                    infodetail = info;

                    huizongtotal = TransformInfo(object);
                    adapter = new DetailAdapter(huizongtotal.getInfos(), context);
                    detaillist.setAdapter(adapter);

//                    if (info.getResult().equals("1")) {
                    if (huizongtotal.getResult().equals("1")) {
                        //设置界面
//                        setView(infodetail);
//                        lndetail.setVisibility(View.VISIBLE);
                        detaillist.setVisibility(View.VISIBLE);
                        printdetail.setText("打印汇总");
//                    } else if (info.getResult().equals("-2")) {
                    } else if (huizongtotal.getResult().equals("-2")) {
                        CommontUtils.toast(context, "密码错误");
                        needagain = true;
                    } else {
                        CommontUtils.toast(context, "获取失败！");
                    }
                } else {
                    needagain = false;
                    CommontUtils.toast(context, "获取失败！");
                }
            }
        });
    }

    private void printandclose() {
//        printLogout(infos);
        printLogout1370(huizongtotal);
        FinishAction();
    }

    private void FinishAction() {
        setResult(1234);
        finish();
    }
    //打印离岗凭条
    public void printLogout1370(HuizongTotal huizongtotal) {
        ArrayList<HuizongItems> infos = huizongtotal.getInfos();
        String Sname="";
        String gang = "";
        if (SharedPreferencesUtils.getIntance(this).getisprintName()) {
            Sname = getStringFromPreference("name");
            gang = "-";
        }
        String str = "**********下班离岗**********\n\n" +
                SharedPreferencesUtils.getIntance(context).getParkname() + "\n" +
                "收费员：" + Sname;
        if (CommontUtils.Is910()) {
            if (!TextUtils.isEmpty(SharedPreferencesUtils.getIntance(context).getmobile())) {
                str += "(" + SharedPreferencesUtils.getIntance(context).getmobile() + ")";
            }
        }
        str += gang + useraccount;

        str += "\n" +
                "停车场：" + SharedPreferencesUtils.getIntance(context).getParkname() +
                "—" + SharedPreferencesUtils.getIntance(context).getberthsec_name() + "\n" +
                "终端编号：" + CommontUtils.GetHardWareAddress(context) + "\n";
        if (infos != null && infos.size() > 0) {
            for (int i = 0; i < infos.size(); i++) {
                HuizongItems items = infos.get(i);
                str += items.getName() + " " + (TextUtils.isEmpty(items.getValue()) ? "" : items.getValue()) + "\n";
                ArrayList<HuizongItem> content = items.getContent();
                if (content != null && content.size() > 0) {
                    for (int j = 0; j < content.size(); j++) {
                        HuizongItem item = content.get(j);
                        str += "    " + item.getName() + " " + (TextUtils.isEmpty(item.getValue()) ? "" : item.getValue()) + "\n";
                    }
                }
            }

        }
        str += "\n*******************************\n\n\n\n\n";
        PullMsgService.sendMessage(str, context);
//        sendMessage(qrbitmap);
    }

    //打印汇总
    public void printDetail1370(HuizongTotal huizongtotal) {
        ArrayList<HuizongItems> infos = huizongtotal.getInfos();
        String Sname="";
        String gang = "";
        if (SharedPreferencesUtils.getIntance(this).getisprintName()) {
            Sname = getStringFromPreference("name");
            gang = "-";
        }
        String str = "**********收费汇总**********\n\n" +
                SharedPreferencesUtils.getIntance(context).getParkname() + "\n" +
                "收费员：" + Sname;
        if (CommontUtils.Is910()) {
            if (!TextUtils.isEmpty(SharedPreferencesUtils.getIntance(context).getmobile())) {
                str += "(" + SharedPreferencesUtils.getIntance(context).getmobile() + ")";
            }
        }
        str += gang + useraccount;

        str += "\n" +
                "停车场：" + SharedPreferencesUtils.getIntance(context).getParkname() +
                "—" + SharedPreferencesUtils.getIntance(context).getberthsec_name() + "\n";
        if (infos != null && infos.size() > 0) {
            for (int i = 0; i < infos.size(); i++) {
                HuizongItems items = infos.get(i);
                str += items.getName() + " " + (TextUtils.isEmpty(items.getValue()) ? "" : items.getValue()) + "\n";
                ArrayList<HuizongItem> content = items.getContent();
                if (content != null && content.size() > 0) {
                    for (int j = 0; j < content.size(); j++) {
                        HuizongItem item = content.get(j);
                        str += "    " + item.getName() + " " + (TextUtils.isEmpty(item.getValue()) ? "" : item.getValue()) + "\n";
                    }
                }
            }

        }
        str += "\n*******************************\n\n\n\n\n";
        PullMsgService.sendMessage(str, context);
//        sendMessage(qrbitmap);
    }

    //    String teststring = "{\"errmsg\":\"\",\"infos\":[{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"签到时间:\",\"value\":\"16-10-24 14:54\"},{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"汇总时间:\",\"value\":\"16-10-24 17:07\"},{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"进场车辆\",\"value\":0},{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"出场车辆\",\"value\":0},{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"停车费-现金支付\",\"value\":[{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"普通订单\",\"value\":2},{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"追缴订单\",\"value\":0}]},{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"停车费-电子支付\",\"value\":[{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"普通订单\",\"value\":0},{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"追缴订单\",\"value\":0}]},{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"停车费-卡片支付\",\"value\":[{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"普通订单\",\"value\":4},{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"追缴订单\",\"value\":0}]},{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"卡片\",\"value\":[{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"现金充值\",\"value\":0},{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"售卡数量\",\"value\":0},{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"售卡总面值\",\"value\":0}]},{\"fontColor\":\"#000000\",\"fontSize\":14,\"name\":\"上缴金额\",\"value\":2}],\"result\":1}";
    private HuizongTotal TransformInfo(String str) {
        HuizongTotal huizongtotal = new HuizongTotal();
        try {
            JSONObject jo = new JSONObject(str);
            String errmsg = jo.getString("errmsg");
            String result = jo.getString("result");
            huizongtotal.setErrmsg(errmsg);
            huizongtotal.setResult(result);
            JSONArray ja = jo.getJSONArray("infos");
            if (ja != null && ja.length() > 0) {
                ArrayList<HuizongItems> infos = new ArrayList<>();
                for (int i = 0; i < ja.length(); i++) {
                    HuizongItems items = new HuizongItems();
                    JSONObject ob = new JSONObject(ja.get(i).toString());
                    String fontColor = ob.getString("fontColor");
                    String fontSize = ob.getString("fontSize");
                    String name = ob.getString("name");
                    items.setFontColor(fontColor);
                    items.setFontSize(fontSize);
                    items.setName(name);
                    Object obj = ob.get("value");
                    if (obj instanceof JSONArray) {
                        JSONArray jvalue = ob.getJSONArray("value");
                        ArrayList<HuizongItem> content = new ArrayList<>();
                        for (int j = 0; j < jvalue.length(); j++) {
                            HuizongItem item = new HuizongItem();
                            JSONObject objc = new JSONObject(jvalue.get(j).toString());
                            String fontColorss = objc.getString("fontColor");
                            String fontSizess = objc.getString("fontSize");
                            String namess = objc.getString("name");
                            String value = objc.getString("value");
                            item.setFontColor(fontColorss);
                            item.setFontSize(fontSizess);
                            item.setName(namess);
                            item.setValue(value);
                            content.add(item);
                        }
                        items.setContent(content);
                    } else {
                        String value = obj.toString();
                        items.setValue(value);
                    }
                    infos.add(items);

                }
                huizongtotal.setInfos(infos);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return huizongtotal;
    }

    public ArrayList<InVehicleInfo> INFO;

    public void getInVehicleInfo() {
        if (CommontUtils.checkString(SharedPreferencesUtils.getIntance(this).getberthid()) && !SharedPreferencesUtils.getIntance(this).getberthid().equals("-1")) {
            if (!IsNetWork.IsHaveInternet(ShouFeiDetail.this)) {
                Toast.makeText(ShouFeiDetail.this, "请检查网络", Toast.LENGTH_SHORT).show();
                return;
            }
            String url = BaseActivity.baseurl + "collectorrequest.do?action=getberths&out=josn&token=" + BaseActivity.token +
                    "&berthid=" + SharedPreferencesUtils.getIntance(this).getberthid() +
                    "&devicecode=" + CommontUtils.GetHardWareAddress(this);
            MyLog.i("InTheVehicleActivity", "获取在场车辆的URL--->" + url);
            AQuery aQuery = new AQuery(this);
            final ProgressDialog dialog = ProgressDialog.show(this, "获取在场车辆数据", "获取中...", true, true);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    dialog.dismiss();
                    if (!TextUtils.isEmpty(object)) {
                        MyLog.i("InTheVehicleActivity", "获取在场车辆结果--->" + object);
                        Gson gson = new Gson();
                        BoWeiListEntity Data = gson.fromJson(object, new TypeToken<BoWeiListEntity>() {
                        }.getType());
                        if (CommontUtils.checkString(Data.getState())) {
                            if (Integer.parseInt(Data.getState()) != 1) {
                                //非正常状态
                            } else {
                                INFO = Data.getData();
                                int count = 0;
                                if (CommontUtils.checkList(INFO)) {
                                    for (InVehicleInfo info : INFO) {
                                        if (!TextUtils.isEmpty(info.getCar_number()) && !TextUtils.isEmpty(info.getOrderid()))
                                            count++;
                                    }
                                    if (count > 0) {
                                        new AlertDialog.Builder(ShouFeiDetail.this).setIcon(R.drawable.app_icon_32)
                                                .setTitle("提示").setMessage("还有车辆在场，是否签退？")
                                                .setPositiveButton("去结算", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Intent intent = new Intent(ShouFeiDetail.this, CarOrderActivity.class);
                                                        intent.putExtra("infos", INFO);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }).setNegativeButton("直接签退", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (SharedPreferencesUtils.getIntance(ShouFeiDetail.this).getsignoutvalid().equals("1")) {
                                                    edtLogout.setText("");
                                                    dialogLogout.show();
                                                } else {
                                                    new AlertDialog.Builder(ShouFeiDetail.this).setTitle("签退").setIcon(R.drawable.app_icon_32)
                                                            .setMessage("确定下班签退").setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            LogOut("");
                                                        }
                                                    }).create().show();

                                                }
                                            }
                                        }).create().show();

                                    } else {
                                        if (SharedPreferencesUtils.getIntance(ShouFeiDetail.this).getsignoutvalid().equals("1")) {
                                            edtLogout.setText("");
                                            dialogLogout.show();
                                        } else {
                                            new AlertDialog.Builder(ShouFeiDetail.this).setTitle("签退").setIcon(R.drawable.app_icon_32)
                                                    .setMessage("确定下班签退").setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    LogOut("");
                                                }
                                            }).create().show();

                                        }
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(ShouFeiDetail.this, "获取数据出错！", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
    }

}
