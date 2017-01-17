package com.tq.zld.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.map.X5WebActivity;

/**
 * Created by GT on 2015/10/10.
 */
public class AddFriendFragment extends BaseFragment implements View.OnClickListener {

    private EditText mPlateEdit;
    private View mParkFriendView;
    private View mPlaygameView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_friend, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        mPlateEdit = (EditText) view.findViewById(R.id.et_add_plate);
        mParkFriendView = view.findViewById(R.id.rl_add_park_friend);
        mPlaygameView = view.findViewById(R.id.rl_add_playgame);


        mPlateEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        mParkFriendView.setOnClickListener(this);
        mPlaygameView.setOnClickListener(this);
    }

    private void performSearch() {
        LogUtils.i("performSearch");
    }

    @Override
    protected String getTitle() {
        return "添加车友";
    }

    @Override
    public void onClick(View v) {
        if (v == mParkFriendView) {
            //TODO 添加附近车友
            LogUtils.i("常去停车场");
            doOpenHistoryPark();
        } else if (v == mPlaygameView) {
            //TODO 打飞机，添加车友
            LogUtils.i("打飞机，添加车友");
            doPlaygame();
        }
    }

    private void doOpenHistoryPark() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, new HistoryParkFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void doPlaygame(){
        Intent intent = new Intent(TCBApp.getAppContext(), X5WebActivity.class);
        intent.putExtra(X5WebActivity.ARG_TITLE, "打灰机");// 标题改变时会影响游戏界面逻辑，请参考WebActivity
        intent.putExtra(X5WebActivity.ARG_URL, TCBApp.mServerUrl + "flygame.do?action=pregame&mobile=" + TCBApp.mMobile);
        startActivity(intent);
    }

}
