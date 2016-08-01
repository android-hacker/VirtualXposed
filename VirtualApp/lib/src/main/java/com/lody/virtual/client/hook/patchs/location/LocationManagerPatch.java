package com.lody.virtual.client.hook.patchs.location;

import android.content.Context;
import android.location.ILocationManager;
import android.os.Build;
import android.os.ServiceManager;
import android.text.TextUtils;

import com.lody.virtual.client.hook.base.HookBinder;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.binders.HookLocationBinder;

/**
 * @author Lody
 *
 *
 * @see ILocationManager
 */
@Patch({
		Hook_RequestLocationUpdates.class,
})
public class LocationManagerPatch extends PatchObject<ILocationManager, HookLocationBinder> {
	@Override
	protected HookLocationBinder initHookObject() {
		return new HookLocationBinder();
	}

	@Override
	public void inject() throws Throwable {
		HookBinder<ILocationManager> hookBinder = getHookObject();
		hookBinder.injectService(Context.LOCATION_SERVICE);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			addHook(new ReplaceLastPkgHook("removeUpdates"));
			addHook(new ReplaceLastPkgHook("requestGeofence"));
			addHook(new ReplaceLastPkgHook("removeGeofence"));
			addHook(new ReplaceLastPkgHook("getLastLocation"));
			addHook(new ReplaceLastPkgHook("addGpsStatusListener"));
			addHook(new ReplaceLastPkgHook("addGpsMeasurementsListener"));
			addHook(new ReplaceLastPkgHook("addGpsNavigationMessageListener"));
			addHook(new ReplaceLastPkgHook("addTestProvider"));
			addHook(new ReplaceLastPkgHook("removeTestProvider"));
			addHook(new ReplaceLastPkgHook("setTestProviderLocation"));
			addHook(new ReplaceLastPkgHook("clearTestProviderLocation"));
			addHook(new ReplaceLastPkgHook("setTestProviderEnabled"));
			addHook(new ReplaceLastPkgHook("clearTestProviderEnabled"));
			addHook(new ReplaceLastPkgHook("setTestProviderStatus"));
			addHook(new ReplaceLastPkgHook("clearTestProviderStatus"));
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			addHook(new ReplaceLastPkgHook("requestLocationUpdates"));
			addHook(new ReplaceLastPkgHook("removeUpdates"));
			addHook(new ReplaceLastPkgHook("requestGeofence"));
			addHook(new ReplaceLastPkgHook("removeGeofence"));
			addHook(new ReplaceLastPkgHook("getLastLocation"));
			addHook(new ReplaceLastPkgHook("addGpsStatusListener"));
			addHook(new ReplaceLastPkgHook("addGpsMeasurementsListener"));
			addHook(new ReplaceLastPkgHook("addGpsNavigationMessageListener"));
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			addHook(new ReplaceLastPkgHook("requestLocationUpdates"));
			addHook(new ReplaceLastPkgHook("removeUpdates"));
			addHook(new ReplaceLastPkgHook("requestGeofence"));
			addHook(new ReplaceLastPkgHook("removeGeofence"));
			addHook(new ReplaceLastPkgHook("getLastLocation"));
			addHook(new ReplaceLastPkgHook("addGpsStatusListener"));
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			addHook(new ReplaceLastPkgHook("requestLocationUpdates"));
			addHook(new ReplaceLastPkgHook("removeUpdates"));
			addHook(new ReplaceLastPkgHook("requestGeofence"));
			addHook(new ReplaceLastPkgHook("removeGeofence"));
			addHook(new ReplaceLastPkgHook("getLastLocation"));
		}

		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN && TextUtils.equals(Build.VERSION.RELEASE, "4.1.2")) {
			addHook(new ReplaceLastPkgHook("requestLocationUpdates"));
			addHook(new ReplaceLastPkgHook("requestLocationUpdatesPI"));
			addHook(new ReplaceLastPkgHook("removeUpdates"));
			addHook(new ReplaceLastPkgHook("removeUpdatesPI"));
			addHook(new ReplaceLastPkgHook("addProximityAlert"));
			addHook(new ReplaceLastPkgHook("getLastKnownLocation"));
		}
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.LOCATION_SERVICE) != getHookObject();
	}
}
