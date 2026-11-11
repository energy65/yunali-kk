package com.fongmi.android.tv.server;

import android.util.Log;

import com.fongmi.android.tv.service.PlaybackService;
import com.fongmi.android.tv.utils.Task;
import com.github.catvod.Proxy;
import com.github.catvod.utils.Util;

public class Server {

    private static final String TAG = "Server";

    private volatile PlaybackService service;
    private volatile Nano nano;

    private static class Loader {
        static volatile Server INSTANCE = new Server();
    }

    public static Server get() {
        return Loader.INSTANCE;
    }

    public PlaybackService getService() {
        return service;
    }

    public void setService(PlaybackService service) {
        this.service = service;
    }

    public String getAddress() {
        return getAddress(false);
    }

    public String getAddress(int tab) {
        return getAddress(false) + "?tab=" + tab;
    }

    public String getAddress(String path) {
        return getAddress(true) + path;
    }

    public String getAddress(boolean local) {
        return "http://" + (local ? "127.0.0.1" : Util.getIp()) + ":" + Proxy.getPort();
    }

    public boolean isRunning() {
        return nano != null;
    }

    public synchronized boolean start() {
        if (nano != null) return true;
        for (int i = 9978; i < 9999; i++) {
            try {
                nano = new Nano(i);
                nano.start(500);
                Proxy.set(i);
                Log.i(TAG, "Server started on port " + i);
                return true;
            } catch (Throwable e) {
                Log.w(TAG, "Failed to start server on port " + i + ": " + e.getMessage());
                nano = null;
            }
        }
        Log.e(TAG, "Failed to start server on all ports");
        return false;
    }

    public void stop() {
        Task.execute(() -> {
            if (nano != null) nano.stop();
            service = null;
            nano = null;
        });
    }
}
