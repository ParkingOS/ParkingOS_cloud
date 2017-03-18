package com.zhenlaidian.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.zhenlaidian.ui.BaseActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by TCB on 2016/4/26.
 * xulu
 * 用于项目中 生成、结算、逃单，拍照压缩工具类
 */
public class CameraBitmapUtil {
    /**
     * @param bmp
     * @param filename
     * @return
     */
    public static boolean saveBitmap2file(Bitmap bmp, String filename) {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
//        int quality = 100;
//        if (!CommontUtils.Is910()) {
           int quality = 60;
//        }
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bmp.compress(format, quality, stream);
    }


    /**
     * 把照片文件压缩后转为bitmap；
     *
     * @param dst
     * @param width
     * @param height
     * @return
     */
    public static Bitmap getBitmapFromFile(File dst, int width, int height) {
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
                FileOutputStream fos = new FileOutputStream(dst);
                int quality = 60;
//                if (CommontUtils.Is910()) {
//                    quality = 100;
//                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                return bitmap;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * http://127.0.0.1/zld/collectorrequest.do?action=uporderpic
     * &token=ca67649c7a6c023e08b0357658c08c3d&orderid=&type=&currentnum=
     * type 0进场，1出场，2置为逃单
     * currentnum 当前张数
     */
    //上传图片到服务器；
    public static void upload(Context context, int num, String uporderid, int uptypes) {
        if (!IsNetWork.IsHaveInternet(context)) {
            Toast.makeText(context, "请检查网络是否连接！", Toast.LENGTH_SHORT).show();
            return;
        }
        String type;
        if (uptypes == 0) {
            type = "in";
        } else if (uptypes == 1) {
            type = "out";
        } else {
            type = "esc";
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("filename", Environment.getExternalStorageDirectory() + "/TingCheBao/" + uporderid + type + num + ".jpeg");
        map.put("url", BaseActivity.baseurl + "collectorrequest.do?action=uporderpic&token=" + BaseActivity.token +
                "&orderid=" + uporderid + "&type=" + uptypes + "&currentnum=" + num);
        Files files = new Files();
        files.setCurrentnum(num);
        files.setFilename(Environment.getExternalStorageDirectory() + "/TingCheBao/" + uporderid + type + num + ".jpeg");
        files.setUrl(BaseActivity.baseurl + "collectorrequest.do?action=uporderpic&token=" + BaseActivity.token +
                "&orderid=" + uporderid + "&type=" + uptypes + "&currentnum=" + num);
        myThread thread = new myThread(files);
        System.out.println("----------开启上传线程");
        thread.start();

    }

    /**
     * http://127.0.0.1/zld/collectorrequest.do?action=uporderpic
     * &token=ca67649c7a6c023e08b0357658c08c3d&orderid=&type=&currentnum=
     * type 0进场，1出场，2置为逃单
     * currentnum 当前张数
     */
    //上传图片到服务器；
//    public static void uploadout(Context context, int num,String up,int uptypes) {
//        if (!IsNetWork.IsHaveInternet(context)) {
//            Toast.makeText(context, "请检查网络是否连接！", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        nownum = num;
//        uporder = up;
//        uptype = uptypes;
//        System.out.println("----------正在上传");
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                String path = BaseActivity.baseurl;
//                String url = path + "collectorrequest.do?action=uporderpic&token=" + BaseActivity.token +
//                        "&orderid=" + uporder + "&type="+uptype+"&currentnum="+nownum;
//                String result = UploadUtil.uploadFile(Environment.getExternalStorageDirectory() + "/TingCheBao/" + uporder + "out"+nownum+".jpeg", url);
//                MyLog.i("TakePhotoUpdateActivity", "上传照片的返回结果是-->>" + result);
//                String isok = "";
//                try {
//                    JSONObject json = new JSONObject(result);
//                    isok = json.getString("result");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                if (isok.equals("1")) {
//                    System.out.println("----------成功");
//                } else {
//                    Looper.prepare();
//                    System.out.println("----------失败");
//                }
//
//            }
//        }.start();
//
//    }


    private static int times0 = 0;
    private static int times1 = 0;
    private static int times2 = 0;
    private static HashMap<String, String> map0 = new HashMap<String, String>();
    private static HashMap<String, String> map1 = new HashMap<String, String>();
    private static HashMap<String, String> map2 = new HashMap<String, String>();
    private static Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MyLog.i("TakePhotoUpdateActivity", "msg.what=" + msg.what);
            switch (msg.what) {
                case 0:
                    map0 = (HashMap<String, String>) msg.obj;
                    if (times0 < Constant.retryTimes) {
                        Files files0 = new Files();
                        files0.setCurrentnum(0);
                        files0.setUrl(map0.get("filename"));
                        files0.setFilename(map0.get("url"));
                        myThread thread0 = new myThread(files0);
                        thread0.start();
                        times0++;
                    }
                    break;
                case 1:
                    map1 = (HashMap<String, String>) msg.obj;
                    if (times1 < Constant.retryTimes) {
                        Files files1 = new Files();
                        files1.setCurrentnum(1);
                        files1.setUrl(map1.get("filename"));
                        files1.setFilename(map1.get("url"));
                        myThread thread1 = new myThread(files1);
                        thread1.start();
                        times1++;
                    }
                    break;
                case 2:
                    map2 = (HashMap<String, String>) msg.obj;
                    if (times2 < Constant.retryTimes) {
                        Files files2 = new Files();
                        files2.setCurrentnum(2);
                        files2.setUrl(map2.get("filename"));
                        files2.setFilename(map2.get("url"));
                        myThread thread2 = new myThread(files2);
                        thread2.start();
                        times2++;
                    }
                    break;
            }
        }
    };

    static class Files {
        String filename;
        String url;
        int currentnum;

        public int getCurrentnum() {
            return currentnum;
        }

        public void setCurrentnum(int currentnum) {
            this.currentnum = currentnum;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    static class myThread extends Thread {
        Files files;

        public myThread(Files files) {
            this.files = files;
        }

        @Override
        public void run() {
            super.run();
            String result = UploadUtil.uploadFile(files.getFilename(), files.getUrl());
//                String result = UploadUtil.uploadFile(uporder, url);
            MyLog.i("TakePhotoUpdateActivity", "上传照片的URL-->>" + files.getFilename() + ">>>>" + files.getUrl());
            MyLog.i("TakePhotoUpdateActivity", "上传照片的返回结果是-->>" + result);
            String isok = "";
            try {
                JSONObject json = new JSONObject(result);
                isok = json.getString("result");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (isok.equals("1")) {
                System.out.println("----------成功");
            } else {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("filename", files.getFilename());
                map.put("url", files.getUrl());
                Message m = new Message();
                m.what = files.getCurrentnum();
                m.obj = map;
                h.sendMessage(m);
                System.out.println("----------失败");
            }

        }
    }

    /**
     * 照片添加水印
     *
     * @param src
     * @param water
     * @param context
     * @return
     */
    public static Bitmap addWaterMark(Bitmap src, String water, Context context) {
        Bitmap tarBitmap = src.copy(Bitmap.Config.ARGB_8888, true);
        int w = tarBitmap.getWidth();
        int h = tarBitmap.getHeight();
        Canvas canvas = new Canvas(tarBitmap);
        //启用抗锯齿和使用设备的文本字距
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
        //字体的相关设置
        if (CommontUtils.IsSunMi()) {
            textPaint.setTextSize(23);//字体大小
        } else {
            textPaint.setTextSize(30);//字体大小
        }
//        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setColor(Color.RED);
        textPaint.setShadowLayer(3f, 1, 1, context.getResources().getColor(android.R.color.background_dark));
        //图片上添加水印的位置，这里设置的是中下部3/4处
        canvas.drawText(water, w / 2 - 60, (float) (h * 0.1), textPaint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return tarBitmap;
    }

}
