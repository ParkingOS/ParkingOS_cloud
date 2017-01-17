package com.tq.zld.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.ParkCommentsAdapter;
import com.tq.zld.bean.ParkComment;
import com.tq.zld.bean.ParkInfo;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ParkCommentsFragment extends
        NetworkFragment<ArrayList<ParkComment>> {

    private static final int PAGE_SIZE = 20;

    public static final String ARG_PARK = "park";

    private View mPayView;
    private ParkCommentsAdapter mAdapter;
    private ImageButton mCommentButton;

    private ParkInfo mPark;

    private int mPage = 1;

    private Button mFootView;

    private HashMap<String, String> params;

    private Set<String> mPlates;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPark = getArguments().getParcelable(ARG_PARK);
        String key = getString(R.string.sp_plate_all);
        mPlates = TCBApp.getAppContext().getAccountPrefs().getStringSet(key, new HashSet<String>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_park_comments, container,
                false);
    }

    @Override
    protected String getTitle() {
        return "";
    }

    @Override
    public void onClick(View v) {
        if (v == mPayView) {
            onPayViewClicked();
        } else if (v == mFootView) {
            if (getString(R.string.load_more).equals(mFootView.getText())) {
                mFootView.setText(getString(R.string.loading));
                getData();
            }
        } else if (v == mCommentButton) {
            onCommentBtnClicked();
        }
    }

    private void onCommentBtnClicked() {
        ParkComment comment = (ParkComment) mCommentButton.getTag();
        Intent intent = new Intent(TCBApp.getAppContext(), MainActivity.class);
        Bundle args = new Bundle();
        args.putInt(CommentParkFragment.ARG_TYPE, CommentParkFragment.TYPE_PARK);
        args.putString(CommentParkFragment.ARG_ID, mPark.id);
        args.putParcelable(CommentParkFragment.ARG_COMMENT, comment);
        intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
        intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_COMMENT_PARK);
        startActivityForResult(intent, PayResultFragment.REQUEST_CODE_COMMENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PayResultFragment.REQUEST_CODE_COMMENT) {
            // 设置已评
            mPage = 1;
            getData();
        }
    }

    private void onPayViewClicked() {

        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            Toast.makeText(getActivity(), "请先登录！", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle args = new Bundle();
        args.putString(ChooseParkingFeeCollectorFragment.ARG_PARK_ID, mPark.id);
        args.putString(ChooseParkingFeeCollectorFragment.ARG_PARK_NAME, mPark.name);
        Intent intent = new Intent(TCBApp.getAppContext(), MainActivity.class);
        intent.putExtra(MainActivity.ARG_FRAGMENT, MainActivity.FRAGMENT_CHOOSE_COLLECTOR);
        intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
        startActivity(intent);
    }

    @Override
    public void onNetWorkResponse(ArrayList<ParkComment> comments) {

        // TODO 处理没有车场评论时的显示界面
        // if (mPage == 1 && (comments == null || comments.size() == 0)) {
        // showEmptyView("暂无评论", 0, null);
        // } else {
        showDataView();

        mAdapter.setData(mPage, comments);
        if (comments != null && comments.size() == PAGE_SIZE) {
            mPage++;
            mFootView.setText(getString(R.string.load_more));
        } else {
            mFootView.setText(getString(R.string.no_more_data));
        }
        // }

        // 判断车主有没有评论过
        LogUtils.i(getClass(), "plates: --->> " + mPlates.size());
        if (comments != null && comments.size() > 0 && mPlates.contains(comments.get(0).user)) {
            // 评论过
            mCommentButton.setImageResource(R.drawable.ic_park_comment_edit);
            mCommentButton.setTag(comments.get(0));
        } else {
            mCommentButton.setImageResource(R.drawable.ic_park_comment);
        }
    }

    @Override
    protected int getFragmentContainerResID() {
        return R.id.park_content;
    }

    @Override
    protected TypeToken<ArrayList<ParkComment>> getBeanListType() {
        return new TypeToken<ArrayList<ParkComment>>() {
        };
    }

    @Override
    protected Class<ArrayList<ParkComment>> getBeanClass() {
        return null;
    }

    @Override
    protected String getUrl() {
        return TCBApp.mServerUrl + "carinter.do";
    }

    @Override
    protected Map<String, String> getParams() {
        if (params == null) {
            params = new HashMap<>();
            params.put("action", "getcomment");
            params.put("comid", mPark.id);
            params.put("mobile", TCBApp.mMobile);
        }
        params.put("page", String.valueOf(mPage));
        return params;
    }

    @Override
    protected void initView(View view) {
        mPayView = view.findViewById(R.id.ll_park_pay);
        if (mPark == null || !"1".equals(mPark.epay) || "-1".equals(mPark.price)) {
            mPayView.setVisibility(View.GONE);
        } else {
            mPayView.setOnClickListener(this);
        }
        ListView commentView = (ListView) view.findViewById(R.id.lv_park_comments);

        // TODO 没有评论的空布局，处理方式待修改
        View emptyView = view.findViewById(R.id.rl_page_null);
        ((TextView) emptyView.findViewById(R.id.tv_page_null)).setText("暂无评论");
        // emptyView.setVisibility(View.VISIBLE);
        // ((ViewGroup) commentView.getParent()).addView(emptyView);
        commentView.setEmptyView(emptyView);

        View footView = View.inflate(getActivity(), R.layout.listitem_foot,
                null);
        mFootView = (Button) footView.findViewById(R.id.btn_listitem_foot);
        mFootView.setText(getString(R.string.load_more));
        mFootView.setOnClickListener(this);
        commentView.addFooterView(footView);
        if (mAdapter == null) {
            mAdapter = new ParkCommentsAdapter(getActivity());
        }
        commentView.setAdapter(mAdapter);

        // 设置评论按钮
        mCommentButton = (ImageButton) view.findViewById(R.id.ib_park_comments_edit);
        mCommentButton.setOnClickListener(this);
        if (!TextUtils.isEmpty(TCBApp.mMobile)) {
            mCommentButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mFootView != null && getString(R.string.loading).equals(mFootView.getText())) {
                mFootView.setText(getString(R.string.load_more));
            }
        }
    }
}
