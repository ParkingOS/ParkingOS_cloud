package com.zhenlaidian.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhenlaidian.R;

/**
 * Created by TCB on 2016/7/1.
 * xulu
 */
public class FragmentShowIMG extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_showimg, null);
        activity = getActivity();
        initView(v);
        return v;
    }

    private void initView(View v) {
        img = ((ImageView) v.findViewById(R.id.fragment_img));
        Bundle b = getArguments();
        Log.i("tmp","********"+b.getString("path"));
        ImageLoader.getInstance().displayImage(b.getString("path"), img);
//        btn = ((Button) v.findViewById(R.id.fragment_btn));
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                click.fragmentclick(v);
//            }
//        });
    }
//    public interface okclick{
//        void fragmentclick(View v);
//    }
//    private okclick click;

    private ImageView img;
    private Activity activity;
//    private Button btn;
}
