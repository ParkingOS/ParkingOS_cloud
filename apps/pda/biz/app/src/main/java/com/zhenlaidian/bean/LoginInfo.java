package com.zhenlaidian.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class LoginInfo {
    // 登陆的返回信息是---{"token":"611f7e53d6638b32e79094592a91c97b","name":"停车宝","role":"2","iscancel":"1","info":"success"}
    // 返回内容：{"comid":"1197","etc":"1","swipe":"1","state":"0","notemsg":"我每笔赚2元，你省3元","info":"success",
    // "worksite":[{"id":"16","worksite_name":"南工作站"},{"id":"17","worksite_name":"北工作站"},{"id":"18","worksite_name":"测试入口"},{"id":"19","worksite_name":"测试出口"}],
    // "token":"95fb0b380317c3c961ac0a3d49742257","name":"大牛","authflag":"1","role":"1","nfc":"0","iscancel":"1","mobile":"13366371718"}

    private String token;
    private String comid;// 车场编号
    private String etc;// 是蓝牙车场；1是蓝牙车场，2通道照牌；3手机照牌；
    private String info;
    private String role;// 角色1，管理员。2.收费员
    private String name;
    private String iscancel;// 订单对话框是否关闭；
    private String isshowepay;// 是否可以查看直接支付订单；
    private String swipe;// 是否开启扫牌 0关闭，1开启；
    private String notemsg;// 主页界面公告信息；(已经废弃不用)
    private String state;// 注册状态；//0正常用户，1禁用，2审核中,3设备未注册
    private String mobile;// 手机号，待审核会返
    private String authflag;// 13为泊车员
    private String qr;// 主页二维码图案
    private ArrayList<WorksiteInfo> worksite;// 蓝牙车场工作站；
    private String cname;// 停车场名字；
    private String ctotal;// 停车位总数；

    private String nfc;

    //2016.4.18 增加
    private String  change_prepay;//是否允许更改预付0/1
    private ArrayList<Integer> photoset;//车辆进场，离场，逃单是否需要拍照，0/1
    private ArrayList<Berths> berths;//标段
    private String isprepay; //是否开启预付
    private ArrayList<String> prepayset;//预付 预设金额
    private ArrayList<String> print_sign;//打印小票的第一行和最后一行
    private String view_plot;//是否默认显示泊位0/1
    private String logontime;//服务器时间，用于校对，与本地时间取差值，以后提交时都加上这个差值

    private String berthid;
    private String berth_name;//泊位段名称
    private String berthsec_name;//泊位段名称 另一个

    private String firstprovince;
    //2016。5.13增加   hidedetail 是否隐藏明细
    private String hidedetail;
    //2016.8.31增加 signoutvalid 签退是否显示密码
    private String signoutvalid;
    //2016.12.3增加 print_order_place2 在点击订单结算时候是否打印订单
    private int print_order_place2;
    //2016.12.28增加 print_name
    private int is_print_name;

    public int getIs_print_name() {
        return is_print_name;
    }

    public void setIs_print_name(int is_print_name) {
        this.is_print_name = is_print_name;
    }

    public int getPrint_order_place2() {
        return print_order_place2;
    }

    public void setPrint_order_place2(int print_order_place2) {
        this.print_order_place2 = print_order_place2;
    }

    public String getSignoutvalid() {
        return signoutvalid;
    }

    public void setSignoutvalid(String signoutvalid) {
        this.signoutvalid = signoutvalid;
    }

    public String getHidedetail() {
        return hidedetail;
    }

    public void setHidedetail(String hidedetail) {
        this.hidedetail = hidedetail;
    }

    public LoginInfo() {
        super();
    }

    public String getBerth_name() {
        return berth_name;
    }

    public String getIsprepay() {
        return isprepay;
    }

    public void setIsprepay(String isprepay) {
        this.isprepay = isprepay;
    }

    public String getFirstprovince() {
        return firstprovince;
    }

    public void setFirstprovince(String firstprovince) {
        this.firstprovince = firstprovince;
    }

    public void setBerth_name(String berth_name) {
        this.berth_name = berth_name;
    }

    public String getBerthid() {
        return berthid;
    }

    public void setBerthid(String berthid) {
        this.berthid = berthid;
    }

    public String getNfc() {
        return nfc;
    }

    public void setNfc(String nfc) {
        this.nfc = nfc;
    }

    public String getComid() {
        return comid;
    }

    public void setComid(String comid) {
        this.comid = comid;
    }

    public String getToken() {
        return token;
    }

    public String getChange_prepay() {
        return change_prepay;
    }

    public void setChange_prepay(String change_prepay) {
        this.change_prepay = change_prepay;
    }

    public ArrayList<Integer> getPhotoset() {
        return photoset;
    }

    public void setPhotoset(ArrayList<Integer> photoset) {
        this.photoset = photoset;
    }

    public ArrayList<Berths> getBerths() {
        return berths;
    }

    public void setBerths(ArrayList<Berths> berths) {
        this.berths = berths;
    }

    public ArrayList<String> getPrepayset() {
        return prepayset;
    }

    public void setPrepayset(ArrayList<String> prepayset) {
        this.prepayset = prepayset;
    }

    public ArrayList<String> getPrint_sign() {
        return print_sign;
    }

    public void setPrint_sign(ArrayList<String> print_sign) {
        this.print_sign = print_sign;
    }

    public String getView_plot() {
        return view_plot;
    }

    public void setView_plot(String view_plot) {
        this.view_plot = view_plot;
    }

    public String getLogontime() {
        return logontime;
    }

    public void setLogontime(String logontime) {
        this.logontime = logontime;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIscancel() {
        return iscancel;
    }

    public void setIscancel(String iscancel) {
        this.iscancel = iscancel;
    }

    public String getSwipe() {
        return swipe;
    }

    public void setSwipe(String swipe) {
        this.swipe = swipe;
    }

    public String getNotemsg() {
        return notemsg;
    }

    public void setNotemsg(String notemsg) {
        this.notemsg = notemsg;
    }

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAuthflag() {
        return authflag;
    }

    public void setAuthflag(String authflag) {
        this.authflag = authflag;
    }

    public String getCtotal() {
        return ctotal;
    }

    public void setCtotal(String ctotal) {
        this.ctotal = ctotal;
    }

    public String getEtc() {
        return etc;
    }

    public void setEtc(String etc) {
        this.etc = etc;
    }

    public String getIsshowepay() {
        return isshowepay;
    }

    public void setIsshowepay(String isshowepay) {
        this.isshowepay = isshowepay;
    }


    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    public ArrayList<WorksiteInfo> getWorksite() {
        return worksite;
    }

    public void setWorksite(ArrayList<WorksiteInfo> worksite) {
        this.worksite = worksite;
    }

    @SuppressWarnings("serial")
    public static class WorksiteInfo implements Serializable {
        public String id;
        public String worksite_name;

        public WorksiteInfo(String id, String worksite_name) {
            super();
            this.id = id;
            this.worksite_name = worksite_name;
        }

        public WorksiteInfo() {
            super();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getWorksite_name() {
            return worksite_name;
        }

        public void setWorksite_name(String worksite_name) {
            this.worksite_name = worksite_name;
        }

        @Override
        public String toString() {
            return "worksite [id=" + id + ", worksite_name=" + worksite_name + "]";
        }

    }

    public String getBerthsec_name() {
        return berthsec_name;
    }

    public void setBerthsec_name(String berthsec_name) {
        this.berthsec_name = berthsec_name;
    }

    @Override
    public String toString() {
        return "LoginInfo{" +
                "token='" + token + '\'' +
                ", comid='" + comid + '\'' +
                ", etc='" + etc + '\'' +
                ", info='" + info + '\'' +
                ", role='" + role + '\'' +
                ", name='" + name + '\'' +
                ", iscancel='" + iscancel + '\'' +
                ", isshowepay='" + isshowepay + '\'' +
                ", swipe='" + swipe + '\'' +
                ", notemsg='" + notemsg + '\'' +
                ", state='" + state + '\'' +
                ", mobile='" + mobile + '\'' +
                ", authflag='" + authflag + '\'' +
                ", qr='" + qr + '\'' +
                ", worksite=" + worksite +
                ", cname='" + cname + '\'' +
                ", ctotal='" + ctotal + '\'' +
                ", nfc='" + nfc + '\'' +
                ", change_prepay='" + change_prepay + '\'' +
                ", photoset=" + photoset +
                ", berths=" + berths +
                ", isprepay='" + isprepay + '\'' +
                ", prepayset=" + prepayset +
                ", print_sign=" + print_sign +
                ", view_plot='" + view_plot + '\'' +
                ", logontime='" + logontime + '\'' +
                ", berthid='" + berthid + '\'' +
                ", berth_name='" + berth_name + '\'' +
                ", berthsec_name='" + berthsec_name + '\'' +
                ", firstprovince='" + firstprovince + '\'' +
                ", hidedetail='" + hidedetail + '\'' +
                ", signoutvalid='" + signoutvalid + '\'' +
                ", print_order_place2=" + print_order_place2 +
                ", is_print_name=" + is_print_name +
                '}';
    }
}
