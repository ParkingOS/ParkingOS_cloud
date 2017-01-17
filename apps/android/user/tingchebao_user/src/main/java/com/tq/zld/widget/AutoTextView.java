package com.tq.zld.widget;

import android.content.Context;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.tq.zld.util.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  自动换行显示的TextView
 *  Created by Gecko on 2015/10/13.
 */
public class AutoTextView extends TextView {

    private List<CharSequence> mLines = new ArrayList<>();
    private int mDuration = 500;
    private OnAutoEndListner onAutoEndListner;

    public AutoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoTextView(Context context) {
        super(context);
    }

    public void setLines(CharSequence[] lines){
        if (lines != null && lines.length > 0) {
            this.mLines.addAll(Arrays.asList(lines));
        }
    }

    public void setLines(List<CharSequence> lines) {
        if (lines != null && lines.size() > 0) {
            this.mLines.addAll(lines);
        }
    }

    public void show() {
        show(null);
    }

    public void show(final OnAutoEndListner onAutoEndListner){
        this.onAutoEndListner = onAutoEndListner;

        if (mLines.size() <= 0) {
            return;
        }

        Runnable r = new Runnable(){
            @Override
            public void run() {
                SpannableStringBuilder builder = new SpannableStringBuilder(getText());

                CharSequence remove = mLines.remove(0);
                if (!TextUtils.isEmpty(remove)) {
                    builder.append(remove);
                    setText(builder);
                    if (mLines.size() > 0) {
                        mHandler.postDelayed(this, mDuration);
                    } else {
                        if (onAutoEndListner != null) {
                            onAutoEndListner.onAutoEnd();
                            LogUtils.i("onAutoEnd");
                        }
                    }
                }
            }
        };
        mHandler.postDelayed(r, mDuration);
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    private Handler mHandler = new Handler();

    public interface OnAutoEndListner{
        void onAutoEnd();
    }
}
