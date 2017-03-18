package com.zhenlaidian.photo;

/**
 *车牌识别调用类；
 */
public  class DecodeManager {
	
	private static DecodeManager manager = new DecodeManager(); 
	private DecodeManager() {}
	
	static {
		System.loadLibrary("TCB");
	}

	public static DecodeManager getinstance(){
	        return manager;
	}
	
	
	public native String init();
	
	public native String destroyAllMemery();
	
	public native byte[] decode(byte[] data, int width, int height, int x, int y, int cwidth, int cheight);
	

	
}


