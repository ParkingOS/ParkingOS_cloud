package com.zhenlaidian.photo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Message;
import android.support.v4.view.MenuCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.zbar.lib.CaptureActivity;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.CarOrderAdapter;
import com.zhenlaidian.adapter.InTheVehicleAdapter;
import com.zhenlaidian.bean.AllOrder;
import com.zhenlaidian.bean.BoWeiListEntity;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.bean.EPayMessageDialog;
import com.zhenlaidian.bean.FinishOrderFailDialog;
import com.zhenlaidian.bean.HistoryOrder;
import com.zhenlaidian.bean.IbeaconCashInfo;
import com.zhenlaidian.bean.InVehicleInfo;
import com.zhenlaidian.bean.LeaveOrder;
import com.zhenlaidian.bean.MainUiInfo;
import com.zhenlaidian.bean.NfcOrder;
import com.zhenlaidian.bean.NfcPrepaymentOrder;
import com.zhenlaidian.bean.StopWaitToCashDialog;
import com.zhenlaidian.printer.PrinterUitls;
import com.zhenlaidian.printer.TcbCheckCarOut;
import com.zhenlaidian.service.BLEService;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.CurrentOrderDetailsActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.PlayerVoiceUtil;
import com.zhenlaidian.util.SharedPreferencesUtils;
import com.zhenlaidian.util.VoiceSynthesizerUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static java.lang.Double.parseDouble;

/**
 * 离场结算订单;
 */

public class CarOrderActivity extends BaseActivity {

    private EditText et_carnumber;
    private LinearLayout ll_delete;
    private LinearLayout ll_back;
    private ListView lv_car_order;
    private TextView tv_car_order_null;
    private Button bt_scancode;//扫描二维码结算;
    public CarOrderAdapter adapter;
    private int pagenumber = 1;
    private final int size = 20;
    private AllOrder allorder;
    private int count = 0;
    private AlertDialog IbeaconOutDialog;
    private int visiblecount = 0;
//    public static CarOrderActivity coinstance;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.onekey_actionbar, menu);
        MenuItem actionSettings = menu.findItem(R.id.onekey);
        if (!SharedPreferencesUtils.getIntance(context).getview_plot().equals("1")) {
            actionSettings.setVisible(false);
        }
        MenuCompat.setShowAsAction(menu.findItem(R.id.onekey), MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    // actionBar的点击回调方法
    @SuppressLint("RtlHardcoded")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MyLog.w("LeaveActivity", "onOptionsItemSelected");
        switch (item.getItemId()) {
            case R.id.onekey:
                new AlertDialog.Builder(CarOrderActivity.this)
                        .setTitle("提示")
                        .setIcon(R.drawable.app_icon_32)
                        .setMessage("确定一键置为未缴吗？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogs, int which) {
                                if (INFO != null && INFO.size() > 0) {
                                    if (!dialog.isShowing())
                                        dialog.show();
                                    getOrderinfo(INFO.get(0).getOrderid());
                                } else {
                                    Toast.makeText(CarOrderActivity.this, "没有在场订单", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }
                        })
                        .create().show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.car_order_activity);

        initView();
        setView();
        dialog = new AlertDialog.Builder(CarOrderActivity.this)
                .setTitle("加载中...")
                .setMessage("提交置为未缴数据...")
                .setCancelable(false).create();
//        coinstance = this;

    }

    private GridView gvBowei;
    private InTheVehicleAdapter gvadapter;
    private ArrayList<InVehicleInfo> infos, INFO;

    private void initView() {
        lv_car_order = (ListView) findViewById(R.id.lv_car_order_orders);
        et_carnumber = (EditText) findViewById(R.id.et_car_order_number);
        ll_delete = (LinearLayout) findViewById(R.id.ll_car_order_delete);
        ll_back = (LinearLayout) findViewById(R.id.ll_car_order_back);
        tv_car_order_null = (TextView) findViewById(R.id.tv_car_order_null);
        bt_scancode = (Button) findViewById(R.id.bt_car_order_scanqrcode);
        et_carnumber.addTextChangedListener(mTextWatcher);
        adapter = new CarOrderAdapter(this);

//        infos = (ArrayList<InVehicleInfo>) getIntent().getSerializableExtra("infos");
//        INFO = new ArrayList<InVehicleInfo>();
        gvBowei = ((GridView) findViewById(R.id.gv_in_vehicle));

        INFO = new ArrayList<InVehicleInfo>();
        gvadapter = new InTheVehicleAdapter(context, INFO);
        gvBowei.setAdapter(gvadapter);


        gvBowei.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, CurrentOrderDetailsActivity.class);
                String orderid = INFO.get(position).getOrderid();
                intent.putExtra("orderid", orderid);
                intent.putExtra("ismonthuser", INFO.get(position).getIsmonthuser());
                intent.putExtra("iscard", INFO.get(position).getIs_card());
                putStringToPreference("berthorderid", getStringFromPreference(INFO.get(position).getId() + "222"));
                MyLog.i("CarOrderActivity", "点击条目的position是" + position + "点单号是" + orderid);
                startActivity(intent);
            }
        });
    }

    private void setView() {
        // 用于上拉加载更多
        // lv_car_order.setPullLoadEnable(true);
        // lv_car_order.setXListViewListener(this);
        // mHandler = new Handler();
        ll_delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 清除输入的内容；
                et_carnumber.setText("");
            }
        });

        ll_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 返回主界面；
                CarOrderActivity.this.finish();
            }
        });

        bt_scancode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击去调用扫描二维码界面;
                Intent intent = new Intent(CarOrderActivity.this, CaptureActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    TextWatcher mTextWatcher = new TextWatcher() {
        TimeCount time;

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            if (s.length() > 0) {
                if (s.length() == 1) {
                    time = new TimeCount(3000, 1000);
                    time.start();
                } else {
                    time.cancel();
                    time = new TimeCount(3000, 1000);
                    time.start();

                }

            }
        }
    };

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);//参数依次为总时长,和计时的时间间隔
        }

        @Override
        public void onFinish() {//计时完毕时触发
            queryOrder();
        }

        @Override
        public void onTick(long millisUntilFinished) {//计时过程显示
        }
    }

    public void setAdapter() {
        lv_car_order.setAdapter(adapter);
        // lv_car_order.setOnItemClickListener(new OnItemClickListener() {
        //
        // @Override
        // public void onItemClick(AdapterView<?> parent, View view,int
        // position, long id) {
        // // TODO Auto-generated method stub
        // adapter.onItemClick(position, CarOrderActivity.this);
        // }
        // });
        lv_car_order.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    // 当不滚动时
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        // 判断滚动到底部
                        // Toast.makeText(CarOrderActivity.this,
                        // count+":"+count+" viewCount:"+(view.getCount() -
                        // 1)+"visiblecount:"+visiblecount, 3).show();
                        if (view.getLastVisiblePosition() == (view.getCount() - 1) && view.getLastVisiblePosition() >= 0) {
                            if (count != visiblecount) {
                                adapter.onItemClick(view.getLastVisiblePosition(), CarOrderActivity.this);
                            } else {
                                AllOrder allOrders = adapter.getAllOrders(count);
                                if (allOrders == null) {
                                    adapter.onItemClick(view.getLastVisiblePosition(), CarOrderActivity.this);
                                }
                            }
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                visiblecount = totalItemCount;
            }
        });
    }

    public void setPageNumber() {
        pagenumber++;
    }

    // 根据输入的车牌尾号查询存在的订单；
    // http://192.168.199.240/zld/cobp.do?action=queryorder&comid=3&uid=100005&carnumber=aaabb
    //新接口cobp.do?action=getorders&carnumber=&comid=
    public void queryOrder() {
        SharedPreferences pfs = this.getSharedPreferences("autologin", Context.MODE_PRIVATE);
        String uid = pfs.getString("account", "");
        String path = Config.getUrl(this);
        String url = path + "cobp.do?action=getorders&comid=" + comid + "&uid=" + uid + "&carnumber="
                + et_carnumber.getText().toString().trim();
        MyLog.i("CarOrderActivity", "车牌查询订单的URL--->" + url);

        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            public void callback(String url, String object, AjaxStatus status) {

                if (status.getCode() == 200 && object != null) {
                    MyLog.i("查询到当前订单为", object);
                    Gson gson = new Gson();
                    HistoryOrder orders = gson.fromJson(object, HistoryOrder.class);
                    System.out.println("查询到当前订单的集合的长度" + orders.getInfo().size());
                    MyLog.i("解析到当前订单为", "-->>" + orders.toString());
                    int total = 0;
                    if (orders.getCount() != null) {
                        total = Integer.parseInt(orders.getCount());
                    }
                    adapter.addOrders(orders.getInfo(), total, CarOrderActivity.this, true);
                }
            }
        });
    }

    // 从网络上获取当前订单；
    // http://s.zhenlaidian.com/zld/cobp.do?action=getcurrorder&comid=1197&page=1&size=20

    public void getCarOrder() {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取订单失败，请检查网络！", Toast.LENGTH_SHORT).show();
            tv_car_order_null.setVisibility(View.VISIBLE);
            tv_car_order_null.setText("获取订单失败，请检查网络！");
            lv_car_order.setVisibility(View.INVISIBLE);
            return;
        }
        AQuery aQuery = new AQuery(this);
        String path = Config.getUrl(this);
        String url = path + "cobp.do?action=getcurrorder&comid=" + comid + "&page=" + pagenumber + "&size=" + size;
        MyLog.i("车牌识别-获取离场结算订单URL---->>", url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取离场订单数据...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            public void callback(String url, String object, AjaxStatus status) {

                if (status.getCode() == 200 && object != null) {
                    dialog.dismiss();
                    MyLog.i("获取到当前订单为", object);
                    Gson gson = new Gson();
                    HistoryOrder orders = gson.fromJson(object, HistoryOrder.class);
                    System.out.println("获取到当前订单的集合的长度" + orders.getInfo().size());
                    MyLog.i("解析到当前订单为", "-->>" + orders.toString());
                    int total = 0;
                    if (orders != null && orders.getCount() != null) {
                        count = Integer.parseInt(orders.getCount());
                        total = Integer.parseInt(orders.getCount());
                    }
                    adapter.addOrders(orders.getInfo(), total, CarOrderActivity.this, false);
                } else {
                    dialog.dismiss();
                }
            }
        });

    }

    // 把订单号提交给服务器结算订单；
    // http://192.168.199.240/zld/cobp.do?action=catorder&orderid=48260&comid=3
    public void cashOrder(final AllOrder allorder) {
        this.allorder = allorder;

        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "结算定单失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }

        AQuery aQuery = new AQuery(this);
        String path = Config.getUrl(this);
        String url = path + "cobp.do?action=catorder&orderid=" + allorder.getId() + "&comid=" + comid + "&ptype=1";
        MyLog.i("车牌识别-请求结算订单URL---->>", url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "请求结算订单数据...", true, true);
        dialog.setCanceledOnTouchOutside(false);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            public void callback(String url, String object, AjaxStatus status) {

                if (status.getCode() == 200 && object != null) {
                    dialog.dismiss();
                    MyLog.i("CarOrderActivity", "离场结算定订单返回的信息" + object);
                    Gson gson = new Gson();
                    NfcOrder order = gson.fromJson(object, NfcOrder.class);
                    if (order == null) {
                        return;
                    }
                    if (order.getIsfast() != null && order.getIsfast().equals("1") || order.getIsfast().equals("2")) {// 极速通类型,1照牌，2取卡；
                        FastOutCarNumberDialog dialog = new FastOutCarNumberDialog(CarOrderActivity.this,
                                R.style.nfcfinishdialog, order, allorder);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);
                        dialog.show();
                    } else if (order.getCollect0() == null) {
                        CarNumberOutDialog dialog = new CarNumberOutDialog(CarOrderActivity.this, R.style.nfcfinishdialog, order);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);
                        dialog.show();
                    } else {
                        if (order.getCollect1() == null) {
                            CarNumberOutAnHeQiaoDialog anheqiaoDialog = new CarNumberOutAnHeQiaoDialog(CarOrderActivity.this,
                                    R.style.nfcfinishdialog, order);
                            anheqiaoDialog.setCanceledOnTouchOutside(false);
                            anheqiaoDialog.setCancelable(false);
                            anheqiaoDialog.show();
                        } else {
                            CarNumberOutOnceDialog oncedialog = new CarNumberOutOnceDialog(CarOrderActivity.this,
                                    R.style.nfcfinishdialog, order);
                            oncedialog.setCanceledOnTouchOutside(false);
                            oncedialog.setCancelable(false);
                            oncedialog.show();
                        }
                    }
                } else {
                    dialog.dismiss();
                    switch (status.getCode()) {
                        case 500:
                            Toast.makeText(CarOrderActivity.this, "服务器错误！", Toast.LENGTH_SHORT).show();
                            break;
                        case 404:
                            Toast.makeText(CarOrderActivity.this, "服务器不可用！", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });

    }

    public void cashIbeaconOrder(final AllOrder order) {
        IbeaconOutDialog = new AlertDialog.Builder(this).create();
        View view = View.inflate(this, R.layout.dialog_ibeacon_out_order, null);
        TextView tv_ibeacon_out_number = (TextView) view.findViewById(R.id.tv_ibeacon_out_number);
        TextView tv_change_add = (TextView) view.findViewById(R.id.tv_ibeacon_out_change_money_add);
        TextView tv_change_subtract = (TextView) view.findViewById(R.id.tv_ibeacon_out_change_money_subtract);
        TextView tv_ibeacon_out_time = (TextView) view.findViewById(R.id.tv_ibeacon_out_time);
        TextView tv_ibeacon_out_accomplish = (TextView) view.findViewById(R.id.tv_ibeacon_out_accomplish);
        final EditText et_change_money_total = (EditText) view.findViewById(R.id.et_ibeacon_out_change_money_money);
        if (!TextUtils.isEmpty(order.getCarnumber())) {
            tv_ibeacon_out_number.setText(order.getCarnumber());
        }
        if (!TextUtils.isEmpty(order.getDuration())) {
            tv_ibeacon_out_time.setText("停车：" + order.getDuration());
        }
        if (!TextUtils.isEmpty(order.getTotal())) {
            et_change_money_total.setText(order.getTotal());
        }
        tv_change_add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 点击对金额做 +1 处理；
                try {
                    Double cmoney = parseDouble(et_change_money_total.getText().toString());
                    et_change_money_total.setText((cmoney + 1.0) + "");
                    order.setTotal((cmoney + 1.0) + "");
                } catch (Exception e) {
                    MyLog.i("LeaveAdapter", "点击对金额做加上1处理——类型转换异常");
                }
            }
        });

        tv_change_subtract.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO 点击对金额做 -1 处理；
                try {
                    Double cmoney = parseDouble(et_change_money_total.getText().toString());
                    if (cmoney >= 1.0) {
                        et_change_money_total.setText((cmoney - 1.0) + "");
                        order.setTotal((cmoney + 1.0) + "");
                    }
                } catch (Exception e) {
                    MyLog.i("LeaveAdapter", "点击对金额做减去1处理——类型转换异常");
                }
            }
        });
        et_change_money_total.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                order.setTotal(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                order.setTotal(s.toString());
            }
        });
        tv_ibeacon_out_accomplish.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO 点击去结算Ibeacon订单；
                order.setTotal(et_change_money_total.getText().toString() == null ? "0.0" : et_change_money_total.getText()
                        .toString());
                getcashIbeaconOrder(order, IbeaconOutDialog);
                try {
                    View view = CarOrderActivity.this.getWindow().peekDecorView();
                    if (view != null) {
                        InputMethodManager inputmanger = (InputMethodManager) CarOrderActivity.this
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputmanger.hideSoftInputFromWindow(view.getWindowToken(), Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    MyLog.i("leaveAdapter", "关闭输入法异常");
                }
            }
        });
        IbeaconOutDialog.setView(view);
        IbeaconOutDialog.setCancelable(true);
        IbeaconOutDialog.setCanceledOnTouchOutside(false);
        IbeaconOutDialog.show();
        Display d = getWindowManager().getDefaultDisplay(); // 获取屏幕宽、高用
        int height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
        int width = (int) (d.getWidth() * 1); // 宽度设置为屏幕的0.65
        IbeaconOutDialog.getWindow().setLayout(width, height);

    }

    // ibencon调价后结算的接口 ibeaconhandle.do?action=payorder&id=&total=
    // id:订单编号//total:订单金额
    // 返回：{\"result\":\"2\",\"info\":\"已支付过，不能重复支付\"}
    // result 0失败 1成功 2错误：重复支付
    // info:提示信息
    public void getcashIbeaconOrder(final AllOrder order, final AlertDialog ibeacondialog) {
        this.allorder = order;
        String path = Config.getUrl(this);
        String url = path + "ibeaconhandle.do?action=payorder&id=" + order.getId() + "&total=" + order.getTotal();
        AQuery aQuery = new AQuery(this);
        System.out.println("ibencon调价后结算接口URL——>>" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "结算中...", "结算蓝牙订单...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (!TextUtils.isEmpty(object)) {
                    dialog.dismiss();
                    MyLog.i("ShowCashDealDialog---->>>", "info的信息" + object);
                    Gson gson = new Gson();
                    IbeaconCashInfo info = gson.fromJson(object, IbeaconCashInfo.class);
                    if (info != null && "1".equals(info.result)) {
                        Toast.makeText(CarOrderActivity.this, "收费结算成功", Toast.LENGTH_SHORT).show();
                        if (allorder != null) {
                            adapter.deleteOrder(allorder);
                            allorder = null;
                        }
                        ibeacondialog.dismiss();
                    } else if (info != null && "0".equals(info.result)) {
                        showDialog(info.info, order);
                        ibeacondialog.dismiss();
                    } else if (info != null && "2".equals(info.result)) {
                        Toast.makeText(CarOrderActivity.this, "收费结算失败--" + info.getInfo(), Toast.LENGTH_SHORT).show();
                        if (allorder != null) {
                            adapter.deleteOrder(allorder);
                            allorder = null;
                        }
                        ibeacondialog.dismiss();
                    } else {
                        ibeacondialog.dismiss();
                        showDialog("", order);
                    }
                } else {
                    dialog.dismiss();
                    Toast.makeText(CarOrderActivity.this, "网络错误！收费结算失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showDialog(String info, AllOrder order) {
        if (allorder != null) {
            adapter.deleteOrder(allorder);
            allorder = null;
        }
        new AlertDialog.Builder(this).setIcon(R.drawable.app_icon_32).setTitle("自动支付失败！")
                .setMessage(order.getCarnumber() + " " + info + ",无法完成自动支付，请收取现金(同时将订单金额调至0元结算掉)").setNegativeButton("知道了", null)
                .setCancelable(false).create().show();
    }

    // 把确认结算订单提交给服务器；返回1提交成功；
    // http://192.168.1.102/zld/nfchandle.do?action=completeorder&orderid=78&collect=20&comid=3  //pay=1现金结算
    public void sumitCahsOrder(final NfcOrder nfcOrder, final String orderid, final String collect, final Dialog dialog, String pay) {
        SharedPreferences pfs = this.getSharedPreferences("autologin", Context.MODE_PRIVATE);
        String uid = pfs.getString("account", "");
        String path = Config.getUrl(this);
        String url = path + "nfchandle.do?action=completeorder&orderid=" + orderid + "&collect=" + collect + "&comid="
                + comid + "&uid=" + uid + "&imei=" + imei + "&ptype" + "&pay=" + pay;
        MyLog.i("CarOrderActivity", "车牌号结算订单的URL--->" + url);
        AQuery aQuery = new AQuery(this);
        final ProgressDialog pd = ProgressDialog.show(this, "结算中...", "正在提交结算数据...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200 && object != null) {
                    pd.dismiss();
                    MyLog.i("CarOrderActivity", "订单结算结果--->" + object);
                    if ("1".equals(object) || "2".equals(object) || "3".equals(object) || "4".equals(object)
                            || "5".equals(object)) {
                        Message msg = new Message();
                        msg.what = 2;
                        msg.obj = new MainUiInfo(true, 5, 1.00);
                        LeaveActivity.handler.sendMessage(msg);

                        String uid = getSharedPreferences("autologin", Context.MODE_PRIVATE).getString("account", "");
                        TcbCheckCarOut out = new TcbCheckCarOut(orderid, nfcOrder.getCarnumber(), nfcOrder.getBtimestr(),
                                nfcOrder.getEtimestr(), nfcOrder.getDuration(), SharedPreferencesUtils.
                                getIntance(CarOrderActivity.this).getName() + "(" + uid + ")", collect);
                        //打印凭条
                        prientCarOut(out);
                    }
                    if (object.equals("1")) {
                        dialog.dismiss();
                        Toast.makeText(CarOrderActivity.this, "结算订单成功！", Toast.LENGTH_SHORT).show();
                        if (allorder != null) {
                            adapter.deleteOrder(allorder);
                            allorder = null;
                        }
                    } else if (object.equals("2")) {
                        showErrorDialog(object);
                        dialog.dismiss();
                    } else if (object.equals("-2")) {
                        showErrorDialog(object);
                        dialog.dismiss();
                    } else if (object.equals("3")) {
                        dialog.dismiss();
                        showErrorDialog(object);
                    } else if (object.equals("-5")) {//正在预支付,可现金结;
                        dialog.dismiss();
                        StopWaitToCashDialog stopdialog = new StopWaitToCashDialog(CarOrderActivity.this, CarOrderActivity.this, nfcOrder, orderid, collect, false);
                        stopdialog.show();
                    } else if (object.equals("-6")) {//已经有预支付金额,不能现金结
                        dialog.dismiss();
                        StopWaitToCashDialog stopdialog = new StopWaitToCashDialog(CarOrderActivity.this, CarOrderActivity.this, nfcOrder, orderid, collect, false);
                        stopdialog.show();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(CarOrderActivity.this, object + " 结算订单失败！", Toast.LENGTH_SHORT).show();
                        showErrorDialog("结算失败");
                    }
                } else {
                    pd.dismiss();
                    if (status.getCode() == -101) {
                        Toast.makeText(CarOrderActivity.this, "网络错误！--请再次结算或联系停车宝处理！", Toast.LENGTH_SHORT).show();
                    } else if (status.getCode() == 500) {
                        Toast.makeText(CarOrderActivity.this, "服务器错误！--请再次结算或联系停车宝处理！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CarOrderActivity.this, "网络请求错误!--请再次结算或联系停车宝处理！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void showErrorDialog(String cade) {
        String title;
        String message;
        if (cade.equals("2")) {
            title = "车主余额不足";
            message = allorder.getCarnumber() + "停车宝账户余额不足,无法完成自动支付,请通知车主手动支付或者收取现金！";
        } else if (cade.equals("-2")) {
            title = "相同的车牌号已存在订单";
            message = allorder.getCarnumber() + "在本车场已存在订单！";
        } else if (cade.equals("3")) {
            title = "速通卡用户未设置自动支付";
            message = allorder.getCarnumber() + "用户没有设置自动支付！";
        } else {
            title = "离场-订单结算失败";
            message = "请再次点击要结算的订单！";
        }
        new AlertDialog.Builder(this).setIcon(R.drawable.app_icon_32).setTitle(title).setMessage(message)
                .setNegativeButton("知道了", null).setCancelable(false).create().show();
    }

    // 结算BLE照牌订单：collectorrequest.do?action=autoup&price=&carnumber=&token=0dc591f7ddda2d6fb73cd8c2b4e4a372
    // （）充足：返回：{state:1,orderid,btime,etime,carnumber,duration,total}
    // （）不足：返回 {state:2,prefee,total,collect}
    // 其它返回：{state:-?,errmsg:XXXX}
    // {\"state\":\"-1\",\"errmsg\":\"车牌没有注册!\"
    // {\"state\":\"-2\",\"errmsg\":\"价格不对
    // {\"state\":\"-3\",\"errmsg\":\"没有停车场或收费员信息，请重新登录!\"
    // {\"state\":\"-4\",\"errmsg\":\"生成订单失败!\"
    // {\"state\":\"-5\",\"errmsg\":\"已支付，不能重复支付!\",
    // {\"state\":\"-6\",\"errmsg\":\"支付失败!\"
    // {\"state\":\"-7\",\"errmsg\":\"余额不足!\"
    // {\"state\":\"-8\",\"errmsg\":\"未开启自动支付!\"
    // {\"state\":\"-9\",\"errmsg\":\"停车费超出自动支付限额!\"
    // {\"state\":\"-10\",\"errmsg\":\"极速通卡未注册!\"
    // {\"state\":\"-11\",\"errmsg\":\"取卡极速通没有预支付!\"
    public void CashoutBLEOrder(final NfcOrder order, final Dialog dialog, final AllOrder allorder) {
        try {
            String path = Config.getUrl(this);
            String url = null;
            if ("1".equals(order.getIsfast())) {
                String carnumber = URLEncoder.encode(URLEncoder.encode(order.getCarnumber(), "utf-8"), "utf-8");
                url = path + "collectorrequest.do?action=autoup&price=" + order.getTotal() + "&carnumber=" + carnumber
                        + "&token=" + token;
            } else if ("2".equals(order.getIsfast())) {
                url = path + "collectorrequest.do?action=autoup&price=" + order.getTotal() + "&cardno=" + order.carnumber
                        + "&token=" + token;
            }
            MyLog.i("CarOrderActivity", "BLE照牌提交并结算订单的URL-->>" + url);
            final ProgressDialog pd = ProgressDialog.show(this, "结算中...", "手动结算极速通订单...", true, true);
            AQuery aq = new AQuery(this);
            aq.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (!TextUtils.isEmpty(object)) {
                        pd.dismiss();
                        VoiceSynthesizerUtil vUtil = new VoiceSynthesizerUtil(CarOrderActivity.this);
                        MyLog.i("CarOrderActivity", "BLE照牌手动提交并结算订单的结果是：" + object);
                        Gson gson = new Gson();
                        LeaveOrder orderinfo = gson.fromJson(object, LeaveOrder.class);
                        if ("1".equals(orderinfo.getState())) {
                            dialog.dismiss();
                            adapter.deleteOrder(allorder);
                            if (!TextUtils.isEmpty(orderinfo.getTotal()) && parseDouble(orderinfo.getTotal()) == 0) {
                                Toast.makeText(CarOrderActivity.this, "0元结算，直接放行", Toast.LENGTH_SHORT).show();
                            } else {
                                BLEService.writeChar6("[CKTG]");
                                MyLog.i("CarOrderActivity", "加载到离场订单...");
                                Message msg1 = new Message();
                                msg1.what = 1;// 加载到主界面离场订单；
                                orderinfo.setState("2");
                                msg1.obj = orderinfo;
                                LeaveActivity.handler.sendMessage(msg1);
                                new PlayerVoiceUtil(CarOrderActivity.this, R.raw.phone_pay).play();
                            }
                        } else if ("2".equals(orderinfo.getState())) {
                            dialog.dismiss();
                            adapter.deleteOrder(allorder);
                            vUtil.playText("请向车主补收现金" + orderinfo.getCollect() + "元");
                            MyLog.i("CarOrderActivity", "主界面弹出补交现金对话框...");
                            NfcPrepaymentOrder nfcPrepaymentOrder = new NfcPrepaymentOrder("", orderinfo.getPrefee(), orderinfo
                                    .getTotal(), orderinfo.getCollect());
                            notBalanceDialog(nfcPrepaymentOrder);
                        } else if ("-1".equals(orderinfo.getState())) {
                            dialog.dismiss();
                            adapter.deleteOrder(allorder);
                            Toast.makeText(CarOrderActivity.this, "不是会员,请收现金", Toast.LENGTH_SHORT).show();
                        } else {
                            if ("-7".equals(orderinfo.getState())) {
                                new PlayerVoiceUtil(CarOrderActivity.this, R.raw.balance_no_more).play();
                            } else if ("-8".equals(orderinfo.getState())) {
                                new PlayerVoiceUtil(CarOrderActivity.this, R.raw.not_set_auto_pay).play();
                            } else if ("-9".equals(orderinfo.getState())) {
                                new PlayerVoiceUtil(CarOrderActivity.this, R.raw.total_morethan_autopay).play();
                            }
                            dialog.dismiss();
                            adapter.deleteOrder(allorder);
                            NfcOrder nfcorder = new NfcOrder();
                            nfcorder.setOrderid(orderinfo.getOrderid());
                            nfcorder.setNetError(orderinfo.getState() + ":" + orderinfo.getErrmsg());
                            FinishOrderFailDialog failDialog = new FinishOrderFailDialog(CarOrderActivity.this,
                                    R.style.nfcnewdialog, nfcorder, "fast");
                            failDialog.setCanceledOnTouchOutside(false);
                            failDialog.show();
                        }
                    } else {
                        pd.dismiss();
                        MyLog.w("CarOrderActivity", "BLE照牌提交并结算--网络错误！！！");
                        Toast.makeText(CarOrderActivity.this, "BLE照牌提交并结算--网络错误", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(CarOrderActivity.this, "车牌转码异常！！！", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // 还需要补交现金的对话框；
    public void notBalanceDialog(NfcPrepaymentOrder preorder) {
        View open_dialog_view = View.inflate(CarOrderActivity.this, R.layout.dialog_repayment_money_nfcorder, null);
        TextView tv_collect = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_collect);
        TextView tv_total = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_total);
        TextView tv_prefee = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_prefee);
        TextView tv_collect1 = (TextView) open_dialog_view.findViewById(R.id.tv_dialog_repayment_nfcorder_collect1);
        Button bt_nfcorder_ok = (Button) open_dialog_view.findViewById(R.id.bt_dialog_repayment_nfcorder_ok);
        tv_collect.setText(preorder.getCollect() != null ? "还需向车主补收" + preorder.getCollect() + "元现金" : "");
        tv_collect1.setText(preorder.getCollect() != null ? preorder.getCollect() : "");
        tv_total.setText(preorder.getTotal() != null ? "停车费			" + preorder.getTotal() + "元" : "");
        tv_prefee.setText(preorder.getPrefee() != null ? "微信预付		" + preorder.getPrefee() + "元" : "");
        final Dialog openDialog = new Builder(CarOrderActivity.this).create();
        bt_nfcorder_ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openDialog.dismiss();
                BLEService.writeChar6("[CKTG]");
            }
        });
        openDialog.setCancelable(false);
        openDialog.show();
        openDialog.setContentView(open_dialog_view);
    }

    /*
    * 预付费的结算接口；
    * http://127.0.0.1/zld/nfchandle.do?action=doprepayorder&orderid=&
    * collect=20&comid="+ comid
    */
    public void cashPrepayOrder(final String collect, final NfcOrder nfcOrder, final Dialog dialog) {
        String path = Config.getUrl(this);
        String url = path + "nfchandle.do?action=doprepayorder&orderid=" + nfcOrder.getOrderid() + "&collect=" + collect
                + "&comid=" + comid;
        MyLog.i("ShowNfcOrder", "确认结算预付费NFC订单的URL--->" + url);
        AQuery aQuery = new AQuery(this);
        final ProgressDialog pd = ProgressDialog.show(this, "结算中...", "正在提交结算数据...", true, true);
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

                            String uid = getSharedPreferences("autologin", Context.MODE_PRIVATE).getString("account", "");
                            TcbCheckCarOut out = new TcbCheckCarOut(nfcOrder.getOrderid(), nfcOrder.getCarnumber(), nfcOrder.getBtimestr(),
                                    nfcOrder.getEtimestr(), nfcOrder.getDuration(), SharedPreferencesUtils.
                                    getIntance(CarOrderActivity.this).getName() + " (" + uid + ")", collect);
                            //打印凭条
                            prientCarOut(out);

                        }
                        if ("1".equals(preorder.getResult())) {// 1成功 -1失败
                            if (collect != null) {
                                try {
                                    Message msg = new Message();
                                    msg.what = 2;
                                    msg.obj = new MainUiInfo(true, 1, parseDouble(collect));
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

    //结算错误的对话框
    public void showFinishFailDialog(NfcOrder order) {
        FinishOrderFailDialog failDialog = new FinishOrderFailDialog(this, R.style.nfcnewdialog, order, "nfc");
        failDialog.setCanceledOnTouchOutside(false);
        failDialog.show();
    }

    // 微信预支付成功后的弹框；
    public void ScanMyCodeCash(LeaveOrder order) {
        EPayMessageDialog dialog = new EPayMessageDialog(this, R.style.nfcnewdialog, order);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();

        MobclickAgent.onResume(this);
        //每次都读一下 存好的泊位所在的状态，每次处理完订单，从消息list中移除车位号
        if (CommontUtils.checkString(getStringFromPreference("boweistate"))) {
            String changedbowei = getStringFromPreference("boweistate");
//            System.out.println("-------" + getStringFromPreference("boweistate"));
//            putStringToPreference(changedbowei,"-10");
//            //移除一个泊位以后置空，避免下次误移除
//            putStringToPreference("boweistate","");
            //结算成功后将车检器订单置 空
            if (getStringFromPreference("berthorderid").equals("succeed")) {
                putStringToPreference(changedbowei + "222", "");
            }
        }
        if (SharedPreferencesUtils.getIntance(context).getview_plot().equals("1")) {
            gvBowei.setVisibility(View.VISIBLE);
            lv_car_order.setVisibility(View.GONE);
            ll_back.setVisibility(View.GONE);
            bt_scancode.setVisibility(View.GONE);
            getInVehicleInfo();

        } else {
            gvBowei.setVisibility(View.GONE);
            lv_car_order.setVisibility(View.VISIBLE);
            ll_back.setVisibility(View.VISIBLE);
//            bt_scancode.setVisibility(View.VISIBLE);

            getCarOrder();
        }
    }

//        public void onResume() {
//        super.onResume();
//        getInVehicleInfo();
//        MobclickAgent.onResume(this);
//    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String qrcode = (String) data.getExtras().get("qrcodeinfo");
            getQrcedeInfo(qrcode);
        }
    }

    /**
     * 访问扫码返回结果的url;
     */
    public void getQrcedeInfo(String qrUrl) {
        String url = qrUrl + "&comid=" + comid + "&uid=" + useraccount;
        MyLog.i("CarOrderActivity", "在离场结算扫码得到的URL-->>" + url);
        final ProgressDialog pd = ProgressDialog.show(this, "访问中...", "获取二维码的类型...", true, true);
        AQuery aQuery = new AQuery(this);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                super.callback(url, object, status);
                pd.dismiss();
                MyLog.i("CarOrderActivity", "获取到二维码的内容:-->>" + object);
                if (!TextUtils.isEmpty(object)) {
                    try {
                        JSONObject json = new JSONObject(object);
                        String type = json.getString("type");
                        //type 是"0"扫码结算,调用结算订单的dialog;
                        if (!TextUtils.isEmpty(type) && type.equals("0")) {
                            Intent intent = new Intent(CarOrderActivity.this, LeaveActivity.class);
                            intent.putExtra("posnfcorder", json.getString("info"));
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(CarOrderActivity.this, "请扫描订单二维码!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                }

            }
        });

    }

    //用dialog查看大图片;
    public void showMaxImgDialog(File file) {
        AlertDialog.Builder builder = new Builder(this);
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ImageLoader.getInstance().displayImage("file://" + file.getAbsolutePath(), imageView);
        builder.setView(imageView);
        builder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    //打印凭条
    public void prientCarOut(TcbCheckCarOut info) {
        Bitmap imgbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_check);
        PrinterUitls.getInstance().printerTCBCheckCarOut(info, null, imgbitmap);
    }

    public BoWeiListEntity Data;

    public void getInVehicleInfo() {

        if (!IsNetWork.IsHaveInternet(context)) {
            Toast.makeText(context, "请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        //测试链接
        // http://127.0.0.1/zld/collectorrequest.do?action=getberths&token=55c0fa9053658bb84e73169d8c742342&berthid=6&devicecode=
        String url = BaseActivity.baseurl + "collectorrequest.do?action=getberths&out=josn&token=" + BaseActivity.token +
                "&berthid=" + SharedPreferencesUtils.getIntance(context).getberthid() +
                "&devicecode=" + CommontUtils.GetHardWareAddress(context);
//        String url = BaseActivity.baseurl + "collectorrequest.do?action=comparks&out=josn&token=" + BaseActivity.token;
        MyLog.w("InTheVehicleActivity", "获取在场车辆的URL--->" + url);
        AQuery aQuery = new AQuery(context);
        final ProgressDialog dialog = ProgressDialog.show(this, "获取在场车辆数据", "获取中...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                dialog.dismiss();

                if (!TextUtils.isEmpty(object)) {
                    MyLog.d("InTheVehicleActivity", "获取在场车辆结果--->" + object);
                    Gson gson = new Gson();
                    Data = gson.fromJson(object, new TypeToken<BoWeiListEntity>() {
                    }.getType());
                    SharedPreferencesUtils.getIntance(context).setworkid(Data.getWorkid());
                    String msg = Data.getErrmsg();
                    if (CommontUtils.checkString(Data.getState())) {
//                        RegisDevice();
                        if (Integer.parseInt(Data.getState()) != 1) {
                            //非正常状态
//                            CommontUtils.toast(context, "获取数据出错！");
                        } else {
                            if (!getBooleanFromPreference("alreadyalert")) {
                                //已签到，显示签到时间
                                if (CommontUtils.checkString(Data.getErrmsg())) {
                                    new AlertDialog.Builder(context).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                            .setMessage(msg).setNegativeButton("确定", null).create().show();
                                }
                                putBooleanToPreference("alreadyalert", true);
                            }
                            if (CommontUtils.checkString(Data.getComid())) {
                                SharedPreferencesUtils.getIntance(context).setComid(Data.getComid());
                            }
                            if (CommontUtils.checkString(Data.getCname())) {
                                SharedPreferencesUtils.getIntance(context).setParkname(Data.getCname());
                            }
//                            System.out.println(Data.getComid()+Data.getCname());
                            infos = Data.getData();
                            INFO.clear();
                            if (CommontUtils.checkList(infos)) {
                                for (int i = 0; i < infos.size(); i++) {
                                    if (CommontUtils.checkString(infos.get(i).getCar_number())) {
                                        INFO.add(infos.get(i));
                                    }
                                }
                            }
                            MyLog.i("InTheVehicleActivity", "解析在场车辆结果--->" + infos.toString());
                            gvadapter.notifyDataSetChanged();
                        }
                    } else {
//                        Toast.makeText(context, "获取数据出错！", Toast.LENGTH_LONG).show();
                    }


                }
            }
        });
    }

    AlertDialog dialog ;

    //            (this, "加载中...", "提交置为未缴数据...", true, true);
    //置为未缴：  cobp.do?action=escape&orderid=124614&comid=10&total=100.98；
    public void MakeLostOrder(final AllOrder order) {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "置为未缴失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }
        AQuery aQuery = new AQuery(this);
        String path = baseurl;
        String url = path + "cobp.do?action=escape&orderid=" + order.getOrderid() +
                "&comid=" + comid +
                "&total=" + order.getTotal() +
                "&workid=" + SharedPreferencesUtils.getIntance(this).getworkid() +
                "&uid=" + getStringFromPreference("uid") + "" +
                "&brethorderid=" + getStringFromPreference("berthorderid") +
                "&endtime=" + order.getEnd();
        MyLog.w("置为未缴的URl-->>", url);

        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            public void callback(String url, String object, AjaxStatus status) {
                MyLog.i("CurrentOrderDetailsActivity", "置为未缴返回的结果是-->>" + object);
                if (object != null) {
                    dialog.dismiss();
                    if (object.equals("1")) {
                        //结算成功后将车检器订单置 succeed
                        putStringToPreference("berthorderid", "succeed");
//                        Toast.makeText(CarOrderActivity.this, order.getCarnumber() + "置为未缴成功！", Toast.LENGTH_SHORT).show();
//                        CarOrderActivity.this.finish();
                        if (INFO != null && INFO.size() > 0) {
                            INFO.remove(0);
                            gvadapter.notifyDataSetChanged();
                            if (INFO.size() > 0) {
                                getOrderinfo(INFO.get(0).getOrderid());
                            } else {
                                finish();
                            }
                        } else {
                            finish();
                        }
                    } else if (object.equals("-1")) {
                        Toast.makeText(CarOrderActivity.this, order.getCarnumber() + "置为未缴失败！", Toast.LENGTH_SHORT).show();
                        onResume();
                    } else if (object.equals("-2")) {
                        Toast.makeText(CarOrderActivity.this, order.getCarnumber() + "已置为未缴，不可再次置为未缴！", Toast.LENGTH_SHORT).show();
                        onResume();
                    } else if (object.equals("-3")) {
                        Toast.makeText(CarOrderActivity.this, order.getCarnumber() + "0元订单不可置为未缴！", Toast.LENGTH_SHORT).show();
                        onResume();
                    } else {
                        Toast.makeText(CarOrderActivity.this, "未知错误！", Toast.LENGTH_SHORT).show();
                        onResume();
                    }
                } else {
                    if (dialog.isShowing())
                        dialog.dismiss();
                    Toast.makeText(CarOrderActivity.this, "网络错误！", Toast.LENGTH_SHORT).show();
                    onResume();
                    return;
                }
            }
        });
    }

    public void getOrderinfo(String orderid) {
        // 从服务器获取数据.设置到界面上;
        AQuery aQuery = new AQuery(CarOrderActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=orderdetail&token=" + token
                + "&orderid=" + orderid
                + "&brethorderid=" + getStringFromPreference("berthorderid")
                + "&out=json";
        MyLog.i("CurrentOrderDetailsActivity", "url>>>>>" + url);
        if (IsNetWork.IsHaveInternet(this)) {
//            final ProgressDialog dialog = ProgressDialog.show(this, "加载中...",
//                    "获取当前订单数据...", true, true);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (!TextUtils.isEmpty(object)) {
//                        dialog.dismiss();
                        MyLog.i("CurrentOrderDetailsActivity", "返回的当前订单详情：" + object);
                        Gson gson = new Gson();
                        AllOrder order = gson.fromJson(object, AllOrder.class);
                        MyLog.i("CurrentOrderDetailsActivity", "解析的当前订单详情：" + order.toString());
                        if (order != null && !TextUtils.isEmpty(order.getOrderid())) {
                            String total = order.getTotal();
                            String prepay = order.getPrepay();
                            if (!TextUtils.isEmpty(order.getIsmonthuser()) && order.getIsmonthuser().equals("5")) {
                                Toast.makeText(CarOrderActivity.this, "月卡用户无法置为未缴", Toast.LENGTH_LONG).show();
                                if (dialog.isShowing())
                                    dialog.dismiss();
                                return;
                            }
                            if (!TextUtils.isEmpty(total) && !TextUtils.isEmpty(prepay)) {
                                if (Double.parseDouble(total) == 0) {
                                    Toast.makeText(CarOrderActivity.this, "停车费0元无法置为未缴", Toast.LENGTH_LONG).show();
                                    if (dialog.isShowing())
                                        dialog.dismiss();
                                    return;
                                }
                                double pay = Double.parseDouble(total) - Double.parseDouble(prepay);
                                if (pay > 0) {
                                    MakeLostOrder(order);
                                } else {
                                    Toast.makeText(CarOrderActivity.this, "预支付金额大于停车费，无法置为未缴", Toast.LENGTH_LONG).show();
                                    if (dialog.isShowing())
                                        dialog.dismiss();
                                    return;
                                }
                            } else {
                                MakeLostOrder(order);
                            }

                        } else {
                            Toast.makeText(CarOrderActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                            onResume();
                        }
                    } else {
//                        dialog.dismiss();
                        onResume();
                        if (dialog.isShowing())
                            dialog.dismiss();
                        return;
                    }
                }

            });
        } else {
            Toast.makeText(this, "请检查网络!", Toast.LENGTH_SHORT).show();
            onResume();
        }
    }
}
