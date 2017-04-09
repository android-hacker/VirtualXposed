package com.lody.virtual.client.hook.proxies.mount;

import com.lody.virtual.client.hook.base.Inject;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;

import mirror.android.os.mount.IMountService;

/**
 * @author Lody
 */
@Inject(MethodProxies.class)
public class MountServiceStub extends BinderInvocationProxy {

	public MountServiceStub() {
		super(IMountService.Stub.asInterface, "mount");
	}
}
