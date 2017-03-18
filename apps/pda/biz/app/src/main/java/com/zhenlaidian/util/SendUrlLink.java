/**
 * 
 */
package com.zhenlaidian.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.tencent.qq.QQ;

/**
 * 分享发送链接到社交平台；
 * 
 * @author zhangyunfei 2015年9月6日
 */
public class SendUrlLink {
	private boolean isShareSDKDialogShowing;// 当前是否正在显示ShareSDK对话框
	private Context context;
	private String titleUrl;
	private String text;// 内容
	private String titel;// 标题
	private String imagepath;// 图片

	// private_packet.png 红包图片路径
	// app_icon.png appicon路径

	public SendUrlLink() {
		super();
	}

	public SendUrlLink(Context context, String titleUrl, String text, String titel, String imagepath) {
		super();
		this.context = context;
		this.titleUrl = titleUrl;
		this.text = text;
		this.titel = titel;
		this.imagepath = imagepath;
	}

	public void showShare() {
		if (isShareSDKDialogShowing) {
			return;
		}
		if (titleUrl == null) {
			Toast.makeText(context, "网络不好，请重新进入此页面", 0).show();
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
			image = new File(new File(path), imagepath);
			if (!(image.exists() && image.length() > 0)) {
				copyAppIconToLocal(image);
			}
		}
		ShareSDK.initSDK(context, "35f1018c0ef0");
		OnekeyShare oks = new OnekeyShare();
		// 隐藏平台
		oks.addHiddenPlatform(QQ.NAME);
		// 关闭sso授权
		oks.disableSSOWhenAuthorize();
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(titel);
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
		oks.show(context);
	}

	public void copyAppIconToLocal(File file) {
		InputStream istream = null;
		OutputStream ostream = null;
		try {
			AssetManager am = context.getAssets();
			istream = am.open(imagepath);
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
}
