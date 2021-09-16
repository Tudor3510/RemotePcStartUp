package com.tudor.remotepcstartup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private Button stopButton;
    private SwitchCompat homeNetworkSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartPCThread startPCThread = new StartPCThread();

                boolean switchStatus = ((SwitchCompat) findViewById(R.id.homeNetworkSwitch)).isChecked();
                startPCThread.setHomeNetwork(switchStatus);

                startPCThread.start();
            }
        });

        stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(view -> new Thread(new StopPCThread()).start());
    }
}