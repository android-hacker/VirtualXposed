package com.lody.virtual.client.hook.patchs.power;

import android.content.Context;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.base.ReplaceSequencePkgHook;
import com.lody.virtual.client.hook.base.ResultStaticHook;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mirror.android.os.IPowerManager;

/**
 * @author Lody
 */
public class PowerManagerPatch extends PatchBinderDelegate {

	public PowerManagerPatch() {
		super(IPowerManager.Stub.TYPE, Context.POWER_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ReplaceSequencePkgHook("acquireWakeLock", 2) {
			@Override
			public Object call(Object who, Method method, Object... args) throws Throwable {
				try {
					return super.call(who, method, args);
				} catch (InvocationTargetException e) {
					return onHandleError(e);
				}
			}
		});
		addHook(new ReplaceLastPkgHook("acquireWakeLockWithUid") {

			@Override
			public Object call(Object who, Method method, Object... args) throws Throwable {
				try {
					return super.call(who, method, args);
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
}
