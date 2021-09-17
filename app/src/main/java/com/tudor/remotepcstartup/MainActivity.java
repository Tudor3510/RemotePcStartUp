package com.tudor.remotepcstartup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private Button stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(view -> {
            boolean switchStatus = ((SwitchCompat) findViewById(R.id.homeNetworkSwitch)).isChecked();
            new StartPCThread(this).setHomeNetwork(switchStatus).start();
        });

        stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(view -> {
            boolean switchStatus = ((SwitchCompat) findViewById(R.id.homeNetworkSwitch)).isChecked();
            new StopPCThread(this).setHomeNetwork(switchStatus).start();
        });
    }
}