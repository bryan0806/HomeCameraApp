package com.example.android.sftpjsch;

import com.jcraft.jsch.SftpProgressMonitor;


/**
 * Created by bryan.liu on 2017/1/11.
 */

public class SystemOutProgressMonito implements SftpProgressMonitor {
    public void SystemOutProgressMonitor() {;}

    public void init(int op, String src, String dest, long max)
    {
        System.out.println("STARTING: "+op+" "+src+" -> "+dest+" total: "+max);
    }

    public boolean count(long bytes)
    {
        for(int x=0;x<bytes;x++) {
            //System.out.print("#");
        }
        return(true);
    }

    public void end()
    {
        System.out.println("\nFINISHED!");
    }

}
