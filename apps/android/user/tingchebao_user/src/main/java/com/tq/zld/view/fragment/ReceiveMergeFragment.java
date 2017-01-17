package com.tq.zld.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.easemob.chat.EMConversation;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Coupon;
import com.tq.zld.im.bean.User;
import com.tq.zld.im.db.UserDao;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.map.WebActivity;

/**
 * Created by GT on 2015/9/17.
 */
public class ReceiveMergeFragment extends BaseFragment implements View.OnClickListener {

    private View mIsbuyView;
    private TextView mMoneyTextView;
    private Coupon mCoupon;
    View mSelectView;
    View mTicketView;
    private TextView mResultTextView;

    private long begin = 0;
    private EMConversation conversation;
    private ImageView mFriendImageView;
    private DisplayImageOptions options;
    private String mToChatName;
    private UserDao userDao;
    private Button mMergeButton;
    private TextView mFriendTitleText;
    private TextView mFriendNickText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToChatName = getArguments().getString("from");
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_chat_head_default)
                .showImageOnFail(R.drawable.ic_chat_head_default)
                .showImageOnLoading(R.drawable.ic_chat_head_default)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        userDao = new UserDao(TCBApp.getAppContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merge_receive, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
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


    private void initView(View view) {
        view.findViewById(R.id.btn_select_ticket).setOnClickListener(this);
        view.findViewById(R.id.btn_begin_merge).setOnClickListener(this);
        mMergeButton = (Button) view.findViewById(R.id.btn_begin_merge);
        mMergeButton.setEnabled(false);

        mSelectView = view.findViewById(R.id.rl_merge_select);
        mTicketView = view.findViewById(R.id.rl_merge_ticket);
        mMoneyTextView = (TextView) view.findViewById(R.id.tv_merge_ticket);
        mIsbuyView = view.findViewById(R.id.iv_merge_isbuy);
        mResultTextView = (TextView) view.findViewById(R.id.tv_merge_result);

        mFriendTitleText = (TextView) view.findViewById(R.id.tv_merge_from_title);
        mFriendNickText = (TextView) view.findViewById(R.id.tv_merge_from_nick);
        mFriendImageView = (ImageView) view.findViewById(R.id.iv_merge_from_head);
        view.findViewById(R.id.tv_merge_rule).setOnClickListener(this);

        User user = userDao.getContact(mToChatName);
        ImageLoader.getInstance().displayImage(user.getAvatar(), mFriendImageView, options);
        mFriendNickText.setText(user.getNick());
        mFriendTitleText.setText(String.format("来自车主%s的停车券，等待合体...", user.getPlate()));
    }

    @Override
    protected String getTitle() {
        return "停车券合体";
    }

    final static int REQUEST_CODE = 11;
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_select_ticket){
//            Intent intent = new Intent();
//            intent.setClass(getActivity(), SelectTicketMergeActivity.class);
//            startActivityForResult(intent, REQUEST_CODE);

            Intent intent = new Intent();
            intent.setClass(getActivity(), MainActivity.class);
            intent.putExtra(MainActivity.ARG_FRAGMENT,MainActivity.FRAGMENT_TICKET_MERGE);
            startActivityForResult(intent, REQUEST_CODE);

        } else if(v.getId() == R.id.btn_begin_merge){
            openProgressMerge();
        } else if (v.getId() == R.id.tv_merge_rule) {
            preOpenMergeRule();
        }
    }

    private void selectComplete(Coupon coupon){
        if (coupon != null) {
            mCoupon = coupon;
        } else {
            return;
        }

        getArguments().putParcelable("coupon", coupon);

        mSelectView.setVisibility(View.GONE);
        mTicketView.setVisibility(View.VISIBLE);

        mMoneyTextView.setText(String.format("%s元停车券", coupon.money));
        if (coupon.isbuy == 1){
            mIsbuyView.setVisibility(View.VISIBLE);
        } else {
            mIsbuyView.setVisibility(View.GONE);
        }

        mMergeButton.setEnabled(true);
    }

    private void openProgressMerge(){
        ProgressMergeFragment fragment = new ProgressMergeFragment();
        fragment.setArguments(this.getArguments());
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_CODE == requestCode && resultCode == Activity.RESULT_OK) {
            Coupon coupon = data.getParcelableExtra("coupon");
            LogUtils.i("get result >> " + coupon.toString());
            selectComplete(coupon);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TCBApp.getAppContext().cancelPendingRequests(this);
    }

}
