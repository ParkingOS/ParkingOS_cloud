package com.zhenlaidian.util;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.device.DeviceManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.lswss.QRCodeEncoder;
import com.google.zxing.qrcode.QRCodeWriter;
import com.zhenlaidian.MyApplication;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommontUtils {
    private static SharedPreferences sharedPreferences = null;

    public static SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = MyApplication.getInstance()
                    .getSharedPreferences("tcblocal.xml", Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    /**
     * 获取应用的的xml
     *
     * @return
     */
    public static SharedPreferences getSharedPreferences(Activity activity) {
        if (sharedPreferences == null) {
            sharedPreferences = activity.getSharedPreferences("tcblocal.xml",
                    Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    public static SharedPreferences LocalSharedPreferences(Context contxContext) {
        if (sharedPreferences == null) {
            sharedPreferences = contxContext.getSharedPreferences(
                    "tcblocal.xml", Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }
    public static Bitmap Drawable2Bitmap(Context context,int id){
        BitmapDrawable bd= (BitmapDrawable) context.getResources().getDrawable(id);
        return bd.getBitmap();
    }
    /**
     * 将字符串转化成二维码
     */
    public static Bitmap GetQrBitmap(String code) {
        QRCodeEncoder d = new QRCodeEncoder();
        return d.encode2BitMap(code, 400, 400);
    }
    /**
     * 在二维码中间添加Logo图案
     */
    public static Bitmap addLogo(String code, Bitmap logo) {
        QRCodeEncoder d = new QRCodeEncoder();
        Bitmap src = d.encode2BitMap(code, 400, 400);
        if (src == null) {
            return null;
        }
        if (logo == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);

            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }
    /**
     * 快速Toast
     */
    public static void toast(Context context, String msg) {

        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

    }

    /**
     * 确认年份是否为闰年
     *
     * @param y
     * @return boolean
     */
    public static boolean isRunYear(int y) {
        if (y % 400 == 0 || y % 4 == 0 && y % 100 != 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 确认字符串是否为email格式
     *
     * @param strEmail
     * @return
     */
    public static boolean isEmail(String strEmail) {
        String strPattern = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Activity context) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Activity context) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 检查字符串
     */
    public static boolean checkString(String s) {
        if (s != null && !s.toString().trim().equals("")) {
            return true;
        }
        return false;
    }

    /**
     * 检查list
     */
    public static boolean checkList(List l) {
        if (l != null && l.size() != 0) {
            return true;
        }
        return false;
    }

    /**
     * 保存搜索商品历史记录
     */
    public static boolean setArrayList2SP(SharedPreferences sp,
                                          ArrayList<String> list) {
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("Status_size", list.size());
        for (int i = 0; i < list.size(); i++) {
            mEdit1.remove("Status_" + i);
            mEdit1.putString("Status_" + i, list.get(i));
        }
        return mEdit1.commit();
    }

    /**
     * 保存搜索门店历史记录
     */
    public static boolean setArrayList2SP1(SharedPreferences sp,
                                           ArrayList<String> list) {
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("Status_size1", list.size());
        for (int i = 0; i < list.size(); i++) {
            mEdit1.remove("Status1_" + i);
            mEdit1.putString("Status1_" + i, list.get(i));
        }
        return mEdit1.commit();
    }


    /**
     * 隐藏 软键盘
     */
    public static void hideSoftInput(Context context, View anyView) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(anyView.getWindowToken(), 0);
    }

    /**
     * 展示软键盘
     *
     * @param :activity内的任意view
     */
    public static void showSoftInput(Context context, View editText) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }


    /**
     * TextView 显示不同字号
     */
    public static SpannableString Main_showPriceText(String price) {
        SpannableString ss = new SpannableString("￥" + price);
        CharacterStyle span1 = new AbsoluteSizeSpan(24);
        ss.setSpan(span1, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    /**
     * 弹出||隐藏 软键盘
     */
    public static void SoftInput(Activity context) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    // 检查电话号码
    public static boolean checkPhoneNum(String phone) {
        if (!CommontUtils.checkString(phone)) {
            return false;
        }
        Pattern p = Pattern.compile("^1(3|5|7|8|4)\\d{9}");
        Matcher m = p.matcher(phone);
        if (!m.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 购物车数量
     */
    public static int setGoodsNum(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        if (screenW < 720) {
            return 22;
        }
        if (screenW >= 720 && screenW < 1080) {
            return 30;
        }
        if (screenW >= 1080) {
            return 50;
        }
        return 40;
    }

    /**
     * 判断是否有网
     */
    public static boolean detect(Activity act) {

        ConnectivityManager manager = (ConnectivityManager) act
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        NetworkInfo networkinfo = manager.getActiveNetworkInfo();

        if (networkinfo == null || !networkinfo.isAvailable()) {
            return false;
        }

        return true;
    }

    /**
     * webview 展示商品详情url拼接
     */
    public static String getWebViewURL1(String goods_id) {
        return "http://www.2688.cn/Shop/dProductContentAppForWebShop.aspx?Pid="
                + goods_id;
    }

    /**
     * 绑定门店
     */
    public static String getWebViewURL12(String url, String MemberId) {
        return url + "&MemberId=" + MemberId;
    }

    /**
     * 公告页面
     */
    public static String getNoticInfo(String BoundValue, String areacode,
                                      Activity activity) {
        return "http://webservicemb.2688.com/NoticeInfoAppShop.aspx?noticeId="
                + BoundValue + "&areacode=" + areacode + "&width="
                + CommontUtils.getScreenWidth(activity)
                + "&appname=android2688webshop";
    }

    /**
     * 获取当前时间戳
     */
    public static String getTimespan() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String str = sdf.format(date);
        return str;
    }

    /**
     * 获取当前时间戳
     */
    public static String getTimespanss() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = sdf.format(date);
        return str;
    }

    /**
     * 获取当前的小时和分钟
     */

    public static String getFormatHour_Min() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String str = sdf.format(d);
        return str;
    }

    /**
     * 获取当前的小时和分钟
     */
    @SuppressWarnings("deprecation")
    public static String getHour_Min() {
        Date d = new Date();
        int hour = d.getHours();
        int min = d.getMinutes();
//		int sec = d.getSeconds();
//		int sec = s;
        return (hour < 10) ? ("0" + hour) : hour + ":" + ((min < 10) ? ("0" + min) : min);
    }

    /**
     * MD5加密
     */
    public static String getMD5Str(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & byteArray[i]));
            } else {
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }
        return md5StrBuff.toString().toLowerCase();
    }

    public static String replaceURL(String url) {
        StringBuffer buffer = new StringBuffer(url);
        buffer.insert(url.lastIndexOf("."), "2");
        return buffer.toString();
    }

    /**
     * px转换成sp
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * sp转换成px
     */
    public static float sp2px(Context context, float spValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue,
                context.getResources().getDisplayMetrics());
    }

    /**
     * dp转换成px
     */
    public static float dp2px(Context context, float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
                context.getResources().getDisplayMetrics());
    }

    public static int setMainMiddleSize(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;
        if (screenW < 720) {
            return 140;
        }
        if (screenW >= 720 && screenW < 1080) {
            return 200;
        }
        if (screenW >= 1080) {
            return 300;
        }
        return 200;
    }

    /*
     * 半角转全角
     */
    public static String ToSBC(String input) {
        char c[] = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == ' ') {
                c[i] = '\u3000';
            } else if (c[i] < '\177') {
                c[i] = (char) (c[i] + 65248);
            }
        }
        return new String(c);
    }

    /**
     * 清除缓存 Fresco
     */
//	public static void ClearCache() {
//		Fresco.getImagePipelineFactory().getMainDiskStorageCache().clearAll();
//	}

    /**
     * unicode 转换中文
     *
     * @param ori
     * @return
     */
    public static String convertUnicode(String ori) {

        char aChar;
        int len = ori.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len; ) {
            aChar = ori.charAt(x++);
            if (aChar == '\\') {
                aChar = ori.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = ori.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);

        }
        return outBuffer.toString();
    }

    /**
     * 比较两个字符串是否相等
     *
     * @param s1
     * @param s2
     * @return
     */
    public static boolean equals(String s1, String s2) {
        if (s1 == null) {
            if (s2 == null) {
                return true;
            } else {
                return false;
            }
        } else {
            if (s2 == null) {
                return false;
            } else if (s1.equals(s2)) {
                return true;
            }
        }
        return false;
    }

    // 得到yyyy-mm格式的当前系统时间
    public static String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM", Locale.US);
        String date = sdf.format(new Date());
        return date;
    }

    // 比较两个时间的大小
    public static boolean isSmaller(String date1, String date2) {
        String temp1 = date1.replace("-", "");
        String temp2 = date2.replace("-", "");
        int d1 = Integer.valueOf(temp1);
        int d2 = Integer.valueOf(temp2);
        return (d1 > d2) ? false : true;

    }

    // 将yyyy-mm格式的时间转换为yyyy年 mm月
    public static String formatDate(String date) {
        String dateformate = date.replace("-", "年    ");
        dateformate = dateformate + "月";
        return dateformate;

    }

//	/**
//	 * 设置SimpleDraweeView图片
//	 */
//	public static void setImageUri(SimpleDraweeView drawview, Object uri) {
//		if (uri instanceof Uri) {
//			drawview.setImageURI((Uri) uri);
//		} else if (uri instanceof String) {
//			drawview.setImageURI(Uri.parse((String) uri));
//		} else {
//			drawview.setImageResource(R.drawable.android_productitem_bg);
//		}
//	}

    /**
     * 生成二维码
     */
    // http://www.2688.cn/h5/scanmember.htm?MemberId=
    // 要转换的地址或字符串,可以是中文
    public static Bitmap createQRImage(String url) {
        int QR_WIDTH = 630;
        int QR_HEIGHT = 630;
        Bitmap bitmap = null;
        try {
            // 判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url,
                    BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            // 显示到一个ImageView上面
            // sweepIV.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 生成二维码连接地址
     *
     * @return
     */
//	public static String createURL(BaseActivity activity) {
//		StringBuilder builder = new StringBuilder();
//		builder.append("http://www.2688.cn/h5/scanmem.htm?mid=");
//		builder.append(activity.getStringFromPreference("MemberId"));
//		return new String(builder);
//	}

    /**
     * 获取堆栈底部Activity的名称
     */
    public static String getBaseActivity(Activity context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            return (runningTaskInfos.get(0).baseActivity).toString();
        } else {
            return null;
        }
    }

    /**
     * 获取堆栈顶部Activity的名称
     */
    public static String getTopActivity(Activity context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            return (runningTaskInfos.get(0).topActivity).toString();
        } else {
            return null;
        }
    }

    /**
     * 显示评论等级
     */
    public static void setLevel(List<ImageView> list, int level) {
        if (checkList(list)) {
            if (list.size() > level) {
                for (int i = 0; i < list.size(); i++) {
                    if (i < level) {
                        list.get(i).setVisibility(View.VISIBLE);
                    } else {
                        list.get(i).setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    /**
     * 判断当前应用程序处于前台还是后台
     */
    public static boolean isBackground(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
        if (tasksInfo.size() > 0) {
            // 应用程序位于堆栈的顶层
            if (packageName.equals(tasksInfo.get(0).topActivity
                    .getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据图片宽高比 获取适配宽度
     */
    public static int getRealWidth(Activity activity, int width, int height) {
        return CommontUtils.getScreenHeight(activity) * width / height;
    }

    /**
     * 根据图片宽高比 获取适配高度
     */
    public static int getRealHeight(Activity activity, int width, int height) {
        if (width == 0) {
            return 0;
        }
        return CommontUtils.getScreenWidth(activity) * height / width;
    }

    /**
     * 点赞 把List<String> 变成 字符串
     */
//	public static String ListToString(List<NotesLikeInfoEntity> list) {
//		StringBuffer buffer = new StringBuffer();
//		for (int i = 0; i < list.size(); i++) {
//			if (i < list.size() - 1) {
//				buffer.append(list.get(i).getNickName() + "、");
//			} else {
//				buffer.append(list.get(i).getNickName());
//			}
//		}
//		return buffer.toString();
//	}

    /**
     * 标签 把List<String> 变成 字符串
     */
//	public static String ListToString1(List<NotesLabelInfoEntity> list) {
//		StringBuffer buffer = new StringBuffer();
//		for (int i = 0; i < list.size(); i++) {
//			if (i < list.size() - 1) {
//				buffer.append(list.get(i).getLabelName() + "  ");
//			} else {
//				buffer.append(list.get(i).getLabelName());
//			}
//		}
//		return buffer.toString();
//	}

    /**
     * 评论显示不同颜色
     */
    public static SpannableStringBuilder setTextColor(String name,
                                                      String name2, String content) {
        SpannableStringBuilder ss;
        if (!checkString(name2)) {
            String str_sub = name + "：";
            ss = new SpannableStringBuilder(str_sub + content);
            ss.setSpan(new ForegroundColorSpan(0xff32b430), 0,
                    str_sub.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            String str_sub = name + " 回复 " + name2 + "：";
            ss = new SpannableStringBuilder(str_sub + content);
            ss.setSpan(new ForegroundColorSpan(0xff32b430), 0,
                    str_sub.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(new ForegroundColorSpan(0xff858585), name.length(),
                    name.length() + 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }

    /**
     * 设置背景
     * roundRadius 圆角 （px） 0为直角
     * strokeWidth 边框宽度（px）
     * strokeColor 边框颜色
     * fillColor   填充色
     * alpha  透明度(0--255) 255为不透明
     */
    public static GradientDrawable setDrawable(int roundRadius,
                                               int strokeWidth, String strokeColor, String fillColor, int alpha) {
        GradientDrawable gd = new GradientDrawable();
        int strokeColor1 = Color.parseColor("#" + Integer.toHexString(alpha)
                + strokeColor);// 边框颜色
        int fillColor1 = Color.parseColor("#" + Integer.toHexString(alpha)
                + fillColor);// 内部填充颜色
        gd.setColor(fillColor1);
        gd.setCornerRadius(roundRadius);
        gd.setStroke(strokeWidth, strokeColor1);
        return gd;
    }


    /**
     * 返回 DEVICE_ID
     *
     * @return
     */
    public static String GetHardWareAddress(Context context) {
        String DEVICE_ID;
        if (Is910()) {
            DEVICE_ID = new DeviceManager().getDeviceId();
        } else {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            DEVICE_ID = tm.getDeviceId();
        }
//        if (TextUtils.isEmpty(DEVICE_ID)) {
//            DEVICE_ID = "0000000099";
//        }
//        return TextUtils.isEmpty(DEVICE_ID)?"00000000":DEVICE_ID;
        return DEVICE_ID;
//        return "00000000";
//        return "863280021119247"; //测试用设备id 大黄
    }

    /**
     * 获取当前的手机号
     */
    public static String getLocalNumber(Context context) {
        TelephonyManager tManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String number = tManager.getLine1Number();
        if (number == null)
            number = "";
        return number;
    }

    // 获取当前应用程序的版本号
    public static String getVersion(Context context) {

        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            System.out.println("banbenhaooo" + String.valueOf(info.versionCode));
            return String.valueOf(info.versionCode);

        } catch (Exception e) {
            e.printStackTrace();
            return "版本号未知";
        }
    }

    /**
     * UNIX时间戳转化为正常时间
     */
    public static String Unix2Time(String unix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(Long.parseLong(unix) * 1000);
        return sdf.format(date);
//        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(order.getBegin()) * 1000
    }

    /**
     * UNIX时间戳转化为正常时间
     */
    public static String Unix2TimeS(String unix) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(Long.parseLong(unix) * 1000);
        return sdf.format(date);
//        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(order.getBegin()) * 1000
    }
    /**
     * 毫秒值转化为正常时间
     */
    public static String Mili2Time(String mili) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(Long.parseLong(mili));
        return sdf.format(date);
//        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(order.getBegin()) * 1000
    }
    /**
     * 毫秒值转化为正常时间
     */
    public static String Mili2TimeMin(String mili) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(Long.parseLong(mili));
        return sdf.format(date);
//        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(order.getBegin()) * 1000
    }

    public static final int MEDIA_TYPE_IMAGE = 1;

    public static Uri getOutputMediaFileUri(int type, Context mContext) {
        return Uri.fromFile(getOutputMediaFile(type, mContext));
    }

    public static File getOutputMediaFile(int type, Context mContext) {
        File mediaStorageDir = null;
        if (Environment.getExternalStorageState() != null) {
//			File dir = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
//			mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "TestCameraFile");
            mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "/TingCheBao");
            Log.d("MyCameraApp", " if create directory" + mediaStorageDir);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory");
                    return null;
                }
            }
        } else {
            mediaStorageDir = new File(mContext.getCacheDir(), "TestCameraFile");
            Log.d("MyCameraApp", " else create directory" + mediaStorageDir);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory");
                    return null;
                }
            }
            Log.d("MyCameraApp", "路径为" + mediaStorageDir);
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }
        Log.d("MyCameraApp", " return directory" + mediaFile.getAbsolutePath());
        return mediaFile;
    }

    /**
     * 将double转为保留两位小数，一般用于金额计算,四舍五入向上
     */
    public static double doubleTwoPoint(double money) {
        double moneytwopint = new BigDecimal(money).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return moneytwopint;
    }

    /**
     * 写入内容到SD卡中的txt文本中
     * str为内容
     */
    public static void writeSDFile(Context context, String describe, String str) {
        try {
            File file = createSDFile(context);
            FileWriter fw = new FileWriter(file.getAbsolutePath(), true);
            fw.write(getTimespanss() + describe + "-->" + str + "\n");
            fw.flush();
            fw.close();
            System.out.println(fw);
        } catch (Exception e) {
        }
    }

    /**
     * 在SD卡上创建文件
     *
     * @throws IOException
     */
    public static File createSDFile(Context mContext) throws IOException {
//        File file = new File(getSDCardPath() + "/tingchebao/tcbdata.txt");
//        File file = getOutputMediaFile();
//        if (!file.exists()) {
//            file.createNewFile();
//        }
//        return file;
//        File mediaStorageDir = null;
//        if (Environment.getExternalStorageState() != null) {
//            mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "/TingCheBao");
//            if (!mediaStorageDir.exists()) {
//                if (!mediaStorageDir.mkdirs()) {
//                    Log.d("MyCameraApp", "failed to create directory");
//                    return null;
//                }
//            }
//        } else {
//            mediaStorageDir = new File(mContext.getCacheDir(), "/TingCheBao");
//            if (!mediaStorageDir.exists()) {
//                if (!mediaStorageDir.mkdirs()) {
//                    return null;
//                }
//            }
//        }
//        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
//                "Tcbdata.jpg");
        File file = null;
        try {
            String DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TingCheBao";
            String NAME = "tcbdata" + ".txt";
            File dir = new File(DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file = new File(dir, NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 删除文件
     *
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.exists()) { // 判断文件是否存在
            if (file.isFile()) { // 判断是否是文件
                file.delete(); // delete()方法 你应该知道 是删除的意思;
            } else if (file.isDirectory()) { // 否则如果它是一个目录
                File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
                for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
                    deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
                }
            }
            file.delete();
        }
    }

    /**
     * 判断手机类型
     *
     */
    public static String PhoneModel() {
        Build bd = new Build();
        return bd.MODEL;
    }

    /**
     * 判断是否优博讯910
     */
    public static boolean Is910() {
        if (!TextUtils.isEmpty(PhoneModel()) && (PhoneModel().equals("BPB-900")||PhoneModel().equals("SQ27C")))
            return true;
        else
            return false;
    }
    /**
     * 判断是否 睿思科900 Pe
     */
    public static boolean Is900() {
        if (!TextUtils.isEmpty(PhoneModel()) && PhoneModel().equals("Pe"))
            return true;
        else
            return false;
    }
    /**
     * 判断是否 商米600 V1-N
     */
    public static boolean IsSunMi() {
        if (!TextUtils.isEmpty(PhoneModel()) && PhoneModel().equals("V1-N"))
            return true;
        else
            return false;
    }
    /**
     * 文字部分变色
     * 这个其实可以用 Html.fromHtml()来代替，内部实现就是用的这个
     */
    public static SpannableStringBuilder textChangeColor(String text, int start, int end, int color) {
        SpannableStringBuilder span = new SpannableStringBuilder(text);
        span.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return span;
    }

    /**
     * 文字部分变色，并且变色部分可点击
     */
    private void TextClorClick(TextView textView, String content, String clickTxt, String id) {
        textView.setText(content);
        if (clickTxt != null && clickTxt != "") {
            SpannableStringBuilder spannable = new SpannableStringBuilder(content);
            int startIndex = content.indexOf(clickTxt);
            int endIndex = startIndex + clickTxt.length();
            //文字变色
//        spannable.setSpan(new ForegroundColorSpan(Color.RED),startIndex,endIndex
//                , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //文字点击
            spannable.setSpan(new TextClick(clickTxt, id), startIndex, endIndex
                    , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //一定要记得设置，不然点击不生效
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(spannable);
        }
    }

    private class TextClick extends ClickableSpan {
        String name;
        String id;

        public TextClick(String name, String id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public void onClick(View v) {
            //在此处理点击事件
            Intent intent = new Intent();

        }

        @Override
        public void updateDrawState(TextPaint tp) {
//            tp.setColor(context.getResources().getColor(R.color.text_bule));
        }
    }

    /**
     * 二进制转16进制吧好像
     *
     * @param src
     * @param len
     * @return
     */
    public static String bytesToHexString(byte[] src, int len) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        if (len <= 0) {
            return "";
        }
        for (int i = 0; i < len; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 将字符串中包含的小写字母转换成大写字母
     *
     * @param s
     * @return
     */
    public static String ToUpperKeys(String s) {
        char[] ctemp = s.toString().toCharArray();
        for (int i = 0; i < ctemp.length; i++) {
            if (ctemp[i] >= 97 && ctemp[i] <= 122) {
                ctemp[i] = String.valueOf(ctemp[i]).toUpperCase().charAt(0);
            } else {
                ctemp[i] = s.charAt(i);
            }
        }
        return new String(ctemp);
    }

    public static int StringLength(String str) {
        String[] number = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "(", ")", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        String[] spe = {"*", "-", ":",",","."};
        boolean isinnumber = false;
        boolean isinspe = false;
        double length = 0;
        String[] strarr = str.split("");
        for (int i = 0; i < strarr.length; i++) {
            String tmp = strarr[i];
            for (String j : number) {
                if (j.equals(tmp)) {
                    length += 0.5;
                    isinnumber = true;
                }
            }
            for (String p : spe) {
                if (p.equals(tmp)) {
                    length += 0.25;
                    isinspe = true;
                }
            }

            if (!isinnumber && !isinspe) {
                length += 1;
            }
            isinnumber = false;
            isinspe = false;
        }
//        Log.i("0000000000",str+"="+length+"=>"+Integer.parseInt(new java.text.DecimalFormat("0").format(length)));
        return Integer.parseInt(new java.text.DecimalFormat("0").format(length));
    }

    public static String cutString(String msg, int cutlength) {
        StringBuffer sb = new StringBuffer("");
        String[] msglist = msg.split("");
        for (int i = 0; i < msglist.length; i++) {
            System.out.println("i=" + i + "--item=" + msglist[i]);
            sb.append(msglist[i]);
            if (i > 0 && i % cutlength == 0) {
                if (!(msg.length() == cutlength))
                    sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 当前时间减一年
     * @throws ParseException
     */
    public static void ZddYear() throws ParseException {
        String currendate = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(System.currentTimeMillis()));
        String nowyear = currendate.substring(6,10);
        int newyear = Integer.parseInt(nowyear);
        String addyear = currendate.replace(nowyear,--newyear+"");
        long addyearmil = (new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(addyear)).getTime();
        new DeviceManager().setCurrentTime(addyearmil);
//        SharedPreferences preferences = getSharedPreferences();
//        int yearspan = preferences.getInt("yearspan",0);
//        preferences.edit().putInt("yearspan",--yearspan).commit();
    }

    /**
     * 当前时间加一年
     * @throws ParseException
     */
    public static void AddYear() throws ParseException {
        String currendate = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(System.currentTimeMillis()));
        String nowyear = currendate.substring(6,10);
        int newyear = Integer.parseInt(nowyear);
        String addyear = currendate.replace(nowyear,++newyear+"");
        long addyearmil = (new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(addyear)).getTime();
        new DeviceManager().setCurrentTime(addyearmil);
//        SharedPreferences preferences = getSharedPreferences();
//        int yearspan = preferences.getInt("yearspan",0);
//        preferences.edit().putInt("yearspan",++yearspan).commit();
    }
}
