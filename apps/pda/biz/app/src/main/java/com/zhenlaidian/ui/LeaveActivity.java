/**
 *
 */
package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.MenuCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.lswss.QRCodeEncoder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.rsk.api.RskApi;
import com.zhenlaidian.MyApplication;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.DrawerAdapter;
import com.zhenlaidian.adapter.InTheVehicleAdapter;
import com.zhenlaidian.adapter.LeaveAdapter;
import com.zhenlaidian.bean.BaseRegisBean;
import com.zhenlaidian.bean.Berths;
import com.zhenlaidian.bean.BoWeiListEntity;
import com.zhenlaidian.bean.BoWeiStateEntity;
import com.zhenlaidian.bean.CarTypeItem;
import com.zhenlaidian.bean.DrawerItemInfo;
import com.zhenlaidian.bean.FinishOrderFailDialog;
import com.zhenlaidian.bean.InVehicleInfo;
import com.zhenlaidian.bean.LeaveOrder;
import com.zhenlaidian.bean.LoginInfo.WorksiteInfo;
import com.zhenlaidian.bean.MainUiInfo;
import com.zhenlaidian.bean.NfcOrder;
import com.zhenlaidian.bean.NfcPrepaymentOrder;
import com.zhenlaidian.bean.ParkingInfo;
import com.zhenlaidian.bean.RedPacketInfo;
import com.zhenlaidian.bean.RewardDialog;
import com.zhenlaidian.bean.SysApplication;
import com.zhenlaidian.bean.WatchInfo;
import com.zhenlaidian.bluetooth.BluetoothUtils;
import com.zhenlaidian.engine.DrawerOnItemClick;
import com.zhenlaidian.engine.SelectParkPositionListener;
import com.zhenlaidian.engine.SelectParkingPositionDialog;
import com.zhenlaidian.engine.ShowNfcFinishOrder;
import com.zhenlaidian.engine.ShowNfcNewOrder;
import com.zhenlaidian.photo.CarOrderActivity;
import com.zhenlaidian.printer.PrinterUitls;
import com.zhenlaidian.printer.TcbCheckCarOut;
import com.zhenlaidian.service.BLEService;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.ui.BaseActivity.MsgToMainListener;
import com.zhenlaidian.ui.score.SelectTicketActicity;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.Constant;
import com.zhenlaidian.util.Coverter;
import com.zhenlaidian.util.FileUtils;
import com.zhenlaidian.util.GPSHandler;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SendUrlLink;
import com.zhenlaidian.util.SharedPreferencesUtils;
import com.zhenlaidian.util.VoiceSynthesizerUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 主页离场订单消息；
 *
 * @author zhangyunfei 2015年7月29日
 */
@SuppressWarnings("deprecation")
public class LeaveActivity extends BaseActivity implements OnClickListener, MsgToMainListener, SelectParkPositionListener {

    public static LeaveAdapter adapter;
    private DrawerLayout drawerLayout = null;
    private ListView lv_left_drawer;
    public static Handler handler;
    public static String[][] TECHLISTS;
    public static IntentFilter[] FILTERS;
    private ActionBar actionBar;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private ActionBarDrawerToggle mDrawerToggle;

    static {
        try {
            TECHLISTS = new String[][]{{IsoDep.class.getName()}, {NfcB.class.getName()}, {NfcA.class.getName()},
                    {NfcV.class.getName()}, {NfcF.class.getName()},};
            FILTERS = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED, "text/plain")};
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String swipe;// 是否开启扫牌 0关闭，1开启；
    public static String name;
    public static String authflag;// 13为泊车员
    public static String qrcodeurl;// 收费二维码连接
    public static String ticketcodeurl;// 发券二维码连接；
    public static ArrayList<RedPacketInfo> ticketinfo;// 停车券详情
    public int mWhich = 0;
    public static String[] tqrcode = new String[2];
    private ProgressDialog nfcdialog;
    private String uuid;
    private boolean gpsDialog = true;
    public static boolean change_name = false;
    public static boolean isclosetcb = true;
    public static WatchInfo mWatchInfo;// 我的当班详情信息；
    private AlertDialog alertDialog;// 有未操作的订单框；
    public static ArrayList<WorksiteInfo> worksites;
    // 左右缩放动画：x轴从两边位置向中间缩小。y轴不动。
    ScaleAnimation anim1 = new ScaleAnimation(1, 0, 1, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    ScaleAnimation anim2 = new ScaleAnimation(0, 1, 1, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    // 上下缩放动画：y轴从中间位置向两边放大。x轴不动。
    ScaleAnimation animup = new ScaleAnimation(1, 1, 1, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    ScaleAnimation animdown = new ScaleAnimation(1, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    private ListView lv_main_order_msg;// 主页消息列表；
    private RelativeLayout rl_main_new_msg;// 主页顶部通知；
    private TextView tv_main_name, tv_main_account, tv_main_workstation, tv_main_fast_bluetooth;// 名字，账号，工作站，极速通
    public RelativeLayout rl_main_collect_money, rl_main_collect;// 收费状态二维码,收费设置金额；
    private RelativeLayout rl_main_send_ticket, rl_lv_main_qrcode;// 发专用券状态二维码；替换订单消息列表的父布局
    private ImageView iv_main_collect_money_qrcode, iv_main_send_ticket_qrcode, iv_call_phone;
    private TextView tv_ticket_total, tv_change_total, tv_consume_score, tv_change_qrcode;// 券金额，改变金额，消耗积分,切换二维码；
    private TextView tv_phone_collect, tv_today_reward, tv_today_score, tv_main_refresh;// 手机收费，今日打赏，我的积分，刷新按钮
    private Button bt_send_private_ticket, bt_collect_share_link, bt_ticket_share_link;// 发送专用券,分享收费二维码,分享发券二维码
    private LinearLayout ll_main_work_state;// 工作状态栏
    private TextView tv_income_and_scan, tv_leave_and_order, tv_main_income, tv_main_leave;// 扫牌,离场订单,今日入场车辆，今日离场车辆
    private RelativeLayout rl_main_car_inout, rl_main_leave_order;// 查看出入场车辆，查看离场订单；
    private TextView tv_change_income_satate, tv_ticket_refresh, tv_main_collect_time;// 切换,刷新二维码,设置出入场时间；
    // 收费设置金额。调整金额按钮，设置停车时长。去设置金额按钮；
    private TextView tv_main_collect_total, tv_main_check_collect_total, tv_main_collect_duration, tv_main_set_collect;
    private LinearLayout ll_main_qrcode;
    private View ll_main_parking_order;
    //    private  CurrnetOrderListFragment fragment;
//    private InTheVehicalFragment fragment;
    private VoiceSynthesizerUtil voice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leave_activity);
        MyApplication.isrunning = true;
        SysApplication.getInstance().addActivity(this);
        Constant.ISNEEDBACKUP = true;
//        SharedPreferencesUtils.getIntance(context).setview_plot("0");
        voice = new VoiceSynthesizerUtil(context);
//        voice.playText("登录主界面");
//        CommontUtils.toast(context,"printer.Open()=="+ printer.Open());printer.Open();
        initVeiw();
        //此处需要判断是否有泊位段，如果有，则进入泊位列表
        berths = (ArrayList<Berths>) getIntent().getSerializableExtra("berths");
        showBoWeiGroup();

        initHandler();
        displayQrcodeView();
        initActionBar();
        getLoginInfo();
        getWatchInfo();// 获取手机收费、打赏、积分数据；
        getParkinfo();
        lv_left_drawer.setAdapter(new DrawerAdapter(DrawerItemInfo.getInstance(), this));
        lv_left_drawer.setOnItemClickListener(new DrawerOnItemClick(this, drawerLayout, this));// 抽屉的条目点击
        lv_left_drawer.setScrollingCacheEnabled(false);// 设置抽屉的listview不能滑动；
        adapter = new LeaveAdapter(this);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), Toast.LENGTH_SHORT);
        onNewIntent(getIntent());
        initService();
        getCenterMessage(token);// 去查消息中心是否有新消息；
        BLEService.setOnConnectListener(this);// 给BLE蓝牙服务设置监听回调
        PullMsgService.setOnConnectListener(this);// 给消息服务设置监听回调

//        txtCash = ((TextView) findViewById(R.id.getmoney_cash));
//        txtCard = ((TextView) findViewById(R.id.getmoney_card));
//        txtElec = ((TextView) findViewById(R.id.getmoney_elec));
        btnLogout = ((Button) findViewById(R.id.getmoney_logout));
        if (SharedPreferencesUtils.getIntance(context).getview_plot().equals("1")) {
//            btnLogout.setVisibility(View.VISIBLE);
            lnNewBar.setVisibility(View.VISIBLE);
            lnOldBar.setVisibility(View.GONE);
            lnBoWeiName.setVisibility(View.VISIBLE);
        } else {
//            btnLogout.setVisibility(View.GONE);
            lnNewBar.setVisibility(View.GONE);
            lnOldBar.setVisibility(View.VISIBLE);
            lnBoWeiName.setVisibility(View.GONE);
        }
        btnLogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ShouFeiDetail.class);
                startActivityForResult(i, 1234);

            }
        });
        FileUtils.fileRegularDelete();
        RskApi.ZGOpenPower();
    }

    //    private TextView txtCash, txtCard, txtElec;
    private Button btnLogout;
    boolean isShowGrid = false;

    //初始化显示订单列表fragment;
    public void initFragmentList() {
        lv_main_order_msg.setVisibility(View.GONE);
        ll_main_qrcode.setVisibility(View.GONE);
        ll_main_parking_order.setVisibility(View.VISIBLE);
//        if (getSupportFragmentManager().findFragmentByTag("currlist") == null) {
//            if (fragment == null) {
////                fragment = new CurrnetOrderListFragment();
//                fragment = new InTheVehicalFragment();
//            }
//            getSupportFragmentManager().beginTransaction().add(R.id.ll_main_parking_order, fragment, "currlist").commit();
//        }else{
//
//        }
        getInVehicleInfo();
        isShowGrid = true;
    }

    //隐藏fragment
    public void HideFragmentList() {
        lv_main_order_msg.setVisibility(View.GONE);
        ll_main_qrcode.setVisibility(View.VISIBLE);
        ll_main_parking_order.setVisibility(View.GONE);
        isShowGrid = false;
        Constant.BerthFresh = true;
//        if (fragment != null) {
//            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
//        }
//        getTodayMoney();
    }

    public void refreshOrderFragment() {
//        if (fragment != null) {
//            fragment.refresh();
//        }
        getInVehicleInfo();
    }

    @SuppressLint("HandlerLeak")
    public void initHandler() {
        handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:// 获取到离场订单
//                        setLeaveOrder(msg);

//                        Message m = new Message();
//                        m.what = Constant.WxCode;
//                        m.obj = msg.obj;
//                        h2.sendMessage(m);

                        Intent intent = new Intent();
                        intent.putExtra("epay", (Serializable) msg.obj);
                        intent.setAction("E_PAY");
                        sendBroadcast(intent);

                        LeaveOrder order = (LeaveOrder) msg.obj;
                        new AlertDialog.Builder(LeaveActivity.this).setTitle("订单消息").setIcon(R.drawable.app_icon_32)
                                .setMessage(TextUtils.isEmpty(order.getMessage()) ? "车主已成功支付停车费" + order.getTotal() + "元" : order.getMessage())
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).setCancelable(false).create().show();
                        break;
                    case 2:// 更新主页数据；
                        MainUiInfo info = (MainUiInfo) msg.obj;
                        changeMainUi(info);
                        break;
                    case 3:// 修改车场信息后重新初始化车场总数。
                        break;
                    case 4:// token失效。
                        relogins();
                        break;
                    case 5:// 打开Gps定位弹框。
                        openGPS(LeaveActivity.this);
                        break;
                    case 6:// nfc刷卡消息。
                        boolean flag = SharedPreferencesUtils.getIntance(LeaveActivity.this).getMainActivity();
                        if (flag) {
                            String object = (String) msg.obj;
                            showOrderDialog(object, uuid);
                        }
                        break;
                    case 7:// 还有未处理订单；
                        noFinishOrder(LeaveActivity.this);
                        break;
                    case 8:// 泊车状态
                        break;
                    case 9:// 修改泊车状态
                        break;
                    case 10:// 蓝牙工作站绑定状态。
                        try {
                            tv_main_workstation.setText("离岗");
                            SharedPreferencesUtils.getIntance(LeaveActivity.this).setWorksite(worksites.size() - 1);
                            tv_main_workstation.setTextColor(getResources().getColor(R.color.red));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 11:// 蓝牙抬杆状态；
                        if ((boolean) msg.obj) {
                            tv_main_fast_bluetooth.setText("极速通");
                            tv_main_fast_bluetooth.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
                        } else {
                            tv_main_fast_bluetooth.setText("未连接");
                            tv_main_fast_bluetooth.setTextColor(getResources().getColor(R.color.red));
                        }
                        break;
                    case 12:// 弹出BLE补交差价对话框；
                        notBalanceDialog(msg);
                        break;
                    case 13:// 弹出BLE结算错误对话框
                        showBLEFailDialog(msg);
                        break;
                    case 14:// 收到打赏的消息；
                        ShowRewardDialog(msg);
                        break;
                    case 15:// 收到主页顶部通知的消息；
                        rl_main_new_msg.setVisibility(View.VISIBLE);
                        ll_main_work_state.setVisibility(View.GONE);
                        break;
                    case 16:// 收到积分被扣的消息；
                        getAccountScore();
                        changeMainUi(new MainUiInfo(false, 3, (Double) msg.obj));
                        String Score = SharedPreferencesUtils.getIntance(LeaveActivity.this).getMyScore();
                        if (!TextUtils.isEmpty(Score)
                                && Double.parseDouble(Score) > Double.parseDouble(tv_consume_score.getText().toString())) {
                            tv_ticket_refresh.setClickable(false);
                            if (tqrcode[0].equals("3")) {
                                getQRCode(tqrcode[0], tqrcode[1]);
                            } else {
                                getRewardScoreInfo(true);
                            }
                        }
                        break;
                    case 17:// 收到推荐成功的消息；
                        showRecommendDialog(msg);
                        break;
                    case 18://泊位变动通知
                        ArrayList<BoWeiStateEntity> bo = (ArrayList<BoWeiStateEntity>) msg.obj;
//                        ArrayList<BoWeiStateEntity> bo = new ArrayList<BoWeiStateEntity>();
                        Constant.boweiMsg.clear();
//                        CommontUtils.toast(context,Constant.boweiMsg.size()+"消息");
                        System.out.println("消息" + bo.size());
                        try {
                            if (CommontUtils.checkList(bo)) {
                                String boweiid = bo.get(0).getId();
                                String boweinum;
                                if (CommontUtils.checkList(Constant.boweiMsgAll)) {
                                    for (int i = 0; i < Constant.boweiMsgAll.size(); i++) {
                                        if (boweiid.equals(Constant.boweiMsgAll.get(i).getId())) {
                                            String s = Constant.boweiMsgAll.get(i).getBer_name();
                                            boweinum = s.substring(s.length() - 3);
                                            if (bo.get(0).getState().endsWith("1")) {
                                                if (!CommontUtils.checkString(Constant.boweiMsgAll.get(i).getOrderid())) {
                                                    voice.playText("有车进场泊位" + boweinum);
                                                }
                                            } else {
                                                if (CommontUtils.checkString(Constant.boweiMsgAll.get(i).getOrderid())) {
                                                    voice.playText("有车离场泊位" + boweinum);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    voice.playText((bo.get(0).getState().endsWith("1") ? "有车进场" : "有车离场"));
                                }
                                Constant.boweiMsg.addAll(bo);
                                getInVehicleInfo();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1028:
                        getInVehicleInfo();
                        break;
                    default:
                        break;
                }
            }
        };
    }
//    public static ArrayList<BoWeiStateEntity> bowei = new ArrayList<BoWeiStateEntity>();

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_main_new_msg:// 弹出通知的对话框
                rl_main_new_msg.setClickable(false);
                getParkinfo();
                break;
            case R.id.tv_main_workstation:// 弹出选择工作站的对话框
                showIbeaconDialog();
                break;
            case R.id.tv_main_change_ticket_total:// 弹出更改金额的对话框
                String myScore = SharedPreferencesUtils.getIntance(LeaveActivity.this).getMyScore();
                if (!TextUtils.isEmpty(myScore) && Double.parseDouble(myScore) > 6.0) {
                    showTicketDialog();
                } else {
                    Toast.makeText(LeaveActivity.this, "积分不足,无法发券！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_main_change_qrcode:// 切换收费和发券二维码；
                if (rl_main_collect_money.getVisibility() == View.VISIBLE) {
                    String mScore = SharedPreferencesUtils.getIntance(LeaveActivity.this).getMyScore();
                    if (!TextUtils.isEmpty(mScore) && Double.parseDouble(mScore) >= 6.0) {
                        rl_main_collect_money.startAnimation(anim1);
                        v.setBackgroundResource(R.drawable.main_bt_money);
                        if (ticketinfo == null || ticketinfo.size() == 0) {
                            getRewardScoreInfo(false);
                        }
                    } else {
                        Toast.makeText(LeaveActivity.this, "积分不足,无法发券！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    rl_main_send_ticket.startAnimation(anim1);
                    v.setBackgroundResource(R.drawable.main_send_ticket_bg);
                }
                break;
            case R.id.tv_main_refresh:// 刷新主页数据
                Animation operatingAnim = AnimationUtils.loadAnimation(LeaveActivity.this, R.anim.botton_rotate);
                LinearInterpolator lin = new LinearInterpolator();
                operatingAnim.setInterpolator(lin);
                tv_main_refresh.startAnimation(operatingAnim);
                getWatchInfo();
                break;
//            case R.id.tv_main_refresh2:// 刷新主页--收费金额
//                Animation operatingAnim2 = AnimationUtils.loadAnimation(LeaveActivity.this, R.anim.botton_rotate);
//                LinearInterpolator lin2 = new LinearInterpolator();
//                operatingAnim2.setInterpolator(lin2);
//                if (operatingAnim2 != null) {
//                    tv_main_refresh2.startAnimation(operatingAnim2);
//                }
//                getTodayMoney();
//                break;
            case R.id.bt_main_send_private_ticket:// 跳转到用积分发专用券的页面；
                String mScore = SharedPreferencesUtils.getIntance(LeaveActivity.this).getMyScore();
                if (!TextUtils.isEmpty(mScore) && Double.parseDouble(mScore) >= 6.0) {
                    Intent Sintent = new Intent(LeaveActivity.this, SelectTicketActicity.class);
                    startActivity(Sintent);
                } else {
                    Toast.makeText(LeaveActivity.this, "积分不足,无法发券！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.tv_main_income_and_scan:// 扫车牌入场
                //改为大键盘界面
                Intent i = new Intent(context, InputCarNumberActivity.class);
                i.putExtra("from", "input");
                startActivity(i);
                break;
            case R.id.tv_main_leave_and_leave_order:// 跳转到离场订单
                Intent intent = new Intent(LeaveActivity.this, CarOrderActivity.class);
//                if (fragment != null) {
                intent.putExtra("infos", infos);
//                }
                startActivity(intent);
//                Intent intent3 = new Intent(LeaveActivity.this, InTheVehicleActivity.class);
//                startActivity(intent3);
                break;
            case R.id.tv_main_change_income_satate:// 切换看出入场车辆
                if (SharedPreferencesUtils.getIntance(this).getview_plot().equals("1")) {
//                    if (getSupportFragmentManager().findFragmentByTag("currlist") == null) {
                    if (!isShowGrid) {
                        initFragmentList();
                        tv_change_income_satate.setText("菜单");
                    } else {
                        HideFragmentList();
                        tv_change_income_satate.setText("泊位");
                    }
                    lnNewBar.setVisibility(View.VISIBLE);
                    lnOldBar.setVisibility(View.GONE);
                    lnBoWeiName.setVisibility(View.VISIBLE);
                } else {
                    tv_change_income_satate.setVisibility(View.GONE);
//                    btnLogout.setVisibility(View.GONE);
                    lnNewBar.setVisibility(View.GONE);
                    lnOldBar.setVisibility(View.VISIBLE);
                    lnBoWeiName.setVisibility(View.GONE);
                    HideFragmentList();
                }

//                if (rl_main_leave_order.getVisibility() == View.VISIBLE) {
//                    rl_main_leave_order.startAnimation(animup);
//                } else {
//                    rl_main_car_inout.startAnimation(animup);
//                }
                break;
            case R.id.tv_main_ticket_refresh:// 刷新发券二维码
                RotateAnimation anim = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setRepeatCount(-1);
                anim.setDuration(500);
                String Score = SharedPreferencesUtils.getIntance(LeaveActivity.this).getMyScore();
                if (!TextUtils.isEmpty(Score)
                        && Double.parseDouble(Score) > Double.parseDouble(tv_consume_score.getText().toString())) {
                    v.startAnimation(anim);
                    tv_ticket_refresh.setClickable(false);
                    if (tqrcode[0].equals("3")) {
                        getQRCode(tqrcode[0], tqrcode[1]);
                    } else {
                        getRewardScoreInfo(true);
                    }
                } else {
                    Toast.makeText(LeaveActivity.this, "积分不足,无法继续发券！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_collect_share_link:// 分享收费二维码连接；
                String total = tv_main_collect_total.getText().toString();
                if (TextUtils.isEmpty(total)) {
                    SendUrlLink send = new SendUrlLink(LeaveActivity.this, baseurl + qrcodeurl, "点我向收费员" + name + "(" + useraccount
                            + ")" + "付停车费", parkname, "collect_icon.png");
                    send.showShare();
                } else {
                    SendUrlLink send = new SendUrlLink(LeaveActivity.this, baseurl + qrcodeurl + "&total=" + total, "点我向收费员" + name
                            + "(" + useraccount + ")" + "付停车费", parkname + "" + ": " + total + "元", "collect_icon.png");
                    send.showShare();
                }
                break;
            case R.id.bt_ticket_share_link:// 分享发券二维码链接
                SendUrlLink send = new SendUrlLink(LeaveActivity.this, ticketcodeurl, "点我领取收费员" + name + "(" + useraccount + ")"
                        + "赠送的专用券,限领一次", parkname + "专用券", "private_packet.png");
                send.showShare();
                String score = SharedPreferencesUtils.getIntance(LeaveActivity.this).getMyScore();
                if (!TextUtils.isEmpty(score)
                        && Double.parseDouble(score) > Double.parseDouble(tv_consume_score.getText().toString())) {
                    tv_ticket_refresh.setClickable(false);
                    if (tqrcode[0].equals("3")) {
                        getQRCode(tqrcode[0], tqrcode[1]);
                    } else {
                        getRewardScoreInfo(true);
                    }
                } else {
                    Toast.makeText(LeaveActivity.this, "积分不足,无法继续发券！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ll_pay_log_delete:
                String edt = edtSearch.getText().toString();
                if (!TextUtils.isEmpty(edt)) {
                    edtSearch.setText(edt.substring(0, edt.length() - 1));
                    edtSearch.setSelection(edtSearch.getText().toString().length());
                }

                break;
            default:
                break;
        }
        anim1.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (rl_main_collect_money.getVisibility() == View.VISIBLE) {
                    rl_main_collect_money.clearAnimation();
                    rl_main_collect_money.setVisibility(View.INVISIBLE);
                    rl_main_send_ticket.setVisibility(View.VISIBLE);
                    rl_main_send_ticket.startAnimation(anim2);
                } else {
                    rl_main_send_ticket.clearAnimation();
                    rl_main_send_ticket.setVisibility(View.INVISIBLE);
                    rl_main_collect_money.setVisibility(View.VISIBLE);
                    rl_main_collect_money.startAnimation(anim2);
                }

            }
        });
        animup.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (rl_main_leave_order.getVisibility() == View.VISIBLE) {
                    rl_main_leave_order.clearAnimation();
                    rl_main_leave_order.setVisibility(View.INVISIBLE);
                    rl_main_car_inout.setVisibility(View.VISIBLE);
                    rl_main_car_inout.startAnimation(animdown);
                } else {
                    rl_main_car_inout.clearAnimation();
                    rl_main_leave_order.setVisibility(View.VISIBLE);
                    rl_main_car_inout.setVisibility(View.INVISIBLE);
                    rl_main_leave_order.startAnimation(animdown);
                }
            }
        });
    }
//
//    // 显示离场订单消息列表。
//    public void displayLeaverOrderView() {
//        lv_main_order_msg.setVisibility(View.VISIBLE);
//        rl_lv_main_qrcode.setVisibility(View.GONE);
//    }

    // 显示收费二维码界面
    public void displayQrcodeView() {
        lv_main_order_msg.setVisibility(View.GONE);
        rl_lv_main_qrcode.setVisibility(View.VISIBLE);
        rl_main_collect_money.setVisibility(View.VISIBLE);
        rl_main_send_ticket.setVisibility(View.GONE);
        tv_change_qrcode.setBackgroundResource(R.drawable.main_send_ticket_bg);
        // 默认显示离场结算界面；
        rl_main_leave_order.setVisibility(View.VISIBLE);
        rl_main_car_inout.setVisibility(View.INVISIBLE);
    }

    public void setView(WatchInfo info) {
        if (info.getMobilemoney() != null) {
            tv_phone_collect.setText(info.getMobilemoney());
        }
        tv_today_reward.setText(info.getRewardmoney());
        tv_today_score.setText(info.getTodayscore());
        tv_main_income.setText(info.getTodayin());
        tv_main_leave.setText(info.getTodayout());
        if (info.getTodayscore() != null) {
            SharedPreferencesUtils.getIntance(this).setMyScore(info.getTodayscore());
        }
    }

    // 更新主界面数据；
    public synchronized void changeMainUi(MainUiInfo info) {
        if (info == null) {
            return;
        }
        switch (info.getType()) {
            case 1:// 今日手机收费
                Double Ptotal = Double.parseDouble(!TextUtils.isEmpty(tv_phone_collect.getText().toString()) ? tv_phone_collect
                        .getText().toString() : "0.0");
                if (info.getAdd()) {
                    tv_phone_collect.setText(String.format("%.2f", Ptotal + info.getScore()));
                } else {
                    tv_phone_collect.setText(String.format("%.2f", Ptotal - info.getScore()));
                }
                break;
            case 2:// 今日打赏
                Double Rtotal = Double.parseDouble(!TextUtils.isEmpty(tv_today_reward.getText().toString()) ? tv_today_reward
                        .getText().toString() : "0.0");
                if (info.getAdd()) {
                    tv_today_reward.setText(String.format("%.2f", Rtotal + info.getScore()));
                } else {
                    tv_today_reward.setText(String.format("%.2f", Rtotal - info.getScore()));
                }
                break;
            case 3:// 今日积分
                Double Stotal = Double.parseDouble(!TextUtils.isEmpty(tv_today_score.getText().toString()) ? tv_today_score.getText()
                        .toString() : "0.0");
                if (info.getAdd()) {
                    tv_today_score.setText(String.format("%.2f", Stotal + info.getScore()));
                } else {
                    if (Stotal < info.getScore()) {
                        tv_today_score.setText("0.0");
                    } else {
                        tv_today_score.setText(String.format("%.2f", Stotal - info.getScore()));
                    }
                }
                break;
            case 4:// 今日入场
                int Itotal = Integer.parseInt(!TextUtils.isEmpty(tv_main_income.getText().toString()) ? tv_main_income.getText()
                        .toString() : "0");
                if (info.getAdd()) {
                    tv_main_income.setText((int) (Itotal + info.getScore()) + "");
                } else {
                    tv_main_income.setText((int) (Itotal - info.getScore()) + "");
                }
                break;
            case 5:// 今日离场
                int Ltotal = Integer.parseInt(!TextUtils.isEmpty(tv_main_leave.getText().toString()) ? tv_main_leave.getText()
                        .toString() : "0");
                if (info.getAdd()) {
                    tv_main_leave.setText((int) (Ltotal + info.getScore()) + "");
                } else {
                    tv_main_leave.setText((int) (Ltotal + info.getScore()) + "");
                }
                break;
            default:
                break;
        }
    }

    private ArrayList<Berths> berths = new ArrayList<Berths>();

    @SuppressWarnings("unchecked")
    public void getLoginInfo() {// 获取登陆成功后的信息；
        if (getIntent().getExtras() != null) {
            swipe = (String) getIntent().getExtras().get("swipe");
            name = (String) getIntent().getExtras().get("name");
            authflag = (String) getIntent().getExtras().get("authflag");
            qrcodeurl = getIntent().getExtras().getString("qrcode");
            worksites = (ArrayList<WorksiteInfo>) getIntent().getExtras().get("worksite");

            if (worksites != null && worksites.size() > 0) {
//                 showIbeaconDialog();
                openBluetoothService();
                // 打开通道蓝牙服务；****************************************************************************
            } else {
                tv_main_workstation.setVisibility(View.INVISIBLE);
                tv_main_fast_bluetooth.setVisibility(View.INVISIBLE);
            }
            tv_main_name.setText(name == null ? "" : name);
            tv_main_account.setText(useraccount == null ? "" : useraccount);
            SharedPreferencesUtils utils = SharedPreferencesUtils.getIntance(this);
            if (!TextUtils.isEmpty(qrcodeurl)) {
                createQrcode(baseurl + qrcodeurl);// 动态构建收费员账号相关的二维码；
                utils.setCode("http://www.tingchebao.com/zld/carservice.do?pid=" + useraccount + "&name="
                        + (name != null ? name : ""));
            } else {
                createQrcode(baseurl + "carservice.do?pid=" + useraccount + "&name=" + (name != null ? name : ""));// 动态构建收费员账号相关的二维码；
                utils.setCode("http://www.tingchebao.com/zld/carservice.do?pid=" + useraccount + "&name="
                        + (name != null ? name : ""));
            }
        }
    }

    /**
     * 弹出选择标段 泊位段 对话框
     */
    public void showBoWeiGroup() {
        //登陆信息中是否返回了已签到的泊位号,如果已返回，则不弹出选择泊位段对话框，直接刷泊位列表，并提示签到时间
        if (Integer.parseInt(SharedPreferencesUtils.getIntance(LeaveActivity.this).getberthid()) > -1) {
            putBooleanToPreference("already", false);
            if (SharedPreferencesUtils.getIntance(this).getview_plot().equals("1")) {
//                if (getSupportFragmentManager().findFragmentByTag("currlist") == null) {
                if (!isShowGrid) {
                    initFragmentList();
                    tv_change_income_satate.setText("菜单");
                } else {
                    HideFragmentList();
                    tv_change_income_satate.setText("泊位");

                }
                String stationname = SharedPreferencesUtils.getIntance(context).getParkname() +
                        "—" + SharedPreferencesUtils.getIntance(context).getberthsec_name();
//                String stationname = SharedPreferencesUtils.getIntance(context).getParkname();
                txtBoweiName.setText(stationname);
                lnBoWeiName.setVisibility(View.VISIBLE);

                lnNewBar.setVisibility(View.VISIBLE);
                lnOldBar.setVisibility(View.GONE);
            } else {
                tv_change_income_satate.setVisibility(View.GONE);
                lnBoWeiName.setVisibility(View.GONE);

                lnNewBar.setVisibility(View.GONE);
                lnOldBar.setVisibility(View.VISIBLE);

                HideFragmentList();
            }

        } else {
            if (!CommontUtils.checkList(berths)) {
                return;
            }
            final ArrayList<Berths> info = berths;
            final String[] berth = new String[info.size()];
//            final String[] id = new String[info.size()];
            for (int i = 0; i < info.size(); i++) {
                berth[i] = info.get(i).getBerthsec_name();
//                id[i] = info.get(i).getId();
            }
            int worksited = SharedPreferencesUtils.getIntance(this).getWorksite();
            if (SharedPreferencesUtils.getIntance(this).getview_plot().equals("1")) {
                new AlertDialog.Builder(this).setTitle("请选择泊位段").setIcon(R.drawable.app_icon_32)
                        .setSingleChoiceItems(berth, worksited, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                Log.e("SetActivity", "点击的which是" + which);
//                        bindingIbeacon(id[which], berth[which]);
                                putBooleanToPreference("already", false);
                                SharedPreferencesUtils.getIntance(LeaveActivity.this).setberthid(info.get(which).getId());
                                SharedPreferencesUtils.getIntance(LeaveActivity.this).setberthsec_name(info.get(which).getBerthsec_name());

                                dialog.dismiss();
                                if (SharedPreferencesUtils.getIntance(LeaveActivity.this).getview_plot().equals("1")) {
                                    initFragmentList();
                                }
                            }
                        }).setCancelable(false).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FinishAction();
                    }
                }).create().show();
            } else {
                tv_change_income_satate.setVisibility(View.GONE);
            }

        }

    }

    public void initActionBar() {
        drawerLayout.setDrawerListener(new MyDrawerListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_am, R.string.hello_world,
                R.string.hello_world);
        mDrawerToggle.syncState();
        actionBar = getSupportActionBar();
        actionBar.setTitle("主页");
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setIcon(R.drawable.app_icon);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    /**
     * 抽屉的监听
     */
    private class MyDrawerListener implements DrawerLayout.DrawerListener {
        @Override
        public void onDrawerOpened(View drawerView) {// 打开抽屉的回调
            mDrawerToggle.onDrawerOpened(drawerView);
            actionBar.setTitle("停车宝");
        }

        @Override
        public void onDrawerClosed(View drawerView) {// 关闭抽屉的回调
            mDrawerToggle.onDrawerClosed(drawerView);
            actionBar.setTitle("主页");
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {// 抽屉滑动的回调
            mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
        }

        @Override
        public void onDrawerStateChanged(int newState) {// 抽屉状态改变的回调
            mDrawerToggle.onDrawerStateChanged(newState);
        }
    }

    @SuppressLint("InlinedApi")
    public boolean onCreateOptionsMenu(Menu menu) {
        MyLog.w("LeaveActivity", "onCreateOptionsMenu");
        //2016.4.20暂时隐藏一键对账，将收费明细放到首页
        getMenuInflater().inflate(R.menu.leave_actionbar, menu);
        MenuCompat.setShowAsAction(menu.findItem(R.id.query), MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    // actionBar的点击回调方法
    @SuppressLint("RtlHardcoded")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MyLog.w("LeaveActivity", "onOptionsItemSelected");
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
                return true;
            case R.id.query:
//                Intent intent = new Intent(LeaveActivity.this, OneKeyQueryActivity.class);
//                startActivity(intent);
                Intent i = new Intent(context, InputCarNumberActivity.class);
                i.putExtra("from", "search");
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 开启极速通服务模块;
     */
    public void openBluetoothService() {
        if (BluetoothUtils.isSupportIbeacon(this)) {
            tv_main_fast_bluetooth.setText("未连接");
            tv_main_fast_bluetooth.setTextColor(getResources().getColor(R.color.red));
            Intent bleService = new Intent(this, BLEService.class);
            startService(bleService);
            MyLog.w("LeaveActivity", "开启极速通低功耗蓝牙服务--->");
            if (getResources().getString(R.string.baidukey).equals("k0XC18nUQ0Mdl24zg1VPCnW5")) {
                tv_main_fast_bluetooth.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 测试阶段手动选择极速通标示；
                        final EditText BLEText = new EditText(LeaveActivity.this);
                        BLEText.setText(SharedPreferencesUtils.getIntance(LeaveActivity.this).getBLEName());
                        new AlertDialog.Builder(LeaveActivity.this).setTitle("输入极速通设备名字").setView(BLEText)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SharedPreferencesUtils.getIntance(LeaveActivity.this).setBLEName(
                                                BLEText.getText().toString().trim());
                                    }
                                }).setNegativeButton("取消", null).show();
                    }
                });
            }
        } else {
            Toast.makeText(this, "车场支持蓝牙，请更换安卓4.3以上版本的手机", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 弹出选择绑定Ibeacon工作站对话框；
     */
    public void showIbeaconDialog() {
        if (worksites == null) {
            return;
        }
        final ArrayList<WorksiteInfo> info = worksites;
        final String[] worksite = new String[info.size()];
        final String[] id = new String[info.size()];
        for (int i = 0; i < info.size(); i++) {
            worksite[i] = info.get(i).getWorksite_name();
            id[i] = info.get(i).getId();
        }
        int worksited = SharedPreferencesUtils.getIntance(this).getWorksite();
        new AlertDialog.Builder(this).setTitle("请选择要绑定的出口").setIcon(R.drawable.app_icon_32)
                .setSingleChoiceItems(worksite, worksited, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("SetActivity", "点击的which是" + which);
                        bindingIbeacon(id[which], worksite[which]);
                        SharedPreferencesUtils.getIntance(LeaveActivity.this).setWorksite(which);
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(tv_main_workstation.getText())) {
                    bindingIbeacon("-1", "离岗");
                    SharedPreferencesUtils.getIntance(LeaveActivity.this).setWorksite(info.size() - 1);
                }
            }
        }).create().show();
    }

    /**
     * 开启后台定时服务
     */
    public void initService() {
        // 启动轮询获取离场订单服务
        Intent service = new Intent(this, PullMsgService.class);
        startService(service);
        MyLog.w("LeaveActivity", "开启获取离场订单服务--->");
    }

    /**
     * 绘制二维码图案
     */
    public void createQrcode(String info) {
        QRCodeEncoder d = new QRCodeEncoder();
        Bitmap encode2BitMap = d.encode2BitMap(info, 400, 400);
        iv_main_collect_money_qrcode.setImageBitmap(encode2BitMap);
    }

    public void setAdapter() {
        lv_main_order_msg.setAdapter(adapter);
    }

//    private Handler h2 = OrderJieSuanActivity.h2;

//    // 获取到离场订单消息；
//    public void setLeaveOrder(Message msg) {
//        LeaveOrder order = (com.zhenlaidian.bean.LeaveOrder) msg.obj;
//        MyLog.i("LeaveActivity", "获取到了消息");
//        if (order != null) {
//            if (order.getOrderid() != null && order.getOrderid().equals("-1")) {// 订单号为-1表示直付；
//                changeMainUi(new MainUiInfo(true, 4, 1.00));
//                changeMainUi(new MainUiInfo(true, 5, 1.00));
//                if (order.getTotal() != null) {
//                    changeMainUi(new MainUiInfo(true, 1, Double.parseDouble(order.getTotal())));
//                }
//                ScanMyCodeCash(order);
//            } else {
//                displayLeaverOrderView();
//                adapter.addOrder(order);
//                changeMainUi(new MainUiInfo(true, 5, 1.00));
//                if (order.getTotal() != null) {
//                    changeMainUi(new MainUiInfo(true, 1, Double.parseDouble(order.getTotal())));
//                }
//            }
//        }
//    }

//    // 扫码直付成功后弹出对话框；
//    public void ScanMyCodeCash(LeaveOrder order) {
//        EPayMessageDialog dialog = new EPayMessageDialog(this, R.style.nfcnewdialog, order);
//        dialog.setCanceledOnTouchOutside(false);
//        dialog.setCancelable(false);
//        dialog.show();
//    }

    // 弹出收到打赏消息框；
    public void ShowRewardDialog(Message msg) {
        LeaveOrder order = (com.zhenlaidian.bean.LeaveOrder) msg.obj;
        if (!TextUtils.isEmpty(order.getTotal())) {
            changeMainUi(new MainUiInfo(true, 1, Double.parseDouble(order.getTotal())));
            changeMainUi(new MainUiInfo(true, 2, Double.parseDouble(order.getTotal())));
            changeMainUi(new MainUiInfo(true, 3, Double.parseDouble(order.getTotal()) * Double.parseDouble(order.getRcount())));
            String myScore = SharedPreferencesUtils.getIntance(this).getMyScore();
            Double d = Double.parseDouble(myScore) + Double.parseDouble(order.getTotal());
            SharedPreferencesUtils.getIntance(this).setMyScore(String.format("%.2f", d));
            getAccountScore();
        }
        RewardDialog dialog = new RewardDialog(this, R.style.nfcnewdialog, order);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 主页通知弹框
     */
    public void showMsgDialog(final Bitmap bmp) {
        final AlertDialog qrCodeDialog = new AlertDialog.Builder(this).create();
        qrCodeDialog.setCancelable(true);
        qrCodeDialog.setCanceledOnTouchOutside(false);
        View dialog_view = View.inflate(this, R.layout.dialog_msgdialog, null);
        ImageView iv_msg = (ImageView) dialog_view.findViewById(R.id.iv_dialog_msgdialog);
        Button bt_msg = (Button) dialog_view.findViewById(R.id.bt_dialog_magdialog);
        Button bt_detail = (Button) dialog_view.findViewById(R.id.bt_dialog_details);
        // final DisplayMetrics displayMetrics = new DisplayMetrics();
        // getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // int width = displayMetrics.widthPixels;
        // LayoutParams params = new LayoutParams(width * 8 / 9, width * 8 / 9);
        // iv_msg.setLayoutParams(params);
        // LayoutParams params2 = new LayoutParams(width * 8 / 9, width * 1 /
        // 6);
        // bt_msg.setLayoutParams(params2);
        iv_msg.setImageBitmap(bmp);
        bt_msg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                qrCodeDialog.dismiss();
            }
        });
        bt_detail.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 查看通知详情
                Intent intent = new Intent(LeaveActivity.this, ScoreRuleActivity.class);
                intent.putExtra("type", 1);
                startActivity(intent);
                qrCodeDialog.dismiss();
            }
        });
        qrCodeDialog.show();
        qrCodeDialog.setContentView(dialog_view);
    }

    // 返回键退出时提示
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (CommontUtils.Is910()) {
                return true;
            } else {
                AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
                mDialog.setTitle("操作提示");
                mDialog.setMessage("后台运行停车宝？");
                mDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Intent i = new Intent(Intent.ACTION_MAIN);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addCategory(Intent.CATEGORY_HOME);
                        startActivity(i);
                    }
                });
                mDialog.setNegativeButton("取消", null);
                mDialog.show();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // token无效后去登陆界面；
    public void relogins() {
        Intent bleService = new Intent(this, BLEService.class);
        Intent pullService = new Intent(this, PullMsgService.class);
        this.stopService(bleService);
        this.stopService(pullService);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("token", "false");
        startActivity(intent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        MyLog.v("LeaveActivity", "onNewIntent-----");
        setIntent(intent);
        if (getIntent().getExtras() != null && getIntent().getExtras().getString("token") != null) {
            swipe = (String) getIntent().getExtras().get("swipe");
            name = (String) getIntent().getExtras().get("name");
            authflag = (String) getIntent().getExtras().get("authflag");
        }
        if (getIntent().getExtras() != null && getIntent().getExtras().getString("posnfcorder") != null) {
            showOrderDialog(getIntent().getExtras().getString("posnfcorder"), "");
        }
        // 得到是否检测到ACTION_TECH_DISCOVERED触发
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            // 处理该intent
            resolveIntent(getIntent());
            // readNfcTag(getIntent());
            // writeDB(intent);
            MyLog.w("LeaveActivity", "ACTION_NDEF_DISCOVERED-----");
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {
            resolveIntent(getIntent());
            // readNfcTag(getIntent());
            // writeDB(intent);
            MyLog.w("LeaveActivity", "ACTION_TAG_DISCOVERED-----");
            MyLog.w("LeaveActivity", "" + NfcAdapter.ACTION_TAG_DISCOVERED + "" + getIntent().getAction());
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
            resolveIntent(getIntent());
            // readNfcTag(getIntent());
            // writeDB(intent);
            MyLog.w("LeaveActivity", "ACTION_TECH_DISCOVERED----");
        }
    }

    public void resolveIntent(Intent intent) {
        uuid = Coverter.getUid(intent);
//        String text = "本标签的UID为" + "【" + uuid + "】";
        if (!TextUtils.isEmpty(uuid)) {
            if (token != null) {
                getNfcInfo(uuid, "0");// 提交nfc卡uid编号；
            } else {
                Intent loginintent = new Intent(LeaveActivity.this, LoginActivity.class);
                startActivity(loginintent);
                LeaveActivity.this.finish();
            }
        }
    }

    // 收费员刷卡接口；
    // http://192.168.1.102/zld/nfchandle.do?action=nfcincom&uuid=0458f902422d80&comid=3&uid=
    // 老接口
    // nfchandle.do?action=incom&uuid=0459C402773480&comid=924&esctype= 新接口
    // esctype:是否不查逃单,0查逃单，1不查
    public void getNfcInfo(final String uuid, String esctype) {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nfcdialog == null) {
            nfcdialog = ProgressDialog.show(this, "提交订单...", "提交NFC卡订单...", true, true);
        } else {
            if (nfcdialog.isShowing()) {
                return;
            }
        }
        SharedPreferences pfs = getSharedPreferences("autologin", Context.MODE_PRIVATE);
        String uid = pfs.getString("account", "");
        String path = baseurl;
        String url = path + "nfchandle.do?action=incom&uuid=" + uuid + "&comid=" + comid + "&uid=" + uid + "&esctype=" + esctype;
        MyLog.w("LeaveActivity", "提交NFC订单的URL--->" + url);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
                    MyLog.i("LeaveActivity", "提交NFC卡订单返回的信息是--->" + object);
                    nfcdialog.dismiss();
                    nfcdialog = null;
                    Message ordermsg = new Message();
                    ordermsg.what = 6;// nfc刷卡信息；
                    ordermsg.obj = object;
                    handler.sendMessage(ordermsg);
                } else {
                    nfcdialog.dismiss();
                    nfcdialog = null;
                    switch (status.getCode()) {
                        case 500:
                            Toast.makeText(LeaveActivity.this, "服务器错误！", Toast.LENGTH_SHORT).show();
                            break;
                        case 404:
                            Toast.makeText(LeaveActivity.this, "服务器不可用！", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    Toast.makeText(LeaveActivity.this, "网络请求错误！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // nfc卡未注册：{info:-1,errormsg:nfc卡未注册}
    // 存在逃单：{info:-2,own:1,other:3,carnumber:京A88888}
    // 直接结算：{info:2,carnumber:京A88888,ctime:2015-02-04
    // 12：00：00,total:5.00,uin:10218,uuid:0484894A9A3D81}
    // 结算订单：{info:1,total=0.00, duration=1小时8分钟, carnumber=京A88888, hascard=1,
    // isedit=0,
    // etime=16:43, handcash=0, btime=15:35, uin=10218, orderid=1040385,
    // collect=6.25}
    // 多重价格：{info:3}
    // 生成订单：{info:0}
    public void showOrderDialog(String object, String uuid) {
        Gson gson = new Gson();
        NfcOrder nfcOrder = gson.fromJson(object, NfcOrder.class);
        if (nfcOrder == null || nfcOrder.getInfo() == null) {
            Toast.makeText(LeaveActivity.this, "获取数据错误，请重新刷卡！", Toast.LENGTH_SHORT).show();
            return;
        }
        MyLog.w("LeaceActivity", "NFC--->刷卡解析返回的数据：" + nfcOrder.toString());
        if (nfcOrder.getInfo().equals("0")) { // 生成订单：{info:0}
            ShowNfcNewOrder instance = ShowNfcNewOrder.getInstance();
            instance.setContext(LeaveActivity.this);
            instance.setUuid(uuid);
            instance.setComid(comid);
            instance.setImei(imei);
            instance.setType("0");
            instance.showNfcOrderDialog();
            MyLog.i("LeaceActivity", "进入生成订单对话框管理类--------");
        } else if (nfcOrder.getInfo().equals("1")) { // 结算订单：{info:1
            if (nfcOrder.getOrderid() != null && nfcOrder.getTotal() != null) {
                ShowNfcFinishOrder finishOrder = ShowNfcFinishOrder.getInstance();
                finishOrder.setContext(LeaveActivity.this);
                finishOrder.setComid(comid);
                finishOrder.setImei(imei);
                finishOrder.setNfcOrder(nfcOrder);
                finishOrder.showNfcFinishOrderDialog();
            } else {
                Toast.makeText(LeaveActivity.this, "结算订单错误信息--" + object, Toast.LENGTH_LONG).show();
            }
        } else if (nfcOrder.getInfo().equals("2")) { // 直接结算：{info:2
            if (nfcOrder.getTotal() != null) {
                ShowNfcNewOrder instance = ShowNfcNewOrder.getInstance();
                instance.setContext(LeaveActivity.this);
                instance.setUuid(uuid);
                instance.setComid(comid);
                instance.setImei(imei);
                instance.setType("2");
                instance.setNfcOrder(nfcOrder);
                instance.showNfcOrderDialog();
            } else {
                Toast.makeText(LeaveActivity.this, "结算订单错误信息--" + object, Toast.LENGTH_LONG).show();
            }
        } else if (nfcOrder.getInfo().equals("3")) { // 3土桥专用生成订单；
            ShowNfcNewOrder instance = ShowNfcNewOrder.getInstance();
            instance.setContext(LeaveActivity.this);
            instance.setUuid(uuid);
            instance.setComid(comid);
            instance.setImei(imei);
            instance.setType("3");
            instance.showNfcOrderDialog();
        } else if (nfcOrder.getInfo().equals("-1")) {
            Toast.makeText(LeaveActivity.this, "请使用停车宝专用NFC卡！", Toast.LENGTH_SHORT).show();
        } else if (nfcOrder.getInfo().equals("-2")) { // 存在逃单：{info:-2
            if (nfcOrder.getOther() != null && nfcOrder.getOwn() != null && nfcOrder.getCarnumber() != null) {
                int parseInt = Integer.parseInt(nfcOrder.getOwn());
                int parseInt2 = Integer.parseInt(nfcOrder.getOther());
                int num = parseInt + parseInt2;
                String lostwarn = nfcOrder.getCarnumber() + "有" + num + "笔逃单,在您的车场逃单" + parseInt + "次!";
                ShowNfcNewOrder instance = ShowNfcNewOrder.getInstance();
                instance.setContext(LeaveActivity.this);
                instance.setCarnumber(nfcOrder.getCarnumber());
                instance.setLostwarn(lostwarn);
                instance.setType("-2");
                instance.setUuid(uuid);
                instance.showNfcOrderDialog();
            } else {
                Toast.makeText(LeaveActivity.this, "查询逃单信息服务器错误！", Toast.LENGTH_SHORT).show();
            }
        } else if (nfcOrder.getInfo().equals("-3")) {//pos机订单已结算
            Toast.makeText(LeaveActivity.this, nfcOrder.getErrmsg(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(LeaveActivity.this, "刷卡错误信息--" + object, Toast.LENGTH_LONG).show();
        }
    }

    public void openGPS(Context context) {
        if (GPSHandler.hasGPSDevice(context) && !GPSHandler.isOPen(context) && gpsDialog) {
            showOpenGpsDialog(context);
            MyLog.w("LeavaActivity", "设备支持Gps硬件，没有开启GPS---提醒用户去打开！");
            gpsDialog = false;
        }
    }

    // 请求消息中心是否有新的消息；zld/collectorrequest.do?action=getmesg&token=&page=&maxid=
    // 参数maxid>=0时，返回消息数 ，为空或其它值时，返回对应的第page(默认1)页
    // 数据，默认返回10条，数组格式[{},{}]没有数据时，返回[]
    public void getCenterMessage(String token) {
        String maxId = SharedPreferencesUtils.getIntance(this).getMsgMaxId(useraccount);
        String url = baseurl + "collectorrequest.do?action=getmesg&token=" + token + "&maxid=" + maxId;
        MyLog.w("CenterMassge", "请求消息中心的url是--->" + url);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200) {
                    MyLog.i("CenterMassge", "获取消息中心消息数的结果" + object);
                    if (!TextUtils.isEmpty(object) && TextUtils.isDigitsOnly(object) && Long.parseLong(object) > 0) {
                        DrawerItemInfo.getInstance().set(4, new DrawerItemInfo("消息中心", R.drawable.new_msg));
                        SharedPreferencesUtils.getIntance(LeaveActivity.this).setNewMsg(true);
                        lv_left_drawer.setAdapter(new DrawerAdapter(DrawerItemInfo.getInstance(), LeaveActivity.this));
                    }
                }
            }
        });
    }

    // 提示用户打开gps
    public void showOpenGpsDialog(Context context) {
        AlertDialog.Builder GPSbuilder = new Builder(context);
        GPSbuilder.setIcon(R.drawable.app_icon_32);
        GPSbuilder.setTitle("GPS未打开提醒");
        GPSbuilder.setCancelable(false);
        GPSbuilder.setMessage("为方便车主找到您付费,增加您的收入,建议您立刻去打开GPS定位 !");
        GPSbuilder.setPositiveButton("去打开", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                gpsDialog = true;
            }
        });
        GPSbuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                gpsDialog = true;
            }
        });
        GPSbuilder.create().show();
    }

    public void noFinishOrder(Context context) {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new Builder(context);
            builder.setIcon(R.drawable.app_icon_32);
            builder.setTitle("错误提示！");
            builder.setMessage("您还有没处理的订单！");
            builder.setCancelable(false);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog = builder.create();
        }
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    // 还需要补交现金的对话框；
    public void notBalanceDialog(Message msg) {
        NfcPrepaymentOrder preorder = (NfcPrepaymentOrder) msg.obj;
        View open_dialog_view = View.inflate(this, R.layout.dialog_repayment_money_nfcorder, null);
        TextView tv_collect = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_collect);
        TextView tv_total = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_total);
        TextView tv_prefee = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_prefee);
        TextView tv_collect1 = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_collect1);
        Button bt_nfcorder_ok = (Button) open_dialog_view.findViewById(R.id.bt_dialog_repayment_nfcorder_ok);
        tv_collect.setText(preorder.getCollect() != null ? "还需向车主补收" + preorder.getCollect() + "元现金" : "");
        tv_collect1.setText(preorder.getCollect() != null ? preorder.getCollect() : "");
        tv_total.setText(preorder.getTotal() != null ? "停车费			" + preorder.getTotal() + "元" : "");
        tv_prefee.setText(preorder.getPrefee() != null ? "微信预付		" + preorder.getPrefee() + "元" : "");
        final Dialog openDialog = new Builder(this).create();
        bt_nfcorder_ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                openDialog.dismiss();
                BLEService.writeChar6("[CKTG]");
            }
        });
        openDialog.setCancelable(false);
        openDialog.show();
        openDialog.setContentView(open_dialog_view);
    }

    public void showBLEFailDialog(Message msg) {
        NfcOrder order = (NfcOrder) msg.obj;
        FinishOrderFailDialog failDialog = new FinishOrderFailDialog(this, R.style.nfcnewdialog, order, "fast");
        failDialog.setCanceledOnTouchOutside(false);
        failDialog.show();
    }

    private TextView txtBoweiName;
    private LinearLayout lnBoWeiName;
//    private LinearLayout lnmain1, lnmain2, lnmain3;

    public void initVeiw() {
        anim1.setDuration(300);
        anim2.setDuration(300);
        animup.setDuration(300);
        animdown.setDuration(300);
//        ll_main_parking_order = (LinearLayout) findViewById(R.id.ll_main_parking_order);
        ll_main_parking_order = findViewById(R.id.ll_main_parking_order);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        lv_left_drawer = (ListView) findViewById(R.id.left_drawer);
        lv_main_order_msg = (ListView) findViewById(R.id.lv_main_order_msg);
        ll_main_qrcode = (LinearLayout) findViewById(R.id.ll_main_qrcode);
        rl_main_new_msg = (RelativeLayout) findViewById(R.id.rl_main_new_msg);
        ll_main_work_state = (LinearLayout) findViewById(R.id.ll_main_work_state);
        rl_main_send_ticket = (RelativeLayout) findViewById(R.id.rl_main_send_ticket);
        rl_lv_main_qrcode = (RelativeLayout) findViewById(R.id.rl_lv_main_qrcode);
        rl_main_collect_money = (RelativeLayout) findViewById(R.id.rl_main_collect_money);
        tv_main_name = (TextView) findViewById(R.id.tv_main_name);
        tv_main_account = (TextView) findViewById(R.id.tv_main_account);
        tv_main_workstation = (TextView) findViewById(R.id.tv_main_workstation);
        tv_main_fast_bluetooth = (TextView) findViewById(R.id.tv_main_fast_bluetooth);
        iv_main_collect_money_qrcode = (ImageView) findViewById(R.id.iv_main_collect_money_qrcode);
        iv_main_send_ticket_qrcode = (ImageView) findViewById(R.id.iv_main_send_ticket_qrcode);
        iv_call_phone = (ImageView) findViewById(R.id.iv_leave_activity_phone);
        tv_ticket_total = (TextView) findViewById(R.id.tv_main_ticket_total);
        tv_change_total = (TextView) findViewById(R.id.tv_main_change_ticket_total);
        tv_consume_score = (TextView) findViewById(R.id.tv_main_consume_score);
        tv_change_qrcode = (TextView) findViewById(R.id.tv_main_change_qrcode);
        tv_phone_collect = (TextView) findViewById(R.id.tv_main_today_phone_collect);
        tv_today_reward = (TextView) findViewById(R.id.tv_main_today_reward);
        tv_today_score = (TextView) findViewById(R.id.tv_main_today_score);
        tv_main_refresh = (TextView) findViewById(R.id.tv_main_refresh);
        tv_ticket_refresh = (TextView) findViewById(R.id.tv_main_ticket_refresh);
        bt_send_private_ticket = (Button) findViewById(R.id.bt_main_send_private_ticket);
        bt_ticket_share_link = (Button) findViewById(R.id.bt_ticket_share_link);
        bt_collect_share_link = (Button) findViewById(R.id.bt_collect_share_link);
        tv_income_and_scan = (TextView) findViewById(R.id.tv_main_income_and_scan);
        tv_change_income_satate = (TextView) findViewById(R.id.tv_main_change_income_satate);
        tv_leave_and_order = (TextView) findViewById(R.id.tv_main_leave_and_leave_order);
        rl_main_car_inout = (RelativeLayout) findViewById(R.id.rl_main_car_inout);
        rl_main_leave_order = (RelativeLayout) findViewById(R.id.rl_main_leave_order);
        tv_main_income = (TextView) findViewById(R.id.tv_main_income);
        tv_main_leave = (TextView) findViewById(R.id.tv_main_leave);
        tv_main_collect_total = (TextView) findViewById(R.id.tv_main_collect_total);
        tv_main_check_collect_total = (TextView) findViewById(R.id.tv_main_check_collect_total);
        tv_main_collect_duration = (TextView) findViewById(R.id.tv_main_collect_duration);
        tv_main_set_collect = (TextView) findViewById(R.id.tv_main_set_collect);
        tv_main_collect_time = (TextView) findViewById(R.id.tv_main_collect_time);
        rl_main_collect = (RelativeLayout) findViewById(R.id.rl_main_collect);
        rl_main_collect.setVisibility(View.GONE);
        tv_main_set_collect.setVisibility(View.VISIBLE);
        rl_main_new_msg.setOnClickListener(this);
        tv_main_workstation.setOnClickListener(this);
        tv_change_total.setOnClickListener(this);
        tv_change_qrcode.setOnClickListener(this);
        tv_main_refresh.setOnClickListener(this);
        tv_ticket_refresh.setOnClickListener(this);
        bt_send_private_ticket.setOnClickListener(this);
        bt_collect_share_link.setOnClickListener(this);
        bt_ticket_share_link.setOnClickListener(this);
        tv_change_income_satate.setOnClickListener(this);
        tv_income_and_scan.setOnClickListener(this);
        tv_leave_and_order.setOnClickListener(this);
        tv_main_set_collect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 去设置金额按钮
                Intent intent = new Intent(LeaveActivity.this, SetCollectActivity.class);
                startActivityForResult(intent, 3);
            }
        });
        tv_main_check_collect_total.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 调整金额按钮，打开调整金额页面；
                Intent intent = new Intent(LeaveActivity.this, SetCollectActivity.class);
                try {
                    intent.putExtra("total", tv_main_collect_total.getText().toString().trim());
                    intent.putExtra("duration", tv_main_collect_duration.getText().toString().trim().replace("停车", ""));
                    intent.putExtra("intime", tv_main_collect_time.getText().toString().trim());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startActivityForResult(intent, 3);
            }
        });
        iv_call_phone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneintent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + "01056450585"));
                startActivity(phoneintent);
            }
        });
        txtBoweiName = ((TextView) findViewById(R.id.tv_main_boweiname));
        lnBoWeiName = ((LinearLayout) findViewById(R.id.ll_main_work_bowei));
        lnNewBar = ((RelativeLayout) findViewById(R.id.newbar));
        lnOldBar = ((RelativeLayout) findViewById(R.id.oldbar));
        gv_in_vehicle = (GridView) findViewById(R.id.gv_in_vehicle);
        tv_parking_state = (TextView) findViewById(R.id.tv_in_vehicle_parking_state);
        gv_in_vehicle.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //车检器消息返回的车检器订单编号
                if (CommontUtils.checkString(INFO.get(position).getBerthorderid())) {
                    putStringToPreference("berthorderid", INFO.get(position).getBerthorderid());
                } else {
                    putStringToPreference("berthorderid", "");
                }
                if (TextUtils.isEmpty(INFO.get(position).getOrderid())) {
                    putStringToPreference("bowei", INFO.get(position).getId());//泊位id
                    putStringToPreference("boweiversion", INFO.get(position).getBer_name());//泊位名
                    Intent i = new Intent(LeaveActivity.this, InputCarNumberActivity.class);
                    i.putExtra("from", "input");
                    startActivity(i);
                } else {
                    Intent intent = new Intent(LeaveActivity.this, CurrentOrderDetailsActivity.class);
                    String orderid = INFO.get(position).getOrderid();
                    intent.putExtra("orderid", orderid);
                    intent.putExtra("ismonthuser", INFO.get(position).getIsmonthuser());
                    intent.putExtra("iscard", INFO.get(position).getIs_card());
                    MyLog.i("InTheVehicleActivity", "点击条目的position是" + position + "点单号是" + orderid);
                    startActivity(intent);
                }

            }
        });
        edtSearch = ((EditText) findViewById(R.id.tv_pay_log_number));
        lnDelete = ((LinearLayout) findViewById(R.id.ll_pay_log_delete));

        lnDelete.setOnClickListener(this);
        INFO = new ArrayList<>();
        infos = new ArrayList<>();

        gridadapter = new InTheVehicleAdapter(LeaveActivity.this, INFO);
        gv_in_vehicle.setAdapter(gridadapter);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //输入后搜索
                searchinfo = new ArrayList<>();
                if (CommontUtils.checkString(s.toString())) {
                    for (int i = 0; i < infos.size(); i++) {
                        if (infos.get(i).getBer_name().contains(s.toString()) || (TextUtils.isEmpty(infos.get(i).getCar_number()) ? false : infos.get(i).getCar_number().contains(s.toString()))) {
                            searchinfo.add(infos.get(i));
                        }
                    }
                    INFO.clear();
                    INFO.addAll(searchinfo);
                } else {
                    INFO.clear();
                    INFO.addAll(infos);
                }
                if (gridadapter != null) {
                    gridadapter.notifyDataSetChanged();
                }
            }
        });

    }

    private EditText edtSearch;
    private LinearLayout lnDelete;
    public ArrayList<InVehicleInfo> infos, INFO, searchinfo;
    public BoWeiListEntity Data;
    public InTheVehicleAdapter gridadapter;
    public GridView gv_in_vehicle;
    public TextView tv_parking_state;// 停车状态

    //    private TextView tv_main_refresh2;
    private RelativeLayout lnNewBar, lnOldBar;

    /**
     * 选择蓝牙出口提交给服务器绑定；
     */
    public void bindingIbeacon(String export, final String state) {
        // 选择蓝牙出口后确认绑定的接口：//collectorrequest.do?action=bindworksite&wid=&token=198f697eb27de5515e91a70d1f64cec7
        // {\"result\":\"1\"} result ：0失败，1成功
        AQuery aQuery = new AQuery(this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=bindworksite&wid=" + export + "&token=" + token;
        MyLog.w("SetActivity-->>", "选择蓝牙出口后确认绑定的接口" + url);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                MyLog.i("SetActivity-->>", "绑定蓝牙车场的结果是：" + object);
                if (TextUtils.isEmpty(object)) {
                    return;
                }
                try {
                    JSONObject resultjson = new JSONObject(object);
                    String result = resultjson.getString("result");
                    if ("1".equals(result)) {
                        Toast.makeText(LeaveActivity.this, "操作成功！", Toast.LENGTH_SHORT).show();
                        tv_main_workstation.setText(state);
                        if ("离岗".equals(state)) {
                            tv_main_workstation.setTextColor(getResources().getColor(R.color.red));
                        } else {
                            tv_main_workstation.setTextColor(getResources().getColor(R.color.tv_leaveItem_state_green));
                        }
                    } else {
                        Toast.makeText(LeaveActivity.this, "操作失败,请重新绑定！", Toast.LENGTH_SHORT).show();
                        showIbeaconDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 获取车场编号和通知的图片地址；
    public void getParkinfo() {
        AQuery aQuery = new AQuery(LeaveActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=cominfo&token=" + token + "&out=json";
        MyLog.w("LeaveActivity-->>", "查看车场信息的url" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "获取新通知", "加载消息中...");
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                rl_main_new_msg.setClickable(true);
                dialog.dismiss();
                if (!TextUtils.isEmpty(object)) {
                    rl_main_new_msg.setVisibility(View.GONE);
                    ll_main_work_state.setVisibility(View.VISIBLE);
                    Gson gson = new Gson();
                    ParkingInfo parkingInfo = gson.fromJson(object, ParkingInfo.class);
                    if (parkingInfo != null) {
                        SharedPreferencesUtils.getIntance(LeaveActivity.this).setParkTotal(parkingInfo.getParkingtotal());
                    }
                    if (parkingInfo != null && !TextUtils.isEmpty(parkingInfo.getMesgurl())) {
                        MyLog.i("LeaveActivity-->>", "解析车场信息为" + parkingInfo.toString());
                        comid = parkingInfo.getId();
                        final DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        ImageSize imageSize = new ImageSize(displayMetrics.widthPixels * 2 / 3,
                                displayMetrics.widthPixels * 2 / 3);
                        ImageLoader.getInstance().loadImage(baseurl + parkingInfo.getMesgurl(), imageSize,
                                new SimpleImageLoadingListener() {
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                        showMsgDialog(loadedImage);
                                    }
                                });
                    }
                }
            }
        });
    }

    /**
     * 获取我的值班信息；今日收费、今日打赏、今日入场和出场信息
     */
    public void getWatchInfo() {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", Toast.LENGTH_SHORT).show();
            tv_main_refresh.clearAnimation();
            return;
        }
        // collectorrequest.do?action=todayaccount&token=5286f078c6d2ecde9b30929f77771149
        AQuery aQuery = new AQuery(LeaveActivity.this);
        String url = baseurl + "collectorrequest.do?action=todayaccount&token=" + token;
        MyLog.w("LeaveActivity-->>", "我的值班信息的url---" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "获取新数据", "刷新数据中...");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                dialog.dismiss();
                tv_main_refresh.clearAnimation();
                if (TextUtils.isEmpty(object)) {
                    return;
                }
                Gson gson = new Gson();
                mWatchInfo = gson.fromJson(object, WatchInfo.class);
                if (mWatchInfo != null) {
                    MyLog.i("LeaveActivity-->>", "解析我的值班信息" + mWatchInfo.toString());
                    setView(mWatchInfo);
                }

            }
        });
    }

    /**
     * 弹出选择专用券金额的对话框；
     */
    public void showTicketDialog() {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ticketinfo == null || ticketinfo.size() == 0) {
            Toast.makeText(this, "获取金额失败，请重新进入此界面", Toast.LENGTH_SHORT).show();
            return;
        }
        int size = 0;
        for (int i = 0; i < ticketinfo.size(); i++) {
            if (ticketinfo.get(i).getType().equals("1")) {
                size = size + 1;
            }
        }
        final String[] tickets = new String[size];
        for (int i = 0; i < ticketinfo.size(); i++) {
            if (ticketinfo.get(i).getType().equals("1")) {
                if (tv_ticket_total.getText().toString().equals(ticketinfo.get(i).getBmoney())) {
                    mWhich = i;
                }
                tickets[i] = ticketinfo.get(i).getBmoney() + "元";
            }
        }
        new AlertDialog.Builder(this).setTitle("请选择专用券面值").setIcon(R.drawable.app_icon_32)
                .setSingleChoiceItems(tickets, mWhich, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        mWhich = which;
                        if ("5".equals(tickets[which].replace("元", ""))) {
                            getRewardScoreInfo(true);
                        } else {
                            for (int i = 0; i < ticketinfo.size(); i++) {
                                if (ticketinfo.get(i).getType().equals("1")) {
                                    if (ticketinfo.get(i).getBmoney().equals(tickets[which].replace("元", ""))) {
                                        tqrcode[0] = tickets[which].replace("元", "");
                                        tqrcode[1] = ticketinfo.get(i).getScore();
                                        getQRCode(tqrcode[0], tqrcode[1]);
                                    }
                                }
                            }
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    // 获取红包详情collectorrequest.do?action=bonusinfo&token=
    public void getRewardScoreInfo(final Boolean isfive) {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", Toast.LENGTH_SHORT).show();
            tv_ticket_refresh.setClickable(true);
            tv_ticket_refresh.clearAnimation();
            return;
        }
        String url = baseurl + "collectorrequest.do?action=bonusinfo&token=" + token;
        MyLog.w("LeaveActivity", "获取红包详情的URL--->" + url);
        // final ProgressDialog dialog = ProgressDialog.show(this,
        // "获取红包详情","加载中...", true, true);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                tv_ticket_refresh.setClickable(true);
                tv_ticket_refresh.clearAnimation();
                if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
                    MyLog.i("LeaveActivity", "获取到红包详情--->" + object);
                    Gson gson = new Gson();
                    ticketinfo = gson.fromJson(object, new TypeToken<ArrayList<RedPacketInfo>>() {
                    }.getType());
                    if (isfive && ticketinfo != null && ticketinfo.size() > 0) {
                        for (int i = 0; i < ticketinfo.size(); i++) {
                            if (ticketinfo.get(i).getType().equals("1")) {
                                if ("5".equals(ticketinfo.get(i).getBmoney())) {
                                    if (ticketinfo.get(i).getLimit() != null && "1".equals(ticketinfo.get(i).getLimit())) {
                                        tv_ticket_total.setText("3");
                                        tv_consume_score.setText("6");
                                        Toast.makeText(LeaveActivity.this, "今日五元券发放数量已达到上限", Toast.LENGTH_SHORT).show();
                                        tqrcode[0] = "3";
                                        tqrcode[1] = "6";
                                        getQRCode(tqrcode[0], tqrcode[1]);
                                        return;
                                    } else {
                                        tv_consume_score.setText(ticketinfo.get(i).getScore());
                                        MyLog.d("LeaveActivity", "五元券需要花费积分：" + ticketinfo.get(i).getScore());
                                        tqrcode[0] = "5";
                                        tqrcode[1] = ticketinfo.get(i).getScore();
                                    }
                                }
                            }
                        }
                        getQRCode(tqrcode[0], tqrcode[1]);
                    } else {
                        if (ticketinfo != null && ticketinfo.size() != 0) {
                            for (int i = 0; i < ticketinfo.size(); i++) {
                                if ("1".equals(ticketinfo.get(i).getType())) {
                                    if ("3".equals(ticketinfo.get(i).getBmoney())) {
                                        tqrcode[0] = ticketinfo.get(i).getBmoney();
                                        tqrcode[1] = ticketinfo.get(i).getScore();
                                        getQRCode(tqrcode[0], tqrcode[1]);
                                        tv_ticket_total.setText(ticketinfo.get(i).getBmoney());
                                        tv_consume_score.setText(ticketinfo.get(i).getScore());
                                    }
                                    return;
                                }
                            }
                        }
                    }
                } else {
                    switch (status.getCode()) {
                        case 500:
                            Toast.makeText(LeaveActivity.this, "服务器错误！", Toast.LENGTH_SHORT).show();
                            break;
                        case 404:
                            Toast.makeText(LeaveActivity.this, "服务器不可用！", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    Toast.makeText(LeaveActivity.this, "网络请求错误！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 获取送券二维码
     */
    public void getQRCode(final String money, final String score) {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", Toast.LENGTH_SHORT).show();
            tv_ticket_refresh.clearAnimation();
            tv_ticket_refresh.setClickable(true);
            return;
        }
        String url = baseurl + "collectorrequest.do?action=sweepticket&bmoney=" + money + "&score=" + score + "&token=" + token;
        MyLog.w("LeaveActivity", "扫码领停车券的URL--->" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取停车券二维码...", true, true);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                dialog.dismiss();
                tv_ticket_refresh.clearAnimation();
                tv_ticket_refresh.setClickable(true);
                tv_ticket_total.setText(money);
                tv_consume_score.setText(score);
                if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
                    MyLog.i("LeaveActivity", "获取到扫码领停车券--->" + object);
                    try {
                        JSONObject json = new JSONObject(object);

                        int result = json.getInt("result");
                        switch (result) {
                            case 1:
                                ticketcodeurl = json.getString("code");
                                setTicketQrcode(ticketcodeurl);
                                break;
                            case -1:
                                iv_main_send_ticket_qrcode.setImageResource(R.drawable.net_error_noqrcode);
                                Toast.makeText(LeaveActivity.this, "获取券出错了,请联系停车宝", Toast.LENGTH_SHORT).show();
                                break;
                            case -3:
                                iv_main_send_ticket_qrcode.setImageResource(R.drawable.net_error_noqrcode);
                                Toast.makeText(LeaveActivity.this, "积分不足", Toast.LENGTH_SHORT).show();
                                tqrcode[0] = "3";
                                tqrcode[1] = "6";
                                tv_ticket_total.setText("3");
                                tv_consume_score.setText("6");
                                break;
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    switch (status.getCode()) {
                        case 500:
                            Toast.makeText(LeaveActivity.this, "服务器错误！", Toast.LENGTH_SHORT).show();
                            break;
                        case 404:
                            Toast.makeText(LeaveActivity.this, "服务器不可用！", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    Toast.makeText(LeaveActivity.this, "网络请求错误！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 专门获取积分的接口 collectorrequest.do?action=remainscore&token=
     */
    public void getAccountScore() {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = baseurl + "collectorrequest.do?action=remainscore&token=" + token;
        MyLog.w("LeaveActivity", "专门获取积分的URL--->" + url);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
                    try {
                        JSONObject json = new JSONObject(object);
                        String mscore = json.getString("score");
                        SharedPreferencesUtils.getIntance(LeaveActivity.this).setMyScore(mscore);
                        tv_today_score.setText(mscore);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void setTicketQrcode(String code) {
        QRCodeEncoder d = new QRCodeEncoder();
        Bitmap codebmp = d.encode2BitMap(code, 400, 400);
        iv_main_send_ticket_qrcode.setImageBitmap(codebmp);
    }

    public void showToast(String msg) {
        Toast.makeText(LeaveActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 获得推荐奖的消息弹框；
     */
    public void showRecommendDialog(Message msg) {
        LeaveOrder info = (LeaveOrder) msg.obj;
        if (info == null || TextUtils.isEmpty(info.getMobile())) {
            return;
        }
        new AlertDialog.Builder(this).setIcon(R.drawable.app_icon_32).setTitle("推荐成功奖励！")
                .setMessage("您已成功推荐车主" + info.getMobile() + "\n获得推荐奖励" + info.getTotal() + "元")
                .setNegativeButton("知道了", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }


    /**
     * 复位到收费二维码界面
     */
    public void resetQrcode() {
        ticketinfo = null;
        tqrcode[0] = "3";
        tqrcode[1] = "6";
        tv_ticket_total.setText("3");
        tv_consume_score.setText("6");
        iv_main_send_ticket_qrcode.setImageResource(R.drawable.net_error_noqrcode);
        rl_main_collect_money.setVisibility(View.VISIBLE);
        rl_main_send_ticket.setVisibility(View.INVISIBLE);
        tv_change_qrcode.setBackgroundResource(R.drawable.main_send_ticket_bg);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case 3: /* 隐藏设置金额按钮 */
                rl_main_collect.setVisibility(View.VISIBLE);
                tv_main_set_collect.setVisibility(View.GONE);
                try {
                    Bundle bunde = data.getExtras();
                    String total = bunde.getString("total");
                    String duration = bunde.getString("duration");
                    String intime = bunde.getString("intime");
                    tv_main_collect_total.setText(total);
                    tv_main_collect_duration.setText("停车" + duration);
                    tv_main_collect_time.setText(intime);
                    createQrcode(baseurl + qrcodeurl + "&total=" + total);
                } catch (Exception e) {
                    rl_main_collect.setVisibility(View.GONE);
                    tv_main_set_collect.setVisibility(View.VISIBLE);
                }
                break;
            case 4:/* 显示设置金额按钮 */
                rl_main_collect.setVisibility(View.GONE);
                tv_main_set_collect.setVisibility(View.VISIBLE);
                tv_main_collect_time.setText("");
                tv_main_collect_total.setText("");
                createQrcode(baseurl + qrcodeurl);
                break;
            case 1234:
                FinishAction();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyLog.w("LeaveActivity", "onStart-----");
        SharedPreferencesUtils.getIntance(this).setMainActivity(true);
        getAccountScore();// 从其它页面返回主页面帮助用户刷新一次我的积分;
    }

    @Override
    public void onResume() {
        MyLog.w("LeaveActivity", "onResume-----");
        super.onResume();
        if (nfcAdapter != null) {
            MyLog.w("LeaveActivity", "enableForegroundDispatch-----");
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, LeaveActivity.FILTERS, LeaveActivity.TECHLISTS);
        }
        putStringToPreference("bowei", "");
        putStringToPreference("boweiversion", "");
        //调用现金 刷卡 电子收费接口，先用一键对账代替
//        getTodayMoney();
        if (CommontUtils.checkString(getStringFromPreference("boweistate"))) {
            String changedbowei = getStringFromPreference("boweistate");
            putStringToPreference(changedbowei, "");
            //移除一个泊位以后置空，避免下次误移除
            putStringToPreference("boweistate", "");
            //结算成功后将车检器订单置 空
            if (getStringFromPreference("berthorderid").equals("succeed")) {
                putStringToPreference(changedbowei + "222", "");
            }
        }
        getInVehicleInfo();
    }

    @Override
    public void onPause() {
        MyLog.w("LeaveActivity", "onPause-----");
        super.onPause();
        if (nfcAdapter != null) {
            MyLog.w("LeaveActivity", "disableForegroundDispatch-----");
            nfcAdapter.disableForegroundDispatch(this);
        }
        Constant.BerthFresh = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyLog.w("LeaveActivity", "onStop-----");
        resetQrcode();
        SharedPreferencesUtils.getIntance(this).setMainActivity(false);
        if (SharedPreferencesUtils.getIntance(this).getNewMsg()) {
            lv_left_drawer.setAdapter(new DrawerAdapter(DrawerItemInfo.getInstance(), LeaveActivity.this));
            SharedPreferencesUtils.getIntance(LeaveActivity.this).setNewMsg(false);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyLog.w("LeaveActivity", "onDestroy-----");
        Intent pullservice = new Intent(this, PullMsgService.class);
        stopService(pullservice);
        Intent bleservice = new Intent(this, BLEService.class);
        stopService(bleservice);
        SharedPreferencesUtils.getIntance(this).setToken("");
        ticketinfo = null;
        if (CommontUtils.checkList(Constant.boweiMsgAll)) {
            for (int i = 0; i < Constant.boweiMsgAll.size(); i++) {
//                System.out.println("DDDDDDDDDDDDD"+Constant.boweiMsgAll.get(i));
//                Constant.boweiMsgAll.get(i).setState("-10");
                putStringToPreference(Constant.boweiMsgAll.get(i).getId(), "-10");
            }
        }
        MyLog.w("LeaveActivity", "onDestroy-----");
        RskApi.ZGClosePower();
    }

    /*
     * 与蓝牙服务类交互的接口
     */
    @Override
    public void onSendMsg(Message msg) {
        // 把接口回调的消息发给handler来处理
        MyLog.d("LeaveActivity", "-------接口回调的消息-----");
        if (handler != null) {
            handler.sendMessage(msg);
        }
    }

    //pos机生成订单后,回调此方法.弹框选择车位;
    @Override
    public void doSelectParkPosition(String carmunber, String orderid) {
        SelectParkingPositionDialog dialog = new SelectParkingPositionDialog(this, carmunber, orderid, null);
        dialog.show();
    }

    //打印离场凭条
    public void prientCarOut(TcbCheckCarOut info) {
        Bitmap imgbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_check);
        PrinterUitls.getInstance().printerTCBCheckCarOut(info, null, imgbitmap);
    }

    private void FinishAction() {
        Intent bleService = new Intent(LeaveActivity.this, BLEService.class);
        Intent pullService = new Intent(LeaveActivity.this, PullMsgService.class);
        LeaveActivity.this.stopService(bleService);
        LeaveActivity.this.stopService(pullService);
        Intent intent = new Intent(LeaveActivity.this, LoginActivity.class);
        startActivity(intent);
        putBooleanToPreference("already", true);
        //登陆时候的用户名
        putStringToPreference("uid", "");
        putStringToPreference("berthorderid", "");
        //移除一个泊位以后置空，避免下次误移除
        putStringToPreference("boweistate", "");
//        if (fragment != null) {
//            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
//        }
        finish();
    }

    /**
     * /zld/collectorrequest.do?action=paydetail&token=&workid=
     * 刷新首页 今日收费的接口
     */


    // 获取在场车辆信息collectorrequest.do?action=comparks&out=josn&token=5f0c0edb1cc891ac9c3fa248a28c14d5
    public void getInVehicleInfo() {
        //只有berthid泊位段id不为-1时才刷新泊位
        if (CommontUtils.checkString(SharedPreferencesUtils.getIntance(this).getberthid()) && !SharedPreferencesUtils.getIntance(this).getberthid().equals("-1")) {
            if (!IsNetWork.IsHaveInternet(LeaveActivity.this)) {
                Toast.makeText(LeaveActivity.this, "请检查网络", Toast.LENGTH_SHORT).show();
                return;
            }
            //测试链接
            // http://127.0.0.1/zld/collectorrequest.do?action=getberths&token=55c0fa9053658bb84e73169d8c742342&berthid=6&devicecode=
            String url = BaseActivity.baseurl + "collectorrequest.do?action=getberths&out=josn&token=" + BaseActivity.token +
                    "&berthid=" + SharedPreferencesUtils.getIntance(this).getberthid() +
                    "&devicecode=" + CommontUtils.GetHardWareAddress(this);
//        String url = BaseActivity.baseurl + "collectorrequest.do?action=comparks&out=josn&token=" + BaseActivity.token;
            MyLog.i("InTheVehicleActivity", "获取在场车辆的URL--->" + url);
            AQuery aQuery = new AQuery(this);
            final ProgressDialog dialog = ProgressDialog.show(this, "获取在场车辆数据", "获取中...", true, true);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    dialog.dismiss();
                    if (!TextUtils.isEmpty(object)) {
                        Constant.BerthFresh = true;
                        INFO.clear();
                        MyLog.i("InTheVehicleActivity", "获取在场车辆结果--->" + object);
                        Gson gson = new Gson();
                        Data = gson.fromJson(object, new TypeToken<BoWeiListEntity>() {
                        }.getType());
                        ArrayList<CarTypeItem> car_type = Data.getCar_type();
                        if (car_type != null && car_type.size() > 0) {
                            String cartype = gson.toJson(car_type);
                            putStringToPreference("car_type", cartype);
                        } else {
                            putStringToPreference("car_type", "");
                        }
                        SharedPreferencesUtils.getIntance(LeaveActivity.this).setworkid(Data.getWorkid());
                        String msg = Data.getErrmsg();
                        if (CommontUtils.checkString(Data.getState())) {
                            if (Integer.parseInt(Data.getState()) != 1) {
                                //非正常状态
                                if (CommontUtils.checkString(Data.getErrmsg())) {
                                    new AlertDialog.Builder(LeaveActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                            .setMessage(msg).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Data.getState().equals("0")) {
                                                //调用注册设备接口
                                                RegisDevice();
                                            } else {
                                                FinishAction();
                                            }

                                        }
                                    }).create().show();
                                }
                            } else {
                                if (!getBooleanFromPreference("alreadyalert")) {
                                    //已签到，显示签到时间
                                    if (CommontUtils.checkString(Data.getErrmsg())) {
                                        new AlertDialog.Builder(LeaveActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                                .setMessage(msg).setNegativeButton("确定", null).create().show();
                                    }
                                    putBooleanToPreference("alreadyalert", true);
                                }
                                if (CommontUtils.checkString(Data.getComid())) {
                                    SharedPreferencesUtils.getIntance(LeaveActivity.this).setComid(Data.getComid());
                                }
                                if (CommontUtils.checkString(Data.getCname())) {
                                    SharedPreferencesUtils.getIntance(LeaveActivity.this).setParkname(Data.getCname());
                                    String stationname = SharedPreferencesUtils.getIntance(context).getParkname() +
                                            "—" + SharedPreferencesUtils.getIntance(context).getberthsec_name();
                                    txtBoweiName.setText(stationname);
                                }
//                            System.out.println(Data.getComid()+Data.getCname());
                                infos = Data.getData();
                                if (CommontUtils.checkList(infos)) {
                                    for (int i = 0; i < infos.size(); i++) {
                                        String state = getStringFromPreference(infos.get(i).getId());
//                                    System.out.println("state="+state+" and i="+infos.get(i).getId());
                                        if (CommontUtils.checkString(state)) {
                                            if (!CommontUtils.checkString(infos.get(i).getSensor_state())) {
                                                infos.get(i).setSensor_state(state);
                                            }

                                        }
                                    }
                                    Constant.boweiMsgAll.clear();
                                    Constant.boweiMsgAll.addAll(infos);
                                }
                                INFO.addAll(infos);
                                MyLog.i("InTheVehicleActivity", "解析在场车辆结果--->" + infos.toString());
                                if (infos != null) {
                                    gridadapter.notifyDataSetChanged();
                                }
                            }
                        } else {
                            Toast.makeText(LeaveActivity.this, "获取数据出错！", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Constant.BerthFresh = false;
                    }
                }
            });
        }
    }

    /**
     * /zld/collectorrequest.do?action=regpossequence&token=a3a0dafbe61d9b491b6094b6f64a0693&device_code=
     * 注册设备接口
     */
    public void RegisDevice() {
        if (!IsNetWork.IsHaveInternet(LeaveActivity.this)) {
            Toast.makeText(LeaveActivity.this, "请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }
        AQuery aQuery = new AQuery(LeaveActivity.this);
        String path = BaseActivity.baseurl;
        String url = path + "collectorrequest.do?action=regpossequence&token=" +
                BaseActivity.token + "&device_code=" + CommontUtils.GetHardWareAddress(LeaveActivity.this);
        MyLog.w("注册的URl-->>", url);
        final ProgressDialog dialog = ProgressDialog.show(LeaveActivity.this, "加载中...", "注册设备...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            public void callback(String url, String object, AjaxStatus status) {
                MyLog.i("CurrentOrderDetailsActivity", "注册设备-->>" + object);
                if (object != null) {
                    dialog.dismiss();
                    Gson gson = new Gson();
                    BaseRegisBean bean = gson.fromJson(object, new TypeToken<BaseRegisBean>() {
                    }.getType());

                    new AlertDialog.Builder(LeaveActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                            .setMessage(bean.getErrmst()).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FinishAction();
                        }
                    }).setCancelable(false).create().show();

                } else {
                    dialog.dismiss();
                }
            }
        });
    }
}
