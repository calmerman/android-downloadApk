package com.downloadapk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progress = findViewById(R.id.tv_progress);
//        findViewById(R.id.tv_download).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DownloadApkUtil downloadApkUtil = new DownloadApkUtil(getApplicationContext(), BuildConfig.APPLICATION_ID, new DownloadApkProgressListener() {
//                    @Override
//                    public void onDownloadStar() {
//                        progress.setText("downloading   " +  "开始下载");
//                    }
//
//                    @Override
//                    public void onDownloading(int percent) {
//                        progress.setText("downloading   " + percent + "%");
//
//                    }
//
//                    @Override
//                    public void onDownloadEnd() {
//                        progress.setText("downloading   " +  "开始完成");
//                    }
//
//                });
//
//                downloadApkUtil.startDownloadAPKByUrl("http://imtt.dd.qq.com/16891/BF302D86013B6105A446C11C9CE3F007.apk?fsname=com.u17.comic.phone_3.4.0.1_3400100.apk&csr=1bbd");
//            }
//        });


    }
}
