package com.tq.zld.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.TicketAdapter;
import com.tq.zld.bean.Coupon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChooseTicketFragment extends NetworkFragment<ArrayList<Coupon>> {

    public static final String ARG_TOTAL = "total";
    public static final String ARG_ORDERID = "orderid";
    public static final String ARG_CHOOSED_ID = "preid";
    public static final String ARG_UID = "uid";
    public static final String ARG_PROD_TYPE = "ptype";

    private Button mOKButton;
    private View mNochoiceView;
    private CheckBox mNochoiceCheckBox;
    private TicketAdapter mAdapter;
    private String mPreId;

	/*
     * public static TicketsFragment newInstance(String total, String orderid,
	 * String uid) { TicketsFragment fragment = new TicketsFragment(); Bundle
	 * args = new Bundle(); args.putString(ARG_TOTAL, total);
	 * args.putString(ARG_ORDER_ID, orderid); args.putString(ARG_UID, uid);
	 * fragment.setArguments(args); return fragment; }
	 */

    @Override
    protected String getTitle() {
        return "选择停车券";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPreId = getArguments().getString(ARG_CHOOSED_ID);
        return inflater.inflate(R.layout.fragment_tickets, container, false);
    }

    @Override
    public void onClick(View v) {
        if (v == mNochoiceView) {
            mNochoiceCheckBox.setChecked(true);
            mAdapter.setSelection(-1);
        } else if (v == mOKButton) {
            onOKButtonClicked();
        }
    }

    private void onOKButtonClicked() {
        Intent intent = new Intent();
        intent.putExtra("coupon", mAdapter.getSelectedItem());
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    protected TypeToken<ArrayList<Coupon>> getBeanListType() {
        return new TypeToken<ArrayList<Coupon>>() {
        };
    }

    @Override
    protected Class<ArrayList<Coupon>> getBeanClass() {
        return null;
    }

    @Override
    protected String getUrl() {
        return TCBApp.mServerUrl + "carinter.do";
    }

    @Override
    protected Map<String, String> getParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "usetickets");
        params.put("mobile", TCBApp.mMobile);
        params.put(ARG_TOTAL, getArguments().getString(ARG_TOTAL));
        params.put(ARG_ORDERID, getArguments().getString(ARG_ORDERID));
        params.put(ARG_UID, getArguments().getString(ARG_UID));
        params.put(ARG_CHOOSED_ID, getArguments().getString(ARG_CHOOSED_ID));
        params.put(ARG_PROD_TYPE, getArguments().getString(ARG_PROD_TYPE));
        // 新版选券，可选大金额券，但抵扣金额有限制
        //老版utype:1,2.3.1 utype:2
        params.put("utype", "2");
        return params;
    }

    @Override
    protected void initView(View view) {
        ListView ticketsView = (ListView) view.findViewById(R.id.lv_tickets);
        ticketsView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (position == mAdapter.getSelection()) {
                    return;
                }

                if (("1".equals(((Coupon) mAdapter.getItem(position)).iscanuse))) {// 表示可用停车券
                    mNochoiceCheckBox.setChecked(false);
                    mAdapter.setSelection(position);
                }
            }
        });

        // 设置返回键监听
        ticketsView.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onOKButtonClicked();
                }
                return false;
            }
        });

        if (mAdapter == null) {
            mAdapter = new TicketAdapter();
        }

        if (!TextUtils.isEmpty(mPreId) && !"-1".equals(mPreId)) {
            mAdapter.setSelection(0);
        }

        // mTicketsView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ticketsView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        // mTicketsView.setDrawSelectorOnTop(true);
        ticketsView.setFocusable(true);
        ticketsView.setFocusableInTouchMode(true);
        ticketsView.setActivated(true);
        ticketsView.setSelected(true);
        ticketsView.setAdapter(mAdapter);
        mOKButton = (Button) view.findViewById(R.id.btn_tickets);
        mOKButton.setOnClickListener(this);
        mNochoiceView = view.findViewById(R.id.ll_tickets_nochoice);
        mNochoiceView.setOnClickListener(this);
        mNochoiceCheckBox = (CheckBox) view.findViewById(R.id.cb_tickets);
        boolean checked = mAdapter.getSelection() == -1;
        mNochoiceCheckBox.setChecked(checked);
    }

    @Override
    public void onNetWorkResponse(ArrayList<Coupon> coupons) {
        if (coupons != null && coupons.size() > 0) {
            mAdapter.setData(1,coupons);
            showDataView();
        }
    }

    @Override
    protected int getFragmentContainerResID() {
        return R.id.fragment_container;
    }
}
