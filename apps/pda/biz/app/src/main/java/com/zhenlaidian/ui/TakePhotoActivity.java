package com.zhenlaidian.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhenlaidian.R;
import com.zhenlaidian.util.ImageUtils;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.UploadUtil;

public class TakePhotoActivity extends BaseActivity {

	private ImageView imbtn_Photo;
	private ImageView imbtn_Photo_null;
	private Button bt_ok;
	private Button bt_takePhoto;
	private File picFile = null;//文件名
	private String comid;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.take_photo_activity);
		comid = getIntent().getStringExtra("comid");
		MyLog.w("TakePhotoActivity", "comid = " + comid);
		intiView();
		setView();
		MyLog.w("TakePhotoActivity", "onCreate----------");
	}

	public void intiView() {
		imbtn_Photo = (ImageView) findViewById(R.id.imv_take_photo_photo);
		imbtn_Photo_null = (ImageView) findViewById(R.id.imv_take_photo_photo_null);
		bt_ok = (Button) findViewById(R.id.bt_take_photo_ok);
		bt_takePhoto = (Button) findViewById(R.id.bt_take_photo_make);
	}

	public void setView() {
		bt_takePhoto.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					takePhoto();
			}
		});
		bt_ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (picFile != null) {
					upload();
				}else {
					registerOverDialog();
				}
			}
		});
	}


	//不拍照生成注册成功；
	public void registerOverDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setIcon(R.drawable.app_icon_32);
		builder.setTitle("注册完成");
		builder.setMessage("暂时不传照片完成注册吗？");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public  void onClick(DialogInterface dialog, int which) {
				registerOver();
				
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(TakePhotoActivity.this, "取消误操作", 1).show();
			}
		});
		builder.create().show();
	}
	
	private void takePhoto() {
		// 执行拍照前，应该先判断SD卡是否存在
		String SDState = Environment.getExternalStorageState();
		if (SDState.equals(Environment.MEDIA_MOUNTED))// 如果有媒体安装的环境
		{
			 File dir = new File(Environment.getExternalStorageDirectory()+ "/TingCheBao");
			if (!dir.exists()) {
				dir.mkdirs();
			}
			picFile = new File(dir.getAbsolutePath(), "pic.jpeg");
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 传拍的照
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,Uri.fromFile(picFile));
			startActivityForResult(intent, 1);// 跳到拍照页面，这里1没用到，可以在一个onActivityResult里设置requestCode为0来接收新页面的数据。
		} else {
			Toast.makeText(this, "内存卡不存在", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		MyLog.w("TakePhotoActivity", "onActivityResult---：");
		if (resultCode == Activity.RESULT_OK && requestCode == 1)// &&的后面可有可无，因为只有一个返回结果，若多个就可用switch
																	// case判断一下。
		{
			String SDState = Environment.getExternalStorageState();
			if (SDState.equals(Environment.MEDIA_MOUNTED))// 如果有媒体安装的环境
			{
				 File dir = new File(Environment.getExternalStorageDirectory()+ "/TingCheBao");
				if (!dir.exists()) {
					dir.mkdirs();
				}
				picFile = new File(dir.getAbsolutePath(), "pic.jpeg");
//				 * 获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转 
		            int degree = ImageUtils.readPictureDegree(picFile.getPath());  
		            Bitmap bm = getBitmapFromFile(picFile, 1000, 1000);//获取照片的bitmap然后压缩存放；
					if (degree != 0) {
						MyLog.i("TakePhotoActivity", "图片的旋转角度是："+degree);
						 Bitmap newbitmap = ImageUtils.rotaingImageView(degree, bm);  
						 saveBitmap2file(newbitmap, picFile.getPath());
						 int degree2 = ImageUtils.readPictureDegree(picFile.getPath()); 
						 MyLog.i("TakePhotoActivity", "图片被旋转后的角度是---："+degree2);
						 imbtn_Photo_null.setVisibility(View.GONE);
						 imbtn_Photo.setImageBitmap(newbitmap);
					}else {
						imbtn_Photo_null.setVisibility(View.GONE);
						imbtn_Photo.setImageBitmap(bm);
					}
					
			}
		}
	}

	//bitmap保存到文件；
	 @SuppressLint("SdCardPath") static boolean  saveBitmap2file(Bitmap bmp,String filename){  
         CompressFormat format= Bitmap.CompressFormat.JPEG;  
        int quality = 70;  
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
				FileOutputStream fos = new FileOutputStream(picFile);
				bitmap.compress(CompressFormat.JPEG, 50, fos);
				return bitmap;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
//上传图片到服务器；
//	http://192.168.1.148/zld/parkedit.do?action=uploadpic&comid=&filename=&longitude=&latitude=
	public void upload(){
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(TakePhotoActivity.this, "请检查网络是否连接！", 0).show();
			return;
		}
//		locationClient.stop();
		final ProgressDialog dialog = ProgressDialog.show(this, "上传中...","正在上传照片数据...", true, true);
		new Thread(){
			@Override
			public void run() {
				super.run();
				String path = baseurl;
				String url = path+"parkedit.do?action=uploadpic&comid="+comid+"&filename="+picFile;
				String result = UploadUtil.uploadFile(Environment.getExternalStorageDirectory()+"/TingCheBao/pic.jpeg", url);
				MyLog.i("TakePhotoActivity", "上传照片的返回结果是-->>"+result);
				if (result != null) {
					dialog.dismiss();
					if (result.equals("1")) {
						registerOver();
					}else {
						  registerOver();
						 Looper.prepare();
			             Toast.makeText(TakePhotoActivity.this, "照片上传失败！", 1).show();
			             Looper.loop();// 进入loop中的循环，查看消息队列
					}
				}else {
					registerOver();
					dialog.dismiss();
				}
			}
		}.start();
		
	}
	
	public void registerOver(){
		Intent intent = new Intent(TakePhotoActivity.this, RegisterSuccessActivity.class);
		startActivity(intent);
		finish();
	}
	

	@Override
    public void onConfigurationChanged(Configuration newConfig) {
     
    super.onConfigurationChanged(newConfig);
    }
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
			 Toast.makeText(this, "亲,直接点击完成注册可跳过拍照", 1).show();
			 return true;
		 }
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		MyLog.w("TakePhotoActivity", "onDestroy----------");
	}
	
}
