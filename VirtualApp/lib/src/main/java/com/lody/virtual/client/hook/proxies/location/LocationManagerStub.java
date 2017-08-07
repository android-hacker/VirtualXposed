package com.lody.virtual.client.hook.proxies.location;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.Inject;
import com.lody.virtual.client.hook.base.LogInvocation;
import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;

import mirror.android.location.ILocationManager;

/**
 * @author Lody
 * @see android.location.LocationManager
 */
@LogInvocation(LogInvocation.Condition.ALWAYS)
@Inject(MethodProxies.class)
public class LocationManagerStub extends BinderInvocationProxy {
    public LocationManagerStub() {
        super(ILocationManager.Stub.asInterface, Context.LOCATION_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addMethodProxy(new ReplaceLastPkgMethodProxy("addTestProvider"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("removeTestProvider"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("setTestProviderLocation"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("clearTestProviderLocation"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("setTestProviderEnabled"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("clearTestProviderEnabled"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("setTestProviderStatus"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("clearTestProviderStatus"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addMethodProxy(new ReplaceLastPkgMethodProxy("addGpsMeasurementsListener"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("addGpsNavigationMessageListener"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            addMethodProxy(new ReplaceLastPkgMethodProxy("requestGeofence"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("removeGeofence"));
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN
                && TextUtils.equals(Build.VERSION.RELEASE, "4.1.2")) {
            addMethodProxy(new ReplaceLastPkgMethodProxy("requestLocationUpdatesPI"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("removeUpdatesPI"));
            addMethodProxy(new ReplaceLastPkgMethodProxy("addProximityAlert"));
        }
    }


}
