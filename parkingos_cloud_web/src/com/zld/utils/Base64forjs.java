package com.zld.utils;

import java.io.ByteArrayOutputStream;

public class Base64forjs {

	private static String key = "zldpass20153344333442222";
	public static String[] keystrs= new String[] {
			"563031d4067bd699cce2a025", "5630314c4aa5720488ebd137",
			"5630318e067bd699cce2a01a", "563031594aa5720488ebd139",
			"563031d84aa5720488ebd14d", "563031a8067bd699cce2a01e",
			"56303190b0aa7963cf656d0e", "56302d66067bd699cce29f82",
			"563031394aa5720488ebd134", "563031d0b0aa7963cf656d18",
			"56302ca5b0aa7963cf656c57", "563031a54aa5720488ebd145",
			"5630321a067bd699cce2a030", "56302c5fb0aa7963cf656c4c",
			"56302bf5067bd699cce29f48", "56302cd3067bd699cce29f6b",
			"56302ea0067bd699cce29faf", "56302e30067bd699cce29fa2",
			"5630318b4aa5720488ebd141", "563031784aa5720488ebd13e" };


	private static byte[] base64DecodeChars = new byte[] { -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59,
			60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
			10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1,
			-1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
			38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1,
			-1, -1 };

	/**
	 * 解密
	 *
	 * @param str
	 * @return
	 */
	public static String decode(String str,Integer index) {
		str = str + keystrs[index].substring(15);
		byte[] data = str.getBytes();
		int len = data.length;
		ByteArrayOutputStream buf = new ByteArrayOutputStream(len);
		int i = 0;
		int b1, b2, b3, b4;

		while (i < len) {
			do {
				b1 = base64DecodeChars[data[i++]];
			} while (i < len && b1 == -1);
			if (b1 == -1) {
				break;
			}

			do {
				b2 = base64DecodeChars[data[i++]];
			} while (i < len && b2 == -1);
			if (b2 == -1) {
				break;
			}
			buf.write((int) ((b1 << 2) | ((b2 & 0x30) >>> 4)));

			do {
				b3 = data[i++];
				if (b3 == 61) {
					return new String(buf.toByteArray());
				}
				b3 = base64DecodeChars[b3];
			} while (i < len && b3 == -1);
			if (b3 == -1) {
				break;
			}
			buf.write((int) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));

			do {
				b4 = data[i++];
				if (b4 == 61) {
					return new String(buf.toByteArray());
				}
				b4 = base64DecodeChars[b4];
			} while (i < len && b4 == -1);
			if (b4 == -1) {
				break;
			}
			buf.write((int) (((b3 & 0x03) << 6) | b4));
		}
		return new String(buf.toByteArray());
	}
}
