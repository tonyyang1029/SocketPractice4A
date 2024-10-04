package com.ty.socketpractice4a;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerServicePro extends Service {
    private final String TAG = "ServerServicePro";
    private ServerConnection mServerConnection;

    public ServerServicePro() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mServerConnection == null) {
            mServerConnection = new ServerConnection();
            Thread thread = new Thread(mServerConnection);
            thread.start();
            Intent broadcast = new Intent(Config.ACTION_SERVER_STARTED);
            sendBroadcast(broadcast);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mServerConnection.quit();
        Intent broadcast = new Intent(Config.ACTION_SERVER_STOPPED);
        sendBroadcast(broadcast);
    }

    private class ServerConnection implements Runnable {
        private ServerSocket mSocket;
        private ArrayList<ClientConnection> mClients;
        private ExecutorService mExecutor;
        private boolean mQuit;
        private int mCount;

        public ServerConnection() {
            mClients = new ArrayList<>();
            mExecutor = Executors.newCachedThreadPool();
            mQuit = false;
            mCount = 0;
        }

        @Override
        public void run() {
            try {
                mSocket = new ServerSocket();
                mSocket.bind(new InetSocketAddress(Config.SOCKET_IP, Config.SOCKET_PORT));

                while (!mQuit) {
                    Socket client = mSocket.accept();
                    ClientConnection clientConnection = new ClientConnection(client, ++mCount);
                    mClients.add(clientConnection);
                    mExecutor.execute(clientConnection);
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        public void quit() {
            mQuit = true;
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            mExecutor.shutdown();
            for (ClientConnection client : mClients) {
                client.quit();
            }
            mClients.clear();

            Log.d(TAG, "Server Connection is completed.");
        }
    }

    private class ClientConnection implements Runnable {
        private Socket mSocket;
        private int mId;
        private BufferedReader mReader;
        private BufferedWriter mWriter;
        private boolean mQuit;

        public ClientConnection(Socket socket, int id) {
            mSocket =  socket;
            mId = id;
        }

        @Override
        public void run() {
            try {
                mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));

                while (!mQuit) {
                    if (mSocket.isConnected() && !mSocket.isClosed() && !mSocket.isInputShutdown()) {
                        String str = mReader.readLine();
                        if (str != null) {
                            Log.d(TAG, str);
                            if (str.equals("Bye-Bye")) {
                                mQuit = true;
                                Log.d(TAG, "#" + mId + " client connection is about to finish.");
                            } else {
                                mWriter.write("#" + mId + " ACK to " + str + "\n");
                                mWriter.flush();
                            }
                        }
                    }
                }

                quit();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        public void quit() {
            mQuit = true;

            try {
                if (mSocket != null) mSocket.close();
                if (mReader != null) mReader.close();
                if (mWriter != null) mWriter.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            mSocket = null;
            mReader = null;
            mWriter = null;
        }
    }
}