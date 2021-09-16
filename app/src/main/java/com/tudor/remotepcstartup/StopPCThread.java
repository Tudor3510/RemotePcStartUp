package com.tudor.remotepcstartup;

import android.os.Environment;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class StopPCThread extends Thread{

    private String PChost;
    private String PCexternal;
    private String PCport;
    private String PCuser;
    private String PCpassword;

    private final String SETTINGS_LOCATION = Environment.getExternalStorageDirectory().getPath() + "/" + "RemotePcStartUp/stop.settings";

    private boolean homeNetwork = false;

    public StopPCThread setHomeNetwork(boolean switchStatus){
        homeNetwork = switchStatus;
        return this;
    }

    public boolean getHomeNetwork(){
        return homeNetwork;
    }

    @Override
    public void start(){
        try {
            BufferedReader settingsReader = new BufferedReader((new FileReader(SETTINGS_LOCATION)));
            PChost = settingsReader.readLine();
            PCexternal = settingsReader.readLine();
            PCport = settingsReader.readLine();
            PCuser = settingsReader.readLine();
            PCpassword = settingsReader.readLine();

            settingsReader.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        super.start();
    }

    @Override
    public void run() {


        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            Session session;
            if (!homeNetwork)
                session = jsch.getSession(PCuser, PCexternal, Integer.parseInt(PCport));
            else
                session = jsch.getSession(PCuser, PChost, Integer.parseInt(PCport));
            session.setPassword(PCpassword);
            session.setConfig(config);
            session.connect();

            String command = "sudo poweroff";
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);

            ((ChannelExec) channel).setPty(true);

            OutputStream out = channel.getOutputStream();
            channel.connect();

            out.write((PCpassword + "\n").getBytes());
            out.flush();

            channel.wait();

            channel.disconnect();
            session.disconnect();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
