package parkingos.com.bolink.constant;

import parkingos.com.bolink.utlis.Defind;

/**
 * 微信业务相关常量
 */
public class WeixinConstants {

    public final static String WXPUBLIC_REDIRECTURL = Defind.getProperty("WXPUBLIC_REDIRECTURL");
    public final static String WXPUBLIC_APPID = Defind.getProperty("WXPUBLIC_APPID");
    public final static String WXPUBLIC_SECRET = Defind.getProperty("WXPUBLIC_SECRET");
    public final static String IS_TO_THIRD_WXPAY = "1";
    public final static String IS_HAVE_WXAPPID = "1";//是否有公众号 0无，1有,parkingos配置为1，私有云无公众号时配置为0

}
