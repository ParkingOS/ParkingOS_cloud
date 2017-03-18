package com.zhenlaidian.util;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import java.util.Set;

/**
 * 登陆界面账户自动提示,此工具是为保存用户输入过得账户;
 */
public class SharedPreferencesHandler {


    final static String regularEx = "|";

    @SuppressLint("NewApi")
    public static Set<String> getStringSet(SharedPreferences prefs, String key,
                                           Set<String> defValues) {
        String str = prefs.getString(key, "");
        MyLog.e("SharedPreferencesHandler", "sp中存的String值是" + str);
        if (!str.isEmpty()) {
            String[] values = str.split("");
            MyLog.e("SharedPreferencesHandler", "sp中存的String去空格后的数组" + values[0].toString());
            if (defValues == null | defValues.isEmpty()) {
                defValues.clear();
                String strs = "";
                for (String value : values) {
                    if (!value.isEmpty() && !value.equals(regularEx)) {
//						Log.e("SharedPreferencesHandler", "sp中存的String去空格后的数组的元素"+value);
                        strs = strs + value;
                    } else {
                        defValues.add(strs);
//						Log.e("SharedPreferencesHandler", "添加一个用户账号--"+strs);
                        strs = "";
                    }

                }
            }
        }
        return defValues;
    }

    public static SharedPreferences.Editor putStringSet(SharedPreferences.Editor ed, String key, Set<String> values) {
        String str = "";
        if (values != null | !values.isEmpty()) {
            Object[] objects = values.toArray();
            for (Object obj : objects) {
                str += obj.toString();
                str += regularEx;
            }
            ed.putString(key, str);
//			Log.e("SharedPreferencesHandler", "sp.put的string值是---"+str);
        }
        return ed;
    }


}

