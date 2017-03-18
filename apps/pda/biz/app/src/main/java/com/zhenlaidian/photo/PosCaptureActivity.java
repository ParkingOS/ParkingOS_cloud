package com.zhenlaidian.photo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.zhenlaidian.R;
import com.zhenlaidian.decode.DecodeThread;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.MyLog;

import java.io.ByteArrayOutputStream;

/**
 * 自定义拍照,照片传给二维码解析zbar库;
 */
@SuppressLint({"NewApi", "HandlerLeak"})
public class PosCaptureActivity extends Activity {

    private Button bt_light;// 开灯
    private Button bt_back;// 返回
    private TextView tv_warn;
    private Button bt_hand_input;// 手动输入
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera = null;
    private Handler handler;
    private final int FIAL = 999;
    private final int SUCCESS = 000;
    private final int AUTOFOCUS = 777;
    private Parameters parameters;
    private boolean lightflag = true;
    private boolean isdecode = true;
    DecodeThread thread;
    private Bitmap bmp;
    int autofocus = 2;
//    private CheckBox cb_default;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        // 窗口标题-没有tile
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 屏幕常亮
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.pos_my_capture_activity);
        if (LeaveActivity.isclosetcb) {
            DecodeManager.getinstance().init();//初始化TCB.so解析库文件；
            MyLog.i("MyCaptureActivity", " onCreate---初始化TCB.so解析库文件!");
            LeaveActivity.isclosetcb = false;
        }
        initView();
        startAnimation();
        setView();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case FIAL:
                        isdecode = true;
                        autofocus++;
                        if (camera != null && autofocus % 2 == 0) {
                            camera.autoFocus((AutoFocusCallback) new MyAutoFocusCallback());
                        }
                        break;
                    case SUCCESS:
                        String resutl = (String) msg.obj;
                        if (!TextUtils.isEmpty(resutl) && camera != null) {
                            String carnumber = resutl.substring(1);
                            Intent intent = new Intent(PosCaptureActivity.this, PosCheckNumberActivity.class);
                            intent.putExtra("number", carnumber);
                            intent.putExtra("from", getIntent().getStringExtra("from"));
                            startActivity(intent);
                            finish();
                        }
                        break;
                    case AUTOFOCUS:
                        if (camera != null) {
                            camera.autoFocus((AutoFocusCallback) new MyAutoFocusCallback());
                        }
                        break;
                }
            }
        };

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);
        surfaceHolder.addCallback(new MyCallback());

        thread = new DecodeThread(handler);


    }

    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.pos_capture_preview);
        bt_back = (Button) findViewById(R.id.bt_pos_back); // 返回
        bt_light = (Button) findViewById(R.id.bt_pos_light);// 开灯
        tv_warn = (TextView) findViewById(R.id.tv_pos_warn);// 提示语句
        bt_hand_input = (Button) findViewById(R.id.bt_pos_hand_input_carnumber);
//        cb_default = (CheckBox) findViewById(R.id.pos_cb_default);
//		mContainer = (RelativeLayout) findViewById(R.id.capture_containter);//全屏容器
//		mCropLayout = (RelativeLayout) findViewById(R.id.capture_crop_layout);//相框容器
    }

    public void setView() {
        bt_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                PosCaptureActivity.this.finish();
            }
        });
        bt_light.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (lightflag == true) {
                    lightflag = false;// 开闪光灯
                    openLight();
                } else {
                    lightflag = true;
                    offLight();// 关闪光灯
                }
            }
        });
        bt_hand_input.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 点击调用车牌输入法;
                //弃用旧版输入，直接关掉界面回上一层
//                Intent intent = new Intent(PosCaptureActivity.this,InCarDialogActivity.class);
//                intent.putExtra("handinput",true);
//                startActivity(intent);

                finish();
//				Intent intent  = new Intent(PosCaptureActivity.this, InputCarNumberActivity.class);
//				startActivity(intent);
//				PosCaptureActivity.this.finish();
            }
        });
//        cb_default.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//               SharedPreferences spf = getSharedPreferences("tingchebao", Context.MODE_PRIVATE);
//                if (isChecked){
//                    spf.edit().putBoolean("indefault",true).commit();
//                }else{
//                    spf.edit().putBoolean("indefault",false).commit();
//                }
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.w("MyCaptureActivity", "onDestroy---!");
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
//			manager.destroyAllMemery();
            MyLog.w("MyCaptureActivity", "onDestroy---释放相机资源!");
        }
    }


    // 相机预览回调
    private class MyCallback implements SurfaceHolder.Callback {

        @SuppressLint({"NewApi", "UseSparseArrays"})
        @Override
        public void surfaceCreated(SurfaceHolder holder) { // 在SurfaceView创建的时候就要进行打开摄像头、设置预览取景所在的SurfaceView、设置拍照的参数、开启预览取景等操作
            camera = getCameraInstance(); // 打开摄像头
            if (camera == null) {
                return;
            }
            try {
                camera.setDisplayOrientation(90);
                MyLog.w("MyCaptureActivity", "拍照--设置相机预览90度旋转");
                camera.setPreviewDisplay(holder);
                camera.setPreviewCallback(new mpcback());
                camera.startPreview();
                camera.autoFocus((AutoFocusCallback) new MyAutoFocusCallback());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) { // 当SurfaceView销毁时，我们进行停止预览、释放摄像机、垃圾回收等工作
            if (camera != null) {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.release();
                camera = null;
                MyLog.i("MyCaptureActivity", "surfaceDestroyed---释放相机资源!");
            }
        }
    }


    // 自动对焦回调
    @SuppressLint("NewApi")
    class MyAutoFocusCallback implements AutoFocusCallback {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            MyLog.i("MyCaptureActivity", "拍照--自动对焦------");
//			camera.cancelAutoFocus();
            // if (success) {
            // camera.takePicture(null, null, new MyPictureCallback());
            // }
        }
    }

    // 拍照回调
    // private class MyPictureCallback implements PictureCallback {
    //
    // @Override
    // public void onPictureTaken(byte[] data, Camera camer) {
    // DecodeThread thread = new DecodeThread(handler);
    // thread.decodeThread(data,null);
    // }
    // }

    // 实现每一帧图像的数据读取接口
    class mpcback implements Camera.PreviewCallback {
        public void onPreviewFrame(byte[] data, Camera camera) {

            if (isdecode) {
                isdecode = false;
                MyLog.i("MyCaptureActivity", "获取一张预览照片---标志位为：" + isdecode);

                Size size = camera.getParameters().getPreviewSize();
                MyLog.i("PreviewCallback", "data.length:" + data.length);
                YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                if (image != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compressToJpeg(new Rect(0, 0, size.width, size.height), 50, stream);
                    byte[] myByte = stream.toByteArray();
                    System.out.println("源bitemap的大小" + myByte.length);
                    Bitmap srcbmp = BitmapFactory.decodeByteArray(myByte, 0, stream.size());
                    if (srcbmp != null) {
                        MyLog.i("PreviewCallback", "源bitemap图片宽度" + srcbmp.getWidth() + "高度" + srcbmp.getHeight());
//						saveBitmap2file(srcbmp);
                        Bitmap catBmp = null;
                        if (srcbmp.getWidth() > srcbmp.getHeight()) {
                            int x = (int) (srcbmp.getWidth() * 0.4);
//							int y = (int) (srcbmp.getHeight()*0.1);
                            int mwidth = (int) (srcbmp.getWidth() * 0.2);
//							int mhight = (int) (srcbmp.getHeight()*0.9);
                            catBmp = Bitmap.createBitmap(srcbmp, x, 0, mwidth, srcbmp.getHeight());
                        } else {
                            MyLog.i("PreviewCallback", "原图片宽度小于高度！！！");
                            catBmp = Bitmap.createBitmap(srcbmp, 0, srcbmp.getHeight() / 3, srcbmp.getWidth(), srcbmp.getHeight() / 3);
                        }
                        MyLog.i("PreviewCallback", "剪切后图片宽度" + catBmp.getWidth() + "高度" + catBmp.getHeight());
                        bmp = turn(catBmp);
//						saveBitmap2file(catBmp);
                        MyLog.i("PreviewCallback", "旋转后图片宽度" + bmp.getWidth() + "高度" + bmp.getHeight());
//						saveBitmap2file(bmp);
                        thread.decodeThread(bmp);
                        try {
                            stream.close();
                            image = null;
                            myByte = null;
                            srcbmp.recycle();
                            catBmp.recycle();
                            srcbmp = null;
                            catBmp = null;
                        } catch (Exception ex) {
                            MyLog.i("PreviewCallback", "Error:" + ex.getMessage());
                        }
                    }
                }
            }
        }
    }


    public void startAnimation() {
        ImageView mQrLineView = (ImageView) findViewById(R.id.pos_capture_scan_line);// 开启扫描动画
        TranslateAnimation mAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE,
                0f, TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.9f);
        mAnimation.setDuration(1500);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        mQrLineView.setAnimation(mAnimation);
    }

    public void openLight() {
        if (camera != null) {
            parameters = camera.getParameters();
            parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
        }
    }

    public void offLight() {
        if (camera != null) {
            parameters = camera.getParameters();
            parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
        }
    }

    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            Toast.makeText(PosCaptureActivity.this, "相机被占用", Toast.LENGTH_SHORT).show();
        }
        return c; // returns null if camera is unavailable
    }


    // 向右旋转90度
    public static Bitmap turn(Bitmap img) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90); /* 翻转90度 */
        int width = img.getWidth();
        int height = img.getHeight();
        img = Bitmap.createBitmap(img, 0, 0, width, height, matrix, true);
        return img;
    }

//    //bitmap保存到文件；
//    @SuppressLint({"SdCardPath", "SimpleDateFormat"})
//    static boolean saveBitmap2file(Bitmap bmp) {
////		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
////		String date = sDateFormat.format(new java.util.Date());
//        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ATCBTest/";
////		Log.e("rootpath",":"+rootPath);
//        File root = new File(rootPath);
//        if (!root.exists()) {
//            root.mkdirs();
//        }
//        File f = new File(rootPath + "CarNumber" + System.currentTimeMillis() + ".jpeg");
//        if (f.exists()) {
//            f.delete();
//        }
//
//        CompressFormat format = CompressFormat.JPEG;
//        int quality = 100;
//        boolean compress = false;
//        OutputStream stream = null;
//        try {
//            f.createNewFile();
//            stream = new FileOutputStream(f);
//            compress = bmp.compress(format, quality, stream);
//            stream.close();
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            MyLog.w("decodeThread", "文件未找到异常！");
//            e.printStackTrace();
//        } catch (IOException e) {
//            MyLog.w("decodeThread", "文件保存异常--IOException！");
//        }
//
//        if (compress) {
//            MyLog.i("decodeThread", "已识别的照片文件保存成功！");
//        }
//        return compress;
//    }

}
