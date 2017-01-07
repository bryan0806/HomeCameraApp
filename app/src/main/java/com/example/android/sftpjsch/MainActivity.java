package com.example.android.sftpjsch;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {
    private final  String TAG="MainActivity";
    private Button buttonUpLoad = null;
    private Button buttonDownLoad = null;
    private SFTPUtils sftp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init(){
        //获取控件对象
        buttonUpLoad = (Button) findViewById(R.id.button_upload);
        buttonDownLoad = (Button) findViewById(R.id.button_download);
        //设置控件对应相应函数
        buttonUpLoad.setOnClickListener(this);
        buttonDownLoad.setOnClickListener(this);
        sftp = new SFTPUtils("1.34.74.162", "osmc","2jdilgxl");
    }
    public void onClick(final View v) {
        // TODO Auto-generated method stub
        new Thread() {
            @Override
            public void run() {
                //这里写入子线程需要做的工作

                switch (v.getId()) {
                    case R.id.button_upload: {
                        //上传文件
                        Log.d(TAG,"上传文件");
                        String localPath = "/storage/609B-EE68/Download/temp/";
                        String remotePath = "/";
                        sftp.connect();
                        Log.d(TAG,"连接成功");
                        sftp.uploadFile(remotePath,"APPInfo.xml", localPath, "APPInfo.xml");
                        Log.d(TAG,"上传成功");
                        sftp.disconnect();
                        Log.d(TAG,"断开连接");
                    }
                    break;

                    case R.id.button_download: {
                        //下载文件
                        Log.d(TAG,"下载文件");
                        String localPath = "/storage/emulated/0/Download/temp/";
                        String remotePath = "/media/MyBook/temp/";
                        sftp.connect();
                        Log.d(TAG,"连接成功");
                        //sftp.downloadFile(remotePath, "01-20170105072637.avi", localPath, "01-20170105072637.avi");
                        sftp.batchDownLoadFile(remotePath,localPath,"",false);
                        Log.d(TAG,"下载成功");
                        sftp.disconnect();
                        Log.d(TAG,"断开连接");

                    }
                    break;
                    default:
                        break;
                }
            }
        }.start();
    };
}

