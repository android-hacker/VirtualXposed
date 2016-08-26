package com.lody.virtual.client.hook.binders;

import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.os.ServiceManager;
import mirror.android.os.mount.IMountService;

/**
 * @author Lody
 *
 */
public class MountServiceBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call("mount");
		return IMountService.Stub.asInterface.call(binder);
	}
}
