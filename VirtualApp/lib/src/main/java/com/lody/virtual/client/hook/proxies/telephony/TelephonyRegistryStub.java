package com.lody.virtual.client.hook.proxies.telephony;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.client.hook.base.ReplaceSequencePkgMethodProxy;

import mirror.com.android.internal.telephony.ITelephonyRegistry;

public class TelephonyRegistryStub extends BinderInvocationProxy {

	public TelephonyRegistryStub() {
		super(ITelephonyRegistry.Stub.asInterface, "telephony.registry");
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ReplaceCallingPkgMethodProxy("listen"));
		addMethodProxy(new ReplaceSequencePkgMethodProxy("listenForSubscriber", 1));
	}
}
