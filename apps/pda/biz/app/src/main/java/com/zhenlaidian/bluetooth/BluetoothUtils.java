/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhenlaidian.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.zhenlaidian.util.MyLog;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * This class includes a small subset of standard GATT attributes for
 * demonstration purposes.这一类包括用于示范标准关贸总协定属性的一小部分.
 */
@SuppressLint({ "InlinedApi", "DefaultLocale" })
public class BluetoothUtils {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static HashMap<Integer, String> serviceTypes = new HashMap();
	static {
		// Sample Services.
		serviceTypes.put(BluetoothGattService.SERVICE_TYPE_PRIMARY, "PRIMARY");
		serviceTypes.put(BluetoothGattService.SERVICE_TYPE_SECONDARY, "SECONDARY");
	}

	public static String getServiceType(int type) {
		return serviceTypes.get(type);
	}

	// -------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static HashMap<Integer, String> charPermissions = new HashMap();
	static {
		charPermissions.put(0, "UNKNOW");
		charPermissions.put(BluetoothGattCharacteristic.PERMISSION_READ, "READ");
		charPermissions.put(BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED, "READ_ENCRYPTED");
		charPermissions.put(BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM, "READ_ENCRYPTED_MITM");
		charPermissions.put(BluetoothGattCharacteristic.PERMISSION_WRITE, "WRITE");
		charPermissions.put(BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED, "WRITE_ENCRYPTED");
		charPermissions.put(BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM, "WRITE_ENCRYPTED_MITM");
		charPermissions.put(BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED, "WRITE_SIGNED");
		charPermissions.put(BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM, "WRITE_SIGNED_MITM");
	}

	public static String getCharPermission(int permission) {
		return getHashMapValue(charPermissions, permission);
	}

	// -------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static HashMap<Integer, String> charProperties = new HashMap();
	static {

		charProperties.put(BluetoothGattCharacteristic.PROPERTY_BROADCAST, "BROADCAST");
		charProperties.put(BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS, "EXTENDED_PROPS");
		charProperties.put(BluetoothGattCharacteristic.PROPERTY_INDICATE, "INDICATE");
		charProperties.put(BluetoothGattCharacteristic.PROPERTY_NOTIFY, "NOTIFY");
		charProperties.put(BluetoothGattCharacteristic.PROPERTY_READ, "READ");
		charProperties.put(BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE, "SIGNED_WRITE");
		charProperties.put(BluetoothGattCharacteristic.PROPERTY_WRITE, "WRITE");
		charProperties.put(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, "WRITE_NO_RESPONSE");
	}

	public static String getCharPropertie(int property) {
		return getHashMapValue(charProperties, property);
	}

	// --------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static HashMap<Integer, String> descPermissions = new HashMap();
	static {
		descPermissions.put(0, "UNKNOW");
		descPermissions.put(BluetoothGattDescriptor.PERMISSION_READ, "READ");
		descPermissions.put(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED, "READ_ENCRYPTED");
		descPermissions.put(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM, "READ_ENCRYPTED_MITM");
		descPermissions.put(BluetoothGattDescriptor.PERMISSION_WRITE, "WRITE");
		descPermissions.put(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED, "WRITE_ENCRYPTED");
		descPermissions.put(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM, "WRITE_ENCRYPTED_MITM");
		descPermissions.put(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED, "WRITE_SIGNED");
		descPermissions.put(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM, "WRITE_SIGNED_MITM");
	}

	public static String getDescPermission(int property) {
		return getHashMapValue(descPermissions, property);
	}

	private static String getHashMapValue(HashMap<Integer, String> hashMap, int number) {
		String result = hashMap.get(number);
		if (TextUtils.isEmpty(result)) {
			List<Integer> numbers = getElement(number);
			result = "";
			for (int i = 0; i < numbers.size(); i++) {
				result += hashMap.get(numbers.get(i)) + "|";
			}
		}
		return result;
	}

	/**
	 * 位运算结果的反推函数10 -> 2 | 8;
	 */
	static private List<Integer> getElement(int number) {
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < 32; i++) {
			int b = 1 << i;
			if ((number & b) > 0)
				result.add(b);
		}

		return result;
	}

	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static String bytesToString(byte[] src) {
		// byte[] 转 string
		String res = new String(src);
		return res;
	}

	// HexString锟斤拷锟斤拷>byte
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	// ***fly***---检测系统是否支持低功耗蓝牙；
		@SuppressWarnings("deprecation")
		@SuppressLint({ "InlinedApi", "NewApi" })
		public static boolean isSupportIbeacon(Context context) {
			if (Integer.valueOf(android.os.Build.VERSION.SDK) >= 18 && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
				MyLog.i("BluetoothUtils", ": --->> SDK版本" + android.os.Build.VERSION.SDK+ "支持BEL");
				return true;
			} else {
				MyLog.e("BluetoothUtils", ": --->> SDK版本" + android.os.Build.VERSION.SDK+ "不支持BEL");
				return false;
			}
		}
}
