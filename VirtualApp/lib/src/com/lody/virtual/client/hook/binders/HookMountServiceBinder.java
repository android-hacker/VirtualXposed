package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.os.IBinder;
import android.os.ServiceManager;
import android.os.storage.IMountService;

/**
 * @author Lody
 *
 */
public class HookMountServiceBinder extends HookBinder<IMountService> {

	public static final String SERVICE_NAME = "mount";
	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(SERVICE_NAME);
	}

	@Override
	protected IMountService createInterface(IBinder baseBinder) {
		return IMountService.Stub.asInterface(baseBinder);
	}
}
