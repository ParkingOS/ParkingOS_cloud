package com.tq.zld.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.exceptions.EaseMobException;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.ToastUtils;
import com.tq.zld.util.URLUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by Gecko on 2015/10/30.
 */
public class SendVerifyFragment extends BaseFragment {
    public final static String ARGS_USER_ID = "args_user_id";
    public final static String ARGS_PARK_NAME = "args_park_name";

    private Button mButton;
    private EditText mEdit;
    private String mUserID;
    private String mHxName = "";
    private String mParkName;
    private boolean isAddSuccess = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserID = getArguments().getString(ARGS_USER_ID);
        mParkName = getArguments().getString(ARGS_PARK_NAME);
        /*
                    carinter.do?action=gethxname&id=21667
            /根据账户查环信账户
            {"result":"-1","errmsg":"车主不存在！","hxname":""}
            {"result":"1","errmsg":"","hxname":"hx21776"}
         */
        Map<String, String> paramsMap = URLUtils.createParamsMap();
        paramsMap.put("action", "gethxname");
        paramsMap.put("id", mUserID);
        String url = URLUtils.createUrl(TCBApp.mServerUrl, "carinter.do", paramsMap);
        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try {
                    if ("1".equals(jsonObject.getString("result"))) {
                        mHxName = jsonObject.getString("hxname");
                        if (!TextUtils.isEmpty(mHxName) && mButton != null) {
                            mButton.setEnabled(true);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, null);

        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    @Override
    protected String getTitle() {
        return "车友验证";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_send_virify, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mButton = (Button) view.findViewById(R.id.button);
        mEdit = (EditText) view.findViewById(R.id.edit);
        mEdit.setText(String.format("我也是在%s车场停车的车友", mParkName));
        mEdit.setSelection(mEdit.getText().length());

        if (TextUtils.isEmpty(mHxName)) {
            mButton.setEnabled(false);
        }

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EMContactManager.getInstance().addContact(mHxName, mEdit.getText().toString());//需异步处理
                            isAddSuccess = true;
                            ///carinter.do?action=preaddfried&fhxname=hx21776&mobile=15210932334&type=&resume=
                            String resume = URLEncoder.encode(mEdit.getText().toString(),"utf-8");

                            Map<String, String> paramsMap = URLUtils.createParamsMap();
                            paramsMap.put("action", "preaddfriend");
                            paramsMap.put("fhxname", mHxName);
                            paramsMap.put("type", "1");
                            paramsMap.put("resume", resume);
                            String url = URLUtils.createUrl(TCBApp.mServerUrl, "carinter.do", paramsMap);
                            JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    try {
                                        String result = jsonObject.getString("result");
                                        String errmsg = jsonObject.getString("errmsg");
                                        LogUtils.i(String.format("result = %s, errmsg = %s", result, errmsg));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    Intent intent = new Intent();
                                    intent.putExtra(ParkUserFragment.KEY_INVITE_USER_ID, mUserID);
                                    getActivity().setResult(Activity.RESULT_OK, intent);
                                    getActivity().finish();
                                }
                            }, null);
                            TCBApp.getAppContext().addToRequestQueue(request, SendVerifyFragment.this);
                        } catch (EaseMobException e) {
                            e.printStackTrace();
                            //环信没有链接上
                            isAddSuccess = false;
                            getActivity().finish();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isAddSuccess) {
            ToastUtils.show(getActivity(), "发送成功!");
            Intent intent = new Intent();
            intent.putExtra(ParkUserFragment.KEY_INVITE_USER_ID, mUserID);
            getActivity().setResult(Activity.RESULT_OK, intent);
        } else {
//            ToastUtils.show(getActivity(), "发送失败!");
        }

    }
}
