package com.tq.zld.bean;

import android.graphics.Bitmap;

public class ShareInfo {
    // {"imgurl":"images/bonus/order_bonu.png","title":"二百多家车场通用的停车券，duang~","description":"祝你新年一路发发发.....",
    // "url":"carowner.do?action=getobonus","total":"66.00","bnum":"30"};

    public String title;// 分享的标题
    public String description;// 分享的描述
    public String imgurl;// 图片url
    public String url;// 活动页面地址

    public String total;// 停车红包总价
    public String bnum;// 停车卷张数；

    public Bitmap thumbImage;

    @Override
    public String toString() {
        return "ShareInfo{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imgurl='" + imgurl + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
