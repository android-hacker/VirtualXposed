package com.lody.virtual.client.hook.proxies.location;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

public class GPSStatusListenerThread extends TimerTask {
    private static GPSStatusListenerThread INSTANCE;
    private boolean isRunning = false;
    private Map<Object, Long> listeners = new HashMap<>();
    private Timer timer = new Timer();

    static {
        INSTANCE = new GPSStatusListenerThread();
    }

    public void addListenerTransport(Object transport) {
        if (!isRunning) {
            synchronized (this) {
                if (!isRunning) {
                    isRunning = true;
                    timer.schedule(this, 100, 800);
                }
            }
        }
        listeners.put(transport, System.currentTimeMillis());
    }

    public void removeListenerTransport(Object obj) {
        if (obj != null) {
            listeners.remove(obj);
        }
    }

    public void run() {
        if (!listeners.isEmpty()) {
            for (Entry entry : listeners.entrySet()) {
                try {
                    Object transport = entry.getKey();
                    MockLocationHelper.invokeSvStatusChanged(transport);
                    MockLocationHelper.invokeNmeaReceived(transport);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        timer.cancel();
    }

    public static GPSStatusListenerThread get() {
        return INSTANCE;
    }

    private GPSStatusListenerThread() {
    }
}