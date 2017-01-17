package com.tq.zld.view.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tq.zld.R;

/**
 * Created by GT on 2015/10/8.
 */
public class EmptyViewHolder {
    public View mEmptyPageView;
    private TextView mEmptyText;
    private Button mEmptyButton;

    public EmptyViewHolder(View parentView) {
        mEmptyPageView = parentView.findViewById(R.id.rl_page_null);
        mEmptyText = (TextView) parentView.findViewById(R.id.tv_page_null);
        mEmptyButton = (Button) parentView.findViewById(R.id.btn_page_null);
    }

    public void setEmptyText(String text, View.OnClickListener listener) {
        mEmptyText.setText(text);
        if (listener != null) {
            mEmptyText.setOnClickListener(listener);
        }
    }

    public void setEmptyButton(String text, View.OnClickListener listener) {
        if (!TextUtils.isEmpty(text)) {
            mEmptyButton.setText(text);
            mEmptyButton.setVisibility(View.VISIBLE);
        } else {
            mEmptyButton.setVisibility(View.GONE);
        }

        mEmptyButton.setOnClickListener(listener);
    }
}
