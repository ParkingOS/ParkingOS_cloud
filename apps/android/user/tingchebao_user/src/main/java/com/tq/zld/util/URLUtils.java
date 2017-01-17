package com.tq.zld.util;

import android.text.TextUtils;

import com.tq.zld.TCBApp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class URLUtils {
    /**
     * 解析出url请求的路径，包括页面
     *
     * @param strURL url地址
     * @return url路径
     */
    public static String UrlPage(String strURL) {
        String strPage = null;
        String[] arrSplit = null;

        strURL = strURL.trim().toLowerCase(Locale.CHINA);

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 0) {
            if (arrSplit.length > 1) {
                if (arrSplit[0] != null) {
                    strPage = arrSplit[0];
                }
            }
        }

        return strPage;
    }

    /**
     * 去掉url中的路径，留下请求参数部分
     *
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String TruncateUrlPage(String strURL) {
        String strAllParam = null;
        String[] arrSplit = null;

        strURL = strURL.trim().toLowerCase(Locale.CHINA);

        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1) {
            if (arrSplit.length > 1) {
                if (arrSplit[1] != null) {
                    strAllParam = arrSplit[1];
                }
            }
        }
        return strAllParam;
    }

    /**
     * 解析出url参数中的键值对 如 "index.jsp?Action=del&id=123"，解析出Action:del,id:123存入map中
     *
     * @param URL url地址
     * @return url请求参数部分
     */
    public static HashMap<String, String> getParameters(String URL) {
        HashMap<String, String> mapRequest = new HashMap<String, String>();

        String[] arrSplit = null;

        String strUrlParam = TruncateUrlPage(URL);
        if (strUrlParam == null) {
            return mapRequest;
        }
        // 每个键值为一组
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");

            // 解析出键值
            if (arrSplitEqual.length > 1) {
                // 正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

            } else {
                if (arrSplitEqual[0] != "") {
                    // 只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

    /**
     * 使用getUrl，只是封装了一下
     * @param serverUrl 服务器地址
     * @param action xx.do
     * @param params 参数
     * @return
     */
    public static String createUrl(String serverUrl, String action, Map<String, String> params){
        return genUrl(serverUrl + action, params);
    }

    /**
     * 将Map格式的参数拼装到给定url后面生成一个新的get请求url，如有中文，会自动进行URLEncoder编码
     *
     * @param url
     * @param params
     * @return
     */
    public static String genUrl(String url, Map<String, String> params) {
        if (params == null || params.size() == 0) {
            return url;
        }
        StringBuilder urlBuilder = new StringBuilder(url);
        int versionCode = AndroidUtils.getVersionCode();
        if (!url.contains("?")) {
            urlBuilder.append("?p=android&v=" + versionCode);
        } else {
            if (url.endsWith("?")) {
                urlBuilder.append("p=android&v=" + versionCode);
            } else {
                urlBuilder.append("&p=android&v=" + versionCode);
            }
        }
        String value;
        for (String key : params.keySet()) {
            urlBuilder.append("&");
            urlBuilder.append(key);
            urlBuilder.append("=");
            value = params.get(key);
            try {
                if (!TextUtils.isEmpty(value)) {
                    value = URLEncoder.encode(value, "UTF-8");
                }
            } catch (UnsupportedEncodingException e) {
                // Do nothing
                e.printStackTrace();
            }
            urlBuilder.append(value);
        }
        return urlBuilder.toString();
    }

    /**
     * 添加公共请求参数
     *
     * @param params
     * @return
     */
    public static Map<String, String> putPublicParam(Map<String, String> params) {
        params.put("p", "android");
        params.put("v", String.valueOf(AndroidUtils.getVersionCode()));
        return params;
    }

    /**
     * 对中文做URL编码
     *
     * @param params
     * @return
     */
    public static Map<String, String> decode(Map<String, String> params) {
        String value;
        for (String key : params.keySet()) {
            value = params.get(key);
            try {
                if (!TextUtils.isEmpty(value)) {
                    value = URLEncoder.encode(value, "UTF-8");
                    params.put(key, value);
                }
            } catch (UnsupportedEncodingException e) {
                // Do nothing
                e.printStackTrace();
            }
        }
        putPublicParam(params);
        return params;
    }

    /**
     * 创建一个<String,String>的HashMap,默认添加{mobile:TCBApp.mMobile}键值对。
     * @return
     */
    public static Map<String,String> createParamsMap(){
        Map<String,String> params = new HashMap<>();
        params.put("mobile", TCBApp.mMobile);
        return params;
    }

    /**
     * 获取微信公众账号文章链接
     * @param type 文章类型
     * @return
     */
    public static String getWXArticleURL(ArticleType type){
        //carinter.do?action=getwxpcartic&mobile=13641309140&artictype=uoinrule,useticket,credit,backbalance
        Map<String, String> params = createParamsMap();
        params.put("action","getwxpcartic");
        params.put("mobile", TCBApp.mMobile);
        params.put("artictype",type.name());

        String url = createUrl(TCBApp.mServerUrl, "carinter.do", params);
        return url;
    }

    public enum ArticleType {
        /**
         * 合并规则
         */
        uoinrule,
        /**
         * 用券规则
         */
        useticket,
        /**
         * 信用
         */
        credit,
        /**
         * 充值
         */
        backbalance;
    }

}
