package com.lody.virtual.client.hook.patchs.appwidget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ResultStaticHook;

import mirror.com.android.internal.appwidget.IAppWidgetService;

/**
 * @author Lody
 *
 * @see android.appwidget.AppWidgetManager
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AppWidgetManagerPatch extends PatchBinderDelegate {

	public AppWidgetManagerPatch() {
		super(IAppWidgetService.Stub.TYPE, Context.APPWIDGET_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ResultStaticHook("startListening", new int[0]));
		addHook(new ResultStaticHook("stopListening", 0));
		addHook(new ResultStaticHook("allocateAppWidgetId", 0));
		addHook(new ResultStaticHook("deleteAppWidgetId", 0));
		addHook(new ResultStaticHook("deleteHost", 0));
		addHook(new ResultStaticHook("deleteAllHosts", 0));
		addHook(new ResultStaticHook("getAppWidgetViews", null));
		addHook(new ResultStaticHook("getAppWidgetIdsForHost", null));
		addHook(new ResultStaticHook("createAppWidgetConfigIntentSender", null));
		addHook(new ResultStaticHook("updateAppWidgetIds", 0));
		addHook(new ResultStaticHook("updateAppWidgetOptions", 0));
		addHook(new ResultStaticHook("getAppWidgetOptions", null));
		addHook(new ResultStaticHook("partiallyUpdateAppWidgetIds", 0));
		addHook(new ResultStaticHook("updateAppWidgetProvider", 0));
		addHook(new ResultStaticHook("notifyAppWidgetViewDataChanged", 0));
		addHook(new ResultStaticHook("getInstalledProvidersForProfile", null));
		addHook(new ResultStaticHook("getAppWidgetInfo", null));
		addHook(new ResultStaticHook("hasBindAppWidgetPermission", false));
		addHook(new ResultStaticHook("setBindAppWidgetPermission", 0));
		addHook(new ResultStaticHook("bindAppWidgetId", false));
		addHook(new ResultStaticHook("bindRemoteViewsService", 0));
		addHook(new ResultStaticHook("unbindRemoteViewsService", 0));
		addHook(new ResultStaticHook("getAppWidgetIds", new int[0]));
		addHook(new ResultStaticHook("isBoundWidgetPackage", false));
	}
}
