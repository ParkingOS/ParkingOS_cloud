package com.zhenlaidian.printer;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.zhenlaidian.util.MyLog;
import com.zsd.printer.JBluetoothEnCoder;
import com.zsd.printer.PrinterCtrl;

/**
 * Created by zhangyunfei on 15/10/8.
 * 调用打印机的工具类.
 */
public class PrinterUitls {

//打印停车宝凭条用例;
//  TcbCheckCarIn info = new TcbCheckCarIn("京N88998","10,23 12:10","黄大锤");
//   TcbCheckCarOut info = new TcbCheckCarOut("京N44944","10-23 12:30","10-30 15:30","3小时","黄大锤","12.5元");
//   Bitmap qrbmp = new QRCodeEncoder().encode2BitMap("www.tingchebao.com", 200, 200);
//   Bitmap imgbmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon_check);
//  PrinterUitls.getInstance().printerTCBCheckCarIn(info, qrbmp, imgbmp);
//  PrinterUitls.getInstance().printerTCBCheckCarOut(info,qrbmp,imgbmp);

    private static PrinterUitls instance = new PrinterUitls();

    private PrinterUitls() {
    }

    public static PrinterUitls getInstance() {
        return instance;
    }

    /**
     * 打印停车宝账单凭条;
     *
     * @param info   要打印的文本
     * @param qrbmp  二维码图片
     * @param imgbmp 图片
     */
    public void printerTCBCheckCarIn(final TcbCheckCarIn info, final Bitmap qrbmp, final Bitmap imgbmp) {

        new Thread() {
            public void run() {
                PrinterCtrl.powerOn();
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (TextUtils.isEmpty(info.getCarnumber())) {
                    info.setCarnumber("车牌号未知");
                }
                byte[] btTexts = JBluetoothEnCoder.EnCodeStringToPrintBytes(info.style,
                        info.carin, info.partline + "\n" + "订单号:" + info.getOrderid() + "\n" + "车牌号:" + info.getCarnumber() + "\n" + "时间:" + info.getTime() + "\n" +
                                "收费员:" + info.getMeterman() + "\n" + info.partline);

                PrinterCtrl.PrintText(btTexts);//第一次打印纯文本

                try {
                    sleep(2300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (qrbmp != null) {
                    PrinterCtrl.WriteImageData(qrbmp, (byte) 70);//第二次打印图片

                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                PrinterCtrl.PrintText("\n       扫码支付 告别零钱");//第三次打印换行

                try {
                    sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (imgbmp != null) {
                    PrinterCtrl.WriteImageData(imgbmp, (byte) 70);//第四次打印图片

                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                PrinterCtrl.PrintText("" + "\n\n\n\n");//第五次打印换行

                try {
                    if (qrbmp != null) {
                        qrbmp.recycle();
                    }
                    if (imgbmp != null) {
                        imgbmp.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void printerTCBCheckCarOut(final TcbCheckCarOut info, final Bitmap qrbmp, final Bitmap imgbmp) {
        new Thread() {
            public void run() {
                PrinterCtrl.powerOn();
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                byte[] btTexts = JBluetoothEnCoder.EnCodeStringToPrintBytes(info.style,
                        info.carout, info.partline + "\n" + "订单号:" + info.getOrderid() + "\n" + "车牌号:" + info.getCarnumber() + "\n" + "入场:" + info.getIntime() + "\n" +
                                "出场:" + info.getOuttime() + "\n" + "停车时长:" + info.getDuration() + "\n" + "收费员:" + info.getMeterman() +
                                "\n" + info.partline + "\n", "停车费:" + info.getColloct() + "\n", info.partline);


                PrinterCtrl.PrintText(btTexts);//第一次打印纯文本

                if (qrbmp != null) {
                    try {
                        sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                    PrinterCtrl.WriteImageData(qrbmp, (byte) 100);//第二次打印图片

                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                    PrinterCtrl.PrintText("\n        扫码支付 告别零钱");//第三次打印换行
                }

                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                if (imgbmp != null){
                    PrinterCtrl.WriteImageData(imgbmp, (byte) 100);//第四次打印图片

                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                PrinterCtrl.PrintText("" + "\n\n\n\n");//第五次打印换行
                try {
                    if (qrbmp != null) {
                        qrbmp.recycle();
                    }
                    if (imgbmp != null) {
                        imgbmp.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    /**
     * 打印纯文本
     */
    public void printerText(final String text) {
        new Thread() {
            public void run() {
                PrinterCtrl.powerOn();
                PrinterCtrl.PrintText(text + "\n\n\n\n");
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    MyLog.e("PrinterUitls", "打印纯文本出错!!!");
                }
            }
        }.start();
    }

    /**
     * 打印二维码。
     */
    public void printerQRCode(final String qrcode) {
        new Thread() {
            public void run() {
                PrinterCtrl.powerOn();
                PrinterCtrl.PrintQRCode(qrcode, 200, 200, 0);//(strText, 300, 120, 0);
                PrinterCtrl.PrintText("\n\n\n\n");
            }
        }.start();
    }

    /**
     * 打印特殊字体。
     */
    public void printerSpecialText() {

        new Thread() {
            public void run() {

                PrinterCtrl.powerOn();
                // 将字体编码后再打印。比如“你好”为1号字体，“我好”为2号字体。。。。。
                byte[] specialText = JBluetoothEnCoder.EnCodeStringToPrintBytes("1, 2, 1", "你好\n", "我好\n", "他好\n");
                PrinterCtrl.PrintText(specialText);

            }

            ;
        }.start();

    }

    //打印图片
    public void printImage(final Bitmap bmp) {
        new Thread() {
            public void run() {
                PrinterCtrl.powerOn();
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//				BitmapDrawable bitmapDrawable = BitmapDrawable.createFromResourceStream(R.drawable.meinv, 0, 0, 0, 0);
//                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.meinv);
                PrinterCtrl.WriteImageData(bmp, (byte) 0);
                PrinterCtrl.PrintText("\n\n\n\n");
            }
        }.start();
    }

}
