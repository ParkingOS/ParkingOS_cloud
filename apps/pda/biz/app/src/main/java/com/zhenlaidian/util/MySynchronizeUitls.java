package com.zhenlaidian.util;

import android.content.Context;
import android.text.TextUtils;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.db.localdb.DaoForPrice;
import com.zhenlaidian.db.localdb.Price_tb;

import java.util.List;

public class MySynchronizeUitls {

	/**
	 * 本地化功能(未实现)
	 * 访问服务器获得服务器时间。
	 * 因操作数据库需要在子线程中运行！！
	 * @param context
	 */
	//parkoffline.do?action=synchroTime
	public static void SynchronizeTime(final Context context) {
		String url = Config.getUrl(context)+"parkoffline.do?action=synchroTime";
		MyLog.w("SynchronizeTime", "请求同步服务器时间的url："+url);
		AQuery aq = new AQuery(context);
		aq.ajax(url, String.class, new AjaxCallback<String>() {
			@Override
			public void callback(String url, String object, AjaxStatus status) {
				super.callback(url, object, status);
				if (!TextUtils.isEmpty(object)) {
					MyLog.i("SynchronizeTime", "服务器返回的时间："+object);
					 long duration = status.getDuration();
					 Long differenceTime = TimeTypeUtil.getDifferenceTime(Long.parseLong(object)-duration);
					 SharedPreferencesUtils.getIntance(context).setLineTime(differenceTime);
					 MyLog.i("SynchronizeTime", "同步服务器时间成功！");
				}
			}
		});
	}

	/**
	 * 请求服务器同步车场价格策略；
	 * 因操作数据库需要在子线程中运行！！
	 * @param context
	 */
	public static void SynchronizePrice(final Context context,String comid){
		String url = Config.getUrl(context)+"parkoffline.do?action=synchroPrice&comid="+comid;
		MyLog.w("SynchronizePrice", "请求同步价格的url："+url);
		AQuery aq = new AQuery(context);
		aq.ajax(url, String.class, new AjaxCallback<String>() {
			@Override
			public void callback(String url, String object, AjaxStatus status) {
				super.callback(url, object, status);
				if (!TextUtils.isEmpty(object)) {
					MyLog.i("SynchronizePrice", "接收的价格表："+object);
					Gson gson = new Gson();
					List<Price_tb> list  = gson.fromJson(object, new TypeToken<List<Price_tb>>() {}.getType());
					MyLog.i("SynchronizePrice", "解析的价格表："+list.toString());
					DaoForPrice dao = new DaoForPrice(context);
					dao.addMorePrice(list);
					MyLog.i("SynchronizePrice", "同步价格表完毕：---------------!!!");
				}
			}
		});
	}
}
