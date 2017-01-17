package com.tq.zld.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.map.ChooseLocationActivity;
import com.tq.zld.view.map.InputTextActivity;
import com.tq.zld.view.map.MapActivity;
import com.tq.zld.view.map.WalkingNaviActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.senab.photoview.PhotoView;


/**
 * Author：ClareChen
 * E-mail：ggchaifeng@gmail.com
 * Date：  15/6/25 下午7:43
 */
public class RemarkFragment2 extends BaseFragment implements View.OnClickListener {

    private static final int REQUEST_CODE_CHOOSE_LOCATION = 0;
    private static final int REQUEST_CODE_ADD_TIPS = 1;

    private PhotoView mPhotoView;
    private TextView mFloorTextView;
    private ImageButton mLocateButton;
    private View mAddTipsView;
    private TextView mAddTipsTextView;
    private Button mAddTipsButton;

    private File mImage;

    private static String[] sFloorItems = new String[]{"5层", "4层", "3层", "2层", "1层", "-1层", "-2层", "-3层"};

    @Override
    protected String getTitle() {
        return getString(R.string.label_remark);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = TCBApp.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (dir != null && !dir.exists()) {
                if (!dir.mkdirs()) {
                    LogUtils.e(getClass(), "--->> make pictures dir failed!!!");
                    return;
                }
            }
            mImage = new File(dir, "remark.jpg");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_remark2, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置照片
        mPhotoView = (PhotoView) view.findViewById(R.id.pv_remark_photo);
        mPhotoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (mImage != null) {
            ImageLoader.getInstance().displayImage("file://" + mImage.getAbsolutePath(), mPhotoView);
        }

        // 设置时间
        TextView timeTextView = (TextView) view.findViewById(R.id.tv_remark_time);
        String timeStr = "";
        long time = TCBApp.getAppContext().readLong(R.string.sp_remark_time, 0);
        if (time != 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
            Date date = new Date(time);
            timeStr = formatter.format(date);
        }
        timeTextView.setText(timeStr);

        // 初始化定位按钮
        mLocateButton = (ImageButton) view.findViewById(R.id.ib_remark_locate);
        mLocateButton.setOnClickListener(this);
        if (TCBApp.getAppContext().readBoolean(R.string.sp_remark_located, false)) {
            mLocateButton.setImageResource(R.drawable.ic_remark_walking);
            mLocateButton.setTag(true);
        } else {
            mLocateButton.setImageResource(R.drawable.ic_remark_locate);
        }

        // 初始化选择楼层按钮
        mFloorTextView = (TextView) view.findViewById(R.id.tv_remark_floor);
        mFloorTextView.setOnClickListener(this);
        int floor = TCBApp.getAppContext().readInt(R.string.sp_remark_floor, -1);
        if (floor != -1) {
            mFloorTextView.setBackgroundResource(R.drawable.ic_remark_floor_choosed);
            mFloorTextView.setText(sFloorItems[floor]);
        } else {
            floor = 0;
        }
        mFloorTextView.setTag(floor);

        //初始化重拍按钮
        view.findViewById(R.id.btn_remark_take_photo).setOnClickListener(this);

        //初始化备注说明
        mAddTipsView = view.findViewById(R.id.fl_remark_add_tips);
        mAddTipsView.setOnClickListener(this);
        mAddTipsTextView = (TextView) view.findViewById(R.id.tv_remark_add_tips);
        String tips = TCBApp.getAppContext().readString(R.string.sp_remark_tips, "");
        if (!TextUtils.isEmpty(tips)) {
            mAddTipsTextView.setText(tips);
        }

        mAddTipsButton = (Button) view.findViewById(R.id.btn_remark_add_tips);
        if (!TextUtils.isEmpty(mAddTipsTextView.getText())) {
            mAddTipsButton.setVisibility(View.GONE);
        }
    }

    private void takePhoto() {
        if (mImage == null) {
            Toast.makeText(getActivity(), "未检测到存储设备！", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // path为保存图片的路径，执行完拍照以后能保存到指定的路径下
        Uri imageUri = Uri.fromFile(mImage);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, MapActivity.REQUEST_CODE_CAMERA);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_remark_take_photo) {
            takePhoto();
        } else if (v == mLocateButton) {
            onLocateBtnClicked();
        } else if (v == mFloorTextView) {
            onFloorTextViewClicked();
        } else if (v == mAddTipsView) {
            onAddTipsViewClicked();
        }
    }

    private void onAddTipsViewClicked() {
        Intent intent = new Intent(TCBApp.getAppContext(), InputTextActivity.class);
        String data = mAddTipsTextView.getText().toString();
        if (!TextUtils.isEmpty(data)) {
            intent.putExtra(InputTextActivity.ARG_DATA, data);
        }
        startActivityForResult(intent, REQUEST_CODE_ADD_TIPS);
    }

    private void onFloorTextViewClicked() {
        //显示选择楼层对话框
        new AlertDialog.Builder(getActivity()).setTitle("请选择您停车的楼层")
                .setSingleChoiceItems(sFloorItems, (int) mFloorTextView.getTag(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TCBApp.getAppContext().saveInt(R.string.sp_remark_floor, which);
                        mFloorTextView.setTag(which);
                        mFloorTextView.setBackgroundResource(R.drawable.ic_remark_floor_choosed);
                        mFloorTextView.setText(sFloorItems[which]);
                        dialog.dismiss();
                    }
                }).setPositiveButton("取消", null).show();
    }

    private void onLocateBtnClicked() {
        if (mLocateButton.getTag() != null) {
            startActivity(new Intent(TCBApp.getAppContext(), WalkingNaviActivity.class));
        } else {
            startActivityForResult(
                    new Intent(TCBApp.getAppContext(), ChooseLocationActivity.class), REQUEST_CODE_CHOOSE_LOCATION);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CHOOSE_LOCATION:
                    mLocateButton.setImageResource(R.drawable.ic_remark_walking);
                    mLocateButton.setTag(true);
                    getActivity().setResult(Activity.RESULT_OK);
                    break;
                case MapActivity.REQUEST_CODE_CAMERA:
                    //刷新图片
                    if (mImage != null) {
                        ImageLoader.getInstance().displayImage("file://" + mImage.getAbsolutePath(), mPhotoView);
                    }
                    break;
                case REQUEST_CODE_ADD_TIPS:
                    String text = data.getStringExtra(InputTextActivity.ARG_DATA);
                    mAddTipsTextView.setText(text);
                    int visibility = TextUtils.isEmpty(text) ? View.VISIBLE : View.GONE;
                    mAddTipsButton.setVisibility(visibility);
                default:
                    break;
            }
        }
    }
}
