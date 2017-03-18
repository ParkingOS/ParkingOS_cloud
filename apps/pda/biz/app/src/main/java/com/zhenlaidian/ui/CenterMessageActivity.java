package com.zhenlaidian.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhenlaidian.R;
import com.zhenlaidian.adapter.CenterMessageAdapter;
import com.zhenlaidian.adapter.DrawerAdapter;
import com.zhenlaidian.bean.CenterMessage;
import com.zhenlaidian.bean.DrawerItemInfo;
import com.zhenlaidian.engine.DrawerOnItemClick;
import com.zhenlaidian.util.IsNetWork;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.SharedPreferencesUtils;

import java.util.ArrayList;

@SuppressWarnings("deprecation")
/**
 * 消息中心
 * @author zhangyunfei
 * 2015年8月24日
 */
public class CenterMessageActivity extends BaseActivity{

	private ListView lv_msg;
	private TextView tv_msgnull;
	private ImageView iv_call_phone;
	private DrawerLayout drawerLayout = null;
	private ListView lv_left_drawer;
	private int page = 1;
	private CenterMessageAdapter adapter;
	private String maxid;
	private boolean hasmessage = true;
	private ActionBarDrawerToggle mDrawerToggle;
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.center_message_activity);
		SharedPreferencesUtils.getIntance(this).setNewMsg(true);
		initVeiw();
		initActionBar();
		getCenterMessage(true);
	}
	
	public void initVeiw(){
		adapter = new CenterMessageAdapter(this);
		lv_msg = (ListView) findViewById(R.id.lv_center_message_msg);
		iv_call_phone = (ImageView) findViewById(R.id.iv_center_message_phone);
		tv_msgnull = (TextView) findViewById(R.id.tv_center_message_null);
		drawerLayout = (DrawerLayout) findViewById(R.id.center_message_layout);
		lv_left_drawer = (ListView) findViewById(R.id.left_drawer_center_message);
		lv_left_drawer.setAdapter(new DrawerAdapter(DrawerItemInfo.getInstance(),CenterMessageActivity.this));
		lv_left_drawer.setOnItemClickListener(new DrawerOnItemClick(this,drawerLayout,this));
		lv_left_drawer.setScrollingCacheEnabled(false);//设置抽屉的listview不能滑动；
		iv_call_phone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent phoneintent = new Intent("android.intent.action.CALL", Uri.parse("tel:"+ "01056450585"));  
				startActivity(phoneintent);
			}
		});
	}
	public void setAdapter(){
		lv_msg.setVisibility(View.VISIBLE);
		tv_msgnull.setVisibility(View.INVISIBLE);
		lv_msg.setAdapter(adapter);
		lv_msg.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch (scrollState) {
				// 当不滚动时          
				case OnScrollListener.SCROLL_STATE_IDLE: 
					// 判断滚动到底部
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) { 
						if (hasmessage) {
							getCenterMessage(false);
						}
					}
					break;
				}
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
	}
	
	public void initActionBar(){
		drawerLayout.setDrawerListener(new MyDrawerListener());
		mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_drawer_am, R.string.hello_world,R.string.hello_world);
		mDrawerToggle.syncState();
		actionBar = getSupportActionBar();
		actionBar.setTitle("消息中心");
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	/** 抽屉的监听 */
	private class MyDrawerListener implements DrawerLayout.DrawerListener {
		@Override
		public void onDrawerOpened(View drawerView) {// 打开抽屉的回调
			mDrawerToggle.onDrawerOpened(drawerView);
			actionBar.setTitle("停车宝");
		}

		@Override
		public void onDrawerClosed(View drawerView) {// 关闭抽屉的回调
			mDrawerToggle.onDrawerClosed(drawerView);
			actionBar.setTitle("消息中心");
		}

		@Override
		public void onDrawerSlide(View drawerView, float slideOffset) {// 抽屉滑动的回调
			mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
		}

		@Override
		public void onDrawerStateChanged(int newState) {// 抽屉状态改变的回调
			mDrawerToggle.onDrawerStateChanged(newState);
		}
	}
	
	// actionBar的点击回调方法
		@SuppressLint("RtlHardcoded")
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case android.R.id.home:
				if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
					drawerLayout.closeDrawers();
				} else {

					drawerLayout.openDrawer(Gravity.LEFT);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
			}
		}

	
//	 请求消息中心是否有新的消息；zld/collectorrequest.do?action=getmesg&mobile=&page=&maxid=
//	 参数maxid>=0时，返回消息数 ，为空或其它值时，返回对应的第page(默认1)页 数据，默认返回10条，数组格式[{},{}]没有数据时，返回[]
	public void getCenterMessage(final Boolean isfirst){
		if ( ! IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请求消息失败，请检查网络！", 0).show();
			return;
		}
		AQuery aQuery = new AQuery(this);
		String path = baseurl;
		String url = path + "collectorrequest.do?action=getmesg&token="+ token+"&page="+page;
		MyLog.w("CenterMessageActivity", "获取消息中心信息的--URl-"+url);
		final ProgressDialog dialog = ProgressDialog.show(this, "加载中...","获取通知消息...", true, true);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			public void callback(String url, String object, AjaxStatus status) {
				MyLog.i("CurrentOrderDetailsActivity", "获取到的通知消息是：-->>"+object);
				if (status.getCode() == 200 && ! TextUtils.isEmpty(object)) {
					dialog.dismiss();
					Gson gson  = new Gson();
					 ArrayList<CenterMessage> msgs = gson.fromJson(object, new TypeToken<ArrayList<CenterMessage>>(){}.getType());
					 if (msgs != null && msgs.size() != 0) {
						 adapter.addOrder(msgs,CenterMessageActivity.this);
						 if (msgs.size() >= 10) {
							page++;
							hasmessage = true;
						 }else {
							 hasmessage = false;
						 }
						 if (isfirst && msgs.get(0) != null && ! TextUtils.isEmpty(msgs.get(0).getId()) && TextUtils.isDigitsOnly(msgs.get(0).getId())) {
							maxid = msgs.get(0).getId();
						 }
					 }
				} else {
					dialog.dismiss();
					switch (status.getCode()) {
					case -101:
						Toast.makeText(CenterMessageActivity.this, "网络错误！--请求消息失败！", 0).show();
						break;
					case 500:
						Toast.makeText(CenterMessageActivity.this, "服务器错误！--请求消息失败！", 0).show();
						break;
					}
				}
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (! TextUtils.isEmpty(maxid)) {
			SharedPreferencesUtils.getIntance(this).setMsgMaxId(maxid,useraccount);
			MyLog.w("CenterMessageActivity","--onDestroy--保存消息中心最大id=	"+maxid);
		}
	}
}
