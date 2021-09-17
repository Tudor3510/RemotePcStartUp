package com.tudor.remotepcstartup;

import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class StartPCThread extends Thread{

    private String DDWRThost;
    private String DDWRTport;
    private String DDWRTuser;
    private String DDWRTpassword;
    private String broadcastAddress;
    private String macAddress;
    private AppCompatActivity appActivity;

    private static final String SETTINGS_LOCATION = Environment.getExternalStorageDirectory().getPath() + "/" + "RemotePcStartUp/start.settings";
    private static final int WOL_PORT = 9;

    private boolean homeNetwork = false;

    public StartPCThread(AppCompatActivity appActivity){
        this.appActivity = appActivity;
    }

    public StartPCThread setHomeNetwork(boolean switchStatus){
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

        super.start();
    }

    @Override
    public void run(){
        if (!homeNetwork) {
            try {

                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                JSch jsch = new JSch();
                Session session = jsch.getSession(DDWRTuser, DDWRThost, Integer.parseInt(DDWRTport));
                session.setPassword(DDWRTpassword);
                session.setConfig(config);
                session.connect();

                String command = "/usr/sbin/wol -i " + broadcastAddress + " " + macAddress;
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
        }else{

            try {
                byte[] macBytes = getMacBytes(macAddress);
                byte[] bytes = new byte[6 + 16 * macBytes.length];
                for (int i = 0; i < 6; i++) {
                    bytes[i] = (byte) 0xff;
                }
                for (int i = 6; i < bytes.length; i += macBytes.length) {
                    System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
                }

                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(broadcastAddress), WOL_PORT);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
                socket.close();
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }

        }

        appActivity.runOnUiThread(new Thread(){
            public void run(){
                Toast.makeText(appActivity.getApplicationContext(),
                        "Your PC is waking up",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("([:\\-])");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
}
