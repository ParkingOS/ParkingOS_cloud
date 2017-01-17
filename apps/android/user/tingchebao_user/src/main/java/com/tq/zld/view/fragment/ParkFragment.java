package com.tq.zld.view.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.ParkInfo;
import com.tq.zld.util.DensityUtils;
import com.tq.zld.util.LogUtils;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.PhotoActivity;
import com.tq.zld.view.map.ParkPriceDetailActivity;

public class ParkFragment extends BaseFragment implements OnClickListener {

    // public static final String ARG_PARK_NAME = "parkname";
    // public static final String ARG_PARK_ID = "parkid";

    public static final String ARG_PARK = "park";

    private View mAddrView;
    private View mPriceView;
    private View mDescView;
    private View mPayView;
    private View mFriendView;
    private TextView mDescTextView;
    private Button mCommentsButton;
    private ImageView mPhotoView;

    private ParkInfo mPark;

    private DisplayImageOptions mOptions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPark = getArguments().getParcelable(ARG_PARK);
        mOptions = DisplayImageOptions.createSimple();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_park, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LogUtils.i(ParkFragment.class, "停车场信息：--->> " + mPark.toString());
        mAddrView = view.findViewById(R.id.ll_park_addr);
        mAddrView.setOnClickListener(this);
        mPriceView = view.findViewById(R.id.ll_park_price);
        mPriceView.setOnClickListener(this);
        mDescView = view.findViewById(R.id.ll_park_desc);
        mDescView.setOnClickListener(this);
        mPayView = view.findViewById(R.id.ll_park_pay);
        mPayView.setOnClickListener(this);
        mFriendView = view.findViewById(R.id.ll_park_friend);
        mFriendView.setOnClickListener(this);

        if (!"1".equals(mPark.epay) || "-1".equals(mPark.price)) {
            mPayView.setVisibility(View.GONE);
        }

        // 设置地址
        TextView addressTextView = (TextView) view.findViewById(R.id.tv_park_addr);
        addressTextView.setText(mPark.addr);

        // 根据是否免费车场显示价格及空闲车位信息
        TextView spaceTextView = (TextView) view.findViewById(R.id.tv_park_space);
        TextView priceTextView = (TextView) view.findViewById(R.id.tv_park_price);
        String price = "免费";
        if (!"-1".equals(mPark.price)) {

            // 收费车场
            if (!TextUtils.isEmpty(mPark.price) && !"0".equals(mPark.price) && !mPark.price.startsWith("0元")) {
                // 价格明确
                price = mPark.price;
            } else {
                // 价格未知
                price = "未知";

                mPriceView.setClickable(false);
                view.findViewById(R.id.tv_park_price_arrow).setVisibility(
                        View.GONE);
            }

            // 设置空闲车位
            String space = TextUtils.isEmpty(mPark.free) ? "未知" : mPark.free;
            String total = TextUtils.isEmpty(mPark.total) ? "未知" : mPark.total;
            if ("未知".equals(space) && "未知".equals(total)) {
                spaceTextView.setText("空闲车位：未知");
            } else {
                spaceTextView.setText("车位：" + space + "/" + total);
            }
        } else {
            mPriceView.setClickable(false);
            view.findViewById(R.id.tv_park_price_arrow)
                    .setVisibility(View.GONE);

            // 免费车场
            spaceTextView.setText("车位: 未知");
        }
        priceTextView.setText(price);

        // 设置描述
        String desc = TextUtils.isEmpty(mPark.desc) ? "暂无描述信息" : mPark.desc;
        mDescTextView = (TextView) view.findViewById(R.id.tv_park_desc);
        mDescTextView.setText(desc);
        if ("暂无描述信息".equals(desc)) {
            view.findViewById(R.id.tv_park_desc_arrow).setVisibility(View.GONE);
            mDescView.setClickable(false);
        }

        // 查看评论按钮
        mCommentsButton = (Button) view.findViewById(R.id.btn_park_comment);
        mCommentsButton.setOnClickListener(this);

        // 设置照片
        mPhotoView = (ImageView) view.findViewById(R.id.iv_park_photo);
        if (mPark.photo_url != null && mPark.photo_url.size() > 0
                && !TextUtils.isEmpty(mPark.photo_url.get(0))) {
            ImageLoader.getInstance().displayImage(
                    TCBApp.mServerUrl + mPark.photo_url.get(0), mPhotoView,
                    mOptions);
            mPhotoView.setOnClickListener(this);
        }
    }

    @Override
    protected String getTitle() {
        return "";
    }

    @Override
    public void onClick(View v) {
        if (v == mAddrView) {
            onAddrViewClicked();
        } else if (v == mPriceView) {
            onPriceViewClicked();
        } else if (v == mPayView) {
            onPayViewClicked();
        } else if (v == mCommentsButton) {
            onCommentsBtnClicked();
        } else if (v == mPhotoView) {
            onPhotoViewClicked();
        } else if (v == mDescView) {
            onDescViewClicked();
        } else if (v == mFriendView){
            onFriendViewClicked();
        }
    }

    private void onDescViewClicked() {

        if ("暂无描述信息".equals(mDescTextView.getText())) {
            return;
        }

        TextView view = new TextView(getActivity());
        int padding = DensityUtils.dip2px(getActivity(), 4);
        view.setPadding(padding, padding, padding, padding);
        view.setText(mPark.desc);
        new AlertDialog.Builder(getActivity()).setView(view).show();
        // AppCompatDialog appCompatDialog = new AppCompatDialog(mActivity);
        // appCompatDialog.setContentView(view);
        // appCompatDialog.show();
    }

    private void onPhotoViewClicked() {
        if (mPark == null || mPark.photo_url == null
                || mPark.photo_url.size() == 0
                || TextUtils.isEmpty(mPark.photo_url.get(0))) {
            return;
        }
        String[] imageURIs = new String[mPark.photo_url.size()];
        for (int i = 0; i < mPark.photo_url.size(); i++) {
            imageURIs[i] = TCBApp.mServerUrl + mPark.photo_url.get(i);
        }
        Intent intent = new Intent(TCBApp.getAppContext(), PhotoActivity.class);
        intent.putExtra(PhotoActivity.ARG_IMAGEURI, imageURIs);
        startActivity(intent);
    }

    private void onAddrViewClicked() {
        // TODO Auto-generated method stub

    }

    private void onPriceViewClicked() {
        // getActivity().getSupportFragmentManager().beginTransaction()
        // .replace(R.id.park_content, new ParkPriceFragment())
        // .addToBackStack(null).commit();

        Intent intent = new Intent(TCBApp.getAppContext(),
                ParkPriceDetailActivity.class);
        intent.putExtra("parkid", mPark.id);
        startActivity(intent);
    }

    private void onPayViewClicked() {

        if (TextUtils.isEmpty(TCBApp.mMobile)) {
            Toast.makeText(getActivity(), "请先登录！", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle args = new Bundle();
        args.putString(ChooseParkingFeeCollectorFragment.ARG_PARK_ID, mPark.id);
        args.putString(ChooseParkingFeeCollectorFragment.ARG_PARK_NAME,
                mPark.name);
        Intent intent = new Intent(TCBApp.getAppContext(), MainActivity.class);
        intent.putExtra(MainActivity.ARG_FRAGMENT,
                MainActivity.FRAGMENT_CHOOSE_COLLECTOR);
        intent.putExtra(MainActivity.ARG_FRAGMENT_ARGS, args);
        startActivity(intent);
    }

    private void onFriendViewClicked() {
        //车场停车记录功能暂未上
//        Intent intent = new Intent(TCBApp.getAppContext(), ParkingRecordsActivity.class);
//        intent.putExtra(ParkingRecordsActivity.ARG_PARK_ID, mPark.id);
//        startActivity(intent);
    }

    private void onCommentsBtnClicked() {
        ParkCommentsFragment fragment = new ParkCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(ParkCommentsFragment.ARG_PARK, mPark);
        fragment.setArguments(args);
        FragmentTransaction ft = getActivity().getSupportFragmentManager()
                .beginTransaction();
        ft.add(R.id.park_content, fragment);
        ft.hide(this);
        ft.addToBackStack(null);
        ft.commit();
    }

}
