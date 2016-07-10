package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.content.Context;
import android.location.ILocationManager;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class HookLocationBinder extends HookBinder<ILocationManager> {
	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.LOCATION_SERVICE);
	}

	@Override
	protected ILocationManager createInterface(IBinder baseBinder) {
		return ILocationManager.Stub.asInterface(baseBinder);
	}
}
