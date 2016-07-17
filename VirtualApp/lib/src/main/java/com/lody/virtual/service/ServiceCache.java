package com.lody.virtual.service;

import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 */

public class ServiceCache {

    private static final Map<String, IBinder> sCache = new HashMap<String, IBinder>(5);

    public static void addService(String name, IBinder service) {
        sCache.put(name, service);
    }

    public static IBinder removeService(String name) {
        return sCache.remove(name);
    }

    public static IBinder getService(String name) {
        return sCache.get(name);
    }


}
