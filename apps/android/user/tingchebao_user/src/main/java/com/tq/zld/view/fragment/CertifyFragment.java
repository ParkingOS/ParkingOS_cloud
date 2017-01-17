package com.tq.zld.view.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.soundcloud.android.crop.Crop;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.Plate;
import com.tq.zld.protocal.UploadRequest;
import com.tq.zld.util.Common;
import com.tq.zld.util.KeyboardUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.util.ScreenUtils;
import com.tq.zld.util.URLUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CertifyFragment extends BaseFragment implements View.OnClickListener {

    private static final String IMAGE_UNSPECIFIED = "image/*";

    /**
     * 拍摄图片
     */
    private static final int REQUEST_CODE_TAKE_PHOTO = 10;

    /**
     * 从相册选取图片
     */
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 11;


    /**
     * 编辑第一张图片
     */
    private static final int REQUEST_CODE_EDIT_PHOTO_FIRST = 0;
    /**
     * 编辑第二张图片
     */
    private static final int REQUEST_CODE_EDIT_PHOTO_SECOND = 1;
    /**
     * 重拍的图片编辑
     */
    private static final int REQUEST_CODE_EDIT_PHOTO_RETRY = 2;

    private static final String ARG_PLATE = "car_number";

    private Plate mPlate;

    private EditText mPlateView;
    private Button mSubmitButton;

    // 上传布局
    private View mNoPhotoView;
    private ImageButton mPhotoButton;
    private Button mExampleButton;

    // 查看图片布局
    private ImageView mPhotoView;
    private TextView mLeftButton;
    private TextView mRightButton;
    private TextView mTipsView;
    private File mPhoto1;
    private File mPhoto2;

    private Uri mPhotoUri;
    private int mRequestCode;
    private String mFilePlate;

    public static CertifyFragment newInstance(Plate plate) {
        CertifyFragment fragment = new CertifyFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLATE, plate);
        fragment.setArguments(args);
        return fragment;
    }

    public CertifyFragment() {
        // Required empty public constructor
    }

    @Override
    protected String getTitle() {
        return mPlate == null || TextUtils.isEmpty(mPlate.car_number) ? "添加车牌号" : "认证车牌号";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlate = getArguments().getParcelable(ARG_PLATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_certify, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPlateView = (EditText) view.findViewById(R.id.et_certify_plate);
        mPlateView.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(7)});
        mPlateView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 7) {
                    KeyboardUtils.closeKeybord(mPlateView, getActivity());
                }
            }
        });

        mSubmitButton = (Button) view.findViewById(R.id.btn_certify_submit);
        mSubmitButton.setOnClickListener(this);
        mSubmitButton.setTag(0);

        if (mPlate == null || TextUtils.isEmpty(mPlate.car_number)) {
            //添加车牌
            inflateNoPhotoView(view);
        } else {
            //编辑车牌
            mPlateView.setText(mPlate.car_number);
            mPlateView.setSelection(mPlate.car_number.length());

            int flag = isLocalPhotoExits(mPlate.car_number);
            LogUtils.i(getClass(), "证件照张数：--->> " + flag);
            switch (flag) {
                case 0:
                    inflateNoPhotoView(view);
                    break;
                case 1:
                case 2:
                case 3:
                    inflatePhotoView(view, flag);
                    break;
                default:
                    break;
            }
        }
    }

    private void inflatePhotoView(View view, final int flag) {
        ViewStub photoStub = (ViewStub) view.findViewById(R.id.vs_certify_photo);
        photoStub.setOnInflateListener(new ViewStub.OnInflateListener() {
            @Override
            public void onInflate(ViewStub stub, View inflated) {

                mPhotoView = (ImageView) inflated.findViewById(R.id.iv_certify_photo);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPhotoView.getLayoutParams();
                params.width = ScreenUtils.getScreenWidth(getActivity());
                params.height = (int) (params.width * 0.67);
                mPhotoView.setLayoutParams(params);

                mLeftButton = (TextView) inflated.findViewById(R.id.tv_certify_left_button);
                mLeftButton.setOnClickListener(CertifyFragment.this);
                mRightButton = (TextView) inflated.findViewById(R.id.tv_certify_right_button);
                mRightButton.setOnClickListener(CertifyFragment.this);
                mTipsView = (TextView) inflated.findViewById(R.id.tv_certify_tips);
                mSubmitButton.setTag(flag);

                switch (flag) {
                    case 1:
                        ImageLoader.getInstance().displayImage("file://" + mPhoto1.getAbsolutePath(), mPhotoView);
                        mTipsView.setText("为确保真实\n请换个遮挡方式，将行驶证再拍一张");
                        mRightButton.setText("再拍一张");
                        mRightButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_add, 0, 0, 0);
                        mSubmitButton.setText("提交车牌");
                        mLeftButton.setTag(mPhoto1);
                        break;
                    case 2:
                        ImageLoader.getInstance().displayImage("file://" + mPhoto2.getAbsolutePath(), mPhotoView);
                        mTipsView.setText("为确保真实\n请换个遮挡方式，将行驶证再拍一张");
                        mRightButton.setText("再拍一张");
                        mRightButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_add, 0, 0, 0);
                        mSubmitButton.setText("提交车牌");
                        mLeftButton.setTag(mPhoto2);
                        break;
                    case 3:
                        ImageLoader.getInstance().displayImage("file://" + mPhoto1.getAbsolutePath(), mPhotoView);
                        mTipsView.setText("");
                        mRightButton.setText("看另一张");
                        mRightButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_eye, 0, 0, 0);
                        mSubmitButton.setText("提交车牌&上传认证");
                        mLeftButton.setTag(mPhoto1);
                        break;
                    default:
                        break;
                }
            }
        });
        photoStub.inflate();
    }

    private void inflateNoPhotoView(View view) {
        ViewStub noPhotoStub = (ViewStub) view.findViewById(R.id.vs_certify_no_photo);
        noPhotoStub.setOnInflateListener(new ViewStub.OnInflateListener() {
            @Override
            public void onInflate(ViewStub stub, View inflated) {
                mNoPhotoView = inflated;
                mPhotoButton = (ImageButton) inflated.findViewById(R.id.ib_certify_take_photo);
                mPhotoButton.setOnClickListener(CertifyFragment.this);

                mExampleButton = (Button) inflated.findViewById(R.id.btn_certify_example);
                mExampleButton.setOnClickListener(CertifyFragment.this);
            }
        });
        noPhotoStub.inflate();
    }

    /**
     * 判断本地是否已存在某个车牌的行驶证照片
     *
     * @param plate 车牌号
     * @return 0:不存在，1:存在第一张，2:存在第二张，3:两张都存在
     */
    private int isLocalPhotoExits(String plate) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (dir != null && dir.exists()) {
                mPhoto1 = new File(dir, plate + "_1.webp");
                mPhoto2 = new File(dir, plate + "_2.webp");
                mFilePlate = plate;
                int photo1Exit = 1;
                int photo2Exit = 1 << 1;
                int flag = 0;
                if (mPhoto1.exists()) {
                    flag |= photo1Exit;
                }
                if (mPhoto2.exists()) {
                    flag |= photo2Exit;
                }
                return flag;
            }
        }
        return 0;
    }

    @Override
    public void onClick(View v) {
        if (v == mPhotoButton) {
            String plate = mPlateView.getText().toString();
            if (!Common.checkPlate(plate)) {
                Toast.makeText(TCBApp.getAppContext(), "车牌号不合法！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Toast.makeText(TCBApp.getAppContext(), "未检测到存储卡！", Toast.LENGTH_SHORT).show();
                return;
            }
            File dir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (dir == null) {
                Toast.makeText(TCBApp.getAppContext(), "未找到文件目录！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!dir.exists() && !dir.mkdirs()) {
                Toast.makeText(TCBApp.getAppContext(), "创建文件目录失败！", Toast.LENGTH_SHORT).show();
                return;
            }
            mPhoto1 = new File(dir, plate + "_1.webp");
            mPhoto2 = new File(dir, plate + "_2.webp");
            mFilePlate = plate;
            mRequestCode = REQUEST_CODE_EDIT_PHOTO_FIRST;
//            showChooseDialog(mPhoto1);
            takePhoto(mPhoto1);
        } else if (v == mExampleButton) {
            lookExample();
        } else if (v == mLeftButton) {
            mRequestCode = REQUEST_CODE_EDIT_PHOTO_RETRY;
//            showChooseDialog((File) mLeftButton.getTag());
            takePhoto((File) mLeftButton.getTag());
        } else if (v == mRightButton) {
            onRightButtonClicked();
        } else if (v == mSubmitButton) {
            onSubmitBtnClicked();
        }
    }

    private void compressPhoto(final File file) {

        if (file == null || !file.exists()) {
            return;
        }

        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "图片处理中...", true, false);
        // 压缩文件
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                try {
                    if (bitmap != null) {
                        FileOutputStream stream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 30, stream);
                        stream.flush();
                        stream.close();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        });
                    }
                } catch (IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    });
                    e.printStackTrace();
                }
            }
        }).start();
    }

//    private void showChooseDialog(final File file) {
//        new AlertDialog.Builder(getActivity()).setItems(new String[]{"拍照", "从手机相册选择"}, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                switch (which) {
//                    case 0:
//                        takePhoto(file);
//                        break;
//                    case 1:
//                        choosePhotoFromAlbum(file);
//                        break;
//                    default:
//                        break;
//                }
//            }
//        }).show();
//    }

    private void choosePhotoFromAlbum(File file) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(IMAGE_UNSPECIFIED);
        mPhotoUri = Uri.fromFile(file);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
//        intent.putExtra("return-data", true);
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_PHOTO);
    }

    private void onSubmitBtnClicked() {

        // 检查车牌号
        String plate = mPlateView.getText().toString();
        if (!Common.checkPlate(plate)) {
            Toast.makeText(TCBApp.getAppContext(), "车牌号不合法！", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = mSubmitButton.getText().toString();
        if (mTipsView != null && mTipsView.getText().toString().contains("再拍")) {
            Toast.makeText(getActivity(), "请再拍一张证件照片！", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "上传中...", true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                TCBApp.getAppContext().cancelPendingRequests(CertifyFragment.this);
                Toast.makeText(getActivity(), "已取消", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setCanceledOnTouchOutside(false);

        ArrayList<File> files = new ArrayList<>();
        if (text.contains("认证")) {

            // 如果拍完照片后修改过车牌号，则此时修改本地文件名以保持同步
            if (!mPhoto1.getAbsolutePath().contains(plate)) {
                File photoTemp1 = new File(mPhoto1.getAbsolutePath().replace(mFilePlate, plate));
                File photoTemp2 = new File(mPhoto2.getAbsolutePath().replace(mFilePlate, plate));
                mPhoto1 = mPhoto1.renameTo(photoTemp1) ? photoTemp1 : mPhoto1;
                mPhoto2 = mPhoto2.renameTo(photoTemp2) ? photoTemp2 : mPhoto2;
            }
            files.add(mPhoto1);
            files.add(mPhoto2);
        }
        // action参数追加在URL后方便服务器解析
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "upuserpic");
        params.put("mobile", TCBApp.mMobile);
        params.put("carnumber", mPlateView.getText().toString());
        params.put("old_carnumber", mPlate.car_number);
        URLUtils.decode(params);
        String url = URLUtils.genUrl(TCBApp.mServerUrl + "carinter.do", params);
        UploadRequest request = new UploadRequest(url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.dismiss();
                Toast.makeText(TCBApp.getAppContext(), "网络出错了~", Toast.LENGTH_SHORT).show();
            }
        }, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                dialog.dismiss();
                LogUtils.i(CertifyFragment.class, "upload image response: --->> " + s);
                try {
                    JSONObject result = new JSONObject(s);
                    if (!result.isNull("result")) {
                        Toast.makeText(TCBApp.getAppContext(), result.getString("errmsg"), Toast.LENGTH_SHORT).show();
                        if (1 == result.getInt("result")) {
                            deletePhotos();
                            getActivity().onBackPressed();
                        }
                    } else {
                        Toast.makeText(TCBApp.getAppContext(), "数据格式错误！", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, "image", files, null);
        request.setRetryPolicy(
                new DefaultRetryPolicy(100000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        TCBApp.getAppContext().addToRequestQueue(request, this);
    }

    private void onRightButtonClicked() {
        File file = (File) mLeftButton.getTag();
        boolean isFirst = file.getAbsolutePath().equals(mPhoto1.getAbsolutePath());
        String rightButtonText = mRightButton.getText().toString();
        if (rightButtonText.contains("看")) {
            //看另一张
            if (isFirst) {
                ImageLoader.getInstance().displayImage("file://" + mPhoto2.getAbsolutePath(), mPhotoView);
                mLeftButton.setTag(mPhoto2);
            } else {
                ImageLoader.getInstance().displayImage("file://" + mPhoto1.getAbsolutePath(), mPhotoView);
                mLeftButton.setTag(mPhoto1);
            }
        } else {
            //再拍一张
            mRequestCode = REQUEST_CODE_EDIT_PHOTO_SECOND;
            file = isFirst ? mPhoto2 : mPhoto1;
//            showChooseDialog(file);
            takePhoto(file);
        }
    }

    private void takePhoto(File file) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // path为保存图片的路径，执行完拍照以后能保存到指定的路径下
        mPhotoUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            //压缩并存到SD卡中
//            Bundle extras = null;
//            if (data != null) {
//                extras = data.getExtras();
//                LogUtils.i(getClass(), "extra is null: " + extras);
//            }
//            try {
//                if (extras != null) {
//                    Bitmap photo = extras.getParcelable("data");
//                    File file = mLeftButton == null ? mPhoto1 : (File) mLeftButton.getTag();
//                    FileOutputStream stream = new FileOutputStream(file);
//                    boolean b = photo.compress(Bitmap.CompressFormat.WEBP, 30, stream);
//                    LogUtils.i(getClass(), "compress file size: --->> " + b);
//                    stream.flush();
//                    stream.close();
//                }
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }

            switch (requestCode) {
                case REQUEST_CODE_CHOOSE_PHOTO:
                    if (data != null) {
                        editPhoto(data.getData());
                    }
                    break;
                case REQUEST_CODE_TAKE_PHOTO:
                    // 编辑图片
                    editPhoto(mPhotoUri);
                    break;
                case REQUEST_CODE_EDIT_PHOTO_FIRST:
                    // 拍摄第一张图片
                    mNoPhotoView.setVisibility(View.GONE);
                    inflatePhotoView(getView(), 1);

                    // 压缩图片
                    compressPhoto(mPhoto1);
                    break;
                case REQUEST_CODE_EDIT_PHOTO_SECOND:
                    //拍摄第二张图片
                    ImageLoader.getInstance().displayImage("file://" + mPhoto2.getAbsolutePath(), mPhotoView);
                    mTipsView.setText("");
                    mRightButton.setText("看另一张");
                    mRightButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_eye, 0, 0, 0);
                    mSubmitButton.setText("提交车牌&上传认证");
                    mLeftButton.setTag(mPhoto2);

                    // 压缩图片
                    compressPhoto(mPhoto2);
                    break;
                case REQUEST_CODE_EDIT_PHOTO_RETRY:
                    //重拍
                    File file = (File) mLeftButton.getTag();
                    ImageLoader.getInstance().displayImage("file://" + file.getAbsolutePath(), mPhotoView);

                    // 压缩图片
                    compressPhoto(file);
                    break;
                default:
                    break;
            }
        }
    }

    private void editPhoto(Uri uri) {
//        调用系统编辑图片界面
//        Intent intent = new Intent("com.android.camera.action.CROP");
//        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
//        intent.putExtra("crop", true);
//        intent.putExtra("scale", true);
//        // aspectX aspectY 是宽高的比例
//        intent.putExtra("aspectX", 3);
//        intent.putExtra("aspectY", 2);
//        // outputX, outputY 是裁剪图片宽高
//        int width = ScreenUtils.getScreenWidth(getActivity());
//        intent.putExtra("outputX", width);
//        intent.putExtra("outputY", width * 0.67);
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.WEBP.toString());
//        File extra = mRequestCode == REQUEST_CODE_EDIT_PHOTO_FIRST ? mPhoto1 : mPhoto2;
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(extra));
//        intent.putExtra("return-data", true);
//        startActivityForResult(intent, mRequestCode);
        editPhotoUseOther(uri);
    }

    private void editPhotoUseOther(Uri uri) {
        int width = ScreenUtils.getScreenWidth(getActivity());
//        File extra = mRequestCode == REQUEST_CODE_EDIT_PHOTO_FIRST ? mPhoto1 : mPhoto2;
        Crop crop = Crop.of(uri, mPhotoUri);
//        Intent intent = crop.getIntent(getActivity());
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.WEBP.toString());
        crop.withAspect(3, 2).withMaxSize(width, (int) (width * 0.67)).start(getActivity(), this, mRequestCode);
    }

    private void lookExample() {
        replace(R.id.fragment_container, new PlateAuthExampleFragment(), true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            KeyboardUtils.closeKeybord(mPlateView, getActivity());
        }
        TCBApp.getAppContext().cancelPendingRequests(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden && getActivity() != null) {
            KeyboardUtils.closeKeybord(mPlateView, getActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (TextUtils.isEmpty(mPlate.car_number)) {
            deletePhotos();
        }
    }

    private void deletePhotos() {
        if (mPhoto1 != null && mPhoto1.exists()) {
            mPhoto1.delete();
        }
        if (mPhoto2 != null && mPhoto2.exists()) {
            mPhoto2.delete();
        }
    }
}
