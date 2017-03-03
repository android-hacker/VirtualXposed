package com.lody.virtual.client.hook.patchs.clipboard;

import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;

import mirror.android.content.ClipboardManager;

/**
 * @author Lody
 *
 * @see ClipboardManager
 */
public class ClipBoardPatch extends PatchBinderDelegate {

	public ClipBoardPatch() {
		super(ClipboardManager.getService.call(), Context.CLIPBOARD_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ReplaceLastPkgHook("getPrimaryClip"));
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
			addHook(new ReplaceLastPkgHook("setPrimaryClip"));
			addHook(new ReplaceLastPkgHook("getPrimaryClipDescription"));
			addHook(new ReplaceLastPkgHook("hasPrimaryClip"));
			addHook(new ReplaceLastPkgHook("addPrimaryClipChangedListener"));
			addHook(new ReplaceLastPkgHook("removePrimaryClipChangedListener"));
			addHook(new ReplaceLastPkgHook("hasClipboardText"));
		}
	}

	@Override
	public void inject() throws Throwable {
		super.inject();
		ClipboardManager.sService.set(getHookDelegate().getProxyInterface());
	}
}
