package com.zhenlaidian.decode;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.zhenlaidian.photo.DecodeManager;
import com.zhenlaidian.util.MyLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * 在子线程中处理解析车牌的工作;
 */
public class DecodeThread {

    private Handler handler;
    private final int FIAL = 999;
    private final int SUCCESS = 000;
    byte[] bytes = null;


    public DecodeThread() {
        super();
        // TODO Auto-generated constructor stub
    }

    public DecodeThread(Handler handler) {
        super();
        this.handler = handler;
    }

    public void decodeThread(final Bitmap bitmap2) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                makeImage(bitmap2);
                saveBitmap2file(bitmap2);
                byte[] resultbytes = DecodeManager.getinstance().decode(bytes, bitmap2.getWidth(), bitmap2.getHeight(), 11, 22, 55, 66);
                String result = readImageData(resultbytes);
                MyLog.i("解析结果是：", "result=" + result + "----!!!!!!!");

                if (result.equals("N")) {
                    Message msg = new Message();
                    msg.what = FIAL;
                    handler.sendMessage(msg);
                } else {
                    boolean issave = saveBitmap2file(bitmap2);
                    if (!bitmap2.isRecycled()) {
                        bitmap2.recycle();//回收图片所占的内存
                    }
                    if (issave) {
                        Message msg = new Message();
                        msg.what = SUCCESS;
                        msg.obj = result;
                        handler.sendMessage(msg);
                    } else {
                        Message msg = new Message();
                        msg.what = FIAL;
                        handler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }

    public void makeImage(Bitmap bitmap) {
        int i = 0;
        int width, height;
        width = bitmap.getWidth();
        height = bitmap.getHeight();

        if (bytes == null) {
            bytes = new byte[(width * 3 + 3) / 4 * 4 * height];
        }

        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                bytes[i++] = (byte) (pixel);
                bytes[i++] = (byte) (pixel >> 8);
                bytes[i++] = (byte) (pixel >> 16);
            }
        }
    }

    //bitmap保存到文件；
    @SuppressLint("SdCardPath")
    static boolean saveBitmap2file(Bitmap bmp) {
        MyLog.i("decodeThread", "开始保存文件！");
//		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");       
//		String date = sDateFormat.format(new java.util.Date());    
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ATingCheBao/";
        File root = new File(rootPath);
        if (!root.exists()) {
            root.mkdirs();
        }
        File f = new File(rootPath + System.currentTimeMillis()+"CarNumber.jpeg");
        if (f.exists()) {
            f.delete();
        }

        CompressFormat format = Bitmap.CompressFormat.JPEG;
        int quality = 100;
        boolean compress = false;
        OutputStream stream = null;
        try {
            f.createNewFile();
            stream = new FileOutputStream(f);
            compress = bmp.compress(format, quality, stream);
            stream.flush();
            stream.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            MyLog.i("decodeThread", "文件未找到异常！");
            e.printStackTrace();
        } catch (IOException e) {
            MyLog.i("decodeThread", "文件输出流关闭异常！");
        }

        if (compress) {
            MyLog.i("decodeThread", "已识别的照片文件保存成功！");
        }
        return compress;
    }

    //读取照片数据中的返回结果；读到的结果转码后返回String类型结果；
    private String readImageData(byte[] bytes) {
        int i;
        for (i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0) {
                break;
            }
        }

        byte[] temp = new byte[i];

        for (int j = 0; j < temp.length; j++) {
            temp[j] = bytes[j];
            MyLog.i("读到的车牌temp[i]：", temp[j] + "");
        }
        String result = "FAIL";
        try {
            result = new String(temp, "gbk");// 这里写转换后的编码方式
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    //缩放bitmap
    public Bitmap CenterBitmap(Bitmap bitmap, int edgeLength) {
        Bitmap result = bitmap;
        int xTopLeft = (bitmap.getWidth() - edgeLength) / 2;
        int yTopLeft = (bitmap.getHeight() - edgeLength) / 2;
        result = Bitmap.createBitmap(bitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
        return result;
    }

}
