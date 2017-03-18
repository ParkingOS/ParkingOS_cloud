package com.zhenlaidian.util;

import android.device.PiccManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.rsk.api.PublicMethod;
import com.rsk.api.RskApi;
import com.zhenlaidian.ui.dialog.PayCardDialog;

/**
 * Created by xulu on 2016/11/29.
 */
public class ReadCardUtil {
    public static boolean isread;
    public static PiccManager piccReader;
    public static void InitReader() {
        RskApi.PiccOpen();
    }

    public static void StopReading() {
        isread = true;
        if (CommontUtils.Is900()) {
            RskApi.PiccClose();
        }
    }
    public static void StartReadCard( final Handler mHandler) {
        if (!TextUtils.isEmpty(CommontUtils.PhoneModel())) {
            if (CommontUtils.Is910()) {
                isread = false;
                piccReader = new PiccManager();
                piccReader.open();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (!isread) {
                            byte CardType[] = new byte[2];
                            byte Atq[] = new byte[14];
                            char SAK = 1;
                            byte sak[] = new byte[1];
                            sak[0] = (byte) SAK;
                            byte SN[] = new byte[10];
                            int scan_card = piccReader.request(CardType, Atq);
                            System.out.println("----------------------scan_card" + scan_card);
                            if (scan_card > 0) {
                                int SNLen = piccReader.antisel(SN, sak);
//                            Log.d("prepayparking", "SNLen = " + SNLen);
                                Message msg = mHandler.obtainMessage(Constant.MSG_FOUND_UID);
                                msg.obj = CommontUtils.bytesToHexString(SN, SNLen);
                                mHandler.sendMessage(msg);
                            }
                            try {
                                Thread.sleep(300);
//                            System.out.println("睡300ms");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        piccReader.close();
                    }
                }.start();
            } else if (CommontUtils.PhoneModel().equals("Pe")) {
                isread = false;
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (!isread) {
                            byte[] CardType = new byte[1];
                            int mode = 0;
                            byte[] mSerialNo = new byte[50];
                            int[] mseralNoLen = new int[1];
                            System.out.println("run");
                            int nResult = RskApi.PiccCheck((byte) mode, CardType, mSerialNo, mseralNoLen);
                            System.out.println("结果：" + nResult);
                            if (nResult == 0) {
                                System.out.println("独到了：" + nResult);
                                Message msg = mHandler.obtainMessage(Constant.MSG_FOUND_UID);
                                //睿思科900pe读出来的卡号是大写，转为小写
                                msg.obj = PublicMethod.bytesToHexString(mSerialNo, 0, mseralNoLen[0]).toLowerCase();
                                mHandler.sendMessage(msg);
                            }
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
        }
    }
    public static void StartReadCard(PayCardDialog cardDialog, final Handler mHandler) {
        if (!TextUtils.isEmpty(CommontUtils.PhoneModel())) {
            if (CommontUtils.Is910()) {
                cardDialog.show();
                isread = false;
                piccReader = new PiccManager();
                piccReader.open();
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (!isread) {
                            byte CardType[] = new byte[2];
                            byte Atq[] = new byte[14];
                            char SAK = 1;
                            byte sak[] = new byte[1];
                            sak[0] = (byte) SAK;
                            byte SN[] = new byte[10];
                            int scan_card = piccReader.request(CardType, Atq);
                            System.out.println("----------------------scan_card" + scan_card);
                            if (scan_card > 0) {
                                int SNLen = piccReader.antisel(SN, sak);
//                            Log.d("prepayparking", "SNLen = " + SNLen);
                                Message msg = mHandler.obtainMessage(Constant.MSG_FOUND_UID);
                                msg.obj = CommontUtils.bytesToHexString(SN, SNLen);
                                mHandler.sendMessage(msg);
                                isread = true;
                            }
                            try {
                                Thread.sleep(300);
//                            System.out.println("睡300ms");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        piccReader.close();
                    }
                }.start();
            } else if (CommontUtils.PhoneModel().equals("Pe")) {
                cardDialog.show();
                isread = false;
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (!isread) {
                            byte[] CardType = new byte[1];
                            int mode = 0;
                            byte[] mSerialNo = new byte[50];
                            int[] mseralNoLen = new int[1];
                            System.out.println("run");
                            int nResult = RskApi.PiccCheck((byte) mode, CardType, mSerialNo, mseralNoLen);
                            System.out.println("结果：" + nResult);
                            if (nResult == 0) {
                                System.out.println("独到了：" + nResult);
                                Message msg = mHandler.obtainMessage(Constant.MSG_FOUND_UID);
                                //睿思科900pe读出来的卡号是大写，转为小写
                                msg.obj = PublicMethod.bytesToHexString(mSerialNo, 0, mseralNoLen[0]).toLowerCase();
                                mHandler.sendMessage(msg);
                                isread = true;
                            }
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
        }
    }


}
