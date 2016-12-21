package com.lody.virtual.client.hook.patchs.search;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.StaticHook;

import mirror.android.app.ISearchManager;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
@Patch({GetSearchableInfo.class,})
public class SearchManagerPatch extends PatchBinderDelegate {

	public SearchManagerPatch() {
		super(ISearchManager.Stub.TYPE, Context.SEARCH_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new StaticHook("launchLegacyAssist"));
	}
}
