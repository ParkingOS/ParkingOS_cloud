package com.tq.zld.receiver;

import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.Html;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Order;
import com.tq.zld.util.AndroidUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.map.MapActivity;
import com.tq.zld.wxapi.WXPayEntryActivity;

public class GeTuiPushReceiver extends BroadcastReceiver {

    public static final String MSG_TYPE_ORDER = "0";// 订单消息
    public static final String MSG_TYPE_BOOK = "1";// 预定消息
    public static final String MSG_TYPE_PAY = "2";// 支付消息
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Bundle bundle = intent.getExtras();
        LogUtils.i("Getui message getted! Action: --->> " + bundle.getInt("action"));
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {
            case PushConsts.GET_MSG_DATA:
                // 获取透传数据
                // String appid = bundle.getString("appid");
                byte[] payload = bundle.getByteArray("payload");

                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");

                // smartPush第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
                boolean success = PushManager.getInstance().sendFeedbackMessage(
                        context, taskid, messageid, 90001);

                LogUtils.i(getClass(), "smartPush result: --->> "
                        + (success ? "Success" : "Failure"));

                if (payload != null) {
                    try {
                        String data = new String(payload);
                        // TODO 解密payload
                        // data = AESEncryptor.decrypt(new Keys().getPartner(),
                        // data);
                        LogUtils.i(getClass(), "Getui Meaasge playload: --->> "
                                + data);
                        JSONObject json = new JSONObject(data);

                        // 如果此条消息已经取过，则不再处理
                        String msgid = TCBApp.getAppContext().readString(R.string.sp_geitui_msg_id, "-1");
                        String newMsgid = "";
                        if (json.has("msgid")) {
                            newMsgid = json.getString("msgid");
                        }
                        if (TextUtils.equals(msgid, newMsgid)) {
                            return;
                        }

                        String info = "";
                        if (json.has("info")) {
                            info = json.getString("info");
                        }

                        String type = "";
                        if (json.has("mtype")) {
                            type = json.getString("mtype");
                        }
                        if (TextUtils.isEmpty(type) || TextUtils.isEmpty("info")) {
                            return;
                        }

                        Gson gson = new Gson();
                        switch (type) {
                            case MSG_TYPE_ORDER:
                                Order order = gson.fromJson(info, Order.class);
                                if (AndroidUtils.isActivityForeground(context,
                                        MapActivity.class)) {
                                    // 用户当前正在地图界面操作
                                    Message.obtain(MapActivity.mHandler,
                                            MapActivity.MSG_WHAT_HANDLER_ORDER, order)
                                            .sendToTarget();
                                } else {
                                    // 用户不在地图界面切不在支付界面，发送Notification
                                    if (WXPayEntryActivity.mPayResultHandler != null
                                            && Order.STATE_PAYED.equals(order.getState())) {
                                        break;
                                    }
                                    sendNotification(order);
                                }

                                break;
                            // TODO 其他类型的消息处理
                        }

                        // 等处理完了再将消息id存储起来
                        if (!TextUtils.isEmpty(newMsgid)) {// 消息的msgid为""，不处理
                            TCBApp.getAppContext().saveString(R.string.sp_geitui_msg_id, newMsgid);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case PushConsts.GET_CLIENTID:
                // 获取ClientID(CID)
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                // String cid = bundle.getString("clientid");
                // if (!TextUtils.isEmpty(TCBApp.mobile) && !TextUtils.isEmpty(cid))
                // {
                // Map<String, Object> params = new HashMap<String, Object>();
                // params.put("cid", cid);
                // params.put("mobile", TCBApp.mobile);
                // params.put("action", "addcid");
                // new SplashActivity().updateClientId(params);
                // }
                break;
            case PushConsts.THIRDPART_FEEDBACK:
            /*
             * String appid = bundle.getString("appid"); String taskid =
			 * bundle.getString("taskid"); String actionid =
			 * bundle.getString("actionid"); String result =
			 * bundle.getString("result"); long timestamp =
			 * bundle.getLong("timestamp");
			 * 
			 * Log.d("GetuiSdkDemo", "appid = " + appid); Log.d("GetuiSdkDemo",
			 * "taskid = " + taskid); Log.d("GetuiSdkDemo", "actionid = " +
			 * actionid); Log.d("GetuiSdkDemo", "result = " + result);
			 * Log.d("GetuiSdkDemo", "timestamp = " + timestamp);
			 */
                break;
        }
    }

    private void sendNotification(Order order) {
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Builder builder = new NotificationCompat.Builder(context);
        builder.setAutoCancel(true).setWhen(System.currentTimeMillis())
                .setOngoing(false)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(getNotificationIcon());
        Intent intent;
        PendingIntent notifyPIntent;
        switch (order.getState()) {
            case Order.STATE_PENDING:// 未结算
                notifyPIntent =
                        PendingIntent.getActivity(TCBApp.getAppContext(), 0, new Intent(), 0);
                builder.setContentIntent(notifyPIntent);
                builder.setContentTitle(order.getParkname())
                        .setContentText("欢迎入场！")
                        .setTicker(String.format("您已进入%s！", order.getParkname()));
                mNotificationManager.notify(0, builder.build());
                return;
            case Order.STATE_PAY_FAILED:// 已结算（未支付）
            case Order.STATE_PAYING:
                // 如果金额是零，则直接支付成功
                if ("0.00".equals(order.getTotal())) {
                    builder.setContentTitle("停车费：" + order.getTotal() + "元，支付成功！")
                            .setContentText(order.getParkname())
                            .setTicker("停车费支付成功！");
                    notifyPIntent =
                            PendingIntent.getActivity(TCBApp.getAppContext(), 0, new Intent(), 0);
                    builder.setContentIntent(notifyPIntent);
                    mNotificationManager.notify(0, builder.build());
                    return;
                }

                // 打开支付页面
                // intent = new Intent(context, WXPayEntryActivity.class);
                // intent.putExtra(WXPayEntryActivity.ARG_TOTALFEE,
                // order.getTotal());
                // intent.putExtra(WXPayEntryActivity.ARG_SUBJECT,
                // "支付停车费_" + order.getParkname());
                // intent.putExtra(WXPayEntryActivity.ARG_PRODTYPE,
                // WXPayEntryActivity.PROD_PARKING_FEE);
                // intent.putExtra(WXPayEntryActivity.ARG_ID,
                // order.getOrderid());

                // 打开地图
                intent = new Intent(TCBApp.getAppContext(), MapActivity.class);
                intent.putExtra("order", order);
                notifyPIntent = PendingIntent.getActivity(context, 2,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentTitle(order.getParkname())
                        .setContentText(Html.fromHtml(order.getTotal() + "元<small>--点击支付</small>"))
                        .setTicker("您有一笔停车费需要支付！").setContentIntent(notifyPIntent);
                mNotificationManager.notify(1, builder.build());
                return;
            case Order.STATE_PAYED:// 已支付
                String contextText = order.getParkname();
                String ticker = "停车费支付成功！";
                // 不在此分享红包
                // if (!TextUtils.isEmpty(order.getBonusid())) {
                // intent = new Intent(context, MapActivity.class);
                // intent.putExtra("order", order);
                // contextText = "点我分享！";
                // ticker = "恭喜您获得一个红包！";
                // } else {
                intent = new Intent(context, MainActivity.class);
                intent.putExtra(MainActivity.ARG_FRAGMENT,
                        MainActivity.FRAGMENT_HISTORY_ORDER);
                // Bundle args = new Bundle();
                // args.putString(OrderDetailFragment.ARG_ORDER_ID,
                // order.getOrderid());
                // intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
                notifyPIntent = PendingIntent.getActivity(context, 2,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentTitle("停车费：" + order.getTotal() + "元，支付成功！")
                        .setContentText(contextText).setTicker(ticker)
                        .setContentIntent(notifyPIntent);
                mNotificationManager.notify(0, builder.build());
                break;
        }
    }

    private int getNotificationIcon() {
        boolean whiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        return whiteIcon ? R.drawable.ic_launcher_lollipop
                : R.mipmap.ic_launcher;
    }
}