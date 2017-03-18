package com.zhenlaidian.engine;

/**
 * pos机入场对话框关闭时,leaveactivi通过此接口回调,弹出选择车位的对话框;
 * Created by zhangyunfei on 15/10/30.
 */
public interface SelectParkPositionListener {
    void doSelectParkPosition(String carmunber,String orderid);
}
