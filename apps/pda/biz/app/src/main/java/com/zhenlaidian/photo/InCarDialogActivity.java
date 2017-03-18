package com.zhenlaidian.photo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.zxing.lswss.QRCodeEncoder;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.InCarDialogInfo;
import com.zhenlaidian.engine.SelectParkPositionListener;
import com.zhenlaidian.printer.PrinterUitls;
import com.zhenlaidian.printer.TcbCheckCarIn;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.util.ImageUtils;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;
import com.zhenlaidian.util.TimeTypeUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;

/**
 * Created by zhangyunfei on 15/10/14.
 * 显示为dialog样式的activity Pos机车辆入场
 */
public class InCarDialogActivity extends Activity {

    private TextView tv_intime;
    private TextView tv_in_car_cancle;
    private TextView tv_add_carnumber;
    private ImageView iv_take_car_photo;
    private Button bt_ok;
    private File picFile = null;//照片文件
    private String filename;//文件名字
    private CheckBox cb_default;
    SharedPreferences spf;
    public static SelectParkPositionListener parkPositionListener = null;

    public DialogInterface.OnCancelListener listener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            InPutCarNumberDialog dialog1 = (InPutCarNumberDialog) dialog;
            tv_add_carnumber.setText(dialog1.getcarnumber());
            bt_ok.setText("入场");
        }
    };

    public static void setParkPositionListener(SelectParkPositionListener ls) {
        parkPositionListener = ls;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_car_dialog);
        spf = getSharedPreferences("tingchebao", Context.MODE_PRIVATE);
        this.setFinishOnTouchOutside(false);
        initView();
        setVeiw();
    }

    private void initView() {
        tv_intime = (TextView) findViewById(R.id.tv_in_car_time);
        tv_in_car_cancle = (TextView) findViewById(R.id.tv_in_car_cancle);
        tv_add_carnumber = (TextView) findViewById(R.id.tv_in_car_carnumber);
        iv_take_car_photo = (ImageView) findViewById(R.id.iv_in_car_carimage);
        bt_ok = (Button) findViewById(R.id.bt_in_car_ok);
        cb_default = (CheckBox) findViewById(R.id.cb_in_car_default);
        if (spf.getBoolean("indefault", false)) {
            cb_default.setChecked(true);
        } else {
            cb_default.setChecked(false);
        }
    }

    private void setVeiw() {
        //入场时间显示为系统当前时间;
        String time = TimeTypeUtil.getMothDay(System.currentTimeMillis());
        tv_intime.setText(time);
        tv_add_carnumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击去调用扫车牌的界面;
                boolean cardef = spf.getBoolean("indefault", false);
                if (cardef) {
                    InPutCarNumberDialog dilaog = new InPutCarNumberDialog(InCarDialogActivity.this, true, "", listener);
                    dilaog.show();
                } else {
                    Intent intent = new Intent(InCarDialogActivity.this, PosCaptureActivity.class);
                    startActivity(intent);
                }
            }
        });
        iv_take_car_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击去调用系统相机拍照;
                takePhoto();
            }
        });
        bt_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击生成订单.无论有没有车牌号或者照片都可以;
                try {
                    createOrderForPos();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        tv_in_car_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击关闭activity
                InCarDialogActivity.this.finish();
            }
        });
        cb_default.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    spf.edit().putBoolean("indefault", true).commit();
                } else {
                    spf.edit().putBoolean("indefault", false).commit();
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String carnumber = intent.getStringExtra("carnumber");
        Boolean handinput = intent.getBooleanExtra("handinput", false);
        if (!TextUtils.isEmpty(carnumber)) {
            tv_add_carnumber.setText(carnumber);
            bt_ok.setText("入场");
        }
        if (handinput) {
            InPutCarNumberDialog dilaog = new InPutCarNumberDialog(InCarDialogActivity.this, true, "", listener);
            dilaog.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyLog.i("INCarDialogActivity", "onActivityResult-------->");
        if (resultCode == Activity.RESULT_OK) {
            String SDState = Environment.getExternalStorageState();
            if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                File dir = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
//				 * 获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转
                int degree = ImageUtils.readPictureDegree(picFile.getPath());
                Bitmap bm = getBitmapFromFile(picFile, 1000, 1000);//获取照片的bitmap然后压缩存放；
                if (degree != 0) {
                    MyLog.i("TakePhotoUpdateActivity", "图片的旋转角度是：" + degree);
                    Bitmap newbitmap = ImageUtils.rotaingImageView(degree, bm);
                    saveBitmap2file(newbitmap, picFile.getPath());
                    int degree2 = ImageUtils.readPictureDegree(picFile.getPath());
                    MyLog.i("TakePhotoUpdateActivity", "图片被旋转后的角度是---：" + degree2);
                    iv_take_car_photo.setImageBitmap(newbitmap);
                    iv_take_car_photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    bt_ok.setText("入场");
                } else {
                    iv_take_car_photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    iv_take_car_photo.setImageBitmap(bm);
                    bt_ok.setText("入场");
                }
            } else {
                Toast.makeText(this, "内存卡不存在", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void takePhoto() {
        // 执行拍照前，应该先判断SD卡是否存在
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            SimpleDateFormat dateaf = new SimpleDateFormat("yyyy年MM月dd日HH分mm秒");
            String filename = dateaf.format(System.currentTimeMillis()) + ".jpeg";
            picFile = new File(dir.getAbsolutePath(), filename);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(picFile));
            startActivityForResult(intent, 1);
            // 跳到拍照页面，这里1没用到，可以在一个onActivityResult里设置requestCode为0来接收新页面的数据。
        } else {
            Toast.makeText(this, "内存卡不存在", Toast.LENGTH_LONG).show();
        }
    }

    //把照片文件压缩后转为bitmap；
    public Bitmap getBitmapFromFile(File dst, int width, int height) {
        if (null != dst && dst.exists()) {
            BitmapFactory.Options opts = null;
            if (width > 0 && height > 0) {
                opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(dst.getPath(), opts);
                // 计算图片缩放比例
                final int minSideLength = Math.min(width, height);
                opts.inSampleSize = ImageUtils.computeSampleSize(opts, minSideLength, width * height);
                opts.inJustDecodeBounds = false;
                opts.inInputShareable = true;
                opts.inPurgeable = true;
            }
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(dst.getPath(), opts);
                FileOutputStream fos = new FileOutputStream(picFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                return bitmap;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //bitmap保存到文件；
    public static boolean saveBitmap2file(Bitmap bmp, String filename) {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 70;
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bmp.compress(format, quality, stream);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        filename = "";
    }

    //api<11点击空白区域不让activity关闭
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && isOutOfBounds(this, event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean isOutOfBounds(Activity context, MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
        final View decorView = context.getWindow().getDecorView();
        return (x < -slop) || (y < -slop) || (x > (decorView.getWidth() + slop)) || (y > (decorView.getHeight() + slop));
    }

    //POS机生成订单接口;
    //collectorrequest.do?action=posincome&token=2dd4b1b320225dfd4fc44ad6b53fa734&carnumber=
    public void createOrderForPos() throws UnsupportedEncodingException {
        SharedPreferences pfs = this.getSharedPreferences("autologin", Context.MODE_PRIVATE);
        final String uid = pfs.getString("account", "");
        final String carnumber = URLEncoder.encode(tv_add_carnumber.getText().toString(), "utf-8");
        String url = BaseActivity.baseurl + "collectorrequest.do?action=posincome&token=" +
                BaseActivity.token + "&carnumber=" + URLEncoder.encode(carnumber, "utf-8");
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
                    if (info != null) {
                        MyLog.d("InCarDialogActivity", info.toString());
                        if ("1".equals(info.getResult())) {
                            if (picFile != null) {
                                String SDState = Environment.getExternalStorageState();
                                if (SDState.equals(Environment.MEDIA_MOUNTED)) {
                                    File dir = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
                                    if (!dir.exists()) {
                                        dir.mkdirs();
                                    }
                                    picFile.renameTo(new File(dir.getAbsolutePath(), info.getOrderid() + ".jpeg"));
                                }
                            }
                            prient(uid, info);
                            if (parkPositionListener != null && !SharedPreferencesUtils.getIntance(InCarDialogActivity.this).getSelectParkPosition()) {
                                parkPositionListener.doSelectParkPosition(tv_add_carnumber.getText().toString(), info.getOrderid());
                            }
                            InCarDialogActivity.this.finish();
                        } else {
                            Toast.makeText(InCarDialogActivity.this, info.getErrmsg(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    dialog.dismiss();
                    switch (status.getCode()) {
                        case -101:
                            Toast.makeText(InCarDialogActivity.this, "网络错误！--请再次确认车牌生成订单！", Toast.LENGTH_SHORT).show();
                            break;
                        case 500:
                            Toast.makeText(InCarDialogActivity.this, "服务器错误！--请再次确认车牌生成订单！", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }

    //打印凭条
    public void prient(String uid, InCarDialogInfo info) {
        TcbCheckCarIn incar = new TcbCheckCarIn();
        incar.setOrderid(info.getOrderid());
        incar.setCarnumber(tv_add_carnumber.getText().toString());
        incar.setTime(info.getBtime());
        incar.setMeterman(SharedPreferencesUtils.getIntance(this).getName() + " (" + uid + ")");
        Bitmap qrbitmap = new QRCodeEncoder().encode2BitMap(BaseActivity.baseurl + info.getQrcode(), 230, 230);
        Bitmap imgbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_check);
        PrinterUitls.getInstance().printerTCBCheckCarIn(incar, qrbitmap, imgbitmap);
    }
}
