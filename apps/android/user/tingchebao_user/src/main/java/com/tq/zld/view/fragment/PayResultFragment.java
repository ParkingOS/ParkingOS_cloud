package com.tq.zld.view.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewStub.OnInflateListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.PayResult;
import com.tq.zld.util.DensityUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.map.ParkingRedPacketsActivity;
import com.tq.zld.wxapi.WXPayEntryActivity;

public class PayResultFragment extends DialogFragment implements
        OnClickListener {

    public static final int REQUEST_CODE_COMMENT = 0;

    private static final String ARG_PAY_RESULT = "result";
    private static final String ARG_PROD_TYPE = "prodType";

    ImageButton mCloseButton;
    ImageView mPayResultImage;
    TextView mPayResultTextView;
    Button mCommentButton;
    Button mBonusButton;
    ViewStub mPayResultViewStub;
    ViewStub mPayResultWithBonusViewStub;

    private boolean mAlreadyShown = false;

    private PayResult result;

    public static PayResultFragment newInstance(PayResult result, String prodType) {
        if (result == null) {
            result = new PayResult(PayResult.PAY_RESULT_FAILED, "", "");
        }
        LogUtils.i(PayResultFragment.class,
                "pay result: --->> " + result.toString());

        PayResultFragment fragment = new PayResultFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PAY_RESULT, result);
        args.putString(ARG_PROD_TYPE, prodType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        setAlreadyShown(true);
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        setAlreadyShown(true);
        return super.show(transaction, tag);
    }

    @Override
    public void onStart() {
        super.onStart();
        // safety check
        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            int width = DensityUtils.dip2px(getActivity(), 180);
            int height = width + 20;
            if (isPositiveInteger(result.tips)) {

                // 有可分享的红包
                width = DensityUtils.dip2px(getActivity(), 280);
                height = DensityUtils.dip2px(getActivity(), 300);
            } else if (isPositiveInteger(result.errmsg)) {

                // 可评论车场
                height = DensityUtils.dip2px(getActivity(), 240);
            } else if (result.bonusid > 0) {
                // 有可分享的红包
                width = DensityUtils.dip2px(getActivity(), 240);
                height = DensityUtils.dip2px(getActivity(), 320);
            }
            window.setBackgroundDrawable(new ColorDrawable(0));
            window.setLayout(width, height);
            // window.setGravity(Gravity.CENTER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.fragment_pay_result, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setCancelable(false);

        mCloseButton = (ImageButton) view
                .findViewById(R.id.ib_pay_result_close);
        mCloseButton.setOnClickListener(this);

        mPayResultImage = (ImageView) view.findViewById(R.id.iv_pay_result);
        mPayResultTextView = (TextView) view.findViewById(R.id.tv_pay_result);

        result = getArguments().getParcelable(ARG_PAY_RESULT);
        String prodType = getArguments().getString(ARG_PROD_TYPE);
        switch (result.result) {
            case PayResult.PAY_RESULT_SUCCESS:

                // 支付成功，取消对话框同时finish Activity
                getDialog().setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        onCloseBtnClicked();
                    }
                });

                switch (prodType) {
                    case WXPayEntryActivity.PROD_PAY_MONEY:
                    case WXPayEntryActivity.PROD_PARKING_FEE:
                        // 判断有无停车券礼包
                        if (isPositiveInteger(result.tips)) {
                            mPayResultImage
                                    .setImageResource(R.drawable.img_pay_success_withbonus);
                            mPayResultTextView.setText("支付成功\n恭喜获得停车券大礼包！");
                            mPayResultTextView.setTextColor(getResources().getColor(R.color.text_red));
                            mPayResultWithBonusViewStub = (ViewStub) view
                                    .findViewById(R.id.vs_pay_result_bonus);
                            mPayResultWithBonusViewStub
                                    .setOnInflateListener(new OnInflateListener() {

                                        @Override
                                        public void onInflate(ViewStub stub, View inflated) {
                                            mCommentButton = (Button) inflated
                                                    .findViewById(R.id.btn_pay_result_left);
                                            mCommentButton
                                                    .setOnClickListener(PayResultFragment.this);
                                            mBonusButton = (Button) inflated
                                                    .findViewById(R.id.btn_pay_result_right);
                                            mBonusButton
                                                    .setOnClickListener(PayResultFragment.this);
                                        }
                                    });
                            mPayResultWithBonusViewStub.inflate();
                        } else {
                            mPayResultImage.setImageResource(R.drawable.img_pay_success);
                            mPayResultTextView.setText("支付成功");
                            if (isPositiveInteger(result.errmsg)) {
                                // errmsg表示车场ID
                                mPayResultViewStub = (ViewStub) view
                                        .findViewById(R.id.vs_pay_result);
                                mPayResultViewStub
                                        .setOnInflateListener(new OnInflateListener() {

                                            @Override
                                            public void onInflate(ViewStub stub,
                                                                  View inflated) {
                                                mCommentButton = (Button) inflated
                                                        .findViewById(R.id.btn_pay_result);
                                                mCommentButton
                                                        .setOnClickListener(PayResultFragment.this);
                                            }
                                        });
                                mPayResultViewStub.inflate();
                            } else {
                                setCancelable(true);
                            }
                        }
                        break;
                    case WXPayEntryActivity.PROD_RECHARGE:
                        if (result.bonusid != 0) {
                            // 有充值礼包
                            mPayResultImage
                                    .setImageResource(R.drawable.img_recharge_success_with_bonus);
                            mPayResultTextView.setText("充值成功\n恭喜获得停车宝充值大礼包！");
                            mPayResultTextView.setTextColor(getResources().getColor(R.color.text_red));
                            // errmsg表示车场ID
                            mPayResultViewStub = (ViewStub) view
                                    .findViewById(R.id.vs_pay_result);
                            mPayResultViewStub
                                    .setOnInflateListener(new OnInflateListener() {

                                        @Override
                                        public void onInflate(ViewStub stub,
                                                              View inflated) {
                                            mCommentButton = (Button) inflated
                                                    .findViewById(R.id.btn_pay_result);
                                            mCommentButton.setBackgroundResource(R.drawable.shape_solid_button_red);
                                            mCommentButton.setText("查看礼包");
                                            mCommentButton.setTextColor(Color.WHITE);
                                            mCommentButton
                                                    .setOnClickListener(PayResultFragment.this);
                                        }
                                    });
                            mPayResultViewStub.inflate();
                        }
                        break;
                    default:
                        setCancelable(true);
                        break;
                }
                break;

            default:
                setCancelable(true);
                mPayResultImage.setImageResource(R.drawable.img_pay_failure);
                // mPayResultTextView.setText(String.format("支付失败\n%s",
                // result.tips));
                if (TextUtils.isEmpty(result.errmsg)) {
                    result.errmsg = "支付失败";
                }
                mPayResultTextView.setText(result.errmsg);
                break;
        }
    }

    private boolean isPositiveInteger(String tips) {
        return !TextUtils.isEmpty(tips) && !tips.startsWith("-")
                && TextUtils.isDigitsOnly(tips) && !"0".equals(tips);
    }

    @Override
    public void onClick(View v) {
        if (v == mCloseButton) {
            onCloseBtnClicked();
        } else if (v == mCommentButton) {
            onCommentBtnClicked();
        } else if (v == mBonusButton) {
            onBonusBtnClicked();
        }
    }

    private void onBonusBtnClicked() {
        if (!isPositiveInteger(result.tips)) {
            return;
        }
        Intent intent = new Intent(TCBApp.getAppContext(),
                ParkingRedPacketsActivity.class);
        intent.putExtra(ParkingRedPacketsActivity.ARG_PID, result.tips);
        startActivity(intent);
    }

    private void onCommentBtnClicked() {
        String text = mCommentButton.getText().toString();
        if (text.contains("已评价")) {

            // 已评价过
            Toast.makeText(getActivity(), "请勿重复评价！", Toast.LENGTH_SHORT).show();
        } else if (text.contains("评")) {

            // 支付完订单可以评论
            if (!isPositiveInteger(result.errmsg)) {
                return;
            }
            Intent intent = new Intent(TCBApp.getAppContext(), MainActivity.class);
            intent.putExtra(MainActivity.ARG_FRAGMENT,
                    MainActivity.FRAGMENT_COMMENT_PARK);
            Bundle args = new Bundle();
            args.putInt(CommentParkFragment.ARG_TYPE, CommentParkFragment.TYPE_COLLECTOR);
            args.putString(CommentParkFragment.ARG_ID, result.errmsg);
            intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
            startActivityForResult(intent, REQUEST_CODE_COMMENT);
        } else if (text.contains("礼包")) {
            // 查看充值礼包
            Intent intent = new Intent(getActivity(), ParkingRedPacketsActivity.class);
            intent.putExtra(ParkingRedPacketsActivity.ARG_PID, String.valueOf(result.bonusid));
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_COMMENT
                && resultCode == Activity.RESULT_OK) {
            mCommentButton.setText("已评价过");

            // 打开订单详情界面
            Intent intent = new Intent(TCBApp.getAppContext(), MainActivity.class);
            Bundle args = new Bundle();
            args.putString(OrderDetailFragment.ARG_ORDER_ID, result.errmsg);
            intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_ORDER_DETAIL);
            intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
            startActivity(intent);

            //关闭支付界面
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    private void onCloseBtnClicked() {
        if (getDialog().isShowing()) {
            dismiss();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (PayResult.PAY_RESULT_SUCCESS.equals(result.result) && getActivity() != null) {
            getActivity().finish();
        }
    }

    public boolean isAlreadyShown() {
        return mAlreadyShown;
    }

    public void setAlreadyShown(boolean mAlreadyShown) {
        this.mAlreadyShown = mAlreadyShown;
    }
}
