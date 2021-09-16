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
    private String PCport;
    private String PCuser;
    private String PCpassword;

    private final String settingsLocation = Environment.getExternalStorageDirectory().getPath() + "/" + "RemotePcStartUp/stop.settings";

    @Override
    public void run() {

        try {
            BufferedReader settingsReader = new BufferedReader((new FileReader(settingsLocation)));
            PChost = settingsReader.readLine();
            PCport = settingsReader.readLine();
            PCuser = settingsReader.readLine();
            PCpassword = settingsReader.readLine();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        String command = "sudo poweroff";

        try {

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            Session session = jsch.getSession(PCuser, PChost, Integer.parseInt(PCport));
            session.setPassword(PCpassword);
            session.setConfig(config);
            session.connect();

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
