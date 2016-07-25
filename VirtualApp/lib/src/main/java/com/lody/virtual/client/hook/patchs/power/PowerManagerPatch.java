package com.lody.virtual.client.hook.patchs.power;

import android.content.Context;
import android.os.IPowerManager;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.HookBinder;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.base.ReplaceSequencePkgHook;
import com.lody.virtual.client.hook.binders.HookPowerBinder;

/**
 * @author Lody
 *
 *
 * @see IPowerManager
 */
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
	protected void applyHooks() {
		super.applyHooks();
		addHook(new ReplaceSequencePkgHook("acquireWakeLock", 2));
		addHook(new ReplaceLastPkgHook("acquireWakeLockWithUid"));
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.POWER_SERVICE) != getHookObject();
	}
}
