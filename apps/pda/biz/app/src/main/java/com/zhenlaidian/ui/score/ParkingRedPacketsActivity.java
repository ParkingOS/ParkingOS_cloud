package com.zhenlaidian.ui.score;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.tencent.qq.QQ;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.bean.RedPacketInfo;
import com.zhenlaidian.bean.ShareInfo;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.ScoreRuleActivity;
import com.zhenlaidian.util.IsNetWork;
/**
 * 发送停车券界面
 * @author zhangyunfei
 * 2015年8月24日
 */
public class ParkingRedPacketsActivity extends BaseActivity {

	private TextView tv_redpackets_number; // 停车卷张数；
	private TextView tv_redpackets_money; // 停车卷金额；
	private TextView tv_redpackets_big_money; // 停车卷金额放大效果；
	private TextView tv_redpackets_send_warn; // 发送提示；
	private EditText et_redpackets_blessing; // 分享祝福语；
	private Button bt_redpackets_send_friend; // 发送给朋友按钮；
	private ActionBar mActionBar;
	private static ShareInfo shareinfo;
	private static RedPacketInfo redpacket;
	private boolean isShareSDKDialogShowing;// 当前是否正在显示ShareSDK对话框
	private String shareurl;
	private String titleUrl = null;
	private String text;

	@SuppressLint({ "NewApi", "HandlerLeak" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_parking_redpackets);
		if (baseurl.equals("http://192.168.199.239/zld/")) {
			shareurl = "http://wang151068941.oicp.net/zld/carowner.do?action=getobonus&id=";// 本地239测试红包
		} else if (baseurl.equals("http://192.168.199.240/zld/")) {
			shareurl = "http://yxiudongyeahnet.vicp.cc/zld/carowner.do?action=getobonus&id=";// 本地240测试红包
		} else if (baseurl.equals("http://192.168.199.251/zld/")) {
			shareurl = "http://drhhyh.xicp.net/zld/carowner.do?action=getobonus&id=";// 本地251测试红包
		} else {
			shareurl = "http://www.tingchebao.com/zld/carowner.do?action=getobonus&id=";// 线上正式红包地址
		}
		mActionBar = getActionBar();
		mActionBar.setTitle("停车劵礼包");
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.red_bg));
		mActionBar.show();
		redpacket = (RedPacketInfo) getIntent().getExtras().getSerializable("redpacket");
		initVeiw();
		setVeiw();
		getRewardScoreInfo();

	}

	public void initVeiw() {
		tv_redpackets_number = (TextView) findViewById(R.id.tv_redpackets_number);
		tv_redpackets_money = (TextView) findViewById(R.id.tv_redpackets_total_money);
		tv_redpackets_big_money = (TextView) findViewById(R.id.tv_redpackets_big_money);
		tv_redpackets_send_warn = (TextView) findViewById(R.id.tv_redpackets_send_warn);
		et_redpackets_blessing = (EditText) findViewById(R.id.et_redpackets_blessing);
		bt_redpackets_send_friend = (Button) findViewById(R.id.bt_redpackets_send_friend);
		bt_redpackets_send_friend.setClickable(true);
		tv_redpackets_send_warn.setText("点击发送后将扣除红包积分！");
		bt_redpackets_send_friend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO 点击调用分享到微信好友
				bt_redpackets_send_friend.setClickable(false);
				tv_redpackets_send_warn.setText("继续发红包,请重新进入此界面！");
				getDeduct();
				showShare();
			}
		});
	}

	public void setVeiw() {
		tv_redpackets_big_money.setText("￥" + redpacket.getBmoney());
		tv_redpackets_money.setText(redpacket.getBmoney());
		if (redpacket.getBnum() != null) {
			tv_redpackets_number.setText(redpacket.getBnum());
		}
	}

	// 56：收费员用赏金积分发红包
	// collectorrequest.do?action=sendbonus&token=5286f078c6d2ecde9b30929f77771149&bmoney=12&bnum=8&score=1
	// 输入参数：
	// bmoney：红包金额
	// bnum：红包个数
	// score：消耗积分
	// 返回值类型：JSON对象
	// result：1：成功 -1：出错了 -3：赏金积分不足
	// bonusid：红包ID
	// cname：车场名称
	public void getRewardScoreInfo() {
		if (!IsNetWork.IsHaveInternet(this)) {
			Toast.makeText(this, "请检查网络", 0).show();
			return;
		}
		String url = baseurl + "collectorrequest.do?action=sendbonus&token=" + token + "&bmoney="
				+ redpacket.getBmoney() + "&bnum=" + redpacket.getBnum() + "&score=" + redpacket.getScore();
		Log.e("ParkingRedPacketsActivity", "获取红包链接的URL--->" + url);
		final ProgressDialog dialog = ProgressDialog.show(this, "获取红包详情", "加载中...", true, true);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {
				if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
					dialog.dismiss();
					Log.e("ParkingRedPacketsActivity", "获取到红包链接--->" + object);
					Gson gson = new Gson();
					shareinfo = gson.fromJson(object, ShareInfo.class);
					Log.e("ParkingRedPacketsActivity", "解析到红包链接--->" + shareinfo.toString());
					if (shareinfo.getResult() != null) {
						if (shareinfo.getResult().equals("-1")) {
							Toast.makeText(ParkingRedPacketsActivity.this, "获取红链接包出错了！", 0).show();
							return;
						}
						if (shareinfo.getResult().equals("-3")) {
							Toast.makeText(ParkingRedPacketsActivity.this, "积分不足了！", 0).show();
							return;
						}
					}
					if (shareinfo != null && !TextUtils.isEmpty(shareinfo.getBonusid())) {
						text = "我是停车宝收费员,给您赠送" + shareinfo.getCname() + "专用券,邀请您来我车场停车";
						et_redpackets_blessing.setText(text);
						titleUrl = shareurl + shareinfo.getBonusid();
					}
				} else {
					dialog.dismiss();
					switch (status.getCode()) {
					case 500:
						Toast.makeText(ParkingRedPacketsActivity.this, "服务器错误！", 0).show();
						break;
					case 404:
						Toast.makeText(ParkingRedPacketsActivity.this, "服务器不可用！", 0).show();
						break;
					}
					Toast.makeText(ParkingRedPacketsActivity.this, "网络请求错误！", 0).show();
				}
			}
		});
	}

	private void showShare() {
		if (isShareSDKDialogShowing) {
			return;
		}
		if (titleUrl == null) {
			Toast.makeText(this, "网络不好，请重新进入此页面", 0).show();
			return;
		}
		isShareSDKDialogShowing = true;
		File image = null;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File externalStorageDirectory = Environment.getExternalStorageDirectory();
			String path = externalStorageDirectory.getAbsolutePath() + "/TinCheBao/pic/";
			File directory = new File(path);
			if (!directory.exists()) {
				directory.mkdirs();
			}
			image = new File(new File(path), "private_packet.png");
			if (!(image.exists() && image.length() > 0)) {
				copyAppIconToLocal(image);
			}
		}
		ShareSDK.initSDK(this, "35f1018c0ef0");
		OnekeyShare oks = new OnekeyShare();
		// 隐藏平台
		oks.addHiddenPlatform(QQ.NAME);
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(shareinfo.getCname() + "专用券");
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl(titleUrl);
		// text是分享文本，所有平台都需要这个字段
		oks.setText(text);
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		if (image != null) {
			oks.setImagePath(image.getAbsolutePath());
			oks.setImageUrl(titleUrl);
		}
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl(titleUrl);
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		oks.setComment("我是测试评论文本");
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite("停车宝");
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl("http://www.tingchebao.com");
		// 设置编辑界面为窗口模式，必须设置，否则报错
		oks.setDialogMode();
		// 启动分享GUI
		oks.show(this);
	}

	private void copyAppIconToLocal(File file) {
		InputStream istream = null;
		OutputStream ostream = null;
		try {
			AssetManager am = this.getAssets();
			istream = am.open("private_packet.png");
			file.createNewFile();
			ostream = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = istream.read(buffer)) > 0) {
				ostream.write(buffer, 0, length);
			}
			istream.close();
			ostream.close();
			Log.e("RecommendActivity", "copy appicon success: --->>" + file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (istream != null)
					istream.close();
				if (ostream != null)
					ostream.close();
			} catch (Exception ee) {
				ee.printStackTrace();
			}
		}
	}

	// 分享成功后扣除积分的url：collectorrequest.do?action=sendsuccess&token=&bonusid&score=
	public void getDeduct() {
		if (!IsNetWork.IsHaveInternet(this)) {
			return;
		}

		if (shareinfo == null || redpacket == null) {
			return;
		}
		String url = Config.getUrl(this) + "collectorrequest.do?action=sendsuccess&token=" + token + "&bonusid="
				+ shareinfo.getBonusid() + "&score=" + redpacket.getScore();
		Log.e("OnekeyShare", "分享后扣除积分的URL--->" + url);
		AQuery aQuery = new AQuery(this);
		aQuery.ajax(url, String.class, new AjaxCallback<String>() {

			@Override
			public void callback(String url, String object, AjaxStatus status) {

				if (status.getCode() == 200 && !TextUtils.isEmpty(object)) {
					if ("1".equals(object)) {
						Log.e("OnekeyShare", "扣分成功");
					} else {
						Log.e("OnekeyShare", "扣分失败： " + object);
					}
				}
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressWarnings("deprecation")
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.what_is_red_packet, menu);
		MenuCompat.setShowAsAction(menu.findItem(R.id.red_packet), MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	// actionBar的点击回调方法
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.red_packet:
			Intent intent = new Intent(ParkingRedPacketsActivity.this, ScoreRuleActivity.class);
			intent.putExtra("type", 3);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onResume() {
		super.onResume();
		isShareSDKDialogShowing = false;
	}
}
