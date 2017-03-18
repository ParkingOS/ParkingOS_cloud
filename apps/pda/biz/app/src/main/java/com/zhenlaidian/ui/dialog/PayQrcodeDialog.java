package com.zhenlaidian.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.util.CommontUtils;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by TCB on 2016/4/18.
 */
public class PayQrcodeDialog extends Dialog{
    private Context context;
    private TextView txtTitle;
    private CheckBox cbwechat,cbalipay;

    public PayQrcodeDialog(Context context) {
        super(context);
        this.context = context;
    }
    private Bitmap bitmap=null;
    private Bitmap alibitmap=null;
    private Handler handler;
    private String title;

    private String orderid;
    private String total;
    private String endtime;
    public PayQrcodeDialog(Context context, Bitmap bitmap, Handler handler, String title) {
        super(context,R.style.nfcfinishdialog);
        this.context = context;
        this.bitmap = bitmap;
        this.handler = handler;
        this.title = title;
    }
    public PayQrcodeDialog(Context context, Bitmap bitmap, Handler handler, String title,String orderid,String total,String endtime) {
        super(context,R.style.nfcfinishdialog);
        this.context = context;
        this.bitmap = bitmap;
        this.handler = handler;
        this.title = title;
        this.orderid = orderid;
        this.total = total;
        this.endtime = endtime;
        isgetali = false;
    }
    ImageView qrimg;
    boolean isgetali = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_dialog_payqrcode_layout);
        txtTitle = ((TextView) findViewById(R.id.dialog_title));
        txtTitle.setText(title);
        cbalipay = ((CheckBox) findViewById(R.id.cbalipay));
        cbwechat = ((CheckBox) findViewById(R.id.cbwechat));
        qrimg = ((ImageView) findViewById(R.id.dialog_qrcode));
        qrimg.setBackgroundDrawable(new BitmapDrawable(bitmap));
        findViewById(R.id.dialog_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message m = new Message();
                m.what = 3;
                handler.sendMessage(m);
                dismiss();
            }
        });
        cbwechat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbwechat.setChecked(true);
                cbalipay.setChecked(false);
                qrimg.setBackgroundDrawable(new BitmapDrawable(bitmap));
            }
        });
        cbalipay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cbalipay.setChecked(true);
                cbwechat.setChecked(false);
                if(isgetali){
                    qrimg.setBackgroundDrawable(new BitmapDrawable(alibitmap));
                }else{
                    getAlipayQr(orderid,total,endtime);
                }
            }
        });
    }

    /**
     * http://127.0.0.1/zld/collectorrequest.do?action=zfbpayqr&orderid=&total=
     * @param orderid
     * @param total
     * @param endtime
     */
    private void getAlipayQr(String orderid,String total,String endtime){
            if (!IsNetWork.IsHaveInternet(context)) {
                Toast.makeText(context, "获取二维码失败，请检查网络！", Toast.LENGTH_SHORT).show();
                return;
            }
            AQuery aQuery = new AQuery(context);
            String path = Config.getUrl(context);
            String url = path + "collectorrequest.do?action=zfbpayqr&token="
                    + SharedPreferencesUtils.getIntance(context).getToken() + "&orderid=" + orderid + "&total=" +
                    total +"&endtime=" + endtime+ "&out=json";
            MyLog.i("PayQrcodeDialog", "请求支付宝二维码的URL-->>" + url);
//        final ProgressDialog dialog = ProgressDialog.show(this, "加载中...", "获取车场信息数据...", true, true);
            aQuery.ajax(url, String.class, new AjaxCallback<String>() {

                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    MyLog.i("PayQrcodeDialog-->>", "解支付宝二维码 " + object);
                    if (object != null) {
//{"state":"1","errmsg":"","qrcode":"https://qr.alipay.com/bax09880tbbqc11rvxwz2080"}
                        JSONObject obj=null;
                        try {
                            obj = new JSONObject(object);
                            String state = obj.getString("state");
                            String errmsg = obj.getString("errmsg");
                            String qrcode = obj.getString("qrcode");
                            alibitmap = CommontUtils.addLogo(qrcode,CommontUtils.Drawable2Bitmap(context,R.drawable.alipay));
                            qrimg.setBackgroundDrawable(new BitmapDrawable(alibitmap));
                            isgetali = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context,"参数转换失败！",Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(context,"请求支付宝二维码失败！",Toast.LENGTH_LONG).show();
                    }
                }

            });

    }

    @Override
    public void dismiss() {
        super.dismiss();
        if(alibitmap!=null&&!alibitmap.isRecycled()){
            alibitmap.recycle();
            alibitmap = null;
            System.gc();
        }
        if(bitmap!=null&&!bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
            System.gc();
        }
    }
}
