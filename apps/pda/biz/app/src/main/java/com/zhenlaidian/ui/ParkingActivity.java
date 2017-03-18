package com.zhenlaidian.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.ParkingInfo;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;

/**
 * 车场详情界面;
 */
public class ParkingActivity extends BaseActivity {
    private Button bt_change;  //修改按钮
    private TextView tv_parkname;  //车场名字
    private TextView tv_address;   //地址
    private TextView tv_updateImage;   //修改照片
    //	private TextView tv_timebet;  //开放时间段
//	private TextView tv_price;    //车场价格
    private TextView tv_parktotal;//总车位数
    private TextView tv_stoptype; //停车类型
    private TextView tv_parktype;  //车场类型
    private TextView tv_phone;   //车场电话
    private TextView tv_park_details; //停车场描述
    //	private TextView tv_mobile;  //手机号
    private ImageView iv_pakePhoto;//车场照片
    private String uri;
    private ParkingInfo parkingInfo;
    private ActionBar actionBar;
    private SharedPreferences autologin;
    private String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        setContentView(R.layout.parking_info_item);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        autologin = getSharedPreferences("autologin", Context.MODE_PRIVATE);
        role = autologin.getString("role", "0");
        initView();
        if (getIntent().getExtras() != null && getIntent().getExtras().get("parkinfo") != null) {
            parkingInfo = (ParkingInfo) getIntent().getExtras().get("parkinfo");
            setView();
        } else {
            getParkInfo();
        }
    }


    public void initView() {
        tv_parkname = (TextView) findViewById(R.id.tv_parking_name);
        tv_address = (TextView) findViewById(R.id.tv_parking_address);
        tv_parktotal = (TextView) findViewById(R.id.tv_parking_parktotal);
        tv_park_details = (TextView) findViewById(R.id.tv_parking_details);
//		tv_price = (TextView) findViewById(R.id.tv_parking_price);
//		tv_mobile = (TextView) findViewById(R.id.tv_parking_mobile);
        tv_stoptype = (TextView) findViewById(R.id.tv_parking_stoptype);
        tv_parktype = (TextView) findViewById(R.id.tv_parking_parktype);
        tv_updateImage = (TextView) findViewById(R.id.tv_parking_info_updateImage);
//		tv_timebet = (TextView) findViewById(R.id.tv_parking_timebet);
        tv_phone = (TextView) findViewById(R.id.tv_parking_phone);
        bt_change = (Button) findViewById(R.id.bt_parking_info_change);
        iv_pakePhoto = (ImageView) findViewById(R.id.iv_parking_info_image);

    }

    public void setView() {
        if (parkingInfo.getPicurls() != null && parkingInfo.getPicurls() != "null") {
            String picurls = parkingInfo.getPicurls();
            String[] s = picurls.split(";");
            uri = baseurl + s[0];
            MyLog.w("ParkingInfoActivity", "照片的uri地址是-->>" + uri);
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(uri, iv_pakePhoto);
        }
        if (parkingInfo.getAddress() != null) {
            tv_address.setText(parkingInfo.getAddress());
        }
        if (parkingInfo.getName() != null) {
            tv_parkname.setText(parkingInfo.getName());
        }
        if (parkingInfo.getParkingtotal() != null) {
            tv_parktotal.setText(parkingInfo.getParkingtotal());
        }
        if (parkingInfo.getParktype() != null) {
            tv_parktype.setText(parkingInfo.getParktype());
        }
        if (parkingInfo.getPhone() != null) {
            tv_phone.setText(parkingInfo.getPhone());
        }
        if (parkingInfo.getStoptype() != null) {
            tv_stoptype.setText(parkingInfo.getStoptype());
        }
        if (parkingInfo.getResume() != null) {
            tv_park_details.setText(parkingInfo.getResume());
        }
        bt_change.setOnClickListener(new OnClickListener() {
            long lasttime;

            @Override
            public void onClick(View arg0) {
                if (role != null && role.equals("1")) {
                    Intent intent = new Intent(ParkingActivity.this, ChangeParkingInfo.class);
                    intent.putExtra("parkinfo", parkingInfo);
                    ParkingActivity.this.startActivity(intent);
                    ParkingActivity.this.finish();
                } else {
                    if (System.currentTimeMillis() - lasttime >= 1000) {
                        Toast.makeText(ParkingActivity.this, "请用管理员账号登录修改停车场信息！", 0).show();
                    }
                }
                lasttime = System.currentTimeMillis();
            }
        });
        iv_pakePhoto.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParkingActivity.this, ShowMaxPhotoActivity.class);
                intent.putExtra("uri", uri);
                startActivity(intent);
            }
        });

        tv_updateImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParkingActivity.this, TakePhotoUpdateActivity.class);
                intent.putExtra("comid", parkingInfo.getId());
                startActivity(intent);
                ParkingActivity.this.finish();
            }
        });
    }


    // actionBar的点击回调方法
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                ParkingActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //http://s.zhenlaidian.com/zld/collectorrequest.do?action=cominfo&token=*
    public void getParkInfo() {
        if (!IsNetWork.IsHaveInternet(this)) {
            Toast.makeText(this, "获取信息失败，请检查网络！", 0).show();
            return;
        }
        AQuery aQuery = new AQuery(ParkingActivity.this);
        String path = baseurl;
        String url = path + "collectorrequest.do?action=cominfo&token=" + token + "&out=json";
        System.out.println("请求车场信息的URL-->>" + url);
        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取车场信息数据...", true, true);
        aQuery.ajax(url, String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (object != null) {
                    dialog.dismiss();
                    Gson gson = new Gson();
                    parkingInfo = gson.fromJson(object, ParkingInfo.class);
                    MyLog.i("ParkingInfoActivity-->>", "解析的车场信息为" + parkingInfo.toString());
                    if (parkingInfo != null && parkingInfo.getId() != null && parkingInfo.getPicurls() != null) {
                        setView();
                    }
                } else {
                    dialog.dismiss();
                    return;
                }
            }
        });
    }

}
