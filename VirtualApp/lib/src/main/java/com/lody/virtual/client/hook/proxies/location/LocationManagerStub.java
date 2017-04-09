package com.lody.virtual.client.hook.proxies.location;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;

import java.lang.reflect.Method;

import mirror.android.location.ILocationManager;
import mirror.android.location.LocationRequestL;

/**
 * @author Lody
 *
 * @see android.location.LocationManager
 */
public class LocationManagerStub extends BinderInvocationProxy {
	public LocationManagerStub() {
		super(ILocationManager.Stub.asInterface, Context.LOCATION_SERVICE);
	}

	private static class BaseMethodProxy extends ReplaceLastPkgMethodProxy {

		public BaseMethodProxy(String name) {
			super(name);
		}
		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			if (args.length > 0) {
				Object request = args[0];
				if (LocationRequestL.mHideFromAppOps != null) {
					LocationRequestL.mHideFromAppOps.set(request, false);
				}
				if (LocationRequestL.mWorkSource != null) {
					LocationRequestL.mWorkSource.set(request, null);
				}
			}
			return super.call(who, method, args);
		}
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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			addMethodProxy(new ReplaceLastPkgMethodProxy("addGpsStatusListener"));
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			addMethodProxy(new BaseMethodProxy("requestLocationUpdates"));
			addMethodProxy(new ReplaceLastPkgMethodProxy("removeUpdates"));
			addMethodProxy(new ReplaceLastPkgMethodProxy("requestGeofence"));
			addMethodProxy(new ReplaceLastPkgMethodProxy("removeGeofence"));
			addMethodProxy(new BaseMethodProxy("getLastLocation"));
		}

		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN
				&& TextUtils.equals(Build.VERSION.RELEASE, "4.1.2")) {
			addMethodProxy(new ReplaceLastPkgMethodProxy("requestLocationUpdates"));
			addMethodProxy(new ReplaceLastPkgMethodProxy("requestLocationUpdatesPI"));
			addMethodProxy(new ReplaceLastPkgMethodProxy("removeUpdates"));
			addMethodProxy(new ReplaceLastPkgMethodProxy("removeUpdatesPI"));
			addMethodProxy(new ReplaceLastPkgMethodProxy("addProximityAlert"));
			addMethodProxy(new ReplaceLastPkgMethodProxy("getLastKnownLocation"));
		}
	}
}
