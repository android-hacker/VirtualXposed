package com.lody.virtual.client.hook.patchs.dropbox;

import com.android.internal.os.IDropBoxManagerService;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ResultStaticHook;
import com.lody.virtual.client.hook.binders.HookDropBoxBinder;

import android.content.Context;
import android.os.ServiceManager;

/**
 * @author Lody
 */
public class DropBoxManagerPatch extends PatchObject<IDropBoxManagerService, HookDropBoxBinder> {

	@Override
	protected HookDropBoxBinder initHookObject() {
		return new HookDropBoxBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.DROPBOX_SERVICE);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new ResultStaticHook("getNextEntry", null));
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.DROPBOX_SERVICE);
	}
}
