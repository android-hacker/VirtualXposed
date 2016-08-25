package com.lody.virtual.client.hook.binders;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.os.ServiceManager;
import mirror.com.android.internal.appwidget.IAppWidgetService;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AppWidgetBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call(Context.APPWIDGET_SERVICE);
		return IAppWidgetService.Stub.asInterface.call(binder);
	}
}
