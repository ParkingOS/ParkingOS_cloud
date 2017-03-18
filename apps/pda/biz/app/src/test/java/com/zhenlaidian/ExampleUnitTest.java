package com.zhenlaidian;

import android.util.Base64;

import org.junit.Test;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
//        int l = StringLength("收费规定小车10分钟内免费服务监督电话61888168");
//        System.out.println(l+"            ");

//        String[] params= new String[4];
//        params[0] = "lint";
//        params[1] = "intl";
//        params[2] = "inlt";
//        params[3] = "code";
//        sort(params);

        System.out.println(removeKuoHao("荷花池停车场(广陵区)"));
    }

    public static String removeKuoHao(String source){
        if(source.contains("(")&&source.contains(")")){
            int left = source.lastIndexOf("(");
            return source.substring(0,left);
        }
        return source;
    }

    //对于字符串数组 ["lint","intl","inlt","code"]
    //返回 ["lint","inlt","intl"]
    public void sort(String[] params){
        String[] newparam =  params;
        for(int s=0;s<newparam.length;s++){
            String param = newparam[s];
            System.out.println("原来的 "+param);
            String[] p = param.split("");
            HashMap<Integer,String> hashMap = new HashMap<>();
            for(String a:p){
                hashMap.put(a.hashCode(),a);
//                System.out.println(a);
            }
            List<Integer> listkeys = new ArrayList<>(hashMap.keySet());
            Collections.sort(listkeys);
            String sorted = "";
            for(int key:listkeys){
                sorted+=hashMap.get(key);
            }
            System.out.println("排序后的 "+sorted);
            System.out.println("");
            newparam[s] = sorted;
        }
        for(int b=0;b<params.length;b++){
            System.out.println("在用的 "+newparam[b]);
            System.out.println("原本的 "+params[b]);
        }
    }

    public  int StringLength(String str) {
        String[] number = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "(", ")", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        String[] spe = {"*", "-", ":",",","."};
        boolean isinnumber = false;
        boolean isinspe = false;
        double length = 0;
        String[] strarr = str.split("");
        for (int i = 0; i < strarr.length; i++) {
            String tmp = strarr[i];
            for (String j : number) {
                if (j.equals(tmp)) {
                    length += 0.5;
                    isinnumber = true;
                }
            }
            for (String p : spe) {
                if (p.equals(tmp)) {
                    length += 0.25;
                    isinspe = true;
                }
            }

            if (!isinnumber && !isinspe) {
                length += 1;

            }
            isinnumber = false;
            isinspe = false;
        }
//        Log.i("0000000000",str+"="+length+"=>"+Integer.parseInt(new java.text.DecimalFormat("0").format(length)));
        return Integer.parseInt(new java.text.DecimalFormat("0").format(length));
    }

    public  String encode(String key, String data) {
        return encode(key, data.getBytes());
    }
    public  String encode(String key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec("a5V25GbI".getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, getRawKey(key), iv);
            byte[] bytes = cipher.doFinal(data);
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    // 对密钥进行处理
    private static Key getRawKey(String key) throws Exception {
        DESKeySpec dks = new DESKeySpec(key.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        return keyFactory.generateSecret(dks);
    }
}