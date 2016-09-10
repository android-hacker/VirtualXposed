package com.lc.interceptor.service.providers;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.text.TextUtils;
import android.util.Log;

import com.lc.interceptor.service.providers.base.InterceptorDataProvider;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.patchs.location.LocationManagerPatch;
import com.lody.virtual.helper.utils.Reflect;

import java.util.HashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import mirror.android.location.ILocationListener;
import mirror.android.location.LocationRequestL;

/**
 * @author legency
 */
public class LocationManagerProvider extends InterceptorDataProvider {

    private static final int TYPE_LOCATION_CHANGED = 1;
    public static final String TAG = "P_L_M";
    public static final String TAG2 = "P_L_M_ThreadPool";

    private static HashMap<IBinder, LocationRunnable> listeners;
    private static ThreadPoolExecutor fixedThreadPool;


    @Override
    public Class<? extends PatchDelegate> getDelegatePatch() {
        return LocationManagerPatch.class;
    }

    /**
     * @param args
     * @see com.lc.interceptor.client.hook.patch.interceptor.location.Interceptor_RequestLocationUpdates
     */
    public void requestLocationUpdates(Object[] args) {
        if (args.length < 2 || !(args[1] instanceof IBinder)) {
            return;
        }
        final IInterface i = ILocationListener.Stub.asInterface.call(args[1]);
        IBinder binder = i.asBinder();
        Object locationObject = args[0];
        String provider = null;

        if (LocationRequestL.TYPE.isInstance(locationObject)) {
            provider = LocationRequestL.getProvider.call(locationObject);
        }
        final Location fakeLocation = createFakeLocation(provider);

        final Object iLocationListener = args[1];
        if (listeners == null) {
            listeners = new HashMap<>();
        }
        if (listeners.containsKey(binder)) {
            Log.e(TAG, "listener exist");
            return;
        }
        LocationRunnable thread = new LocationRunnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //fixme 该方法 存在 线程杀不死的问题
                    try {
                        ILocationListener.onLocationChanged.call(i, fakeLocation);
                    } catch (Exception e) {
                        Log.e("LocationProvider", "send location failed kill thread", e);
                        running = false;
                    }
                    Log.d(TAG, "thread running pool size :" + fixedThreadPool.getPoolSize() + "index" + index);
                }
            }
        };
        listeners.put(binder, thread);
        if (fixedThreadPool == null) {
            fixedThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>());
        }
        thread.index = fixedThreadPool.getPoolSize();
        fixedThreadPool.submit(thread);
        Log.d(TAG2, "new thread added pool size :" + fixedThreadPool.getPoolSize() + " hash :" + iLocationListener.hashCode());
    }

    /**
     * @param args
     * @see com.lc.interceptor.client.hook.patch.interceptor.location.Interceptor_RemoveUpdates
     */
    public void removeUpdates(Object[] args) {

        if (args.length <= 2 || !(args[0] instanceof IBinder)) {
            return;
        }
        Object iLocationListener = args[0];
        final IInterface i = ILocationListener.Stub.asInterface.call(iLocationListener);
        IBinder binder = i.asBinder();
        if (listeners != null) {
            LocationRunnable a = listeners.remove(binder);
            if (a != null) {
                a.running = false;
                Log.d(TAG2, "remove location succeed hash:" + iLocationListener.hashCode());
            } else {
                Log.e(TAG, "remove location target listener not found ");
            }
            fixedThreadPool.remove(a);

        } else {
            Log.e(TAG, "remove location failed listeners is empty");
        }
    }


    static abstract class LocationRunnable implements Runnable {
        boolean running = true;
        public int index;
    }

    /**
     * @param provider
     * @return
     * @see LocationManager#GPS_PROVIDER;
     * @see LocationManager#NETWORK_PROVIDER;
     * @see LocationManager#PASSIVE_PROVIDER;
     */
    private static Location createFakeLocation(String provider) {

        if (TextUtils.isEmpty(provider)) {
            provider = LocationManager.GPS_PROVIDER;
        }
        Location location = new Location(provider);

        //测试经纬度 天安门
        location.setLongitude(116.403958);
        location.setLatitude(39.915049);

        location.setAccuracy(32.0F);
        location.setAltitude(0.0);
        location.setBearing(0.0F);
        Bundle bundle = new Bundle();
        bundle.putInt("satellites", 5);
        location.setExtras(bundle);
        Reflect.on(location).call("setIsFromMockProvider", false);
        location.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(317611316791260L);
        }
        return location;
    }
}
