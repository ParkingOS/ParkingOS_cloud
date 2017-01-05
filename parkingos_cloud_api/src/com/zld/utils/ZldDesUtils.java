package com.zld.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;



public class ZldDesUtils {
	//test
//	private static final String PASSWORD_CRYPT_KEY = "g9A6eELT";
//	private static final String IV = "GTvn6aEw";
	//line
	private static final String PASSWORD_CRYPT_KEY = "NQ0eSXs7";
	private static final String IV = "309VvJzn";
	private static final String DES = "DES/CBC/PKCS5Padding";
	


	/**
	 * 
	 * 加密 *
	 * 
	 * @param src数据源
	 * @param key密钥
	 *            ，长度必须是8的倍数
	 * @return 返回加密后的数据
	 * @throws Exception
	 */

	public static String encrypt(String message) throws Exception {
		byte [] betys =null;
		// 从原始密匙数据创建DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(PASSWORD_CRYPT_KEY.getBytes());
		// 一个SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		// 创建一个密匙工厂，然后用它把DESKeySpec转换成
		SecretKey securekey = keyFactory.generateSecret(dks);
		IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
		// Cipher对象实际完成加密操作
		Cipher cipher = Cipher.getInstance(DES);
		// 用密匙初始化Cipher对象
		cipher.init(Cipher.ENCRYPT_MODE, securekey, iv);
		// 现在，获取数据并加密
		// 正式执行加密操作
		betys = cipher.doFinal(message.getBytes("utf-8"));
		return new String(BASE64.encode(betys));

	}
	/**
	 * 解密
	 * @param src数据源
	 * @param key密钥 ，长度必须是8的倍数
	 * @return 返回解密后的原始数据
	 * @throws Exception
	 */
	public static String decrypt(String value) throws Exception {
		// DES算法要求有一个可信任的随机数源

		// 从原始密匙数据创建一个DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(PASSWORD_CRYPT_KEY.getBytes());
		// 创建一个密匙工厂，然后用它把DESKeySpec对象转换成
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		// 一个SecretKey对象
		SecretKey securekey = keyFactory.generateSecret(dks);
		IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
		// Cipher对象实际完成解密操作
		// 用密匙初始化Cipher对象
		Cipher cipher = Cipher.getInstance(DES);
		cipher.init(Cipher.DECRYPT_MODE, securekey, iv);
		// 现在，获取数据并解密
		// 正式执行解密操作
		return new String(cipher.doFinal(BASE64.decode(value)),"utf-8");
	}



	public static void main(String[] args) {
		String value = "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";
		try {
			//System.out.println("md5加密后的字符串是       "					+ new ZldDesUtils().encrypt("{9214392:12330[a:1,b:2,c:3]}"));
			System.out
					.println("解密后的字符串是       "
							+ ZldDesUtils.decrypt(value));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
