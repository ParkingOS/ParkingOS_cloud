package com.zhenlaidian.engine;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.bean.EPayMessageDialog;
import com.zhenlaidian.bean.FinishOrderFailDialog;
import com.zhenlaidian.bean.LeaveOrder;
import com.zhenlaidian.bean.MainUiInfo;
import com.zhenlaidian.bean.NfcFinishOrderAnHeQiao;
import com.zhenlaidian.bean.NfcFinishOrderDialog;
import com.zhenlaidian.bean.NfcFinishOrderOnceDialog;
import com.zhenlaidian.bean.NfcOrder;
import com.zhenlaidian.bean.NfcPrepaymentOrder;
import com.zhenlaidian.bean.StopWaitToCashDialog;
import com.zhenlaidian.printer.TcbCheckCarOut;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * nfc结算订单,分别调用不同类型的结算对话框;
 */
public class ShowNfcFinishOrder {
    private LeaveActivity activity;
    private String comid;
    private NfcOrder nfcOrder;
    private String imei;
    private static ShowNfcFinishOrder instance;
    private NfcFinishOrderDialog finish_dialog;
    private NfcFinishOrderOnceDialog oncedialog;
    private NfcFinishOrderAnHeQiao anheqiao_dialog;
    private StopWaitToCashDialog stopdialog;

    private ShowNfcFinishOrder() {
        super();
    }

    public static ShowNfcFinishOrder getInstance() {
        if (instance == null) {
            instance = new ShowNfcFinishOrder();
        }
        return instance;
    }

    public void setContext(LeaveActivity context) {
        this.activity = context;
    }

    public void setComid(String comid) {
        this.comid = comid;
    }

    public void setNfcOrder(NfcOrder nfcOrder) {
        this.nfcOrder = nfcOrder;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public void showNfcFinishOrderDialog() {
        if (stopdialog != null && stopdialog.isShowing()) {
            if (activity != null && !activity.isFinishing()) {
                noFinishOrder();
            }
            return;
        }
        if (nfcOrder.getCollect0() == null) {// 没有按次价格。弹出按时对话框
            if (finish_dialog == null || !finish_dialog.isShowing()) {
                finish_dialog = new NfcFinishOrderDialog(activity, R.style.nfcfinishdialog, this, nfcOrder);
                finish_dialog.setCancelable(false);
                MyLog.i("ShowNfcFinishOrder", "弹出按时对话框：" + activity.isFinishing());
                if (activity != null && !activity.isFinishing()) {
                    finish_dialog.show();
                }
            } else {
                if (activity != null && !activity.isFinishing()) {
                    noFinishOrder();
                }
            }
        } else {
            if (nfcOrder.getCollect1() == null) {// 没有多个按次计费，弹出土桥专用对话框；
                if (anheqiao_dialog == null || !anheqiao_dialog.isShowing()) {
                    anheqiao_dialog = new NfcFinishOrderAnHeQiao(activity, R.style.nfcfinishdialog, this, nfcOrder);
                    anheqiao_dialog.setCancelable(false);
                    MyLog.i("ShowNfcFinishOrder", "弹出土桥桥专用对话框；：" + activity.isFinishing());
                    if (activity != null && !activity.isFinishing()) {
                        anheqiao_dialog.show();
                    }
                } else {
                    if (activity != null && !activity.isFinishing()) {
                        noFinishOrder();
                    }
                }
            } else {
                if (oncedialog == null || !oncedialog.isShowing()) {// 弹出按次对话框！
                    oncedialog = new NfcFinishOrderOnceDialog(activity, R.style.nfcfinishdialog, this, nfcOrder);
                    oncedialog.setCancelable(false);
                    MyLog.i("ShowNfcFinishOrder", "弹出正常按次对话框！：" + activity.isFinishing());
                    if (activity != null && !activity.isFinishing()) {
                        oncedialog.show();
                    }
                } else {
                    if (activity != null && !activity.isFinishing()) {
                        noFinishOrder();
                    }
                }
            }
        }
    }

    // 把确认结算订单提交给服务器；返回1提交成功；
    // nfchandle.do?action=completeorder&orderid=78&collect=20&comid=3&carnumber=&uid=&uin=&uuid=&pay=
    // 说明：orderid为空时，uin和uuid，carnumber这三个参数不能为空,用于生成订单 pay= 1现金结；0默认值；
    // 返回：1成功，2:车主余额不足，3: 速通卡用户没有设置自动支付 , -2：相同的车牌已在本车场存在订单，-5等会刷可现金结 -6只能等会刷
    // ,其它失败
    public void submitCash(final String collect, final Dialog dialog, String pay, final String car_number) {
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
                + "&uin=" + nfcOrder.getUin() + "&pay=" + pay;
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
                        Message msg = new Message();
                        msg.what = 2;
                        msg.obj = new MainUiInfo(true, 5, 1.00);
                        LeaveActivity.handler.sendMessage(msg);

                        String uid = activity.getSharedPreferences("autologin", Context.MODE_PRIVATE).getString("account", "");
                        TcbCheckCarOut out = new TcbCheckCarOut(nfcOrder.getOrderid(), nfcOrder.getCarnumber(), nfcOrder.getBtimestr(),
                                nfcOrder.getEtimestr(), nfcOrder.getDuration(), SharedPreferencesUtils.
                                getIntance(activity).getName() + " (" + uid + ")", collect);
                        //打印凭条
                        activity.prientCarOut(out);
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
                    } else if (object.equals("-5")) {
                        dialog.dismiss();
                        stopdialog = new StopWaitToCashDialog(activity, ShowNfcFinishOrder.this, nfcOrder, false);
                        stopdialog.setCanceledOnTouchOutside(false);
                        stopdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        stopdialog.show();
                    } else if (object.equals("-6")) {
                        dialog.dismiss();
                        stopdialog = new StopWaitToCashDialog(activity, ShowNfcFinishOrder.this, nfcOrder, true);
                        stopdialog.setCanceledOnTouchOutside(false);
                        stopdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        stopdialog.show();
                    } else if (object.equals("3")) {
                        showErrorDialog(object, car_number);
                        dialog.dismiss();
                    } else if (object.equals("4")) {
                        dialog.dismiss();
                        showErrorDialog(object, car_number);
                    } else {
                        dialog.dismiss();
                        nfcOrder.setNetError("服务器返回值异常：" + object);
                        showFinishFailDialog(nfcOrder);
                    }
                } else {
                    pd.dismiss();
                    if (status.getCode() == -101) {
                        dialog.dismiss();
                        nfcOrder.setNetError(" -101：网络错误");
                        showFinishFailDialog(nfcOrder);
                    } else if (status.getCode() == 500) {
                        dialog.dismiss();
                        nfcOrder.setNetError(" 500：服务器错误");
                        showFinishFailDialog(nfcOrder);
                    } else {
                        dialog.dismiss();
                        nfcOrder.setNetError(" 000：网络请求错误");
                        showFinishFailDialog(nfcOrder);
                    }
                }
            }
        });
    }

    public void showFinishFailDialog(NfcOrder order) {
        FinishOrderFailDialog failDialog = new FinishOrderFailDialog(activity, R.style.nfcnewdialog, order, "nfc");
        failDialog.setCanceledOnTouchOutside(false);
        failDialog.show();
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
        } else if (cade.equals("5")) {
            title = "微信用户支付成功";
            message = car_number + " 用户已电子支付请确认放行！";
        } else {
            title = "订单结算失败！";
            message = "请重新刷卡结算订单！";
        }
        new AlertDialog.Builder(activity).setIcon(R.drawable.app_icon_32).setTitle(title).setMessage(message)
                .setNegativeButton("知道了", null).setCancelable(false).create().show();
    }

    /*
     * 预付费的结算接口；
     * http://127.0.0.1/zld/nfchandle.do?action=doprepayorder&orderid=&
     * collect=20&comid="+ comid
     */
    public void cashPrepayOrder(final String collect, final Dialog dialog) {
        String path = Config.getUrl(activity);
        String url = path + "nfchandle.do?action=doprepayorder&orderid=" + nfcOrder.getOrderid() + "&collect=" + collect
                + "&comid=" + comid;
        MyLog.i("ShowNfcOrder", "确认结算预付费NFC订单的URL--->" + url);
        final AQuery aQuery = new AQuery(activity);
        final ProgressDialog pd = ProgressDialog.show(activity, "结算中...", "正在提交结算数据...", true, true);
        pd.setCanceledOnTouchOutside(false);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
                    pd.dismiss();
                    MyLog.i("ShowNfcOrder", "预付费订单结算结果--->" + object);
                    Gson gson = new Gson();
                    NfcPrepaymentOrder preorder = gson.fromJson(object, NfcPrepaymentOrder.class);
                    if (preorder != null) {
                        if ("1".equals(preorder.getResult()) || "2".equals(preorder.getResult())
                                || "3".equals(preorder.getResult()) || "4".equals(preorder.getResult())
                                || "5".equals(preorder.getResult())) {
                            Message msg = new Message();
                            msg.what = 2;
                            msg.obj = new MainUiInfo(true, 5, 1.00);
                            LeaveActivity.handler.sendMessage(msg);
                            String uid = activity.getSharedPreferences("autologin", Context.MODE_PRIVATE).getString("account", "");
                            TcbCheckCarOut out = new TcbCheckCarOut(nfcOrder.getOrderid(), nfcOrder.getCarnumber(), nfcOrder.getBtimestr(),
                                    nfcOrder.getEtimestr(), nfcOrder.getDuration(), SharedPreferencesUtils.
                                    getIntance(activity).getName() + " (" + uid + ")", collect);
                            //打印凭条
                            activity.prientCarOut(out);
                            activity.refreshOrderFragment();
                        }
                        if ("1".equals(preorder.getResult())) {// 1成功 -1失败
                            if (collect != null) {
                                try {
                                    Message msg = new Message();
                                    msg.what = 2;
                                    msg.obj = new MainUiInfo(true, 1, Double.parseDouble(collect));
                                    LeaveActivity.handler.sendMessage(msg);
                                } catch (Exception e) {

                                }
                            }
                            dialog.dismiss();
                            LeaveOrder order = new LeaveOrder();
                            order.setCarnumber(nfcOrder.getCarnumber());
                            order.setTotal(collect);
                            ScanMyCodeCash(order);
                        } else if ("-1".equals(preorder.getResult())) {
                            dialog.dismiss();
                            nfcOrder.setNetError(" -1  预付费结算失败");
                            showFinishFailDialog(nfcOrder);
                        } else if ("2".equals(preorder.getResult())) {// 2需要补差价；
                            notBalanceDialog(preorder);
                            dialog.dismiss();
                        } else {
                            dialog.dismiss();
                            nfcOrder.setNetError("预付费结算失败" + preorder.getResult());
                            showFinishFailDialog(nfcOrder);
                        }
                    } else {
                        dialog.dismiss();
                        nfcOrder.setNetError("预付费-解析错误" + object);
                        showFinishFailDialog(nfcOrder);
                    }
                } else {
                    pd.dismiss();
                    if (status.getCode() == -101) {
                        dialog.dismiss();
                        nfcOrder.setNetError(" -101：网络错误");
                        showFinishFailDialog(nfcOrder);
                    } else if (status.getCode() == 500) {
                        dialog.dismiss();
                        nfcOrder.setNetError(" 500：服务器错误");
                        showFinishFailDialog(nfcOrder);
                    } else {
                        dialog.dismiss();
                        nfcOrder.setNetError(" 000：网络请求错误");
                        showFinishFailDialog(nfcOrder);
                    }
                }
            }
        });
    }

    // 微信预支付成功后的弹框；
    public void ScanMyCodeCash(LeaveOrder order) {
        EPayMessageDialog dialog = new EPayMessageDialog(activity, R.style.nfcnewdialog, order);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    // 还需要补交现金的对话框；
    public void notBalanceDialog(NfcPrepaymentOrder preorder) {
        View open_dialog_view = View.inflate(activity, R.layout.dialog_repayment_money_nfcorder, null);
        TextView tv_collect = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_collect);
        TextView tv_total = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_total);
        TextView tv_prefee = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_prefee);
        TextView tv_collect1 = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_collect1);
        Button bt_nfcorder_ok = (Button) open_dialog_view.findViewById(R.id.bt_dialog_repayment_nfcorder_ok);
        tv_collect.setText(preorder.getCollect() != null ? "还需向车主补收" + preorder.getCollect() + "元现金" : "");
        tv_collect1.setText(preorder.getCollect() != null ? preorder.getCollect() : "");
        tv_total.setText(preorder.getTotal() != null ? "停车费			" + preorder.getTotal() + "元" : "");
        tv_prefee.setText(preorder.getPrefee() != null ? "微信预付		" + preorder.getPrefee() + "元" : "");
        final Dialog openDialog = new Builder(activity).create();
        bt_nfcorder_ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openDialog.dismiss();
            }
        });
        openDialog.setCancelable(false);
        openDialog.show();
        openDialog.setContentView(open_dialog_view);
    }

    public void noFinishOrder() {
        Message msg = new Message();
        msg.what = 7;
        LeaveActivity.handler.sendMessage(msg);
    }

}
