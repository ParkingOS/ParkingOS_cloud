package com.tq.zld.view.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.ParkComment;
import com.tq.zld.bean.ParkingFeeCollector;
import com.tq.zld.bean.PayResult;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.KeyboardUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.URLUtils;

import java.util.HashMap;
import java.util.Map;

public class CommentParkFragment extends BaseFragment implements
        OnClickListener {

    public static final String ARG_TYPE = "type";
    public static final String ARG_ID = "id";
    public static final String ARG_COLLECTOR = "collector";
    public static final String ARG_COMMENT = "comment";

    public static final int TYPE_PARK = 0;
    public static final int TYPE_COLLECTOR = 1;

    EditText mCommentEditText;
    Button mCommentButton;

    private int mType;
    private String mId;
    private ParkingFeeCollector mCollector;
    private ParkComment mComment;

    /**
     * 获取实例
     *
     * @param type 评论类型：TYPE_PARK 表示车场，TYPE_COLLECTOR 表示收费员
     * @param id   车场ID或订单ID
     * @param data 数据体
     * @return
     */
    public static CommentParkFragment newInstance(int type, String id, Parcelable data) {
        CommentParkFragment fragment = new CommentParkFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE, type);
        args.putString(ARG_ID, id);
        if (data != null) {
            if (data instanceof ParkingFeeCollector) {
                args.putParcelable(ARG_COLLECTOR, data);
            } else if (data instanceof ParkComment) {
                args.putParcelable(ARG_COMMENT, data);
            }
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt(ARG_TYPE);
        mId = getArguments().getString(ARG_ID);
        switch (mType) {
            case TYPE_COLLECTOR:
                if (getArguments().containsKey(ARG_COLLECTOR)) {
                    mCollector = getArguments().getParcelable(ARG_COLLECTOR);
                } else {
                    // TODO 联网请求该笔订单的收费员
                }
                break;
            case TYPE_PARK:
                if (getArguments().containsKey(ARG_COMMENT)) {
                    mComment = getArguments().getParcelable(ARG_COMMENT);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected String getTitle() {
        return mType == TYPE_COLLECTOR ? "评价收费员" : "评价车场";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment_park, container,
                false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCommentEditText = (EditText) view.findViewById(R.id.et_comment_park);
        mCommentButton = (Button) view.findViewById(R.id.btn_comment_park);
        mCommentButton.setOnClickListener(this);

        switch (mType) {
            case TYPE_COLLECTOR:
                initCollectorView();
                break;
            case TYPE_PARK:
                initCommentParkView();
                break;
        }
    }

    private void initCommentParkView() {
        if (mComment != null) {
            mCommentEditText.setText(mComment.info);
            mCommentEditText.setSelection(mComment.info.length());
        }
    }

    private void initCollectorView() {
//            if ("收费员".equals(mCollector.name)) {
//                mCollector.name = "";
//            }
//            mCommentEditText.setHint(String.format("写下你对收费员%s本次服务的感受。。。", mCollector.name));
        mCommentEditText.setHint("写下你对本次服务的评价。。。");
    }

    @Override
    public void onClick(View v) {
        if (v == mCommentButton) {
            onCommentBtnClicked();
        }
    }

    private void onCommentBtnClicked() {
        final String comment = mCommentEditText.getText().toString().trim();
        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(getActivity(), "请填写评论内容！", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        switch (mType) {
            case TYPE_COLLECTOR:
                commentCollector(comment);
                break;
            case TYPE_PARK:
                commentPark(comment);
                break;
            default:
                break;
        }
    }

    private void commentPark(final String comment) {
        String url = TCBApp.mServerUrl + "carowner.do";
        StringRequest request = new StringRequest(Method.POST, url,
                new Listener<String>() {

                    @Override
                    public void onResponse(String result) {
                        if ("1".equals(result)) {
                            Toast.makeText(getActivity(), "评价成功！",
                                    Toast.LENGTH_SHORT).show();
                            // mCommentEditText.setText("");
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                        } else {
                            Toast.makeText(getActivity(), "评价失败！",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                Toast.makeText(getActivity(), "网络错误！",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("action", "comment");
                params.put("comid", mId);
                params.put("mobile", TCBApp.mMobile);
                params.put("comment", comment);
                URLUtils.decode(params);
                LogUtils.i(CommentParkFragment.class, "comment Park params: --->> " + params.toString());
                return params;
            }
        };
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void commentCollector(final String comment) {
        String url = TCBApp.mServerUrl + "carowner.do";
        GsonRequest<PayResult> request = new GsonRequest<PayResult>(Method.POST, url, PayResult.class,
                new Listener<PayResult>() {

                    @Override
                    public void onResponse(PayResult result) {
                        if ("1".equals(result.result)) {
                            Toast.makeText(getActivity(), result.errmsg,
                                    Toast.LENGTH_SHORT).show();
                            // mCommentEditText.setText("");
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                        } else if ("-1".equals(result.result)) {
                            Toast.makeText(getActivity(), result.errmsg,
                                    Toast.LENGTH_SHORT).show();
                            // mCommentEditText.setText("");
                            getActivity().finish();
                        } else {
                            Toast.makeText(getActivity(), result.errmsg,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError arg0) {
                Toast.makeText(getActivity(), "网络错误！",
                        Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("action", "pusercomment");
                params.put("orderid", mId);
                params.put("mobile", TCBApp.mMobile);
                params.put("comment", comment);
                URLUtils.decode(params);
                LogUtils.i(CommentParkFragment.class, "comment Collector params: --->> " + params.toString());
                return params;
            }
        };
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        KeyboardUtils.closeKeybord(mCommentEditText, getActivity());
        TCBApp.getAppContext().cancelPendingRequests(this);
    }
}
