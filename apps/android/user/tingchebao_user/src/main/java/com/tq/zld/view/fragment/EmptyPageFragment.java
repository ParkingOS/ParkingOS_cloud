package com.tq.zld.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tq.zld.R;

public class EmptyPageFragment extends Fragment {

    private TextView mTextView;

    private ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_empty, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextView = (TextView) view.findViewById(R.id.tv_empty);
        mProgressBar = (ProgressBar) view.findViewById(R.id.pb_empty);
    }

    /**
     * 显示空界面
     *
     * @param tips     提示信息
     * @param imageRes 图片资源ID
     * @param listener 界面点击响应事件
     */
    public void showEmptyView(final String tips, final int imageRes,
                              final OnClickListener listener) {

        // 解决某些时候OnCreateView未完成时造成的mTextView等View的空指针异常
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (imageRes != 0) {
                    mTextView
                            .setCompoundDrawablesWithIntrinsicBounds(0, imageRes, 0, 0);
                }
                if (listener != null) {
                    mTextView.setOnClickListener(listener);
                }
                mTextView.setText(tips);
                mProgressBar.setVisibility(View.GONE);
                mTextView.setVisibility(View.VISIBLE);
            }
        }, 1000);
    }

    /**
     * 显示进度框界面
     */
    public void showProgressView() {
        mProgressBar.setVisibility(View.VISIBLE);
        mTextView.setVisibility(View.GONE);
    }

    public void setEmptyText(String string) {
        mTextView.setText(string);
        mTextView.setVisibility(View.VISIBLE);
    }

    public String getEmptyText() {
        return mTextView == null ? "" : mTextView.getText().toString();
    }
}
