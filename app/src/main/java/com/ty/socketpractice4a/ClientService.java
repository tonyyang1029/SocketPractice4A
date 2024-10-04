package com.ty.socketpractice4a;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientService extends Service {
    private final String TAG = "ClientService";
    //
    private final int ACTION_CONNECT = 1;
    private final int ACTION_READ    = 2;
    private final int ACTION_WRITE   = 3;
    private final int ACTION_STOP    = 4;
    //
    private Socket mSocket;
    private SocketAddress mSocketAddr;
    private BufferedReader mReader;
    private BufferedWriter mWriter;
    //
    private ClientHandler mHandler;
    private HandlerThread mHandlerThread;
    //
    private int mCount;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mHandlerThread == null) {
            mCount = 0;
            //
            mHandlerThread = new HandlerThread("ClientHandlerThread");
            mHandlerThread.start();
            mHandler = new ClientHandler(this, mHandlerThread.getLooper());
            //
            mHandler.sendMessage(mHandler.obtainMessage(ACTION_CONNECT));
            //
            Log.d(TAG, "Service is started.");
            Intent broadcast = new Intent(Config.ACTION_CLIENT_STARTED);
            sendBroadcast(broadcast);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        reset();
        //
        Log.d(TAG, "Service is destroyed.");
        Intent broadcast = new Intent(Config.ACTION_CLIENT_STOPPED);
        sendBroadcast(broadcast);
    }

    private static class ClientHandler extends Handler {
        private final WeakReference<ClientService> mServiceReference;

        public ClientHandler(ClientService service, Looper looper) {
            super(looper);
            mServiceReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            ClientService service = mServiceReference.get();

            if (msg.what == service.ACTION_CONNECT) {
                try {
                    service.mSocket = new Socket();
                    service.mSocketAddr = new InetSocketAddress(Config.SOCKET_IP, Config.SOCKET_PORT);
                    service.mSocket.connect(service.mSocketAddr);
                    //
                    service.mReader = new BufferedReader(new InputStreamReader(service.mSocket.getInputStream()));
                    service.mWriter = new BufferedWriter(new OutputStreamWriter(service.mSocket.getOutputStream()));
                    //
                    service.mWriter.write("Greeting from client!\n");
                    service.mWriter.flush();
                    //
                    String str = service.mReader.readLine();
                    Log.d(service.TAG, str);
                    //
                    service.mHandler.sendMessageDelayed(service.mHandler.obtainMessage(service.ACTION_WRITE), 2000);
                } catch (IOException e) {
                    Log.e(service.TAG, e.getMessage());
                }
            } else if (msg.what == service.ACTION_WRITE) {
                service.mCount++;
                //
                if (!service.mSocket.isClosed() && service.mSocket.isConnected() && !service.mSocket.isOutputShutdown()) {
                    try {
                        if (service.mCount > 20) {
                            service.mWriter.write("Bye-Bye\n");
                            service.mWriter.flush();
                            service.mHandler.sendMessageDelayed(service.mHandler.obtainMessage(service.ACTION_STOP), 1000);
                        } else {
                            service.mWriter.write("Client Heartbeat [" + service.mCount + "]\n");
                            service.mWriter.flush();
                            service.mHandler.sendMessage(service.mHandler.obtainMessage(service.ACTION_READ));
                        }
                    } catch (IOException e) {
                        Log.e(service.TAG, e.getMessage());
                    }
                }
            } else if (msg.what == service.ACTION_READ) {
                if (!service.mSocket.isClosed() && service.mSocket.isConnected() && !service.mSocket.isInputShutdown()) {
                    try {
                        String str = service.mReader.readLine();
                        if (str != null) {
                            Log.d(service.TAG, str);
                        }
                        service.mHandler.sendMessageDelayed(service.mHandler.obtainMessage(service.ACTION_WRITE), 2000);
                    } catch (IOException e) {
                        Log.e(service.TAG, e.getMessage());
                    }
                }
            } else if (msg.what == service.ACTION_STOP) {
                service.stopSelf();
            }
        }
    }

    private void reset() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
            mHandlerThread = null;
        }

        try {
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
            //
            if (mReader != null) {
                mReader.close();
                mReader = null;
            }
            //
            if (mWriter != null) {
                mWriter.close();
                mWriter = null;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void showError(String error) {
        Intent intent = new Intent();
        intent.setAction(Config.ACTION_CLIENT_ERROR);
        intent.putExtra("error", error);
        sendBroadcast(intent);
    }
}