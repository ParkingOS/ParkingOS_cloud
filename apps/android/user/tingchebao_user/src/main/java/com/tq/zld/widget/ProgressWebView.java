package com.tq.zld.widget;

import com.tq.zld.R;
import com.tq.zld.util.DensityUtils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class ProgressWebView extends WebView {

    private ProgressBar progressbar;

    public ProgressWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        progressbar = new ProgressBar(context, null,
                android.R.attr.progressBarStyleHorizontal);
        progressbar.setMax(100);
        progressbar.setProgressDrawable(new ColorDrawable(getResources()
                .getColor(R.color.primary_green)));
        progressbar.setBackgroundColor(Color.TRANSPARENT);
        progressbar.setLayoutParams(new RelativeLayout.LayoutParams(
                android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                DensityUtils.dip2px(getContext(), 3)));
        addView(progressbar);
        setWebChromeClient(new WebChromeClient(progressbar));
    }

    public ProgressBar getProgressbar() {
        return progressbar;
    }

    public static class WebChromeClient extends android.webkit.WebChromeClient {

        private ProgressBar mProgressBar;

        public WebChromeClient(ProgressBar progressBar) {
            this.mProgressBar = progressBar;
        }

        @Override
        public final void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                mProgressBar.setVisibility(GONE);
            } else {
                if (mProgressBar.getVisibility() == GONE)
                    mProgressBar.setVisibility(VISIBLE);
                mProgressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }
    }
}
