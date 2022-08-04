package com.lody.virtual.client.hook.proxies.location;

import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Build;

import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;
import com.lody.virtual.client.ipc.VirtualLocationManager;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.remote.vloc.VLocation;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import mirror.android.location.LocationRequestL;

/**
 * @author Lody
 */
@SuppressWarnings("ALL")
public class MethodProxies {

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

    static class AddGpsStatusListener extends ReplaceLastPkgMethodProxy {

        public AddGpsStatusListener() {
            super("addGpsStatusListener");
        }

        public AddGpsStatusListener(String name) {
            super(name);
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                Object transport = ArrayUtils.getFirst(args, mirror.android.location.LocationManager.GpsStatusListenerTransport.TYPE);
                Object locationManager = mirror.android.location.LocationManager.GpsStatusListenerTransport.this$0.get(transport);
                mirror.android.location.LocationManager.GpsStatusListenerTransport.onGpsStarted.call(transport);
                mirror.android.location.LocationManager.GpsStatusListenerTransport.onFirstFix.call(transport, 0);
                if (mirror.android.location.LocationManager.GpsStatusListenerTransport.mListener.get(transport) != null) {
                    MockLocationHelper.invokeSvStatusChanged(transport);
                } else {
                    MockLocationHelper.invokeNmeaReceived(transport);
                }
                GPSListenerThread.get().addListenerTransport(locationManager);
                return true;
            }
            return super.call(who, method, args);
        }
    }

    static class RequestLocationUpdates extends ReplaceLastPkgMethodProxy {

        public RequestLocationUpdates() {
            super("requestLocationUpdates");
        }

        @Override
        public Object call(final Object who, Method method, Object... args) throws Throwable {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                LocationRequest request = (LocationRequest) args[0];
                fixLocationRequest(request);
            }
            if (isFakeLocationEnable()) {
                Object transport = ArrayUtils.getFirst(args, mirror.android.location.LocationManager.ListenerTransport.TYPE);
                if (transport != null) {
                    Object locationManager = mirror.android.location.LocationManager.ListenerTransport.this$0.get(transport);
                    MockLocationHelper.setGpsStatus(locationManager);
                    GPSListenerThread.get().addListenerTransport(locationManager);
                }
                return 0;
            }
            return super.call(who, method, args);
        }
    }

    static class RemoveUpdates extends ReplaceLastPkgMethodProxy {

        public RemoveUpdates() {
            super("removeUpdates");
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                // TODO
                return 0;
            }
            return super.call(who, method, args);
        }
    }

    static class GetLastLocation extends ReplaceLastPkgMethodProxy {

        public GetLastLocation() {
            super("getLastLocation");
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (!(args[0] instanceof String)) {
                LocationRequest request = (LocationRequest) args[0];
                fixLocationRequest(request);
            }
            if (isFakeLocationEnable()) {
                VLocation loc = VirtualLocationManager.get().getLocation();
                if (loc != null) {
                    return loc.toSysLocation();
                } else {
                    return null;
                }
            }
            return super.call(who, method, args);
        }
    }

    static class GetLastKnownLocation extends GetLastLocation {
        @Override
        public String getMethodName() {
            return "getLastKnownLocation";
        }
    }

    static class getProviders extends MethodProxy {

        static List PROVIDERS = Arrays.asList(
                LocationManager.GPS_PROVIDER,
                LocationManager.PASSIVE_PROVIDER,
                LocationManager.NETWORK_PROVIDER
        );

        @Override
        public String getMethodName() {
            return "getProviders";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            return PROVIDERS;
        }
    }

    static class IsProviderEnabled extends MethodProxy {
        @Override
        public String getMethodName() {
            return "isProviderEnabled";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                String provider = (String) args[0];
                if (LocationManager.PASSIVE_PROVIDER.equals(provider)) {
                    return true;
                }
                if (LocationManager.GPS_PROVIDER.equals(provider)) {
                    return true;
                }
                if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
                    return true;
                }
                return false;

            }
            return super.call(who, method, args);
        }
    }

    static class getAllProviders extends getProviders {

        @Override
        public String getMethodName() {
            return "getAllProviders";
        }
    }

    static class GetBestProvider extends MethodProxy {
        @Override
        public String getMethodName() {
            return "getBestProvider";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                return LocationManager.GPS_PROVIDER;
            }
            return super.call(who, method, args);
        }
    }


    static class RemoveGpsStatusListener extends ReplaceLastPkgMethodProxy {
        public RemoveGpsStatusListener() {
            super("removeGpsStatusListener");
        }

        public RemoveGpsStatusListener(String name) {
            super(name);
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                return 0;
            }
            return super.call(who, method, args);
        }
    }

    static class sendExtraCommand extends MethodProxy {

        @Override
        public String getMethodName() {
            return "sendExtraCommand";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                return true;
            }
            return super.call(who, method, args);
        }
    }


    static class UnregisterGnssStatusCallback extends RemoveGpsStatusListener {
        public UnregisterGnssStatusCallback() {
            super("unregisterGnssStatusCallback");
        }
    }

    static class RegisterGnssStatusCallback extends MethodProxy {

        @Override
        public String getMethodName() {
            return "registerGnssStatusCallback";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (!isFakeLocationEnable()) {
                return super.call(who, method, args);
            }
            Object transport = ArrayUtils.getFirst(args, mirror.android.location.LocationManager.GnssStatusListenerTransport.TYPE);
            if (transport != null) {
                mirror.android.location.LocationManager.GnssStatusListenerTransport.onGnssStarted.call(transport, new Object[0]);
                if (mirror.android.location.LocationManager.GnssStatusListenerTransport.mGpsListener.get(transport) != null) {
                    MockLocationHelper.invokeSvStatusChanged(transport);
                } else {
                    MockLocationHelper.invokeNmeaReceived(transport);
                }
                mirror.android.location.LocationManager.GnssStatusListenerTransport.onFirstFix.call(transport, Integer.valueOf(0));
                Object locationManager = mirror.android.location.LocationManager.GnssStatusListenerTransport.this$0.get(transport);
                GPSListenerThread.get().addListenerTransport(locationManager);
            }
            return true;
        }
    }

    static class getProviderProperties extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getProviderProperties";
        }

        @Override
        public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
            if (!isFakeLocationEnable()) {
                return super.afterCall(who, method, args, result);
            }
            try {
                Reflect.on(result).set("mRequiresNetwork", false);
                Reflect.on(result).set("mRequiresCell", false);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    static class locationCallbackFinished extends MethodProxy {

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (isFakeLocationEnable()) {
                return true;
            }
            return super.call(who, method, args);
        }

        @Override
        public String getMethodName() {
            return "locationCallbackFinished";
        }
    }

}
