package com.lody.virtual.client.hook.patchs.location;

import com.lody.virtual.client.hook.base.HookBinder;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookLocationBinder;

import android.content.Context;
import android.location.ILocationManager;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 * @see ILocationManager
 */
@Patch({Hook_AddGpsMeasurementsListener.class, Hook_AddGpsNavigationMessageListener.class,
		Hook_AddGpsStatusListener.class, Hook_AddTestProvider.class, Hook_ClearTestProviderEnabled.class,
		Hook_ClearTestProviderLocation.class, Hook_ClearTestProviderStatus.class, Hook_GetLastLocation.class,
		Hook_RemoveGeofence.class, Hook_RemoveTestProvider.class, Hook_RemoveUpdates.class, Hook_RequestGeofence.class,
		Hook_RequestLocationUpdates.class, Hook_SetTestProviderEnabled.class, Hook_SetTestProviderLocation.class,
		Hook_SetTestProviderStatus.class

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
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.LOCATION_SERVICE) != getHookObject();
	}
}
