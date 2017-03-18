package com.zhenlaidian.ui.register;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhenlaidian.R;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.util.ImageUtils;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.UploadUtil;
/**
 * 拍照上传注册收费员
 * @author zhangyunfei
 * 2015年8月24日
 */
public class RegisterAsDocumentPhotoActivity extends BaseActivity {

	private ImageView iv_photo1;
	private ImageView iv_photo2;
	private ImageView iv_photo3;
	private ImageView iv_photo4;
	private ImageView iv_photo5;
	private ImageView iv_photo6;
	private Button bt_finish;
	private Button bt_callme;
	private TextView tv_addmore;
	private TextView tv_look_other;
	private TextView tv_warn;
	private RelativeLayout rl_waitCheck;
	private int position = 0;
	private String[] filename = new String[]{"photo1","photo2","photo3","photo4","photo5","photo6"};//照片的名字
	private String mobile = null;
	private File file;
	private boolean[] isphoto = new boolean[]{false,false,false,false,false,false};//六个位置上是否有照片；
	private int upload = 1;//上传成功一张后 upload ++;
	private Handler handler ;
	private final int TOAST = 1;//主线程打土司；
	private final int UPLOAD = 2;//主线程调上传；
	private 	ImageLoader imageLoader ;
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		setContentView(R.layout.registers_documentphoto_activity);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
				ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		imageLoader = ImageLoader.getInstance();
		SharedPreferences sp = getSharedPreferences("tingchebao", Context.MODE_PRIVATE);
		if (getIntent().getExtras() != null) {
			MyLog.i("RegisterAsDocumentPhotoActivity", getIntent().getExtras()+"");
			mobile = getIntent().getExtras().getString("mobile");
			if (! mobile.isEmpty()) {
				sp.edit().putString("regMobile", mobile).commit();
				sp.edit().putBoolean("iswait", true).commit();
			}
		}
		mobile = sp.getString("regMobile", null);
		handler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == TOAST) {
					showToast(msg.obj+"");
				} else if (msg.what == UPLOAD) {
					upladMore();
				}
			}
		};
		initVeiw();
		setVeiw();
	}
	
	private void initVeiw(){
		iv_photo1 = (ImageView) findViewById(R.id.iv_register_photo1);
		iv_photo2 = (ImageView) findViewById(R.id.iv_register_photo2);
		iv_photo3 = (ImageView) findViewById(R.id.iv_register_photo3);
		iv_photo4 = (ImageView) findViewById(R.id.iv_register_photo4);
		iv_photo5 = (ImageView) findViewById(R.id.iv_register_photo5);
		iv_photo6 = (ImageView) findViewById(R.id.iv_register_photo6);
		bt_finish = (Button) findViewById(R.id.bt_register_photo_finish);
		bt_callme = (Button) findViewById(R.id.bt_register_photo_callme);
		tv_addmore =  (TextView) findViewById(R.id.tv_register_photo_add);
		tv_look_other =  (TextView) findViewById(R.id.tv_register_look_other);
		tv_warn =  (TextView) findViewById(R.id.tv_register_photo_warn);
		rl_waitCheck = (RelativeLayout) findViewById(R.id.rl_wait_check);
	}
	
	private void setVeiw(){
		SharedPreferences sp = getSharedPreferences("tingchebao", Context.MODE_PRIVATE);
		boolean iswait = sp.getBoolean("iswait", false);
		MyLog.i("RegisterAsDocumentPhotoActivity", "iswait="+iswait);
		iv_photo1.setOnClickListener(new MyOnClickListener(1));
		iv_photo2.setOnClickListener(new MyOnClickListener(2));
		iv_photo3.setOnClickListener(new MyOnClickListener(3));
		iv_photo4.setOnClickListener(new MyOnClickListener(4));
		iv_photo5.setOnClickListener(new MyOnClickListener(5));
		iv_photo6.setOnClickListener(new MyOnClickListener(6));
		
		iv_photo2.setClickable(false);
		iv_photo3.setClickable(false);
		iv_photo4.setClickable(false);
		iv_photo5.setClickable(false);
		iv_photo6.setClickable(false);
		
		bt_finish.setOnClickListener(new MyOnClickListener(7));
		bt_callme.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 致电停车宝客服
				Intent phoneintent = new Intent("android.intent.action.CALL", Uri.parse("tel:"+"010-56450585")); 
				startActivity(phoneintent);
			}
		});
		tv_addmore.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 添加更多信息；
				tv_warn.setText("请补充资料证明你是一个真实收费员！");
				iv_photo1.setImageDrawable(getResources().getDrawable(R.drawable.photo_add));
				iv_photo2.setImageDrawable(getResources().getDrawable(R.drawable.photo_hide));
				iv_photo3.setImageDrawable(getResources().getDrawable(R.drawable.photo_hide));
				iv_photo4.setImageDrawable(getResources().getDrawable(R.drawable.photo_hide));
				iv_photo5.setImageDrawable(getResources().getDrawable(R.drawable.photo_hide));
				iv_photo6.setImageDrawable(getResources().getDrawable(R.drawable.photo_hide));
				position = 0;
				upload = 1;
				for (int i = 0; i < isphoto.length; i++) {
					isphoto[i] = false;
				}
				iv_photo1.setClickable(true);
				bt_finish.setClickable(true);
				rl_waitCheck.setVisibility(View.GONE);
			}
		});
		tv_look_other.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(RegisterAsDocumentPhotoActivity.this, RegisterAsOtherPhoto.class);
				startActivity(intent);
			}
		});
		if (iswait) {
			MyLog.w("RegisterAsDocumentPhotoActivity", "iswait="+iswait);
			waitCheck();
//			setImageView();
		}
	}
	
//	public void setImageView(){
//		ArrayList<ImageView> ivphoto = new ArrayList<ImageView>();
//		for (int i = 0; i < 6; i++) {
//			switch (i) {
//			case 0:
//				ivphoto.add(iv_photo1);
//				break;
//			case 1:
//				ivphoto.add(iv_photo2);
//				break;
//			case 2:
//				ivphoto.add(iv_photo3);
//				break;
//			case 3:
//				ivphoto.add(iv_photo4);
//				break;
//			case 4:
//				ivphoto.add(iv_photo5);
//				break;
//			case 5:
//				ivphoto.add(iv_photo6);
//				break;
//			}
//		}
//		String SDState = Environment.getExternalStorageState();
//		if (SDState.equals(Environment.MEDIA_MOUNTED)){	// 如果有媒体安装的环境
//			for (int i = 0; i < filename.length; i++) {
//				File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/TingCheBao/"+filename[i]+".jpeg");
//				if (file.exists()) {
//					imageLoader.displayImage("file://"+Environment.getExternalStorageDirectory().getAbsolutePath() + "/TingCheBao/"+filename[i]+".jpeg", ivphoto.get(i));
//					ivphoto.get(i).setClickable(true);
//					isphoto[i] = true;
//				}else {
//					isphoto[i] = true;
//					ivphoto.get(i).setClickable(true);
//				}
//			}
//		} else {
//			Toast.makeText(this, "内存卡不存在", Toast.LENGTH_LONG).show();
//		}
//	}
	
	private class MyOnClickListener implements OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		public void onClick(View v) {
			switch (index) {
			case 1:
				if (isphoto[0]) {
					position = 0;
					showMaxDialog(filename[position]);
				}else {
					position = 0;
					takePhoto(filename[position]);
				}
				break;
			case 2:
				if (isphoto[1]) {
					position = 1;
					showMaxDialog(filename[position]);
				}else {
					position = 1;
					takePhoto(filename[position]);
				}
				break;
			case 3:
				if (isphoto[2]) {
					position = 2;
					showMaxDialog(filename[position]);
				}else {
					position = 2;
					takePhoto(filename[position]);
				}
				break;
			case 4:
				if (isphoto[3]) {
					position = 3;
					showMaxDialog(filename[position]);
				}else {
					position = 3;
					takePhoto(filename[position]);
				}
				break;
			case 5:
				if (isphoto[4]) {
					position = 4;
					showMaxDialog(filename[position]);
				}else {
					position = 4;
					takePhoto(filename[position]);
				}
				break;
			case 6:
				if (isphoto[5]) {
					position = 5;
					showMaxDialog(filename[position]);
				}else {
					position = 5;
					takePhoto(filename[position]);
				}
				break;
			case 7://确认上传图片；
				if (isphoto[5]) {
					upladMore();
				}else {
					showDialog();
				}
				break;
			}
		}
	}
	
	public void chengeVeiw(Bitmap bmp){
		switch (position) {
		case 0:
			if (isphoto[0]) {
				iv_photo1.setImageBitmap(bmp);
			}else {
				iv_photo1.setImageBitmap(bmp);
				iv_photo2.setImageDrawable(getResources().getDrawable(R.drawable.photo_more));
				iv_photo2.setClickable(true);
				isphoto[0] = true;
			}
			break;
		case 1:
			if (isphoto[1]) {
				iv_photo2.setImageBitmap(bmp);
			}else {
				iv_photo2.setImageBitmap(bmp);
				isphoto[1] = true;
				iv_photo3.setImageDrawable(getResources().getDrawable(R.drawable.photo_more));
				iv_photo3.setClickable(true);
			}
			break;
		case 2:
			if (isphoto[2]) {
				iv_photo3.setImageBitmap(bmp);
			}else {
				iv_photo3.setImageBitmap(bmp);
				isphoto[2] = true;
				iv_photo4.setImageDrawable(getResources().getDrawable(R.drawable.photo_more));
				iv_photo4.setClickable(true);
			}
			break;
		case 3:
			if (isphoto[3]) {
				iv_photo4.setImageBitmap(bmp);
			}else {
				iv_photo4.setImageBitmap(bmp);
				isphoto[3] = true;
				iv_photo5.setImageDrawable(getResources().getDrawable(R.drawable.photo_more));
				iv_photo5.setClickable(true);
			}
			break;
		case 4:
			if (isphoto[4]) {
				iv_photo5.setImageBitmap(bmp);
			}else {
				iv_photo5.setImageBitmap(bmp);
				isphoto[4] = true;
				iv_photo6.setImageDrawable(getResources().getDrawable(R.drawable.photo_more));
				iv_photo6.setClickable(true);
			}
			break;
		case 5:
			if (isphoto[5]) {
				iv_photo6.setImageBitmap(bmp);
			}else {
				iv_photo6.setImageBitmap(bmp);
				isphoto[5] = true;
			}
			break;
		}
	}
	
	@SuppressWarnings("deprecation")
	private void showMaxDialog(final String file) {
		final AlertDialog maxDialog = new AlertDialog.Builder(this).create();
		ImageView max = new ImageView(this);
		max.setScaleType(ScaleType.FIT_XY);
		String SDState = Environment.getExternalStorageState();
		if (SDState.equals(Environment.MEDIA_MOUNTED)){	// 如果有媒体安装的环境
			String imgfilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/TingCheBao/"+file+".jpeg";
			imageLoader.displayImage("file://"+imgfilePath, max);
			maxDialog.setView(max);
		} else {
			Toast.makeText(this, "内存卡不存在", Toast.LENGTH_LONG).show();
		}
		maxDialog.setCancelable(true);
		maxDialog.setCanceledOnTouchOutside(true);
		max.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				maxDialog.dismiss();
			}
		});
		maxDialog.setButton("重新拍摄", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				takePhoto(file);
			}
		});
		maxDialog.show();
	}
	private void takePhoto(String filename) {
		// 执行拍照前，应该先判断SD卡是否存在
		String SDState = Environment.getExternalStorageState();
		if (SDState.equals(Environment.MEDIA_MOUNTED))// 如果有媒体安装的环境
		{
			 File dir = new File(Environment.getExternalStorageDirectory()+ "/TingCheBao");
			if (!dir.exists()) {
				dir.mkdirs();
			}
			file = new File(dir.getAbsolutePath(), filename+".jpeg");
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 传拍的照
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,Uri.fromFile(file));
			startActivityForResult(intent, 1);// 跳到拍照页面，这里1没用到，可以在一个onActivityResult里设置requestCode为0来接收新页面的数据。
		} else {
			Toast.makeText(this, "内存卡不存在", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		MyLog.w("RegisterAsDocumentPhotoActivity", "onActivityResult---：");
		if (resultCode == Activity.RESULT_OK && requestCode == 1) { 
			String SDState = Environment.getExternalStorageState();
			if (SDState.equals(Environment.MEDIA_MOUNTED)){
				if (file == null) {
					Toast.makeText(this, "找不到照片路径！", 0).show();
				}else {
				  int degree = ImageUtils.readPictureDegree(file.getPath());  //* 获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转 
//			            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
		            Bitmap bitmap = getBitmapFromFile(file, 800, 600);
					if (degree != 0) {
						MyLog.i("TakePhotoActivity", "图片的旋转角度是："+degree);
						Bitmap newbitmap = ImageUtils.rotaingImageView(degree, bitmap);  
						boolean savefile = saveBitmap2file(newbitmap, file.getPath());
						if (savefile) {
							chengeVeiw(newbitmap);
						}
						int degree2 = ImageUtils.readPictureDegree(file.getPath()); 
						MyLog.i("TakePhotoActivity", "图片被旋转后的角度是---："+degree2);
						bitmap.recycle();
						bitmap = null;
					}else {
						boolean saveBitmap2file = saveBitmap2file(bitmap, file.getPath());
						if (saveBitmap2file) {
							chengeVeiw(bitmap);
						}
					}
				}
			}
		}
	}
	
//上传图片到服务器；
//	regparker.do?action=uploadpic&mobile=15801482643 目前一次上传一张
//	返回    1：上传成功，-1：上传失败
	public void upload(final String mobile,final String filepath){
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(RegisterAsDocumentPhotoActivity.this, "请检查网络是否连接！", 0).show();
			return;
		}
		if (mobile == null) {
			Toast.makeText(RegisterAsDocumentPhotoActivity.this, "请回到上一步重新验证您的手机号！", 1).show();
			return;
		}
		final ProgressDialog dialog = ProgressDialog.show(this, "上传中...","第" +upload+ "张照片正在上传...", true, true);
		dialog.setCanceledOnTouchOutside(false);
		new Thread(){
			@Override
			public void run() {
				super.run();
				String path = baseurl;
				String url = path+"regparker.do?action=uploadpic&mobile="+mobile;
				String result = UploadUtil.uploadFile(filepath, url);
				MyLog.i("RegisterAsDocumentPhotoActivity", "上传照片的返回结果是-->>"+result);
				if (result != null) {
					dialog.dismiss();
					if (result.equals("1")) {
						upload ++;
						Message msg = new Message();
						msg.what = UPLOAD;
						handler.sendMessage(msg);
					}else {
						Message msg = new Message();
						msg.what = TOAST;
						msg.obj = "第"+upload+"张照片上传失败，请继续点击上传！";
						handler.sendMessage(msg);
					}
				}else {
					Message msg = new Message();
					msg.what = TOAST;
					msg.obj = "第"+upload+"张照片上传失败，请继续点击上传！";
					handler.sendMessage(msg);
					dialog.dismiss();
				}
			}
		}.start();
		
	}
	
	public void upladMore(){
		switch (upload) {
		case 1:
			if (isphoto[0]) {
				upload(mobile,Environment.getExternalStorageDirectory().getAbsolutePath() + "/TingCheBao/"+filename[0]+".jpeg");
			}else {
				showToast("请先拍摄照片再点击上传！");
			}
			break;
		case 2:
			if (isphoto[1]) {
				upload(mobile,Environment.getExternalStorageDirectory().getAbsolutePath() + "/TingCheBao/"+filename[1]+".jpeg");
			}else {
				showToast("您上传了 1 张照片，上传完毕！");
				waitCheck();
			}
			break;
		case 3:
			if (isphoto[2]) {
				upload(mobile,Environment.getExternalStorageDirectory().getAbsolutePath() + "/TingCheBao/"+filename[2]+".jpeg");
			}else {
				showToast("您上传了 2 张照片，上传完毕！");
				waitCheck();
			}
			break;
		case 4:
			if (isphoto[3]) {
				upload(mobile,Environment.getExternalStorageDirectory().getAbsolutePath() + "/TingCheBao/"+filename[3]+".jpeg");
			}else {
				showToast("您上传了 3 张照片，上传完毕！");
				waitCheck();
			}
			break;
		case 5:
			if (isphoto[4]) {
				upload(mobile,Environment.getExternalStorageDirectory().getAbsolutePath() + "/TingCheBao/"+filename[4]+".jpeg");
			}else {
				showToast("您上传了 4 张照片，上传完毕！");
				waitCheck();
			}
			break;
		case 6:
			if (isphoto[5]) {
				upload(mobile,Environment.getExternalStorageDirectory().getAbsolutePath() + "/TingCheBao/"+filename[5]+".jpeg");
			}else {
				showToast("您上传了 5 张照片，上传完毕！");
				waitCheck();
			}
			break;
		case 7:
			showToast("恭喜您成功上传了 6 张照片，上传完毕！");
			waitCheck();
			break;
		}
	}
	
	public void waitCheck(){
		iv_photo1.setClickable(false);
		iv_photo2.setClickable(false);
		iv_photo3.setClickable(false);
		iv_photo4.setClickable(false);
		iv_photo5.setClickable(false);
		iv_photo6.setClickable(false);
		bt_finish.setClickable(false);
		rl_waitCheck.setVisibility(View.VISIBLE);
		SharedPreferences sp = getSharedPreferences("tingchebao", Context.MODE_PRIVATE);
		sp.edit().putBoolean("iswait", true).commit();
	}
	
	public void showToast( String info){
		Toast.makeText(this, info, 1).show();
	}
	
	public void showDialog(){
		AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
		mDialog.setTitle("操作提示");
		mDialog.setMessage("亲，照片越多审核越快，您确定现在要上传吗？");
		mDialog.setNegativeButton("确定",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					upladMore();
				}
			});
		mDialog.setPositiveButton("再多拍些",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			  
			}
		});
		mDialog.create().show();
	}
	
	//bitmap保存到文件；
	static boolean saveBitmap2file(Bitmap bmp, String filename) {
		CompressFormat format = Bitmap.CompressFormat.JPEG;
		int quality = 80;
		OutputStream stream = null;
		try {
			stream = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bmp.compress(format, quality, stream);
	}
	
	//把照片文件压缩后转为bitmap；
	public Bitmap getBitmapFromFile(File dst, int width, int height) {
		if (null != dst && dst.exists()) {
			BitmapFactory.Options opts = null;
			if (width > 0 && height > 0) {
				opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(dst.getPath(), opts);
				// 计算图片缩放比例
				final int minSideLength = Math.min(width, height);
				opts.inSampleSize = ImageUtils.computeSampleSize(opts,minSideLength, width * height);
				opts.inJustDecodeBounds = false;
				opts.inInputShareable = true;
				opts.inPurgeable = true;
			}
			try {
				Bitmap bitmap = BitmapFactory.decodeFile(dst.getPath(), opts);
				FileOutputStream fos = new FileOutputStream(dst);
				bitmap.compress(CompressFormat.JPEG, 80, fos);
				return bitmap;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			if (rl_waitCheck.getVisibility() == View.VISIBLE) {
				RegisterAsDocumentPhotoActivity.this.finish();
			}else {
				AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
				mDialog.setTitle("操作提示");
				mDialog.setMessage("您要退出注册吗？");
				mDialog.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						RegisterAsDocumentPhotoActivity.this.finish();
					}
				});
				mDialog.setNegativeButton("取消", null);
				mDialog.show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}  
	
	// 返回键退出时提示
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (rl_waitCheck.getVisibility() == View.VISIBLE) {
				RegisterAsDocumentPhotoActivity.this.finish();
			}else {
				AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
				mDialog.setTitle("操作提示");
				mDialog.setMessage("您要退出注册吗？");
				mDialog.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						RegisterAsDocumentPhotoActivity.this.finish();
					}
				});
				mDialog.setNegativeButton("取消", null);
				mDialog.show();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
