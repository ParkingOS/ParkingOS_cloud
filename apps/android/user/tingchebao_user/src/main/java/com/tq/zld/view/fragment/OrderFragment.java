package com.tq.zld.view.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewStub.OnInflateListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response.Listener;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Order;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.protocal.SimpleVolleyErrorListener;
import com.tq.zld.util.DateUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.map.CaptureActivity;
import com.tq.zld.view.map.MapActivity;
import com.tq.zld.wxapi.WXPayEntryActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class OrderFragment extends DialogFragment implements OnClickListener {

    public static final int REQUEST_CODE_CAMERA = 0;

    public static final String ARG_ORDER = "order";
    public static final String ARG_SETTLE = "settle";

    private TextView mTitleView;
    private TextView mParkTextView;
    private TextView mStartTextView;
    private TextView mDurationTextView;
    private TextView mNonOrderView;
    //    private TextView mRemarkTextView;
//    private Button mRemarkButton;
    private Button mPayButton;
    private Button mWaitButton;
    private Button mScanButton;
    private Button mSettleButton;
    //    private View mRemarkView;
    private View mCloseView;

    private Order mOrder;
    private boolean mSettle = false;

//    private File mRemarkImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_ORDER)) {
            mOrder = getArguments().getParcelable(ARG_ORDER);
            mSettle = getArguments().getBoolean(ARG_SETTLE, false);
            LogUtils.i("订单信息：--->> " + mOrder.toString());
            LogUtils.i("settle>> " + mSettle);
        }
        setCancelable(false);
    }

//    private File genRemarkImageFile(final String orderid) {
//
//        if (TextUtils.isEmpty(orderid)) {
//            return null;
//        }
//
//        // 删除旧的图片文件
//        File dir = getActivity().getExternalFilesDir(
//                Environment.DIRECTORY_PICTURES);
//        File remarkImageDir = new File(dir.getAbsolutePath()
//                + File.separator + "remark" + File.separator);
//        if (!remarkImageDir.exists()) {
//            remarkImageDir.mkdirs();
//        } else {
//            File[] listFiles = remarkImageDir
//                    .listFiles(new FilenameFilter() {
//
//                        @Override
//                        public boolean accept(File dir, String filename) {
//                            return !filename.contains(orderid);
//                        }
//                    });
//            for (File file : listFiles) {
//                file.delete();
//                LogUtils.i(OrderFragment.class, "delete file: --->> "
//                        + file.getName());
//            }
//        }
//        return new File(remarkImageDir, orderid
//                + ".jpg");
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.fragment_order, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTitleView = (TextView) view.findViewById(R.id.tv_order_title);
        mNonOrderView = (TextView) view.findViewById(R.id.tv_order_non);
        mScanButton = (Button) view.findViewById(R.id.btn_order_scan);
        mScanButton.setOnClickListener(this);
        mScanButton.setVisibility(View.GONE);
        mPayButton = (Button) view.findViewById(R.id.btn_order_pay);
        mPayButton.setOnClickListener(this);
        mPayButton.setVisibility(View.GONE);
        mWaitButton = (Button) view.findViewById(R.id.btn_order_wait);
        mWaitButton.setOnClickListener(this);
        mWaitButton.setVisibility(View.GONE);
        mCloseView = view.findViewById(R.id.ib_order_close);
        mCloseView.setOnClickListener(this);
        mSettleButton = (Button) view.findViewById(R.id.btn_order_settle);
        mSettleButton.setOnClickListener(this);
        mSettleButton.setVisibility(View.GONE);

        if (mOrder == null) {
            getOrder();
        } else {
            refreshView(mOrder);
        }
    }

    public void inflateOrderView() {
        mNonOrderView.setVisibility(View.GONE);
        ViewStub vs = (ViewStub) getView().findViewById(R.id.vs_fragment_order);
        vs.setOnInflateListener(new OnInflateListener() {

            @Override
            public void onInflate(ViewStub stub, View inflated) {
                mParkTextView = (TextView) inflated
                        .findViewById(R.id.tv_order_park);
                mStartTextView = (TextView) inflated
                        .findViewById(R.id.tv_order_start);
                mDurationTextView = (TextView) inflated
                        .findViewById(R.id.tv_order_duration);
//                mRemarkView = inflated.findViewById(R.id.rl_order_remark);
//                mRemarkButton = (Button) inflated
//                        .findViewById(R.id.btn_order_remark);
//                mRemarkButton.setOnClickListener(OrderFragment.this);
//                mRemarkTextView = (TextView) inflated
//                        .findViewById(R.id.tv_order_remark);
            }
        });
        vs.inflate();
    }

    private void getOrder() {
        // http://s.tingchebao.com/zld/carowner.do?action=currentorder&mobile=15375242041

        HashMap<String, String> params = new HashMap<>();
        params.put("action", "currentorder");
        params.put("mobile", TCBApp.mMobile);
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "carowner.do", params);
        Listener<Order> listener = new Listener<Order>() {

            @Override
            public void onResponse(Order order) {
                refreshView(order);
            }
        };
        GsonRequest<Order> request = new GsonRequest<>(url, Order.class,
                listener, new SimpleVolleyErrorListener());
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    public void refreshView(Order order) {
        this.mOrder = order;
        if (order == null || TextUtils.isEmpty(order.getOrderid())) {
            ((MapActivity) getActivity()).updateOrderButton(Order.STATE_PAYED);
            mNonOrderView.setText("你没有在停订单");
            mScanButton.setVisibility(View.VISIBLE);
        } else {
            ((MapActivity) getActivity()).updateOrderButton(order.getState());
            mScanButton.setVisibility(View.GONE);
            inflateOrderView();

            String duration = DateUtils.getSecDuration(order.getBtime(),
                    order.getEtime());
            if (TextUtils.isEmpty(duration)) {
                duration = "不足一分钟";
            }

            String template = "HH:mm";
            Calendar begin = Calendar.getInstance();
            begin.setTimeInMillis(Long.parseLong(order.getBtime()));
            Calendar end = Calendar.getInstance();
            end.setTimeInMillis(Long.parseLong(order.getEtime()));

            // 判断开始结束时间是否在同一天
            if (end.get(Calendar.DAY_OF_MONTH) != begin
                    .get(Calendar.DAY_OF_MONTH)
                    || end.get(Calendar.MONTH) != begin.get(Calendar.MONTH)) {
                template = "MM月dd日 HH:mm";
            }
            SimpleDateFormat formatter = new SimpleDateFormat(template,
                    Locale.CHINA);
            String state = order.getState();


            switch (state) {
                case Order.STATE_PENDING:// 等待结算
                    duration = "已停" + duration;
//                    mRemarkView.setVisibility(View.VISIBLE);
                    mPayButton.setVisibility(View.GONE);
                    mSettleButton.setVisibility(View.GONE);
                    mWaitButton.setVisibility(View.VISIBLE);
                    mStartTextView.setText(formatter.format(new Date(Long
                            .parseLong(order.getBtime()))) + "入场");
                    break;

                case Order.STATE_PAYING:// 等待支付
                case Order.STATE_PAY_FAILED:
                    duration = "停车" + duration;
                    setOrderTitle(order);
                    Date beginD = new Date(Long.parseLong(order.getBtime()));
                    Date endD = new Date(Long.parseLong(order.getEtime()));
                    mStartTextView.setText(formatter.format(beginD) + " -- "
                            + formatter.format(endD));
                    mPayButton.setVisibility(View.VISIBLE);
                    mWaitButton.setVisibility(View.GONE);
                    mSettleButton.setVisibility(View.GONE);
                    break;
            }

            if (mSettle) {
                mPayButton.setVisibility(View.GONE);
                mWaitButton.setVisibility(View.GONE);
                mSettleButton.setVisibility(View.VISIBLE);
                setOrderTitle(order);
            } else {
                mSettleButton.setVisibility(View.GONE);
            }

            mParkTextView.setText(order.getParkname());
            mDurationTextView.setText(duration);

            // 本地保存的订单备注不为空，并且以本订单号结尾（标识是本次停车的备注）
//            if (!Order.STATE_PAYING.equals(order.getState())) {
//                refreshRemarkView(order);
//            } else {
//                mRemarkView.setVisibility(View.GONE);
//            }
        }
    }

    private void setOrderTitle(Order order) {
        mTitleView.setTextColor(getResources().getColor(R.color.text_green));
        mTitleView.setTypeface(Typeface.DEFAULT_BOLD);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mTitleView.setText(Html.fromHtml("金额:  <big><big>￥"
                + order.getTotal() + "</big></big>"));
    }

//    private void refreshRemarkView(Order order) {
//
//        if (order == null) {
//            return;
//        }
//
//        String remark = TCBApp.getAppContext().readString(R.string.sp_remark, "");
//        if (!TextUtils.isEmpty(remark) && remark.endsWith(order.getOrderid())) {
//            remark = remark.replace(order.getOrderid(), "");
//            if (!TextUtils.isEmpty(remark)) {
//                mRemarkTextView.setText(remark);
//            }
//        }
//
//        if (mRemarkImage == null) {
//            mRemarkImage = genRemarkImageFile(order.getOrderid());
//        }
//
//        if (mRemarkImage != null && mRemarkImage.exists()) {
//            LogUtils.i(OrderFragment.class, "mRemarkImage path: --->> " + mRemarkImage.getAbsolutePath());
//            mRemarkButton.setText("查看照片");
//        }
//    }

    @Override
    public void onClick(View v) {
//        if (v == mRemarkButton) {
//            onRemarkBtnClicked();
//        } else
        if (v == mPayButton || v == mSettleButton) {
            onPayButtonClicked();
        } else if (v == mScanButton) {
            // if (MapActivity.mHandler != null) {
            // MapActivity.mHandler
            // .sendEmptyMessage(MapActivity.MSG_WHAT_QRSCAN);
            // }
            // dismiss();
            startActivity(new Intent(TCBApp.getAppContext(),
                    CaptureActivity.class));
        } else if (v == mCloseView) {
            TCBApp.getAppContext().cancelPendingRequests(
                    this.getClass().getSimpleName());
            dismiss();
        }

    }

//    private void onRemarkBtnClicked() {
//
//        if (mRemarkImage == null) {
//            return;
//        }
//
//        if ("查看照片".equals(mRemarkButton.getText())) {
//            RemarkFragment.newInstance(mRemarkImage.getAbsolutePath(), mOrder.getOrderid())
//                    .show(getFragmentManager(), "RemarkFragment");
//        } else {
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            // path为保存图片的路径，执行完拍照以后能保存到指定的路径下
//            Uri imageUri = Uri.fromFile(mRemarkImage);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//            startActivityForResult(intent, REQUEST_CODE_CAMERA);
//        }
//    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        LogUtils.i(OrderFragment.class,
//                "onActivityResult: --->> " + requestCode + "," + resultCode);
//        // MIUI系统调用完相机后可能回不到此Activity
//        if (requestCode == REQUEST_CODE_CAMERA
//                && resultCode == Activity.RESULT_OK) {
//
//            // 打开标记对话框
//            new Handler().postDelayed(new Runnable() {
//
//                @Override
//                public void run() {
//
//                    // 更新标记布局信息
////                    refreshRemarkView(mOrder);
//
//                    RemarkFragment.newInstance(mRemarkImage.getAbsolutePath(),
//                            mOrder.getOrderid()).show(getFragmentManager(),
//                            "RemarkFragment");
//                }
//            }, 1000);
//
//            LogUtils.i(OrderFragment.class,
//                    "take picture succeed, save to: --->> " + mRemarkImage);
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    private void onPayButtonClicked() {
        if (mOrder == null || TextUtils.isEmpty(mOrder.getOrderid())) {
            return;
        }
        Intent intent = new Intent(TCBApp.getAppContext(),
                WXPayEntryActivity.class);
        intent.putExtra(WXPayEntryActivity.ARG_TOTALFEE, mOrder.getTotal());
        intent.putExtra(WXPayEntryActivity.ARG_PRODTYPE,WXPayEntryActivity.PROD_PARKING_FEE);
        intent.putExtra(WXPayEntryActivity.ARG_SUBJECT,"支付停车费_" + mOrder.getParkname());
        intent.putExtra(WXPayEntryActivity.ARG_ORDER_ID, mOrder.getOrderid());
        startActivity(intent);
        ((MapActivity) getActivity()).updateOrderButton(Order.STATE_PAYED);
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        TCBApp.getAppContext().cancelPendingRequests(this);
        mSettle = false;
    }
}
