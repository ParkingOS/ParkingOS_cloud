package com.tq.zld.view.map;

import java.util.ArrayList;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.tq.zld.R;
import com.tq.zld.view.BaseActivity;

/**
 * 显示停车场图片
 * 已过时，由PhotoActivity替代
 */
@Deprecated
public class ParkPhotoActivity extends BaseActivity {

    DisplayImageOptions options;
    ViewPager viewPager;
    ImageView[] imageViews;
    LinearLayout group;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_image_pager);
        initActionBar();
        ArrayList<String> imageUrls = getIntent().getStringArrayListExtra(
                "photoUrls");
        imageViews = new ImageView[imageUrls.size()];
        group = (LinearLayout) this.findViewById(R.id.viewGroup);
        viewPager = (ViewPager) this.findViewById(R.id.pager);

        /**
         * 有几张图片 下面就显示几个小圆点
         */

        for (int i = 0; i < imageUrls.size(); i++) {
            LinearLayout.LayoutParams margin = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            // 设置每个小圆点距离左边的间距
            margin.setMargins(15, 0, 0, 0);
            ImageView imageView = new ImageView(ParkPhotoActivity.this);
            // 设置每个小圆点的宽高
            imageView.setLayoutParams(new LayoutParams(25, 25));

            imageViews[i] = imageView;
            if (i == 0) {
                // 默认选中第一张图片
                imageViews[i]
                        .setBackgroundResource(R.drawable.ic_page_indicator_focused_gray);
            } else {
                // 其他图片都设置未选中状态
                imageViews[i]
                        .setBackgroundResource(R.drawable.ic_page_indicator_unfocused);
            }
            group.addView(imageViews[i], margin);
        }
        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.img_page_null).cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .cacheInMemory(true).imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        viewPager.setAdapter(new ImagePagerAdapter(imageUrls));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                // 遍历数组让当前选中图片下的小圆点设置颜色
                for (int i = 0; i < imageViews.length; i++) {
                    imageViews[arg0]
                            .setBackgroundResource(R.drawable.ic_page_indicator_focused_gray);

                    if (arg0 != i) {
                        imageViews[i]
                                .setBackgroundResource(R.drawable.ic_page_indicator_unfocused);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private ArrayList<String> images;
        private LayoutInflater inflater;

        ImagePagerAdapter(ArrayList<String> images) {
            this.images = images;
            inflater = getLayoutInflater();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.item_pager_image,
                    view, false);
            assert imageLayout != null;
            ImageView imageView = (ImageView) imageLayout
                    .findViewById(R.id.image);
            final ProgressBar spinner = (ProgressBar) imageLayout
                    .findViewById(R.id.loading);

            ImageLoader.getInstance().displayImage(
                    getString(R.string.url_release) + images.get(position), imageView,
                    options, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            spinner.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view,
                                                    FailReason failReason) {
                            String message = null;
                            switch (failReason.getType()) {
                                case IO_ERROR:
                                    message = "Input/Output error";
                                    break;
                                case DECODING_ERROR:
                                    message = "Image can't be decoded";
                                    break;
                                case NETWORK_DENIED:
                                    message = "Downloads are denied";
                                    break;
                                case OUT_OF_MEMORY:
                                    message = "Out Of Memory error";
                                    break;
                                case UNKNOWN:
                                    message = "Unknown error";
                                    break;
                            }
                            Toast.makeText(ParkPhotoActivity.this, message,
                                    Toast.LENGTH_SHORT).show();

                            spinner.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri,
                                                      View view, Bitmap loadedImage) {
                            spinner.setVisibility(View.GONE);
                        }
                    });

            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

    private void initActionBar() {
        ActionBar mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("查看大图");
        mActionBar.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
