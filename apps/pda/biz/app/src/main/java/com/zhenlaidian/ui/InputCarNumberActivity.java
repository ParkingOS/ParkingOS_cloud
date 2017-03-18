package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.lswss.QRCodeEncoder;
import com.wintone.plateid.AuthService;
import com.wintone.plateid.PlateAuthParameter;
import com.wintone.plateid.RecogService;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.CartypeAdapter;
import com.zhenlaidian.adapter.ChoseCarNumPageAdapter;
import com.zhenlaidian.adapter.InputCarNumberGridAdapter;
import com.zhenlaidian.bean.CarTypeItem;
import com.zhenlaidian.bean.InCarDialogInfo;
import com.zhenlaidian.bean.ZhuiJiaoItemEntity;
import com.zhenlaidian.bean.ZhuiJiaoListEntity;
import com.zhenlaidian.camera.CameraActivity;
import com.zhenlaidian.plate_wentong.Devcode;
import com.zhenlaidian.plate_wentong.MemoryCameraActivity;
import com.zhenlaidian.printer.TcbCheckCarIn;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.util.CameraBitmapUtil;
import com.zhenlaidian.util.CheckUtils;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.Constant;
import com.zhenlaidian.util.ImageUtils;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;
import com.zhenlaidian.util.VoiceSynthesizerUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by TCB on 2016/4/16.
 * xulu
 */
public class InputCarNumberActivity extends BaseActivity {
    //    public static InputCarNumberActivity instance = null;
//    private InputCarNumberActivity(){}
//    public static InputCarNumberActivity getInstance(){
//        if(null == instance){
//            instance = new InputCarNumberActivity();
//        }
//        return instance;
//    }
    private ViewPager viewPager;// 页卡内容
    private ImageView imageView;// 动画图片
    private TextView textView1, textView2, textView3;
    private List<View> views;// Tab页面列表
    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度
    private View view1, view2, view3;// 各个页卡
    private EditText et_carnumber;
    private RelativeLayout rl_delete_edtext;
    private Button bt_ok;
    private String carnumber;

    public AuthService.MyBinder authBinder;
    public AuthService.MyBinder recogBinder;
    String cls;
    String pic;
    int imageformat = 1;
    boolean bGetVersion = false;
    String sn;
    String authfile;
    int bVertFlip = 0;

    String userdata;

    int butsetId;

    int authButtonId;

    int recogButtonId;

    int editresultId;

    private int ReturnAuthority = -1;
    String[] fieldvalue = new String[14];
    // 授权验证服务绑定后的操作与start识别服务
    public ServiceConnection authConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            authBinder = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            authBinder = (AuthService.MyBinder) service;
            Toast.makeText(getApplicationContext(), "服务连接onServiceConnected", Toast.LENGTH_LONG).show();
            try {

                PlateAuthParameter pap = new PlateAuthParameter();
                pap.sn = sn;
                pap.authFile = authfile;
                pap.devCode = Devcode.DEVCODE;
                ReturnAuthority = authBinder.getAuth(pap);
                if (ReturnAuthority != 0) {
                    Toast.makeText(getApplicationContext(), "R.string.license_verification_failed:" + ReturnAuthority,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "R.string.license_verification_success", Toast.LENGTH_LONG)
                            .show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "R.string.failed_check_failure", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                if (authBinder != null) {
                    unbindService(authConn);
                }
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        instance = this;
        setContentView(R.layout.x_inputcarnumber_layout);

        actionBar.show();
        InitImageView();
        InitTextView();
        InitViewPager();
        hideTypewriting();
        setView();
        setView1();
        setView2();
        sheView3();

        String strcartype = getStringFromPreference("car_type");
//        String strcartype = "[{\"id\":\"66\",\"name\":\"小型车\"},{\"id\":\"67\",\"name\":\"中型车\"},{\"id\":\"68\",\"name\":\"加长版\"}]";

        cartype = ((TextView) findViewById(R.id.input_cartype));
        if (!TextUtils.isEmpty(strcartype)) {
            Gson gson = new Gson();
            car_type = gson.fromJson(strcartype, new TypeToken<ArrayList<CarTypeItem>>() {
            }.getType());
            cartype.setVisibility(View.VISIBLE);
            cartype.setText(car_type.get(0).getName());
            cartypecode = car_type.get(0).getId();
        } else {
            cartype.setVisibility(View.GONE);
        }
        cartype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示距离，下拉列表
                if (pow != null && pow.isShowing()) {
                    pow.dismiss();
                    return;
                } else {
                    Drawable d = getResources().getDrawable(R.drawable.set_collect_off);
                    d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight()); //设置边界
                    cartype.setCompoundDrawables(null, null, d, null);
                    initPopWindow(car_type);
                    int[] location = new int[2];
                    v.getLocationOnScreen(location);
                    pow.showAsDropDown(cartype);
                }
            }
        });
    }

    private ArrayList<CarTypeItem> car_type;
    private CartypeAdapter cartypeAdapter;
    private TextView cartype;
    private PopupWindow pow;
    private ListView listtype;

    private void initPopWindow(final ArrayList<CarTypeItem> car_type) {

        View cartypeView = getLayoutInflater().inflate(R.layout.cartype_layout, null, false);
        pow = new PopupWindow(cartypeView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        // 点击屏幕其他部分及Back键时PopupWindow消失
        pow.setOutsideTouchable(true);
        pow.setBackgroundDrawable(new BitmapDrawable());
        cartypeView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (pow != null && pow.isShowing()) {
                    pow.dismiss();
                    pow = null;
                }
                return false;
            }
        });
        pow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                Drawable d = getResources().getDrawable(R.drawable.set_collect_on);
                d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight()); //设置边界
                cartype.setCompoundDrawables(null, null, d, null);
            }
        });
        listtype = ((ListView) cartypeView.findViewById(R.id.cartype_lv_popview));

        if (car_type.size() > 9) {
            ViewGroup.LayoutParams params = listtype.getLayoutParams();
            params.height = CommontUtils.getScreenWidth(this);
            listtype.setLayoutParams(params);
        }
        cartypeAdapter = new CartypeAdapter(car_type, context);
        listtype.setAdapter(cartypeAdapter);
        listtype.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cartypecode = car_type.get(position).getId();
                cartype.setText(car_type.get(position).getName());
                pow.dismiss();
            }
        });
    }

    String cartypecode = "0";

    private void InitViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager_dialog_dialog);
        views = new ArrayList<View>();
        LayoutInflater inflater = getLayoutInflater();
        view1 = inflater.inflate(R.layout.x_inputcarnumber_grid_layout, null);
        view2 = inflater.inflate(R.layout.x_inputcarnumber_grid_layout, null);
        view3 = inflater.inflate(R.layout.x_inputcarnumber_grid_layout, null);
        views.add(view1);
        views.add(view2);
        views.add(view3);
        viewPager.setAdapter(new ChoseCarNumPageAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        et_carnumber = (EditText) findViewById(R.id.et_input_carnumber_dialog);

        if (carnumber != null) {
            et_carnumber.setText(carnumber);
        }
        bt_ok = (Button) findViewById(R.id.bt_input_carnumber_ok_dialog);
        rl_delete_edtext = (RelativeLayout) findViewById(R.id.rl_input_carnumber_delete_dialog);
    }

    public void setView() {
        rl_delete_edtext.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String carnumber = et_carnumber.getText().toString().trim();
                if (carnumber.length() >= 1) {
                    int index = et_carnumber.getSelectionStart();
                    Editable editable = et_carnumber.getText();
                    if (index >= 1) {
                        editable.delete(index - 1, index);
                    }
                }
            }
        });
        rl_delete_edtext.setOnLongClickListener(new Button.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                et_carnumber.setText("");
                return false;
            }
        });
        findViewById(R.id.bt_input_carnumber_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //切为扫描车牌
//                try {
//                    CommontUtils.ZddYear();
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
                RecogService.recogModel = false;
                Intent intent = new Intent(context, MemoryCameraActivity.class);
//                Intent intent = new Intent(context, PosCaptureActivity.class);
                intent.putExtra("camera", true);
                intent.putExtra("from", getIntent().getStringExtra("from"));
                startActivity(intent);
                finish();
            }
        });
        bt_ok.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (CheckUtils.CarChecked(et_carnumber.getText().toString())) {
//                    Toast.makeText(context, "下一步", Toast.LENGTH_SHORT).show();
                    //通过判断是否有泊位号，如果 没有的话，跳转进入选择泊位界面
                    //如果之前已经设置泊位，或者本车场没有泊位界面，跳转进入拍照界面
                    //此处同样判断是否已经有泊位，如果已分配泊位，则下一步追缴，未分配泊位，则进泊位step
//                    CommontUtils.toast(context, "--" + getStringFromPreference("bowei"));
                    try {
                        //调用追缴接口
                        getParkInfo(et_carnumber.getText().toString());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }


                } else {
                    Toast.makeText(context, "请正确输入车牌号!!!", Toast.LENGTH_SHORT).show();
                }


            }
        });
        if (CommontUtils.checkString(SharedPreferencesUtils.getIntance(context).getfirstprovince()) && (!SharedPreferencesUtils.getIntance(context).getfirstprovince().equals("null"))) {
            et_carnumber.setText(SharedPreferencesUtils.getIntance(context).getfirstprovince());
            et_carnumber.setSelection(SharedPreferencesUtils.getIntance(context).getfirstprovince().length());
            viewPager.setCurrentItem(1);
        }

        et_carnumber.setInputType(InputType.TYPE_NULL);
        et_carnumber.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    viewPager.setCurrentItem(1);
                }
            }
        });

    }


    /**
     * 初始化头标
     */

    private void InitTextView() {
        textView1 = (TextView) findViewById(R.id.text1_dialog);
        textView2 = (TextView) findViewById(R.id.text2_dialog);
        textView3 = (TextView) findViewById(R.id.text3_dialog);
        textView1.setOnClickListener((View.OnClickListener) new MyOnClickListener(0));
        textView2.setOnClickListener((View.OnClickListener) new MyOnClickListener(1));
        textView3.setOnClickListener((View.OnClickListener) new MyOnClickListener(2));
    }

    /**
     * 2 * 初始化动画，这个就是页卡滑动时，下面的横线也滑动的效果，在这里需要计算一些数据
     */

    private void InitImageView() {
        imageView = (ImageView) findViewById(R.id.cursor_dialog);
        bmpW = BitmapFactory.decodeResource(context.getResources(), R.drawable.viewpage).getWidth();// 获取图片宽度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        offset = (screenW / 3 - bmpW) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        imageView.setImageMatrix(matrix);// 设置动画初始位置
    }

    /**
     * 头标点击监听 3
     */
    private class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        public void onClick(View v) {
            viewPager.setCurrentItem(index);
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
        int two = one * 2;// 页卡1 -> 页卡3 偏移量

        public void onPageScrollStateChanged(int arg0) {

        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        public void onPageSelected(int arg0) {

            Animation animation = new TranslateAnimation(one * currIndex, one * arg0, 0, 0);
            currIndex = arg0;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(300);
            imageView.startAnimation(animation);
            switch (viewPager.getCurrentItem()) {
                case 0:
                    textView1.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                    textView2.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    textView3.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    break;
                case 1:
                    textView2.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                    textView1.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    textView3.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    break;
                case 2:
                    textView3.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                    textView1.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    textView2.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    break;
            }
        }
    }

    public void setView1() {
        final GridView gv_province = (GridView) view1.findViewById(R.id.input_gridview_dialog);
        gv_province.setSelector(new ColorDrawable(Color.TRANSPARENT));
        final String[] province = new String[]{"京", "沪", "浙", "苏", "粤", "鲁", "晋", "冀", "豫", "川", "渝", "辽", "吉", "黑", "皖", "鄂",
                "湘", "赣", "闽", "陕", "甘", "宁", "蒙", "津", "贵", "云", "桂", "琼", "青", "新", "藏", "港", "澳"};
        ArrayList<String> provinces = new ArrayList<String>();
        for (int i = 0; i < province.length; i++) {
            provinces.add(province[i]);
        }
        InputCarNumberGridAdapter adapter = new InputCarNumberGridAdapter(context, provinces, false);
        gv_province.setAdapter(adapter);
        gv_province.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final int index = et_carnumber.getSelectionStart();
                final Editable editable = et_carnumber.getText();
                editable.insert(index, province[position]);
            }
        });
    }

    public void setView2() {
        final GridView gv_number = (GridView) view2.findViewById(R.id.input_gridview_dialog);
        gv_number.setSelector(new ColorDrawable(Color.TRANSPARENT));
        final String[] number = new String[]{"A", "B", "C", "D", "0", "5", "E", "F", "G", "H", "1", "6", "J", "K", "L", "M",
                "2", "7", "N", "P", "Q", "R", "3", "8", "S", "T", "U", "V", "4", "9", "W", "X", "Y", "Z"};
        ArrayList<String> numbers = new ArrayList<String>();
        for (int i = 0; i < number.length; i++) {
            numbers.add(number[i]);
        }
        InputCarNumberGridAdapter adapter = new InputCarNumberGridAdapter(context, numbers, true);
        gv_number.setAdapter(adapter);
        gv_number.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final int index = et_carnumber.getSelectionStart();
                final Editable editable = et_carnumber.getText();
                editable.insert(index, number[position]);
            }
        });
    }

    public void sheView3() {
        final GridView gv_police = (GridView) view3.findViewById(R.id.input_gridview_dialog);
        gv_police.setSelector(new ColorDrawable(Color.TRANSPARENT));
        final String[] police = new String[]{"军", "空", "海", "北", "沈", "兰", "济", "南", "广", "成", "WJ", "警",
                "消", "边", "水", "电", "林", "通", "使", "学", "无"};
        ArrayList<String> polices = new ArrayList<String>();
        for (int i = 0; i < police.length; i++) {
            polices.add(police[i]);
        }

        InputCarNumberGridAdapter adapter = new InputCarNumberGridAdapter(context, polices, false);
        gv_police.setAdapter(adapter);
        gv_police.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final int index = et_carnumber.getSelectionStart();
                final Editable editable = et_carnumber.getText();
                editable.insert(index, police[position]);

            }
        });
    }

    public void hideTypewriting() {

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_carnumber.getWindowToken(), 0);
        // Android.edittext点击时,隐藏系统弹出的键盘,显示出光标
        // 3.0以下版本可以用editText.setInputType(InputType.TYPE_NULL)来实现。
        // 3.0以上版本除了调用隐藏方法:setShowSoftInputOnFocus(false)
        int sdkInt = Build.VERSION.SDK_INT;// 16 -- 4.1系统
        if (sdkInt >= 11) {
            Class<EditText> cls = EditText.class;
            try {
                Method setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setShowSoftInputOnFocus.setAccessible(false);
                setShowSoftInputOnFocus.invoke(et_carnumber, false);
                setShowSoftInputOnFocus.invoke(et_carnumber, false);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            et_carnumber.setInputType(InputType.TYPE_NULL);
        }
    }

    /**
     * 检查逃单
     * //http://127.0.0.1/zld/collectorrequest.do?
     * action=getecsorder&token=ca67649c7a6c023e08b0357658c08c3d&car_number=
     */
    private ZhuiJiaoListEntity listEntity = new ZhuiJiaoListEntity();
    private ArrayList<ZhuiJiaoItemEntity> entity = new ArrayList<ZhuiJiaoItemEntity>();
    private String ismonthuser = "";

    public void getParkInfo(String carnumber) throws UnsupportedEncodingException {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取信息失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }
        String car = URLEncoder.encode(carnumber, "utf-8");
        AQuery aQuery = new AQuery(InputCarNumberActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=getecsorder&token="
                + token + "&car_number=" + URLEncoder.encode(car, "utf-8") + "&berthid=" +
                SharedPreferencesUtils.getIntance(this).getberthid()
                + "&version=" + CommontUtils.getVersion(this) + "&out=json";
        MyLog.w("InputCarNumberActivity-->>", "检查逃单的URL-->>" + url);
        final ProgressDialog dialog = ProgressDialog.show(InputCarNumberActivity.this, "加载中...", "获取车场信息数据...", true, true);
        dialog.setCancelable(false);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                dialog.dismiss();
                if (getIntent().getStringExtra("from").equals("input")) {
                    //正常的输车牌进场
                    if (object != null) {
                        Gson gson = new Gson();
                        listEntity = gson.fromJson(object, ZhuiJiaoListEntity.class);
                        MyLog.i("ParkingInfoActivity-->>", "解析的逃单" + listEntity.toString());
                        ismonthuser = listEntity.getIsmonthuser();
                        if (listEntity.getResult().equals("0")) {
                            //有逃单，跳转到追缴界面
                            //返回的预订单
                            putStringToPreference("preorderid", listEntity.getOrderid());
                            if (getBooleanFromPreference("next")) {
                                //如果已标记下次缴费，走正常流程
                                intent2();
                            } else {
                                entity = listEntity.getOrders();
                                Intent i = new Intent(context, ZhuiJiaoListActivity.class);
                                putStringToPreference("carnumber", et_carnumber.getText().toString());
                                i.putExtra("list", entity);
                                startActivityForResult(i, Constant.BACK_FROM_OWE);
                            }
                        } else if (listEntity.getResult().equals("1")) {
                            //没有逃单情况，走正常流程
                            putStringToPreference("preorderid", listEntity.getOrderid());
                            intent2();
                        } else if (listEntity.getResult().equals("-2")) {
                            new AlertDialog.Builder(InputCarNumberActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                    .setMessage("" + listEntity.getErrmsg())
                                    .setPositiveButton("继续进场", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            putStringToPreference("preorderid", listEntity.getOrderid());
                                            entity = listEntity.getOrders();
                                            if (getBooleanFromPreference("next")) {
                                                //如果已标记下次缴费，走正常流程
                                                intent2();
                                            } else if (!(entity != null && entity.size() > 0)) {
                                                //如果没有未缴订单
                                                intent2();
                                            } else {
                                                Intent i = new Intent(context, ZhuiJiaoListActivity.class);
                                                putStringToPreference("carnumber", et_carnumber.getText().toString());
                                                i.putExtra("list", entity);
                                                startActivityForResult(i, Constant.BACK_FROM_OWE);
                                            }
                                        }
                                    })
                                    .setNegativeButton("取消进场", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    }).create().show();
                        } else {
                            //该车辆已入场 -1 -3
                            new AlertDialog.Builder(InputCarNumberActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                    .setMessage("" + listEntity.getErrmsg()).setPositiveButton("确定", null).create().show();
                        }

                    } else {
                        putStringToPreference("preorderid", "");
                        intent2();
                    }
                } else {
                    //查询车牌是否欠费
                    if (object != null) {
                        Gson gson = new Gson();
                        listEntity = gson.fromJson(object, ZhuiJiaoListEntity.class);
                        MyLog.i("ParkingInfoActivity-->>", "解析的逃单" + listEntity.toString());
                        if (listEntity.getResult().equals("1")) {
                            //没有逃单情况，结束
                            CommontUtils.toast(context, "未查询到未缴记录");
                            finish();
                        } else {
                            //有逃单，跳转到追缴界面
                            entity = listEntity.getOrders();
                            if (CommontUtils.checkList(entity)) {
                                Intent i = new Intent(context, ZhuiJiaoListActivity.class);
                                putStringToPreference("carnumber", et_carnumber.getText().toString());
                                i.putExtra("list", entity);
                                startActivityForResult(i, Constant.BACK_FROM_OWE);
                            } else {
                                CommontUtils.toast(context, "未查询到未缴记录");
                                finish();
                            }

                        }

                    } else {
                        CommontUtils.toast(context, "未查询到未缴记录");
                        finish();
                    }
                }
            }

        }.timeout(5000));
    }

    private void intent2() {
        //走正常流程时，将下次补缴置为false
        putBooleanToPreference("next", false);
        putStringToPreference("carnumber", et_carnumber.getText().toString());
        if (CommontUtils.checkString(getStringFromPreference("bowei"))) {
            if (ismonthuser.equals("1")) {
                //会员不需要预付，直接生成订单
                photonum = SharedPreferencesUtils.getIntance(this).getphotoset(0);
                if (photonum > 0) {
//                    takePhoto(1);
                    Intent i = new Intent(InputCarNumberActivity.this, CameraActivity.class);
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
                    intent.putExtra("cartype", cartypecode);
                    startActivity(intent);
                    finish();
                } else {
                    //不需要预付，直接生成订单
                    photonum = SharedPreferencesUtils.getIntance(this).getphotoset(0);
                    if (photonum > 0) {
//                        takePhoto(1);
                        Intent i = new Intent(InputCarNumberActivity.this, CameraActivity.class);
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
//            MyLog.i("iiiiiiiiiinput", "没有选择泊位");
            Intent i = new Intent(context, InTheVehicleStepActivity.class);
            i.putExtra("ismonthuser", ismonthuser);
            i.putExtra("cartype", cartypecode);
            startActivity(i);
            MyLog.i("iiiiiiiiiinput", "没有选择泊位>>进入InTheVehicleStepActivity" + ismonthuser);
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        MyLog.i("INCarDialogActivity", "onActivityResult-------->");
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constant.BACK_FROM_OWE:
                    if (!TextUtils.isEmpty(getIntent().getStringExtra("from")) && getIntent().getStringExtra("from").equals("input")) {
                        intent2();
                    } else {
                        finish();
                    }
                    break;
                case 10:

                    String SDState = Environment.getExternalStorageState();
                    MyLog.i("INCarDialogActivity", " String SDState = Environment.getExternalStorageState()");
                    if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                        MyLog.i("INCarDialogActivity", "if");
//				  获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转
                        int degree = ImageUtils.readPictureDegree(files.get(numcount - 1).getPath());
                        MyLog.i("INCarDialogActivity", "int degree");
                        Bitmap bm = CameraBitmapUtil.getBitmapFromFile(files.get(numcount - 1), 1000, 1000);//获取照片的bitmap然后压缩存放；
                        MyLog.i("INCarDialogActivity", "bitmap bm:");
                        if (degree != 0) {
                            MyLog.i("TakePhotoUpdateActivity", "图片的旋转角度是：" + degree);
                            Bitmap newbitmap = ImageUtils.rotaingImageView(degree, bm);
                            CameraBitmapUtil.saveBitmap2file(newbitmap, files.get(numcount - 1).getPath());
                        } else {
                            MyLog.i("INCarDialogActivity", "eeeelse");
                            CameraBitmapUtil.saveBitmap2file(bm, files.get(numcount - 1).getPath());
                        }
                        MyLog.i("INCarDialogActivity", "numcount=" + numcount + "---photonum" + photonum);
                        try {
                            if (numcount < photonum) {
                                takePhoto(++numcount);
                            } else {
                                createOrderForPos();
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, "内存卡不存在", Toast.LENGTH_LONG).show();
                    }
                    Constant.ISNEEDBACKUP = true;
                    break;
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private List<File> files = new ArrayList<File>();
    private int photonum = 0;
    private int numcount = 1;

    private void takePhoto(int num) {
        // 执行拍照前，应该先判断SD卡是否存在
        Constant.ISNEEDBACKUP = false;
        File picFile = null;
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            SimpleDateFormat dateaf = new SimpleDateFormat("yyyy年MM月dd日HH分mm秒");
            String filename = dateaf.format(System.currentTimeMillis()) + num + ".jpeg";
            picFile = new File(dir.getAbsolutePath(), filename);
            files.add(picFile);
            MyLog.i("INCarDialogActivity", "picFile=" + picFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(picFile));
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityForResult(intent, 10);
            MyLog.i("INCarDialogActivity", "startActivityForResult(intent, 10)");
            // 跳到拍照页面，这里1没用到，可以在一个onActivityResult里设置requestCode为0来接收新页面的数据。
        } else {
            Toast.makeText(this, "内存卡不存在", Toast.LENGTH_LONG).show();
        }
    }

    private VoiceSynthesizerUtil voice;
    //POS机生成订单接口;
    //collectorrequest.do?action=posincome&token=2dd4b1b320225dfd4fc44ad6b53fa734&carnumber=
    private String uid;
    private InCarDialogInfo infos;
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
                    + "&car_type=" + cartypecode;
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
                        infos = info;
                        if (info != null) {
                            MyLog.d("InCarDialogActivity", info.toString());
                            if ("1".equals(info.getResult())) {
                                //生成订单成功后将车检器订单置空
                                putStringToPreference("berthorderid", "");
                                putBooleanToPreference("next", false);
                                voice = new VoiceSynthesizerUtil(context);
                                voice.playText("生成订单");
                                bt_ok.setOnClickListener(null);
//                                if (CommontUtils.checkList(files)) {
//                                    for (int i = 0; i < files.size(); i++) {
//                                        String SDState = Environment.getExternalStorageState();
//                                        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
//                                            File dir = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
//                                            if (!dir.exists()) {
//                                                dir.mkdirs();
//                                            }
//                                            files.get(i).renameTo(new File(dir.getAbsolutePath(), info.getOrderid() + "in" + i + ".jpeg"));
//                                            CameraBitmapUtil.upload(context, i, info.getOrderid(), 0);
//                                        }
//                                    }
//                                }
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
//                                if (mService.getState() != BluetoothService.STATE_CONNECTED) {
//                                    conn2bluetooth2();
//                                } else {
                                    prient(uid, info);
//                                }
                                } else {
                                    FinishAction();
                                }


//                            if (parkPositionListener != null && !SharedPreferencesUtils.getIntance(InCarDialogActivity.this).getSelectParkPosition()) {
//                                parkPositionListener.doSelectParkPosition(tv_add_carnumber.getText().toString(), info.getOrderid());
//                            }
//                            InCarDialogActivity.this.finish();
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

    // if(ismonthuser.equals("1"))
    //打印凭条
    public void prient(String uid, InCarDialogInfo info) {
        TcbCheckCarIn incar = new TcbCheckCarIn();
        incar.setOrderid(info.getOrderid());
//        incar.setCarnumber(tv_add_carnumber.getText().toString());
        incar.setTime(info.getBtime());
        String Sname="";
        String gang = "";
        if (SharedPreferencesUtils.getIntance(this).getisprintName()) {
            Sname = getStringFromPreference("name");
            gang = "-";
        }
        incar.setMeterman(Sname);
        Bitmap qrbitmap = new QRCodeEncoder().encode2BitMap(BaseActivity.baseurl + info.getQrcode(), 240, 240);
        Bitmap imgbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_check);
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
        //将已处理的车位号存入，回到泊位列表时移除这个车位号消息 泊位id
        putStringToPreference("boweistate", getStringFromPreference("bowei"));
        finish();
    }


}
