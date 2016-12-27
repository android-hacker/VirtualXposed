package com.lody.virtual.client.hook.patchs.location;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;

import java.lang.reflect.Method;

import mirror.android.location.ILocationManager;
import mirror.android.location.LocationRequestL;

/**
 * @author Lody
 *
 * @see android.location.LocationManager
 */
public class LocationManagerPatch extends PatchBinderDelegate {
	public LocationManagerPatch() {
		super(ILocationManager.Stub.TYPE, Context.LOCATION_SERVICE);
	}

	private static class BaseHook extends ReplaceLastPkgHook {

		public BaseHook(String name) {
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
	protected void onBindHooks() {
		super.onBindHooks();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			addHook(new ReplaceLastPkgHook("addTestProvider"));
			addHook(new ReplaceLastPkgHook("removeTestProvider"));
			addHook(new ReplaceLastPkgHook("setTestProviderLocation"));
			addHook(new ReplaceLastPkgHook("clearTestProviderLocation"));
			addHook(new ReplaceLastPkgHook("setTestProviderEnabled"));
			addHook(new ReplaceLastPkgHook("clearTestProviderEnabled"));
			addHook(new ReplaceLastPkgHook("setTestProviderStatus"));
			addHook(new ReplaceLastPkgHook("clearTestProviderStatus"));
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			addHook(new ReplaceLastPkgHook("addGpsMeasurementsListener"));
			addHook(new ReplaceLastPkgHook("addGpsNavigationMessageListener"));
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			addHook(new ReplaceLastPkgHook("addGpsStatusListener"));
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			addHook(new BaseHook("requestLocationUpdates"));
			addHook(new ReplaceLastPkgHook("removeUpdates"));
			addHook(new ReplaceLastPkgHook("requestGeofence"));
			addHook(new ReplaceLastPkgHook("removeGeofence"));
			addHook(new BaseHook("getLastLocation"));
		}

		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN
				&& TextUtils.equals(Build.VERSION.RELEASE, "4.1.2")) {
			addHook(new ReplaceLastPkgHook("requestLocationUpdates"));
			addHook(new ReplaceLastPkgHook("requestLocationUpdatesPI"));
			addHook(new ReplaceLastPkgHook("removeUpdates"));
			addHook(new ReplaceLastPkgHook("removeUpdatesPI"));
			addHook(new ReplaceLastPkgHook("addProximityAlert"));
			addHook(new ReplaceLastPkgHook("getLastKnownLocation"));
		}
	}
}
