package com.example.android.sftpjsch;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.Calendar;

import static android.R.attr.format;
import static android.R.attr.scaleGravity;

public class MainActivity extends Activity implements View.OnClickListener {
    private final  String TAG="MainActivity";
    private Button buttonUpLoad = null;
    private Button buttonDownLoad = null;
    private Button buttonDownloadDate = null;
    private Button buttonPlay = null;
    private Button buttonExit = null;
    private SFTPUtils sftp;
    private int mYear, mMonth, mDay;
    private TextView dateText = null;
    DecimalFormat formatter = new DecimalFormat("00");
    private String theYear, theMonth, theDay = "";
    // Save server address / user name / password to sharepreferences
    SharedPreferences sharedpreferences;
    TextView server;
    TextView user;
    TextView password;
    public static final String mypreference = "mypref";
    public static final String Server = "serverKey";
    public static final String User = "userKey";
    public static final String Password = "passwordKey";

    Handler progressBarHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            if (msg.what == 1) {
                progressBar.setMax(sftp.returnMax()-2);
                progressBar.setProgress(msg.what);
            } else if (msg.what < sftp.returnMax()) {
                progressBar.setProgress(msg.what);
            } else if (msg.what == sftp.returnMax()-2) {
                progressBar.dismiss();
            }
        }
    };
    ProgressDialog progressBar;
    private int progressBarStatus = 0;
    //private Handler progressBarHandler = new Handler();
    private long fileSize = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        server = (TextView) findViewById(R.id.server);
        user = (TextView) findViewById(R.id.user);
        password = (TextView) findViewById(R.id.password);
        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);
        if (sharedpreferences.contains(Server)) {
            server.setText(sharedpreferences.getString(Server, ""));
        }
        if (sharedpreferences.contains(User)) {
            user.setText(sharedpreferences.getString(User, ""));
        }
        if (sharedpreferences.contains(Password)) {
            password.setText(sharedpreferences.getString(Password, ""));
        }

        init();
    }

    public void init(){
        //获取控件对象
        buttonUpLoad = (Button) findViewById(R.id.button_upload);
        buttonDownLoad = (Button) findViewById(R.id.button_download);
        buttonDownloadDate = (Button) findViewById(R.id.button_download_date);
        buttonPlay = (Button) findViewById(R.id.button_play);
        buttonExit = (Button) findViewById(R.id.button_exit);
        dateText = (TextView)findViewById(R.id.dateText);
        //设置控件对应相应函数
        buttonUpLoad.setOnClickListener(this);
        buttonDownLoad.setOnClickListener(this);
        buttonDownloadDate.setOnClickListener(this);
        buttonPlay.setOnClickListener(this);
        buttonExit.setOnClickListener(this);

        String server_string = "";
        String user_string = "";
        String password_string = "";
        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);

        if (sharedpreferences.contains(Server)) {
            server_string = sharedpreferences.getString(Server, "");
        }
        if (sharedpreferences.contains(User)) {
            user_string = sharedpreferences.getString(User, "");
        }
        if (sharedpreferences.contains(Password)) {
            password_string = sharedpreferences.getString(Password, "");
        }


        sftp = new SFTPUtils(server_string, user_string,password_string,progressBarHandler);
    }

    public void Save(View view) {
        String server_add = server.getText().toString();
        String user_name = user.getText().toString();
        String pass = password.getText().toString();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Server, server_add);
        editor.putString(User, user_name);
        editor.putString(Password, pass);
        editor.commit();
    }

    public void clear(View view) {
        server = (TextView) findViewById(R.id.server);
        user = (TextView) findViewById(R.id.user);
        password = (TextView) findViewById(R.id.password);
        server.setText("");
        user.setText("");
        password.setText("");
    }

    public void Get(View view) {
        server = (TextView) findViewById(R.id.server);
        user = (TextView) findViewById(R.id.user);
        password = (TextView) findViewById(R.id.password);
        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);

        if (sharedpreferences.contains(Server)) {
            server.setText(sharedpreferences.getString(Server, ""));
        }
        if (sharedpreferences.contains(User)) {
            user.setText(sharedpreferences.getString(User, ""));
        }
        if (sharedpreferences.contains(Password)) {
            password.setText(sharedpreferences.getString(Password, ""));
        }

    }


    public void onClick(final View v) {
        // TODO Auto-generated method stub
        String server_string = "";
        String user_string = "";
        String password_string = "";
        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);

        if (sharedpreferences.contains(Server)) {
            server_string = sharedpreferences.getString(Server, "");
        }
        if (sharedpreferences.contains(User)) {
            user_string = sharedpreferences.getString(User, "");
        }
        if (sharedpreferences.contains(Password)) {
            password_string = sharedpreferences.getString(Password, "");
        }

                switch (v.getId()) {
                    case R.id.button_upload: {
                        statusBar(v);
                        final String finalServer_string1 = server_string;
                        final String finalPassword_string = password_string;
                        final String finalUser_string1 = user_string;
                        new Thread() {
                            @Override
                            public void run() {
                                //这里写入子线程需要做的工作
                                //開始連結到server然後下載到temp資料夾
                                try {
                                    String dateCommand = "sudo rm /media/69a94f44-6f67-49f6-96b2-fe9112c75893/temp/*;cp /media/69a94f44-6f67-49f6-96b2-fe9112c75893/camera/*`date +\"%Y%m%d\" -d \"1 day ago\"`*.avi /media/69a94f44-6f67-49f6-96b2-fe9112c75893/temp";
                                    connectAndDownload(finalUser_string1, finalPassword_string, finalServer_string1, 22, dateCommand);
                                    Log.d(TAG, "連接server下達指令 - download yesterday's files");
                                } catch (Exception e) {
                                    Log.d(TAG, "enter exception and user.tostring is "+user);
                                    e.printStackTrace();
                                    //Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
                                }
                                String localPath = "/storage/emulated/0/Download/temp/";
                                Log.d(TAG, "internal storage ok !!");
                                String remotePath = "/media/69a94f44-6f67-49f6-96b2-fe9112c75893/temp/";
                                downloadToDevice(remotePath, localPath);
                                //playFile(localPath);
                            }
                        }.start();
                    }
                    break;

                    case R.id.button_download: {
                        statusBar(v);
                        final String finalUser_string = user_string;
                        final String finalServer_string = server_string;
                        final String finalPasswrod_string = password_string;
                        new Thread() {
                            @Override
                            public void run() {
                                //这里写入子线程需要做的工作
                                //開始連結到server然後下載到temp資料夾
                                try {
                                    Log.d(TAG,"user: "+ finalUser_string);
                                    String dateCommand = "sudo rm /media/69a94f44-6f67-49f6-96b2-fe9112c75893/temp/*;cp /media/69a94f44-6f67-49f6-96b2-fe9112c75893/camera/*`date +\"%Y%m%d\"`*.avi /media/69a94f44-6f67-49f6-96b2-fe9112c75893/temp";
                                    connectAndDownload(finalUser_string, finalPasswrod_string, finalServer_string, 22, dateCommand);
                                    Log.d(TAG, "連接server下達指令 - download today's files");
                                } catch (Exception e) {
                                    Log.d(TAG, "enter exception and user.tostring is "+user);
                                    e.printStackTrace();
                                    //Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
                                }

                                String localPath = "/storage/emulated/0/Download/temp/";

                                String remotePath = "/media/69a94f44-6f67-49f6-96b2-fe9112c75893/temp/";

                                downloadToDevice(remotePath, localPath);
                                //playFile(localPath);
                            }
                        }.start();
                    }
                    break;

                    case R.id.button_play: {
                        //上传文件
                        Log.d(TAG,"播放影片");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String type = "video/avi";
                        //Uri uri = Uri.parse("file:///storage/emulated/0/Download/temp/01-20170105072637.avi");
                        Uri uri = FileProvider.getUriForFile(MainActivity.this,
                                BuildConfig.APPLICATION_ID + ".provider",
                                new File("/storage/emulated/0/Download/temp/01-20170105072637.avi"));
                        intent.setDataAndType(uri, type);
                        startActivity(intent);
                    }
                    break;

                    case R.id.button_download_date: {
                        Log.d(TAG,"Enter date case");
                        final Calendar c = Calendar.getInstance();
                        mYear = c.get(Calendar.YEAR);
                        mMonth = c.get(Calendar.MONTH);
                        mDay = c.get(Calendar.DAY_OF_MONTH);
                        new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int day) {
                                //String format = getString(R.string.set_date) + setDateFormat(year,month,day);
                                theYear = String.valueOf(year);
                                theMonth = formatter.format(month+1);
                                theDay = formatter.format(day);
                                Log.d(TAG,"Year:"+String.valueOf(year)+"Month:"+theMonth+"Day:"+theDay);
                                //dateText.setText(format);
                                downloadTheDate(theYear+theMonth+theDay,v);

                            }

                        }, mYear,mMonth, mDay).show();

                    }
                    break;

                    case R.id.button_exit:{
                        File deletefiles = new File("/storage/emulated/0/Download/temp/");
                        deleteRecursive(deletefiles);
                        System.exit(0);
                    }
                    break;

                    default:
                        Log.d(TAG,"Enter DEFAULT case");
                        break;
                }
            }

    public void playFile(String filePathName){

        File folder = new File(filePathName);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String type = "video/avi";
                //Uri uri = Uri.parse("file://"+filePathName+listOfFiles[i].getName());
                Uri uri = FileProvider.getUriForFile(MainActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        new File(filePathName+listOfFiles[i]));

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
        //增加這個while loop讓程式可以順利等到server下載完成之後才執行下載到手機的動作
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
        //sftp.downloadFile(remotePath, "16-20170623063156.avi", localPath, "16-20170623063156.avi");
        sftp.batchDownLoadFile(remotePath,localPath,"",false);
        Log.d(TAG,"下载成功");
        sftp.disconnect();
        Log.d(TAG,"断开连接");
    }

    private String setDateFormat(int year,int monthOfYear,int dayOfMonth){
        return String.valueOf(year) + "-"
                + String.valueOf(monthOfYear + 1) + "-"
                + String.valueOf(dayOfMonth);
    }

    private void downloadTheDate(final String theexecdate,View view){
        statusBar(view);
        String server_string = "";
        String user_string = "";
        String password_string = "";
        sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);

        if (sharedpreferences.contains(Server)) {
            server_string = sharedpreferences.getString(Server, "");
        }
        if (sharedpreferences.contains(User)) {
            user_string = sharedpreferences.getString(User, "");
        }
        if (sharedpreferences.contains(Password)) {
            password_string = sharedpreferences.getString(Password, "");
        }

        final String finalUser_string = user_string;
        final String finalPassword_string = password_string;
        final String finalServer_string = server_string;
        new Thread() {
            @Override
            public void run() {
                //这里写入子线程需要做的工作
                //開始連結到server然後下載到temp資料夾
                try {
                    String dateCommand = "sudo rm /media/69a94f44-6f67-49f6-96b2-fe9112c75893/temp/*;cp /media/69a94f44-6f67-49f6-96b2-fe9112c75893/camera/*"+theexecdate+"*.avi /media/69a94f44-6f67-49f6-96b2-fe9112c75893/temp";
                    connectAndDownload(finalUser_string, finalPassword_string, finalServer_string, 22, dateCommand);
                    Log.d(TAG, "連接server下達指令 - download today's files");
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Not Connected", Toast.LENGTH_LONG).show();
                }
                String localPath = "/storage/emulated/0/Download/temp/";
                String remotePath = "/media/69a94f44-6f67-49f6-96b2-fe9112c75893/temp/";

                downloadToDevice(remotePath, localPath);
                //playFile(localPath);
            }
        }.start();
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    private void statusBar(View view){

        // prepare for a progress bar dialog
        progressBar = new ProgressDialog(view.getContext());
        progressBar.setCancelable(true);
        progressBar.setMessage("File downloading ...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(0);
        progressBar.setMax(sftp.returnMax());
        progressBar.show();

        //reset progress bar status
        progressBarStatus = 0;

        //reset filesize
        fileSize = 0;
    }



}



