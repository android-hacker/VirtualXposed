package com.lody.virtual.client.hook.patchs.power;

import com.lody.virtual.client.hook.base.HookBinder;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookPowerBinder;

import android.content.Context;
import android.os.IPowerManager;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 * @see IPowerManager
 */
@Patch({Hook_AcquireWakeLock.class, Hook_AcquireWakeLockWithUid.class,})
public class PowerManagerPatch extends PatchObject<IPowerManager, HookPowerBinder> {

	@Override
	protected HookPowerBinder initHookObject() {
		return new HookPowerBinder();
	}

	@Override
	public void inject() throws Throwable {
		HookBinder<IPowerManager> hookBinder = getHookObject();
		hookBinder.injectService(Context.POWER_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.POWER_SERVICE) != getHookObject();
	}
}
