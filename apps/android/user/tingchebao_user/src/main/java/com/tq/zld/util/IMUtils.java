package com.tq.zld.util;

import android.text.TextUtils;

import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.im.IMConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GT on 2015/9/7.
 */
public class IMUtils {
    static String SPLIT_CHAR = "#";
    /**
     * 防止恶意，多次访问，缓存。
     */
    static Map<String,String[]> cache = new HashMap<>();
    private IMUtils(){}

    /**
     * 缓存环信账号
     *
     * @param username
     * @param password
     */
    public static void saveHXAccount(String username, String password){
        String hx = getSpString(TCBApp.mMobile, username, password);
        TCBApp.getAppContext().saveString(R.string.sp_im_account, hx);
    }

    public static String getUsername(){
        String hx = TCBApp.getAppContext().readString(R.string.sp_im_account, "");
        String[] result = getAllString(hx);
        if (!check(hx, TCBApp.mMobile)) {
            result = null;
        }
        return result == null ? "" : result[1];
    }

    public static String getPassword(){
        String hx = TCBApp.getAppContext().readString(R.string.sp_im_account,"");
        String[] result = getAllString(hx);
        if (!check(hx, TCBApp.mMobile)) {
            result = null;
        }
        return result == null ? "" : result[2];
    }

    public static void saveHead(String imgurl){
        TCBApp.getAppContext().saveString(R.string.sp_im_image_url, String.format("%s@.@%s",TCBApp.mMobile,imgurl));
    }

    public static String getHead(){
        String image = TCBApp.getAppContext().readString(R.string.sp_im_image_url, "");
        String[] split = image.split("@\\.@");
        String imgurl = "";
        if (split.length == 2 && TCBApp.mMobile.equals(split[0])) {
            //用缓存头像
            LogUtils.i("用缓存头像>>"+split[1]);
            imgurl = split[1];
        }

        return imgurl;
    }

    private static String[] getAllString(String hx){
        String[] result = null;

        if (!TextUtils.isEmpty(hx)){
            if (cache.containsKey(hx)) {
                result = cache.get(hx);
            } else {
                result = hx.split(SPLIT_CHAR);
                if (result.length != 3){
                    result = null;
                } else {
                    cache.put(hx, result);
                }
            }

        }

        return result;
    }

    private static boolean check(String hx, String mobile){
        String[] result = getAllString(hx);
        if (result != null){
            //手机号码一致，账户密码不为空，则验证成功。
            if (mobile.equals(result[0]) && !TextUtils.isEmpty(result[1]) && !TextUtils.isEmpty(result[2])) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取 hxim账号的存储sp的结果字符串
     * @param mobile
     * @param username
     * @param password
     * @return
     */
    private static String getSpString(String mobile, String username, String password){
        StringBuilder builder = new StringBuilder();
        builder.append(mobile).append(SPLIT_CHAR)
                .append(username).append(SPLIT_CHAR)
                .append(password);
        return builder.toString();
    }

    public static String getMsgString(EMMessage msg){
        String mid = msg.getStringAttribute(IMConstant.MSG_ATTR_MERGE_ID, "-1");
        boolean receive = msg.getBooleanAttribute(IMConstant.MSG_ATTR_MERGE_RECEIVE, false);
        TextMessageBody body = (TextMessageBody) msg.getBody();

        String text = body.getMessage();
        return String.format("id->%s: %s : %b :%s", msg.getMsgId(), mid, receive, text);
    }

}
