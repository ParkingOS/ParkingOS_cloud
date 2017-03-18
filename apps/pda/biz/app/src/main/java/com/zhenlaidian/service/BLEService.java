/**
 * 
 */
package com.zhenlaidian.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.google.gson.Gson;
import com.zhenlaidian.R;
import com.zhenlaidian.bean.BLEOrder;
import com.zhenlaidian.bean.Config;
import com.zhenlaidian.bean.LeaveOrder;
import com.zhenlaidian.bean.MainUiInfo;
import com.zhenlaidian.bean.NfcOrder;
import com.zhenlaidian.bean.NfcPrepaymentOrder;
import com.zhenlaidian.bluetooth.BluetoothLeClass;
import com.zhenlaidian.bluetooth.BluetoothLeClass.OnDataAvailableListener;
import com.zhenlaidian.bluetooth.BluetoothLeClass.OnServiceDiscoverListener;
import com.zhenlaidian.bluetooth.BluetoothUtils;
import com.zhenlaidian.bluetooth.iBeaconClass;
import com.zhenlaidian.bluetooth.iBeaconClass.iBeacon;
import com.zhenlaidian.ui.BaseActivity;
import com.zhenlaidian.ui.BaseActivity.MsgToMainListener;
import com.zhenlaidian.util.MyLog;
import com.zhenlaidian.util.PlayerVoiceUtil;
import com.zhenlaidian.util.SharedPreferencesUtils;
import com.zhenlaidian.util.VoiceSynthesizerUtil;

/**
 * 极速通连接服务类；
 * 
 * @author zhangyunfei 2015年8月28日
 */
@SuppressLint("NewApi")
public class BLEService extends Service {
	public static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";
	public static String UUID_CHAR6 = "0000fff6-0000-1000-8000-00805f9b34fb";
	public static String UUID_HERATRATE = "00002a37-0000-1000-8000-00805f9b34fb";
	public static String UUID_TEMPERATURE = "00002a1c-0000-1000-8000-00805f9b34fb";

	static BluetoothGattCharacteristic gattCharacteristic_char1 = null;
	static BluetoothGattCharacteristic gattCharacteristic_char6 = null;
	static BluetoothGattCharacteristic gattCharacteristic_heartrate = null;
	static BluetoothGattCharacteristic gattCharacteristic_keydata = null;
	static BluetoothGattCharacteristic gattCharacteristic_temperature = null;

	public String BLEmsg = "";// 接收到BLE设备发送的信息；
	private static BluetoothAdapter mBluetoothAdapter;
	private static BluetoothLeClass mBLE;
	public String bluetoothAddress;
	private static byte writeValue_char1 = 0;
	private static boolean isconnect = false;// 蓝牙设备是否已连接；
	private static int heartbeat = 0;
	private final static String TAG = "BLEService";
	private Handler mHandler;
	private static final long SCAN_PERIOD = 10000;
	private final Timer timer = new Timer();
	private TimerTask task;
	private static MsgToMainListener mMsgToMainListener;
	private Runnable runnable = null;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void setOnConnectListener(MsgToMainListener l) {
		mMsgToMainListener = l;
	}

	@Override
	public void onCreate() {
		MyLog.i(TAG, "onCreate" + "初始化蓝牙服务！");
		mHandler = new Handler();
		initBLE();// 初始化蓝牙管理者，BLE连接管理类；
		scanLeDevice(true);// 开始搜索BLE设备；
		task = new TimerTask() {
			@Override
			public void run() {
				checkIbeacon();// 检查蓝牙是否连接正常；
			}
		};
		timer.schedule(task, 15000, 25000);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		timer.cancel();// 关闭定时任务；
		try {
			if (mBLE != null) {
				mBLE.setOnServiceDiscoverListener(null);
				mBLE.setOnDataAvailableListener(null);
				mBLE.disconnect();
				mBLE.close();
			}
			if (mBluetoothAdapter != null) {
				scanLeDevice(false);
				mBluetoothAdapter.cancelDiscovery();
				mBluetoothAdapter = null;
			}
		} catch (Exception e) {
		}
		MyLog.w(TAG, "onDestroy");
	}

	// 初始化蓝牙管理者，BLE连接管理类；
	public void initBLE() {
		mBluetoothAdapter = ((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
		if (!mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
		}
		mBLE = new BluetoothLeClass(BLEService.this);
		if (!mBLE.initialize()) {
			MyLog.w(TAG, "Unable to initialize Bluetooth");
		}
		// 发现BLE终端的Service时回调?
		mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
		// 收到BLE终端数据交互的事回调?
		mBLE.setOnDataAvailableListener(mOnDataAvailable);
		mBLE.close();
	}

	// 搜索周围蓝牙设备。周期为10秒；
	private void scanLeDevice(final boolean enable) {
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			initBLE();
			return;
		}
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			runnable = new Runnable() {
				@Override
				public void run() {
					MyLog.w(TAG, "停止扫描BLE设备" + enable);
					if (mBluetoothAdapter != null) {
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
					}
					mHandler.removeCallbacks(runnable);
				}
			};
			mHandler.postDelayed(runnable, SCAN_PERIOD);
			MyLog.w(TAG, "开始扫描BLE设备" + enable);
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			MyLog.w(TAG, "停止扫描BLE设备" + enable);
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	public void checkIbeacon() {
		MyLog.i(TAG, "--->>>检查蓝牙连接情况");
		if (isconnect) {// 已经连接到ibeacon;
			if (heartbeat > 0) {// 心跳还在
				MyLog.i(TAG, "--->>>已经连接到BLE保持心跳=" + heartbeat);
				heartbeat = 0;
				Message msg1 = new Message();
				msg1.what = 11;// 蓝牙抬杆练连接成功信息
				msg1.obj = true;
				sendMsgToMainListener(msg1);
			} else {
				MyLog.i(TAG, "--->>>连接BLE失去心跳=" + heartbeat + "重新开启扫描蓝牙扫描15秒");
				isconnect = false;
				scanLeDevice(true);
				Message msg1 = new Message();
				msg1.what = 11;// 蓝牙抬杆信息
				msg1.obj = false;
				sendMsgToMainListener(msg1);
			}
		} else {
			if (mBluetoothAdapter != null) {
				scanLeDevice(true);
				MyLog.i(TAG, "--->>>未连接BLE设备，重新开始扫描--15秒  mBluetoothAdapter=" + mBluetoothAdapter);
			} else {
				initBLE();
				MyLog.w(TAG, "--->>>未连接BLE设备mBluetoothAdapter=NULL 重新初始化蓝牙管理者");
			}
		}
	}

	/**
	 * 搜索到蓝牙设备回调；
	 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

			MyLog.i(TAG, "rssi = " + rssi);
			MyLog.i(TAG, "mac = " + device.getAddress());
			MyLog.i(TAG, "scanRecord.length = " + scanRecord.length);
			iBeacon ibeacon = iBeaconClass.fromScanData(device, rssi, scanRecord);
			if (ibeacon == null)
				return;
			MyLog.w(TAG, "ibeacon_name = " + ibeacon.name);

			if (SharedPreferencesUtils.getIntance(BLEService.this).getBLEName().equals(ibeacon.name)) {
				mBLE.close();
				bluetoothAddress = ibeacon.bluetoothAddress;
				boolean connect1 = mBLE.connect(ibeacon.bluetoothAddress);
				MyLog.w(TAG, "connect bRet = " + connect1);
			} else {
				MyLog.w(TAG, "非停车宝专用ibeacon" + ibeacon.name);
			}
		}
	};

	public void writeChar1() {
		byte[] writeValue = new byte[1];
		MyLog.i(TAG, "gattCharacteristic_char1 = " + gattCharacteristic_char1);
		if (gattCharacteristic_char1 != null) {
			writeValue[0] = writeValue_char1++;
			MyLog.i(TAG, "gattCharacteristic_char1.setValue writeValue[0] =" + writeValue[0]);
			boolean bRet = gattCharacteristic_char1.setValue(writeValue);
			mBLE.writeCharacteristic(gattCharacteristic_char1);
		}
	}

	public static void writeChar6(String string) {
		// byte[] writeValue = new byte[1];
		MyLog.i(TAG, "gattCharacteristic_char6 = " + gattCharacteristic_char6);
		if (gattCharacteristic_char6 != null) {
			// writeValue[0] = writeValue_char1++;
			// Log.i(TAG, "gattCharacteristic_char6.setValue writeValue[0] =" +
			// writeValue[0]);
			boolean bRet = gattCharacteristic_char6.setValue(string);
			mBLE.writeCharacteristic(gattCharacteristic_char6);
		}
	}

	public void read_char1() {
		// byte[] writeValue = new byte[1];
		MyLog.i(TAG, "readCharacteristic = ");
		if (gattCharacteristic_char1 != null) {
			mBLE.readCharacteristic(gattCharacteristic_char1);
		}
	}

	/**
	 * 搜索到BLE终端服务的事件?
	 */
	private BluetoothLeClass.OnServiceDiscoverListener mOnServiceDiscover = new OnServiceDiscoverListener() {

		@Override
		public void onServiceDiscover(BluetoothGatt gatt, Boolean connect) {
			// TODO Auto-generated method stub
			if (connect) {
				MyLog.w(TAG, "搜索到BLE终端服务的事件");
				displayGattServices(mBLE.getSupportedGattServices());
			} else {
				if (BaseActivity.token != null) {
					mBLE.close();
					heartbeat = 0;
					isconnect = false;
					Message msg1 = new Message();
					msg1.what = 11;// 蓝牙抬杆信息
					msg1.obj = false;
					sendMsgToMainListener(msg1);
				}
			}
		}
	};

	/**
	 * 收到BLE终端数据交互的事件?
	 */
	private BluetoothLeClass.OnDataAvailableListener mOnDataAvailable = new OnDataAvailableListener() {
		/**
		 * BLE终端数据被读的事件?
		 */
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			// 执行 mBLE.readCharacteristic(gattCharacteristic); 后就会收到数�? if
			// (status == BluetoothGatt.GATT_SUCCESS)
			MyLog.w(TAG, "onCharRead " + gatt.getDevice().getName() + " read " + characteristic.getUuid().toString() + " -> "
					+ BluetoothUtils.bytesToHexString(characteristic.getValue()));
		}

		/**
		 * 收到BLE终端写入数据回调
		 */
		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			MyLog.w(TAG, "onCharWrite " + new String(characteristic.getValue()));
			heartbeat++;
			if (BaseActivity.token == null) {
				try {
					if (mBLE != null) {
						mBLE.setOnServiceDiscoverListener(null);
						mBLE.setOnDataAvailableListener(null);
						mBLE.disconnect();
						mBLE.close();
					}
					mBluetoothAdapter.cancelDiscovery();
					mBluetoothAdapter = null;
					stopSelf();
				} catch (Exception e) {
					// TODO: handle exception
				}
				MyLog.i(TAG, "onCharWrite方法检测到leaveActivity为null" + "关闭蓝牙服务！");
			}
			try {
				String msg = new String(characteristic.getValue(), "GBK");
				if (!TextUtils.isEmpty(msg) && !msg.equals("#") && !msg.equals("[HB]")) {
					if (msg.indexOf("#") != -1) {
						msg = msg.replace("#", "");
						MyLog.w(TAG, "替换后的字符串是" + msg);
					}
					int i = msg.indexOf("{");
					int j = msg.indexOf("}");
					if (i != -1) {
						BLEmsg = BLEmsg + msg;
					} else if (j != -1) {
						BLEmsg = BLEmsg + msg;
						try {
							checkHeartMessage(BLEmsg);
						} catch (Exception e) {
							BLEmsg = "";
						} finally {
							BLEmsg = "";
						}
					} else {
						if (!"".equals(BLEmsg)) {
							BLEmsg = BLEmsg + msg;
						}
					}
				} else {
					// writeChar6("#");
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	public void checkHeartMessage(String message) {
		if (TextUtils.isEmpty(message) || "#".equals(message)) {
			return;
		}
		MyLog.i(TAG, "接收到的照牌进出场信息是：" + message);
		Gson gson = new Gson();
		BLEOrder order = gson.fromJson(message, BLEOrder.class);
		if (order != null && order.getInout() != null) {
			MyLog.i(TAG, "解析到的照牌进出场信息是：" + order.toString());
			if ("in".equals(order.getInout())) {
				addOrder(order);
			} else if ("out".equals(order.getInout())) {
				outBLEOrder(order);
			} else {
				MyLog.w(TAG, "照牌进出场信息类型错误：" + order.getInout());
			}
		}
	}

	// 按车牌生成BLE照牌订单； 不查逃单 isfast=1照牌极速通。isfast=2取卡极速通
	// http://192.168.199.240/zld/cobp.do?action=addorder&comid=3&uid=100005&carnumber=aaabbdd&through=3
	public void addOrder(BLEOrder order) {
		try {
			String path = Config.getUrl(this);
			String url = null;
			if (TextUtils.isEmpty(order.getLp())) {
				url = path + "cobp.do?action=addorder&comid=" + BaseActivity.comid + "&uid=" + BaseActivity.useraccount
						+ "&cardno=" + order.getCn() + "&imei=" + BaseActivity.imei + "&through=3" + "&isfast=2";
			} else {
				String carnumber = URLEncoder.encode(URLEncoder.encode(order.getLp(), "utf-8"), "utf-8");
				url = path + "cobp.do?action=addorder&comid=" + BaseActivity.comid + "&uid=" + BaseActivity.useraccount
						+ "&carnumber=" + carnumber + "&imei=" + BaseActivity.imei + "&through=3" + "&isfast=1";
			}
			MyLog.w(TAG, "通道照牌生成订单的URL--->" + url);
			AQuery aQuery = new AQuery(this);
			aQuery.ajax(url, String.class, new AjaxCallback<String>() {

				@Override
				public void callback(String url, String object, AjaxStatus status) {
					if (status.getCode() == 200 && object != null) {
						MyLog.i("CheckNumberActivity", "车牌识别生成订单的结果--->" + object);
						if (object.equals("1")) {
							Log.e(TAG, "通道照牌生成订单成功！！！");
							Message msg = new Message();
							msg.what = 2;
							msg.obj = new MainUiInfo(true, 4, 1.00);
							sendMsgToMainListener(msg);
						} else {
							Log.e(TAG, "通道照牌生成订单失败！！！");
						}
					} else {
						Log.e(TAG, "通道照牌生成订单--网络错误！！！");
					}
				}
			});
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 结算BLE照牌订单：collectorrequest.do?action=autoup&price=&carnumber=&token=0dc591f7ddda2d6fb73cd8c2b4e4a372
	// （）充足：返回：{state:1,orderid,btime,etime,carnumber,duration,total}
	// （）不足：返回 {state:2,prefee,total,collect}
	// 其它返回：{state:-?,errmsg:XXXX}
	// {\"state\":\"-1\",\"errmsg\":\"车牌没有注册!\"
	// {\"state\":\"-2\",\"errmsg\":\"价格不对
	// {\"state\":\"-3\",\"errmsg\":\"没有停车场或收费员信息，请重新登录!\"
	// {\"state\":\"-4\",\"errmsg\":\"生成订单失败!\"
	// {\"state\":\"-5\",\"errmsg\":\"已支付，不能重复支付!\",
	// {\"state\":\"-6\",\"errmsg\":\"支付失败!\"
	// {\"state\":\"-7\",\"errmsg\":\"余额不足!\"
	// {\"state\":\"-8\",\"errmsg\":\"未开启自动支付!\"
	// {\"state\":\"-9\",\"errmsg\":\"停车费超出自动支付限额!\"
	// {\"state\":\"-10\",\"errmsg\":\"极速通卡未注册!\"
	// {\"state\":\"-11\",\"errmsg\":\"取卡极速通没有预支付!\"
	public void outBLEOrder(BLEOrder order) {
		try {
			String path = Config.getUrl(this);
			String url = null;
			if (TextUtils.isEmpty(order.getLp())) {
				url = path + "collectorrequest.do?action=autoup&price=" + order.getFee() + "&cardno=" + order.getCn() + "&token="
						+ BaseActivity.token;
			} else {
				String carnumber = URLEncoder.encode(URLEncoder.encode(order.getLp(), "utf-8"), "utf-8");
				url = path + "collectorrequest.do?action=autoup&price=" + order.getFee() + "&carnumber=" + carnumber + "&token="
						+ BaseActivity.token;
			}
			MyLog.w(TAG, "BLE照牌提交并结算订单的URL-->>" + url);
			AQuery aq = new AQuery(this);
			aq.ajax(url, String.class, new AjaxCallback<String>() {

				@Override
				public void callback(String url, String object, AjaxStatus status) {
					if (!TextUtils.isEmpty(object)) {
						MyLog.i(TAG, "BLE照牌提交并结算订单的结果是：" + object);
						Gson gson = new Gson();
						LeaveOrder orderinfo = gson.fromJson(object, LeaveOrder.class);
						if (orderinfo == null) {
							return;
						}
						if ("1".equals(orderinfo.getState())) {
							if (!TextUtils.isEmpty(orderinfo.getTotal()) && Double.parseDouble(orderinfo.getTotal()) == 0) {

							} else {
								writeChar6("[CKTG]");
								MyLog.i(TAG, "加载到离场订单...");
								Message msg1 = new Message();
								msg1.what = 1;// 加载到主界面离场订单；
								orderinfo.setState("2");
								msg1.obj = orderinfo;
								sendMsgToMainListener(msg1);
								new PlayerVoiceUtil(BLEService.this, R.raw.phone_pay).play();
							}
						} else if ("2".equals(orderinfo.getState())) {
							VoiceSynthesizerUtil vUtil = new VoiceSynthesizerUtil(BLEService.this);
							vUtil.playText("请向车主补收现金" + orderinfo.getCollect() + "元");
							MyLog.i(TAG, "主界面弹出补交现金对话框...");
							Message msg1 = new Message();
							msg1.what = 12;// 主界面弹出补交现金对话框
							msg1.obj = new NfcPrepaymentOrder("", orderinfo.getPrefee(), orderinfo.getTotal(), orderinfo
									.getCollect());
							sendMsgToMainListener(msg1);
						} else if ("-1".equals(orderinfo.getState())) {

						} else if ("-7".equals(orderinfo.getState())) {
							new PlayerVoiceUtil(BLEService.this, R.raw.balance_no_more).play();
							// Log.i(TAG, "加载到离场订单...");
							// Message msg1 = new Message();
							// msg1.what = 1;// 加载到主界面离场订单；
							// orderinfo.setState("-2");
							// msg1.obj = orderinfo;
							// LeaveActivity.handler.sendMessage(msg1);
						} else if ("-8".equals(orderinfo.getState())) {
							new PlayerVoiceUtil(BLEService.this, R.raw.not_set_auto_pay).play();
						} else if ("-9".equals(orderinfo.getState())) {
							new PlayerVoiceUtil(BLEService.this, R.raw.total_morethan_autopay).play();
						} else if ("-10".equals(orderinfo.getState())) {
							VoiceSynthesizerUtil vUtil = new VoiceSynthesizerUtil(BLEService.this);
							vUtil.playText("极速通卡未注册");
						} else if ("-11".equals(orderinfo.getState())) {
							VoiceSynthesizerUtil vUtil = new VoiceSynthesizerUtil(BLEService.this);
							vUtil.playText("车主没有预支付");
						} else {
							MyLog.i(TAG, "加载到离场订单...");
							Message msg1 = new Message();
							msg1.what = 13;// 主界面弹出结算错误对话框
							NfcOrder nfcorder = new NfcOrder();
							nfcorder.setOrderid(orderinfo.getOrderid());
							nfcorder.setNetError(orderinfo.getState() + ":" + orderinfo.getErrmsg());
							msg1.obj = nfcorder;
							sendMsgToMainListener(msg1);
						}
					} else {
						MyLog.w(TAG, "BLE照牌提交并结算--网络错误！！！");
						Toast.makeText(BLEService.this, "BLE照牌提交并结算--网络错误", 1).show();
					}
				}
			});
		} catch (UnsupportedEncodingException e) {
			Toast.makeText(BLEService.this, "车牌转码异常！！！", 1).show();
			e.printStackTrace();
		}
	}

	private void displayGattServices(List<BluetoothGattService> gattServices) {
		if (gattServices == null)
			return;
		@SuppressWarnings("unused")
		BluetoothGattCharacteristic Characteristic_cur = null;

		for (BluetoothGattService gattService : gattServices) {
			// -----Service的字段信息?----//
			int type = gattService.getType();
			MyLog.d(TAG, "-->service type:" + BluetoothUtils.getServiceType(type));
			MyLog.d(TAG, "-->includedServices size:" + gattService.getIncludedServices().size());
			MyLog.d(TAG, "-->service uuid:" + gattService.getUuid());

			// -----Characteristics的字段信息?----//
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				MyLog.d(TAG, "---->char uuid:" + gattCharacteristic.getUuid());

				int permission = gattCharacteristic.getPermissions();
				MyLog.d(TAG, "---->char permission:" + BluetoothUtils.getCharPermission(permission));

				int property = gattCharacteristic.getProperties();
				MyLog.d(TAG, "---->char property:" + BluetoothUtils.getCharPropertie(property));

				byte[] data = gattCharacteristic.getValue();
				if (data != null && data.length > 0) {
					MyLog.d(TAG, "---->char value:" + new String(data));
				}

				if (gattCharacteristic.getUuid().toString().equals(UUID_CHAR6)) {
					// 把char1 保存起来以方便后面读写数据时使用
					gattCharacteristic_char6 = gattCharacteristic;
					Characteristic_cur = gattCharacteristic;
					mBLE.setCharacteristicNotification(gattCharacteristic, true);
					MyLog.d(TAG, "+++++++++UUID_CHAR6");
				}

				if (gattCharacteristic.getUuid().toString().equals(UUID_HERATRATE)) {
					// 把heartrate 保存起来以方便后面读写数据时使用
					gattCharacteristic_heartrate = gattCharacteristic;
					Characteristic_cur = gattCharacteristic;
					// 接受Characteristic被写的收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
					mBLE.setCharacteristicNotification(gattCharacteristic, true);
					MyLog.d(TAG, "+++++++++UUID_HERATRATE");
				}

				if (gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)) {
					// 把heartrate 保存起来以方便后面读写数据时使用
					gattCharacteristic_keydata = gattCharacteristic;
					Characteristic_cur = gattCharacteristic;
					// 接受Characteristic被写的收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
					mBLE.setCharacteristicNotification(gattCharacteristic, true);
					MyLog.d(TAG, "+++++++++UUID_KEY_DATA");
				}

				if (gattCharacteristic.getUuid().toString().equals(UUID_TEMPERATURE)) {
					// 把heartrate 保存起来以方便后面读写数据时使用
					gattCharacteristic_temperature = gattCharacteristic;
					Characteristic_cur = gattCharacteristic;
					// 接受Characteristic被写的收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
					mBLE.setCharacteristicNotification(gattCharacteristic, true);
					MyLog.d(TAG, "+++++++++UUID_TEMPERATURE");
				}

				// -----Descriptors的字段信?----//
				List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
				for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
					MyLog.d(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
					int descPermission = gattDescriptor.getPermissions();
					MyLog.d(TAG, "-------->desc permission:" + BluetoothUtils.getDescPermission(descPermission));

					byte[] desData = gattDescriptor.getValue();
					if (desData != null && desData.length > 0) {
						MyLog.d(TAG, "-------->desc value:" + new String(desData));
					}
				}
			}
		}
		isconnect = true;
		if (isconnect) {
			heartbeat = 1;
		}
		Message msg1 = new Message();
		msg1.what = 11;// 蓝牙抬杆练连接成功信息
		msg1.obj = true;
		sendMsgToMainListener(msg1);
		scanLeDevice(false);
		mHandler.removeCallbacks(runnable);
	}

	// 回调内容给activity；
	public void sendMsgToMainListener(Message msg) {
		if (mMsgToMainListener != null) {
			mMsgToMainListener.onSendMsg(msg);
		}
	}
}
