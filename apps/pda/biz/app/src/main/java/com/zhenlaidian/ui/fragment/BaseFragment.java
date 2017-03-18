package com.zhenlaidian.ui.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.zhenlaidian.util.CommontUtils;

/**
 * Created by TCB on 2016/4/17.
 * xulu
 */
public class BaseFragment extends Fragment {
    protected SharedPreferences sharedPreferences;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = CommontUtils.getSharedPreferences(getActivity());
    }
    /**
     * 把boolean类型的变量存进xml
     *
     * @param s
     *            键
     * @param boolean1
     *            值
     */
    public void putBooleanToPreference(String s, Boolean boolean1) {
        android.content.SharedPreferences.Editor editor = sharedPreferences
                .edit();
        boolean flag = boolean1.booleanValue();
        editor.putBoolean(s, flag).commit();
    }

    /**
     * 把string类型的变量存进xml
     *
     * @param s
     *            键
     * @param s1
     *            值
     */
    public void putStringToPreference(String s, String s1) {
        sharedPreferences.edit().putString(s, s1).commit();
    }

    public void putIntToPreference(String s, int s1) {
        sharedPreferences.edit().putInt(s, s1).commit();
    }

    /**
     * 获取xml中键值是s对应的值(字符串)
     *
     * @param s
     *            键
     * @return value
     */
    public String getStringFromPreference(String s) {
        return sharedPreferences.getString(s, "");
    }

    /**
     * 获取xml中键值是s对应的值(字符串)
     *
     * @param s
     *            键
     * @param s1
     *            默认值
     * @return value
     */
    public String getStringFromPreference(String s, String s1) {
        return sharedPreferences.getString(s, s1);
    }

    /**
     * 获取xml中键值是s对应的值(boolean型值)
     *
     * @param s
     *            键
     * @return
     */
    public boolean getBooleanFromPreference(String s) {

        return sharedPreferences.getBoolean(s, false);
    }

    /**
     * 获取xml中键值是s对应的值(boolean型值)
     *
     * @param s
     *            键
     * @param flag
     *            默认值
     * @return value
     */
    public boolean getBooleanFromPreference(String s, boolean flag) {
        return sharedPreferences.getBoolean(s, flag);
    }

    public int getIntFromPreference(String s, int flag) {
        return sharedPreferences.getInt(s, flag);
    }

    /**
     *
     * @param s  键
     * @param flag 默认值
     * @return
     */
    public long getLongFromPreference(String s, long flag) {
        return sharedPreferences.getLong(s, flag);
    }

    public void putLongToPreference(String s, long s1) {
        sharedPreferences.edit().putLong(s, s1).commit();
    }
}
