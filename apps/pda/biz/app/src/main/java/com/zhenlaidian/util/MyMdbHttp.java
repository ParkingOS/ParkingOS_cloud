package com.zhenlaidian.util;


import static android.os.Environment.MEDIA_MOUNTED;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import android.content.Context;
import android.os.Environment;


import com.nostra13.universalimageloader.utils.StorageUtils;

/**
 * Created by TCB on 2016/4/20.
 * xulu
 */
public class MyMdbHttp {
    /**
     * 获取图片的名字含.jpg（非压缩图）
     *
     * @return
     */
    public String getImageName(String PreImageName) {
        String imageName = PreImageName + ".jpg";
        return imageName;
    }



    public static String imageUpload(Map<String, Object> params,
                                     String compressImagePath, String img) {
        String result = "";
        String end = "\r\n";
        String uploadUrl = "http://127.0.0.1/zld/collectorrequest.do?action=workout";
        String MULTIPART_FORM_DATA = "multipart/form-data";
        String BOUNDARY = "---------7d4a6d158c9"; // 数据分隔线
        String imguri = "";
        if (!compressImagePath.equals("")) {
            imguri = compressImagePath.substring(compressImagePath
                    .lastIndexOf("/") + 1);// 获得图片或文件名称
        }
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false);// 不使用Cache
            conn.setConnectTimeout(60000);// 6秒钟连接超时
            conn.setReadTimeout(60000);// 6秒钟读数据超时
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type", MULTIPART_FORM_DATA
                    + "; boundary=" + BOUNDARY);

            StringBuilder sb = new StringBuilder();

            // 上传的表单参数部分，格式请参考文章
            for (Map.Entry<String, Object> entry : params.entrySet()) {// 构建表单字段内容
                sb.append("--");
                sb.append(BOUNDARY);
                sb.append("\r\n");
                sb.append("Content-Disposition: form-data; name=\""
                        + entry.getKey() + "\"\r\n\r\n");
                sb.append(entry.getValue());
                sb.append("\r\n");
            }

            sb.append("--");
            sb.append(BOUNDARY);
            sb.append("\r\n");

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.write(sb.toString().getBytes());

            if (!compressImagePath.equals("")
                    && !compressImagePath.equals(null)) {
                dos.writeBytes("Content-Disposition: form-data; name=\"" + img
                        + "\"; filename=\"" + imguri + "\"" + "\r\n"
                        + "Content-Type: image/jpg\r\n\r\n");
                FileInputStream fis = new FileInputStream(compressImagePath);
                byte[] buffer = new byte[1024]; // 8k
                int count = 0;
                while ((count = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, count);
                }
                dos.writeBytes(end);
                fis.close();
            }
            dos.writeBytes("--" + BOUNDARY + "--\r\n");
            dos.flush();

            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            result = br.readLine();
            // System.out.println("result==" + result);
            dos.close();
        } catch (Exception e) {
            result = "{\"ImgUrl\":null,\"Result\":false,\"Msg\":\"上传失败\"}";
        }
        return result;

    }
}
