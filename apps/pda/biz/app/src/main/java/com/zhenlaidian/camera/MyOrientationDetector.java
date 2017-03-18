package com.zhenlaidian.camera;

import android.content.Context;
import android.util.Log;
import android.view.OrientationEventListener;

/**
 * 方向变化监听器，监听传感器方向的改变
 * @author zw.yan
 *
 */
public class MyOrientationDetector extends OrientationEventListener{
	int Orientation;
    public MyOrientationDetector(Context context ) {
        super(context );
    }
    @Override
    public void onOrientationChanged(int orientation) {
        Log.i("MyOrientationDetector ","onOrientationChanged:"+orientation);
        this.Orientation=orientation;
        Log.d("MyOrientationDetector","当前的传感器方向为"+orientation);
    }
    
    public int getOrientation(){
    	return Orientation;
    }
}
