package com.zhenlaidian.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.lswss.QRCodeEncoder;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.InTheVehicleAdapter;
import com.zhenlaidian.bean.BoWeiListEntity;
import com.zhenlaidian.bean.InCarDialogInfo;
import com.zhenlaidian.bean.InVehicleInfo;
import com.zhenlaidian.camera.CameraActivity;
import com.zhenlaidian.photo.InCarDialogActivity;
import com.zhenlaidian.printer.TcbCheckCarIn;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.util.CameraBitmapUtil;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.Constant;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
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
public class InTheVehicleStepActivity extends BaseActivity implements View.OnClickListener {

    public ActionBar actionBar;
    public InTheVehicleAdapter adapter;
    public GridView gv_in_vehicle;
    public TextView tv_parking_state;// 停车状态
    private String ismonthuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        setContentView(R.layout.x_in_the_vehicle);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        ismonthuser = getIntent().getStringExtra("ismonthuser");
        initView();
        initHeadFoot();
    }

    private EditText edtSearch;
    private LinearLayout lnDelete;
    private TextView txtIn, txtSwitch, txtOut;
    private TextView txtCancel, txtNext;

    private void initHeadFoot() {
        edtSearch = ((EditText) findViewById(R.id.tv_pay_log_number));
        lnDelete = ((LinearLayout) findViewById(R.id.ll_pay_log_delete));
//        txtIn = ((TextView) findViewById(R.id.tv_main_income_and_scan));
//        txtSwitch = ((TextView) findViewById(R.id.tv_main_change_income_satate));
//        txtOut = ((TextView) findViewById(R.id.tv_main_leave_and_leave_order));
//        txtOut.setOnClickListener(this);
//        txtSwitch.setOnClickListener(this);
//        txtIn.setOnClickListener(this);
        lnDelete.setOnClickListener(this);
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
                searchinfo = new ArrayList<InVehicleInfo>();
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
                adapter.notifyDataSetChanged();
            }
        });
        findViewById(R.id.bt_input_carnumber_cancel).setOnClickListener(this);
        btnNext = ((Button) findViewById(R.id.bt_input_carnumber_ok_dialog));
        btnNext.setOnClickListener(this);
        INFO = new ArrayList<InVehicleInfo>();
        adapter = new InTheVehicleAdapter(context, INFO);
        gv_in_vehicle.setAdapter(adapter);
    }

    private Button btnNext;
    public ArrayList<InVehicleInfo> infos, INFO, searchinfo;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_pay_log_delete:
                //删除按钮
                String tmp = edtSearch.getText().toString();
//				if(){
//					String newtemp = tmp.substring(0,tmp.length()-1);
//					edtSearch.setText(newtemp);
//					edtSearch.setSelection(newtemp.length());
//				}

                break;
            case R.id.tv_main_income_and_scan:
                //车辆入场
                Intent carintent = new Intent(context, InCarDialogActivity.class);
//				carintent.putExtra("positon",position);
//				carintent.putExtra("id",infos.get(position).getCid());
                startActivity(carintent);
//                InCarDialogActivity.setParkPositionListener(context);
                break;
            case R.id.tv_main_change_income_satate:
                //中间切换

                break;
            case R.id.tv_main_leave_and_leave_order:
                //右扫码支付

                break;
            case R.id.bt_input_carnumber_cancel:
                //左取消
                finish();
                break;
            case R.id.bt_input_carnumber_ok_dialog:
                //右下一步
                //调用追缴接口
                break;
        }
    }

    public void initView() {
        gv_in_vehicle = (GridView) findViewById(R.id.gv_in_vehicle);
        tv_parking_state = (TextView) findViewById(R.id.tv_in_vehicle_parking_state);
        gv_in_vehicle.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (TextUtils.isEmpty(INFO.get(position).getOrderid())) {
                    putStringToPreference("bowei", INFO.get(position).getId());
                    putStringToPreference("boweiversion", INFO.get(position).getBer_name());
                    if (CommontUtils.checkString(ismonthuser) && ismonthuser.equals("1")) {
                        //会员不需要预付，直接生成订单
                        photonum = SharedPreferencesUtils.getIntance(InTheVehicleStepActivity.this).getphotoset(0);
                        if (photonum > 0) {
                            Intent i = new Intent(InTheVehicleStepActivity.this, CameraActivity.class);
                            i.putExtra("num", photonum);
                            startActivityForResult(i, 100);
                        } else {
                            try {
                                createOrderForPos();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if (SharedPreferencesUtils.getIntance(context).getisprepay().equals("1")) {
                            Intent intent = new Intent(context, PrePayParkingActivity.class);
                            intent.putExtra("cartype", getIntent().getStringExtra("cartype"));
                            startActivity(intent);
                            finish();
                        } else {
                            //不需要预付，直接生成订单
                            photonum = SharedPreferencesUtils.getIntance(InTheVehicleStepActivity.this).getphotoset(0);
                            if (photonum > 0) {
                                Intent i = new Intent(InTheVehicleStepActivity.this, CameraActivity.class);
                                i.putExtra("num", photonum);
                                startActivityForResult(i, 100);
                            } else {
                                try {
                                    createOrderForPos();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                } else {
                    CommontUtils.toast(context, "车位已被占用");
                }

            }
        });
    }

    public BoWeiListEntity Data;

    // 获取在场车辆信息collectorrequest.do?action=comparks&out=josn&token=5f0c0edb1cc891ac9c3fa248a28c14d5
    public void getInVehicleInfo() {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "请检查网络", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = BaseActivity.baseurl + "collectorrequest.do?action=getberths&out=josn&token=" + BaseActivity.token +
                "&berthid=" + SharedPreferencesUtils.getIntance(this).getberthid() +
                "&devicecode=" + CommontUtils.GetHardWareAddress(this);
        MyLog.w("InTheVehicleActivity", "获取在场车辆的URL--->" + url);
        AQuery aQuery = new AQuery(this);
//        final ProgressDialog dialog = ProgressDialog.show(context, "获取在场车辆数据", "获取中...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
//                dialog.dismiss();
                if (!TextUtils.isEmpty(object)) {
                    MyLog.d("InTheVehicleActivity", "获取在场车辆结果--->" + object);
                    Gson gson = new Gson();
                    Data = gson.fromJson(object, new TypeToken<BoWeiListEntity>() {
                    }.getType());
                    infos = Data.getData();
                    INFO.clear();
                    for (int i = 0; i < infos.size(); i++) {
                        if (!CommontUtils.checkString(infos.get(i).getCar_number())) {
                            INFO.add(infos.get(i));
                        }
                    }
//                    INFO.addAll(infos);
                    MyLog.i("InTheVehicleActivity", "解析在场车辆结果--->" + infos.toString());
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        conn2bluetooth();
        getInVehicleInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 100:
                    listPath = data.getStringArrayListExtra("list");
                    try {
                        createOrderForPos();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private ArrayList<String> listPath = new ArrayList<String>();
    private int photonum = 0;
    private VoiceSynthesizerUtil voice;
    //POS机生成订单接口;
    //collectorrequest.do?action=posincome&token=2dd4b1b320225dfd4fc44ad6b53fa734&carnumber=
    private String uid;
    private InCarDialogInfo infoss;
    private long currentM = 0;

    public void createOrderForPos() throws UnsupportedEncodingException {
        if (System.currentTimeMillis() - currentM > 1000) {
            SharedPreferences pfs = this.getSharedPreferences("autologin", Context.MODE_PRIVATE);
            uid = pfs.getString("account", "");
            final String carnumber = URLEncoder.encode(getStringFromPreference("carnumber"), "utf-8");
            String url = BaseActivity.baseurl + "collectorrequest.do?action=posincome&token=" +
                    BaseActivity.token + "&carnumber=" + URLEncoder.encode(carnumber, "utf-8") +
                    "&bid=" + getStringFromPreference("bowei") + "&berthid=" + SharedPreferencesUtils.getIntance(this).getberthid() + "&workid=" +
                    SharedPreferencesUtils.getIntance(this).getworkid() + "&prepay=0&ismonthuser=" + ismonthuser
                    + "&berthorderid=" + getStringFromPreference("berthorderid") + "&orderid=" + getStringFromPreference("preorderid")
                    + "&car_type=" + getIntent().getStringExtra("cartype");
            MyLog.w("InCarDialogActivity", "车牌识别生成订单的URL--->" + url);
            final ProgressDialog dialog = ProgressDialog.show(this, "生成订单", "提交订单数据中...", true, true);
            dialog.setCanceledOnTouchOutside(false);
            AQuery aQuery = new AQuery(this);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (status.getCode() == 200 && object != null) {
                        dialog.dismiss();
                        MyLog.i("InCarDialogActivity", "车牌识别生成订单的结果--->" + object);
                        Gson gson = new Gson();
                        InCarDialogInfo info = gson.fromJson(object, InCarDialogInfo.class);
                        infoss = info;
                        if (info != null) {
                            MyLog.d("InCarDialogActivity", info.toString());
                            if ("1".equals(info.getResult())) {
                                //生成订单成功后将车检器订单置空
                                putStringToPreference("berthorderid", "");
                                putBooleanToPreference("next", false);
                                voice = new VoiceSynthesizerUtil(context);
                                voice.playText("生成订单");
                                btnNext.setOnClickListener(null);
                                if (CommontUtils.checkList(listPath)) {
                                    for (int i = 0; i < listPath.size(); i++) {
                                        String SDState = Environment.getExternalStorageState();
                                        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                                            File dir = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
                                            if (!dir.exists()) {
                                                dir.mkdirs();
                                            }
                                            (new File(listPath.get(i))).renameTo(new File(dir.getAbsolutePath(), info.getOrderid() + "in" + i + ".jpeg"));
                                            CameraBitmapUtil.upload(context, i, info.getOrderid(), 0);
                                        }
                                    }
                                }
                                if (PullMsgService.CanPrint) {
                                    prient(uid, info);
                                } else {
                                    FinishAction();
                                }
                            } else {
                                Toast.makeText(context, info.getErrmsg(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        dialog.dismiss();
                        switch (status.getCode()) {
                            case -101:
                                Toast.makeText(context, "网络错误！--请再次确认车牌生成订单！", Toast.LENGTH_SHORT).show();
                                break;
                            case 500:
                                Toast.makeText(context, "服务器错误！--请再次确认车牌生成订单！", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }
            });
            currentM = System.currentTimeMillis();
        }
    }


    //打印凭条
    public void prient(String uid, InCarDialogInfo info) {
        //将已处理的车位号存入，回到泊位列表时移除这个车位号消息 泊位id

        TcbCheckCarIn incar = new TcbCheckCarIn();
        incar.setOrderid(info.getOrderid());
//        incar.setCarnumber(tv_add_carnumber.getText().toString());
        incar.setTime(info.getBtime());
        String Sname = "";
        String gang = "";
        if (SharedPreferencesUtils.getIntance(this).getisprintName()) {
            Sname = getStringFromPreference("name");
            gang = "-";
        }
        incar.setMeterman(Sname);
        Bitmap qrbitmap = new QRCodeEncoder().encode2BitMap(BaseActivity.baseurl + info.getQrcode(), 240, 240);
        Bitmap imgbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_check);
//        PrinterUitls.getInstance().printerTCBCheckCarIn(incar, qrbitmap, imgbitmap);
        String str = Constant.HEADIN +
                SharedPreferencesUtils.getIntance(context).getprint_signInHead() + "\n" +
                "停车场：" + SharedPreferencesUtils.getIntance(context).getParkname() +
                "—" + SharedPreferencesUtils.getIntance(context).getberthsec_name() + "\n" +
                "收费员：" + incar.getMeterman();
        if (CommontUtils.Is910()) {
            if (!TextUtils.isEmpty(SharedPreferencesUtils.getIntance(context).getmobile())) {
                str += "(" + SharedPreferencesUtils.getIntance(context).getmobile() + ")";
            }
        }
        str += gang + uid;

        str += "\n" + "车位：" + getStringFromPreference("boweiversion") + "\n" +
                "车牌号：" + getStringFromPreference("carnumber") + "\n";
        if (ismonthuser.equals("1")) {
            str += "停车类型：月卡用户\n" + "进场时间：" + incar.getTime() + "\n" + "\n";
        } else {
            str += "停车类型：临时停车\n" + "进场时间：" + incar.getTime() + "\n" +
                    "预收金额：0 元\n" +
                    "支付方式：现金\n\n";
        }
        str += Constant.FOOT +
                SharedPreferencesUtils.getIntance(context).getprint_signIn() + "\n\n\n\n\n";
        PullMsgService.sendMessage(str, context);
//        sendMessage(qrbitmap);

        FinishAction();
    }

    private void FinishAction() {
        putStringToPreference("boweistate", getStringFromPreference("bowei"));
        finish();
    }


}
