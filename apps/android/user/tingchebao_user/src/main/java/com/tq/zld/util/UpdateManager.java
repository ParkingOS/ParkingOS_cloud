package com.tq.zld.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.tq.zld.BuildConfig;
import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.bean.UpdateInfo;
import com.tq.zld.view.MainActivity;
import com.tq.zld.view.map.MapActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 软件更新管理，已过时，采用友盟自动更新SDK替代
 */
@Deprecated
public class UpdateManager {

    /**
     * 下载的文件名
     */
    private static final String FILE_NAME = "tingchebao.apk";

    /**
     * 下载通知的ID
     */
    public static final int NOTIFICATION_ID = 10;

    private Context mContext;

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    /**
     * 检查是否需要更新版本
     */
    public void checkUpdate() {
        String url = "http://d.tingchebao.com/update/user/update.xml?r=";

        if (BuildConfig.BUILD_TYPE.equals("beta")) {
            url = "http://d.tingchebao.com/update/user/update_beta.xml?r=";
        }

        url += System.currentTimeMillis();

        LogUtils.i(getClass(), "checkUpdate url: --->> " + url);
        if (NetWorkUtils.IsHaveInternet(mContext)) {
            new AQuery(mContext).ajax(url, byte[].class,
                    new AjaxCallback<byte[]>() {

                        @Override
                        public void callback(String url, byte[] object,
                                             AjaxStatus status) {
                            if (object != null) {
                                LogUtils.i(getClass(),
                                        "checkUpdate result: --->> "
                                                + new String(object));
                                InputStream is = new ByteArrayInputStream(
                                        object);
                                try {
                                    UpdateInfo info = getUpdateInfo(is);
                                    if (info == null) {
                                        showToast("数据解析异常！");
                                        return;
                                    }
                                    if (canShowUpdateDialog(info)) {
                                        showUpdateDialog(info);
                                    } else {
                                        showToast("已经是最新版本~");
                                    }
                                } catch (Exception e) {
                                    showToast("检查更新出错了！");
                                    e.printStackTrace();
                                }
                            } else {
                                showToast("网络异常，请稍后再试！");
                            }
                        }
                    });
        } else {
            showToast("无网络连接！");
        }
    }

    private boolean canShowUpdateDialog(UpdateInfo info) {

        if (info != null) {
            int versionCode = AndroidUtils.getVersionCode();
            if (versionCode < info.versionCode) {
                if (mContext instanceof MapActivity) {
                    int ignoreVersion = TCBApp.getAppContext().readInt(R.string.sp_ignore_version, versionCode);
                    if ("0".equals(info.force) || (ignoreVersion != info.versionCode && !"0"
                            .equals(info.remind))) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private void showToast(String msg) {
        if (!(mContext instanceof MapActivity)) {
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        }
    }

    // 需要更新时弹出升级对话框；
    private void showUpdateDialog(final UpdateInfo update) {
        AlertDialog.Builder builder = new Builder(mContext);
        builder.setTitle("发现新版本！");
        String description = update.versionName + "更新内容：\n";
        String[] descs = update.description.split(";");
        for (String desc : descs) {
            description += "\t" + desc + "；\n";
        }
        builder.setMessage(description.substring(0, description.length() - 2));
        builder.setCancelable(false);
        builder.setPositiveButton("现在更新", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {

                    File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs();
                    }

                    File file = new File(downloadDir, FILE_NAME);
                    if (file.exists()) {
                        String md5 = MD5Utils.getFileMD5(file);
                        if (md5.equals(update.md5)) {
                            install(file);
                            return;
                        }
                    }
                    // 先删除旧的下载文件
                    deleteOldApk(file);
                    downloadFile(update);
                } else {
                    Toast.makeText(mContext, "sd卡不可用", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        if (!"0".equals(update.force)) {
            builder.setNegativeButton("不，谢谢", null);
            if (mContext.getClass() != MainActivity.class) {
                builder.setNeutralButton("忽略此版本", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TCBApp.getAppContext().saveInt(R.string.sp_ignore_version, update.versionCode);
                    }
                });
            }
        }
        builder.create().show();
    }

    private DownloadTask mDownload;

    public void cancelDownloadTask() {
        if (mDownload != null && !mDownload.isCancelled()) {
            mDownload.cancel(true);
            mDownload.cancelNotification();
            deleteOldApk(null);
        }
    }

    private void downloadFile(UpdateInfo update) {
        int state = mContext.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
        if (state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER ||
                state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {

            // 自定义下载
            mDownload = new DownloadTask(mContext, update);
            mDownload.execute(update.apkurl);
            return;
        }
        // 调用系统下载管理组件
        downloadUseDownloadManager(update);
    }

    private void downloadUseDownloadManager(UpdateInfo update) {
        DownloadManager downloadManager = getDownloadManager();
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(update.apkurl));
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, FILE_NAME);
        request.setTitle("停车宝");
        request.setDescription(update.apkurl);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("application/vnd.android.package-archive");
        // request.setAllowedOverMetered(true);
        // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
        // | DownloadManager.Request.NETWORK_MOBILE);
        // request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        downloadManager.enqueue(request);
        Toast.makeText(mContext, "正在后台下载...", Toast.LENGTH_SHORT).show();
    }

    private void deleteOldApk(File oldApk) {

        if (oldApk == null) {
            oldApk = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);
        }

        if (oldApk.exists()) {
            oldApk.delete();
        }
    }

    private DownloadManager getDownloadManager() {
        return (DownloadManager) mContext
                .getSystemService(Context.DOWNLOAD_SERVICE);
    }

    // 安装apk；
    private void install(File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    /**
     * @param is 解析的xml的inputstream
     * @return UpdateInfo
     */
    private UpdateInfo getUpdateInfo(InputStream is) {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance()
                    .newPullParser();
            UpdateInfo info = new UpdateInfo();
            parser.setInput(is, "utf-8");
            int type = parser.getEventType();

            while (type != XmlPullParser.END_DOCUMENT) {
                switch (type) {
                    case XmlPullParser.START_TAG:
                        switch (parser.getName()) {
                            case "description":
                                info.description = parser.nextText();
                                break;
                            case "apkurl":
                                info.apkurl = parser.nextText();
                                break;
                            case "force":
                                info.force = parser.nextText();
                                break;
                            case "remind":
                                info.remind = parser.nextText();
                                break;
                            case "md5":
                                info.md5 = parser.nextText();
                                break;
                            case "versionCode":
                                info.versionCode = Integer.parseInt(parser.nextText());
                                break;
                            case "versionName":
                                info.versionName = parser.nextText();
                                break;
                        }
                        break;
                }
                type = parser.next();
            }
            return info;
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class DownloadTask extends AsyncTask<String, Integer, File> {

        private UpdateInfo mUpdateInfo;
        private ProgressDialog mDialog;
        private PowerManager.WakeLock mWakeLock;
        private Context mContext;
        private NotificationCompat.Builder mNotificationBuilder;
        private NotificationManager mNotificationManager;

        public DownloadTask(Context context, UpdateInfo info) {
            this.mContext = context;
            this.mUpdateInfo = info;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            if ("0".equals(mUpdateInfo.force)) {
                showProgressDialog();
                return;
            }
            showNotification();
        }

        private void showProgressDialog() {
            mDialog = new ProgressDialog(mContext);
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.setProgressNumberFormat("%1d KB/%2d KB");
            mDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "取消", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDialog.cancel();
                }
            });
            mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    DownloadTask.this.cancel(true);
                }
            });
            mDialog.show();
        }

        private void showNotification() {
            mNotificationBuilder = new NotificationCompat.Builder(mContext)
                    .setTicker("正在下载中...")
                    .setContentTitle("停车宝-" + mUpdateInfo.versionName)
                    .setWhen(System.currentTimeMillis())
                    .setProgress(0, 0, true)
                    .setColor(mContext.getResources().getColor(R.color.bg_green))
                    .setSmallIcon(getNotificationIcon())
                            //TODO ticker图标问题
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), getNotificationIcon()));
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
        }

        private int getNotificationIcon() {
            boolean whiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
            return whiteIcon ? R.drawable.ic_launcher_lollipop
                    : R.mipmap.ic_launcher;
        }

        public void cancelNotification() {
            if (mNotificationManager != null) {
                mNotificationManager.cancel(NOTIFICATION_ID);
            }
        }

        @Override
        protected File doInBackground(String... params) {

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            File apk;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                apk = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);
                output = new FileOutputStream(apk);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        UpdateManager.this.deleteOldApk(apk);
                        return null;
                    }
                    total += count;
                    if (fileLength > 0) {
                        // publishing the progress....
                        // only if total length is known
                        publishProgress((int) total, fileLength);
                    }
                    output.write(data, 0, count);
                }
                return apk;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mDialog != null) {
                mDialog.setIndeterminate(false);
                if (mDialog.getMax() < 1024) {
                    mDialog.setMax(values[1] / 1024);
                }
                mDialog.setProgress(values[0] / 1024);
            } else {
                if (mNotificationBuilder != null && mNotificationManager != null) {
                    mNotificationBuilder
                            .setProgress(values[1], values[0], false)
                            .setOnlyAlertOnce(true)
                            .setOngoing(true)
                            .setContentText(String.format("已下载：%d%", values[0] * 100 / values[1]));
                    mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                }
            }
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            mWakeLock.release();
            if (mDialog != null) {
                // 强制更新
                mDialog.dismiss();
                if (file != null) {
                    UpdateManager.this.install(file);
                }
            } else {
                // TODO 程序意外退出时，通知处理
                if (file != null) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file),
                            "application/vnd.android.package-archive");
                    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mNotificationBuilder.setAutoCancel(true)
                            .setContentText("下载完成，点击安装")
                            .setTicker("下载完成，点击安装").setContentIntent(pendingIntent);
                    mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                } else {
                    mNotificationBuilder
                            .setContentText("下载失败，您可以在“设置”->“关于”界面手动更新。").setTicker("下载失败！");
                    mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                    UpdateManager.this.deleteOldApk(null);
                }
            }
        }
    }
}
