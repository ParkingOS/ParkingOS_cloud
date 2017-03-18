package com.zhenlaidian.engine;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.bean.MainUiInfo;
import com.zhenlaidian.bean.NfcDirectCashOrderDialog;
import com.zhenlaidian.bean.NfcNewOrderDialog;
import com.zhenlaidian.bean.NfcNewOrderToQiaoDialog;
import com.zhenlaidian.bean.NfcOrder;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.ui.LostOrderRecordActivity;
import com.zhenlaidian.util.MyLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ShowNfcNewOrder {
    private LeaveActivity activity;
    public String uuid;//
    public String comid;// 车场编号
    public String carnumber;// 车牌号
    public String lostwarn;// 逃单提示
    private String imei;
    private String type;// 3土桥专用 0生成订单 -2逃单
    NfcNewOrderToQiaoDialog tuqiao_dialog;
    NfcNewOrderDialog nfc_dialog;
    private AlertDialog lostdialog;
    private NfcOrder nfcOrder;// 刷一次就结算；
    private NfcDirectCashOrderDialog direct_cash_dialog;
    static ShowNfcNewOrder instance;

    private ShowNfcNewOrder() {
        super();
    }

    public static ShowNfcNewOrder getInstance() {
        if (instance == null) {
            instance = new ShowNfcNewOrder();
        }
        return instance;
    }

    public void setContext(LeaveActivity conn) {
        this.activity = conn;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setComid(String comid) {
        this.comid = comid;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public void setCarnumber(String carnumber) {
        this.carnumber = carnumber;
    }

    public void setNfcOrder(NfcOrder nfcOrder) {
        this.nfcOrder = nfcOrder;
    }

    public void setLostwarn(String lostwarn) {
        this.lostwarn = lostwarn;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void showNfcOrderDialog() {
        if ("3".equals(type)) {// 土桥专用生成订单对话框
            if (tuqiao_dialog == null || !tuqiao_dialog.isShowing()) {
                tuqiao_dialog = new NfcNewOrderToQiaoDialog(activity, R.style.nfcnewdialog, this);
                tuqiao_dialog.setCanceledOnTouchOutside(false);
                MyLog.i("ShowNfcNewOrder", "土桥生成订单：" + activity.isFinishing());
                if (!activity.isFinishing()) {
                    tuqiao_dialog.show();
                }
            } else {
                if (activity != null && !activity.isFinishing()) {
                    noFinishOrder();
                }
            }
        } else if ("0".equals(type)) {// 正常生成订单对话框；
            if (nfc_dialog == null || !nfc_dialog.isShowing()) {
                nfc_dialog = new NfcNewOrderDialog(activity, R.style.nfcnewdialog, this);
                nfc_dialog.setCanceledOnTouchOutside(false);
                MyLog.i("ShowNfcNewOrder", "生成订单：" + activity.isFinishing());
                if (!activity.isFinishing()) {
                    nfc_dialog.show();
                }
            } else {
                if (activity != null && !activity.isFinishing()) {
                    noFinishOrder();
                }
            }
        } else if ("-2".equals(type)) {// 逃单提示框；
            if (lostdialog == null || !lostdialog.isShowing()) {
                if (nfc_dialog != null && nfc_dialog.isShowing()) {
                    if (activity != null && !activity.isFinishing()) {
                        noFinishOrder();
                    }
                } else if (tuqiao_dialog != null && tuqiao_dialog.isShowing()) {
                    if (activity != null && !activity.isFinishing()) {
                        noFinishOrder();
                    }
                } else if (direct_cash_dialog != null && direct_cash_dialog.isShowing()) {
                    if (activity != null && !activity.isFinishing()) {
                        noFinishOrder();
                    }
                } else {
                    lostdialog = setlostdialog(lostwarn, carnumber, uuid);
                    MyLog.i("ShowNfcNewOrder", "逃单提示：" + activity.isFinishing());
                    if (!activity.isFinishing()) {
                        lostdialog.show();
                    }
                }
            } else {
                if (activity != null && !activity.isFinishing()) {
                    noFinishOrder();
                }
            }
        } else if ("2".equals(type)) {// 按次结算，刷一次直接结算；
            if (direct_cash_dialog == null || !direct_cash_dialog.isShowing()) {
                direct_cash_dialog = new NfcDirectCashOrderDialog(activity, R.style.nfcnewdialog, this, nfcOrder);
                direct_cash_dialog.setCanceledOnTouchOutside(false);
                MyLog.i("ShowNfcFinishOrder", "按次结算，刷一次直接结算" + activity.toString() + "---" + activity.isFinishing());
                if (activity != null && !activity.isFinishing()) {
                    direct_cash_dialog.show();
                }
            } else {
                if (activity != null && !activity.isFinishing()) {
                    noFinishOrder();
                }
            }
        }
    }

    // 把确认生成订单提交给服务器；返回1提交成功；———土桥专用加参数：&ctype=结算方式；返回结果1成功；-1存在重复订单；-2刚结算就入场；
    // http://192.168.1.102/zld/nfchandle.do?action=addorder&uuid=0458F902422D80&comid=3
    // &ctype=
    public void submitOrder(final boolean isopen, final Dialog dialog, String ctype) {
        SharedPreferences pfs = activity.getSharedPreferences("autologin", Context.MODE_PRIVATE);
        String uid = pfs.getString("account", "");
        String path = Config.getUrl(activity);
        String url;
        if (ctype.equals("")) {
            url = path + "nfchandle.do?action=addorder&uuid=" + uuid + "&comid=" + comid + "&uid=" + uid + "&imei=" + imei;
            MyLog.i("ShowNfcOrder", "确认生成NFC订单的URL--->" + url);
        } else {
            url = path + "nfchandle.do?action=addorder&uuid=" + uuid + "&comid=" + comid + "&uid=" + uid + "&imei=" + imei
                    + "&ctype=" + ctype;
            MyLog.i("ShowNfcOrder", "土桥专用确认生成NFC订单的URL--->" + url);
        }
        AQuery aQuery = new AQuery(activity);
        final ProgressDialog pd = ProgressDialog.show(activity, "生成订单...", "订单生成中...", true, true);
        pd.setCanceledOnTouchOutside(false);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && object != null) {
                    pd.dismiss();
                    MyLog.i("ShowNfcOrder", "订单生成结果--->" + object);
                    if (object.equals("1")) {
                        dialog.dismiss();
                        // activity.setNfcChenge("new");
                        activity.showToast("生成订单成功");
                        Message msg = new Message();
                        msg.what = 2;
                        msg.obj = new MainUiInfo(true, 4, 1.00);
                        LeaveActivity.handler.sendMessage(msg);
                        activity.refreshOrderFragment();
                    } else if (object.equals("-1")) {
                        dialog.dismiss();
                        showErrorDialog("-1", "");
                    } else if (object.equals("-2")) {
                        dialog.dismiss();
                        showErrorDialog("-4", "");
                    } else {
                        showErrorDialog("-3", "");
                        dialog.dismiss();
                    }
                } else {
                    pd.dismiss();
                    if (status.getCode() == -101) {
                        Toast.makeText(activity, "网络错误！--请再次确认生成订单！", 0).show();
                        dialog.dismiss();
                    } else if (status.getCode() == 500) {
                        Toast.makeText(activity, "服务器错误！--请再次确认生成订单！", 0).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(activity, "网络请求错误!--请再次确认生成订单！", 0).show();
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    public AlertDialog setlostdialog(String warn, final String carnumber, final String uuid) {
        AlertDialog.Builder builder = new Builder(activity);
        builder.setIcon(R.drawable.app_icon_32);
        builder.setTitle("订单尚未生成");
        builder.setMessage(warn);
        builder.setCancelable(false);
        builder.setPositiveButton("查看", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(activity, LostOrderRecordActivity.class);
                intent.putExtra("carnumber", carnumber);
                activity.startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("继续生成订单", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (activity != null) {
                    activity.getNfcInfo(uuid, "1");
                    dialog.dismiss();
                }
            }
        });
        return builder.create();
    }

    public void noFinishOrder() {
        Message msg = new Message();
        msg.what = 7;
        LeaveActivity.handler.sendMessage(msg);
    }

    // 按次结算把确认结算订单提交给服务器；返回1提交成功；
    // nfchandle.do?action=completeorder&orderid=78&collect=20&comid=3&carnumber=&uid=&uin=&uuid=
    // 说明：orderid为空时，uin和uuid，carnumber这三个参数不能为空,用于生成订单
    // 返回：1成功，2:车主余额不足，3: 速通卡用户没有设置自动支付 , -2：相同的车牌已在本车场存在订单 ,其它失败
    public void onceCashOrder(final String collect, final Dialog dialog, final String car_number) {
        SharedPreferences pfs = activity.getSharedPreferences("autologin", Context.MODE_PRIVATE);
        String uid = pfs.getString("account", "");
        String path = Config.getUrl(activity);
        String carnumber = "";
        try {
            carnumber = URLEncoder.encode(URLEncoder.encode(car_number, "utf-8"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(activity, "车牌号转码异常", 0).show();
        }
        String url = path + "nfchandle.do?action=completeorder&orderid=" + nfcOrder.getOrderid() + "&collect=" + collect
                + "&comid=" + comid + "&uid=" + uid + "&imei=" + imei + "&carnumber=" + carnumber + "&uuid=" + nfcOrder.getUuid()
                + "&uin=" + nfcOrder.getUin();
        MyLog.i("ShowNfcOrder", "确认结算NFC订单的URL--->" + url);
        AQuery aQuery = new AQuery(activity);
        final ProgressDialog pd = ProgressDialog.show(activity, "结算中...", "正在提交结算数据...", true, true);
        pd.setCanceledOnTouchOutside(false);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && object != null) {
                    pd.dismiss();
                    MyLog.i("ShowNfcOrder", "订单结算结果--->" + object);
                    if ("1".equals(object) || "2".equals(object) || "3".equals(object) || "4".equals(object)
                            || "5".equals(object)) {
                        Message msg2 = new Message();
                        msg2.what = 2;
                        msg2.obj = new MainUiInfo(true, 4, 1.00);
                        LeaveActivity.handler.sendMessage(msg2);
                        Message msg = new Message();
                        msg.what = 2;
                        msg.obj = new MainUiInfo(true, 5, 1.00);
                        LeaveActivity.handler.sendMessage(msg);
                        activity.refreshOrderFragment();
                    }
                    if (object.equals("1")) {
                        dialog.dismiss();
                        activity.showToast("结算订单成功");
                    } else if (object.equals("2")) {
                        showErrorDialog(object, car_number);
                        dialog.dismiss();
                    } else if (object.equals("-2")) {
                        dialog.dismiss();
                        showErrorDialog(object, car_number);
                    } else if (object.equals("3")) {
                        showErrorDialog(object, car_number);
                        dialog.dismiss();
                    } else if (object.equals("4")) {
                        dialog.dismiss();
                        showErrorDialog(object, car_number);
                    } else {
                        dialog.dismiss();
                        showErrorDialog("结算失败", car_number);
                        activity.showToast(object + " 结算订单失败！");
                    }
                } else {
                    pd.dismiss();
                    if (status.getCode() == -101) {
                        Toast.makeText(activity, "网络错误！--请再次刷卡结算订单！", 0).show();
                        dialog.dismiss();
                    } else if (status.getCode() == 500) {
                        Toast.makeText(activity, "服务器错误！--请再次刷卡结算订单！", 0).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(activity, "网络请求错误!--请再次刷卡结算订单！", 0).show();
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    public void showErrorDialog(String cade, String car_number) {
        String title;
        String message;
        if (cade.equals("2")) {
            title = "车主余额不足";
            message = car_number + "停车宝账户余额不足,无法完成自动支付,请通知车主手动支付或者收取现金！";
        } else if (cade.equals("-2")) {
            title = "相同车牌号已存在订单";
            message = car_number + "在本车场已存在订单！";
        } else if (cade.equals("3")) {
            title = "未设自动支付";
            message = car_number + "用户没有设置自动支付！";
        } else if (cade.equals("4")) {
            title = "支付失败";
            message = car_number + "停车费超过车主自动支付限额！";
        } else if (cade.equals("-1")) {
            title = "订单生成失败！";
            message = "在本车场已存在订单！";
        } else if (cade.equals("-4")) {
            title = "订单生成失败";
            message = "频繁进入同一车场，请稍后再试！";
        } else if (cade.equals("-3")) {
            title = "订单生成失败";
            message = "生成订单失败---请重试！";
        } else {
            title = "订单结算失败！";
            message = "请重新刷卡结算订单！";
        }
        new AlertDialog.Builder(activity).setIcon(R.drawable.app_icon_32).setTitle(title).setMessage(message)
                .setNegativeButton("知道了", null).setCancelable(false).create().show();
    }

}
