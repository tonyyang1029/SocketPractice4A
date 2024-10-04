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
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Objects;

public class ServerService extends Service {
    private final String TAG = "ServerService";
    private ServerConnection mServerConnection;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mServerConnection == null) {
            mServerConnection = new ServerConnection();
            Thread serverThread = new Thread(mServerConnection);
            serverThread.start();
            //
            Log.d(TAG, "Service is started.");
            Intent broadcast = new Intent(Config.ACTION_SERVER_STARTED);
            sendBroadcast(broadcast);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServerConnection != null) {
            mServerConnection.reset();
            mServerConnection = null;
        }
        //
        Log.d(TAG, "Server Service is destroyed.");
        Intent broadcast = new Intent(Config.ACTION_SERVER_STOPPED);
        sendBroadcast(broadcast);
    }

    private class ServerConnection implements Runnable {
        private ServerSocket mServerSocket;
        private ClientConnection mClientConnection;

        @Override
        public void run() {
            try {
                mServerSocket = new ServerSocket();
                mServerSocket.bind(new InetSocketAddress(Config.SOCKET_IP, Config.SOCKET_PORT));
                Socket clientSocket = mServerSocket.accept();
                mClientConnection = new ClientConnection(clientSocket);
                Thread clientThread = new Thread(mClientConnection);
                clientThread.start();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        public void reset() {
            if (mServerSocket != null) {
                try {
                    mServerSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            mServerSocket = null;

            if (mClientConnection != null) {
                mClientConnection.reset();
            }
            mClientConnection = null;
        }
    }

    private class ClientConnection implements Runnable {
        private Socket mClient;
        private BufferedWriter mWriter;
        private BufferedReader mReader;

        public ClientConnection(Socket client) {
            try {
                mClient = client;
                mReader = new BufferedReader(new InputStreamReader(mClient.getInputStream()));
                mWriter = new BufferedWriter(new OutputStreamWriter(mClient.getOutputStream()));
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        public void reset() {
            try {
                if (mWriter != null) {
                    mWriter.close();
                    mWriter = null;
                }

                if (mReader != null) {
                    mReader.close();
                    mReader = null;
                }

                if (mClient != null) {
                    mClient.close();
                    mClient = null;
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        @Override
        public void run() {
            while (true) {
                if (mClient.isConnected() && !mClient.isClosed() && !mClient.isInputShutdown() && !mClient.isOutputShutdown()) {
                    try {
                        String str = mReader.readLine();
                        if (str != null) {
                            Log.d(TAG, str);
                            if ("Bye-Bye".equals(str)) {
                                stopSelf();
                                break;
                            } else {
                                mWriter.write("ACK to " + str + "\n");
                                mWriter.flush();
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }
    }
}