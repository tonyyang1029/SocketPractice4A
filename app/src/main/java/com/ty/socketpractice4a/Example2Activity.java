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

public class Example2Activity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtnStartServer;
    private Button mBtnStopServer;
    private Button mBtnStartClient;
    private Button mBtnStopClient;
    private Button mBtnClearInfo;
    //
    private TextView mTvInfo;
    //
    BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_example2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createViewComponents();
        //
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.ACTION_SERVER_STARTED)) {
                    mTvInfo.append("[Server] service is started\n");
                    mBtnStartServer.setEnabled(false);
                    mBtnStopServer.setEnabled(true);
                } else if (intent.getAction().equals(Config.ACTION_SERVER_STOPPED)) {
                    mTvInfo.append("[Server] service is stopped\n");
                    mBtnStartServer.setEnabled(true);
                    mBtnStopServer.setEnabled(false);
                } else if (intent.getAction().equals(Config.ACTION_SERVER_ERROR)) {
                    mTvInfo.append("[Server] " + intent.getStringExtra("error") + "\n");
                } else if (intent.getAction().equals(Config.ACTION_CLIENT_STARTED)) {
                    mTvInfo.append("[Client] service is started\n");
                    mBtnStartClient.setEnabled(false);
                    mBtnStopClient.setEnabled(true);
                } else if (intent.getAction().equals(Config.ACTION_CLIENT_STOPPED)) {
                    mTvInfo.append("[Client] service is stopped\n");
                    mBtnStartClient.setEnabled(true);
                    mBtnStopClient.setEnabled(false);
                } else if (intent.getAction().equals(Config.ACTION_CLIENT_ERROR)) {
                    mTvInfo.append("[Client] " + intent.getStringExtra("error") + "\n");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        //
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.ACTION_SERVER_STARTED);
        filter.addAction(Config.ACTION_SERVER_STOPPED);
        filter.addAction(Config.ACTION_SERVER_ERROR);
        filter.addAction(Config.ACTION_CLIENT_STARTED);
        filter.addAction(Config.ACTION_CLIENT_STOPPED);
        filter.addAction(Config.ACTION_CLIENT_ERROR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(mReceiver, filter);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ex2_btn_start_server) {
            startService(new Intent(this, ServerService.class));
        } else if (v.getId() == R.id.ex2_btn_stop_server) {
            stopService(new Intent(this, ServerService.class));
        } else if (v.getId() == R.id.ex2_btn_start_client) {
            startService(new Intent(this, ClientService.class));
        } else if (v.getId() == R.id.ex2_btn_stop_client) {
            stopService(new Intent(this, ClientService.class));
        } else if (v.getId() == R.id.ex2_btn_clear_info) {
            mTvInfo.setText("");
        }
    }

    private void createViewComponents() {
        mBtnStartServer = findViewById(R.id.ex2_btn_start_server);
        mBtnStartServer.setOnClickListener(this);
        mBtnStartServer.setEnabled(true);
        //
        mBtnStopServer = findViewById(R.id.ex2_btn_stop_server);
        mBtnStopServer.setOnClickListener(this);
        mBtnStopServer.setEnabled(false);
        //
        mBtnStartClient = findViewById(R.id.ex2_btn_start_client);
        mBtnStartClient.setOnClickListener(this);
        mBtnStartClient.setEnabled(true);
        //
        mBtnStopClient = findViewById(R.id.ex2_btn_stop_client);
        mBtnStopClient.setOnClickListener(this);
        mBtnStopClient.setEnabled(false);
        //
        mBtnClearInfo = findViewById(R.id.ex2_btn_clear_info);
        mBtnClearInfo.setOnClickListener(this);
        //
        mTvInfo = findViewById(R.id.ex2_tv_info);
        mTvInfo.setText("");
    }
}