package com.lody.virtual.client.hook.binders;

import com.android.internal.appwidget.IAppWidgetService;
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
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class HookAppWidgetBinder extends HookBinder<IAppWidgetService> {

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.APPWIDGET_SERVICE);
	}

	@Override
	protected IAppWidgetService createInterface(IBinder baseBinder) {
		return IAppWidgetService.Stub.asInterface(baseBinder);
	}
}
