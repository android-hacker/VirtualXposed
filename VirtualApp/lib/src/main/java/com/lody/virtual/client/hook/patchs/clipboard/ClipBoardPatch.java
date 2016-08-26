package com.lody.virtual.client.hook.patchs.clipboard;

import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.binders.ClipboardBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 */
public class ClipBoardPatch extends PatchDelegate<ClipboardBinderDelegate> {
	@Override
	protected ClipboardBinderDelegate createHookDelegate() {
		return new ClipboardBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.CLIPBOARD_SERVICE);
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
	public boolean isEnvBad() {
		return ServiceManager.getService.call(Context.CLIPBOARD_SERVICE) != getHookDelegate();
	}

}
