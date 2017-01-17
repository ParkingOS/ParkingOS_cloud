package com.tq.zld.view;

import uk.co.senab.photoview.PhotoView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.LayoutParams;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tq.zld.R;
import com.tq.zld.util.ScreenUtils;
import com.tq.zld.widget.HackyViewPager;

public class PhotoActivity extends BaseActivity {

    public static final String ARG_IMAGEURI = "imageuris";

    private HackyViewPager mPhotoPages;
    private PhotoPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        initToolbar();
        mPhotoPages = (HackyViewPager) findViewById(R.id.viewpager_photo);
        mAdapter = new PhotoPagerAdapter(getIntent().getStringArrayExtra(
                ARG_IMAGEURI));
        mPhotoPages.setAdapter(mAdapter);
    }

    private void initToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.black));
//            getWindow().setNavigationBarColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        Toolbar bar = (Toolbar) findViewById(R.id.toolbar_photo);
        bar.setTitle("");
        setSupportActionBar(bar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        bar.setNavigationOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    class PhotoPagerAdapter extends PagerAdapter {

        private String[] mImageURIs;

        public PhotoPagerAdapter(String[] imageURIs) {
            this.mImageURIs = imageURIs;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // View view = getLayoutInflater().inflate(R.layout.pageritem_photo,
            // container, false);
            // final PhotoView imageView = (PhotoView) view
            // .findViewById(R.id.iv_pageritem);
            // final PhotoViewAttacher attacher = new
            // PhotoViewAttacher(imageView);
            final PhotoView imageView = new PhotoView(PhotoActivity.this);
            String uri = mImageURIs[position];
            // imageView.setImageURI(Uri.parse(uri));
            if (!uri.startsWith("http")) {
                uri = "file://" + uri;
            }
            ImageLoader.getInstance().displayImage(uri, imageView);
            container.addView(imageView, LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            return imageView;
        }

        @Override
        public int getCount() {
            return mImageURIs == null ? 0 : mImageURIs.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }
    }
}
