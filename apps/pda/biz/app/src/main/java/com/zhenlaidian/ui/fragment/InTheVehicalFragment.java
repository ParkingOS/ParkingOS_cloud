//package com.zhenlaidian.ui.fragment;
//
//import android.app.AlertDialog;
//import android.app.ProgressDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.support.v7.app.ActionBar;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.view.LayoutInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.EditText;
//import android.widget.GridView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.androidquery.AQuery;
//import com.androidquery.callback.AjaxCallback;
//import com.androidquery.callback.AjaxStatus;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//import com.zhenlaidian.R;
//import com.zhenlaidian.adapter.InTheVehicleAdapter;
//import com.zhenlaidian.bean.BaseRegisBean;
//import com.zhenlaidian.bean.BoWeiListEntity;
//import com.zhenlaidian.bean.InVehicleInfo;
//import com.zhenlaidian.engine.SelectParkPositionListener;
//import com.zhenlaidian.service.BLEService;
//import com.zhenlaidian.service.PullMsgService;
//import com.zhenlaidian.ui.BaseActivity;
//import com.zhenlaidian.ui.CurrentOrderDetailsActivity;
//import com.zhenlaidian.ui.InputCarNumberActivity;
//import com.zhenlaidian.ui.LeaveActivity;
//import com.zhenlaidian.ui.LoginActivity;
//import com.zhenlaidian.util.CommontUtils;
//import com.zhenlaidian.util.Constant;
//import com.zhenlaidian.util.IsNetWork;
//import com.zhenlaidian.util.MyLog;
//import com.zhenlaidian.util.SharedPreferencesUtils;
//
//import java.util.ArrayList;
//
///**
// * Created by TCB on 2016/4/16.
// */
//
//
///**
// * 在停车辆
// */
//public class InTheVehicalFragment extends Fragment implements SelectParkPositionListener, View.OnClickListener {
//
//    public ActionBar actionBar;
//    public ArrayList<InVehicleInfo> infos, INFO, searchinfo;
//    public BoWeiListEntity Data;
//    public InTheVehicleAdapter adapter;
//    public GridView gv_in_vehicle;
//    public TextView tv_parking_state;// 停车状态
//    protected SharedPreferences sharedPreferences;
//    private LeaveActivity activity;
//    public InTheVehicalFragment() {
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.activity_in_the_vehicle, null);
//        activity = (LeaveActivity)getActivity();
//        sharedPreferences = CommontUtils.getSharedPreferences(activity);
//        initView(view);
//        initHeadFoot(view);
//        return view;
//    }
//
//    private EditText edtSearch;
//    private LinearLayout lnDelete;
//
//    private void initHeadFoot(View view) {
//        edtSearch = ((EditText) view.findViewById(R.id.tv_pay_log_number));
//        lnDelete = ((LinearLayout) view.findViewById(R.id.ll_pay_log_delete));
//
//        lnDelete.setOnClickListener(this);
//        INFO = new ArrayList<InVehicleInfo>();
//        infos = new ArrayList<InVehicleInfo>();
//
//        adapter = new InTheVehicleAdapter(activity, INFO);
//        gv_in_vehicle.setAdapter(adapter);
//
//        edtSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                //输入后搜索
//                searchinfo = new ArrayList<InVehicleInfo>();
//                if (CommontUtils.checkString(s.toString())) {
//                    for (int i = 0; i < infos.size(); i++) {
//                        if (infos.get(i).getBer_name().contains(s.toString()) || infos.get(i).getCar_number().contains(s.toString())) {
//                            searchinfo.add(infos.get(i));
//                        }
//                    }
//                    INFO.clear();
//                    INFO.addAll(searchinfo);
//                } else {
//                    INFO.clear();
//                    INFO.addAll(infos);
//                }
//                if (adapter != null) {
//                    adapter.notifyDataSetChanged();
//                }
//            }
//        });
//    }
//
//
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.ll_pay_log_delete:
//                //删除按钮
//                String tmp = edtSearch.getText().toString();
//                if (CommontUtils.checkString(tmp)) {
//                    String newtemp = tmp.substring(0, tmp.length() - 1);
//                    edtSearch.setText(newtemp);
//                    edtSearch.setSelection(newtemp.length());
//                }
//                break;
//
//        }
//    }
//
//    public void initView(View view) {
//        gv_in_vehicle = (GridView) view.findViewById(R.id.gv_in_vehicle);
//        tv_parking_state = (TextView) view.findViewById(R.id.tv_in_vehicle_parking_state);
//        gv_in_vehicle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                //车检器消息返回的车检器订单编号
//                if(CommontUtils.checkString(INFO.get(position).getBerthorderid())){
//                    putStringToPreference("berthorderid",INFO.get(position).getBerthorderid());
//                }else{
//                    putStringToPreference("berthorderid","");
//                }
//                if (TextUtils.isEmpty(INFO.get(position).getOrderid())) {
//                    putStringToPreference("bowei", INFO.get(position).getId());//泊位id
//                    putStringToPreference("boweiversion", INFO.get(position).getBer_name());//泊位名
//                    Intent i =  new Intent(activity, InputCarNumberActivity.class);
//                    i.putExtra("from","input");
//                    startActivity(i);
//                } else {
//                    Intent intent = new Intent(activity, CurrentOrderDetailsActivity.class);
//                    String orderid = INFO.get(position).getOrderid();
//                    intent.putExtra("orderid", orderid);
//                    intent.putExtra("ismonthuser",INFO.get(position).getIsmonthuser());
//                    MyLog.i("InTheVehicleActivity", "点击条目的position是" + position + "点单号是" + orderid);
//                    activity.startActivity(intent);
//                }
//
//            }
//        });
//    }
//
//    // 获取在场车辆信息collectorrequest.do?action=comparks&out=josn&token=5f0c0edb1cc891ac9c3fa248a28c14d5
//    public void getInVehicleInfo() {
//        INFO.clear();
//        if (!IsNetWork.IsHaveInternet(activity)) {
//            Toast.makeText(activity, "请检查网络", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        //测试链接
//        // http://127.0.0.1/zld/collectorrequest.do?action=getberths&token=55c0fa9053658bb84e73169d8c742342&berthid=6&devicecode=
//        String url = BaseActivity.baseurl + "collectorrequest.do?action=getberths&out=josn&token=" + BaseActivity.token +
//                "&berthid=" + SharedPreferencesUtils.getIntance(activity).getberthid() +
//                "&devicecode=" + CommontUtils.GetHardWareAddress(activity);
////        String url = BaseActivity.baseurl + "collectorrequest.do?action=comparks&out=josn&token=" + BaseActivity.token;
//        MyLog.i("InTheVehicleActivity", "获取在场车辆的URL--->" + url);
//        AQuery aQuery = new AQuery(activity);
//        final ProgressDialog dialog = ProgressDialog.show(activity, "获取在场车辆数据", "获取中...", true, true);
//        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
//
//            @Override
//            public void callback(String url, String object, AjaxStatus status) {
//                dialog.dismiss();
//
//                if (!TextUtils.isEmpty(object)) {
//                    MyLog.i("InTheVehicleActivity", "获取在场车辆结果--->" + object);
//                    Gson gson = new Gson();
//                    Data = gson.fromJson(object, new TypeToken<BoWeiListEntity>() {
//                    }.getType());
//                    SharedPreferencesUtils.getIntance(activity).setworkid(Data.getWorkid());
//                    String msg = Data.getErrmsg();
//                    if (CommontUtils.checkString(Data.getState())) {
////                        RegisDevice();
//                        if (Integer.parseInt(Data.getState()) != 1) {
//                            //非正常状态
//                            if (CommontUtils.checkString(Data.getErrmsg())) {
//                                new AlertDialog.Builder(activity).setTitle("提示").setIcon(R.drawable.app_icon_32)
//                                        .setMessage(msg).setNegativeButton("确定", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        if (Data.getState().equals("0")) {
//                                            //调用注册设备接口
//                                            RegisDevice();
//                                        } else {
//                                            finishs();
//                                        }
//
//                                    }
//                                }).create().show();
//                            }
//                        } else {
//                            if (!getBooleanFromPreference("alreadyalert")) {
//                                //已签到，显示签到时间
//                                if (CommontUtils.checkString(Data.getErrmsg())) {
//                                    new AlertDialog.Builder(activity).setTitle("提示").setIcon(R.drawable.app_icon_32)
//                                            .setMessage(msg).setNegativeButton("确定", null).create().show();
//                                }
//                                putBooleanToPreference("alreadyalert", true);
//                            }
//                            if(CommontUtils.checkString(Data.getComid())){
//                                SharedPreferencesUtils.getIntance(activity).setComid(Data.getComid());
//                            }
//                            if(CommontUtils.checkString(Data.getCname())){
//                                SharedPreferencesUtils.getIntance(activity).setParkname(Data.getCname());
//                            }
////                            System.out.println(Data.getComid()+Data.getCname());
//                            infos = Data.getData();
//                            if (CommontUtils.checkList(infos)) {
//                                for (int i = 0; i < infos.size(); i++) {
//                                    String state = getStringFromPreference(infos.get(i).getId());
////                                    System.out.println("state="+state+" and i="+infos.get(i).getId());
//                                    if(CommontUtils.checkString(state)){
//                                        if (!CommontUtils.checkString(infos.get(i).getSensor_state())){
//                                            infos.get(i).setSensor_state(state);
//                                        }
//
//                                    }
//                                }
//                                Constant.boweiMsgAll.clear();
//                                Constant.boweiMsgAll.addAll(infos);
//                            }
//                            INFO.addAll(infos);
//                            MyLog.i("InTheVehicleActivity", "解析在场车辆结果--->" + infos.toString());
//                            if (infos != null) {
//                                adapter.notifyDataSetChanged();
//                            }
//                        }
//                    } else {
//                        Toast.makeText(activity, "获取数据出错！", Toast.LENGTH_LONG).show();
//                    }
//
//
//                }
//            }
//        });
//    }
//
//    /**
//     * /zld/collectorrequest.do?action=regpossequence&token=a3a0dafbe61d9b491b6094b6f64a0693&device_code=
//     * 注册设备接口
//     */
//    public void RegisDevice() {
//        if (!IsNetWork.IsHaveInternet(activity)) {
//            Toast.makeText(activity, "请检查网络！", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        AQuery aQuery = new AQuery(activity);
//        String path = BaseActivity.baseurl;
//        String url = path + "collectorrequest.do?action=regpossequence&token=" +
//                BaseActivity.token + "&device_code=" + CommontUtils.GetHardWareAddress(activity);
//        MyLog.w("注册的URl-->>", url);
//        final ProgressDialog dialog = ProgressDialog.show(activity, "加载中...", "注册设备...", true, true);
//        aQuery.ajax(url, String.class, new AjaxCallback<String>() {
//
//            public void callback(String url, String object, AjaxStatus status) {
//                MyLog.i("CurrentOrderDetailsActivity", "注册设备-->>" + object);
//                if (object != null) {
//                    dialog.dismiss();
//                    Gson gson = new Gson();
//                    BaseRegisBean bean = gson.fromJson(object, new TypeToken<BaseRegisBean>() {
//                    }.getType());
//
//                    new AlertDialog.Builder(activity).setTitle("提示").setIcon(R.drawable.app_icon_32)
//                            .setMessage(bean.getErrmst()).setNegativeButton("确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            finishs();
//                        }
//                    }).setCancelable(false).create().show();
//
//                } else {
//                    dialog.dismiss();
//                    return;
//                }
//            }
//        });
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // TODO Auto-generated method stub
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                activity.finish();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
//
//    public Handler hand = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case 101:
//                    getInVehicleInfo();
//                    break;
//            }
//        }
//    };
//    @Override
//    public void onResume() {
//        super.onResume();
////        Toast.makeText(getActivity(),"onresume",Toast.LENGTH_SHORT).show();
//        //每次都读一下 存好的泊位所在的状态，每次处理完订单，从消息list中移除车位号
//        if (CommontUtils.checkString(getStringFromPreference("boweistate"))) {
//            String changedbowei = getStringFromPreference("boweistate");
////            System.out.println("-------" + getStringFromPreference("boweistate"));
////            putStringToPreference(changedbowei,"-10");
//            putStringToPreference(changedbowei,"");
//            //移除一个泊位以后置空，避免下次误移除
//            putStringToPreference("boweistate","");
//            //结算成功后将车检器订单置 空
//            if(getStringFromPreference("berthorderid").equals("succeed")){
//                putStringToPreference(changedbowei+"222","");
//            }
//        }
//        getInVehicleInfo();
//
//    }
//
//    @Override
//    public void doSelectParkPosition(String carmunber, String orderid) {
////        System.out.print("--------------添加完车位的回调-------------");
//    }
//
//    //刷新订单数据
//    public void refresh() {
//        getInVehicleInfo();
//    }
//
//    /**
//     * 把boolean类型的变量存进xml
//     *
//     * @param s        键
//     * @param boolean1 值
//     */
//    public void putBooleanToPreference(String s, Boolean boolean1) {
//        android.content.SharedPreferences.Editor editor = sharedPreferences
//                .edit();
//        boolean flag = boolean1.booleanValue();
//        editor.putBoolean(s, flag).commit();
//    }
//
//    /**
//     * 把string类型的变量存进xml
//     *
//     * @param s  键
//     * @param s1 值
//     */
//    public void putStringToPreference(String s, String s1) {
//        sharedPreferences.edit().putString(s, s1).commit();
//    }
//
//    public void putIntToPreference(String s, int s1) {
//        sharedPreferences.edit().putInt(s, s1).commit();
//    }
//
//    /**
//     * 获取xml中键值是s对应的值(字符串)
//     *
//     * @param s 键
//     * @return value
//     */
//    public String getStringFromPreference(String s) {
//        return sharedPreferences.getString(s, "");
//    }
//
//    /**
//     * 获取xml中键值是s对应的值(字符串)
//     *
//     * @param s  键
//     * @param s1 默认值
//     * @return value
//     */
//    public String getStringFromPreference(String s, String s1) {
//        return sharedPreferences.getString(s, s1);
//    }
//
//    /**
//     * 获取xml中键值是s对应的值(boolean型值)
//     *
//     * @param s 键
//     * @return
//     */
//    public boolean getBooleanFromPreference(String s) {
//
//        return sharedPreferences.getBoolean(s, false);
//    }
//
//    /**
//     * 获取xml中键值是s对应的值(boolean型值)
//     *
//     * @param s    键
//     * @param flag 默认值
//     * @return value
//     */
//    public boolean getBooleanFromPreference(String s, boolean flag) {
//        return sharedPreferences.getBoolean(s, flag);
//    }
//
//    public int getIntFromPreference(String s, int flag) {
//        return sharedPreferences.getInt(s, flag);
//    }
//
//    /**
//     * @param s    键
//     * @param flag 默认值
//     * @return
//     */
//    public long getLongFromPreference(String s, long flag) {
//        return sharedPreferences.getLong(s, flag);
//    }
//
//    public void putLongToPreference(String s, long s1) {
//        sharedPreferences.edit().putLong(s, s1).commit();
//    }
//
//    private void finishs() {
//        Intent bleService = new Intent(activity, BLEService.class);
//        Intent pullService = new Intent(activity, PullMsgService.class);
//        activity.stopService(bleService);
//        activity.stopService(pullService);
//        Intent intent = new Intent(activity, LoginActivity.class);
//        startActivity(intent);
//        putBooleanToPreference("already", true);
//        activity.finish();
//    }
//
//}
