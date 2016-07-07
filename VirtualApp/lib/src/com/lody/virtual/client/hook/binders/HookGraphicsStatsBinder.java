package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.os.IBinder;
import android.os.ServiceManager;
import android.view.IGraphicsStats;

/**
 * @author Lody
 *
 */
public class HookGraphicsStatsBinder extends HookBinder<IGraphicsStats> {

	public static final String SERVICE_NAME = "graphicsstats";

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(SERVICE_NAME);
	}

	@Override
	protected IGraphicsStats createInterface(IBinder baseBinder) {
		return IGraphicsStats.Stub.asInterface(baseBinder);
	}
}
