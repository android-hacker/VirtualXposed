package com.lody.virtual.client.hook.base;

import android.os.IBinder;
import android.os.IInterface;

import mirror.android.os.ServiceManager;

/**
 * @author Paulo Costa
 *
 * @see PatchDelegate
 */
public abstract class PatchBinderDelegate extends PatchDelegate<HookBinderDelegate> {

	protected String serviceName;

	public PatchBinderDelegate(IInterface stub, String serviceName) {
		this(new HookBinderDelegate(stub), serviceName);
	}

	public PatchBinderDelegate(Class<?> stubClass, String serviceName) {
		this(new HookBinderDelegate(stubClass, ServiceManager.getService.call(serviceName)), serviceName);
	}

	public PatchBinderDelegate(HookBinderDelegate hookDelegate, String serviceName) {
		super(hookDelegate);
		this.serviceName = serviceName;
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(serviceName);
	}

	@Override
	public boolean isEnvBad() {
		IBinder binder = ServiceManager.getService.call(serviceName);
		return binder != null && getHookDelegate() != binder;
	}
}
