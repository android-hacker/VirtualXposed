package com.lody.virtual.client.hook.binders;

import com.android.internal.app.IAppOpsService;
import com.lody.virtual.client.hook.base.HookBinder;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class HookAppOpsBinder extends HookBinder<IAppOpsService> {

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.APP_OPS_SERVICE);
	}

	@Override
	protected IAppOpsService createInterface(IBinder baseBinder) {
		return IAppOpsService.Stub.asInterface(baseBinder);
	}
}
