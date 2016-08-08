package com.lody.virtual.client.hook.patchs.clipboard;

import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.binders.HookClipboardBinder;

import android.content.Context;
import android.content.IClipboard;
import android.os.Build;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 * @see IClipboard
 */
public class ClipBoardPatch extends PatchObject<IClipboard, HookClipboardBinder> {
	@Override
	protected HookClipboardBinder initHookObject() {
		return new HookClipboardBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.CLIPBOARD_SERVICE);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new ReplaceLastPkgHook("getPrimaryClip"));
		if (Build.VERSION.SDK_INT > 17) {
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
		return ServiceManager.getService(Context.CLIPBOARD_SERVICE) != getHookObject();
	}

}
