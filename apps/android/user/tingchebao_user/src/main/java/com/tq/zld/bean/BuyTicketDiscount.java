package com.tq.zld.bean;

/**
 * Author：ClareChen
 * E-mail：ggchaifeng@gmail.com
 * Date：  15/8/26 上午11:32
 */
public class BuyTicketDiscount {

    /**
     * 当前用户是否认证过：0未认证，1已认证
     */
    public int isauth;
    /**
     * 认证的折扣信息
     */
    public double auth;
    /**
     * 未认证的折扣信息
     */
    public double notauth;
}
