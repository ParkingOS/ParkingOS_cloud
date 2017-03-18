package com.zhenlaidian.util;

import com.zhenlaidian.bean.BoWeiStateEntity;
import com.zhenlaidian.bean.InVehicleInfo;

import java.util.ArrayList;

/**
 * Created by TCB on 2016/4/21.
 * xulu
 */
public class Constant {
    /**
     * 微信支付轮询得到的消息发送给orderjiesuanactivity
     */
    public static final int WxCode = 101001;
    /**
     * 存储指定的activity
     */
//    public static List<Activity> listactivity = new ArrayList<Activity>();
    /**
     * 照相时不调用守护程序的开关
     */
    public static boolean ISNEEDBACKUP = true;
    /**
     * 存储首页的地磁状态消息
     */
    public static ArrayList<BoWeiStateEntity> boweiMsg = new ArrayList<BoWeiStateEntity>();
    /**
     * 存储所有的泊位
     */
    public static ArrayList<InVehicleInfo> boweiMsgAll = new ArrayList<InVehicleInfo>();
    /**
     * 存储重连打印机 打印小票的次数
     */
    public static final int TRYCOUNT = 3;
    /**
     * 启动蓝牙的request code
     */
    public static final int REQUEST_ENABLE_BT = 2016;
    /**
     * 打印头尾的固定格式
     */

    public static final String HEADIN = "***********进场联**********\n\n";
    public static final String HEADOut = "************出场联***********\n\n";
    public static final String FOOT = "*****************************\n";
    public static final String OWEHead = "***********欠费联**********\n\n";

    /**
     * 查看收费汇总的密码
     */
    public static final String PASSWORD = "182262471911";
//    public static final String PASSWORD = "110";
    /**
     * 上传照片失败重传的次数
     */
    public static final int retryTimes = 3;
    /**
     * 跳转到追缴列表所携带的request code
     */
    public static final int BACK_FROM_OWE = 21;
    /**
     * 进场拍照跳转到自定义相机界面携带的request code
     */
    public static final int BACK_FROM_CAMERA_IN = 100;
    /**
     * 出场拍照跳转到自定义相机界面携带的request code
     */
    public static final int BACK_FROM_CAMERA_OUT = 101;
    /**
     * 出场拍照跳转到自定义相机界面携带的request code，刷卡支付
     */
    public static final int BACK_FROM_CAMERA_OUT_CARD = 1011;
    /**
     * 逃单拍照跳转到自定义相机界面携带的request code
     */
    public static final int BACK_FROM_CAMERA_ESC = 102;
    /**
     * 刷卡时 获得卡号
     */
    public static final int MSG_FOUND_UID = 811;
    /**
     *标记泊位列表是否刷新成功
     */
    public static boolean BerthFresh = true;
    /**
     * 高级登录密码
     */
    public static String AdvancePw = "201617";
}
