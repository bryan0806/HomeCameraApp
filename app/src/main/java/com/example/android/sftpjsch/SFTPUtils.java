package com.example.android.sftpjsch;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;


/**
 * Created by bryan.liu on 2017/1/6.
 */

public class SFTPUtils {

    private String TAG="SFTPUtils";
    private String host;
    private String username;
    private String password;
    private int port = 22;
    private ChannelSftp sftp = null;
    private Session sshSession = null;
    private int maxFiles;
    private int downloadStatus = 1;
    private Handler progressBarHandler = new Handler();


    public SFTPUtils (String host, String username, String password, Handler handler) {
        this.host = host;
        this.username = username;
        this.password = password;
        progressBarHandler = handler;
    }

    /**
     * connect server via sftp
     */
    public ChannelSftp connect() {
        JSch jsch = new JSch();
        try {
            sshSession = jsch.getSession(username, host, port);
            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            if (channel != null) {
                channel.connect();
            } else {
                Log.e(TAG, "channel connecting failed.");
            }
            sftp = (ChannelSftp) channel;
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return sftp;
    }


    /**
     * 断开服务器
     */
    public void disconnect() {
        if (this.sftp != null) {
            if (this.sftp.isConnected()) {
                this.sftp.disconnect();
                Log.d(TAG,"sftp is closed already");
            }
        }
        if (this.sshSession != null) {
            if (this.sshSession.isConnected()) {
                this.sshSession.disconnect();
                Log.d(TAG,"sshSession is closed already");
            }
        }
    }

    /**
     * 单个文件上传
     * @param remotePath
     * @param remoteFileName
     * @param localPath
     * @param localFileName
     * @return
     */
    public boolean uploadFile(String remotePath, String remoteFileName,
                              String localPath, String localFileName) {
        FileInputStream in = null;
        try {
            createDir(remotePath);
            System.out.println(remotePath);
            File file = new File(localPath + localFileName);
            in = new FileInputStream(file);
            System.out.println(in);
            sftp.put(in, remoteFileName);
            System.out.println(sftp);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();       } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 批量上传
     * @param remotePath
     * @param localPath
     * @param del
     * @return
     */
    public boolean bacthUploadFile(String remotePath, String localPath,
                                   boolean del) {
        try {
            File file = new File(localPath);
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()
                        && files[i].getName().indexOf("bak") == -1) {
                    synchronized(remotePath){
                        if (this.uploadFile(remotePath, files[i].getName(),
                                localPath, files[i].getName())
                                && del) {
                            deleteFile(localPath + files[i].getName());
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.disconnect();
        }
        return false;

    }

    /**
     * 批量下载文件
     *
     * @param remotPath
     *            远程下载目录(以路径符号结束)
     * @param localPath
     *            本地保存目录(以路径符号结束)
     * @param fileFormat
     *            下载文件格式(以特定字符开头,为空不做检验)
     * @param del
     *            下载后是否删除sftp文件
     * @return
     */
    @SuppressWarnings("rawtypes")
    public boolean batchDownLoadFile(String remotPath, String localPath,
                                     String fileFormat, boolean del) {
        try {
            connect();
            Vector v = listFiles(remotPath);
            if (v.size() > 0) {

                Iterator it = v.iterator();
                System.out.println("There are "+Integer.toString(v.size())+" files to download");
                maxFiles = v.size();
                while (it.hasNext()) {
                    LsEntry entry = (LsEntry) it.next();
                    String filename = entry.getFilename();
                    System.out.println("filename:"+filename);
                    SftpATTRS attrs = entry.getAttrs();
                    if (!attrs.isDir()) {
                        if (fileFormat != null && !"".equals(fileFormat.trim())) {
                            if (filename.startsWith(fileFormat)) {
                                if (this.downloadFile(remotPath, filename,
                                        localPath, filename)
                                        && del) {
                                    deleteSFTP(remotPath, filename);
                                }
                            }
                        } else {
                            if (this.downloadFile(remotPath, filename,
                                    localPath, filename)
                                    && del) {
                                deleteSFTP(remotPath, filename);
                            }
                        }
                    }
                }
            }
        } catch (SftpException e) {
            e.printStackTrace();
        } finally {
            this.disconnect();
        }
        return false;
    }

    /**
     * 单个文件下载
     * @param remotePath
     * @param remoteFileName
     * @param localPath
     * @param localFileName
     * @return
     */
    public boolean downloadFile(String remotePath, String remoteFileName,
                                String localPath, String localFileName) {
        try {
            sftp.cd(remotePath);
            //File file = new File(localPath + localFileName);
            //mkdirs(localPath + localFileName);

            // from http://www.cnblogs.com/longyg/archive/2012/06/25/2561332.html


            //sftp.get(remoteFileName, new FileOutputStream(file));
            byte[] buffer = new byte[1024000];
            BufferedInputStream bis = new BufferedInputStream(sftp.get(remotePath+remoteFileName));
            File newFile = new File(localPath+localFileName);
            mkdirs(localPath + localFileName);
            OutputStream os = new FileOutputStream(newFile);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            int readCount;
            //System.out.println("Getting: " + theLine);
            while( (readCount = bis.read(buffer)) > 0) {
                //System.out.println("Writing: " );
                checkStatus();
                bos.write(buffer, 0, readCount);
            }
            bis.close();
            bos.close();
            System.out.println("Finished Writing "+remoteFileName );
            downloadStatus++;
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 删除文件
     * @param filePath
     * @return
     */
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        if (!file.isFile()) {
            return false;
        }
        return file.delete();
    }

    public boolean createDir(String createpath) {
        try {
            if (isDirExist(createpath)) {
                this.sftp.cd(createpath);
                Log.d(TAG,createpath);
                return true;
            }
            String pathArry[] = createpath.split("/");
            StringBuffer filePath = new StringBuffer("/");
            for (String path : pathArry) {
                if (path.equals("")) {
                    continue;
                }
                filePath.append(path + "/");
                if (isDirExist(createpath)) {
                    sftp.cd(createpath);
                } else {
                    sftp.mkdir(createpath);
                    sftp.cd(createpath);
                }
            }
            this.sftp.cd(createpath);
            return true;
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断目录是否存在
     * @param directory
     * @return
     */
    @SuppressLint("DefaultLocale")
    public boolean isDirExist(String directory) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            isDirExistFlag = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }

    public void deleteSFTP(String directory, String deleteFile) {
        try {
            sftp.cd(directory);
            sftp.rm(deleteFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建目录
     * @param path
     */
    public void mkdirs(String path) {
        File f = new File(path);
        String fs = f.getParent();
        f = new File(fs);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    /**
     * 列出目录文件
     * @param directory
     * @return
     * @throws SftpException
     */

    @SuppressWarnings("rawtypes")
    public Vector listFiles(String directory) throws SftpException {
        return sftp.ls(directory);
    }

    public int returnMax(){
        return maxFiles;
    }

    public int returnStatus(){
        return downloadStatus;
    }



    private void checkStatus(){
        if (downloadStatus < maxFiles) {

            // process some tasks
            //progressBarStatus = returnStatus();
            System.out.println("returnMax="+returnMax());
            System.out.println("return Status="+returnStatus());
            //System.out.println("return status = "+returnStatus());
            // your computer is too fast, sleep 1 second
            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            // Update the progress bar
            Message msg = new Message();
            msg.what = downloadStatus;
            progressBarHandler.sendMessage(msg);

        }

        // ok, file is downloaded,
        if (downloadStatus >= maxFiles) {

            // sleep 2 seconds, so that you can see the 100%
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // close the progress bar dialog
            //progressBar.dismiss();
            Message msg = new Message();
            msg.what = downloadStatus;
            progressBarHandler.sendMessage(msg);
        }
    }



}
