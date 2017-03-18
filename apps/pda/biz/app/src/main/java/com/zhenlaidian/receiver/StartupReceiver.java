package com.zhenlaidian.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zhenlaidian.ui.LoginActivity;

public class StartupReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("StartupReceiver", "收到开机广播，启动应程序---");
		 Intent i = new Intent(context,LoginActivity.class);
	        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        //将intent以startActivity传送给操作系统
	        context.startActivity(i);
	}

}
