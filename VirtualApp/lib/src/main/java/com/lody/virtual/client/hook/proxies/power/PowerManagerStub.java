package com.lody.virtual.client.hook.proxies.power;

import android.content.Context;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;
import com.lody.virtual.client.hook.base.ReplaceSequencePkgMethodProxy;
import com.lody.virtual.client.hook.base.ResultStaticMethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mirror.android.os.IPowerManager;

/**
 * @author Lody
 */
public class PowerManagerStub extends BinderInvocationProxy {

	public PowerManagerStub() {
		super(IPowerManager.Stub.asInterface, Context.POWER_SERVICE);
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ReplaceSequencePkgMethodProxy("acquireWakeLock", 2) {
			@Override
			public Object call(Object who, Method method, Object... args) throws Throwable {
				try {
					return super.call(who, method, args);
				} catch (InvocationTargetException e) {
					return onHandleError(e);
				}
			}
		});
		addMethodProxy(new ReplaceLastPkgMethodProxy("acquireWakeLockWithUid") {

			@Override
			public Object call(Object who, Method method, Object... args) throws Throwable {
				try {
					return super.call(who, method, args);
				} catch (InvocationTargetException e) {
					return onHandleError(e);
				}
			}
		});
		addMethodProxy(new ResultStaticMethodProxy("updateWakeLockWorkSource", 0));
	}

	private Object onHandleError(InvocationTargetException e) throws Throwable {
		if (e.getCause() instanceof SecurityException) {
			return 0;
		}
		throw e.getCause();
	}
}
