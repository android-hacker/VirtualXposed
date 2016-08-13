package com.lody.virtual.client.hook.patchs.location;

import android.content.Context;
import android.location.ILocationManager;
import android.location.LocationRequest;
import android.os.Build;
import android.os.ServiceManager;
import android.text.TextUtils;

import com.lody.virtual.client.hook.base.HookBinder;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.binders.HookLocationBinder;
import com.lody.virtual.helper.utils.ArrayUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see ILocationManager
 */
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

	private static class BaseHook extends ReplaceLastPkgHook {

		public BaseHook(String name) {
			super(name);
		}
		@Override
		public Object onHook(Object who, Method method, Object... args) throws Throwable {
			LocationRequest request = ArrayUtils.getFirst(args, LocationRequest.class);
			if (request != null) {
				try {
					request.setWorkSource(null);
					request.setHideFromAppOps(false);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			return super.onHook(who, method, args);
		}
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
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

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.LOCATION_SERVICE) != getHookObject();
	}
}
