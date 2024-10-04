package com.ty.socketpractice4a;

public class Config {
    public static final String SOCKET_IP = "127.0.0.1";
    // public static final String SOCKET_IP = "192.168.1.45"; // Austin Wi-Fi
    public static final int SOCKET_PORT = 1234;

    public static final String ACTION_SERVER_STARTED = "com.ty.socketpractice4a.SERVER_STARTED";
    public static final String ACTION_SERVER_STOPPED = "com.ty.socketpractice4a.SERVER_STOPPED";
    public static final String ACTION_SERVER_ERROR   = "com.ty.socketpractice4a.SERVER_ERROR";

    public static final String ACTION_CLIENT_STARTED = "com.ty.socketpractice4a.CLIENT_STARTED";
    public static final String ACTION_CLIENT_STOPPED = "com.ty.socketpractice4a.CLIENT_STOPPED";
    public static final String ACTION_CLIENT_ERROR   = "com.ty.socketpractice4a.CLIENT_ERROR";
}
