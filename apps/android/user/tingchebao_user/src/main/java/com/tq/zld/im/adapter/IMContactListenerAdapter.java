package com.tq.zld.im.adapter;

import com.easemob.chat.EMContactListener;

import java.util.List;

/**
 * Created by GT on 2015/9/7.
 * 空实现，适配器
 */
public class IMContactListenerAdapter implements EMContactListener{
    @Override
    public void onContactAdded(List<String> list) {

    }

    @Override
    public void onContactDeleted(List<String> list) {

    }

    @Override
    public void onContactInvited(String s, String s1) {

    }

    @Override
    public void onContactAgreed(String s) {

    }

    @Override
    public void onContactRefused(String s) {

    }
}
