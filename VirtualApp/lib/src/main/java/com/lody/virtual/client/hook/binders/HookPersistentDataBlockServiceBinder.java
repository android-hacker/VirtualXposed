package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.os.IBinder;
import android.os.ServiceManager;
import android.service.persistentdata.IPersistentDataBlockService;

/**
 * @author Lody
 */

public class HookPersistentDataBlockServiceBinder extends HookBinder<IPersistentDataBlockService> {

	public static final String SERVICE_NAME = "persistent_data_block";

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(SERVICE_NAME);
	}

	@Override
	protected IPersistentDataBlockService createInterface(IBinder baseBinder) {
		return IPersistentDataBlockService.Stub.asInterface(baseBinder);
	}
}
