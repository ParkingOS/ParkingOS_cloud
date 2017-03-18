package com.zhenlaidian.camera;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.device.DeviceManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.zhenlaidian.R;
import com.zhenlaidian.util.CameraBitmapUtil;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.ImageUtils;
import com.zhenlaidian.util.MyLog;

import java.io.File;
import java.util.ArrayList;

import static com.zhenlaidian.ui.BaseActivity.baseurl;
import static com.zhenlaidian.ui.BaseActivity.token;

public class CameraActivity extends Activity implements CameraPreview.callbackFile, View.OnClickListener {

    private CameraPreview mPreview;
    private String TAG = "CameraActivity";
    private FrameLayout preview;
    private ImageButton captureButton;

    private ImageView flash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.camera_preview);
        mPreview = new CameraPreview(CameraActivity.this, CameraActivity.this);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        initPreviewPic();
        bar1 = ((ImageView) findViewById(R.id.bar1));
        bar2 = ((ImageView) findViewById(R.id.bar2));
        bar3 = ((ImageView) findViewById(R.id.bar3));
        bar4 = ((ImageView) findViewById(R.id.bar4));
        flash = ((ImageView) findViewById(R.id.flash));

        NUM = getIntent().getIntExtra("num", 0);

        captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(this);
        flash.setOnClickListener(this);
        if (CommontUtils.Is910()) {
            new DeviceManager().enableHomeKey(false);
        }
    }

    private boolean light = false;
    private String path;
    private int degree;
    private int NUM = 0;
    private Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    flashBar(true);
                    break;
                case 0:
                    flashBar(false);
                    break;
                case 200:
                    path = (String) msg.obj;
                    degree = ImageUtils.readPictureDegree(path);
//                    SaveImage(path, degree, System.currentTimeMillis()+"");
                    String url = baseurl + "collectorrequest.do?action=getservertime&token=" + token;
                    AQuery aQuery = new AQuery(CameraActivity.this);
                    final ProgressDialog dialog = ProgressDialog.show(CameraActivity.this, "", "处理中...", true, true);
                    aQuery.ajax(url, String.class, new AjaxCallback<String>() {
                        @Override
                        public void callback(String url, String object, AjaxStatus status) {
                            // TODO Auto-generated method stub
                            super.callback(url, object, status);
                            dialog.dismiss();
                            Message m = new Message();
                            m.what = 1222;
                            if (object != null && object != "") {
                                m.obj = object;
                            } else {
                                m.obj = System.currentTimeMillis() + "";
                            }
                            h.sendMessage(m);
                        }
                    });
                    break;
                case 1222:
                    SaveImage((String) msg.obj);
                    break;
            }

        }
    };

    private void SaveImage(String unixtime) {
        try {
            if (CommontUtils.checkString(path)) {
                Bitmap bm = CameraBitmapUtil.addWaterMark(CameraBitmapUtil.getBitmapFromFile(new File(path), 600, 800),
                        CommontUtils.Mili2Time(unixtime), CameraActivity.this);
                //获取照片的bitmap然后压缩存放；
                if (degree != 0) {
                    MyLog.i("INCarDialogActivity", "图片的旋转角度是：" + degree);
                    Bitmap newbitmap = ImageUtils.rotaingImageView(degree, bm);
                    CameraBitmapUtil.saveBitmap2file(newbitmap, path);
                    preImg.setImageBitmap(newbitmap);
                } else {
                    MyLog.i("INCarDialogActivity", "eeeelse");
                    CameraBitmapUtil.saveBitmap2file(bm, path);
                    preImg.setImageBitmap(bm);
                }
                lnPre.setVisibility(View.VISIBLE);
                lnPhoto.setVisibility(View.GONE);
            } else {
                Toast.makeText(CameraActivity.this, "拍照失败", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
//        if (CommontUtils.Is910()) {
//            new DeviceManager().enableHomeKey(true);
//        }
    }

    private ArrayList<String> listPath = new ArrayList<String>();
    private String returnpath;

    @Override
    public void fileaddr(String path) {
//        Toast.makeText(CameraActivity.this,"pppppp"+path,1).show();
        Log.d(TAG, "callback = " + path);

        h.sendEmptyMessage(0);
//        Toast.makeText(this,"照片已存储："+path,Toast.LENGTH_LONG).show();
        Message m = new Message();
        m.what = 200;
        m.obj = path;
        h.sendMessage(m);
        returnpath = path;


    }

    private ImageView bar1, bar2, bar3, bar4;

    private void flashBar(boolean flash) {
        if (flash) {
            bar1.setVisibility(View.VISIBLE);
            bar2.setVisibility(View.VISIBLE);
            bar3.setVisibility(View.VISIBLE);
            bar4.setVisibility(View.VISIBLE);
        } else {
            bar1.setVisibility(View.GONE);
            bar2.setVisibility(View.GONE);
            bar3.setVisibility(View.GONE);
            bar4.setVisibility(View.GONE);
        }
    }

    private Button btnOk, btnAgain;
    private ZoomImageView preImg;
    private FrameLayout lnPre;
    private LinearLayout lnPhoto;

    private void initPreviewPic() {
        lnPre = ((FrameLayout) findViewById(R.id.preview_ln));
        preImg = ((ZoomImageView) findViewById(R.id.preview_img));
        btnAgain = ((Button) findViewById(R.id.preview_refresh));
        btnOk = ((Button) findViewById(R.id.preview_ok));
        lnPhoto = ((LinearLayout) findViewById(R.id.photo_ln));
        btnAgain.setOnClickListener(this);
        btnOk.setOnClickListener(this);
    }

    private long currenttime = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_capture:
                //点击拍照
                if (System.currentTimeMillis() - currenttime > 1000) {
                    h.sendEmptyMessage(1);
                    mPreview.takePicture();
                    currenttime = System.currentTimeMillis();
                } else {
                    Toast.makeText(CameraActivity.this, "点击的太快了！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.flash:
                //闪光灯开关
                if (light) {
                    mPreview.offLight();
                    light = false;
                } else {
                    mPreview.openLight();
                    light = true;
                }
                break;
            case R.id.preview_refresh:
                //重拍
                lnPre.setVisibility(View.GONE);
                lnPhoto.setVisibility(View.VISIBLE);
                break;
            case R.id.preview_ok:
                //ok，拍下一张
                listPath.add(returnpath);
                if (listPath.size() >= NUM) {
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra("list", listPath);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    lnPre.setVisibility(View.GONE);
                    lnPhoto.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:

            case KeyEvent.KEYCODE_HOME:

            case KeyEvent.KEYCODE_MENU:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
