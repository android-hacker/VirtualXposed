package com.lody.virtual.client.hook.proxies.appwidget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ResultStaticMethodProxy;

import mirror.com.android.internal.appwidget.IAppWidgetService;

/**
 * @author Lody
 *
 * @see android.appwidget.AppWidgetManager
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AppWidgetManagerStub extends BinderInvocationProxy {

	public AppWidgetManagerStub() {
		super(IAppWidgetService.Stub.asInterface, Context.APPWIDGET_SERVICE);
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new ResultStaticMethodProxy("startListening", new int[0]));
		addMethodProxy(new ResultStaticMethodProxy("stopListening", 0));
		addMethodProxy(new ResultStaticMethodProxy("allocateAppWidgetId", 0));
		addMethodProxy(new ResultStaticMethodProxy("deleteAppWidgetId", 0));
		addMethodProxy(new ResultStaticMethodProxy("deleteHost", 0));
		addMethodProxy(new ResultStaticMethodProxy("deleteAllHosts", 0));
		addMethodProxy(new ResultStaticMethodProxy("getAppWidgetViews", null));
		addMethodProxy(new ResultStaticMethodProxy("getAppWidgetIdsForHost", null));
		addMethodProxy(new ResultStaticMethodProxy("createAppWidgetConfigIntentSender", null));
		addMethodProxy(new ResultStaticMethodProxy("updateAppWidgetIds", 0));
		addMethodProxy(new ResultStaticMethodProxy("updateAppWidgetOptions", 0));
		addMethodProxy(new ResultStaticMethodProxy("getAppWidgetOptions", null));
		addMethodProxy(new ResultStaticMethodProxy("partiallyUpdateAppWidgetIds", 0));
		addMethodProxy(new ResultStaticMethodProxy("updateAppWidgetProvider", 0));
		addMethodProxy(new ResultStaticMethodProxy("notifyAppWidgetViewDataChanged", 0));
		addMethodProxy(new ResultStaticMethodProxy("getInstalledProvidersForProfile", null));
		addMethodProxy(new ResultStaticMethodProxy("getAppWidgetInfo", null));
		addMethodProxy(new ResultStaticMethodProxy("hasBindAppWidgetPermission", false));
		addMethodProxy(new ResultStaticMethodProxy("setBindAppWidgetPermission", 0));
		addMethodProxy(new ResultStaticMethodProxy("bindAppWidgetId", false));
		addMethodProxy(new ResultStaticMethodProxy("bindRemoteViewsService", 0));
		addMethodProxy(new ResultStaticMethodProxy("unbindRemoteViewsService", 0));
		addMethodProxy(new ResultStaticMethodProxy("getAppWidgetIds", new int[0]));
		addMethodProxy(new ResultStaticMethodProxy("isBoundWidgetPackage", false));
	}
}
