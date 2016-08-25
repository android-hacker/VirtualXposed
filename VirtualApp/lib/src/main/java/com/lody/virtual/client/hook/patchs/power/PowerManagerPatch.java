package com.lody.virtual.client.hook.patchs.power;

import android.content.Context;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.base.ReplaceSequencePkgHook;
import com.lody.virtual.client.hook.base.ResultStaticHook;
import com.lody.virtual.client.hook.binders.PowerBinderDelegate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class PowerManagerPatch extends PatchDelegate<PowerBinderDelegate> {

	@Override
	protected PowerBinderDelegate createHookDelegate() {
		return new PowerBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.POWER_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ReplaceSequencePkgHook("acquireWakeLock", 2) {
			@Override
			public Object onHook(Object who, Method method, Object... args) throws Throwable {
				try {
					return super.onHook(who, method, args);
				} catch (InvocationTargetException e) {
					return onHandleError(e);
				}
			}
		});
		addHook(new ReplaceLastPkgHook("acquireWakeLockWithUid") {

			@Override
			public Object onHook(Object who, Method method, Object... args) throws Throwable {
				try {
					return super.onHook(who, method, args);
				} catch (InvocationTargetException e) {
					return onHandleError(e);
				}
			}
		});
		addHook(new ResultStaticHook("updateWakeLockWorkSource", 0));
	}

	private Object onHandleError(InvocationTargetException e) throws Throwable {
		if (e.getCause() instanceof SecurityException) {
			return 0;
		}
		throw e.getCause();
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService.call(Context.POWER_SERVICE) != getHookDelegate();
	}
}
