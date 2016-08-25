package com.lody.virtual.client.hook.binders;

import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.os.ServiceManager;
import mirror.android.service.persistentdata.IPersistentDataBlockService;

/**
 * @author Lody
 */

public class PersistentDataBlockServiceBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call("persistent_data_block");
		return IPersistentDataBlockService.Stub.asInterface.call(binder);
	}
}
