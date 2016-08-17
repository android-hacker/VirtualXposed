package com.lody.virtual.client.hook.patchs.search;

import android.annotation.TargetApi;
import android.app.ISearchManager;
import android.content.Context;
import android.os.Build;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.StaticHook;
import com.lody.virtual.client.hook.binders.HookSearchBinder;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
@Patch({Hook_GetSearchableInfo.class,})
public class SearchManagerPatch extends PatchObject<ISearchManager, HookSearchBinder> {
	@Override
	protected HookSearchBinder initHookObject() {
		return new HookSearchBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.SEARCH_SERVICE);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new StaticHook("launchLegacyAssist")).replaceUid(1);
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.SEARCH_SERVICE);
	}
}
