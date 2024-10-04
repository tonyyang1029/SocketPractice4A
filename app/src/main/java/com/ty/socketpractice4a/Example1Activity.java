package com.ty.socketpractice4a;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Example1Activity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtnStartClient;
    private Button mBtnStopClient;
    private Button mBtnClearInfo;
    private TextView mTvInfo;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_example1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mBtnStartClient = findViewById(R.id.ex1_btn_start_client);
        mBtnStartClient.setOnClickListener(this);
        mBtnStartClient.setEnabled(true);
        //
        mBtnStopClient = findViewById(R.id.ex1_btn_stop_client);
        mBtnStopClient.setOnClickListener(this);
        mBtnStopClient.setEnabled(false);
        //
        mBtnClearInfo = findViewById(R.id.ex1_btn_clear_info);
        mBtnClearInfo.setOnClickListener(this);

        mTvInfo = findViewById(R.id.ex1_tv_info);
        mTvInfo.setText("");

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Config.ACTION_CLIENT_STARTED.equals(intent.getAction())) {
                    mBtnStartClient.setEnabled(false);
                    mBtnStopClient.setEnabled(true);
                    mTvInfo.append("[Client] service is started \n");
                } else if (Config.ACTION_CLIENT_STOPPED.equals(intent.getAction())) {
                    mBtnStartClient.setEnabled(true);
                    mBtnStopClient.setEnabled(false);
                    mTvInfo.append("[Client] service is stopped \n");
                } else if (Config.ACTION_CLIENT_ERROR.equals(intent.getAction())) {
                    mTvInfo.append("[Client] " + intent.getStringExtra("error") + "\n");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Config.ACTION_CLIENT_STARTED);
        intentFilter.addAction(Config.ACTION_CLIENT_STOPPED);
        intentFilter.addAction(Config.ACTION_CLIENT_ERROR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, intentFilter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(mReceiver, intentFilter);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService(new Intent(this, ClientService.class));
        unregisterReceiver(mReceiver);
        mBtnStartClient.setEnabled(true);
        mBtnStopClient.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ex1_btn_start_client) {
            startService(new Intent(this, ClientService.class));
        } else if (v.getId() == R.id.ex1_btn_stop_client) {
            stopService(new Intent(this, ClientService.class));
        } else if (v.getId() == R.id.ex1_btn_clear_info) {
            mTvInfo.setText("");
        }
    }
}