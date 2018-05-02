package com.downloadapkutil;

/**
 * Created by wanxin on 2018/4/26.
 */

public interface DownloadApkProgressListener {

    void onDownloadStar();

    void onDownloading(int percent);

    void onDownloadEnd();
}
