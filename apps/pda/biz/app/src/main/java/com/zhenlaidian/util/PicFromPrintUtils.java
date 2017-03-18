package com.zhenlaidian.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 将图片转化为二进制
 * @author xulu
 * 2016.4.17
 */
public class PicFromPrintUtils {
     
     
    /*************************************************************************
     * 我们的热敏打印机是RP-POS80S或RP-POS80P或RP-POS80CS或RP-POS80CP打印机
     * 360*360的图片，8个字节（8个像素点）是一个二进制，将二进制转化为十进制数值
     * y轴：24个像素点为一组，即360就是15组（0-14）
     * x轴：360个像素点（0-359）
     * 里面的每一组（24*360），每8个像素点为一个二进制，（每组有3个，3*8=24）
     *
     * 14、ESC * m nL nH d1... dk 选择位图模式
     [格式] ASCII码   ESC   *  m nL nH d1...dk
     十六进制码  1B  2A  m nL nH d1...dk
     十进制码	27  42  m nL nH d1...dk
     [范围] m = 0, 1, 32, 33
     0 ≤ nL ≤ 255
     0 ≤ nH ≤ 3	0 ≤ d ≤255
     • 如果m的值超出规定范围，nL 和其后的数据被作为普通数据处理。
     • 横向打印点数由nL和nH决定，总的点数为 nL + nH × 256。
     • 位图超出当前区域的部分被截掉。
     • d 是位图的数据。数据各个位为1则打印这个点，为0不打印。
     • 位图数据发送完成后，打印机返回普通数据处理模式。
     • 除了倒置模式，这条命令不受其它打印模式影响 (加粗、双重打印、下划线、字符放 大和反显)。
     • 如果用GS L 和GS W 设置的打印范围的宽度比用ESC *命令发送的数据所要求的宽度小 时， 则对有问题的行执行下列操作（但是打印不能超出最大可打印范围）：
     1、打印区域的宽度向右扩展以容纳数据量。
     2、如果步骤¬不能为数据提供足够的宽度，那么左边缘就被减少以容纳数据。
     **************************************************************************/
    /**
     * 把一张Bitmap图片转化为打印机可以打印的bit(将图片压缩为360*360)
     * 效率很高（相对于下面）
     * @param bit
     * @return
     * 240*10*3+10*6
     *
     */
    private static int Hline;
    public static byte[] draw2PxPoint(Bitmap bit) {
        if (bit.getHeight()%24 == 0){
            Hline = bit.getHeight()/24;
        }else{
            Hline = bit.getHeight()/24+1;
        }
        byte[] data = new byte[(bit.getWidth()*3+5)*Hline];
//        byte[] data = new byte[16290];
        int k = 0;
        System.out.println("bit.getHeight()="+bit.getHeight()+"-------bit.getWidth()="+bit.getWidth());
        for (int j = 0; j < Hline; j++) {
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33; // m=33时，选择24点双密度打印，分辨率达到200DPI。
            if(bit.getWidth()>255){
                data[k++] = (byte)(bit.getWidth()-256);
                data[k++] = 0x01;
            }else{
                data[k++] = (byte)bit.getWidth();
                data[k++] = 0x00;
            }
            for (int i = 0; i < bit.getWidth(); i++) {
                for (int m = 0; m < 3; m++) {
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(i, j * 24 + m * 8 + n, bit);
                        data[k] += data[k] + b;
                    }
                    k++;
                }
            }
//            data[k++] = 10;
        }
        return data;
    }
    /**
     * 把一张Bitmap图片转化为打印机可以打印的bit
     * @param bit
     * @return
     * 360*15*3+15*6
     */
    public static byte[] bitToByte(Bitmap bit){
        byte[] data = new byte[7250];
        int k = 0;
        for (int j = 0; j < 10; j++) {
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33; // m=33时，选择24点双密度打印，分辨率达到200DPI。
            data[k++] = (byte) 240;
            data[k++] = 0x00;
            for (int i = 0; i < 240; i++) {
                for (int m = 0; m < 3; m++) {
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(i, j * 24 + m * 8 + n, bit);
                        data[k] += data[k] + b;
                    }
                    k++;
                }
            }
//            data[k++] = 10;
        }
        return data;
    }
    /**
     * 把一张Bitmap图片转化为打印机可以打印的bit
     * @param bit
     * @return
     * 360*15*3+15*6
     */
    public static byte[] pic2PxPoint(Bitmap bit){
        long start = System.currentTimeMillis();
        byte[] data = new byte[16290];
        int k = 0;
        for (int i = 0; i < 15; i++) {
            data[k++] = 0x1B;
            data[k++] = 0x2A;
            data[k++] = 33; // m=33时，选择24点双密度打印，分辨率达到200DPI。
//            data[k++] = 0x68;
//            data[k++] = 0x01;
            data[k++] = -128;
            data[k++] = 0;
            for (int x = 0; x < 360; x++) {
                for (int m = 0; m < 3; m++) {
                    byte[]  by = new byte[8];
                    for (int n = 0; n < 8; n++) {
                        byte b = px2Byte(x, i * 24 + m * 8 +7-n, bit);
                        by[n] = b;
                    }
                    data[k] = (byte) changePointPx1(by);
                    k++;
                }
            }
            data[k++] = 10;
        }
        long end = System.currentTimeMillis();
        long str = end - start;
        Log.i("TAG", "str:" + str);
        return data;
    }
     
    /**
     * 图片二值化，黑色是1，白色是0
     * @param x  横坐标
     * @param y     纵坐标           
     * @param bit 位图
     * @return
     */
    public static byte px2Byte(int x, int y, Bitmap bit) {
        byte b;
//        System.out.println("x="+x+"||||bit.width"+bit.getWidth());
        int pixel = bit.getPixel(x, y);
        int red = (pixel & 0x00ff0000) >> 16; // 取高两位
        int green = (pixel & 0x0000ff00) >> 8; // 取中两位
        int blue = pixel & 0x000000ff; // 取低两位
        int gray = RGB2Gray(red, green, blue);
        if ( gray < 128 ){
            b = 1;
        } else {
            b = 0;
        }
        return b;
    }
     
    /**
     * 图片灰度的转化
     * @param r  
     * @param g
     * @param b
     * @return
     */
    private static int RGB2Gray(int r, int g, int b){
        int gray = (int) (0.29900 * r + 0.58700 * g + 0.11400 * b);  //灰度转化公式
        return  gray;
    }
     
    /**
     * 对图片进行压缩（去除透明度）
     * @param bitmapOrg
     */
    public static Bitmap compressPic(Bitmap bitmapOrg) {
        // 获取这个图片的宽和高
        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();
        // 定义预转换成的图片的宽度和高度
        int newWidth = 360;
        int newHeight = 360;
        Bitmap targetBmp = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);  
        Canvas targetCanvas = new Canvas(targetBmp);
        targetCanvas.drawColor(0xffffffff);
        targetCanvas.drawBitmap(bitmapOrg, new Rect(0, 0, width, height), new Rect(0, 0, newWidth, newHeight), null);
        return targetBmp;
    }
     
     
    /**
     * 对图片进行压缩(不去除透明度)
     * @param bitmapOrg
     */
    public static Bitmap compressBitmap(Bitmap bitmapOrg) {
        // 加载需要操作的图片，这里是一张图片
//        Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(),R.drawable.alipay);
        // 获取这个图片的宽和高
        int width = bitmapOrg.getWidth();
        int height = bitmapOrg.getHeight();
        // 定义预转换成的图片的宽度和高度
        int newWidth = 360;
        int newHeight = 360;
        // 计算缩放率，新尺寸除原始尺寸
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width,height, matrix, true);
        // 将上面创建的Bitmap转换成Drawable对象，使得其可以使用在ImageView, ImageButton中
//        BitmapDrawable bmd = new BitmapDrawable(resizedBitmap);
        return resizedBitmap;
    }
     
    /**
     * 将[1,0,0,1,0,0,0,1]这样的二进制转为化十进制的数值（效率更高）
     * @param arry
     * @return
     */
    public static int changePointPx1(byte[] arry){
        int v = 0;
        for (int j = 0; j <arry.length; j++) {
            if( arry[j] == 1) {
                v = v | 1 << j;
            }
        }
        return v;
    }
     
    /**
     * 将[1,0,0,1,0,0,0,1]这样的二进制转为化十进制的数值
     * @param arry
     * @return
     */
    public byte changePointPx(byte[] arry){
        byte v = 0;
        for (int i = 0; i < 8; i++) {
            v += v + arry[i];
        }
        return v;
    }
     
    /**
     * 得到位图的某个点的像素值
     * @param bitmap
     * @return
     */
    public byte[] getPicPx(Bitmap bitmap){
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];// 保存所有的像素的数组，图片宽×高
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < pixels.length; i++) {
            int clr = pixels[i];
            int red = (clr & 0x00ff0000) >> 16; // 取高两位
        int green = (clr & 0x0000ff00) >> 8; // 取中两位
                int blue = clr & 0x000000ff; // 取低两位
                System.out.println("r=" + red + ",g=" + green + ",b=" + blue);
        }
        return null;
    }


    /**
     * 使用光栅位图打印
     *
     * @return 字节
     */
    public static byte[] printDraw(Bitmap bm) {

        byte[] imgbuf = new byte[bm.getWidth() / 8 * bm.getHeight() + 8];
        bitbuf = new byte[bm.getWidth() / 8];
        int s = 0;

        // 打印光栅位图的指令
        imgbuf[0] = 29;// 十六进制0x1D
        imgbuf[1] = 118;// 十六进制0x76
        imgbuf[2] = 48;// 30
        imgbuf[3] = 0;// 位图模式 0,1,2,3
        // 表示水平方向位图字节数（xL+xH × 256）
        imgbuf[4] = (byte) (bm.getWidth() / 8);
        imgbuf[5] = 0;
        // 表示垂直方向位图点数（ yL+ yH × 256）
        imgbuf[6] = (byte) (bm.getHeight() % 256);//
        imgbuf[7] = (byte) (bm.getHeight() / 256);

        s = 7;
        for (int i = 0; i < bm.getHeight(); i++) {// 循环位图的高度
            for (int k = 0; k < bm.getWidth() / 8; k++) {// 循环位图的宽度
                int c0 = bm.getPixel(k * 8 + 0, i);// 返回指定坐标的颜色
                int p0;
                if (c0 == -1)// 判断颜色是不是白色
                    p0 = 0;// 0,不打印该点
                else {
                    p0 = 1;// 1,打印该点
                }
                int c1 = bm.getPixel(k * 8 + 1, i);
                int p1;
                if (c1 == -1)
                    p1 = 0;
                else {
                    p1 = 1;
                }
                int c2 = bm.getPixel(k * 8 + 2, i);
                int p2;
                if (c2 == -1)
                    p2 = 0;
                else {
                    p2 = 1;
                }
                int c3 = bm.getPixel(k * 8 + 3, i);
                int p3;
                if (c3 == -1)
                    p3 = 0;
                else {
                    p3 = 1;
                }
                int c4 = bm.getPixel(k * 8 + 4, i);
                int p4;
                if (c4 == -1)
                    p4 = 0;
                else {
                    p4 = 1;
                }
                int c5 = bm.getPixel(k * 8 + 5, i);
                int p5;
                if (c5 == -1)
                    p5 = 0;
                else {
                    p5 = 1;
                }
                int c6 = bm.getPixel(k * 8 + 6, i);
                int p6;
                if (c6 == -1)
                    p6 = 0;
                else {
                    p6 = 1;
                }
                int c7 = bm.getPixel(k * 8 + 7, i);
                int p7;
                if (c7 == -1)
                    p7 = 0;
                else {
                    p7 = 1;
                }
                int value = p0 * 128 + p1 * 64 + p2 * 32 + p3 * 16 + p4 * 8
                        + p5 * 4 + p6 * 2 + p7;
                bitbuf[k] = (byte) value;
            }

            for (int t = 0; t < bm.getWidth() / 8; t++) {
                s++;
                imgbuf[s] = bitbuf[t];
            }
        }
        if (null != bm) {
            bm.recycle();
            bm = null;
        }
        return imgbuf;
    }
    public  static byte[]  bitbuf = null;


}