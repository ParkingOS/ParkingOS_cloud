package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.ParkPriceInfo;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

import java.util.HashMap;
import java.util.Map;

/**
 * 设置价格的界面;
 */
public class PriceSettingActivity extends BaseActivity {

    private ActionBar actionBar;
    private static final String[] day = {"(07:00-21:00)", "(08:00-18:00)", "(08:00-20:00)", "(10:00-21:00)"};
    private static final String[] dayPrice = {"元/15分钟", "元/30分钟", "元/1小时", "元/2小时"};
    private static final String[] nightPrice = {"元/15分钟", "元/30分钟", "元/1小时", "元/2小时", "元/3小时"};
    private static final String[] firstTime = {"15分内", "30分内", "1小时内", "2小时内", " 3小时内"};
    private static final String[] NfirstTime = {"30分内", "1小时内", "2小时内", "3小时内"};
    private ArrayAdapter<String> dayAdapter;
    private ArrayAdapter<String> dayPriceAdapter;
    private ArrayAdapter<String> nightPriceAdapter;
    private ArrayAdapter<String> firstTimeAdapter;
    private ArrayAdapter<String> NfirstTimeAdapter;
    private Spinner sp_time_day;//日间时段
    private Spinner sp_day_price;//日间价格单位
    private Spinner sp_night_price;//夜间价格单位
    private Spinner sp_day_fristTime;//日间首时段
    private Spinner sp_night_fristTime;//夜间首时段
    private TextView tv_night;//夜间时段
    private TextView tv_out_time;//日间首小时外
    private TextView tv_night_out_time;//夜间首小时外
    private ImageButton iv_night_null;//不支持夜晚停车
    private Button bt_ok;
    private EditText et_day_fprice;//日间首优惠价格
    private EditText et_day_price;//日间价格
    private EditText et_night_fprice;//----夜间首优惠价格
    private EditText et_night_price;//夜间价格
    private EditText et_day_temporary_stop_time; //日间临停免费时长
    private EditText et_night_temporary_stop_time; //夜间临停免费时长
    LinearLayout ll_no_night_parking;//不支持晚上停车
    private CheckBox cb_is_night_stop;//夜间是否支持停车；
    private CheckBox cb_is_day_ftime;  //超过日间临停免费时间是否收完整费用；
    private CheckBox cb_is_night_ftime;//超过夜间临停免费时间是否收完整费用；

    public String comid;   //公司编号
    public String unit = "15";        //日间价格单位
    public String nunit = "120";    //夜间价格单位
    public String price = "2.25";    //日间价格
    public String nprice = "1";    //夜间价格
    public String pay_type = "0";//计费类型。0，按时段，1按次数；
    public String b_time = "7";    //日间开始时间
    public String e_time = "21";    //日间结束时间
    public String first_times = "30";    //日间首优惠时长
    public String fprice = "1.5"; //日间首优惠价格
    public String countless;    //可免费时长
    public String nfirst_times = "60";    //夜间首优惠时长
    public String nfprice = "1";        //夜间首优惠价格
    public String is_day_ftime = "0";        //日间超免费时长是否收完整费用
    public String is_night_ftime = "0";    //夜间超免费时长是否收完整费用；
    public String is_night_stop = "0"; //夜间是否支持停车；
    public String day_temporary_stop = "0";    //日间临停免费时长；
    public String night_temporary_stop = "0";    //夜间临停免费时长；
//	 free_time integer DEFAULT 0, -- 免费时长，单位:分钟 
//	  fpay_type integer DEFAULT 0, -- 超免费时长计费方式，1:免费 ，0:收费
//	  isnight integer DEFAULT 0, -- 夜晚停车，0:支持，1不支持

    public ParkPriceInfo priceinfo;
    private boolean addprice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        setContentView(R.layout.price_settitng_two_activity);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        initView();
        sp_time_day.setAdapter(dayAdapter);
        sp_day_price.setAdapter(dayPriceAdapter);
        sp_night_price.setAdapter(nightPriceAdapter);
        sp_day_fristTime.setAdapter(firstTimeAdapter);
        sp_night_fristTime.setAdapter(NfirstTimeAdapter);

        sp_time_day.setOnItemSelectedListener(new DayListener());
        sp_day_price.setOnItemSelectedListener(new DayPriceListener());
        sp_night_price.setOnItemSelectedListener(new NightPriceListener());
        sp_day_fristTime.setOnItemSelectedListener(new FirstTimeListener());
        sp_night_fristTime.setOnItemSelectedListener(new NFirstTimeListener());
        setView();

    }

    public void initView() {
        comid = getIntent().getStringExtra("comid");
        MyLog.w("TakePhotoActivity", "comid = " + comid);
        sp_time_day = (Spinner) findViewById(R.id.sp_price_setting_day);
        sp_day_price = (Spinner) findViewById(R.id.sp_price_setting_day_price);
        sp_night_price = (Spinner) findViewById(R.id.sp_price_setting_night_price);
        sp_day_fristTime = (Spinner) findViewById(R.id.sp_price_setting_first_time);
        sp_night_fristTime = (Spinner) findViewById(R.id.sp_price_setting_Nfirst_time);
        dayAdapter = new ArrayAdapter<String>(this, R.layout.my_spinner_item, day);
        dayPriceAdapter = new ArrayAdapter<String>(this, R.layout.my_spinner_item, dayPrice);
        nightPriceAdapter = new ArrayAdapter<String>(this, R.layout.my_spinner_item, nightPrice);
        firstTimeAdapter = new ArrayAdapter<String>(this, R.layout.my_spinner_item, firstTime);
        NfirstTimeAdapter = new ArrayAdapter<String>(this, R.layout.my_spinner_item, NfirstTime);
        tv_night = (TextView) findViewById(R.id.tv_price_setting_night);
        tv_out_time = (TextView) findViewById(R.id.tv_price_setting_day_out_time);
        tv_night_out_time = (TextView) findViewById(R.id.tv_price_setting_Nout_time);
        iv_night_null = (ImageButton) findViewById(R.id.iv_price_setting_night_null);
        et_day_fprice = (EditText) findViewById(R.id.et_price_setting_day_inTime);
        et_day_price = (EditText) findViewById(R.id.et_price_setting_day_outTime);
        et_night_fprice = (EditText) findViewById(R.id.et_price_setting_night_inTime);
        et_night_price = (EditText) findViewById(R.id.et_price_setting_night_outTime);
        et_day_temporary_stop_time = (EditText) findViewById(R.id.et_price_setting_freeTime);
        et_night_temporary_stop_time = (EditText) findViewById(R.id.et_price_setting_nitgt_freeTime);
        cb_is_night_stop = (CheckBox) findViewById(R.id.cb_is_night_parking);
        cb_is_day_ftime = (CheckBox) findViewById(R.id.cb_price_setting_day_ftime);
        cb_is_night_ftime = (CheckBox) findViewById(R.id.cb_price_setting_night_ftime);
        ll_no_night_parking = (LinearLayout) findViewById(R.id.ll_price_setting_night_info);
        bt_ok = (Button) findViewById(R.id.bt_price_setting_ok);
        String forPakringInfo = getIntent().getStringExtra("change");
        String changeNull = getIntent().getStringExtra("changeNull");
        if (forPakringInfo != null) {
            if (forPakringInfo.equals("change")) {
                priceinfo = (ParkPriceInfo) getIntent().getSerializableExtra("priceInfo");
                MyLog.i("ChangeParkingInfo", "getIntent()获取的价格信息--->" + priceinfo.toString());
                bt_ok.setText("提交");
                bt_ok.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        update();
                    }
                });
            }
        } else if (changeNull != null) {
            bt_ok.setText("提交");
            addprice = true;
            bt_ok.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    submit();
                }
            });
        } else {
            bt_ok.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    submit();

                }
            });
        }

    }

    public void setView() {
        if (priceinfo != null && priceinfo.getB_time() != null) {
            if (priceinfo.getFprice() != null) {
                et_day_fprice.setText(priceinfo.getFprice());
            }
            if (priceinfo.getPrice() != null) {
                et_day_price.setText(priceinfo.getPrice());
            }
            if (priceinfo.getNprice() != null) {
                et_night_price.setText(priceinfo.getNprice());
            }
            if (priceinfo.getCountless() != null) {
                et_day_temporary_stop_time.setText(priceinfo.getCountless());
            }
            if (priceinfo.getNfprice() != null) {
                et_night_fprice.setText(priceinfo.getNfprice());
            }
            if (priceinfo.getFree_time() != null) {
                et_day_temporary_stop_time.setText(priceinfo.getFree_time());
            }
            if (priceinfo.getNfree_time() != null) {
                et_night_temporary_stop_time.setText(priceinfo.getNfree_time());
            }
            if (priceinfo.getFpay_type() != null) {
                if (priceinfo.getFpay_type().equals("0")) {//1-->免费；0-->收费
                    cb_is_day_ftime.setChecked(true);
                } else if (priceinfo.getFpay_type().equals("1")) {
                    cb_is_day_ftime.setChecked(false);
                }
            }
            if (priceinfo.getNfpay_type() != null) {
                if (priceinfo.getNfpay_type().equals("0")) {//1-->免费；0-->收费
                    cb_is_night_ftime.setChecked(true);
                } else if (priceinfo.getNfpay_type().equals("1")) {
                    cb_is_night_ftime.setChecked(false);
                }
            }
            if (priceinfo.getIsnight() != null) {
                if (priceinfo.getIsnight().equals("0")) {//0:支持，1不支持
                    cb_is_night_stop.setChecked(true);
                } else if (priceinfo.getIsnight().equals("1")) {
                    cb_is_night_stop.setChecked(false);
                    iv_night_null.setVisibility(View.VISIBLE);
                    is_night_stop = "1";
                }
            }

            if (priceinfo.getB_time() != null) {
                switch (Integer.parseInt(priceinfo.getB_time())) {
                    case 7:
                        sp_time_day.setSelection(0);
                        tv_night.setText("(21:00-07:00)");
                        break;
                    case 8:
                        if (priceinfo.getE_time().endsWith("18")) {
                            sp_time_day.setSelection(1);
                            tv_night.setText("(18:00-08:00)");
                        } else {
                            sp_time_day.setSelection(2);
                            tv_night.setText("(20:00-08:00)");
                        }
                        break;
                    case 10:
                        sp_time_day.setSelection(3);
                        tv_night.setText("(21:00-10:00)");
                        break;
                }
            }
            if (priceinfo.getUnit() != null) {
                switch (Integer.parseInt(priceinfo.getUnit())) {
                    case 15:
                        sp_day_price.setSelection(0);
                        break;
                    case 30:
                        sp_day_price.setSelection(1);
                        break;
                    case 60:
                        sp_day_price.setSelection(2);
                        break;
                    case 120:
                        sp_day_price.setSelection(3);
                        break;
                }
            }
            if (priceinfo.getNuint() != null) {
                switch (Integer.parseInt(priceinfo.getNuint())) {
                    case 15:
                        sp_night_price.setSelection(0);
                        break;
                    case 30:
                        sp_night_price.setSelection(1);
                        break;
                    case 60:
                        sp_night_price.setSelection(2);
                        break;
                    case 120:
                        sp_night_price.setSelection(3);
                        break;
                    case 180:
                        sp_night_price.setSelection(4);
                        break;
                }
            }

            if (priceinfo.getFirst_times() != null) {
                switch (Integer.parseInt(priceinfo.getFirst_times())) {
                    case 15:
                        sp_day_fristTime.setSelection(0);
                        break;
                    case 30:
                        sp_day_fristTime.setSelection(1);
                        break;
                    case 60:
                        sp_day_fristTime.setSelection(2);
                        break;
                    case 120:
                        sp_day_fristTime.setSelection(3);
                        break;
                    case 180:
                        sp_day_fristTime.setSelection(4);
                        break;
                }
            }
            //30分内","1小时内","2小时内", "3小时内
            if (priceinfo.getNfirst_times() != null) {
                switch (Integer.parseInt(priceinfo.getNfirst_times())) {
                    case 30:
                        sp_night_fristTime.setSelection(0);
                        break;
                    case 60:
                        sp_night_fristTime.setSelection(1);
                        break;
                    case 120:
                        sp_night_fristTime.setSelection(2);
                        break;
                    case 180:
                        sp_night_fristTime.setSelection(3);
                        break;
                }
            }
        } else {
            et_day_fprice.setText(fprice);
            et_day_price.setText(price);
            et_night_price.setText(nprice);
            et_night_fprice.setText(nfprice);
            et_day_temporary_stop_time.setText(day_temporary_stop);
            et_night_temporary_stop_time.setText(night_temporary_stop);
            cb_is_night_stop.setChecked(true);
            cb_is_day_ftime.setChecked(true);
            cb_is_night_ftime.setChecked(true);
            sp_time_day.setSelection(0);
            sp_day_fristTime.setSelection(2);
            sp_day_price.setSelection(0);
            sp_night_fristTime.setSelection(2);
            sp_night_price.setSelection(1);
        }
        //是否支持夜间停车
        cb_is_night_stop.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @SuppressLint("ResourceAsColor")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    iv_night_null.setVisibility(View.INVISIBLE);//0:支持，1不支持
                    is_night_stop = "0";
                } else {
                    iv_night_null.setVisibility(View.VISIBLE);
                    is_night_stop = "1";
                }
            }
        });
        //日间超过临停免费时长是否收完整费用；
        cb_is_day_ftime.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    is_day_ftime = "0";//收费
                } else {
                    is_day_ftime = "1";//不收费
                }
            }
        });
        //日间超过临停免费时长是否收完整费用；
        cb_is_night_ftime.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    is_night_ftime = "0";//收费
                } else {
                    is_night_ftime = "1";//不收费
                }
            }
        });
    }

    //日间时段选择事件监听；
    class DayListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    tv_night.setText("(21:00-07:00)");
                    b_time = "7";
                    e_time = "21";
                    break;
                case 1:
                    tv_night.setText("(18:00-08:00)");
                    b_time = "8";
                    e_time = "18";
                    break;
                case 2:
                    tv_night.setText("(20:00-08:00)");
                    b_time = "8";
                    e_time = "20";
                    break;
                case 3:
                    tv_night.setText("(21:00-10:00)");
                    b_time = "10";
                    e_time = "21";
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }

    }

    //日间价格选择事件监听；
    class DayPriceListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    unit = "15";
                    break;
                case 1:
                    unit = "30";
                    break;
                case 2:
                    unit = "60";
                    break;
                case 3:
                    unit = "120";
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }

    }

    //夜间价格选择事件监听；
    class NightPriceListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    nunit = "15";
                    break;
                case 1:
                    nunit = "30";
                    break;
                case 2:
                    nunit = "60";
                    break;
                case 3:
                    nunit = "120";
                    break;
                case 4:
                    nunit = "180";
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    //日间首时段优惠择事件监听；
//	 "15分内","30分内","首小时内","2小时内","3小时内" };
    class FirstTimeListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            switch (position) {
                case 0:
                    first_times = "15";
                    tv_out_time.setText("15分外");
                    break;
                case 1:
                    first_times = "30";
                    tv_out_time.setText("30分外");
                    break;
                case 2:
                    first_times = "60";
                    tv_out_time.setText("1小时外");
                    break;
                case 3:
                    first_times = "120";
                    tv_out_time.setText("2小时外");
                    break;
                case 4:
                    first_times = "180";
                    tv_out_time.setText("3小时外");
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    //夜间首时段优惠择事件监听；
//	NfirstTime = { "30分内","1小时内","2小时内", "3小时内" };
    class NFirstTimeListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            switch (position) {
                case 0:
                    nfirst_times = "30";
                    tv_night_out_time.setText("30分外");
                    break;
                case 1:
                    nfirst_times = "60";
                    tv_night_out_time.setText("1小时外");
                    break;
                case 2:
                    nfirst_times = "120";
                    tv_night_out_time.setText("2小时外");
                    break;
                case 3:
                    nfirst_times = "180";
                    tv_night_out_time.setText("3小时外");
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

//	public String comid;   //公司编号
//	public String price; 	//日间价格
//	public String nprice;	//夜间价格
//	public String unit;	//日间价格单位
//	public String nunit;	//夜间价格单位
//	public String pay_type;//计费类型。0，按时段，1按次数；
//	public String b_time;	//日间开始时间
//	public String e_time;	//日间结束时间
//	public String first_times;	//日间首优惠时长
//			//public String nfirst_times;	//夜间首优惠时长
//	public String fprice;		//日间首优惠价格
//			//public String nfprice;		//夜间首优惠价格
//	public String countless;	//可免费时长
// free_time integer DEFAULT 0, -- 免费时长，单位:分钟
// fpay_type integer DEFAULT 0, -- 超免费时长计费方式，1:免费 ，0:收费
//	isnight integer DEFAULT 0, -- 夜晚停车，0:支持，1不支持


    // http:192.168.1.148/zld/parkedit.do?action=addprice
    // retrun -1,车场编号不合法，1添加成功，-2添加失败
    public void submit() {
        String path = baseurl;
        String url = path + "parkedit.do?action=addprice";
        Map<String, String> params = new HashMap<String, String>();
        try {
            params.put("comid", comid);
            params.put("price", et_day_price.getText().toString().trim());
            params.put("nprice", et_night_price.getText().toString().trim());
            params.put("unit", unit);
            params.put("nunit", nunit);
            params.put("pay_type", pay_type);
            params.put("b_time", b_time);
            params.put("e_time", e_time);
            params.put("fprice", et_day_fprice.getText().toString().trim());
            params.put("nfprice", et_night_fprice.getText().toString().trim());
            params.put("first_times", first_times);
            params.put("nfirst_times", nfirst_times);
            params.put("free_time", et_day_temporary_stop_time.getText().toString().trim());
            params.put("nfree_time", et_night_temporary_stop_time.getText().toString().trim());
            params.put("fpay_type", is_day_ftime);
            params.put("nfpay_type", is_night_ftime);
            params.put("isnight", is_night_stop);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MyLog.w("PriceSettingActivity", "修改定价标准的URL--->" + url + params.toString());
        AQuery aQuery = new AQuery(this);
        if (IsNetWork.IsHaveInternet(PriceSettingActivity.this)) {
            final ProgressDialog dialog = ProgressDialog.show(this, "提交中...", "提交修改信息...");
            aQuery.ajax(url, params, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (object != null && object != "") {
                        MyLog.i("PriceSettingActivity", "返回的信息是--->" + object);
                        dialog.dismiss();
                        if (object.equals("1")) {
                            if (addprice) {
                                finish();
                            } else {
                                Intent intent = new Intent(PriceSettingActivity.this, TakePhotoActivity.class);
                                intent.putExtra("comid", comid);
                                startActivity(intent);
                                finish();
                            }
                        } else if (object.equals("-1")) {
                            Toast.makeText(PriceSettingActivity.this, "车场编号不合法！", 0).show();
                        } else if (object.equals("-2")) {
                            Toast.makeText(PriceSettingActivity.this, "注册失败！", 0).show();
                        }
                    } else {
                        dialog.dismiss();
                    }
                }

            });
        } else {
            Toast.makeText(PriceSettingActivity.this, "修改失败，网络不给力！", 0).show();
        }
    }

    //http:192.168.1.148/zld/parkedit.do?action=editprice&comid=&id=&nid=  修改价格；
    public void update() {
        String path = baseurl;
        String url = path + "parkedit.do?action=editprice&comid=" + comid + "&id=" + priceinfo.getId() + "&nid=" + priceinfo.getNid();
        Map<String, String> params = new HashMap<String, String>();
        try {
            params.put("comid", comid);
            params.put("price", et_day_price.getText().toString().trim());
            params.put("nprice", et_night_price.getText().toString().trim());
            params.put("unit", unit);
            params.put("nunit", nunit);
            params.put("pay_type", pay_type);
            params.put("b_time", b_time);
            params.put("e_time", e_time);
            params.put("fprice", et_day_fprice.getText().toString().trim());
            params.put("nfprice", et_night_fprice.getText().toString().trim());
            params.put("first_times", first_times);
            params.put("nfirst_times", nfirst_times);
            params.put("free_time", et_day_temporary_stop_time.getText().toString().trim());
            params.put("nfree_time", et_night_temporary_stop_time.getText().toString().trim());
            params.put("fpay_type", is_day_ftime);
            params.put("nfpay_type", is_night_ftime);
            params.put("isnight", is_night_stop);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MyLog.w("PriceSettingActivity", "修改定价标准的URL--->" + url + params.toString());
        AQuery aQuery = new AQuery(this);
        if (IsNetWork.IsHaveInternet(PriceSettingActivity.this)) {
            final ProgressDialog dialog = ProgressDialog.show(this, "提交中...", "提交修改信息...");
            aQuery.ajax(url, params, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    if (object != null && object != "") {
                        MyLog.i("PriceSettingActivity", "返回的信息是--->" + object);
                        dialog.dismiss();
                        if (object.equals("1")) {
//								Toast.makeText(FeeScaleActivity.this, "修改成功！", 0).show();
                            Intent intent = new Intent(PriceSettingActivity.this, ShowPriceSettingActivity.class);
                            intent.putExtra("comid", comid);
                            startActivity(intent);
                            PriceSettingActivity.this.finish();
                        } else if (object.equals("-1")) {
                            Toast.makeText(PriceSettingActivity.this, "车场编号不合法！", 0).show();
                        } else if (object.equals("-2")) {
                            Toast.makeText(PriceSettingActivity.this, "修改失败！", 0).show();
                        }
                    } else {
                        dialog.dismiss();
                    }
                }

            });
        } else {
            Toast.makeText(PriceSettingActivity.this, "修改失败，网络不给力！", 0).show();
        }

    }

    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                PriceSettingActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
