package com.tq.zld.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.view.PhotoActivity;

import java.io.File;

/**
 * 用于展示停车标记信息
 * 已过时，使用RemarkFragment2替代
 */
@Deprecated
public class RemarkFragment extends DialogFragment implements OnClickListener {

    private static final String ARG_ORDER_ID = "orderid";
    private static String sArgImagePath;

    private ImageView mImageView;
    private View mRephotoView;
    private View mBigPicView;
    private View mCloseView;
    private EditText mTipsView;
    private Button mSaveButton;
    private DisplayImageOptions mOptions;

    /**
     * Use newInstance() instead
     */
    public RemarkFragment() {

    }

    public static RemarkFragment newInstance(String imagePath, String orderId) {
        RemarkFragment fragment = new RemarkFragment();
        sArgImagePath = TCBApp.getAppContext().getString(R.string.sp_remark_image_path);
        Bundle args = new Bundle();
        args.putString(sArgImagePath, imagePath);
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        fragment.setCancelable(false);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOptions = new DisplayImageOptions.Builder().cacheInMemory(false)
                .bitmapConfig(Config.RGB_565).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return inflater.inflate(R.layout.fragment_remark, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mImageView = (ImageView) view.findViewById(R.id.iv_remark);
        String imageUri = getArguments().getString(sArgImagePath);
        if (!TextUtils.isEmpty(imageUri)) {
            ImageLoader.getInstance()
                    .displayImage("file://" + imageUri, mImageView, mOptions);
        }
        mRephotoView = view.findViewById(R.id.tv_remark_rephoto);
        mRephotoView.setOnClickListener(this);
        mBigPicView = view.findViewById(R.id.tv_remark_bigpic);
        mBigPicView.setOnClickListener(this);
        mTipsView = (EditText) view.findViewById(R.id.et_remark);
        String tips = TCBApp.getAppContext().readString(R.string.sp_remark, "");
        String orderId = getArguments().getString(ARG_ORDER_ID);
        if (!TextUtils.isEmpty(tips) && tips.endsWith(orderId)) {
            tips = tips.replace(orderId, "");
            mTipsView.setText(tips);
            mTipsView.setSelection(tips.length());
        }
        mSaveButton = (Button) view.findViewById(R.id.btn_remark_save);
        mSaveButton.setOnClickListener(this);
        mCloseView = view.findViewById(R.id.ib_remark_close);
        mCloseView.setOnClickListener(this);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if (v == mRephotoView) {

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // path为保存图片的路径，执行完拍照以后能保存到指定的路径下
            String imagePath = getArguments().getString(sArgImagePath);
            File file = new File(imagePath);
            Uri imageUri = Uri.fromFile(file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, OrderFragment.REQUEST_CODE_CAMERA);
        } else if (v == mSaveButton) {
            onSaveButtonClicked();
        } else if (v == mBigPicView) {
            String[] imageURIs = new String[]{getArguments().getString(sArgImagePath)};
            Intent intent = new Intent(TCBApp.getAppContext(),
                    PhotoActivity.class);
            intent.putExtra(PhotoActivity.ARG_IMAGEURI, imageURIs);
            startActivity(intent);
            // if (!TextUtils.isEmpty(imagePath)) {
            // final ImageView imageView = new ImageView(getActivity());
            // imageView.setScaleType(ScaleType.FIT_XY);
            // ImageLoader.getInstance().loadImage("file://" + imagePath,
            // new SimpleImageLoadingListener() {
            // @Override
            // public void onLoadingComplete(String mImageUri,
            // final View view, final Bitmap loadedImage) {
            // super.onLoadingComplete(mImageUri, view,
            // loadedImage);
            // imageView.setImageBitmap(loadedImage);
            // new AlertDialog.Builder(getActivity())
            // .setView(imageView)
            // .show()
            // .setOnDismissListener(
            // new OnDismissListener() {
            //
            // @Override
            // public void onDismiss(
            // DialogInterface dialog) {
            // // 释放图片资源
            // imageView
            // .setImageDrawable(null);
            // if (loadedImage != null) {
            // loadedImage
            // .recycle();
            // }
            // System.gc();
            // }
            // });
            // }
            // });
            // }
        } else if (v == mCloseView) {
            dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OrderFragment.REQUEST_CODE_CAMERA
                && resultCode == Activity.RESULT_OK) {
            ImageLoader.getInstance()
                    .displayImage("file://" + getArguments().getString(sArgImagePath),
                            mImageView, mOptions);
        }
    }

    private void onSaveButtonClicked() {
        TCBApp.getAppContext().saveString(R.string.sp_remark, mTipsView.getText().toString().trim()
                + getArguments().getString(ARG_ORDER_ID));
        dismiss();
    }
}
