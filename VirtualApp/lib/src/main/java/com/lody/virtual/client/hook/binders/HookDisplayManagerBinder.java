package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.IDisplayManager;
import android.os.Build;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 *         API 17后加入
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class HookDisplayManagerBinder extends HookBinder<IDisplayManager> {
	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.DISPLAY_SERVICE);
	}

	@Override
	protected IDisplayManager createInterface(IBinder baseBinder) {
		return IDisplayManager.Stub.asInterface(baseBinder);
	}
}
