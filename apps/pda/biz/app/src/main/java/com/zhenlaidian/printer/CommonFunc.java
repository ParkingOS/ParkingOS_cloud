package com.zhenlaidian.printer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 类名: CommonFunc <br/><br/>
 * <p/>
 * 功能: <br/>
 * &nbsp;&nbsp;&nbsp;用于提供基础的函数㿼br/><br/>
 */
public class CommonFunc {

    /**
     * 功能:<br/>
     * &nbsp;&nbsp;让线程沉睡一定的毫秒的时闿
     *
     * @param iMilliSecond (霿沉睡的毫秒数).
     */
    public static void Sleep(int iMilliSecond) {

        try {
            Thread.sleep(iMilliSecond);
        } catch (InterruptedException e) {
        }

    }

    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;用于弹出MessageBox.<br/><br/>
     *
     * @param context           (用于显示MessageBox的上下文).
     * @param strTitle          (MessageBox的标题栏应该显示的文孿.
     * @param strMessage        (MessageBox的正文部分的文字).
     * @param OkBtnListener     (OK按钮的响应事仿.
     * @param CancelBtnListener (取消按钮的响应事仿.
     * @param isCancelable      (是否可以按_键，取消显示该界面).
     */
    public static void MessageBox(Context context, String strTitle,
                                  String strMessage,
                                  OnClickListener OkBtnListener,
                                  OnClickListener CancelBtnListener,
                                  boolean isCancelable) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(strMessage);
        builder.setTitle(strTitle);

        if (CancelBtnListener != null) {
            builder.setNegativeButton("取消", CancelBtnListener);
        }

        if (OkBtnListener != null) {
            builder.setPositiveButton("确定", OkBtnListener);
        }

        builder.create();
        builder.setCancelable(isCancelable);
        builder.show();
    }


    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;弹出MessageBox(自带OK按钮).<br/><br/>
     *
     * @param context    (上下斿.<br/>
     * @param strTitle   (标题).<br/>
     * @param strMessage (内容).<br/><br/>
     */
    public static void MessageBox(Context context,
                                  String strTitle,
                                  String strMessage) {

        ///////////////////////
        //
        // 响应OK按钮的事仿
        OnClickListener OkBtnListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };

        //////////////////////
        //
        // 设置弹出对话框的内容.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(strMessage);
        builder.setTitle(strTitle);

        //////////////////////
        //
        // 设置OK按钮的响应事仿
        builder.setPositiveButton("确定", OkBtnListener);

        //////////////////////
        //
        // MessageBox创建并显礿
        builder.create();
        builder.show();

    } // MessageBox


    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;用于在线程中显示Toast的界面，用于提示信息显示.<br/>
     *
     * @param context    (上下斿.
     * @param strMessage (霿显示的消恿.
     * @param iDuration  显示的等待时闿可_?
     *                   {@link Toast#LENGTH_SHORT LENGTH_SHORT} 咿
     *                   {@link Toast#LENGTH_LONG LENGTH_LONG}<br/>
     * @param iGravity   显示Toast的风格，比如居中筿{@link Gravity#CENTER_HORIZONTAL CENTER_HORIZONTAL}
     * @see Toast
     * @see Gravity
     */
    public static void ShowToastInThread(final Context context,
                                         final String strMessage,
                                         final int iDuration,
                                         final int iGravity) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                Toast toast = Toast.makeText(context.getApplicationContext(),
                        strMessage, Toast.LENGTH_LONG);

                toast.setDuration(iDuration);
                toast.getView().setBackgroundColor(Color.BLACK);
                toast.setGravity(iGravity, 0, 0);

                toast.show();
            }
        });


    } // ShowToastInThread


    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;用于在线程中显示Toast的界靿默认在屏幕中央显礿，用于提示信息显礿<br/>
     *
     * @param context    (上下斿.
     * @param strMessage (霿显示的消恿.
     * @param iDuration  显示的等待时闿可_?
     *                   {@link Toast#LENGTH_SHORT LENGTH_SHORT} 咿
     *                   {@link Toast#LENGTH_LONG LENGTH_LONG}<br/>
     * @see Toast
     * @see Gravity
     */
    public static void ShowToastInThread(final Context context,
                                         final String strMessage,
                                         final int iDuration) {

        ShowToastInThread(context, strMessage, iDuration,
                Gravity.CENTER_VERTICAL |
                        Gravity.CENTER_HORIZONTAL);


    } // ShowToastInThread


    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;用于在线程中显示Toast的界靿默认在屏幕中央显礿默认长时间显礿，用于提示信息显礿<br/>
     *
     * @param context    (上下斿.
     * @param strMessage (霿显示的消恿.
     * @param iDuration  显示的等待时闿可_?
     *                   {@link Toast#LENGTH_SHORT LENGTH_SHORT} 咿
     *                   {@link Toast#LENGTH_LONG LENGTH_LONG}<br/>
     * @see Toast
     * @see Gravity
     */
    public static void ShowToastInThread(final Context context,
                                         final String strMessage) {

        ShowToastInThread(context, strMessage, Toast.LENGTH_LONG,
                Gravity.CENTER_VERTICAL |
                        Gravity.CENTER_HORIZONTAL);
    }


    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;用于获取当前设备里的时间信息. <br/>
     *
     * @return 当前时间丿949广1朿1旿0＿0＿0 的时间差的毫秒数〿
     */
    public static long GetTickTime() {
        long iCurrentTickCount = System.currentTimeMillis();
        return iCurrentTickCount;
    }


    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;从HTTP服务器上获取POST的HTTP报文.<br/><br/>
     *
     * @param strUrl      (霿POST数据的URL地址).
     * @param strPostData (霿POST的数捿如extcode=pppppppp).
     * @return 从HTTP服务器上获取到HTML报文.
     */
    public static String GetHtmlByPostFromHttp(String strUrl, String strPostData) {

        ////////////////////
        //
        // 获取URL对象.
        URL url = null;
        try {
            url = new URL(strUrl);
        } catch (MalformedURLException e) {
            return "";
        }

        ////////////////////
        //
        // 获取服务器的链接.
        HttpURLConnection openConnection = null;
        try {
            openConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            return "";
        }

        ////////////////////
        //
        // 设置可以附带数据.
        openConnection.setDoOutput(true);

        ////////////////////
        //
        // 弿输出POST数据.
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(openConnection.getOutputStream(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            openConnection.disconnect();
            return "";
        } catch (IOException e) {
            openConnection.disconnect();
            return "";
        }

        ///////////////////////
        //
        // 向服务器写入POST的数捿
        try {
            outputStreamWriter.write(strPostData);
            outputStreamWriter.flush();
            outputStreamWriter.close();
        } catch (IOException e) {
            try {
                outputStreamWriter.close();
            } catch (IOException e1) {
            }
            ;
            openConnection.disconnect();
            return "";
        }

        ///////////////////////
        //
        // 循环读出服务器的相应信息.
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(openConnection.getInputStream());
        } catch (IOException e) {
            try {
                outputStreamWriter.close();
            } catch (IOException e1) {
            }
            ;
            openConnection.disconnect();
            return "";
        }


        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String line = "";
        String strContent = "";
        try {
            while ((line = bufferedReader.readLine()) != null) {

                if (strContent.length() != 0) {
                    strContent = strContent + "\n";
                }

                strContent = strContent + line;
            }
        } catch (IOException e) {
            return "";
        }

        return strContent;

    }


    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;以GET方式从服务器上获取HTTP报文.<br/><br/>
     *
     * @param strUrl (要获取HTML的URL地址)
     * @return HTTP报文.<br/><br/>
     */
    public static String GetHtmlFromHttp(String strUrl) {

        String strHtml = "";
        URL url = null;

        try {
            url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();// 利用HttpURLConnection对象,我们可以从网络中获取网页数据
            conn.setConnectTimeout(20 * 1000);
            conn.setRequestMethod("GET"); // HttpURLConnection是_HTTP协议请求path路径的，承霿设置请求方式,可以不设置，因为默认为GET
            if (conn.getResponseCode() == 200) {// 判断请求码是否是200码，否则失败
                InputStream is = conn.getInputStream(); // 获取输入浿
                byte[] data = readStream(is); // 把输入流转换成字符数绿
                strHtml = new String(data);
            }

        } catch (Exception e) {
            return "";
        }


        return strHtml;
    }


    /**
     * 功能:
     * 用于读取Stream内的信息.
     * 参数:
     *
     * @param inputStream
     * @return
     * @throws Exception
     */
    private static byte[] readStream(InputStream inputStream) throws Exception {

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            bout.write(buffer, 0, len);
        }
        bout.close();
        inputStream.close();


        return bout.toByteArray();
    }


    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;判断SD卡是否存圿
     *
     * @return true(有SD卿 <br/>
     * false(没有SD卿
     */
    public static boolean IsHasSdCard() {
        boolean bRet = false;

        try {
            bRet = Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED);
        } catch (Exception e) {

        }

        return bRet;
    }


    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;获取SD卡的路径.<br/><br/>
     *
     * @return null (表示该设备没有SD卡，或迦法访问SD卿.<br/>
     * 非空字符丿(SD卡的路径).
     */
    public static String GetSdCardPath() {

        String strRet = null;

        if (IsHasSdCard() == false) {
            return strRet;
        }

        File fileSdcardDirectory = Environment.getExternalStorageDirectory();

        strRet = fileSdcardDirectory.getAbsolutePath();

        return strRet;
    }

    /**
     * 功能:<br/>
     * &nbsp;&nbsp;&nbsp;用于获取SD卡的路径，如果SD卡不用，则提示SD卡不可用〿
     *
     * @param context
     * @return
     */
    public static String GetSdCardPath(Context context) {

        String strRet = null;

        if (IsHasSdCard() == false) {
            ShowToastInThread(context, "SD卡不可用,请插入SD卡！");
            return strRet;
        }

        File fileSdcardDirectory = Environment.getExternalStorageDirectory();

        strRet = fileSdcardDirectory.getAbsolutePath();

        return strRet;

    }


}
