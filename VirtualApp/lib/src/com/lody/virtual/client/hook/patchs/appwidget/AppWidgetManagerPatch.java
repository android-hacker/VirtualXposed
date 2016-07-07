package com.lody.virtual.client.hook.patchs.appwidget;

import com.android.internal.appwidget.IAppWidgetService;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookAppWidgetBinder;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 *         Hook桌面小组件服务
 *
 * @see IAppWidgetService
 * @see android.appwidget.AppWidgetManager
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@Patch({Hook_IsBoundWidgetPackage.class, Hook_BindRemoteViewsService.class, Hook_UnbindRemoteViewsService.class,
		Hook_GetAppWidgetIds.class})
public class AppWidgetManagerPatch extends PatchObject<IAppWidgetService, HookAppWidgetBinder> {

	@Override
	protected HookAppWidgetBinder initHookObject() {
		return new HookAppWidgetBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.APPWIDGET_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.APPWIDGET_SERVICE) != getHookObject();
	}
}
