package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import android.view.IWindowManager;

/**
 * @author Lody
 *
 */
public class HookWindowManagerBinder extends HookBinder<IWindowManager> {
	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.WINDOW_SERVICE);
	}

	@Override
	protected IWindowManager createInterface(IBinder baseBinder) {
		return IWindowManager.Stub.asInterface(baseBinder);
	}
}
