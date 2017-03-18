package com.zhenlaidian.photo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.lswss.QRCodeEncoder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.CartypeAdapter;
import com.zhenlaidian.bean.CarTypeItem;
import com.zhenlaidian.bean.InCarDialogInfo;
import com.zhenlaidian.bean.ZhuiJiaoItemEntity;
import com.zhenlaidian.bean.ZhuiJiaoListEntity;
import com.zhenlaidian.camera.CameraActivity;
import com.zhenlaidian.plate_wentong.MemoryCameraActivity;
import com.zhenlaidian.printer.TcbCheckCarIn;
import com.zhenlaidian.service.PullMsgService;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.InTheVehicleStepActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.ui.PrePayParkingActivity;
import com.zhenlaidian.ui.ZhuiJiaoListActivity;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * pos机车牌校正;
 */
public class PosCheckNumberActivity extends BaseActivity {

    private ImageView iv_car_number;
    private EditText et_hand_write;
    private TextView tv_time;
    private Button bt_ok;
    private Button bt_again;
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pos_check_number_activity);
        if (getIntent().getExtras() != null) {
            result = getIntent().getStringExtra("number");

        }
        initVeiw();
        setveiw();
//        DecodeManager.getinstance().destroyAllMemery();// 释放TCBso库文件资源；
//        MyLog.i("CheckNumberActivity", " onCreate---释放TCB.so解析库文件!");
        LeaveActivity.isclosetcb = true;

        String strcartype = getStringFromPreference("car_type");
//        String strcartype = "[{\"id\":\"66\",\"name\":\"小型车\"},{\"id\":\"67\",\"name\":\"中型车\"},{\"id\":\"68\",\"name\":\"加长版\"}]";

        cartype = ((TextView) findViewById(R.id.poscheck_cartype));
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
//        try {
//            CommontUtils.AddYear();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
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

    private void initVeiw() {
        iv_car_number = (ImageView) findViewById(R.id.iv_pos_check_car_number_img);
        et_hand_write = (EditText) findViewById(R.id.et_pos_check_number_write);
        tv_time = (TextView) findViewById(R.id.tv_pos_check_number_time);
        bt_ok = (Button) findViewById(R.id.bt_pos_check_number_ok);
        bt_again = (Button) findViewById(R.id.bt_pos_check_number_again);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!TextUtils.isEmpty(bitmapPath)) {
            try {
                File file = new File(bitmapPath);
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String bitmapPath;

    @SuppressLint("SimpleDateFormat")
    private void setveiw() {
//        String imgfilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ATingCheBao/CarNumber.jpeg";
//        String imgfilePath = getIntent().getStringExtra("path");
//        ImageLoader imageLoader = ImageLoader.getInstance();
//        ImageLoader.getInstance().displayImage("file://" + imgfilePath, iv_car_number);

        bitmapPath = getIntent().getStringExtra("path");
        int left = getIntent().getIntExtra("left", -1);
        int top = getIntent().getIntExtra("top", -1);
        int w = getIntent().getIntExtra("width", -1);
        int h = getIntent().getIntExtra("height", -1);
        System.out.println("图片路径" + bitmapPath);

        try {
            if (bitmapPath != null && !bitmapPath.equals("")) {
                Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath);
                bitmap = Bitmap.createBitmap(bitmap, left, top, w, h);
                if (bitmap != null) {
                    iv_car_number.setImageBitmap(bitmap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (bitmapPath != null && !bitmapPath.equals("")) {
                ImageLoader.getInstance().displayImage("file://" + bitmapPath, iv_car_number);
            }
        }
        et_hand_write.setText(result);
        et_hand_write.setSelection(et_hand_write.getText().length());//设置光标位置
        java.util.Date date = new java.util.Date();
        SimpleDateFormat timef = new SimpleDateFormat("HH:mm");
        tv_time.setText(timef.format(date));

        bt_again.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //点击重新扫牌;
//                try {
//                    CommontUtils.ZddYear();
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
                Intent intent = new Intent(PosCheckNumberActivity.this, MemoryCameraActivity.class);
//                Intent intent = new Intent(PosCheckNumberActivity.this, PosCaptureActivity.class);
                intent.putExtra("camera", true);
                intent.putExtra("from", getIntent().getStringExtra("from"));
                startActivity(intent);
                PosCheckNumberActivity.this.finish();
            }
        });

        bt_ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //点击完成车牌校正;
                try {
                    //调用追缴接口
                    getParkInfo(et_hand_write.getText().toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 检查逃单
     * //http://127.0.0.1/zld/collectorrequest.do?
     * action=getecsorder&token=ca67649c7a6c023e08b0357658c08c3d&car_number=
     */
    private ZhuiJiaoListEntity listEntity = new ZhuiJiaoListEntity();
    private ArrayList<ZhuiJiaoItemEntity> entity = new ArrayList<ZhuiJiaoItemEntity>();
    private String ismonthuser;
    private long current;

    public void getParkInfo(String carnumber) throws UnsupportedEncodingException {
        if (System.currentTimeMillis() - current > 2000)
            current = System.currentTimeMillis();
        else
            return;
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取信息失败，请检查网络！", Toast.LENGTH_SHORT).show();
            return;
        }
        String car = URLEncoder.encode(carnumber, "utf-8");
        AQuery aQuery = new AQuery(PosCheckNumberActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=getecsorder&token="
                + token + "&car_number=" + URLEncoder.encode(car, "utf-8") + "&berthid=" +
                SharedPreferencesUtils.getIntance(this).getberthid() + "&version=" + CommontUtils.getVersion(this) +
                "&out=json";
        MyLog.w("InputCarNumberActivity-->>", "检查逃单的URL-->>" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取车场信息数据...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                dialog.dismiss();
                if (getIntent().getStringExtra("from").equals("input")) {
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
                                i.putExtra("list", entity);
                                startActivityForResult(i, Constant.BACK_FROM_OWE);
                            }
                        } else if (listEntity.getResult().equals("1")) {
                            //没有逃单情况，走正常流程
                            //返回的预订单
                            putStringToPreference("preorderid", listEntity.getOrderid());
                            intent2();
                        } else if (listEntity.getResult().equals("-2")) {
                            new AlertDialog.Builder(PosCheckNumberActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
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
                            //该车辆已入场
                            new AlertDialog.Builder(PosCheckNumberActivity.this).setTitle("提示").setIcon(R.drawable.app_icon_32)
                                    .setMessage("" + listEntity.getErrmsg()).setPositiveButton("确定", null).create().show();
                        }
                    } else {
                        MyLog.w("InputCarNumberActivity-->>", "NULL-->>" + object);
//                        putStringToPreference("preorderid", "");
//                        intent2();
                        Toast.makeText(PosCheckNumberActivity.this, "查询失败，请稍后再试", Toast.LENGTH_LONG).show();
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
                                putStringToPreference("carnumber", et_hand_write.getText().toString());
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

        });
    }

    private void intent2() {
        //走正常流程时，将下次补缴置为false
        putBooleanToPreference("next", false);
        putStringToPreference("carnumber", et_hand_write.getText().toString());

        if (CommontUtils.checkString(getStringFromPreference("bowei"))) {
//                        intent.setClass(context, InCarDialogActivity.class);
            if (ismonthuser.equals("1")) {
                //会员不需要预付，直接生成订单
                photonum = SharedPreferencesUtils.getIntance(this).getphotoset(0);
                if (photonum > 0) {
//                takePhoto(1);
                    Intent i = new Intent(PosCheckNumberActivity.this, CameraActivity.class);
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
//                    takePhoto(1);
                        Intent i = new Intent(PosCheckNumberActivity.this, CameraActivity.class);
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
            Intent i = new Intent(context, InTheVehicleStepActivity.class);
            i.putExtra("ismonthuser", ismonthuser);
            i.putExtra("cartype", cartypecode);
            startActivity(i);
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constant.BACK_FROM_OWE:
//                    CommontUtils.toast(context,"inputnumberacti!!!!!!!!!!!");
//                    intent2();
                    if (!TextUtils.isEmpty(getIntent().getStringExtra("from")) && getIntent().getStringExtra("from").equals("input")) {
                        intent2();
                    } else {
                        finish();
                    }
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
    private int photonum = 0;
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
        String gang = "-";
        incar.setTime(info.getBtime());
        if (SharedPreferencesUtils.getIntance(this).getisprintName()) {
            incar.setMeterman(SharedPreferencesUtils.getIntance(this).getName());
        } else {
            incar.setMeterman("");
            gang = "";
        }
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
        SharedPreferencesUtils.getIntance(context).getmobile();
//        sendMessage(qrbitmap);

        FinishAction();
    }

    private void FinishAction() {
        putStringToPreference("boweistate", getStringFromPreference("bowei"));
        finish();
    }
}
