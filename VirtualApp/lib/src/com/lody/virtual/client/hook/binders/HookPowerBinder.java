package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.content.Context;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 * @see IPowerManager
 */
public class HookPowerBinder extends HookBinder<IPowerManager> {

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.POWER_SERVICE);
	}

	@Override
	protected IPowerManager createInterface(IBinder baseBinder) {
		return IPowerManager.Stub.asInterface(baseBinder);
	}
}
