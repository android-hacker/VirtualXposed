package com.lody.virtual.client.hook.patchs.search;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.StaticHook;
import com.lody.virtual.client.hook.binders.SearchBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
@Patch({GetSearchableInfo.class,})
public class SearchManagerPatch extends PatchDelegate<SearchBinderDelegate> {
	@Override
	protected SearchBinderDelegate createHookDelegate() {
		return new SearchBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.SEARCH_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new StaticHook("launchLegacyAssist"));
	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate() != ServiceManager.getService.call(Context.SEARCH_SERVICE);
	}
}
