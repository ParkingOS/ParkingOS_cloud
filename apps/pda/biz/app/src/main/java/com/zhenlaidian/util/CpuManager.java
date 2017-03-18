package com.zhenlaidian.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by xulu on 2016/6/14.
 */
public class CpuManager {


    // 获取CPU最大频率（单位KHZ）

    // "/system/bin/cat" 命令行

    // "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" 存储最大频率的文件的路径

    public static String getMaxCpuFreq() {

        String result = "";

        ProcessBuilder cmd;

        try {

            String[] args = {"/system/bin/cat",

                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};

            cmd = new ProcessBuilder(args);

            Process process = cmd.start();

            InputStream in = process.getInputStream();

            byte[] re = new byte[24];

            while (in.read(re) != -1) {

                result = result + new String(re);

            }

            in.close();

        } catch (IOException ex) {

            ex.printStackTrace();

            result = "N/A";

        }

        return result.trim();

    }


    // 获取CPU最小频率（单位KHZ）

    public static String getMinCpuFreq() {

        String result = "";

        ProcessBuilder cmd;

        try {

            String[] args = {"/system/bin/cat",

                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq"};

            cmd = new ProcessBuilder(args);

            Process process = cmd.start();

            InputStream in = process.getInputStream();

            byte[] re = new byte[24];

            while (in.read(re) != -1) {

                result = result + new String(re);

            }

            in.close();

        } catch (IOException ex) {

            ex.printStackTrace();

            result = "N/A";

        }

        return result.trim();

    }


    // 实时获取CPU当前频率（单位KHZ）

    public static String getCurCpuFreq() {

        String result = "N/A";

        try {

            FileReader fr = new FileReader(

                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");

            BufferedReader br = new BufferedReader(fr);

            String text = br.readLine();

            result = text.trim();

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

        return result;

    }


    // 获取CPU名字

    public static String getCpuName() {

        try {

            FileReader fr = new FileReader("/proc/cpuinfo");

            BufferedReader br = new BufferedReader(fr);

            String text = br.readLine();

            String[] array = text.split(":\\s+", 2);

            for (int i = 0; i < array.length; i++) {

            }

            return array[1];

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

        return null;

    }

    public static void getTotalMemory(Context context) {
        String str1 = "/proc/meminfo";
        String str2 = "";
        ArrayList<String> infos = new ArrayList<String>();
        int count = 0;
        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
            while ((str2 = localBufferedReader.readLine()) != null) {
                if (count < 2) {
                    String temp = "";
                    for (int i = 0; i < str2.length(); i++) {
//                        Log.i("MEMORY", "---" + str2.charAt(i));
                        if (str2.charAt(i) >= 48 && str2.charAt(i) <= 57) {
                            temp += str2.charAt(i);
                        }
                    }
                    infos.add(temp);
                }
                count++;
//                Log.i("MEMORY", "---" + str2);
            }
//            Log.i("MEMORY", "总内存：" + infos.get(0));
//            Log.i("MEMORY", "空余内存：" + infos.get(1));
//            Log.i("MEMORY", "内存百分比：" + Integer.parseInt(infos.get(1))*100/Integer.parseInt(infos.get(0)));
            if(Integer.parseInt(infos.get(1))*100/Integer.parseInt(infos.get(0))<20){
                CommontUtils.writeSDFile(context,"TotalMemory：",""+infos.get(0));
                CommontUtils.writeSDFile(context,"FreeMemory：",""+infos.get(1));
            }
        } catch (IOException e) {
        }
    }


}

