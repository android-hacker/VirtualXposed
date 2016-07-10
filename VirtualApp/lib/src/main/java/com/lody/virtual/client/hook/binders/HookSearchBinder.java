package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.app.ISearchManager;
import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class HookSearchBinder extends HookBinder<ISearchManager> {

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.SEARCH_SERVICE);
	}

	@Override
	protected ISearchManager createInterface(IBinder baseBinder) {
		return ISearchManager.Stub.asInterface(baseBinder);
	}
}
