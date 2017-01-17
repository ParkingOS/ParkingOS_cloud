package com.tq.zld.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.reflect.TypeToken;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.adapter.PlateListAdapter;
import com.tq.zld.bean.Plate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author：ClareChen
 * E-mail：ggchaifeng@gmail.com
 * Date：  15/7/9 下午5:21
 */
public class PlateFragment extends NetworkFragment<ArrayList<Plate>> {

    private HashMap<String, String> mParams;
    private ListView mListView;
    private PlateListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plate, container, false);
    }

    @Override
    protected TypeToken<ArrayList<Plate>> getBeanListType() {
        return new TypeToken<ArrayList<Plate>>() {
        };
    }

    @Override
    protected Class<ArrayList<Plate>> getBeanClass() {
        return null;
    }

    @Override
    protected String getUrl() {
        return TCBApp.mServerUrl + "carinter.do";
    }

    @Override
    protected Map<String, String> getParams() {
        if (mParams == null) {
            mParams = new HashMap<>();
            mParams.put("mobile", TCBApp.mMobile);
            mParams.put("action", "getcarnumbs");
        }
        return mParams;
    }

    @Override
    protected void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.lv_plate);
        if (mAdapter == null) {
            mAdapter = new PlateListAdapter();
        }
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Plate plate = (Plate) mAdapter.getItem(position);
                goCertify(plate);
            }
        });
    }

    private void goCertify(Plate plate) {
        replace(R.id.fragment_container, CertifyFragment.newInstance(plate), true);
    }

    @Override
    protected String getTitle() {
        return "我的车牌号";
    }

    @Override
    protected void onNetWorkResponse(ArrayList<Plate> response) {
        mAdapter.setPlates(response);
        showDataView();

        //存储车牌号列表到本地
        if (response != null && response.size() > 0) {
            String key = getString(R.string.sp_plate_all);
            Set<String> plateSet = new HashSet<>();
            for (Plate plate : response) {
                plateSet.add(plate.car_number);
            }
            TCBApp.getAppContext().getAccountPrefs().edit().putStringSet(key, plateSet).apply();
        }
    }

    @Override
    protected int getFragmentContainerResID() {
        return R.id.fragment_container;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        // 相当于OnResume刷新界面
        if (!hidden) {
            getData();
        }
    }
}
