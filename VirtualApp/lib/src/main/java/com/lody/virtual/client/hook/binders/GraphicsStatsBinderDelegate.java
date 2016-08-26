package com.lody.virtual.client.hook.binders;


import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.os.ServiceManager;
import mirror.android.view.IGraphicsStats;

/**
 * @author Lody
 *
 */
public class GraphicsStatsBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call("graphicsstats");
		if (binder != null) {
			return IGraphicsStats.Stub.asInterface.call(binder);
		}
		return null;
	}
}
