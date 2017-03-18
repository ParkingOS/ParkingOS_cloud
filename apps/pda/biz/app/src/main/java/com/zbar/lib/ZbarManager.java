package com.zbar.lib;

/**
 * 调用本地jni方法来解析二维码信息;
 */
public class ZbarManager {

    static {
        System.loadLibrary("zbar");
    }

    public native String decode(byte[] data, int width, int height, boolean isCrop, int x, int y, int cwidth, int cheight);
}
