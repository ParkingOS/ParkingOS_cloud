package com.tq.zld.util;

import android.util.Log;

import java.io.File;

/**
 * Author：ClareChen
 * E-mail：ggchaifeng@gmail.com
 * Date：  15/6/25 下午4:46
 */
public class IOUtils {

    public static boolean deleteDirectory(File directory) {

        if (directory == null) {
            LogUtils.e(IOUtils.class, "--->> directory is null!!!");
            return true;
        }

        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    } else {
                        files[i].delete();
                    }
                }
            }
        }
        return (directory.delete());
    }


}
