package com.tudor.remotepcstartup;

import android.os.Environment;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class StartPCThread extends Thread{

    private String DDWRThost;
    private String DDWRTport;
    private String DDWRTuser;
    private String DDWRTpassword;
    private String broadcastAddress;
    private String macAddress;

    private final String settingsLocation = Environment.getExternalStorageDirectory().getPath() + "/" + "RemotePcStartUp/start.settings";

    private boolean homeNetwork = false;

    public StartPCThread setHomeNetwork(boolean switchStatus){
        homeNetwork = switchStatus;
        return this;
    }

    public boolean getHomeNetwork(){
        return homeNetwork;
    }

    @Override
    public void run(){

        try {
            BufferedReader settingsReader = new BufferedReader((new FileReader(settingsLocation)));
            DDWRThost = settingsReader.readLine();
            DDWRTport = settingsReader.readLine();
            DDWRTuser = settingsReader.readLine();
            DDWRTpassword = settingsReader.readLine();
            broadcastAddress = settingsReader.readLine();
            macAddress = settingsReader.readLine();

            settingsReader.close();
        }catch (IOException exception){
            exception.printStackTrace();
        }

        String command = "/usr/sbin/wol -i " + broadcastAddress + " " + macAddress;

        if (homeNetwork) {
            try {

                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                JSch jsch = new JSch();
                Session session = jsch.getSession(DDWRTuser, DDWRThost, Integer.parseInt(DDWRTport));
                session.setPassword(DDWRTpassword);
                session.setConfig(config);
                session.connect();

                Channel channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);

                channel.connect();

                channel.disconnect();
                session.disconnect();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

    }
}
