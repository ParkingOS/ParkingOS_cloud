package com.tq.zld.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {

	/**
	 * 默认的密码字符串组合，apache校验下载的文件的正确性用的就是默认的这个组合
	 */
	private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private static MessageDigest messagedigest = null;
	static {
		try {
			messagedigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException nsaex) {
			nsaex.printStackTrace();
		}
	}

	/**
	 * 适用于上G大的文件
	 * 
	 * @param file
	 * @return
	 */
	public static String getFileMD5(File file) {

		if (file == null || !file.isFile() || messagedigest == null) {
			return "";
		}

		try {
			FileInputStream in = new FileInputStream(file);
			FileChannel ch = in.getChannel();
			MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY,
					0, file.length());
			messagedigest.update(byteBuffer);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bufferToHex(messagedigest.digest());
	}

	private static String bufferToHex(byte bytes[]) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte bytes[], int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & 0xf0) >> 4];
		char c1 = hexDigits[bt & 0xf];
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}

	public final static String getStringMD5(String str) {
		if (str == null || str.length() == 0 || messagedigest == null) {
			return null;
		}
		messagedigest.update(str.getBytes());
		byte[] md = messagedigest.digest();
		int j = md.length;
		char temp[] = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) {
			byte byte0 = md[i];
			temp[k++] = hexDigits[byte0 >>> 4 & 0xf];
			temp[k++] = hexDigits[byte0 & 0xf];
		}
		return new String(temp);
	}

	public static String getStringSHA1(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		try {
			MessageDigest sha1D = MessageDigest.getInstance("SHA1");
			sha1D.update(str.getBytes());

			byte[] md = sha1D.digest();
			int j = md.length;
			char buf[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
				buf[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(buf);
		} catch (NoSuchAlgorithmException nsaex) {
			nsaex.printStackTrace();
		}
		return null;
	}
}
