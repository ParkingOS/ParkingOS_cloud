package parkingos.com.bolink.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * Test：Description
 * 
 * @author fangjie@iminer.com
 * @version V1.0
 * @date 2017年2月7日 上午9:49:27
 */
public class Encryption {

	// 算法/模式/填充
	private static String PKCS5PADDINGALGORITHM = "AES/CBC/PKCS5Padding";
	public static final String KEY="zldboink20170613";
	/**
	 * main:(这里用一句话描述这个方法的作用)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			// String password = System.currentTimeMillis()+"000";
			String key = "1234567812345678";
			String content = "123";
			System.out.println("加密前：" + content);
			String encryptString = encryptToAESPKCS5(content, key);
			System.out.println("加密后：" + encryptString);

			System.out.println("解密前：" + encryptString);
			String decrypyResult = decryptToAESPKCS5(encryptString, key);
			System.out.println("解密后：" + decrypyResult);
		}
		catch(Exception e) {
			// TODO: handle exception
		}

	}
	
	/**
	 * 
	 * encryptToAESPKCS5FromObject:将对象进行AES加密
	 * @param encryptData : 加密对象
	 * @param key : 加密密钥
	 * @return
	 */
	public static String encryptToAESPKCS5FromObject(Object encryptData,String key){
		String encryptResultStr = null;
		try {
			ObjectMapper om = new ObjectMapper();
			String encryptDataStr = om.writeValueAsString(encryptData);
			encryptResultStr = encryptToAESPKCS5(encryptDataStr, key);
		}
		catch(Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return encryptResultStr;
	}

	/**
	 * public static final int ENCRYPT_MODE 用于将 Cipher 初始化为加密模式的常量。 
	 * public static final int DECRYPT_MODE 用于将 Cipher 初始化为解密模式的常量。 
	 * public static final int WRAP_MODE 用于将 Cipher 初始化为密钥包装模式的常量。
	 * public static final int UNWRAP_MODE 用于将 Cipher 初始化为密钥解包模式的常量。 
	 * public static final int PUBLIC_KEY 用于表示要解包的密钥为“公钥”的常量。 
	 * public static final int PRIVATE_KEY 用于表示要解包的密钥为“私钥”的常量。 
	 * public static final int SECRET_KEY 用于表示要解包的密钥为“秘密密钥”的常量。
	 **/

	/**
	 * encryptToAESPKCS5:加密AES
	 * 
	 * @param content ： 加密内容
	 * @param key ： 加密密钥
	 * @return
	 */
	public static String encryptToAESPKCS5(String content, String key) {
		byte[] encryptResult = null;
		try {
			// 密钥
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
			// 算法/模式/填充
			Cipher cipher = Cipher.getInstance(PKCS5PADDINGALGORITHM);
			byte[] byteContent = content.getBytes("utf-8");
			// 初始化向量,在密钥相同的前提下，加上初始化向量，相同内容加密后相同
			IvParameterSpec zeroIv = new IvParameterSpec(key.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, zeroIv);
			encryptResult = cipher.doFinal(byteContent);

		}
		catch(Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return Base64.encode(encryptResult);
	}

	/**
	 * decryptToAESPKCS5:解密AES
	 * 
	 * @param content ： 解密内容
	 * @param key ： 解密密钥
	 * @return
	 */
	public static String decryptToAESPKCS5(String content, String key) {
		byte[] decryptResult = null;
		try {
			// 密钥
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance(PKCS5PADDINGALGORITHM);
			// base64转换
			byte[] byteContent = Base64.decode(content);
			IvParameterSpec zeroIv = new IvParameterSpec(key.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, secretKey, zeroIv);
			decryptResult = cipher.doFinal(byteContent);

			String originalString = new String(decryptResult, "utf-8");
			return originalString;
		}
		catch(Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将二进制转换成16进制
	 * 
	 * @param buf
	 * @return
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if(hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 将16进制转换为二进制
	 * 
	 * @param hexStr
	 * @return
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if(hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for(int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte)(high * 16 + low);
		}
		return result;
	}
}
