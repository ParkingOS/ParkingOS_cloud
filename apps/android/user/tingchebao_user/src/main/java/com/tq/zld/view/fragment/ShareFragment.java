package com.tq.zld.view.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.pay.Keys;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.QRCodeEncoder;
import com.tq.zld.view.map.RecommendActivity;

import java.util.HashMap;

public class ShareFragment extends BaseFragment implements OnClickListener,
        IUiListener {

    private ImageView ivQRCode;// 二维码图片

    private String shareUrl;
    private IWXAPI iWXApi;
    private Tencent mTencent;// QQ分享实例
    private Bitmap thumbImage;// 分享的图片
    private static final String APPID_QQ = "1102349481";
    private Bundle bundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_share, container,
                false);
        initView(rootView);
        getShareUrl();
        return rootView;
    }

    private void initView(View rootView) {
        ivQRCode = (ImageView) rootView.findViewById(R.id.iv_share_qrcode);
        rootView.findViewById(R.id.tv_share_qq).setOnClickListener(this);
        rootView.findViewById(R.id.tv_share_wx_session)
                .setOnClickListener(this);
        rootView.findViewById(R.id.tv_share_wx_timeline).setOnClickListener(
                this);
        rootView.findViewById(R.id.tv_share_sms).setOnClickListener(this);
    }

    // carowner.do?action=getrecomurl&mobile=15801482643
    private void getShareUrl() {
        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                "请稍后...", false, true);
        dialog.setCanceledOnTouchOutside(false);
        // carowner.do?action=regcarmsg&mobile=%@
        String url = TCBApp.mServerUrl + "carowner.do";
        LogUtils.i(getClass(), "获取推荐短连接url: --->> " + url);
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "getrecomurl");
        params.put("mobile", TCBApp.mMobile);
        new AQuery(TCBApp.getAppContext()).ajax(url, params, String.class,
                new AjaxCallback<String>() {

                    @Override
                    public void callback(String url, String object,
                                         AjaxStatus status) {
                        dialog.dismiss();
                        LogUtils.i(RecommendActivity.class,
                                "获取推荐短连接result: --->> " + object);
                        if (!TextUtils.isEmpty(object)) {
                            shareUrl = object;
                            refreshView();
                        } else {
                            Toast.makeText(getActivity(), "网络异常，请稍后再试...",
                                    Toast.LENGTH_SHORT).show();
                        }
                        super.callback(url, object, status);
                    }
                });
        // HashMap<String, String> params = new HashMap<String, String>();
        // params.put("action", "long2short");
        // params.put("long_url",
        // "http://www.tingchebao.com/zld/carservice.do?pid=16003&name=刘发才");
        // JSONObject jo = new JSONObject(params);
        // new AQuery(MyApp.getAppContext())
        // .post("https://api.weixin.qq.com/cgi-bin/shorturl?access_token=cAKKARZ1euB4n3rTCCYC_ljDe06dtULuAHGERUmucWfS46QQATW3ahsIDAi0R5L3HdMaWHjDMHYt2XKkEyxNcHOz-o2G5TrON_THH-rtKbE",
        // jo, String.class, new AjaxCallback<String>() {
        // @Override
        // public void callback(String url, String object,
        // AjaxStatus status) {
        // LogUtils.i(ShareFragment.class,
        // "微信转短连接result：--->> " + object);
        // super.callback(url, object, status);
        // }
        // });
    }

    private void refreshView() {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels * 3 / 5;
        ivQRCode.setImageBitmap(new QRCodeEncoder().encode2BitMap(shareUrl,
                width, width));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_share_qq:
                shareToQQ();
                break;
            case R.id.tv_share_wx_session:
                shareToWX(SendMessageToWX.Req.WXSceneSession);
                break;
            case R.id.tv_share_wx_timeline:
                shareToWX(SendMessageToWX.Req.WXSceneTimeline);
                break;
            case R.id.tv_share_sms:
                sendSMS();
                break;
        }
    }

    private void shareToQQ() {
        if (mTencent == null) {
            mTencent = Tencent.createInstance(APPID_QQ, TCBApp.getAppContext());
        }
        if (bundle == null) {
            bundle = new Bundle();
            bundle.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE,
                    QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
            bundle.putString(QQShare.SHARE_TO_QQ_TITLE, "我在用停车宝，邀你一起来!");
            bundle.putString(QQShare.SHARE_TO_QQ_SUMMARY, "这是给新用户的红包，快来领取吧~");
            bundle.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareUrl);
            bundle.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, getString(R.string.url_release)
                    + "share.png");
            bundle.putString(QQShare.SHARE_TO_QQ_APP_NAME, "返回停车宝");
        }
        // bundle.putInt(QQShare.SHARE_TO_QQ_EXT_INT, "其他附加功能");//可指定分享到QQ空间
        mTencent.shareToQQ(getActivity(), bundle, this);
    }

    private void sendSMS() {
        Uri smsToUri = Uri.parse("smsto:" + "");
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", "我在用停车宝付车费，送新人红包邀你一起来！领取地址：" + shareUrl);
        startActivity(intent);
    }

    private void shareToWX(int reqScene) {
        if (iWXApi == null) {
            initIWXApi();
        }
        if (!checkWXAppAliable()) {
            return;
        }
        WXWebpageObject webObject = new WXWebpageObject();
        webObject.webpageUrl = shareUrl;

        WXMediaMessage msg = new WXMediaMessage(webObject);
        msg.mediaObject = webObject;
        msg.title = "我在用停车宝付车费，送新人红包邀你一起来！";
        msg.description = "这是给新用户的红包，快来领取吧！";
        if (thumbImage == null) {
            thumbImage = BitmapFactory.decodeResource(getResources(),
                    R.drawable.share);
        }
        msg.setThumbImage(thumbImage);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.scene = reqScene;
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;

        iWXApi.sendReq(req);
    }

    private boolean checkWXAppAliable() {
        if (!iWXApi.isWXAppInstalled()) {
            Toast.makeText(getActivity(), "请先安装微信客户端！", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        if (!iWXApi.isWXAppSupportAPI()) {
            Toast.makeText(getActivity(), "请先升级微信客户端！", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        return true;
    }

    private void initIWXApi() {
        iWXApi = WXAPIFactory.createWXAPI(getActivity(), Keys.WXPAY_APPID);
        iWXApi.registerApp(Keys.WXPAY_APPID);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mTencent != null) {
            mTencent.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (thumbImage != null) {
            thumbImage.recycle();
            thumbImage = null;
        }
        super.onDestroy();
    }

    @Override
    public void onCancel() {
        if (getActivity() != null)
            Toast.makeText(getActivity(), "分享取消~", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onComplete(Object arg0) {
        if (getActivity() != null)
            Toast.makeText(getActivity(), "分享成功~", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(UiError arg0) {
        LogUtils.i(getClass(), "QQ分享错误信息：--->> " + arg0.errorMessage);
        if (getActivity() != null)
            Toast.makeText(getActivity(), "分享出现错误了~", Toast.LENGTH_SHORT)
                    .show();
    }

    @Override
    protected String getTitle() {
        return "邀请好友";
    }
}
