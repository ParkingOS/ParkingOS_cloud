package com.zhenlaidian.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * Created by TCB on 2016/4/28.
 */
public class PrintUtil {

    public static boolean conn2bluetooth(BluetoothService mService, Set<BluetoothDevice> pairedDevices,
                                         BluetoothAdapter bluetoothAdapter, Handler mHandler) {

        System.out.println("获取设备");
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
//                bluetoothAdapter.add(device.getName() + "\n" + device.getAddress());
//                System.out.println(device.getName() + "\n" + device.getAddress());
                Message m = new Message();
                m.what = 1001;
                m.obj = device;
                mHandler.sendMessage(m);
                System.out.println("获取并发送消息");
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 打印
     *
     * @param message
     */
    public static void sendMessage(BluetoothService mService, String message) {
        // Check that we're actually connected before trying anything
//        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
//            Toast.makeText(this, "蓝牙没有连接", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // Check that there's actually something to send
//        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothService to write
        if(CommontUtils.checkString(message)){
            byte[] send;
            try {
                send = message.getBytes("GB2312");
            } catch (UnsupportedEncodingException e) {
                send = message.getBytes();
            }
            mService.printLeft();
            mService.write(send);
        }
    }


    public static void sendMessage(BluetoothService mService, Bitmap bitmap, Context context) {
        // Check that we're actually connected before trying anything
        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(context, "蓝牙没有连接", Toast.LENGTH_SHORT).show();
            return;
        }
        // 发送打印图片前导指令
        byte[] start = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1B,
                0x40, 0x1B, 0x33, 0x00};
        mService.write(start);

        /**获取打印图片的数据**/
        mService.printCenter();
//      byte[] draw2PxPoint = PicFromPrintUtils.draw2PxPoint(bitmap);
        byte[] draw2PxPoint = PicFromPrintUtils.bitToByte(bitmap);
        mService.write(draw2PxPoint);

        // 发送结束指令
        byte[] end = {0x1d, 0x4c, 0x1f, 0x00};
        mService.write(end);
        sendMessage(mService, "\n\n\n");
    }
}
