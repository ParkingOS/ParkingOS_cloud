package com.zhenlaidian.util;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by zhangyunfei on 15/10/16.
 * 文件操作的工具类
 */
public class FileUtils {

    /**
     * 根据文件名查找文件是否存在;
     * @param file
     * @param keyword
     * @return
     */
    public static Boolean findFile(File file, String keyword) {
        if (!file.isDirectory()) {
            return false;
        }

        File[] files = new File(file.getPath()).listFiles();

        for (File f : files) {
            if (f.getName().equals(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 文件定期删除
     */
    public static void fileRegularDelete() {
        File file = new File(Environment.getExternalStorageDirectory() + "/TingCheBao");
        if(file != null){
            File[] listFiles = file.listFiles();
            if(listFiles == null){
                return;
            }
            int listFilesLength = listFiles.length;
            //Log.e(TAG, "Constant.FRAME_DUMP_FOLDER_PATH：文件个数"+listFilesLength);
            //如果文件个数大于3500个,5天，最大一天700辆车,图片300k一个，相当于1G
            //
            if(listFilesLength > 1000){
                long currentTime = System.currentTimeMillis();
                ArrayList<File> deleList = new ArrayList<File>();
                for(int i=0;i<listFilesLength;i++){
                    if(listFiles[i].isFile()){
                        long lastModified = listFiles[i].lastModified();
                        //Log.e(TAG, "文件最后修改时间："+lastModified);
                        //Log.e(TAG, "当前时间："+currentTime);
                        //Log.e(TAG, "时间："+FIVEDAYTAMP);
                        //判断最后修改日期是否小于五天前，是则删除
                        if((currentTime - 5*24*60*60*1000) >lastModified){
                            //Log.e(TAG,"删除文件名："+listFiles[i].getName());
                            deleList.add(listFiles[i]);
                        }
                    }
                }
                int size = deleList.size();
                if(size>0){
                    for(int i=0;i<size;i++){
                        deleList.get(i).delete();
                    }
                }
            }
        }
    }

}
