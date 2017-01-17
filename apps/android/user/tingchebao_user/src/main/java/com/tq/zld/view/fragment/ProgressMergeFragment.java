package com.tq.zld.view.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMMessage;
import com.easemob.chat.TextMessageBody;
import com.easemob.exceptions.EaseMobException;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Coupon;
import com.tq.zld.bean.MergeWeight;
import com.tq.zld.im.IMConstant;
import com.tq.zld.im.bean.User;
import com.tq.zld.im.db.UserDao;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.IMUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.TimeUtils;
import com.tq.zld.util.ToastUtils;
import com.tq.zld.util.URLUtils;
import com.tq.zld.widget.AutoTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by GT on 2015/9/19.
 */
public class ProgressMergeFragment extends BaseFragment {

    private TextView mOtherTitleTextView;
    private TextView mTicketTextView;
    private TextView mOtherTicketTextView;
    private TextView mDestTextView;
    private View mIsBuyView;

    private String mID;
    private String mToChatName;
    private EMMessage mMsg;
    private Coupon mCoupon;

    private int mResult = 0;
    private String errorMsg = "";
    private int mOwnWeigth = 0;

    private long begin = 0;
    private EMConversation mConversation;
    private ImageView mUserImageView;
    private ImageView mFriendImageView;
    private DisplayImageOptions options;
    private UserDao userDao;

    private AutoTextView mOwnWeightText;
    private AutoTextView mFriendWeightText;
    private AutoTextView mWinRateText;

    private Dialog mDialog;

    private boolean isMerge = false;
    private View mEmptyView;
    private ScrollView mScrollView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_merge_progress, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
    }

    private void initData() {
        mID = getArguments().getString("mid");
        mToChatName = getArguments().getString("from");
        mMsg = getArguments().getParcelable("msg");
        mCoupon = getArguments().getParcelable("coupon");
        mConversation = EMChatManager.getInstance().getConversation(mToChatName);

        if (mCoupon.isbuy == 1){
            mIsBuyView.setVisibility(View.VISIBLE);
        } else {
            mIsBuyView.setVisibility(View.GONE);
        }

        mTicketTextView.setText(String.format("%s元", mCoupon.money));

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_chat_head_large)
                .showImageOnFail(R.drawable.ic_chat_head_large)
                .showImageOnLoading(R.drawable.ic_chat_head_large)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        userDao = new UserDao(TCBApp.getAppContext());
        User user = userDao.getContact(mToChatName);
        String myUrl = IMUtils.getHead();
        ImageLoader.getInstance().displayImage(user.getAvatar(), mFriendImageView, options);
        ImageLoader.getInstance().displayImage(myUrl, mUserImageView, options);
        mOtherTitleTextView.setText(String.format("来自车主%s的停车券", user.getPlate()));

//        responseMerge();
//        getMergeWeight();
    }


    private void initView(View view) {
        mOtherTitleTextView = (TextView) view.findViewById(R.id.tv_merge_title_other);
        mTicketTextView = (TextView) view.findViewById(R.id.tv_merge_ticket);
        mOtherTicketTextView = (TextView) view.findViewById(R.id.tv_merge_ticket_other);
        mDestTextView = (TextView) view.findViewById(R.id.tv_merge_desc);
        mIsBuyView = view.findViewById(R.id.iv_merge_isbuy);
        mUserImageView = (ImageView) view.findViewById(R.id.iv_chat_head);
        mFriendImageView = (ImageView) view.findViewById(R.id.iv_friend_head);

        mOwnWeightText = (AutoTextView) view.findViewById(R.id.tv_merge_own_weight);
        mFriendWeightText = (AutoTextView) view.findViewById(R.id.tv_merge_friend_weight);
        mWinRateText = (AutoTextView) view.findViewById(R.id.tv_merge_win_rate);

        mEmptyView = view.findViewById(R.id.ll_page_null);
        mEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMergeWeight();
            }
        });

        mScrollView = (ScrollView) view.findViewById(R.id.scroll_merge);
    }

    private void getMergeWeight() {
        mEmptyView.setVisibility(View.GONE);

        if (mCoupon == null) {
            return;
        }

        //carinter.do?action=ticketuioninfo&mobile=13641309140&tid=46401&id=461
        Map<String, String> params = URLUtils.createParamsMap();
        params.put("action", "ticketuioninfo");
        params.put("mobile", TCBApp.mMobile);
        params.put("tid", mCoupon.id);
        params.put("id", mID);

        String url = URLUtils.createUrl(TCBApp.mServerUrl, "carinter.do", params);
        GsonRequest<MergeWeight> request = new GsonRequest<>(url, MergeWeight.class, new Response.Listener<MergeWeight>() {
            @Override
            public void onResponse(MergeWeight mergeWeight) {
                if (mergeWeight == null) {
                    return;
                }
                isMerge = true;
                mResult = mergeWeight.result;
                List<CharSequence> ownLines = new ArrayList<>();
                ownLines.add("正在核对双方出的停车券是否为一奇一偶");
                AutoTextView.OnAutoEndListner onAutoEndListner = null;
                errorMsg = mergeWeight.errmsg;
                if (mergeWeight.result == 1) {
                    mOwnWeigth = mergeWeight.own.uiontotal;

                    ownLines.add(Html.fromHtml("<br>正在计算你的合体值"));
                    ownLines.add(Html.fromHtml(String.format("<br>停车券金额%s元,合体值<font color='#32a669'>+%d</font>", mCoupon.money, mergeWeight.own.ticketvalue)));
                    ownLines.add(Html.fromHtml(String.format("<br>有效期%s天,合体值<font color='#32a669'>+%d</font>", TimeUtils.millisToDay(Long.parseLong(mCoupon.limitday)), mergeWeight.own.expvalue)));

                    if (mCoupon.isbuy == 1) {
                        ownLines.add(Html.fromHtml(String.format("<br>停车券是购买属性,合体值<font color='#32a669'>*%d</font>", mergeWeight.own.buyvalue)));
                    }

                    ownLines.add(Html.fromHtml(String.format("<br><font color='#32a669'>您的合体值:%s</font>", mergeWeight.own.uiontotal)));
                    ownLines.add(Html.fromHtml("<br>正在获取对方的合体值"));
                    ownLines.add(Html.fromHtml(String.format("<br><font color='#32a669'>对方合体值为%d</font>", mergeWeight.friend.uiontotal)));

                    mWinRateText.setLines(new String[]{String.format("您本轮的合体的胜算为:%s", mergeWeight.winrate)});
                    mWinRateText.setDuration(800);
                    mWinRateText.setVisibility(View.VISIBLE);

                    onAutoEndListner = new AutoTextView.OnAutoEndListner() {
                        @Override
                        public void onAutoEnd() {
                            mWinRateText.show(new AutoTextView.OnAutoEndListner() {
                                @Override
                                public void onAutoEnd() {
                                    mScrollView.fullScroll(View.FOCUS_DOWN);
                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            responseMerge();
                                        }
                                    }, 1000);
                                }
                            });
                        }
                    };
                } else {
                    mWinRateText.setVisibility(View.INVISIBLE);
                    onAutoEndListner = new AutoTextView.OnAutoEndListner() {
                        @Override
                        public void onAutoEnd() {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    responseMerge();
                                }
                            }, 1000);
                        }
                    };
                }

                mOwnWeightText.setVisibility(View.VISIBLE);
                mOwnWeightText.setLines(ownLines);
                mOwnWeightText.setDuration(800);

                mOwnWeightText.show(onAutoEndListner);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                LogUtils.w(volleyError.toString());
                mEmptyView.setVisibility(View.VISIBLE);
            }
        });


        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void responseMerge(){
        if (mCoupon == null) {
            return;
        }

        String resultString = "";
        if (mResult == 1) {
            resultString = "正在和对方PK...";
        } else if (mResult == -1) {
            resultString = "很遗憾\n对方出的也是奇数停车券";
        } else if (mResult == -2) {
            resultString = "很遗憾\n对方出的也是偶数停车券";
        }

        if (mResult < -2) {
            //各种不能合体的情况。
            ToastUtils.show(getActivity(), errorMsg);
//            getActivity().finish();
            return;
        } else {
            mDialog = new Dialog(getActivity(), R.style.DialogFull);
            TextView tv = new TextView(getActivity());
            tv.setText(resultString);
            tv.setTextColor(Color.WHITE);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            mDialog.setContentView(tv);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
        }

        //carinter.do?action=resticketuion&mobile=15801482643&tid=44878&id=8
        Map<String, String> params = URLUtils.createParamsMap();
        params.put("action","resticketuion");
        params.put("mobile", TCBApp.mMobile);
        params.put("tid",mCoupon.id);
        params.put("id",mID);
        String url = URLUtils.createUrl(TCBApp.mServerUrl, "carinter.do", params);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject jsonObject) {
                long cost = SystemClock.uptimeMillis() - begin;
                String winner = null;
                String result;
                try {
                    winner = jsonObject.getString("winner");
                    result = jsonObject.getString("result");
                    if ("-1".equals(result)){
                        winner = "-1";
                    }

                    //表示合体请求已经被响应
                    mMsg.setAttribute(IMConstant.MSG_ATTR_MERGE_RECEIVE, false);
                    TextMessageBody fromBody = (TextMessageBody) mMsg.getBody();
                    TextMessageBody newBody = new TextMessageBody(fromBody.getMessage() + " = 响应");
                    mMsg.addBody(newBody);
                    mConversation.removeMessage(mMsg.getMsgId());
                    EMChatManager.getInstance().saveMessage(mMsg, false);
                    LogUtils.i("修改消息的接受状态" + IMUtils.getMsgString(mMsg));
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    handleResponse(winner,cost);
                }

            }
        }, null);

//        mResultTextView.setText("正在计算结果....");
        begin = SystemClock.uptimeMillis();
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void handleResponse(final String w, long cost){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                LogUtils.i("run");
                EMMessage message = EMMessage.createSendMessage(EMMessage.Type.TXT);
                message.setAttribute(IMConstant.MSG_TYPE_TICKET_MERGE, true);
                message.setAttribute(IMConstant.MSG_ATTR_MERGE_WINNER, w);
                message.setAttribute(IMConstant.MSG_ATTR_MERGE_ID, mID);
                message.setAttribute(IMConstant.MSG_ATTR_MERGE_RESULT, true);
                TextMessageBody body = new TextMessageBody("合并结果");
                message.addBody(body);
                message.setReceipt(mToChatName);

                try {
                    EMChatManager.getInstance().sendMessage(message);
                } catch (EaseMobException e) {
                    e.printStackTrace();
                } finally {
                    openMergeResult();
                }

            }
        };

        if (cost > 1500){
            mHandler.post(r);
        } else {
            mHandler.postDelayed(r, 1500 - cost);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isMerge) {
            getMergeWeight();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    private void openMergeResult(){
        ResultMergeFragment fragment = new ResultMergeFragment();
        Bundle args = new Bundle();
        args.putString("mid",mID);
        args.putString("toChatName",mToChatName);
        fragment.setArguments(args);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    @Override
    protected String getTitle() {
        return "合并停车券";
    }

    private static Handler mHandler = new Handler();

}
