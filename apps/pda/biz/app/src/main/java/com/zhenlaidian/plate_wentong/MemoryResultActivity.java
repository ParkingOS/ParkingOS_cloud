package com.zhenlaidian.plate_wentong;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zhenlaidian.R;

public class MemoryResultActivity extends Activity{
	private TextView number,color;
	private Button confirm;
	private int width,height;
	private TextView text_num,text_color,text_title;
	private ImageView image,image_back;
	private String bitmapPath;
	private Bitmap bitmap = null;
	private boolean recogType;// 记录进入此界面时是拍照识别界面还是视频识别界面   	 true:视频识别 		false:拍照识别

	private static final String PATH = Environment
			.getExternalStorageDirectory().toString() + "/DCIM/Camera/";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 竖屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
		setContentView(R.layout.memoryresult_activity);
		findView();
		recogType  = getIntent().getBooleanExtra("recogType", false);
		System.out.println("识别时间："+getIntent().getStringExtra("time"));
	}

/**
 *
* @Title: findView
* @Description: TODO(这里用一句话描述这个方法的作用)
* @param     设定文件
* @return void    返回类型
* @throws
 */
	private void findView() {
		// TODO Auto-generated method stub

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		width = metric.widthPixels; // 屏幕宽度（像素）
		height = metric.heightPixels; // 屏幕高度（像素）
		number = (TextView) findViewById(R.id.plate_number);
		color = (TextView) findViewById(R.id.plate_color);
		confirm = (Button) findViewById(R.id.confirm);
		text_num=(TextView) findViewById(R.id.text_number);
		text_color = (TextView) findViewById(R.id.text_color);
		image = (ImageView) findViewById(R.id.plate_image);
		image_back = (ImageView) findViewById(R.id.plate_back);
		text_title  = (TextView) findViewById(R.id.plate_title);

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.BELOW, R.id.plate_image);
		layoutParams.leftMargin = width/4;
		layoutParams.bottomMargin = height/6;
		text_num.setLayoutParams(layoutParams);

		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.text_number);
		layoutParams.addRule(RelativeLayout.BELOW, R.id.plate_image);
		layoutParams.leftMargin = width/5;
		layoutParams.bottomMargin = height/8;
		number.setLayoutParams(layoutParams);

		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.BELOW, R.id.text_number);
		layoutParams.leftMargin = width/4;
		layoutParams.bottomMargin = height/10;
		text_color.setLayoutParams(layoutParams);

		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.text_color);
		layoutParams.addRule(RelativeLayout.BELOW, R.id.text_number);
		layoutParams.leftMargin = width/5;
		layoutParams.bottomMargin = height/10;
		color.setLayoutParams(layoutParams);

		layoutParams = new RelativeLayout.LayoutParams(width/4, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
		layoutParams.bottomMargin = height/5;
		confirm.setLayoutParams(layoutParams);

		int bm_width = (int) (width*0.5);
		int bm_height = bm_width*1;
		layoutParams= new RelativeLayout.LayoutParams(bm_width, bm_height);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);
		layoutParams.leftMargin = width/4;
		layoutParams.topMargin = height/8;
		image.setLayoutParams(layoutParams);

		int back_h = (int) (height * 0.066796875);
		int back_w = (int) (back_h * 1);
		layoutParams= new RelativeLayout.LayoutParams(back_w, back_h);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,RelativeLayout.TRUE);
		layoutParams.leftMargin =  (int) (width * 0.05);
		image_back.setLayoutParams(layoutParams);

		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		text_title.setLayoutParams(layoutParams);




		bitmapPath = getIntent().getStringExtra("path");
		int left = getIntent().getIntExtra("left", -1);
		int top  = getIntent().getIntExtra("top", -1);
		int w =getIntent().getIntExtra("width", -1);
		int h = getIntent().getIntExtra("height", -1);
		System.out.println("图片路径"+bitmapPath);
		if(bitmapPath!=null&&!bitmapPath.equals("")){
			bitmap = BitmapFactory.decodeFile(bitmapPath);
			bitmap = Bitmap.createBitmap(bitmap, left, top, w, h);
			if(bitmap!=null){
				image.setImageBitmap(bitmap);
			}
		}

		text_title.setTextSize(20);
		number.setText(getIntent().getCharSequenceExtra("number"));
		color.setText(getIntent().getCharSequenceExtra("color"));
		color.setTextColor(Color.BLACK);
		number.setTextColor(Color.BLACK);
		text_num.setTextColor(Color.BLACK);
		text_color.setTextColor(Color.BLACK);
		image_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MemoryResultActivity.this,MemoryCameraActivity.class);
				intent.putExtra("camera", recogType);//
				startActivity(intent);
				finish();
			}
		});
		confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (bitmap!=null) {
			bitmap = null;
		}
	}

}
