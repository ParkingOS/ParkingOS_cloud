package com.tq.zld.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.MergeResult;
import com.tq.zld.im.bean.User;
import com.tq.zld.im.db.UserDao;
import com.tq.zld.util.IMUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.ToastUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.im.FriendActivity;
import com.tq.zld.view.map.WebActivity;

import java.util.Map;

/**
 * Created by GT on 2015/9/18.
 */
public class ResultMergeFragment extends NetworkFragment<MergeResult> implements View.OnClickListener {

    private TextView mTitleTextView;
    private TextView mFriendTitleTextView;
    private TextView mTicketTextView;
    private TextView mFriendTicketTextView;
    private ImageView mOwnImageView;
    private ImageView mFriendImageView;
    private TextView mTipTextView;
    private TextView mTip2TextView;
    private TextView mFriendTipTextView;
    private TextView mFriendTip2TextView;
    private TextView mOwnWinTextView;
    private TextView mFriendWinTextView;
    private TextView mLabel;
    private View mOwnView;
    private View mFriendView;

    int colorGreen = 0;
    int colorRed = 0;
    int colorCyan = 0;

    private String mID;
    private DisplayImageOptions options;
    private UserDao userDao;
    private String mToChatName;
    private View mResultView;
    private TextView mRuleText;
    private TextView mBeginText;
    private View mOwnIsBuyView;
    private View mFriendIsBuyView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mID = getArguments().getString("mid");
        mToChatName = getArguments().getString("toChatName");
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_chat_head_large)
                .showImageOnFail(R.drawable.ic_chat_head_large)
                .showImageOnLoading(R.drawable.ic_chat_head_large)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        userDao = new UserDao(TCBApp.getAppContext());
        colorGreen = getActivity().getResources().getColor(R.color.text_green);
        colorRed = getActivity().getResources().getColor(R.color.text_red);
        colorCyan = getActivity().getResources().getColor(R.color.merge_cyan);
        LogUtils.i("mid>" + mID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merge_result, container, false);
    }

    @Override
    protected TypeToken<MergeResult> getBeanListType() {
        return null;
    }

    @Override
    protected Class<MergeResult> getBeanClass() {
        return MergeResult.class;
    }

    @Override
    protected String getUrl() {
        return TCBApp.mServerUrl + "carinter.do";
    }

    @Override
    protected Map<String, String> getParams() {
        //?action=viewticketuion&mobile=15801482643&id=8
        Map<String, String> params = URLUtils.createParamsMap();
        params.put("action", "viewticketuion");
        params.put("mobile", TCBApp.mMobile);
        params.put("id",mID);
        return params;
    }

    @Override
    protected void initView(View view) {
        mOwnView = view.findViewById(R.id.rl_merge_own);
        mFriendView = view.findViewById(R.id.rl_merge_friend);

        mTitleTextView = (TextView) view.findViewById(R.id.tv_merge_title);
        mFriendTitleTextView = (TextView) view.findViewById(R.id.tv_merge_title_other);
        mTicketTextView = (TextView) view.findViewById(R.id.tv_merge_ticket);
        mFriendTicketTextView = (TextView) view.findViewById(R.id.tv_merge_ticket_other);
        mOwnImageView = (ImageView) view.findViewById(R.id.iv_chat_head);
        mFriendImageView = (ImageView) view.findViewById(R.id.iv_friend_head);
        mTipTextView = (TextView) view.findViewById(R.id.tv_merge_own_tip);
        mTip2TextView = (TextView) view.findViewById(R.id.tv_merge_own_tip_2);
        mFriendTipTextView = (TextView) view.findViewById(R.id.tv_merge_friend_tip);
        mFriendTip2TextView = (TextView) view.findViewById(R.id.tv_merge_friend_tip_2);
        mOwnWinTextView = (TextView) view.findViewById(R.id.tv_merge_own_win);
        mFriendWinTextView = (TextView) view.findViewById(R.id.tv_merge_friend_win);

        mTip2TextView.setOnClickListener(this);
        mFriendTip2TextView.setOnClickListener(this);

        mOwnIsBuyView = view.findViewById(R.id.iv_merge_isbuy);
        mFriendIsBuyView = view.findViewById(R.id.iv_merge_friend_isbuy);

        mLabel = (TextView) view.findViewById(R.id.tv_merge_label);

        mResultView = view.findViewById(R.id.ll_merge_result);
        mRuleText = (TextView) view.findViewById(R.id.tv_merge_rule);
        mBeginText = (TextView) view.findViewById(R.id.tv_merger_begin);

        view.findViewById(R.id.btn_merge_play_again).setOnClickListener(this);
        view.findViewById(R.id.btn_merge_play_wechat).setOnClickListener(this);
        mRuleText.setOnClickListener(this);

//        ((MainActivity)getActivity()).getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackClicked();
//            }
//        });

        User friend = userDao.getContact(mToChatName);
        String myUrl = IMUtils.getHead();
        ImageLoader.getInstance().displayImage(myUrl, mOwnImageView, options);
        ImageLoader.getInstance().displayImage(friend.getAvatar(), mFriendImageView, options);
    }

    private void initData(MergeResult response) {
        LogUtils.i(response.toString());

        //对方还没有接受合体请求之前的查询。
        if ("-1".equals(response.result)) {
            mResultView.setVisibility(View.GONE);
//            mRuleText.setVisibility(View.VISIBLE);
            mBeginText.setText("等待合体");

            mTitleTextView.setText(response.ownticket.name);
            mFriendTitleTextView.setText("等待对方出券");
            mTicketTextView.setText(String.format("%s元", response.ownticket.money));
            mFriendTicketTextView.setText("?元");

            if (response.ownticket.isbuy == 1) {
                mOwnIsBuyView.setVisibility(View.VISIBLE);
            } else {
                mOwnIsBuyView.setVisibility(View.INVISIBLE);
            }

            if (!TextUtils.isEmpty(response.errmsg)) {
                ToastUtils.show(getActivity(), response.errmsg);
            }

        } else {
            mResultView.setVisibility(View.VISIBLE);
//            mRuleText.setVisibility(View.GONE);
            mBeginText.setText("合体前");

            mTitleTextView.setText(response.ownticket.name);
            mFriendTitleTextView.setText(response.friendticket.name);
            mTicketTextView.setText(String.format("%s元", response.ownticket.money));
            mFriendTicketTextView.setText(String.format("%s元", response.friendticket.money));

            if (response.ownticket.isbuy == 1) {
                mOwnIsBuyView.setVisibility(View.VISIBLE);
            } else {
                mOwnIsBuyView.setVisibility(View.INVISIBLE);
            }

            if (response.friendticket.isbuy == 1) {
                mFriendIsBuyView.setVisibility(View.VISIBLE);
            } else {
                mFriendIsBuyView.setVisibility(View.INVISIBLE);
            }

            mLabel.setText(response.errmsg);

            mTipTextView.setText(response.ownret.toptip);
            mFriendTipTextView.setText(response.friendret.toptip);

            //有一方赢
            if ("1".equals(response.result)) {
//                mLabel.setBackgroundResource(R.drawable.shape_merge_result_bg_green);
                mLabel.setTextColor(colorGreen);
                mTip2TextView.setVisibility(View.VISIBLE);
                mFriendTip2TextView.setVisibility(View.VISIBLE);

                mTip2TextView.setText(response.ownret.buttip);
                mFriendTip2TextView.setText(response.friendret.buttip);
            } else if ("0".equals(response.result)) {//停车宝赢
//                mLabel.setBackgroundResource(R.drawable.shape_merge_result_bg_red);
                mLabel.setTextColor(colorRed);
                mTip2TextView.setVisibility(View.GONE);
                mFriendTip2TextView.setVisibility(View.GONE);
            }

            //自己赢
            if ("1".equals(response.ownret.win)) {
                mTipTextView.setTextColor(colorCyan);
//                mOwnView.setBackgroundResource(R.drawable.bg_merge_result_ok);
                mOwnWinTextView.setTextColor(colorCyan);
            } else {
                mTipTextView.setTextColor(colorRed);
//                mOwnView.setBackgroundResource(R.drawable.bg_merge_result_fail);
                mOwnWinTextView.setTextColor(colorRed);
            }

            //基友赢
            if ("1".equals(response.friendret.win)) {
                mFriendTipTextView.setTextColor(colorCyan);
//                mFriendView.setBackgroundResource(R.drawable.bg_merge_result_ok);
                mFriendWinTextView.setTextColor(colorCyan);
            } else {
                mFriendTipTextView.setTextColor(colorRed);
//                mFriendView.setBackgroundResource(R.drawable.bg_merge_result_fail);
                mFriendWinTextView.setTextColor(colorRed);
            }

            mOwnWinTextView.setText(response.ownret.righttip);
            mFriendWinTextView.setText(response.friendret.righttip);
        }

    }

    private void openMergeRule(String url){
        LogUtils.i("openMergeRule");
        if (TextUtils.isEmpty(url)) {
            url = getString(R.string.url_merge_help);
        }

        Intent intent = new Intent();
        intent.setClass(getActivity(), WebActivity.class);
        intent.putExtra(WebActivity.ARG_TITLE, "券合体帮助");
        intent.putExtra(WebActivity.ARG_URL, url);
        startActivity(intent);
    }

    private void preOpenMergeRule(){
        String url = URLUtils.getWXArticleURL(URLUtils.ArticleType.uoinrule);
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                LogUtils.i(s);
                openMergeRule(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                openMergeRule("");
            }
        });

        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void openGameStage2Help(){
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_stage2_help, null);
        final Dialog dialog = new AlertDialog.Builder(getActivity()).setView(v).setCancelable(false).create();
        v.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    /**
     * 返回键按下
     */
    private void onBackClicked() {
        LogUtils.i("onBackClicked");
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    protected String getTitle() {
        return "合体停车券";
    }

    @Override
    protected void onNetWorkResponse(MergeResult response) {
        if (response != null) {
            showDataView();
            initData(response);
        } else {
            showEmptyView("网络错误,点击重试~", 0, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getData();
                }
            });
        }
    }

    @Override
    protected int getFragmentContainerResID() {
        return R.id.fragment_container;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_merge_play_again) {
            LogUtils.i("再玩一次");
            Intent intent = new Intent(getActivity(), FriendActivity.class);
            intent.putExtra(FriendActivity.ARGS_PLAY_AGAIN, true);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_merge_play_wechat) {
            LogUtils.i("跟微信好友玩");
        } else if (v.getId() == mRuleText.getId()) {
            //查看停车券规则
            preOpenMergeRule();
        } else if (v == mTip2TextView || v == mFriendTip2TextView) {
            //打开第二关资格卡提示
            openGameStage2Help();
        }
    }


}
