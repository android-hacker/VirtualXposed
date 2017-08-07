package com.lody.virtual.client.hook.proxies.location;

import android.location.ILocationListener;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;

import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;
import com.lody.virtual.client.ipc.VirtualLocationManager;
import com.lody.virtual.client.stub.VASettings;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.SchedulerTask;
import com.lody.virtual.remote.vloc.VLocation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import mirror.android.location.LocationRequestL;

/**
 * @author Lody
 */
@SuppressWarnings("ALL")
public class MethodProxies {

    private static Map<IBinder, UpdateLocationTask> tasks = new HashMap<>();

    private static void fixLocationRequest(LocationRequest request) {
        if (request != null) {
            if (LocationRequestL.mHideFromAppOps != null) {
                LocationRequestL.mHideFromAppOps.set(request, false);
            }
            if (LocationRequestL.mWorkSource != null) {
                LocationRequestL.mWorkSource.set(request, null);
            }
        }
    }

    private static class AddGpsStatusListener extends ReplaceLastPkgMethodProxy {

        public AddGpsStatusListener() {
            super("addGpsStatusListener");
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                return false;
            }
            return super.call(who, method, args);
        }
    }

    private static class UpdateLocationTask extends SchedulerTask {

        private ILocationListener listener;

        public UpdateLocationTask(ILocationListener listener) {
            super(VirtualRuntime.getUIHandler(), VASettings.LOCATION_UPDATE_PERIOD);
            this.listener = listener;
        }

        @Override
        public void run() {
            final VLocation vLocation = VirtualLocationManager.get().getLocation(MethodProxy.getAppUserId(), MethodProxy.getAppPkg());
            Location location = createLocation(vLocation);
        }
    }

    private static class RequestLocationUpdates extends ReplaceLastPkgMethodProxy {

        public RequestLocationUpdates() {
            super("requestLocationUpdates");
        }

        @Override
        public Object call(final Object who, Method method, Object... args) throws Throwable {
            LocationRequest request = (LocationRequest) args[0];
            fixLocationRequest(request);
            if (isFakeLocationEnable()) {
                final ILocationListener listener = (ILocationListener) args[1];
                UpdateLocationTask task = new UpdateLocationTask(listener);
                tasks.put(listener.asBinder(), task);
                task.schedule();
                return 0;
            }
            return super.call(who, method, args);
        }
    }

    private static class RemoveUpdates extends ReplaceLastPkgMethodProxy {

        public RemoveUpdates() {
            super("removeUpdates");
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                IBinder binder = (IBinder) args[0];
                UpdateLocationTask task = tasks.get(binder);
                if (task != null) {
                    task.cancel();
                }
                return 0;
            }
            return super.call(who, method, args);
        }
    }

    private static class GetLastLocation extends ReplaceLastPkgMethodProxy {

        public GetLastLocation() {
            super("getLastLocation");
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                LocationRequest request = (LocationRequest) args[0];
                fixLocationRequest(request);
                VLocation loc = VirtualLocationManager.get().getLocation(getAppUserId(), getAppPkg());
                if (loc != null) {
                    return createLocation(loc);
                } else {
                    return null;
                }
            }
            return super.call(who, method, args);
        }
    }

    private static class GetLastKnownLocation extends GetLastLocation {
        @Override
        public String getMethodName() {
            return "getLastKnownLocation";
        }
    }

    private static class IsProviderEnabled extends MethodProxy {
        @Override
        public String getMethodName() {
            return "isProviderEnabled";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                String provider = (String) args[0];
                if (LocationManager.GPS_PROVIDER.equals(provider)) {
                    return true;
                }
            }
            return super.call(who, method, args);
        }
    }

    private static class getAllProviders extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getAllProviders";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                return Arrays.asList(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER);
            }
            return super.call(who, method, args);
        }
    }

    private static class GetBestProvider extends MethodProxy {
        @Override
        public String getMethodName() {
            return "getBestProvider";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                return LocationManager.NETWORK_PROVIDER;
            }
            return super.call(who, method, args);
        }
    }


    public static Location createLocation(VLocation loc) {
        Location sysLoc = new Location(LocationManager.GPS_PROVIDER);
        if (loc.accuracy == 0f) {
            sysLoc.setAccuracy(8f);
        } else {
            sysLoc.setAccuracy(loc.accuracy);
        }
        Bundle extraBundle = new Bundle();
        extraBundle.putInt("satellites", 23);
        sysLoc.setBearing(loc.bearing);
        Reflect.on(sysLoc).call("setIsFromMockProvider", false);
        sysLoc.setLatitude(loc.latitude);
        sysLoc.setLongitude(loc.longitude);
        sysLoc.setSpeed(loc.speed);
        sysLoc.setTime(System.currentTimeMillis());
        sysLoc.setExtras(extraBundle);
        Reflect.on(sysLoc).call("setElapsedRealtimeNanos", SystemClock.elapsedRealtime());
        return sysLoc;
    }
}
