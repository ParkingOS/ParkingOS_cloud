package com.zld.tcp.client;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.apache.commons.codec.binary.Base64;

public class DES {
	
	 /**
     * 加密
     * @param data 需要加密的数据
     * @param key  加解密密钥
     * @return
     */
    public  String encrypt(String data, String key) {
    	try{
    		byte[] bt = encrypt(data.getBytes("utf-8"), key);
            String strs = new Base64().encodeBase64String(bt);
            return strs;
    	}catch(Exception ex){}
        return "";
    }
 
    /**
     * 解密
     * @param data  需要解密的数据
     * @param key   加解密密钥
     * @return
     */
    public  String decrypt(String data, String key) {
    	try{
    		byte[] buf = new Base64().decodeBase64(data);
            byte[] bt = decrypt(buf,key);
            return new String(bt,"UTF-8");
    	}catch(Exception ex){}
    	return "";
        
    }
    
	private static byte[] encrypt(byte[] datasource, String password){
		try {
			SecureRandom random = new SecureRandom();
			DESKeySpec desKey = new DESKeySpec(password.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(desKey);
			Cipher cipher = Cipher.getInstance("DES");
			cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
			return cipher.doFinal(datasource);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] decrypt(byte[] src, String password) throws Exception{
		SecureRandom random = new SecureRandom();
		DESKeySpec desKey = new DESKeySpec(password.getBytes());
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		SecretKey securekey = keyFactory.generateSecret(desKey);
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.DECRYPT_MODE, securekey, random);
		return cipher.doFinal(src);
	}
}