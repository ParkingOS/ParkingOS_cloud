package com.zhenlaidian.ui.person_account;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhenlaidian.R;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.LeaveActivity;
import com.zhenlaidian.util.ImageUtils;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;
import com.zhenlaidian.util.UploadUtil;

@SuppressLint("HandlerLeak")
/**
 * 拍照上传绑定银行卡
 * @author zhangyunfei
 * 2015年8月24日
 */
public class TakePhotoForBankCardActivity extends BaseActivity {

	TextView tv_title;
	TextView tv_next;
	TextView tv_warn;
	ImageView iv_photo;
	Button bt_take;
	Button bt_retake;
	Button bt_ok;
	LinearLayout ll_button;
	private File file;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		setContentView(R.layout.take_photo_bank_card_activity);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		initVeiw();
		setView();
		handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if (msg.what == 111) {
					Toast.makeText(getApplicationContext(), "照片上传成功！", 0).show();
					SharedPreferencesUtils.getIntance(TakePhotoForBankCardActivity.this).setIsCardCheck(true);
					TakePhotoForBankCardActivity.this.finish();
				}else if (msg.what == 112) {
					Toast.makeText(getApplicationContext(), "照片上传失败！", 0).show();
				}
			}};
	}
	
	public void initVeiw(){
		tv_title = (TextView) findViewById(R.id.tv_bandcard_photo_title);
		tv_next = (TextView) findViewById(R.id.tv_bandcard_photo_next);
		tv_warn = (TextView) findViewById(R.id.tv_bandcard_photo_warn);
		iv_photo = (ImageView) findViewById(R.id.iv_bandcard_photo_img);
		bt_take = (Button) findViewById(R.id.bt_bandcard_photo_take);
		bt_retake = (Button) findViewById(R.id.bt_bandcard_photo_retake);
		bt_ok = (Button) findViewById(R.id.bt_bandcard_photo_ok);
		ll_button = (LinearLayout) findViewById(R.id.ll_bandcard_photo_button);
		ll_button.setVisibility(View.GONE);
	}
	
	public void setView(){
		bt_take.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 点击去拍照；
				takePhoto("bankcard");
			}
		});
		bt_retake.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 重新拍摄
				takePhoto("bankcard");
			}
		});
		bt_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 点击长传图片
				upload(file.getAbsolutePath());
			}
		});
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
		MyLog.w("TakePhotoForBankCardActivity", "onActivityResult---：");
		if (resultCode == Activity.RESULT_OK && requestCode == 1) { 
			String SDState = Environment.getExternalStorageState();
			if (SDState.equals(Environment.MEDIA_MOUNTED)){
				if (file == null) {
					Toast.makeText(this, "找不到照片路径！", 0).show();
				}else {
				  int degree = ImageUtils.readPictureDegree(file.getPath());  //* 获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转 
		            Bitmap bitmap = getBitmapFromFile(file, 800, 600);
					if (degree != 0) {
						MyLog.i("TakePhotoForBankCardActivity", "图片的旋转角度是："+degree);
						Bitmap newbitmap = ImageUtils.rotaingImageView(degree, bitmap);  
						boolean savefile = saveBitmap2file(newbitmap, file.getPath());
						if (savefile) {
							tv_next.setVisibility(View.INVISIBLE);
							tv_warn.setVisibility(View.GONE);
							tv_title.setText("您的银行卡信息照片：");
							bt_take.setVisibility(View.GONE);
							ll_button.setVisibility(View.VISIBLE);
							iv_photo.setImageBitmap(newbitmap);
						}
						int degree2 = ImageUtils.readPictureDegree(file.getPath()); 
						MyLog.i("TakePhotoForBankCardActivity", "图片被旋转后的角度是---："+degree2);
						bitmap.recycle();
						bitmap = null;
					}else {
						boolean saveBitmap2file = saveBitmap2file(bitmap, file.getPath());
						if (saveBitmap2file) {
							tv_next.setVisibility(View.INVISIBLE);
							tv_warn.setVisibility(View.GONE);
							tv_title.setText("您的银行卡信息照片：");
							bt_take.setVisibility(View.GONE);
							ll_button.setVisibility(View.VISIBLE);
							iv_photo.setImageBitmap(bitmap);
						}
					}
				}
			}
		}
	}
	
	//上传图片到服务器；
//	useraccount.do?action=uploadpic&uin=   返回1成功！
	public void upload(final String filepath){
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(TakePhotoForBankCardActivity.this, "请检查网络是否连接！", 0).show();
			return;
		}
		final ProgressDialog dialog = ProgressDialog.show(this, "上传中...","银行卡照片正在上传...", true, true);
		dialog.setCanceledOnTouchOutside(false);
		new Thread(){
			@Override
			public void run() {
				super.run();
				String path = baseurl;
				String url = path+"useraccount.do?action=uploadpic&uin="+useraccount;
				String result = UploadUtil.uploadFile(filepath, url);
				MyLog.i("TakePhotoForBankCardActivity", "上传照片的返回结果是-->>"+result);
				if (result != null) {
					dialog.dismiss();
					if (result.equals("1")) {
						Message msg = new Message();
						msg.what = 111;
						handler.sendMessage(msg);
					}else {
						Message msg = new Message();
						msg.what = 112;
						handler.sendMessage(msg);
					}
				}else {
					dialog.dismiss();
				}
			}
		}.start();
		
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
	
	// 返回键退出时提示
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			Intent intent = new Intent(TakePhotoForBankCardActivity.this,EditBankCardActivity.class);
//			startActivity(intent);
			TakePhotoForBankCardActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
//			Intent intent = new Intent(TakePhotoForBankCardActivity.this,EditBankCardActivity.class);
//			startActivity(intent);
			TakePhotoForBankCardActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
