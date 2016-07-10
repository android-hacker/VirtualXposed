package com.lody.virtual.client.hook.patchs.clipboard;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookClipboardBinder;

import android.content.Context;
import android.content.IClipboard;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 * @see IClipboard
 */
@Patch({Hook_SetPrimaryClip.class, Hook_GetPrimaryClip.class, Hook_HasPrimaryClip.class,
		Hook_GetPrimaryDescription.class, Hook_HasClipboardText.class, Hook_AddPrimaryClipChangedListener.class,

})
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
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.CLIPBOARD_SERVICE) != getHookObject();
	}

}
