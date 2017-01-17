package com.tq.zld.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Order;
import com.tq.zld.util.DateUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.map.ParkActivity;
import com.tq.zld.view.map.ParkingRedPacketsActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OrderDetailFragment extends NetworkFragment<Order> {

    public static final String ARG_ORDER_ID = "orderid";

    private View mParkView;
    private String mOrderID;
    private TextView mTotalTextView;
    private TextView mParkTextView;
    private TextView mDurationTextView;
    private TextView mOrderIDTextView;
    private TextView mDateTextView;
    private TextView mPayeeTextView;
    private ImageButton mPayeeMobileView;
    private Button mTipButton;
    private Button mCommentButton;
    private ImageButton mBonusButton;

    private Order mOrder;

    // private TextView mTicketMoneyTextView;
    // private TextView mBalanceMoneyTextView;
    // private TextView mOtherMoneyView;
    // private TextView mOtherNameTextView;
    // private View mTicketView;
    // private View mBalanceView;
    // private View mOtherPayView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOrderID = getArguments().getString(ARG_ORDER_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_order_detail, container,
                false);
    }

    public void setView(Order order) {

        if (TextUtils.isEmpty(order.getOrderid())) {
            LogUtils.e(getClass(), "--->> 订单编号不能为空！！！");
            return;
        }

        //设置时间
        String duration;
        if ("4".equals(order.ctype)) {
            duration = "直接付费";
        } else {
            duration = DateUtils.getMinDuration(order.getBtime(),
                    order.getEtime());
            if (TextUtils.isEmpty(duration)) {
                duration = "不足一分钟";
            }
        }
        mDurationTextView.setText(duration);


        mTotalTextView.setText("￥" + order.getTotal());
        mOrderIDTextView.setText(order.getOrderid());
        long beginTime = Long.parseLong(order.getBtime());
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm",
                Locale.CHINA);
        long endTime = Long.parseLong(order.getEtime());
        mDateTextView.setText(formatter.format(new Date(beginTime)) + " -- "
                + formatter.format(new Date(endTime)));
        mParkTextView.setText(order.getParkname());
        mPayeeTextView.setText(order.payee.name + "：" + order.payee.id);

        //设置打赏和评价布局
        boolean flag = "1".equals(order.comment);
        mCommentButton.setText(flag ? "已评" : "评价");
        mCommentButton.setActivated(!flag);
        mCommentButton.setClickable(!flag);

        flag = "1".equals(order.reward);
        mTipButton.setText(flag ? "已打赏过" : "去打赏");
        mTipButton.setActivated(!flag);
        mTipButton.setClickable(!flag);

        // 设置红包可不可见
        int visibility = TextUtils.isEmpty(order.getBonusid()) ? View.GONE : View.VISIBLE;
        mBonusButton.setVisibility(visibility);
    }

    private void openParkInfo(String parkId, String name) {
        Intent intent = new Intent(TCBApp.getAppContext(), ParkActivity.class);
        intent.putExtra(ParkActivity.ARG_ID, parkId);
        intent.putExtra(ParkActivity.ARG_NAME, name);
        startActivity(intent);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && mOrder != null && !"1".equals(mOrder.reward)) {
            getData();
        }
    }

    @Override
    protected String getTitle() {
        return "订单详情";
    }

    @Override
    public void onClick(View v) {
        if (v == mParkView) {
            if (mOrder == null) {
                return;
            }
            openParkInfo(mOrder.getParkid(), mOrder.getParkname());
        } else if (v == mPayeeMobileView) {
            onPayeeMobileViewClicked();
        } else if (v == mCommentButton) {
            onCommentButtonClicked();
        } else if (v == mTipButton) {
            onTipButtonClicked();
        } else if (v == mBonusButton) {
            onBonusButtonClicked();
        }
    }

    private void onBonusButtonClicked() {
        Intent intent = new Intent(TCBApp.getAppContext(),
                ParkingRedPacketsActivity.class);
        intent.putExtra(ParkingRedPacketsActivity.ARG_PID, mOrder.getBonusid());
        startActivity(intent);
    }

    private void onCommentButtonClicked() {

        if (mCommentButton.getText().toString().contains("已")) {
            Toast.makeText(getActivity(), "请勿重复评价！", Toast.LENGTH_SHORT).show();
            return;
        }

//        CommentParkFragment fragment
//                = CommentParkFragment.newInstance(
//                CommentParkFragment.TYPE_COLLECTOR, mOrder.getOrderid(), mOrder.payee);
//        replace(R.id.fragment_container, fragment, true);
        Intent intent = new Intent(TCBApp.getAppContext(), MainActivity.class);
        Bundle args = new Bundle();
        args.putInt(CommentParkFragment.ARG_TYPE, CommentParkFragment.TYPE_COLLECTOR);
        args.putParcelable(CommentParkFragment.ARG_COLLECTOR, mOrder.payee);
        args.putString(CommentParkFragment.ARG_ID, mOrderID);
        intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
        intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_COMMENT_PARK);
        startActivityForResult(intent, PayResultFragment.REQUEST_CODE_COMMENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PayResultFragment.REQUEST_CODE_COMMENT && resultCode == Activity.RESULT_OK) {
            mOrder.comment = "1";
            mCommentButton.setText("已评");
            mCommentButton.setClickable(false);
            mCommentButton.setActivated(false);
        }
    }

    private void onTipButtonClicked() {
        if (mTipButton.getText().toString().contains("已")) {
            Toast.makeText(getActivity(), "您已经打赏过啦～", Toast.LENGTH_SHORT).show();
            return;
        }
        RechargeFragment rechargeFragment = RechargeFragment.newInstance(mOrder);
        replace(R.id.fragment_container, rechargeFragment, true);
    }

    private void onPayeeMobileViewClicked() {
        if (mOrder != null && mOrder.payee != null
                && !TextUtils.isEmpty(mOrder.payee.mobile)) {
            Uri uri = Uri.parse("tel:" + mOrder.payee.mobile);
            startActivity(new Intent(Intent.ACTION_DIAL, uri));
        } else {
            Toast.makeText(getActivity(), "收费员未提供电话~", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected TypeToken<Order> getBeanListType() {
        return null;
    }

    @Override
    protected Class<Order> getBeanClass() {
        return Order.class;
    }

    @Override
    protected String getUrl() {
        return TCBApp.mServerUrl + "carowner.do";
    }

    @Override
    protected Map<String, String> getParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "orderdetail");
        params.put("orderid", mOrderID);
        params.put("mobile", TCBApp.mMobile);
        return params;
    }

    @Override
    protected void initView(View view) {
        mParkView = view.findViewById(R.id.rl_order_detail_park);
        mParkView.setOnClickListener(this);
        mDurationTextView = (TextView) view
                .findViewById(R.id.tv_order_detail_duration);
        mParkTextView = (TextView) view.findViewById(R.id.tv_order_detail_park);
        mOrderIDTextView = (TextView) view
                .findViewById(R.id.tv_order_detail_id);
        mTotalTextView = (TextView) view
                .findViewById(R.id.tv_order_detail_money);
        mDateTextView = (TextView) view.findViewById(R.id.tv_order_detail_date);
        mPayeeTextView = (TextView) view
                .findViewById(R.id.tv_order_detail_payee);
        mPayeeMobileView = (ImageButton) view
                .findViewById(R.id.ib_order_detail_payee);
        mPayeeMobileView.setOnClickListener(this);
        mCommentButton = (Button) view.findViewById(R.id.btn_order_detail_comment);
        mCommentButton.setOnClickListener(this);
        mTipButton = (Button) view.findViewById(R.id.btn_order_detail_tip);
        mTipButton.setOnClickListener(this);
        mBonusButton = (ImageButton) view.findViewById(R.id.ib_order_detail_bonus);
        mBonusButton.setOnClickListener(this);
        // mTicketMoneyTextView = (TextView) view
        // .findViewById(R.id.tv_order_detail_ticket_money);
        // mBalanceMoneyTextView = (TextView) view
        // .findViewById(R.id.tv_order_detail_balance_money);
        // mOtherMoneyView = (TextView) view
        // .findViewById(R.id.tv_order_detail_other_money);
        // mTicketView = view.findViewById(R.id.rl_order_detail_ticket);
        // mBalanceView = view.findViewById(R.id.rl_order_detail_balance);
        // mOtherPayView = view.findViewById(R.id.rl_order_detail_other);
        // mOtherNameTextView = (TextView) view
        // .findViewById(R.id.tv_order_detail_other);
    }

    @Override
    public void onNetWorkResponse(Order order) {
        if (order == null || TextUtils.isEmpty(order.getOrderid())) {
            showEmptyView("未查询到订单信息", 0, null);
        } else {
            mOrder = order;
            showDataView();
            setView(order);
        }
    }

    @Override
    protected int getFragmentContainerResID() {
        return R.id.fragment_container;
    }

}
