package com.zhenlaidian.plate_wentong;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.text.format.Time;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.wintone.plateid.PlateCfgParameter;
import com.wintone.plateid.PlateRecognitionParameter;
import com.wintone.plateid.RecogService;
import com.zhenlaidian.R;
import com.zhenlaidian.photo.PosCheckNumberActivity;
import com.zhenlaidian.util.CommontUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
/**
 *
 *
 * 项目名称：plate_id_sample_service
 * 类名称：MemoryCameraActivity
 * 类描述：  视频扫描界面  扫描车牌并识别 （与视频流的拍照识别同一界面）
 * 创建人：张志朋
 * 创建时间：2016-1-29 上午10:55:28
 * 修改人：user
 * 修改时间：2016-1-29 上午10:55:28
 * 修改备注：
 * @version
 *
 */
public class MemoryCameraActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback
{
	private Camera camera;
	private SurfaceView surfaceView;
	private static final String PATH = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera/";
	// private TextView resultEditText;
	private ImageButton back_btn, flash_btn, back, take_pic;
	private ViewfinderView myview;
	private RelativeLayout re;
	private int width, height;
	private TimerTask timer;
	private int preWidth = 0;
	private int preHeight = 0;
	private String number = "", color = "";
	private SurfaceHolder holder;
	private int iInitPlateIDSDK = -1;
	private int nRet = -1;
	private int imageformat = 6;// NV21 -->6
	private int bVertFlip = 0;
	private int bDwordAligned = 1;
	private String[] fieldvalue = new String[14];
	private int rotation = 0;
	private static int tempUiRot = 0;
	private Bitmap bitmap,bitmap1;
	private Vibrator mVibrator;
	private PlateRecognitionParameter prp = new PlateRecognitionParameter();;
	private boolean setRecogArgs = true;// 刚进入此界面后对识别车牌函数进行参数设置
	private boolean isCamera;// 判断是预览识别还是视频识别    true:视频识别        false:预览识别
	private boolean recogType;// 记录进入此界面时是拍照识别还是视频识别   	 true:视频识别 		false:拍照识别
	private byte[] tempData;
	private byte[] picData;
	private Timer time = new Timer();
	public RecogService.MyBinder recogBinder;
	public ServiceConnection recogConn = new ServiceConnection()
	{
		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			recogConn = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			recogBinder = (RecogService.MyBinder) service;
			iInitPlateIDSDK = recogBinder.getInitPlateIDSDK();

			if (iInitPlateIDSDK != 0)
			{
				nRet = iInitPlateIDSDK;
				String[] str = { "" + iInitPlateIDSDK };
				getResult(str);
			}
			// recogBinder.setRecogArgu(recogPicPath, imageformat,
			// bGetVersion, bVertFlip, bDwordAligned);
			PlateCfgParameter cfgparameter = new PlateCfgParameter();
			cfgparameter.armpolice = 4;
			cfgparameter.armpolice2 = 16;
			cfgparameter.embassy = 12;
			cfgparameter.individual = 0;
			// cfgparameter.nContrast = 9;
			cfgparameter.nOCR_Th = 0;
			cfgparameter.nPlateLocate_Th = 5;
			cfgparameter.onlylocation = 15;
			cfgparameter.tworowyellow = 2;
			cfgparameter.tworowarmy = 6;
			cfgparameter.szProvince = "";
			cfgparameter.onlytworowyellow = 11;
			cfgparameter.tractor = 8;
			cfgparameter.bIsNight = 1;
			recogBinder.setRecogArgu(cfgparameter, imageformat, bVertFlip, bDwordAligned);

			// fieldvalue = recogBinder.doRecog(recogPicPath, width,
			// height);

		}
	};
	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			re.removeView(myview);
			setRotationAndView(msg.what);
			re.addView(myview);
			initCamera(holder, rotation);
			super.handleMessage(msg);
		}
	};
	@SuppressLint("NewApi") @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

//		View mDecorView = getWindow().getDecorView();
//		hiddenVirtualButtons(mDecorView);
		if(CommontUtils.Is910()){
			try {
				CommontUtils.ZddYear();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		int uiRot = getWindowManager().getDefaultDisplay().getRotation();// 获取屏幕旋转的角度
		System.out.println("旋转角度——————"+uiRot);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_carmera);

		isCamera = getIntent().getBooleanExtra("camera", false);
		recogType = getIntent().getBooleanExtra("camera", false);
		RecogService.initializeType = recogType;

		findiew();
		setRotationAndView(uiRot);
	}
	//设置相机取景方向和扫面框
	private void setRotationAndView(int uiRot){

		setScreenSize(this);
		System.out.println("屏幕宽："+width+"     屏幕高："+height);
		rotation= Utils.setRotation(width, height, uiRot, rotation);
		System.out.println("rotation------"+rotation);
		if (rotation == 90 || rotation == 270)  //竖屏状态下
		{
			myview = new ViewfinderView(MemoryCameraActivity.this, width, height, false);
			setLinearButton();

		}
		else
		{    //横屏状态下
			myview = new ViewfinderView(MemoryCameraActivity.this, width, height, true);
			setHorizontalButton();

		}
	}
	@SuppressLint("NewApi") private void findiew()
	{
		// TODO Auto-generated method stub
		surfaceView = (SurfaceView) findViewById(R.id.surfaceViwe_video);

		back_btn = (ImageButton) findViewById(R.id.back_camera);
		flash_btn = (ImageButton) findViewById(R.id.flash_camera);
		back = (ImageButton) findViewById(R.id.back);
		take_pic = (ImageButton) findViewById(R.id.take_pic_btn);
		re = (RelativeLayout) findViewById(R.id.memory);
//		hiddenVirtualButtons(re);
		holder = surfaceView.getHolder();
		holder.addCallback(MemoryCameraActivity.this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		if (isCamera)
		{
			take_pic.setVisibility(View.GONE);
		}
		else
		{
			take_pic.setVisibility(View.VISIBLE);
		}
		//因为箭头方向的原因，横竖屏状态下 返回按钮是两张不同的ImageView
		//横屏状态下返回按钮

		back_btn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				// TODO Auto-generated method stub
				finish();
			}
		});
		//竖屏状态下返回按钮
		back.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				// TODO Auto-generated method stub\
				closeCamera();
				finish();
			}
		});
		//闪光灯监听事件
		flash_btn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				// b = true;
				// TODO Auto-generated method stub
				if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
				{
					Toast.makeText(MemoryCameraActivity.this,
							getResources().getString(
									getResources().getIdentifier("no_flash", "string",
											getPackageName())), Toast.LENGTH_LONG).show();
				}
				else
				{
					if (camera != null)
					{
						Camera.Parameters parameters = camera.getParameters();
//						String flashMode = parameters.getFlashMode();
//						if (flashMode.equals(Camera.Parameters.FLASH_MODE_TORCH))
//						{
//							parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//							parameters.setExposureCompensation(0);
//						}
//						else
//						{
//							parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);// 闪光灯常亮
//							parameters.setExposureCompensation(-1);
//						}
//						try
//						{
//							camera.setParameters(parameters);
//						}
//						catch (Exception e)
//						{
//							Toast.makeText(MemoryCameraActivity.this,
//									getResources().getString(
//											getResources().getIdentifier("no_flash",
//													"string", getPackageName())),
//									Toast.LENGTH_LONG).show();
//						}
//						camera.startPreview();
						if(!isFlash){
							parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
							camera.setParameters(parameters);
							isFlash = true;
						}else{
							parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
							camera.setParameters(parameters);
							isFlash = false;
						}
					}
				}
			}

		});
		//拍照按钮
		take_pic.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				// TODO Auto-generated method stub

				isCamera = true;

			}

		});
	}
	private boolean isFlash;
	//设置竖屏方向按钮布局
	private void setLinearButton(){
		int back_w;
		int back_h;
		int flash_w;
		int flash_h;
		int Fheight;
		int take_h;
		int take_w;
		RelativeLayout.LayoutParams layoutParams;
		back.setVisibility(View.VISIBLE);
		back_btn.setVisibility(View.GONE);
		back_h = (int) (height * 0.066796875);
		back_w = (int) (back_h * 1);
		layoutParams = new RelativeLayout.LayoutParams(back_w, back_h);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

		Fheight = (int) (width * 0.75);
		layoutParams.topMargin = (int) (((height - Fheight * 0.8 * 1.585) / 2 - back_h) / 2);
		layoutParams.leftMargin = (int) (width * 0.10486111111111111111111111111111);
		back.setLayoutParams(layoutParams);

		flash_h = (int) (height * 0.066796875);
		flash_w = (int) (flash_h * 1);
		layoutParams = new RelativeLayout.LayoutParams(flash_w, flash_h);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

		Fheight = (int) (width * 0.75);
		layoutParams.topMargin = (int) (((height - Fheight * 0.8 * 1.585) / 2 - flash_h) / 2);
		layoutParams.rightMargin = (int) (width * 0.10486111111111111111111111111111);
		flash_btn.setLayoutParams(layoutParams);

		take_h = (int) (height * 0.105859375);
		take_w = (int) (take_h * 1);
		layoutParams = new RelativeLayout.LayoutParams(take_w, take_h);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

		layoutParams.bottomMargin = (int) (width * 0.10486111111111111111111111111111);
		take_pic.setLayoutParams(layoutParams);
	}
	//设置横屏屏方向按钮布局
	private void setHorizontalButton(){
		int back_w;
		int back_h;
		int flash_w;
		int flash_h;
		int Fheight;
		int take_h;
		int take_w;
		RelativeLayout.LayoutParams layoutParams;
		back_btn.setVisibility(View.VISIBLE);
		back.setVisibility(View.GONE);
		back_w = (int) (width * 0.066796875);
		back_h = (int) (back_w * 1);
		layoutParams = new RelativeLayout.LayoutParams(back_w, back_h);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		Fheight = height;

		Fheight = (int) (height * 0.75);
		layoutParams.leftMargin = (int) (((width - Fheight * 0.8 * 1.585) / 2 - back_h) / 2);
		layoutParams.bottomMargin = (int) (height * 0.10486111111111111111111111111111);
		back_btn.setLayoutParams(layoutParams);

		flash_w = (int) (width * 0.066796875);
		flash_h = (int) (flash_w * 1);
		layoutParams = new RelativeLayout.LayoutParams(flash_w, flash_h);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

		Fheight = (int) (height * 0.75);
		layoutParams.leftMargin = (int) (((width - Fheight * 0.8 * 1.585) / 2 - back_h) / 2);
		layoutParams.topMargin = (int) (height * 0.10486111111111111111111111111111);
		flash_btn.setLayoutParams(layoutParams);

		take_h = (int) (width * 0.105859375);
		take_w = (int) (take_h * 1);
		layoutParams = new RelativeLayout.LayoutParams(take_w, take_h);
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

		layoutParams.rightMargin = (int) (height * 0.10486111111111111111111111111111);
		take_pic.setLayoutParams(layoutParams);
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{

		if (camera == null)
		{
			try
			{
				camera = Camera.open();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return;
			}
		}
		try
		{
			camera.setPreviewDisplay(holder);

			initCamera(holder, rotation);
			re.addView(myview);

			if (timer == null)
			{
				timer = new TimerTask()
				{
					public void run()
					{
						// isSuccess=false;
						if (camera != null)
						{
							try
							{
								camera.autoFocus(new AutoFocusCallback()
								{
									public void onAutoFocus(boolean success, Camera camera)
									{
										// isSuccess=success;
									}
								});
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					};
				};
			}
			time.schedule(timer, 500, 2500);

		}
		catch (IOException e)
		{
			e.printStackTrace();

		}
	}
	@Override
	public void surfaceChanged(final SurfaceHolder holder, int format, int width, int height)
	{


		if (camera != null)
		{
			try {
				camera.autoFocus(new AutoFocusCallback()
				{
					@Override
					public void onAutoFocus(boolean success, Camera camera)
					{
						if (success)
						{
							synchronized (camera)
							{
								new Thread()
								{
									public void run()
									{

										initCamera(holder, rotation);
										super.run();
									}
								}.start();
							}
						}
					}
				});

			} catch (Exception e) {
				// TODO: handle exception
			}

		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{

		try
		{
			if (camera != null)
			{
				camera.setPreviewCallback(null);
				camera.stopPreview();
				camera.release();
				camera = null;
			}
		}
		catch (Exception e)
		{
		}

	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		if(timer!=null){
			timer.cancel();
			timer=null;
		}
		re.removeView(myview);
	}
	int nums = -1;
	int switchs = -1;
	private byte[] intentNV21data;

	@Override
	public void onPreviewFrame(byte[] data, Camera camera)
	{

		// 实时监听屏幕旋转角度

		int uiRot = getWindowManager().getDefaultDisplay().getRotation();// 获取屏幕旋转的角度
		if (uiRot != tempUiRot)
		{
			System.err.println("uiRot:" + uiRot);
			Message mesg = new Message();
			mesg.what = uiRot;
			handler.sendMessage(mesg);
			tempUiRot = uiRot;

		}


		if (setRecogArgs)
		{
			Intent authIntent = new Intent(MemoryCameraActivity.this, RecogService.class);
			bindService(authIntent, recogConn, Service.BIND_AUTO_CREATE);
			setRecogArgs = false;
		}

		if (iInitPlateIDSDK == 0)
		{
			nums++;
			if (nums == 1)
			{
				nums = -1;
				prp.height = preHeight;//
				prp.width = preWidth;//
				prp.picByte = data;
				picData =data;
//				 prp.isCheckDevType = true;
				// 开发码
				prp.devCode = Devcode.DEVCODE;
				if (rotation == 0)
				{
					//通知识别核心,识别前图像应先旋转的角度
					prp.plateIDCfg.bRotate = 0;
					setHorizontalRegion();
				}
				else if (rotation == 90)
				{

					prp.plateIDCfg.bRotate = 1;
					setLinearRegion();

				}
				else if (rotation == 180)
				{
					prp.plateIDCfg.bRotate = 2;
					setHorizontalRegion();
				}
				else if (rotation == 270)
				{
					prp.plateIDCfg.bRotate = 3;
					setLinearRegion();
				}
				// System.out.println("实际区域  " + preWidth +
				// "    " + preHeight);
				// System.out.println("边长："+ViewfinderView.length*2);
				// System.out.println("敏感区域：   " +
				// prp.plateIDCfg.left + "    "
				// + prp.plateIDCfg.right + "    " +
				// prp.plateIDCfg.top
				// + "    " + prp.plateIDCfg.bottom);
				if (isCamera)
				{
					//  进行授权验证  并开始识别
					fieldvalue = recogBinder.doRecogDetail(prp);
					nRet = recogBinder.getnRet();

					if (nRet != 0){
						String[] str = { "" + nRet };
						getResult(str);
					}else{
						getResult(fieldvalue);
						intentNV21data = data;
					}

				}

			}
		}
	}
	//设置横屏时的识别区域
	private void setHorizontalRegion(){
//		prp.plateIDCfg.left = preWidth / 2 - myview.length * preHeight / height;
//		prp.plateIDCfg.right = preWidth / 2 + myview.length * preHeight / height;
		prp.plateIDCfg.left = 0;
		prp.plateIDCfg.right = preWidth;
		prp.plateIDCfg.top = preHeight / 2 - myview.length * preHeight / height;
		prp.plateIDCfg.bottom = preHeight / 2 + myview.length * preHeight / height;
	}
	//设置竖屏时的识别区域
	private void setLinearRegion(){
//		prp.plateIDCfg.left = preHeight / 2 - myview.length * preWidth / height;
//		prp.plateIDCfg.right = preHeight / 2 + myview.length * preWidth / height;
		prp.plateIDCfg.left = 0;
		prp.plateIDCfg.right = preHeight;
		prp.plateIDCfg.top = preWidth / 2 - myview.length * preWidth / height;
		prp.plateIDCfg.bottom = preWidth / 2 + myview.length * preWidth / height;
	}


	/**
	 *
	 * @Title: initCamera
	 * @Description: TODO(初始化相机)
	 * @param @param holder
	 * @param @param r 相机取景方向
	 * @return void 返回类型
	 * @throws
	 */
	@TargetApi(14)
	private void initCamera(SurfaceHolder holder, int r)
	{
		Camera.Parameters parameters = camera.getParameters();
		List<Camera.Size> list = parameters.getSupportedPreviewSizes();
		Camera.Size size;
		int length = list.size();
		int previewWidth = 480;
		int previewheight = 640;
		int second_previewWidth = 0;
		int second_previewheight = 0;
		if (length == 1)
		{
			size = list.get(0);
			previewWidth = size.width;
			previewheight = size.height;
		}
		else
		{
			for (int i = 0; i < length; i++)
			{
//				size = list.get(i);
////				System.out.println("宽   "+size.width+"   高"+size.height);
//				if ((width<height&&height*3==width*4)||(width>height&&width*3==height*4))
//				{
//					if (size.height <= 960 || size.width <= 1280)
//					{
//
//						second_previewWidth = size.width;
//						second_previewheight = size.height;
//
//						if (previewWidth <= second_previewWidth
//								&& second_previewWidth * 3 == second_previewheight * 4)
//						{
//							previewWidth = second_previewWidth;
//							previewheight = second_previewheight;
//						}
//
//					}
//				}
//				else
//				{
//					if((width<height&&height*9==width*16)||(width>height&&width*9==height*16)){
//              		  if ((size.height <= 960 || size.width <= 1280)&&size.width*9==size.height*16) {
//
//                            second_previewWidth = size.width;
//                            second_previewheight = size.height;
//                            if (previewWidth <=second_previewWidth) {
//                                previewWidth = second_previewWidth;
//                                previewheight = second_previewheight;
//                            }
//                        }
//              	}
//					else{
//              		if (size.height <= 960 || size.width <= 1280)
//					{
//
//						second_previewWidth = size.width;
//						second_previewheight = size.height;
//						if (previewWidth <= second_previewWidth)
//						{
//							previewWidth = second_previewWidth;
//							previewheight = second_previewheight;
//						}
//					}
//              	}
//
//				}


				size = list.get(i);
//				System.out.println("宽   "+size.width+"   高"+size.height);
				if ((width<height&&height*3==width*4)||(width>height&&width*3==height*4))
				{
					if (size.height <= 960 || size.width <= 1280)
					{

						second_previewWidth = size.width;
						second_previewheight = size.height;

						if (previewWidth <= second_previewWidth
								&& second_previewWidth * 3 == second_previewheight * 4)
						{
							previewWidth = second_previewWidth;
							previewheight = second_previewheight;
						}

					}
				}
				else
				{
					if((width<height&&height*9==width*16)||(width>height&&width*9==height*16)){
						if ((size.height <= 960 || size.width <= 1280)&&size.width*9==size.height*16) {

							second_previewWidth = size.width;
							second_previewheight = size.height;
							if (previewWidth <=second_previewWidth) {
								previewWidth = second_previewWidth;
								previewheight = second_previewheight;
							}
						}
					}
					else{
						if (size.height <= 960 || size.width <= 1280)
						{

							second_previewWidth = size.width;
							second_previewheight = size.height;
							if (previewWidth <= second_previewWidth)
							{
								previewWidth = second_previewWidth;
								previewheight = second_previewheight;
							}
						}
					}

				}


			}
		}
		preWidth = previewWidth;
		preHeight = previewheight;
		System.out.println("预览分辨率：" + preWidth + "    " + preHeight);
		parameters.setPictureFormat(PixelFormat.JPEG);
		parameters.setPreviewSize(preWidth, preHeight);
//		parameters.setPreviewSize(1280, 720);
//		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS))
//		{
//			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//		}

		if (parameters.getSupportedFocusModes().contains(parameters.FOCUS_MODE_AUTO))
		{

//			isAutoFocus=true;
			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		}

		// parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
		// parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		// setDispaly(parameters,camera);
		// parameters.setExposureCompensation(0);

		camera.setParameters(parameters);
		if (rotation == 90 || rotation == 270)
		{
			if (width < 1080)
			{
				camera.stopPreview();
				camera.setPreviewCallback(null);
			}
		}
		else
		{
			if (height < 1080)
			{
				camera.stopPreview();
				camera.setPreviewCallback(null);
			}
		}

		camera.setDisplayOrientation(r);

		try
		{
			camera.setPreviewDisplay(holder);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		camera.setPreviewCallback(MemoryCameraActivity.this);
		camera.startPreview();

	}

	int[] fieldname = { R.string.plate_number, R.string.plate_color, R.string.plate_color_code, R.string.plate_type_code,
			R.string.plate_reliability, R.string.plate_brightness_reviews, R.string.plate_move_orientation,
			R.string.plate_leftupper_pointX, R.string.plate_leftupper_pointY, R.string.plate_rightdown_pointX,
			R.string.plate_rightdown_pointY, R.string.plate_elapsed_time, R.string.plate_light, R.string.plate_car_color };

	/**
	 *
	 * @Title: getResult
	 * @Description: TODO(获取结果)
	 * @param @param fieldvalue 调用识别接口返回的数据
	 * @return void 返回类型
	 * @throwsbyte[]picdata
	 */

	private void getResult(String[] fieldvalue){

		if (nRet != 0)
		//未通过验证  将对应错误码返回
		{
			feedbackWrongCode();
		}
		else{
			//通过验证   获取识别结果
			String result = "";
			String[] resultString;
			String timeString = "";
			String boolString = "";
			boolString = fieldvalue[0];

			if (boolString != null && !boolString.equals(""))
			//检测到车牌后执行下列代码
			{

				resultString = boolString.split(";");
				int lenght = resultString.length;
				// Log.e("DEBUG", "nConfidence:" +
				// fieldvalue[4]);
				if (lenght > 0)
				{

					String[] strarray = fieldvalue[4].split(";");

					//静态识别下  判断图像清晰度是否大于75

					if (recogType ? true : Integer.valueOf(strarray[0]) > 75)
					{


						tempData = recogBinder.getRecogData();

						if (tempData != null)
						{

							if (lenght == 1)
							{

								if (fieldvalue[11] != null && !fieldvalue[11].equals(""))
								{
									int time = Integer.parseInt(fieldvalue[11]);
									time = time / 1000;
									timeString = "" + time;
								}
								else
								{
									timeString = "null";
								}

								if (null != fieldname)
								{

									BitmapFactory.Options options = new BitmapFactory.Options();
									options.inPreferredConfig = Config.ARGB_8888;
									options.inPurgeable = true;
									options.inInputShareable = true;
									ByteArrayOutputStream baos = new ByteArrayOutputStream();

									int Height = 0,Width = 0;
									if (rotation == 90||rotation == 270)
									{
										Height = preWidth;
										Width = preHeight;
									}
									else if (rotation == 180||rotation == 0)
									{
										Height = preHeight;
										Width = preWidth;
									}
									YuvImage yuvimage = new YuvImage(tempData, ImageFormat.NV21, Width,Height, null);
									yuvimage.compressToJpeg(new Rect(0, 0, Width,Height), 100, baos);

									bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size(), options);

									bitmap1= Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
											bitmap.getHeight(), null, true);
									String path = savePicture(bitmap1);

									mVibrator = (Vibrator) getApplication().getSystemService(
											Service.VIBRATOR_SERVICE);
									mVibrator.vibrate(100);
									closeCamera();
									Intent intent = new Intent(MemoryCameraActivity.this,
											PosCheckNumberActivity.class);
									number = fieldvalue[0];
									color = fieldvalue[1];

									int left = Integer.valueOf(fieldvalue[7]);
									int top = Integer.valueOf(fieldvalue[8]);
									int w = Integer.valueOf(fieldvalue[9])
											- Integer.valueOf(fieldvalue[7]);
									int h = Integer.valueOf(fieldvalue[10])
											- Integer.valueOf(fieldvalue[8]);
									intent.putExtra("number", number);
									intent.putExtra("from",getIntent().getStringExtra("from"));
									intent.putExtra("color", color);
									intent.putExtra("path", path);
									intent.putExtra("left", left);
									intent.putExtra("top", top);
									intent.putExtra("width", w);
									intent.putExtra("height", h);
									intent.putExtra("time", fieldvalue[11]);
									intent.putExtra("recogType", recogType);
									startActivity(intent);

//									new FrameCapture(intentNV21data, preWidth, preHeight, "10");
									MemoryCameraActivity.this.finish();

								}

							}
							else
							{
								String itemString = "";

								mVibrator = (Vibrator) getApplication().getSystemService(
										Service.VIBRATOR_SERVICE);
								mVibrator.vibrate(100);
								closeCamera();
								Intent intent = new Intent(MemoryCameraActivity.this,
										PosCheckNumberActivity.class);
								for (int i = 0; i < lenght; i++)
								{

									itemString = fieldvalue[0];
									resultString = itemString.split(";");
									number += resultString[i] + ";\n";

									itemString = fieldvalue[1];
									// resultString
									// =
									// itemString.split(";");
									color += resultString[i] + ";\n";
									itemString = fieldvalue[11];
									resultString = itemString.split(";");
									//

								}
								intent.putExtra("from",getIntent().getStringExtra("from"));
								intent.putExtra("number", number);
								intent.putExtra("color", color);
								intent.putExtra("time", resultString);
								intent.putExtra("recogType", recogType);
								startActivity(intent);

								MemoryCameraActivity.this.finish();
							}
						}
					}

				}

			}
			else
			//未检测到车牌时执行下列代码
			{
				if (!recogType)
				//预览识别执行下列代码   不是预览识别  不做处理等待下一帧
				{
					;
					if (picData != null)
					{

						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inPreferredConfig = Config.ARGB_8888;
						options.inPurgeable = true;
						options.inInputShareable = true;
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						YuvImage yuvimage = new YuvImage(picData, ImageFormat.NV21, preWidth,preHeight, null);
						yuvimage.compressToJpeg(new Rect(0, 0, preWidth,preHeight), 100, baos);
						bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size(), options);

						Matrix matrix = new Matrix();
						matrix.reset();
						if (rotation == 90)
						{
							matrix.setRotate(90);
						}
						else if (rotation == 180)
						{
							matrix.setRotate(180);
						}
						else if (rotation == 270)
						{
							matrix.setRotate(270);
							//
						}
						bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
								true);
						String path = savePicture(bitmap1);

						if (fieldvalue[11] != null && !fieldvalue[11].equals(""))
						{
							int time = Integer.parseInt(fieldvalue[11]);
							time = time / 1000;
							timeString = "" + time;
						}
						else
						{
							timeString = "null";
						}

						if (null != fieldname)
						{
							mVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
							mVibrator.vibrate(100);
							closeCamera();
							Intent intent = new Intent(MemoryCameraActivity.this, PosCheckNumberActivity.class);
							number = fieldvalue[0];
							color = fieldvalue[1];
							if (fieldvalue[0] == null)
							{
								number = "null";
							}
							if (fieldvalue[1] == null)
							{
								color = "null";
							}

							int left = prp.plateIDCfg.left;
							int top = prp.plateIDCfg.top;
							int w = prp.plateIDCfg.right - prp.plateIDCfg.left;
							int h = prp.plateIDCfg.bottom - prp.plateIDCfg.top;
							intent.putExtra("from",getIntent().getStringExtra("from"));
							intent.putExtra("number", number);
							intent.putExtra("color", color);
							intent.putExtra("path", path);
							intent.putExtra("left", left);
							intent.putExtra("top", top);
							intent.putExtra("width", w);
							intent.putExtra("height", h);
							intent.putExtra("time", fieldvalue[11]);
							intent.putExtra("recogType", recogType);
							startActivity(intent);

							MemoryCameraActivity.this.finish();
						}
					}
				}
			}
		}

		nRet = -1;
		fieldvalue = null;
	}



	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(CommontUtils.Is910()){
			try {
				CommontUtils.AddYear();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		if (bitmap != null)
		{
			if (!bitmap.isRecycled())
			{
				bitmap.recycle();
				bitmap = null;
			}

		}
		if (bitmap1 != null)
		{
			if (!bitmap1.isRecycled())
			{
				bitmap1.recycle();
				bitmap1 = null;
			}

		}

		if (mVibrator!=null) {
			mVibrator.cancel();
		}
		if (recogBinder != null)
		{
			unbindService(recogConn);
		}

	}
	/**
	 * @Title: closeCamera
	 * @Description: TODO(这里用一句话描述这个方法的作用) 关闭相机
	 * @param
	 * @return void    返回类型
	 * @throws
	 */
	private void closeCamera() {
		// TODO Auto-generated method stub

		synchronized (this) {
			try {
				if(time!=null){
					time.cancel();
					time=null;
				}
				if (camera != null) {
					camera.setPreviewCallback(null);
					camera.stopPreview();
					camera.release();
					camera = null;
				}

			} catch (Exception e) {

			}
		}
	}
	private void feedbackWrongCode(){
		String nretString = nRet + "";
		if (nretString.equals("-1001"))
		{
			Toast.makeText(MemoryCameraActivity.this,
					getString(R.string.recognize_result) + nRet + "\n"
							+ getString(R.string.failed_readJPG_error), Toast.LENGTH_SHORT).show();

		}
		else if (nretString.equals("-10001"))
		{
			Toast.makeText(MemoryCameraActivity.this,
					getString(R.string.recognize_result) + nRet + "\n"
							+ getString(R.string.failed_noInit_function), Toast.LENGTH_SHORT)
					.show();

		}
		else if (nretString.equals("-10003"))
		{
			Toast.makeText(MemoryCameraActivity.this,
					getString(R.string.recognize_result) + nRet + "\n"
							+ getString(R.string.failed_validation_faile), Toast.LENGTH_SHORT)
					.show();

		}
		else if (nretString.equals("-10004"))
		{
			Toast.makeText(MemoryCameraActivity.this,
					getString(R.string.recognize_result) + nRet + "\n"
							+ getString(R.string.failed_serial_number_null), Toast.LENGTH_SHORT)
					.show();

		}
		else if (nretString.equals("-10005"))
		{
			Toast.makeText(MemoryCameraActivity.this,
					getString(R.string.recognize_result) + nRet + "\n"
							+ getString(R.string.failed_disconnected_server), Toast.LENGTH_SHORT)
					.show();

		}
		else if (nretString.equals("-10006"))
		{
			Toast.makeText(MemoryCameraActivity.this,
					getString(R.string.recognize_result) + nRet + "\n"
							+ getString(R.string.failed_obtain_activation_code),
					Toast.LENGTH_SHORT).show();

		}
		else if (nretString.equals("-10007"))
		{
			Toast.makeText(MemoryCameraActivity.this,
					getString(R.string.recognize_result) + nRet + "\n"
							+ getString(R.string.failed_noexist_serial_number), Toast.LENGTH_SHORT)
					.show();

		}
		else if (nretString.equals("-10008"))
		{
			Toast.makeText(MemoryCameraActivity.this,
					getString(R.string.recognize_result) + nRet + "\n"
							+ getString(R.string.failed_serial_number_used), Toast.LENGTH_SHORT)
					.show();

		}
		else if (nretString.equals("-10009"))
		{
			Toast.makeText(MemoryCameraActivity.this,
					getString(R.string.recognize_result) + nRet + "\n"
							+ getString(R.string.failed_unable_create_authfile),
					Toast.LENGTH_SHORT).show();

		}
		else if (nretString.equals("-10010"))
		{
			Toast.makeText(MemoryCameraActivity.this,
					getString(R.string.recognize_result) + nRet + "\n"
							+ getString(R.string.failed_check_activation_code), Toast.LENGTH_SHORT)
					.show();

		}
		else if (nretString.equals("-10011"))
		{
			Toast.makeText(MemoryCameraActivity.this,
					getString(R.string.recognize_result) + nRet + "\n"
							+ getString(R.string.failed_other_errors), Toast.LENGTH_SHORT).show();

		}
		else if (nretString.equals("-10012"))
		{
			Toast.makeText(MemoryCameraActivity.this,
					getString(R.string.recognize_result) + nRet + "\n"
							+ getString(R.string.failed_not_active), Toast.LENGTH_SHORT).show();

		}
		else if (nretString.equals("-10015"))
		{
			Toast.makeText(MemoryCameraActivity.this,
					getString(R.string.recognize_result) + nRet + "\n"
							+ getString(R.string.failed_check_failure), Toast.LENGTH_SHORT).show();

		}
		else
		{
			Toast.makeText(MemoryCameraActivity.this, getString(R.string.recognize_result) + nRet + "\n",
					Toast.LENGTH_SHORT).show();

		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{

			closeCamera();
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	public String savePicture(Bitmap bitmap)
	{
		String strCaptureFilePath = PATH + "plateID_" + ".jpg";
		File dir = new File(PATH);
		if (!dir.exists())
		{
			dir.mkdirs();
		}
		File file = new File(strCaptureFilePath);
		if (file.exists())
		{
			file.delete();
		}
		try
		{
			file.createNewFile();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return strCaptureFilePath;
	}

	public String pictureName()
	{
		String str = "";
		Time t = new Time();
		t.setToNow(); // 取得系统时间。
		int year = t.year;
		int month = t.month + 1;
		int date = t.monthDay;
		int hour = t.hour; // 0-23
		int minute = t.minute;
		int second = t.second;
		if (month < 10)
			str = String.valueOf(year) + "0" + String.valueOf(month);
		else
		{
			str = String.valueOf(year) + String.valueOf(month);
		}
		if (date < 10)
			str = str + "0" + String.valueOf(date + "_");
		else
		{
			str = str + String.valueOf(date + "_");
		}
		if (hour < 10)
			str = str + "0" + String.valueOf(hour);
		else
		{
			str = str + String.valueOf(hour);
		}
		if (minute < 10)
			str = str + "0" + String.valueOf(minute);
		else
		{
			str = str + String.valueOf(minute);
		}
		if (second < 10)
			str = str + "0" + String.valueOf(second);
		else
		{
			str = str + String.valueOf(second);
		}
		return str;
	}
	/**
	 * @param mDecorView{tags} 设定文件
	 * @return ${return_type}    返回类型
	 * @throws
	 * @Title: 沉寂模式
	 * @Description: 隐藏虚拟按键
	 */
//    @TargetApi(19)
//    public void hiddenVirtualButtons(View mDecorView) {
//        if (Build.VERSION.SDK_INT >= 19) {
//            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
//        }
//    }
	/**
	 *
	 * @Title: setScreenSize
	 * @Description: TODO(这里用一句话描述这个方法的作用) 获取屏幕真实分辨率，不受虚拟按键影响
	 * @param @param context    设定文件
	 * @return void    返回类型
	 * @throws
	 */
	@SuppressLint("NewApi") private void setScreenSize(Context context)
	{
		int x, y;
		WindowManager wm = ((WindowManager)
				context.getSystemService(Context.WINDOW_SERVICE));
		Display display = wm.getDefaultDisplay();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
		{
			Point screenSize = new Point();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			{
				display.getRealSize(screenSize);
				x = screenSize.x;
				y = screenSize.y;
			} else
			{
				display.getSize(screenSize);
				x = screenSize.x;
				y = screenSize.y;
			}
		} else
		{
			x = display.getWidth();
			y = display.getHeight();
		}

		width = x;
		height = y;
	}


}
