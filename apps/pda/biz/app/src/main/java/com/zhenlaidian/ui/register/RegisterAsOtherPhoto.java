package com.zhenlaidian.ui.register;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhenlaidian.R;
import com.zhenlaidian.ui.BaseActivity;
/**
 * 注册需要照片材料样例；
 * @author zhangyunfei
 * 2015年8月24日
 */
public class RegisterAsOtherPhoto extends BaseActivity {

	private ImageView iv_photo;
	private TextView bt_cancel;
	private Button bt_next;
	private TextView tv_warn;
	private int page;//第几个图片页面；

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		setContentView(R.layout.register_other_photo_activity);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
				ActionBar.DISPLAY_HOME_AS_UP);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.show();
		page = 1;
		setView();
	}

	public void setView() {
		iv_photo = (ImageView) findViewById(R.id.iv_register_other_photo);
		bt_cancel = (TextView) findViewById(R.id.bt_register_other_cancle);
		bt_next = (Button) findViewById(R.id.bt_register_other_next);
		tv_warn = (TextView) findViewById(R.id.tv_register_other_warn);
		iv_photo.setBackgroundResource(R.drawable.register_photo11);
		
		bt_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RegisterAsOtherPhoto.this.finish();
			}
		});
		
		bt_next.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				switch (page) {
				case 1:
					page = 2;
					iv_photo.setBackgroundResource(R.drawable.register_photo12);
					tv_warn.setText("若以上内容齐全，则相对容易通过审核！");
					break;
				case 2:
					page = 3;
					iv_photo.setBackgroundResource(R.drawable.register_photo13);
					tv_warn.setText("若只上传类似图片，则不可能通过审核！");
					bt_cancel.setVisibility(View.GONE);
					bt_next.setText("知道了");
					break;
				case 3:
					RegisterAsOtherPhoto.this.finish();
					break;
				}
				
			}
		});
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			RegisterAsOtherPhoto.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
