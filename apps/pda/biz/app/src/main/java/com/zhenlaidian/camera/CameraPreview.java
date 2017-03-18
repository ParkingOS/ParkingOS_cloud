package com.zhenlaidian.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.zhenlaidian.util.CommontUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * sufaceView 的预览类，其中SurfaceHolder.CallBack用来监听Surface的变化，
 * 当Surface发生改变的时候自动调用该回调方法
 * 通过调用方SurfaceHolder.addCallBack来绑定该方法
 * @author zw.yan
 *
 */
public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {

	private String TAG = "CameraPreview";
	/**
	 * Surface的控制器，用来控制预览等操作
	 */
	private SurfaceHolder mHolder;
	/**
	 * 相机实例
	 */
	private Camera mCamera = null;
	/**
	 * 图片处理
	 */
	public static final int MEDIA_TYPE_IMAGE = 1;
	/**
	 * 预览状态标志
	 */
	private boolean isPreview = false;
	/**
	 * 设置一个固定的最大尺寸
	 */
	private int maxPictureSize = 5000000;
	/**
	 * 是否支持自动聚焦，默认不支持
	 */
	private Boolean isSupportAutoFocus = false;
	/**
	 * 获取当前的context
	 */
	private Context mContext;
	/**
	 * 当前传感器的方向，当方向发生改变的时候能够自动从传感器管理类接受通知的辅助类
	 */
	MyOrientationDetector cameraOrientation;
	/**
	 * 设置最适合当前手机的图片宽度
	 */
	int setFixPictureWidth = 0;
	/**
	 * 设置当前最适合的图片高度
	 */
	int setFixPictureHeight = 0;

	@SuppressWarnings("deprecation")
	public CameraPreview(Context context,callbackFile call) {
		super(context);
		this.mContext = context;
		isSupportAutoFocus = context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_AUTOFOCUS);
		mHolder = getHolder();
		//兼容android 3.0以下的API，如果超过3.0则不需要设置该方法
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		mHolder.addCallback(this);//绑定当前的回调方法
		this.call = call;
	}

	/**
	 * 创建的时候自动调用该方法
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (mCamera == null) {
			mCamera = CameraCheck.getCameraInstance(mContext);
		}
		try {
			if(mCamera!=null){
				mCamera.setPreviewDisplay(holder);
			}
		} catch (IOException e) {
			if (null != mCamera) {
				mCamera.release();
				mCamera = null;
				isPreview=false;
			}
			e.printStackTrace();
		}

	}
	/**
	 * 当surface的大小发生改变的时候自动调用的
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (mHolder.getSurface() == null) {
			return;
		}
		try {
			setCameraParms();
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();
			reAutoFocus();
		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
	}

	private void setCameraParms()  {
		Camera.Parameters myParam = mCamera.getParameters();

		List<Camera.Size> mSupportedsizeList =myParam.getSupportedPictureSizes();
		if(mSupportedsizeList.size() > 1) {
			Iterator<Camera.Size> itos = mSupportedsizeList.iterator();
			while (itos.hasNext()){
				Camera.Size curSize = itos.next();
				int curSupporSize=curSize.width * curSize.height;
				int fixPictrueSize= setFixPictureWidth  * setFixPictureHeight;
				if( curSupporSize>fixPictrueSize && curSupporSize <= maxPictureSize) {
					setFixPictureWidth  = curSize.width;
					setFixPictureHeight = curSize.height;
				}
			}
		}
		myParam.setJpegQuality(100);
		if(CommontUtils.Is900()){
			myParam.setPreviewSize(640,480);
			myParam.setPictureSize(800,600);
		}else{
			myParam.setPreviewSize(1280,720);
			myParam.setPictureSize(800,600);
		}

		mCamera.setParameters(myParam);
		if (myParam.getMaxNumDetectedFaces() > 0){
		       mCamera.startFaceDetection();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
//		Log.i("11111111111","destroy");
		if(mCamera!=null){
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}
	/**
	 * Call the camera to Auto Focus
	 */
	public void reAutoFocus() {
		if (isSupportAutoFocus&&mCamera!=null) {
			try {
				mCamera.autoFocus(new AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean success, Camera camera) {
					}
				});
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	/**
	 * 自动聚焦，然后拍照
	 */
	public void takePicture() {
		if (mCamera != null) {
			if(CommontUtils.Is910()){
				takePhoto();
			}else{
				mCamera.autoFocus(autoFocusCallback);
			}

		}

	}

	private AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {

		public void onAutoFocus(boolean success, Camera camera) {
			// TODO Auto-generated method stub

			if (success) {
				Log.i(TAG, "autoFocusCallback: success...");
				takePhoto();
			} else {
				Log.i(TAG, "autoFocusCallback: fail...");
				if (isSupportAutoFocus) {
					takePhoto();
				}
			}
		}
	};
	/**
	 * 调整照相的方向，设置拍照相片的方向
	 */
	private void takePhoto() {
		cameraOrientation = new MyOrientationDetector(mContext);
		if (mCamera != null) {
			int orientation = cameraOrientation.getOrientation();
			Camera.Parameters cameraParameter = mCamera.getParameters();
			//910 这里是给相机旋转角度
			cameraParameter.setRotation(90);
			cameraParameter.set("rotation", 90);
			if ((orientation >= 45) && (orientation < 135)) {
				cameraParameter.setRotation(180);
				cameraParameter.set("rotation", 180);
			}
			if ((orientation >= 135) && (orientation < 225)) {
				cameraParameter.setRotation(270);
				cameraParameter.set("rotation", 270);
			}
			if ((orientation >= 225) && (orientation < 315)) {
				cameraParameter.setRotation(0);
				cameraParameter.set("rotation", 0);
			}
			mCamera.setParameters(cameraParameter);
			mCamera.takePicture(shutterCallback, pictureCallback, mPicture);
		}
	}

	private ShutterCallback shutterCallback = new ShutterCallback() {
		@Override
		public void onShutter() {
			// TODO Auto-generated method stub
			Log.d(TAG, "onShutter  正在拍");
		}
	};

	private PictureCallback pictureCallback = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] arg0, Camera arg1) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onPictureTaken  拍完了");
		}
	};
	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			new SavePictureTask().execute(data);
			Log.d(TAG, "mPicture  又对焦");
			mCamera.startPreview();//重新开始预览
		}
	};

	public class SavePictureTask extends AsyncTask<byte[], String, String> {
		@SuppressLint("SimpleDateFormat")
		@Override
		protected String doInBackground(byte[]... params) {
			File pictureFile = CommontUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE,
					mContext);
			Log.d(TAG, "pictureFile"+pictureFile.getAbsolutePath());
			call.fileaddr(pictureFile.getAbsolutePath());
			if (pictureFile == null) {
				Toast.makeText(mContext, "请插入存储卡！", Toast.LENGTH_SHORT).show();
				return null;
			}
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(params[0]);
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}

			return null;
		}
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		reAutoFocus();
		return false;
	}

	public  interface callbackFile{
		void fileaddr(String path);
	}
	private callbackFile call;

	public void openLight() {
		if (mCamera != null) {
			parameters = mCamera.getParameters();
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(parameters);
		}
	}
	private Camera.Parameters parameters;
	public void offLight() {
		if (mCamera != null) {
			parameters = mCamera.getParameters();
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(parameters);
		}
	}


}
