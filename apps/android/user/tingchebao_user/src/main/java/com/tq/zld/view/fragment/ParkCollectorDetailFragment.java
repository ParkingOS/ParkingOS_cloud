package com.tq.zld.view.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.ParkingFeeCollector;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by clare on 15/6/13.
 */
public class ParkCollectorDetailFragment extends NetworkFragment<ParkingFeeCollector> {

    public static final String ARG_COLLECTOR = "collector";

    private ParkingFeeCollector mCollector;

    private TextView mTotalServiceTextView;
    private TextView mLastWeekServiceTextView;
    private TextView mRewardCountTextView;
    private TextView mRewardMoneyTextView;
    private TextView mCommentCountTextView;

    public static ParkCollectorDetailFragment newInstance(ParkingFeeCollector collector) {
        ParkCollectorDetailFragment fragment = new ParkCollectorDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_COLLECTOR, collector);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCollector = getArguments().getParcelable(ARG_COLLECTOR);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_park_collector_detail, container, false);
    }

    @Override
    protected TypeToken<ParkingFeeCollector> getBeanListType() {
        return null;
    }

    @Override
    protected Class<ParkingFeeCollector> getBeanClass() {
        return ParkingFeeCollector.class;
    }

    @Override
    protected String getUrl() {
        return TCBApp.mServerUrl + "carinter.do";
    }

    @Override
    protected Map<String, String> getParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "puserdetail");
        params.put("uid", mCollector.id);
        return params;
    }

    @Override
    protected void initView(View view) {
        // 支付按钮
        view.findViewById(R.id.btn_collector_detail_pay).setOnClickListener(this);

        //电话按钮
        view.findViewById(R.id.ib_collector_detail_phone).setOnClickListener(this);

        // 设置头像
        ImageView photoView = (ImageView) view.findViewById(R.id.iv_collector_detail_photo);
        if ("23".equals(mCollector.online)) {
            ImageLoader.getInstance().displayImage(
                    "drawable://" + R.drawable.img_parkingfee_collector_online,
                    photoView);
        } else {
            ImageLoader
                    .getInstance()
                    .displayImage(
                            "drawable://"
                                    + R.drawable.img_parkingfee_collector_offline,
                            photoView);
        }

        // 设置姓名，车场
        TextView nameView = (TextView) view.findViewById(R.id.tv_collector_detail_name);
        nameView.setText(mCollector.name);
        TextView parkNameView = (TextView) view.findViewById(R.id.tv_collector_detail_parkname);
        parkNameView.setText(mCollector.parkname);

        mTotalServiceTextView = (TextView) view.findViewById(R.id.tv_collector_detail_service_total);
        mLastWeekServiceTextView = (TextView) view.findViewById(R.id.tv_collector_detail_service_last_week);
        mRewardCountTextView = (TextView) view.findViewById(R.id.tv_collector_detail_reward_count);
        mRewardMoneyTextView = (TextView) view.findViewById(R.id.tv_collector_detail_reward_money);
        mCommentCountTextView = (TextView) view.findViewById(R.id.tv_collector_detail_comment_count);

        view.findViewById(R.id.ll_collector_detail_comment).setOnClickListener(this);

    }

    @Override
    protected String getTitle() {
        return "收费员详情";
    }

    @Override
    protected void onNetWorkResponse(ParkingFeeCollector response) {

        //复制属性
        mCollector.rcount = response.rcount;
        mCollector.money = response.money;
        mCollector.scount = response.scount;
        mCollector.wcount = response.wcount;
        mCollector.ccount = response.ccount;
        mCollector.mobile = response.mobile;
        updateView(mCollector);
        showDataView();
    }

    private void updateView(ParkingFeeCollector collector) {
        //设置服务次数
        mTotalServiceTextView.setText(String.format("服务次数: %d", collector.scount));

        //设置最近一周服务次数
        mLastWeekServiceTextView.setText(String.format("最近一周（%d次）", collector.wcount));

        // 设置打赏总数
        mRewardCountTextView.setText(String.format("收到打赏：%d笔", collector.rcount));

        //设置打赏总额
        mRewardMoneyTextView.setText(String.format("共%1$.2f元", collector.money));

        //设置评价数
        mCommentCountTextView.setText(String.format("收到评价：%d", collector.ccount));
    }

    @Override
    protected int getFragmentContainerResID() {
        return R.id.fragment_container;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_collector_detail_pay) {
            getActivity().onBackPressed();
        } else if (v.getId() == R.id.ib_collector_detail_phone) {
            onPhoneButtonClicked();
        } else if (v.getId() == R.id.ll_collector_detail_comment) {
            onCommentViewClicked();
        }
    }

    private void onCommentViewClicked() {
        replace(R.id.fragment_container, CommentFragment.newInstance(mCollector.id), true);
    }

    private void onPhoneButtonClicked() {
        if (mCollector == null || TextUtils.isEmpty(mCollector.mobile)) {
            Toast.makeText(getActivity(), "收费员未提供电话信息！", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = Uri.parse("tel:" + mCollector.mobile);
        getActivity().startActivity(new Intent(
                Intent.ACTION_DIAL, uri));
    }
}
