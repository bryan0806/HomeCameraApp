package com.example.android.sftpjsch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Properties;

public class MainActivity extends Activity implements View.OnClickListener {
    private final  String TAG="MainActivity";
    private Button buttonUpLoad = null;
    private Button buttonDownLoad = null;
    private Button buttonPlay = null;
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
        buttonPlay = (Button) findViewById(R.id.button_play);
        //设置控件对应相应函数
        buttonUpLoad.setOnClickListener(this);
        buttonDownLoad.setOnClickListener(this);
        buttonPlay.setOnClickListener(this);
        sftp = new SFTPUtils("host address", "user","password");

    }
    public void onClick(final View v) {
        // TODO Auto-generated method stub
        new Thread() {
            @Override
            public void run() {
                //这里写入子线程需要做的工作

                switch (v.getId()) {
                    case R.id.button_upload: {
                        //開始連結到server然後下載到temp資料夾
                        try {
                            String dateCommand = "sudo rm /media/MyBook/temp/*;cp /media/MyBook/camera/*`date +\"%Y%m%d\" -d \"1 day ago\"`*.avi /media/MyBook/temp";
                            connectAndDownload("user", "password", "host address", 22, dateCommand);
                            Log.d(TAG,"連接server下達指令 - download yesterday's files");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
                        }
                        String localPath = "/storage/emulated/0/Download/temp/";
                        String remotePath = "/media/MyBook/temp/";

                        downloadToDevice(remotePath,localPath);
                        playFile(localPath);
                    }
                    break;

                    case R.id.button_download: {
                        //開始連結到server然後下載到temp資料夾
                        try {
                            String dateCommand = "sudo rm /media/MyBook/temp/*;cp /media/MyBook/camera/*`date +\"%Y%m%d\"`*.avi /media/MyBook/temp";
                            connectAndDownload("user", "password", "host address", 22, dateCommand);
                            Log.d(TAG,"連接server下達指令 - download today's files");
                        } catch (Exception e) {
                            e.printStackTrace();
                             Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
                        }
                        String localPath = "/storage/emulated/0/Download/temp/";
                        String remotePath = "/media/MyBook/temp/";

                        downloadToDevice(remotePath,localPath);
                        playFile(localPath);
                    }
                    break;

                    case R.id.button_play: {
                        //上传文件
                        Log.d(TAG,"播放影片");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String type = "video/avi";
                        Uri uri = Uri.parse("file:///storage/emulated/0/Download/temp/01-20170105072637.avi");
                        intent.setDataAndType(uri, type);
                        startActivity(intent);
                    }
                    break;
                    default:
                        break;
                }
            }
        }.start();
    };

    public void playFile(String filePathName){

        File folder = new File(filePathName);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String type = "video/avi";
                Uri uri = Uri.parse("file://"+filePathName+listOfFiles[i].getName());
                intent.setDataAndType(uri, type);
                startActivity(intent);
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }


    }

    public Boolean connectAndDownload(
            String username,
            String password,
            String hostname,
            int port,
            String datecommand
    ) throws Exception {

        JSch jsch=new JSch();
        Session session = jsch.getSession(username,hostname,port);
        session.setPassword(password);

        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking","no");


        session.setConfig(prop);

        session.connect();

        ChannelExec channelssh=(ChannelExec)session.openChannel("exec");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        channelssh.setCommand(datecommand);
        channelssh.connect();
        while (channelssh.getExitStatus() == -1){
            try{Thread.sleep(1000);}catch(Exception e){System.out.println(e);}
        }

        channelssh.disconnect();
        session.disconnect();
        return true;
    }

    public void downloadToDevice(String remotePath,String localPath){
        //下载文件
        Log.d(TAG,"下载文件");

        sftp.connect();
        Log.d(TAG,"连接成功");
        //sftp.downloadFile(remotePath, "01-20170105072637.avi", localPath, "01-20170105072637.avi");
        sftp.batchDownLoadFile(remotePath,localPath,"",false);
        Log.d(TAG,"下载成功");
        sftp.disconnect();
        Log.d(TAG,"断开连接");
    }


}



