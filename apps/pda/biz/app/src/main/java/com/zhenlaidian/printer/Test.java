package com.zhenlaidian.printer;


import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.zsd.printer.JBluetoothEnCoder;
import com.zsd.printer.PrinterCtrl;

public class Test {

    private BarcodeGunCtrl mBarcodeGunCtrl = null;

    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;打印普通文字，注意打印\n换行符，可以出纸哦。
     *
     * @param view
     */
    public void onPrintTextBtn(View view) {


        new Thread() {
            public void run() {
                PrinterCtrl.powerOn();
                PrinterCtrl.PrintText("要打印的文字" + "\n\n\n\n");

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                }

            }

            ;
        }.start();

    }


    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;打印一维码。
     *
     * @param view
     */
    public void onPrintBarCodeBtn(View view) {



        new Thread() {
            public void run() {

                PrinterCtrl.powerOn();
                PrinterCtrl.PrintBarCode("要打印的文字", 300, 120, 0);

                PrinterCtrl.PrintText("\n\n\n\n");
            }

            ;
        }.start();


    }


    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;扫描一维码。
     *
     * @param view
     */
//    public void onScanBarcode(View view) {
//
//        new Thread() {
//            public void run() {
//
//                Handler handler = new Handler(Looper.getMainLooper());
//
//                BarcodeGunCtrl barcodeGunCtrl = new BarcodeGunCtrl();
//                final String strBarcode = barcodeGunCtrl.tryToReadBarcode();
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        EditText editText = (EditText) findViewById(R.id.barcodegunedit);
//
//                        if (editText != null) {
//                            editText.setText(strBarcode);
//                        }
//                    }
//                });
//
//
//            }
//
//            ;
//        }.start();
//
//    }

    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;打印二维码。
     *
     * @param view
     */
    public void onPrintQRCodeBtn(View view) {



        new Thread() {
            public void run() {

                PrinterCtrl.powerOn();
                PrinterCtrl.PrintQRCode("要打印的二维码文字", 200, 200, 0);//(strText, 300, 120, 0);

                PrinterCtrl.PrintText("\n\n\n\n");
            }

            ;
        }.start();

    }

    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;打印特殊字体。
     *
     * @param view
     */
    public void OnPrintSpacialTextBtn(View view) {

        new Thread() {
            public void run() {

                PrinterCtrl.powerOn();
                // 将字体编码后再打印。比如“你好”为1号字体，“我好”为2号字体。。。。。
                byte[] btTexts = JBluetoothEnCoder.EnCodeStringToPrintBytes("1, 2, 1", "你好\n", "我好\n", "他好\n");
                PrinterCtrl.PrintText(btTexts);

            }

            ;
        }.start();

    }

    //打印图片
    public void OnPrintImage(View v) {
        new Thread() {
            public void run() {
                PrinterCtrl.powerOn();
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                }
//				
//				BitmapDrawable bitmapDrawable = (BitmapDrawable) MainActivity.this.getResources().getDrawable(R.drawable.meinv);
//				
//				if(bitmapDrawable == null){
//					return ;
//				}
//				
//				bitmapDrawable.getBitmap();

//				BitmapDrawable bitmapDrawable = BitmapDrawable.createFromResourceStream(R.drawable.meinv, 0, 0, 0, 0);
//                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.meinv);
//                PrinterCtrl.WriteImageData(bmp, (byte) 0);
//                PrinterCtrl.PrintText("\n\n\n\n");

            }

            ;
        }.start();


    }

    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;二维码读取回调。
     */
    private IQRCodeReadCallBack mIqrCodeReadCallBack = new IQRCodeReadCallBack() {
        @Override
        public void onQRCodeReadSuccess(String strText) {
            System.out.println(strText);

//			EditText editText = (EditText) findViewById(R.id.qrcodetext);
//			
//			if(editText == null){
//				return ;
//			}
//			
//			editText.setText(strText);
        }
    };


    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;用于开始读取二维码。
     *
     * @param view 视图.
     */
    public void onStartQRCodeReadding(View view) {

        if (mBarcodeGunCtrl == null) {
            mBarcodeGunCtrl = new BarcodeGunCtrl();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                runUICode(new Runnable() {
                    @Override
                    public void run() {

//						Button btnScanQRCode = (Button) findViewById(R.id.scanqrcode);

//						if(btnScanQRCode == null){
//							return ;
//						}

//						findViewById(R.id.scanqrcode).setEnabled(false);

                    }
                });

                final String strQRCode = mBarcodeGunCtrl.tryToReadQRCode();

                if (strQRCode.trim().length() != 0) {
                    runUICode(new Runnable() {
                        @Override
                        public void run() {
//							EditText textEdit = (EditText) findViewById(R.id.qrcodetext);

//							if(textEdit != null){
//								textEdit.setText(strQRCode);
//							}
                        }
                    });
                }

                runUICode(new Runnable() {
                    @Override
                    public void run() {

//						Button btnScanQRCode = (Button) findViewById(R.id.scanqrcode);

//						if(btnScanQRCode == null){
//							return ;
//						}

//						findViewById(R.id.scanqrcode).setEnabled(true);
                    }
                });

            }
        }).start();


//		if(mBarcodeGunCtrl == null){
//			mBarcodeGunCtrl = new BarcodeGunCtrl();
//		}
//		
//		mBarcodeGunCtrl.startQRCodeRead(mIqrCodeReadCallBack);

    }

    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;用于在非UI线程里执行UI代码。
     *
     * @param runnable 需要执行的代码。
     */
    public static void runUICode(Runnable runnable) {

        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(runnable);
        }

    }


}
