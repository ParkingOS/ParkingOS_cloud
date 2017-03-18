package com.zhenlaidian.photo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhenlaidian.R;
import com.zhenlaidian.adapter.InputCarNumberDialogAdapter;
import com.zhenlaidian.util.CheckUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义车牌输入法的dialog;通过传入canclelistener接口来回调输入的车牌给调用者;
 */
public class InPutCarNumberDialog extends Dialog {

    private ViewPager viewPager;// 页卡内容
    private ImageView imageView;// 动画图片
    private TextView textView1, textView2, textView3;
    private List<View> views;// Tab页面列表
    private int offset = 0;// 动画图片偏移量
    private int currIndex = 0;// 当前页卡编号
    private int bmpW;// 动画图片宽度
    private View view1, view2, view3;// 各个页卡
    private EditText et_carnumber;
    private RelativeLayout rl_delete_edtext;
    private Button bt_ok;
    private String carnumber;
    private Context context;

    public InPutCarNumberDialog(Context context, boolean cancelable, String carnumber, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
        this.carnumber = carnumber;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);
        win.setGravity(Gravity.BOTTOM);
        setContentView(R.layout.dialog_input_car_number);
        InitImageView();
        InitTextView();
        InitViewPager();
        hideTypewriting();
        setView();
        setView1();
        setView2();
        sheView3();
    }

    public String getcarnumber() {
        return et_carnumber.getText().toString().trim();
    }

    /**
     * 初始化数据
     */
    @SuppressLint("InflateParams")
    private void InitViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager_dialog_dialog);
        views = new ArrayList<View>();
        LayoutInflater inflater = getLayoutInflater();
        view1 = inflater.inflate(R.layout.dialog_input_carnumber_province, null);
        view2 = inflater.inflate(R.layout.dialog_input_carnumber_number, null);
        view3 = inflater.inflate(R.layout.dialog_input_carnumber_police_, null);
        views.add(view1);
        views.add(view2);
        views.add(view3);
        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        et_carnumber = (EditText) findViewById(R.id.et_input_carnumber_dialog);

        if (carnumber != null) {
            et_carnumber.setText(carnumber);
        }
        bt_ok = (Button) findViewById(R.id.bt_input_carnumber_ok_dialog);
        rl_delete_edtext = (RelativeLayout) findViewById(R.id.rl_input_carnumber_delete_dialog);
    }

    public void setView() {
        rl_delete_edtext.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String carnumber = et_carnumber.getText().toString().trim();
                if (carnumber.length() >= 1) {
                    int index = et_carnumber.getSelectionStart();
                    Editable editable = et_carnumber.getText();
                    if (index >= 1) {
                        editable.delete(index - 1, index);
                    }
                }
            }
        });
        rl_delete_edtext.setOnLongClickListener(new Button.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                et_carnumber.setText("");
                return false;
            }
        });

        bt_ok.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (CheckUtils.CarChecked(et_carnumber.getText().toString())) {
                    cancel();
                } else {
                    Toast.makeText(context, "请正确输入车牌号!!!", 0).show();
                }
            }
        });
        et_carnumber.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    viewPager.setCurrentItem(1);
                }
            }
        });
    }

    /**
     * 初始化头标
     */

    private void InitTextView() {
        textView1 = (TextView) findViewById(R.id.text1_dialog);
        textView2 = (TextView) findViewById(R.id.text2_dialog);
        textView3 = (TextView) findViewById(R.id.text3_dialog);
        textView1.setOnClickListener((View.OnClickListener) new MyOnClickListener(0));
        textView2.setOnClickListener((View.OnClickListener) new MyOnClickListener(1));
        textView3.setOnClickListener((View.OnClickListener) new MyOnClickListener(2));
    }

    /**
     * 2 * 初始化动画，这个就是页卡滑动时，下面的横线也滑动的效果，在这里需要计算一些数据
     */

    private void InitImageView() {
        imageView = (ImageView) findViewById(R.id.cursor_dialog);
        bmpW = BitmapFactory.decodeResource(context.getResources(), R.drawable.viewpage).getWidth();// 获取图片宽度
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// 获取分辨率宽度
        offset = (screenW / 3 - bmpW) / 2;// 计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        imageView.setImageMatrix(matrix);// 设置动画初始位置
    }

    /**
     * 头标点击监听 3
     */
    private class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        public void onClick(View v) {
            viewPager.setCurrentItem(index);
        }
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        public MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    public class MyOnPageChangeListener implements OnPageChangeListener {

        int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
        int two = one * 2;// 页卡1 -> 页卡3 偏移量

        public void onPageScrollStateChanged(int arg0) {

        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        public void onPageSelected(int arg0) {

            Animation animation = new TranslateAnimation(one * currIndex, one * arg0, 0, 0);
            currIndex = arg0;
            animation.setFillAfter(true);// True:图片停在动画结束位置
            animation.setDuration(300);
            imageView.startAnimation(animation);
            switch (viewPager.getCurrentItem()) {
                case 0:
                    textView1.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                    textView2.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    textView3.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    break;
                case 1:
                    textView2.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                    textView1.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    textView3.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    break;
                case 2:
                    textView3.setTextColor(context.getResources().getColor(R.color.tv_leaveItem_state_green));
                    textView1.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    textView2.setTextColor(context.getResources().getColor(R.color.input_dialog_black));
                    break;
            }
        }
    }

    public void setView1() {
        final GridView gv_province = (GridView) view1.findViewById(R.id.gridview_province_dialog);
        gv_province.setSelector(new ColorDrawable(Color.TRANSPARENT));
        final String[] province = new String[]{"京", "沪", "浙", "苏", "粤", "鲁", "晋", "冀", "豫", "川", "渝", "辽", "吉", "黑", "皖", "鄂",
                "湘", "赣", "闽", "陕", "甘", "宁", "蒙", "津", "贵", "云", "桂", "琼", "青", "新", "藏", "港", "澳", "使", ""};
        ArrayList<String> provinces = new ArrayList<String>();
        for (int i = 0; i < province.length; i++) {
            provinces.add(province[i]);
        }
        InputCarNumberDialogAdapter adapter = new InputCarNumberDialogAdapter(context, provinces, false);
        gv_province.setAdapter(adapter);
        gv_province.setOnItemClickListener(new OnItemClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final int index = et_carnumber.getSelectionStart();
                final Editable editable = et_carnumber.getText();
                editable.insert(index, province[position]);
            }
        });
    }

    public void setView2() {
        final GridView gv_number = (GridView) view2.findViewById(R.id.gridview_number_dialog);
        gv_number.setSelector(new ColorDrawable(Color.TRANSPARENT));
        final String[] number = new String[]{"A", "B", "C", "D", "E", "0", "1", "F", "G", "H", "J", "K", "2", "3", "L", "M",
                "N", "O", "P", "4", "5", "Q", "R", "S", "T", "U", "6", "7", "V", "W", "X", "Y", "Z", "8", "9"};
        ArrayList<String> numbers = new ArrayList<String>();
        for (int i = 0; i < number.length; i++) {
            numbers.add(number[i]);
        }
        InputCarNumberDialogAdapter adapter = new InputCarNumberDialogAdapter(context, numbers, true);
        gv_number.setAdapter(adapter);
        gv_number.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final int index = et_carnumber.getSelectionStart();
                final Editable editable = et_carnumber.getText();
                editable.insert(index, number[position]);
            }
        });
    }

    public void sheView3() {
        final GridView gv_police = (GridView) view3.findViewById(R.id.gridview_police_dialog);
        gv_police.setSelector(new ColorDrawable(Color.TRANSPARENT));
        final String[] police = new String[]{"军", "空", "海", "北", "沈", "兰", "济", "南", "广", "成", "", "", "", "", "", "WJ", "警",
                "消", "边", "水", "", "电", "林", "通", ""};
        ArrayList<String> polices = new ArrayList<String>();
        for (int i = 0; i < police.length; i++) {
            polices.add(police[i]);
        }

        InputCarNumberDialogAdapter adapter = new InputCarNumberDialogAdapter(context, polices, false);
        gv_police.setAdapter(adapter);
        gv_police.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final int index = et_carnumber.getSelectionStart();
                final Editable editable = et_carnumber.getText();
                editable.insert(index, police[position]);

            }
        });
    }

    public void hideTypewriting() {

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_carnumber.getWindowToken(), 0);
        // Android.edittext点击时,隐藏系统弹出的键盘,显示出光标
        // 3.0以下版本可以用editText.setInputType(InputType.TYPE_NULL)来实现。
        // 3.0以上版本除了调用隐藏方法:setShowSoftInputOnFocus(false)
        int sdkInt = Build.VERSION.SDK_INT;// 16 -- 4.1系统
        if (sdkInt >= 11) {
            Class<EditText> cls = EditText.class;
            try {
                Method setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                setShowSoftInputOnFocus.setAccessible(false);
                setShowSoftInputOnFocus.invoke(et_carnumber, false);
                setShowSoftInputOnFocus.invoke(et_carnumber, false);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            et_carnumber.setInputType(InputType.TYPE_NULL);
        }
    }

}
