package com.lody.virtual.client.hook.proxies.location;

import android.location.Location;
import android.os.Build.VERSION;
import android.os.Handler;

import com.lody.virtual.client.ipc.VirtualLocationManager;
import com.lody.virtual.remote.vloc.VLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import mirror.android.location.LocationManager;


public class GPSListenerThread extends TimerTask {
    private static GPSListenerThread INSTANCE;
    private Handler handler = new Handler();
    private boolean isRunning = false;
    private HashMap<Object, Long> listeners = new HashMap<>();
    private Timer timer = new Timer();

    static {
        INSTANCE = new GPSListenerThread();
    }

    private void notifyGPSStatus(Map listeners) {
        if (listeners != null && !listeners.isEmpty()) {
            //noinspection unchecked
            Set<Map.Entry> entries = listeners.entrySet();
            for (Map.Entry entry : entries) {
                try {
                    Object value = entry.getValue();
                    if (value != null) {
                        MockLocationHelper.invokeSvStatusChanged(value);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void notifyLocation(Map listeners) {
        if (listeners != null) {
            try {
                if (!listeners.isEmpty()) {
                    VLocation vLocation = VirtualLocationManager.get().getLocation();
                    if (vLocation != null) {
                        Location location = vLocation.toSysLocation();
                        //noinspection unchecked
                        Set<Map.Entry> entries = listeners.entrySet();
                        for (Map.Entry entry : entries) {
                            Object value = entry.getValue();
                            if (value != null) {
                                try {
                                    LocationManager.ListenerTransport.onLocationChanged.call(value, location);
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyMNmeaListener(Map listeners) {
        if (listeners != null && !listeners.isEmpty()) {
            //noinspection unchecked
            Set<Map.Entry> entries = listeners.entrySet();
            for (Map.Entry entry : entries) {
                try {
                    Object value = entry.getValue();
                    if (value != null) {
                        MockLocationHelper.invokeNmeaReceived(value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addListenerTransport(Object transport) {
        this.listeners.put(transport, System.currentTimeMillis());
        if (!isRunning) {
            synchronized (this) {
                if (!isRunning) {
                    isRunning = true;
                    timer.schedule(this, 1000, 1000);
                }
            }
        }
    }

    public void removeListenerTransport(Object transport) {
        if (transport != null) {
            listeners.remove(transport);
        }
    }

    public void run() {
        if (!listeners.isEmpty()) {
            if (VirtualLocationManager.get().getMode() == VirtualLocationManager.MODE_CLOSE) {
                listeners.clear();
                return;
            }
            for (Map.Entry entry : this.listeners.entrySet()) {
                try {
                    Object transport = entry.getKey();
                    Map gpsStatusListeners;
                    if (VERSION.SDK_INT >= 24) {
                        Map nmeaListeners = LocationManager.mGnssNmeaListeners.get(transport);
                        notifyGPSStatus(LocationManager.mGnssStatusListeners.get(transport));
                        notifyMNmeaListener(nmeaListeners);
                        gpsStatusListeners = LocationManager.mGpsStatusListeners.get(transport);
                        notifyGPSStatus(gpsStatusListeners);
                        notifyMNmeaListener(LocationManager.mGpsNmeaListeners.get(transport));
                    } else {
                        gpsStatusListeners = LocationManager.mGpsStatusListeners.get(transport);
                        notifyGPSStatus(gpsStatusListeners);
                        notifyMNmeaListener(LocationManager.mNmeaListeners.get(transport));
                    }
                    final Map listeners = LocationManager.mListeners.get(transport);
                    if (gpsStatusListeners != null && !gpsStatusListeners.isEmpty()) {
                        if (listeners == null || listeners.isEmpty()) {
                            // listeners not ready
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    GPSListenerThread.this.notifyLocation(listeners);
                                }
                            }, 100);
                        } else {
                            notifyLocation(listeners);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        this.timer.cancel();
    }

    public static GPSListenerThread get() {
        return INSTANCE;
    }

    private GPSListenerThread() {
    }
}