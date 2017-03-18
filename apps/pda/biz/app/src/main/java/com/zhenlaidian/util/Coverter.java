package com.zhenlaidian.util;

import android.content.Intent;
import android.nfc.NfcAdapter;

/**
 * nfc标签uid十六进制转换
 * @author lsh
 *
 */
public class Coverter {
	// Hex help
	private final static byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1',
			(byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6',
			(byte) '7', (byte) '8', (byte) '9', (byte) 'A', (byte) 'B',
			(byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F' };

	/**
	 * convert a byte arrary to hex string
	 * 
	 * @param raw
	 *            byte arrary
	 * @param len
	 *            lenght of the arrary.
	 * @return hex string.
	 */
	public static String getHexString(byte[] raw, int len) {
		byte[] hex = new byte[2 * len];
		int index = 0;
		int pos = 0;

		for (byte b : raw) {
			if (pos >= len)
				break;

			pos++;
			int v = b & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}

		return new String(hex);
	}
	public static String getUid(Intent intent){
		byte[] myNFCID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
		return getHexString(myNFCID, myNFCID.length);
	}
}
