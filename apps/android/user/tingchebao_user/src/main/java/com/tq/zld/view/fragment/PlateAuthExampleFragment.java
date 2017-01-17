package com.tq.zld.view.fragment;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tq.zld.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlateAuthExampleFragment extends BaseFragment {


    public PlateAuthExampleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plate_auth_example, container, false);
    }

    @Override
    protected String getTitle() {
        return "示例照片";
    }
}
