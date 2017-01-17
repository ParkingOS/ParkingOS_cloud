package com.tq.zld.view.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.ParkCommentsAdapter;
import com.tq.zld.bean.ParkComment;
import com.tq.zld.protocal.GsonRequest;
import com.tq.zld.util.URLUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentFragment extends ListFragment {

    public static final String ARG_UID = "uid";

    /**
     * 一次返20条数据
     */
    private static final int PAGE_SIZE = 20;

    private int mPage;
    private String mUid;

    View mEmptyView;
    TextView mEmptyText;

    private Button mFootView;
    private ParkCommentsAdapter mAdapter;

    public static CommentFragment newInstance(String uid) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_UID, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle("评论详情");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUid = getArguments().getString(ARG_UID);
        mAdapter = new ParkCommentsAdapter(getActivity());
        mPage = 1;
        getComments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmptyView = view.findViewById(R.id.rl_page_null);
        // mEmptyView = getActivity().getLayoutInflater().inflate(
        // R.layout.page_null, (ViewGroup) view, false);
        mEmptyText = (TextView) mEmptyView.findViewById(R.id.tv_page_null);
        mEmptyText.setText("暂无评论。");
        getListView().setEmptyView(mEmptyView);
        View footView = getActivity().getLayoutInflater().inflate(
                R.layout.listitem_foot, null);
        mFootView = (Button) footView.findViewById(R.id.btn_listitem_foot);
        if (mAdapter.getCount() % PAGE_SIZE == 0) {
            mFootView.setText(getString(R.string.load_more));
        } else {
            mFootView.setText(getString(R.string.no_more_data));
        }
        mFootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getString(R.string.load_more).equals(mFootView.getText())) {
                    mFootView.setText(getString(R.string.loading));
                    getComments();
                }
            }
        });
        getListView().addFooterView(footView);
        setListAdapter(mAdapter);
    }

    private void getComments() {

        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "", false, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                TCBApp.getAppContext().cancelPendingRequests(CommentFragment.this);
            }
        });
        dialog.setCanceledOnTouchOutside(false);

        final HashMap<String, String> params = new HashMap<>();
        params.put("action", "pusrcomments");
        params.put("uid", mUid);
        params.put("page", String.valueOf(mPage));
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "carinter.do", params);
        GsonRequest<ArrayList<ParkComment>> request = new GsonRequest<>(url,
                new TypeToken<ArrayList<ParkComment>>() {
                }, new Response.Listener<ArrayList<ParkComment>>() {
            @Override
            public void onResponse(ArrayList<ParkComment> details) {
                dialog.dismiss();
                if (mFootView != null) {
                    if (details.size() < PAGE_SIZE) {
                        mFootView.setText(getString(R.string.no_more_data));
                    } else {
                        mFootView.setText(getString(R.string.load_more));
                    }
                    if (mPage == 1 && details.size() == 0) {
                        mEmptyText.setText("暂无评论");
                    }
                }
                ((ParkCommentsAdapter) getListAdapter()).setData(mPage, details);
                mPage++;

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.dismiss();
                mEmptyText.setText("网络错误，点击重试~");
                mEmptyView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String loading = getString(R.string.loading);
                        if (loading.equals(mEmptyText.getText().toString())) {
                            return;
                        }
                        mEmptyText.setText(loading);
                        getComments();
                    }
                });
            }
        });
        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        TCBApp.getAppContext().cancelPendingRequests(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mFootView != null && getString(R.string.loading).equals(mFootView.getText())) {
                mFootView.setText(getString(R.string.load_more));
            }
        } else {
            TCBApp.getAppContext().cancelPendingRequests(this);
        }
    }
}
