/**
 *
 */
package com.zhenlaidian.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.device.PrinterManager;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.rsk.api.Printer;
import com.rsk.api.RskApi;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.BoWeiStateEntity;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.bean.LeaveOrder;
import com.zhenlaidian.bean.PullMessage;
import com.zhenlaidian.bean.ScoreMessageInfo;
import com.zhenlaidian.decode.CrashHandler;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.BaseActivity.MsgToMainListener;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.BluetoothService;
import com.zhenlaidian.util.CheckUtils;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.Constant;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 轮询获取消息；
 *
 * @author zhangyunfei 2015年8月28日
 */
public class PullMsgService extends Service {
    private final static String TAG = "PullMsgService";
    private final Timer timer = new Timer();
    private TimerTask task;
    private Notification mNotification;
    private int count = 0;// 用于计数；临时解决软件初装时重启应用的bug；

    private static long updateLocationTime = 0;// 更新位置信息时间；
    private static MsgToMainListener mMsgToMainListener;

    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void setOnConnectListener(MsgToMainListener l) {
        mMsgToMainListener = l;
    }

    // 回调内容给activity；
    public void sendMsgToMainListener(Message msg) {
        if (mMsgToMainListener != null) {
            mMsgToMainListener.onSendMsg(msg);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        // 初始化百度地图
        initMap();
        task = new TimerTask() {
            @Override
            public void run() {
                getLeaveInfo();// 获取即时消息；
                updateLoaction();// 10分钟提交一次我的定位信息；
                //如果为真，则调用守护,照相时，置为false，不执行守护
                if (Constant.ISNEEDBACKUP) {
                    guardMe();
                }
                if (CommontUtils.Is910() || CommontUtils.Is900()) {
                    CanPrint = true;
                } else {
                    conn2bluetooth();
                }
                /**
                 * 检测是否泊位列表刷新失败
                 */
                if (!Constant.BerthFresh) {
                    Message message = new Message();
                    message.what = 1028;
                    sendMsgToMainListener(message);
                }
                if (hasGPSDevice(getApplicationContext()) && !isOPen(getApplicationContext())) {
                    MyLog.d(TAG, "设备支持Gps硬件，没有开启GPS---提醒用户去打开！");
                    Message msg = new Message();
                    msg.what = 5;// 提醒打开gps
                    sendMsgToMainListener(msg);
                }
            }
        };
        timer.schedule(task, 1000, 5000);

    }

    private void initMap() {
        mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
        mLocationClient.registerLocationListener(myListener); // 注册监听函数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(1000*60*10);// 设置发起定位请求的间隔时间为2000ms,过于频繁，改为10分钟
        option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
        option.setOpenGps(true);
        mLocationClient.setLocOption(option);
        mLocationClient.requestLocation();
        if (mLocationClient != null) {
            mLocationClient.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 关闭定时任务；
        timer.cancel();
        // 关闭位置搜索；
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
        MyLog.d(TAG, "onDestroy");
    }


    public void updateLoaction() {
        if (updateLocationTime == 0 || System.currentTimeMillis() - updateLocationTime > 1000 * 60 * 10) {
            updateLocationTime = System.currentTimeMillis();
            if (hasGPSDevice(getApplicationContext()) && !isOPen(getApplicationContext())) {
                MyLog.d(TAG, "设备支持Gps硬件，没有开启GPS---提醒用户去打开！");
                Message msg = new Message();
                msg.what = 5;// 提醒打开gps
                sendMsgToMainListener(msg);
            }
            if (mLocationClient != null) {
                mLocationClient.start();
            }
        }
    }


    // 判断手机是否支持GPS硬件设备；
    public boolean hasGPSDevice(Context context) {
        LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    // 判断手机GPS是否打开；
    public static final boolean isOPen(final Context context) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        // boolean network =
        // locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps) {
            return true;
        }
        return false;

    }

    // 弹出Notification
    private void showNotification(String info) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, LeaveActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
        Notification.Builder builder = new Notification.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(info)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
        mNotification = builder.getNotification();

        mNotification.defaults |= Notification.DEFAULT_SOUND;// 系统默认声音
        mNotification.flags = Notification.FLAG_AUTO_CANCEL;// 点击自动消失
        mNotification.when = System.currentTimeMillis();
        mNotification.contentIntent = contentIntent;
        startForeground(1, mNotification);
    }

    /**
     * 轮询访问服务器获取即时消息；
     */
    // // String url = path + "getmesg.do?token=" + token ;
    public void getLeaveInfo() {
        if (!IsNetWork.IsHaveInternet(this)) {
            MyLog.d(TAG, "网络已断开");
            return;
        }

        String path = Config.getMserver(this);
        String url = path + "getmesg.do?token=" + SharedPreferencesUtils.getIntance(this).getToken() + "&berthid=" + SharedPreferencesUtils.getIntance(getApplicationContext()).getberthid() + "&out=json";
        MyLog.d(TAG, " 轮询访问服务器获取即时消息-->>" + url);
        AQuery aq = new AQuery(this);
        aq.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (TextUtils.isEmpty(object)) {
                    MyLog.d(TAG, "获取到的消息是空的！！！");
                } else {
                    MyLog.d(TAG, "获取到的消息" + object);
                    makeMessage(object);
                }
            }
        });

    }

    private void guardMe() {
        try {
//				MyLog.d(TAG,"已经开启程序");
            MyLog.d(TAG, "主界面在最前端startRun");
            if (!top(this)) {
                startZld("com.zhenlaidian.ui.HelloActivity");
            } else {
                MyLog.d(TAG, "主界面在最前端");
            }
        } catch (Exception e) {
            MyLog.d(TAG, "捕获异常" + e.getMessage());
        }
    }

    private void startZld(String activityStr) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName cn = new ComponentName("com.zhenlaidian", activityStr);
        intent.setComponent(cn);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(intent);
    }

    private boolean top(Context context) {
        boolean result = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //** 获取当前正在运行的任务栈列表， 越是靠近当前运行的任务栈会被排在第一位，之后的以此类推 */
        List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
        //** 获得当前最顶端的任务栈，即前台任务栈 */
        ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
        //** 获取前台任务栈的最顶端 Activity */
        ComponentName topActivity = runningTaskInfo.topActivity;
        //** 获取应用的包名 */
        String packageName = topActivity.getPackageName();
        //** 输出检测到的启动应用信息 */
        MyLog.d("sunzn", packageName);
        if (packageName.equals("com.zhenlaidian")) {
            result = true;
        }
        return result;
    }

    public void makeMessage(String object) {

        MyLog.d(TAG, "获取到的Message-->>" + object);
        Gson gson = new Gson();
        PullMessage pullmsg = gson.fromJson(object, PullMessage.class);
        if (pullmsg.getMtype() == null) {
            return;
        }
        MyLog.d(TAG, "获取到的消息类型为" + pullmsg.getMtype());
        switch (Integer.parseInt(pullmsg.getMtype())) {
            case 0:
                MyLog.d(TAG, "获取到离场订单消息....." + pullmsg.getInfo());
                if (pullmsg.getInfo() != null) {
                    LeaveOrder leaveorder = gson.fromJson(pullmsg.getInfo(), LeaveOrder.class);
                    MyLog.d(TAG, "解析到的离场订单为" + leaveorder.toString());
                    MyLog.d(TAG, "加载到离场订单...");
                    Message msg1 = new Message();
                    msg1.what = 1;// 获取到离场订单
                    msg1.obj = leaveorder;
                    sendMsgToMainListener(msg1);
                }
                break;
            case -1:
                if (count > 1) {
                    MyLog.d(TAG, "检查token的状态--token无效");
                    Message msg = new Message();
                    msg.what = 4;// token失效
                    sendMsgToMainListener(msg);
                    count = 0;
                } else {
                    count++;
                }
                break;
            case 3:// 泊车订单；
                MyLog.d(TAG, "获取到离场订单消息....." + pullmsg.getInfo());
                if (pullmsg.getInfo() != null) {
                    LeaveOrder leaveorder = gson.fromJson(pullmsg.getInfo(), LeaveOrder.class);
                    MyLog.d(TAG, "解析到的离场订单为" + leaveorder.toString());
                    boolean flag = SharedPreferencesUtils.getIntance(this).getMainActivity();
                    if (flag) {
                        MyLog.d(TAG, "加载到离场订单...");
                        // 判断取到的消息类型;
                        Message msg1 = new Message();
                        msg1.what = 1;// 获取到离场订单
                        msg1.obj = leaveorder;
                        sendMsgToMainListener(msg1);
                        MyLog.d(TAG, "flag为true.........");
                        break;
                    } else {
                        if (isBackground(PullMsgService.this) == false) {// 在前台
                            MyLog.d(TAG, "程序在前台.........");
                            Message msg2 = new Message();
                            msg2.what = 1;// 获取到离场订单
                            msg2.obj = leaveorder;
                            sendMsgToMainListener(msg2);
                            Intent intent = new Intent(PullMsgService.this, LeaveActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            PullMsgService.this.startActivity(intent);
                            break;
                        } else {// 在后台
                            MyLog.d(TAG, "程序在后台.........");
                            showNotification("停车宝车主支付消息!");
                            Message msg3 = new Message();
                            msg3.what = 1;// 获取到离场订单
                            msg3.obj = leaveorder;
                            sendMsgToMainListener(msg3);
                        }
                    }
                }
                break;
            case 4:
                MyLog.d(TAG, "获取到工作站被同事绑定消息.....");
                showNotification("停车宝工作站被同事绑定，请查看！");
                Message msg10 = new Message();
                msg10.what = 10;// 工作站被同事绑定消息
                sendMsgToMainListener(msg10);
                break;
            case 5:
                MyLog.d(TAG, "获取到打赏消息....." + pullmsg.getInfo());
                LeaveOrder leaveorder = gson.fromJson(pullmsg.getInfo(), LeaveOrder.class);
                MyLog.d(TAG, "解析到打赏内容是：" + leaveorder.toString());
                if (isBackground(PullMsgService.this)) {
                    showNotification("您收到一笔车主打赏，请查看！");
                }
                Message msg14 = new Message();
                msg14.what = 14;// 打赏消息
                msg14.obj = leaveorder;
                sendMsgToMainListener(msg14);
                break;
            case 6:
                MyLog.d(TAG, "主页顶部消息通知....." + pullmsg.getInfo());
                Message msg15 = new Message();
                msg15.what = 15;// 主页顶部消息通知
                sendMsgToMainListener(msg15);
                break;
            case 7:
                MyLog.d(TAG, "获取积分被扣除的消息....." + pullmsg.getInfo());
                ScoreMessageInfo sinfo = gson.fromJson(pullmsg.getInfo(), ScoreMessageInfo.class);
                Message msg16 = new Message();
                msg16.what = 16;// 积分被扣除的消息.
                msg16.obj = sinfo.getScore();
                sendMsgToMainListener(msg16);
                break;
            case 8:
                MyLog.d(TAG, "推荐奖到账通知....." + pullmsg.getInfo());
                LeaveOrder info = gson.fromJson(pullmsg.getInfo(), LeaveOrder.class);
                Message msg17 = new Message();
                msg17.what = 17;// 积分被扣除的消息.
                msg17.obj = info;
                sendMsgToMainListener(msg17);
                break;
            case 10:
                MyLog.d(TAG, "泊位变动通知....." + pullmsg.getMesgs());
                JsonArray array = pullmsg.getMesgs();
                ArrayList<BoWeiStateEntity> entity = new ArrayList<BoWeiStateEntity>();
                try {
                    if (array != null && array.size() > 0) {
                        MyLog.d(TAG, "array....." + array);
                        for (int i = 0; i < array.size(); i++) {
                            BoWeiStateEntity en = gson.fromJson(array.get(i), BoWeiStateEntity.class);
                            entity.add(en);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Message msg18 = new Message();
                msg18.what = 18;
                msg18.obj = entity;
                sendMsgToMainListener(msg18);
                break;

        }
    }


    /**
     * 判断当前应用是否在后台运行
     *
     * @param context
     * @return
     */
    public static boolean isBackground(Context context) {

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    Log.i("后台", appProcess.processName);
                    return true;
                } else {
                    Log.i("前台", appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean CanPrint = false;
    public static BluetoothService mService = null;
    Set<BluetoothDevice> pairedDevices;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


    private void conn2bluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            //打开蓝牙
//            System.out.println("打开蓝牙");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PullMsgService.this.startActivity(enableIntent);
        }
//        System.out.println("以打开");
        if (mService == null) {
            mService = new BluetoothService();
        }
//        System.out.println("获取设备");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                System.out.println(device.getName() + "\n" + device.getAddress());
                Message m = new Message();
                m.what = 1001;
                m.obj = device;
                mHandler.sendMessage(m);
//                System.out.println("获取并发送消息");
            }

        } else {
//            System.out.println("pairedDevices的size小于0");
            CanPrint = false;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    BluetoothDevice device = (BluetoothDevice) msg.obj;
                    if (mService.getState() != BluetoothService.STATE_CONNECTED) {
                        mService.connect(device);
                        MyLog.d(TAG, "未连接--在连接");
                    } else {
                        MyLog.d(TAG, "已连接HHHHHHHHHH");
                        CanPrint = true;
                    }

                    break;
            }

        }
    };

    public static void sendMessage(final String message, final Context context) {
        if (message.length() > 0) {
            if (CommontUtils.Is910()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {//910
                            PrinterManager printer = new PrinterManager();
                            printer.setGrayLevel(2);
                            printer.setupPage(384, -1);
                            String[] messageList = message.split("\n");
                            StringBuffer sb = new StringBuffer("");
                            for (int i = 0; i < messageList.length; i++) {
                                String tmp = messageList[i];
                                int length = CommontUtils.StringLength(tmp);
                                if (length < 17) {
                                    sb.append(tmp);
                                    sb.append("\n");
                                } else {
                                    sb.append(CommontUtils.cutString(tmp, 16));
                                    sb.append("\n");
                                }
                            }
                            String[] MSGlist = sb.toString().split("\n");
                            int height = 0;
                            for (String m : MSGlist) {
                                if (m.contains("收费凭证") || m.contains("逃单金额") || m.contains("上缴金额") || m.contains("充值凭证"))
                                    printer.prn_drawText(m, 0, height, "宋体", 32, false, false, 0);
                                else
                                    printer.prn_drawText(m, 0, height, "宋体", 24, false, false, 0);
                                height += 30;
                            }
                            height += 100;
                            printer.prn_drawText("   ", 0, height, "宋体", 24, false, false, 0);

                            int ret = printer.printPage(0);
                            Intent intent = new Intent("urovo.prnt.message");
                            intent.putExtra("ret", ret);
                            context.sendBroadcast(intent);
                            printer.clearPage();
                        } catch (Exception ex) {
                            CrashHandler.WriteLog(context, "ex " + ex.getMessage());
                            CrashHandler.WriteLog(context, "ex " + ex.getCause());
                            CrashHandler.WriteLog(context, "ex " + ex.getStackTrace());
                            Toast.makeText(context, "打印机未连接", Toast.LENGTH_SHORT);
                        }
                    }
                }).start();

            } else if (CommontUtils.Is900()) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        RskApi.PrintOpen();
                        byte[] senddata;
                        try {
                            senddata = message.getBytes("gbk");
                        } catch (UnsupportedEncodingException e) {
                            senddata = message.getBytes();
                        }
                        Printer.PrintChars(senddata, senddata.length);

//                        String qrmessage = "www.baidu.com";
//                        RskApi.PrintQRWidth(250);
//                        byte[] qrdata;
//                        try {
//                            qrdata = qrmessage.getBytes("gbk");
//                        } catch (UnsupportedEncodingException e) {
//                            qrdata = qrmessage.getBytes();
//                        }
//                        Printer.PrintQR(qrdata, qrdata.length);// 打印QR码

//                        String message2 = "\n\n\n\n\n\n\n\n\n\n";
                        String message2 = "\n\n\n\n\n";
                        byte[] senddata2;
                        try {
                            senddata2 = message2.getBytes("gbk");
                        } catch (UnsupportedEncodingException e) {
                            senddata2 = message2.getBytes();
                        }
                        Printer.PrintChars(senddata2, senddata2.length);
                        Printer.StartPrint(); //开始打印，注意：打印完才会接收命令，比如IC卡或者打印机命令
//                RskApi.PrintLines((byte)5);
                        RskApi.PrintClose();
                    }
                }.start();

            } else {
                byte[] send;
                try {
                    send = message.getBytes("GB2312");
                } catch (UnsupportedEncodingException e) {
                    send = message.getBytes();
                }
                mService.printLeft();
                mService.write(send);
            }
        } else {

        }
    }


    class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation arg0) {
            if (arg0 == null) {
                return;
            }
            double Latitude = arg0.getLatitude();
            double longitude = arg0.getLongitude();

            MyLog.d(TAG, "MyLocationListener定位回调--longitude = " + longitude + "--  Latitude = " + Latitude);
            if (Latitude != 0 && longitude != 0 && CheckUtils.LocationChecked(longitude + "")) {
//                if (mLocationClient != null && mLocationClient.isStarted()) {
//                    mLocationClient.stop();
//                }
                uploadLocationInfo(Latitude+"",longitude+"");
            } else {
                if (Latitude != 0 && longitude != 0 && longitude != 4.9E-324 && longitude != 0) {
//                    if (mLocationClient != null && mLocationClient.isStarted()) {
//                        mLocationClient.stop();
//                    }
                    uploadLocationInfo(Latitude+"",longitude+"");
                }
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            Log.e(TAG,i+s);
        }
    }

    // 十分钟上传一次我的位置（经纬度）；
    // collectorrequest.do?action=uploadll&token=aa9a48d2f41bb2722f29c8714cbc754c&lon=&lat=
    // 返回 1成功 其它失败

    public void uploadLocationInfo(String lat, String lng) {
        String token = BaseActivity.token;
        String path = Config.getUrl(this);
        String url = path + "collectorrequest.do?action=uploadll&token=" + token + "&lon=" + lng + "&lat=" + lat;
        MyLog.d(TAG, "上传经纬度的url是--->" + url);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (object != null) {
                    MyLog.d(TAG, "获取到上传经纬度的结果" + object);
                }
            }
        });

    }

}
