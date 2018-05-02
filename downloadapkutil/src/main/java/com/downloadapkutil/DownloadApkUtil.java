package com.downloadapkutil;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by wanxin on 2018/4/26.
 */

public class DownloadApkUtil {
    //apk 文件的MIME TYPE
    private static final String APK_MIME_TYPE = "application/vnd.android.package-archive";
    //下载进度回调监听
    private DownloadApkProgressListener downloadApkProgressListener;
    private Handler handler = new Handler();

    private DownloadManager downManager;
    private long downloadTaskId;
    private Context context;
    private String packageName;
    private static String downloadUrl = "";

    public DownloadApkUtil(Context context, String packageName, DownloadApkProgressListener downloadApkProgressListener) {
        this.context = context;
        this.packageName = packageName;
        this.downloadApkProgressListener = downloadApkProgressListener;
        downManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

    }

    //设置允许下载的网络环境
    private int downloadNetworkType = ~0;
    public static final int MOBILE = DownloadManager.Request.NETWORK_MOBILE;
    public static final int WIFI = DownloadManager.Request.NETWORK_WIFI;

    /**
     * 设置允许下载网络环境 默认所有网络环境都可以下载
     *
     * @param downloadNetworkType MOBILE WIFI
     */

    public void setDownloadNetworkType(int downloadNetworkType) {
        this.downloadNetworkType = downloadNetworkType;
    }

    //设置下载文件名称
    private String downloadAPKName = "myFile.apk";

    /**
     * 设置下载文件名称
     *
     * @param downloadAPKName 下载文件名称
     */
    public void setDownloadAPKName(String downloadAPKName) {
        this.downloadAPKName = downloadAPKName;
    }


    public void startDownloadAPKByUrl(String downloadApkUrl) {
        if (downloadUrl.equals(downloadApkUrl)) {
            Toast.makeText(context, "正在下载", Toast.LENGTH_SHORT).show();
            return;
        }
        downloadUrl = downloadApkUrl;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        //网络环境下载
        request.setAllowedNetworkTypes(downloadNetworkType);
        //移动网络是否使用漫游 默认允许漫游下载
        request.setAllowedOverRoaming(true);
        //下载类型
        request.setMimeType(APK_MIME_TYPE);
        //设置下载位置
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, downloadAPKName);
        //不显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        //下载的id
        downloadTaskId = downManager.enqueue(request);
        downloadApkProgressListener.onDownloadStar();

        final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadTaskId);
                Cursor cursor = downManager.query(query);
                if (cursor.moveToNext()) {
                    double downloadSize = cursor.getDouble(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    double downSizeTotal = cursor.getDouble(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    //下载成功
                    if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //设置达到最大进度
                                downloadApkProgressListener.onDownloading(100);
                                //回调下载结束
                                downloadApkProgressListener.onDownloadEnd();
                            }
                        });
                        downloadUrl = "";
                        ses.shutdown();
                        installApk();
                    } else {
                        if (downSizeTotal > 0 && downloadSize > 0) {
                            final int percent = ((int) ((downloadSize / downSizeTotal) * 100));
                            if (downloadSize != downSizeTotal) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //回调正在下载的进度
                                        downloadApkProgressListener.onDownloading(percent);
                                    }
                                });
                            }
                        }
                    }
                }
                cursor.close();

            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }


    private void installApk() {
        File apkFile = queryDownloadedApk(context);
        if (!apkFile.exists()) {
            Toast.makeText(context, "APP安装文件不存在或已损坏", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, packageName + ".fileProvider", apkFile);
            intent.setDataAndType(contentUri, APK_MIME_TYPE);
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), APK_MIME_TYPE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    //通过downLoadId查询下载的apk，解决6.0以后安装的问题
    private File queryDownloadedApk(Context context) {
        File targetApkFile = null;
        DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadTaskId != -1) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadTaskId);
            query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
            Cursor cur = downloader.query(query);
            if (cur != null) {
                if (cur.moveToFirst()) {
                    String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    if (!TextUtils.isEmpty(uriString)) {
                        targetApkFile = new File(Uri.parse(uriString).getPath());
                    }
                }
                cur.close();
            }
        }
        return targetApkFile;
    }
}
