package com.zhenlaidian.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Calendar;

public class SharedPreferencesUtils {

    private static SharedPreferences sp;
    private static SharedPreferencesUtils utils;

    @SuppressWarnings("static-access")
    private SharedPreferencesUtils(Context context) {
        sp = context.getSharedPreferences("tingchebao", context.MODE_PRIVATE);
    }

    public static SharedPreferencesUtils getIntance(Context context) {
        if (utils == null) {
            utils = new SharedPreferencesUtils(context);
        }
        return utils;
    }

    // 线上线下切换url
    public void setUrl(String url) {
        sp.edit().putString("url", url).commit();
    }

    public String getUrl() {
        return sp.getString("url", "1");
    }

    // 改变极速通连接标示；
    public void setBLEName(String bleName) {
        sp.edit().putString("bleName", bleName).commit();
    }

    public String getBLEName() {
        return sp.getString("bleName", "TCB1");
    }

    // 记录主页是否在前台
    public void setMainActivity(Boolean isdisplay) {
        sp.edit().putBoolean("isdisplay", isdisplay).commit();
    }

    public Boolean getMainActivity() {
        return sp.getBoolean("isdisplay", false);
    }

    // 用户的密码
    public void setPasswd(String passwd) {
        sp.edit().putString("passwd", passwd).commit();
    }

    public String getPasswd() {
        return sp.getString("passwd", "");
    }

    // 用户的账号
    public void setAccount(String account) {
        sp.edit().putString("account", account).commit();
    }

    public String getAccount() {
        return sp.getString("account", "");
    }

    // 收费员名字
    public void setName(String name) {
        sp.edit().putString("name", name).commit();
    }

    public String getName() {
        return sp.getString("name", "");
    }

    // 用户的角色
    public void setRole(String role) {
        sp.edit().putString("role", role).commit();
    }

    public String getRole() {
        return sp.getString("role", "");
    }

    // 用户登陆的token
    public void setToken(String token) {
        sp.edit().putString("token", token).commit();
    }

    public String getToken() {
        return sp.getString("token", "");
    }

    // 停车场编号
    public void setComid(String comid) {
        sp.edit().putString("comid", comid).commit();
    }

    public String getComid() {
        return sp.getString("comid", "");
    }

    // 停车场名字
    public void setParkname(String parkname) {
        sp.edit().putString("parkname", parkname).commit();
    }

    public String getParkname() {
        return sp.getString("parkname", "");
    }

    //停车场总车位
    public void setParkTotal(String parkTotal) {
        sp.edit().putString("parktotal", parkTotal).commit();
    }

    public String getParkTotal() {
        return sp.getString("parktotal", "");
    }

    // 推荐车主二维码的段链接
    public void setCode(String code) {
        sp.edit().putString("code", code).commit();
    }

    public String getCode() {
        return sp.getString("code", "");
    }

    // 保存余额信息
    public void setBanlance(String banlance) {
        sp.edit().putString("banlance", banlance).commit();
    }

    // 获取余额信息
    public String getBanlance() {
        return sp.getString("banlance", "0.00");
    }

    // 保存提现次数
    public void setWithdrawalNum(String withdrawalnum) {
        sp.edit().putString("withdrawalnum", withdrawalnum).commit();
    }

    // 获取提现次数
    public String getWithdrawalNum() {
        return sp.getString("withdrawalnum", "3");
    }

    // 保存当前时间戳
    public void setCurrentData() {
        sp.edit().putString("data", "" + (Calendar.getInstance().get(Calendar.DAY_OF_MONTH))).commit();
    }

    public String getCurrentData() {
        return sp.getString("data", null);
    }

    // 保存银行卡待绑定的状态；
    public void setIsCardCheck(Boolean ischeck) {
        sp.edit().putBoolean("iscardcheck", ischeck).commit();
    }

    public Boolean getIsCardCheck() {
        return sp.getBoolean("iscardcheck", false);
    }

    // 保存消息中心消息最大值
    public void setMsgMaxId(String maxid, String idname) {
        sp.edit().putString(idname, maxid).commit();
    }

    // 获取消息中心消息最大值
    public String getMsgMaxId(String idname) {
        return sp.getString(idname, "0");
    }

    // 保存消息中心是否有新消息
    public void setNewMsg(Boolean isnew) {
        sp.edit().putBoolean("msgnew", isnew).commit();
    }

    // 获取消息中心是否有新消息
    public Boolean getNewMsg() {
        return sp.getBoolean("msgnew", false);
    }

    // 保存ibeacon工作站
    public void setWorksite(int id) {
        sp.edit().putInt("worksite", id).commit();
    }

    // 获取ibeacon工作站
    public int getWorksite() {
        return sp.getInt("worksite", 0);
    }

    // 保存服务器与本地时间差；
    public void setLineTime(Long time) {
        sp.edit().putLong("linetime", time).commit();
    }

    // 获取服务器与本地时间差；
    public Long getLineTime() {
        return sp.getLong("linetime", 0);
    }

    // 保存被忽略升级的版本号；
    public void setVersion(String version) {
        sp.edit().putString("version", version).commit();
    }

    // 获取被忽略升级的版本号；
    public String getVersion() {
        return sp.getString("version", "0");
    }

    // 保存新版本提醒标记；
    public void setNewVersion(Boolean newversion) {
        sp.edit().putBoolean("newversion", newversion).commit();
    }

    // 获取新版本提醒标记；
    public boolean getNewVersion() {
        return sp.getBoolean("newversion", false);
    }

    // 保存是否可以查看直接支付标记
    public void setIsShowEpay(String isshow) {
        sp.edit().putString("isshow", isshow).commit();
    }

    // 获取是否可以查看直接支付标记
    public String getIsShowEpay() {
        return sp.getString("isshow", "1");
    }

    // 保存积分说明的url
    public void setScoreUrl(String url) {
        sp.edit().putString("scoreurl", url).commit();
    }

    public String getScoreUrl() {
        return sp.getString("scoreurl", "0");
    }

    // 保存停车券说明的url
    public void setTicketUrl(String url) {
        sp.edit().putString("ticketurl", url).commit();
    }

    public String getTicketUrl() {
        return sp.getString("ticketurl", "0");
    }

    // 保存我的积分
    public void setMyScore(String score) {
        sp.edit().putString("myscore", score).commit();
    }

    public String getMyScore() {
        return sp.getString("myscore", "0");
    }

    // 记录收入统计选项，false自己，true车场；
    public void setdefaultCheck(Boolean defaut) {
        sp.edit().putBoolean("defaultcheck", defaut).commit();
    }

    public Boolean getdefaultCheck() {
        return sp.getBoolean("defaultcheck", false);
    }

    //保存是否打开选择车牌的对话框;
    public void setSelectParkPosition(Boolean select) {
        sp.edit().putBoolean("defselect", select).commit();
    }

    public Boolean getSelectParkPosition() {
        return sp.getBoolean("defselect", true);
    }

    //是否允许更改预付
    public void setchange_prepay(String change_prepay) {
        sp.edit().putString("change_prepay", change_prepay).commit();
    }

    public String getchange_prepay() {
        return sp.getString("change_prepay", "0");
    }

    //是否默认显示泊位
    public void setview_plot(String view_plot) {
        sp.edit().putString("view_plot", view_plot).commit();
    }

    public String getview_plot() {
        return sp.getString("view_plot", "0");
    }

    //系统时间校对
    public void setlogontime(String logontime) {
        sp.edit().putString("logontime", (Long.parseLong(logontime) - System.currentTimeMillis() / 1000) + "").commit();
    }

    public String getlogontime() {
        return sp.getString("logontime", "0");
    }

    //存储车辆进场 出场 逃单是否需要拍照
    public void setphotoset(int i, int value) {
        sp.edit().putInt("photo" + i, value).commit();
    }

    public int getphotoset(int i) {
        return sp.getInt("photo" + i, 0);
    }

    //存储预设的 预付金额
    public void setprepayset(int i, String value) {
        sp.edit().putString("pre" + i, value).commit();
    }

    public String getprepayset(int i) {
        return sp.getString("pre" + i, "");
    }

    //存储打印小票的第一行和最后一行
    public void setprint_signIn(String str) {
        sp.edit().putString("print_signIn", str).commit();
    }

    public String getprint_signIn() {
        return sp.getString("print_signIn", "");
    }

    public void setprint_signOut(String str) {
        sp.edit().putString("print_signOut", str).commit();
    }

    public String getprint_signOut() {
        return sp.getString("print_signOut", "");
    }
    //存储打印小票的进场票头和出场票头
    public void setprint_signInHead(String str) {
        sp.edit().putString("print_signInHead", str).commit();
    }

    public String getprint_signInHead() {
        return sp.getString("print_signInHead", "");
    }

    public void setprint_signOutHead(String str) {
        sp.edit().putString("print_signOutHead", str).commit();
    }

    public String getprint_signOutHead() {
        return sp.getString("print_signOutHead", "");
    }


    //存储选择的泊位段 id
    public void setberthid(String berthid) {
        sp.edit().putString("berthid", berthid).commit();
    }

    public String getberthid() {
        return sp.getString("berthid", "-1");
    }

    //存储选择的泊位段 name
    public void setberth_name(String berth_name) {
        sp.edit().putString("berth_name", berth_name).commit();
    }

    public String getberth_name() {
        return sp.getString("berth_name", "");
    }

    //存储选择的泊位段 name 汉语
    public void setberthsec_name(String berthsec_name) {
        sp.edit().putString("berthsec_name", berthsec_name).commit();
    }

    public String getberthsec_name() {
        return sp.getString("berthsec_name", "");
    }

    //存储 签到工作编号 上班流水编号
    public void setworkid(String workid) {
        sp.edit().putString("workid", workid).commit();
    }

    public String getworkid() {
        return sp.getString("workid", "-1");
    }

    //存储 省份
    public void setfirstprovince(String firstprovince) {
        sp.edit().putString("firstprovince", firstprovince).commit();
    }

    public String getfirstprovince() {
        return sp.getString("firstprovince", "");
    }

    //存储 是否需要预付
    public void setisprepay(String isprepay) {
        sp.edit().putString("isprepay", isprepay).commit();
    }

    public String getisprepay() {
        return sp.getString("isprepay", "");
    }
    //存储 是否首页显示 今日收费汇总

    public void sethidedetail(String hidedetail) {
        sp.edit().putString("hidedetail", hidedetail).commit();
    }

    public String gethidedetail() {
        return sp.getString("hidedetail", "");
    }

    //存储 收费员手机号
    public void setmobile(String mobile) {
        if(TextUtils.isEmpty(mobile)||mobile.equals("null")){
            sp.edit().putString("mobile", "").commit();
        }else{
            sp.edit().putString("mobile", mobile).commit();
        }
    }

    public String getmobile() {
        return sp.getString("mobile", "");
    }
    //存储 签退是否需要密码
    public void setsignoutvalid(String signoutvalid) {
        if(TextUtils.isEmpty(signoutvalid)||signoutvalid.equals("null")){
            sp.edit().putString("signoutvalid", "").commit();
        }else{
            sp.edit().putString("signoutvalid", signoutvalid).commit();
        }
    }

    public String getsignoutvalid() {
        return sp.getString("signoutvalid", "");
    }
    //存储 结算订单时是否顺便打印订单告知
    public void setprint_order_place2(int print_order_place2) {
        sp.edit().putInt("print_order_place2", print_order_place2).commit();
    }
    public int getprint_order_place2() {
        return sp.getInt("print_order_place2", 0);
    }
    //存储 是否打印收费员姓名
    public void setisprintName(int isprint) {
        boolean isprintName;
        if(isprint==1)
            isprintName = true;
        else
            isprintName = false;
        sp.edit().putBoolean("isprintName", isprintName).commit();
    }
    public boolean getisprintName() {
        return sp.getBoolean("isprintName", true);
    }
}
