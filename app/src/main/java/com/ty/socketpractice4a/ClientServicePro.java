package com.ty.socketpractice4a;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientServicePro extends Service {
    private ExecutorService mExecutor;
    private ArrayList<Client> mList;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mExecutor == null) {
            mExecutor = Executors.newCachedThreadPool();
            mList = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Client client = new Client("Client " + (i + 1));
                mList.add(client);
                mExecutor.execute(client);
            }

            Intent broadcast = new Intent(Config.ACTION_CLIENT_STARTED);
            sendBroadcast(broadcast);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutor.shutdown();
        for (Client client : mList) {
            client.quit();
        }
        mList.clear();

        Intent broadcast = new Intent(Config.ACTION_CLIENT_STOPPED);
        sendBroadcast(broadcast);
    }

    private class Client implements Runnable {
        private String mName;
        private boolean mQuit;

        public Client(String name) {
            mName = name;
            mQuit = false;
        }

        public void quit() {
            mQuit = true;
        }

        @Override
        public void run() {
            Socket socket = null;
            BufferedReader reader = null;
            BufferedWriter writer = null;

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(Config.SOCKET_IP, Config.SOCKET_PORT));
                //
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                //
                int i = 0;
                while ((i < 5) && !mQuit) {
                    writer.write(mName + ": " + "No." + (i+1) + " Heartbeat!\n");
                    writer.flush();
                    String str = reader.readLine();
                    if (str != null) {
                        Log.d(mName, str);
                    }
                    i++;
                    SystemClock.sleep(2000);
                }
                //
                writer.write("Bye-Bye\n");
                writer.flush();
            } catch (IOException e) {
                Log.e(mName, e.getMessage());
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    Log.e(mName, e.getMessage());
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(mName, e.getMessage());
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.e(mName, e.getMessage());
                }
            }

            Log.d(mName, "Client is completed.");
        }
    }
}